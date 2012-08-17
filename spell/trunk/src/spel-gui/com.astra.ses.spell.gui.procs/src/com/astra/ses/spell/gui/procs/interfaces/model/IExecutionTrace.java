////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IExecutionTrace.java
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

/*******************************************************************************
 * 
 * IExecutionTrace specifies how a class which tracks the procedure execution
 * shall behave
 * 
 ******************************************************************************/
public interface IExecutionTrace
{

	/***************************************************************************
	 * Get the times a line has been executed
	 * 
	 * @param codeId
	 *            the code identifier
	 * @param lineNumber
	 *            the line index
	 * @return an integer specifying the times a line has been executed
	 **************************************************************************/
	public int getExecutionCount(String codeId, int lineNumber);

	/***************************************************************************
	 * Get the latest notification for a given line in the given code
	 * 
	 * @param codeId
	 *            the code id where the line is
	 * @param lineNumber
	 *            the line where retrieve the notifications
	 * @param summary
	 *            if the notification to retrieve shall be a summary
	 * @return an Array of {@link ILineData} containing the notification for the
	 *         given line in the given code id according to the latest parameter
	 *         value. If no notifications are retrieve,d an empty array is
	 *         returned
	 **************************************************************************/
	public ILineData[] getNotifications(String codeId, int lineNumber,
	        boolean latest);

	/***************************************************************************
	 * Get the summary item for the given line
	 * 
	 * @param codeId
	 *            the code id
	 * @param lineNumber
	 *            the line number
	 * @return an {@link ILineData} object containing the summary
	 **************************************************************************/
	public ILineSummaryData getSummary(String codeId, int lineNumber);

	/***************************************************************************
	 * Notifications for a given line have been received from the server
	 * 
	 * @param node
	 *            the {@link IExecutionTreeNode} which received the
	 *            notifications
	 * @param lineNumber
	 *            the line number
	 **************************************************************************/
	public void notifyItem(IExecutionTreeLine line);

	/***************************************************************************
	 * Cleanup the trace
	 **************************************************************************/
	public void reset();

	/***************************************************************************
	 * Set max sequence filter to its default value
	 **************************************************************************/
	public void resetSequenceFilter();

	/***************************************************************************
	 * Set the max sequence value filter to apply on the trace
	 * 
	 * @param maxSequence
	 *            the new sequence filter value to use
	 **************************************************************************/
	public void setSequenceFilter(long maxSequence);
}
