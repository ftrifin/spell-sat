////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ProcedureController.java
//
// DATE      : 2010-07-30
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

import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcExtensions;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation.StepOverMode;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeController;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController;
import com.astra.ses.spell.gui.procs.interfaces.model.ISourceCodeProvider;
import com.astra.ses.spell.gui.procs.interfaces.model.priv.IExecutionInformationHandler;

/*******************************************************************************
 * 
 * Procedure controller
 * 
 ******************************************************************************/
public class ProcedureController implements IProcedureController
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Context proxy */
	private static IContextProxy s_proxy = null;
	/** Listener id */
	private static final String LISTENER_ID = "com.astra.ses.spell.gui.procs.model.Procedure";

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the procedure model */
	private IProcedure m_model;
	private ISourceCodeProvider m_sourceCodeProvider;
	private IExecutionTreeController m_treeController;
	private InputData m_promptData;

	/*
	 * Static block to retrieve the context proxy
	 */
	static
	{
		s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ProcedureController(IProcedure model, ISourceCodeProvider srcProvider, IExecutionTreeController treeController)
	{
		m_sourceCodeProvider = srcProvider;
		m_treeController = treeController;
		m_model = model;
		m_promptData = null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return LISTENER_ID;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void issueCommand(ExecutorCommand cmd, String[] args)
	{
		ExecutorStatus status = m_model.getRuntimeInformation().getStatus();
		if (!cmd.validate(status))
		{
			throw new CommandFailed("Cannot execute command, invalid procedure status");
		}
		releaseExecutorCommand(cmd, args);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void refresh() throws Exception
	{
		updateInfo();
		updateConfig();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public InputData getPromptData()
	{
		return m_promptData;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void updateInfo() throws Exception
	{
		if (s_proxy.isConnected())
		{
			Logger.debug("Refreshing execution model", Level.PROC, this);
			String procId = m_model.getProcId();
			IExecutorInfo info = new ExecutorInfo(procId);
			s_proxy.updateExecutorInfo(procId, info);
			((IExecutionInformationHandler) m_model.getRuntimeInformation()).copyFrom(info);
			Logger.debug("Execution model updated", Level.PROC, this);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void updateConfig() throws Exception
	{
		if (s_proxy.isConnected())
		{
			Logger.debug("Refreshing configuration model", Level.PROC, this);
			ExecutorConfig cfg = new ExecutorConfig(m_model.getProcId());
			s_proxy.updateExecutorConfig(m_model.getProcId(), cfg);
			((IExecutionInformationHandler) m_model.getRuntimeInformation()).copyFrom(cfg);
			Logger.debug("Configuration model updated", Level.PROC, this);
		}
	}

	/*
	 * ==========================================================================
	 * Breakpoints management
	 * =========================================================================
	 */

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void clearBreakpoints()
	{
		// Send the request
		try
		{
			s_proxy.clearBreakpoints(m_model.getProcId());
			m_sourceCodeProvider.clearBreakpoints();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setBreakpoint(int lineNumber, BreakpointType type) throws UninitProcedureException
	{
		String codeId = m_treeController.getCurrentCodeId();
		// Send the request
		try
		{
			s_proxy.toggleBreakpoint(m_model.getProcId(), codeId, lineNumber, type);
			m_sourceCodeProvider.setBreakpoint(codeId, lineNumber, type);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void gotoLine(int lineNumber) throws CommandFailed
	{
		m_treeController.getCurrentNode().getCurrentLine().markExecuted(false);
		releaseExecutorCommand(ExecutorCommand.GOTO, new String[] { "", String.valueOf(lineNumber) });
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void gotoLabel(String label) throws CommandFailed
	{
		m_treeController.getCurrentNode().getCurrentLine().markExecuted(false);
		releaseExecutorCommand(ExecutorCommand.GOTO, new String[] { label, "" });
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void script(String script) throws CommandFailed
	{
		if (script != null && (!script.isEmpty()))
		{
			script = script.replaceAll("'", "\"");
		}
		releaseExecutorCommand(ExecutorCommand.SCRIPT, new String[] { script });
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setBrowsableLib(boolean showLib)
	{
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setShowLib(showLib);
		ProcExtensions.get().fireModelConfigured(m_model);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutorStatus(ExecutorStatus status)
	{
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setExecutorStatus(status);
		switch (status)
		{
		case PAUSED:
			/*
			 * If there is a temporary breakpoint at the current line Then there
			 * is need to remove it
			 */
			try
			{
				String codeId = m_treeController.getCurrentCodeId();
				int lineNumber = m_treeController.getCurrentLine();
				m_sourceCodeProvider.removeTemporaryBreakpoint(codeId, lineNumber);
			}
			catch (UninitProcedureException e)
			{
			}
			;
			break;
		default:
			break;
		}
	}

	@Override
	public void setError(ErrorData data)
	{
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setError(data);
		m_model.getRuntimeProcessor().notifyProcedureError(data);

		if (!m_model.isInReplayMode())
		{
			// Redirect the data to the consumers
			ProcExtensions.get().fireProcedureError(m_model, data);
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setRunInto(boolean runInto)
	{
		// Internal information model
		IExecutionInformationHandler handler = (IExecutionInformationHandler) m_model.getRuntimeInformation();
		if (runInto)
		{
			handler.setStepOverMode(StepOverMode.STEP_INTO_ALWAYS);
		}
		else
		{
			handler.setStepOverMode(StepOverMode.STEP_OVER_ALWAYS);
		}
		// If we are controlling, perform the change request to the server
		if (m_model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROLLING))
		{
			try
			{
				// Send the request to configure the server
				ExecutorConfig config = new ExecutorConfig(m_model.getProcId());
				m_model.getRuntimeInformation().visit(config);
				s_proxy.setExecutorConfiguration(m_model.getProcId(), config);
				ProcExtensions.get().fireModelConfigured(m_model);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setExecutionDelay(int msec)
	{
		// Update the internal information model
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setExecutionDelay(msec);
		// If we are controlling, perform the change request to the server
		if (m_model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROLLING))
		{
			try
			{
				// Send the request to configure the server
				ExecutorConfig config = new ExecutorConfig(m_model.getProcId());
				m_model.getRuntimeInformation().visit(config);
				s_proxy.setExecutorConfiguration(m_model.getProcId(), config);
				ProcExtensions.get().fireModelConfigured(m_model);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setStepByStep(boolean value)
	{
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setStepByStep(value);
		// If we are controlling, perform the change request to the server
		if (m_model.getRuntimeInformation().getClientMode().equals(ClientMode.CONTROLLING))
		{
			try
			{
				// Send the request to configure the server
				ExecutorConfig config = new ExecutorConfig(m_model.getProcId());
				m_model.getRuntimeInformation().visit(config);
				s_proxy.setExecutorConfiguration(m_model.getProcId(), config);
				ProcExtensions.get().fireModelConfigured(m_model);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedurePrompt(InputData inputData)
	{
		m_promptData = inputData;
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setWaitingInput(true);
		// Rredirect the data to the consumers
		ProcExtensions.get().firePrompt(m_model);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureCancelPrompt(InputData inputData)
	{
		m_promptData = null;
		m_treeController.backToExecution();
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setWaitingInput(false);
		// Redirect the data to the consumers
		ProcExtensions.get().fireCancelPrompt(m_model);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void notifyProcedureFinishPrompt(InputData inputData)
	{
		m_promptData = null;
		m_treeController.backToExecution();
		((IExecutionInformationHandler) m_model.getRuntimeInformation()).setWaitingInput(false);
		// Redirect the data to the consumers
		ProcExtensions.get().fireFinishPrompt(m_model);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController#abort
	 * ()
	 * =========================================================================
	 */
	@Override
	public void abort()
	{
		releaseExecutorCommand(ExecutorCommand.ABORT);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController#pause
	 * ()
	 * =========================================================================
	 */
	@Override
	public void pause()
	{
		releaseExecutorCommand(ExecutorCommand.PAUSE);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController#recover
	 * ()
	 * ========================================================================
	 * ==
	 */
	@Override
	public void recover()
	{
		releaseExecutorCommand(ExecutorCommand.RECOVER);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController#reload
	 * ()
	 * ========================================================================
	 * ==
	 */
	@Override
	public void reload()
	{
		releaseExecutorCommand(ExecutorCommand.RELOAD);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController#reload
	 * ()
	 * ========================================================================
	 * ==
	 */
	@Override
	public void save()
	{
		s_proxy.saveInterpreterInformation(m_model.getProcId());
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController#run()
	 * ==
	 * ========================================================================
	 */
	@Override
	public void run()
	{
		m_treeController.backToExecution();
		releaseExecutorCommand(ExecutorCommand.RUN);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController#skip
	 * ()
	 * ========================================================================
	 * ==
	 */
	@Override
	public void skip()
	{
		m_treeController.backToExecution();
		m_treeController.getCurrentNode().getCurrentLine().markExecuted(false);
		releaseExecutorCommand(ExecutorCommand.SKIP);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IProcedureController#step()
	 * ==========================================
	 * ================================
	 */
	@Override
	public void step()
	{
		m_treeController.backToExecution();
		m_treeController.setStepIntoOnce();
		releaseExecutorCommand(ExecutorCommand.STEP);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IProcedureController#stepOver()
	 * =========================================================================
	 */
	@Override
	public void stepOver()
	{
		m_treeController.backToExecution();
		m_treeController.setStepOverOnce();
		releaseExecutorCommand(ExecutorCommand.STEP_OVER);
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see IProcedureController#setVisibleNode(java.lang.String)
	 * ========================================================================
	 */
	@Override
	public void setVisibleNode(String stack)
	{
		int depth = m_treeController.setVisibleNode(stack);
		if (depth >= 0)
		{
			s_proxy.viewNodeAtDepth(m_model.getProcId(), depth);
		}
	}

	/***************************************************************************
	 * Send an executor command with the given command identifier
	 * 
	 * @param cmdId
	 *            the command identifier
	 * @args an array list with the arguments
	 **************************************************************************/
	private void releaseExecutorCommand(ExecutorCommand cmd, String[] args)
	{
		Logger.debug("Release command: " + cmd.getId(), Level.PROC, this);

		/*
		 * Add the procedure id at first position
		 */
		String[] argsWithProc = new String[args.length + 1];
		for (int i = 0; i < args.length; i++)
		{
			argsWithProc[i + 1] = args[i];
		}
		argsWithProc[0] = m_model.getProcId();

		/*
		 * Issue the command
		 */
		s_proxy.command(cmd, argsWithProc);
	}

	/***************************************************************************
	 * Send an executor command with the given command identifier
	 * 
	 * @param cmdId
	 *            the command identifier
	 **************************************************************************/
	private void releaseExecutorCommand(ExecutorCommand cmd)
	{
		releaseExecutorCommand(cmd, new String[0]);
	}
}
