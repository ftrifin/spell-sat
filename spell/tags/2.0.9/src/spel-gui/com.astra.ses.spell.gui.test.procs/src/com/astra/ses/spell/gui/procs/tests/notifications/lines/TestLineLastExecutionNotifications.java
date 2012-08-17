////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.tests.notifications.lines
// 
// FILE      : TestLineLastExecutionNotifications.java
//
// DATE      : Nov 10, 2010 5:46:10 PM
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

public class TestLineLastExecutionNotifications extends TestCase {

	/** Target line to test */
	private IExecutionTreeLine m_testLine;
	
	/***************************************************************************
	 * Constructor
	 * @param methodToTest
	 **************************************************************************/
	public TestLineLastExecutionNotifications(String methodToTest)
	{
		super(methodToTest);
	}
	
	/*==========================================================================
	 * TEST B SECTION
	 =========================================================================*/
	
	/***************************************************************************
	 * TEST B.1
	 * Retrieve the last execution notifications for a given line
	 * Notifications are received in execution increasing order
	 **************************************************************************/
	public void testLatestExecutionNotifications()
	{
		int sequence = 0;
		
		/*
		 * Execution 0 : 3 items
		 * Execution 1 : 3 items
		 * Execution 2 : 4 items
		 */
		String[] notificationStacks = new String[]
		                              {
				"A:1/0",
				"A:1/0",
				"A:1/0",
				"A:1/1",
				"A:1/1",
				"A:1/1",
				"A:1/2",
				"A:1/2",
				"A:1/2",
				"A:1/2"
		                              };
		
		/*
		 * Notify the items
		 */
		int i = 0;
		for (String stack : notificationStacks)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, stack);
			item.setItems(String.valueOf(i), "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, sequence);
			i++;
		}
		
		/*
		 * Check items for the last notification shall be 4
		 */
		ILineData[] items = m_testLine.getNotifications(true, 0);
		assertEquals("There should be 4 items for the last execution",
					 4,
					 items.length);
		
		/*
		 * Check items sequence
		 */
		assertEquals("6", items[0].getName());
		assertEquals("7", items[1].getName());
		assertEquals("8", items[2].getName());
		assertEquals("9", items[3].getName());
	}
	
	/***************************************************************************
	 * TEST B.2
	 * Tests the way notifications are stored.
	 * Notifications are stored per execution, so even if different
	 * notifications for different executions arrive at the same time, they all
	 * should be ordered
	 **************************************************************************/
	public void testAsynchronousNotificationsInDifferentExecutions()
	{
		/*
		 * Execution 0 : 6 executions
		 * Execution 1 : 7 executions
		 * Execution 2 : 4 executions
		 */
		String[] notificationStacks = new String[]
		                              {
				"A:1/0",
				"A:1/0",
				"A:1/0",
				"A:1/1",
				"A:1/1",
				"A:1/1",
				"A:1/0",
				"A:1/2",
				"A:1/2",
				"A:1/2",
				"A:1/1",
				"A:1/1",
				"A:1/1",
				"A:1/0",
				"A:1/1",
				"A:1/2",
				"A:1/0"
		                              };
		
		int sequence = 0;
		
		int i = 0;
		for (String stack : notificationStacks)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, stack);
			item.setItems(String.valueOf(i), "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, sequence);
			i++;
		}
		
		/*
		 * Check received notifications for that element are 
		 */
		ILineData[] data = m_testLine.getNotifications(false, 0);
		assertEquals("Line 1 should store " + notificationStacks.length +
				" notifications for the last execution",
				notificationStacks.length,
				data.length);
		
		// Check receive notifications for that line for the last execution are
		// 4
		data = m_testLine.getNotifications(true, 0);
		assertEquals("Line 1 should store 4 notifications for the last execution",
				4,
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
