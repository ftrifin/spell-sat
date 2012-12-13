///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : ProcedureView.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.dialogs.CloseProcDialog;
import com.astra.ses.spell.gui.interfaces.IProcedurePresentation;
import com.astra.ses.spell.gui.model.commands.ToggleByStep;
import com.astra.ses.spell.gui.model.commands.ToggleRunInto;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation.StepOverMode;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IStepOverControl;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;
import com.astra.ses.spell.gui.views.controls.ControlArea;
import com.astra.ses.spell.gui.views.controls.PresentationPanel;
import com.astra.ses.spell.gui.views.controls.SplitPanel;
import com.astra.ses.spell.gui.views.presentations.PresentationManager;
import com.astra.ses.spell.gui.views.presentations.PresentationNotifier;
import com.astra.ses.spell.gui.views.presentations.PresentationStack;

/*******************************************************************************
 * @brief This view (multiple) shows a procedure code and contains the controls
 *        required for executing/controlling the procedure.
 * @date 09/10/07
 ******************************************************************************/
public class ProcedureView extends ViewPart implements ISaveablePart2
{
	private static IConfigurationManager s_cfg = null;
	private static IContextProxy s_proxy = null;
	/** Holds the view identifier */
	public static final String ID = "com.astra.ses.spell.gui.views.ProcedureView";

