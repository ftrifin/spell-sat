/*
 * Created on Nov 18, 2004
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.codecompletion.revisited.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.Document;
import org.python.pydev.core.ExtensionHelper;
import org.python.pydev.core.FullRepIterable;
import org.python.pydev.core.ICodeCompletionASTManager;
import org.python.pydev.core.ICompletionCache;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IToken;
import org.python.pydev.core.REF;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.cache.LRUCache;
import org.python.pydev.editor.codecompletion.revisited.CompletionStateFactory;
import org.python.pydev.editor.codecompletion.revisited.visitors.Definition;
import org.python.pydev.editor.codecompletion.shell.AbstractShell;
import org.python.pydev.plugin.PydevPlugin;

/**
 * @author Fabio Zadrozny
 */
public class CompiledModule extends AbstractModule{
    
    public static boolean COMPILED_MODULES_ENABLED = true; 

    public static final boolean TRACE_COMPILED_MODULES = false; 
    
    private Map<String, Map<String, IToken> > cache = new HashMap<String, Map<String, IToken>>();
    
    private static final Definition[] EMPTY_DEFINITION = new Definition[0];
    
    /**
     * These are the tokens the compiled module has.
     */
    private Map<String, CompiledToken> tokens = null;
    
    /**
     * A map with the definitions that have already been found for this compiled module.
     */
    private LRUCache<String, Definition[]> definitionsFoundCache = new LRUCache<String, Definition[]>(30);
    
    private File file;
    
    @Override
    public File getFile() {
        return file;
    }

    /**
     * 
     * @param module - module from where to get completions.
     */
    public CompiledModule(String name, ICodeCompletionASTManager manager){
        this(name, IToken.TYPE_BUILTIN, manager);
    }

    /**
     * 
     * @param module - module from where to get completions.
     */
    @SuppressWarnings("unchecked")
    public CompiledModule(String name, int tokenTypes, ICodeCompletionASTManager manager){
        super(name);
        if(COMPILED_MODULES_ENABLED){
            try {
                setTokens(name, manager);
            } catch (Exception e) {
                //ok, something went wrong... let's give it another shot...
                synchronized (this) {
                    try {
                        wait(10);
                    } catch (InterruptedException e1) {
                        //empty block
                    } //just wait a little before a retry...
                }
                
                try {
                    AbstractShell shell = AbstractShell.getServerShell(manager.getNature(), 
                            AbstractShell.COMPLETION_SHELL);
                    synchronized(shell){
                        shell.clearSocket();
                    }
                    setTokens(name, manager);
                } catch (Exception e2) {
                    tokens = new HashMap<String, CompiledToken>();
                    e2.printStackTrace();
                    PydevPlugin.log(e2);
                }
            }
            if(tokens != null && tokens.size() > 0){
                List<IModulesObserver> participants = ExtensionHelper.getParticipants(ExtensionHelper.PYDEV_MODULES_OBSERVER);
                for (IModulesObserver observer : participants) {
                    observer.notifyCompiledModuleCreated(this, manager);
                }
            }
        }else{
            //not used if not enabled.
            tokens = new HashMap<String, CompiledToken>();
        }

    }

