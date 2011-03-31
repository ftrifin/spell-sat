/////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards.pages.job
// 
// FILE      : AddNewProcedureJob.java
//
// DATE      : Dec 2, 2010 2:18:22 PM
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
/////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.wizards.procs.job;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.client.StatusAndInfoCommand;
import org.tigris.subversion.subclipse.core.commands.LockResourcesCommand;
import org.tigris.subversion.subclipse.core.resources.LocalFile;
import org.tigris.subversion.subclipse.core.resources.LocalFolder;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.actions.LockAction;
import org.tigris.subversion.subclipse.ui.actions.SVNPropertyAction;
import org.tigris.subversion.subclipse.ui.actions.SVNPropertyModifyAction;
import org.tigris.subversion.subclipse.ui.actions.UpdateAction;
import org.tigris.subversion.subclipse.ui.operations.CommitOperation;
import org.tigris.subversion.svnclientadapter.ISVNStatus;

/*******************************************************************************
 * 
 * {@link AddNewProcedureJob} attemtps to put the new file under version control
 *
 ******************************************************************************/
public class AddNewProcedureJob implements IRunnableWithProgress {
	
	private IFile m_procedure;
	private boolean m_commit;
	private boolean m_lockProperty;
	
	/***************************************************************************
	 * constructor
	 * @param commit true if the file shall also be commited to the repository
	 * @param lockProp true if the file shall contain the svn:needs-lock
	 * property. If commit is true and lockProp is true, the file will be locked
	 * after it has been committed
	 **************************************************************************/
	public AddNewProcedureJob(IFile procedure,
							     boolean commit,
							     boolean lockProp)
	{
		m_procedure = procedure;
		m_commit = commit;
		m_lockProperty = lockProp;
	}
	
	/*==========================================================================
	 * (non-Javadoc)
	 * @see IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 =========================================================================*/
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
													 InterruptedException												 
	{
		/*
		 * Step 0. Determine in containing project is version controlled
		 * If so, iterate from the IFile's parent to the Project updating the
		 * resources.
		 * Then changed resources must be committed 
		 */
		monitor.setTaskName("Checking Project SVN info");
		IProject project = m_procedure.getProject();
		boolean hasSvnInfo = isProjectUnderControl(project, monitor);
		
		if (monitor.isCanceled()) return;
		/*
		 * Step 1. update parent resources to the latest version
		 */
		// If the parent does not have subversion information the field 
		// cannot be committed
		if (!hasSvnInfo)
		{
			return;
		}
		// Update resources
		monitor.setTaskName("Project update");
		updateProject(project);
		if (monitor.isCanceled()) return;
		
		/*
		 * Step 2. Commit the files, including changed parents
		 */
		ArrayList<IResource> toAdd = new ArrayList<IResource>();
		toAdd.add(m_procedure);
		
		ArrayList<IResource> toCommit = new ArrayList<IResource>();
		toCommit.add(m_procedure);

		// Iterate from the IFile parent to the project finding resources
		// which needs to be updated
		IContainer parent = m_procedure.getParent();
		boolean goNext = true;
		while (parent != project)
		{
			goNext = processResource(parent, m_commit, toAdd, toCommit);
			if (goNext)
			{
				parent = parent.getParent();
			}
			else
			{
				break;
			}
			if (monitor.isCanceled()) return;
		}

		// If lock needs to be performed
		if (m_commit || m_lockProperty)
		{
			if (m_lockProperty)
			{
				IResource[] addArray = new IResource[]{m_procedure};
				toAdd.remove(m_procedure);
				String comment = "Adding procedure to repository";
				monitor.setTaskName("Adding procedure to repository");
				performCommit(addArray, new IResource[0], comment);
				if (monitor.isCanceled()) return;
				/*
				 * Modify the needs-lock flag
				 */
				monitor.setTaskName("Setting svn:needs-lock property");
				setSVNproperty(m_procedure, "svn:needs-lock", "*");
				if (monitor.isCanceled()) return;
			}
			
			IResource[] commitArray = new IResource[0];
			IResource[] addArray = toAdd.toArray(new IResource[0]);
			commitArray = new IResource[toCommit.size()];
			toCommit.toArray(commitArray);
			monitor.setTaskName("Committing files");
			performCommit(addArray, commitArray, "");
			if (monitor.isCanceled()) return;
			
			// If lock needs to be performed
			if (m_lockProperty)
			{
				monitor.setTaskName("Locking file " + m_procedure.getName());
				lockProcedure(m_procedure);
			}
		}	
		

	}

	
	/***************************************************************************
	 * Check if the project where the procedure is about to be created is
	 * version controlled
	 * @param project the project to check
	 * @return true if the project is version controlled. false otherwise
	 **************************************************************************/
	private boolean isProjectUnderControl(IProject project, 
										  IProgressMonitor monitor)
	{
		boolean hasSvnInfo = false;
		
		ISVNLocalResource projectSvn = new LocalFolder(project);
		StatusAndInfoCommand getStatusCmd = 
			new StatusAndInfoCommand(projectSvn, false, false, false);
		try {
			getStatusCmd.run(monitor);
			hasSvnInfo = true;
		} catch (SVNException e1) {
			e1.printStackTrace();
		}
		
		return hasSvnInfo;
	}
	
	/***************************************************************************
	 * 
	 * @param project
	 * @return
	 **************************************************************************/
	private boolean updateProject(final IProject project)
	{
		boolean result = false;
		
		// Update resources
		UpdateAction updateAction = new UpdateAction()
		{
			@Override
			protected IResource[] getSelectedResources() 
			{
				return new IResource[]{project};
			}
		};
		
		// update resources
		updateAction.setCanRunAsJob(false);
		updateAction.run(null);
		
		return result;
	}
	
