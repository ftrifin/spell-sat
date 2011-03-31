////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards.pages
// 
// FILE      : SourceFolderWizardPage.java
//
// DATE      : 2010-07-08
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.wizards.pages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class SourceFolderWizardPage extends WizardPage {

	/** Page name */
	private static final String PAGE_NAME = "Source folder information";
	
	/** Structured selection */
	private IStructuredSelection m_selection;
	/** Project name */
	private Text m_projectName;
	/** Folder name */
	private Text m_folderName;
	
	public SourceFolderWizardPage(IStructuredSelection selection) {
		super(PAGE_NAME);
		setTitle("Source folder");
        setDescription("Create a new source folder");
        m_selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		/*
		 * Main container
		 */
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1,true);
        container.setLayout(gridLayout);
        container.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
        container.setFont(parent.getFont());
        
        ModifyListener modify = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		};
        
        /*
         * Input widgets
         */
        Group main = new Group(container, SWT.BORDER);
        main.setText("Source folder information");
        // Group layout
        GridLayout groupLayout = new GridLayout(3,false);
        main.setLayout(groupLayout);
        GridData groupData = new GridData(GridData.FILL_HORIZONTAL);
        groupData.grabExcessHorizontalSpace = true;
        main.setLayoutData(groupData);
        // Project name widgets
        Label nameLabel = new Label(main, SWT.NONE);
        nameLabel.setText("Project");
        m_projectName = new Text(main, SWT.BORDER);
        m_projectName.setEditable(false);
        m_projectName.setLayoutData(GridDataFactory.copyData(groupData));
        m_projectName.addModifyListener(modify);
        Button browseProject = new Button(main, SWT.PUSH);
        browseProject.setText("Browse");
        browseProject.addSelectionListener(new SelectionAdapter(){
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		/* Open the project selection dialog */
                ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new WorkbenchLabelProvider());
                dialog.setTitle("Project selection");
                dialog.setTitle("Select a project.");
                dialog.setElements(ResourcesPlugin.getWorkspace().getRoot().getProjects());
                dialog.open();
                
                Object[] result = dialog.getResult();
                if(result != null && result.length > 0){
                    m_projectName.setText(((IProject)result[0]).getName());
                }
        	}
        });
        // Source folder widgets
        Label folderLabel = new Label(main, SWT.NONE);
        folderLabel.setText("Folder name");
        m_folderName = new Text(main, SWT.BORDER);
        m_folderName.setLayoutData(GridDataFactory.copyData(groupData));
        m_folderName.addModifyListener(modify);
        
        /*
         * Initialize the widgets
         */
        
        if (m_selection != null)
        {
        	Object first = m_selection.getFirstElement();
        	if (first instanceof IResource)
        	{
        		IResource resource = (IResource) first;
        		IProject project = resource.getProject();
        		m_projectName.setText(project.getName());
        		browseProject.setEnabled(false);
        	}
        	System.out.println("SourceFolderWizardPage.createControl()");
        }
        
        validatePage();
        
        /* Set control */
        setControl(container);
	}

	/***************************************************************************
	 * Return the project where the source folder should be created
	 * @return the project's name where the folder should be created
	 **************************************************************************/
	public String getProject()
	{
		return m_projectName.getText();
	}
	
	/***************************************************************************
	 * Get folder's name
	 * @return the folder's name, or an empty string if it has not been set
	 **************************************************************************/
	public IPath getFolderPath()
	{
		String projectName = m_projectName.getText();
		String folderName = m_folderName.getText();
		
		Path path = new Path(Path.SEPARATOR + projectName + Path.SEPARATOR + folderName + Path.SEPARATOR);
		return path;
	}
	
	/***************************************************************************
	 * Check user's input values are correct
	 * @return
	 **************************************************************************/
	private boolean validatePage()
	{
		String projectName = m_projectName.getText();
		String folderName = m_folderName.getText();
		
		String errorName = "";
		if (projectName.isEmpty())
		{
			errorName += "Project has not been set";
		}
		if (folderName.isEmpty())
		{
			errorName += "\nFolder has not been set";
		}
		else
		{
			/*
			 * Check if the folder already exists
			 */
			Path path = new Path(Path.SEPARATOR + projectName + Path.SEPARATOR + folderName + Path.SEPARATOR);
			IResource res = (IWorkspaceRoot) ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (res != null)
			{
				errorName += "\nFolder already exists";
			}
		}
		
		/*
		 * Check if there are errors. Then, update the error message
		 */
		boolean valid = errorName.isEmpty();
		if (!valid)
		{
			setErrorMessage(errorName.trim());
		}
		else{
			setErrorMessage(null);
		}
		setPageComplete(valid);
		return valid;
	}
}
