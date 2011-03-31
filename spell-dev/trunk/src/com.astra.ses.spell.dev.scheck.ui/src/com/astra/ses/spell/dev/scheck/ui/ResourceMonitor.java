///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.ui
//
// FILE      : ResourceMonitor.java
//
// DATE      : Feb 17, 2011
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
package com.astra.ses.spell.dev.scheck.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;

import com.astra.ses.spell.dev.scheck.ResourceManager;
import com.astra.ses.spell.dev.scheck.ui.handlers.JobHelper;
import com.astra.ses.spell.dev.scheck.ui.jobs.PerformCheckJob;
import com.astra.ses.spell.dev.spelleditor.utils.SpellEditorInfo;

public class ResourceMonitor implements IExecutionListener
{
	private static ResourceMonitor s_instance = null;
	
	/**************************************************************************
	* 
	**************************************************************************/
	public static ResourceMonitor instance()
	{
		if (s_instance == null)
		{
			s_instance = new ResourceMonitor();
		}
		return s_instance;
	}

	/**************************************************************************
	* 
	**************************************************************************/
	private ResourceMonitor()
	{
		
	}

	/**************************************************************************
	* 
	**************************************************************************/
	public void activate()
	{
		ICommandService svc = (ICommandService) Activator.getDefault().getWorkbench().getService(ICommandService.class);
		svc.addExecutionListener(this);
	}

	/**************************************************************************
	* 
	**************************************************************************/
	public void deactivate()
	{
	}

	@Override
    public void notHandled(String commandId, NotHandledException exception)
    {
    }

	@Override
    public void postExecuteFailure(String commandId,
            ExecutionException exception)
    {
    }

	@Override
    public void postExecuteSuccess(String commandId, Object returnValue)
    {
	    if (commandId.equals("org.eclipse.ui.file.save"))
	    {
	    	IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
	    	IEditorPart editor = window.getActivePage().getActiveEditor();
	    	if (editor != null)
			{
				SpellEditorInfo info = (SpellEditorInfo) editor.getAdapter(SpellEditorInfo.class);
				// If the editor could adapt
				if (info != null)
				{
					// Reset the source buffered for the associated file
					ResourceManager.instance().resetSource(info.getFile());
					// Reset the markers for the file
					if (MarkerManager.cleanMarkers(info.getFile()))
					{
						// If we got here, there were markers in that file so redo the check
						List<IResource> item = new ArrayList<IResource>();
						item.add(info.getFile());
						PerformCheckJob job = new PerformCheckJob(item);
						JobHelper.executeJob(job, true, true);
					}
				}
	    		
			}
	    }
    }

	@Override
    public void preExecute(String commandId, ExecutionEvent event)
    {
    }
	
}
