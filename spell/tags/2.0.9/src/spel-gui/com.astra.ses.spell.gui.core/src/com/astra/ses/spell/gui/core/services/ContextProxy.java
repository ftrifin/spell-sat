///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : ContextProxy.java
//
// DATE      : 2008-11-21 08:58
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import com.astra.ses.spell.gui.core.CoreExtensions;
import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.comm.messages.SPELLlistenerLost;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageAttachExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageBreakpoint;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageChangeVariable;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClearBreakpoints;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClientInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClose;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCloseExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDetachExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageError;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecConfig;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetInstance;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetVariables;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageKillExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageNoWatchVariable;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOneway;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOpenExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcCode;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageServerFile;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageSetConfig;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageViewNodeDepth;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageWatchNothing;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageWatchVariable;
import com.astra.ses.spell.gui.core.exceptions.CommException;
import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.files.FileTransferData;
import com.astra.ses.spell.gui.core.model.files.IServerFile;
import com.astra.ses.spell.gui.core.model.files.LogFile;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.VariableData;
import com.astra.ses.spell.gui.core.model.notification.WhichVariables;
import com.astra.ses.spell.gui.core.model.server.ClientInfo;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * @brief Provides access to the SPEL context services
 * @date 20/05/08
 ******************************************************************************/
