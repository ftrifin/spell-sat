///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : ExecutorsDialog.java
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
package com.astra.ses.spell.gui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.interfaces.IProcedureOperation;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.extensions.ProcedureBridge;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.CloseProcedureJob;
import com.astra.ses.spell.gui.model.jobs.ControlRemoteProcedureJob;
import com.astra.ses.spell.gui.model.jobs.KillProcedureJob;
import com.astra.ses.spell.gui.model.jobs.MonitorRemoteProcedureJob;
import com.astra.ses.spell.gui.model.jobs.ReleaseProcedureJob;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.views.controls.tables.ExecutorsTable;


/*******************************************************************************
 * @brief Dialog for selecting the executors to be controlled
 * @date 18/09/07
 * @author Rafael Chinchilla (GMV)
 ******************************************************************************/
public class ExecutorsDialog extends TitleAreaDialog implements SelectionListener, IProcedureOperation
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Button labels */
	private static final String BTN_TAKE_CONTROL		= "Take control";
	private static final String BTN_RELEASE_CONTROL   	= "Release control";
	private static final String BTN_START_MONITOR		= "Start monitor";
	private static final String BTN_STOP_MONITOR		= "Stop monitor";
	private static final String BTN_STOP_EXECUTOR		= "Stop execution";
	private static final String BTN_KILL_EXECUTOR		= "Kill execution";
	/** Procedure manager handle */
	private static ProcedureManager s_procMgr = null;
	/** Procedure manager handle */
	private static ContextProxy s_proxy = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String ID = "com.astra.ses.spell.gui.dialogs.ExecutorsDialog";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the table of contexts */
	private ExecutorsTable m_executors;
	/** Holds the take control button */
	private Button m_btnTakeControl;
	/** Holds the release control button */
	private Button m_btnReleaseControl;
	/** Holds the start monitor button */
	private Button m_btnStartMonitor;
	/** Holds the stop monitor button */
	private Button m_btnStopMonitor;
	/** Holds the stop executor button */
	private Button m_btnStopExecutor;
	/** Holds the kill executor button */
	private Button m_btnKillExecutor;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public ExecutorsDialog(Shell shell)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_exec.png");
		m_image = descr.createImage();
		ProcedureBridge.get().addProcedureMonitor(this);
		if (s_procMgr == null)
		{
			s_procMgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
		}
		if (s_proxy == null)
		{
			s_proxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		}
	}

	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		m_image.dispose();
		ProcedureBridge.get().removeProcedureMonitor(this);
		return super.close();
	}

	/***************************************************************************
	 * Button callback
	 **************************************************************************/
	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	/***************************************************************************
	 * Button callback
	 **************************************************************************/
	public void widgetSelected(SelectionEvent e)
	{
		if (e.widget instanceof Button)
		{
			Button b = (Button) e.widget;
			String label = b.getText();
			String procId = m_executors.getSelectedProcedure();
			if (procId == null) return;
			boolean closeAfter = true;
			if (label.equals(BTN_TAKE_CONTROL))
			{
				Logger.debug("Taking control of "+ procId, Level.PROC, this);
				ControlRemoteProcedureJob job = new ControlRemoteProcedureJob(procId);
				CommandHelper.executeInProgress(job,false,false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(getShell(), "Attach error", job.message);
					closeAfter = false;
				}
			}
			else if (label.equals(BTN_RELEASE_CONTROL))
			{
				Logger.debug("Releasing control of "+ procId, Level.PROC, this);
				ReleaseProcedureJob job = new ReleaseProcedureJob(procId);
				CommandHelper.executeInProgress(job,false,false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(getShell(), "Detach error", job.message);
				}
				closeAfter = false;
			}
			else if (label.equals(BTN_START_MONITOR))
			{
				Logger.debug("Start monitor of "+ procId, Level.PROC, this);
				MonitorRemoteProcedureJob job = new MonitorRemoteProcedureJob(procId);
				CommandHelper.executeInProgress(job,false,false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(getShell(), "Attach error", job.message);
					closeAfter = false;
				}
			}
			else if (label.equals(BTN_STOP_MONITOR))
			{
				Logger.debug("Stop monitoring "+ procId, Level.PROC, this);
				ReleaseProcedureJob job = new ReleaseProcedureJob(procId);
				CommandHelper.executeInProgress(job,false,false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(getShell(), "Detach error", job.message);
				}
				closeAfter = false;
			}
			else if (label.equals(BTN_STOP_EXECUTOR))
			{
				Logger.debug("Closing procedure "+ procId, Level.PROC, this);
				CloseProcedureJob job = new CloseProcedureJob(procId);
				CommandHelper.executeInProgress(job,false,false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(getShell(), "Close error", job.message);
				}
				closeAfter = false;
			}
			else if (label.equals(BTN_KILL_EXECUTOR))
			{
				Logger.debug("Killing procedure "+ procId, Level.PROC, this);
				KillProcedureJob job = new KillProcedureJob(procId);
				CommandHelper.executeInProgress(job,false,false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(getShell(), "Kill error", job.message);
				}
				closeAfter = false;
			}
			if (closeAfter) 
			{
				close();
			}
			else
			{
				updateExecutors();
			}
		}
		else if (e.widget instanceof Table)
		{
			updateButtons();
		}
	}

	/***************************************************************************
	 * ID as context service listener
	 **************************************************************************/
	public String getID()
	{
		return "Executor List";
	}

	/***************************************************************************
	 * Context service listener callback
	 **************************************************************************/
	public void serviceUpdated(String id)
	{
		m_executors.updateExecutors();
	}
	
	/***************************************************************************
	 * Procedure listener callback
	 **************************************************************************/
	@Override
	public void procedureClosed(String procId, String guiKey)
	{
		Logger.debug("Procedure closed: " + procId, Level.GUI, this);
		m_executors.removeExecutor(procId);
	}

	/***************************************************************************
	 * Procedure listener callback
	 **************************************************************************/
	@Override
	public void procedureControlled(String procId, String guiKey)
	{
		Logger.debug("Procedure controlled: " + procId, Level.GUI, this);
		m_executors.updateExecutor(procId);
		updateButtons();
	}

	/***************************************************************************
	 * Procedure listener callback
	 **************************************************************************/
	@Override
	public void procedureKilled(String procId, String guiKey)
	{
		Logger.debug("Procedure killed: " + procId, Level.GUI, this);
		m_executors.removeExecutor(procId);
	}

	/***************************************************************************
	 * Procedure listener callback
	 **************************************************************************/
	@Override
	public void procedureMonitored(String procId, String guiKey)
	{
		Logger.debug("Procedure monitored: " + procId, Level.GUI, this);
		m_executors.updateExecutor(procId);
	}

	/***************************************************************************
	 * Procedure listener callback
	 **************************************************************************/
	@Override
	public void procedureOpen(String procId, String guiKey)
	{
		Logger.debug("Procedure open: " + procId, Level.GUI, this);
		m_executors.addExecutor(procId);
	}

	/***************************************************************************
	 * Procedure listener callback
	 **************************************************************************/
	@Override
	public void procedureReleased(String procId, String guiKey)
	{
		Logger.debug("Procedure released: " + procId, Level.GUI, this);
		m_executors.updateExecutor(procId);
		updateButtons();
	}

	/***************************************************************************
	 * Procedure listener callback
	 **************************************************************************/
	@Override
	public void procedureStatus(String procId, ExecutorStatus status, String guiKey)
	{
		Logger.debug("Procedure status: " + procId, Level.GUI, this);
		m_executors.updateExecutor(procId);
		updateButtons();
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Creates the dialog contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		setMessage("These are the currently running procedures in the context:");
		setTitle("Current Procedures");
		setTitleImage(m_image);
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createDialogArea(Composite parent)
	{
		// Main composite of the dialog area -----------------------------------
		Composite top = new Composite(parent, SWT.NONE);
		GridData areaData = new GridData(GridData.FILL_BOTH);
		areaData.widthHint = 700;
		top.setLayoutData(areaData);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.numColumns = 1;
		top.setLayout(layout);

		createExecutorsGroup(top);
		
		return parent;
	}
	
	/***************************************************************************
	 * Create the executor information group
	 **************************************************************************/
	protected void createExecutorsGroup( Composite parent )
	{
		Group executorsGroup = new Group( parent, SWT.NONE );
		GridData cld = new GridData(GridData.FILL_HORIZONTAL);
		cld.heightHint = 220;
		executorsGroup.setLayoutData(cld);
		executorsGroup.setText( "Available Contexts" );
		GridLayout clayout = new GridLayout();
		clayout.marginHeight = 2;
		clayout.marginWidth = 2;
		clayout.marginTop = 2;
		clayout.marginBottom = 2;
		clayout.marginLeft = 2;
		clayout.marginRight = 2;
		clayout.numColumns = 1;
		executorsGroup.setLayout(clayout);
		

		m_executors = new ExecutorsTable(executorsGroup,this);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.widthHint = m_executors.getTableWidthHint();
		tableLayoutData.heightHint = m_executors.getTableHeightHint();	
		m_executors.getTable().setLayoutData(tableLayoutData);

		Composite buttonBar = new Composite(executorsGroup, SWT.BORDER);
		buttonBar.setLayout( new FillLayout(SWT.HORIZONTAL) );
		buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_btnTakeControl = new Button(buttonBar, SWT.PUSH);
		m_btnTakeControl.setText(BTN_TAKE_CONTROL);
		m_btnTakeControl.addSelectionListener(this);
		m_btnReleaseControl= new Button(buttonBar, SWT.PUSH);
		m_btnReleaseControl.setText(BTN_RELEASE_CONTROL);
		m_btnReleaseControl.addSelectionListener(this);
		m_btnStartMonitor = new Button(buttonBar, SWT.PUSH);
		m_btnStartMonitor.setText(BTN_START_MONITOR);
		m_btnStartMonitor.addSelectionListener(this);
		m_btnStopMonitor = new Button(buttonBar, SWT.PUSH);
		m_btnStopMonitor.setText(BTN_STOP_MONITOR);
		m_btnStopMonitor.addSelectionListener(this);
		m_btnStopExecutor = new Button(buttonBar, SWT.PUSH);
		m_btnStopExecutor.setText(BTN_STOP_EXECUTOR);
		m_btnStopExecutor.addSelectionListener(this);
		m_btnKillExecutor = new Button(buttonBar, SWT.PUSH);
		m_btnKillExecutor.setText(BTN_KILL_EXECUTOR);
		m_btnKillExecutor.addSelectionListener(this);
		
		updateExecutors();
	}
	
	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL,
				true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	protected void buttonPressed(int buttonId)
	{
		switch (buttonId)
		{
		case IDialogConstants.CLOSE_ID:
			close();
		}
	}

	/***************************************************************************
	 * Update the executors table
	 **************************************************************************/
	protected void updateExecutors()
	{
		Logger.debug("Updating executors table", Level.GUI, this);
		m_executors.updateExecutors();
		updateButtons();
	}

	/***************************************************************************
	 * Update button bar  
	 **************************************************************************/
	protected void updateButtons()
	{
		String procId = m_executors.getSelectedProcedure();
		if (procId == null)
		{
			m_btnTakeControl.setEnabled(false);
			m_btnReleaseControl.setEnabled(false);
			m_btnStartMonitor.setEnabled(false);
			m_btnStopMonitor.setEnabled(false);
			m_btnStopExecutor.setEnabled(false);
			m_btnKillExecutor.setEnabled(false);
		}
		else 
		{
			if (s_procMgr.isLocallyLoaded(procId))
			{
				// If it is local determine if it is loaded in control or monitoring mode
				ClientMode mode = s_procMgr.getProcedure(procId).getClientMode();
				boolean onPrompt = s_procMgr.getProcedure(procId).isWaitingInput();
				if (mode == ClientMode.CONTROLLING)
				{
					m_btnTakeControl.setEnabled(false);
					m_btnReleaseControl.setEnabled(!onPrompt);
					m_btnStartMonitor.setEnabled(false);
					m_btnStopMonitor.setEnabled(false);
					m_btnStopExecutor.setEnabled(true);
					m_btnKillExecutor.setEnabled(true);
				}
				else
				{
					m_btnTakeControl.setEnabled(false);
					m_btnReleaseControl.setEnabled(false);
					m_btnStartMonitor.setEnabled(false);
					m_btnStopMonitor.setEnabled(true);
					m_btnStopExecutor.setEnabled(false);
					m_btnKillExecutor.setEnabled(false);
				}
			}
			else
			{
				ExecutorInfo info = s_procMgr.getRemoteProcedure(procId);
				String cClient = info.getControllingClient();
				String mKey = s_proxy.getClientKey();
				boolean iAmControlling = mKey.equals(cClient);
				boolean iAmMonitoring = info.getMonitoringClients().contains(mKey);
				if (cClient.length()==0)
				{
					// Nobody controlling
					m_btnTakeControl.setEnabled(true);
					m_btnStopExecutor.setEnabled(true);
					m_btnKillExecutor.setEnabled(true);
				}
				else
				{
					// Cannot control, because I am controlling already
					// or because there is somebody controlling
					m_btnTakeControl.setEnabled(false);
					m_btnStopExecutor.setEnabled(false);
					m_btnKillExecutor.setEnabled(false);
				}
				if (iAmControlling)
				{
					// I am monitoring already
					m_btnStartMonitor.setEnabled(false);
					m_btnStopMonitor.setEnabled(false);
				}
				else if (iAmMonitoring)
				{
					m_btnStartMonitor.setEnabled(false);
					m_btnStopMonitor.setEnabled(true);
				}
				else
				{
					m_btnStartMonitor.setEnabled(true);
					m_btnStopMonitor.setEnabled(false);
				}
			}
		}
	}

	@Override
	public String getListenerId()
	{
		return ID;
	}
}
