///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.model
// 
// FILE      : BasicStyleScheme.java
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
package com.astra.ses.spell.gui.presentation.code.model;

import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;


/*******************************************************************************
 * @brief Implementation of a basic style scheme for syntax highlighting
 * @date 27/03/08
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class BasicStyleScheme implements StyleScheme
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Flag for default style usage warning */
	private static boolean s_firstTimeWarningStyle = true;
	/** Face name for the code font */
	private static final String CODE_FONT_FACE = "Courier New";
	/** Face name for the normal font */
	private static final String NORMAL_FONT_FACE = "Arial";
	// PROTECTED ---------------------------------------------------------------
	/** Holds the colors associated for each token type */
	protected static TreeMap<TokenTypes,Color> s_colors = new TreeMap<TokenTypes,Color>();
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Code-style font */
	private Font m_codeFont = null;
	/** Normal font */
	private Font m_normalFont = null;
	/** Default style */
	private TextStyle m_defaultStyle = null; 
	// PROTECTED ---------------------------------------------------------------
	/** Currently selected scheme */
	protected SchemeType m_scheme = null;
	/** Currently configured font size*/
	protected int m_fontSize = 0;
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
	public BasicStyleScheme( int initialFontSize )
	{
		//Initialize the scheme parameters
		setScheme(SchemeType.DAY);
		setFontSize(initialFontSize);
		Logger.debug("Color scheme ready", Level.GUI,this);
	}
	
	/***************************************************************************
	 * Set the desired color scheme.
	 * @param s
	 * 		The scheme identifier, defined in SchemeType enumeration
	 **************************************************************************/
	public void setScheme(SchemeType s)
	{
		// Each time the scheme is changed, colors shall be reloaded
		if (m_scheme != s)
		{
			m_scheme = s;
			loadColorScheme();
		}
	}

	/***************************************************************************
	 * Set the current font size.
	 * @param fontSize
	 * 		The current font size
	 **************************************************************************/
	public void setFontSize( int fontSize )
	{
		// Each time the font size is changed, fonts shall be recreated
		if (fontSize != m_fontSize)
		{
			m_fontSize = fontSize;
			generateFonts();
		}
	}
	
	/***************************************************************************
	 * Obtain the text style corresponding to the given token type
	 * @param type
	 * 		Token type id
	 * @return
	 * 		The corresponding style
	 **************************************************************************/
	public TextStyle getStyle( TokenTypes type )
	{
		// Obtain the corresponding color first.
		// If no color is found for the given type, use the default (NORMAL)
		Color c = null;
		if (s_colors.containsKey(type))
		{
			c = s_colors.get(type);
		}
		else
		{
			c = s_colors.get(TokenTypes.NORMAL);
		}
		// Find the corresponding style, depending on the token type.
		TextStyle toApply = null;
		switch(type)
		{
		case NORMAL:
		case SYMBOL:
		case CONSTANT:
		case NUMBER:
		case CODE:
		case MODIFIER:
		case SPEL:
			toApply = new TextStyle(m_codeFont,c,null);
			break;
		case STRING:
			toApply = new TextStyle(m_codeFont,c,null);
			break;
		case COMMENT:
			toApply = new TextStyle(m_codeFont,c,null);
			break;
		default:
			if (s_firstTimeWarningStyle)
			{
				s_firstTimeWarningStyle = false;
				Logger.warning("Using default style (given " + type + ")", Level.GUI, this);
			}
			toApply = new TextStyle(m_codeFont,c,null);
			break;
		}
		return toApply;
	}
	
	/***************************************************************************
	 * Obtain the currently defined code-style font
	 * @return	
	 * 		The code font
	 **************************************************************************/
	public Font getCodeFont()
	{
		return m_codeFont;
	}
	
	/***************************************************************************
	 * Obtain the currently defined normal-style font
	 * @return	
	 * 		The font
	 **************************************************************************/
	public Font getNormalFont()
	{
		return m_normalFont;
	}
	
	/***************************************************************************
	 * Obtain the default style
	 * @return	
	 * 		The default text style  
	 **************************************************************************/
	public TextStyle getDefaultStyle()
	{
		return m_defaultStyle;
	}
	
	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================
	
	/***************************************************************************
	 * Load colors depending on the configured scheme. Each scheme defines a
	 * different set of colors. There is one color defined per each existing
	 * token type.
	 **************************************************************************/
	protected void loadColorScheme()
	{
		Logger.debug("Loading color schemes", Level.GUI,this);
		//TODO: load colors from configuration file
		s_colors.put(TokenTypes.NORMAL, Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		s_colors.put(TokenTypes.STRING, Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		s_colors.put(TokenTypes.CODE, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
		s_colors.put(TokenTypes.SPEL, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
		s_colors.put(TokenTypes.COMMENT, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
		s_colors.put(TokenTypes.MODIFIER, new Color(Display.getCurrent(), 170, 115, 0));
		s_colors.put(TokenTypes.SYMBOL, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
		s_colors.put(TokenTypes.NUMBER, new Color(Display.getCurrent(), 10, 110, 255));
		s_colors.put(TokenTypes.CONSTANT, new Color(Display.getCurrent(), 140, 0, 180));
		s_colors.put(TokenTypes.BACKGROUND, new Color(Display.getCurrent(), 225, 235, 240));
	}

	/***************************************************************************
	 * Re-generate the fonts. Shall be invoked after changing the configured 
	 * font size.
	 **************************************************************************/
	protected void generateFonts()
	{
		m_codeFont = new Font(Display.getCurrent(), CODE_FONT_FACE, m_fontSize,SWT.BOLD);
		m_normalFont = new Font(Display.getCurrent(), NORMAL_FONT_FACE, m_fontSize,SWT.NORMAL);
		m_defaultStyle = new TextStyle(m_codeFont,Display.getCurrent().getSystemColor(SWT.COLOR_BLACK),null);
	}

}
