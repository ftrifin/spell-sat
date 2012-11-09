////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IExecutionTreeNode.java
//
// DATE      : 2010-08-03
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
package com.astra.ses.spell.gui.procs.interfaces.model;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;

/*******************************************************************************
 * 
 * IProcedureExecutionNode models an execution scope behavior An execution scope
 * may be considered as a piece of code which handles the lines that receive
 * notifications from the server
 * 
 ******************************************************************************/
public interface IExecutionTreeNode
{
	/***************************************************************************
	 * Return this node's identifier
	 * 
	 * @return the node's identifier
	 **************************************************************************/
	public String getCodeId();

	/***************************************************************************
	 * Return this node's code name if any
	 * 
	 * @return the node's code name if any
	 **************************************************************************/
	public String getCodeName();

	/***************************************************************************
	 * Get line that is being executed at this moment
	 * 
	 * @return the on execution line. null if there is not any
	 **************************************************************************/
	public IExecutionTreeLine getCurrentLine();

	/***************************************************************************
	 * Get the times this node has been called
	 * 
	 * @return
	 **************************************************************************/
	public Integer[] getExecutions();

	/***************************************************************************
	 * Check if the given sequence value is contained in any of the executions
	 **************************************************************************/
	public boolean containsSequence(long sequence);

	/***************************************************************************
	 * Get the last sequence value for the given execution
	 * 
	 * @param execution
	 *            the execution number
	 * @return the last execution value
	 **************************************************************************/
	public long getLastSequenceForExecution(int execution);

	/***************************************************************************
	 * Get a {@link IExecutionTreeLine} by giving its line number
	 * 
	 * @param index
	 *            the line number
	 * @return an {@link IExecutionTreeLine} corresponding to the given number
	 **************************************************************************/
	public IExecutionTreeLine getLine(int index);

	/***************************************************************************
	 * Get the complete set of {@link IExecutionTreeLine}
	 * 
	 * @return a set of {@link IExecutionTreeLine} corresponding to the given
	 *         number
	 **************************************************************************/
	public IExecutionTreeLine[] getLines();

	/***************************************************************************
	 * Get the parent line
	 * 
	 * @return this node's parent line
	 **************************************************************************/
	public IExecutionTreeLine getParentLine();

	/***************************************************************************
	 * Get node recursion depth.
	 **************************************************************************/
	public int getRecursionDepth();

	/***************************************************************************
	 * Notify about a current line change
	 * 
	 * @param data
	 *            the {@link StackNotification} received from the server
	 **************************************************************************/
	public void notifyLineChanged( int lineNo, StackNotification data);

	/***************************************************************************
	 * Notify about a current line change
	 * 
	 * @param data
	 *            the {@link StackNotification} received from the server
	 **************************************************************************/
	public void notifyLineChanged( int lineNo );

	/***************************************************************************
	 * Notify about a item information
	 **************************************************************************/
	public void notifyItem(int lineNumber, ItemNotification notification);

	/***************************************************************************
	 * Reexecute this node
	 * 
	 * @param execution
	 **************************************************************************/
	public void reExecute(int execution, long sequence);
}
