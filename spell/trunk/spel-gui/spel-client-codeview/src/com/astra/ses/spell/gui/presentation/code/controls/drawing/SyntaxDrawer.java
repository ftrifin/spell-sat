///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : SyntaxDrawer.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.presentation.code.controls.drawing;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.procs.model.ProcedureLine;

/*******************************************************************************
 * @brief Table item drawer used for formatting procedure code.
 * @date 27/03/08
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class SyntaxDrawer implements Listener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the text layout to be drawn */
	private static TextLayout s_layout = new TextLayout(Display.getCurrent());
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	
	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================
	
	// PRIVATE -----------------------------------------------------------------
	/** Reference to the syntax formatter */
	private SyntaxFormatter m_formatter = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * @param formatter
	 * 		Syntax formatter class to be used
	 **************************************************************************/ 
	public SyntaxDrawer( SyntaxFormatter formatter )
	{
		m_formatter = formatter;
	}

	/***************************************************************************
	 * Table cell item paint event callback
	 * @param event
	 * 		Event information
	 **************************************************************************/ 
	public void handleEvent(Event event)
	{
		// Obtain the column index
		int index = event.index;
		// Only for code column
		if (index==2)
		{
			// Get the corresponding table item
			TableItem item = (TableItem) event.item;
			// And the associated graphic context
			GC gc = event.gc;
			// Obtain the code text. It is stored this way in order to
			// prevent the normal alg. to paint it
			ProcedureLine line = (ProcedureLine) item.getData("PROC_LINE");
			String text = line.getSource();
			// Assign the text to the text layout
			s_layout.setText(text);
			// And apply the styles to the layout
			m_formatter.applyScheme(s_layout);
			int y = event.y + (event.height-gc.getFontMetrics().getHeight())/2;
			// Then draw it in the table item
			s_layout.draw(gc, event.x + 5, y);
		}
	}
}
