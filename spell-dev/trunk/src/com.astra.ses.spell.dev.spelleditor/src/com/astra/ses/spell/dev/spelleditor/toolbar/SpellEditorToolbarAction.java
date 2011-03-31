///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.toolbar
// 
// FILE      : SpellEditorToolbarAction.java
//
// DATE      : 2009-11-23
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
package com.astra.ses.spell.dev.spelleditor.toolbar;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorPart;

import com.astra.ses.spell.dev.spelleditor.SpellEditor;

public abstract class SpellEditorToolbarAction extends Action {

	/** Menu manager */
	private MenuManager m_menuManager;
	/** toolbar action */
	private IAction m_toolbarAction;
	/** Current editor */
	private IEditorPart m_editor;
	
	/****************************************************************************
	 * Constructor
	 ***************************************************************************/
	public SpellEditorToolbarAction(String text) {
		m_menuManager = new MenuManager(text);
		m_menuManager.add(new Action ("dummyAction") {});
		m_menuManager.setRemoveAllWhenShown(true);
		m_menuManager.addMenuListener(new IMenuListener () {
			public void menuAboutToShow(IMenuManager manager) {
				
			}			
		});
		m_toolbarAction = createToolbarAction();
	}
	
	/****************************************************************************
	 * Get toolbar action
	 * @return
	 ***************************************************************************/
	public IAction getToolbarAction () {
		return m_toolbarAction;
	}

	/****************************************************************************
	 * Set enabled
	 ***************************************************************************/
	public void setEnabled (boolean enabled) {
		m_toolbarAction.setEnabled(enabled);
		m_menuManager.setVisible(enabled);
	}
	
	/****************************************************************************
	 * Active editor has changed
	 * @param activePart
	 ***************************************************************************/
	public void setActiveEditor(IEditorPart activePart)
	{
		m_editor = activePart instanceof SpellEditor ? activePart : null;
	}
	
	/****************************************************************************
	 * Returns the active editor
	 * @return
	 ***************************************************************************/
	protected IEditorPart getActiveEditor()
	{
		return m_editor;
	}
	
	/****************************************************************************
	 * Fill the menu with actions
	 ***************************************************************************/
	public abstract void prepareMenu(IMenuManager mgr);
	
	/****************************************************************************
	 * Create toolbar action
	 * @return
	 ***************************************************************************/
	public abstract IAction createToolbarAction();
}