	/***************************************************************************
	 * Get project SVN status, including nested elements
	 * @param project the project to check
	 * @return an {@link ISVNStatus} array containing all the information
	 * related to one project
	 **************************************************************************/
	public ISVNStatus[] getResourceStatus(IResource resource,
										  IProgressMonitor monitor)
	{	
		ISVNLocalResource svnResource = null;	
		if (resource.getType() == IResource.FILE)
		{
			svnResource = new LocalFile((IFile) resource);
		}
		else
		{
			svnResource = new LocalFolder((IContainer) resource);
		}
		
		StatusAndInfoCommand getStatusCmd = 
			new StatusAndInfoCommand(svnResource, false, false, false);
		ISVNStatus[] statuses = null;
		try 
		{
			getStatusCmd.run(monitor);
			statuses = getStatusCmd.getStatuses();
		} 
		catch (SVNException e1) 
		{
			e1.printStackTrace();
		}	
		
		return statuses;
	}
	
	/***************************************************************************
	 * Perform a commit over the given {@link IResource} objects
	 * @param toAdd {@link IResource} objects to be added
	 * @param toCommit {@link IResource} objects to be committed
	 * @param comment the comment forthe commif
	 * @return true if the commit was performed successfully. false otherwise
	 **************************************************************************/
	public boolean performCommit(IResource[] toAdd,
								 IResource[] toCommit, 
								 String comment)
	{
		boolean result = false;
		
		/*
		 * Commit changes
		 */
		CommitOperation commitOperation = new CommitOperation(
				null, 							// part
				new IResource[0], 				// selected
				toAdd, 							// toAdd
				new IResource[0],			   	// toDelete
				toCommit, 						// toCommit
				comment , 						// Comments
				true);						   	// keep locks       		
		try {
			commitOperation.run(null);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/***************************************************************************
	 * Lock the given file
	 * @param procedure
	 * @return
	 **************************************************************************/
	public boolean lockProcedure(final IFile procedure)
	{
		boolean result = false;
		
		LockAction lockAction = new LockAction()
		{
			@Override
			protected void execute(IAction action) throws InvocationTargetException, InterruptedException 
			{
				run(new WorkspaceModifyOperation() {
	                protected void execute(IProgressMonitor monitor) throws CoreException,
	                														InvocationTargetException, 
	                														InterruptedException
	                {
	                	final IResource[] resources = new IResource[]{procedure};
	                    try {
	    					Hashtable table = getProviderMapping(new IResource[]{procedure});
	    					Set keySet = table.keySet();
	    					Iterator iterator = keySet.iterator();
	    					while (iterator.hasNext()) {
	    					    SVNTeamProvider provider = (SVNTeamProvider)iterator.next();
	    				    	LockResourcesCommand command =
	    				    		new LockResourcesCommand(provider.getSVNWorkspaceRoot(),
	    				    								 resources,
	    				    								 true,
	    				    								 "Acquire lock for file " + procedure.getName());
	    				        command.run(Policy.subMonitorFor(monitor,1000));    					
	    					}
	                    } catch (Exception e) {
	    					throw new InvocationTargetException(e);
	    				} finally {
	    					monitor.done();
	    				}
	                }              
	            }, true /* cancelable */, PROGRESS_DIALOG);
			}
		};
		lockAction.run(null);
		result = true;
		
		return result;
	}
	
	/***************************************************************************
	 * Change the SVN property for the given resource
	 * @param resource the target resource
	 * @param property the property name
	 * @param value the property value
	 * @return true if the property was correctly set. Otherwise false is
	 * returned
	 **************************************************************************/
	public boolean setSVNproperty(final IResource resource,
								  final String property,
								  final String value)
	{
		boolean result = false;
		
		SVNPropertyAction needsLockAction = new SVNPropertyModifyAction()
		{
			protected void execute(IAction action)
			{
				ISVNLocalResource svnResource = null;	
				if (resource.getType() == IResource.FILE)
				{
					svnResource = new LocalFile((IFile) resource);
				}
				else
				{
					svnResource = new LocalFolder((IContainer) resource);
				}
				
				try {
					svnResource.setSvnProperty(property, value, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		needsLockAction.run(null);		
		return result;
	}
	
	/***************************************************************************
	 * Process the given resource in order to apply commit actions over it
	 * @param resource
	 * @return true if some action needs to be performed over the resource.
	 * otherwise false is returned
	 **************************************************************************/
	public boolean processResource(IResource resource,
								boolean commit,
								ArrayList<IResource> toAdd,
								ArrayList<IResource> toCommit)
	{
		boolean result = false;
		
		ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
		// Check resource status
		boolean isManaged = false;
		try {
			isManaged = svnResource.isManaged();
		} catch (SVNException e1) {
			return false;
		}
	
		boolean isContainer = svnResource.isFolder();
		boolean isPackage = false;	
		if (isContainer)
		{
			IContainer container = (IContainer) resource;
			IFile packageDefinition = container.getFile(new Path("__init__.py"));
			isPackage = packageDefinition.exists();
		}
		
		// If it is not managed, there is need at least to add the resource
		if (!isManaged)
		{
			toAdd.add(0,resource);
			if (commit) toCommit.add(0,resource);
			if (isPackage)
			{
				IContainer container = (IContainer) resource;
				IFile packageFile = container.getFile(new Path("__init__.py"));
				toAdd.add(1,packageFile);
				if (commit) toCommit.add(1,packageFile);
			}
			result = true;
		}
		
		return result;
	}
}
