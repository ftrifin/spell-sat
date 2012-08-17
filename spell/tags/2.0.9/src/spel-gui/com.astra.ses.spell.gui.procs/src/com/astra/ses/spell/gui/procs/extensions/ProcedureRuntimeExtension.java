///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.extensions
// 
// FILE      : ProcedureRuntime.java
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
package com.astra.ses.spell.gui.procs.extensions;

import com.astra.ses.spell.gui.core.extensionpoints.IProcedureRuntimeExtension;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.ScopeNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.VariableNotification;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;

public class ProcedureRuntimeExtension implements IProcedureRuntimeExtension
{
	private static ProcedureManager	s_mgr	= null;

	public ProcedureRuntimeExtension()
	{
		Logger.debug("Created", Level.INIT, this);
		s_mgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
	}

	@Override
	public void notifyProcedureDisplay(DisplayData data)
	{
		s_mgr.notifyProcedureDisplay(data);
	}

	@Override
	public void notifyProcedureError(ErrorData data)
	{
		s_mgr.notifyProcedureError(data);
	}

	@Override
	public void notifyProcedureItem(ItemNotification data)
	{
		s_mgr.notifyProcedureItem(data);
	}

	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		s_mgr.notifyProcedureStack(data);
	}

	@Override
	public void notifyProcedureStatus(StatusNotification data)
	{
		s_mgr.notifyProcedureStatus(data);
	}

	@Override
	public String getListenerId()
	{
		return s_mgr.getListenerId();
	}

	@Override
	public void notifyProcedureUserAction(UserActionNotification data)
	{
		s_mgr.notifyProcedureUserAction(data);
	}

	@Override
	public void notifyVariableScopeChange(ScopeNotification data)
	{
		s_mgr.notifyVariableScopeChange(data);
	}

	@Override
	public void notifyVariableChange(VariableNotification data)
	{
		s_mgr.notifyVariableChange(data);
	}

}
