////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.tests
// 
// FILE      : TestProcedureStack.java
//
// DATE      : Nov 10, 2010 5:17:35 PM
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
package com.astra.ses.spell.gui.procs.tests;

import java.util.Map;

import junit.framework.TestCase;

import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification.StackType;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification.UserActionStatus;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeController;
import com.astra.ses.spell.gui.procs.interfaces.model.priv.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.model.ExecutionTrace;
import com.astra.ses.spell.gui.procs.model.ExecutionTreeController;

/*******************************************************************************
 * 
 * {@link TestProcedureStack} tests the correct behaviour of 
 * {@link IExecutionTreeController}
 * @author jpizar
 *
 ******************************************************************************/
public class TestProcedureStack extends TestCase {

	/** Tree Controller */
	private IExecutionTreeController m_treeController;
	
	/***************************************************************************
	 * Constructor
	 * @param methodToTest
	 **************************************************************************/
	public TestProcedureStack(String methodToTest)
	{
		super(methodToTest);
	}
	
	/***************************************************************************
	 * Test that after the procedure has been created, the only stack
	 * notification allowed should be a call.
	 * After notifying a call, then a line, or a return can be notified
	 **************************************************************************/
	public void testInitialStackStatus()
	{
		StackNotification notification = null;
		
		try
		{
			notification = new StackNotification(StackType.LINE, "", "A:1", "Code_A");
			m_treeController.notifyProcedureStack(notification);
			fail("A NullPointer Exception should have been thrown");
		}
		catch (NullPointerException e)
		{
			// Test is OK
		}

		try
		{
			notification = new StackNotification(StackType.RETURN, "", "A:1", "Code_A");
			m_treeController.notifyProcedureStack(notification);
			fail("A NullPointer Exception should have been thrown");
		}
		catch (NullPointerException e)
		{
			// Test is OK
		}
		
		notification = new StackNotification(StackType.CALL, "", "A:1", "Code_A");
		m_treeController.notifyProcedureStack(notification);
		
		notification = new StackNotification(StackType.LINE, "", "A:2", "Code_A");
		m_treeController.notifyProcedureStack(notification);
	}
	
	/***************************************************************************
	 * Test messages received concurrently are stored and processed in the
	 * right order
	 * For this purposes, multiple threads will be created, each of them
	 * notifying a message to the procedure
	 **************************************************************************/
	public void testProcedureStack()
	{
		String[] stackNotifications = new String[]
		                                         {
				"CALL.A:1",
					"LINE.A:1",
					"LINE.A:2",
					"LINE.A:3",
					"LINE.A:4",
				"CALL.A:4:B:1",
					"LINE.A:4:B:2",
					"LINE.A:4:B:3",
					"LINE.A:4:B:4",
				"CALL.A:4:B:4:C:1",
					"LINE.A:4:B:4:C:2",
					"LINE.A:4:B:4:C:3",
					"LINE.A:4:B:4:C:4",
					"LINE.A:4:B:4:C:5",
					"LINE.A:4:B:4:C:6",
				"RETURN.A:4:B:4",
					"LINE.A:4:B:6",
					"LINE.A:4:B:7",
					"LINE.A:4:B:8",
					"LINE.A:4:B:9",
				"RETURN.A:4",
					"LINE.A:6",
				"CALL.A:6:D:1",
				"CALL.A:6:D:1:E:1",
				"CALL.A:6:D:1:E:1:F:1",
					"LINE.A:6:D:1:E:1:F:2",
				"RETURN.A:6:D:1:E:1",
					"LINE.A:6:D:1:E:3",
				"RETURN.A:6:D:1",
				"RETURN.A:6"
		                                         };
		
		for (String notification : stackNotifications)
		{
			/*
			 * Starts a new threads for every notification which notifies the
			 * procedure
			 */
			String[] splitted = notification.split("\\.");
			StackType type = StackType.valueOf(splitted[0]);
			StackNotification n = new StackNotification(type, "X", splitted[1], "X");
			m_treeController.notifyProcedureStack(n);
		}
		
		/*
		 * Check line should be the same
		 */
		try {
			int line = m_treeController.getCurrentLine();
			assertEquals(6, line);
		} catch (UninitProcedureException e) {
			fail("Procedure should have been initialized");
		}
		
		/*
		 * Check current node depth
		 * As we return to the initial node, depth should be 0
		 */
		int depth = m_treeController.getCurrentNode().getRecursionDepth();
		assertEquals(0, depth);
		
		/*
		 * Perform a call and check depth
		 */
		StackNotification n = new StackNotification(StackType.CALL, "X", "A:6:G:1", "X");
		m_treeController.notifyProcedureStack(n);
		depth = m_treeController.getCurrentNode().getRecursionDepth();
		assertEquals(1, depth);
	}
	
