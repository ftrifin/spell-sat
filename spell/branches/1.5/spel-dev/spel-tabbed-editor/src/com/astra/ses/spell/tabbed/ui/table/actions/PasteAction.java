///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.tabbed.ui.table.actions
// 
// FILE      : PasteAction.java
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.tabbed.ui.editor.TabularEditorInput;

/******************************************************************************
 * Paste row action will paste the cell content where the mouse is over
 * @author jpizar
 *****************************************************************************/
public class PasteAction extends Action {

	/** Target table */
	private TableViewer m_viewer;
	/** Cursor for getting selected position */
	private TableCursor m_cursor;
	/** clipboard for storing contents */
	private Clipboard m_clipboard;
	
	/**************************************************************************
	 * Constructor
	 * @param table
	 *************************************************************************/
	public PasteAction(TableViewer viewer, TableCursor cursor, Clipboard clipboard) {
		super("Paste");
		m_viewer = viewer;
		m_cursor = cursor;
		m_clipboard = clipboard;
	}

	@Override
	public void run() {
		/* Get selected cell */
		TableItem row = m_cursor.getRow();
		if (row != null)
		{
			int col = m_cursor.getColumn();
			int rowPosition = m_viewer.getTable().indexOf(row);
			Object clipContent = m_clipboard.getContents(TextTransfer.getInstance(), DND.CLIPBOARD);
			if (clipContent == null)
			{
				return;
			}
			String clipText = clipContent.toString();
			TabularEditorInput input = (TabularEditorInput) m_viewer.getInput();
			/* Update contents */
			row.setText(col, clipText);
			row.setData(String.valueOf(col), clipText);
			input.setValue(rowPosition, col, clipText);
			/*
			 * Clear the clipboard
			 */
			m_clipboard.clearContents();
		}
	}
}
