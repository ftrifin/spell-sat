///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.painter
// 
// FILE      : RectangleTextPainter.java
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
package com.astra.ses.spell.gui.print.painter;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

/******************************************************************************
 * Rectangle Text Painter will be able to paint flat text inside a given area
 * and crop the text if wanted
 *****************************************************************************/
public class RectangleTextPainter
{

	/***************************************************************************
	 * Text alignment
	 **************************************************************************/
	public enum HorizontalAlignment
	{
		LEFT, CENTER, RIGHT;
	}

	/** Graphic context object */
	private Graphics	m_graphics;
	/** Inner margin */
	private int	     m_cellPadding;

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public RectangleTextPainter(Graphics graphics)
	{
		this(graphics, 0);
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param graphics
	 * @param cellPadding
	 **************************************************************************/
	public RectangleTextPainter(Graphics graphics, int cellPadding)
	{
		m_graphics = graphics;
		m_cellPadding = cellPadding;
	}

	/***************************************************************************
	 * Draw a text inside the given area
	 * 
	 * @param textToPaint
	 * @param area
	 * @return
	 ***************************************************************************/
	public Point paintText(String textToPaint, Rectangle area,
	        boolean forceFit, HorizontalAlignment align)
	{
		FontMetrics metrics = m_graphics.getFontMetrics();
		int fontHeight = metrics.getHeight();
		int descent = metrics.getDescent();
		int x = area.x;
		int y = area.y;
		int width = area.width;
		String[] toPaint = null;
		if (forceFit)
		{
			toPaint = new String[] { cropStringToCell(textToPaint, width
			        - m_cellPadding) };
		}
		else
		{
			toPaint = splitToLines(textToPaint, width);
		}
		for (String line : toPaint)
		{
			int xCoord = x;
			switch (align)
			{
			case CENTER:
				int lineWidth = (int) metrics.getStringBounds(line, m_graphics)
				        .getWidth();
				int margin = (width - lineWidth) / 2;
				xCoord += margin;
				break;
			case RIGHT:
				lineWidth = (int) metrics.getStringBounds(line, m_graphics)
				        .getWidth();
				xCoord = xCoord + width - lineWidth - m_cellPadding;
				break;
			default: // LEFT
				xCoord += m_cellPadding;
				break;
			}
			m_graphics.drawString(line, xCoord, y - descent);
			y += fontHeight;
		}
		return new Point(x + width, y);
	}

	/***************************************************************************
	 * Default way of painting, which is at the left
	 * 
	 * @param textToPaint
	 * @param area
	 * @param forceFit
	 * @return
	 **************************************************************************/
	public Point paintText(String textToPaint, Rectangle area, boolean forceFit)
	{
		return paintText(textToPaint, area, forceFit, HorizontalAlignment.LEFT);
	}

	/**************************************************************************
	 * Get the cell text adapted to area width
	 * 
	 * @param gc
	 * @param text
	 * @param maxWidth
	 * @return
	 *************************************************************************/
	private String cropStringToCell(String text, int maxWidth)
	{
		int length = text.length();
		if (length == 0) { return ""; }
		int textWidth = (int) m_graphics.getFontMetrics()
		        .getStringBounds(text, m_graphics).getWidth();
		if (textWidth <= maxWidth) { return text; }
		int maxCharacters = (length * maxWidth) / textWidth;
		if (maxCharacters == 0) { return ""; }
		return text.substring(0, maxCharacters - 1);
	}

	/**************************************************************************
	 * Split the string into different lines
	 * 
	 * @param text
	 * @param width
	 * @return
	 *************************************************************************/
	private String[] splitToLines(String text, int width)
	{
		int textWidth = (int) m_graphics.getFontMetrics()
		        .getStringBounds(text, m_graphics).getWidth();
		int numberOfLines = (int) Math.ceil((double) textWidth / width);
		String[] result = new String[numberOfLines];
		String remainingText = text;
		int i = 0;
		while (!remainingText.isEmpty())
		{
			result[i] = cropStringToCell(remainingText, width);
			remainingText = remainingText.substring(result[i].length());
			i++;
		}
		return result;
	}
}
