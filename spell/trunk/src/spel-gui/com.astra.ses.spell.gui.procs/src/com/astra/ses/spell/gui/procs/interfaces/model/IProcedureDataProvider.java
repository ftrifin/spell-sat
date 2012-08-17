////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IProcedureDataProvider.java
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

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IStackChangesListener;

/*******************************************************************************
 * 
 * IProcedureDataProvider gives information about its parent procedure
 * 
 ******************************************************************************/
public interface IProcedureDataProvider
{
	/***************************************************************************
	 * Get the affected lines in the last notification
	 * 
	 * @return a collection of integer, representing each a visible line
	 * @throws UninitProcedureException
	 **************************************************************************/
	public Integer[] getAffectedLines( int lineNumber ) throws UninitProcedureException;

	/***************************************************************************
	 * Get current code id
	 * 
	 * @return
	 * @throws UninitProcedureException
	 **************************************************************************/
	public String getCurrentCodeId() throws UninitProcedureException;

	/***************************************************************************
	 * Get the current visible execution node source code
	 * 
	 * @return the source code for the currently executed node
	 * @throws UninitProcedureException
	 **************************************************************************/
	public String[] getCurrentSource( IProgressMonitor monitor ) throws UninitProcedureException;

	/***************************************************************************
	 * Get the root source code
	 * 
	 * @return the source code for the currently executed node
	 **************************************************************************/
	public String[] getRootSource( IProgressMonitor monitor );

	/***************************************************************************
	 * Get the line index currently being executed
	 * 
	 * @return the on-execution line index
	 * @throws UninitProcedureException
	 **************************************************************************/
	public int getCurrentLine() throws UninitProcedureException;

	/***************************************************************************
	 * Check if the line has been executed at least once
	 * 
	 * @throws UninitProcedureException
	 **************************************************************************/
	public boolean isExecuted( int lineNo ) throws UninitProcedureException;

	/***************************************************************************
	 * Get current executor status
	 * 
	 * @return the current executor status
	 **************************************************************************/
	public ExecutorStatus getExecutorStatus();

	/***************************************************************************
	 * Get the execution number for a given line inside its node
	 * 
	 * @param lineNumber
	 *            the line number
	 * @return
	 * @throws UninitProcedureException
	 **************************************************************************/
	public int getExecutionCount(int lineNumber)
	        throws UninitProcedureException;

	/***************************************************************************
	 * Get the notifications received by a given line
	 * 
	 * @param summary
	 *            for a line that has been executed at least twice, if summary
	 *            is true the the returned notification will refer to the latest
	 *            execution
	 * @return a collection of {@link ItemNotification} containing information
	 *         about the line execution
	 * @throws UninitProcedureException
	 **************************************************************************/
	public ILineData[] getItemData(int line, boolean latest)
	        throws UninitProcedureException;

	/***************************************************************************
	 * Get the source code for a given line
	 * 
	 * @param line
	 * @return
	 * @throws UninitProcedureException
	 **************************************************************************/
	public String getLineSource(int line) throws UninitProcedureException;

	/***************************************************************************
	 * Get an {@link ItemNotification} containing a summary of the line's
	 * executions
	 * 
	 * @param line
	 *            the line index
	 * @return an ItemNotification containing a summary of the line's executions
	 * @throws UninitProcedureException
	 **************************************************************************/
	public ILineSummaryData getSummary(int line)
	        throws UninitProcedureException;

	/***************************************************************************
	 * Check if the line at the givn index has a breakpoint
	 * 
	 * @param lineNumber
	 *            the line index
	 * @return true if the line has a breakpoint, false otherwise
	 * @throws UninitProcedureException
	 **************************************************************************/
	public BreakpointType getBreakpoint(int lineNumber)
	        throws UninitProcedureException;

	/***************************************************************************
	 * Register to execution node changes
	 * 
	 * @param listener
	 *            an {@link IStackChangesListener}
	 **************************************************************************/
	public void addStackChangesListener(IStackChangesListener listener);

	/***************************************************************************
	 * Unregister to execution node changes
	 * 
	 * @param listener
	 *            an {@link IStackChangesListener}
	 **************************************************************************/
	public void removeStackChangesListener(IStackChangesListener listener);
}
