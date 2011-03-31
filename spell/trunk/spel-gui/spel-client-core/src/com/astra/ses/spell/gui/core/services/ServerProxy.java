///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : ServerProxy.java
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

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import com.astra.ses.spell.gui.core.CoreExtensions;
import com.astra.ses.spell.gui.core.comm.messages.SPELLcontextLost;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageAttachCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCloseCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDestroyCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOpenCtx;
import com.astra.ses.spell.gui.core.exceptions.CommException;
import com.astra.ses.spell.gui.core.exceptions.ServerError;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Level;


/*******************************************************************************
 * @brief Provides access to the SPEL server services
 * @date 20/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ServerProxy extends BasicProxy 
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static ContextProxy s_ctx = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Service identifier */
	public static final String ID = "com.astra.ses.spell.gui.ListenerProxy";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the list of context information objects */
	private TreeMap<String,ContextInfo> m_contextInfos;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	 
	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ServerProxy()
	{
		super(ID);
		m_contextInfos = new TreeMap<String,ContextInfo>();
		Logger.debug("Created", Level.INIT, this);
	}

	//##########################################################################
	// SERVER PROXY SETUP METHODS 
	//##########################################################################

	/***************************************************************************
	 * Setup the service
	 **************************************************************************/
	public void setup()
	{
		Logger.debug("Setting up", Level.INIT, this);
		m_interface.setCommListener(this);
	}

	/***************************************************************************
	 * Cleanup the service
	 **************************************************************************/
	public void cleanup()
	{
		if (m_interface.isConnected())
		{
			try
			{
				m_interface.disconnect();
			}
			catch(CommException ex)
			{
				m_interface.forceDisconnect();
			}
		}
	}

	//##########################################################################
	// CONNECTIVITY METHODS 
	//##########################################################################
	
	/***************************************************************************
	 * Change the currently selected server name and port
	 * 
	 * @param serverID
	 *            The server identifier
	 **************************************************************************/
	public void changeServer( ServerInfo server ) throws ServerError
	{
		boolean reconnect = false;
		try
		{
			if (m_interface.isConnected())
			{
				disconnect();
				reconnect = true;
			}
			m_currentServer = server;
			m_interface.configure(server);
			if (reconnect)
			{
				connect();
			}
		}
		catch(Exception ex)
		{
			throw new ServerError("Cannot change server: "+ ex.getLocalizedMessage()); 
		}
	}

	/***************************************************************************
	 * Connect to the currently selected SPELL server
	 **************************************************************************/
	public void connect() throws ServerError
	{
		if (s_ctx == null)
		{
			s_ctx = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		}
		try
		{
			Logger.debug("Connecting proxy", Level.COMM, this);
			if (m_currentServer.getUser() != null)
			{
				Logger.info("Using tunneled connection", Level.COMM, this);
				if (m_currentServer.getPwd() == null)
				{
					Logger.error("No password information", Level.COMM, this);
					return;
				}
			}
			Logger.debug("Connecting IPC", Level.COMM, this);
			m_interface.connect();
			Logger.debug("Login on listener", Level.COMM, this);
			login();
			CoreExtensions.get().fireListenerConnected(m_currentServer);
			Logger.debug("Connected", Level.COMM, this);
		}
		catch (Exception e)
		{
			ErrorData data = new ErrorData("SRV", "Cannot connect", e.getLocalizedMessage());
			connectionFailed(data);
			throw new ServerError(e.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Disconnect from SPELL server
	 **************************************************************************/
	public void disconnect() throws ServerError
	{
		if (!m_interface.isConnected())
		{
			return;
		}
		try
		{
			logout();
			m_interface.disconnect();
			m_contextInfos.clear();
			Logger.info("Connection closed", Level.COMM, this);
			CoreExtensions.get().fireListenerDisconnected();
		}
		catch (Exception e)
		{
			throw new ServerError("Cannot disconnect: " + e.getLocalizedMessage());
		}
	}

	//##########################################################################
	// LISTENER SERVICES (CONTEXT MANAGEMENT) 
	//##########################################################################

	/***************************************************************************
	 * Open a context on the server
	 * 
	 * @param ctxName
	 * 		The context name
	 **************************************************************************/
	public void openContext( String ctxName ) throws ServerError
	{
		try
		{
			Logger.info("Requesting opening context " + ctxName, Level.COMM, this);
			SPELLmessage request = new SPELLmessageOpenCtx(ctxName);
			SPELLmessage response = performRequest(request);
			if (response != null)
			{
				ContextInfo info = getContextInfo(ctxName);
				Logger.info("Context open: " + ctxName, Level.PROC,this);
				CoreExtensions.get().fireContextStarted( info );
			}
		}
		catch(Exception ex)
		{
			throw new ServerError("Cannot open context: "+ ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Close a context on the server
	 * 
	 * @param ctxName
	 * 		The context name
	 **************************************************************************/
	public void closeContext( String ctxName ) throws ServerError
	{
		try
		{
			SPELLmessage msg = new SPELLmessageCloseCtx( ctxName );
			SPELLmessage response = performRequest(msg);
			
			if (response != null)
			{
				ContextInfo info = getContextInfo(ctxName);
				Logger.info("Context stopped: " + ctxName, Level.COMM, this);
				CoreExtensions.get().fireContextStopped( info );
			}
		}
		catch(Exception ex)
		{
			throw new ServerError("Cannot close context: "+ ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Destroy a context on the server
	 * 
	 * @param ctxName
	 * 		The context name
	 **************************************************************************/
	public void destroyContext( String ctxName ) throws ServerError
	{
		try
		{
			SPELLmessage msg = new SPELLmessageDestroyCtx( ctxName );
			SPELLmessage response = performRequest(msg);
			
			if (response != null)
			{
				ContextInfo info = getContextInfo(ctxName);
				Logger.info("Context destroyed: " + ctxName, Level.COMM, this);
				CoreExtensions.get().fireContextStopped( info );
			}
		}
		catch(Exception ex)
		{
			throw new ServerError("Cannot close context: "+ ex.getLocalizedMessage());
		}
	}


	/***************************************************************************
	 * Attach to an execution context on server
	 * 
	 * @param ctxName
	 * 		The context name
	 **************************************************************************/
	public void attachContext( String ctxName ) throws ServerError
	{
		try
		{
			if (s_ctx.isConnected())
			{
				detachContext();
			}
			SPELLmessage msg = new SPELLmessageAttachCtx( ctxName );
			SPELLmessage response = performRequest(msg);
	
			if (response != null)
			{
				ContextInfo cinfo = new ContextInfo(ctxName);
				SPELLmessageCtxInfo.fillCtxInfo(cinfo, response);
				cinfo.setHost(m_currentServer.getHost());
				Logger.info("Attaching to context " + cinfo.getName(), Level.PROC,this);
				Logger.info("Status: " + cinfo.getStatus(), Level.PROC,this);
				Logger.info("Port  : " + cinfo.getPort(), Level.PROC,this);
				s_ctx.attach(cinfo);
				m_contextInfos.put(ctxName, s_ctx.getInfo());
				Logger.info("Attached to context " + cinfo.getName(), Level.COMM, this);
			}
		}
		catch(Exception ex)
		{
			throw new ServerError("Cannot attach context: "+ ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Attach to an execution context on server
	 * 
	 * @param ctxName
	 * 		The context name
	 **************************************************************************/
	public void detachContext() throws ServerError
	{
		try
		{
			if (s_ctx.isConnected())
			{
				s_ctx.detach();
			}
		}
		catch(Exception ex)
		{
			throw new ServerError("Cannot open context: "+ ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Obtain the list of available contexts
	 * 
	 * @return The list of context names
	 **************************************************************************/
	public Vector<String> getAvailableContexts() throws ServerError
	{
		Vector<String> list = null;
		if (!isConnected()) return null;
		if (m_contextInfos.size()>0)
		{
			Set<String> set = m_contextInfos.keySet();
			list = new Vector<String>();
			for(String ctx : set)
			{
				list.addElement(ctx);
			}
		}
		try
		{
			SPELLmessage request = new SPELLmessageCtxList();
			SPELLmessage response = performRequest(request);
			if (response != null)
			{
				String listStr = SPELLmessageCtxList.getCtxListFrom(response);
				if (listStr!=null)
				{
					StringTokenizer tokenizer = new StringTokenizer( listStr, ",");
					list = new Vector<String>();
					int count = 0;
					while(tokenizer.hasMoreTokens())
					{
						list.addElement(tokenizer.nextToken());
						count++;
					}
				}
			}
		}
		catch(Exception ex)
		{
			throw new ServerError("Cannot open context: "+ ex.getLocalizedMessage());
		}
		return list;
	}
	
	/***************************************************************************
	 * Obtain context details
	 * 
	 * @param ctxName
	 * 		The context name
	 * @return The context details
	 **************************************************************************/
	public ContextInfo getContextInfo(String ctxName) throws ServerError
	{
		ContextInfo info = null;
		try
		{
			SPELLmessage msg = new SPELLmessageCtxInfo( ctxName );
			SPELLmessage response = performRequest(msg);
			
			if (response != null)
			{
				info = new ContextInfo(ctxName);
				SPELLmessageCtxInfo.fillCtxInfo(info, response);
				m_contextInfos.put(ctxName, info);
			}
		}
		catch(Exception ex)
		{
			throw new ServerError("Cannot get context information: "+ ex.getLocalizedMessage());
		}
		return info;
	}

	/***************************************************************************
	 * Clear context information buffer on failure
	 **************************************************************************/
	public void connectionLost( ErrorData data )
	{
		Logger.error(data.getMessage(), Level.COMM, this);
		m_contextInfos.clear();
		data.setOrigin("SRV");
		CoreExtensions.get().fireListenerError(data);
	}

	/***************************************************************************
	 * Clear context information buffer on failure
	 **************************************************************************/
	public void connectionFailed( ErrorData data )
	{
		Logger.error(data.getMessage(), Level.COMM, this);
		CoreExtensions.get().fireListenerError(data);
	}

	/***************************************************************************
	 * Workaround needed due to python bug in subprocess.Popen
	 **************************************************************************/
	@Override
	protected boolean contextConnectionLost( SPELLcontextLost error )
	{
		ContextProxy ctx = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		if (ctx.isConnected())
		{
			ctx.forceDisconnect();
			ctx.connectionLost(error.getData());
			return false;
		}
		return true;
	}
}
