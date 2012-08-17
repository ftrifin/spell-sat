///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : BreakpointColumnDrawer.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.presentation.code.controls.drawing;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.presentation.code.Activator;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerColumn;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

public class BreakpointColumnDrawer extends AbstractColumnDrawer
{
	/** Breakpoint images */
	private Image[]	m_breakpointImages;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public BreakpointColumnDrawer(IProcedureDataProvider dataProvider,
	        ITableColorProvider colorProvider)
	{
		super(dataProvider, colorProvider);
		// Breakpoint images
		m_breakpointImages = new Image[2];
		m_breakpointImages[BreakpointType.PERMANENT.ordinal()] = Activator
		        .getImageDescriptor("icons/breakpoint_permanent.png")
		        .createImage();
		m_breakpointImages[BreakpointType.TEMPORARY.ordinal()] = Activator
		        .getImageDescriptor("icons/breakpoint_temporary.png")
		        .createImage();
	}

	@Override
	public void paintItem(Event event, TableItem item, int rowIndex)
	{
		if ((event.detail & SWT.SELECTED) != 0)
		{
			event.detail &= ~SWT.SELECTED;
		}

		Color background = getColorProvider().getBackground(item,
		        CodeViewerColumn.BREAKPOINT.ordinal());
		Color foreground = getColorProvider().getForeground(item,
		        CodeViewerColumn.BREAKPOINT.ordinal());

		event.gc.setBackground(background);
		event.gc.setForeground(foreground);

		// Paint basic background
		event.gc.fillRectangle(event.x, event.y, event.width, event.height);

		BreakpointType breakpoint = BreakpointType.UNKNOWN;
		try
		{
			breakpoint = getDataProvider().getBreakpoint(rowIndex);
			// Breakpoint image
			Image breakpointImage = null;
			if (!breakpoint.equals(BreakpointType.UNKNOWN))
			{
				breakpointImage = m_breakpointImages[breakpoint.ordinal()];
				int width = breakpointImage.getBounds().width;
				int height = breakpointImage.getBounds().height;
				int y = (event.height - height) / 2;
				event.gc.drawImage(breakpointImage, 0, 0, width, height,
				        event.x, event.y + y, width, height);
			}
		}
		catch (UninitProcedureException e)
		{
		}
		;

		// Paint column borders
		event.gc.drawLine(event.x + event.width - 1, event.y, event.x
		        + event.width - 1, event.y + event.height);
		event.gc.drawLine(event.x, event.y, event.x, event.y + event.height);
	}

}
