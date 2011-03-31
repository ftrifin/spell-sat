///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.tabbed.ui.table.actions
// 
// FILE      : CutAction.java
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
package com.astra.ses.spell.tabbed.ui.table.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.tabbed.ui.editor.TabularEditorInput;

/******************************************************************************
 * Cut row action will cut the cell content where the mouse is over
 * @author jpizar
 *****************************************************************************/
public class CutAction extends Action {

	/** Table viewer */
	private TableViewer m_viewer;
	/** Table cursor */
	private TableCursor m_cursor;
	/** clipboard for storing contents */
	private Clipboard m_clipboard;
	
	/**************************************************************************
	 * Constructor
	 * @param table
	 *************************************************************************/
	public CutAction(TableViewer viewer, TableCursor cursor, Clipboard clipboard) {
		super("Cut");
		m_cursor = cursor;
		m_viewer = viewer;
		m_clipboard = clipboard;
	}

	@Override
	public void run() {
		/*
		 * Get selected cell
		 */
		TableItem row = m_cursor.getRow();
		if (row != null)
		{
			int col = m_cursor.getColumn();
			int rowPosition = m_viewer.getTable().indexOf(row);
			String cellContent = row.getText(col);
			TabularEditorInput input = (TabularEditorInput) m_viewer.getInput();
			m_clipboard.setContents(new Object[]{cellContent}, 
					new Transfer[]{TextTransfer.getInstance()},
					DND.CLIPBOARD);
			row.setText(col, "");
			row.setData(String.valueOf(col), "");
			input.setValue(rowPosition, col, "");	
			firePropertyChange(new PropertyChangeEvent(this, String.valueOf(DND.CLIPBOARD),false,true));
		}
	}
}
