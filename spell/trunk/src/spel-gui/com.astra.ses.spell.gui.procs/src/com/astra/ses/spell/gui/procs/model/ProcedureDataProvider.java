////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ProcedureDataProvider.java
//
// DATE      : 2010-08-03
//
// Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
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

import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IStackChangesListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTrace;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeController;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineSummaryData;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;
import com.astra.ses.spell.gui.procs.interfaces.model.ISourceCodeProvider;

/*******************************************************************************
 * 
 * ProcedureDataProvider implements {@link IProcedureDataProvider} to give
 * information about {@link Procedure} objects
 * 
 ******************************************************************************/
public class ProcedureDataProvider implements IProcedureDataProvider
{

	/** Execution information */
	private IExecutionInformation	 m_execInfo;
	/** Procedure controller */
	private IExecutionTreeController	m_controller;
	/** Execution trace */
	private IExecutionTrace	         m_trace;
	/** Source provider */
	private ISourceCodeProvider	     m_sourceProvider;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param controller
	 * @param sourceProvider
	 **************************************************************************/
	public ProcedureDataProvider(IExecutionInformation info,
	        IExecutionTreeController controller, IExecutionTrace trace,
	        ISourceCodeProvider sourceProvider)
	{
		m_execInfo = info;
		m_trace = trace;
		m_controller = controller;
		m_sourceProvider = sourceProvider;
	}

	@Override
	public Integer[] getAffectedLines() throws UninitProcedureException
	{
		String viewCode = getCurrentCodeId();
		return m_controller.getCodeStackLines(viewCode);
	}

	@Override
	public String getCurrentCodeId() throws UninitProcedureException
	{
		return m_controller.getCurrentCodeId();
	}

	@Override
	public String[] getCurrentSource() throws UninitProcedureException
	{
		String codeId = getCurrentCodeId();
		return m_sourceProvider.getSource(codeId);
	}

	@Override
	public String[] getRootSource()
	{
		String codeId = m_controller.getRootCodeId();
		return m_sourceProvider.getSource(codeId);
	}

	@Override
	public int getCurrentLine() throws UninitProcedureException
	{
		return m_controller.getCurrentLine();
	}

	@Override
	public ExecutorStatus getExecutorStatus()
	{
		return m_execInfo.getStatus();
	}

	@Override
	public BreakpointType getBreakpoint(int lineNumber)
	        throws UninitProcedureException
	{
		String codeId = getCurrentCodeId();
		return m_sourceProvider.getBreakpoint(codeId, lineNumber);
	}

	@Override
	public ILineData[] getItemData(int line, boolean latest)
	        throws UninitProcedureException
	{
		String codeId = getCurrentCodeId();
		return m_trace.getNotifications(codeId, line, latest);
	}

	@Override
	public String getLineSource(int line) throws UninitProcedureException
	{
		String codeId = getCurrentCodeId();
		return m_sourceProvider.getSource(codeId)[line];
	}

	@Override
	public ILineSummaryData getSummary(int line)
	        throws UninitProcedureException
	{
		String codeId = getCurrentCodeId();
		return m_trace.getSummary(codeId, line);
	}

	@Override
	public void addStackChangesListener(IStackChangesListener listener)
	{
		m_controller.addStackChangesListener(listener);
	}

	@Override
	public void removeStackChangesListener(IStackChangesListener listener)
	{
		m_controller.removeStackChangesListener(listener);
	}

	@Override
	public int getExecutionCount(int lineNumber)
	        throws UninitProcedureException
	{
		String codeId = getCurrentCodeId();
		return m_trace.getExecutionCount(codeId, lineNumber);
	}
}
