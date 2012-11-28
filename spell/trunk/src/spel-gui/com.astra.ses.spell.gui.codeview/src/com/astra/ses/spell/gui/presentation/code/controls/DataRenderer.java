///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : DataRenderer.java
//
// DATE      : Nov 21, 2012
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.presentation.code.controls;


import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class DataRenderer extends SourceRenderer
{
	public DataRenderer( IProcedure model )
	{
		super(model);
	}
	
	@Override
	public void paint(GC gc, Object value)
	{
		GridItem item = (GridItem) value;
		ICodeLine line = getLineModel(item);
		ICodeLine prev = null;
		if (line.getLineNo()>1)
		{
			prev = getModel().getExecutionManager().getLine(line.getLineNo()-2);
		}
		
		gc.setFont(getFont());

		try
		{
			if (line.hasNotifications())
			{
				boolean drawBackground = true;
				if (item.getParent().isEnabled())
				{
					Color back = getBackground(line);
					if (back != null)
					{
						gc.setBackground(back);
					}
					else
					{
						drawBackground = false;
					}
				}
				else
				{
					gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				}
				gc.setForeground( Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
	
				if (drawBackground)
				{
					gc.fillRectangle(getBounds().x, getBounds().y-1, getBounds().width, getBounds().height+2);
				}
	
				int x = m_leftMargin;
				
				int cellW = getBounds().width - m_leftMargin - m_rightMargin;

				String textName = line.getSummaryName();
				String textValue = line.getSummaryValue();

				int top = getBounds().y-1;
				int bottom = getBounds().y + getBounds().height-1;

				if (textName.trim().isEmpty() && textValue.trim().isEmpty())
				{
					// Nothing to write
				}
				else if (textValue.trim().isEmpty())
				{
					// Draw name
					textName = getShortString(gc, textName, cellW );
					x += setupAlignment( gc, textName, cellW );
					gc.drawString(textName, getBounds().x + x, getBounds().y + m_textTopMargin + m_topMargin);
				}
				// Both value and name present
				else
				{
					// Draw name
					textName = textName + " = " + textValue;
					textName = getShortString(gc, textName, cellW );
					x += setupAlignment( gc, textName, cellW );
					gc.drawString(textName, getBounds().x + x, getBounds().y + m_textTopMargin + m_topMargin);
				}

				// Vertical cell separator
				gc.drawLine(getBounds().x-1, top, getBounds().x-1, bottom);

				if (prev == null || !prev.hasNotifications())
				{
					gc.drawLine(getBounds().x-1, top, getBounds().x + getBounds().width, top);
				}
				gc.drawLine(getBounds().x-1, bottom, getBounds().x + getBounds().width, bottom);
	
			}
			else
			{
				super.paint(gc, value);
			}
		}
		catch(Exception ignore) {};
	}
}
