///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : Procedure.java
//
// DATE      : 2008-11-24 08:34
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.gui.procs.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IProcedureRuntime;
import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.server.AsRunFile;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.server.TabbedFileLine;
import com.astra.ses.spell.gui.core.model.server.AsRunFile.AsRunLine;
import com.astra.ses.spell.gui.core.model.types.AsRunType;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.DisplayType;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.IProcProperties;
import com.astra.ses.spell.gui.core.model.types.ItemType;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.procs.ProcExtensions;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;



/*******************************************************************************
 * @brief Domain class that represents a set of procedure properties.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class Procedure implements IProcedureRuntime
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static ContextProxy s_proxy = null;
	
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the procedure identifier */
	private String m_procId;
	private Map<String,String> m_properties;
	/** Holds the execution information */
	private ExecutorInfo m_info;
	/** Holds the execution configuration  */
	private ExecutorConfig m_config;
	/** Holds the procedure execution model */
	private ProcedureCode m_code;
	/** Holds the set of displayed messages. Used only on replay */
	private ArrayList<DisplayData> m_replayMessages;
	/** True if awaiting for user input */
	private boolean m_waitingInput;
	
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Basic constructor
	 * 
	 * @param id
	 *            The procedure identifier
	 **************************************************************************/
	public Procedure(String procId)
	{
		super();
		if (s_proxy == null)
		{
			s_proxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		}
		m_procId = procId;
		m_info = new ExecutorInfo(procId);
		m_config = new ExecutorConfig();
		m_code = null;
		m_properties = null;
		initialize();
	}

	/***************************************************************************
	 * Set properties
	 **************************************************************************/
	public void setProperties( Map<String,String> props )
	{
		m_properties = props;
	}

	/***************************************************************************
	 * Get properties
	 **************************************************************************/
	public Map<String,String> getProperties()
	{
		return m_properties;
	}

	/***************************************************************************
	 * Refresh the model using remote executor information 
	 **************************************************************************/
	public void refresh()
	{
		if (s_proxy.isConnected())
		{
			Logger.debug("Refreshing execution model", Level.PROC, this);
			s_proxy.updateExecutorInfo(m_procId,m_info);
			Logger.debug("Execution model updated", Level.PROC, this);
			Logger.debug("Refreshing configuration model", Level.PROC, this);
			s_proxy.updateExecutorConfig(m_procId,m_config);
			Logger.debug("Configuration model updated", Level.PROC, this);
		}
	}

	/***************************************************************************
	 * Issue command to executor
	 **************************************************************************/
	public void issueCommand( ExecutorCommand cmd )
	{
		cmd.setProcId(m_procId);
		if (!cmd.validate(getStatus()))
		{
			throw new CommandFailed("Cannot execute command, invalid procedure status");
		}
		ProcedureCommands.issueCommand(cmd);
	}

	/***************************************************************************
	 * Set as waiting input
	 **************************************************************************/
	public void setWaitingInput( boolean waiting )
	{
		m_waitingInput = waiting;
	}

	/***************************************************************************
	 * Check if waiting input
	 **************************************************************************/
	public boolean isWaitingInput()
	{
		return m_waitingInput;
	}

	/***************************************************************************
	 * Change executor configuration
	 **************************************************************************/
	public void setRunInto( boolean enabled )
	{
		m_config.setRunInto(enabled);
		s_proxy.setExecutorConfiguration( m_procId, m_config );
		ProcExtensions.get().fireModelConfigured(m_procId);
	}

	/***************************************************************************
	 * Get executor configuration
	 **************************************************************************/
	public boolean getRunInto()
	{
		return m_config.getRunInto();
	}

	/***************************************************************************
	 * Change executor configuration
	 **************************************************************************/
	public void setExecDelay( double delay )
	{
		m_config.setExecDelay(delay);
		s_proxy.setExecutorConfiguration( m_procId, m_config );
		ProcExtensions.get().fireModelConfigured(m_procId);
	}


	/***************************************************************************
	 * Change executor configuration
	 **************************************************************************/
	public void setStepByStep( boolean enable )
	{
		m_config.setStepByStep(enable);
		s_proxy.setExecutorConfiguration( m_procId, m_config );
		ProcExtensions.get().fireModelConfigured(m_procId);
	}

	/***************************************************************************
	 * Change executor configuration
	 **************************************************************************/
	public boolean getStepByStep()
	{
		return m_config.getStepByStep();
	}

	/***************************************************************************
	 * Listener ID
	 **************************************************************************/
	public String getListenerID()
	{
		return m_procId;
	}

	/***************************************************************************
	 * Obtain the list of monitoring clients
	 **************************************************************************/
	public Vector<String> getMonitoringClients()
	{
		return m_info.getMonitoringClients();
	}

	/***************************************************************************
	 * Obtain the controlling client
	 **************************************************************************/
	public String getControllingClient()
	{
		return m_info.getControllingClient();
	}

	/***************************************************************************
	 * Obtain the controlling client
	 **************************************************************************/
	public ClientMode getClientMode()
	{
		return m_info.getMode();
	}

	/***************************************************************************
	 * Obtain the current executor status
	 **************************************************************************/
	public ExecutorStatus getStatus()
	{
		return m_info.getStatus();
	}

	/***************************************************************************
	 * Obtain the current executor wait condition
	 **************************************************************************/
	public String getCondition()
	{
		return m_info.getCondition();
	}

	/***************************************************************************
	 * Set the executor status
	 **************************************************************************/
	public void setStatus( ExecutorStatus st )
	{
		m_info.setStatus(st);
	}

	/***************************************************************************
	 * Obtain the current stack position
	 **************************************************************************/
	public Vector<String> getStackPosition()
	{
		return m_code.getStackPosition();
	}

	/***************************************************************************
	 * Obtain the current code (call stack position)
	 **************************************************************************/
	public ProcedureCode getCurrentCode()
	{
		return m_code.getCodeAt( m_code.getStackPosition() );
	}

	/***************************************************************************
	 * Obtain the source code to be shown by viewers (viewer position)
	 **************************************************************************/
	public ProcedureCode getCurrentViewCode()
	{
		return m_code.getCodeAt( m_code.getViewPosition() );
	}

	/***************************************************************************
	 * Obtain the root code
	 **************************************************************************/
	public ProcedureCode getRootCode()
	{
		return m_code;
	}

	/***************************************************************************
	 * Obtain code by identifier
	 **************************************************************************/
	public ProcedureCode getCodeById( String codeId )
	{
		return m_code.getCodeById( codeId );
	}

	/***************************************************************************
	 * Obtain the procedure identifier
	 * 
	 * @return The procedure identifier
	 **************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}

	/***************************************************************************
	 * Obtain the procedure name
	 * 
	 * @return The procedure name
	 **************************************************************************/
	public String getProcName()
	{
		String procName = "(unknown)";
		if (m_properties != null)
		{
			procName = m_properties.get(IProcProperties.PROC_NAME);
		}
		int idx = m_procId.indexOf("#");
		if (idx != -1)
		{
			procName += " (" + m_procId.substring(idx+1) + ")";
		}
		return procName;
	}

	/***************************************************************************
	 * Get the replay messages
	 * 
	 * @return The replay messages
	 **************************************************************************/
	public ArrayList<DisplayData> getReplayMessages()
	{
		return m_replayMessages;
	}

	/***************************************************************************
	 * Obtain the execution info
	 * 
	 * @return The execution info
	 **************************************************************************/
	public ExecutorInfo getInfo()
	{
		return m_info;
	}

	/***************************************************************************
	 * Set the execution info
	 **************************************************************************/
	public void setInfo( ExecutorInfo info, AsRunFile asRun, boolean replay ) throws LoadFailed
	{
		m_info = info;
		if (replay)
		{
			if (asRun != null)
			{
				Logger.info("Replaying execution", Level.PROC, this);
				if (replayExecution(asRun))
				{
					Logger.info("Replay done", Level.PROC, this);
				}
				else
				{
					throw new LoadFailed("Unable to load AsRun data");
				}
			}
			else
			{
				Logger.error("Unable to replay execution, no ASRUN information", Level.PROC, this);
			}
		}
	}

	/***************************************************************************
	 * Callback for code notifications
	 **************************************************************************/
	@Override
	public void procedureCode( CodeNotification data )
	{
		Logger.debug("Notified procedure '" + data.getProcId() + "' code", Level.COMM, this);
		if (data.getCode() != null && data.getCode().size() >0  )
		{
			Logger.debug("Received new code for " + data.getProcId() + ":" + data.getStackPosition() + "(" + data.getCode().size() + ")", Level.PROC, this);
			addCode( data );
		}
		else
		{
			Logger.debug("Notified code position change: " + data.getProcId() + ":" + data.getStackPosition(), Level.PROC, this);
			setStackPosition( data.getStackPosition() );
			
		}
	}

	/***************************************************************************
	 * Callback for item notifications
	 **************************************************************************/
	@Override
	public void procedureItem( ItemNotification data )
	{
		Logger.debug("NOTIFIED ITEM: " + data.getStackPosition(), Level.COMM, this);
		m_code.addNotification(data.getStackPosition(),data);
	}

	/***************************************************************************
	 * Callback for executed line changes
	 **************************************************************************/
	@Override
	public void procedureLine( LineNotification  data )
	{
		Logger.debug("NOTIFIED LINE: " + data.getStackPosition(), Level.COMM, this);
		setStackPosition(data.getStackPosition());
		setStage(data.getStageId(),data.getStageTitle());
	}

	/***************************************************************************
	 * Callback for status changes.
	 **************************************************************************/
	@Override
	public void procedureStatus( StatusNotification data )
	{
		ExecutorStatus status = data.getStatus();
		Logger.info("Notified procedure '" + data.getProcId() + "' status :" + status.toString(), Level.COMM, this);
		setStatus(status);
		if (status == ExecutorStatus.LOADED)
		{
			reset();
		}
	}

	/***************************************************************************
	 * Get current stage identifier
	 **************************************************************************/
	public String getStageId()
	{
		return m_info.getStageId();
	}

	/***************************************************************************
	 * Get current stage title
	 **************************************************************************/
	public String getStageTitle()
	{
		return m_info.getStageTitle();
	}

	/***************************************************************************
	 * Get parent procedure if any
	 **************************************************************************/
	public String getParent()
	{
		return m_info.getParent();
	}

	// =========================================================================
	// STATIC METHODS
	// =========================================================================

	
	@Override
	public void procedureDisplay(DisplayData data) { /* Not used */ }; 

	@Override
	public void procedureError(ErrorData data) { /* Not used */ }

	@Override
	public String getListenerId()
	{
		return m_procId;
	}

	/***************************************************************************
	 * Initialize the model
	 **************************************************************************/
	private boolean initialize()
	{
		// No replay messages
		m_replayMessages = null;
		// No prompt active
		m_waitingInput = false;
		// Build the code part
		String codeId = rawID(m_procId);
		try
		{
			m_code = new ProcedureCode(codeId,null);
		}
		catch (LoadFailed lf)
		{
			return false;
		}
		return true;
	}

	/***************************************************************************
	 * Position the stack
	 * 
	 * @param stack The stack
	 *
	 **************************************************************************/
	private boolean setStackPosition( Vector<String> stack )
	{
		//Logger.debug("Set stack position: " + stack, Level.PROC, this);
		m_code.setStackPosition(stack);
		return true;
	}

	/***************************************************************************
	 * Add new source code
	 * 
	 * @param code The procedure code model
	 *
	 **************************************************************************/
	private void addCode( CodeNotification data )
	{
		Logger.debug("Add new code: " + data.getStackPosition(), Level.PROC, this);
		m_code.addProcedureCode( data.getStackPosition(), data.getCode() );
		m_code.setStackPosition( data.getStackPosition() );
	}

	/***************************************************************************
	 * Reset model
	 **************************************************************************/
	private void reset()
	{
		Logger.debug("Reset execution model: " + m_procId, Level.PROC, this);
		if (m_code != null)
		{
			m_code.reset();
		}
		m_info.reset();
		m_waitingInput = false;
		Logger.debug("Execution model reset", Level.PROC, this);
	}

	/***************************************************************************
	 * Reproduce an execution session using an AsRun file 
	 **************************************************************************/
	private boolean replayExecution( AsRunFile file )
	{
		Logger.info("Start execution replay on " + m_procId, Level.PROC, this);
		ExecutorStatus lastStatus = ExecutorStatus.UNKNOWN;
		String lastCSP = "";
		int count = 1;
		LineNotification currentLine = null;
		CodeNotification currentCode = null;
		StatusNotification currentStatus = null;
		
		boolean retrievedStatus = false;
		boolean retrievedLine = false;
		
		Logger.debug("AsRun lines: " + file.getData().size(), Level.PROC, this);
		boolean result = true;
		for(TabbedFileLine tabbedLine : file.getData())
		{
			AsRunLine arLine = (AsRunLine) tabbedLine;
			try
			{
				if (arLine.type == AsRunType.LINE)
				{
					lastCSP = arLine.elements.get(0);
					currentLine = new LineNotification( m_procId, lastCSP );
					currentLine.setExecutionMode(ExecutionMode.REPLAY);
					procedureLine(currentLine);
					retrievedLine = true;
				}
				else if (arLine.type == AsRunType.STAGE)
				{
					String stageData = arLine.getTypeValue();
					if (currentLine == null)
					{
						currentLine = new LineNotification( m_procId, lastCSP );
					}
					String stageIT[] = stageData.split(",");
					if (stageIT.length==2)
					{
						currentLine.setStage(stageIT[0], stageIT[1]);
					}
					else
					{
						currentLine.setStage(stageIT[0], "");
					}
					currentLine.setExecutionMode(ExecutionMode.REPLAY);
					procedureLine(currentLine);
				}
				else if (arLine.type == AsRunType.CODE)
				{
					lastCSP = arLine.elements.get(0);
					currentCode = new CodeNotification(m_procId, lastCSP );
					currentCode.setExecutionMode(ExecutionMode.REPLAY);
					procedureCode(currentCode);
				}
				else if (arLine.type == AsRunType.ITEM)
				{
					lastCSP = arLine.elements.get(0);
					String itemTypeStr = arLine.elements.get(1);
					ItemType type = ItemType.fromName(itemTypeStr);
					String itemName    = arLine.elements.get(2);
					String itemValue   = arLine.elements.get(3);
					String itemStatus  = arLine.elements.get(4);
					String itemReason  = arLine.elements.get(5);
					String itemTime    = arLine.elements.get(6);
					ItemNotification item = new ItemNotification(m_procId, type, lastCSP);
					item.setItems(itemName,itemValue,itemStatus,itemReason,itemTime);
					item.setExecutionMode(ExecutionMode.REPLAY);
					procedureItem(item);
				}
				else if (arLine.type == AsRunType.DISPLAY)
				{
					String displaySev = arLine.elements.get(2);
					Severity sev = Severity.valueOf(displaySev);
					String msg = arLine.elements.get(1);
					msg = msg.replace("%C%", "\n");
					DisplayData display = new DisplayData(m_procId, msg, DisplayType.DISPLAY, sev );
					if (m_replayMessages == null)
					{
						m_replayMessages = new ArrayList<DisplayData>();
					}
					m_replayMessages.add(display);
				}
				else if (arLine.type == AsRunType.PROMPT)
				{
					if (m_replayMessages == null)
					{
						m_replayMessages = new ArrayList<DisplayData>();
					}
					String msg = arLine.elements.get(1);
					DisplayData display1 = new DisplayData(m_procId, "REQUIRED USER INPUT", DisplayType.DISPLAY, Severity.PROMPT );
					m_replayMessages.add(display1);
					DisplayData display2 = new DisplayData(m_procId, msg, DisplayType.DISPLAY, Severity.PROMPT );
					m_replayMessages.add(display2);
				}
				else if (arLine.type == AsRunType.ANSWER)
				{
					String msg = arLine.elements.get(1);
					DisplayData display = new DisplayData(m_procId, "ANSWER: " + msg, DisplayType.DISPLAY, Severity.PROMPT );
					if (m_replayMessages == null)
					{
						m_replayMessages = new ArrayList<DisplayData>();
					}
					m_replayMessages.add(display);
				}
				else if (arLine.type == AsRunType.ERROR)
				{
				}
				else if (arLine.type == AsRunType.STATUS)
				{
					lastStatus = ExecutorStatus.valueOf(arLine.elements.get(0));
					currentStatus = new StatusNotification(m_procId,lastStatus);
					currentStatus.setExecutionMode(ExecutionMode.REPLAY);
					procedureStatus(currentStatus);
					retrievedStatus = true;
				}
				else
				{
					Logger.error("Unknown AsRun data in line " + count, Level.PROC, this);
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				Logger.error("Failed to process ASRUN line " + count, Level.PROC, this);
				Logger.error("   " + ex.getLocalizedMessage(), Level.PROC, this);
				result = false;
			}
			count++;
		}
		if (retrievedLine == false || retrievedStatus == false)
		{
			Logger.error("Unable to process status or current line", Level.PROC, this);
			result = false;
		}
		Logger.info("Finished execution replay on " + m_procId, Level.PROC, this);
		return result;
	}
	
	/***************************************************************************
	 * Set current stage
	 **************************************************************************/
	private void setStage( String id, String title )
	{
		if (id!=null)
		{
			m_info.setStage(id,title);
		}
	}

	/***************************************************************************
	 * Obtain the raw procedure identifier
	 * 
	 * @return The raw procedure identifier
	 **************************************************************************/
	private static String rawID( String procId )
	{
		int idx = procId.indexOf("#"); 
		if (idx!=-1)
		{
			procId = procId.substring(0, idx);
		}
		return procId;
	}
}
