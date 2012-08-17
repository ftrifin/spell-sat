///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.presentations
// 
// FILE      : PresentationManager.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views.presentations;

import java.util.ArrayList;

import com.astra.ses.spell.gui.core.extensionpoints.IProcedureRuntimeExtension;
import com.astra.ses.spell.gui.core.interfaces.IProcedureInput;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.ScopeNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.VariableNotification;
import com.astra.ses.spell.gui.interfaces.IPresentationNotifier;
import com.astra.ses.spell.gui.interfaces.IProcedureItemsListener;
import com.astra.ses.spell.gui.interfaces.IProcedureMessageListener;
import com.astra.ses.spell.gui.interfaces.IProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.IProcedureRuntimeListener;
import com.astra.ses.spell.gui.interfaces.IProcedureStackListener;
import com.astra.ses.spell.gui.interfaces.IProcedureStatusListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief Manages procedure presentations for a view
 * @date 09/10/07
 ******************************************************************************/
public class PresentationNotifier implements IPresentationNotifier,
        IProcedureRuntimeExtension, IProcedureInput
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String	                 ID	= "com.astra.ses.spell.gui.views.models.PresentationNotifier";

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Item listeners */
	private ArrayList<IProcedureItemsListener>	 m_itemsListeners;
	/** Message listeners */
	private ArrayList<IProcedureMessageListener>	m_msgListeners;
	/** Status listeners */
	private ArrayList<IProcedureStatusListener>	 m_statusListeners;
	/** Runtime listeners */
	private ArrayList<IProcedureRuntimeListener>	m_runtimeListeners;
	/** Stack listeners */
	private ArrayList<IProcedureStackListener>	 m_stackListeners;
	/** Prompt listeners */
	private ArrayList<IProcedurePromptListener>	 m_promptListeners;
	/** Holds reference to the procedure model */
	private IProcedure	                         m_model;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public PresentationNotifier(IProcedure model)
	{
		m_model = model;
		m_itemsListeners = new ArrayList<IProcedureItemsListener>();
		m_msgListeners = new ArrayList<IProcedureMessageListener>();
		m_statusListeners = new ArrayList<IProcedureStatusListener>();
		m_runtimeListeners = new ArrayList<IProcedureRuntimeListener>();
		m_stackListeners = new ArrayList<IProcedureStackListener>();
		m_promptListeners = new ArrayList<IProcedurePromptListener>();
	}

	@Override
	public void addMessageListener(IProcedureMessageListener listener)
	{
		m_msgListeners.add(listener);
	}

	@Override
	public void removeMessageListener(IProcedureMessageListener listener)
	{
		m_msgListeners.remove(listener);
	}

	@Override
	public void addStatusListener(IProcedureStatusListener listener)
	{
		m_statusListeners.add(listener);
	}

	@Override
	public void removeStatusListener(IProcedureStatusListener listener)
	{
		m_statusListeners.remove(listener);
	}

	@Override
	public void addItemListener(IProcedureItemsListener listener)
	{
		m_itemsListeners.add(listener);
	}

	@Override
	public void removeItemListener(IProcedureItemsListener listener)
	{
		m_itemsListeners.remove(listener);
	}

	@Override
	public void addRuntimeListener(IProcedureRuntimeListener listener)
	{
		m_runtimeListeners.add(listener);
	}

	@Override
	public void removeRuntimeListener(IProcedureRuntimeListener listener)
	{
		m_runtimeListeners.remove(listener);
	}

	@Override
	public void addStackListener(IProcedureStackListener listener)
	{
		m_stackListeners.add(listener);
	}

	@Override
	public void removeStackListener(IProcedureStackListener listener)
	{
		m_stackListeners.remove(listener);
	}

	@Override
	public void addPromptListener(IProcedurePromptListener listener)
	{
		m_promptListeners.add(listener);
	}

	@Override
	public void removePromptListener(IProcedurePromptListener listener)
	{
		m_promptListeners.remove(listener);
	}

	@Override
	public String getListenerId()
	{
		return ID;
	}

	@Override
	public void notifyProcedureDisplay(DisplayData data)
	{
		for (IProcedureMessageListener listener : m_msgListeners)
		{
			listener.notifyDisplay(m_model, data);
		}
	}

	@Override
	public void notifyProcedureError(ErrorData data)
	{
		for (IProcedureStatusListener listener : m_statusListeners)
		{
			listener.notifyError(m_model, data);
		}
	}

	@Override
	public void notifyProcedureItem(ItemNotification data)
	{
		for (IProcedureItemsListener listener : m_itemsListeners)
		{
			listener.notifyItem(m_model, data);
		}
	}

	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		for (IProcedureStackListener listener : m_stackListeners)
		{
			listener.notifyStack(m_model, data);
		}
	}

	@Override
	public void notifyProcedureStatus(StatusNotification data)
	{
		for (IProcedureStatusListener listener : m_statusListeners)
		{
			listener.notifyStatus(m_model, data);
		}
	}

	@Override
	public void notifyProcedureUserAction(UserActionNotification data)
	{
		// Not issued to presentations
	}

	public void notifyModelDisabled()
	{
		for (IProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelDisabled(m_model);
		}
	}

	public void notifyModelEnabled()
	{
		for (IProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelEnabled(m_model);
		}
	}

	public void notifyModelLoaded()
	{
		for (IProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelLoaded(m_model);
		}
	}

	public void notifyModelReset()
	{
		for (IProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelReset(m_model);
		}
	}

	public void notifyModelUnloaded()
	{
		for (IProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelUnloaded(m_model);
		}
	}

	public void notifyModelConfigured()
	{
		for (IProcedureRuntimeListener listener : m_runtimeListeners)
		{
			listener.notifyModelConfigured(m_model);
		}
	}

	@Override
	public void notifyProcedurePrompt(Input inputData)
	{
		for (IProcedurePromptListener listener : m_promptListeners)
		{
			listener.notifyPrompt(m_model, inputData);
		}
	}

	@Override
	public void notifyProcedureFinishPrompt(Input inputData)
	{
		for (IProcedurePromptListener listener : m_promptListeners)
		{
			listener.notifyFinishPrompt(m_model, inputData);
		}
	}

	@Override
	public void notifyProcedureCancelPrompt(Input inputData)
	{
		for (IProcedurePromptListener listener : m_promptListeners)
		{
			listener.notifyCancelPrompt(m_model, inputData);
		}
	}

	@Override
	public void notifyVariableScopeChange(ScopeNotification data)
	{
	}

	@Override
	public void notifyVariableChange(VariableNotification data)
	{
	}

}
