///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : NavigationView.java
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
package com.astra.ses.spell.gui.views;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.model.IConfig;
import com.astra.ses.spell.gui.model.commands.OpenProcedure;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.nav.ProcedureListContentProvider;
import com.astra.ses.spell.gui.model.nav.ProcedureListLabelProvider;
import com.astra.ses.spell.gui.model.nav.ProceduresStructureManager;
import com.astra.ses.spell.gui.model.nav.content.NodeSorter;
import com.astra.ses.spell.gui.model.nav.content.ProcedureNode;
import com.astra.ses.spell.gui.services.ConfigurationManager;
import com.astra.ses.spell.gui.services.ViewManager;


/*******************************************************************************
 * @brief This view allows go over the list of available procedures and to
 *        select one of them to be opened.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class NavigationView extends ViewPart implements IOpenListener, ISelectionChangedListener
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String ID = "com.astra.ses.spell.gui.views.NavigationView";

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private TreeViewer m_procTree;
	/** Procedures structure manager */
	private ProceduresStructureManager m_proceduresManager;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================
	
	/***********************************************************************
	 * Create the view contents.
	 * 
	 * @param parent The top composite of the view
	 **********************************************************************/
	public void createPartControl(Composite parent)
	{
		Logger.debug("Created", Level.INIT, this);

		FillLayout layout = (FillLayout) parent.getLayout();
		layout.spacing = 5;
		layout.type = SWT.VERTICAL;
		
		m_proceduresManager = new ProceduresStructureManager();
		m_proceduresManager.setView(this);
		m_procTree = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		// Set the providers
		m_procTree.setContentProvider(new ProcedureListContentProvider());
		m_procTree.setLabelProvider(new ProcedureListLabelProvider());
		m_procTree.setSorter( new NodeSorter() );
		// Set the procedure manager as the model provider
		m_procTree.setInput(m_proceduresManager.getRootElements());
		// Register the navigation view in the selection service
		getSite().setSelectionProvider(m_procTree);
		// Register this view as open listener as well
		m_procTree.addOpenListener(this);
		m_procTree.addSelectionChangedListener(this);
		// Register the view as a service listener for the procedure manager
		// in order to receive updates when the list of available procedures
		// may have changed.
		ViewManager vmgr = (ViewManager) ServiceManager.get(ViewManager.ID);
		vmgr.registerView(ID, this);
	}

	/***********************************************************************
	 * Destroy the view.
	 **********************************************************************/
	public void dispose()
	{
		m_proceduresManager.setView(null);
		super.dispose();
		Logger.debug("Disposed", Level.PROC, this);
	}

	/***********************************************************************
	 * Receive the input focus.
	 **********************************************************************/
	public void setFocus()
	{
		m_procTree.getControl().setFocus();
	}

	/***********************************************************************
	 * Refresh the view
	 **********************************************************************/
	public void refresh()
	{
		m_procTree.setInput(m_proceduresManager.getRootElements());
		m_procTree.refresh();
	}

	/***********************************************************************
	 * Obtain the model
	 **********************************************************************/
	public ProceduresStructureManager getModel()
	{
		return m_proceduresManager;
	}

	/***********************************************************************
	 * Open event on double click
	 **********************************************************************/
	@Override
	public void open(OpenEvent event)
	{
		TreeSelection sel = (TreeSelection) event.getSelection();
		try
		{
			ProcedureNode item = (ProcedureNode) sel.getFirstElement();
			// Open procedures only, ignore categories
			String category = item.getProcID(); 
			if (category == null 
					|| category.equals("")
					|| category.equals("CATEGORY"))
			{
				return;
			}
			CommandHelper.execute(OpenProcedure.ID);
		}
		catch (Exception e)
		{
			return;
		}
	}

	/***********************************************************************
	 * Selection changed on the tree
	 **********************************************************************/
	@Override
	public void selectionChanged(SelectionChangedEvent event) 
	{
		TreeSelection sel = (TreeSelection) event.getSelection();
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		try
		{
			ProcedureNode item = (ProcedureNode) sel.getFirstElement();
			cfg.setSelection(IConfig.ID_NAVIGATION_VIEW_SELECTION, item.getProcID());
		}
		catch(Exception ex)
		{
			cfg.setSelection(IConfig.ID_NAVIGATION_VIEW_SELECTION, null);
		}
	}
}