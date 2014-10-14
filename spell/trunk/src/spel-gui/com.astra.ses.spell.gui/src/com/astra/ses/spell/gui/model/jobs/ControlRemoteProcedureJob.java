///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.jobs
// 
// FILE      : ControlRemoteProcedureJob.java
//
// DATE      : 2008-11-21 08:55
//
// Copyright (C) 2008, 2014 SES ENGINEERING, Luxembourg S.A.R.L.
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.model.jobs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.exceptions.NotConnected;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunProcessing;
import com.astra.ses.spell.gui.procs.interfaces.model.AsRunReplayResult;

public class ControlRemoteProcedureJob extends AbstractProcedureJob
{
	private boolean m_downloadData;
	
	public ControlRemoteProcedureJob(String instanceId, boolean downloadData)
	{
		super(instanceId);
		m_downloadData = downloadData;
				
	}

	private void warning(final String message)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Control Procedure", message);
			}
		});
	}

	@Override
	public void performTask(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		try
		{
			Logger.debug("Controlling procedure task for " + m_instanceId, Level.PROC, this);
			monitor.setTaskName("Control procedure " + m_instanceId);

			AsRunReplayResult ar = null;
			
			if (m_downloadData)
			{
				Logger.debug("Use ASRUN data", Level.PROC, this);
				ar = new AsRunReplayResult();
			}
			else
			{
				Logger.debug("Do not use ASRUN data", Level.PROC, this);
			}
			
			mgr.controlProcedure(m_instanceId, ar, monitor);

			if (m_downloadData)
			{
				Logger.info("Controlling process finished: " + ar.status, Level.GUI, this);
				Logger.info("Cancel flag                 : " + monitor.isCanceled(), Level.GUI, this);
				Logger.info("Controlling process message : " + ar.message, Level.GUI, this);
	
				if (ar.status.equals(AsRunProcessing.PARTIAL))
				{
					if (monitor.isCanceled())
					{
						warning("Retrieval of ASRUN information was not complete: canceled by user");
						result = CommandResult.CANCELLED;
					}
					else
					{
						warning("Retrieval of ASRUN information was not complete: " + ar.message);
						result = CommandResult.SUCCESS;
					}
				}
				else if (ar.status.equals(AsRunProcessing.FAILED))
				{
					warning("Retrieval of ASRUN information failed: " + ar.message);
					result = CommandResult.FAILED;
				}
				else
				{
					result = CommandResult.SUCCESS;
				}
			}
			else
			{
				result = CommandResult.SUCCESS;
			}
			
			Logger.debug("Controlling procedure task for " + m_instanceId + " success", Level.PROC, this);
		}
		catch (LoadFailed ex)
		{
			ex.printStackTrace();
			message = "Could not control procedure:\n\n" + ex.getLocalizedMessage();
			result = CommandResult.FAILED;
		}
		catch (NotConnected ex)
		{
			message = "Could not control procedure, not connected to context";
			result = CommandResult.FAILED;
		}
		monitor.done();
	}
}
