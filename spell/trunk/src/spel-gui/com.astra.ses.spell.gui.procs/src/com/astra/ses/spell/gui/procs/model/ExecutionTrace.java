////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionTrace.java
//
// DATE      : Aug 17, 2010 3:19:34 PM
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
// SUBPROJECT: SPELL GUI Client
//
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTrace;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineSummaryData;

/*******************************************************************************
 * 
 * ExecutionTrace implements {@link IExecutionTrace}. It tracks the procedure
 * execution in terms of executed lines, and received notifications relative to
 * those lines
 * 
 ******************************************************************************/
public class ExecutionTrace implements IExecutionTrace
{

	/***************************************************************************
	 * 
	 * CodeExecutionTrace holds the trace for a single code id
	 * 
	 **************************************************************************/
	private class CodeExecutionTrace
	{
		/**
		 * Line notifications Keys in this map refers to the different line
		 * numbers The values are vectors containing {@link IExecutionTreeLine}
		 * instances Whenever a line number is executed, the
		 * {@link IExecutionTreeLine} that did it is appended to this vector.
		 * Whenever an {@link IExecutionTreeLine} is reexecuted, it is moved to
		 * the last position in the vector
		 */
		private Map<Integer, Vector<IExecutionTreeLine>>	m_lineNotifications;

		/***********************************************************************
		 * Constructor
		 **********************************************************************/
		public CodeExecutionTrace()
		{
			m_lineNotifications = new HashMap<Integer, Vector<IExecutionTreeLine>>();
		}

		/***********************************************************************
		 * Check if the given line has been executed
		 * 
		 * @param line
		 *            the line to check
		 * @return <code>true</code> if the line has been executed.
		 *         <code>false</code> otherwise
		 **********************************************************************/
		public boolean isExecuted(int line)
		{
			return m_lineNotifications.containsKey(line);
		}

		/***********************************************************************
		 * Get the notifications for the given line
		 * 
		 * @param lineNumber
		 *            the line number
		 * @param latest
		 *            true if the latest notifications shall be obtained
		 * @return an Array of {@link ILineData} each of them containing a
		 *         notifications
		 **********************************************************************/
		public ILineData[] getNotifications(int lineNumber, boolean latest)
		{
			ILineData[] data = null;

			/*
			 * Notifications are available only if the line has been executed
			 * Otherwise an empty array is returned
			 */
			if (!isExecuted(lineNumber)) { return new ILineData[0]; }

			IExecutionTreeLine line = null;
			Vector<IExecutionTreeLine> lines = m_lineNotifications
			        .get(lineNumber);

			int targetIndex = lines.size() - 1;
			if (m_maxSequenceFilter != Long.MAX_VALUE)
			{
				/*
				 * If the sequence filter value is different from its default
				 * value, there is need to search for the line whose starting
				 * sequence is lower than the filter reference value
				 */
				for (int i = lines.size() - 1; i >= 0; i--)
				{
					line = lines.get(i);
					targetIndex = i;
					if (line.getExecutionNode().containsSequence(
					        m_maxSequenceFilter))
					{
						break;
					}
				}
			}

			/*
			 * Once the target line index has been found, there is need to
			 * retrieve its notifications.
			 */
			if (latest)
			{
				line = lines.get(targetIndex);
				data = line.getNotifications(latest, m_maxSequenceFilter);
			}
			else
			{
				ArrayList<ILineData> notifications = new ArrayList<ILineData>();
				while (targetIndex >= 0)
				{
					line = lines.get(targetIndex);
					ILineData[] n = line.getNotifications(latest,
					        m_maxSequenceFilter);
					notifications.addAll(Arrays.asList(n));
					targetIndex--;
				}
				data = new ILineData[notifications.size()];
				notifications.toArray(data);
			}

			/*
			 * Sort the notifications and return
			 */
			Arrays.sort(data);
			return data;
		}

		/***********************************************************************
		 * Get the summary item for the given line
		 * 
		 * @param lineNumber
		 *            the line number
		 * @return an {@link ILineData} object containing the summary
		 **********************************************************************/
		public ILineSummaryData getSummary(int lineNumber)
		{
			ILineSummaryData data = null;

			/*
			 * Notifications are available only if the line has been executed
			 * Otherwise an empty array is returned
			 */
			if (!isExecuted(lineNumber)) { return null; }

			/*
			 * Determine the line whose starting sequence value is the most
			 * approximate to the sequence value without exceeding it
			 */
			IExecutionTreeLine line = null;
			Vector<IExecutionTreeLine> lines = m_lineNotifications
			        .get(lineNumber);

			if (m_maxSequenceFilter == Long.MAX_VALUE)
			{
				line = lines.lastElement();
			}
			else
			{
				for (int i = lines.size() - 1; i >= 0; i--)
				{
					line = lines.get(i);
					if (line.getExecutionNode().containsSequence(
					        m_maxSequenceFilter))
					{
						break;
					}
				}
			}

			/*
			 * Get the summary for this line
			 */
			data = line.getSummary(m_maxSequenceFilter);

			return data;
		}

