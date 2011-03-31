///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.tabbed.ui.table.actions
// 
// FILE      : DeleteRowAction.java
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
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.tabbed.Activator;
import com.astra.ses.spell.tabbed.ui.editor.TabularEditorInput;


/******************************************************************************
 * Delete row action will remove the row where the mouse is over
 * @author jpizar
 *****************************************************************************/
public class DeleteRowAction extends Action {

	/** Target table */
	private TableViewer m_viewer;
	
	/**************************************************************************
	 * Constructor
	 * @param table
	 *************************************************************************/
	public DeleteRowAction(TableViewer viewer) {
		super("Delete Row");
		m_viewer = viewer;
		setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/table_delete.png"));
	}

	@Override
	public void run() {
		/*
		 * Get selected cell
		 */
		ViewerCell cell = m_viewer.getCell(Display.getCurrent().getCursorLocation());
		TableItem row = (TableItem) cell.getItem();
		if (row != null)
		{
			Table table = m_viewer.getTable();
			int rowPosition = table.indexOf(row);
			TabularEditorInput input = (TabularEditorInput) m_viewer.getInput();
			/*
			 * Delete line
			 */
			input.removeLine(rowPosition);
		}		
	}
	
}
