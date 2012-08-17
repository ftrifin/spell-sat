////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.tests.notifications.lines
// 
// FILE      : TestLineNotificationsRetrieval.java
//
// DATE      : Nov 10, 2010 5:45:59 PM
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.tests.notifications.lines;

import junit.framework.TestCase;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.ItemType;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;
import com.astra.ses.spell.gui.procs.model.ExecutionTreeLine;

/*******************************************************************************
 * 
 * {@link TestLineNotificationsRetrieval} attempts to test that
 * notifications are retrieved correctly through the following tests:
 * 
 * 	A.- Retrieve all the notifications stored in a given line
 * 			A.1.- Several notifications in one single execution refers to the same item
 * 			A.2.- Several notification in several executions referred to the same item
 * 			A.2.- All the notifications refers to different items
 * 
 * @author jpizar
 *
 ******************************************************************************/
public class TestLineNotificationsRetrieval extends TestCase {

	/** Target line */
	private IExecutionTreeLine m_testLine;
	
	/***************************************************************************
	 * Constructor
	 * @param methodToTest
	 **************************************************************************/
	public TestLineNotificationsRetrieval(String methodToTest)
	{
		super(methodToTest);
	}
	
	/*==========================================================================
	 * 
	 * TEST A SECTION
	 * 
	 =========================================================================*/
	
	/***************************************************************************
	 * TEST A.1
	 * Send notification to a line which does not have a child node
	 * Notifications will refer always to the same item, so final notifications
	 * count should be 1
	 **************************************************************************/
	public void testSingleItemLineNotifications()
	{		
		//Send a bunch of ItemNotifications to this line referred to the same item
		int maxNotifications = 10;
		for (int i= 0; i < maxNotifications; i++)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, "A:1");
			item.setSequence(i);
			item.setItems("ITEM_X", "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, 0);
		}
		
		// Check receive notifications for that line are maxNotifications
		ILineData[] data = m_testLine.getNotifications(true, 1);
		assertEquals("Line 1 should store 1 notification",
				1,
				data.length);
	}
	
	/***************************************************************************
	 * TEST A.2
	 * Send notification to a line which does not have a child node
	 * Notifications will refer always to the same item, but for different
	 * executions, so number of notifications should be 2
	 **************************************************************************/
	public void testSingleItemMultipleExecutionLineNotifications()
	{		
		//Send a bunch of ItemNotifications to this line referred to the same item
		int maxNotifications = 10;
		for (int i= 0; i < maxNotifications; i++)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, "A:1/0");
			item.setSequence(i);
			item.setItems("ITEM_X", "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, 0);
		}
		
		for (int i= 0; i < maxNotifications; i++)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, "A:1/1");
			item.setSequence(i);
			item.setItems("ITEM_X", "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, 1);
		}
		
		// Check receive notifications for that line are maxNotifications
		ILineData[] data = m_testLine.getNotifications(false, 1);
		assertEquals("Line 1 should store 2 notifications",
				2,
				data.length);

		// Check receive notifications for that line are maxNotifications
		data = m_testLine.getNotifications(true, 1);
		assertEquals("Line 1 should store 1 notifications for the last execution",
				1,
				data.length);
	}
	
	/***************************************************************************
	 * TEST A.3
	 * Send notifications referred to different names at the same execution
	 * Notifications stored by the target line should be equals to the different
	 * names used
	 **************************************************************************/
	public void testMultipleItemsLineNotifications()
	{		
		//Send a bunch of ItemNotifications to this line referred to the same item
		int maxNotifications = 10;
		for (int i= 0; i < maxNotifications; i++)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, "A:1");
			item.setSequence(i);
			item.setItems("ITEM_X_"+i, "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, 0);
		}
		
		// Check receive notifications for that line are maxNotifications
		ILineData[] data = m_testLine.getNotifications(true, 1);
		assertEquals("Line 1 should store " + maxNotifications + " notifications",
				maxNotifications,
				data.length);
	}
	
	/*==========================================================================
	 * SETUP/TEAR DOWN SECTION
	 =========================================================================*/
	
	@Override
	public void setUp() throws Exception 
	{
		m_testLine = new ExecutionTreeLine(null, 1);
	}

	@Override
	public void tearDown() throws Exception 
	{
		m_testLine = null;
	}
}