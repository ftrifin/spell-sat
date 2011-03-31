///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck
//
// FILE      : SemanticsChecker.java
//
// DATE      : Feb 7, 2011
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
package com.astra.ses.spell.dev.scheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.dev.scheck.interfaces.ComparableResource;
import com.astra.ses.spell.dev.scheck.interfaces.IIssue;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.dev.scheck.processing.KnownFunctionsRegistry;
import com.astra.ses.spell.dev.scheck.processing.RuleCheckerManager;

/******************************************************************************
 * Singleton providing access to the Semantics Check functionality
 *****************************************************************************/
public class SemanticsChecker 
{
	/** Holds the singleton instance */
	private static SemanticsChecker s_instance = null;
	/** Manages the available rule checkers */
	private RuleCheckerManager m_checkerManager;
	/** True when a check is being performed */
	private boolean m_busy;
	
	/**************************************************************************
	* Constructor.
	**************************************************************************/
	private SemanticsChecker()
	{
		m_checkerManager = new RuleCheckerManager();
		m_busy = false;
	}
	
	/**************************************************************************
	* Singleton instance.
	**************************************************************************/
	public static SemanticsChecker instance()
	{
		if (s_instance == null)
		{
			s_instance = new SemanticsChecker();
		}
		return s_instance;
	}
	
	/**************************************************************************
	* Initialize the checkers at plugin activation
	**************************************************************************/
	public void initialize()
	{
		m_checkerManager.loadCheckers();
		// Load known functions
		KnownFunctionsRegistry.instance().loadKnownFunctions();
		// Initialize resource helper
		ResourceManager.instance();
	}

	/**************************************************************************
	* Set the busy state
	**************************************************************************/
	protected synchronized void setBusy( boolean busy )
	{
		m_busy = busy;
	}

	/**************************************************************************
	* Get the busy state
	**************************************************************************/
	protected synchronized boolean isBusy()
	{
		return m_busy;
	}

	/**************************************************************************
	* Perform the checks agains a list of given resources. Resources are
	* expanded by the resource processor, to finally get a list of files.
	**************************************************************************/
	public int check( List<IFile> items, Map<ComparableResource,IIssueList> issues, IProgressMonitor monitor )
	{
		// Do not perform checks in parallel
		if (isBusy()) return 0;
		setBusy(true);
		// Total files processed
		int total = 0;
		try
		{
			// Prepare other resources (database, userlib...)
			monitor.subTask("Loading UserLib");
			List<IProject> projectsInvolved = new ArrayList<IProject>();
			ResourceManager.instance().findProjects(items, projectsInvolved);
			for(IProject project : projectsInvolved)
			{
				UserLibRegistry.instance().loadUserLibFunctions( project, issues, monitor, false );
			}
	
			if (!monitor.isCanceled()) 
			{
				// Passed items are all IFiles
				monitor.subTask("Starting checks");
				checkInternal( items, issues, monitor );
			}

			total = items.size();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			setBusy(false);
		}
		return total;
	}
	
	/**************************************************************************
	* Perform the checks against a list of IFile instances.
	**************************************************************************/
	private void checkInternal( List<IFile> items, Map<ComparableResource,IIssueList> issues, IProgressMonitor monitor )
	{
		monitor.beginTask("Checking " + items.size() + " procedure files", items.size());
		for(IResource item : items)
		{
			IFile file = (IFile) item;
			IIssueList issuesForFile = null;
			// Obtain or create the associated issue list
			if (!issues.containsKey( new ComparableResource(item) ))
			{
				issuesForFile = IssueFactory.createIssueList(item);
				issues.put( new ComparableResource(item), issuesForFile );
			}
			else
			{
				issuesForFile = issues.get( new ComparableResource(item) );
			}
			// Perform the checks
			performCheck( file, issuesForFile, monitor );
			monitor.worked(1);
		}
	}
	
	/**************************************************************************
	* Perform the check against a particular file.
	**************************************************************************/
	private void performCheck( IFile file, IIssueList issues, IProgressMonitor monitor )
	{
		try 
		{
			if (monitor.isCanceled()) return;
			monitor.subTask("Checking file " + file.getName());
			if (monitor.isCanceled()) return;
			// Static checks, will fill the list of issues
			m_checkerManager.performStaticChecks(issues, monitor);
			if (monitor.isCanceled()) return;
			// Event checks, will fill the list of issues
			m_checkerManager.performEventChecks(issues, monitor);
		} 
		catch (Exception e) 
		{
			// Notify the issue: could not process this file
			IIssue issue = IssueFactory.createErrorIssue( "Cannot process file " + file.getName() +
									  " in " + file.getParent().getName() + ": " + e.getLocalizedMessage());
			issues.addIssue( issue );
		}
	}
}