    private void setTokens(String name, ICodeCompletionASTManager manager) throws IOException, Exception, CoreException {
        if(TRACE_COMPILED_MODULES){
            PydevPlugin.log(IStatus.INFO, "Compiled modules: getting info for:"+name, null);
        }
        final IPythonNature nature = manager.getNature();
        AbstractShell shell = AbstractShell.getServerShell(nature, AbstractShell.COMPLETION_SHELL);
        synchronized(shell){
            Tuple<String, List<String[]>> completions = shell.getImportCompletions(name, 
                    manager.getModulesManager().getCompletePythonPath(nature.getProjectInterpreter(), 
                            nature.getRelatedInterpreterManager())); //default
            
            String fPath = completions.o1;
            if(!fPath.equals("None")){
                this.file = new File(fPath);
            }

            String f = fPath;
            if(f.toLowerCase().endsWith(".pyc")){
                f = f.substring(0, f.length()-1); //remove the c from pyc
                File f2 = new File(f);
                if(f2.exists()){
                    this.file = f2;
                }
            }
            ArrayList<IToken> array = new ArrayList<IToken>();
            
            for (Iterator<String[]> iter = completions.o2.iterator(); iter.hasNext();) {
                String[] element = iter.next();
                //let's make this less error-prone.
                try {
                    String o1 = element[0]; //this one is really, really needed
                    String o2 = "";
                    String o3 = "";
                    String o4;
                    
                    if(element.length > 0)
                        o2 = element[1];
                    
                    if(element.length > 0)
                        o3 = element[2];
                    
                    if(element.length > 0)
                        o4 = element[3];
                    else
                        o4 = ""+IToken.TYPE_BUILTIN;
                    
                    IToken t = new CompiledToken(o1, o2, o3, name, Integer.parseInt(o4));
                    array.add(t);
                } catch (Exception e) {
                    String received = "";
                    for (int i = 0; i < element.length; i++) {
                        received += element[i];
                        received += "  ";
                    }
                    
                    PydevPlugin.log(IStatus.ERROR, "Error getting completions for compiled module "+name+" received = '"+received+"'", e);
                }
            }
            
            //as we will use it for code completion on sources that map to modules, the __file__ should also
            //be added...
            if(array.size() > 0 && (name.equals("__builtin__") || name.equals("builtins"))){
                array.add(new CompiledToken("__file__","","",name,IToken.TYPE_BUILTIN));
                array.add(new CompiledToken("__builtins__","","",name,IToken.TYPE_BUILTIN));
            }
            
            this.tokens = new HashMap<String, CompiledToken>();
            for (IToken token : array) {
                this.tokens.put(token.getRepresentation(), (CompiledToken) token);
            } 
        }
    }
    
    /**
     * Compiled modules do not have imports to be seen
     * @see org.python.pydev.editor.javacodecompletion.AbstractModule#getWildImportedModules()
     */
    public IToken[] getWildImportedModules() {
        return new IToken[0];
    }

    /**
     * Compiled modules do not have imports to be seen
     * @see org.python.pydev.editor.javacodecompletion.AbstractModule#getTokenImportedModules()
     */
    public IToken[] getTokenImportedModules() {
        return new IToken[0];
    }

    /**
     * @see org.python.pydev.editor.javacodecompletion.AbstractModule#getGlobalTokens()
     */
    public IToken[] getGlobalTokens() {
        if(tokens == null){
            return new IToken[0];
        }
        
        Collection<CompiledToken> values = tokens.values();
        return values.toArray(new IToken[values.size()]);
    }

    /**
     * @see org.python.pydev.editor.javacodecompletion.AbstractModule#getDocString()
     */
    public String getDocString() {
        return "compiled extension";
    }

    /**
     * @see org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule#getGlobalTokens(java.lang.String)
     */
    public IToken[] getGlobalTokens(ICompletionState state, ICodeCompletionASTManager manager) {
        Map<String, IToken> v = cache.get(state.getActivationToken());
        if(v != null){
            Collection<IToken> values = v.values();
            return values.toArray(new IToken[values.size()]);
        }
        
        IToken[] toks = new IToken[0];

        if(COMPILED_MODULES_ENABLED){
            try {
                final IPythonNature nature = manager.getNature();
                final AbstractShell shell = AbstractShell.getServerShell(nature, AbstractShell.COMPLETION_SHELL);
                synchronized(shell){
                    String act = name+"."+state.getActivationToken();
                    List<String[]> completions = shell.getImportCompletions(act, 
                            manager.getModulesManager().getCompletePythonPath(nature.getProjectInterpreter(), 
                                    nature.getRelatedInterpreterManager())).o2;
                    
                    ArrayList<IToken> array = new ArrayList<IToken>();
                    
                    for (Iterator<String[]> iter = completions.iterator(); iter.hasNext();) {
                        String[] element = iter.next(); 
                        if(element.length >= 4){//it might be a server error
                            IToken t = new CompiledToken(element[0], element[1], element[2], act, Integer.parseInt(element[3]));
                            array.add(t);
                        }
                        
                    }
                    toks = (CompiledToken[]) array.toArray(new CompiledToken[0]);
                    HashMap<String, IToken> map = new HashMap<String, IToken>();
                    for (IToken token : toks) {
                        map.put(token.getRepresentation(), token);
                    }
                    cache.put(state.getActivationToken(), map);
                }
            } catch (Exception e) {
                System.err.println("Error while getting info for module:"+this.name);
                e.printStackTrace();
                PydevPlugin.log(e);
            }
        }
        return toks;
    }
    
