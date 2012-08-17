////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.tests.notifications.lines
// 
// FILE      : TestLineFilteredNotifications.java
//
// DATE      : Nov 10, 2010 5:46:18 PM
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
 * {@link TestLineFilteredNotifications} test the way single lines hold
 * notifications
 * This class will perform the following tests:
 *          
 *  A.1- Retrieve the filtered notifications for a given line
 *  
 *  
 * @author jpizar
 *
 ******************************************************************************/
public class TestLineFilteredNotifications extends TestCase 
{

	/** Procedure */
	private IExecutionTreeLine m_testLine;
	
	/***************************************************************************
	 * Constructor
	 * @param methodToTest
	 **************************************************************************/
	public TestLineFilteredNotifications(String methodToTest)
	{
		super(methodToTest);
	}
	
	/*==========================================================================
	 * TEST A SECTION
	 =========================================================================*/
	
	/***************************************************************************
	 * TEST A.1
	 * Execute the same line over the same node several times and check the
	 * notifications referred to the latest execution refer to the line's
	 * latest execution
	 **************************************************************************/
	public void testLineReexecutionNotifications()
	{
		int sequence = 0;
		
		/*
		 * Execution 0 : 3 items
		 */
		String[] notificationStacks = new String[]
		                              {
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
		
		int i = 0;
		for (String stack : notificationStacks)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, stack);
			item.setSequence(i);
			item.setItems(String.valueOf(i), "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, sequence);
			i++;
		}
		
		/*
		 * Apply the filter for the first execution
		 */
		ILineData[] data = m_testLine.getNotifications(true, 0);
		assertEquals("Line 1 should store 2 notifications for the first",
				4,
				data.length);
		
		data = m_testLine.getNotifications(false, 0);
		assertEquals("Line 1 should store 9 notifications for every line execution",
				9,
				data.length);
	}
	
	/***************************************************************************
	 * TEST A.2
	 * Test filtered notifications retrieval when the same node is reexecuted
	 * more than once
	 **************************************************************************/
	public void testNodeReexecutionNotifications()
	{
		int sequence = 0;
		
		/*
		 * Execution 0 : 3 items
		 */
		String[] notificationStacks = new String[]
		                              {
				"A:1:B:1/0",
				"A:1:B:1/0"
		                              };
		
		int i = 0;
		for (String stack : notificationStacks)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, stack);
			item.setSequence(i);
			item.setItems(String.valueOf(i), "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, sequence);
			i++;
		}
		sequence++;
		
		/*
		 * Execution 0 : 2 items
		 */
		notificationStacks = new String[]
		                              {
				"A:1:B:1/1",
				"A:1:B:1/1",
				"A:1:B:1/1"
		                              };
		
		for (String stack : notificationStacks)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, stack);
			item.setSequence(i);
			item.setItems(String.valueOf(i), "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, sequence);
			i++;
		}
		sequence++;
		
		/*
		 * Execution 2 : 4 items
		 */
		notificationStacks = new String[]
		                              {
				"A:1:B:1/2",
				"A:1:B:1/2",
				"A:1:B:1/2",
				"A:1:B:1/2"
		                              };
		
		i = 0;
		for (String stack : notificationStacks)
		{
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, stack);
			item.setSequence(i);
			item.setItems(String.valueOf(i), "VALUE_X", "STATUS_X", "COMMENTS_X", "TIME_X");
			m_testLine.processNotification(item, sequence);
			i++;
		}
		
		/*
		 * Step 1: Test how by changing the visible node, the latest notification
		 * retrieval is also different.
		 * To test this, the visible node shall be changed to the different 
		 * executions of the same node, and then retrieve the notifications
		 */
		/*
		 * Apply the filter for the first execution
		 */	
		ILineData[] data = m_testLine.getNotifications(true, 0);
		assertEquals("Line 1 should store 2 notifications for the first node execution",
				2,
				data.length);

		data = m_testLine.getNotifications(true, 1);
		assertEquals("Line 1 should store 3 notifications for the second node execution",
				3,
				data.length);
		
		data = m_testLine.getNotifications(true, 2);
		assertEquals("Line 1 should store 2 notifications for the third node execution",
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