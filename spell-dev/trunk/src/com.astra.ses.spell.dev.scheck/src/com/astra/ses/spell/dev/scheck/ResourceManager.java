///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.processing
//
// FILE      : ResourceManager.java
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
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
import com.astra.ses.spell.dev.workspace.ResourceAdapter;


public class ResourceManager
{
	private static ResourceManager s_instance = null;
	
	private Map<String,String> m_sources;
	private Map<String,Integer> m_sourceLines;
	
	/**************************************************************************
	* 
	**************************************************************************/
	public static ResourceManager instance()
	{
		if (s_instance == null)
		{
			s_instance = new ResourceManager();
		}
		return s_instance;
	}

	/**************************************************************************
	* 
	**************************************************************************/
	private ResourceManager()
	{
		m_sources = new TreeMap<String,String>();
		m_sourceLines = new TreeMap<String,Integer>();
	}

	/**************************************************************************
	* 
	**************************************************************************/
	private void findFiles( IResource item, List<IFile> processedItems, 
			                      Map<ComparableResource,IIssueList> issues,
			                      IProgressMonitor monitor )
	{
		
		if (item instanceof IProject)
		{
			monitor.subTask("Processing project: " + item.getName());
			IProject project = (IProject) item;
			if (!project.exists() || !project.isAccessible() || !project.isOpen())
			{
				if (issues != null)
				{
					// Notify the issue: could not open this project
					IIssueList projectIssues = IssueFactory.createIssueList(item);
					IIssue issue = IssueFactory.createErrorIssue( "Cannot process project " + project.getName() );
					projectIssues.addIssue( issue );
					issues.put( new ComparableResource( ResourcesPlugin.getWorkspace().getRoot() ), projectIssues );
				}
			}
			else
			{
				try 
				{
					IResource[] members = project.members();
					for(IResource member : members)
					{
						// Call recursively for each one
						findFiles( member, processedItems, issues, monitor );
					}
				} 
				catch (CoreException e) 
				{
					if (issues != null)
					{
						// Notify the issue: could not open this project
						IIssueList projectIssues = IssueFactory.createIssueList(item);
						IIssue issue = IssueFactory.createErrorIssue( "Cannot process project " + project.getName() +
												  ": " + e.getLocalizedMessage() );
						projectIssues.addIssue( issue );
						issues.put( new ComparableResource( ResourcesPlugin.getWorkspace().getRoot() ), projectIssues );
					}
					else
					{
						e.printStackTrace();
					}
				}
			}
		}
		else if (item instanceof IFolder)
		{
			if (item.getName().startsWith(".")) return;
			if (item.getName().equals("UserLib")) return;
			
			monitor.subTask("Processing folder: " + item.getName());
			IFolder folder = (IFolder) item;
			if (!folder.exists() || !folder.isAccessible())
			{
				if (issues != null)
				{
					// Notify the issue: could not open this folder
					IIssueList folderIssues = IssueFactory.createIssueList(item);
					IIssue issue = IssueFactory.createErrorIssue( "Cannot process folder " + folder.getName() +
											  " in " + folder.getParent().getName());
					folderIssues.addIssue( issue );
					issues.put( new ComparableResource(ResourcesPlugin.getWorkspace().getRoot()), folderIssues );
				}
			}
			else
			{
				try 
				{
					IResource[] members = folder.members();
					for(IResource member : members)
					{
						// Call recursively for each one
						findFiles( member, processedItems, issues, monitor );
					}
				} 
				catch (CoreException e) 
				{
					if (issues != null)
					{
						// Notify the issue: could not open this folder
						IIssueList folderIssues = IssueFactory.createIssueList(item);
						IIssue issue = IssueFactory.createErrorIssue( "Cannot process folder " + folder.getName() +
												  " in " + folder.getParent().getName() + ": " + e.getLocalizedMessage());
						folderIssues.addIssue( issue );
						issues.put( new ComparableResource(ResourcesPlugin.getWorkspace().getRoot() ), folderIssues );
					}
					else
					{
						e.printStackTrace();
					}
				}
			}
		}
		else if (item instanceof IFile)
		{
			if (item.getName().startsWith(".")) return;

			IFile file = (IFile) item;
			
			// General filters
			String name = file.getName();
			String extension = file.getFileExtension();
			if (!extension.equals("py")) return; 
			if (name.equals("__init__.py")) return;
			
			if (!file.exists() || !file.isAccessible())
			{
				if (issues != null)
				{
					// Notify the issue: could not open this file
					IIssueList fileIssues = IssueFactory.createIssueList(item);
					IIssue issue = IssueFactory.createErrorIssue( "Cannot process file " + file.getName() +
											  " in " + file.getParent().getName());
					fileIssues.addIssue( issue );
					issues.put( new ComparableResource(item), fileIssues );
				}
			}
			else
			{
				monitor.subTask("Found file: " + file.getName());
				processedItems.add( (IFile) item);
			}
		}
		else
		{
			throw new RuntimeException("Unable to process resource: " + item + ": " + item.getClass().getCanonicalName());
		}
	}

