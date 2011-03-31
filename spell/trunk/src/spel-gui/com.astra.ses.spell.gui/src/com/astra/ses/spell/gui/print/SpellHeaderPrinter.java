///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print
// 
// FILE      : SpellHeaderPrinter.java
//
// DATE      : 2009-11-05 08:55
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
package com.astra.ses.spell.gui.print;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.print.interfaces.IHeaderPrinter;
import com.astra.ses.spell.gui.print.interfaces.IPrintingProgress;

/*******************************************************************************
 * Spell printing header is responsible for printing the header with the spell
 * logo and some extra information
 ******************************************************************************/
@SuppressWarnings("restriction")
public class SpellHeaderPrinter implements IHeaderPrinter
{

	private class CellLabelValuePainter
	{
		/** Available Area for painting */
		private Rectangle	m_availableArea;
		/** Label */
		private String		m_labelString;
		/** Value */
		private String		m_valueString;

		/***********************************************************************
		 * Constructor
		 * 
		 * @param area
		 * @param label
		 * @param value
		 **********************************************************************/
		public CellLabelValuePainter(Rectangle area, String label, String value)
		{
			m_availableArea = area;
			m_labelString = label;
			m_valueString = value;
		}

		/***********************************************************************
		 * Paint the text inside the available area
		 * 
		 * @param graphics
		 **********************************************************************/
		public void paint(Graphics graphics)
		{
			// Center alignment
			int totalSize = (int) graphics.getFontMetrics()
			        .getStringBounds(m_labelString + m_valueString, graphics)
			        .getWidth();
			int centerPadding = (m_availableArea.width - totalSize) / 2;

			// coordinates
			int currentX = (int) m_availableArea.getX() + centerPadding;
			int textBaseline = (int) (m_availableArea.getY()
			        + m_availableArea.getHeight() - graphics.getFontMetrics()
			        .getDescent());
			graphics.setFont(BOLD_FONT);
			graphics.drawString(m_labelString, currentX, textBaseline);
			double labelSize = graphics.getFontMetrics()
			        .getStringBounds(m_labelString, graphics).getWidth();
			currentX += labelSize;
			graphics.setFont(PLAIN_FONT);
			graphics.drawString(m_valueString, currentX, textBaseline);
		}
	}

	/** Row Widths */
	private static final double[][]	s_rowWidth	 = new double[][] {
	        { 0.5, 0.5 }, { 0.17, 0.16, 0.17, 0.25, 0.25 } };
	/** Image height */
	private static final int	    IMAGE_HEIGHT	= 15;
	/** Title font */
	private static final Font	    TITLE_FONT	 = new Font(
	                                                     Font.SANS_SERIF,
	                                                     Font.PLAIN | Font.BOLD,
	                                                     13);
	/** Plain font */
	private static final Font	    PLAIN_FONT	 = new Font(Font.SANS_SERIF,
	                                                     Font.PLAIN, 9);
	/** Bold font */
	private static final Font	    BOLD_FONT	 = new Font(Font.SANS_SERIF,
	                                                     Font.BOLD, 9);
	/** Image for drawing in the header */
	private static Image	        s_printHeaderImage;
	/** Header Title */
	private String	                m_headerTitle;

