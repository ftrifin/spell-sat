///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : BasicProxy.java
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

import com.astra.ses.spell.gui.core.CoreExtensions;
import com.astra.ses.spell.gui.core.comm.CommInterfaceFactory;
import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.comm.messages.RequestException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLcontextLost;
import com.astra.ses.spell.gui.core.comm.messages.SPELLlistenerLost;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageClientOperation;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageCtxOperation;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDisableUserAction;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDismissUserAction;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageEnableUserAction;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageError;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageExecOperation;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogin;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageLogout;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageNotify;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageNotifyAsync;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOneway;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessagePrompt;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessagePromptEnd;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessagePromptStart;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageResponse;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageSetUserAction;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageVariableChange;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageVariableScopeChange;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageDisplay;
import com.astra.ses.spell.gui.core.interfaces.ICommInterface;
import com.astra.ses.spell.gui.core.interfaces.ICommListener;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.NotificationData;
import com.astra.ses.spell.gui.core.model.notification.ScopeNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.VariableNotification;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.services.BaseService;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ClientOperation;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;
import com.astra.ses.spell.gui.core.model.types.ExecutorOperation;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

abstract class BasicProxy extends BaseService implements ICommListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	protected ICommInterface	m_interface;
	/** Currently selected server */
	protected ServerInfo	 m_currentServer;
	/** Timeout for opening processes */
	protected long	         m_openTimeout	= 0;

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public BasicProxy(String id)
	{
		super(id);
		m_interface = CommInterfaceFactory.createCommInterface();
		m_currentServer = null;
		m_openTimeout = 25000;
	}

	@Override
	/***************************************************************************
	 * Subscribe to required resources
	 **************************************************************************/
	public void subscribe()
	{
	}

	/***************************************************************************
	 * Set the response timeout
	 **************************************************************************/
	public void setResponseTimeout(long timeoutUSecs)
	{
		m_interface.setResponseTimeout(timeoutUSecs);
	}

	/***************************************************************************
	 * Set the timeout for opening executors in msecs
	 **************************************************************************/
	public void setOpenTimeout(long timeout)
	{
		m_openTimeout = timeout;
	}

	/***************************************************************************
	 * Check if the connection is established
	 * 
	 * @return True if it is
	 **************************************************************************/
	public boolean isConnected()
	{
		return m_interface.isConnected();
	}

	/***************************************************************************
	 * Login on
	 **************************************************************************/
	protected void login()
	{
		try
		{
			Logger.info("Login", Level.COMM, this);
			SPELLmessage login = new SPELLmessageLogin();
			m_interface.sendMessage(login);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Logout
	 **************************************************************************/
	protected void logout()
	{
		try
		{
			Logger.info("Logout", Level.COMM, this);
			SPELLmessage logout = new SPELLmessageLogout();
			m_interface.sendMessage(logout);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Obtain the currently selected server
	 * 
	 * @return The current server
	 **************************************************************************/
	public String getCurrentServerID()
	{
		if (m_currentServer == null) return null;
		return m_currentServer.getName();
	}

	/***************************************************************************
	 * Obtain the currently selected server data
	 * 
	 * @return The current server data
	 **************************************************************************/
	public ServerInfo getCurrentServer()
	{
		return m_currentServer;
	}

	/***************************************************************************
	 * Receive service update notifications
	 * 
	 * @param service
	 *            Service notifying the update
	 **************************************************************************/
	public void serviceUpdated(String service)
	{
		// Nothing to do
	}

	// ##########################################################################
	// LISTENER SERVICES
	// ##########################################################################

	// ##########################################################################
	// COMMUNICATION INTERFACE CALLBACKS
	// ##########################################################################

	/***************************************************************************
	 * Callback for messages sent by the SPELL server
	 * 
	 * @param received
	 *            SPELL message received
	 **************************************************************************/
	public void receiveMessage(SPELLmessage received)
	{
		// Logger.debug("Received message " + received.getId()
		// + " of type " + received.getType()
		// + " from sender " + received.getSender(), Level.COMM, this);

		if (received instanceof SPELLmessageNotifyAsync)
		{
			processNotificationMessage(received);
		}
		else if (received instanceof SPELLmessageDisplay)
		{
			SPELLmessageDisplay display = (SPELLmessageDisplay) received;
			CoreExtensions.get().fireProcedureDisplay(display.getData());
		}
		else if (received instanceof SPELLlistenerLost)
		{
			boolean notify = listenerConnectionLost((SPELLlistenerLost) received);
			if (notify)
			{
				CoreExtensions.get().fireListenerError(
				        ((SPELLlistenerLost) received).getData());
			}
		}
		else if (received instanceof SPELLcontextLost)
		{
			boolean notify = contextConnectionLost((SPELLcontextLost) received);
			if (notify)
			{
				CoreExtensions.get().fireContextError(
				        ((SPELLcontextLost) received).getData());
			}
		}
		else if (received instanceof SPELLmessageError)
		{
			SPELLmessageError error = (SPELLmessageError) received;
			CoreExtensions.get().fireProcedureError(error.getData());
		}
		else if (received instanceof SPELLmessagePromptStart)
		{
			SPELLmessagePromptStart prompt = (SPELLmessagePromptStart) received;
			Input promptData = prompt.getData();
			CoreExtensions.get().firePrompt(promptData);
		}
		else if (received instanceof SPELLmessagePromptEnd)
		{
			SPELLmessagePromptEnd prompt = (SPELLmessagePromptEnd) received;
			Input promptData = prompt.getData();
			CoreExtensions.get().fireCancelPrompt(promptData);
		}
		else if (received instanceof SPELLmessageExecOperation)
		{
			SPELLmessageExecOperation op = (SPELLmessageExecOperation) received;
			if (op.getOperation() == ExecutorOperation.ATTACH)
			{
				ClientMode mode = op.getClientMode();
				if (mode == ClientMode.CONTROLLING)
				{
					CoreExtensions.get().fireProcedureControlled(
					        op.getProcId(), op.getClientKey());
				}
				else if (mode == ClientMode.MONITORING)
				{
					CoreExtensions.get().fireProcedureMonitored(op.getProcId(),
					        op.getClientKey());
				}
			}
			else if (op.getOperation() == ExecutorOperation.CLOSE)
			{
				CoreExtensions.get().fireProcedureClosed(op.getProcId(),
				        op.getClientKey());
			}
			else if (op.getOperation() == ExecutorOperation.DETACH)
			{
				CoreExtensions.get().fireProcedureReleased(op.getProcId(),
				        op.getClientKey());
			}
			else if (op.getOperation() == ExecutorOperation.KILL)
			{
				CoreExtensions.get().fireProcedureKilled(op.getProcId(),
				        op.getClientKey());
			}
			else if (op.getOperation() == ExecutorOperation.CRASH)
			{
				CoreExtensions.get().fireProcedureCrashed(op.getProcId(),
				        op.getClientKey());
			}
			else if (op.getOperation() == ExecutorOperation.OPEN)
			{
				CoreExtensions.get().fireProcedureOpen(op.getProcId(),
				        op.getClientKey());
			}
			else if (op.getOperation() == ExecutorOperation.STATUS)
			{
				CoreExtensions.get().fireProcedureStatus(op.getProcId(),
				        op.getProcStatus(), op.getClientKey());
			}
			else
			{
				Logger.error("Unknown executor operation '" + op.getOperation()
				        + "' for " + op.getProcId(), Level.COMM, this);
			}
		}
		else if (received instanceof SPELLmessageCtxOperation)
		{
			SPELLmessageCtxOperation ctxOp = (SPELLmessageCtxOperation) received;
			if (ctxOp.getStatus() == ContextStatus.RUNNING)
			{
				CoreExtensions.get().fireContextStarted(ctxOp.getData());
			}
			else if (ctxOp.getStatus() == ContextStatus.AVAILABLE)
			{
				CoreExtensions.get().fireContextStopped(ctxOp.getData());
			}
			else if (ctxOp.getStatus() == ContextStatus.ERROR)
			{
				CoreExtensions.get().fireContextError(ctxOp.getErrorData());
			}
			else
			{
				Logger.error("Unknown context operation: "
				        + ctxOp.getData().getName(), Level.COMM, this);
			}
		}
		else if (received instanceof SPELLmessageClientOperation)
		{
			SPELLmessageClientOperation op = (SPELLmessageClientOperation) received;
			if (op.getOperation() == ClientOperation.LOGIN)
			{
				CoreExtensions.get().fireClientConnected(op.getHost(),
				        op.getClientKey());
			}
			else if (op.getOperation() == ClientOperation.LOGOUT)
			{
				CoreExtensions.get().fireClientDisconnected(op.getHost(),
				        op.getClientKey());
			}
			else
			{
				Logger.error("Unknown client operation: " + op.getOperation(),
				        Level.COMM, this);
			}
		}
		else if (received instanceof SPELLmessageSetUserAction)
		{
			SPELLmessageSetUserAction msg = (SPELLmessageSetUserAction) received;
			CoreExtensions.get().fireProcedureUserAction(msg.getData());
		}
		else if (received instanceof SPELLmessageEnableUserAction)
		{
			SPELLmessageEnableUserAction msg = (SPELLmessageEnableUserAction) received;
			CoreExtensions.get().fireProcedureUserAction(msg.getData());
		}
		else if (received instanceof SPELLmessageDisableUserAction)
		{
			SPELLmessageDisableUserAction msg = (SPELLmessageDisableUserAction) received;
			CoreExtensions.get().fireProcedureUserAction(msg.getData());
		}
		else if (received instanceof SPELLmessageDismissUserAction)
		{
			SPELLmessageDismissUserAction msg = (SPELLmessageDismissUserAction) received;
			CoreExtensions.get().fireProcedureUserAction(msg.getData());
		}
		else if (received instanceof SPELLmessageVariableScopeChange)
		{
			ScopeNotification data = ((SPELLmessageVariableScopeChange) received)
			        .getData();
			CoreExtensions.get().fireVariableScopeChange(data);
		}
		else if (received instanceof SPELLmessageVariableChange)
		{
			VariableNotification data = ((SPELLmessageVariableChange) received)
			        .getData();
			CoreExtensions.get().fireVariableChange(data);
		}
		else if (received instanceof SPELLmessageOneway)
		{
			Logger.error("Unknown oneway message: " + received.getId(),
			        Level.COMM, this);
		}
		else
		{
			Logger.error("Unexpected message type: " + received, Level.COMM,
			        this);
		}
	}

	/***************************************************************************
	 * Callback for SPELL server requests
	 * 
	 * @param received
	 *            Received SPELL message
	 * @return Response message
	 **************************************************************************/
	public SPELLmessageResponse receiveRequest(SPELLmessageRequest received)
	{
		// Logger.debug("Received request " + received.getId() + " from sender "
		// + received.getSender(), Level.COMM, this);
		SPELLmessageResponse response = null;
		response = new SPELLmessageResponse(received);
		if (received instanceof SPELLmessagePrompt)
		{
			SPELLmessagePrompt prompt = (SPELLmessagePrompt) received;
			Input promptData = prompt.getData();
			CoreExtensions.get().firePrompt(promptData);
			while (promptData.isReady() == false)
			{
				try
				{
					Thread.sleep(300);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			SPELLmessagePrompt.fillResponseData(response, promptData);
			if (promptData.isCancelled())
			{
				Logger.debug("Procedure " + promptData.getProcId()
				        + " cancelled prompt", Level.COMM, this);
				CoreExtensions.get().fireCancelPrompt(promptData);
			}
			else
			{
				Logger.debug("Obtained value: " + promptData.getReturnValue(),
				        Level.COMM, this);
				CoreExtensions.get().fireFinishPrompt(promptData);
			}
		}
		else if (received instanceof SPELLmessageNotify)
		{
			processNotificationMessage((SPELLmessage) received);
		}
		else
		{
			Logger.error("Unexpected request type: " + received, Level.COMM,
			        this);
		}
		if (response != null)
		{
			Logger.debug("Returning response " + response.getId() + " to "
			        + response.getReceiver(), Level.COMM, this);
		}
		return response;
	}

	/***************************************************************************
	 * Process both synchronous and asynchronous notifications.
	 * 
	 * @param received
	 **************************************************************************/
	protected void processNotificationMessage(SPELLmessage received)
	{
		NotificationData data = null;
		if (received instanceof SPELLmessageNotifyAsync)
		{
			data = ((SPELLmessageNotifyAsync) received).getData();
		}
		else
		{
			data = ((SPELLmessageNotify) received).getData();
		}
		if (data instanceof ItemNotification)
		{
			CoreExtensions.get().fireProcedureItem((ItemNotification) data);
		}
		else if (data instanceof StatusNotification)
		{
			CoreExtensions.get().fireProcedureStatus((StatusNotification) data);
		}
		else if (data instanceof StackNotification)
		{
			CoreExtensions.get().fireProcedureStack((StackNotification) data);
		}
		else
		{
			Logger.warning("Notification type not implemented: " + received,
			        Level.COMM, this);
		}
	}

	/***************************************************************************
	 * Process the given command arguments and update the SPELL message properly
	 * 
	 * @param cmd
	 *            ExecutorCommand identifier
	 * @param session
	 *            Procedure execution session information
	 * @param arguments
	 *            ExecutorCommand arguments
	 * @param msg
	 *            SPELL message to update
	 **************************************************************************/
	protected void processCommandArguments(ExecutorCommand cmd,
	        SPELLmessageOneway msg, String[] args)
	{
		msg.fillCommandData(cmd, args);
	}

	/***************************************************************************
	 * Check a SPELL message response correctness
	 * 
	 * @param msg
	 *            SPELL message to check
	 * @throws RequestException
	 *             if the message contains errors
	 **************************************************************************/
	protected void checkRequestFailure(SPELLmessage msg)
	        throws RequestException
	{
		RequestException ex = null;
		if (msg == null)
		{
			String error = "Request failed on server side: no response";
			ex = new RequestException(error);
			ex.isFatal = true;
		}
		else if (msg instanceof SPELLmessageError)
		{
			SPELLmessageError errorMsg = (SPELLmessageError) msg;
			String error = "Request failed on server side:\n"
			        + errorMsg.getData().getMessage();
			String reason = errorMsg.getData().getReason();
			if (!reason.equals("(unknown)")) error += "\nReason: " + reason;
			ex = new RequestException(error);
		}
		if (ex != null) throw ex;
	}

	/***************************************************************************
	 * Send a generic request and return the response, controlling the
	 * communication process
	 * 
	 * @throws RequestException
	 **************************************************************************/
	protected synchronized SPELLmessage performRequest(SPELLmessage request,
	        long timeout) throws Exception
	{
		SPELLmessage response = null;
		try
		{
			if (request == null) return null;

			if (!isConnected()) return null;

			Logger.debug("Request: " + request.getId(), Level.COMM, this);

			if (timeout > 0)
			{
				response = m_interface.sendRequest(request, timeout);
			}
			else
			{
				response = m_interface.sendRequest(request);
			}
			checkRequestFailure(response);
			Logger.debug("Response received for " + request.getId(),
			        Level.COMM, this);
		}
		catch (RequestException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			String origin = "COMM";
			String message = "Request error (" + request.getId() + ")";
			String reason = ex.getLocalizedMessage();
			Logger.error(message + ": " + reason, Level.COMM, this);
			ErrorData data = new ErrorData(origin, message, reason, true);
			connectionFailed(data);
			throw new Exception(ex.getLocalizedMessage());
		}
		return response;
	}

	/***************************************************************************
	 * Send a generic request and return the response, controlling the
	 * communication process
	 * 
	 * @throws RequestException
	 **************************************************************************/
	protected SPELLmessage performRequest(SPELLmessage request)
	        throws Exception
	{
		return performRequest(request, 0);
	}

	/***************************************************************************
	 * Workaround needed due to a python bug in subprocess.Popen and sockets
	 **************************************************************************/
	protected boolean listenerConnectionLost(SPELLlistenerLost error)
	{
		return false;
	}

	/***************************************************************************
	 * Workaround needed due to a python bug in subprocess.Popen and sockets
	 **************************************************************************/
	protected boolean contextConnectionLost(SPELLcontextLost error)
	{
		return false;
	}

	/***************************************************************************
	 * Force disconnection from SPELL server
	 **************************************************************************/
	void forceDisconnect()
	{
		m_interface.forceDisconnect();
	}
}
