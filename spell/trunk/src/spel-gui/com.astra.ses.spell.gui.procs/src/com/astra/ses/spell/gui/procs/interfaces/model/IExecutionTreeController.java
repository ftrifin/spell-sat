////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IExecutionTreeController.java
//
// DATE      : 2010-08-03
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
package com.astra.ses.spell.gui.procs.interfaces.model;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IStackChangesListener;

/*******************************************************************************
 * 
 * IProcedureController handles the different procedure notifications as well as
 * it enables access to some properties
 * 
 ******************************************************************************/
public interface IExecutionTreeController extends IExecutionTreeInformation
{
	/***************************************************************************
	 * Callback for item information notification
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void notifyProcedureItem(ItemNotification data);

	/***************************************************************************
	 * Callback for procedure stack position changes notification
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void notifyProcedureStack(StackNotification data);

	/***************************************************************************
	 * Unregister the routine execution listener
	 **************************************************************************/
	public void removeStackChangesListener(IStackChangesListener listener);

	/***************************************************************************
	 * Reset this object to its original status
	 **************************************************************************/
	public void reset();

	/***************************************************************************
	 * Set step over for the current execution
	 **************************************************************************/
	public void setStepOverOnce();

	/***************************************************************************
	 * Set step into for the current execution
	 **************************************************************************/
	public void setStepIntoOnce();

	/***************************************************************************
	 * Change visible execution node
	 * 
	 * @param stack
	 *            a stack representation for node location
	 * @return an integer higher than 0 which indicates the depth of the node
	 *         relative to the root node. Otherwise a value lower than 0 is
	 *         returned
	 **************************************************************************/
	public int setVisibleNode(String stack);

	/***************************************************************************
	 * After a pause, a procedure line is about to be executed
	 **************************************************************************/
	public void backToExecution();

	/***************************************************************************
	 * Enable or disable the replay mode.
	 **************************************************************************/
	public void setReplayMode(boolean doingReplay);

	/***************************************************************************
	 * Check if in replay mode
	 **************************************************************************/
	public boolean isInReplayMode();

	/***************************************************************************
	 * Should be called on model unload
	 **************************************************************************/
	public void dispose();
	
}