		/***********************************************************************
		 * A notification for the given line has arrived from the server
		 * 
		 * @param line
		 *            the line related to the notification
		 * @param node
		 *            the node which receives the notification
		 **********************************************************************/
		public void notifyItem(IExecutionTreeLine line)
		{
			int lineNo = line.getLineNumber();
			Vector<IExecutionTreeLine> list = null;
			if (!m_lineNotifications.containsKey(lineNo))
			{
				list = new Vector<IExecutionTreeLine>(0, 1);
				m_lineNotifications.put(lineNo, list);
			}
			else
			{
				list = m_lineNotifications.get(lineNo);
			}
			/*
			 * Append the line which has been notified If it was previously
			 * contained in the vector, remove it
			 */
			list.remove(line);
			list.add(line);
		}

		/***********************************************************************
		 * Get the times a line as been executed
		 * 
		 * @param lineNumber
		 *            the line number
		 * @return the times the line has been executed
		 **********************************************************************/
		public int getExecutionCount(int lineNumber)
		{
			int result = 0;
			if (isExecuted(lineNumber))
			{
				IExecutionTreeLine line = null;
				Vector<IExecutionTreeLine> lines = m_lineNotifications
				        .get(lineNumber);

				if (m_maxSequenceFilter == Long.MAX_VALUE)
				{
					line = lines.lastElement();
				}
				else
				{
					for (int i = lines.size() - 1; i >= 0; i--)
					{
						line = lines.get(i);
						if (line.getExecutionNode().containsSequence(
						        m_maxSequenceFilter))
						{
							break;
						}
					}
				}

				Integer[] executions = line.getExecutions();
				result = executions[executions.length - 1] + 1;
			}
			return result;
		}
	}

	/**
	 * Execution code traces The key in the map refers to a source code id The
	 * value is a {@link CodeExecutionTrace} object For accessing to a line
	 * execution trace, there is need to provide the source code id as well as
	 * the line number
	 */
	private Map<String, CodeExecutionTrace>	m_codeTraces;

	/**
	 * Max sequence filter value This is the reference value for filtering
	 * purposes Default value is the Maximum value that can be held, meaning
	 * that every stored notification will pass the filter
	 */
	private long	                        m_maxSequenceFilter;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ExecutionTrace()
	{
		m_codeTraces = new HashMap<String, CodeExecutionTrace>();
		m_maxSequenceFilter = Long.MAX_VALUE;
	}

	@Override
	public ILineData[] getNotifications(String codeId, int lineNumber,
	        boolean latest)
	{
		if (m_codeTraces.containsKey(codeId)) { return m_codeTraces.get(codeId)
		        .getNotifications(lineNumber, latest); }
		return new ILineData[0];
	}

	@Override
	public ILineSummaryData getSummary(String codeId, int lineNumber)
	{
		if (m_codeTraces.containsKey(codeId)) { return m_codeTraces.get(codeId)
		        .getSummary(lineNumber); }
		return null;
	}

	@Override
	public void notifyItem(IExecutionTreeLine line)
	{
		String codeId = line.getExecutionNode().getCodeId();
		CodeExecutionTrace trace = null;
		if (!m_codeTraces.containsKey(codeId))
		{
			trace = new CodeExecutionTrace();
			m_codeTraces.put(codeId, trace);
		}
		else
		{
			trace = m_codeTraces.get(codeId);
		}
		trace.notifyItem(line);
	}

	@Override
	public void reset()
	{
		m_codeTraces.clear();
	}

	@Override
	public void resetSequenceFilter()
	{
		m_maxSequenceFilter = Long.MAX_VALUE;
	}

	@Override
	public void setSequenceFilter(long maxSequence)
	{
		m_maxSequenceFilter = maxSequence;
	}

	@Override
	public int getExecutionCount(String codeId, int lineNumber)
	{
		if (m_codeTraces.containsKey(codeId)) { return m_codeTraces.get(codeId)
		        .getExecutionCount(lineNumber); }
		return 0;
	}
}
