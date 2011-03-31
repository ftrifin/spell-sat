///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.ui.jobs
//
// FILE      : PerformCheckJob.java
//
// DATE      : Feb 9, 2011
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
package com.astra.ses.spell.dev.scheck.ui.jobs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.astra.ses.spell.dev.scheck.ResourceManager;
import com.astra.ses.spell.dev.scheck.SemanticsChecker;
import com.astra.ses.spell.dev.scheck.interfaces.ComparableResource;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.ui.MarkerManager;

public class PerformCheckJob implements IRunnableWithProgress
{
	private List<IResource> m_resources;
	private Map<ComparableResource,IIssueList> m_issues;
	
	public List<String> notProcessed;
	public IStatus status;
	public int numProcessedItems;
	
	public PerformCheckJob( List<IResource> items )
	{
		m_resources = items;
		status = null;
		m_issues = null;
		numProcessedItems = 0;
		notProcessed = new ArrayList<String>();
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		// Create the lists of m_issues (initially empty)
		m_issues = new TreeMap<ComparableResource,IIssueList>();

		// Process the passed items, to finally get only a list of files
		List<IFile> processedItems = new ArrayList<IFile>();
		monitor.subTask("Processing resources");
		ResourceManager.instance().findFiles( m_resources, processedItems, m_issues, monitor );
		
		if (!monitor.isCanceled())
		{
			processedItems = ResourceManager.instance().adaptFiles(processedItems, notProcessed, monitor);
		}		
		
		if (!monitor.isCanceled())
		{ 
			// Call the semantics checker
			numProcessedItems = SemanticsChecker.instance().check( processedItems, m_issues, monitor );
		}

		if (!monitor.isCanceled())
		{
			// Publish the results in the markers
			MarkerManager.generateMarkers(m_issues.values(),monitor);
		}
		else
		{
			// If monitor was cancelled during the marker generation, clean all markers
			for( IIssueList issueList : m_issues.values())
			{
				MarkerManager.cleanMarkers(issueList.getResource());
			}
			status = Status.CANCEL_STATUS;
			return;
		}

		monitor.done();
	
		status = Status.OK_STATUS;
		return;
	}
}
