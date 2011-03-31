/*
 * Created on Aug 11, 2004
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.codecompletion;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.python.pydev.core.ExtensionHelper;
import org.python.pydev.core.FullRepIterable;
import org.python.pydev.core.ICallback;
import org.python.pydev.core.ICodeCompletionASTManager;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.ILocalScope;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IToken;
import org.python.pydev.core.ICodeCompletionASTManager.ImportInfo;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.core.log.Log;
import org.python.pydev.core.structure.CompletionRecursionException;
import org.python.pydev.core.structure.FastStack;
import org.python.pydev.editor.codecompletion.revisited.ASTManager;
import org.python.pydev.editor.codecompletion.revisited.AssignAnalysis;
import org.python.pydev.editor.codecompletion.revisited.CompletionState;
import org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule;
import org.python.pydev.editor.codecompletion.revisited.modules.CompiledModule;
import org.python.pydev.editor.codecompletion.revisited.visitors.FindScopeVisitor;
import org.python.pydev.editor.codecompletion.shell.AbstractShell;
import org.python.pydev.logging.DebugSettings;
import org.python.pydev.parser.PyParser;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.visitors.NodeUtils;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.NotConfiguredInterpreterException;

/**
 * @author Dmoore
 * @author Fabio Zadrozny
 */
public class PyCodeCompletion extends AbstractPyCodeCompletion {

    
    /**
     * Called when a recursion exception is detected.
     */
    public static ICallback<Object, CompletionRecursionException> onCompletionRecursionException;
    
