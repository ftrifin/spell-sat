///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.model.sfunctions
//
// FILE      : DefinedArgument.java
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
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IDefinedArgument;
import com.astra.ses.spell.dev.scheck.interfaces.ValueChecker;
import com.astra.ses.spell.dev.scheck.interfaces.ValueType;

public class DefinedArgument implements IDefinedArgument
{
	private static final String ATTRIBUTE_POSITION = "position";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_VALUES = "values";
	private static final String ATTRIBUTE_COMBINED = "combined";
	private static final String VALUES_SEPARATOR= ",";

	private int m_position;
	private List<ValueType> m_validTypes;
	private List<String> m_expectedValues;
	private boolean m_canCombineValues;
	
	/**************************************************************************
	 * Constructor from XML rules
	 * @param configElement
	 *************************************************************************/
	public DefinedArgument( Element configElement )
	{
		m_position = Integer.parseInt(configElement.getAttribute(ATTRIBUTE_POSITION));
		String typeStr = configElement.getAttribute(ATTRIBUTE_TYPE);
		if ((typeStr != null)&&(!typeStr.isEmpty()))
		{
			typeStr = typeStr.toUpperCase();
			for( String element : typeStr.split(VALUES_SEPARATOR) )
			{
				if (!element.trim().isEmpty())
				{
					addValidType( ValueType.valueOf(element.trim().toUpperCase()) );
				}
			}
		}
		if (m_validTypes.isEmpty()) m_validTypes.add(ValueType.ANY);

		m_canCombineValues = false;
		String combined = configElement.getAttribute(ATTRIBUTE_COMBINED);
		if ((combined != null)&&(!combined.isEmpty()))
		{
			m_canCombineValues = Boolean.parseBoolean(combined);
		}

		m_expectedValues = new ArrayList<String>();
		// Compute expected values
		String values = configElement.getAttribute(ATTRIBUTE_VALUES);
		if ((values != null)&&(!values.isEmpty()))
		{
			String[] elements = values.split(VALUES_SEPARATOR);
			for(String element : elements)
			{
				element = element.trim();
				if (!element.isEmpty())
				{
					m_expectedValues.add(element);
				}
			}
		}
	}
	
	protected void addValidType( ValueType type )
	{
		if (m_validTypes == null)
		{
			m_validTypes = new ArrayList<ValueType>();
		}
		m_validTypes.add(type);
	}

	protected void setValidTypes( List<ValueType> types )
	{
		m_validTypes = types;
	}

	@Override
    public int getPosition()
    {
	    return m_position;
    }

	@Override
    public List<String> getExpectedValues()
    {
	    return m_expectedValues;
    }

	@Override
    public List<ValueType> getValidTypes()
    {
	    return m_validTypes;
    }
	
	@Override
	public boolean isValidType( ValueType type )
	{
		return m_validTypes.contains(type) || m_validTypes.contains(ValueType.ANY) || type.equals(ValueType.ANY);
	} 

	@Override
    public boolean isValidValue( String value, ValueType type )
    {
		return ValueChecker.isValid(value, type, m_expectedValues, m_validTypes, m_canCombineValues);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	public String toString()
	{
		return "D" + m_position + ": " + Arrays.toString(getValidTypes().toArray()) + "|" + Arrays.toString(getExpectedValues().toArray());
	}
}
