///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database
// 
// FILE      : DatabaseManager.java
//
// DATE      : 2008-11-21 13:54
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.dev.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.astra.ses.spell.dev.context.workspace.IWorkspaceListener;
import com.astra.ses.spell.dev.context.workspace.exception.WorkspaceException;
import com.astra.ses.spell.dev.database.exception.DatabaseLoadException;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.jobs.DatabaseLoadJob;
import com.astra.ses.spell.dev.database.properties.DatabasePropertiesManager;

/******************************************************************************
 * Class for managing the different databases from the different projects
 * @author jpizar
 *****************************************************************************/
public class DatabaseManager implements IWorkspaceListener {
	
	/**************************************************************************
	 * Objects interested in knowing the database in use must implement
	 * this interface
	 * @author jpizar
	 *************************************************************************/
	public interface IWorkingDatabaseListener
	{
		/**********************************************************************
		 * Database in use has changed
		 * @param db
		 *********************************************************************/
		public void workingDatabaseChanged(ISpellDatabase db);
	}
	
	/** Singleton instance */
	private static DatabaseManager s_instance;
	/** Working Database */
	private IProject m_workingProject;
	/** Working Database Listeners */
	private ArrayList<IWorkingDatabaseListener> m_listeners;
	/** Map between projects and strings */
	private Map<IProject, ISpellDatabase> m_projectDatabase;
	
	/**************************************************************************
	 * Singleton instance retrieval
	 * @return
	 *************************************************************************/
	public static DatabaseManager getInstance()
	{
		if (s_instance == null)
		{
			s_instance = new DatabaseManager();
		}
		return s_instance;
	}
	
	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public DatabaseManager()
	{
		m_listeners = new ArrayList<IWorkingDatabaseListener>();
		m_projectDatabase = new HashMap<IProject, ISpellDatabase>();
	}
	
	/**************************************************************************
	 * Get the database for the given project
	 * @return
	 *************************************************************************/
	public ISpellDatabase getProjectDatabase(IResource resource)
	{
		return m_projectDatabase.get(resource.getProject());
	}
	
	/**************************************************************************
	 * Load the database for the given project
	 *************************************************************************/
	public void setDB(IProject project, ISpellDatabase newDB)
	{
		m_projectDatabase.put(project, newDB);
		DatabasePropertiesManager manager = new DatabasePropertiesManager(project);
		manager.setDatabasePath(newDB.getDatabasePath());
		if (project.equals(m_workingProject))
		{
			ISpellDatabase workingDB = m_projectDatabase.get(m_workingProject);
			notifyListeners(workingDB);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// WORKSPACE LISTENER METHODS
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void procedureOpened(IFile opened, IProgressMonitor monitor)
			throws WorkspaceException {
		IProject resourceProject = opened.getProject();
		// If database has not still been loaded, we need to load it
		if (!m_projectDatabase.containsKey(resourceProject))
		{
			try {
				loadProjectdatabase(resourceProject, monitor);
			} catch (DatabaseLoadException e) {
				notifyListeners(null);
				throw new WorkspaceException(e.getMessage());
			}
		}
	}
	
	@Override
	public void procedureFocused(IFile opened) {
		IProject resourceProject = opened.getProject();
		m_workingProject = resourceProject;
		ISpellDatabase workingDatabase = getProjectDatabase(resourceProject);
		notifyListeners(workingDatabase);
	}

	@Override
	public void projectClosed(IProject project) {
		// Remove the database
		m_projectDatabase.remove(project);
		if (project.equals(m_workingProject))
		{
			notifyListeners(null);
		}
	}

	@Override
	public void fileFocused() {
		/*
		 * Notify the listeners for not to use any database
		 */
		notifyListeners(null);
	}
	
	/***************************************************************************
	 * Return working project
	 * @return
	 **************************************************************************/
	public IProject getFocusedProject()
	{
		return m_workingProject;
	}
	
	/***************************************************************************
	 * Refresh project's database
	 * @param project
	 **************************************************************************/
	public void refreshProjectDatabase(IProject project)
	{
		try {
			loadProjectdatabase(project, new NullProgressMonitor());
		} catch (DatabaseLoadException e) {
			return;
		}
	}
	
	/***************************************************************************
	 * Load the database for the given project
	 * @param project
	 * @throws DatabaseLoadException 
	 **************************************************************************/
	private void loadProjectdatabase(IProject project, IProgressMonitor monitor) 
			throws DatabaseLoadException
	{
		DatabasePropertiesManager manager = new DatabasePropertiesManager(project);
		String driver = manager.getDatabaseDriverName();
		String path = manager.getDatabasePath();
		DatabaseLoadJob dbLoad = new DatabaseLoadJob(project, driver, path);
		IStatus result = dbLoad.run(monitor);
		if (result.getSeverity() == IStatus.ERROR)
		{
			throw new DatabaseLoadException(result.getMessage());
		}
	}
	
	/***************************************************************************
	 * Notify listeners about the current database in use
	 **************************************************************************/
	private void notifyListeners(ISpellDatabase db)
	{
		/*
		 * Notify the listeners for not to use any database
		 */
		for (IWorkingDatabaseListener listener : m_listeners)
		{
			listener.workingDatabaseChanged(db);
		}
	}

	/***************************************************************************
	 * Register a working database listener
	 **************************************************************************/
	public void registerListener(IWorkingDatabaseListener listener) {
		m_listeners.add(listener);
		ISpellDatabase workingDB = m_projectDatabase.get(m_workingProject);
		if (workingDB != null)
		{
			listener.workingDatabaseChanged(workingDB);
		}
	}

	/***************************************************************************
	 * Unregister a working database listener
	 **************************************************************************/
	public void unregisterListener(IWorkingDatabaseListener listener) {
		m_listeners.remove(listener);
	}
}
