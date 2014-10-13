///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.callstack
// 
// FILE      : CallstackNode.java
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

import java.util.ArrayList;

/**************************************************************************
 * Base node of the callstack tree viewer.
 *************************************************************************/
public abstract class CallstackNode
{
	/***************************************************************************
	 * 
	 * {@link CallstackNodeType} determines the type of node
	 **************************************************************************/
	public enum CallstackNodeType
	{
		ROOT, EXECUTION
	}

	/** Node type */
	private CallstackNodeType	     m_type;
	/** Label for presenting this node */
	private String	                 m_codeId;
	/** Code name if any */
	private String	                 m_codeName;
	/** Line number */
	private int	                     m_lineNo;
	/** Latest execution */
	private int	                     m_execution;
	/** Parent node if any */
	private CallstackNode	         m_parent;
	/** Parent stack */
	private String	                 m_parentStack;
	/** Children nodes if any */
	private ArrayList<CallstackNode>	m_children;
	/** Parent line number */
	private int	                     m_parentLineNo;
	/** Check if this node's execution has finished */
	private boolean	                 m_finished;

	/**************************************************************************
	 * Constructor.
	 * 
	 * @param type
	 *            the node type
	 * @param codeId
	 *            the code id this node represents
	 * @param lineNo
	 *            the initial lineNumber
	 *************************************************************************/
	public CallstackNode(CallstackNodeType type, String codeId,
	        String codeName, int lineNumber, int execution)
	{
		m_type = type;
		m_codeId = codeId;
		m_codeName = codeName;
		m_lineNo = lineNumber;
		m_children = new ArrayList<CallstackNode>();
		m_parentStack = null;
		m_finished = false;
		m_execution = execution;
	}

	/**************************************************************************
	 * Get node identifier.
	 * 
	 * @return The identifier
	 *************************************************************************/
	String getCodeId()
	{
		return m_codeId;
	}

	/**************************************************************************
	 * Get node name.
	 * 
	 * @return The name
	 *************************************************************************/
	String getCodeName()
	{
		return m_codeName;
	}

	/**************************************************************************
	 * Get line number
	 *************************************************************************/
	int getLine()
	{
		return m_lineNo;
	}

	/***************************************************************************
	 * Get latest execution time
	 * 
	 * @return
	 **************************************************************************/
	long getExecution()
	{
		return m_execution;
	}

	/**************************************************************************
	 * This node's execution has finished
	 *************************************************************************/
	void returnNotified()
	{
		m_finished = true;
	}

	/**************************************************************************
	 * A line to be executed has been notified
	 *************************************************************************/
	void lineNotified(int lineNumber)
	{
		m_lineNo = lineNumber;
	}

	/**************************************************************************
	 * Get a stack representation
	 * 
	 * @return
	 *************************************************************************/
	public String getStack()
	{
		String stack = m_parentStack;
		if (!stack.isEmpty())
		{
			stack += ":";
		}
		stack += m_codeId + ":" + m_lineNo + ":" + m_execution;
		return stack;
	}

	/**************************************************************************
	 * Get the parent node if any.
	 * 
	 * @return
	 *************************************************************************/
	public CallstackNode getParent()
	{
		return m_parent;
	}

	/**************************************************************************
	 * Get the children nodes.
	 *************************************************************************/
	public CallstackNode[] getChildren()
	{
		return m_children.toArray(new CallstackNode[m_children.size()]);
	}

	/**************************************************************************
	 * Get node type
	 * 
	 * @return
	 *************************************************************************/
	public CallstackNodeType getType()
	{
		return m_type;
	}

	/**************************************************************************
	 * Check if there are children.
	 *************************************************************************/
	public boolean hasChildren()
	{
		return m_children.size() > 0;
	}

	/**************************************************************************
	 * Obtain the node label.
	 *************************************************************************/
	@Override
	public String toString()
	{
		return getCodeId() + ":" + getLine();
	}

	/**************************************************************************
	 * Check if this node's execution has finished
	 * 
	 * @return true if the execution has finished, false otherwise
	 *************************************************************************/
	public boolean isFinished()
	{
		return m_finished;
	}

	/**************************************************************************
	 * Set the parent node.
	 * 
	 * @param parent
	 *************************************************************************/
	protected void setParent(CallstackNode parent)
	{
		m_parent = parent;
		m_parentLineNo = parent.getLine();
		m_parentStack = parent.getStack();
	}

	/**************************************************************************
	 * Add a child node.
	 * 
	 * @param child
	 *************************************************************************/
	protected void addChild(CallstackNode child)
	{
		m_children.add(child);
		child.setParent(this);
	}

	/**************************************************************************
	 * Clear the node children.
	 *************************************************************************/
	protected void clearChildren()
	{
		m_children.clear();
	}

	/***************************************************************************
	 * Get parent line number
	 * 
	 * @return the parent's line number where this node was called
	 **************************************************************************/
	protected int getParentLine()
	{
		return m_parentLineNo;
	}
}
