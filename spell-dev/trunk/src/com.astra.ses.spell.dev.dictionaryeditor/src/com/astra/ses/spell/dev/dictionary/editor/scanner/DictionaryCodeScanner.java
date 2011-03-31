///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.dictionary.editor.scanner
// 
// FILE      : DictionaryCodeScanner.java
//
// DATE      : 2010-07-06
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
package com.astra.ses.spell.dev.dictionary.editor.scanner;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/*******************************************************************************
 * Database code scanner will detect tokens in the database file 
 *
 ******************************************************************************/
public class DictionaryCodeScanner extends RuleBasedScanner {
	
	/***************************************************************************
	 * Word detector for brackets
	 **************************************************************************/
	private class ListDictWordDetector implements IWordDetector
	{
		@Override
		public boolean isWordPart(char c) {
			return false;
		}

		@Override
		public boolean isWordStart(char c) {
			return ((c == '[') || (c == ']') || (c == '{') || (c == '}'));
		}
	}
	
	/** Comment line begins with a hash */
	private static final String commentLineBeginning = "#";
	/** Comment background and foreground */
	private static final Color COMMENT_FG;
	private static final Color KEY_COLOR;
	private static final Color VALUE_COLOR;
	
	static
	{
		Display display = Display.getCurrent();
		COMMENT_FG = new Color(display, 0,121,0);
		KEY_COLOR = new Color(display, 32,32,32);
		VALUE_COLOR = new Color(display, 10,64,80);
	}
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public DictionaryCodeScanner()
	{
		defineRules();
	}
	
	/***************************************************************************
	 * Define rules contributed by the scanner
	 **************************************************************************/
	private void defineRules()
	{
		/*
		 * Comment line rule
		 */
		Token commentToken = new Token(new TextAttribute(COMMENT_FG, null, SWT.ITALIC));
		SingleLineRule commentRule = new EndOfLineRule(commentLineBeginning, commentToken);
		/*
		 * Key Value line pair rules
		 */
		Token keyToken = new Token(new TextAttribute(KEY_COLOR, null, SWT.BOLD));
		Token valueToken = new Token(new TextAttribute(VALUE_COLOR, null, SWT.NONE));
		SingleLineRule stringRule = new SingleLineRule("'", "'", valueToken);
		NumberRule numberRule = new NumberRule(valueToken);
		WordRule bracketRule = new WordRule(new ListDictWordDetector(), keyToken);
		WordRule identifierRule = new WordRule(new IWordDetector(){
			@Override
			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart((char) c);
			}

			@Override
			public boolean isWordStart(char c) {
				return Character.isJavaIdentifierStart((char) c);
			}	
		}, keyToken);
		/*
		 * Rules setting
		 */
		setRules(new IRule[]{commentRule, bracketRule, stringRule, numberRule, identifierRule});
	}
}
