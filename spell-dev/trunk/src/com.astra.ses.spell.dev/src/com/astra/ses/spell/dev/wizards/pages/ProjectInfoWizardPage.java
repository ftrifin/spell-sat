///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards.pages
// 
// FILE      : ProjectInfoWizardPage.java
//
// DATE      : 2010-05-28
//
// Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.wizards.pages;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.astra.ses.spell.dev.preferences.SpellPythonInterpreterPage;

/*******************************************************************************
 * 
 * ProjectInfoWizard page is used to retrieve project information from the user
 * such as the name or the Python interpreter to use
 *
 ******************************************************************************/
public class ProjectInfoWizardPage extends WizardPage {

	/** Internal page name */
	private static final String PAGE_NAME = "projectInfoPage";
	
	/** Project name text widget */
	private Text m_projectNameField;
	/** Interpreter composite */
	private Composite m_linkComposite;
	private Composite m_interpreterComposite;
	
	private Link m_preferencesLink;
	private Label m_interpreterLabel;
	private Label m_interpreterVersion;
	private Button m_defaultLocation;
	private Text m_customLocation;
	private Button m_defaultStructureButton;
	
	/***************************************************************************
	 * Constructor
	 * @param pageName
	 *************************************************************************/
	public ProjectInfoWizardPage() {
		super(PAGE_NAME);
		setTitle("Procedure suite");
        setDescription("Create a new Procedure suite");
	}
	
    /***************************************************************************
     * Get project name
     * @return
     **************************************************************************/
    public String getProjectName()
    {
    	return m_projectNameField.getText();
    }
    
    /***************************************************************************
     * Return python interpreter's version
     * @return
     **************************************************************************/
    public String getInterpreterVersion()
    {
    	return m_interpreterVersion.getText();
    }
    
    /***************************************************************************
     * Return new project's location where files will be stored
     * @return
     **************************************************************************/
    public String getProjectLocation()
    {
    	return m_customLocation.getText();
    }
    
    /***************************************************************************
     * Determine if default folders structure should be created for the new
     * project
     * @return
     **************************************************************************/
    public boolean createDefaultStructure()
    {
    	return m_defaultStructureButton.getSelection();
    }

	@Override
	public void createControl(Composite parent) {
		
		// Main composite
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(1, true);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.verticalAlignment = SWT.TOP;
        composite.setLayout(layout);
        composite.setLayoutData(layoutData);
        
        /*
         * Name section
         */
        createProjectNameGroup(composite);
        createLocationGroup(composite);
        createInterpreterInfoGroup(composite);
        
        /*
         * Perform a pre validation to avoid pressing the finish button
         */
        validatePage();
        
        /*
         * Initialize messages
         */
        setControl(composite);
	}

