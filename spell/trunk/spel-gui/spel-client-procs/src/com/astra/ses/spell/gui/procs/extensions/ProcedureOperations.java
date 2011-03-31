///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.extensions
// 
// FILE      : ProcedureOperations.java
//
// DATE      : 2008-11-21 08:55
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.extensions;

import com.astra.ses.spell.gui.core.interfaces.IProcedureOperation;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;


public class ProcedureOperations implements IProcedureOperation
{
	private static ProcedureManager s_mgr = null;
	
	public ProcedureOperations()
	{
		Logger.debug("Created", Level.INIT, this);
		s_mgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
	}
	
	@Override
	public String getListenerId()
	{
		return s_mgr.getListenerId();
	}

	@Override
	public void procedureClosed(String procId, String guiKey)
	{
		s_mgr.procedureClosed(procId, guiKey);
	}

	@Override
	public void procedureControlled(String procId, String guiKey)
	{
		s_mgr.procedureControlled(procId, guiKey);
	}

	@Override
	public void procedureKilled(String procId, String guiKey)
	{
		s_mgr.procedureKilled(procId, guiKey);
	}

	@Override
	public void procedureMonitored(String procId, String guiKey)
	{
		s_mgr.procedureMonitored(procId, guiKey);
	}

	@Override
	public void procedureOpen(String procId, String guiKey)
	{
		s_mgr.procedureOpen(procId, guiKey);
	}

	@Override
	public void procedureReleased(String procId, String guiKey)
	{
		s_mgr.procedureReleased(procId, guiKey);
	}

	@Override
	public void procedureStatus(String procId, ExecutorStatus status,
			String guiKey)
	{
		s_mgr.procedureStatus(procId, status, guiKey);
	}

}
