///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : ProcedureControlPanel.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.views.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.comm.commands.CmdAbort;
import com.astra.ses.spell.gui.core.comm.commands.CmdGoto;
import com.astra.ses.spell.gui.core.comm.commands.CmdRun;
import com.astra.ses.spell.gui.core.comm.commands.CmdScript;
import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.dialogs.GotoDialog;
import com.astra.ses.spell.gui.dialogs.ScriptDialog;
import com.astra.ses.spell.gui.model.GuiCommands;
import com.astra.ses.spell.gui.model.commands.ToggleByStep;
import com.astra.ses.spell.gui.model.commands.ToggleRunInto;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.services.ConfigurationManager;
import com.astra.ses.spell.gui.views.ProcedureView;


/*******************************************************************************
 * @brief Composite which contains the set of controls used during the procedure
 *        execution.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ProcedureControlPanel extends Composite implements
		SelectionListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final String CMD_ID = "CommandId";
	private static final int POS_RUN 		= 0;
	private static final int POS_STEP 		= 1;
	private static final int POS_STEP_OVER 	= 2;
	private static final int POS_SKIP 		= 3;
	private static final int POS_PAUSE		= 4;
	private static final int POS_GOTO 		= 5;
	private static final int POS_SCRIPT     = 6;
	private static final int POS_RELOAD 	= 7;
	private static final int POS_ABORT 		= 8;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Handle for the containing view */
	private ProcedureView m_view;
	/** List of control buttons */
	private Vector<Button> m_controlButtons;
	/** Holds the current proc status */
	private ExecutorStatus m_currentStatus;
	/** Holds the status display */
	private Label m_statusText;
	/** Holds the autoscroll checkbox */
	private Button m_autoScroll;
	/** Holds the run over checkbox */
	private Button m_runInto;
	/** Holds the by step checkbox */
	private Button m_byStep;
	/** Holds the client mode */
	private ClientMode m_clientMode;
	/** Map for getting the color for each ExecutorStatus */
	private Map<ExecutorStatus, Color> m_procColors;
	/** Holds the script dialog */
	private ScriptDialog m_scriptDialog;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================
	
	/***************************************************************************
	 * Constructor
	 * 
	 *  @param view
	 *  	Containing view
	 *  @param parent
	 *  	Parent composite
	 *  @style
	 *  	SWT style
	 **************************************************************************/
	public ProcedureControlPanel(ProcedureView view, Composite parent, int style)
	{
		super(parent, style);

		// Store the procedure view handle
		m_view = view;
		m_clientMode = null;
		m_scriptDialog = null;
		
		ArrayList<ExecutorCommand> guiCommands = GuiCommands.getGuiCommands();
		
		// Construct the composite contents
		
		// Use gridlayout for placing the buttons
		setLayoutData(new GridData(SWT.BEGINNING,SWT.END, true, false));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 1;
		layout.numColumns = guiCommands.size() + 4;
		setLayout(layout);

		m_controlButtons = new Vector<Button>();

		// For each defined command, create a button
		// ExecutorCommand names and images are defined in the ICommands interface
		int counter = 0;
		
		// Create the buttons for commands
		for (ExecutorCommand cmd : guiCommands )
		{
			Button btn = new Button(this, SWT.PUSH);
			btn.setText(cmd.getCmdString());
			Image image = Activator.getImageDescriptor(cmd.getIconPath()).createImage();
			btn.setImage(image);
			btn.setData(CMD_ID, cmd);
			btn.setToolTipText(cmd.getHelp());
			counter++;
			btn.addSelectionListener(this);
			m_controlButtons.addElement(btn);
		}

		m_procColors = new HashMap<ExecutorStatus,Color>();
		ConfigurationManager rsc = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		for (ExecutorStatus status : ExecutorStatus.values())
		{
			Color statusColor = rsc.getProcedureColor(status);
			m_procColors.put(status, statusColor);
		}
		
		m_statusText = new Label(this,SWT.BORDER);
		m_statusText.setText("    UNINIT    ");
		m_statusText.setFont(rsc.getFont("HEADER"));
		m_statusText.setAlignment(SWT.CENTER);
		GridData ldata = new GridData(GridData.FILL_HORIZONTAL);
		ldata.grabExcessHorizontalSpace = true;
		ldata.widthHint = 150;
		m_statusText.setLayoutData(ldata);
		
		m_autoScroll = new Button(this, SWT.CHECK);
		m_autoScroll.setText("Autoscroll");
		m_autoScroll.setSelection(true);
		m_autoScroll.setToolTipText("Enable or disable automatic scroll on procedure views");
		m_autoScroll.addSelectionListener(this);

		m_runInto = new Button(this, SWT.CHECK);
		m_runInto.setText("Run Into");
		m_runInto.setSelection(false);
		m_runInto.setToolTipText("Enable or disable run into functions mode");
		m_runInto.addSelectionListener(this);

		m_byStep = new Button(this, SWT.CHECK);
		m_byStep.setText("By Step");
		m_byStep.setSelection(false);
		m_byStep.setToolTipText("Enable or disable step by step run mode");
		m_byStep.addSelectionListener(this);

		pack();
		
		// Update the buttons to loaded state
		m_currentStatus = ExecutorStatus.UNINIT;
		notifyProcStatus(ExecutorStatus.UNINIT);
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setEnabled( boolean enabled )
	{
		if (enabled && m_clientMode == ClientMode.CONTROLLING )
		{
			notifyProcStatus(m_currentStatus);
		}
		else
		{
			for(Button b : m_controlButtons)
			{
				b.setEnabled(false);
			}
		}
		super.setEnabled(enabled);
	}
	
	@Override
	public boolean setFocus()
	{
		for (Button button : m_controlButtons)
		{
			if (button.isEnabled())
			{
				button.setFocus();
				break;
			}
		}
		return super.setFocus();
	}
	
	@Override
	public void dispose()
	{
		for (Color color : m_procColors.values())
		{
			color.dispose();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setClientMode( ClientMode mode )
	{
		m_clientMode = mode;
		if (mode != ClientMode.CONTROLLING)
		{
			setEnabled(false);
		}
	}

	/***************************************************************************
	 * Callback for procedure status change. Depending on the status, some
	 * buttons are enabled and other are disabled.
	 * 
	 * @param status
	 * 		The procedure status
	 **************************************************************************/
	public void notifyProcStatus( ExecutorStatus status )
	{
		//Logger.instance().debug("Received status: " + ProcedureHelper.toString(status), Logger.Level.GUI, this);
		m_currentStatus = status;
		if (status == ExecutorStatus.LOADED)
		{
			m_statusText.setText("PREPARING");
		}
		else
		{
			m_statusText.setText(status.toString());
		}
		m_statusText.setBackground(m_procColors.get(status));
		pack();
		//Procedure control buttons enablement
		if (m_clientMode != null)
		{
			boolean controlling = false;
			controlling = m_clientMode.equals(ClientMode.CONTROLLING);
			for (Button procButton : m_controlButtons)
			{
				ExecutorCommand comm = (ExecutorCommand) procButton.getData(CMD_ID);
				boolean enabled = comm.validate(status) && controlling;
				procButton.setEnabled(enabled);
			}
			// Code execution tracking buttons enablement
			boolean trackingEnabled = status.equals(ExecutorStatus.RUNNING) ||
									  status.equals(ExecutorStatus.STEPPING) ||
									  status.equals(ExecutorStatus.PAUSED);
			m_autoScroll.setEnabled(trackingEnabled);
			m_runInto.setEnabled(trackingEnabled && controlling);
			m_byStep.setEnabled(trackingEnabled && controlling);
		}
	}
	
	/***************************************************************************
	 * Callback procedure model configuration changes
	 **************************************************************************/
	public void notifyModelConfigured()
	{
		m_runInto.setSelection( m_view.getModel().getRunInto() );
		m_byStep.setSelection( m_view.getModel().getStepByStep() );
	}

	/***************************************************************************
	 * Callback of selection service, called when a button is clicked. NOT USED
	 * 
	 *  @param e 
	 *  	Selection event
	 **************************************************************************/
	public void widgetDefaultSelected(SelectionEvent e)
	{
		System.err.println("UNUSED CALLBACK! - ProcedureControlPanel");
	}

	/***************************************************************************
	 * Callback of selection service, called when a button is clicked.
	 * 
	 *  @param e 
	 *  	Selection event
	 **************************************************************************/
	public void widgetSelected(SelectionEvent e)
	{
		// Check first special buttons
		if (e.widget == m_autoScroll)
		{
			m_view.setAutoScroll( m_autoScroll.getSelection() );
			return;
		}
		if (e.widget == m_runInto)
		{
			CommandHelper.execute(ToggleRunInto.ID);
		}
		else if (e.widget == m_byStep)
		{
			CommandHelper.execute(ToggleByStep.ID);
		}
		else
		{
			ExecutorCommand cmd = (ExecutorCommand) e.widget.getData(CMD_ID);
			processGuiCommand(cmd);
		}
	}
	
	/***************************************************************************
	 * Enter the prompt state. Only abort is allowed
	 **************************************************************************/
	public void onPrompt( boolean promptActive )
	{
		if (!promptActive)
		{
			notifyProcStatus(m_currentStatus);
		}
		else
		{
			m_controlButtons.elementAt(POS_RUN).setEnabled(false);
			m_controlButtons.elementAt(POS_STEP).setEnabled(false);
			m_controlButtons.elementAt(POS_STEP_OVER).setEnabled(false);
			m_controlButtons.elementAt(POS_SKIP).setEnabled(false);
			m_controlButtons.elementAt(POS_PAUSE).setEnabled(true);
			m_controlButtons.elementAt(POS_GOTO).setEnabled(false);
			m_controlButtons.elementAt(POS_SCRIPT).setEnabled(true);
			m_controlButtons.elementAt(POS_RELOAD).setEnabled(false);
			m_controlButtons.elementAt(POS_ABORT).setEnabled(true);
		}
	}

	/***************************************************************************
	 * Add the script output
	 **************************************************************************/
	public void addManualDisplay( DisplayData data )
	{
		if (m_scriptDialog != null)
		{
			m_scriptDialog.addScriptOutput(data.getMessage());
		}
	}

	/***************************************************************************
	 * Add the script output
	 **************************************************************************/
	public void addManualError( ErrorData data )
	{
		if (m_scriptDialog != null)
		{
			m_scriptDialog.addScriptOutput( "ERROR: " + data.getMessage());
		}
	}

	/***************************************************************************
	 * Add the script output
	 **************************************************************************/
	public void addManualItem( ItemNotification data )
	{
		if (m_scriptDialog != null)
		{
			//TODO
		}
	}

	/***************************************************************************
	 * Process a panel command
	 **************************************************************************/
	private void processGuiCommand( ExecutorCommand cmd )
	{
		boolean doIt = true;
		try
		{
			String id = m_view.getModel().getProcId();
			cmd.setProcId(id);
			if (cmd instanceof CmdAbort) 
			{
				// If a prompt has been cancelled as a result of the abort,
				// do not need to send the command, the procedure will be
				// aborted immediately
				if (m_view.cancelPrompt()) 
				{
					doIt = false;
				}
			}
			else if (cmd instanceof CmdGoto) 
			{
				GotoDialog dialog = new GotoDialog(Display.getCurrent().getActiveShell());
				// Process the returned value
				if (dialog.open() == Window.OK)
				{
					String target = dialog.getTarget();
					if (target != null && (!target.isEmpty()))
					{
						CmdGoto gto = (CmdGoto) cmd;
						if (dialog.isLabel())
						{
							gto.setTargetLabel(target);
						}
						else
						{
							gto.setTargetLine(target);
						}
					}
					else
					{
						doIt = false;
					}
				}
				else
				{
					doIt = false;
				}
			}
			else if (cmd instanceof CmdScript) 
			{
				// Since more complex processing is required,
				// the ScriptDialog will handle the command by itself
				// no need to send any executor command from here.
				doIt = false;
				// Open the dialog.
				if (m_scriptDialog == null)
				{
					// Create the dialog
					m_scriptDialog = new ScriptDialog(Display.getCurrent().getActiveShell(), m_view.getProcId());
					// Process the returned value
					m_scriptDialog.open();
					m_scriptDialog = null;
				}
			}
			else if (cmd instanceof CmdRun) 
			{
				Vector<String> args = new Vector<String>();
				if (m_runInto.getSelection())
				{
					args.addElement("DO_RUN_INTO");
				}
				else
				{
					args.addElement("DO_RUN_OVER");
				}
				cmd.setArgs( args );
			}
			try
			{
				if (doIt)
				{
					m_view.getModel().issueCommand(cmd);
				}
			}
			catch(CommandFailed ex)
			{
				MessageDialog.openError(getShell(), "Command error", ex.getLocalizedMessage());	
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		};
	}
}
