///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.model
// 
// FILE      : SpellCodeScanner.java
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.spelleditor.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.python.pydev.editor.PyCodeScanner;
import org.python.pydev.ui.ColorAndStyleCache;

import com.astra.ses.spell.dev.spelleditor.preferences.ColorPreferences;
import com.astra.ses.spell.language.SpellProgrammingLanguage;

public class SpellCodeScanner extends PyCodeScanner {

	public SpellCodeScanner(ColorAndStyleCache colorCache) {
		super(colorCache);
	}

	/****************************************************************************
	 * 
	 * WordDetector used for the word rule
	 *
	 ***************************************************************************/
	private static class SpellWordDetector implements IWordDetector
	{
		@Override
		public boolean isWordPart(char c) {
			 return Character.isJavaIdentifierPart(c);
		}

		@Override
		public boolean isWordStart(char c) {
			 return Character.isJavaIdentifierStart(c);
		}		
	}
	
	/** Spell language rules */
	private static final IRule[] SPELL_RULES;
    
	static
	{
		/*
		 * Colors should be read from preferences
		 */
		RGB funRGB = ColorPreferences.getRGBColor(ColorPreferences.FUNCTION_COLOR);
        IToken spellFunctionToken = new Token( new TextAttribute(new Color(Display.getCurrent(), funRGB), null, SWT.BOLD));
        RGB modRGB = ColorPreferences.getRGBColor(ColorPreferences.MODIFIER_COLOR);
        IToken spellModifierToken = new Token( new TextAttribute(new Color(Display.getCurrent(), modRGB), null, SWT.BOLD));
        RGB conRGB = ColorPreferences.getRGBColor(ColorPreferences.CONSTANT_COLOR);
        IToken spellConstantToken = new Token( new TextAttribute(new Color(Display.getCurrent(), conRGB), null, SWT.BOLD));
        RGB entRGB = ColorPreferences.getRGBColor(ColorPreferences.ENTITY_COLOR);
		IToken spellEntityToken = new Token( new TextAttribute(new Color(Display.getCurrent(), entRGB), null, SWT.BOLD));
        
		List<IRule> rules = new ArrayList<IRule>();
		
        SpellProgrammingLanguage spellLang = SpellProgrammingLanguage.getInstance();
        
        WordRule wordRule = new WordRule(new SpellWordDetector());
        IToken token = null;
        //Adding spell constants
        token = spellConstantToken;
        for (String keyword : spellLang.getSpellConstants()) {
            wordRule.addWord(keyword, token);
        }
        
        //Adding spell functions
        token = spellFunctionToken;
        for (String keyword : spellLang.getSpellFunctions()) {
            wordRule.addWord(keyword, token);
        }
        
        //Adding spell modifiers
        token = spellModifierToken;
        for (String keyword : spellLang.getSpellModifiers()) {
            wordRule.addWord(keyword, token);
        }
        
        //Adding spell entities
        token = spellEntityToken;
        for (String keyword : spellLang.getSpellEntities()) {
            wordRule.addWord(keyword, token);
        }
        
        //Set the rule
        rules.add(wordRule);
        
        /*
         * Set the rules to the scanner
         */
        SPELL_RULES = new IRule[rules.size()];
        rules.toArray(SPELL_RULES);
	}
	
	/***************************************************************************
	 * Set the rules for parsing the code
	 **************************************************************************/
	public void setRules(IRule[] rules)
	{
		if (rules == null) return;
		
		IRule[] rulesJoint = new IRule[rules.length + SPELL_RULES.length];
		/*
		 * Copy SPELL rules 
		 */
		for (int j = 0; j < SPELL_RULES.length; j++)
		{
			rulesJoint[j] = SPELL_RULES[j];
		}
		/*
		 * Copy rules given by PyCodeScanner
		 */
		for (int i = 0; i < rules.length; i++)
		{
			rulesJoint[i + SPELL_RULES.length] = rules[i];
		}
		/*
		 * setRules
		 */
		super.setRules(rulesJoint);
	}
}
