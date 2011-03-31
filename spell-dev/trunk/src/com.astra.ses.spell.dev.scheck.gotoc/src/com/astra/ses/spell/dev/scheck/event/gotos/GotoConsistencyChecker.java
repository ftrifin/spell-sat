///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.event.gotos
// 
// FILE      : GotoConsistencyChecker.java
//
// DATE      : Mar 22, 2011
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
package com.astra.ses.spell.dev.scheck.event.gotos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.astra.ses.spell.dev.scheck.interfaces.AbstractEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IEvent;
import com.astra.ses.spell.dev.scheck.interfaces.IEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.BinOp;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.Str;
import com.astra.ses.spell.language.model.ast.StrJoin;
import com.astra.ses.spell.language.model.ast.Subscript;
import com.astra.ses.spell.language.model.ast.exprType;

public class GotoConsistencyChecker extends AbstractEventRuleChecker implements IEventRuleChecker 
{
	private Map<String,SimpleNode> m_gotoTargets;
	private List<String> m_stepIds;
	
	@Override
	public String getName()
	{
		return "Goto consistency checker";
	}
	
	@Override
	public void initialize()
	{
	}
	
	@Override
	public void notifyEvent(IEvent event, IIssueList issues) 
	{
		/* Call[
		  		func=Name[id=Step, ctx=Load, reserved=false], 
		        
		        args=[
		        	Str[s=..., type=SingleDouble, unicode=false, raw=false, binary=false], 
		        	Str[s=..., type=SingleDouble, unicode=false, raw=false, binary=false]], 
		        
		        keywords=[], 
		        starargs=null, 
		        kwargs=null
		       ]
		*/
		
		Call call = (Call) event.getNode();
		try
		{
			if (! (call.func instanceof Name)) return;
			Name functionName = (Name) call.func;
			if (functionName.id.equals("Step"))
			{
				if (call.args.length!=2)
				{
					issues.addIssue( 
							IssueFactory.createErrorIssue("Wrong number of arguments in Step call", 
									call));
					return;
				}
				
				if (  (!(call.args[0] instanceof Str)) &&
					  (!(call.args[0] instanceof StrJoin ))
				   )
				{
					issues.addIssue( 
							IssueFactory.createErrorIssue("Step identifier shall be a string", 
									call.args[0]));
					return;
				}
	
				if (  (!(call.args[1] instanceof Str))   &&
					  (!(call.args[1] instanceof BinOp)) &&
				      (!(call.args[1] instanceof StrJoin ))
				   )
				{
					issues.addIssue( 
							IssueFactory.createErrorIssue("Step title shall be a string (found " + call.args[1] + ")", 
									call.args[1]));
					return;
				}
				
				// Store the step identifier for check at the end of the parsing
				Str stepId = (Str) call.args[0];
				m_stepIds.add( stepId.s );
			}
			else if (functionName.id.equals("Goto"))
			{
				if (call.args.length!=1)
				{
					issues.addIssue( 
							IssueFactory.createErrorIssue("Wrong number of arguments in Goto call", 
									call));
					return;
				}
	
				if ( (!(call.args[0] instanceof Str))       &&
					 (!(call.args[0] instanceof Name))      &&
					 (!(call.args[0] instanceof Call))      &&
					 (!(call.args[0] instanceof Subscript)) &&
					 (!(call.args[0] instanceof BinOp))     &&
					 (!(call.args[0] instanceof StrJoin)) 
				   )
				{
					issues.addIssue( 
							IssueFactory.createErrorIssue("Goto target shall be a variable or string", 
									call.args[0]));
					return;
				}
				
				// Store the goto target for check at the end of the parsing
				// Consider only string types, we don't know what is in variables
				if (call.args[0] instanceof Str)
				{
					Str target = (Str) call.args[0];
					m_gotoTargets.put( target.s, call.args[0] );
				}
				if (call.args[0] instanceof StrJoin)
				{
					StrJoin target = (StrJoin) call.args[0];
					String total = "";
					for( exprType str : target.strs )
					{
						if (str instanceof Str)
						{
							total += ((Str) str).s;
						}
					}
					if (!total.isEmpty()) m_gotoTargets.put( total, call.args[0] );
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			issues.addIssue( 
					IssueFactory.createErrorIssue("Internal error processing token " + call + ": " + ex.getLocalizedMessage(), 
							call));
		}
	}

	@Override
	public void notifyParsingStarted( IIssueList issues ) 
	{
		super.notifyParsingStarted(issues);
		m_gotoTargets = new TreeMap<String,SimpleNode>();
		m_stepIds = new ArrayList<String>();
	}

	@Override
	public void notifyParsingFinished(IIssueList issues) 
	{
		for(String target : m_gotoTargets.keySet())
		{
			if (!m_stepIds.contains(target))
			{
				SimpleNode token = m_gotoTargets.get(target);
				issues.addIssue( 
						IssueFactory.createErrorIssue("Goto target has not been found: '" + target + "'" , 
								token));
			}
		}
		m_gotoTargets.clear();
		m_stepIds.clear();
	}

}