	/**************************************************************************
	* 
	**************************************************************************/
	public List<IFile> adaptFiles( List<IFile> toAdapt, List<String> notProcessed, IProgressMonitor monitor )
	{
		List<IFile> processable = new ArrayList<IFile>();
		for(IFile file : toAdapt)
		{
			String unprocessedReason = ResourceAdapter.adaptFile(file, monitor);
			if (!unprocessedReason.isEmpty())
			{
				notProcessed.add( file.getName() + " " + unprocessedReason);
			}
			else
			{
				processable.add(file);
			}
		}
		return processable;
	}

	/**************************************************************************
	* 
	**************************************************************************/
	public void findFiles( List<IResource> items, List<IFile> processedItems, 
								  Map<ComparableResource,IIssueList> issues, 
								  IProgressMonitor monitor )
	{
		for(IResource item : items)
		{
			findFiles( item, processedItems, issues, monitor );
		}

	}

	/**************************************************************************
	* 
	**************************************************************************/
	public void findProjects( List<IFile> items, List<IProject> projects )
	{
		for(IResource item : items)
		{
			IProject project = item.getProject();
			if (!projects.contains(project))
			{
				projects.add(project);
			}
		}
	}

	/**************************************************************************
	* 
	**************************************************************************/
	private void parseSource( String key, IFile file )
	{
		// Read the source code
		String source = "";
		InputStream istream = null;
		try
		{
			istream = file.getContents();
			InputStreamReader reader = new InputStreamReader(istream);
			int read = reader.read();
			StringWriter writer = new StringWriter();
			while(read != -1)
			{
				writer.write(read);
				read = reader.read();
			}
			source = writer.toString();
			m_sources.put( key, source );
			m_sourceLines.put( key, source.split("\n").length );
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (istream != null)
			{
				try 
				{
					istream.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**************************************************************************
	* 
	**************************************************************************/
	public String getSource( IFile file )
	{
		String key = file.getFullPath().toPortableString();

		if (!m_sources.containsKey(key))
		{
			parseSource( key, file );
		}
		return m_sources.get(key);
	}

	/**************************************************************************
	* 
	**************************************************************************/
	public int getSourceLines( IFile file )
	{
		String key = file.getFullPath().toPortableString();

		if (m_sourceLines.containsKey(key))
		{
			parseSource( key, file );
		}
		return m_sourceLines.get(key);
	}

	/**************************************************************************
	* 
	**************************************************************************/
	public void resetSource( IFile file )
	{
		String key = file.getFullPath().toPortableString();
		if (m_sources.containsKey( key ))
		{
			m_sources.remove(key);
			m_sourceLines.remove(key);
		}
	}

}
