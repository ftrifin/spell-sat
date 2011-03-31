////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards.pages
// 
// FILE      : PackageWizardPage.java
//
// DATE      : 2010-07-13
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
import org.python.pydev.ui.dialogs.PythonPackageSelectionDialog;
import org.python.pydev.ui.dialogs.SourceFolder;

public class PackageWizardPage extends WizardPage {

	/** Page name */
	private static final String PAGE_NAME = "Package information";
	
	/** Source folder inptu widget */
	private Text m_sourceFolder;
	/** Package name widget */
	private Text m_package;
	
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public PackageWizardPage(IStructuredSelection selection) {
		super(PAGE_NAME);
		setTitle("Package");
		setDescription("Create a new package inside a source folder or another package");
	}

	@Override
	public void createControl(Composite parent) {
		/*
		 * Main container
		 */
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1,true);
        container.setLayout(gridLayout);
        
        ModifyListener modify = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		};
        
        /*
         * Package location group
         */
        Group packageGroup = new Group(container, SWT.BORDER);
        packageGroup.setText("Package information");
        GridLayout groupLayout = new GridLayout(3, false);
        GridData groupData = new GridData(GridData.FILL_HORIZONTAL);
        groupData.grabExcessHorizontalSpace = true;
        packageGroup.setLayout(groupLayout);
        packageGroup.setLayoutData(groupData);
        // SOURCE FOLDER LABEL
        Label sourceFolderLabel = new Label(packageGroup, SWT.NONE);
        sourceFolderLabel.setText("Source folder");
        m_sourceFolder = new Text(packageGroup, SWT.BORDER | SWT.READ_ONLY);
        m_sourceFolder.setLayoutData(GridDataFactory.copyData(groupData));
        Button browseSourceFolder = new Button(packageGroup, SWT.PUSH);
        browseSourceFolder.setText("Browse");
        browseSourceFolder.addSelectionListener(new SelectionAdapter()
        {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		PythonPackageSelectionDialog dialog = new PythonPackageSelectionDialog(getShell(), false);
        		dialog.open();
        		Object firstResult = dialog.getFirstResult();
        		if(firstResult instanceof SourceFolder){ //it is the default package
        			SourceFolder f = (SourceFolder) firstResult;
        			m_sourceFolder.setText(f.folder.getFullPath().toString());
        			m_package.setText("");

        		}
        		if(firstResult instanceof org.python.pydev.ui.dialogs.Package){
        			org.python.pydev.ui.dialogs.Package f = (org.python.pydev.ui.dialogs.Package) firstResult;
        			m_sourceFolder.setText(f.sourceFolder.folder.getFullPath().toString());
        			m_package.setText(f.getPackageName());
        		}
        	}
        });
        // PACKAGE NAME
        Label packageNameLabel = new Label(packageGroup, SWT.NONE);
        packageNameLabel.setText("Package name");
        m_package = new Text(packageGroup, SWT.BORDER);
        m_package.setLayoutData(GridDataFactory.copyData(groupData));
        m_package.addModifyListener(modify);
        /*
         * Validate the whole page
         */
        validatePage();
        
        /*
         * Set page's control
         */
        setControl(container);
	}

	/***************************************************************************
	 * Get the package's path to create
	 * @return
	 **************************************************************************/
	public IPath getPackagePath()
	{
		String sourceFolder = m_sourceFolder.getText();
		String packageName = m_package.getText();
		packageName = packageName.replace('.', '/');
        IPath path = new Path(sourceFolder + IPath.SEPARATOR + packageName);
        return path;
	}
	
	/***************************************************************************
	 * Get the selected source folder
	 * @return
	 **************************************************************************/
	public IPath getSourceFolder()
	{
		String sourceFolder = m_sourceFolder.getText();
		return new Path(sourceFolder);
	}
	
	/***************************************************************************
	 * 
	 * @return
	 **************************************************************************/
	public boolean validatePage()
	{
		String sourceFolder = m_sourceFolder.getText();
		String packageName = m_package.getText();
		String errors = "";
		// Check source folder widget is not empty
		if (sourceFolder.isEmpty())
		{
			errors += "\nSource folder is empty";
		}
		// Check package name is not empty
		if (packageName.isEmpty())
		{
			errors += "\nPackage name is empty";
		}
		else if (!packageName.isEmpty() && !sourceFolder.isEmpty())
		{
	        if(packageName.indexOf('/') != -1){
	        	errors += "\nThe package name must not contain '/'.";
	        }
	        if(packageName.indexOf('\\') != -1){
	        	errors += "\nThe package name must not contain '\\'.";
	        }
	        if(packageName.endsWith(".")){
	        	errors += "\nThe package may not end with a dot";
	        }
	        packageName = packageName.replace('.', '/');
	        IPath path = new Path(sourceFolder + IPath.SEPARATOR + packageName);
	        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	        IResource resource = root.findMember(path);
	        if (resource != null)
	        {
	        	errors += "\nPackage already exists";
	        }
		}

        boolean valid = errors.isEmpty();
        if (!valid)
        {
        	setErrorMessage(errors.trim());
        }
        else
        {
        	setErrorMessage(null);
        }
        setPageComplete(valid);
		return valid;
	}
}
