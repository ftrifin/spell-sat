///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : ControlArea.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.views.controls;

import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.views.ProcedureView;
import com.astra.ses.spell.gui.views.controls.actions.GuiExecutorCommand;
import com.astra.ses.spell.gui.views.controls.input.InputArea;

public class ControlArea extends Composite
{
	/** Handle to the console manager */
	private static IProcedureManager	s_pmgr	= null;

	/** Dynamic input area for receiving user input from the code page */
	private InputArea	             m_input;
	/** Button set for controlling the procedure in the code page */
	private ProcedureControlPanel	 m_controlPanel;
	/** Model */
	private IProcedure	             m_model;
	/** Container */
	private Composite	             m_top;
	/** Holds the prompt message if any */
	private InputData	             m_promptData;

	/***************************************************************************
	 * 
	 **************************************************************************/
	public ControlArea(ProcedureView view, IProcedure model, Composite top, String procId)
	{
		super(top, SWT.NONE);
		m_top = top;
		m_model = model;
		m_promptData = null;
		if (s_pmgr == null)
		{
			s_pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}

		GridLayout ca_layout = new GridLayout();
		// We do not want extra margins
		ca_layout.marginTop = 5;
		ca_layout.marginBottom = 5;
		ca_layout.marginLeft = 5;
		ca_layout.marginRight = 5;
		ca_layout.marginHeight = 0;
		ca_layout.marginWidth = 0;
		// Will place each component below the previous one
		ca_layout.numColumns = 1;
		setLayout(ca_layout);

		// Create the procedure control panel
		Logger.debug("Creating control panel", Level.INIT, this);
		m_controlPanel = new ProcedureControlPanel(view, model, this, SWT.NONE);
		// Construct the composite contents
		m_controlPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the input area
		Logger.debug("Creating input area", Level.INIT, this);
		m_input = new InputArea(view, this);

		GridData ldata = new GridData(GridData.FILL_HORIZONTAL);
		m_input.setLayoutData(ldata);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setEnabled(boolean enabled)
	{
		m_controlPanel.setEnabled(enabled);
		m_input.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean setFocus()
	{
		return m_input.setFocus();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void zoom(boolean increase)
	{
		m_input.zoom(increase);
	}

	/***************************************************************************
	 * Set the procedure status
	 **************************************************************************/
	public void setProcedureStatus(ExecutorStatus status, boolean fatalError)
	{
		m_controlPanel.notifyProcStatus(status, fatalError);
		if (status == ExecutorStatus.UNINIT || status == ExecutorStatus.UNKNOWN || status == ExecutorStatus.ERROR
		        || status == ExecutorStatus.LOADED)
		{
			m_input.setEnabled(false);
		}
		else
		{
			m_input.setEnabled(true);
		}
		// Update the user action status if needed
		IExecutionInformation info = m_model.getRuntimeInformation();
		String actionLabel = info.getUserAction();
		boolean actionEnabled = info.getUserActionStatus().equals(UserActionStatus.ENABLED);
		Severity actionSev = info.getUserActionSeverity();
		if ((actionLabel != null) && (!actionLabel.isEmpty()))
		{
			updateUserAction(actionEnabled ? UserActionStatus.ENABLED : UserActionStatus.DISABLED, actionLabel,
			        actionSev);
		}
	}

	/***************************************************************************
	 * Set the client mode (will enable or disable the control panel and input
	 * area)
	 **************************************************************************/
	public void setClientMode(ClientMode mode)
	{
		m_controlPanel.setClientMode(mode);
		m_input.setClientMode(mode);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void prompt(InputData promptData)
	{
		// Store the prompt data, used later for cancel or reset
		m_promptData = promptData;

		// If it is not a notification but a controlling prompt, update
		// the control panel buttons. If it is a notification the
		// buttons are already grayed out since this is a monitoring GUI.
		if (!promptData.isNotification())
		{
			// Prompt panel buttons to be updated accordingly
			m_controlPanel.onPrompt(true);
		}

		// Update the prompt input field and re-layout the area
		m_input.prompt(promptData);
		m_top.layout();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void resetPrompt()
	{
		Logger.debug("Reset prompt", Level.PROC, this);
		if (m_promptData != null)
		{
			// If it is not a notification but a controlling prompt, update
			// the control panel buttons. If it is a notification the
			// buttons must be kept grayed out since this is a monitoring GUI.
			if (!m_promptData.isNotification())
			{
				m_controlPanel.onPrompt(false);
				m_controlPanel.setFocus();
			}
			// Clear the prompt data
			m_promptData = null;
			// Reset the input area and re-layout
			m_input.reset();
			m_top.layout();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean cancelPrompt()
	{
		Logger.debug("Cancel prompt", Level.PROC, this);
		if (m_promptData != null)
		{
			// If it is not a notification but a controlling prompt, tell
			// the prompt data object to assume 'cancel' value. If this
			// is a notification this is a monitoring GUI and no return value
			// is expected.
			if (!m_promptData.isNotification())
			{
				m_controlPanel.onPrompt(false);
			}
			m_input.cancelPrompt();
			m_promptData = null;
			m_top.layout();
			return true;
		}
		return false;
	}

	/***************************************************************************
	 * Determine if we are inside a prompt
	 * 
	 * @return
	 **************************************************************************/
	public boolean isPrompt()
	{
		return ((m_promptData != null) && (!m_promptData.isReady()));
	}

	/***************************************************************************
	 * Parse and issue a command
	 **************************************************************************/
	public void issueCommand(String cmdString)
	{
		String[] elements = cmdString.split(" ");
		for (GuiExecutorCommand action : GuiExecutorCommand.values())
		{
			if (elements[0].toLowerCase().equals(action.command.toString().toLowerCase()))
			{
				String[] arguments = new String[0];
				if (elements.length > 1)
				{
					arguments = Arrays.copyOfRange(elements, 1, elements.length - 1);
				}
				try
				{
					m_model.getController().issueCommand(action.command, arguments);
				}
				catch (CommandFailed ex)
				{
					MessageDialog.openError(getShell(), "Command error", ex.getLocalizedMessage());
				}
				return;
			}
		}
		MessageDialog.openError(getShell(), "Command error", "Unrecognised command: " + cmdString);
	}

	/***************************************************************************
	 * Update user action related controls
	 * 
	 * @param st
	 * @param action
	 **************************************************************************/
	public void updateUserAction(UserActionStatus st, String action, Severity sev)
	{
		m_controlPanel.updateUserAction(st, action, sev);
	}

	/***************************************************************************
	 * Get height of input area
	 **************************************************************************/
	public int getInputAreaHeight()
	{
		return m_input.getClientArea().height;
	}
}
