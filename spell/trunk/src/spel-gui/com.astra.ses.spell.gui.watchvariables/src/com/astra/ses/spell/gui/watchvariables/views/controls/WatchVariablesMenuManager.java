///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.views.controls
// 
// FILE      : WatchVariablesMenuManager.java
//
// DATE      : 2010-08-26
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
package com.astra.ses.spell.gui.watchvariables.views.controls;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.astra.ses.spell.gui.watchvariables.model.WatchVariablesSelection;

/*******************************************************************************
 * 
 * {@link WathcVariablesMenuManager} manages the tables's popup menu
 * 
 ******************************************************************************/
public class WatchVariablesMenuManager
{
	/** Viewer handle */
	private TableViewer	       m_viewer;
	/** Watch page */
	private WatchVariablesPage	m_page;
	/** Menu */
	private Menu	           m_menu;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 **************************************************************************/
	public WatchVariablesMenuManager(TableViewer viewer, WatchVariablesPage page)
	{
		m_viewer = viewer;
		m_page = page;
		m_menu = new Menu(viewer.getTable());
		m_menu.addMenuListener(new MenuListener()
		{

			@Override
			public void menuShown(MenuEvent e)
			{
				fillMenu();
			}

			@Override
			public void menuHidden(MenuEvent e)
			{
			}
		});
		viewer.getTable().setMenu(m_menu);
	}

	/**************************************************************************
	 * Fill the menu with the appropiate actions
	 *************************************************************************/
	private void fillMenu()
	{
		/*
		 * Remove current items from the menu
		 */
		for (MenuItem item : m_menu.getItems())
		{
			item.dispose();
		}

		// First thing, if there are no elements, do not build the menu
		IStructuredContentProvider provider = (IStructuredContentProvider) m_viewer
		        .getContentProvider();
		Object[] allElements = provider.getElements(null);
		if (allElements.length == 0) return;

		// See if there is an element registered in the whole table, no matter
		// if selected
		boolean atLeastOneRegistered = WatchVariablesSelection
		        .anyRegistered(allElements);

		// Now check the selection
		IStructuredSelection sel = (IStructuredSelection) m_viewer
		        .getSelection();
		// Will be false if any of the items selected is not registered
		boolean allSelectedRegistered = WatchVariablesSelection
		        .allRegistered(sel);
		// Will be false if any of the items selected is registered
		boolean noneSelectedRegistered = WatchVariablesSelection
		        .noneRegistered(sel);

		if (sel.size() > 0) // At lease one item selected
		{
			if (allSelectedRegistered)
			{
				MenuItem unsubscribe = new MenuItem(m_menu, SWT.PUSH);
				unsubscribe.setText("Remove watch");
				unsubscribe.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						m_page.unsubscribeSelected();
					}
				});
			}
			else if (noneSelectedRegistered)
			{
				MenuItem unsubscribe = new MenuItem(m_menu, SWT.PUSH);
				unsubscribe.setText("Add watch");
				unsubscribe.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						m_page.subscribeSelected();
					}
				});
			}

			if (atLeastOneRegistered)
			{
				MenuItem unsubscribe = new MenuItem(m_menu, SWT.PUSH);
				unsubscribe.setText("Remove all watches");
				unsubscribe.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						m_page.unsubscribeAll();
					}
				});
			}
		}
	}
}
