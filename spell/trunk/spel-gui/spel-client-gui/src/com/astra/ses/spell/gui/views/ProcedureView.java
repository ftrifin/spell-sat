///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : ProcedureView.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views;

import java.util.Vector;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.dialogs.CloseProcDialog;
import com.astra.ses.spell.gui.interfaces.IProcedurePresentation;
import com.astra.ses.spell.gui.model.IConfig;
import com.astra.ses.spell.gui.model.commands.ToggleByStep;
import com.astra.ses.spell.gui.model.commands.ToggleRunInto;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.procs.model.Procedure;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.services.ConfigurationManager;
import com.astra.ses.spell.gui.views.controls.ControlArea;
import com.astra.ses.spell.gui.views.controls.PresentationPanel;
import com.astra.ses.spell.gui.views.controls.SplitPanel;


/*******************************************************************************
 * @brief This view (multiple) shows a procedure code and contains the controls
 *        required for executing/controlling the procedure.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ProcedureView extends ViewPart implements ISaveablePart2
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static ConfigurationManager s_cfg = null;
	private static ContextProxy s_proxy = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Holds the view identifier */
	public static final String ID = "com.astra.ses.spell.gui.views.ProcedureView";

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the current domain name (sat) for the procedure */
	private String m_domain;
	/** View contents root composite */
	private Composite m_top;
	/** Stacked composite for holding the pages */
	private Composite m_stack;
	/** Layout for the stack */
	private StackLayout m_slayout;
	/** Current presentation */
	private int m_currentPresentation = 0;
	/** Top composites for presentations */
	private Vector<Composite> m_presentationPages;
	/** Top composites for presentations */
	private Vector<IProcedurePresentation> m_presentations;
	/** Holds the presentation control ares*/
	private PresentationPanel m_presentationPanel;
	/** Holds the control area */
	private ControlArea m_controlArea;
	/** Holds the procedure id */
	private String m_procId;
	/** Holds the procedure name with instance */
	private String m_procName;
	/** Holds the procedure model */
	private Procedure m_model;
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
		CLOSE,
		KILL,
		DETACH,
		NONE
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
		m_procName = null;
		if (s_cfg == null)
		{
			s_cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		}
		if (s_proxy == null)
		{
			s_proxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);
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
	public void setCloseMode( CloseMode mode )
	{
		m_closeMode = mode;
	}

	/***************************************************************************
	 * Obtain view close mode
	 **************************************************************************/
	public CloseMode getCloseMode()
	{
		// If we have no model return the original mode
		if (getModel() != null)
		{
			// If we are not controlling, we shall ensure that the only
			// thing we can do is detach
			if (getModel().getInfo().getMode() != ClientMode.CONTROLLING )
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
	 * Clear the view
	 **************************************************************************/
	public void clear()
	{
		//TODO clear presentations
	}

	/***************************************************************************
	 * Enable or disable the view
	 **************************************************************************/
	public void setEnabled( boolean enable )
	{
		// TODO enable/disable presentations
	}

	/***************************************************************************
	 * Enable or disable the autoscroll
	 **************************************************************************/
	public void setAutoScroll( boolean enable )
	{
		for(IProcedurePresentation p : m_presentations)
		{
			p.setAutoScroll(enable);
		}
	}

	/***************************************************************************
	 * Create the view contents.
	 * 
	 * @param parent The view top composite.
	 **************************************************************************/
	public void createPartControl(Composite parent)
	{
		Logger.debug("Creating controls", Level.INIT, this);
		
		// Set the top composite layout
		m_top = parent; //new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		// We do not want extra margins
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		// Will place each component below the previous one
		layout.numColumns = 1;
		m_top.setLayout(layout);
		m_top.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

		// Obtain the corresponding sat name
		m_domain = s_proxy.getInfo().getSC();
		// Save the procedure id
		m_procId = getViewSite().getSecondaryId();
		setTitleToolTip(m_procId);
		Logger.debug("Identification (" + m_procId + ":" + m_domain + ")", Level.INIT, this);

		int numPresentations = getNumPresentations();
		
		// Page control pannel
		m_presentationPanel = new PresentationPanel(this, m_top, SWT.NONE, numPresentations);
		m_presentations = new Vector<IProcedurePresentation>();
		m_presentationPages = new Vector<Composite>();
		
		// Splitter panel
		m_splitPanel = new SplitPanel(m_top, true, 50, 81, SplitPanel.Section.SECOND );
		m_splitPanel.setLayoutData( new GridData( GridData.FILL_BOTH ));
		GridLayout section1_layout = new GridLayout();
		section1_layout.numColumns = 1;
		section1_layout.marginTop = 0;
		section1_layout.marginBottom = 0;
		section1_layout.marginLeft = 0;
		section1_layout.marginRight = 0;
		section1_layout.marginHeight = 0;
		section1_layout.marginWidth = 0;
		m_splitPanel.getSection( SplitPanel.Section.FIRST ).setLayout(section1_layout);
		GridLayout section2_layout = new GridLayout();
		section2_layout.numColumns = 1;
		section2_layout.marginTop = 0;
		section2_layout.marginBottom = 0;
		section2_layout.marginLeft = 0;
		section2_layout.marginRight = 0;
		section2_layout.marginHeight = 0;
		section2_layout.marginWidth = 0;
		m_splitPanel.getSection( SplitPanel.Section.SECOND ).setLayout(section2_layout);
		
		// Create the stack control for presentations
		m_stack = new Composite(m_splitPanel.getSection( SplitPanel.Section.FIRST ), SWT.NONE);
		m_stack.setLayoutData( new GridData( GridData.FILL_BOTH ));
		m_slayout = new StackLayout();
		m_slayout.marginHeight = 0;
		m_slayout.marginWidth = 0;
		m_stack.setLayout(m_slayout);

		// Load and create presentations
		loadPresentations();
		if (m_presentations.size()>0)
		{
			m_presentationPanel.selectPresentation(s_cfg.getPresentations().firstElement());
		}

		// Create the control area
		m_controlArea = new ControlArea(this,m_splitPanel.getSection( SplitPanel.Section.SECOND ),m_procId);
		//GridData ca_data = new GridData( SWT.FILL, SWT.END, true, false);
		m_controlArea.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));

		m_splitPanel.computeSection( SplitPanel.Section.BOTH );
		
		m_presentationPanel.layout();
		
		Logger.debug("Controls created", Level.INIT, this);
		m_controlArea.setFocus();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public Procedure getModel()
	{
		return m_model;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getProcId()
	{
		return m_procId;
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
	 * Get the procedure presentation for the given key
	 * @return
	 **************************************************************************/
	public IProcedurePresentation getPresentation(String extensionId)
	{
		for (IProcedurePresentation presentation : m_presentations)
		{
			String extId = presentation.getExtensionId();
			if (extId.equalsIgnoreCase(extensionId))
			{
				return presentation;
			}
		}
		return null;
	}

	/***************************************************************************
	 * Compute split panel sections (size and scroll values)
	 **************************************************************************/
	public void computeSplit()
	{
		m_splitPanel.computeSection( SplitPanel.Section.SECOND );
		Point ssSize = m_splitPanel.getSection( SplitPanel.Section.SECOND ).computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int offset = m_top.getSize().y - m_presentationPanel.getSize().y - ssSize.y - 24;
		if (offset < 300)
		{
			offset = 300;
		}
		m_splitPanel.setDivision(offset);
	}
	
	/***************************************************************************
	 * Show the desired page: code, log or display.
	 **************************************************************************/
	public void showPresentation(int index)
	{
		Logger.debug("Show presentation '" + m_presentations.get(index).getTitle() + "'", Level.GUI, this);
		m_currentPresentation = index;
		m_slayout.topControl = m_presentationPages.get(index);
		// Refresh the stack
		m_stack.layout();
		// Move the focus to the page
		setFocus();
		m_presentations.get(m_currentPresentation).selected();
	}

	/***************************************************************************
	 * Reset prompt input
	 **************************************************************************/
	public void resetPrompt()
	{
		m_model.setWaitingInput(false);
	}

	/***************************************************************************
	 * Cancel prompt input
	 **************************************************************************/
	public boolean cancelPrompt()
	{
		return m_controlArea.cancelPrompt();
	}

	/***************************************************************************
	 * Change font size
	 * 
	 * @param increase
	 * 		If true, increase the font size. Otherwise decrease it.
	 **************************************************************************/
	public void zoom( boolean increase )
	{
		for(IProcedurePresentation p : m_presentations)
		{
			p.zoom(increase);
		}
		m_controlArea.zoom(increase);
	}

	/***************************************************************************
	 * Unused
	 **************************************************************************/
	public void doSave(IProgressMonitor monitor){}

	/***************************************************************************
	 * Unused
	 **************************************************************************/
	public void doSaveAs(){}

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
	public void setCloseable( boolean closeable )
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
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		try
		{
			cfg.setSelection(IConfig.ID_PROCEDURE_SELECTION, m_procId);
		}
		catch(Exception ex) {}
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
		ExecutorStatus st = getModel().getStatus();
		ClientMode mode = getModel().getInfo().getMode();
		String status = st.toString();
		String name = m_model.getProcName();
		boolean onPrompt = m_model.isWaitingInput();
		CloseProcDialog dialog = new CloseProcDialog(shell, name, status, mode, onPrompt);
		int retcode = dialog.open();
		Logger.debug("User selection " + retcode, Level.GUI, this);
		if (retcode==IDialogConstants.CANCEL_ID)
		{
			Logger.debug("Cancelling closure", Level.GUI, this);
			return ISaveablePart2.CANCEL;
		}
		else if (retcode==CloseProcDialog.DETACH)
		{
			m_closeMode = CloseMode.DETACH;
		}
		else if (retcode==CloseProcDialog.KILL)
		{
			if (onPrompt) cancelPrompt();
			m_closeMode = CloseMode.KILL;
		}
		else if (retcode==CloseProcDialog.CLOSE)
		{
			m_closeMode = CloseMode.CLOSE;
		}
		return ISaveablePart2.NO;
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyCode(CodeNotification data)
	{
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyCode(data);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyDisplay(DisplayData data)
	{
		if (data.getExecutionMode() == ExecutionMode.MANUAL )
		{
			m_controlArea.addManualDisplay(data);
		}
		else
		{
			m_presentationPanel.displayMessage(data.getMessage(), data.getSeverity());
			for(IProcedurePresentation p : m_presentations)
			{
				p.notifyDisplay(data);
			}
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyError(ErrorData data)
	{
		if (data.getExecutionMode() == ExecutionMode.MANUAL )
		{
			m_controlArea.addManualError(data);
		}
		else
		{
			m_presentationPanel.displayMessage(data.getMessage(), Severity.ERROR);
			for(IProcedurePresentation p : m_presentations)
			{
				p.notifyError(data);
			}
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyItem(ItemNotification data)
	{
		if (data.getExecutionMode() == ExecutionMode.MANUAL )
		{
			m_controlArea.addManualItem(data);
		}
		else
		{
			for(IProcedurePresentation p : m_presentations)
			{
				p.notifyItem(data);
			}
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyLine(LineNotification data)
	{
		m_presentationPanel.setStage(data.getStageId(), data.getStageTitle());
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyLine(data);
		}
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
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyModelDisabled();
		}
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
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyModelEnabled();
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelLoaded()
	{
		Logger.debug(this + ": Notified LOADED. Assigned view model", Level.GUI, this);
		// Link to the model
		ProcedureManager pmgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
		m_model = pmgr.getProcedure(m_procId);
		updatePartName(m_model.getStatus());
		updateCloseable(m_model.getStatus());
		m_controlArea.setProcedureStatus(m_model.getStatus());
		m_controlArea.setClientMode(m_model.getClientMode());
		m_presentationPanel.setStage(m_model.getStageId(), m_model.getStageTitle());

		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyModelLoaded();
		}
		StatusNotification status = new StatusNotification(m_procId,m_model.getStatus());
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyStatus(status);
		}
		LineNotification line = new LineNotification(m_procId, m_model.getStackPosition());
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyLine(line);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelReset()
	{
		Logger.debug(this + ": Notified model reset", Level.GUI, this);
		m_presentationPanel.reset();
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyModelReset();
		}
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
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyModelConfigured()
	{
		m_controlArea.notifyModelConfigured();
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyModelConfigured();
		}
		updateDependentCommands();
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyPrompt(Input inputData)
	{
		m_controlArea.prompt(inputData);
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyPrompt(inputData);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyCancelPrompt(Input inputData)
	{
		m_controlArea.cancelPrompt();
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyCancelPrompt(inputData);
		}
	}

	/***************************************************************************
	 * 
	 * @param data
	 **************************************************************************/
	public void notifyStatus(StatusNotification data)
	{
		Logger.debug(this + ": Notified status " + data.getStatus(), Level.GUI, this);
		if (m_model==null) return;
		updatePartName(data.getStatus());
		updateCloseable(data.getStatus());
		m_controlArea.setProcedureStatus(data.getStatus());
		for(IProcedurePresentation p : m_presentations)
		{
			p.notifyStatus(data);
		}
	}

	/***************************************************************************
	 * Update part name
	 **************************************************************************/
	private void updatePartName( ExecutorStatus status )
	{
		// Parse the ID. If there are several instances, show the
		// instance number in the part title
		if (m_procName == null)
		{
			String name = m_model.getProcName();
			m_procName = name;
		}
		String name = m_procName;
		if (status != ExecutorStatus.UNINIT)
		{
			String eStatus = status.toString().toLowerCase();
			name += " - " + eStatus;
		}
		Logger.debug("Setting name: " + name, Level.INIT, this);
		setPartName(name);
	}
	
	/***************************************************************************
	 * Serialize
	 **************************************************************************/
	public String toString()
	{
		return "[ ProcView "+ m_procId + "]";
	}

	/***************************************************************************
	 * Obtain the number of defined presentations
	 **************************************************************************/
	private int getNumPresentations()
	{
		return s_cfg.getPresentations().size();
	}

	/***************************************************************************
	 * Load all defined procedure presentations
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	private void loadPresentations()
	{
		Logger.debug( this + ": Loading presentation extensions", Level.GUI, this);
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(IProcedurePresentation.EXTENSION_ID);
		IExtension[] extensions = ep.getExtensions();
		Logger.debug(this + ": Defined extensions: "+ extensions.length, Level.GUI, this);
		//The same collections exists twice because for checking order
		//and the fact that every expected extension has been loaded.
		Vector<String> requiredPresentations = (Vector<String>) s_cfg.getPresentations().clone();
		Vector<String> requiredPresentationsClone = (Vector<String>) requiredPresentations.clone();
		IProcedurePresentation[] orderedPresentations = new IProcedurePresentation[requiredPresentations.size()];
		for(IExtension extension : extensions)
		{
			// Obtain the configuration element for this extension point
			IConfigurationElement cfgElem = extension.getConfigurationElements()[0];
			String elementName  = cfgElem.getAttribute(IProcedurePresentation.ELEMENT_NAME);
			String elementDesc  = cfgElem.getAttribute(IProcedurePresentation.ELEMENT_DESC);
			String elementClass = cfgElem.getAttribute(IProcedurePresentation.ELEMENT_CLASS);
			Logger.debug(this + ": Extension name : " + elementName, Level.GUI, this);
			Logger.debug(this + ": Extension desc : " + elementDesc, Level.GUI, this);
			Logger.debug(this + ": Extension class: " + elementClass, Level.GUI, this);
			try
			{
				IProcedurePresentation presentation = 
					(IProcedurePresentation) 
					IProcedurePresentation.class.cast(
							cfgElem.createExecutableExtension(
									IProcedurePresentation.ELEMENT_CLASS));
				
				int presentationPosition = requiredPresentations.indexOf(presentation.getTitle());
				if (presentationPosition != -1)
				{
					orderedPresentations[presentationPosition] = presentation;
					requiredPresentationsClone.remove(presentation.getTitle());
				}
				Logger.debug(this + ": Extension loaded: " + presentation.getExtensionId(), Level.GUI, this);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		int count = 0;
		//Once we have the presentations in the correct order, we should 
		//check if there are any null object in the array
		Vector<IProcedurePresentation> presentations = new Vector<IProcedurePresentation>();
		for (IProcedurePresentation presentation : orderedPresentations)
		{
			if (presentation != null)
			{
				presentations.add(presentation);
			}
		}
		//Add presentations to the panel
		for(IProcedurePresentation presentation : orderedPresentations)
		{
			m_presentations.add(presentation);
			m_presentationPages.add( presentation.createContents(this,m_stack) );
			m_presentationPanel.addPresentation(presentation.getTitle(), 
										    presentation.getDescription(), 
										    presentation.getIcon(), 
										    count);
			count++;
		}
		//If we expected to load a presentation and it failed, 
		//a warning message is raised
		if (requiredPresentationsClone.size()>0)
		{
			Logger.error("Could not find the following presentation plugins: ", Level.GUI, this);
			for(String pname : requiredPresentationsClone)
			{
				Logger.error("\t- "+ pname,Level.GUI,this);
			}
		}
	}
	
	/***************************************************************************
	 * Update the closeable property
	 **************************************************************************/
	private void updateCloseable( ExecutorStatus status )
	{
		// Set the closeable flag. If closeable is false, it means that
		// the procedure is in such status that it cannot be just unloaded
		// therefore the user must choose wether explicitly abort/kill it or not.
		boolean notifyCloseable = false;
		if (status != ExecutorStatus.LOADED && 
			status != ExecutorStatus.FINISHED &&
			status != ExecutorStatus.ABORTED &&
			status != ExecutorStatus.ERROR)
		{
			if (m_closeable) notifyCloseable = true;
			m_closeable = false;
		}
		else
		{
			if (!m_closeable) notifyCloseable = true;
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
		// Update command states for those commands which depend on the model configuration
		try
		{
			boolean stateValue = m_model.getRunInto();
			CommandHelper.setToggleCommandState(ToggleRunInto.ID, ToggleRunInto.STATE_ID, stateValue);
			stateValue = m_model.getStepByStep();
			CommandHelper.setToggleCommandState(ToggleByStep.ID, ToggleByStep.STATE_ID, stateValue);
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
	}
}
