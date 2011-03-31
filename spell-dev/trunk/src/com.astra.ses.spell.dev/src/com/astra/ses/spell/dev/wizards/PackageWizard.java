////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards
// 
// FILE      : PackageWizard.java
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
package com.astra.ses.spell.dev.wizards;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.astra.ses.spell.dev.wizards.pages.PackageWizardPage;

/*******************************************************************************
 * 
 * Package wizard helps users to create a package inside a source folder
 *
 ******************************************************************************/
public class PackageWizard extends Wizard  implements INewWizard {

	/** WIZARD ID */
	public static final String WIZARD_ID = 
		"com.astra.ses.spell.dev.wizard.newpackage";
	
	/** Wizard page */
	private PackageWizardPage m_page;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		createWizardPages(selection);
	}
	
	/***************************************************************************
	 * Create wizard pages
	 * @param selection navigator view selected elements
	 **************************************************************************/
	private void createWizardPages(IStructuredSelection selection)
	{
		m_page = new PackageWizardPage(selection);
		addPage(m_page);
	}
	
	@Override
	public boolean performFinish() {
		// Get the path where the package shall be created
		IPath packagePath = m_page.getPackagePath();
		IPath sourceFolderPath = m_page.getSourceFolder();
		IPath relative = packagePath.makeRelativeTo(sourceFolderPath);
		// Process the path so any non existing package should be created
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IContainer currentContainer = root.getFolder(sourceFolderPath);
		IPath currentPath = sourceFolderPath;
		for (String segment : relative.segments())
		{
			IPath newFolderPath = currentPath.append(segment);
			IFolder newFolder = currentContainer.getFolder(new Path(segment));

			try {
				if (!newFolder.exists())
				{
					//Create the folder
					newFolder.create(true, true, null);
					//Create the package definition file
					IFile defFile = newFolder.getFile("__init__.py");
					defFile.create(new ByteArrayInputStream(new byte[0]), true, null);
				}
				currentPath = newFolderPath;
				currentContainer = newFolder;
			} catch (CoreException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

}
