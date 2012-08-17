///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.callstack
// 
// FILE      : CallstackProcedureModel.java
//
// DATE      : 2008-11-21 08:55
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.model.callstack;

import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeNode;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/******************************************************************************
 * Special tree node representing the main procedure. There is only one node of
 * this type on each callstack tree model.
 * 
 *****************************************************************************/
public class CallstackProcedureModel
{
	/**
	 * Boolean to determine if the first call has been received This bug exists
	 * due to the reload feature, because a call message is received for
	 * entering in the procedure
	 */

	private boolean	          m_firstCallReceived;
	/** Holds the root (invisible parent node on the tree) */
	private CallstackRootNode	m_root;
	/** Holds the currently active node */
	private CallstackNode	  m_currentNode;

	/**************************************************************************
	 * Constructor.
	 * 
	 * @param name
	 *            The identifier of the procedure
	 * @param model
	 *            The associated procedure model, used to populate the tree
	 *            model.
	 *************************************************************************/
	public CallstackProcedureModel(String name, IProcedure model)
	{
		// If we have a model available, populate this node with the data
		m_root = null;
		initialize(model);
	}

	/***************************************************************************
	 * Get the current node
	 * 
	 * @return
	 **************************************************************************/
	public CallstackNode getCurrentNode()
	{
		return m_currentNode;
	}

	/***************************************************************************
	 * Get this model's root node
	 * 
	 * @return
	 **************************************************************************/
	public CallstackNode getRootNode()
	{
		return m_root;
	}

	/**************************************************************************
	 * Clear the model children.
	 *************************************************************************/
	public void clear()
	{
		// If we have a model available, populate this node with the data
		m_root.clearChildren();
		m_firstCallReceived = false;
	}

	/**************************************************************************
	 * Initialize the tree model with the given procedure model.
	 * 
	 * @param model
	 *            Procedure model to use
	 *************************************************************************/
	private void initialize(IProcedure model)
	{
		IExecutionTreeInformation info = model.getExecutionTree();
		m_root = new CallstackRootNode(model.getProcId());
		IExecutionTreeNode root = info.getRootNode();
		if (root != null)
		{
			processExecutionNode(root, m_root);
			m_firstCallReceived = true;
		}
	}

	/**************************************************************************
	 * Process recursively the data given by the execution tree node from a
	 * procedure model. Add the data to the given tree node and recurse over
	 * child execution tree nodes.
	 * 
	 * @param execNode
	 *            the execution tree node.
	 * @param treeNode
	 *            the callstack tree node.
	 *************************************************************************/
	private void processExecutionNode(IExecutionTreeNode execNode,
	        CallstackNode treeNode)
	{
		m_currentNode = treeNode;
		// Get the total of lines that received notifications
		IExecutionTreeLine[] lines = execNode.getLines();
		// Get the currently executed line
		int currentExecLine = execNode.getCurrentLine().getLineNumber();
		// Iterate over the notified lines
		for (IExecutionTreeLine line : lines)
		{
			// Get line number
			int lineNumber = line.getLineNumber();

			Integer[] executions = line.getExecutions();

			for (int execution : executions)
			{
				// In out node, indicate that the line has been executed
				treeNode.lineNotified(lineNumber);

				IExecutionTreeNode child = line.getChildExecutionNode();
				if (child != null)
				{
					CallstackNode newNode = new CallstackExecutionNode(
					        child.getCodeId(), child.getCodeName(), lineNumber,
					        execution);

					if (lineNumber != currentExecLine)
					{
						newNode.returnNotified();
					}
					treeNode.addChild(newNode);
					// Recurse over node children
					processExecutionNode(child, newNode);
				}
			}
		}
	}

	/**************************************************************************
	 * Notify about a stack change. This callback is called by the callstack
	 * content provider.
	 * 
	 * @param model
	 *            Model to be used
	 * @param data
	 *            Stack notification data
	 *************************************************************************/
	public CallstackNode notifyStack(IProcedure model, StackNotification data)
	{
		switch (data.getStackType())
		{
		case CALL:
			// Otherwise, a function call means that we add a node to the
			// callstack tree.
			if (!m_firstCallReceived)
			{
				m_currentNode = m_root;
				m_firstCallReceived = true;
			}
			else
			{
				String codeId = data.getStackPosition().get(
				        data.getStackPosition().size() - 2);
				int lineNumber = Integer.valueOf(data.getStackPosition()
				        .lastElement());
				CallstackNode newNode = new CallstackExecutionNode(codeId,
				        data.getCodeName(), lineNumber, data.getNumExecutions());
				m_currentNode.addChild(newNode);
				m_currentNode = newNode;
			}
			break;
		case RETURN:
			// Process a return event and go back to the parent node. 
			// When we are in the main code, the return event
			// shall not be processed since there is no parent to go to.
			if (m_currentNode.getParent() != null)
			{
				m_currentNode.returnNotified();
				m_currentNode = m_currentNode.getParent();
			}
			break;
		case LINE:
			// Set the line number in the current node
			int lineNumber = Integer.valueOf(data.getStackPosition()
			        .lastElement());
			;
			m_currentNode.lineNotified(lineNumber);
			break;
		}
		return m_currentNode;
	}
}
