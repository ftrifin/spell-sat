///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.model.sfunctions
//
// FILE      : ValueChecker.java
//
// DATE      : Feb 15, 2011
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
package com.astra.ses.spell.dev.scheck.interfaces;

import java.util.List;
import java.util.StringTokenizer;

import com.astra.ses.spell.language.SpellProgrammingLanguage;

public class ValueChecker
{
	public static final String COMBINE_SEPARATOR = "|";
	
    public static boolean isValid( String value, ValueType type, List<String> expectedValues, List<ValueType> expectedTypes, boolean canCombine )
    {
    	// If the list of expected values is null or empty the value is valid
    	if ((expectedValues == null)||(expectedValues.isEmpty())) return true;
    	
    	// If the given value is of type NAME, accept it. We cannot see inside variables
    	if (type.equals(ValueType.NAME)) return true;

    	// If the value contains the combination separator and we can combine values
	    if (value.contains(COMBINE_SEPARATOR) && canCombine)
	    {
			StringTokenizer tokenizer = new StringTokenizer(value, COMBINE_SEPARATOR);
	    	while(tokenizer.hasMoreTokens())
	    	{
	    		String givenElement = (String) tokenizer.nextElement();
	    		givenElement = givenElement.trim();
	    		if (!givenElement.isEmpty())
	    		{
		    		if (!isValid(givenElement, type, expectedValues, expectedTypes, false)) 
	    			{
		    			return false;
	    			}
	    		}
	    	}
	    	return true;
	    }
	    // For single elements
	    else
	    {
	    	// If the list of expected types includes ANY, the value is valid.
	    	if ((expectedTypes==null)||expectedTypes.contains(ValueType.ANY)||expectedTypes.isEmpty()) 
	    	{
	    		if (expectedValues.contains(value)) return true;
	    	}
	    	// If the list of expected types includes NAME and the value is the code "@name", is valid
	    	if (expectedTypes.contains(ValueType.NAME))
	    	{
	    		if (value.equals("@name")) return true;
	    	}
	    	// If the list of expected types includes LIST and the value is the code "@list", is valid
	    	if (expectedTypes.contains(ValueType.LIST))
	    	{
	    		if (value.equals("@list")) return true;
	    	}
	    	// If the list of expected types includes DICT and the value is the code "@dict", is valid
	    	if (expectedTypes.contains(ValueType.DICT))
	    	{
	    		if (value.equals("@dict")) return true;
	    	}
	    	if (expectedTypes.contains(ValueType.CONSTANT))
	    	{
		    	if (expectedTypes.contains(ValueType.NAME))
		    	{
		    		if (value.equals("@name")) return true;
		    	}
	    		if (SpellProgrammingLanguage.getInstance().isSpellConstant(value)) 
    			{
	    			System.err.println("is spelll");
	    			if (expectedValues.contains(value)) return true;
    			}
	    	}
	    	return false;
	    }
    }
}
