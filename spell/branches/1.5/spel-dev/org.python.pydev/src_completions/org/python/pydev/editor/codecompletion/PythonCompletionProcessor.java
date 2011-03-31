/*
 * Created on Mar 29, 2004
 *
 */
package org.python.pydev.editor.codecompletion;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.codecompletion.templates.PyTemplateCompletionProcessor;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.SystemPythonNature;
import org.python.pydev.ui.interpreters.ChooseInterpreterManager;

/**
 * @author Dmoore
 * @author Fabio Zadrozny
 * 
 * This class is responsible for code completion / template completion.
 */
public class PythonCompletionProcessor extends AbstractCompletionProcessorWithCycling {

    
    /**
     * This makes the templates completion
     */
    private PyTemplateCompletionProcessor templatesCompletion = new PyTemplateCompletionProcessor();

    /**
     * This makes python code completion
     */
    private IPyCodeCompletion codeCompletion;

    /**
     * Edit.
     */
    private PyEdit edit;

    /**
     * Some error...
     */
    private Throwable error;

    /**
     * These are the activation chars (cache)
     */
    private volatile static char[] activationChars = null;

    /**
     * This is the class that manages the context information (validates it and
     * changes its presentation).
     */
    private PyContextInformationValidator contextInformationValidator;
    
    
    /**
     * @param edit the editor that works with this processor
     * @param pyContentAssistant the content assistant that will invoke this completion
     */
    public PythonCompletionProcessor(PyEdit edit, PyContentAssistant pyContentAssistant) {
        super(pyContentAssistant);
        this.edit = edit;
        this.pyContentAssistant = pyContentAssistant;
        this.codeCompletion = getCodeCompletionEngine();
        
        contextInformationValidator = new PyContextInformationValidator();
        

        pyContentAssistant.addCompletionListener(new ICompletionListener(){

            public void assistSessionEnded(ContentAssistEvent event) {
            }

            public void assistSessionStarted(ContentAssistEvent event) {
                startCycle();
            }

            public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
                //ignore
            }
            
        });

    }


    protected IPyCodeCompletion getCodeCompletionEngine() {
        return new PyCodeCompletion();
    }

    /**
     * This is the interface implemented to get the completions.
     * 
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    @SuppressWarnings("unchecked")
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
        updateStatus();
        ICompletionProposal[] proposals;
        
        try {
            //FIRST: discover activation token and qualifier.
            IDocument doc = viewer.getDocument();


            //list for storing the proposals
            ArrayList<ICompletionProposal> pythonAndTemplateProposals = new ArrayList<ICompletionProposal>();
            
            IPythonNature nature = edit.getPythonNature();
            
            if(nature == null){
                IInterpreterManager manager = ChooseInterpreterManager.chooseInterpreterManager();
                if(manager != null){
                    nature = new SystemPythonNature(manager);
                }
            }
            
            if(nature == null || !nature.startRequests()){
                return new ICompletionProposal[0];
            }
            try{
                CompletionRequest request = new CompletionRequest(edit.getEditorFile(), 
                        nature, doc, documentOffset, codeCompletion);
                
    
                
                //SECOND: getting code completions and deciding if templates should be shown too.
                //Get code completion proposals
                if(PyCodeCompletionPreferencesPage.useCodeCompletion()){
                    if(whatToShow == SHOW_ALL){
                        try {
                            pythonAndTemplateProposals.addAll(getPythonProposals(viewer, documentOffset, doc, request));
                        } catch (Throwable e) {
                            setError(e);
                        }
                    }
    
                }
                
                
                String[] strs = PySelection.getActivationTokenAndQual(doc, documentOffset, false); 
    
                String activationToken = strs[0];
                String qualifier = strs[1];
    
                
                //THIRD: Get template proposals (if asked for)
                if(request.showTemplates && (activationToken == null || activationToken.trim().length() == 0)){
                    List templateProposals = getTemplateProposals(viewer, documentOffset, activationToken, qualifier);
                    pythonAndTemplateProposals.addAll(templateProposals);
                }
    
                
                //to show the valid ones, we'll get the qualifier from the initial request
                proposals = PyCodeCompletionUtils.onlyValidSorted(pythonAndTemplateProposals, request.qualifier, request.isInCalltip);
            }finally{
                nature.endRequests();
            }
        } catch (RuntimeException e) {
            proposals = new ICompletionProposal[0];
            setError(e);
        }
    
        doCycle();
        // Return the proposals
        return proposals;
    }

    
    
    



    /**
     * @param e
     */
    private void setError(Throwable e) {
        this.error = e;
        PydevPlugin.log(e);
    }


    /**
     * Returns the python proposals as a list.
     * First parameter of tuple is a list and second is a Boolean object indicating whether the templates
     * should be also shown or not. 
     * @param viewer 
     * @throws CoreException
     * @throws BadLocationException
     */
    @SuppressWarnings("unchecked")
    private List getPythonProposals(ITextViewer viewer, int documentOffset, IDocument doc, CompletionRequest request) throws CoreException, BadLocationException {
        //if non empty string, we're in imports section.
        String importsTipperStr = request.codeCompletion.getImportsTipperStr(request).importsTipperStr;
        
        if (importsTipperStr.length() != 0 || request.isInCalltip){
            request.showTemplates = false; //don't show templates if we are in the imports section or inside a calltip.
        }
        
        List allProposals = request.codeCompletion.getCodeCompletionProposals(viewer, request);
        return allProposals;
    }

    
    
    /**
     * Returns the template proposals as a list.
     */
    @SuppressWarnings("unchecked")
    private List getTemplateProposals(ITextViewer viewer, int documentOffset, String activationToken, java.lang.String qualifier) {
        List<ICompletionProposal> propList = new ArrayList<ICompletionProposal>();
        this.templatesCompletion.addTemplateProposals(viewer, documentOffset, propList);
        return propList;
    }

    /**
     * Ok, if we have 
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
        //System.out.println("computeContextInformation");
        if(viewer.getDocument() != this.contextInformationValidator.doc){
            return null;
        }
        //System.out.println("this.contextInformationValidator.returnedFalseOnce:"+this.contextInformationValidator.returnedFalseOnce);
        //if we didn't return false at least once, it is already installed.
        if(this.contextInformationValidator.returnedFalseOnce && this.contextInformationValidator.isContextInformationValid(documentOffset)){
            return new IContextInformation[]{this.contextInformationValidator.fInformation};
        }
        return null;
    }

    /**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return getStaticCompletionProposalAutoActivationCharacters();
    }

    /**
     * Attribute that determines if the listener that'll clear the auto activation chars is already in place.
     */
    private volatile static boolean listenerToClearAutoActivationAlreadySetup = false;

    /**
     * @return the auto-activation chars that should be used.
     */
    public static char[] getStaticCompletionProposalAutoActivationCharacters() {
        if(!listenerToClearAutoActivationAlreadySetup){
            //clears the cache when the preferences are changed.
            IPreferenceStore preferenceStore = PydevPlugin.getDefault().getPreferenceStore();
            preferenceStore.addPropertyChangeListener(new IPropertyChangeListener(){
    
                public void propertyChange(PropertyChangeEvent event) {
                    activationChars = null; //clear the cache when it changes
                }
                
            });
            listenerToClearAutoActivationAlreadySetup = true;
        }
        
        
        if(activationChars == null){ //let's cache this
         
            if(!PyCodeCompletionPreferencesPage.useAutocomplete()){
                activationChars = new char[0];
                
            }else{
                char[] c = new char[0];
                if (PyCodeCompletionPreferencesPage.isToAutocompleteOnDot()) {
                    c = addChar(c, '.');
                }
                if (PyCodeCompletionPreferencesPage.isToAutocompleteOnPar()) {
                    c = addChar(c, '(');
                }
                activationChars = c;
            }
        }
        return activationChars;
    }

    /**
     * Adds a char to an array of chars and returns the new array. 
     * 
     * @param c
     * @param toAdd
     * @return
     */
    public static char[] addChar(char[] c, char toAdd) {
        char[] c1 = new char[c.length + 1];

        System.arraycopy(c, 0, c1, 0, c.length);
        c1[c.length] = toAdd;
        return c1;

    }

    /**
     * 
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /**
     * If completion fails for some reason, we could give it here...
     * 
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
     */
    public java.lang.String getErrorMessage() {
        String msg = null;
        if(this.error != null){
            msg = this.error.getMessage();
            this.error = null;
        }
        return msg;
    }

    /**
     * 
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator() {
        return this.contextInformationValidator;
    }


}