	/***************************************************************************
	 * Create project group
	 * @param parent
	 **************************************************************************/
    private void createProjectNameGroup(Composite parent) {
        // project specification group
        Group projectGroup = new Group(parent, SWT.BORDER);   
        projectGroup.setText("Project information");
        
        GridLayout layout = new GridLayout(2, false);
        GridData projectData = new GridData(GridData.FILL_HORIZONTAL);
        projectData.verticalAlignment = SWT.TOP;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(projectData);

        // new project label
        Label projectLabel = new Label(projectGroup, SWT.NONE);             
        projectLabel.setText("&Suite name");

        // new project name entry field
        m_projectNameField = new Text(projectGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        m_projectNameField.setLayoutData(data);

        // When project name is modified, then check the form
        m_projectNameField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				boolean useDefaults = m_defaultLocation.getSelection();
				/*
				 * Update location info if default location is selected
				 */
		         m_customLocation.setEnabled(!useDefaults);
		         if (useDefaults)
		         {
		         	String path = 
		         		Platform.getLocation().append(m_projectNameField.getText()).toOSString();
		         	m_customLocation.setText(path);
		         }
		         /*
		          * Validate page
		          */
				validatePage();
			}
		});
    }
    
    /***************************************************************************
     * Create project location group, where user can introduce information 
     * about where the project will be stored
     * @param parent
     **************************************************************************/
    private void createLocationGroup(Composite parent)
    {
    	boolean defaultLocation = true;
    	
        // project specification group
        Group projectGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectGroup.setText("Suite contents");

        m_defaultLocation = new Button(projectGroup, SWT.CHECK
                | SWT.RIGHT);
        m_defaultLocation.setText("Use &default location");
        m_defaultLocation.setSelection(defaultLocation);

        GridData buttonData = new GridData();
        buttonData.horizontalSpan = 3;
        m_defaultLocation.setLayoutData(buttonData);

        // location label
        final Label locationLabel = new Label(projectGroup, SWT.NONE);
        locationLabel.setText("Director&y");
        locationLabel.setEnabled(!defaultLocation);

        // project location entry field
        m_customLocation = new Text(projectGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        m_customLocation.setLayoutData(data);
        m_customLocation.setEnabled(!defaultLocation);
        m_customLocation.setText(Platform.getLocation().toOSString());

        // browse button
        final Button browseButton = new Button(projectGroup, SWT.PUSH);
        browseButton.setText("B&rowse");
        browseButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(m_customLocation.getShell());
                dialog.setMessage("Select the project contents directory.");
                String selectedDirectory = dialog.open();
                if (selectedDirectory != null) {
                    m_customLocation.setText(selectedDirectory);
                    validatePage();
                }
            }
        });

        browseButton.setEnabled(!defaultLocation);

        // Set the initial value first before listener
        // to avoid handling an event during the creation.
        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean useDefaults = m_defaultLocation.getSelection();
                browseButton.setEnabled(!useDefaults);
                m_customLocation.setEnabled(!useDefaults);
                locationLabel.setEnabled(!useDefaults);
                if (useDefaults)
                {
                	String path = 
                		Platform.getLocation().append(m_projectNameField.getText()).toOSString();
                	m_customLocation.setText(path);
                	validatePage();
                }
            }
        };
        m_defaultLocation.addSelectionListener(listener);
        
        m_defaultStructureButton = new Button(projectGroup , SWT.CHECK);
        m_defaultStructureButton.setLayoutData(
        		new GridData(SWT.FILL, SWT.TOP, false, false, layout.numColumns, 1));
        m_defaultStructureButton.setText("Cr&eate default suite structure");
        m_defaultStructureButton.setSelection(true);
    }
    
    /***************************************************************************
     * Create interpreter info dialog if it has not been set
     * @param parent
     **************************************************************************/
    private void createInterpreterInfoGroup(Composite parent)
    {
    	// Python interpreter group
        Group interpreterGroup = new Group(parent, SWT.BORDER);  
        interpreterGroup.setText("Python interpreter information");
        
        StackLayout layout = new StackLayout();
        GridData interpreterData = new GridData(SWT.FILL, SWT.TOP, true, false);
        interpreterGroup.setLayout(layout);
        interpreterGroup.setLayoutData(interpreterData);
    	
        /*
         * Link composite
         */
        m_linkComposite = new Composite(interpreterGroup, SWT.NONE);
        m_linkComposite.setLayout(new GridLayout());
        
    	m_preferencesLink = new Link(m_linkComposite, SWT.NONE);
    	m_preferencesLink.setData("PAGE", SpellPythonInterpreterPage.PAGE_ID);
    	m_preferencesLink.setText("<a>Click here to set a Python interpreter</a>");
    	m_preferencesLink.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	String page = (String) e.widget.getData("PAGE");
                PreferenceDialog dialog = 
                	PreferencesUtil.createPreferenceDialogOn(null, page, null, null);
                if (dialog.open() == PreferenceDialog.OK)
                {
                	fillInterpreterInfoWidget();
                	validatePage();
                }
            }
    	});
    		
    	/*
    	 * Python interpreter composite
    	 */
    	m_interpreterComposite = new Composite(interpreterGroup, SWT.NONE);
    	m_interpreterComposite.setLayout(new GridLayout(2, false));
    	
    	Label pathLabel = new Label(m_interpreterComposite, SWT.NONE);
    	pathLabel.setText("Interpreter path");
        m_interpreterLabel = new Label(m_interpreterComposite, SWT.NONE);
        
        Label versionLabel = new Label(m_interpreterComposite, SWT.NONE);
        versionLabel.setText("Version");
        m_interpreterVersion = new Label(m_interpreterComposite, SWT.NONE);
        
        Link explanation = new Link(m_interpreterComposite, SWT.WRAP);
        explanation.setText("Interpreter to use will be the first in the list at <a>Interpreters preference page</a>");
        explanation.setData("PAGE", SpellPythonInterpreterPage.PAGE_ID);
        explanation.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	String page = (String) e.widget.getData("PAGE");
                PreferenceDialog dialog = 
                	PreferencesUtil.createPreferenceDialogOn(null, page, null, null);
                if (dialog.open() == PreferenceDialog.OK)
                {
                	fillInterpreterInfoWidget();
                	validatePage();
                }
            }
    	});
        GridData explData = new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1);
        explanation.setLayoutData(explData);
        
        fillInterpreterInfoWidget();
    }
    
    /***************************************************************************
     * Lookup configured python interpreters
     **************************************************************************/
    private String lookupPythonInterpreter()
    {
    	String plugin = "org.python.pydev";
    	String prefName = "INTERPRETER_PATH_NEW";
    	String result = null;
    	
    	IEclipsePreferences instanceScope = new InstanceScope().getNode(plugin);
    	IEclipsePreferences defaultScope = new DefaultScope().getNode(prefName);
    	
    	result = instanceScope.get(prefName, null);
    	if (result == null)
    	{
    		result = defaultScope.get(prefName, null);
    	}
    	return result;
    }
    
    /***************************************************************************
     * Fill interpreter info area with the appropiate widgets according to the
     * configured interpreter
     **************************************************************************/
    private void fillInterpreterInfoWidget()
    {
    	String interpreter = lookupPythonInterpreter();
    	Composite main = m_interpreterComposite.getParent();
    	StackLayout layout = (StackLayout) main.getLayout();
        if (interpreter == null)
        {
        	layout.topControl = m_linkComposite;
        }
        else
        {        	
        	String[] splitted = interpreter.split("\\|");
        	String versionExecutable = splitted[0];
        	String executable = versionExecutable.split("Executable:")[1];
        	String version = versionExecutable.split("Executable:")[0].split("Version")[1];
        	m_interpreterLabel.setText(executable);
        	m_interpreterLabel.pack(true);
        	m_interpreterVersion.setText(version);
        	m_interpreterVersion.pack(true);
        	
        	layout.topControl = m_interpreterComposite;
        }
        main.layout(true);
    }
    
    /***************************************************************************
     * Validate field widgets to allow the user to finish the project creation
     * @return
     **************************************************************************/
    private boolean validatePage() {
    	String errors = "";
    	setErrorMessage(null);
    	/*
    	 * Project exists
    	 */
    	String name = m_projectNameField.getText();
    	if (name.isEmpty())
    	{
    		errors += "Project name can't be empty\n";
    	}
    	else
    	{
    		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    		if (p.exists())
    		{
    			errors += "Project already exists\n";
    		}
    	}
    	/*
    	 * Project location settings
    	 */
    	if (!m_defaultLocation.getSelection())
    	{
    		String customLocation = m_customLocation.getText();
    		if (customLocation.isEmpty())
    		{
    			errors += "Undefined project location";
    		}
    		File location = new File(customLocation);
    		if (!location.isDirectory())
    		{
    			errors += "Project location is not a directory\n";
    		}
    		if (!location.canWrite())
    		{
    			errors += "Can't write in project location\n";
    		}
    	}
    	/*
    	 * Interpreter defined
    	 */
    	if (m_interpreterLabel.getText().isEmpty())
    	{
    		errors += "Python interpreter has not been defined";
    	}
    	/*
    	 *  If there are errors, then show the message
    	 */
    	if (!errors.isEmpty())
    	{
    		setErrorMessage(errors);
    	}
    	boolean canFinish = errors.isEmpty();
    	setPageComplete(canFinish);
    	return canFinish;
    }
}