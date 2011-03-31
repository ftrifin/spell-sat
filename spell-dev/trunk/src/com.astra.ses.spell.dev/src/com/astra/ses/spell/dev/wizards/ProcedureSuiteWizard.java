///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards
// 
// FILE      : ProcedureSuiteWizard.java
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
package com.astra.ses.spell.dev.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;

import com.astra.ses.spell.dev.config.ConfigurationManager;
import com.astra.ses.spell.dev.resources.builder.SpellProjectVisitor;
import com.astra.ses.spell.dev.resources.nature.SpellNature;
import com.astra.ses.spell.dev.wizards.pages.ProjectInfoWizardPage;

/*******************************************************************************
 *
 * Procedure suite wizard eases the creation of new procedure suites
 * in a graphical way
 *
 ******************************************************************************/
public class ProcedureSuiteWizard extends Wizard implements INewWizard {

	/** WIZARD ID */
	public static final String WIZARD_ID = 
		"com.astra.ses.spell.dev.wizard.newproject";

	/** Project page */
	private ProjectInfoWizardPage m_projectPage;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		createWizardPages();
	}
	
	@Override
	public boolean performFinish() {
		createNewProject();
		return true;
	}
	
	/***************************************************************************
	 * Fill this wizard with the appropiate pages
	 **************************************************************************/
	private void createWizardPages()
	{
		/*
		 * Project info wizard page
		 */
		m_projectPage = new ProjectInfoWizardPage();
		addPage(m_projectPage);
	}
	
    /***************************************************************************
     * Creates a new project resource with the entered name.
     * 
     * @return the created project resource, or <code>null</code> if the project
     * was not created
     **************************************************************************/
    private IProject createNewProject() {
        // Get a project handle
    	String projectName = m_projectPage.getProjectName();
    	final String pythonVersion = m_projectPage.getInterpreterVersion();
    	final boolean defaultStructure = m_projectPage.createDefaultStructure();
    	
        final IProject newProjectHandle = 
        	ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        // Get a project descriptor
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProjectDescription description = 
        	workspace.newProjectDescription(newProjectHandle.getName());
        // New project path will the default workspace path
    	IPath location = null;
    	if (!m_projectPage.createDefaultStructure())
    	{
    		File custom = new File(m_projectPage.getProjectLocation().trim());
    		URI customURI = custom.toURI();
    		URI defaultURI = Platform.getLocation().append(projectName).toFile().toURI();
    		if (!defaultURI.equals(customURI))
    		{
    			location = new Path(m_projectPage.getProjectLocation().trim());
    		}
    	}
        description.setLocation(location);

        // define the operation to create a new project
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor) throws CoreException {
                createProject(description, newProjectHandle, pythonVersion, monitor, defaultStructure);
            }
        };

        // run the operation to create a new project
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof CoreException) {
                if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
                    MessageDialog.openError(getShell(), "IDEWorkbenchMessages.CreateProjectWizard_errorTitle", "IDEWorkbenchMessages.CreateProjectWizard_caseVariantExistsError");
                } else {
                    ErrorDialog.openError(getShell(), "IDEWorkbenchMessages.CreateProjectWizard_errorTitle", null, ((CoreException) t).getStatus());
                }
            } else {
                // Unexpected runtime exceptions and errors may still occur.
                PydevPlugin.log(IStatus.ERROR, t.toString(), t);
                MessageDialog.openError(getShell(), "IDEWorkbenchMessages.CreateProjectWizard_errorTitle", t.getMessage());
            }
            return null;
        }
        return newProjectHandle;
    }
    
    /***************************************************************************
     * Creates a project resource given the project handle and description.
     * 
     * @param description the project description to create a project resource 
     *        for
     * @param projectHandle the project handle to create a project resource for
     * @param monitor the progress monitor to show visual progress with
     * @param projectType
     * @param projectInterpreter 
     * 
     * @exception CoreException if the operation fails
     * @exception OperationCanceledException if the operation is canceled
     ***************************************************************************/
    private void createProject(IProjectDescription description, IProject project,
    		String version, IProgressMonitor monitor, boolean defaultStructure) 
    throws CoreException, OperationCanceledException {
    	
        monitor.beginTask("", 2000);
        project.create(description, new SubProgressMonitor(monitor, 1000));
        if (monitor.isCanceled()){
            throw new OperationCanceledException();
        }
    	try
    	{
			project.open(IProject.BACKGROUND_REFRESH, monitor);
            /*
             * Add SPELL nature to the project
             */
            SpellNature.addNature(project, monitor, defaultStructure);
            // Create the folder structure
            SpellProjectVisitor visitor = new SpellProjectVisitor();
            project.accept(visitor,IContainer.DEPTH_ONE, false);
            /*
             * Adding Python project nature
             */
			project.open(IProject.BACKGROUND_REFRESH, monitor);
            String sourceFolder = ConfigurationManager.getInstance().getProjectSourceFolder();
            IPath sourcePath = project.getFullPath();
            if (sourceFolder != null)
            {
            	sourcePath = sourcePath.append(sourceFolder); 
            }
            String interpreter = "python " + version;
            //PythonNature.addNature(project, monitor,interpreter,sourcePath.toString(),null);
            /*
             * For python 1.5.7 addNature method interface has been extended
             */
            PythonNature.addNature(project, monitor,interpreter,sourcePath.toString(),null,null,null);
    	}
    	finally
    	{
    		monitor.done();
    	}
    }
}