	/***************************************************************************
	 * Check current execution depth after a set of call/return events
	 **************************************************************************/
	public void testStackDepth()
	{
		String[] stackNotifications = new String[]
                                        {
					"CALL.A:1", 				// DEPTH 0
					"CALL.A:4:B:1",				// DEPTH 1
					"CALL.A:4:B:4:C:1",			// DEPTH 2
					"RETURN.A:4:B:4",			// DEPTH 1
					"RETURN.A:4",				// DEPTH 0
					"CALL.A:6:D:1",				// DEPTH 1
					"CALL.A:6:D:1:E:1",			// DEPTH 2
					"CALL.A:6:D:1:E:1:F:1"		// DEPTH 3
                                        };
		
		for (String notification : stackNotifications)
		{
			/*
			 * Starts a new threads for every notification which notifies the
			 * procedure
			 */
			String[] splitted = notification.split("\\.");
			StackType type = StackType.valueOf(splitted[0]);
			StackNotification sn = new StackNotification(type, "X", splitted[1], "X");
			m_treeController.notifyProcedureStack(sn);
		}
		
		int depth = m_treeController.getCurrentNode().getRecursionDepth();
		assertEquals(3, depth);
	}
	
	/*==========================================================================
	 * SET UP / TEAR DOWN SECTIONS
	 =========================================================================*/
	
	@Override
	public void setUp() throws Exception {	
		IExecutionInformationHandler info = new IExecutionInformationHandler() {
			
			@Override
			public void visit(ExecutorConfig cfg) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void visit(ExecutorInfo info) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isWaitingInput() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isVisible() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isStepByStep() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isShowLib() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isBlocking() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isAutomatic() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public UserActionStatus getUserActionStatus() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Severity getUserActionSeverity() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getUserAction() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public StepOverMode getStepOverMode() {
				return StepOverMode.STEP_INTO_ALWAYS;
			}
			
			@Override
			public ExecutorStatus getStatus() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getStageTitle() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getStageId() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getParent() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String[] getMonitoringClients() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getExecutionDelay() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public ErrorData getError() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public DisplayData[] getDisplayMessages() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getControllingClient() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Map<String, String> getConfigMap() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getCondition() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ClientMode getClientMode() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void setWaitingInput(boolean waiting) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setVisible(boolean visible) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setUserActionStatus(UserActionStatus status, Severity severity) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setStepOverMode(StepOverMode mode) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setStepByStep(boolean value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setStage(String id, String title) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setShowLib(boolean value) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setParent(String parentId) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setMonitoringClients(String[] clientKeys) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setExecutorStatus(ExecutorStatus status) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setExecutionDelay(int msec) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setError(ErrorData error) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setControllingClient(String clientKey) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCondition(String condition) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setClientMode(ClientMode mode) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBlocking(boolean blocking) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setAutomatic(boolean automatic) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void resetStepOverMode() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void displayMessage(DisplayData data) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void copyFrom(ExecutorConfig config) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void copyFrom(ExecutorInfo info) {
				// TODO Auto-generated method stub
				
			}
		};
		
		m_treeController = new ExecutionTreeController("A", info, new ExecutionTrace());
	}

	@Override
	public void tearDown() throws Exception {
		m_treeController = null;
	}
}