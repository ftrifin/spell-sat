////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionInfo.java
//
// DATE      : Sep 28, 2010 10:25:19 AM
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

import java.util.Collection;
import java.util.LinkedHashMap;

import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineSummaryData;

/*******************************************************************************
 * 
 * ExecutionInfo stores information about a line's single execution
 * 
 ******************************************************************************/
class ExecutionInfo
{

	/** Start sequence value */
	private ExecutionRange	                 m_range;
	/** Notifications */
	private LinkedHashMap<String, ILineData>	m_notifications;
	/** Execution summary */
	private ILineSummaryData	             m_summary;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param startSequence
	 **************************************************************************/
	public ExecutionInfo(long startSequence)
	{
		m_range = new ExecutionRange(startSequence);
		m_notifications = new LinkedHashMap<String, ILineData>();
	}

	/***************************************************************************
	 * Get the start sequence value
	 * 
	 * @return
	 **************************************************************************/
	public long getStartSequence()
	{
		return m_range.getFirst();
	}

	/***************************************************************************
	 * Get the end sequence value
	 * 
	 * @return
	 **************************************************************************/
	public long getEndSequence()
	{
		return m_range.getLast();
	}

	/***************************************************************************
	 * Store a received notification
	 * 
	 * @param name
	 * @param data
	 **************************************************************************/
	public void processNotification(String name, ILineData data)
	{
		m_range.notifySequence(data.getSequence());

		if (m_notifications.containsKey(name))
		{
			m_notifications.get(name).update(data);
		}
		else
		{
			m_notifications.put(name, data);
		}
		updateSummary();
	}

	/***************************************************************************
	 * Get the stored notifications
	 * 
	 * @return
	 **************************************************************************/
	public Collection<ILineData> getNotifications()
	{
		return m_notifications.values();
	}

	/***************************************************************************
	 * Return a {@link ILineSummaryData} for this execution
	 **************************************************************************/
	public ILineSummaryData getSummary()
	{
		return m_summary;
	}

	@Override
	public String toString()
	{
		return m_range.toString() + ":" + m_notifications.size();
	}

	/***************************************************************************
	 * Update summary object
	 **************************************************************************/
	private void updateSummary()
	{
		ILineSummaryData result = null;
		int successCount = 0;
		int totalCount = m_notifications.size();
		int numExecutions = 0;
		String name = "";
		String value = "";
		ItemStatus status = ItemStatus.UNKNOWN;
		String comments = "";
		String time = "";
		ItemStatus summaryStatus = ItemStatus.UNKNOWN;

		if (totalCount > 1)
		{
			/*
			 * Summary data will only provide information about the status
			 */
			for (ILineData data : m_notifications.values())
			{
				ItemStatus infoStatus = data.getStatus();
				if (infoStatus.ordinal() > summaryStatus.ordinal())
				{
					summaryStatus = infoStatus;
				}
				if (infoStatus.equals(ItemStatus.SUCCESS))
				{
					successCount++;
				}
			}
		}
		else if (totalCount == 1)
		{
			ILineData element = m_notifications.values().iterator().next();

			numExecutions = 0;
			/** TODO */
			name = element.getName();
			value = element.getValue();
			status = element.getStatus();
			time = element.getTime();
			comments = element.getComments();
			summaryStatus = status;
			successCount = element.getStatus().equals(ItemStatus.SUCCESS) ? 1
			        : 0;
		}
		result = new LineSummaryData(numExecutions, name, value, status,
		        comments, time, totalCount, successCount, summaryStatus);

		m_summary = result;
	}
}
