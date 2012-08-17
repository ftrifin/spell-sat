///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : SourceCodeColumnDrawer.java
//
// DATE      : 2008-11-21 08:55
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerColumn;
import com.astra.ses.spell.gui.presentation.code.syntax.ISyntaxFormatter;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

public class SourceCodeColumnDrawer extends AbstractColumnDrawer
{
	/** Reference to syntax formatter */
	private ISyntaxFormatter m_formatter;
	/** Holds the text layout to be drawn */
	private static TextLayout s_layout = new TextLayout(Display.getCurrent());

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public SourceCodeColumnDrawer(ISyntaxFormatter formatter, IProcedureDataProvider dataProvider, ITableColorProvider colorProvider)
	{
		super(dataProvider, colorProvider);
		m_formatter = formatter;
	}

	@Override
	public void paintItem(Event event, TableItem item, int rowIndex)
	{
		Color background = getColorProvider().getBackground(item, CodeViewerColumn.CODE.ordinal());
		Color foreground = getColorProvider().getForeground(item, CodeViewerColumn.LINE_NO.ordinal());

		int dataLineIndex = -1;
		try
		{
			dataLineIndex = getDataProvider().getCurrentLine();
		}
		catch (UninitProcedureException e)
		{
		}

		// Selection drawing
		if ((event.detail & SWT.SELECTED) != 0)
		{
			event.detail &= ~SWT.SELECTED;
			background = getSelectionColor(background);
		}

		event.gc.setBackground(background);
		event.gc.setForeground(foreground);

		// Paint basic background
		event.gc.fillRectangle(event.x, event.y, event.width, event.height);

		// Obtain the code text. It is stored this way in order to
		// prevent the normal alg. to paint it
		String text = event.item.getData(CodeViewer.DATA_SOURCE).toString();
		text = text.replace("\t", "    ");
		// Assign the text to the text layout
		s_layout.setText(text);
		
		// And apply the styles to the layout
		m_formatter.applyScheme(s_layout, rowIndex, (rowIndex == dataLineIndex));

		// If there is a search highlight range, apply it
		StyleRange hr = (StyleRange) item.getData(CodeViewer.DATA_SEARCH_RANGE);
		if (hr != null)
		{
			TextStyle original = s_layout.getStyle(hr.start);
			hr.font = original.font;
			hr.borderStyle = SWT.BORDER_DOT;
			s_layout.setStyle(hr, hr.start, hr.start + hr.length);
		}

		int y = event.y + (event.height - event.gc.getFontMetrics().getHeight()) / 2;
		// Then draw it in the table item
		s_layout.draw(event.gc, event.x + 5, y);

		// Paint column border
		event.gc.drawLine(event.x + event.width - 1, event.y, event.x + event.width - 1, event.y + event.height);
	}

}