public class ContextProxy extends BasicProxy
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Service identifier */
	public static final String	ID	= "com.astra.ses.spell.gui.ContextProxy";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	private ContextInfo	       m_ctxInfo;

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ContextProxy()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
		m_ctxInfo = null;
	}

	// ##########################################################################
	// SERVICE SETUP
	// ##########################################################################

	@Override
	/***************************************************************************
	 * Setup the service
	 **************************************************************************/
	public void setup()
	{
		Logger.debug("Setup done", Level.INIT, this);
	}

	@Override
	/***************************************************************************
	 * Cleanup the service
	 **************************************************************************/
	public void cleanup()
	{
		if (isConnected())
		{
			try
			{
				m_interface.disconnect();
			}
			catch (CommException ex)
			{
				m_interface.forceDisconnect();
			}
			m_ctxInfo = null;
		}
	}

	// ##########################################################################
	// CONTEXT SERVICES
	// ##########################################################################

	/***************************************************************************
	 * Attach to context
	 **************************************************************************/
	void attach(ContextInfo ctxInfo)
	{
		if (isConnected())
		{
			detach();
		}
		m_ctxInfo = ctxInfo;
		m_interface.configure(ctxInfo.getServerInfo());
		m_interface.setCommListener(this);
		connect();
		if (m_ctxInfo != null)
		{
			Logger.info(
			        "Context is " + ctxInfo.getHost() + ":" + ctxInfo.getPort(),
			        Level.COMM, this);
			CoreExtensions.get().fireContextAttached(m_ctxInfo);
		}
	}

	/***************************************************************************
	 * Detach to from context on server
	 **************************************************************************/
	void detach()
	{
		if (isConnected())
		{
			logout();
			disconnect();
			CoreExtensions.get().fireContextDetached();
			Logger.info("Context detached", Level.COMM, this);
		}
	}

	/***************************************************************************
	 * Close a context on the server
	 * 
	 * @param ctxName
	 *            The context name
	 **************************************************************************/
	void close()
	{
		if (isConnected())
		{
			logout();
			closeIt();
			disconnect();
			CoreExtensions.get().fireContextDetached();
			Logger.info("Context detached", Level.COMM, this);
		}
	}

	// ##########################################################################
	// INFORMATION SERVICES
	// ##########################################################################

	/***************************************************************************
	 * Obtain the currently selected context
	 * 
	 * @return The current context
	 **************************************************************************/
	public String getCurrentContext()
	{
		if (m_ctxInfo == null) return null;
		return m_ctxInfo.getName();
	}

	/***************************************************************************
	 * Obtain GUI key
	 **************************************************************************/
	public String getClientKey()
	{
		return m_interface.getKey();
	}

	/***************************************************************************
	 * Obtain the current context information
	 **************************************************************************/
	public ContextInfo getInfo()
	{
		return m_ctxInfo;
	}

	// ##########################################################################
	// CONTEXT OPERATIONS (EXTERNAL)
	// ##########################################################################

	/***************************************************************************
	 * Launch a new executor in the context
	 * 
	 * @param procedureId
	 *            The procedure identifier
	 * @return Executor status information
	 **************************************************************************/
	public void openExecutor(String procedureId, String condition,
	        Map<String, String> arguments) throws ContextError
	{
		Logger.debug("Opening executor " + procedureId, Level.COMM, this);
		try
		{
			// Build the request message
			SPELLmessageOpenExec msg = new SPELLmessageOpenExec(procedureId);
			if (condition != null)
			{
				msg.setCondition(condition);
			}
			if (arguments != null)
			{
				msg.setArguments(arguments);
			}
			// Send the request
			performRequest(msg, m_openTimeout);
		}
		catch (Exception ex)
		{
			throw new ContextError(ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Obtain an available ID for a procedure
	 **************************************************************************/
	public String getProcedureInstanceId(String procedureId)
	{
		String instanceId = null;
		try
		{
			// Build the request message
			SPELLmessageGetInstance msg = new SPELLmessageGetInstance(
			        procedureId);
			// Send the request
			SPELLmessage response = performRequest(msg);
			// Response will be null if failed
			if (response != null && response instanceof SPELLmessageResponse)
			{
				instanceId = SPELLmessageGetInstance.getInstance(response);
			}
		}
		catch (Exception ex)
		{
			throw new ContextError(ex.getLocalizedMessage());
		}
		return instanceId;
	}

	/***************************************************************************
	 * Close the given executor process
	 * 
	 * @param procedureId
	 *            The procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean closeExecutor(String procedureId)
	{
		Logger.debug("Closing executor " + procedureId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageCloseExec(procedureId);
			SPELLmessage response = performRequest(msg);
			if (response != null) { return true; }
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return false;
	}

	/***************************************************************************
	 * Kill the given executor process
	 * 
	 * @param procedureId
	 *            The procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean killExecutor(String procedureId)
	{
		Logger.debug("Killing executor " + procedureId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageKillExec(procedureId);
			SPELLmessage response = performRequest(msg);
			if (response != null) { return true; }
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return false;
	}

	/***************************************************************************
	 * Attach to the given executor
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public ExecutorInfo attachToExecutor(String procId, ClientMode mode)
	{
		Logger.debug("Attaching to executor " + procId + " in mode "
		        + ClientInfo.modeToString(mode), Level.COMM, this);
		ExecutorInfo info = null;
		try
		{
			SPELLmessage msg = new SPELLmessageAttachExec(procId, mode);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				info = new ExecutorInfo(procId);
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return info;
	}

	/***************************************************************************
	 * Detach from the given executor
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean detachFromExecutor(String procId)
	{
		Logger.debug("Detaching from executor " + procId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageDetachExec(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null) { return true; }
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return false;
	}

	/***************************************************************************
	 * Obtain the list of running executors
	 * 
	 * @return Executor name list
	 **************************************************************************/
	public Vector<String> getAvailableExecutors()
	{
		Vector<String> executors = new Vector<String>();
		try
		{
			Logger.debug("Retrieving available executors", Level.COMM, this);
			SPELLmessage request = new SPELLmessageExecList();
			SPELLmessage response = performRequest(request);
			if (response != null)
			{
				String list = SPELLmessageExecList.getExecListFrom(response);
				if (list != null)
				{
					StringTokenizer tokenizer = new StringTokenizer(list, ",");
					int count = tokenizer.countTokens();
					if (count > 0)
					{
						int index = 0;
						while (tokenizer.hasMoreTokens())
						{
							String token = tokenizer.nextToken();
							Logger.debug("Found executor: " + token,
							        Level.COMM, this);
							executors.addElement(token);
							index++;
						}
					}
				}
				Logger.debug("Active executors: " + executors.size(),
				        Level.PROC, this);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return executors;
	}

	/***************************************************************************
	 * Obtain executor information
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return Executor details
	 **************************************************************************/
	public ExecutorInfo getExecutorInfo(String procId)
	{
		ExecutorInfo info = null;
		try
		{
			Logger.debug("Retrieving executor information: " + procId,
			        Level.COMM, this);
			SPELLmessage msg = new SPELLmessageExecInfo(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				info = new ExecutorInfo(procId);
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			info = null;
		}
		return info;
	}

	/***************************************************************************
	 * Obtain a log file
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @param logId
	 *            Kind of log (AsRun or ExecutorLog)
	 * @return log file data
	 **************************************************************************/
	public IServerFile getServerFile(String procId, ServerFileType typeId)
	{
		IServerFile file = null;
		try
		{
			ArrayList<String> lines = null;
			// The server file data may be splitted into chunks
			boolean getMoreChunks = true;
			SPELLmessage msg = null;
			SPELLmessage response = null;
			// This class holds chunks of the file
			FileTransferData chunk = null;
			int chunkNo = 0;

			while (getMoreChunks)
			{
				// If chunkNo == 0, it is the initial request
				if (chunkNo == 0)
				{
					msg = new SPELLmessageServerFile(procId, typeId);
				}
				// Subsequent requests
				else
				{
					msg = new SPELLmessageServerFile(procId, typeId, chunkNo);
				}
				// Perform the request
				response = performRequest(msg);

				// Process the response and obtain the transfer data
				if (response != null)
				{
					chunk = SPELLmessageServerFile.getDataFrom(response);
				}

				// If data is not chunked
				if (chunk.getTotalChunks() == 0)
				{
					lines = chunk.getLines();
					getMoreChunks = false;
				}
				// Else if this is the last chunk to obtain
				else if (chunk.getChunkNo() == chunk.getTotalChunks()) // This
																	   // is the
																	   // last
																	   // chunk
				{
					if (lines == null) lines = new ArrayList<String>();
					lines.addAll(chunk.getLines());
					getMoreChunks = false;
				}
				// Otherwise, get the next chunk
				else
				{
					if (lines == null) lines = new ArrayList<String>();
					lines.addAll(chunk.getLines());
					if (chunkNo < chunk.getTotalChunks() - 1)
					{
						getMoreChunks = true;
						chunkNo = chunk.getChunkNo() + 1;
					}
					else
					{
						getMoreChunks = false;
					}
				}
			}

			// We got the lines, create the file
			switch (typeId)
			{
			case ASRUN:
				file = new AsRunFile(procId, lines);
				break;
			case EXECUTOR_LOG:
				file = new LogFile(procId, lines);
				break;
			}
		}
		catch (Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.PROC, this);
		}
		return file;
	}

	/***************************************************************************
	 * Obtain executor information
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 * @return Executor details
	 **************************************************************************/
	public void updateExecutorInfo(String procId, ExecutorInfo info)
	{
		try
		{
			Logger.debug("Retrieving executor information: " + procId,
			        Level.COMM, this);
			SPELLmessage msg = new SPELLmessageExecInfo(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			info = null;
		}
	}

	/***************************************************************************
	 * Obtain other client information
	 * 
	 * @param client
	 *            key Client identifier
	 * @return Client details
	 **************************************************************************/
	public ClientInfo getClientInfo(String clientKey)
	{
		ClientInfo info = null;
		try
		{
			Logger.debug("Retrieving client information: " + clientKey,
			        Level.COMM, this);
			SPELLmessage msg = new SPELLmessageClientInfo(clientKey);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				info = new ClientInfo();
				SPELLmessageClientInfo.fillClientInfo(info, response);
				Logger.debug("Client information obtained: " + clientKey,
				        Level.COMM, this);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			info = null;
		}
		return info;
	}

	/***************************************************************************
	 * Obtain the list of available procedures for a given profile
	 * 
	 * @return A list of procedure identifiers
	 **************************************************************************/
	public Map<String, String> getAvailableProcedures()
	{
		Map<String, String> procs = new HashMap<String, String>();
		if (isConnected())
		{
			try
			{
				SPELLmessage msg = new SPELLmessageProcList();
				SPELLmessage response = performRequest(msg);

				if (response != null)
				{
					// Parse the list of available procedures (comma separated)
					/*
					 * Returned list contains tuples of procIDs and Procedures
					 * Names this way procID1|procName1,procID2|procName2,
					 */
					String list = SPELLmessageProcList
					        .getProcListFrom(response);
					if (list != null)
					{
						StringTokenizer tokenizer = new StringTokenizer(list,
						        ",");
						int count = tokenizer.countTokens();
						if (count > 0)
						{
							while (tokenizer.hasMoreTokens())
							{
								String token = tokenizer.nextToken();
								String[] splittedToken = token.split("\\|");
								procs.put(splittedToken[0], splittedToken[1]);
							}
						}
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			}
		}
		return procs;
	}

	/***************************************************************************
	 * Obtain the procedure code
	 * 
	 * @param procedureId
	 *            The procedure identifier
	 * @return The procedure source lines
	 **************************************************************************/
	public ArrayList<String> getProcedureCode(String procedureId)
	{
		ArrayList<String> code = null;
		try
		{
			boolean getMoreChunks = true;
			SPELLmessage msg = null;
			SPELLmessage response = null;
			FileTransferData data = null;
			int chunkNo = 0;

			while (getMoreChunks)
			{
				if (chunkNo == 0)
				{
					msg = new SPELLmessageProcCode(procedureId);
				}
				else
				{
					msg = new SPELLmessageProcCode(procedureId, chunkNo);
				}
				response = performRequest(msg);
				if (response != null)
				{
					data = SPELLmessageProcCode.getCodeFrom(response);
				}

				if (data.getTotalChunks() == 0) // Data is not chunked
				{
					code = data.getLines();
					getMoreChunks = false;
				}
				else if (data.getChunkNo() == data.getTotalChunks()) // This is
																	 // the last
																	 // chunk
				{
					if (code == null) code = new ArrayList<String>();
					code.addAll(data.getLines());
					getMoreChunks = false;
				}
				else
				// Get next chunk
				{
					if (code == null) code = new ArrayList<String>();
					code.addAll(data.getLines());
					if (chunkNo < data.getTotalChunks() - 1)
					{
						getMoreChunks = true;
						chunkNo = data.getChunkNo() + 1;
					}
					else
					{
						getMoreChunks = false;
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		if (code == null)
		{
			Logger.error("Unable to obtain procedure code", Level.COMM, this);
		}
		return code;
	}

	/***************************************************************************
	 * Obtain the given procedure properties
	 * 
	 * @param procedureId
	 *            The procedure identifier
	 * @return A map with the procedure properties
	 **************************************************************************/
	public TreeMap<ProcProperties, String> getProcedureProperties(
	        String procedureId)
	{
		TreeMap<ProcProperties, String> properties = null;
		try
		{
			SPELLmessageProcInfo msg = new SPELLmessageProcInfo(procedureId);
			SPELLmessage response = performRequest(msg);

			if (response != null)
			{
				properties = msg.getProcProperties(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
		}
		return properties;
	}

	// ##########################################################################
	// COMMAND MANAGEMENT
	// ##########################################################################

	/***************************************************************************
	 * Send a predefined command
	 * 
	 * @param cmd
	 *            ExecutorCommand identifier
	 **************************************************************************/
	public void command(ExecutorCommand cmd, String[] args)
	{
		try
		{
			Logger.debug("Executor command: " + cmd.toString(), Level.PROC,
			        this);
			SPELLmessageOneway msg = new SPELLmessageOneway(cmd.getId());
			processCommandArguments(cmd, msg, args);
			m_interface.sendMessage(msg);
			Logger.debug("Executor command sent: " + cmd, Level.COMM, this);
		}
		catch (Exception e)
		{
			Logger.error(e.getLocalizedMessage(), Level.COMM, this);
		}
	}

	/***************************************************************************
	 * Change executor configuration
	 * 
	 * @param procId
	 *            Procedure identifier
	 * @param config
	 *            Configuration map
	 **************************************************************************/
	public void setExecutorConfiguration(String procId, ExecutorConfig config)
	{
		try
		{
			SPELLmessageSetConfig req = new SPELLmessageSetConfig(procId);
			for (String key : config.getConfigMap().keySet())
			{
				req.set(key, config.getConfigMap().get(key));
			}
			SPELLmessage response = performRequest(req);
			if (response == null || response instanceof SPELLmessageError)
			{
				Logger.error("Unable to set executor configuration",
				        Level.PROC, this);
			}
		}
		catch (Exception e)
		{
			Logger.error(e.getLocalizedMessage(), Level.COMM, this);
		}

	}

	/***************************************************************************
	 * Obtain executor configuration
	 * 
	 * @param procId
	 *            Procedure (executor) identifier
	 **************************************************************************/
	public void updateExecutorConfig(String procId, ExecutorConfig config)
	{
		try
		{
			Logger.debug("Retrieving executor configuration: " + procId,
			        Level.COMM, this);
			SPELLmessage msg = new SPELLmessageExecConfig(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				SPELLmessageExecConfig.fillExecConfig(config, response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM, this);
			config = null;
		}
	}

	/***************************************************************************
	 * Toggle a breakpoint at the given line for the given procedure (its id)
	 * 
	 * @param procId
	 * @param lineNo
	 * @throws Exception
	 **************************************************************************/
	public void toggleBreakpoint(String procId, String codeId, int lineNo,
	        BreakpointType type) throws Exception
	{
		Logger.debug("Toggling breakpoint at line " + lineNo + " in " + procId,
		        Level.COMM, this);
		SPELLmessage msg = new SPELLmessageBreakpoint(procId, codeId, lineNo,
		        type);
		performRequest(msg);
	}

	/***************************************************************************
	 * A request for removing all the breakpoints for the given proc is sent
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public void clearBreakpoints(String procId) throws Exception
	{
		Logger.debug("Removing all breakpoints in " + procId, Level.COMM, this);
		SPELLmessage msg = new SPELLmessageClearBreakpoints(procId);
		performRequest(msg);
	}

	/***************************************************************************
	 * Request the list of global variables in the current scope
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public VariableData[] getGlobalVariables(String procId) throws Exception
	{
		Logger.debug("Requesting local variables in " + procId, Level.COMM,
		        this);
		SPELLmessage msg = new SPELLmessageGetVariables(procId,
		        WhichVariables.AVAILABLE_GLOBALS);
		SPELLmessage response = performRequest(msg);
		return SPELLmessageGetVariables.getVariables(
		        WhichVariables.AVAILABLE_GLOBALS, response);
	}

	/***************************************************************************
	 * Request the list of local variables in the current scope
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public VariableData[] getLocalVariables(String procId) throws Exception
	{
		Logger.debug("Requesting local variables in " + procId, Level.COMM,
		        this);
		SPELLmessage msg = new SPELLmessageGetVariables(procId,
		        WhichVariables.AVAILABLE_LOCALS);
		SPELLmessage response = performRequest(msg);
		VariableData[] list = null;
		if (response != null)
		{
			list = SPELLmessageGetVariables.getVariables(
			        WhichVariables.AVAILABLE_LOCALS, response);
		}
		return list;
	}

	/***************************************************************************
	 * Request the list of all variables in the current scope
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public VariableData[] getAllVariables(String procId) throws Exception
	{
		Logger.debug("Requesting all variables in " + procId, Level.COMM, this);
		SPELLmessage msg = new SPELLmessageGetVariables(procId,
		        WhichVariables.AVAILABLE_ALL);
		SPELLmessage response = performRequest(msg);
		VariableData[] list = null;
		if (response != null)
		{
			list = SPELLmessageGetVariables.getVariables(
			        WhichVariables.AVAILABLE_ALL, response);
		}
		return list;
	}

	/***************************************************************************
	 * Request the list of global variables in the current scope
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public VariableData[] getRegisteredGlobalVariables(String procId)
	        throws Exception
	{
		Logger.debug("Requesting local variables in " + procId, Level.COMM,
		        this);
		SPELLmessage msg = new SPELLmessageGetVariables(procId,
		        WhichVariables.REGISTERED_GLOBALS);
		SPELLmessage response = performRequest(msg);
		return SPELLmessageGetVariables.getVariables(
		        WhichVariables.REGISTERED_GLOBALS, response);
	}

	/***************************************************************************
	 * Request the list of local variables in the current scope
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public VariableData[] getRegisteredLocalVariables(String procId)
	        throws Exception
	{
		Logger.debug("Requesting local variables in " + procId, Level.COMM,
		        this);
		SPELLmessage msg = new SPELLmessageGetVariables(procId,
		        WhichVariables.REGISTERED_LOCALS);
		SPELLmessage response = performRequest(msg);
		VariableData[] list = null;
		if (response != null)
		{
			list = SPELLmessageGetVariables.getVariables(
			        WhichVariables.REGISTERED_LOCALS, response);
		}
		return list;
	}

	/***************************************************************************
	 * Request the list of all variables in the current scope
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public VariableData[] getRegisteredVariables(String procId)
	        throws Exception
	{
		Logger.debug("Requesting all variables in " + procId, Level.COMM, this);
		SPELLmessage msg = new SPELLmessageGetVariables(procId,
		        WhichVariables.REGISTERED_ALL);
		SPELLmessage response = performRequest(msg);
		VariableData[] list = null;
		if (response != null)
		{
			list = SPELLmessageGetVariables.getVariables(
			        WhichVariables.REGISTERED_ALL, response);
		}
		return list;
	}

	/***************************************************************************
	 * Request to register variable watch
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public VariableData registerVariableWatch(String procId, String varName,
	        boolean global) throws Exception
	{
		Logger.debug("Requesting variable watch in " + procId, Level.COMM, this);
		SPELLmessage msg = new SPELLmessageWatchVariable(procId, varName,
		        global);
		SPELLmessage response = performRequest(msg);
		VariableData data = null;
		if (response != null)
		{
			data = new VariableData(varName,
			        SPELLmessageWatchVariable.getType(response),
			        SPELLmessageWatchVariable.getValue(response), global, true);
		}
		return data;
	}

	/***************************************************************************
	 * Request to unregister variable watch
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public void unregisterVariableWatch(String procId, String varName,
	        boolean global) throws Exception
	{
		Logger.debug("Requesting variable no-watch in " + procId, Level.COMM,
		        this);
		SPELLmessage msg = new SPELLmessageNoWatchVariable(procId, varName,
		        global);
		performRequest(msg);
	}

	/***************************************************************************
	 * Request to unregister all variable watches
	 * 
	 * @param procId
	 *            the procedure's id
	 * @throws Exception
	 **************************************************************************/
	public void watchNothing(String procId) throws Exception
	{
		Logger.debug("Requesting variable no-watch in " + procId, Level.COMM,
		        this);
		SPELLmessage msg = new SPELLmessageWatchNothing(procId);
		performRequest(msg);
	}

	/***************************************************************************
	 * Request a variable change
	 * 
	 * @param procId
	 *            the procedure's id
	 * @param varName
	 *            the variable name
	 * @param varExpression
	 *            the value expression
	 * @param global
	 *            True if the variable is global
	 * 
	 * @throws Exception
	 **************************************************************************/
	public void changeVariable(String procId, String varName,
	        String valueExpression, boolean isGlobal) throws Exception
	{
		Logger.debug("Requesting variable change in " + procId, Level.COMM,
		        this);
		SPELLmessage msg = new SPELLmessageChangeVariable(procId, varName,
		        valueExpression, isGlobal);
		performRequest(msg);
	}

	/***************************************************************************
	 * Indicate the server that the visible on-execution node has been
	 * explicitly changed by the user
	 * 
	 * @param procId
	 *            the procId
	 * @param depth
	 *            the depth relative to the root node
	 **************************************************************************/
	public void viewNodeAtDepth(String procId, int depth)
	{
		Logger.debug("Switching view in " + procId + " to depth" + depth,
		        Level.COMM, this);
		SPELLmessageOneway cmd = new SPELLmessageViewNodeDepth(procId, depth);
		m_interface.sendMessage(cmd);
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Connect to the currently selected SPELL server
	 **************************************************************************/
	private void connect()
	{
		try
		{
			Logger.debug("Connecting context proxy", Level.COMM, this);
			// Get password from listener proxy if any
			ServerProxy lproxy = (ServerProxy) ServiceManager
			        .get(ServerProxy.ID);
			ServerInfo sinfo = lproxy.getCurrentServer();
			if (sinfo.getUser() != null)
			{
				m_ctxInfo.setUser(sinfo.getUser());
				m_ctxInfo.setPwd(sinfo.getPwd());
			}
			m_interface.configure(m_ctxInfo.getServerInfo());
			Logger.debug("Connecting IPC", Level.COMM, this);
			m_interface.connect();
			login();
		}
		catch (Exception e)
		{
			Logger.error("Connection failed: " + e.getLocalizedMessage(),
			        Level.COMM, this);
			m_interface.forceDisconnect();
			ErrorData data = new ErrorData(m_ctxInfo.getName(),
			        "Cannot connect", e.getLocalizedMessage(), true);
			m_ctxInfo = null;
			connectionFailed(data);
			return;
		}
	}

	/***************************************************************************
	 * Disconnect interface from SPELL context process
	 **************************************************************************/
	private void disconnect()
	{
		if (!m_interface.isConnected()) { return; }
		try
		{
			m_interface.disconnect();
		}
		catch (CommException e)
		{
			return;
		}
		finally
		{
			m_ctxInfo = null;
		}
		// Create the local peer and obtain the SPELL peer
		Logger.info("Connection closed", Level.COMM, this);
	}

	/***************************************************************************
	 * Close context
	 **************************************************************************/
	private void closeIt()
	{
		try
		{
			SPELLmessage msg = new SPELLmessageClose();
			m_interface.sendMessage(msg);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Clear context information buffer on failure
	 **************************************************************************/
	@Override
	public void connectionFailed(ErrorData data)
	{
		Logger.error("Unable to connect to context: " + data.getMessage(),
		        Level.COMM, this);
		data.setOrigin(m_ctxInfo.getName());
		m_ctxInfo = null;
		forceDisconnect();
		CoreExtensions.get().fireContextError(data);
	}

	/***************************************************************************
	 * Clear context information buffer on failure
	 **************************************************************************/
	@Override
	public void connectionLost(ErrorData data)
	{
		Logger.error("Context proxy connection lost: " + data.getMessage(),
		        Level.COMM, this);
		data.setOrigin(m_ctxInfo.getName());
		m_ctxInfo = null;
		CoreExtensions.get().fireContextError(data);
	}

	/***************************************************************************
	 * Workaround needed due to python bug in subprocess.Popen
	 **************************************************************************/
	@Override
	protected boolean listenerConnectionLost(SPELLlistenerLost error)
	{
		ServerProxy server = (ServerProxy) ServiceManager.get(ServerProxy.ID);
		if (server.isConnected())
		{
			server.forceDisconnect();
			server.connectionLost(error.getData());
		}
		return false;
	}

}
