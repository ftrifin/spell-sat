////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards.pages
// 
// FILE      : ProcedureInfoWizardPage.java
//
// DATE      : 2010-07-06
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
// SUBPROJECT: SPELL DEV
//
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.wizards.pages;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.dialogs.PythonPackageSelectionDialog;
import org.python.pydev.ui.dialogs.SourceFolder;

import com.astra.ses.spell.dev.Activator;

public class ProcedureInfoWizardPage extends WizardPage {

	/** Wizard page identifier */
	private static final String PAGE_NAME = "ProcedureInformation";
	
	/** Structured selection */
	private IStructuredSelection m_selection;
	
	/** Procedure file name widget */
	private Text m_procedureFilename;
	/** Procedure name text widget */
	private Text m_procedureName;
	/** Procedure description */
	private Text m_procedureDescription;
	/** Procedure author */
	private Text m_procedureAuthor;
	/** Procedure spacecrafts */
	private Text m_procedureSpacecrafts;
	/** Procedure specification */
	private Text m_procedureSpecification;
	/** Procedure database */
	private Text m_procedureDatabase;
	/** Procedure category */
	private Text m_procedureCategory;
	/** Procedure validator */
	private Text m_procedureValidator;
	/** Procedure reviewer */
	private Text m_procedureReviewer;
	
	/** Source folder text widget */
	private Text m_packageText;
	/** Project text */
	private Text m_projectText;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ProcedureInfoWizardPage(IStructuredSelection selection) {
		super(PAGE_NAME);
		m_selection = selection;
		//title
		setTitle("Procedure");
		//description
		setDescription("Set procedure information and location");
		// image
		String pluginId = Activator.getDefault().getBundle().getSymbolicName();
		String imagePath = "images/wizards/spellWizardHeader.png";
		setImageDescriptor(Activator.imageDescriptorFromPlugin(pluginId, imagePath));
	}

	@Override
	public void createControl(Composite parent) {
		/*
		 * Main composite
		 */
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.verticalSpacing = 25;
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayout(layout);
		container.setLayoutData(layoutData);
		
		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		};
		
		/*
		 * Procedure name group
		 */
		Group procInfo = new Group(container, SWT.BORDER);
		procInfo.setText("Procedure information");
		GridLayout infoLayout = new GridLayout(2, false);
		procInfo.setLayout(infoLayout);
		procInfo.setLayoutData(GridDataFactory.copyData(layoutData));
		
		Label procInfoDesc = new Label(procInfo, SWT.NONE);
		GridData infoData = new GridData(GridData.FILL_HORIZONTAL);
		infoData.horizontalSpan = infoLayout.numColumns;
		infoData.heightHint = 25;
		infoData.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
		procInfoDesc.setLayoutData(infoData);
		procInfoDesc.setText("Procedure information fields will be used to create " +
				"the procedure source code header automatically.");
		
		Label procFilenameLabel = new Label(procInfo, SWT.NONE);
		procFilenameLabel.setText("File name");
		
		m_procedureFilename = new Text(procInfo, SWT.BORDER);
		m_procedureFilename.setLayoutData(GridDataFactory.copyData(layoutData));
		m_procedureFilename.addModifyListener(modifyListener);
		
		Label procNameLabel = new Label(procInfo, SWT.NONE);
		procNameLabel.setText("Name");
		
		m_procedureName = new Text(procInfo, SWT.BORDER);
		m_procedureName.setLayoutData(GridDataFactory.copyData(layoutData));
		m_procedureName.addModifyListener(modifyListener);
		
		Label procDescLabel = new Label(procInfo, SWT.NONE);
		procDescLabel.setText("Description");
		
		m_procedureDescription = new Text(procInfo, SWT.BORDER);
		m_procedureDescription.setLayoutData(GridDataFactory.copyData(layoutData));

		Label procSpacecraftsLabel = new Label(procInfo, SWT.NONE);
		procSpacecraftsLabel.setText("Spacecrafts");
		
		m_procedureSpacecrafts = new Text(procInfo, SWT.BORDER);
		m_procedureSpacecrafts.setLayoutData(GridDataFactory.copyData(layoutData));
		m_procedureSpacecrafts.addModifyListener(modifyListener);
		
		new Label(procInfo, SWT.NONE);
		
		Label scInfoLabel = new Label(procInfo, SWT.NONE);
		scInfoLabel.setText("Applicable spacecrafts list must be a comma sepparated list of " +
				"spacecraft names");
		
