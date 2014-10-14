///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.interfaces
// 
// FILE      : IPresentationNotifier.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.interfaces;

import com.astra.ses.spell.gui.core.extensionpoints.ICoreProcedureRuntimeListener;
import com.astra.ses.spell.gui.core.interfaces.listeners.ICoreProcedureInputListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureItemsListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureMessageListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureRuntimeListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStackListener;
import com.astra.ses.spell.gui.interfaces.listeners.IGuiProcedureStatusListener;

/*******************************************************************************
 * @brief
 ******************************************************************************/
public interface IPresentationNotifier extends ICoreProcedureRuntimeListener, ICoreProcedureInputListener
{
	/***************************************************************************
	 * Add listener for procedure display messages
	 **************************************************************************/
	public void addMessageListener(IGuiProcedureMessageListener listener);

	/***************************************************************************
	 * Remove listener for procedure display messages
	 **************************************************************************/
	public void removeMessageListener(IGuiProcedureMessageListener listener);

	/***************************************************************************
	 * Add listener for procedure status changes
	 **************************************************************************/
	public void addStatusListener(IGuiProcedureStatusListener listener);

	/***************************************************************************
	 * Remove listener for procedure status changes
	 **************************************************************************/
	public void removeStatusListener(IGuiProcedureStatusListener listener);

	/***************************************************************************
	 * Add listener for procedure item notifications
	 **************************************************************************/
	public void addItemListener(IGuiProcedureItemsListener listener);

	/***************************************************************************
	 * Remove listener for procedure item notifications
	 **************************************************************************/
	public void removeItemListener(IGuiProcedureItemsListener listener);

	/***************************************************************************
	 * Add listener for procedure runtime changes
	 **************************************************************************/
	public void addRuntimeListener(IGuiProcedureRuntimeListener listener);

	/***************************************************************************
	 * Remove listener for procedure runtime changes
	 **************************************************************************/
	public void removeRuntimeListener(IGuiProcedureRuntimeListener listener);

	/***************************************************************************
	 * Add listener for procedure stack changes
	 **************************************************************************/
	public void addStackListener(IGuiProcedureStackListener listener);

	/***************************************************************************
	 * Remove listener for procedure stack
	 **************************************************************************/
	public void removeStackListener(IGuiProcedureStackListener listener);

	/***************************************************************************
	 * Add listener for procedure prompts
	 **************************************************************************/
	public void addPromptListener(IGuiProcedurePromptListener listener);

	/***************************************************************************
	 * Remove listener for procedure prompts
	 **************************************************************************/
	public void removePromptListener(IGuiProcedurePromptListener listener);
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelEnabled();
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelDisabled();
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelLoaded();
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelReset();
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelUnloaded();
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelConfigured();
}
