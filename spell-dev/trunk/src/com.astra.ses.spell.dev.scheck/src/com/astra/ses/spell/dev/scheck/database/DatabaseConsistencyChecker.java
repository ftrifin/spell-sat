///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.database
//
// FILE      : DatabaseConsistencyChecker.java
//
// DATE      : Feb 21, 2011
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
package com.astra.ses.spell.dev.scheck.database;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import com.astra.ses.spell.dev.database.DatabaseManager;
import com.astra.ses.spell.dev.database.interfaces.ICalibration;
import com.astra.ses.spell.dev.database.interfaces.IRepresentation;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.interfaces.ITelemetryParameter;
import com.astra.ses.spell.dev.database.interfaces.IRepresentation.Type;
import com.astra.ses.spell.dev.scheck.interfaces.AbstractEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IEvent;
import com.astra.ses.spell.dev.scheck.interfaces.IEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.Num;
import com.astra.ses.spell.language.model.ast.Str;
import com.astra.ses.spell.language.model.ast.StrJoin;
import com.astra.ses.spell.language.model.ast.exprType;

public abstract class DatabaseConsistencyChecker extends AbstractEventRuleChecker implements IEventRuleChecker 
{
	private ISpellDatabase m_currentDatabase;
	private Map<String,ISpellDatabase> m_databases;
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public DatabaseConsistencyChecker()
	{
		m_databases = new TreeMap<String, ISpellDatabase>();
		m_currentDatabase = null;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void notifyEvent(IEvent event, IIssueList issues) 
	{
		String project = issues.getResource().getProject().getName();
		checkDatabaseAvailability(project,issues);
		processEvent(event,issues);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	protected abstract void processEvent( IEvent event, IIssueList issues );
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private void checkDatabaseAvailability( String project, IIssueList issues )
	{
		if (!m_databases.containsKey(project))
		{
			ISpellDatabase db = DatabaseManager.getInstance().getProjectDatabase(project);
			if (db == null)
			{
				m_currentDatabase = null;
			}
			else
			{
				m_databases.put(project, db);
				m_currentDatabase = db;
			}
		}
		else
		{
			m_currentDatabase = m_databases.get(project);
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected void error( String message, SimpleNode node, IIssueList issues )
	{
		issues.addIssue( IssueFactory.createErrorIssue(message, node));
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected void warning( String message, SimpleNode node, IIssueList issues )
	{
		issues.addIssue( IssueFactory.createWarningIssue(message, node));
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected ISpellDatabase getDatabase()
	{
		return m_currentDatabase;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected boolean hasDatabase()
	{
		return (m_currentDatabase!= null);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	protected String getEquivalentStringLiteral( SimpleNode node )
	{
		String literal = "";
		if (node instanceof Str)
		{
			literal = ((Str)node).s;
		}
		else if (node instanceof StrJoin )
		{
			StrJoin join = (StrJoin) node;
			for( exprType s : join.strs)
			{
				literal += getEquivalentStringLiteral(s);
			}
		}
		return literal;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected String checkMnemonic( exprType item, IIssueList issues )
	{
		String mnemonic = null;
		
		// Check the TM point. The general TM consistency plugin is doing checks,
		// but we need to cover those cases like not using 'T' keyword in the
		// verification steps
		if (hasDatabase())
		{
			if ((item instanceof Str)||(item instanceof StrJoin))
			{
				String literal = getEquivalentStringLiteral(item);
				
				// Warning if no T prefix used
				if (!literal.startsWith("T "))
				{
					warning("Telemetry point names should use the 'T' prefix", item, issues);
				}
				
				// If no mnemonic directly is used
				if (!getDatabase().isTelemetryParameter(literal))
				{
					// And it is not starting with the T keyword
					if (!isLiteralRelevant(literal))
					{
						error("Unknown telemetry point '" + literal + "'", item, issues);
					}
					// It starts with T, check it in the database
					else
					{
						String[] parts = literal.split(" ");
						if (parts.length>=2)
						{
							mnemonic = parts[1];
							if (!getDatabase().isTelemetryParameter(mnemonic))
							{
								error("Telemetry point '" + mnemonic + "' not found in database", item, issues);
								mnemonic = null;
							}
						}							
					}
				}
				// Otherwise we are good
				else
				{
					mnemonic = literal;
				}
			}
		}
		return mnemonic;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	protected void checkValue( String mnemonic, exprType value, IIssueList issues, UsingRaw usingRaw )
	{
		// Check the value consistency
		if (hasDatabase() && (mnemonic != null))
		{
			ITelemetryParameter param = getDatabase().getTelemetryModel(mnemonic);
			IRepresentation def = param.getDefaultRepresentation();
			
			if (value instanceof Num)
			{
				// If using a number, and not using raw
				//TODO assuming that notset is equivalent to ENG, but this is in configuration defaults!
				if (usingRaw.equals(UsingRaw.NO) || usingRaw.equals(UsingRaw.NOT_SET))
				{
					if ((def!=null)&&(!def.getType().equals(Type.NUMERICAL)))
					{
						error("Expected a status value for this TM point", value, issues);
					}
				}
			}
			else if ((value instanceof Str) || (value instanceof StrJoin))
			{
				switch(usingRaw)
				{
				case YES:
					error("Expected numerical value for this TM point, using raw format", value, issues);
					break;
				case DONT_KNOW:
					// Cant check
					break;
				case NOT_SET:
				case NO:
					// Wrong type, parameter is not status
					if ((def!=null)&&(!def.getType().equals(Type.STATUS)))
					{
						error("Expected a numerical value for this TM point", value, issues);
					}
					// Correct type, parameter is status. Check valid value
					else
					{
						ICalibration calib = def.getCalibration();
						String valStr = getEquivalentStringLiteral(value);
						if (calib != null)
						{
							if (!calib.isValidValue(valStr))
							{
								error("Wrong value for this TM point calibration, expected " + Arrays.toString(calib.getValidValues().toArray()), value, issues);
							}
						}
					}
				}
			}
		}

	}


	/**************************************************************************
	 * 
	 *************************************************************************/
	protected abstract boolean isLiteralRelevant( String literal );
}
