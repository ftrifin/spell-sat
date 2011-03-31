///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.model
// 
// FILE      : SyntaxParser.java
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

import java.util.Vector;

import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;

import de.susebox.jtopas.Flags;
import de.susebox.jtopas.StandardTokenizer;
import de.susebox.jtopas.StandardTokenizerProperties;
import de.susebox.jtopas.StringSource;
import de.susebox.jtopas.Token;
import de.susebox.jtopas.TokenizerException;
import de.susebox.jtopas.TokenizerProperties;

/*******************************************************************************
 * @brief Source code parser based on JTOPAS package. Implements a basic token
 * recognition algorithm for SPEL. Requires JTOPAS runtime package.
 * @date 27/03/08
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class SyntaxParser
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the list of SPEL functions */
	//TODO: load from configuration file
	private static String[] s_functions =
	{
		"GetTM", "Display", "Verify", "SendAndVerify",
		"SendAndVerifyAdjLim",
		"Send","SetGroundParameter","Prompt","Abort",
		"Event","Step","GetResource", "SetResource",
		"UserLogin", "UserLogout", "CheckUser",
		"StartTask", "StopTask", "CheckTask",
		"SetExecDelay", "StartProc","LoadDictionary",
		"WaitFor","AdjustLimits","EnableAlarms",
		"DisableAlarms","SetTMparam","GetTMparam",
		"Script","OpenDisplay","CloseDisplay",
		"PrintDisplay","Goto"
	};
	/** Holds the list of SPEL modifiers */
	//TODO: load from configuration file
	private static String[] s_modifiers =
	{
		"ValueFormat", "OnFailure", "Wait",
		"Timeout", "Delay", "TryAll", "Time",
		"Retries", "Host", "Tolerance",
		"Delay","Type","Severity","Scope",
		"OnTrue","OnFalse","PromptUser",
		"Retry","GiveChoice","HandleError",
		"ValueType","Radix","Units","Strict",
		"Interval","Until","HiYel","HiRed",
		"LoYel","LoRed","HiBoth","LoBoth",
		"Midpoint","Limits","IgnoreCase",
		"Block","Sequence","Default","Mode",
		"Confirm","OnSkip","SendDelay",
		"eq", "gt", "lt", "neq", "ge", "le", "btw", "nbw",
		"command", "sequence", "group", "args",
		"verify", "config", "Printer", "Format"
	};
	/** Holds the list of SPEL constants */
	//TODO: load from configuration file
	private static String[] s_constants =
	{
		"YES_NO", "ALPHA", "NUM", "LIST", "YES", "OK_CANCEL",
		"OK", "CANCEL", "NO", "YES_NO", "COMBO",
		"ENG", "RAW", "DEC", "BIN", "OCT", "HEX",
		"INFO", "WARNING", "ERROR", 
		"DISPLAY", "LOGVIEW","DIALOG","LONG","DATETIME","STRING",
		"FLOAT","BOOLEAN",
		"ABORT", "SKIP", "REPEAT", "RECHECK", "RESEND", "NOACTION",
		"PROMPT","NOPROMPT", 
		"ACTION_ABORT", "ACTION_SKIP", "ACTION_REPEAT", "ACTION_RESEND",
		"ACTION_CANCEL",
		"MINUTE", "HOUR", "TODAY", "YESTERDAY", "DAY", "SECOND"
	};
	/** Holds the list of python and other keywords */
	private static String[] s_language =
	{
		"for", "if", "elif", "else", "try", "except",
		"while","in","print",
		"del", "def", "config", "verify",
		"True", "False", "import", "type", "level",
		"and", "or", "not", "global", "str",
		"abs", "float", "int", "pass", "assert"
	};
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	
	
	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Currently configured scheme */
	private StyleScheme m_scheme = null;
	// PROTECTED ---------------------------------------------------------------
	/** Holds the list of defined functions */
	protected Vector<String> m_listFunctions;
	/** Holds the list of defined modifiers */
	protected Vector<String> m_listModifiers;
	/** Holds the list of defined constants */
	protected Vector<String> m_listConstants;
	/** Holds the list of defined language keywords */
	protected Vector<String> m_listLanguage;
	/** Holds the JTOPAS tokenizer configuration */
	protected TokenizerProperties m_properties = null;
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INNER CLASSES
	// =========================================================================

	/***************************************************************************
	 * Holds the applicability range for a text style 
	 **************************************************************************/
	private class Range
	{
		/** Start position */
		int start;
		/** End position */
		int end;
	}

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * @param scheme
	 * 		Initial color scheme 
	 **************************************************************************/
	public SyntaxParser( StyleScheme scheme )
	{
		Logger.debug("Reading word list", Level.GUI,this);
		m_listFunctions = new Vector<String>();
		m_listModifiers = new Vector<String>();
		m_listConstants = new Vector<String>();
		m_listLanguage = new Vector<String>();
		for(String f : s_functions) m_listFunctions.addElement(f);
		for(String m : s_modifiers) m_listModifiers.addElement(m);
		for(String l : s_language)  m_listLanguage.addElement(l);
		for(String c : s_constants)  m_listConstants.addElement(c);
		Logger.debug("Word list read", Level.GUI,this);
		m_scheme = scheme;
		// Configure the JTOPAS tokenizer to recognise SPEL
		configureTokenizer();
	}
	
	/***************************************************************************
	 * Parse the text contained in the given layout and apply the corresponding
	 * text styles to it
	 * @param layout
	 *  	Text layout containing the text to be parsed. Styles are applied
	 *  	to this layout as well.
	 **************************************************************************/
	public void parseSyntax( TextLayout layout )
	{
		// Obtain the text to parse
		String text = layout.getText();
		// Do not process null or blank text
		if (text == null || text.length()==0) return;
		// Default style
		layout.setStyle(m_scheme.getDefaultStyle(), 0, text.length());

		// Create a source for the tokenizer with the text
		StringSource source = new StringSource( text );
		// Create the tokenizer using the current configuration
		StandardTokenizer tokenizer = new StandardTokenizer(m_properties);
		// Assign the source
		tokenizer.setSource(source);
		boolean inBlockComment = false;
		try
		{
			// For each token recognised in the text
			while(tokenizer.hasMoreToken())
			{
				Token token = tokenizer.nextToken();
				int type = token.getType();
				// This is the token text
				String word = tokenizer.currentImage();
				// When the image is null, we are processing EOL
				if (word == null) break;
				boolean isDocString = (type == Token.SPECIAL_SEQUENCE && word.equals("\"\"\"")); 
				if (isDocString)
				{
					inBlockComment = !inBlockComment;
				}
				// Find the applicable style, depending on the token type
				TextStyle toApply = null; 
				if (inBlockComment || isDocString)
				{
					toApply = m_scheme.getStyle(TokenTypes.COMMENT);
				}
				else
				{
					toApply = getApplicableStyle(word,type);
				}
				// If no style is returned, continue to next token
				if (toApply == null) continue;
				// Get the applicable range (find token position)
				Range range = getApplicableRange(text, word, token);
				// Apply the style to the layout
				layout.setStyle(toApply, range.start, range.end);
			}
			// Close tokenizer
			tokenizer.close();
			tokenizer = null;
		}
		catch (TokenizerException e)
		{
			e.printStackTrace();
		}
	}
	
	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================
	
	/***************************************************************************
	 * Configure the JTOPAS tokenizer to recognise SPEL
	 **************************************************************************/
	protected void configureTokenizer()
	{
		Logger.debug("Configuring syntax tokenizer", Level.GUI,this);
		try
		{
			Logger.debug("Setting flags", Level.GUI,this);
			// Use the standard configuration as a base
			m_properties = new StandardTokenizerProperties();
			// Return token positions and line comments
			m_properties.setParseFlags( Flags.F_TOKEN_POS_ONLY | Flags.F_RETURN_LINE_COMMENTS );
			// Python comments
			// Block comments are parsed manually
			m_properties.addLineComment("#");
			// Python strings
			m_properties.addString("\"", "\"", "\"");
			m_properties.addString("\'", "\'", "\'");
			// Normal whitespaces
			m_properties.addWhitespaces(TokenizerProperties.DEFAULT_WHITESPACES);
			// Normal separators
			m_properties.addSeparators(TokenizerProperties.DEFAULT_SEPARATORS);
			// Add our keywords
			Logger.debug("Adding keywords", Level.GUI,this);
			for(String word : m_listFunctions)
			{
				m_properties.addKeyword(word);
			}
			for(String word : m_listModifiers)
			{
				m_properties.addKeyword(word);
			}
			for(String word : m_listLanguage)
			{
				m_properties.addKeyword(word);
			}
			for(String word : m_listConstants)
			{
				m_properties.addKeyword(word);
			}
			// Special symbols
			Logger.debug("Adding symbols", Level.GUI,this);
			m_properties.addSpecialSequence("\"\"\"");
			m_properties.addSpecialSequence("{");
			m_properties.addSpecialSequence("}");
			m_properties.addSpecialSequence("(");
			m_properties.addSpecialSequence(")");
			m_properties.addSpecialSequence("[");
			m_properties.addSpecialSequence("]");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/***************************************************************************
	 * Check if the given word is a SPEL function
	 * @return
	 * 		True if the word is a SPEL function
	 **************************************************************************/
	protected boolean isFunction( String word )
	{
		return m_listFunctions.contains(word);
	}

	/***************************************************************************
	 * Check if the given word is a SPEL modifier
	 * @return
	 * 		True if the word is a SPEL modifier
	 **************************************************************************/
	protected boolean isModifier( String word )
	{
		return m_listModifiers.contains(word);
	}

	/***************************************************************************
	 * Check if the given word is a language keyword
	 * @return
	 * 		True if the word is a language keyword
	 **************************************************************************/
	protected boolean isLanguage( String word )
	{
		return m_listLanguage.contains(word);
	}

	/***************************************************************************
	 * Check if the given word is a SPEL constant
	 * @return
	 * 		True if the word is a SPEL constant
	 **************************************************************************/
	protected boolean isConstant( String word )
	{
		return m_listConstants.contains(word);
	}

	/***************************************************************************
	 * Obtain the applicability range for the given token.
	 * @param text
	 * 		The whole line
	 * @param word
	 * 		The image of the token being processed
	 * @param token
	 * 		The token being processed
	 * @return
	 * 		The corresponding range
	 **************************************************************************/
	protected Range getApplicableRange( String text, String word, Token token )
	{
		Range r = new Range();
		switch(token.getType())
		{
		case Token.STRING:
		case Token.UNKNOWN:
		case Token.SPECIAL_SEQUENCE:
		case Token.NORMAL:
		case Token.KEYWORD:
			r.start = token.getStartPosition();
			r.end = r.start + word.length();
			break;
		case Token.SEPARATOR:
			// Separators shall be processed this way
			r.start = token.getStartPosition();
			r.end = r.start + 1;
			break;
		case Token.LINE_COMMENT:
		default:
			// For comments, and by default apply the style to the whole line
			r.start = token.getStartPosition();
			r.end = text.length();
			break;
		}
		return r;
	}
	
	/***************************************************************************
	 * Check if the given token is a number
	 * @param s
	 * 		The token
	 * @return
	 * 		True if it is a number
	 **************************************************************************/
	protected boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
			return true;
		}
		catch(NumberFormatException ex)
		{
			return false;
		}
	}

	/***************************************************************************
	 * Find the corresponding style for the given token type
	 * @param word
	 * 		Token image
	 * @param type
	 * 		Token type
	 * @return
	 * 		The corresponding style
	 **************************************************************************/
	protected TextStyle getApplicableStyle( String word, int type ) 
	{
		TextStyle toApply = null;
		switch(type)
		{
		case Token.KEYWORD:
			if (isFunction(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.SPEL);
			}
			else if (isLanguage(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.CODE);
			}
			else if (isModifier(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.MODIFIER);
			}
			else if (isConstant(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.CONSTANT);
			}
			else
			{
				toApply = m_scheme.getStyle(TokenTypes.NORMAL);
			}
			break;
		case Token.LINE_COMMENT:
			toApply = m_scheme.getStyle(TokenTypes.COMMENT);
			break;
		case Token.NORMAL:
			// Easier than using a pattern for recognising numbers
			if (isNumber(word))
			{
				toApply = m_scheme.getStyle(TokenTypes.NUMBER);
			}
			else
			{
				toApply = m_scheme.getStyle(TokenTypes.NORMAL);
			}
			break;
		case Token.WHITESPACE:
		case Token.EOF:
			break;
		case Token.SEPARATOR:
		case Token.SPECIAL_SEQUENCE:
			toApply = m_scheme.getStyle(TokenTypes.SYMBOL);
			break;
		case Token.STRING:
			toApply = m_scheme.getStyle(TokenTypes.STRING);
			break;
		case Token.UNKNOWN:
		default:
			toApply = m_scheme.getDefaultStyle();
			break;
		}
		return toApply;
	}
}
