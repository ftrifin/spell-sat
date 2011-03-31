///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.event.functions
//
// FILE      : FunctionCallConsistencyChecker.java
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
package com.astra.ses.spell.dev.scheck.event.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.astra.ses.spell.dev.scheck.UserLibRegistry;
import com.astra.ses.spell.dev.scheck.interfaces.AbstractEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IEvent;
import com.astra.ses.spell.dev.scheck.interfaces.IEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.dev.scheck.processing.KnownFunctionsRegistry;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.FunctionDef;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.NameTok;
import com.astra.ses.spell.language.model.ast.argumentsType;
import com.astra.ses.spell.language.model.ast.exprType;

public class FunctionCallConsistencyChecker extends AbstractEventRuleChecker implements IEventRuleChecker 
{
	private Map<String, FunctionDef> m_declaredFunctions;
	private List<Call> m_calledFunctions;
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public String getName()
	{
		return "Function Call consistency checker";
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
		case FUNCTION_DEF:
			processDef(event,issues);
			break;
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyParsingStarted( IIssueList issues ) 
	{
		super.notifyParsingStarted(issues);
		m_declaredFunctions = new TreeMap<String, FunctionDef>();
		m_calledFunctions = new ArrayList<Call>();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyParsingFinished(IIssueList issues) 
	{
		List<String> processableFunctions = checkCallConsistency( issues );
		checkArgumentsConsistency( processableFunctions, issues );
		m_declaredFunctions.clear();
		m_calledFunctions.clear();
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
			if (call.func instanceof Name)
			{
				m_calledFunctions.add(call);
			}
		}
		catch(Exception ex)
		{
			issues.addIssue( 
					IssueFactory.createErrorIssue("Internal error processing token " + call + ": " + ex.getLocalizedMessage(), 
							call));
		}
	}
	
	/**************************************************************************
	 * Process a function definition event
	 * @param event
	 * @param issues
	 *************************************************************************/
	private void processDef( IEvent event, IIssueList issues )
	{
		FunctionDef def = (FunctionDef) event.getNode();
		try
		{
			String name = ((NameTok) def.name).id;
			m_declaredFunctions.put(name, def);
		}
		catch(Exception ex)
		{
			issues.addIssue( 
					IssueFactory.createErrorIssue("Internal error processing token " + def + ": " + ex.getLocalizedMessage(), 
							def));
		}
	}

	/**************************************************************************
	 * Check the consistency of function calls. Functions shall be known, or
	 * be SPELL functions, or be declared in the procedure
	 * @param issues
	 * @return The list of functions that can be processed in further checks
	 *************************************************************************/
	private List<String> checkCallConsistency( IIssueList issues )
	{
		List<FunctionDef> matchedLocalFunctions = new ArrayList<FunctionDef>();
		List<String> processableFunctions = new ArrayList<String>();
		
		for( Call call : m_calledFunctions )
		{
			String name = ((Name) call.func ).id;
			
			// Check match with discouraged functions
			if (KnownFunctionsRegistry.instance().isDiscouragedFunction(name))
			{
				issues.addIssue( 
						IssueFactory.createWarningIssue("Discouraged function: '" + name + "'", 
								call));
			}
			else // Not discouraged
			{
				// Check existence in declared functions
				boolean found = false;
				for( String defName : m_declaredFunctions.keySet() )
				{
					if (defName.equals(name)) 
					{
						found = true;
						FunctionDef def = m_declaredFunctions.get(defName);
						matchedLocalFunctions.add(def);
						break;
					}
				}
				
				if (!found) // Function not declared locally
				{
					// Check existence in userlib
					if (!UserLibRegistry.instance().isUserLibFunction( issues.getResource().getProject(), name ))
					{
						// Not userlib, check existence in known functions
						if (!KnownFunctionsRegistry.instance().isKnownFunction(name))
						{
							issues.addIssue( 
									IssueFactory.createErrorIssue("Unknown function: '" + name + "'", 
											call));
						}
					}
				}
			}
		}
		
		for(FunctionDef def : m_declaredFunctions.values())
		{
			NameTok defName = (NameTok) def.name;
			if (!matchedLocalFunctions.contains(def))
			{
				issues.addIssue( 
						IssueFactory.createWarningIssue("Declared function is never used: '" + defName.id + "'", 
								def));
			}
			else
			{
				processableFunctions.add(defName.id);
			}
		}
		return processableFunctions;
	}

	/**************************************************************************
	 * Check the consistency of function calls in terms of argument numbers
	 * @param issues
	 *************************************************************************/
	private void checkArgumentsConsistency( List<String> processableFunctions, IIssueList issues )
	{
		for( Call call : m_calledFunctions )
		{
			String functionName = ((Name)call.func).id;
			if (processableFunctions.contains(functionName))
			{
				// Get the definition
				FunctionDef def = m_declaredFunctions.get(functionName);
				argumentsType args = (argumentsType)def.args;
				
				// If the definition has flexible positional or keyword arguments, dont do checks
				if ((args.vararg == null)&&(args.kwarg == null)) 
				{
					// This is the, in principle, amount of arguments in the definition
					int numArgumentsInDef = args.args.length;
					
					// We will need to consider less args though, if there are default values
					int numDefaultValues = 0;
					for( exprType type : args.defaults )
					{
						if (type != null) numDefaultValues++;
					}
					
					// Get the number of arguments in the call
					int numArgumentsInCall = call.args.length;
					
					if (numArgumentsInCall != numArgumentsInDef)
					{
						if (numArgumentsInCall != (numArgumentsInDef - numDefaultValues))
						{
							issues.addIssue( 
									IssueFactory.createErrorIssue("Wrong number of arguments in function call, expected at least " + 
											(numArgumentsInDef-numDefaultValues) + ", found " + numArgumentsInCall, 
											call));
						}
					}
				}
			}
		}		
	}
}
