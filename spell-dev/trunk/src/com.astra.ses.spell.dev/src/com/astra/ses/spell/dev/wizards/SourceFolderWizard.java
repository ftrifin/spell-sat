////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards
// 
// FILE      : SourceFolderWizard.java
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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.python.pydev.plugin.nature.PythonNature;

import com.astra.ses.spell.dev.wizards.pages.SourceFolderWizardPage;

/*******************************************************************************
 * 
 * Source folder wizard helps users to create a source folder for one project
 *
 ******************************************************************************/
public class SourceFolderWizard extends Wizard implements INewWizard {

	/** WIZARD ID */
	public static final String WIZARD_ID = 
		"com.astra.ses.spell.dev.wizard.newsourcefolder";
	
	/** Wizard page */
	private SourceFolderWizardPage m_wizardPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		createWizardPages(selection);
	}

	/***************************************************************************
	 * Fill this wizard with the appropiate pages
	 **************************************************************************/
	private void createWizardPages(IStructuredSelection selection)
	{
		/*
		 * Project info wizard page
		 */
		m_wizardPage = new SourceFolderWizardPage(selection);
		addPage(m_wizardPage);
	}
	
	@Override
	public boolean performFinish() {
		boolean result = false;
		IPath folderPath = m_wizardPage.getFolderPath();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			IFolder folder = root.getFolder(folderPath);
			folder.create(true,true, null);
			/*
			 * add the source folder to the Python nature
			 */
			PythonNature nature = (PythonNature) folder.getProject().getNature(PythonNature.PYTHON_NATURE_ID);
			String sourcePath = nature.getPythonPathNature().getProjectSourcePath(false);
			sourcePath += "|" + folder.getFullPath().toOSString();
			nature.getPythonPathNature().setProjectSourcePath(sourcePath);
			result = true;
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}
		return result;
	}
}