	/** Holds the current domain name (sat) for the procedure */
	private String m_domain;
	/** View contents root composite */
	private Composite m_top;
	/** Holds the presentation stack */
	private PresentationStack m_presentationStack;
	/** Holds the presentation control ares */
	private PresentationPanel m_presentationPanel;
	/** Holds the presentation manager */
	private PresentationManager m_presentationManager;
	/** Holds the presentation notifier */
	private PresentationNotifier m_presentationNotifier;
	/** Holds the control area */
	private ControlArea m_controlArea;
	/** Holds the procedure model */
	private IProcedure m_model;
	/** Holds the closeable flag */
	private boolean m_closeable;
	/** Enabled flag */
	private boolean m_enabled;
	/** Close mode */
	private CloseMode m_closeMode;
	/** Splitter composite */
	private SplitPanel m_splitPanel;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	public enum CloseMode
	{
		CLOSE, KILL, DETACH, NONE
	}

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public ProcedureView()
	{
		super();
		m_enabled = true;
		m_model = null;
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		if (s_proxy == null)
		{
			s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
		m_closeable = true;
		m_closeMode = CloseMode.CLOSE;
		Logger.debug("Created", Level.INIT, this);
	}

	/***************************************************************************
	 * Dispose the view. Called when the view part is closed.
	 **************************************************************************/
	public void dispose()
	{
		super.dispose();
		// If the view is closeable, promptToSaveOnClose won't be called
		Logger.debug("Disposed", Level.GUI, this);
	}

	/***************************************************************************
	 * Set view close mode
	 **************************************************************************/
	public void setCloseMode(CloseMode mode)
	{
		m_closeMode = mode;
	}

	/***************************************************************************
	 * Obtain view close mode
	 **************************************************************************/
	public CloseMode getCloseMode()
	{
		// If we have no model return the original mode
		if (m_model != null)
		{
			// If we are not controlling, we shall ensure that the only
			// thing we can do is detach
			ClientMode mode = m_model.getRuntimeInformation().getClientMode();
			if (!mode.equals(ClientMode.CONTROLLING))
			{
				if (m_closeMode != CloseMode.NONE)
				{
					return CloseMode.DETACH;
				}
			}
		}
		return m_closeMode;
	}

	/***************************************************************************
	 * Enable or disable the view
	 **************************************************************************/
	public void setEnabled(boolean enable)
	{
		m_presentationManager.enablePresentations(enable);
	}

	/***************************************************************************
	 * Enable or disable the autoscroll
	 **************************************************************************/
	public void setAutoScroll(boolean enable)
	{
		m_presentationManager.setPresentationsAutoScroll(enable);
	}

	/***************************************************************************
	 * Create the view contents.
	 * 
	 * @param parent
	 *            The view top composite.
	 **************************************************************************/
	public void createPartControl(Composite parent)
	{
		Logger.debug("Creating controls", Level.INIT, this);

		// Set the top composite layout
		m_top = parent; 
		GridLayout layout = new GridLayout();
		// We do not want extra margins
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		// Will place each component below the previous one
		layout.numColumns = 1;
		m_top.setLayout(layout);
		m_top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Obtain the corresponding sat name
		m_domain = s_proxy.getInfo().getSC();
		// Save the procedure id
		String procId = getViewSite().getSecondaryId();
		// Get the model
		m_model = ((IProcedureManager) ServiceManager.get(IProcedureManager.class)).getProcedure(procId);

		// Presentations loaded and controller
		m_presentationManager = new PresentationManager();

		setTitleToolTip(procId);
		Logger.debug("Identification (" + procId + ":" + m_domain + ")", Level.INIT, this);

		// Page control pannel, controls the page switch and provides pres.
		// buttons
		int numAvailablePresentations = m_presentationManager.getNumAvailablePresentations();
		m_presentationPanel = new PresentationPanel(this, m_model, m_top, SWT.NONE, numAvailablePresentations);

		// Splitter panel
		m_splitPanel = new SplitPanel(m_top, 40, 83);
		m_splitPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout section1_layout = new GridLayout();
		section1_layout.numColumns = 1;
		section1_layout.marginTop = 0;
		section1_layout.marginBottom = 0;
		section1_layout.marginLeft = 0;
		section1_layout.marginRight = 0;
		section1_layout.marginHeight = 0;
		section1_layout.marginWidth = 0;
		m_splitPanel.getSection(SplitPanel.Section.PRESENTATION).setLayout(section1_layout);
		GridLayout section2_layout = new GridLayout();
		section2_layout.numColumns = 1;
		section2_layout.marginTop = 0;
		section2_layout.marginBottom = 0;
		section2_layout.marginLeft = 0;
		section2_layout.marginRight = 0;
		section2_layout.marginHeight = 0;
		section2_layout.marginWidth = 0;
		m_splitPanel.getSection(SplitPanel.Section.CONTROL_AREA).setLayout(section2_layout);

		/*
		 * Load and create presentations
		 */

		// Create the stack control for presentations
		m_presentationStack = new PresentationStack(m_model, m_splitPanel.getSection(SplitPanel.Section.PRESENTATION));
		// Presentation notifier
		m_presentationNotifier = new PresentationNotifier(m_model);

		// Check amount of available presentations
		int numLoadedPresentations = 0;
		if (numAvailablePresentations > 0)
		{
			m_presentationManager.loadPresentations();
			numLoadedPresentations = m_presentationManager.getNumLoadedPresentations();
		}

		// Check amount of actually loaded presentations
		if (numLoadedPresentations > 0)
		{
			for (int index = 0; index < numLoadedPresentations; index++)
			{
				IProcedurePresentation pres = m_presentationManager.getPresentation(index);

				// Add the presentation to the presentation panel
				m_presentationPanel.addPresentation(pres.getTitle(), pres.getDescription(), pres.getIcon(), index);

				// Allow the presentation to connect listeners
				pres.subscribeNotifications(m_presentationNotifier);

				// Allow the presentation to create the graphical ui
				m_presentationStack.addPresentation(pres);
			}
			m_presentationPanel.selectPresentation(0);
		}

		// Create the control area
		m_controlArea = new ControlArea(this, m_model, m_splitPanel.getSection(SplitPanel.Section.CONTROL_AREA), m_model.getProcId());
		m_controlArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		m_splitPanel.addSashListener(m_controlArea);

		Logger.debug("Controls created", Level.INIT, this);
		m_controlArea.setFocus();
	}

	/***************************************************************************
	 * Get a presentation by identifier
	 **************************************************************************/
	public IProcedurePresentation getPresentation(String identifier)
	{
		return m_presentationManager.getPresentation(identifier);
	}

	/***************************************************************************
	 * Get associated procedure identifier
	 **************************************************************************/
	public String getProcId()
	{
		return m_model.getProcId();
	}

	/***************************************************************************
	 * Get associated procedure name
	 **************************************************************************/
	public String getProcName()
	{
		return m_model.getProcName();
	}

	/***************************************************************************
	 * Obtain the associated satellite name
	 * 
	 * @return The satellite name
	 **************************************************************************/
	public String getDomain()
	{
		return m_domain;
	}

	/***************************************************************************
	 * Compute split panel sections (size and scroll values)
	 **************************************************************************/
	public void computeSplit( boolean options )
	{
		Point ssSize = m_splitPanel.getSection(SplitPanel.Section.CONTROL_AREA).computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int offset = m_top.getClientArea().height - m_presentationPanel.getClientArea().height - ssSize.y + (options ? 15 : 0);
		if (offset < 200) offset = 200;
		m_splitPanel.setDivision(offset);
	}

	/***************************************************************************
	 * Show the desired page: code, log or display.
	 **************************************************************************/
	public void showPresentation(int index)
	{
		// Show the required presentation in the stack
		m_presentationStack.showPresentation(index);
		// Set the current presentation in the manager
		m_presentationManager.setPresentationSelected(index);
	}

	/***************************************************************************
	 * Cancel prompt input
	 **************************************************************************/
	public boolean cancelPrompt()
	{
		Logger.debug("Prompt cancelled", Level.GUI, this);
		return m_controlArea.cancelPrompt();
	}

	/***************************************************************************
	 * Change font size
	 * 
	 * @param increase
	 *            If true, increase the font size. Otherwise decrease it.
	 **************************************************************************/
	public void zoom(boolean increase)
	{
		m_presentationManager.zoomPresentations(increase);
		m_controlArea.zoom(increase);
		m_splitPanel.layout();
		computeSplit(false);
	}

	/***************************************************************************
	 * Show a procedure line
	 **************************************************************************/
	public void showLine(int lineNo)
	{
		m_presentationManager.showLine(lineNo);
	}

	/***************************************************************************
	 * Unused
	 **************************************************************************/
	public void doSave(IProgressMonitor monitor)
	{
	}

	/***************************************************************************
	 * Unused
	 **************************************************************************/
	public void doSaveAs()
	{
	}

	/***************************************************************************
	 * Makes an asterisk to appear in the title when the procedure is running
	 **************************************************************************/
	public boolean isDirty()
	{
		return (m_enabled && !m_closeable);
	}

	/***************************************************************************
	 * Doesn't make sense
	 **************************************************************************/
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	/***************************************************************************
	 * Force the dirty status
	 **************************************************************************/
	public void setCloseable(boolean closeable)
	{
		m_closeable = closeable;
	}

	/***************************************************************************
	 * Trigger the "save on close" event if the procedure is runnning
	 **************************************************************************/
	public boolean isSaveOnCloseNeeded()
	{
		return (!m_closeable);
	}

	// =========================================================================
	// NON-ACCESSIBLE METHODS
	// =========================================================================

	@Override
	public void setFocus()
	{
		if (m_controlArea != null)
		{
			m_controlArea.setFocus();
		}
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		try
		{
			runtime.setRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION, m_model.getProcId());
		}
		catch (Exception ex)
		{
		}
	}

