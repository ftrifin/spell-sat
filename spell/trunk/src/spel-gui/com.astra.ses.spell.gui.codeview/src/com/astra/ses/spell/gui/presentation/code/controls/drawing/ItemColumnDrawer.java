///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : NameColumnDrawer.java
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

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

public class ItemColumnDrawer extends AbstractColumnDrawer
{
	private int	m_columnIndex;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public ItemColumnDrawer(int columnIndex,
	        IProcedureDataProvider dataProvider,
	        ITableColorProvider colorProvider)
	{
		super(dataProvider, colorProvider);
		m_columnIndex = columnIndex;
	}

	@Override
	public synchronized void paintItem(Event event, TableItem item, int rowIndex)
	{
		boolean disposeBkg = false;

		Color background = getColorProvider()
		        .getBackground(item, m_columnIndex);
		Color foreground = getColorProvider()
		        .getForeground(item, m_columnIndex);

		// Selection drawing
		if ((event.detail & SWT.SELECTED) != 0)
		{
			event.detail &= ~SWT.SELECTED;
			background = item.getParent().getBackground();
			background = getSelectionColor(background);
			disposeBkg = true;
		}

		GC gc = event.gc;
		gc.setForeground(foreground);
		gc.setBackground(background);

		gc.fillRectangle(event.x, event.y, event.width, event.height);

		// TODO: this leaves a thick line between to subsequent items in the
		// table. UGLY!

		// Paint rectangle if there is data, column borders otherwise
		if (item.getText(m_columnIndex).isEmpty())
		{
			// Paint column border
			gc.drawLine(event.x + event.width - 1, event.y, event.x
			        + event.width - 1, event.y + event.height);
		}
		else
		{
			gc.drawRectangle(event.x - 1, event.y, event.width,
			        event.height - 1);
		}

		if (disposeBkg) background.dispose();
	}

}
