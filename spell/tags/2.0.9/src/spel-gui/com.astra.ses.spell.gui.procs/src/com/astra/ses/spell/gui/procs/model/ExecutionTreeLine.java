////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionLine.java
//
// DATE      : 2010-07-30
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
import java.util.LinkedHashMap;
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
	private int	                                  m_lineNumber;
	/** Item notifications Collection */
	/*
	 * Each entry in the map will be String name : the item's name ILineData
	 * lineData : line data related to that object.
	 */
	/** Execution item notifications */
	private LinkedHashMap<Integer, ExecutionInfo>	m_executions;
	/** Parent node */
	private IExecutionTreeNode	                  m_parent;
	/** List of lines of the child code which are relevant for this line */
	private IExecutionTreeNode	                  m_child;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param lineNumber
	 *            the source line's number this object refers to
	 **************************************************************************/
	public ExecutionTreeLine(ExecutionTreeNode node, int lineNumber)
	{
		m_lineNumber = lineNumber;
		m_executions = new LinkedHashMap<Integer, ExecutionInfo>();
		m_parent = node;
		m_child = null;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeLine#getLineNumber()
	 * =========================================================================
	 */
	@Override
	public int getLineNumber()
	{
		return m_lineNumber;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine#
	 * getExecutions()
	 * =========================================================================
	 */
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
			executions = m_executions.keySet().toArray(new Integer[0]);
		}
		return executions;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeLine#getChildNode()
	 * =========================================================================
	 */
	@Override
	public IExecutionTreeNode getChildNode()
	{
		return m_child;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeLine#getExecutionNode()
	 * =========================================================================
	 */
	@Override
	public IExecutionTreeNode getExecutionNode()
	{
		return m_parent;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeLine#getNotifications(boolean, long)
	 * =========================================================================
	 */
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
			for (int execution : m_executions.keySet())
			{
				long startSequence = m_executions.get(execution)
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
					notifications.addAll(0, m_executions.get(targetExecution)
					        .getNotifications());
				}
				else
				{
					for (int execution : m_executions.keySet())
					{
						if (execution <= targetExecution)
						{
							notifications.addAll(m_executions.get(execution)
							        .getNotifications());
						}
					}
				}
			}
			result = notifications.toArray(new ILineData[0]);
		}
		return result;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeLine#getSummary(long)
	 * =========================================================================
	 */
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

			if (executions.length == 0) return null;

			int targetExecution = -1;

			for (int execution : executions)
			{
				long startSequence = m_executions.get(execution)
				        .getStartSequence();
				if (startSequence >= maxSequence)
				{
					break;
				}
				targetExecution = execution;
			}

			if (targetExecution != -1)
			{
				summary = m_executions.get(targetExecution).getSummary();
			}

		}

		return summary;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeLine#processNotification(ItemNotification, long)
	 * =========================================================================
	 */
	@Override
	public void processNotification(ItemNotification data, long sequence)
	{
		int execution = data.getNumExecutions();
		if (!m_executions.containsKey(execution))
		{
			m_executions.put(execution, new ExecutionInfo(sequence));
		}

		ExecutionInfo targetInfo = m_executions.get(execution);

		ArrayList<String> names = data.getItemName();
		ArrayList<String> values = data.getItemValue();
		ArrayList<String> status = data.getItemStatus();
		ArrayList<String> times = data.getTimes();
		ArrayList<String> comments = data.getComments();
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

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeLine#setChild(IExecutionTreeNode)
	 * =========================================================================
	 */
	@Override
	public void setChild(IExecutionTreeNode childNode)
	{
		m_child = childNode;
	}
}
