///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.printables
// 
// FILE      : PlainTextPrintable.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.print.printables;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import com.astra.ses.spell.gui.print.interfaces.IFooterPrinter;
import com.astra.ses.spell.gui.print.interfaces.IHeaderPrinter;
import com.astra.ses.spell.gui.print.painter.RectangleTextPainter;

/*******************************************************************************
 * TextPlainPrintable will print a set of plain text lines
 ******************************************************************************/
public class PlainTextPrintable extends PrintableWithHeader
{

	/** Text Lines */
	private String[]	     m_textLines;
	/** Pages to use for printing */
	int[]	                 m_pageBreaks;	         // array of page break line
													 // positions
	/** Font to be used by this printable */
	private static Font	     s_font;
	/** INNER MARGIN */
	private static final int	INNER_MARGIN	= 3;

	/*
	 * Font setting
	 */
	static
	{
		s_font = new Font("Courier", Font.PLAIN, 8);
	}

	/***************************************************************************
	 * Constructor defining header printer and footer printer
	 * 
	 * @param lines
	 **************************************************************************/
	public PlainTextPrintable(String[] lines, IHeaderPrinter header,
	        IFooterPrinter footer)
	{
		super(header, footer);
		m_textLines = lines;
	}

	@Override
	protected boolean printInsideRectangle(Graphics graphics, int pageIndex)
	{
		/* Set the font */
		Font original = graphics.getFont();
		graphics.setFont(s_font);

		/* Number of pages */
		if (pageIndex > m_pageBreaks.length) { return true; }

		Rectangle area = graphics.getClipBounds();
		FontMetrics metrics = graphics.getFontMetrics();
		int fontHeight = metrics.getHeight();

		/*
		 * First of all, compute the range of lines we are printing
		 */
		int x = area.x;
		int y = area.y + fontHeight;
		int xMax = x + area.width;

		/* Rectangle text painter */
		RectangleTextPainter textPrinter = new RectangleTextPainter(graphics);

		// Draw line by line
		int start = (pageIndex == 0) ? 0 : m_pageBreaks[pageIndex - 1];
		int end = (pageIndex == m_pageBreaks.length) ? m_textLines.length
		        : m_pageBreaks[pageIndex];
		for (int lineIndex = start; lineIndex < end; lineIndex++)
		{
			String line = m_textLines[lineIndex];
			Rectangle lineArea = new Rectangle(x + INNER_MARGIN, y, xMax - x
			        - INNER_MARGIN, -10);
			Point lastPoint = textPrinter.paintText(line, lineArea, false);
			y = lastPoint.y;
		}

		/* Restore the font */
		graphics.setFont(original);
		return false;
	}

	@Override
	protected int getPageCount(Graphics g, int height)
	{
		/*
		 * If total of breaks has not been computed, then do it
		 */
		// This hardcoded value of 3 is the interline space
		// used by the rectangle text painter
		g.setFont(s_font);
		int lineHeight = g.getFontMetrics().getHeight();
		int linesPerPage = (int) Math.ceil(height / lineHeight);
		// We have to obtain the characters that can be drawn in a line
		int width = g.getClipBounds().width - 2 * INNER_MARGIN;
		if (m_pageBreaks == null)
		{
			ArrayList<Integer> pageBreaks = new ArrayList<Integer>();
			int currentLines = 0;
			int i = 0;
			for (String line : m_textLines)
			{
				int lineWidth = (int) Math.floor(g.getFontMetrics()
				        .getStringBounds(line, g).getWidth());
				int lines = lineWidth / width;
				if ((lineWidth % width) != 0) lines++;
				if (currentLines + lines > linesPerPage)
				{
					pageBreaks.add(i);
					currentLines = lines;
				}
				else
				{
					currentLines += lines;
				}
				i++;
			}
			// construct the array
			m_pageBreaks = new int[pageBreaks.size()];
			for (int j = 0; j < pageBreaks.size(); j++)
			{
				m_pageBreaks[j] = pageBreaks.get(j);
			}
		}

		return m_pageBreaks.length + 1;
	}
}
