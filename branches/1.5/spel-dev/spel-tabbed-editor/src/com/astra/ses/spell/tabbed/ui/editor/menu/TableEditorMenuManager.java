///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.tabbed.ui.editor.menu
// 
// FILE      : TableEditorMenuManager.java
//
// DATE      : 2009-11-23
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.tabbed.ui.editor.menu;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;

import com.astra.ses.spell.tabbed.ui.table.actions.CopyAction;
import com.astra.ses.spell.tabbed.ui.table.actions.CutAction;
import com.astra.ses.spell.tabbed.ui.table.actions.DeleteRowAction;
import com.astra.ses.spell.tabbed.ui.table.actions.InsertRowAction;
import com.astra.ses.spell.tabbed.ui.table.actions.PasteAction;


/******************************************************************************
 * TabularEditorMenuManager will provide menu with available actions over the
 * table
 * @author jpizar
 *****************************************************************************/
public class TableEditorMenuManager extends MenuManager {
	
	private IAction m_insertRowAction;
	private IAction m_deleteRowAction;
	private IAction m_copyAction;
	private IAction m_pasteAction;
	private IAction m_cutAction;
	
	/**************************************************************************
	 * Constructor
	 * @param table
	 *************************************************************************/
	public TableEditorMenuManager(TableViewer table, TableCursor cursor, Clipboard clipboard)
	{
		super();
		init(table, cursor, clipboard);
	}
	
	/**************************************************************************
	 * Initialize menu with table actions
	 *************************************************************************/
	private void init(TableViewer table, TableCursor cursor, Clipboard clipboard)
	{
		m_insertRowAction = new InsertRowAction(table);
		m_deleteRowAction = new DeleteRowAction(table);
		m_copyAction = new CopyAction(cursor, clipboard);
		m_pasteAction = new CutAction(table, cursor, clipboard);
		m_cutAction = new PasteAction(table, cursor, clipboard);
		
		/*
		 * Fill menu with actions
		 */
	    add(m_insertRowAction);
	    add(m_deleteRowAction);
	    add(new Separator());
	    add(m_copyAction);
	    add(m_pasteAction);
	    add(m_cutAction);
	}
}
