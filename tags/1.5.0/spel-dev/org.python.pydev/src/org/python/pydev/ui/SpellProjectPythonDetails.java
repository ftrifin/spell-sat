package org.python.pydev.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.PyProjectPythonDetails.ProjectInterpreterAndGrammarConfig;
import org.python.pydev.utils.ICallback;

public class SpellProjectPythonDetails extends ProjectInterpreterAndGrammarConfig {
	
	/**
	 * PYTHON GRAMMAR TO USE
	 */
	private static final String PYTHON_GRAMMAR = "2.6";
	
	/**
	 * Python interpreter choice
	 */
	private Combo interpretersChoice;
	
	/**
	 * Additional info about python interpreters
	 */
	private Link interpreterNoteText;
	
	/**
	 * Constructor
	 * @param callback
	 */
	public SpellProjectPythonDetails(ICallback callback) {
		super(callback);
	}
	
	/**
	 * Fill the composite with the python version to use
	 */
	public Control doCreateContents(Composite p) {
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		
		Group pythonDetailsGroup = new Group(p, SWT.NONE);
		GridLayout groupLayout = new GridLayout(2,false);
		pythonDetailsGroup.setLayout(groupLayout);
		
		pythonDetailsGroup.setLayoutData(gd);
		pythonDetailsGroup.setText("Python interpreter details");
		
		Label pythonGrammarVersionLabel = new Label(pythonDetailsGroup, SWT.NONE);
		pythonGrammarVersionLabel.setText("Grammar version to use");
		
		Label pythonGrammarVersion = new Label(pythonDetailsGroup, SWT.NONE);
		pythonGrammarVersion.setText(getSelectedPythonOrJythonAndGrammarVersion());
		
		Label projectInterpreterLabel = new Label(pythonDetailsGroup, SWT.NONE);
		projectInterpreterLabel.setText("Interpreter");
		
        interpretersChoice = new Combo(pythonDetailsGroup, SWT.READ_ONLY);
        
        interpreterNoteText = new Link(pythonDetailsGroup, SWT.LEFT | SWT.WRAP);
        interpreterNoteText.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, "org.python.pydev.ui.pythonpathconf.interpreterPreferencesPagePython", null, null);
                dialog.open();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
		
        IInterpreterManager interpreterManager = PydevPlugin.getPythonInterpreterManager();
   
        String[] interpreters = interpreterManager.getInterpreters();
        if(interpreters.length > 0){
            ArrayList<String> interpretersWithDefault = new ArrayList<String>();
            interpretersWithDefault.add(IPythonNature.DEFAULT_INTERPRETER);
            interpretersWithDefault.addAll(Arrays.asList(interpreters));
            interpretersChoice.setItems(interpretersWithDefault.toArray(new String[0]));
            
            interpretersChoice.setVisible(true);
            interpreterNoteText.setText("<a>Click here to configure an interpreter not listed.</a>");
            interpretersChoice.setText(IPythonNature.DEFAULT_INTERPRETER);         
        }else{
            interpretersChoice.setVisible(false);
            interpreterNoteText.setText("<a>Please configure an interpreter in the related preferences before proceeding.</a>");         
        }
        
		return p;
	}
	
	@Override
    public void setDefaultSelection() {
		//Do nothing
    }
	
    /**
     * @return a string as specified in the constants in IPythonNature
     * @see IPythonNature#PYTHON_VERSION_XXX 
     * @see IPythonNature#JYTHON_VERSION_XXX
     */
    public String getSelectedPythonOrJythonAndGrammarVersion() {
        return "python " + PYTHON_GRAMMAR;
    }
    
    @Override
    public String getProjectInterpreter(){
        if(interpretersChoice.isVisible() == false){
            return null;
        }
        return interpretersChoice.getText();
    }    
}
