///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.event.sfunctions
//
// FILE      : SpellCallConsistencyChecker.java
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
package com.astra.ses.spell.dev.scheck.event.sfunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.astra.ses.spell.dev.config.ConfigurationManager;
import com.astra.ses.spell.dev.scheck.event.sfunctions.model.SpellFunctionRule;
import com.astra.ses.spell.dev.scheck.interfaces.AbstractEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IEvent;
import com.astra.ses.spell.dev.scheck.interfaces.IEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.Name;

public class SpellCallConsistencyChecker extends AbstractEventRuleChecker implements IEventRuleChecker 
{
	private static final String SPELL_FUNCTION_CHECKER_EXTENSION_POINT = "com.astra.ses.spell.dev.scheck.SpellCallChecker";
	private static final String CFG_ELEMENT_FUNCTION = "spellFunction";

	private Map<String,List<SpellFunctionRule>> m_checkers;
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public String getName()
	{
		return "SPELL Function Call consistency checker";
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void initialize()
	{
		loadSubcheckers();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyEvent(IEvent event, IIssueList issues) 
	{
		switch(event.getType())
		{
		case FUNCTION_CALL:
			processCall(event,issues);
			break;
		}
	}

	/**************************************************************************
	 * Process a function call event
	 * @param event
	 * @param issues
	 *************************************************************************/
	private void processCall( IEvent event, IIssueList issues )
	{
		Call call = (Call) event.getNode();
		try
		{
			if (!(call.func instanceof Name)) return;
			Name name = (Name) call.func;
			String functionName = name.id;
			if (m_checkers.containsKey(functionName))
			{
				for( SpellFunctionRule checker : m_checkers.get(functionName))
				{
					try
					{
						checker.checkCall(call, issues);
					}
					catch( Exception ex )
					{
						ex.printStackTrace();
						issues.addIssue( 
								IssueFactory.createErrorIssue("Internal error when calling checkCall on checker for " + call + ": " + ex.getLocalizedMessage(), 
										call));
					}
				}
			}
		}
		catch(Exception ex)
		{
			issues.addIssue( 
					IssueFactory.createErrorIssue("Internal error processing token " + call + ": " + ex.getLocalizedMessage(), 
							call));
		}
	}
	
	private void loadSubcheckers()
	{
		m_checkers = new TreeMap<String,List<SpellFunctionRule>>();
		
		String configBase = ConfigurationManager.getInstance().getConfigHome() + File.separator + "semantics" + File.separator;
		
		// Extensions registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// Load static checker extensions
		IExtensionPoint ep = registry.getExtensionPoint(SPELL_FUNCTION_CHECKER_EXTENSION_POINT);
		// If there are extensions
		if (ep != null)
		{
			IExtension[] extensions = ep.getExtensions();
			for (IExtension extension : extensions)
			{
				for(IConfigurationElement cfgElement : extension.getConfigurationElements())
				{
					String spellFunction = cfgElement.getAttribute(CFG_ELEMENT_FUNCTION);
					if (!m_checkers.containsKey(spellFunction))
					{
						System.out.println("[SEMANTICS] Loaded SPELL function checker: " + spellFunction);
						m_checkers.put(spellFunction, new ArrayList<SpellFunctionRule>() );
					}
					SpellFunctionRule rule = new SpellFunctionRule( spellFunction, configBase + spellFunction.toLowerCase() + ".xml" );
					m_checkers.get(spellFunction).add(rule);
				}
			}
		}
	}

}
