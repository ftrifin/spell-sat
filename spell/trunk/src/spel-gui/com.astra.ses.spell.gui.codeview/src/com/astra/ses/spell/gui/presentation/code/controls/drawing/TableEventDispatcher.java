///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : PaintEventDispatcher.java
//
// DATE      : 2008-11-21 08:55
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.presentation.code.controls.drawing;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerColumn;
import com.astra.ses.spell.gui.presentation.code.syntax.ISyntaxFormatter;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

public class TableEventDispatcher implements Listener
{
	private ArrayList<AbstractColumnDrawer>	m_drawers;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public TableEventDispatcher(ISyntaxFormatter formatter,
	        IProcedureDataProvider dataProvider,
	        ITableColorProvider colorProvider)
	{
		m_drawers = new ArrayList<AbstractColumnDrawer>();
		m_drawers.add(new BreakpointColumnDrawer(dataProvider, colorProvider));
		m_drawers.add(new LineNumberColumnDrawer(dataProvider, colorProvider));
		m_drawers.add(new SourceCodeColumnDrawer(formatter, dataProvider,
		        colorProvider));
		m_drawers.add(new ItemColumnDrawer(CodeViewerColumn.NAME.ordinal(),
		        dataProvider, colorProvider));
		m_drawers.add(new ItemColumnDrawer(CodeViewerColumn.VALUE.ordinal(),
		        dataProvider, colorProvider));
		m_drawers.add(new ItemColumnDrawer(CodeViewerColumn.STATUS.ordinal(),
		        dataProvider, colorProvider));
	}

	@Override
	public void handleEvent(Event event)
	{
		TableItem item = (TableItem) event.item;
		int rowIndex = item.getParent().indexOf(item) + 1;
		m_drawers.get(event.index).paintItem(event, item, rowIndex);
	}

}
