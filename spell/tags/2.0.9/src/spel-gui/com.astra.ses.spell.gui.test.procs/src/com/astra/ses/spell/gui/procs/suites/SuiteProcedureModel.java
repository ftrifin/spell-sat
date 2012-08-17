////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.suites
// 
// FILE      : SuiteProcedureModel.java
//
// DATE      : Nov 10, 2010 5:17:53 PM
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
package com.astra.ses.spell.gui.procs.suites;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.astra.ses.spell.gui.procs.tests.TestProcedureStack;
import com.astra.ses.spell.gui.procs.tests.notifications.TestProcedureNotificationsOrder;
import com.astra.ses.spell.gui.procs.tests.notifications.lines.TestLineFilteredNotifications;
import com.astra.ses.spell.gui.procs.tests.notifications.lines.TestLineLastExecutionNotifications;
import com.astra.ses.spell.gui.procs.tests.notifications.lines.TestLineNotificationsRetrieval;
import com.astra.ses.spell.gui.procs.tests.notifications.nodes.TestNodeFilteredNotifications;
import com.astra.ses.spell.gui.procs.tests.notifications.nodes.TestNodeLastExecutionNotifications;
import com.astra.ses.spell.gui.procs.tests.notifications.nodes.TestNodeNotificationsRetrieval;

/*******************************************************************************
 * 
 * ProcedureModelTestSuite englobes all the test cases and suites which 
 * attempts to assess that the procedure model works correctly
 * @author jpizar
 *
 ******************************************************************************/
public class SuiteProcedureModel extends TestSuite
{
	
	/** Test suite name */
	private static final String TEST_SUITE_NAME = "Procedure model test suite";
	
	/***************************************************************************
	 * Return the main test to run
	 * @return
	 **************************************************************************/
	public static Test suite()
	{
		return new SuiteProcedureModel();
	}
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public SuiteProcedureModel()
	{
		super(TEST_SUITE_NAME);
		
		/*
		 * PROCEDURE STATUS TRANSITIONS TESTS
		 */
		//addTestSuite(TestProcedureStatus.class);
		
		/*
		 * PROCEDUR STACK HANDLING TESTS
		 */
		addTestSuite(TestProcedureStack.class);
		
		/*
		 * LINE NOTIFICATIONS HANDLING TESTS
		 */
		addTestSuite(TestLineNotificationsRetrieval.class);
		addTestSuite(TestLineLastExecutionNotifications.class);
		addTestSuite(TestProcedureNotificationsOrder.class);
		addTestSuite(TestLineFilteredNotifications.class);
		
		/*
		 * NODE NOTIFICATIONS HANDLING TESTS
		 */
		addTestSuite(TestNodeNotificationsRetrieval.class);
		addTestSuite(TestNodeLastExecutionNotifications.class);		
		addTestSuite(TestNodeFilteredNotifications.class);
	}
}