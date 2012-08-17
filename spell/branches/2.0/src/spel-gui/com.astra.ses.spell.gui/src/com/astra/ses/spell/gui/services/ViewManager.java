///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.services
// 
// FILE      : ViewManager.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.services;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.ScopeNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.VariableNotification;
import com.astra.ses.spell.gui.core.model.services.BaseService;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.exceptions.NoSuchViewException;
import com.astra.ses.spell.gui.extensions.ProcedureBridge;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.CloseProcedureJob;
import com.astra.ses.spell.gui.model.jobs.KillProcedureJob;
import com.astra.ses.spell.gui.model.jobs.ReleaseProcedureJob;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.extensionpoints.IProcedureViewExtension;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.views.ProcedureView;
import com.astra.ses.spell.gui.views.TabbedView;

/*******************************************************************************
 * @brief This class mantains a registry of all relevant views of the GUI,
 *        including procedure views, control view and the navigation view.
 * @date 09/10/07
 ******************************************************************************/
public class ViewManager extends BaseService implements
        IProcedureViewExtension, IPartListener2
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String	       ID	= "com.astra.ses.spell.gui.ViewManager";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the list of registered views */
	private Map<String, ViewPart>	   m_viewList;
	/** Holds the list of registered procedure views */
	private Map<String, ProcedureView>	m_procViewList;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ViewManager()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
	}

	@Override
	public void setup()
	{
		Logger.debug("Setting up", Level.INIT, this);
		m_viewList = new TreeMap<String, ViewPart>();
		m_procViewList = new TreeMap<String, ProcedureView>();
		ProcedureBridge.get().addProcedureListener(this);
	}

	@Override
	public void cleanup()
	{
		ProcedureBridge.get().removeProcedureListener(this);
	}

	@Override
	public void subscribe()
	{
	}

	@Override
	public String getServiceId()
	{
		return ID;
	}

	/***************************************************************************
	 * Register a view part
	 * 
	 * @param viewId
	 *            View identifier
	 * @param view
	 *            View reference
	 **************************************************************************/
	public void registerView(String viewId, ViewPart view)
	{
		Logger.debug("Registering view: " + viewId, Level.PROC, this);
		m_viewList.put(viewId, view);
	}

	/***************************************************************************
	 * Check if the given view is visible
	 * 
	 * @param viewId
	 * @return True if visible
	 **************************************************************************/
	public boolean isVisible(String viewId)
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench()
		        .getActiveWorkbenchWindow();
		IViewPart partRef = window.getActivePage().findView(viewId);
		if (partRef == null) return false;
		return window.getActivePage().isPartVisible(partRef);
	}

	/***************************************************************************
	 * Register a procedure view part
	 * 
	 * @param viewId
	 *            View identifier
	 * @param view
	 *            View reference
	 **************************************************************************/
	private void registerProcView(String viewId, ProcedureView view)
	{
		Logger.debug("Registering proc view: " + viewId, Level.PROC, this);
		m_procViewList.put(viewId, view);
	}

	/***************************************************************************
	 * Unregister a view part
	 * 
	 * @param viewId
	 *            View identifier
	 **************************************************************************/
	private void unregisterProcView(String viewId)
	{
		Logger.debug("Unregistering proc view: " + viewId, Level.PROC, this);
		m_procViewList.remove(viewId);

		// Close all tabbed views (AsRun and Log views) associated with
		// this procedure
		IWorkbenchWindow window = PlatformUI.getWorkbench()
		        .getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		for (IViewReference viewReference : page.getViewReferences())
		{
			if (viewReference.getView(false) instanceof TabbedView)
			{
				TabbedView logOrAsRunView = (TabbedView) viewReference
				        .getView(false);
				if (logOrAsRunView.getProcId().equals(viewId))
				{
					page.hideView(logOrAsRunView);
				}
			}
		}
	}

	/***************************************************************************
	 * Obtain a registered view
	 * 
	 * @param viewId
	 *            View identifier
	 * @return The view reference
	 * @throws NoSuchViewException
	 **************************************************************************/
	public ViewPart getView(String viewId) throws NoSuchViewException
	{
		if (!m_viewList.containsKey(viewId)) throw new NoSuchViewException(
		        "Unknown view: " + viewId);
		return m_viewList.get(viewId);
	}

	/***************************************************************************
	 * Obtain a procedure view
	 * 
	 * @param viewId
	 *            View identifier
	 * @return The view reference
	 * @throws NoSuchViewException
	 **************************************************************************/
	public ProcedureView getProcView(String viewId) throws NoSuchViewException
	{
		if (!m_procViewList.containsKey(viewId)) throw new NoSuchViewException(
		        "Unknown view: " + viewId);
		return m_procViewList.get(viewId);
	}

	/***************************************************************************
	 * Open a procedure view
	 * 
	 * @param procId
	 *            The view identifier
	 **************************************************************************/
	private void openProcedureView(IProcedure model)
	{
		String procId = model.getProcId();
		Logger.debug("Open procedure view: " + procId, Level.PROC, this);
		IWorkbenchWindow wbw = PlatformUI.getWorkbench()
		        .getActiveWorkbenchWindow();
		try
		{
			wbw.getActivePage().showView(ProcedureView.ID, procId,
			        IWorkbenchPage.VIEW_ACTIVATE);
		}
		catch (PartInitException e)
		{
			Logger.error(
			        "Could not open procedure view " + procId + ": "
			                + e.getLocalizedMessage(), Level.PROC, this);
		}
	}

	/***************************************************************************
	 * Close a procedure view
	 * 
	 * @param procId
	 *            The view identifier
	 **************************************************************************/
	private void closeProcedureView(String procId)
	{
		Logger.debug("Close procedure view: " + procId, Level.PROC, this);
		IWorkbenchWindow wbw = PlatformUI.getWorkbench()
		        .getActiveWorkbenchWindow();
		IWorkbenchPage page = wbw.getActivePage();
		if (page != null)
		{
			IViewReference ref = page.findViewReference(ProcedureView.ID,
			        procId);
			if (ref != null)
			{
				getProcView(procId).setCloseable(true);
				wbw.getActivePage().hideView(ref);
				unregisterProcView(procId);
			}
		}
	}

	@Override
	public void notifyProcedureModelDisabled(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyModelDisabled();
	}

	@Override
	public void notifyProcedureModelEnabled(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyModelEnabled();
	}

	@Override
	public void notifyProcedureModelLoaded(IProcedure model)
	{
		openProcedureView(model);
	}

	@Override
	public void notifyProcedureModelReset(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyModelReset();
	}

	@Override
	public void notifyProcedureModelUnloaded(IProcedure model,
	        boolean doneLocally)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
		{
			if (!doneLocally)
			{
				Shell shell = m_procViewList.get(instanceId).getSite()
				        .getShell();
				MessageDialog
				        .openWarning(shell, "Procedure closed", "Procedure '"
				                + instanceId
				                + "' has been closed by the controlling client");
			}
			m_procViewList.get(instanceId).setCloseMode(
			        ProcedureView.CloseMode.NONE);
			closeProcedureView(instanceId);
		}
	}

	@Override
	public void notifyProcedureModelConfigured(IProcedure model)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
		{
			m_procViewList.get(instanceId).notifyModelConfigured();
		}
	}

	@Override
	public void notifyProcedureDisplay(IProcedure model, DisplayData data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyDisplay(data);
	}

	@Override
	public void notifyProcedureError(IProcedure model, ErrorData data)
	{
		if (m_procViewList.containsKey(data.getOrigin())) m_procViewList.get(
		        data.getOrigin()).notifyError(data);
	}

	@Override
	public void notifyProcedureItem(IProcedure model, ItemNotification data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyItem(data);
	}

	@Override
	public void notifyProcedureStack(IProcedure model, StackNotification data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyStack(data);
	}

	@Override
	public void notifyProcedureStatus(IProcedure model, StatusNotification data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
		{
			m_procViewList.get(instanceId).notifyStatus(data);
		}

		// Check wether the status is from a child of a shown proc
		// If the status is a finished status, show the parent
		ExecutorStatus st = data.getStatus();
		switch (st)
		{
		case FINISHED:
		case ERROR:
		case ABORTED:
			ProcedureManager mgr = (ProcedureManager) ServiceManager
			        .get(ProcedureManager.ID);
			// If the (child) procedure is visible, that is, locally loaded
			if (mgr.isLocallyLoaded(instanceId))
			{
				IProcedure proc = mgr.getProcedure(instanceId);
				try
				{
					// If there is parent proc, go back to it and show it
					if (m_procViewList.containsKey(proc.getParent()))
					{
						IProcedure parentProc = mgr.getProcedure(proc
						        .getParent());
						openProcedureView(parentProc);
					}
				}
				catch (NoSuchProcedure ex)
				{
				}
				;
			}
		}
	}

	@Override
	public String getListenerId()
	{
		return ID;
	}

	@Override
	public void notifyProcedurePrompt(IProcedure model, Input inputData)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyPrompt(inputData);
	}

	@Override
	public void notifyProcedureFinishPrompt(IProcedure model, Input inputData)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyFinishPrompt(inputData);
	}

	@Override
	public void notifyProcedureCancelPrompt(IProcedure model, Input inputData)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId)) m_procViewList.get(
		        instanceId).notifyCancelPrompt(inputData);
	}

	@Override
	public void notifyProcedureUserAction(IProcedure model,
	        UserActionNotification data)
	{
		String instanceId = model.getProcId();
		if (m_procViewList.containsKey(instanceId))
		{
			m_procViewList.get(instanceId).notifyUserAction(data);
		}
	}

	@Override
	public void notifyProcedureVariableScopeChange(IProcedure model,
	        ScopeNotification data)
	{
		// To be coded if presentations require this info
	}

	@Override
	public void notifyProcedureVariableChange(IProcedure model,
	        VariableNotification data)
	{
		// To be coded if presentations require this info
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef)
	{
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof ProcedureView)
		{
			ProcedureView view = (ProcedureView) part;
			view.updateDependentCommands();
		}
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef)
	{
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof ProcedureView)
		{
			ProcedureView view = (ProcedureView) part;
			ProcedureView.CloseMode mode = view.getCloseMode();
			CommandResult result = CommandResult.FAILED;
			if (mode == ProcedureView.CloseMode.DETACH)
			{
				ReleaseProcedureJob job = new ReleaseProcedureJob(
				        view.getProcId());
				CommandHelper.executeInProgress(job, false, false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(part.getSite().getShell(),
					        "Detach error", job.message);
				}
				result = job.result;
			}
			else if (mode == ProcedureView.CloseMode.KILL)
			{
				KillProcedureJob job = new KillProcedureJob(view.getProcId());
				CommandHelper.executeInProgress(job, false, false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(part.getSite().getShell(),
					        "Kill error", job.message);
				}
				result = job.result;
			}
			else if (mode == ProcedureView.CloseMode.CLOSE)
			{
				CloseProcedureJob job = new CloseProcedureJob(view.getProcId());
				CommandHelper.executeInProgress(job, false, false);
				if (job.result != CommandResult.SUCCESS)
				{
					MessageDialog.openError(part.getSite().getShell(),
					        "Close error", job.message);
				}
				result = job.result;
			}
			if (result == CommandResult.SUCCESS)
			{
				unregisterProcView(view.getProcId());
			}
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef)
	{
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof ProcedureView)
		{
			ProcedureView view = (ProcedureView) part;
			Logger.debug("View " + view + " part open", Level.GUI, this);
			registerProcView(view.getProcId(), view);
			m_procViewList.get(view.getProcId()).notifyModelLoaded();
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef)
	{
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef)
	{
	}

}
