///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : ContextProxy.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.services;

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
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClientInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClose;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCloseExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCommand;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDetachExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageError;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecConfig;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageGetInstance;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageKillExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOpenExec;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcCode;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageProcList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageServerFile;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageSetConfig;
import com.astra.ses.spell.gui.core.exceptions.CommException;
import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ClientInfo;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.server.FileTransferData;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;


/*******************************************************************************
 * @brief Provides access to the SPEL context services
 * @date 20/05/08
 * @author Rafael Chinchilla Camara (GMV)
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
	public static final String ID = "com.astra.ses.spell.gui.ContextProxy";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	private ContextInfo m_ctxInfo;
	 
	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ContextProxy()
	{
		super(ID);
		Logger.debug("Created", Level.INIT,this);
		m_ctxInfo = null;
	}

	//##########################################################################
	// SERVICE SETUP 
	//##########################################################################

	@Override
	/***************************************************************************
	 * Setup the service
	 **************************************************************************/
	public void setup()	
	{
		Logger.debug("Setup done", Level.INIT,this);
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
			catch(CommException ex)
			{
				m_interface.forceDisconnect();
			}
			m_ctxInfo = null;
		}
	}
	
	//##########################################################################
	// CONTEXT SERVICES 
	//##########################################################################

	/***************************************************************************
	 * Attach to context
	 **************************************************************************/
	void attach( ContextInfo ctxInfo )
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
			Logger.info("Context is " + ctxInfo.getHost() + ":" + ctxInfo.getPort(), Level.COMM, this);
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
	 * 		The context name
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

	//##########################################################################
	// INFORMATION SERVICES 
	//##########################################################################

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

	//##########################################################################
	// CONTEXT OPERATIONS (EXTERNAL)  
	//##########################################################################

	/***************************************************************************
	 * Launch a new executor in the context
	 * 
	 * @param procedureId
	 * 		The procedure identifier
	 * @return
	 * 		Executor status information
	 **************************************************************************/
	public ExecutorInfo openExecutor( String procedureId, String condition, 
			Map<String,String> arguments ) throws ContextError
	{
		Logger.debug("Opening executor " + procedureId, Level.COMM, this);
		ExecutorInfo info = null;
		try
		{
			// Build the request message 
			SPELLmessageOpenExec msg = new SPELLmessageOpenExec( procedureId );
			if (condition != null)
			{
				msg.setCondition( condition );
			}
			if (arguments != null)
			{
				msg.setArguments(arguments);
			}
			// Send the request
			SPELLmessage response = performRequest(msg, m_openTimeout);
			// Response will be null if failed
			if (response != null && response instanceof SPELLmessageResponse)
			{
				info = new ExecutorInfo(procedureId);
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
			else if ( response instanceof SPELLmessageError )
			{
				System.err.println("RESPONSE ERROR TO IMPLEMENT");
			}
		}
		catch(Exception ex)
		{
			throw new ContextError(ex.getLocalizedMessage());
		}
		return info;
	}

	/***************************************************************************
	 * Obtain an available ID for a procedure
	 **************************************************************************/
	public String getProcedureInstanceId( String procedureId )
	{
		String instanceId = null;
		try
		{
			// Build the request message 
			SPELLmessageGetInstance msg = new SPELLmessageGetInstance( procedureId );
			// Send the request
			SPELLmessage response = performRequest(msg);
			// Response will be null if failed
			if (response != null && response instanceof SPELLmessageResponse)
			{
				instanceId = SPELLmessageGetInstance.getInstance(response); 
			}
		}
		catch(Exception ex)
		{
			throw new ContextError(ex.getLocalizedMessage());
		}
		return instanceId;
	}
	
	/***************************************************************************
	 * Close the given executor process
	 * 
	 * @param procedureId
	 * 		The procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean closeExecutor( String procedureId )
	{
		Logger.debug("Closing executor " + procedureId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageCloseExec( procedureId );
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				return true;
			}
		}
		catch(Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		return false;
	}

	/***************************************************************************
	 * Kill the given executor process
	 * 
	 * @param procedureId
	 * 		The procedure (executor) identifier
	 * @return True if success
	 **************************************************************************/
	public boolean killExecutor( String procedureId )
	{
		Logger.debug("Killing executor " + procedureId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageKillExec( procedureId );
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				return true;
			}
		}
		catch(Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		return false;
	}

	/***************************************************************************
	 * Attach to the given executor
	 * 
	 * @param procId
	 * 		Procedure (executor) identifier
	 * @return	
	 * 		True if success
	 **************************************************************************/
	public ExecutorInfo attachToExecutor(String procId, ClientMode mode)
	{
		Logger.debug("Attaching to executor " + procId + " in mode " + ClientInfo.modeToString(mode), Level.COMM, this);
		ExecutorInfo info = null;
		try
		{
			SPELLmessage msg = new SPELLmessageAttachExec( procId, mode );
			SPELLmessage response = performRequest(msg);
			if (response != null) 
			{
				info = new ExecutorInfo(procId);
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
		}
		catch(Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		return info;
	}

	/***************************************************************************
	 * Detach from the given executor
	 * 
	 * @param procId
	 * 		Procedure (executor) identifier
	 * @return	
	 * 		True if success
	 **************************************************************************/
	public boolean detachFromExecutor(String procId)
	{
		Logger.debug("Detaching from executor " + procId, Level.COMM, this);
		try
		{
			SPELLmessage msg = new SPELLmessageDetachExec( procId );
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				return true;
			}
		}
		catch(Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		return false;
	}

	/***************************************************************************
	 * Obtain the list of running executors
	 * 
	 * @return	
	 * 		Executor name list
	 **************************************************************************/
	public Vector<String> getAvailableExecutors()
	{
		Vector<String> executors = new Vector<String>();
		try
		{
			Logger.debug("Retrieving available executors", Level.COMM,this);
			SPELLmessage request = new SPELLmessageExecList();
			SPELLmessage response = performRequest(request);
			if (response != null)
			{
				String list = SPELLmessageExecList.getExecListFrom(response);
				if (list!=null)
				{
					StringTokenizer tokenizer = new StringTokenizer(list, ",");
					int count = tokenizer.countTokens();
					if (count > 0)
					{
						int index = 0;
						while (tokenizer.hasMoreTokens())
						{
							String token = tokenizer.nextToken();
							Logger.debug("Found executor: " + token, Level.COMM,this);
							executors.addElement(token);
							index++;
						}
					}
				}
				Logger.debug("Active executors: " + executors.size(), Level.PROC, this);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		return executors;
	}

	/***************************************************************************
	 * Obtain executor information
	 * 
	 * @param procId
	 * 		Procedure (executor) identifier
	 * @return	
	 * 		Executor details
	 **************************************************************************/
	public ExecutorInfo getExecutorInfo(String procId )
	{
		ExecutorInfo info = null;
		try
		{
			Logger.debug("Retrieving executor information: " + procId, Level.COMM,this);
			SPELLmessage msg = new SPELLmessageExecInfo( procId );
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				info = new ExecutorInfo(procId);
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
		}
		catch(Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
			info = null;
		}
		return info;
	}

	/***************************************************************************
	 * Obtain a log file
	 * 
	 * @param procId
	 * 		Procedure (executor) identifier
	 * @param logId
	 * 		Kind of log (AsRun or ExecutorLog)
	 * @return	
	 * 		log file data
	 **************************************************************************/
	public Vector<String> getServerFile( String procId, ServerFileType typeId )
	{
		Vector<String> lines = null;
		try
		{
			boolean getMoreChunks = true;
			SPELLmessage msg = null;
			SPELLmessage response = null;
			FileTransferData data = null;
			int chunkNo = 0;
			
			while(getMoreChunks)
			{
				if (chunkNo == 0)
				{
					msg = new SPELLmessageServerFile( procId, typeId );
				}
				else
				{
					msg = new SPELLmessageServerFile( procId, typeId, chunkNo );
				}
				response = performRequest(msg);
				if (response != null)
				{
					data = SPELLmessageServerFile.getDataFrom(response);
				}

				if (data.getTotalChunks()==0) // Data is not chunked
				{
					lines = data.getLines();
					getMoreChunks = false;
				}
				else if (data.getChunkNo() == data.getTotalChunks()) // This is the last chunk
				{
					if (lines == null) lines = new Vector<String>();
					lines.addAll(data.getLines());
					getMoreChunks = false;
				}
				else // Get next chunk
				{
					if (lines == null) lines = new Vector<String>();
					lines.addAll(data.getLines());
					if (chunkNo < data.getTotalChunks()-1)
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
		catch(Exception ex)
		{
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		return lines;
	}

	/***************************************************************************
	 * Obtain executor information
	 * 
	 * @param procId
	 * 		Procedure (executor) identifier
	 * @return	
	 * 		Executor details
	 **************************************************************************/
	public void updateExecutorInfo(String procId, ExecutorInfo info)
	{
		try
		{
			Logger.debug("Retrieving executor information: " + procId, Level.COMM,this);
			SPELLmessage msg = new SPELLmessageExecInfo(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				SPELLmessageExecInfo.fillExecInfo(info, response);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
			info = null;
		}
	}

	/***************************************************************************
	 * Obtain other client information
	 * 
	 * @param client key
	 * 		Client identifier
	 * @return	
	 * 		Client details
	 **************************************************************************/
	public ClientInfo getClientInfo(String clientKey)
	{
		ClientInfo info = null;
		try
		{
			Logger.debug("Retrieving client information: " + clientKey, Level.COMM,this);
			SPELLmessage msg = new SPELLmessageClientInfo( clientKey );
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				info = new ClientInfo();
				SPELLmessageClientInfo.fillClientInfo(info,response);
				Logger.debug("Client information obtained: " + clientKey, Level.COMM,this);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
			info = null;
		}
		return info;
	}

	/***************************************************************************
	 * Obtain the list of available procedures for a given profile
	 * 
	 * @return	
	 * 		A list of procedure identifiers
	 **************************************************************************/
	public Map<String, String> getAvailableProcedures()
	{
		Map<String, String> procs = null;
		if (!isConnected()) return null;
		try
		{
			SPELLmessage msg = new SPELLmessageProcList();
			SPELLmessage response = performRequest(msg);

			if (response != null)
			{
				// Parse the list of available procedures (comma separated)
				/* Returned list contains tuples of procIDs and Procedures Names this way
				 * procID1|procName1,procID2|procName2,
				 */
				String list = SPELLmessageProcList.getProcListFrom(response);
				if (list!=null)
				{
					StringTokenizer tokenizer = new StringTokenizer(list, ",");
					int count = tokenizer.countTokens();
					if (count > 0)
					{
						procs = new HashMap<String, String>();
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
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		return procs;
	}

	/***************************************************************************
	 * Obtain the procedure code
	 * 
	 * @param procedureId
	 * 		The procedure identifier
	 * @return
	 * 		The procedure source lines
	 **************************************************************************/
	public Vector<String> getProcedureCode(String procedureId)
	{
		Vector<String> code = null;
		try
		{
			boolean getMoreChunks = true;
			SPELLmessage msg = null;
			SPELLmessage response = null;
			FileTransferData data = null;
			int chunkNo = 0;
			
			while(getMoreChunks)
			{
				if (chunkNo == 0)
				{
					msg = new SPELLmessageProcCode( procedureId );
				}
				else
				{
					msg = new SPELLmessageProcCode( procedureId, chunkNo );
				}
				response = performRequest(msg);
				if (response != null)
				{
					data = SPELLmessageProcCode.getCodeFrom(response);
				}

				if (data.getTotalChunks()==0) // Data is not chunked
				{
					code = data.getLines();
					getMoreChunks = false;
				}
				else if (data.getChunkNo() == data.getTotalChunks()) // This is the last chunk
				{
					if (code == null) code = new Vector<String>();
					code.addAll(data.getLines());
					getMoreChunks = false;
				}
				else // Get next chunk
				{
					if (code == null) code = new Vector<String>();
					code.addAll(data.getLines());
					if (chunkNo < data.getTotalChunks()-1)
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
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		if (code==null)
		{
			Logger.error("Unable to obtain procedure code", Level.COMM,this);
		}
		return code;
	}

	/***************************************************************************
	 * Obtain the given procedure properties
	 * 
	 * @param procedureId
	 * 		The procedure identifier
	 * @return	
	 * 		A map with the procedure properties
	 **************************************************************************/
	public TreeMap<String, String> getProcedureProperties(String procedureId)
	{
		TreeMap<String, String> properties = null;
		try
		{
			SPELLmessageProcInfo msg = new SPELLmessageProcInfo( procedureId );
			SPELLmessage response = performRequest(msg);

			if (response != null)
			{
				properties = msg.getProcProperties(response);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
		}
		return properties;
	}

	//##########################################################################
	// COMMAND MANAGEMENT 
	//##########################################################################

	/***************************************************************************
	 * Send a predefined command
	 * 
	 * @param cmd
	 * 		ExecutorCommand identifier
	 **************************************************************************/
	public void command( ExecutorCommand cmd )
	{
		try
		{
			Logger.debug("Executor command: " + cmd.getCmdString(), Level.PROC, this);
			SPELLmessageCommand msg = new SPELLmessageCommand(cmd.getMsgId());
			processCommandArguments(cmd, msg);
			m_interface.sendMessage(msg);
			Logger.debug("Executor command sent: " + cmd, Level.COMM,this);
		}
		catch (Exception e)
		{
			Logger.error(e.getLocalizedMessage(), Level.COMM,this);
		}
	}
	
	/***************************************************************************
	 * Change executor configuration
	 * 
	 * @param procId
	 * 		Procedure identifier
	 * @param config
	 * 		Configuration map
	 **************************************************************************/
	public void setExecutorConfiguration( String procId, ExecutorConfig config )
	{
		try
		{
			SPELLmessageSetConfig req = new SPELLmessageSetConfig(procId);
			for( String key : config.getConfigMap().keySet() )
			{
				req.set(key, config.getConfigMap().get(key));
			}
			SPELLmessage response = performRequest(req);
			if (response == null || response instanceof SPELLmessageError)
			{
				Logger.error("Unable to set executor configuration", Level.PROC, this);
			}
		}
		catch(Exception e)
		{
			Logger.error(e.getLocalizedMessage(), Level.COMM, this);
		}

	}
	
	/***************************************************************************
	 * Obtain executor configuration
	 * 
	 * @param procId
	 * 		Procedure (executor) identifier
	 **************************************************************************/
	public void updateExecutorConfig(String procId, ExecutorConfig config)
	{
		try
		{
			Logger.debug("Retrieving executor configuration: " + procId, Level.COMM,this);
			SPELLmessage msg = new SPELLmessageExecConfig(procId);
			SPELLmessage response = performRequest(msg);
			if (response != null)
			{
				SPELLmessageExecConfig.fillExecConfig(config, response);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			Logger.error(ex.getLocalizedMessage(), Level.COMM,this);
			config = null;
		}
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
			Logger.debug("Connecting context proxy", Level.COMM,this);
			// Get password from listener proxy if any
			ServerProxy lproxy = (ServerProxy) ServiceManager.get(ServerProxy.ID);
			ServerInfo sinfo = lproxy.getCurrentServer();
			if (sinfo.getUser() != null)
			{
				m_ctxInfo.setUser(sinfo.getUser());
				m_ctxInfo.setPwd(sinfo.getPwd());
			}
			m_interface.configure( m_ctxInfo.getServerInfo() );
			Logger.debug("Connecting IPC", Level.COMM,this);
			m_interface.connect();
			login();
		}
		catch (Exception e)
		{
			Logger.error("Connection failed: " + e.getLocalizedMessage(), Level.COMM,this);
			m_interface.forceDisconnect();
			ErrorData data = new ErrorData(m_ctxInfo.getName(), "Cannot connect", e.getLocalizedMessage());
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
		if (!m_interface.isConnected())
		{
			return;
		}
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
		Logger.info("Connection closed", Level.COMM,this);
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
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/***************************************************************************
	 * Clear context information buffer on failure
	 **************************************************************************/
	public void connectionFailed( ErrorData data )
	{
		Logger.error("Unable to connect to context: " + data.getMessage(), Level.COMM, this);
		m_ctxInfo = null;
		CoreExtensions.get().fireContextError(data);
	}
	

	/***************************************************************************
	 * Clear context information buffer on failure
	 **************************************************************************/
	@Override
	public void connectionLost( ErrorData data )
	{
		Logger.error("Context proxy connection lost: " + data.getMessage(), Level.COMM, this);
		data.setOrigin(m_ctxInfo.getName());
		m_ctxInfo = null;
		CoreExtensions.get().fireContextError(data);
	}

	/***************************************************************************
	 * Workaround needed due to python bug in subprocess.Popen
	 **************************************************************************/
	@Override
	protected boolean listenerConnectionLost( SPELLlistenerLost error )
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
