///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.ses.astra.spell.dev.spelleditor.dnd
// 
// FILE      : SpellEditorDropTargetListener.java
//
// DATE      : 2009-10-06
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
package com.astra.ses.spell.dev.spelleditor.dnd;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.texteditor.ITextEditorDropTargetListener;

import com.astra.ses.spell.dev.database.datatransfer.DatabaseTransferable;
import com.astra.ses.spell.dev.database.datatransfer.OfflineDatabaseTransfer;

public class SpellEditorDropTargetListener implements
		ITextEditorDropTargetListener {

	/** Menu factory */
	private DropletsMenuFactory m_menuFactory;
	
	/***************************************************************************
	 * Constructor
	 * @param viewer
	 **************************************************************************/
	public SpellEditorDropTargetListener(ISourceViewer viewer)
	{
		m_menuFactory = new DropletsMenuFactory(viewer);
	}
	
	@Override
	public Transfer[] getTransfers() {
		return new Transfer[]{OfflineDatabaseTransfer.getInstance()};
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT)
		{
			if ((event.operations & DND.DROP_COPY) != 0)
			{
				event.detail = DND.DROP_COPY;
			}
			else
			{
				event.detail = DND.DROP_MOVE;
			}
		}
	}

	@Override
	public void dragLeave(DropTargetEvent event) {}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
		if (event.detail == DND.DROP_DEFAULT)
		{
			if ((event.operations & DND.DROP_COPY) != 0)
			{
				event.detail = DND.DROP_COPY;
			}
			else
			{
				event.detail = DND.DROP_MOVE;
			}
		}
	}

	@Override
	public void dragOver(DropTargetEvent event) {
		event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
	}

	@Override
	public void drop(DropTargetEvent event) {
		if (OfflineDatabaseTransfer.getInstance().isSupportedType(event.currentDataType))
		{
			DatabaseTransferable db = (DatabaseTransferable) event.data;
			Menu menuToShow = m_menuFactory.createMenu(db.getTM(), db.getTC());
			menuToShow.setVisible(true);
		}
	}

	@Override
	public void dropAccept(DropTargetEvent event) {}
}
