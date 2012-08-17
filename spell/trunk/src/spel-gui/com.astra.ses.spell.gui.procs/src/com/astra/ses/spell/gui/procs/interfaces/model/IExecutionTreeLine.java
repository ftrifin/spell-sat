////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IExecutionTreeLine.java
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

/*******************************************************************************
 * 
 * IProcedureExecutionLine models executed lines that have been executed
 * 
 ******************************************************************************/
public interface IExecutionTreeLine
{
	/***************************************************************************
	 * Get the execution node where this line is contained
	 * 
	 * @return the parent {@link IExecutionTreeNode} where this line was called
	 **************************************************************************/
	public IExecutionTreeNode getParentExecutionNode();

	/***************************************************************************
	 * Get this line's execution subnode, if it has any
	 * 
	 * @return a {@link IExecutionTreeNode} nested in this line, or null if it
	 *         does not have any
	 **************************************************************************/
	public IExecutionTreeNode getChildExecutionNode();

	/***************************************************************************
	 * Get the execution numbers for this line The number refers to the absolute
	 * order along the procedure's life
	 * 
	 * @return an array of integers indicating the execution
	 **************************************************************************/
	public Integer[] getExecutions();

	/***************************************************************************
	 * Check if the line has been executed
	 **************************************************************************/
	public boolean isExecuted();

	/***************************************************************************
	 * Mark line as executed
	 * 
	 **************************************************************************/
	public void markExecuted( boolean executed );

	/***************************************************************************
	 * Get the line number
	 * 
	 * @return the line number
	 **************************************************************************/
	public int getLineNumber();

	/***************************************************************************
	 * Process a received notification
	 **************************************************************************/
	public void processNotification(ItemNotification notification, long sequence);

	/***************************************************************************
	 * Get processed notifications related to this line whose sequence value is
	 * not higher than the given one
	 * 
	 * @param latest
	 *            if true, only notification related to the last sequence are
	 *            considered
	 * @param maxSequence
	 *            the maximum sequence value
	 * @return and array of {@link ILineData}. If no notifications are retrieved
	 *         and empty array is returned
	 **************************************************************************/
	public ILineData[] getNotifications(boolean latest, long maxSequence);

	/***************************************************************************
	 * Return an {@link ILineSummaryData} which summarizes the closes execution
	 * to the given maxSequence value
	 * 
	 * @param maxSequence
	 *            the limit sequence
	 * @return an {@link ILineSummaryData}
	 **************************************************************************/
	public ILineSummaryData getSummary(long maxSequence);

	/***************************************************************************
	 * Set a child for this line
	 * 
	 * @param childNode
	 *            the child node
	 **************************************************************************/
	public void setChild(IExecutionTreeNode childNode);
}
