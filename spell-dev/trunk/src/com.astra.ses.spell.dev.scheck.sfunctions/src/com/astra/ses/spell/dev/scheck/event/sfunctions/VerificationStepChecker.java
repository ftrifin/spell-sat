///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.event.sfunctions
//
// FILE      : VerificationStepChecker.java
//
// DATE      : Feb 23, 2011
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.scheck.event.sfunctions;

import java.util.ArrayList;

import com.astra.ses.spell.dev.scheck.database.DatabaseConsistencyChecker;
import com.astra.ses.spell.dev.scheck.database.UsingRaw;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.language.SpellProgrammingLanguage;
import com.astra.ses.spell.language.model.ast.Dict;
import com.astra.ses.spell.language.model.ast.List;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.exprType;

public abstract class VerificationStepChecker extends DatabaseConsistencyChecker 
{
	private static java.util.List<String> s_validModifiers;
	
	static 
	{
		s_validModifiers = new ArrayList<String>();
		s_validModifiers.add("Timeout");
		s_validModifiers.add("Wait");
		s_validModifiers.add("Tolerance");
		s_validModifiers.add("AdjLimits");
		s_validModifiers.add("ValueFormat");
	};

	/**************************************************************************
	 * 
	 *************************************************************************/
	private UsingRaw checkConfig( exprType config, IIssueList issues )
	{
		// Cannot check variables
		if (config instanceof Name ) 
		{
			// The config is in a variable so we don't know if value format is set
			return UsingRaw.DONT_KNOW;
		}
		if (!(config instanceof Dict))
		{
			error("Malformed verification step, expected a configuration dictionary", config, issues);
			return UsingRaw.DONT_KNOW;
		}
		Dict dict = (Dict) config;
		// By default the value format is not set
		UsingRaw usingRaw = UsingRaw.NOT_SET;
		int index = 0;
		for( exprType key : dict.keys )
		{
			if (!(key instanceof Name))
			{
				error("Malformed verification step, expected a modifier", key, issues);
			}
			Name modname = (Name) key; 
			if (! SpellProgrammingLanguage.getInstance().isSpellModifier(modname.id))
			{
				error("Malformed verification step, expected a modifier", key, issues);
			}
			else if (!s_validModifiers.contains(modname.id))
			{
				error("Malformed verification step, modifier not allowed", key, issues);
			}
			
			if (modname.id.equals("ValueFormat"))
			{
				exprType format = dict.values[index];
				if (format instanceof Name)
				{
					Name formatName = (Name) format;
					if (formatName.id.equals("RAW")) 
					{
						usingRaw = UsingRaw.YES;
					}
					else if (formatName.id.equals("ENG"))
					{
						usingRaw = UsingRaw.NO;
					}
					else
					{
						// Config is set in variable, so we dont know
						usingRaw = UsingRaw.DONT_KNOW;
					}
				}
			}
			
			index++;
		}
		return usingRaw;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected void processVerificationStep( List step, IIssueList issues, UsingRaw globalUsingRaw )
	{
		if (step.elts.length<3)
		{
			error("Malformed verification step, list shall be composed of 3 elements at least", step, issues);
			return;
		}
		
		// Depending on the unary or binary operator, the checks are different.
		// So, check the operator first
		// Check the operator
		boolean binary = false;
		boolean unary = false;
		exprType op = step.elts[1];
		if (op instanceof Name)
		{
			Name nameTok = (Name) op;
			String name = nameTok.id;
			if (!SpellProgrammingLanguage.getInstance().isSpellKeyword(name))
			{
				error("Not a valid operator: '" + name + "'", op, issues);
			}
			else
			{
				binary = name.equals("bw") || name.equals("nbw");
				unary = !binary;
			}
		}
		else
		{
			error("Not a valid operator", op, issues);
		}

		if (binary)
		{
			if ((step.elts.length < 4)||(step.elts.length > 5))
			{
				error("Malformed verification step, list shall be composed of 4 or 5 elements when using binary operators", step, issues);
				return;
			}
		}
		else if (unary)
		{
			if ((step.elts.length < 3)||(step.elts.length > 4))
			{
				error("Malformed verification step, list shall be composed of 3 or 4 elements when using unary operators", step, issues);
				return;
			}
		}
		
		// Check the mnemonic  
		exprType item = step.elts[0];
		String mnemonic = checkMnemonic( item, issues );
		
		UsingRaw stepUsingRaw = UsingRaw.NOT_SET;
		// If there is a config dictionary, check the modifiers
		if ((unary && step.elts.length == 4)||(binary && step.elts.length == 5))
		{
			exprType config = step.elts[ step.elts.length -1 ];
			stepUsingRaw = checkConfig( config, issues );
		}
		
		// Check the values
		exprType value = step.elts[2];
		
		checkValue( mnemonic, value, issues, getFinalUsingRaw( globalUsingRaw, stepUsingRaw ) );	
		
		// For binary operations, check the second value
		if (binary)
		{
			exprType value2 = step.elts[3];
			checkValue( mnemonic, value2, issues, getFinalUsingRaw( globalUsingRaw, stepUsingRaw ) );	
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	protected static UsingRaw getFinalUsingRaw( UsingRaw global, UsingRaw step )
	{
		switch(step)
		{
		case YES:
			return UsingRaw.YES;
		case NO: 
			return UsingRaw.NO;
		default:// Step does not have particular config or the config is in variable
			return global;
		}
	}
}
