///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.processing
// 
// FILE      : EventDispatcher.java
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
package com.astra.ses.spell.dev.scheck.processing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.dev.scheck.interfaces.EventType;
import com.astra.ses.spell.dev.scheck.interfaces.IEvent;
import com.astra.ses.spell.dev.scheck.interfaces.IEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IIssue;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.language.Visitor;
import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.FunctionDef;
import com.astra.ses.spell.language.model.ast.Str;
import com.astra.ses.spell.language.model.ast.StrJoin;

public class EventDispatcher extends Visitor implements IEventDispatcher  
{
	private List<IEventRuleChecker> m_stringEventListeners;
	private List<IEventRuleChecker> m_callEventListeners;
	private List<IEventRuleChecker> m_defEventListeners;
	private IIssueList m_issues;
	private IProgressMonitor m_monitor;
	private int m_currentLine;
	private int m_totalLines;
	
	/**************************************************************************
	* 
	**************************************************************************/
	public EventDispatcher()
	{
		m_stringEventListeners = new ArrayList<IEventRuleChecker>();
		m_callEventListeners = new ArrayList<IEventRuleChecker>();
		m_defEventListeners = new ArrayList<IEventRuleChecker>();
		m_issues = null;
	}
	
	/**************************************************************************
	* 
	**************************************************************************/
	@Override
	public void initialize( IResource resource, int totalLines, IProgressMonitor monitor )
	{
		m_issues = IssueFactory.createIssueList(resource);
		m_monitor = monitor;
		m_totalLines = totalLines;
		m_currentLine = 0;
	}

	/**************************************************************************
	* 
	**************************************************************************/
	@Override
	public void finalize()
	{
		if (m_currentLine < m_totalLines)
		{
			m_monitor.worked( m_totalLines - m_currentLine );
		}
		if (m_issues != null) m_issues.clear();
	}

	/**************************************************************************
	* 
	**************************************************************************/
	@Override
	public void gatherIssues( IIssueList issues )
	{
		for(IIssue issue : m_issues.getIssues())
		{
			issues.addIssue(issue);
			if (m_monitor.isCanceled()) return;
		}
	}
	
	/**************************************************************************
	* 
	**************************************************************************/
	@Override
	public void registerForEvent( EventType event, IEventRuleChecker listener) 
	{
		switch(event)
		{
		case STRING_LITERAL:
			if (!m_stringEventListeners.contains(listener)) m_stringEventListeners.add(listener);
			break;
		case FUNCTION_CALL:
			if (!m_callEventListeners.contains(listener)) m_callEventListeners.add(listener);
			break;
		case FUNCTION_DEF:
			if (!m_defEventListeners.contains(listener)) m_defEventListeners.add(listener);
			break;
		}
	}

	/**************************************************************************
	* 
	**************************************************************************/
	private void updateStatus( SimpleNode node )
	{
		if (m_currentLine < node.beginLine )
		{
			int diff = (node.beginLine - m_currentLine);
			m_monitor.worked(diff);
			m_currentLine = node.beginLine;
		}
	}

	/**************************************************************************
	* 
	**************************************************************************/
	private boolean dispatch( IEvent event, List<IEventRuleChecker> list ) throws Exception
	{
		for(IEventRuleChecker checker : list)
		{
			try
			{
				//m_monitor.subTask( "Rule checker: " + checker.getName() );
				if (m_monitor.isCanceled()) 
				{
					return false;
				}
				checker.notifyEvent(event, m_issues);
				if (m_monitor.isCanceled()) 
				{
					return false;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return true;
	}

	/**************************************************************************
	* 
	**************************************************************************/
	@Override
	public Object visitCall( Call node ) throws Exception
	{
		if (m_monitor.isCanceled()) 
		{
			return null;
		}
		updateStatus(node);
		IEvent event = new ParseEvent( EventType.FUNCTION_CALL, node );
		if (dispatch(event, m_callEventListeners ))
		{
			return super.visitCall(node);
		}
		return null;
	}

	/**************************************************************************
	* 
	**************************************************************************/
	@Override
	public Object visitFunctionDef( FunctionDef def ) throws Exception
	{
		if (m_monitor.isCanceled()) 
		{
			return null;
		}
		updateStatus(def);
		IEvent event = new ParseEvent( EventType.FUNCTION_DEF, def );
		if (dispatch(event, m_defEventListeners ))
		{
			return super.visitFunctionDef(def);
		}
		return null;
	}

	/**************************************************************************
	* 
	**************************************************************************/
	@Override
	public Object visitStr( Str str ) throws Exception
	{
		if (m_monitor.isCanceled()) 
		{
			return null;
		}
		updateStatus(str);
		IEvent event = new ParseEvent( EventType.STRING_LITERAL, str );
		if (dispatch(event, m_stringEventListeners ))
		{
			return super.visitStr(str);
		}
		return null;
	}

	/**************************************************************************
	* 
	**************************************************************************/
	@Override
	public Object visitStrJoin( StrJoin str ) throws Exception
	{
		if (m_monitor.isCanceled()) 
		{
			return null;
		}
		updateStatus(str);
		IEvent event = new ParseEvent( EventType.STRING_LITERAL, str );
		if (dispatch(event, m_stringEventListeners ))
		{
			return super.visitStrJoin(str);
		}
		return null;
	}
}