	/***************************************************************************
	 * Called when the procedure view is about to close and the procedure status
	 * implies that the procedure is not directly closeable
	 **************************************************************************/
	@Override
	public int promptToSaveOnClose()
	{
		Logger.debug("Procedure not directly closeable, asking user", Level.GUI, this);
		Shell shell = Display.getCurrent().getActiveShell();
		CloseProcDialog dialog = new CloseProcDialog(shell, m_model);
		int retcode = dialog.open();
		Logger.debug("User selection " + retcode, Level.GUI, this);
		if (retcode == IDialogConstants.CANCEL_ID)
		{
			Logger.debug("Cancelling closure", Level.GUI, this);
			return ISaveablePart2.CANCEL;
		}
		else if (retcode == CloseProcDialog.DETACH)
		{
			m_closeMode = CloseMode.DETACH;
		}
		else if (retcode == CloseProcDialog.KILL)
		{
			m_closeMode = CloseMode.KILL;
		}
		else if (retcode == CloseProcDialog.CLOSE)
		{
			m_closeMode = CloseMode.CLOSE;
		}
		return ISaveablePart2.NO;
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyDisplay(DisplayData data)
	{
		if (m_presentationPanel.isDisposed())
			return;
		if (data.getExecutionMode() != ExecutionMode.MANUAL)
		{
			m_presentationPanel.displayMessage(data.getMessage(), data.getSeverity());
		}
		m_presentationNotifier.notifyProcedureDisplay(data);
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyError(ErrorData data)
	{
		Logger.debug("Notified error: " + data.getMessage(), Level.GUI, this);
		if (m_presentationPanel.isDisposed())
			return;
		// Cancel any ongoing prompt
		cancelPrompt();
		// Display the error message
		m_presentationPanel.displayMessage(data.getMessage(), Severity.ERROR);
		// Set the status in the control area
		m_controlArea.setProcedureStatus(ExecutorStatus.ERROR, data.isFatal());
		updatePartName(ExecutorStatus.ERROR);
		updateCloseable(ExecutorStatus.ERROR);
		// Report error to presentations
		m_presentationNotifier.notifyProcedureError(data);
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyItem(ItemNotification data)
	{
		// Report to presentations
		m_presentationNotifier.notifyProcedureItem(data);
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyStack(StackNotification data)
	{
		m_presentationPanel.setStage(data.getStageId(), data.getStageTitle());
		// Report to presentations
		m_presentationNotifier.notifyProcedureStack(data);
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelDisabled()
	{
		m_enabled = false;
		m_presentationPanel.setEnabled(false);
		m_controlArea.setEnabled(false);
		// Report to presentations
		m_presentationNotifier.notifyModelDisabled();
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelEnabled()
	{
		m_enabled = true;
		m_presentationPanel.setEnabled(true);
		m_controlArea.setEnabled(true);
		// Report to presentations
		m_presentationNotifier.notifyModelEnabled();
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelLoaded()
	{
		Logger.debug(this + ": Notified LOADED. Assigned view model", Level.PROC, this);
		// Link to the model

		IExecutionInformation info = m_model.getRuntimeInformation();
		ExecutorStatus st = info.getStatus();
		ClientMode cm = info.getClientMode();

		updatePartName(st);
		updateCloseable(st);
		m_controlArea.setClientMode(cm);
		m_presentationPanel.setClientMode(cm);
		m_presentationPanel.setProcedureStatus(st);
		m_presentationPanel.setStage(info.getStageId(), info.getStageTitle());

		// First notification: presentations shall prepare things
		// Report to presentations
		m_presentationNotifier.notifyModelLoaded();

		ExecutorStatus modelStatus = st;
		Logger.debug(this + ": Setting status " + modelStatus, Level.PROC, this);

		// Notify the initial data now.
		if (st.equals(ExecutorStatus.ERROR))
		{
			ErrorData error = info.getError();

			Logger.debug(this + ": Procedure initially in error state: " + error, Level.PROC, this);

			m_controlArea.setProcedureStatus(st, error.isFatal());

			m_presentationPanel.displayMessage(error.getMessage() + ", " + error.getReason(), Severity.ERROR);
			// Report to presentations
			m_presentationNotifier.notifyProcedureError(error);

		}
		else
		{
			m_controlArea.setProcedureStatus(st, false);

			// Notify the initial status now
			StatusNotification statusNotification = new StatusNotification(m_model.getProcId(), modelStatus);
			// Report to presentations
			m_presentationNotifier.notifyProcedureStatus(statusNotification);
			
			if (st.equals(ExecutorStatus.PROMPT))
			{
				notifyPrompt();
			}
		}
		Logger.debug(this + ": Initialization done", Level.PROC, this);
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelReset()
	{
		Logger.debug(this + ": Notified model reset", Level.GUI, this);
		m_presentationPanel.reset();
		// Report to presentations
		m_presentationNotifier.notifyModelReset();
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelUnloaded()
	{
		Logger.debug(this + ": Removed view model", Level.GUI, this);
		// Link to the model
		m_model = null;
		// Report to presentations
		m_presentationNotifier.notifyModelUnloaded();
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelConfigured()
	{
		m_presentationPanel.notifyModelConfigured(m_model);
		updateDependentCommands();
		// Report to presentations
		m_presentationNotifier.notifyModelConfigured();
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyPrompt()
	{
		// Do not process the notification update, a second notification of prompt
		// will be done in monitoring clients
		if (m_model.getController().getPromptData().isNotification()) return;

		Logger.debug("Prompt start notified", Level.GUI, this);
		// Notify the control area so that the input area creates the
		// appropriate controls
		m_controlArea.startPrompt( m_model.getController().getPromptData() );
		// Report to presentations
		m_presentationNotifier.notifyProcedurePrompt(m_model.getController().getPromptData());
		// Then compute the split of the view
		computeSplit( m_model.getController().getPromptData().getOptions() != null );
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyFinishPrompt()
	{
		Logger.debug("Prompt finish notified", Level.GUI, this);
		// Compute the split of the view
		computeSplit(false);
		// Report to presentations
		m_presentationNotifier.notifyProcedureFinishPrompt(m_model.getController().getPromptData());
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyCancelPrompt()
	{
		Logger.debug("Prompt cancel notified", Level.GUI, this);
		m_controlArea.cancelPrompt();
		// Then compute the split of the view
		computeSplit(false);
		// Report to presentations
		m_presentationNotifier.notifyProcedureCancelPrompt(m_model.getController().getPromptData());
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyStatus(StatusNotification data)
	{
		Logger.debug(this + ": Notified status " + data.getStatus(), Level.GUI, this);
		if (m_model == null)
			return;
		updatePartName(data.getStatus());
		updateCloseable(data.getStatus());

		ExecutorStatus st = data.getStatus();
		m_presentationPanel.setProcedureStatus(st);
		m_controlArea.setProcedureStatus(st, false);

		// Report to presentations
		m_presentationNotifier.notifyProcedureStatus(data);
	}

	/***************************************************************************
	 * An event about the user action arrives
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyUserAction(UserActionNotification data)
	{
		UserActionStatus st = data.getUserActionStatus();
		m_controlArea.updateUserAction(st, data.getAction(), data.getSeverity());

		// NOTE: no need to notify presentations about this
	}

	/***************************************************************************
	 * Update part name
	 **************************************************************************/
	private void updatePartName(ExecutorStatus status)
	{
		// Parse the ID. If there are several instances, show the
		// instance number in the part title
		String name = getProcName();
		if (status != ExecutorStatus.UNINIT)
		{
			String eStatus = status.toString().toLowerCase();
			String instanceNum = getProcId().substring(getProcId().indexOf("#") + 1);
			name += "(" + instanceNum + ") - " + eStatus;
		}
		setPartName(name);
	}

	/***************************************************************************
	 * Serialize
	 **************************************************************************/
	public String toString()
	{
		return "[ ProcView " + m_model.getProcId() + "]";
	}

	/***************************************************************************
	 * Update the closeable property
	 **************************************************************************/
	private void updateCloseable(ExecutorStatus status)
	{
		// Set the closeable flag. If closeable is false, it means that
		// the procedure is in such status that it cannot be just unloaded
		// therefore the user must choose wether explicitly abort/kill it or
		// not.
		boolean notifyCloseable = false;
		if (status != ExecutorStatus.LOADED && status != ExecutorStatus.FINISHED && status != ExecutorStatus.ABORTED
		        && status != ExecutorStatus.ERROR)
		{
			if (m_closeable)
				notifyCloseable = true;
			m_closeable = false;
		}
		else
		{
			if (!m_closeable)
				notifyCloseable = true;
			m_closeable = true;
		}
		// Notify changes only
		if (notifyCloseable)
		{
			firePropertyChange(ISaveablePart2.PROP_DIRTY);
		}
	}

	/***************************************************************************
	 * Update dependent command status
	 **************************************************************************/
	public void updateDependentCommands()
	{
		// Update command states for those commands which depend on the model
		// configuration
		IExecutionInformation info = m_model.getRuntimeInformation();
		IStepOverControl so = m_model.getController().getStepOverControl();
		try
		{
			boolean stateValue = so.getMode().equals(StepOverMode.STEP_INTO_ALWAYS);
			CommandHelper.setToggleCommandState(ToggleRunInto.ID, ToggleRunInto.STATE_ID, stateValue);
			stateValue = info.isStepByStep();
			CommandHelper.setToggleCommandState(ToggleByStep.ID, ToggleByStep.STATE_ID, stateValue);
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
	}
}