    /* (non-Javadoc)
     * @see org.python.pydev.editor.codecompletion.IPyCodeCompletion#getCodeCompletionProposals(org.eclipse.jface.text.ITextViewer, org.python.pydev.editor.codecompletion.CompletionRequest)
     */
    @SuppressWarnings("unchecked")
    public List getCodeCompletionProposals(ITextViewer viewer, CompletionRequest request) throws CoreException, BadLocationException {
        if(request.getPySelection().getCursorLineContents().trim().startsWith("#")){
            //this may happen if the context is still not correctly computed in python
            return new PyStringCodeCompletion().getCodeCompletionProposals(viewer, request);
        }
        if(DebugSettings.DEBUG_CODE_COMPLETION){
            Log.toLogFile(this,"Starting getCodeCompletionProposals");
            Log.addLogLevel();
            Log.toLogFile(this,"Request:"+request);
        }
        
        ArrayList<ICompletionProposal> ret = new ArrayList<ICompletionProposal>();
        
        //let's see if we should do a code-completion in the current scope...
        if(!isValidCompletionContext(request)){
            request.showTemplates = false;
            return ret;
        }
        
        try {
            IPythonNature pythonNature = request.nature;
            checkPythonNature(pythonNature);
            
            ICodeCompletionASTManager astManager = pythonNature.getAstManager();
            if (astManager == null) { 
                //we're probably still loading it.
                return ret;
            }
            
            //list of Object[], IToken or ICompletionProposal
            List<Object> tokensList = new ArrayList<Object>();
            try {
                lazyStartShell(request);
            } catch (NotConfiguredInterpreterException e) {
                Log.log(IStatus.WARNING, "Warning: unable to get code-completion for builtins: No interpreter configured.", null);
            }
            String trimmed = request.activationToken.replace('.', ' ').trim();

            ImportInfo importsTipper = getImportsTipperStr(request);

            int line = request.doc.getLineOfOffset(request.documentOffset);
            IRegion region = request.doc.getLineInformation(line);

            ICompletionState state = new CompletionState(line, request.documentOffset - region.getOffset(), null, request.nature, request.qualifier);
            state.setIsInCalltip(request.isInCalltip);

            boolean importsTip = false;
            
            if (importsTipper.importsTipperStr.length() != 0) {
                //code completion in imports 
                request.isInCalltip = false; //if found after (, but in an import, it is not a calltip!
                importsTip = doImportCompletion(request, astManager, tokensList, importsTipper);

            } else if (trimmed.length() > 0 && request.activationToken.indexOf('.') != -1) {
                //code completion for a token
                doTokenCompletion(request, astManager, tokensList, trimmed, state);

            } else { 
                //go to globals
                doGlobalsCompletion(request, astManager, tokensList, state);
            }

            Map<String, IToken> alreadyChecked = new HashMap<String, IToken>();
            
            String lowerCaseQual = request.qualifier.toLowerCase();
            if(lowerCaseQual.length() >= PyCodeCompletionPreferencesPage.getArgumentsDeepAnalysisNChars()){
                //this can take some time on the analysis, so, let's let the user choose on how many chars does he
                //want to do the analysis...
                state.pushFindResolveImportMemoryCtx();
                try{
                    for(Iterator<Object> it=tokensList.listIterator(); it.hasNext();){
                        Object o = it.next();
                        if(o instanceof IToken){
                            it.remove(); // always remove the tokens from the list (they'll be re-added later once they are filtered)
                            
                            IToken initialToken = (IToken) o;
                            
                            IToken token = initialToken;
                            String strRep = token.getRepresentation();
                            IToken prev = alreadyChecked.get(strRep);
                            
                            if(prev != null){
                                if(prev.getArgs().length() != 0){
                                    continue; // we already have a version with args... just keep going
                                }
                            }
                            
                            if(!strRep.toLowerCase().startsWith(lowerCaseQual)){
                                //just re-add it if we're going to actually use it (depending on the qualifier)
                                continue;
                            }
                            
                            while(token.isImportFrom()){
                                //we'll only add it here if it is an import from (so, set the flag to false for the outer add)
                                
                                if(token.getArgs().length() > 0){
                                    //if we already have the args, there's also no reason to do it (that's what we'll do here)
                                    break;
                                }
                                ICompletionState s = state.getCopyForResolveImportWithActTok(token.getRepresentation());
                                s.checkFindResolveImportMemory(token);
                                
                                IToken token2 = astManager.resolveImport(s, token);
                                if(token2 != null && initialToken != token2){
                                    String args = token2.getArgs();
                                    if(args.length() > 0){
                                        //put it into the map (may override previous if it didn't have args)
                                        initialToken.setArgs(args);
                                        initialToken.setDocStr(token2.getDocStr());
                                        break;
                                    }
                                    if(token2 == null || 
                                           (token2.equals(token) && 
                                            token2.getArgs().equals(token.getArgs()) && 
                                            token2.getParentPackage().equals(token.getParentPackage()))){
                                        
                                        token2.equals(token);
                                        break;
                                    }
                                    token = token2;
                                }else{
                                    break;
                                }
                            }
                            
                            alreadyChecked.put(strRep, initialToken);
                        }
                    }
    
                }finally{
                    state.popFindResolveImportMemoryCtx();
                }
            }
            
            tokensList.addAll(alreadyChecked.values());
            changeItokenToCompletionPropostal(viewer, request, ret, tokensList, importsTip, state);
        } catch (CompletionRecursionException e) {
            if(onCompletionRecursionException != null){
                onCompletionRecursionException.call(e);
            }
            if(DebugSettings.DEBUG_CODE_COMPLETION){
                Log.toLogFile(e);
            }
            //PydevPlugin.log(e);
            //ret.add(new CompletionProposal("",request.documentOffset,0,0,null,e.getMessage(), null,null));
        }
        
        if(DebugSettings.DEBUG_CODE_COMPLETION){
            Log.remLogLevel();
            Log.toLogFile(this, "Finished completion. Returned:"+ret.size()+" completions.\r\n");
        }

        return ret;
    }

    /**
     * @return whether we're currently in a valid context for a code-completion request for this engine.
     */
    private boolean isValidCompletionContext(CompletionRequest request) {
        //this engine does not work 'correctly' in the default scope on: 
        //- class definitions - after 'class' and before '('
        //- method definitions - after 'def' and before '(' 
        PySelection ps = request.getPySelection();
        if(ps.isInDeclarationLine() != PySelection.DECLARATION_NONE){
            return false;
        }
        return true;
    }

    /**
     * Does a code-completion that will retrieve the globals in the module
     */
    private void doGlobalsCompletion(CompletionRequest request, ICodeCompletionASTManager astManager, List<Object> tokensList, ICompletionState state) throws CompletionRecursionException {
        state.setActivationToken(request.activationToken);
        if(DebugSettings.DEBUG_CODE_COMPLETION){
            Log.toLogFile(this,"astManager.getCompletionsForToken");
            Log.addLogLevel();
        }
        IToken[] comps = astManager.getCompletionsForToken(request.editorFile, request.doc, state);
        if(DebugSettings.DEBUG_CODE_COMPLETION){
            Log.remLogLevel();
            Log.toLogFile(this,"END astManager.getCompletionsForToken");
        }

        tokensList.addAll(Arrays.asList(comps));
        
        tokensList.addAll(getGlobalsFromParticipants(request, state));
    }

