///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.database.db.ui.actions
// 
// FILE      : ChangeDatabaseAction.java
//
// DATE      : 2009-09-14
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.database.db.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;


/******************************************************************************
 * Dummy action for being shown in the navigator popup dialog
 *****************************************************************************/
public class ChangeDatabaseAction extends Action {

	/** Dummy Action ID */
	public static final String DUMMY_ID = "org.python.pydev.navigator.actions.dummyAction";
	/** Selection provider */
	private ISelectionProvider m_selectionProvider;
	
	/**************************************************************************
	 * Constructor 
	 * @param provider
	 * @param text
	 *************************************************************************/
	public ChangeDatabaseAction(ISelectionProvider selectionProvider) {
		super();
		this.setText("Select working database");
		m_selectionProvider = selectionProvider;
	}

	@Override
	public boolean isEnabled()
	{
		/*
		 * Conditions for enabling this action
		 * 1.- Only one element can be selected
		 */
		IStructuredSelection selection = (IStructuredSelection) m_selectionProvider.getSelection();
		return (selection.size() == 1);
	}

	@Override
	public void run()
	{
		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().
		getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getService(IHandlerService.class);
		try {
			handlerService.executeCommand("com.astra.ses.spell.database.database.ui.handlers.changeDatabase", null);
		} catch (Exception ex) {
			throw new RuntimeException("com.astra.ses.spell.database.database.ui.handlers.changeDatabase not found");
		}
		super.run();
	}
}