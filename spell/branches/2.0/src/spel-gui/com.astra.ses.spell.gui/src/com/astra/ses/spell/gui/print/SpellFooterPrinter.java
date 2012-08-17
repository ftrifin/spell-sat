///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print
// 
// FILE      : SpellFooterPrinter.java
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

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.astra.ses.spell.gui.print.interfaces.IFooterPrinter;
import com.astra.ses.spell.gui.print.interfaces.IPrintingProgress;

/*******************************************************************************
 * SpellFotterPrinter will print the footer in every page when trying to print
 * something in Spell GUI
 ******************************************************************************/
public class SpellFooterPrinter implements IFooterPrinter
{

	/* INNER MARGIN */
	private static final int	INNER_MARGIN	= 3;
	/** Plain font */
	private static final Font	PLAIN_FONT	 = new Font(Font.SANS_SERIF,
	                                                 Font.PLAIN, 9);

	@Override
	public Graphics printFooter(Graphics graphics, IPrintingProgress progress)
	{
		Rectangle availableArea = graphics.getClipBounds();
		graphics.setFont(PLAIN_FONT);
		FontMetrics metrics = graphics.getFontMetrics();

		// Starting coordinates and width
		int x = availableArea.x + INNER_MARGIN;
		int y = availableArea.y;
		int width = availableArea.width - 2 * INNER_MARGIN;
		int height = availableArea.height;
		// Page number text
		String pageNumber = "Page " + progress.getPageNumber() + " of "
		        + progress.getPageCount();
		// Footer rectangle starts at the bottom
		// This will be the header's size
		Rectangle2D size = metrics.getStringBounds(pageNumber, graphics);
		int headerHeight = (int) size.getHeight();
		int headerY = y + height - metrics.getDescent();
		// Draw Page Number
		graphics.drawString(pageNumber, x, headerY);
		// Draw timestamp
		Date timeStamp = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
		        "EEE MMM dd HH:mm:ss yyyy");
		String time = dateFormat.format(timeStamp);
		int timeLength = (int) metrics.getStringBounds(time, graphics)
		        .getWidth();
		graphics.drawString(time, width - timeLength, headerY);
		// Return the remaining area
		graphics.drawLine(x, y + height - headerHeight, x + width, y + height
		        - headerHeight);
		graphics.clipRect(availableArea.x, y, availableArea.width, height
		        - headerHeight);
		return graphics;
	}

	@Override
	public int getFooterHeight(Graphics graphics)
	{
		FontMetrics metrics = graphics.getFontMetrics(PLAIN_FONT);
		// Area used by the footer is the same as the font size
		Graphics2D g2 = (Graphics2D) graphics;
		int lineWidth = (int) ((BasicStroke) g2.getStroke()).getLineWidth();
		return metrics.getHeight() + lineWidth;
	}
}
