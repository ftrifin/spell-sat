///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.printables
// 
// FILE      : TabbedTextPrintable.java
//
// DATE      : 2008-11-21 13:54
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.print.printables;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

import com.astra.ses.spell.gui.print.interfaces.IFooterPrinter;
import com.astra.ses.spell.gui.print.interfaces.IHeaderPrinter;
import com.astra.ses.spell.gui.print.painter.RectangleTextPainter;

public class TabbedTextPrintable extends PrintableWithHeader
{

	/** Tabbed text to print */
	private String[][]	m_tabbedText;
	/** Column width */
	private int[]	    m_columnLayout;
	/** Header names */
	private String[]	m_header;
	/** Page breaks */
	private int[]	    m_pageBreaks;
	/** Font to be used by this printable */
	private static Font	s_font;
	/** Horizontal grid */
	private boolean	    m_horizontalGrid;
	/** vertical grid */
	private boolean	    m_verticalGrid;

	/*
	 * Font setting
	 */
	static
	{
		s_font = new Font("Courier", Font.PLAIN, 8);
	}

	/***************************************************************************
	 * Constructor without defining column widths It will be assumed that all
	 * the columns have equals width
	 * 
	 * @param table
	 **************************************************************************/
	public TabbedTextPrintable(String[][] table)
	{
		this(table, null, null);
	}

	/***************************************************************************
	 * Constructor defining column widths
	 * 
	 * @param table
	 * @param columnsLayout
	 **************************************************************************/
	public TabbedTextPrintable(String[][] table, int[] columnsLayout)
	{
		this(table, null, columnsLayout, null, null);
	}

	/***************************************************************************
	 * Constructor without defining column widths, specifying footer and header
	 * printers It will be assumed that all the columns have equals width
	 * 
	 * @param table
	 **************************************************************************/
	public TabbedTextPrintable(String[][] table, IHeaderPrinter header,
	        IFooterPrinter footer)
	{
		super(header, footer);
		m_tabbedText = table;
		// If no column layout is defined, all column will be equal width
		m_columnLayout = new int[m_tabbedText[0].length];
		Arrays.fill(m_columnLayout, 1);
		// Enable grid
		m_horizontalGrid = true;
		m_verticalGrid = true;
	}

	/***************************************************************************
	 * Constructor defining column widths, and header and footer printers
	 * 
	 * @param table
	 * @param columnsLayout
	 **************************************************************************/
	public TabbedTextPrintable(String[][] table, String[] tableHeader,
	        int[] columnsLayout, IHeaderPrinter header, IFooterPrinter footer)
	{
		super(header, footer);
		m_tabbedText = table;
		m_header = tableHeader;
		m_columnLayout = columnsLayout;
		// Enable grid
		m_horizontalGrid = true;
		m_verticalGrid = true;
	}

	/***************************************************************************
	 * Set if grid lines hosuld be painted
	 * 
	 * @param horizontalGrid
	 * @param verticalGrid
	 **************************************************************************/
	public void setGridEnabled(boolean horizontalGrid, boolean verticalGrid)
	{
		m_horizontalGrid = horizontalGrid;
		m_verticalGrid = verticalGrid;
	}

	@Override
	protected boolean printInsideRectangle(Graphics graphics, int pageIndex)
	{
		/* Set the font */
		Font original = graphics.getFont();
		graphics.setFont(s_font);
		// line width
		Graphics2D gr = (Graphics2D) graphics;
		BasicStroke stroke = new BasicStroke(0.0f);
		gr.setStroke(stroke);

		/* Number of pages */
		if (pageIndex > m_pageBreaks.length) { return true; }

		int totalWidth = 0;
		Rectangle area = graphics.getClipBounds();

		for (int column : m_columnLayout)
		{
			totalWidth += column;
		}
		for (int i = 0; i < m_columnLayout.length; i++)
		{
			int col = m_columnLayout[i];
			double ratio = (double) area.width / (double) totalWidth;
			m_columnLayout[i] = (int) (col * ratio);
		}
		/*
		 * Get font metrics
		 */
		FontMetrics metrics = graphics.getFontMetrics();
		int fontHeight = metrics.getHeight();
		int descent = metrics.getDescent();
		int leading = metrics.getLeading();

		/*
		 * First of all, compute the range of lines we are printing
		 */
		int x = area.x;
		int y = area.y + fontHeight - descent;
		int xMax = x + area.width;
		RectangleTextPainter textPrinter = new RectangleTextPainter(graphics, 3);
		Point lastPoint = new Point(x, y);

		/*
		 * If header has been set, then draw the header
		 */
		int rowTop = y - fontHeight - leading;
		int rowBottom = y + descent;
		if (m_header != null)
		{
			// Draw a grey rectangle for the background
			Color prev = graphics.getColor();
			graphics.setColor(new Color(220, 220, 220));
			graphics.fillRect(x, area.y, area.width, fontHeight);
			graphics.setColor(prev);
			for (int i = 0; i < m_header.length; i++)
			{
				int width = m_columnLayout[i];
				lastPoint = textPrinter.paintText(m_header[i], new Rectangle(x,
				        y, width, fontHeight), true);
				if (m_verticalGrid)
				{
					graphics.drawLine(x, rowTop, x, rowBottom);
				}
				x = lastPoint.x;
			}
			graphics.drawLine(area.x, rowBottom, xMax, rowBottom);
			y = lastPoint.y;
		}

		/*
		 * Draw line by line
		 */
		int start = (pageIndex == 0) ? 0 : m_pageBreaks[pageIndex - 1];
		int end = (pageIndex == m_pageBreaks.length) ? m_tabbedText.length
		        : m_pageBreaks[pageIndex];
		for (int lineIndex = start; lineIndex < end; lineIndex++)
		{
			x = area.x;
			rowTop = y - fontHeight - leading;
			rowBottom = y + descent;
			String[] line = m_tabbedText[lineIndex];
			for (int j = 0; j < line.length; j++)
			{
				int width = m_columnLayout[j];
				lastPoint = textPrinter.paintText(line[j], new Rectangle(x, y,
				        width, fontHeight), true);
				if (m_verticalGrid)
				{
					graphics.drawLine(x, rowTop, x, rowBottom);
				}
				x = lastPoint.x;
			}
			y = lastPoint.y;
			// Line at the bottom
			if (m_horizontalGrid)
			{
				graphics.drawLine(area.x, rowBottom, xMax, rowBottom);
			}
		}

		/* Restore the font */
		graphics.setFont(original);

		// Result
		return false;
	}

	@Override
	protected int getPageCount(Graphics g, int height)
	{
		/*
		 * If total of breaks has not been computed, the do it
		 */
		FontMetrics metrics = g.getFontMetrics(s_font);
		int lineHeight = metrics.getHeight();

		if (m_pageBreaks == null)
		{
			double lpp = Double.valueOf(height / lineHeight);
			int linesPerPage = (int) Math.floor(lpp) - 1;
			// 1 is substracted to lines per page in order to add the header
			// on each page printed
			int numBreaks = (m_tabbedText.length - 1) / (linesPerPage);
			m_pageBreaks = new int[numBreaks];
			for (int b = 0; b < numBreaks; b++)
			{
				m_pageBreaks[b] = (b + 1) * linesPerPage;
			}
		}
		return m_pageBreaks.length + 1;
	}
}
