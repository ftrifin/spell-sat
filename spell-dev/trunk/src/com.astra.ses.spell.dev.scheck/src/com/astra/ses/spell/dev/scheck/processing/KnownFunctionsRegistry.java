///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.processing
//
// FILE      : KnownFunctionsRegistry.java
//
// DATE      : Feb 14, 2011
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
package com.astra.ses.spell.dev.scheck.processing;

import java.util.ArrayList;
import java.util.List;

import com.astra.ses.spell.dev.config.ConfigurationManager;
import com.astra.ses.spell.language.SpellProgrammingLanguage;

public class KnownFunctionsRegistry 
{
	private static KnownFunctionsRegistry s_instance = null;
	
	private List<String> m_knownFunctions;
	private List<String> m_discouragedFunctions;
	
	public static KnownFunctionsRegistry instance()
	{
		if (s_instance == null)
		{
			s_instance = new KnownFunctionsRegistry();
		}
		return s_instance;
	}
	
	private KnownFunctionsRegistry()
	{
		m_knownFunctions = new ArrayList<String>();
		m_discouragedFunctions = new ArrayList<String>();
	}

	public void loadKnownFunctions()
	{
		m_knownFunctions.clear();
		m_discouragedFunctions.clear();
		for(String function : SpellProgrammingLanguage.getInstance().getSpellFunctions())
		{
			m_knownFunctions.add(function);
		}
		List<String> configuredFuns = ConfigurationManager.getInstance().getKnownFunctions();
		for(String function : configuredFuns )
		{
			m_knownFunctions.add(function);
		}
		List<String> discFuns = ConfigurationManager.getInstance().getDiscouragedFunctions();
		for(String function : discFuns )
		{
			m_discouragedFunctions.add(function);
		}
	}
	
	public boolean isKnownFunction( String functionName )
	{
		return m_knownFunctions.contains(functionName);
	}

	public boolean isSpellFunction( String functionName )
	{
		return SpellProgrammingLanguage.getInstance().isSpellFunction(functionName);
	}

	public boolean isDiscouragedFunction( String functionName )
	{
		return m_discouragedFunctions.contains(functionName);
	}
}
