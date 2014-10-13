///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : GuiProcedureViewProxy.java
//
// DATE      : 2008-11-21 08:55
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.gui.extensions;

import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.extensionpoints.IProcedureViewExtension;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class GuiProcedureViewProxy implements IProcedureViewExtension
{
	public GuiProcedureViewProxy()
	{
		Logger.debug("Created", Level.INIT, this);
	}

	@Override
	public void notifyProcedureModelDisabled(IProcedure model)
	{
		ProcedureBridge.get().fireModelDisabled(model);
	}

	@Override
	public void notifyProcedureModelEnabled(IProcedure model)
	{
		ProcedureBridge.get().fireModelEnabled(model);
	}

	@Override
	public void notifyProcedureModelLoaded(IProcedure model)
	{
		ProcedureBridge.get().fireModelLoaded(model);
	}

	@Override
	public void notifyProcedureModelReset(IProcedure model)
	{
		ProcedureBridge.get().fireModelReset(model);
	}

	@Override
	public void notifyProcedureModelUnloaded(IProcedure model, UnloadType type)
	{
		ProcedureBridge.get().fireModelUnloaded(model, type);
	}

	@Override
	public void notifyProcedureModelConfigured(IProcedure model)
	{
		ProcedureBridge.get().fireModelConfigured(model);
	}

	@Override
	public void notifyProcedureDisplay(IProcedure model, DisplayData data)
	{
		// Logger.debug("Fired [procedure display]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureDisplay(model, data);
	}

	@Override
	public void notifyProcedureError(IProcedure model, ErrorData data)
	{
		// Logger.debug("Fired [procedure error]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureError(model, data);
	}

	@Override
	public void notifyProcedureItem(IProcedure model, ItemNotification data)
	{
		// Logger.debug("Fired [procedure item]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureItem(model, data);
	}

	@Override
	public void notifyProcedureStack(IProcedure model, StackNotification data)
	{
		// Logger.debug("Fired [procedure stack]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureStack(model, data);
	}

	@Override
	public void notifyProcedureStatus(IProcedure model, StatusNotification data)
	{
		// Logger.debug("Fired [procedure status]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureStatus(model, data);
	}

	@Override
	public void notifyProcedurePrompt(IProcedure model)
	{
		// Logger.debug("Fired [procedure prompt]", Level.COMM, this);
		ProcedureBridge.get().fireProcedurePrompt(model);
	}

	@Override
	public void notifyProcedureFinishPrompt(IProcedure model)
	{
		// Logger.debug("Fired [procedure prompt finish]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureFinishPrompt(model);
	}

	@Override
	public void notifyProcedureCancelPrompt(IProcedure model)
	{
		// Logger.debug("Fired [procedure prompt cancel]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureCancelPrompt(model);
	}

	@Override
	public String getListenerId()
	{
		return "com.astra.ses.spell.gui.extensions.ProcedureViewProxy";
	}

	@Override
	public void notifyProcedureUserAction(IProcedure model, UserActionNotification data)
	{
		ProcedureBridge.get().fireProcedureUserAction(model, data);
	}
}
