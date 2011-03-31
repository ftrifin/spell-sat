///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.printable
// 
// FILE      : CodeViewPrintable.java
//
// DATE      : 2009-11-21 08:55
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
package com.astra.ses.spell.gui.presentation.code.printable;

import java.awt.BasicStroke;
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
import com.astra.ses.spell.gui.print.painter.RectangleTextPainter.HorizontalAlignment;
import com.astra.ses.spell.gui.print.printables.PrintableWithHeader;

public class CodeViewPrintable extends PrintableWithHeader {

	/** Tabbed text to print */
	private String[][] m_tabbedText;
	/** Column width */
	private int[] m_columnLayout;
	/** Page breaks */
	private int[] m_pageBreaks;
	/** Font to be used by this printable */
	private static Font s_font;
	/** Column number which starts showing status info */
	private static final int CODE_STATUS_SEPARATOR = 3;
	/** Column alignment */
	private static final HorizontalAlignment[] COL_ALIGNMENT = 
	   {HorizontalAlignment.RIGHT,
		HorizontalAlignment.RIGHT, 
		HorizontalAlignment.LEFT, 
		HorizontalAlignment.CENTER,
		HorizontalAlignment.CENTER,
		HorizontalAlignment.CENTER};
	
	/*
	 * Font setting
	 */
	static
	{
		s_font = new Font("Courier",Font.PLAIN, 8);
	}
	
	/***************************************************************************
	 * Constructor without defining column widths
	 * It will be assumed that all the columns have equals width
	 * @param table
	 **************************************************************************/
	public CodeViewPrintable(String[][] table)
	{
		this(table, null, null);
	}
	
	/***************************************************************************
	 * Constructor defining column widths
	 * @param table
	 * @param columnsLayout
	 **************************************************************************/
	public CodeViewPrintable(String[][] table, int[] columnsLayout)
	{
		this(table, columnsLayout, null, null);
	}
	
	/***************************************************************************
	 * Constructor without defining column widths, specifying footer and header
	 * printers
	 * It will be assumed that all the columns have equals width
	 * @param table
	 **************************************************************************/
	public CodeViewPrintable(String[][] table,
			IHeaderPrinter header, IFooterPrinter footer)
	{
		super(header, footer);
		m_tabbedText = table;
		// If no column layout is defined, all column will be equal width
		m_columnLayout = new int[m_tabbedText[0].length];
		Arrays.fill(m_columnLayout, 1);
	}
	
	/***************************************************************************
	 * Constructor defining column widths, and header and footer printers
	 * @param table
	 * @param columnsLayout
	 **************************************************************************/
	public CodeViewPrintable(String[][] table, int[] columnsLayout,
			IHeaderPrinter header, IFooterPrinter footer)
	{
		super(header, footer);
		m_tabbedText = table;
		m_columnLayout = columnsLayout;
	}

	@Override
	protected boolean printInsideRectangle(Graphics graphics, int pageIndex) {
		/* Set the font */
		Font original = graphics.getFont();
		graphics.setFont(s_font);
		// line width
		Graphics2D gr = (Graphics2D) graphics;
		BasicStroke stroke = new BasicStroke((float) 0.1);
		gr.setStroke(stroke);
        
        /* Number of pages */
        if (pageIndex > m_pageBreaks.length) {
            return true;
        }
		
		int totalWidth = 0;
		Rectangle area = graphics.getClipBounds();

		for (int column : m_columnLayout)
		{
			totalWidth += column;
		}
		int xMin = 0;
		for (int i = 0; i < m_columnLayout.length; i++)
		{
			int col = m_columnLayout[i];
			double ratio = (double) area.width / (double) totalWidth;
			m_columnLayout[i] = (int) (col * ratio);
			if (i < CODE_STATUS_SEPARATOR)
			{
				xMin += Math.ceil(col * ratio);
			}
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
		RectangleTextPainter textPrinter = new RectangleTextPainter(graphics, 2);
		Point lastPoint = new Point(x,y);
		
		// Draw line by line
        int start = (pageIndex == 0) ? 0 : m_pageBreaks[pageIndex-1];
        int end   = (pageIndex == m_pageBreaks.length)
                         ? m_tabbedText.length : m_pageBreaks[pageIndex];
        for (int lineIndex=start; lineIndex<end; lineIndex++) {
        	x = area.x;
        	int rowTop = y - fontHeight - leading;
        	int rowBottom = y + descent;
			String[] line = m_tabbedText[lineIndex];
			for (int j = 0; j < line.length; j++)
			{
				int width = m_columnLayout[j];
				lastPoint = textPrinter.paintText(line[j], new Rectangle(x,y,width, fontHeight), true, COL_ALIGNMENT[j]);
				graphics.drawLine(x, rowTop, x, rowBottom);
				x = lastPoint.x;
			}	
			y  = lastPoint.y;
			graphics.drawLine(xMin, rowBottom, xMax, rowBottom);
        }
        
		/*Restore the font*/
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
		int lineHeight = s_font.getSize() + 3;
        if (m_pageBreaks == null) {
            int linesPerPage = (int) Math.floor(height/lineHeight);
            int numBreaks = (m_tabbedText.length - 1)/linesPerPage;
            m_pageBreaks = new int[numBreaks];
            for (int b=0; b<numBreaks; b++) {
                m_pageBreaks[b] = (b+1)*linesPerPage; 
            }
        }
        return m_pageBreaks.length  + 1;
	}
}