    /**
     * Does a code-completion that will retrieve the all matches for some token in the module
     */
    private void doTokenCompletion(CompletionRequest request, ICodeCompletionASTManager astManager, List<Object> tokensList, String trimmed, ICompletionState state) throws CompletionRecursionException {
        if (request.activationToken.endsWith(".")) {
            request.activationToken = request.activationToken.substring(0, request.activationToken.length() - 1);
        }
        
        char[] toks = new char[]{'.', ' '};
        List<Object> completions = new ArrayList<Object>();
        
        boolean lookInGlobals = true;
        
        if (trimmed.equals("self") || FullRepIterable.getFirstPart(trimmed, toks).equals("self")) {
            lookInGlobals = !getSelfOrClsCompletions(request, tokensList, state, false, true, "self");
            
        }else if (trimmed.equals("cls") || FullRepIterable.getFirstPart(trimmed, toks).equals("cls")) { 
            lookInGlobals = !getSelfOrClsCompletions(request, tokensList, state, false, true, "cls");

        } 
        
        if(lookInGlobals){
            state.setActivationToken(request.activationToken);

            //Ok, looking for a token in globals.
            IToken[] comps = astManager.getCompletionsForToken(request.editorFile, request.doc, state);
            tokensList.addAll(Arrays.asList(comps));
        }
        tokensList.addAll(completions);
    }

    /**
     * Does a code-completion that will check for imports
     */
    private boolean doImportCompletion(CompletionRequest request, ICodeCompletionASTManager astManager, List<Object> tokensList, ImportInfo importsTipper) throws CompletionRecursionException {
        boolean importsTip;
        //get the project and make the code completion!!
        //so, we want to do a code completion for imports...
        //let's see what we have...

        importsTip = true;
        importsTipper.importsTipperStr = importsTipper.importsTipperStr.trim();
        IToken[] imports = astManager.getCompletionsForImport(importsTipper, request);
        tokensList.addAll(Arrays.asList(imports));
        return importsTip;
    }

    /**
     * Checks if the python nature is valid
     */
    private void checkPythonNature(IPythonNature pythonNature) {
        if (pythonNature == null) {
            throw new RuntimeException("Unable to get python nature.");
        }
    }

