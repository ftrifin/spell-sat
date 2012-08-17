////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : RemoteProcedure.java
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

import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.extensionpoints.IProcedureRuntimeExtension;
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
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionTreeInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;
import com.astra.ses.spell.gui.procs.interfaces.model.priv.IExecutionInformationHandler;

/*******************************************************************************
 * 
 * Procedure holds the static and interactive procedure information: - Holds the
 * source code blocks to execute - Holds the runtime information that can be
 * handled by the user * Breakpoints, run into mode, step by step execution
 * 
 ******************************************************************************/
public class RemoteProcedure implements IProcedure
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	/** Context proxy */
	private static IContextProxy s_proxy = null;

	/*
	 * Static block to retrieve the context proxy
	 */
	static
	{
		s_proxy = (IContextProxy) ServiceManager.get(IContextProxy.class);
	}

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	
	class RemoteController implements IProcedureController
	{
        @Override
        public void notifyProcedurePrompt(InputData inputData){}
        @Override
        public void notifyProcedureCancelPrompt(InputData inputData){}
        @Override
        public void notifyProcedureFinishPrompt(InputData inputData){}
        @Override
        public String getListenerId(){ return null; }
        @Override
        public void setError(ErrorData data){}
        @Override
        public void abort() {}
        @Override
        public void clearBreakpoints() {}
        @Override
        public void gotoLine(int lineNumber) {}
        @Override
        public void gotoLabel(String label) {}
        @Override
        public void pause() {}
        @Override
        public void issueCommand(ExecutorCommand cmd, String[] args) throws CommandFailed {}
        @Override
        public void recover() {}
        @Override
        public void save() {}
        @Override
        public void reload() {}
        @Override
        public void refresh() throws Exception
        {
        	updateInfoFromRemote();
        }
        public void updateInfo() throws Exception
        {
        	updateInfoFromRemote();
        }
        public void updateConfig() throws Exception {};
        @Override
        public void run() {}
        @Override
        public void script(String script){}
        @Override
        public void setBrowsableLib(boolean showLib) {}
        @Override
        public void setRunInto(boolean runInto) {}
        @Override
        public void setStepByStep(boolean value) {}
        @Override
        public void setExecutionDelay(int msec) {}
        @Override
        public void setExecutorStatus(ExecutorStatus status) {}
        @Override
        public void setBreakpoint(int lineNumber, BreakpointType type) throws UninitProcedureException {}
        @Override
        public void skip() {}
        @Override
        public InputData getPromptData() { return null; }
        @Override
        public void step() {}
        @Override
        public void stepOver() {}
        @Override
        public void setVisibleNode(String stack) {}
	};
	
	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the procedure identifier */
	private String	                     m_procId;
	/** Procedure properties */
	private Map<ProcProperties, String>	 m_properties;
	/** Executor information */
	private IExecutionInformationHandler m_executionInformation;
	/** Fake controller for remote updates */
	private IProcedureController         m_controller;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public RemoteProcedure(String procId)
	{
		m_procId = procId;
		m_properties = new HashMap<ProcProperties,String>();//TODO
		m_executionInformation = new ExecutionInformationHandler(ClientMode.UNKNOWN);
		m_controller = new RemoteController();
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public RemoteProcedure( IProcedure wasLocalProcedure )
	{
		m_procId = wasLocalProcedure.getProcId();
		m_properties = new HashMap<ProcProperties,String>();
		for( ProcProperties prop : ProcProperties.values())
		{
			m_properties.put(prop, wasLocalProcedure.getProperty(prop));
		}
		m_controller = new RemoteController();
		m_executionInformation = new ExecutionInformationHandler(wasLocalProcedure.getRuntimeInformation().getClientMode());
		IExecutorInfo info = (IExecutorInfo) wasLocalProcedure.getAdapter(ExecutorInfo.class);
		m_properties.put(ProcProperties.PROC_NAME, info.getName());
		m_executionInformation.copyFrom(info);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void updateInfoFromRemote() throws Exception
	{
		IExecutorInfo info = new ExecutorInfo(m_procId);
		s_proxy.updateExecutorInfo(m_procId, info);
		m_executionInformation.copyFrom(info);
		ExecutorConfig cfg = new ExecutorConfig(m_procId);
		s_proxy.updateExecutorConfig(m_procId, cfg);
		m_properties.put(ProcProperties.PROC_NAME, info.getName());
		m_executionInformation.copyFrom(cfg);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureController getController()
	{
		return m_controller;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutionTreeInformation getExecutionTree()
	{
		return null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureDataProvider getDataProvider()
	{
		return null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureRuntimeExtension getRuntimeProcessor()
	{
		return null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProcId()
	{
		return m_procId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProcName()
	{
		return getProperty(ProcProperties.PROC_NAME);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getParent()
	{
		return getRuntimeInformation().getParent();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProperty(ProcProperties property)
	{
		return m_properties.get(property);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutionInformation getRuntimeInformation()
	{
		return m_executionInformation;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setReplayMode(boolean doingReplay) {};

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isInReplayMode() { return false; };

	/***************************************************************************
	 * 
	 **************************************************************************/
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter)
	{
		Object result = null;
		if (adapter.equals(ExecutorInfo.class))
		{
			IExecutorInfo info = new ExecutorInfo(m_procId);
			// TODO info.setParent(getParent().getProcId());
			getRuntimeInformation().visit(info);
			result = info;
		}
		else if (adapter.equals(ExecutorConfig.class))
		{
			ExecutorConfig cfg = new ExecutorConfig(m_procId);
			getRuntimeInformation().visit(cfg);
			result = cfg;
		}
		return result;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void reset() {};
}
