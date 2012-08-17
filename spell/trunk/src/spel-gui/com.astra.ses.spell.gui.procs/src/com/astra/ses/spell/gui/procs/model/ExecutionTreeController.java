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
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
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
	private static final int MAX_NOTIFICATION_FREQ = 100;
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
	/** Lock used for notification ordering */
	private Lock m_sequenceLock;
	/** Condition for notification ordering */
	private Condition m_notifyCondition;
	/**
	 * Long with concurrency support, holds the last notification sequence
	 * reveived
	 */
	private AtomicLong m_lastSequence;
	/** Used to ease graphical notifications */
	private long m_lastGUInotif = -1;
	/** Used to know when are we doing AsRun replay */
	private boolean m_doingReplay;

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
		m_sequenceLock = new ReentrantLock();
		m_notifyCondition = m_notifyLock.newCondition();
		m_lastSequence = new AtomicLong(-1);
		m_doingReplay = false;
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
		Vector<String> stack = data.getStackPosition();
		int lineNumber = Integer.parseInt(stack.lastElement());
		// Add the notification
		m_executionNode.notifyItem(lineNumber, data);
		/*
		 * Every line in the stack shall notify an item
		 */
		IExecutionTreeLine line = m_executionNode.getCurrentLine();
		while (line != null)
		{
			m_trace.notifyItem(line);
			line = line.getParentExecutionNode().getParentLine();
		}
	}

	/***************************************************************************
	 * Process stack events to be executed
	 * 
	 * @param procId
	 **************************************************************************/
	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		// We use the stack notification sequence number to organise the
		// notification processing. If there is a jump in the sequence, the
		// incoming notification is blocked in the wait, until the correct
		// notification arrives and signals the condition.

		// IMPORTANT we do not use the organization mechanism when doing AsRun
		// replay.

		long sequence = -1;
		if (!m_doingReplay)
		{
			// Get the sequence number
			sequence = data.getSequence();
			// We use an atomic long for concurrency.
			// Take into account the initial case.
			if (!m_lastSequence.equals(-1))
			{
				// If there is a jump in the sequence
				while (sequence > m_lastSequence.longValue() + 1)
				{
					// Wait until the previous notification arrives
					waitCondition();
				}
			}
			// This lock ensures ordered, concurrent processing of each call.
			// A different lock must be used, since several notifications may be
			// blocked at once.
			m_sequenceLock.lock();
		}
		else
		{
			m_lastSequence.set(data.getSequence());
		}

		try
		{
			// Process the notification in the execution tree
			switch (data.getStackType())
			{
			case LINE:
				processLineEvent(data);
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
		finally
		{
			if (!m_doingReplay)
			{
				// Update the last sequence number
				m_lastSequence.set(sequence);
				// Signal this sequence
				signalCondition();
				// Unlock the sequence ordering lock
				m_sequenceLock.unlock();
			}
		}
	}

	/***************************************************************************
	 * Notify the current line changed to be executed
	 * 
	 * @param procId
	 **************************************************************************/
	private void processLineEvent(StackNotification data)
	{
		if (m_executionNode == m_viewNode)
		{
			((IExecutionInformationHandler) m_executionInfo).resetStepOverMode();
		}

		// Update the currently executed line
		int lineNumber = Integer.parseInt(data.getStackPosition().lastElement());
		m_executionNode.notifyLineChanged(lineNumber,data);
		// Discard notifications to the graphical layer if they come too
		// frequently
		if ((System.currentTimeMillis() - m_lastGUInotif) > MAX_NOTIFICATION_FREQ)
		{
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
		String codeId = data.getStackPosition().elementAt(0);
		int execution = data.getNumExecutions();
		long sequence = data.getSequence();
		if (m_rootNode == null)
		{
			// If this is the first call, set the root node
			m_rootNode = new ExecutionTreeNode(codeId, codeId, null, 0, execution, sequence);
			m_executionNode = m_rootNode;
		}
		else
		{
			IExecutionTreeLine parentLine = m_executionNode.getCurrentLine();
			if (parentLine.getChildExecutionNode() == null)
			{
				m_executionNode = new ExecutionTreeNode(codeId, data.getCodeName(), parentLine, m_executionNode.getRecursionDepth() + 1,
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
		int lineNumber = Integer.parseInt(data.getStackPosition().lastElement());
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
		signalCondition();
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
	 * Wait a signal for correct ordering
	 **************************************************************************/
	void waitCondition()
	{
		m_notifyLock.lock();
		try
		{
			m_notifyCondition.awaitNanos(1000000000);
		}
		catch (InterruptedException err)
		{
			err.printStackTrace();
		}
		finally
		{
			m_notifyLock.unlock();
		}
	}

	/***************************************************************************
	 * Signal ordering condition
	 **************************************************************************/
	void signalCondition()
	{
		m_notifyLock.lock();
		try
		{
			m_notifyCondition.signalAll();
		}
		finally
		{
			m_notifyLock.unlock();
		}
	}
}
