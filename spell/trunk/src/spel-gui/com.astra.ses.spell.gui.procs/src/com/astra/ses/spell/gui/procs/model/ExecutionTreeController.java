////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionTreeController.java
//
// DATE      : 2010-08-16
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IStackChangesListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation.StepOverMode;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTrace;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeController;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeNode;
import com.astra.ses.spell.gui.procs.interfaces.model.priv.IExecutionInformationHandler;

/*******************************************************************************
 * 
 * ProcedureRuntimeManager deals with notifications received from the
 * ProcedureManager
 * 
 ******************************************************************************/
public class ExecutionTreeController implements IExecutionTreeController
{
	private static final int MAX_NOTIFICATION_FREQ = 250;

	/** Runtime model */
	private IExecutionInformation m_executionInfo;
	/** Execution trace */
	private IExecutionTrace m_trace;
	/** Root node */
	private IExecutionTreeNode m_rootNode;
	/** View node */
	private IExecutionTreeNode m_viewNode;
	/** Execution node */
	private IExecutionTreeNode m_executionNode;

	/** On demand visible node */
	private IExecutionTreeNode m_analysisNode;
	/** Routine execution listener */
	private Collection<IStackChangesListener> m_listeners;
	/** Root code identifier */
	private String m_rootCodeId;
	/** Wait condition for sequence ordering */
	private Lock m_notifyLock;
	/** Used to know when are we doing AsRun replay */
	private boolean m_doingReplay;
	/** Holds last time of graphical notification */
	private long m_lastGUInotif = -1;
	
	/****************************************/
	/** Line notifications synchornization **/
	/****************************************/
	
	/** The line notifications are asynchronous. In order to guarantee the correct display of line information
	 * to the users, the asynchronous messages shall be ordered by notification sequence (see NotificationData.getSequence)
	 * To do so, we use a prioritized queue that guarantees the ordering by Comparable, and a processing thread
	 * to poll notifications from it. Note that this mechanism is NOT USED when in replay mode (ASRUN).
	 * 
	 *  It is important to note that the code model shall be notified about its removal in order to shutdown the
	 *  processing thread.
	 */
	
	/** Processing thread for line notifications. */
	private ProcessingThread m_processingThread;
	/** Holds the line events in order of sequence */
	private PriorityQueue<StackNotification> m_arrivedNotifications;
	/** Used to control the processing thread */
	private AtomicBoolean m_processing = new AtomicBoolean(true);
	
