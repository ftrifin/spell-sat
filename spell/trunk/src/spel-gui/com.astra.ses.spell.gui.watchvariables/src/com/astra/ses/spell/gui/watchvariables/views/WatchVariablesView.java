////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.views
// 
// FILE      : WatchVariablesView.java
//
// DATE      : Sep 22, 2010 10:22:20 AM
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
package com.astra.ses.spell.gui.watchvariables.views;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.views.ProcedurePageView;
import com.astra.ses.spell.gui.watchvariables.Activator;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.views.controls.WatchVariablesPage;
import com.astra.ses.spell.gui.watchvariables.views.controls.WatchVariablesPage.IWatchVariablesPageListener;

/*******************************************************************************
 * 
 * WatchVariables page allows the user to follow variable value changes
 * 
 ******************************************************************************/
public class WatchVariablesView extends ProcedurePageView implements ISelectionChangedListener, IWatchVariablesPageListener
{
	public static final String ID = "com.astra.ses.spell.gui.views.tools.WatchVariables";

	/** Subscribe action */
	private Action m_subscribeAction;
	/** Unsubscribe action */
	private Action m_unsubscribeAction;
	/** Unsubscribe all action */
	private Action m_unsubscribeAllAction;
	/** Refresh action */
	private Action m_refreshAction;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public WatchVariablesView()
	{
		super("(No variables to watch)", "Variables");
		makeActions();
		resetActions();
	}

	@Override
	protected Page createMyPage(String procedureId, String name)
	{
		IProcedureManager mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		return new WatchVariablesPage(mgr.getProcedure(procedureId), this);
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part)
	{
		PageRec pageRec = super.doCreatePage(part);
		/* Add a selection change listener to the page */
		WatchVariablesPage wv = (WatchVariablesPage) pageRec.page;
		wv.addSelectionChangedListener(this);
		return pageRec;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord)
	{
		WatchVariablesPage wv = (WatchVariablesPage) pageRecord.page;
		/* Remove the listener */
		wv.removeSelectionChangedListener(this);
		wv.cleanup();
		/* Destroy the page */
		super.doDestroyPage(part, pageRecord);
	}

