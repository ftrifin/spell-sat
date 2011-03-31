///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.processing
//
// FILE      : UserLibRegistry.java
//
// DATE      : Feb 14, 2011
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.dev.scheck.interfaces.ComparableResource;
import com.astra.ses.spell.dev.scheck.interfaces.IIssue;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.dev.scheck.processing.UserLibParser;
import com.astra.ses.spell.language.ParseException;
import com.astra.ses.spell.language.Parser;
import com.astra.ses.spell.language.model.TokenMgrError;

public class UserLibRegistry 
{
	private static UserLibRegistry s_instance = null;
	
	private Map<String,List<String>> m_userLibFunctions;
	
	
	public static UserLibRegistry instance()
	{
		if (s_instance == null)
		{
			s_instance = new UserLibRegistry();
		}
		return s_instance;
	}
	
	private UserLibRegistry()
	{
		m_userLibFunctions = new TreeMap<String,List<String>>();
	}

	public void reload( IProgressMonitor monitor )
	{
		List<String> projects = new ArrayList<String>(); 
		for( String projectId : m_userLibFunctions.keySet() ) projects.add(projectId);
		monitor.beginTask("Reload user libraries for " + projects.size() + " projects", projects.size());
		for( String projectId : projects )
		{
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectId);
			monitor.subTask("Project " + projectId);
			loadUserLibFunctions( project, null, monitor, true );
			monitor.worked(1);
			if (monitor.isCanceled()) return;
		}
	}
	
	public void loadUserLibFunctions( IProject project, Map<ComparableResource,IIssueList> issues, IProgressMonitor monitor, boolean force )
	{
		String projectId = project.getName();
		// Reset list
		if (m_userLibFunctions.containsKey(projectId))
		{
			if (force)
			{
				m_userLibFunctions.remove(projectId);
			}
			else
			{
				return;
			}
		}

		IFolder folder = project.getFolder("UserLib");
		
		// If no userlib folder, give up
		if (folder == null) return;
		
		List<IFile> files = new ArrayList<IFile>();
		try 
		{
			ResourceManager.instance().findFiles( Arrays.asList(folder.members()), files, null, monitor);
		}
		catch (CoreException e) 
		{
			e.printStackTrace();
		}

		// If no files in userlib, give up
		if (files.size()==0) return;

		System.err.println("[SEMANTICS] Processing user library files: " + projectId);
		monitor.subTask("Processing user libraries for project '" + projectId + "'");

		// Will hold the userlib functions for this project
		m_userLibFunctions.put( projectId, new ArrayList<String>() );
		
		// Notify monitor
		monitor.beginTask("Processing " + files.size() + " user libraries", files.size());
		
		// Will parse each userlib file searching for functions
		Parser codeParser = new Parser(); 
		for(IResource file : files)
		{
			if (file.getName().equals("__init__.py")) continue;
			if (!file.getFileExtension().equals("py")) continue;
			monitor.subTask("Parsing '" + file.getName() + "'");
			
			if (monitor.isCanceled()) return;
			
			String source = ResourceManager.instance().getSource( (IFile)file );
			// Hack required for userlib, dont ask my what is going on with the parser...$#%#$
			source += "\npass\n";
			
			if (monitor.isCanceled()) return;

			UserLibParser uparser = new UserLibParser( m_userLibFunctions.get(projectId) );
			try
			{
				codeParser.parseCode(source, uparser);
			}
			catch (ParseException error) 
			{
				if (issues != null)
				{
					ComparableResource pr = new ComparableResource(file);
					IIssueList issueList = IssueFactory.createIssueList(file); 
					IIssue issue = IssueFactory.createErrorIssue( "Parser error, cannot continue check: " + error.getLocalizedMessage(), error.currentToken.beginLine, -1, -1);
					issueList.addIssue(issue);
					issues.put(pr,issueList);
				}
				else
				{
					error.printStackTrace();
				}
			}
			catch(TokenMgrError error)
			{
				if (issues != null)
				{
					ComparableResource pr = new ComparableResource(file);
					IIssueList issueList = IssueFactory.createIssueList(file); 
					IIssue issue = IssueFactory.createErrorIssue( "Token error, cannot continue check: " + error.getLocalizedMessage(), error.errorLine, -1, -1);
					issueList.addIssue(issue);
					issues.put(pr,issueList);
				}
				else
				{
					error.printStackTrace();
				}
			}
			catch(Exception error)
			{
				if (issues != null)
				{
					ComparableResource pr = new ComparableResource(file);
					IIssueList issueList = IssueFactory.createIssueList(file); 
					error.printStackTrace();
					IIssue issue = IssueFactory.createErrorIssue( "Internal error, cannot continue check: " + error.getLocalizedMessage());
					issueList.addIssue(issue);
					issues.put(pr,issueList);
				}
				else
				{
					error.printStackTrace();
				}
			}
			monitor.worked(1);
		}
	}
	
	public boolean isUserLibFunction( IProject project, String functionName )
	{
		if (!m_userLibFunctions.containsKey(project.getName())) return false;
		return m_userLibFunctions.get(project.getName()).contains(functionName);
	}
}