		Label procSpecificationLabel = new Label(procInfo, SWT.NONE);
		procSpecificationLabel.setText("Specification");
		
		m_procedureSpecification = new Text(procInfo, SWT.BORDER);
		m_procedureSpecification.setLayoutData(GridDataFactory.copyData(layoutData));

		Label procDatabaseLabel = new Label(procInfo, SWT.NONE);
		procDatabaseLabel.setText("Database");
		
		m_procedureDatabase = new Text(procInfo, SWT.BORDER);
		m_procedureDatabase.setLayoutData(GridDataFactory.copyData(layoutData));

		Label procCategoryLabel = new Label(procInfo, SWT.NONE);
		procCategoryLabel.setText("Category");
		
		m_procedureCategory = new Text(procInfo, SWT.BORDER);
		m_procedureCategory.setLayoutData(GridDataFactory.copyData(layoutData));

		Label procAuthorLabel = new Label(procInfo, SWT.NONE);
		procAuthorLabel.setText("Developed by");
		
		m_procedureAuthor = new Text(procInfo, SWT.BORDER);
		m_procedureAuthor.setLayoutData(GridDataFactory.copyData(layoutData));
		
		Label procReviewerLabel = new Label(procInfo, SWT.NONE);
		procReviewerLabel.setText("Verified by");
		
		m_procedureReviewer = new Text(procInfo, SWT.BORDER);
		m_procedureReviewer.setLayoutData(GridDataFactory.copyData(layoutData));
		
		Label procValidatorLabel = new Label(procInfo, SWT.NONE);
		procValidatorLabel.setText("Validated by");
		
		m_procedureValidator = new Text(procInfo, SWT.BORDER);
		m_procedureValidator.setLayoutData(GridDataFactory.copyData(layoutData));
		
		/*
		 * Procedure location group
		 */
		Group procLocation = new Group(container, SWT.BORDER);
		procLocation.setText("Procedure location");
		GridLayout locationLayout = new GridLayout(3, false);
		GridData locationData = GridDataFactory.copyData(layoutData);
		procLocation.setLayout(locationLayout);
		procLocation.setLayoutData(locationData);
		
		Label projectLabel = new Label(procLocation, SWT.NONE);
		projectLabel.setText("Project");
		
		m_projectText = new Text(procLocation, SWT.BORDER | SWT.READ_ONLY);
		GridData projectData = new GridData(GridData.FILL_HORIZONTAL);
		projectData.horizontalSpan = 2;
		m_projectText.setLayoutData(projectData);
		m_projectText.addModifyListener(modifyListener);
		
		Label packageLabel = new Label(procLocation, SWT.NONE);
		packageLabel.setText("Source folder");
		
		m_packageText = new Text(procLocation, SWT.BORDER|SWT.READ_ONLY);
		m_packageText.setLayoutData(GridDataFactory.copyData(layoutData));
		m_packageText.addModifyListener(modifyListener);
		