	/**************************************************************************
	 * Create the image
	 *************************************************************************/
	static
	{
		try
		{
			// if the bundle is not ready then there is no image
			Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
			if (BundleUtility.isReady(bundle))
			{
				// look for the image (this will check both the plugin and
				// fragment folders
				URL fullPathString = BundleUtility.find(bundle,
				        "icons/printHeader.jpg");
				s_printHeaderImage = ImageIO.read(fullPathString.openStream());
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**************************************************************************
	 * Constructor
	 * 
	 * @param title
	 *************************************************************************/
	public SpellHeaderPrinter(String title)
	{
		m_headerTitle = title;
	}

	@Override
	public Graphics printHeader(Graphics graphics, IPrintingProgress progress)
	{

		// We must retrieve the original font for being able to restore it at
		// the end
		Rectangle availableArea = graphics.getClipBounds();
		FontMetrics metrics = graphics.getFontMetrics(PLAIN_FONT);
		int fontHeight = metrics.getHeight();
		int descent = metrics.getDescent();
		// Starting coordinates and width
		int x = availableArea.x;
		int y = availableArea.y;
		int width = availableArea.width;
		// Current drawing position
		int currentRow = 0;
		int currentCol = 0;
		int currentX = x;
		int currentY = y;
		// First Line contains
		// Title - Icon
		// TITLE
		graphics.setFont(TITLE_FONT);
		int firstLineHeight = Math.max(graphics.getFontMetrics().getHeight()
		        + descent, IMAGE_HEIGHT);

		int cellWidth = (int) (width * s_rowWidth[currentRow][currentCol]);
		Rectangle titleArea = new Rectangle(currentX, currentY, cellWidth,
		        firstLineHeight);
		int titleHeight = graphics.getFontMetrics().getAscent()
		        + graphics.getFontMetrics().getLeading();
		// 3 added to theX coordinate as an inner margin value
		graphics.drawString(m_headerTitle, currentX + 3, currentY + titleHeight);
		currentX += cellWidth;

		// IMAGE
		// First we have to compute the scaled size of the image
		Image img = s_printHeaderImage.getScaledInstance(-1, firstLineHeight
		        - descent, Image.SCALE_SMOOTH);
		graphics.drawImage(img, x + width - img.getWidth(null) - 10, currentY
		        + descent, null);
		// Next Line
		int base = currentY + firstLineHeight;
		graphics.drawLine(x, base, x + width, base);
		// Draw second line with these contents
		// Spacecraft - Family - GCS - Driver - Workstation
		// Context Info
		ContextProxy ctx = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		ContextInfo info = ctx.getInfo();
		// If Context Info is not available, skip the line
		if (info != null)
		{
			graphics.setFont(PLAIN_FONT);
			currentX = x;
			currentY += firstLineHeight;
			currentCol = 0;
			currentRow++;

			// Spacecraft
			String label = "Spacecraft: ";
			String value = info.getSC();
			cellWidth = (int) (width * s_rowWidth[currentRow][currentCol]);
			titleArea = new Rectangle(currentX, currentY, cellWidth, fontHeight);
			CellLabelValuePainter cell = new CellLabelValuePainter(titleArea,
			        label, value);
			cell.paint(graphics);
			currentX += cellWidth;
			graphics.drawLine(currentX, currentY, currentX, currentY
			        + fontHeight);
			currentCol++;

			// Family
			label = "Family: ";
			value = info.getFamily();
			cellWidth = (int) (width * s_rowWidth[currentRow][currentCol]);
			titleArea = new Rectangle(currentX, currentY, cellWidth, fontHeight);
			cell = new CellLabelValuePainter(titleArea, label, value);
			cell.paint(graphics);
			currentX += cellWidth;
			graphics.drawLine(currentX, currentY, currentX, currentY
			        + fontHeight);
			currentCol++;
			// GCS
			label = "GCS: ";
			value = info.getGCS();
			cellWidth = (int) (width * s_rowWidth[currentRow][currentCol]);
			titleArea = new Rectangle(currentX, currentY, cellWidth, fontHeight);
			cell = new CellLabelValuePainter(titleArea, label, value);
			cell.paint(graphics);
			currentX += cellWidth;
			graphics.drawLine(currentX, currentY, currentX, currentY
			        + fontHeight);
			currentCol++;
			// Driver
			label = "Driver: ";
			value = info.getDriver();
			cellWidth = (int) (width * s_rowWidth[currentRow][currentCol]);
			titleArea = new Rectangle(currentX, currentY, cellWidth, fontHeight);
			cell = new CellLabelValuePainter(titleArea, label, value);
			cell.paint(graphics);
			currentX += cellWidth;
			graphics.drawLine(currentX, currentY, currentX, currentY
			        + fontHeight);
			currentCol++;
			// Workstation
			label = "Workstation: ";
			value = System.getenv("HOSTNAME");
			cellWidth = (int) (width * s_rowWidth[currentRow][currentCol]);
			titleArea = new Rectangle(currentX, currentY, cellWidth, fontHeight);
			cell = new CellLabelValuePainter(titleArea, label, value);
			cell.paint(graphics);
			currentX += cellWidth;
			currentCol++;
			// Next Line
			currentX = x;
		}
		currentRow++;
		currentCol = 0;
		// Draw a line at the bottom
		base = currentY + fontHeight;
		graphics.drawLine(x, base, x + width, base);
		// Restore the font
		graphics.clipRect(x, base, width, availableArea.height - base);
		return graphics;
	}

	@Override
	public int getHeaderHeight(Graphics graphics)
	{
		FontMetrics metrics = graphics.getFontMetrics(TITLE_FONT);
		int firstLineFontHeight = metrics.getHeight();
		int descent = metrics.getDescent();
		metrics = graphics.getFontMetrics(PLAIN_FONT);
		int secondLineFontHeight = metrics.getHeight();
		return Math.max(firstLineFontHeight + descent, IMAGE_HEIGHT)
		        + secondLineFontHeight;
	}

}
