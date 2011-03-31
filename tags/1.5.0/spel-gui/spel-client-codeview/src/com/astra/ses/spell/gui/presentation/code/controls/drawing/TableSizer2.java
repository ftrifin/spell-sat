///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : TableSizer2.java
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
package com.astra.ses.spell.gui.presentation.code.controls.drawing;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/*******************************************************************************
 * @brief Manages and adjusts table columns on resize events
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class TableSizer2 extends ControlAdapter
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final String WIDTH = "width";
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private Composite m_container;
	private Table m_table;
	private boolean m_resizingTable;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	
	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public TableSizer2(Composite container, Table table, int adjustableColumn, double[] widths )
	{
		m_container = container;
		m_table = table;
		m_resizingTable = false;
		initializeColumns(widths);
	}

	/***************************************************************************
	 * Resize callback
	 **************************************************************************/
	public void controlResized(ControlEvent event)
	{
		m_resizingTable = true;
		Rectangle area = m_container.getClientArea();
		if (area.width==0) return;

		int width = area.width - 2 * m_table.getBorderWidth();
		if (m_table.getVerticalBar().isVisible())
		{
			width -= m_table.getVerticalBar().getSize().x;
		}
		resize(width);
		m_resizingTable = false;
	}
	
	/****************************************************************************
	 * Set columns weight
	 ***************************************************************************/
	private void initializeColumns(double[] widths)
	{
		for( int cidx = 0; cidx<m_table.getColumnCount(); cidx++)
		{
			TableColumn column = m_table.getColumn(cidx);
			column.setData(WIDTH, widths[cidx]);
			column.addControlListener(new ControlAdapter()
			{
				public void controlResized(ControlEvent event)
				{
					if (m_resizingTable)
					{
						return;
					}
					Rectangle area = m_container.getClientArea();
					if (area.width==0) return;

					int width = area.width - 2 * m_table.getBorderWidth();
					if (m_table.getVerticalBar().isVisible())
					{
						width -= m_table.getVerticalBar().getSize().x;
					}
					TableColumn col = (TableColumn) event.widget;
					int colWidth = col.getWidth();
					double ratio = (double) colWidth / (double) width;
					col.setData(WIDTH, ratio);
				}
			});
		}
	}
	
	/***************************************************************************
	 * Initial resize
	 **************************************************************************/
	private void resize(int width)
	{
		for (TableColumn col : m_table.getColumns())
		{
			double ratio = (Double) col.getData(WIDTH);
			int cw = (int) (width * ratio);
			col.setWidth(cw);
		}
	}
}
