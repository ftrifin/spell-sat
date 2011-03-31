///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : StackView.java
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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.model.stack.StackModel;
import com.astra.ses.spell.gui.model.stack.StackViewContentProvider;
import com.astra.ses.spell.gui.model.stack.StackViewLabelProvider;
import com.astra.ses.spell.gui.services.ViewManager;


/*******************************************************************************
 * @brief This view shows the current procedure call stack.
 * @date 28/04/08
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class StackView extends ViewPart 
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static final String ID = "com.astra.ses.spell.gui.views.StackView";

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private TreeViewer m_viewer;
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
		// Create the viewer control
		m_viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		// Set the providers
		m_viewer.setContentProvider(new StackViewContentProvider());
		m_viewer.setLabelProvider(new StackViewLabelProvider());
		// Set the procedure manager as the model provider
		StackModel model = new StackModel(this);
		
		m_viewer.setInput(model.getModelData());
		// Register the navigation view in the selection service
		getSite().setSelectionProvider(m_viewer);
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
		super.dispose();
		Logger.debug("Disposed", Level.GUI, this);
	}

	/***********************************************************************
	 * Receive the input focus.
	 **********************************************************************/
	public void setFocus()
	{
		m_viewer.getControl().setFocus();
	}
	
	/***********************************************************************
	 * Refresh the tree
	 **********************************************************************/
	public void refresh()
	{
		m_viewer.refresh();
	}
}