	@Override
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);
		contributeToActionBars();
	}

	@Override
	protected void showPageRec(PageRec pageRec)
	{
		super.showPageRec(pageRec);
		/*
		 * Update actions according to the selection of the page
		 */
		if (pageRec.page.getClass().equals(WatchVariablesPage.class))
		{
			WatchVariablesPage page = (WatchVariablesPage) pageRec.page;
			IStructuredSelection selection = (IStructuredSelection) page.getSelection();
			updateActionState(selection);
		}
		else
		{
			resetActions();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	/***************************************************************************
	 * 
	 * @param manager
	 **************************************************************************/
	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(m_refreshAction);
		manager.add(m_subscribeAction);
		manager.add(m_unsubscribeAction);
		manager.add(m_unsubscribeAllAction);
		manager.add(new Separator());
	}

	/***************************************************************************
	 * Make actions to apply over the different pages on demand
	 **************************************************************************/
	private void makeActions()
	{
		String baseLocation = "platform:/plugin/" + Activator.PLUGIN_ID;

		m_refreshAction = new Action()
		{
			public void run()
			{
				try
				{
					showBusy(true);
					IPage current = getCurrentPage();
					if (current.getClass().equals(WatchVariablesPage.class))
					{
						WatchVariablesPage page = (WatchVariablesPage) current;
						page.updateModel();
					}
				}
				finally
				{
					showBusy(false);
				}
			}
		};
		m_refreshAction.setText("Refresh variables");
		m_refreshAction.setToolTipText("Refresh variables");
		m_refreshAction.setImageDescriptor(Activator.getImageDescriptor(baseLocation + "/icons/16x16/refresh.png"));

		m_subscribeAction = new Action()
		{
			public void run()
			{
				try
				{
					showBusy(true);
					IPage current = getCurrentPage();
					if (current.getClass().equals(WatchVariablesPage.class))
					{
						WatchVariablesPage page = (WatchVariablesPage) current;
						page.subscribeSelected();
					}
				}
				finally
				{
					showBusy(false);
				}
			}
		};
		m_subscribeAction.setText("Watch variables");
		m_subscribeAction.setToolTipText("Watch variables");
		m_subscribeAction.setImageDescriptor(Activator.getImageDescriptor(baseLocation + "/icons/16x16/zoom_in.png"));

		m_unsubscribeAction = new Action()
		{
			public void run()
			{
				try
				{
					showBusy(true);
					IPage current = getCurrentPage();
					if (current.getClass().equals(WatchVariablesPage.class))
					{
						WatchVariablesPage page = (WatchVariablesPage) current;
						page.unsubscribeSelected();
					}
				}
				finally
				{
					showBusy(false);
				}
			}
		};
		m_unsubscribeAction.setText("Stop watching variables");
		m_unsubscribeAction.setToolTipText("Stop watching variables");
		m_unsubscribeAction.setImageDescriptor(Activator.getImageDescriptor(baseLocation + "/icons/16x16/zoom_out.png"));

		m_unsubscribeAllAction = new Action()
		{
			public void run()
			{
				try
				{
					showBusy(true);
					IPage current = getCurrentPage();
					if (current.getClass().equals(WatchVariablesPage.class))
					{
						WatchVariablesPage page = (WatchVariablesPage) current;
						boolean proceed = MessageDialog.openConfirm(getSite().getShell(), "Remove all watches",
						        "Do you really want to remove all variable watches?");
						if (proceed)
						{
							page.unsubscribeAll();
						}
					}
				}
				finally
				{
					showBusy(false);
				}
			}
		};
		m_unsubscribeAllAction.setText("Stop watching all variables");
		m_unsubscribeAllAction.setToolTipText("Stop watching all variables");
		m_unsubscribeAllAction.setImageDescriptor(Activator.getImageDescriptor(baseLocation + "/icons/16x16/cancel.png"));
	}

	/***************************************************************************
	 * Enable the actions at their default status
	 **************************************************************************/
	private void resetActions()
	{
		m_refreshAction.setEnabled(false);
		m_unsubscribeAction.setEnabled(false);
		m_subscribeAction.setEnabled(false);
		m_unsubscribeAllAction.setEnabled(false);
	}

	/***************************************************************************
	 * Update actions status
	 **************************************************************************/
	private void updateActionState(IStructuredSelection selection)
	{
		int selectionSize = selection.size();

		boolean refresh = true;
		boolean subscribe = false;
		boolean unsubscribe = false;
		boolean cleanup = false;

		WatchVariablesPage page = (WatchVariablesPage) getCurrentPage();
		boolean registered = page.isShowingRegistered();
		boolean active = page.isActive();

		refresh = active;

		if (selectionSize > 0)
		{
			subscribe = !registered;
			unsubscribe = true;

			@SuppressWarnings("unchecked")
			Iterator<VariableData> variableIterator = selection.iterator();
			while (variableIterator.hasNext())
			{
				VariableData var = variableIterator.next();
				subscribe = (subscribe && (!var.isRegistered));
				unsubscribe = (unsubscribe && (var.isRegistered));
			}
			cleanup = !subscribe;
		}

		m_refreshAction.setEnabled(refresh);
		m_unsubscribeAction.setEnabled(unsubscribe);
		m_subscribeAction.setEnabled(subscribe);
		m_unsubscribeAllAction.setEnabled(cleanup);
	}

	/*
	 * ==========================================================================
	 * Selection listener methods
	 * =========================================================================
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		updateActionState(selection);
	}

	@Override
	public void notifyActive(IPage page, boolean active)
	{
		IPage current = getCurrentPage();
		if (current == page)
		{
			WatchVariablesPage wv = (WatchVariablesPage) page;
			IStructuredSelection selection = (IStructuredSelection) wv.getSelection();
			updateActionState(selection);
		}
	}
}
