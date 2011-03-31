////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.tests.notifications.nodes
// 
// FILE      : TestNodeLastExecutionNotifications.java
//
// DATE      : Nov 10, 2010 5:45:49 PM
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
package com.astra.ses.spell.gui.procs.tests.notifications.nodes;

import java.util.Arrays;

import junit.framework.TestCase;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification.StackType;
import com.astra.ses.spell.gui.core.model.types.ItemType;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeNode;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;
import com.astra.ses.spell.gui.procs.model.ExecutionTreeLine;
import com.astra.ses.spell.gui.procs.model.ExecutionTreeNode;

/*******************************************************************************
 * 
 * {@link TestNodeLastExecutionNotifications} test how last execution
 * notifications are retrieved
 * @author jpizar
 *
 ******************************************************************************/
public class TestNodeLastExecutionNotifications extends TestCase
{
	
	/** Line used for retrieving notifications */
	private IExecutionTreeLine m_testLine;
	/** Target Node to test */
	private IExecutionTreeNode m_testNode;
	
	/***************************************************************************
	 * Constructor
	 * @param methodToTest
	 **************************************************************************/
	public TestNodeLastExecutionNotifications(String methodToTest)
	{
		super(methodToTest);
	}
	
	/***************************************************************************
	 * Test notifications retrieval when the last execution has been completed
	 * A {@link IExecutionTreeNode} containing three lines holding notifications
	 * is executed completely once, so when retrieving their notifications
	 * 3 notifications must be returned
	 **************************************************************************/
	public void testFinishedExecutionNotifications()
	{
		String codeId = "A";
		String codeName = "A";
		int stackSequence = 0;
		int itemSequence = 0;
		int execution = 0;
		/*
		 *  First execution
		 */
		// Line 1
		String stack = "A:1/" + execution;
		StackNotification stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		// Add some notifications to the current line
		ItemNotification itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(1, itemNotification);
		
		// Line 2
		stack = "A:2/" + execution;
		stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(2, itemNotification);
		
		// Line 3
		stack = "A:3/" + execution;
		stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(3, itemNotification);
		
		/*
		 * Test notifications for latest execution
		 */
		ILineData[] data = m_testLine.getNotifications(true, Long.MAX_VALUE);
		assertEquals("Latest execution should contain 3 notifications",
					 3,
					 data.length);
		
		/*
		 * Test ILineData sequence value
		 */
		Arrays.sort(data);
		assertEquals(0, data[0].getSequence());
		assertEquals(1, data[1].getSequence());
		assertEquals(2, data[2].getSequence());
	}
	
	/***************************************************************************
	 * Test notifications retrieval when the last execution has not finished.
	 * A {@link IExecutionTreeNode} containing three lines holding notifications
	 * but only two of them have been executed, so when retrieving their 
	 * notifications 2 notifications must be returned
	 **************************************************************************/
	public void testUnfinishedExecutionNotifications()
	{
		String codeId = "A";
		String codeName = "A";
		int stackSequence = 0;
		int itemSequence = 0;
		int execution = 0;
		/*
		 *  First execution
		 */
		// Line 1
		String stack = "A:1/" + execution;
		StackNotification stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		// Add some notifications to the current line
		ItemNotification itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(1, itemNotification);
		
		// Line 2
		stack = "A:2/" + execution;
		stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(2, itemNotification);
		
		// Line 3
		stack = "A:3/" + execution;
		stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		
		/*
		 * Test notifications for latest execution
		 */
		ILineData[] data = m_testLine.getNotifications(true, Long.MAX_VALUE);
		assertEquals("Latest execution should contain 2 notifications",
					 2,
					 data.length);
		
		/*
		 * Test ILineData sequence value
		 */
		Arrays.sort(data);
		assertEquals(0, data[0].getSequence());
		assertEquals(1, data[1].getSequence());
	}
	
	/***************************************************************************
	 * Test notifications retrieval when the node has been executed multiple
	 * times, and the last execution has not been completed
	 * A {@link IExecutionTreeNode} containing three lines holding notifications
	 * is execeuted twice, but only the first one is complete, while only two of
	 * the three lines at the third execution have been completed.
	 * So when retrieving their notifications 2 notifications must be returned
	 **************************************************************************/
	public void testUnfinishedReexecutionNotifications()
	{
		String codeId = "A";
		String codeName = "A";
		int stackSequence = 0;
		int itemSequence = 0;
		int execution = 0;
		/*
		 *  First execution
		 */
		// Line 1
		String stack = "A:1/" + execution;
		StackNotification stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		// Add some notifications to the current line
		ItemNotification itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(1, itemNotification);
		
		// Line 2
		stack = "A:2/" + execution;
		stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(2, itemNotification);
		
		// Line 3
		stack = "A:3/" + execution;
		stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(3, itemNotification);
		
		/*
		 *  Second execution
		 */
		execution++;
		m_testNode.reExecute(execution, stackSequence);
		// Line 1
		stack = "A:1/" + execution;
		stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		// Add some notifications to the current line
		itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(1, itemNotification);
		
		// Line 2
		stack = "A:2/" + execution;
		stackNotification = new StackNotification(StackType.LINE, codeId, stack, codeName);
		stackNotification.setSequence(stackSequence);
		stackSequence++;
		m_testNode.notifyLineChanged(stackNotification);
		itemNotification = new ItemNotification(codeId, ItemType.VALUE, stack);
		itemNotification.setItems("Name", "Value", "Status", "Comment", "Time");
		itemNotification.setSequence(itemSequence);
		itemSequence++;
		m_testNode.notifyItem(2, itemNotification);
		
		/*
		 * Test notifications for latest execution
		 */
		ILineData[] data = m_testLine.getNotifications(true, Long.MAX_VALUE);
		assertEquals("Latest execution should contain 2 notifications",
					 2,
					 data.length);
		
		/*
		 * Test ILineData sequence value
		 */
		Arrays.sort(data);
		assertEquals(3, data[0].getSequence());
		assertEquals(4, data[1].getSequence());
	}
	
	/*==========================================================================
	 * SETUP/TEAR DOWN SECTION
	 =========================================================================*/
	
	@Override
	public void setUp() throws Exception 
	{		
		m_testLine = new ExecutionTreeLine(null, 1);
		m_testNode = new ExecutionTreeNode("A","A", m_testLine, 0, 0, 0);
		m_testLine.setChild(m_testNode);
	}

	@Override
	public void tearDown() throws Exception 
	{	
		m_testNode = null;
		m_testLine = null;
	}
}