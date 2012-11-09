////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionTreeLine.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeNode;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineSummaryData;

/*******************************************************************************
 * 
 * ProcedureLine holds the execution trace of one of the source block's line
 * 
 ******************************************************************************/
public class ExecutionTreeLine implements IExecutionTreeLine
{
	/** The source line number this object refers to */
	private final int m_lineNumber;
	/** Item notifications Collection */
	/*
	 * Each entry in the map will be String name : the item's name ILineData
	 * lineData : line data related to that object.
	 */
	/** Execution item notifications */
	private LinkedHashMap<Integer, ExecutionInfo> m_executionData;
	/** Parent node */
	private IExecutionTreeNode m_parent;
	/** List of lines of the child code which are relevant for this line */
	private IExecutionTreeNode m_child;
	/** Executed flag */
	private boolean m_executed;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param lineNumber
	 *            the source line's number this object refers to
	 **************************************************************************/
	public ExecutionTreeLine(ExecutionTreeNode node, int lineNumber)
	{
		m_lineNumber = lineNumber;
		m_executionData = new LinkedHashMap<Integer, ExecutionInfo>();
		m_parent = node;
		m_executed = false;
		m_child = null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public int getLineNumber()
	{
		return m_lineNumber;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isExecuted()
	{
		return m_executed;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void markExecuted( boolean executed )
	{
		m_executed = executed;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Integer[] getExecutions()
	{
		Integer[] executions = null;
		if (m_child != null)
		{
			executions = m_child.getExecutions();
		}
		else
		{
			executions = m_executionData.keySet().toArray(new Integer[0]);
		}
		return executions;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutionTreeNode getChildExecutionNode()
	{
		return m_child;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutionTreeNode getParentExecutionNode()
	{
		return m_parent;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ILineData[] getNotifications(boolean latest, long maxSequence)
	{
		// First add notifications related to this line
		ILineData[] result = null;
		// If there is a child node, retrieve its notifications
		if (m_child != null)
		{
			Vector<ILineData> linesData = new Vector<ILineData>();
			for (IExecutionTreeLine line : m_child.getLines())
			{
				ILineData[] lineData = line.getNotifications(latest,
				        maxSequence);
				linesData.addAll(0, Arrays.asList(lineData));
			}
			result = linesData.toArray(new ILineData[0]);
			return result;
		}
		else
		{
			ArrayList<ILineData> notifications = new ArrayList<ILineData>();

			int targetExecution = -1;
			for (int execution : m_executionData.keySet())
			{
				long startSequence = m_executionData.get(execution)
				        .getStartSequence();
				if (startSequence > maxSequence)
				{
					break;
				}
				targetExecution = execution;
			}
			if (targetExecution != -1)
			{
				// In this case there is no function call (no child code), there
				// may
				// be just local notifications due to, e.g., a GetTM.
				if (latest)
				{
					notifications.addAll(0, m_executionData.get(targetExecution)
					        .getNotifications());
				}
				else
				{
					for (int execution : m_executionData.keySet())
					{
						if (execution <= targetExecution)
						{
							notifications.addAll(m_executionData.get(execution)
							        .getNotifications());
						}
					}
				}
			}
			result = notifications.toArray(new ILineData[0]);
		}
		return result;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ILineSummaryData getSummary(long maxSequence)
	{
		ILineSummaryData summary = null;

		/*
		 * If there is a child node, retrieve its notifications by iterating
		 * over the child's lines and retrieving their summaries
		 */
		if (m_child != null)
		{
			int itemsCount = 0;
			int successCount = 0;
			ItemStatus status = ItemStatus.UNKNOWN;

			for (IExecutionTreeLine line : m_child.getLines())
			{
				ILineSummaryData lineSummaryData = line.getSummary(maxSequence);
				if (lineSummaryData != null)
				{
					itemsCount += lineSummaryData.getElementCount();
					successCount += lineSummaryData.getSuccessCount();
					ItemStatus lineStatus = lineSummaryData.getSummaryStatus();
					status = (lineStatus.ordinal() > status.ordinal()) ? lineStatus
					        : status;
				}
			}

			summary = new LineSummaryData(0, "", "", status, "", "",
			        itemsCount, successCount, status);
		}
		/*
		 * If the line does not contain a child, look for the execution whose
		 * starting sequence value is closest to maxSequence without exceeding
		 * it and then get the ILineSummaryData object for that execution
		 */
		else
		{
			Integer[] executions = getExecutions();

			if (executions.length == 0) 
			{
				return null;
			}

			int targetExecution = -1;

			for (int execution : executions)
			{
				long startSequence = m_executionData.get(execution)
				        .getStartSequence();
				if (startSequence >= maxSequence)
				{
					break;
				}
				targetExecution = execution;
			}

			if (targetExecution != -1)
			{
				summary = m_executionData.get(targetExecution).getSummary();
			}

		}

		return summary;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void processNotification(ItemNotification data, long sequence)
	{
		int execution = data.getNumExecutions();
		if (!m_executionData.containsKey(execution))
		{
			m_executionData.put(execution, new ExecutionInfo(sequence));
		}

		ExecutionInfo targetInfo = m_executionData.get(execution);

		List<String> names = data.getItemName();
		List<String> values = data.getItemValue();
		List<String> status = data.getItemStatus();
		List<String> times = data.getTimes();
		List<String> comments = data.getComments();
		data.getNumExecutions();
		for (int i = 0; i < names.size(); i++)
		{
			String name = names.get(i);
			LineData lineData = new LineData(sequence, execution, name,
			        ItemStatus.fromName(status.get(i)), values.get(i),
			        comments.get(i), times.get(i));
			targetInfo.processNotification(name, lineData);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setChild(IExecutionTreeNode childNode)
	{
		m_child = childNode;
	}
	
	public String toString()
	{
		return "[LINE " + m_lineNumber + "] (Parent " + m_parent + ", Child " + m_child + ")";
	}
}