    @Override
    public boolean isInDirectGlobalTokens(String tok, ICompletionCache completionCache) {
        if(this.tokens != null){
            return this.tokens.containsKey(tok);
        }
        return false;
    }
    
    @Override
    public boolean isInGlobalTokens(String tok, IPythonNature nature, ICompletionCache completionCache) {
        //we have to override because there is no way to check if it is in some import from some other place if it has dots on the tok...
        
        
        if(tok.indexOf('.') == -1){
            return isInDirectGlobalTokens(tok, completionCache);
        }else{
            ICompletionState state = CompletionStateFactory.getEmptyCompletionState(nature, completionCache);
            String[] headAndTail = FullRepIterable.headAndTail(tok);
            state.setActivationToken (headAndTail[0]);
            String head = headAndTail[1];
            IToken[] globalTokens = getGlobalTokens(state, nature.getAstManager());
            for (IToken token : globalTokens) {
                if(token.getRepresentation().equals(head)){
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * @param findInfo 
     * @see org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule#findDefinition(java.lang.String, int, int)
     */
    public Definition[] findDefinition(ICompletionState state, int line, int col, IPythonNature nature) throws Exception {
        String token = state.getActivationToken();
        
        if(TRACE_COMPILED_MODULES){
            System.out.println("CompiledModule.findDefinition:"+token);
        }
        Definition[] found = this.definitionsFoundCache.getObj(token);
        if(found != null){
            if(TRACE_COMPILED_MODULES){
                System.out.println("CompiledModule.findDefinition: found in cache.");
            }
            return found;
        }
        
        
        AbstractShell shell = AbstractShell.getServerShell(nature, AbstractShell.COMPLETION_SHELL);
        synchronized(shell){
            Tuple<String[],int[]> def = shell.getLineCol(this.name, token, 
                    nature.getAstManager().getModulesManager().getCompletePythonPath(nature.getProjectInterpreter(), 
                            nature.getRelatedInterpreterManager())); //default
            if(def == null){
                if(TRACE_COMPILED_MODULES){
                    System.out.println("CompiledModule.findDefinition:"+token+" = empty");
                }
                this.definitionsFoundCache.add(token, EMPTY_DEFINITION);
                return EMPTY_DEFINITION;
            }
            String fPath = def.o1[0];
            if(fPath.equals("None")){
                if(TRACE_COMPILED_MODULES){
                    System.out.println("CompiledModule.findDefinition:"+token+" = empty");
                }
                this.definitionsFoundCache.add(token, EMPTY_DEFINITION);
                return EMPTY_DEFINITION;
            }
            File f = new File(fPath);
            String foundModName = nature.resolveModule(f);
            String foundAs = def.o1[1];
            
            IModule mod = nature.getAstManager().getModule(foundModName, nature, true);
            if(TRACE_COMPILED_MODULES){
                System.out.println("CompiledModule.findDefinition: found at:"+mod.getName());
            }
            int foundLine = def.o2[0];
            if(foundLine == 0 && foundAs.length() > 0 && mod != null && state.canStillCheckFindSourceFromCompiled(mod, foundAs)){
                //TODO: The nature (and so the grammar to be used) must be defined by the file we'll parse
                //(so, we need to know the system modules manager that actually created it to know the actual nature)
                IModule sourceMod = AbstractModule.createModuleFromDoc(mod.getName(), f, new Document(REF.getFileContents(f)), nature, 0);
                if(sourceMod instanceof SourceModule){
                    Definition[] definitions = (Definition[]) sourceMod.findDefinition(state.getCopyWithActTok(foundAs), -1, -1, nature);
                    if(definitions.length > 0){
                        this.definitionsFoundCache.add(token, definitions);
                        return definitions;
                    }
                }
            }
            if(mod == null){
                mod = this;
            }
            int foundCol = def.o2[1];
            if(foundCol < 0){
                foundCol = 0;
            }
            if(TRACE_COMPILED_MODULES){
                System.out.println("CompiledModule.findDefinition: found compiled at:"+mod.getName());
            }
            Definition[] definitions = new Definition[]{new Definition(foundLine+1, foundCol+1, token, null, null, mod)};
            this.definitionsFoundCache.add(token, definitions);
            return definitions;
        }
    }

}