	/**
	 * Processing thread for the async line notifications
	 *
	 */
	private class ProcessingThread extends Thread
	{
		public void run()
		{
			// Perform retrieval of notifications continuously
			while(m_processing.get())
			{
				try
				{
					StackNotification data = getNotification();
					// Trigger the line event if there is data available
					if (data != null)
					{
						processLineEvent(data);
					}
					// Otherwise wait a bit
					else
					{
						Thread.sleep(100);
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param proc
	 *            the Procedure instance this Object will receive notifications
	 *            for
	 **************************************************************************/
	public ExecutionTreeController(String initialCodeId, IExecutionInformation execInfo, IExecutionTrace trace)
	{
		m_executionInfo = execInfo;
		m_trace = trace;
		m_viewNode = null;
		m_executionNode = null;
		m_rootNode = null;
		m_listeners = new ArrayList<IStackChangesListener>();
		m_rootCodeId = initialCodeId;
		m_notifyLock = new ReentrantLock();
		m_doingReplay = false;

		m_arrivedNotifications = new PriorityQueue<StackNotification>();

		m_processingThread = new ProcessingThread();
		m_processingThread.start();
		
	}

	@Override
	public int getCurrentLine() throws UninitProcedureException
	{
		IExecutionTreeNode node = (m_analysisNode != null) ? m_analysisNode : m_viewNode;
		if (node == null)
			throw new UninitProcedureException();
		return node.getCurrentLine().getLineNumber();
	}

	@Override
	public boolean isExecuted( int lineNo ) throws UninitProcedureException
	{
		IExecutionTreeNode node = (m_analysisNode != null) ? m_analysisNode : m_viewNode;
		if (node == null) throw new UninitProcedureException();
		return node.getLine(lineNo).isExecuted();
	}

	@Override
	public String getCurrentCodeId() throws UninitProcedureException
	{
		IExecutionTreeNode node = (m_analysisNode != null) ? m_analysisNode : m_viewNode;
		if (node == null)
			throw new UninitProcedureException();
		return node.getCodeId();
	}

	@Override
	public String getRootCodeId()
	{
		return m_rootCodeId;
	}

	@Override
	public void addStackChangesListener(IStackChangesListener listener)
	{
		m_listeners.add(listener);
		listener.viewChanged(true);
	}

	@Override
	public void removeStackChangesListener(IStackChangesListener listener)
	{
		m_listeners.remove(listener);
	}

	@Override
	public IExecutionTreeNode getCurrentNode()
	{
		return m_viewNode;
	}

	@Override
	public IExecutionTreeNode getRootNode()
	{
		return m_rootNode;
	}

	/***************************************************************************
	 * Process item information events to be executed
	 * 
	 * @param procId
	 **************************************************************************/
	@Override
	public synchronized void notifyProcedureItem(ItemNotification data)
	{
		// Retrieve line number
		List<String> stack = data.getStackPosition();

		String rootNodeName = stack.get(0);
		int lineNumber = Integer.parseInt(stack.get(stack.size()-1));
		
		if (rootNodeName.equals(m_executionNode.getCodeId()))
		{
			// Add the notification
			m_executionNode.notifyItem(lineNumber, data);
			/*
			 * Every line in the stack shall notify an item
			 */
			IExecutionTreeLine line = m_executionNode.getLine(lineNumber);
			while (line != null)
			{
				m_trace.notifyItem(line);
				line = line.getParentExecutionNode().getParentLine();
			}
			
			// Update the currently executed line
			if (m_executionNode == m_viewNode)
			{
				m_viewNode.notifyLineChanged(lineNumber);
			}
		}
		// If the notification corresponds to a path different from the current one
		// we need to search for the node
		else
		{
			IExecutionTreeNode node = searchNode( stack, m_rootNode );
			IExecutionTreeLine line = node.getLine(lineNumber);
			node.notifyItem(lineNumber, data);

			// Update the currently executed line
			if (m_executionNode == m_viewNode)
			{
				m_viewNode.notifyLineChanged(lineNumber);
			}

			while (line != null)
			{
				m_trace.notifyItem(line);
				line = line.getParentExecutionNode().getParentLine();
			}
		}
	}
	
	private IExecutionTreeNode searchNode( List<String> stack, IExecutionTreeNode root )
	{
		String nodeToSearch = stack.get(0);
		if (root.getCodeId().equals(nodeToSearch)) return root;
		
		int lineOnNode = Integer.parseInt(stack.get(1));
		
		IExecutionTreeLine line = root.getLine(lineOnNode);
		
		if (line == null) return null;
		
		return searchNode(stack.subList(2, stack.size()), line.getChildExecutionNode() );
	}

	/***************************************************************************
	 * Process stack events to be executed
	 * 
	 * @param procId
	 **************************************************************************/
	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		// Process the notification in the execution tree
		switch (data.getStackType())
		{
		case LINE:
			if (isInReplayMode())
			{
				processLineEvent(data);
			}
			else
			{
				placeNotification(data);
			}
			break;
		case CALL:
			processCallEvent(data);
			break;
		case RETURN:
			processReturnEvent(data);
			break;
		default:
		}
	}

	/***************************************************************************
	 * Notify the current line changed to be executed
	 * 
	 * @param procId
	 **************************************************************************/
	private void processLineEvent(StackNotification data)
	{
		List<String> stack = data.getStackPosition();
		if (stack.size()<2) return;
		String headNodeName = stack.get(stack.size()-2);
		int lineNumber = Integer.parseInt(stack.get(stack.size()-1));

		IExecutionTreeNode node = m_executionNode;
		
		// If we are in the wrong position, switch there first
		if (!headNodeName.equals(node.getCodeId()))
		{
			node = searchNode(stack,m_rootNode);
		}

		if (node == m_viewNode)
		{
			((IExecutionInformationHandler) m_executionInfo).resetStepOverMode();
			// Update the currently executed line
			node.notifyLineChanged(lineNumber,data);
		}

		if ((System.currentTimeMillis() - m_lastGUInotif) > MAX_NOTIFICATION_FREQ)
		{
			Logger.debug("Broadcast line: " + Arrays.toString(data.getStackPosition().toArray()), Level.PROC, this);
			for (IStackChangesListener listener : m_listeners)
			{
				listener.lineChanged(lineNumber);
			}
		}
		m_lastGUInotif = System.currentTimeMillis();
	}

	/***************************************************************************
	 * Notify that a function or a function in another procedure file is about
	 * to be executed
	 * 
	 * @param procId
	 **************************************************************************/
	private void processCallEvent(StackNotification data)
	{
		List<String> stack = data.getStackPosition();
		int execution = data.getNumExecutions();
		long sequence = data.getSequence();
		if (m_rootNode == null)
		{
			// If this is the first call, set the root node
			String codeId = stack.get(0);
			m_rootNode = new ExecutionTreeNode(codeId, codeId, null, 0, execution, sequence);
			m_executionNode = m_rootNode;
		}
		else
		{
			String headNodeName = stack.get(stack.size()-2);
			
			// If we are in the wrong position, switch there first
			if (!headNodeName.equals(m_executionNode.getCodeId()))
			{
				m_executionNode = searchNode(stack,m_rootNode);
			}
			
			int lineNumber = Integer.parseInt(stack.get(stack.size()-3));
			IExecutionTreeLine parentLine = m_executionNode.getLine(lineNumber);
			if (parentLine.getChildExecutionNode() == null)
			{
				m_executionNode = new ExecutionTreeNode(headNodeName, data.getCodeName(), parentLine, m_executionNode.getRecursionDepth() + 1,
				        execution, sequence);
				parentLine.setChild(m_executionNode);
			}
			else
			{
				m_executionNode = parentLine.getChildExecutionNode();
				m_executionNode.reExecute(execution, sequence);
			}
		}

		// Assign the current line in the newly created node
		int lineNumber = Integer.parseInt(stack.get(stack.size()-1));
		m_executionNode.notifyLineChanged(lineNumber,data);

		// Update view node
		updateViewNode();
	}

	/***************************************************************************
	 * Notify that the current node execution has finished, so there is need to
	 * return to its parent node execution scope
	 **************************************************************************/
	private void processReturnEvent(StackNotification data)
	{
		IExecutionTreeLine currentLine = m_executionNode.getParentLine();
		// It can be null if we are processing the return event of the
		// head code
		if (currentLine != null)
		{
			m_executionNode = currentLine.getParentExecutionNode();
			updateViewNode();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Integer[] getCodeStackLines(String codeId, int lineNumber)
	{
		ArrayList<Integer> temp = new ArrayList<Integer>();
		// Get the model length
		IExecutionTreeNode currentNode = null;
		IExecutionTreeLine currentLine = null;
		if (m_executionNode != null)
		{
			currentLine = m_executionNode.getLine(lineNumber);
		}
		while (currentLine != null)
		{
			currentNode = currentLine.getParentExecutionNode();
			if (currentNode.getCodeId().equals(codeId))
			{
				temp.add(currentLine.getLineNumber());
			}
			currentLine = currentNode.getParentLine();
		}
		return temp.toArray(new Integer[0]);
	}

	@Override
	/***************************************************************************
	 * NOTE: Listeners are not removed
	 **************************************************************************/
	public void reset()
	{
		m_analysisNode = null;
		m_viewNode = null;
		m_executionNode = null;
		m_rootNode = null;
		clearNotifications();
	}

	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setStepOverOnce()
	{
		((IExecutionInformationHandler) m_executionInfo).setStepOverMode(StepOverMode.STEP_OVER_ONCE);
	}

	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setStepIntoOnce()
	{
		((IExecutionInformationHandler) m_executionInfo).setStepOverMode(StepOverMode.STEP_INTO_ONCE);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * =========================================================================
	 */
	@Override
	public int setVisibleNode(String stack)
	{
		int result = -1;

		String[] splitted = stack.split(":");
		IExecutionTreeNode oldAnalyzed = m_analysisNode;
		IExecutionTreeNode current = getRootNode();
		for (int i = 0; i < splitted.length - 3; i = i + 3)
		{
			// Code id is supposed to be correct
			// So we must focus on the line number
			int lineNumber = Integer.valueOf(splitted[i + 1]);
			IExecutionTreeLine line = current.getLine(lineNumber);
			current = line.getChildExecutionNode();
		}
		m_analysisNode = current;

		// If the node moved, notify it
		IExecutionTreeNode compare = m_viewNode;
		if (oldAnalyzed != null)
		{
			compare = oldAnalyzed;
		}

		/*
		 * Check if the node is currently being executed if so, send a message
		 * to the server
		 */
		boolean visibleLiving = false;
		int nodesToRoot = 0;
		IExecutionTreeNode currentNode = m_executionNode;
		while (currentNode != null)
		{
			if (currentNode == m_analysisNode)
			{
				visibleLiving = true;
			}
			if (visibleLiving)
			{
				nodesToRoot++;
			}
			IExecutionTreeLine line = currentNode.getParentLine();
			if (line != null)
			{
				currentNode = line.getParentExecutionNode();
			}
			else
			{
				currentNode = null;
			}
		}
		if (visibleLiving)
		{
			result = nodesToRoot - 1;
		}

		/*
		 * Update sequence filter value
		 */
		int lastExecution = Integer.parseInt(splitted[splitted.length - 1]);
		long sequence = m_analysisNode.getLastSequenceForExecution(lastExecution);
		m_trace.setSequenceFilter(sequence);

		if (compare != m_analysisNode)
		{
			String viewId = compare.getCodeId();
			String analysisId = m_analysisNode.getCodeId();

			// Source code changed condition
			boolean sourceChanged = !viewId.equals(analysisId);

			notifyViewChanged(sourceChanged);
		}

		return result;
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IExecutionTreeController#backToExecution()
	 * ==========================
	 * ================================================
	 */
	@Override
	public void backToExecution()
	{
		IExecutionTreeNode oldAnalysis = m_analysisNode;
		m_analysisNode = null;
		m_trace.resetSequenceFilter();

		// This code exists to avoid a crash the first time run or step is
		// commanded
		if (oldAnalysis == null)
			return;
		// If the node moved, notify it
		if (m_viewNode != oldAnalysis)
		{
			String viewId = m_viewNode.getCodeId();
			String analysisId = oldAnalysis.getCodeId();

			// Source code changed condition
			boolean sourceChanged = !viewId.equals(analysisId);
			notifyViewChanged(sourceChanged);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setReplayMode(boolean doingReplay)
	{
		m_doingReplay = doingReplay;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isInReplayMode()
	{
		return m_doingReplay;
	}

	/***************************************************************************
	 * Update the visible execution node
	 * 
	 * @param runInto
	 *            runInto flag
	 **************************************************************************/
	private void updateViewNode()
	{
		IExecutionTreeNode oldViewNode = m_viewNode;
		if (m_viewNode == null)
		{
			m_viewNode = m_executionNode;
			if (m_viewNode != null)
				notifyViewChanged(true);
		}
		else
		{
			// We need to notify a view changed when the node moves. The view
			// node moves
			// when:
			//
			// 1. The execution node is the direct child of the current view
			// node, and step over
			// is disabled OR run into is enabled
			//
			// 2. The execution node is above the current view node, in any
			// case.
			//
			// NOTE: even if a view node change is notified, it does not mean
			// that the source
			// code changes, that happens when the new node CODE id is
			// different.

			// Condition 2
			if (m_viewNode.getRecursionDepth() > m_executionNode.getRecursionDepth())
			{
				m_viewNode = m_executionNode;
			}
			// Condition 1 + step over checks
			else
			{
				StepOverMode soMode = m_executionInfo.getStepOverMode();
				if (soMode == null)
				{
					m_viewNode = m_executionNode;
				}
				else
				{
					switch (soMode)
					{
					case STEP_OVER_ALWAYS:
					case STEP_OVER_ONCE:
						break;
					case STEP_INTO_ALWAYS:
					case STEP_INTO_ONCE:
						m_viewNode = m_executionNode;
						break;
					}
				}
			}

			// If the node moved, notify it
			if (oldViewNode != m_viewNode)
			{
				String oldId = oldViewNode.getCodeId();
				String newId = m_viewNode.getCodeId();

				// Source code changed condition
				boolean sourceChanged = !oldId.equals(newId);

				notifyViewChanged(sourceChanged);
			}

		}
	}

	/***************************************************************************
	 * Notify the current view change
	 **************************************************************************/
	private void notifyViewChanged(boolean sourceCodeChanged)
	{
		for (IStackChangesListener listener : m_listeners)
		{
			listener.viewChanged(sourceCodeChanged);
		}
	}

	/***************************************************************************
	 * Close the processing mechanism
	 **************************************************************************/
	public void dispose()
	{
		m_processing.set(false);
	}
	
	/***************************************************************************
	 * Place a new notification in the prioritized queue. It will be automatically
	 * put in the correct position according to the sequence number. 
	 **************************************************************************/
	private void placeNotification( StackNotification data )
	{
		m_notifyLock.lock();
		try
		{
			m_arrivedNotifications.add(data);
		}
		finally
		{
			m_notifyLock.unlock();
		}
	}

	/***************************************************************************
	 * Obtain the next stack notification if any (return null if none available)
	 **************************************************************************/
	private StackNotification getNotification()
	{
		m_notifyLock.lock();
		try
		{
			return m_arrivedNotifications.poll();
		}
		finally
		{
			m_notifyLock.unlock();
		}
	}

	/***************************************************************************
	 * Clear all pending notifications
	 **************************************************************************/
	private void clearNotifications()
	{
		m_notifyLock.lock();
		try
		{
			m_arrivedNotifications.clear();
		}
		finally
		{
			m_notifyLock.unlock();
		}
	}

}
