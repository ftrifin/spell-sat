////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ProcedureRuntimeProcessor.java
//
// DATE      : 2010-07-30
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import com.astra.ses.spell.gui.core.extensionpoints.IProcedureRuntimeExtension;
import com.astra.ses.spell.gui.core.model.notification.ControlNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcExtensions;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeController;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.priv.IExecutionInformationHandler;

/*******************************************************************************
 * 
 *
 ******************************************************************************/
public class ProcedureRuntimeProcessor implements IProcedureRuntimeExtension
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Listener id */
	private static final String LISTENER_ID = "com.astra.ses.spell.gui.procs.model.Procedure";

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the procedure model */
	private IProcedure m_model;
	/** Reference to the tree controller */
	private IExecutionTreeController m_treeController;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ProcedureRuntimeProcessor(IProcedure model, IExecutionTreeController treeController)
	{
		m_model = model;
		m_treeController = treeController;
	}

	/*
	 * IProcedureRuntime methods
	 */

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return LISTENER_ID;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureDisplay(DisplayData data)
	{
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).displayMessage(data);
		// Redirect the data to the consumers
		ProcExtensions.get().fireProcedureDisplay(m_model, data);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureError(ErrorData data)
	{
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setExecutorStatus(ExecutorStatus.ERROR);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureItem(ItemNotification data)
	{
		m_treeController.notifyProcedureItem(data);
		// Redirect the data to the consumers
		ProcExtensions.get().fireProcedureItem(m_model, data);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		m_treeController.notifyProcedureStack(data);
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setStage(data.getStageId(), data.getStageTitle());
		// Redirect the data to the consumers
		ProcExtensions.get().fireProcedureStack(m_model, data);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureStatus(StatusNotification data)
	{
		// Update the status
		m_model.getController().setExecutorStatus(data.getStatus());
		if (!m_model.isInReplayMode())
		{
			Logger.debug("Not in replay mode", Level.PROC, this);
			if (data.getStatus() == ExecutorStatus.RELOADING)
			{
				m_model.reset();
				ProcExtensions.get().fireModelReset(m_model);
			}
		}
		// Redirect the data to the consumers
		ProcExtensions.get().fireProcedureStatus(m_model, data);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureUserAction(UserActionNotification data)
	{
		UserActionStatus status = data.getUserActionStatus();
		Severity severity = data.getSeverity();
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setUserActionStatus(status, severity);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureConfiguration(ExecutorConfig data)
	{
		// Nothing to do at the moment
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureControl(ControlNotification data)
	{
		// To be coded if the procedure model requires this info
	}
}
