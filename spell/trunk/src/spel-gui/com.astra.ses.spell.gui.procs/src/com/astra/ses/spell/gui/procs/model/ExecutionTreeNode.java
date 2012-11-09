////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionTreeNode.java
//
// DATE      : 2010-07-30
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
// SUBPROJECT: SPELL GUI Client
//
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.Map;
import java.util.TreeMap;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeNode;

/*******************************************************************************
 * 
 * ProcedureCode holds the Source code block execution information
 * 
 ******************************************************************************/
public class ExecutionTreeNode implements IExecutionTreeNode
{

	/** Code id */
	private String m_codeId;
	/** Code name if any */
	private String m_codeName;
	/** Parent line */
	private IExecutionTreeLine m_parentLine;
	/** On execution line */
	private IExecutionTreeLine m_currentLine;
	/** Notified lines map */
	private Map<Integer, IExecutionTreeLine> m_lines;
	/** Execution ranges */
	private Map<Integer, ExecutionRange> m_executionRanges;
	/** Holds the recursion depth */
	private int m_depth;
	/** Current execution */
	private int m_currentExecution;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param codeId
	 *            the source code's id this block this object refers to
	 **************************************************************************/
	public ExecutionTreeNode(String codeId, String codeName, IExecutionTreeLine parent, int depth, int execution, long sequence)
	{
		m_depth = depth;
		m_codeId = codeId;
		m_codeName = codeName;
		m_parentLine = parent;
		m_lines = new TreeMap<Integer, IExecutionTreeLine>();
		m_executionRanges = new TreeMap<Integer, ExecutionRange>();
		m_currentExecution = execution;
		/*
		 * Initialize ranges
		 */
		m_executionRanges.put(execution, new ExecutionRange(sequence));
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getCodeId()
	 * =========================================================================
	 */
	@Override
	public String getCodeId()
	{
		return m_codeId;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getCodeName()
	 * =========================================================================
	 */
	@Override
	public String getCodeName()
	{
		return m_codeName;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getRecursionDepth()
	 * =========================================================================
	 */
	@Override
	public int getRecursionDepth()
	{
		return m_depth;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getCurrentLine()
	 * =========================================================================
	 */
	@Override
	public IExecutionTreeLine getCurrentLine()
	{
		return m_currentLine;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getLines()
	 * =========================================================================
	 */
	@Override
	public IExecutionTreeLine[] getLines()
	{
		return m_lines.values().toArray(new IExecutionTreeLine[0]);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getLine(int)
	 * =========================================================================
	 */
	@Override
	public synchronized IExecutionTreeLine getLine(int lineNo)
	{
		// Create a new line if it does not exists
		IExecutionTreeLine line = m_lines.get(lineNo);
		if (line == null)
		{
			line = new ExecutionTreeLine(this, lineNo);
			m_lines.put(lineNo, line);
		}
		return line;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getParentLine()
	 * ==================================
	 * ========================================
	 */
	@Override
	public IExecutionTreeLine getParentLine()
	{
		return m_parentLine;
	}

	@Override
	public void notifyLineChanged(int lineNumber, StackNotification data)
	{
		// Line number
		long sequence = data.getSequence();
		// Retrieve the line
		IExecutionTreeLine line = getLine(lineNumber);
		// Update current line
		m_currentLine = line;
		line.markExecuted(true);
		// Update execution range
		m_executionRanges.get(m_currentExecution).notifySequence(sequence);
	}

	@Override
	public void notifyLineChanged(int lineNumber)
	{
		// Retrieve the line
		IExecutionTreeLine line = getLine(lineNumber);
		// Update current line
		m_currentLine = line;
		line.markExecuted(true);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#notifyItem(int, ItemNotification)
	 * ================
	 * ==========================================================
	 */
	@Override
	public void notifyItem(int lineNumber, ItemNotification notification)
	{
		// Retrieve the line
		IExecutionTreeLine line = getLine(lineNumber);
		// Adds the notification
		long sequence = m_executionRanges.get(m_currentExecution).getLast();
		line.processNotification(notification, sequence);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#containsSequence(long)
	 * ============================
	 * ==============================================
	 */
	@Override
	public boolean containsSequence(long sequence)
	{
		for (ExecutionRange range : m_executionRanges.values())
		{
			if (range.isBetween(sequence))
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getLastSequenceForExecution(int)
	 * ==================
	 * ========================================================
	 */
	@Override
	public long getLastSequenceForExecution(int execution)
	{
		return m_executionRanges.get(execution).getLast();
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#reExecute(int, long)
	 * ==============================
	 * ============================================
	 */
	@Override
	public void reExecute(int execution, long sequence)
	{
		m_currentExecution = execution;
		m_executionRanges.put(execution, new ExecutionRange(sequence));
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeNode#getExecutions()
	 * ==================================
	 * ========================================
	 */
	@Override
	public Integer[] getExecutions()
	{
		return m_executionRanges.keySet().toArray(new Integer[0]);
	}

	public String toString()
	{
		if (m_parentLine != null)
		{
			return "[NODE " + m_codeId + ":" + m_parentLine.getLineNumber() + "]";
		}
		else
		{
			return "[NODE " + m_codeId + ":(top) ]";
		}
	}
}