		Button browsePackage = new Button(procLocation, SWT.PUSH);
		browsePackage.setText("Browse");
		browsePackage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
                try {
                    PythonPackageSelectionDialog dialog = new PythonPackageSelectionDialog(getShell(), false);
                    if (dialog.open() == Dialog.OK)
                    {
                        Object firstResult = dialog.getFirstResult();
                        IResource res = null;
                        if(firstResult instanceof SourceFolder){ //it is the default package
                        	res = ((SourceFolder) firstResult).folder;
                        }
                        if(firstResult instanceof org.python.pydev.ui.dialogs.Package){
                            org.python.pydev.ui.dialogs.Package f = (org.python.pydev.ui.dialogs.Package) firstResult;
                            res = f.folder;
                        }
                        m_projectText.setText(res.getProject().getName());
                        m_packageText.setText(res.getProjectRelativePath().toOSString());
                    }
                } catch (Exception e1) {
                    PydevPlugin.log(e1);
                }
			}
		});
		
		new Label(procLocation, SWT.NONE);
		Label explaining = new Label(procLocation, SWT.WRAP);
		GridData explData = new GridData(GridData.FILL_HORIZONTAL);
		explData.horizontalSpan = locationLayout.numColumns - 1;
		explaining.setLayoutData(explData);
		String explanation = "Procedures can only be created inside source folders";
		explaining.setText(explanation);
		
		/*
		 * Initialize some controls depending on the initial selection
		 */
		if (m_selection != null)
		{
			Object selected = m_selection.getFirstElement();
			// Selected element may be a packge
			if (selected instanceof IFolder)
			{
				// retrieve project
				IProject project = ((IFolder) selected).getProject();
				
				// retrieve source folder
				IFolder folder = (IFolder) selected;
				String sourceFolder = folder.getProjectRelativePath().toOSString();
				
				// update widgets
				m_projectText.setText(project.getName());
				m_packageText.setText(sourceFolder);
			}
		}
		
		validatePage();
		
		container.layout();
		container.pack();
		
		// set Control
		setControl(container);
	}
	
	/***************************************************************************
	 * Validate inputs given by the user to assert a new procedure can be
	 * created safely
	 * @return true if procedure can be created. False otherwise
	 **************************************************************************/
	private boolean validatePage()
	{
		String procFilename = m_procedureFilename.getText();
		String procName = m_procedureName.getText();
		String procSpacecrafts = m_procedureSpacecrafts.getText();
		String folderName = m_packageText.getText();
		String projectName = m_projectText.getText();
		String error = "";

		if (folderName.isEmpty())
		{
			error += "Source folder";
		}
		
		if (procSpacecrafts.isEmpty())
		{
			if (!error.isEmpty())
			{
				error += ", spacecraft list";
			}
			else
			{
				error = "Spacecrafts list";
			}
			m_procedureSpacecrafts.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
		}
		else
		{
			m_procedureSpacecrafts.setBackground(null);
		}
		
		if (procFilename.isEmpty())
		{
			if (!error.isEmpty())
			{
				error += ", file name";
			}
			else
			{
				error = "File name";
			}
			m_procedureFilename.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
		}
		else
		{
			m_procedureFilename.setBackground(null);
		}
		
		if (procName.isEmpty())
		{
			if (!error.isEmpty())
			{
				error += " and procedure name";
			}
			else
			{
				error = "Procedure name";
			}
			m_procedureName.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
		}
		else
		{
			m_procedureName.setBackground(null);
		}
		
		if (!error.isEmpty()) 
		{
			if (error.contains(","))
			{
				error += " are not set.\n";
			}
			else
			{
				error += " is not set\n";
			}
		}
		
		if (!projectName.isEmpty() && !procFilename.isEmpty() && !folderName.isEmpty())
		{
			//To check if the file exists, append python file extension
			if (!procFilename.endsWith(".py"))
			{
				procFilename = procFilename + ".py";
			}
			if (fileExists(projectName, folderName, procFilename))
			{
				error += "Procedure already exists\n";
			}
		}
		
		boolean errors = !error.isEmpty();
		if (errors)
		{
			setErrorMessage(error);
		}
		else
		{
			setErrorMessage(null);
		}
		setPageComplete(!errors);
		return errors;
	}
	
	/***************************************************************************
	 * Determine if the given file, in the given source folder in the given
	 * project exists
	 * @param project
	 * @param sourceFolder
	 * @param procName
	 * @return
	 **************************************************************************/
	private boolean fileExists(String project, String sourceFolder, String procName)
	{
		Path path = new Path(IPath.SEPARATOR + project + 
							IPath.SEPARATOR + sourceFolder + 
							IPath.SEPARATOR + procName);
		boolean fileExists = ResourcesPlugin.getWorkspace().getRoot().getFile(path).exists();
		return fileExists; 
	}
	
	/***************************************************************************
	 * Returns the absolute file path to be created
	 * @return the absolute file path to create
	 **************************************************************************/
	public Path getAbsoluteFilePath()
	{
		String procName = m_procedureFilename.getText();
		if (!procName.toLowerCase().endsWith(".py"))
		{
			procName = procName + ".py";
		}
		String folderName = m_packageText.getText();
		String projectName = m_projectText.getText();
		Path path = new Path(IPath.SEPARATOR + projectName + 
				IPath.SEPARATOR + folderName + 
				IPath.SEPARATOR + procName);
		return path;
	}
	
	/***************************************************************************
	 * Get the code header to insert in the just created procedure
	 * @return a String containing the source code header
	 **************************************************************************/
	public String getCodeHeader()
	{
		String procName = m_procedureFilename.getText();
		if (!procName.toLowerCase().endsWith(".py"))
		{
			procName = procName + ".py";
		}
		
		return SPELLSourceCodeHeader.generateHeader(m_procedureName.getText(),
													m_procedureDescription.getText(),
													m_procedureAuthor.getText(),
													procName,
													m_procedureSpacecrafts.getText().split(","),
													m_procedureSpecification.getText(),
													m_procedureDatabase.getText(),
													m_procedureCategory.getText(),
													m_procedureValidator.getText(),
													m_procedureReviewer.getText());
	}
}
