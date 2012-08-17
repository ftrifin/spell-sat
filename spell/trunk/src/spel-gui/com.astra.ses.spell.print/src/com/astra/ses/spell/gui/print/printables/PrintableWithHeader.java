///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.printables
// 
// FILE      : PrintableWithHeader.java
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import com.astra.ses.spell.gui.print.interfaces.IFooterPrinter;
import com.astra.ses.spell.gui.print.interfaces.IHeaderPrinter;
import com.astra.ses.spell.gui.print.interfaces.IPrintingProgress;
import com.astra.ses.spell.gui.print.printables.footer.DummyFooterPrinter;
import com.astra.ses.spell.gui.print.printables.header.DummyHeaderPrinter;

/*******************************************************************************
 * Printable with header will print and IPrintable with header and footer
 ******************************************************************************/
public abstract class PrintableWithHeader implements Printable
{

	/***************************************************************************
	 * PrintingProgress implements IPrintingProgress
	 **************************************************************************/
	private class PrintingProgress implements IPrintingProgress
	{
		/** Current page */
		private int	m_currentPage;
		/** Page count */
		private int	m_pageCount;

		/***********************************************************************
		 * Constructor
		 **********************************************************************/
		public PrintingProgress()
		{
			m_currentPage = 0;
			m_pageCount = -1;
		}

		/***********************************************************************
		 * A new page has been create
		 **********************************************************************/
		public void setPageNumber(int pageNumber)
		{
			m_currentPage = pageNumber;
		}

		@Override
		public int getPageNumber()
		{
			return m_currentPage;
		}

		/***********************************************************************
		 * Set page count
		 * 
		 * @param count
		 **********************************************************************/
		public void setPageCount(int count)
		{
			m_pageCount = count;
		}

		@Override
		public int getPageCount()
		{
			return m_pageCount;
		}
	}

	/** Footer printer */
	private IFooterPrinter	 m_footerPrinter;
	/** Header printer */
	private IHeaderPrinter	 m_headerPrinter;
	/** Printing progress object */
	private PrintingProgress	m_progress;

	/**************************************************************************
	 * Constructor
	 * 
	 * @param headerTitle
	 *************************************************************************/
	public PrintableWithHeader(IHeaderPrinter header, IFooterPrinter footer)
	{
		m_progress = new PrintingProgress();
		m_headerPrinter = header;
		m_footerPrinter = footer;
		if (m_footerPrinter == null)
		{
			m_footerPrinter = new DummyFooterPrinter();
		}
		if (m_headerPrinter == null)
		{
			m_headerPrinter = new DummyHeaderPrinter();
		}
	}

	@Override
	public int print(Graphics graphics, PageFormat p, int pageIndex)
	        throws PrinterException
	{

		if (m_progress.getPageCount() == -1)
		{
			int pageHeight = (int) graphics.getClipBounds().getHeight();
			int headerHeight = m_headerPrinter.getHeaderHeight(graphics);
			int footerHeight = m_footerPrinter.getFooterHeight(graphics);
			int availableHeight = pageHeight - footerHeight - headerHeight;
			int pageCount = getPageCount(graphics, availableHeight);
			m_progress.setPageCount(pageCount);
		}

		/* New page progress */
		m_progress.setPageNumber(pageIndex + 1);

		Graphics2D gr = (Graphics2D) graphics;
		gr.translate(p.getImageableX(), p.getImageableY());
		int lw = (int) ((BasicStroke) gr.getStroke()).getLineWidth();

		/* Draw a rectangle */
		Rectangle bounds = gr.getClipBounds();
		int x = bounds.x;
		int y = bounds.y;
		int width = bounds.width;
		int height = bounds.height;

		/*
		 * Draw rectangle around the page
		 */
		Rectangle clipped = new Rectangle(x + lw, y + lw, width - 2 * lw,
		        height - 2 * lw);
		gr.draw(clipped);
		gr.setClip(clipped);

		/*
		 * Print header
		 */
		m_headerPrinter.printHeader(graphics, m_progress);

		/*
		 * Print footer
		 */
		m_footerPrinter.printFooter(graphics, m_progress);

		/*
		 * Print contents
		 */
		boolean finished = printInsideRectangle(graphics, pageIndex);

		if (finished) { return NO_SUCH_PAGE; }
		return PAGE_EXISTS;
	}

	/**************************************************************************
	 * Print the object, or at least part of it, inside the rectangle
	 * 
	 * @return true if job is finished, false otherwise
	 *************************************************************************/
	protected abstract boolean printInsideRectangle(Graphics graphics,
	        int pageIndex);

	/***************************************************************************
	 * Get page count
	 * 
	 * @return
	 **************************************************************************/
	protected abstract int getPageCount(Graphics g, int height);
}
