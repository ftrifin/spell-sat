///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.model.sfunctions
//
// FILE      : Modifier.java
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

import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IModifier;
import com.astra.ses.spell.dev.scheck.interfaces.ValueType;

public class Modifier implements IModifier
{
	private String m_name;
	private List<ValueType> m_validTypes;
	
	/**************************************************************************
	 * Constructor 
	 *************************************************************************/
	public Modifier()
	{
		m_name = null;
		m_validTypes = new ArrayList<ValueType>();
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	protected void setName( String name )
	{
		m_name = name;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected void addValidType( ValueType type )
	{
		m_validTypes.add(type);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected void setValidTypes( List<ValueType> types )
	{
		m_validTypes = types;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getName()
    {
	    return m_name;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public List<ValueType> getValidTypes()
    {
	    return m_validTypes;
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public boolean isValidType( ValueType type )
	{
		if (type.equals(ValueType.NAME) && m_validTypes.contains(ValueType.CONSTANT)) return true;
		return m_validTypes.contains(type) || m_validTypes.contains(ValueType.ANY);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public String toString()
	{
		return getName();
	}
}
