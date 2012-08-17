///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.jobs
// 
// FILE      : OpenLocalProcedureJob.java
//
// DATE      : 2008-11-21 08:55
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.model.jobs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.exceptions.NotConnected;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;

public class ScheduleProcedureJob extends AbstractProcedureJob
{
	protected String	m_condition;

	public ScheduleProcedureJob(String instanceId, String condition)
	{
		super(instanceId);
		m_condition = condition;
	}

	@Override
	public void performTask(IProgressMonitor monitor)
	        throws InvocationTargetException, InterruptedException
	{
		ProcedureManager mgr = (ProcedureManager) ServiceManager
		        .get(ProcedureManager.ID);
		try
		{
			monitor.setTaskName("Scheduling procedure " + m_instanceId);
			mgr.scheduleProcedure(m_instanceId, m_condition, monitor);
			result = CommandResult.SUCCESS;
		}
		catch (LoadFailed ex)
		{
			message = "Could not schedule procedure:\n\n"
			        + ex.getLocalizedMessage();
			result = CommandResult.FAILED;
		}
		catch (NotConnected ex)
		{
			message = "Could not schedule procedure, not connected to context";
			result = CommandResult.FAILED;
		}
		monitor.done();
	}

}
