///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.extensionpoints
// 
// FILE      : IProcedureListener.java
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
package com.astra.ses.spell.gui.procs.extensionpoints;

import com.astra.ses.spell.gui.core.interfaces.listeners.IBaseListener;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public interface IProcedureListener extends IBaseListener
{
	/***************************************************************************
	 * Callback for procedure messages notification
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void notifyProcedureDisplay(IProcedure model, DisplayData data);

	/***************************************************************************
	 * Callback for procedure status changes notificiation
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void notifyProcedureStatus(IProcedure model, StatusNotification data);

	/***************************************************************************
	 * Callback for procedure error notifications
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void notifyProcedureError(IProcedure model, ErrorData data);

	/***************************************************************************
	 * Callback for item information notification
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void notifyProcedureItem(IProcedure model, ItemNotification data);

	/***************************************************************************
	 * Callback for procedure stack position changes notification
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void notifyProcedureStack(IProcedure model, StackNotification data);

	/***************************************************************************
	 * Callback for user action events notification
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void notifyProcedureUserAction(IProcedure model,
	        UserActionNotification data);

	/***************************************************************************
	 * Callback for notifying about procedures requiring input from user
	 * 
	 * @param inputData
	 *            Request information
	 **************************************************************************/
	public void notifyProcedurePrompt(IProcedure model);

	/***************************************************************************
	 * Callback for notifying about procedures not requiring input anymore due
	 * to finished prompt
	 * 
	 * @param inputData
	 *            Request information
	 **************************************************************************/
	public void notifyProcedureFinishPrompt(IProcedure model);

	/***************************************************************************
	 * Callback for notifying about procedures not requiring input anymore due
	 * to a cancellation
	 * 
	 * @param inputData
	 *            Request information
	 **************************************************************************/
	public void notifyProcedureCancelPrompt(IProcedure model);
}
