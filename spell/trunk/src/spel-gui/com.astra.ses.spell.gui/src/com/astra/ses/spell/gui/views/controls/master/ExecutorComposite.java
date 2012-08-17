///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : ExecutorComposite.java
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
package com.astra.ses.spell.gui.views.controls.master;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.interfaces.IProcedureOperation;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.extensions.ProcedureBridge;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.CloseProcedureJob;
import com.astra.ses.spell.gui.model.jobs.ControlRemoteProcedureJob;
import com.astra.ses.spell.gui.model.jobs.KillProcedureJob;
import com.astra.ses.spell.gui.model.jobs.MonitorRemoteProcedureJob;
import com.astra.ses.spell.gui.model.jobs.ReleaseProcedureJob;
import com.astra.ses.spell.gui.model.jobs.RemoveProcedureControlJob;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/**
 * @author Rafael Chinchilla
 * 
 */
public class ExecutorComposite extends Composite implements IProcedureOperation, SelectionListener,
        IDoubleClickListener

{

	/** Button labels */
	private static final String	    BTN_TAKE_CONTROL	= "Take control";
	private static final String	    BTN_RELEASE_CONTROL	= "Release control";
	private static final String	    BTN_START_MONITOR	= "Start monitor";
	private static final String	    BTN_STOP_MONITOR	= "Stop monitor";
	private static final String	    BTN_STOP_EXECUTOR	= "Stop execution";
	private static final String	    BTN_KILL_EXECUTOR	= "Kill execution";
	private static final String	    BTN_REFRESH         = "Refresh";
	/** Procedure manager handle */
	private static IProcedureManager	s_procMgr	        = null;
	/** Procedure manager handle */
	private static IContextProxy	    s_proxy	            = null;

	public static final String	    ID	                = "com.astra.ses.spell.gui.dialogs.ExecutorsDialog";

	/** Holds the table of contexts */
	private CurrentExecutorsTable	m_executorsTable;
	/** Holds the take control button */
	private Button	                m_btnTakeControl;
	/** Holds the release control button */
	private Button	                m_btnReleaseControl;
	/** Holds the start monitor button */
	private Button	                m_btnStartMonitor;
	/** Holds the stop monitor button */
	private Button	                m_btnStopMonitor;
	/** Holds the stop executor button */
	private Button	                m_btnStopExecutor;
	/** Holds the kill executor button */
	private Button	                m_btnKillExecutor;
	/** Holds the refresh button */
	private Button	                m_btnRefresh;
	 


	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutorComposite(Composite parent, int style)
	{
		super(parent, style);
		ProcedureBridge.get().addProcedureOperationListener(this);
		if (s_procMgr == null)
		{
			s_procMgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}
		if (s_proxy == null)
		{
			s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
		
		// Ensure list of remote open procedures is up to date
		if (s_procMgr.canOperate())
		{
			s_procMgr.getOpenRemoteProcedures(true);
		}
		
		createContents();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void dispose()
	{
		super.dispose();
		ProcedureBridge.get().removeProcedureOperationListener(this);
	}

	/***************************************************************************
	 * Button callback
	 **************************************************************************/
	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	/***************************************************************************
	 * Button callback
	 **************************************************************************/
	@Override
	public void widgetSelected(SelectionEvent e)
	{
		if (e.widget instanceof Button)
		{
			Button button = (Button) e.widget;

			Logger.debug("Button pressed: " + button.getText(), Level.GUI, this);
			
			String[] procIds = m_executorsTable.getSelectedProcedures();

			disableButtons();

			if ( button == m_btnTakeControl )
			{
				doTakeControl(procIds);
			}
			else if ( button == m_btnReleaseControl )
			{
				doReleaseControl(procIds);
			}
			else if ( button == m_btnStartMonitor )
			{
				doStartMonitor(procIds);
			}
			else if ( button == m_btnStopMonitor )
			{
				doStopMonitor(procIds);
			}
			else if ( button == m_btnStopExecutor )
			{
				doStopExecutor(procIds);
			}
			else if ( button == m_btnKillExecutor )
			{
				doKillExecutor(procIds);
			}
			else  if ( button == m_btnRefresh )
			{
				doRefreshProcedures();
			}
			// Refresh button falls thru here
			updateExecutors();
		}
		else if (e.widget instanceof Table)
		{
			updateButtons();
		}
	}

	/***************************************************************************
	 * ID as context service listener
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return ID;
	}

	/***************************************************************************
	 * Refresh the table
	 **************************************************************************/
	public void refresh()
	{
		updateExecutors();
	}

	@Override
	public void notifyRemoteProcedureClosed(String procId, String guiKey)
	{
		Logger.debug("Procedure closed: " + procId, Level.GUI, this);
		updateExecutors();
	}

	@Override
	public void notifyRemoteProcedureControlled(String procId, String guiKey)
	{
		Logger.debug("Procedure controlled: " + procId, Level.GUI, this);
		m_executorsTable.refresh(procId);
		updateButtons();
	}

	@Override
	public void notifyRemoteProcedureKilled(String procId, String guiKey)
	{
		Logger.debug("Procedure killed: " + procId, Level.GUI, this);
		updateExecutors();
	}

	@Override
	public void notifyRemoteProcedureCrashed(String procId, String guiKey)
	{
		Logger.debug("Procedure crashed: " + procId, Level.GUI, this);
		m_executorsTable.refresh(procId);
		updateButtons();
	}

	@Override
	public void notifyRemoteProcedureMonitored(String procId, String guiKey)
	{
		Logger.debug("Procedure monitored: " + procId, Level.GUI, this);
		m_executorsTable.refresh(procId);
		updateButtons();
	}

	@Override
	public void notifyRemoteProcedureOpen(String procId, String guiKey)
	{
		Logger.debug("Procedure open: " + procId, Level.GUI, this);
		updateExecutors();
	}

	@Override
	public void notifyRemoteProcedureReleased(String procId, String guiKey)
	{
		Logger.debug("Procedure released: " + procId, Level.GUI, this);
		updateExecutors();
	}

	@Override
	public void notifyRemoteProcedureStatus(String procId, ExecutorStatus status, String guiKey)
	{
		Logger.debug("Procedure status: " + procId, Level.GUI, this);
		m_executorsTable.refresh(procId);
		updateButtons();
	}

	/***************************************************************************
	 * Create the executor information group
	 **************************************************************************/
	private void createContents()
	{
		GridLayout clayout = new GridLayout();
		clayout.marginHeight = 2;
		clayout.marginWidth = 2;
		clayout.marginTop = 2;
		clayout.marginBottom = 2;
		clayout.marginLeft = 2;
		clayout.marginRight = 2;
		clayout.numColumns = 1;
		setLayout(clayout);
		
		m_executorsTable = new CurrentExecutorsTable(this);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.grabExcessHorizontalSpace = true;
		tableLayoutData.widthHint = 700;
		tableLayoutData.heightHint = 200;
		m_executorsTable.getTable().setLayoutData(tableLayoutData);
		m_executorsTable.addDoubleClickListener(this);

		Composite buttonBar = new Composite(this, SWT.BORDER);
		buttonBar.setLayout(new FillLayout(SWT.HORIZONTAL));
		buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		m_btnTakeControl = new Button(buttonBar, SWT.PUSH);
		m_btnTakeControl.setText(BTN_TAKE_CONTROL);
		m_btnTakeControl.addSelectionListener(this);
		
		m_btnReleaseControl = new Button(buttonBar, SWT.PUSH);
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
		
		m_btnRefresh = new Button(buttonBar, SWT.PUSH);
		m_btnRefresh.setText(BTN_REFRESH);
		m_btnRefresh.addSelectionListener(this);

		disableButtons();
	}

	/***************************************************************************
	 * Update the executors table
	 **************************************************************************/
	private void updateExecutors()
	{
		Logger.debug("Updating executors table", Level.GUI, this);
		m_executorsTable.refresh();
		updateButtons();
	}

	/***************************************************************************
	 * Disable button bar
	 **************************************************************************/
	private void disableButtons()
	{
		m_btnTakeControl.setEnabled(false);
		m_btnReleaseControl.setEnabled(false);
		m_btnStartMonitor.setEnabled(false);
		m_btnStopMonitor.setEnabled(false);
		m_btnStopExecutor.setEnabled(false);
		m_btnKillExecutor.setEnabled(false);
	}

	/***************************************************************************
	 * Update button bar
	 **************************************************************************/
	private void updateButtons()
	{
		m_btnRefresh.setEnabled(s_proxy.isConnected());
		String[] procIds = m_executorsTable.getSelectedProcedures();
		if (procIds.length == 0)
		{
			disableButtons();
		}
		else
		{
			/*
			 * Activation rules:
			 * 
			 * 1.Take control: all selected procedures shall be remote and
			 * uncontrolled, OR local, monitored by me and uncontrolled
			 * 2.Release control: all selected procedures shall be local and
			 * controlled by me. Also, NOT waiting input. 3.Start monitor: all
			 * selected procedures shall be remote 4.Stop monitor: all selected
			 * procedures shall be local and monitored by me 5.Stop execution:
			 * all selected procedures shall be local and controlled by me
			 * 6.Kill execution: all selected procedures shall be uncontrolled
			 * or controlled by me
			 */
			boolean canTakeControl = canTakeControl(procIds);
			boolean canReleaseControl = canReleaseControl(procIds);
			boolean canStartMonitor = canStartMonitor(procIds);
			boolean canStopMonitor = canStopMonitor(procIds);
			boolean canStopExecution = canStopExecution(procIds);

			m_btnTakeControl.setEnabled(canTakeControl);
			m_btnReleaseControl.setEnabled(canReleaseControl);
			m_btnStartMonitor.setEnabled(canStartMonitor);
			m_btnStopMonitor.setEnabled(canStopMonitor);
			m_btnStopExecutor.setEnabled(canStopExecution);
			m_btnKillExecutor.setEnabled(canStopExecution);
		}
	}

	/***************************************************************************
	 * Check if Take Control command can be used for all selected procs
	 **************************************************************************/
	private boolean canTakeControl(String[] procIds)
	{
		// Ensure that all are uncontrolled.
		for (String procId : procIds)
		{
			if (s_procMgr.isLocallyLoaded(procId))
			{
				IProcedure proc = s_procMgr.getProcedure(procId);
				// If local and already controlled by me
				IExecutionInformation info = proc.getRuntimeInformation();
				IProcedureClient client = info.getControllingClient();
				if (client != null)
				{
					String cClient = client.getKey();
					if (cClient.equals(s_proxy.getClientKey())) { return false; }
				}

				// If local and being monitored by me
				IProcedureClient[] mClients = info.getMonitoringClients();
				if (mClients != null)
				for (IProcedureClient mclient : mClients)
				{
					if (mclient.getKey().equals(s_proxy.getClientKey())) { return false; }
				}
			}
			// When it is remote or it is not already controlled by my gui
			// allow to take control but not if the status is RUNNING. Further conditions will be imposed
			else 
			{
				IProcedure proc = s_procMgr.getRemoteProcedure(procId);
				if (proc.getRuntimeInformation().getStatus().equals(ExecutorStatus.RUNNING))
				{
					return false;
				}
			}
		}
		return true;
	}

	/***************************************************************************
	 * Check if Release Control command can be used for all selected procs
	 **************************************************************************/
	private boolean canReleaseControl(String[] procIds)
	{
		// Ensure that all are controlled by me. They shall be local for that.
		for (String procId : procIds)
		{
			if (!s_procMgr.isLocallyLoaded(procId)) return false;
			IProcedure proc = s_procMgr.getProcedure(procId);
			IExecutionInformation info = proc.getRuntimeInformation();
			ClientMode mode = info.getClientMode();
			boolean onPrompt = info.isWaitingInput();
			boolean okToRelease = (mode == ClientMode.CONTROLLING) && (!onPrompt);
			if (!okToRelease) return false;
		}
		return true;
	}

	/***************************************************************************
	 * Check if Start Monitor command can be used for all selected procs
	 **************************************************************************/
	private boolean canStartMonitor(String[] procIds)
	{
		// Ensure that all are uncontrolled by me.
		for (String procId : procIds)
		{
			// It cannot be local
			if (s_procMgr.isLocallyLoaded(procId)) return false;
			IProcedure proc = s_procMgr.getRemoteProcedure(procId);
			//TODO set this in preferences
			if (proc.getRuntimeInformation().getMonitoringClients().length>5)
			{
				return false;
			}
		}
		return true;
	}

	/***************************************************************************
	 * Check if Stop Monitor command can be used for all selected procs
	 **************************************************************************/
	private boolean canStopMonitor(String[] procIds)
	{
		// Ensure that all are uncontrolled by me.
		for (String procId : procIds)
		{
			// It cannot be remote
			if (!s_procMgr.isLocallyLoaded(procId)) return false;
			IProcedure proc = s_procMgr.getProcedure(procId);
			if (proc.getRuntimeInformation().getClientMode() != ClientMode.MONITORING) return false;
		}
		return true;
	}

	/***************************************************************************
	 * Check if Stop Execution command can be used for all selected procs
	 **************************************************************************/
	private boolean canStopExecution(String[] procIds)
	{
		// Ensure that all are uncontrolled by me.
		for (String procId : procIds)
		{
			if (s_procMgr.isLocallyLoaded(procId))
			{
				IProcedure proc = s_procMgr.getProcedure(procId);
				if (proc.getRuntimeInformation().getClientMode() != ClientMode.CONTROLLING) return false;
			}
			else
			{
				// We can stop/kill it if nobody is controlling or it is remote but assigned to me
				IProcedure proc = s_procMgr.getRemoteProcedure(procId);
				if (proc.getRuntimeInformation().getControllingClient() != null)
				{
					String myKey = s_proxy.getClientKey();
					if (proc.getRuntimeInformation().getControllingClient().getKey().equals(myKey))
					{
						return true;
					}
					return false;
				}
			}
		}
		return true;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doTakeControl(String[] procIds)
	{
		for (String procId : procIds)
		{
			// If we want to steal control
			IProcedure proc = s_procMgr.getRemoteProcedure(procId);
			IExecutionInformation info = proc.getRuntimeInformation();
			IProcedureClient cClient = info.getControllingClient();
			
			boolean attachControl = false;
			
			if (cClient != null)
			{
				boolean proceed = MessageDialog.openConfirm(getShell(), "Steal Procedure Control", 
						"The procedure is begin controlled already. Do you want to steal the control?");
				if (proceed)
				{
					RemoveProcedureControlJob job = new RemoveProcedureControlJob(procId);
					CommandHelper.executeInProgress(job, true, true);
					if (job.result.equals(CommandResult.CANCELLED)) continue;
					attachControl = true;
				}
			}
			else
			{
				attachControl = true;
			}
			
			if (attachControl)
			{
				Logger.debug("Taking control of " + procId, Level.PROC, this);
				ControlRemoteProcedureJob job = new ControlRemoteProcedureJob(procId);
				CommandHelper.executeInProgress(job, true, true);
				if (job.result == CommandResult.FAILED)
				{
					MessageDialog.openError(getShell(), "Attach error", job.message);
				}
				else if (job.result == CommandResult.CANCELLED)
				{
					break;
				}
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doReleaseControl(String[] procIds)
	{
		for (String procId : procIds)
		{
			Logger.debug("Releasing control of " + procId, Level.PROC, this);
			ReleaseProcedureJob job = new ReleaseProcedureJob(procId);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Detach error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doStartMonitor(String[] procIds)
	{
		for (String procId : procIds)
		{
			Logger.debug("Start monitor of " + procId, Level.PROC, this);
			MonitorRemoteProcedureJob job = new MonitorRemoteProcedureJob(procId);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Attach error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doStopMonitor(String[] procIds)
	{
		for (String procId : procIds)
		{
			Logger.debug("Stop monitoring " + procId, Level.PROC, this);
			ReleaseProcedureJob job = new ReleaseProcedureJob(procId);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Detach error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doStopExecutor(String[] procIds)
	{
		for (String procId : procIds)
		{
			Logger.debug("Closing procedure " + procId, Level.PROC, this);
			CloseProcedureJob job = new CloseProcedureJob(procId);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Close error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doKillExecutor(String[] procIds)
	{
		for (String procId : procIds)
		{
			Logger.debug("Killing procedure " + procId, Level.PROC, this);
			KillProcedureJob job = new KillProcedureJob(procId);
			CommandHelper.executeInProgress(job, true, true);
			if (job.result == CommandResult.FAILED)
			{
				MessageDialog.openError(getShell(), "Kill error", job.message);
			}
			else if (job.result == CommandResult.CANCELLED)
			{
				break;
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void doRefreshProcedures()
	{
		Logger.debug("Refreshing procedures", Level.PROC, this);
		s_procMgr.getOpenRemoteProcedures(true);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void doubleClick(DoubleClickEvent event)
	{
		Object sel = ((IStructuredSelection) event.getSelection()).getFirstElement();
		if (sel instanceof IProcedure)
		{

		}
		else if (sel instanceof IExecutorInfo)
		{

		}
	}

}
