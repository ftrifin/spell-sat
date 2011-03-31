///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.model.sfunctions
//
// FILE      : GivenModifier.java
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

import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IGivenModifier;
import com.astra.ses.spell.dev.scheck.interfaces.TokenHelper;
import com.astra.ses.spell.dev.scheck.interfaces.ValueType;
import com.astra.ses.spell.language.model.ast.NameTok;
import com.astra.ses.spell.language.model.ast.exprType;
import com.astra.ses.spell.language.model.ast.keywordType;

public class GivenModifier extends Modifier implements IGivenModifier
{
	private String m_givenValue;
	private ValueType m_givenType;
	private NameTok m_nameToken;
	private exprType m_valueToken;
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public GivenModifier( keywordType kwd )
	{
		super();
		/*
		 * 		  	keywords=[	keyword[	arg=NameTok[id=Wait, ctx=KeywordName], 
		  						    value=Name[id=True, ctx=Load, reserved=true]
		  					   ]
		 */
		
		m_nameToken = (NameTok) kwd.arg;
		setName( m_nameToken.id );
		m_valueToken = kwd.value;
		
		try
		{
			m_givenType = TokenHelper.getEquivalentType(kwd.value);
			m_givenValue = TokenHelper.getEquivalentValue(kwd.value);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getGivenValue()
    {
	    return m_givenValue;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public ValueType getGivenType()
    {
	    return m_givenType;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public NameTok getNameToken()
    {
	    return m_nameToken;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public exprType getValueToken()
    {
	    return m_valueToken;
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public String toString()
	{
		return getName() + "=" + m_givenValue;
	}

}
