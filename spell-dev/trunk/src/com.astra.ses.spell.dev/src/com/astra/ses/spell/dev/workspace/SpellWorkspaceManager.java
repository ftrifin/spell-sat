///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.context.workspace
// 
// FILE      : SpellWorkspaceManager.java
//
// DATE      : 2008-11-21 13:54
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.workspace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.astra.ses.spell.dev.workspace.exception.WorkspaceException;


/******************************************************************************
 * This class will provide information about the resources that projects 
 * have attached, such as database driver or recent loaded database
 *****************************************************************************/
public class SpellWorkspaceManager implements IPartListener2, IStartup {

	private static final String CONTEXT_CHANGES_EXTENSION_ID = "com.astra.ses.spell.dev.workspace.SpellContextListener";
	private static final String ELEMENT_CLASS = "class";

	/** Map for handling opened procedures */
	private Map<IProject, Collection<IFile>> m_openedProcedures;
	/** Collection of workspace listeners */
	private ArrayList<IWorkspaceListener> m_workspaceListeners;

	
	/**************************************************************************
	 * Constructor 
	 *************************************************************************/
	public SpellWorkspaceManager()
	{
		m_openedProcedures = new HashMap<IProject, Collection<IFile>>();
		m_workspaceListeners = new ArrayList<IWorkspaceListener>();		
		loadExtensions();
	}
	
	/***************************************************************************
	 * Stop listening part events
	 **************************************************************************/
	public void dispose()
	{
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(this);
	}
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(true);
		EditorPart editor = (EditorPart) part.getAdapter(EditorPart.class);
		if (editor == null)
		{
			return;
		}
		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (file != null)
		{
			/*
			 * If IFile != null that means that a procedure inside the workspace
			 * has been focused
			 */
			checkProcedureOpened(file);
			procedureFocused(file);
		}
		else
		{
			// Notify the listeners that a non workspace file has been focused
			for (IWorkspaceListener listener : m_workspaceListeners)
			{
				listener.fileFocused();
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(true);
		EditorPart editor = (EditorPart) part.getAdapter(EditorPart.class);
		if (editor == null)
		{
			return;
		}
		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (file != null)
		{
			// If IFile != null that means that a procedure inside the workspace
			// has been closed. If the project has been closed, unload the database.
			if (!file.getProject().isOpen())
			{
				projectClosed(file.getProject());
			}
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(true);
		EditorPart editor = (EditorPart) part.getAdapter(EditorPart.class);
		if (editor == null)
		{
			return;
		}
		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		if (file != null)
		{
			/*
			 * If IFile != null that means that a procedure inside the workspace
			 * has been opened
			 */
			procedureOpened(file);
		}
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {}
	
	/***************************************************************************
	 * A procedure has been brought to top in the editor Area
	 * @param procedureFile
	 **************************************************************************/
	private void procedureFocused(IFile procedureFile)
	{
		// Notify the listeners
		for (IWorkspaceListener listener : m_workspaceListeners)
		{
			listener.procedureFocused(procedureFile);
		}
	}
	
	/***************************************************************************
	 * A procedure file has been opened, so there is need of loading the 
	 * database if it has one
	 * @param procedureFile
	 * @throws WorkspaceException 
	 **************************************************************************/
	private void procedureOpened(IFile procedureFile)
	{
		// The database for that project shall be opened
		IProject project = procedureFile.getProject();
		if (m_openedProcedures.containsKey(project))
		{
			m_openedProcedures.get(project).add(procedureFile);
		}
		else
		{
			ArrayList<IFile> projectOpenedFiles = new ArrayList<IFile>();
			projectOpenedFiles.add(procedureFile);
			m_openedProcedures.put(project, projectOpenedFiles);
		}
		// Notify the listeners
		for (IWorkspaceListener listener : m_workspaceListeners)
		{
			try {
				listener.procedureOpened(procedureFile, new NullProgressMonitor());
			} catch (WorkspaceException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "An error ocurred", e.getMessage());
			}
		}
	}
	
	/**************************************************************************
	 * Close the project and unload the database if necessary
	 *************************************************************************/
	private void projectClosed( IProject project )
	{
		//Remove from opened procedures and unload the database if necessary
		m_openedProcedures.get(project).remove(project);
		// Notify the listeners
		for (IWorkspaceListener listener : m_workspaceListeners)
		{
			listener.projectClosed(project);
		}
	}

	/**************************************************************************
	 * When restoring the workbench at startup, listeners are not notified
	 * about opened procedures
	 * @param procedureFile
	 *************************************************************************/
	private void checkProcedureOpened(IFile procedureFile)
	{
		IProject project = procedureFile.getProject();
		if (m_openedProcedures.containsKey(project))
		{
			if (m_openedProcedures.get(project).contains(procedureFile))
			{
				return;
			}
			m_openedProcedures.get(project).add(procedureFile);
		}
		else
		{
			ArrayList<IFile> projectOpenedFiles = new ArrayList<IFile>();
			projectOpenedFiles.add(procedureFile);
			m_openedProcedures.put(project, projectOpenedFiles);
		} 
		// Notify the listeners
		for (IWorkspaceListener listener : m_workspaceListeners)
		{
			try {
				listener.procedureOpened(procedureFile, new NullProgressMonitor());
			} catch (WorkspaceException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "An error ocurred", e.getMessage());
			}
		}
	}
	
	@Override
	public void earlyStartup() {
		 final IWorkbench workbench = PlatformUI.getWorkbench();
		 
		 final IPartListener2 listener = this;
		 
		 workbench.getDisplay().asyncExec(new Runnable() {
		   public void run() {
		     IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		     if (window != null) {
		       for (IWorkbenchPage page : window.getPages())
		       {
		    	   /*
		    	    * Opened editor while workbench startup must be tracked
		    	    */
		    	   for (IEditorReference edReference : page.getEditorReferences())
		    	   {
		    			IEditorPart editor = edReference.getEditor(false);
		    			if (editor !=  null)
		    			{
			    			IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
			    			procedureOpened(file);
			    			if (page.getActiveEditor() == editor)
			    			{
			    				procedureFocused(file);
			    			}
		    			}
		    	   }
		       }
		       /*
		        * Start listening to part events
		        */
		       window.getPartService().addPartListener(listener);
		     }
		   }
		 });
	}
	
	/***************************************************************************
	 * Load objects which extends Context Changes extension point
	 **************************************************************************/
	private void loadExtensions()
	{
		System.out.println("[*] Loading extensions for point '" + CONTEXT_CHANGES_EXTENSION_ID + "'");
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(CONTEXT_CHANGES_EXTENSION_ID);
		IExtension[] extensions = ep.getExtensions();
		for(IExtension extension : extensions)
		{
			System.out.println("[*] Extension ID: "+ extension.getUniqueIdentifier());
			// Obtain the configuration element for this extension point
			IConfigurationElement cfgElem = extension.getConfigurationElements()[0];
			try
			{
				IWorkspaceListener listener = 
					(IWorkspaceListener) IWorkspaceListener.class.cast(cfgElem.createExecutableExtension(ELEMENT_CLASS));
				m_workspaceListeners.add(listener);
			}
			catch(CoreException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
