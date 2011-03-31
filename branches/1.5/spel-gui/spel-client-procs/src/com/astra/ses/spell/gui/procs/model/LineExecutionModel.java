///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : LineExecutionModel.java
//
// DATE      : 2008-11-24 08:34
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;


/*******************************************************************************
 * Holds the execution information model of a procedure line.
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class LineExecutionModel
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the line number */
	private int m_lineNo;
	/** Holds the ordered identifiers */
	private ArrayList<String> m_stackIds;
	/** Holds the notifications for this line */
	private Map<String,ItemNotification> m_notifications;
	/** Holds the reference to the parent line */
	private ProcedureLine m_parent;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INNER CLASSES
	// =========================================================================

	/***************************************************************************
	 * Item information structure
	 **************************************************************************/
	public class ItemInfo implements Comparable<ItemInfo>
	{
		public int		  execution;
		public String	  id;
		public String 	  name;
		public String 	  value;
		public String     status;
		public String 	  reason;
		public String 	  time;
		public String 	  stack;
		
		public String toString()
		{
			return "[" + name + "(" + id + ")=" + value + "|" + status + "]";
		}

		@Override
		public int compareTo(ItemInfo arg0)
		{
			ItemInfo arg = (ItemInfo) arg0;
			if (arg.execution == execution)
			{
				return id.compareTo(arg.id);
			}
			else if (arg.execution > execution)
			{
				return -1;
			}
			return 1;
		}
	}

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public LineExecutionModel( ProcedureLine line )
	{
		m_lineNo = line.getLineNum();
		m_notifications = null;
		m_stackIds = null;
		m_parent = line;
	}
	
	/***************************************************************************
	 * Add a notification regarding this line
	 * @param data ItemType data
	 **************************************************************************/
	public boolean addNotification( ItemNotification data )
	{
		boolean changed = false;
		
		Vector<String> elements = data.getStackPosition();
		int lineNo = Integer.parseInt(elements.lastElement());
		if (lineNo != m_lineNo)
		{
			Logger.error("[-"+ m_lineNo + "-] Unmatched line number: " + lineNo + "<>" + m_lineNo, Level.PROC, this);
			return false;
		}
		
		if (m_stackIds == null) m_stackIds = new ArrayList<String>();
		if (m_notifications == null) m_notifications = new HashMap<String,ItemNotification>();

		String stackID = getStackID( data.getStackPosition() );
		//data.dump();
		if (m_stackIds.contains(stackID))
		{
			// Update the notification
			ItemNotification oldData = m_notifications.get(stackID);
			ArrayList<String> oldNames = oldData.getItemName();
			ArrayList<String> oldValues = oldData.getItemValue();
			ArrayList<String> oldStatus = oldData.getItemStatus();
			ArrayList<String> oldComments = oldData.getComments();
			ArrayList<String> oldTimes = oldData.getTimes();
			ArrayList<String> newNames = data.getItemName();
			ArrayList<String> newValues = data.getItemValue();
			ArrayList<String> newStatus = data.getItemStatus();
			ArrayList<String> newComments = data.getComments();
			ArrayList<String> newTimes = data.getTimes();
			for(String name : newNames)
			{
				if (oldNames.contains(name))
				{
					int oldIdx = oldNames.indexOf(name);
					int newIdx = newNames.indexOf(name);
					
					oldValues.remove(oldIdx);
					oldValues.add(oldIdx, newValues.get(newIdx));
					
					oldStatus.remove(oldIdx);
					oldStatus.add(oldIdx, newStatus.get(newIdx));
					
					oldComments.remove(oldIdx);
					oldComments.add(oldIdx, newComments.get(newIdx));

					oldTimes.remove(oldIdx);
					oldTimes.add(oldIdx, newTimes.get(newIdx));
				}
				else
				{
					oldNames.add(name);
					int newIdx = newNames.indexOf(name);
					oldValues.add(newValues.get(newIdx));
					oldStatus.add(newStatus.get(newIdx));
					oldComments.add(newComments.get(newIdx));
					oldTimes.add(newTimes.get(newIdx));
				}
			}
			data.setItems(oldNames, oldValues, oldStatus, oldComments, oldTimes);
			// Recalculate the number of ok
			int numOk = 0;
			for(String st : data.getItemStatus())
			{
				if (st.equals(ItemStatus.SUCCESS.getName())) numOk++;
			}
			data.setOkItems(numOk);
		}
		else
		{
			m_stackIds.add(stackID);
		}
		// Add the notification
		m_notifications.put(stackID,data);
		return changed;
	}

	/***************************************************************************
	 * Reset the notification information
	 **************************************************************************/
	public void clear()
	{
		if (m_notifications != null)
		{
			m_notifications.clear();
		}
	}
	
	/***************************************************************************
	 * Obtain number elements
	 **************************************************************************/
	public int getElementCount( boolean onlyLatest )
	{
		int numItems = 0;
		if (m_notifications!=null)
		{
			if (onlyLatest)
			{
				String stackID = m_stackIds.get(m_stackIds.size() - 1);
				numItems += m_notifications.get(stackID).getTotaltems();
			}
			else
			{
				for(String stackID : m_stackIds)
				{
					numItems += m_notifications.get(stackID).getTotaltems();
				}
			}
		}
		return numItems;
	}

	/***************************************************************************
	 * Obtain number elements
	 **************************************************************************/
	public int getSuccessCount( boolean onlyLatest )
	{
		int numDone = 0;
		if (m_notifications!=null)
		{
			if (onlyLatest)
			{
				String stackID = m_stackIds.get(m_stackIds.size() - 1);
				numDone += m_notifications.get(stackID).getOkItems();
			}
			else
			{
				for(String stackID : m_stackIds)
				{
					numDone += m_notifications.get(stackID).getOkItems();
				}
			}
		}
		return numDone;
	}
	
	
	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	
	/***************************************************************************
	 * Put all notifications together
	 **************************************************************************/
	public Vector<ItemInfo> getViewItemData(boolean onlyLatest)
	{
		Vector<ItemInfo> rows = new Vector<ItemInfo>();
		if (m_notifications!=null)
		{
			if (onlyLatest)
			{
				String stackID = m_stackIds.get(m_stackIds.size() - 1);
				rows = getItemInfo(stackID);
			}
			else // SHOW ALL
			{
				for(String stackID : m_stackIds)
				{
					Vector<ItemInfo> stackInfos = getItemInfo(stackID);
					rows.addAll(stackInfos);
				}
			}
		}
		if (rows.size()==0) return null;
		return rows;
	}
	
	/**************************************************************************
	 * Return the ItemInfo object for the given stack ID
	 * @param stackID
	 **************************************************************************/
	public Vector<ItemInfo> getItemInfo(String stackID)
	{
		int round = Integer.parseInt(stackID.split("-")[0]);
		Vector<ItemInfo> rows = new Vector<ItemInfo>();
		ItemNotification data = m_notifications.get(stackID);
		ArrayList<String> names    = data.getItemName();
		ArrayList<String> values   = data.getItemValue();
		ArrayList<String> status   = data.getItemStatus();
		ArrayList<String> comments = data.getComments();
		ArrayList<String> times    = data.getTimes();
		int count = 0;
		for(String name : names)
		{
			ItemInfo info = new ItemInfo();
			Vector<String> stack = data.getStackPosition();
			String procs = stack.elementAt(stack.size()-2);
			procs += ":" + stack.lastElement();
			info.id = stackID + ">" + count;
			info.execution = round;
			info.stack = procs;
			
			int idx = name.indexOf("@");
			info.name = name.substring(idx+1);
			if (comments.size()>count)
			{
				info.reason = comments.get(count);
			}
			else
			{
				info.reason = "";
			}
			info.status = status.get(count);
			info.time = times.get(count);
			info.value = values.get(count);
			
			rows.add(info);
			count++;
		}
		return rows;
	}

	/***************************************************************************
	 * Obtain a unique stack identifier
	 **************************************************************************/
	private String getStackID( Vector<String> stack )
	{
		String stackID = Integer.toString(m_parent.getNumVisits()) + "-";
		for(String elem : stack) stackID += ":" + elem;
		return stackID;
	}
}
