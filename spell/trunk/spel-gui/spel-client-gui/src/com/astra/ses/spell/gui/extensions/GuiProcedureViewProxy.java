///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : GuiProcedureViewProxy.java
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
package com.astra.ses.spell.gui.extensions;

import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureView;


public class GuiProcedureViewProxy implements IProcedureView
{
	public GuiProcedureViewProxy()
	{
		Logger.debug("Created", Level.INIT, this);
	}

	@Override
	public void procedureModelDisabled(String instanceId)
	{
		//Logger.debug("Fired [model disabled]", Level.COMM, this);
		ProcedureBridge.get().fireModelDisabled(instanceId);
	}

	@Override
	public void procedureModelEnabled(String instanceId)
	{
		//Logger.debug("Fired [model enabled]", Level.COMM, this);
		ProcedureBridge.get().fireModelEnabled(instanceId);
	}

	@Override
	public void procedureModelLoaded(String instanceId)
	{
		//Logger.debug("Fired [model loaded]", Level.COMM, this);
		ProcedureBridge.get().fireModelLoaded(instanceId);
	}

	@Override
	public void procedureModelReset(String instanceId)
	{
		//Logger.debug("Fired [model reset]", Level.COMM, this);
		ProcedureBridge.get().fireModelReset(instanceId);
	}

	@Override
	public void procedureModelUnloaded(String instanceId, boolean doneLocally )
	{
		//Logger.debug("Fired [model unloaded]", Level.COMM, this);
		ProcedureBridge.get().fireModelUnloaded(instanceId, doneLocally);
	}

	@Override
	public void procedureModelConfigured(String instanceId)
	{
		//Logger.debug("Fired [model configured]", Level.COMM, this);
		ProcedureBridge.get().fireModelConfigured(instanceId);
	}

	@Override
	public void procedureCode(CodeNotification data)
	{
		//Logger.debug("Fired [procedure code]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureCode(data);
	}

	@Override
	public void procedureDisplay(DisplayData data)
	{
		//Logger.debug("Fired [procedure display]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureDisplay(data);
	}

	@Override
	public void procedureError(ErrorData data)
	{
		//Logger.debug("Fired [procedure error]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureError(data);
	}

	@Override
	public void procedureItem(ItemNotification data)
	{
		//Logger.debug("Fired [procedure item]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureItem(data);
	}

	@Override
	public void procedureLine(LineNotification data)
	{
		//Logger.debug("Fired [procedure line]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureLine(data);
	}

	@Override
	public void procedureStatus(StatusNotification data)
	{
		//Logger.debug("Fired [procedure status]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureStatus(data);
	}

	@Override
	public void procedureCancelPrompt(Input inputData)
	{
		//Logger.debug("Fired [procedure prompt cancel]", Level.COMM, this);
		ProcedureBridge.get().fireProcedureCancelPrompt(inputData);
	}

	@Override
	public void procedurePrompt(Input inputData)
	{
		//Logger.debug("Fired [procedure prompt]", Level.COMM, this);
		ProcedureBridge.get().fireProcedurePrompt(inputData);
	}

	@Override
	public String getListenerId()
	{
		return "com.astra.ses.spell.gui.extensions.ProcedureViewProxy";
	}

}
