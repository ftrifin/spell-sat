///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : ServerProxy.java
//
// DATE      : 2008-11-21 08:58
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.services;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import com.astra.ses.spell.gui.core.CoreExtensions;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageAttachCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCloseCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxInfo;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxList;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxOperation;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDestroyCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOpenCtx;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.exceptions.ServerError;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IServerProxy;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * @brief Provides access to the SPEL server services
 * @date 20/10/07
 ******************************************************************************/
public class ServerProxy extends BaseProxy implements IServerProxy
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static IContextProxy	     s_ctx	= null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Service identifier */
	public static final String	         ID	   = "com.astra.ses.spell.gui.ListenerProxy";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the list of context information objects */
	private TreeMap<String, ContextInfo>	m_contextInfos;
	/** Hods the current listener information */
	private ServerInfo	                    m_currentServer;

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
		m_contextInfos = new TreeMap<String, ContextInfo>();
		m_currentServer = null;
		Logger.debug("Created", Level.INIT, this);
	}

	// ##########################################################################
	// SERVER PROXY SETUP METHODS
	// ##########################################################################

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#getCurrentServerID
	 * ()
	 */
	@Override
	public String getCurrentServerID()
	{
		if (m_currentServer == null) return null;
		return m_currentServer.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#getCurrentServer
	 * ()
	 */
	@Override
	public ServerInfo getCurrentServer()
	{
		return m_currentServer;
	}

	@Override
	public void cleanup()
	{
		super.cleanup();
		m_currentServer = null;
		m_contextInfos = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#changeServer()
	 */
	@Override
	public void changeServer(ServerInfo server) throws ServerError
	{
		boolean reconnect = false;
		try
		{
			if (isConnected())
			{
				disconnect();
				reconnect = true;
			}
			m_currentServer = server;
			getIPC().configure(server);
			if (reconnect)
			{
				connect();
			}
		}
		catch (Exception ex)
		{
			throw new ServerError("Cannot change server: " + ex.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.astra.ses.spell.gui.core.model.services.IServerProxy#connect()
	 */
	@Override
	public void connect() throws ServerError
	{
		if (s_ctx == null)
		{
			s_ctx = (IContextProxy) ServiceManager.get(IContextProxy.class);
		}
		try
		{
			Logger.debug("Connecting proxy", Level.COMM, this);
			if (m_currentServer.getTunnelUser() != null)
			{
				Logger.info("Using tunneled connection", Level.COMM, this);
				if (m_currentServer.getTunnelPassword() == null)
				{
					Logger.error("No password information", Level.COMM, this);
					return;
				}
			}
			performConnect( m_currentServer );
			CoreExtensions.get().fireListenerConnected(m_currentServer);
			Logger.debug("Connected", Level.COMM, this);
		}
		catch (Exception e)
		{
			ErrorData data = new ErrorData("SRV", "Cannot connect", e.getLocalizedMessage(), true);
			connectionFailed(data);
			throw new ServerError(e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#disconnect()
	 */
	@Override
	public void disconnect() throws ServerError
	{
		if (!isConnected()) { return; }
		try
		{
			performDisconnect();
		}
		catch (Exception e)
		{
			throw new ServerError("Cannot disconnect: " + e.getLocalizedMessage());
		}
		finally
		{
			m_contextInfos.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#openContext()
	 */
	@Override
	public void openContext(String ctxName) throws ServerError
	{
		try
		{
			Logger.info("Requesting opening context " + ctxName, Level.COMM, this);
			SPELLmessage request = new SPELLmessageOpenCtx(ctxName);
			SPELLmessage response = performRequest(request);
			if (response != null)
			{
				ContextInfo info = getContextInfo(ctxName);
				Logger.info("Context open: " + ctxName, Level.PROC, this);
				CoreExtensions.get().fireContextStarted(info);
			}
		}
		catch (Exception ex)
		{
			throw new ServerError("Cannot open context: " + ex.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#closeContext()
	 */
	@Override
	public void closeContext(String ctxName) throws ServerError
	{
		try
		{
			SPELLmessage msg = new SPELLmessageCloseCtx(ctxName);
			SPELLmessage response = performRequest(msg);

			if (response != null)
			{
				ContextInfo info = getContextInfo(ctxName);
				Logger.info("Context stopped: " + ctxName, Level.COMM, this);
				CoreExtensions.get().fireContextStopped(info);
			}
		}
		catch (Exception ex)
		{
			throw new ServerError("Cannot close context: " + ex.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#destroyContext()
	 */
	@Override
	public void destroyContext(String ctxName) throws ServerError
	{
		try
		{
			if (s_ctx.isConnected())
			{
				s_ctx.forceDisconnect();
			}

			SPELLmessage msg = new SPELLmessageDestroyCtx(ctxName);
			SPELLmessage response = performRequest(msg);

			if (response != null)
			{
				ContextInfo info = getContextInfo(ctxName);
				Logger.info("Context destroyed: " + ctxName, Level.COMM, this);
				CoreExtensions.get().fireContextStopped(info);
			}
		}
		catch (Exception ex)
		{
			throw new ServerError("Cannot close context: " + ex.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#attachContext()
	 */
	@Override
	public void attachContext(String ctxName) throws ServerError
	{
		try
		{
			if (s_ctx.isConnected())
			{
				detachContext();
			}
			SPELLmessage msg = new SPELLmessageAttachCtx(ctxName);
			SPELLmessage response = performRequest(msg);

			if (response != null)
			{
				ContextInfo cinfo = new ContextInfo(ctxName);
				SPELLmessageCtxInfo.fillCtxInfo(cinfo, response);
				cinfo.setHost(m_currentServer.getHost());
				Logger.info("Attaching to context " + cinfo.getName(), Level.PROC, this);
				Logger.info("Status: " + cinfo.getStatus(), Level.PROC, this);
				Logger.info("Port  : " + cinfo.getPort(), Level.PROC, this);
				s_ctx.attach(cinfo);
				m_contextInfos.put(ctxName, s_ctx.getInfo());
				Logger.info("Attached to context " + cinfo.getName(), Level.COMM, this);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new ServerError("Cannot attach context: " + ex.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#detachContext()
	 */
	@Override
	public void detachContext() throws ServerError
	{
		try
		{
			if (s_ctx.isConnected())
			{
				s_ctx.detach();
			}
		}
		catch (Exception ex)
		{
			throw new ServerError("Cannot open context: " + ex.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#getAvailableContexts
	 * ()
	 */
	@Override
	public Vector<String> getAvailableContexts() throws ServerError
	{
		Vector<String> list = null;
		if (!isConnected()) return null;
		if (m_contextInfos.size() > 0)
		{
			Set<String> set = m_contextInfos.keySet();
			list = new Vector<String>();
			for (String ctx : set)
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
				if (listStr != null)
				{
					StringTokenizer tokenizer = new StringTokenizer(listStr, ",");
					list = new Vector<String>();
					while (tokenizer.hasMoreTokens())
					{
						list.addElement(tokenizer.nextToken());
					}
				}
			}
		}
		catch (Exception ex)
		{
			throw new ServerError("Cannot open context: " + ex.getLocalizedMessage());
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#getContextInfo()
	 */
	@Override
	public ContextInfo getContextInfo(String ctxName) throws ServerError
	{
		ContextInfo info = null;
		try
		{
			SPELLmessage msg = new SPELLmessageCtxInfo(ctxName);
			SPELLmessage response = performRequest(msg);

			if (response != null)
			{
				info = new ContextInfo(ctxName);
				SPELLmessageCtxInfo.fillCtxInfo(info, response);
				m_contextInfos.put(ctxName, info);
			}
		}
		catch (Exception ex)
		{
			throw new ServerError("Cannot get context information: " + ex.getLocalizedMessage());
		}
		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.core.model.services.IServerProxy#connectionLost()
	 */
	@Override
	public void connectionLost(ErrorData data)
	{
		Logger.error("Connection to listener lost: " + data.getMessage(), Level.COMM, this);
		m_contextInfos.clear();
		data.setOrigin("SRV");
		CoreExtensions.get().fireListenerError(data);
	}

	@Override
	public void connectionFailed(ErrorData data)
	{
		Logger.error("Connection to listener failed: " + data.getMessage(), Level.PROC, this);
		data.setOrigin("SRV");
		forceDisconnect();
		CoreExtensions.get().fireListenerError(data);
	}

	@Override
	public void connectionClosed()
	{
		Logger.info("Connection to listener closed", Level.PROC, this);
		forceDisconnect();
		CoreExtensions.get().fireListenerDisconnected();
	}

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.interfaces.IBaseProxy#processIncomingMessage(com.astra.ses.spell.gui.core.comm.messages.SPELLmessage)
     */
    @Override
    public boolean processIncomingMessage(SPELLmessage msg)
    {
		if (msg instanceof SPELLmessageCtxOperation)
		{
			SPELLmessageCtxOperation opMsg = (SPELLmessageCtxOperation) msg;
			if (opMsg.getContextInfo().getStatus().equals(ContextStatus.ERROR) || 
				opMsg.getContextInfo().getStatus().equals(ContextStatus.KILLED ))
			{
				IContextProxy ctx = (IContextProxy) ServiceManager.get(IContextProxy.class);
				ctx.connectionLost(opMsg.getErrorData());
			}
			return true;
		}
		return false;
    }

	/* (non-Javadoc)
     * @see com.astra.ses.spell.gui.core.interfaces.IBaseProxy#processIncomingRequest(com.astra.ses.spell.gui.core.comm.messages.SPELLmessage)
     */
    @Override
    public SPELLmessageResponse processIncomingRequest(SPELLmessage msg)
    {
	    return null;
    }

}
