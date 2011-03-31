///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : SyntaxFormatter.java
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

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextLayout;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.presentation.code.model.BasicStyleScheme;
import com.astra.ses.spell.gui.presentation.code.model.SchemeType;
import com.astra.ses.spell.gui.presentation.code.model.StyleScheme;
import com.astra.ses.spell.gui.presentation.code.model.SyntaxParser;


/*******************************************************************************
 * @brief Composite of a style scheme and a syntax parser make a syntax formatter
 * @date 27/03/08
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class SyntaxFormatter
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================
	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================
	// PRIVATE -----------------------------------------------------------------
	/** The style scheme */
	private StyleScheme m_scheme;
	/** The syntax parser */
	private SyntaxParser m_parser;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------


	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param initialFontSize
	 * 		The initial font size
	 **************************************************************************/
	public SyntaxFormatter( int initialFontSize )
	{
		Logger.debug("Creating syntax highlighter", Level.GUI,this);
		m_scheme = new BasicStyleScheme( initialFontSize );
		m_parser = new SyntaxParser(m_scheme);
		Logger.debug("Syntax highlighter ready", Level.GUI,this);
	}
	
	/***************************************************************************
	 * Assign the desired style scheme
	 * 
	 * @param scheme
	 * 		The style scheme
	 **************************************************************************/
	public void setScheme( SchemeType scheme )
	{
		Logger.debug("Set scheme: " + scheme, Level.GUI,this);
		m_scheme.setScheme(scheme);
	}
	
	/***************************************************************************
	 * Assign the font size
	 * 
	 * @param size
	 * 		The font size
	 **************************************************************************/
	public void setFontSize( int size )
	{
		m_scheme.setFontSize( size );
	}
	
	/***************************************************************************
	 * Obtain the current code font
	 * 
	 * @return
	 * 		The font
	 **************************************************************************/
	public Font getCodeFont()
	{
		return m_scheme.getCodeFont();
	}
	
	/***************************************************************************
	 * Apply the syntax scheme to the given text layout
	 * 
	 * @param layout
	 * 		The layout containing the text to be formatted.
	 **************************************************************************/
	public void applyScheme( TextLayout layout )
	{
		m_parser.parseSyntax(layout);
	}
}
