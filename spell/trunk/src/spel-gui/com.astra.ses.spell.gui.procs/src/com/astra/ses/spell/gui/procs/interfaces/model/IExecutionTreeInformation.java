////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : IExecutionTreeInformation.java
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

import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IStackChangesListener;

/*******************************************************************************
 * 
 * IProcedureController handles the different procedure notifications as well as
 * it enables access to some properties
 * 
 ******************************************************************************/
public interface IExecutionTreeInformation
{
	/***************************************************************************
	 * Register the listener to notify it about routine execution changes
	 **************************************************************************/
	public void addStackChangesListener(IStackChangesListener listener);

	/***************************************************************************
	 * Get the on-execution lines on the given code id in the whole stack
	 * 
	 * @param codeId
	 *            the code id
	 **************************************************************************/
	public Integer[] getCodeStackLines(String codeId, int lineNumber);

	/***************************************************************************
	 * Get the line currently being executed
	 * 
	 * @return the on execution line index
	 **************************************************************************/
	public int getCurrentLine() throws UninitProcedureException;

	/***************************************************************************
	 * Check if the given line has been executed
	 * 
	 * @return the on execution line index
	 **************************************************************************/
	public boolean isExecuted( int lineNo ) throws UninitProcedureException;

	/***************************************************************************
	 * Get the root execution node
	 * 
	 * @return the root node
	 **************************************************************************/
	public IExecutionTreeNode getRootNode();

	/***************************************************************************
	 * Get the on execution code block node
	 * 
	 * @return the currently on execution node.
	 **************************************************************************/
	public IExecutionTreeNode getCurrentNode();

	/***************************************************************************
	 * Get the root code id
	 * 
	 * @return the root code identifier
	 **************************************************************************/
	public String getRootCodeId();

	/***************************************************************************
	 * Get the current code id
	 * 
	 * @return the visible code identifier
	 **************************************************************************/
	public String getCurrentCodeId() throws UninitProcedureException;
}
