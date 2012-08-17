////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.tests.notifications
// 
// FILE      : TestProcedureNotificationsOrder.java
//
// DATE      : Nov 10, 2010 5:17:24 PM
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
package com.astra.ses.spell.gui.procs.tests.notifications;

import junit.framework.TestCase;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.ItemType;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;
import com.astra.ses.spell.gui.procs.model.ExecutionTreeLine;

public class TestProcedureNotificationsOrder extends TestCase 
{

	/** Target line */
	private IExecutionTreeLine m_testLine;
	
	/***************************************************************************
	 * Constructor
	 * @param methodToTest
	 **************************************************************************/
	public TestProcedureNotificationsOrder(String methodToTest)
	{
		super(methodToTest);
	}
	
	/*==========================================================================
	 * TEST D SECTION
	 =========================================================================*/
	
	/***************************************************************************
	 * TEST D
	 * Test that notifications are returned in the right order.
	 * The order notifications should be returned is according to the notified
	 * names. That is, is notified elements are A,B,C,D,E,A(2),E(2),B(2),D(2),A(3). 
	 * Returned elements should be four and they must be this way A(3),B(2),C,D(2),E(2)
	 * but they should keep the sequence the first element arrived that is 0,1,2,3,4
	 **************************************************************************/
	public void testNotificationsOrder()
	{
		String[] notifiedNames = new String[]
		                                    {
				"A", //sequence 0
				"B", //sequence 1
				"C", //sequence 2
				"D", //sequence 3
				"E", //sequence 4
				"A", //sequence 5
				"E", //sequence 6
				"B", //sequence 7
				"C", //sequence 8
				"A", //sequence 9
		                                    };
		
		int i = 0;
		for (String name : notifiedNames)
		{			
			ItemNotification item = new ItemNotification("X", ItemType.VALUE, "A:1/0");
			item.setItems(name, String.valueOf(i), "STATUS_X", "COMMENTS_X", "TIME_X");
			item.setSequence(i);
			m_testLine.processNotification(item, i);
			i++;
		}
		
		ILineData[] data = m_testLine.getNotifications(false, 0);
		assertEquals(0, data[0].getSequence() );
		assertEquals("9", data[0].getValue());

		assertEquals(1, data[1].getSequence() );
		assertEquals("7", data[1].getValue());

		assertEquals(2, data[2].getSequence() );
		assertEquals("8", data[2].getValue());

		assertEquals(3, data[3].getSequence() );
		assertEquals("3", data[3].getValue());

		assertEquals(4, data[4].getSequence() );
		assertEquals("6", data[4].getValue());
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