    /**
     * Pre-initializes the shell (NOT in a thread, as we may need it shortly, so, no use in putting it into a thread)
     */
    private void lazyStartShell(CompletionRequest request) {
        try {
            if(DebugSettings.DEBUG_CODE_COMPLETION){
                Log.toLogFile(this,"AbstractShell.getServerShell");
            }
            if (CompiledModule.COMPILED_MODULES_ENABLED) {
                AbstractShell.getServerShell(request.nature, AbstractShell.COMPLETION_SHELL); //just start it
            }
            if(DebugSettings.DEBUG_CODE_COMPLETION){
                Log.toLogFile(this,"END AbstractShell.getServerShell");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return completions added from contributors
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> getGlobalsFromParticipants(CompletionRequest request, ICompletionState state) {
        ArrayList ret = new ArrayList();
        
        List participants = ExtensionHelper.getParticipants(ExtensionHelper.PYDEV_COMPLETION);
        for (Iterator iter = participants.iterator(); iter.hasNext();) {
            IPyDevCompletionParticipant participant = (IPyDevCompletionParticipant) iter.next();
            ret.addAll(participant.getGlobalCompletions(request, state));
        }
        return ret;
    }

    
    /**
     * @param request this is the request for the completion
     * @param theList OUT - returned completions are added here. (IToken instances)
     * @param getOnlySupers whether we should only get things from super classes (in this case, we won't get things from the current class)
     * @param checkIfInCorrectScope if true, we'll first check if we're in a scope that actually has a method with 'self' or 'cls'
     * 
     * @return true if we actually tried to get the completions for self or cls.
     */
    @SuppressWarnings("unchecked")
    public static boolean getSelfOrClsCompletions(CompletionRequest request, List theList, ICompletionState state, 
            boolean getOnlySupers, boolean checkIfInCorrectScope, String lookForRep) {
        
        SimpleNode s = PyParser.reparseDocument(new PyParser.ParserInfo(request.doc, true, request.nature, state.getLine())).o1;
        if(s != null){
            FindScopeVisitor visitor = new FindScopeVisitor(state.getLine(), 0);
            try {
                s.accept(visitor);
                if(checkIfInCorrectScope){
                    boolean scopeCorrect = false;
                    
                    FastStack<SimpleNode> scopeStack = visitor.scope.getScopeStack();
                    for(Iterator<SimpleNode> it=scopeStack.topDownIterator();scopeCorrect == false && it.hasNext();){
                        SimpleNode node = it.next();
                        if (node instanceof FunctionDef) {
                            FunctionDef funcDef = (FunctionDef) node;
                            if(funcDef.args != null && funcDef.args.args != null && funcDef.args.args.length > 0){
                                //ok, we have some arg, let's check for self or cls
                                String rep = NodeUtils.getRepresentationString(funcDef.args.args[0]);
                                if(rep != null && (rep.equals("self") || rep.equals("cls"))){
                                    scopeCorrect = true;
                                }
                            }
                        }
                    }
                    if(!scopeCorrect){
                        return false;
                    }
                }
                if(lookForRep.equals("self")){
                    state.setLookingFor(ICompletionState.LOOKING_FOR_INSTANCED_VARIABLE);
                }else{
                    state.setLookingFor(ICompletionState.LOOKING_FOR_CLASSMETHOD_VARIABLE);
                }
                getSelfOrClsCompletions(visitor.scope, request, theList, state, getOnlySupers);
            } catch (Exception e1) {
                PydevPlugin.log(e1);
            }
            return true;
        }
        return false;
    }

    /**
     * Get self completions when you already have a scope
     */
    @SuppressWarnings("unchecked")
    public static void getSelfOrClsCompletions(ILocalScope scope, CompletionRequest request, List theList, ICompletionState state, boolean getOnlySupers) throws BadLocationException {
        for(Iterator<SimpleNode> it = scope.iterator(); it.hasNext();){
            SimpleNode node = it.next();
            if(node instanceof ClassDef){
                ClassDef d = (ClassDef) node;
                
                if(getOnlySupers){
                    List gottenComps = new ArrayList();
                    for (int i = 0; i < d.bases.length; i++) {
                        if(d.bases[i] instanceof Name){
                            Name n = (Name) d.bases[i];
                            state.setActivationToken(n.id);
                            IToken[] completions;
                            try {
                                completions = request.nature.getAstManager().getCompletionsForToken(request.editorFile, request.doc, state);
                                gottenComps.addAll(Arrays.asList(completions));
                            } catch (CompletionRecursionException e) {
                                //ok...
                            }
                        }
                    }
                    theList.addAll(gottenComps);
                }else{
                    //ok, get the completions for the class, only thing we have to take care now is that we may 
                    //not have only 'self' for completion, but somthing lile self.foo.
                    //so, let's analyze our activation token to see what should we do.
                    
                    String trimmed = request.activationToken.replace('.', ' ').trim();
                    String[] actTokStrs = trimmed.split(" ");
                    if(actTokStrs.length == 0 || (!actTokStrs[0].equals("self")&& !actTokStrs[0].equals("cls")) ){
                        throw new AssertionError("We need to have at least one token (self or cls) for doing completions in the class.");
                    }
                    
                    if(actTokStrs.length == 1){
                        //ok, it's just really self, let's get on to get the completions
                        state.setActivationToken(NodeUtils.getNameFromNameTok((NameTok) d.name));
                        try {
                            theList.addAll(Arrays.asList(request.nature.getAstManager().getCompletionsForToken(request.editorFile, request.doc, state)));
                        } catch (CompletionRecursionException e) {
                            //ok
                        }
                        
                    }else{
                        //it's not only self, so, first we have to get the definition of the token
                        //the first one is self, so, just discard it, and go on, token by token to know what is the last 
                        //one we are completing (e.g.: self.foo.bar)
                        int line = request.doc.getLineOfOffset(request.documentOffset);
                        IRegion region = request.doc.getLineInformationOfOffset(request.documentOffset);
                        int col =  request.documentOffset - region.getOffset();
                        
                        
                        //ok, try our best shot at getting the module name of the current buffer used in the request.
                        String modName = "";
                        File requestFile = request.editorFile;
                        if(request.editorFile != null){
                            String resolveModule = request.nature.resolveModule(requestFile);
                            if(resolveModule != null){
                                modName = resolveModule;
                            }
                        }
                        
                        IModule module = AbstractModule.createModuleFromDoc(modName, requestFile, request.doc, request.nature, line);
                      
                        ASTManager astMan = ((ASTManager)request.nature.getAstManager());
                        theList.addAll(new AssignAnalysis().getAssignCompletions(astMan, module, new CompletionState(line, col, request.activationToken, request.nature, request.qualifier)));
                    }
                }
            }
        }
    }


}