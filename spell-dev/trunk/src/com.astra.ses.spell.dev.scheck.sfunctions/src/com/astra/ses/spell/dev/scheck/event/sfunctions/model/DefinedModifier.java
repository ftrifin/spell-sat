///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.model.sfunctions
//
// FILE      : DefinedModifier.java
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
package com.astra.ses.spell.dev.scheck.event.sfunctions.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IDefinedModifier;
import com.astra.ses.spell.dev.scheck.interfaces.ValueChecker;
import com.astra.ses.spell.dev.scheck.interfaces.ValueType;

public class DefinedModifier extends Modifier implements IDefinedModifier
{
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_REQUIRES = "requires";
	private static final String ATTRIBUTE_VALUES = "values";
	private static final String ATTRIBUTE_ALTERNATIVES = "alternatives";
	private static final String ATTRIBUTE_COMBINED = "combined";
	private static final String VALUES_SEPARATOR= ",";

	private List<String> m_required;
	private List<String> m_expectedValues;
	private List<String> m_alternatives;
	private boolean m_canCombineValues;
	
	/**************************************************************************
	 * Constructor from XML rules
	 * @param configElement
	 *************************************************************************/
	public DefinedModifier( Element configElement )
	{
		super();
		setName(configElement.getAttribute(ATTRIBUTE_NAME));
		m_alternatives = new ArrayList<String>();
		String alt = configElement.getAttribute(ATTRIBUTE_ALTERNATIVES);
		if ((alt!=null)&&(!alt.isEmpty()))
		{
			String[] alternatives = alt.split(VALUES_SEPARATOR);
			for(String alternative : alternatives)
			{
				alternative = alternative.trim();
				if (!alternative.isEmpty())
				{
					m_alternatives.add(alternative);
				}
			}
		}
		String typeStr = configElement.getAttribute(ATTRIBUTE_TYPE);
		typeStr = typeStr.toUpperCase();
		String[] elements = typeStr.split(VALUES_SEPARATOR);
		for(String element : elements )
		{
			element = element.trim().toUpperCase();
			if (!element.isEmpty())
			{
				ValueType type = ValueType.valueOf(element.toUpperCase());
				addValidType(type);
			}
		}
		if (getValidTypes().size()==0) addValidType(ValueType.ANY);
		
		m_expectedValues = new ArrayList<String>();
		m_canCombineValues = false;
		String combined = configElement.getAttribute(ATTRIBUTE_COMBINED);
		if ((combined != null)&&(!combined.isEmpty()))
		{
			m_canCombineValues = Boolean.parseBoolean(combined);
		}
		
		// Compute requirements
		m_required = new ArrayList<String>();
		String requires = configElement.getAttribute(ATTRIBUTE_REQUIRES);
		if ((requires != null)&&(!requires.isEmpty()))
		{
			String[] relements = requires.split(VALUES_SEPARATOR);
			for(String element : relements)
			{
				element = element.trim();
				if (!element.isEmpty())
				{
					m_required.add( element );
				}
			}
		}
		
		// Compute expected values
		String values = configElement.getAttribute(ATTRIBUTE_VALUES);
		if (values != null)
		{
			String[] velements = values.split(VALUES_SEPARATOR);
			for(String element : velements)
			{
				String elementStr = element.trim();
				if (!elementStr.isEmpty())
				{
					m_expectedValues.add(elementStr);
				}
			}
		}
	}
	
	/**************************************************************************
	 * Alternative constructor
	 *************************************************************************/
	public DefinedModifier( String name, IDefinedModifier original )
	{
		super();
		setName(name);
		m_required = original.getRequires();
		m_expectedValues = original.getExpectedValues();
		m_alternatives = null;
		for( ValueType type : original.getValidTypes() )
		{
			addValidType(type);
		}
	}


	@Override
    public List<String> getAlternatives()
    {
	    return m_alternatives;
    }

    public List<String> getExpectedValues()
    {
	    return m_expectedValues;
    }

	@Override
    public List<String> getRequires()
    {
	    return m_required;
    }

	@Override
    public boolean isValidValue( String value, ValueType type )
    {
		return ValueChecker.isValid(value, type, m_expectedValues, getValidTypes(), m_canCombineValues);
    }
}
