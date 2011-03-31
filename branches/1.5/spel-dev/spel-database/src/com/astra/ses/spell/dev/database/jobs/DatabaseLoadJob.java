///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.jobs
// 
// FILE      : DatabaseLoadJob.java
//
// DATE      : 2009-09-14
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database.jobs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.astra.ses.spell.dev.database.Activator;
import com.astra.ses.spell.dev.database.DatabaseDriverManager;
import com.astra.ses.spell.dev.database.DatabaseManager;
import com.astra.ses.spell.dev.database.exception.DatabaseLoadException;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabaseDriver;


/**************************************************************************
 * DatabaseLoadJob class will try to retrieve a ISpellDatabase in a 
 * different thread
 * @author jpizar
 *
 *************************************************************************/
public class DatabaseLoadJob extends Job
{
	/** Current project */
	private IProject m_project;
	/** Driver Name */
	private String m_driverName;
	/** DB path */
	private String m_path;
	
	/**********************************************************************
	 * Constructor
	 * @param driver
	 * @param path
	 *********************************************************************/
	public DatabaseLoadJob(IProject project, String driver, String path)
	{
		super("Loading database ...");
		m_project = project;
		m_driverName = driver;
		m_path = path;
	}
	
	@Override
	public IStatus run(IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		ISpellDatabase loadedDatabase = null;
		monitor.beginTask("Loading database for project " + m_project.getName(), IProgressMonitor.UNKNOWN);
		try
		{
			ISpellDatabaseDriver driver = DatabaseDriverManager.getInstance().getDriver(m_driverName);
			if (driver == null)
			{
				return Status.CANCEL_STATUS;
			}
			boolean correctDb = driver.checkDatabase(m_path);
			if (!correctDb)
			{
				String errMessage = "Database for project " + m_project.getName() + " is not accessible";
				result = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errMessage, new DatabaseLoadException(errMessage));
			}
			else
			{
				try {
					loadedDatabase = driver.loadDatabase(m_path, monitor);
					DatabaseManager.getInstance().setDB(m_project, loadedDatabase);
				} catch (DatabaseLoadException e) {
					String errMessage = "Error loading database for project " + m_project.getName();
					result = new Status(IStatus.ERROR, Activator.PLUGIN_ID, errMessage, new DatabaseLoadException(errMessage));
				}
			}
		}
		finally
		{
			monitor.done();
		}
		if (monitor.isCanceled())
		{
			result = Status.CANCEL_STATUS;
		}
		return result;
	}
}
