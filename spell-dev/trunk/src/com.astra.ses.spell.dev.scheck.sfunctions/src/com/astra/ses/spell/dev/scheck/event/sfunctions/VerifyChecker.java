///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.event.sfunctions
//
// FILE      : VerifyChecker.java
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

import com.astra.ses.spell.dev.scheck.database.UsingRaw;
import com.astra.ses.spell.dev.scheck.interfaces.EventType;
import com.astra.ses.spell.dev.scheck.interfaces.IEvent;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.language.model.ast.BinOp;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.List;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.NameTok;
import com.astra.ses.spell.language.model.ast.exprType;
import com.astra.ses.spell.language.model.ast.keywordType;

public class VerifyChecker extends VerificationStepChecker 
{
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public String getName()
	{
		return "Verify function rule";
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    protected void processEvent(IEvent event, IIssueList issues)
    {
		if(event.getType().equals(EventType.FUNCTION_CALL))
		{
			Call call = (Call) event.getNode();
			if (!(call.func instanceof Name)) return;
			Name name = (Name) call.func;
			String functionName = name.id;
			if (functionName.equals("Verify"))
			{
				processCall(call,issues);
			}
		}
    }

	/**************************************************************************
	 * Process a function call event
	 * @param event
	 * @param issues
	 *************************************************************************/
	private void processCall( Call call, IIssueList issues )
	{
		UsingRaw usingRaw = UsingRaw.NOT_SET;
		if (call.keywords.length >0)
		{
			// Get the raw condition first, important for verification steps
			for( keywordType kwd : call.keywords )
			{
				NameTok nametok = (NameTok) kwd.arg;
				if (nametok.id.equals("ValueFormat"))
				{
					exprType format = kwd.value;
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
							// Config is in variable so we cannot know
							usingRaw = UsingRaw.DONT_KNOW;
						}
					}
					break;
				}
			}
		}
		
		if (call.args.length>0)
		{
			exprType mainListExpr = (exprType) call.args[0];
			
			// Ignore the rule if we have a name or binary operation
			if ((mainListExpr instanceof Name)||(mainListExpr instanceof BinOp))
			{
				return;
			}
			
			if (!(mainListExpr instanceof List))
			{
				error("Expected a list as main argument", mainListExpr, issues);
			}
			else
			{
				List mainList = (List) mainListExpr;
				if (mainList.elts.length==0)
				{
					error("Verification list cannot be empty", mainList, issues);
				}
				for( exprType element : mainList.elts )
				{
					if (!(element instanceof List))
					{
						error("Expected lists within the verification list", element, issues );
					}
					else
					{
						processVerificationStep( (List) element, issues, usingRaw );
					}
				}
			}
		}
	}

	@Override
    protected boolean isLiteralRelevant(String literal)
    {
	    return literal.startsWith("T ");
    }
}
