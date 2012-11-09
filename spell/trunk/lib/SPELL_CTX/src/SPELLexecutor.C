// ################################################################################
// FILE       : SPELLexecutor.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor manager
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLexecutor.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLdataHelper.H"
#include "SPELL_CTX/SPELLcontext.H"
// Project includes --------------------------------------------------------
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_SYN/SPELLthread.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
#include "SPELL_IPC/SPELLipc_Context.H"
// System includes ---------------------------------------------------------

class LoginMonitor : public SPELLthread
{
public:
	LoginMonitor( SPELLexecutor& exec )
	: SPELLthread("login-" + exec.getModel().getInstanceId() ),
	  m_exec( exec )
	{}

	void run()
	{
		SPELLtime checkStart;
		for(;;)
		{
			SPELLtime now;
			SPELLtime delta = now - checkStart;
			if (delta.getSeconds()>10)
			{
				SPELLexecutorManager::instance().callback_executorNotReconnected( m_exec.getModel().getInstanceId() );
				return;
			}
			else
			{
				if (m_exec.isLoggedIn())
				{
					SPELLexecutorManager::instance().callback_executorReconnected( m_exec.getModel().getInstanceId() );
					return;
				}
				else
				{
					usleep(30000);
				}
			}
		}
	}

private:
	SPELLexecutor& m_exec;
};

// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLexecutor::SPELLexecutor()
//=============================================================================
SPELLexecutor::SPELLexecutor( const SPELLexecutorConfiguration& config, SPELLclient* controllingClient )
: SPELLexecutorListener(),
  SPELLprocessListener(),
  m_model(config),
  m_ipc(*this,config),
  m_controllingClient(controllingClient)
{
	SPELLprocessManager::instance().addListener( m_model.getInstanceId(), this );
	m_reconnecting = config.isReconnecting();
	m_loggedIn = false;
	m_loginMonitor = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLexecutor::~SPELLexecutor()
//=============================================================================
SPELLexecutor::~SPELLexecutor()
{
	SPELLprocessManager::instance().removeListener( m_model.getInstanceId(), this );
	m_ipc.cleanup();
	if (m_loginMonitor)
	{
		try
		{
			m_loginMonitor->join();
		}
		catch(...){};
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::start()
{
	LOG_INFO("Starting executor " + m_model.getInstanceId());
	m_ipc.setup();
	m_model.setIpcPort( m_ipc.getPort() );
	m_executorLoggedInEvent.clear();

	if (!m_reconnecting)
	{
		m_processStartedEvent.clear();
		std::string command = SPELLconfiguration::instance().getContextParameter( "ExecutorProgram" );
		if (command == "")
		{
			THROW_EXCEPTION("Cannot launch executor", "No executor command defined in configuration", SPELL_ERROR_CONFIG);
		}
		command += " -c " + m_model.getConfigFile();
		command += " -n " + m_model.getContextName();
		command += " -s " + ISTR(m_ipc.getPort());
		command += " -p " + m_model.getInstanceId();
		command += " -w ";
		if (m_model.getWsFilename() != "")
		{
			command += " -r " + m_model.getWsFilename();
		}

		SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
		SPELLprocessManager::instance().startProcess( m_model.getInstanceId(), command );

		m_processStatus = SPELLprocessManager::instance().getProcessStatus( m_model.getInstanceId() );

		DEBUG("Waiting executor process to begin");
		bool timedOut = m_processStartedEvent.wait( 60 * 1000 ); // milliseconds
		if (timedOut)
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process did not begin in time", SPELL_ERROR_PROCESS);
		}
		DEBUG("Start event received");

		if (m_processStatus == PSTATUS_RUNNING)
		{
			if (!m_loggedIn)
			{
				DEBUG("Executor process started, waiting for login");
				bool timeout = m_executorLoggedInEvent.wait( 20 * 1000 ); // milliseconds
				if (timeout)
				{
					THROW_EXCEPTION("Cannot launch executor", "Executor did not login in time", SPELL_ERROR_PROCESS);
				}
			}
			DEBUG("Executor process logged in");
			m_model.setPID( SPELLprocessManager::instance().getProcessId( m_model.getInstanceId() ));
			LOG_INFO("Executor started: " + m_model.getInstanceId() + " with pid " + ISTR(m_model.getPID()));
		}
		else if (m_processStatus == PSTATUS_FINISHED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process finished too quickly", SPELL_ERROR_PROCESS);
		}
		else if (m_processStatus == PSTATUS_FAILED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process failed to start", SPELL_ERROR_PROCESS);
		}
		else if (m_processStatus == PSTATUS_KILLED )
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process crashed or was killed", SPELL_ERROR_PROCESS);
		}
		else
		{
			THROW_EXCEPTION("Cannot launch executor", "Executor process in unexpected state: " + SPELLprocessUtils::processStatusToString(m_processStatus), SPELL_ERROR_PROCESS);
		}
	}
	else
	{
		m_loginMonitor = new LoginMonitor(*this);
		m_loginMonitor->start();
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::waitLogin( unsigned int timeoutMsec )
{
	if (m_loggedIn) return true;
	bool timeout = m_executorLoggedInEvent.wait( timeoutMsec); // milliseconds
	return !timeout;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::processOk()
{
	switch(m_processStatus)
	{
	case PSTATUS_FINISHED:
	case PSTATUS_FAILED:
	case PSTATUS_KILLED:
		DEBUG("Process is NOK");
		return false;
	default:
		return true;
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::close()
{
	bool needToClose = processOk();

	if (needToClose)
	{
		LOG_INFO("Closing executor " + m_model.getInstanceId());
		m_executorLoggedOutEvent.clear();

		// This has effect only in non-child processes (recovered executors)
		SPELLprocessManager::instance().aboutToCloseProcess( m_model.getInstanceId() );

		ExecutorCommand cmd;
		cmd.id = CMD_CLOSE;
		command(cmd);
		bool timeout = m_executorLoggedOutEvent.wait(5 * 1000); // Wait for 5 seconds
		if (timeout)
		{
			LOG_ERROR("Failed to close executor, it did not log out in time");
			try
			{
		    	SPELLprocessManager::instance().removeListener( m_model.getInstanceId(), this );
				SPELLprocessManager::instance().killProcess( m_model.getInstanceId() );
		    	SPELLprocessManager::instance().waitProcess( m_model.getInstanceId() );
		    	SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
			}
			catch( SPELLcoreException& ex )
			{
				LOG_ERROR("Could not kill process: " + m_model.getInstanceId());
			}
	    	return;
		}
		DEBUG("Executor logged out, disconnecting IPC");
		m_ipc.cleanup();
		DEBUG("Executor logged out, disconnecting IPC done");
	}

	if (needToClose)
	{
		// Will not wait if the process is no longer there
		DEBUG("Now waiting process to finish");
		waitFinish();
		DEBUG("Waiting process to finish done");
	}

	LOG_INFO("Executor closed: " + m_model.getInstanceId());
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::kill()
{
	bool needToKill = processOk();

	if (needToKill)
	{
		LOG_INFO("Killing executor " + m_model.getInstanceId());

		// Remove process listener, not to be notified for the kill
    	SPELLprocessManager::instance().removeListener( m_model.getInstanceId(), this );

		// Cancel requests to client
		SPELLclient* ctrl = getControllingClient();
		if (ctrl != NULL)
		{
			ctrl->cancelRequestsToClient();
		}

		// Disconnect IPC
		m_ipc.cleanup();

		try
		{
			SPELLprocessManager::instance().killProcess( m_model.getInstanceId() );
	    	SPELLprocessManager::instance().waitProcess( m_model.getInstanceId() );
	    	SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
		}
		catch( SPELLcoreException& ex )
		{
			LOG_ERROR("Could not kill process: " + m_model.getInstanceId());
		}
		LOG_INFO("Executor killed: " + m_model.getInstanceId());
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::sendMessageToExecutor( const SPELLipcMessage& msg )
{
	if (processOk())
	{
		m_ipc.sendMessage( m_model.getInstanceId(), msg );
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLipcMessage SPELLexecutor::sendRequestToExecutor( const SPELLipcMessage& msg )
{
	SPELLipcMessage response = VOID_MESSAGE;
	if (processOk())
	{
		response =  m_ipc.sendRequest( m_model.getInstanceId(), msg, 15000 );
	}
	else
	{
		LOG_ERROR("Cannot forward request to executor, process is not OK");
		response = SPELLipcHelper::createErrorResponse( MessageId::MSG_ID_ERROR, msg );
		response.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
		response.set( MessageField::FIELD_ERROR, "Cannot send request " + msg.getId() );
		response.set( MessageField::FIELD_REASON, "Executor process crashed" );
		response.set( MessageField::FIELD_FATAL, PythonConstants::True );
	}
	return response;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processMessageFromExecutor( SPELLipcMessage msg )
{
	std::string id = msg.getId();
	if (id == ExecutorMessages::MSG_NOTIF_EXEC_CLOSE )
	{
		DEBUG("Executor logged out");
		m_executorLoggedOutEvent.set();
		return;
	}
    else if ( id == MessageId::MSG_ID_NOTIFICATION )
    {
    	executorNotification(msg);
    }
	else
	{
		// In case of error, store the information
		if ( msg.getType() == MSG_TYPE_ERROR )
		{
			m_model.setError( msg.get( MessageField::FIELD_ERROR ), msg.get( MessageField::FIELD_REASON) );
			executorStatusChanged( STATUS_ERROR, msg.get( MessageField::FIELD_ERROR ), msg.get( MessageField::FIELD_REASON ) );
		}
	}
	forwardMessageToClient( msg );
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLipcMessage SPELLexecutor::processRequestFromExecutor( SPELLipcMessage msg )
{
	DEBUG("Executor request start: " + msg.getId());

	// Create the login message
    std::string requestId = msg.getId();

    SPELLipcMessage response = VOID_MESSAGE;

    // Otherwise continue processing, first local, then forward to client
    if (requestId == ExecutorMessages::REQ_NOTIF_EXEC_OPEN )
    {
    	response = executorLogin(msg);
    	return response;
    }

    // If there is no client to propagate the request, respond to the executor
    // TBD: this will conflict with background procedures, will see then
    if (m_controllingClient == NULL)
    {
    	response = SPELLipcHelper::createErrorResponse( MessageId::MSG_PEER_LOST, msg );
    	response.set(MessageField::FIELD_ERROR, "Cannot forward " + msg.getId());
    	response.set(MessageField::FIELD_REASON, "No controlling client available");
    	response.set(MessageField::FIELD_FATAL, PythonConstants::False );
    	LOG_ERROR("Executor request end in peer error");
    	return response;
    }

    if ( requestId == ContextMessages::REQ_OPEN_EXEC )
    {
        response = SPELLcontext::instance().openExecutor( msg, m_controllingClient );
    }
    else if ( requestId == ContextMessages::REQ_INSTANCE_ID )
    {
        response = SPELLcontext::instance().getInstanceId( msg );
    }
    else
    {
		response = forwardRequestToClient( msg );
    }

	DEBUG("Executor request end");
    return response;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processStarted( const std::string& identifier )
{
	DEBUG("Callback - Executor process started: " + identifier);
	m_processStatus = SPELLprocessManager::instance().getProcessStatus( m_model.getInstanceId() );
	m_processStartedEvent.set();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processFinished( const std::string& identifier, const int& retValue )
{
	LOG_INFO("Executor process finished: " + identifier);
	m_processStatus = SPELLprocessManager::instance().getProcessStatus( m_model.getInstanceId() );
	m_processStartedEvent.set();
	m_processStoppedEvent.set();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processKilled( const std::string& identifier )
{
	LOG_ERROR("Executor process killed: " + identifier);
	m_model.setStatus(STATUS_ERROR);
	m_processStartedEvent.set();
	m_executorStatusEvent.set();
	m_processStatus = SPELLprocessManager::instance().getProcessStatus( m_model.getInstanceId() );

	SPELLipcMessage error( MessageId::MSG_ID_ERROR );
	error.setType( MSG_TYPE_ERROR );
	error.set( MessageField::FIELD_PROC_ID, identifier );
	error.setSender("CTX");
	error.setReceiver("CLT");
	error.set( MessageField::FIELD_ERROR, "Lost connection wih executor" );
	error.set( MessageField::FIELD_REASON, "Process crashed" );
	error.set( MessageField::FIELD_FATAL, PythonConstants::True );

	forwardMessageToClient( error );

	SPELLcontext::instance().executorLost( identifier );

	m_processStoppedEvent.set();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::processFailed( const std::string& identifier )
{
	LOG_ERROR("Executor process failed startup: " + identifier);
	m_model.setStatus(STATUS_ABORTED);
	m_processStartedEvent.set();
	m_executorStatusEvent.set();
	m_processStatus = SPELLprocessManager::instance().getProcessStatus( m_model.getInstanceId() );

	SPELLipcMessage error( MessageId::MSG_ID_ERROR );
	error.setType( MSG_TYPE_ERROR );
	error.set( MessageField::FIELD_PROC_ID, identifier );
	error.setSender("CTX");
	error.setReceiver("CLT");
	error.set( MessageField::FIELD_ERROR, "Could not start executor" );
	error.set( MessageField::FIELD_REASON, "Process crashed at startup" );
	error.set( MessageField::FIELD_FATAL, PythonConstants::True );

	forwardMessageToClient( error );

	SPELLcontext::instance().executorLost( identifier );

	m_processStoppedEvent.set();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::waitFinish()
{
	if (m_processStatus == PSTATUS_RUNNING)
	{
		DEBUG("Waiting executor process to finish");
		m_processStoppedEvent.clear();
		bool timeout = m_processStoppedEvent.wait(2000);
		if (timeout)
		{
			LOG_WARN("Did not see the process close, try kill");
			SPELLprocessManager::instance().removeListener( m_model.getInstanceId(), this );
			try
			{
				SPELLprocessManager::instance().killProcess( m_model.getInstanceId() );
		    	SPELLprocessManager::instance().waitProcess( m_model.getInstanceId() );
			}
			catch( SPELLcoreException& ex )
			{
				LOG_ERROR("Could not kill process: " + m_model.getInstanceId());
			}
		}
		else
		{
			DEBUG("Executor process finished, stop waiting");
		}
	}
	SPELLprocessManager::instance().clearProcess( m_model.getInstanceId() );
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::waitStatus( const SPELLexecutorStatus& status, unsigned int timeoutMSec )
{
	if (m_model.getStatus() != status)
	{
		DEBUG("Waiting for executor status " + SPELLdataHelper::executorStatusToString(status));
		while(true)
		{
			m_executorStatusEvent.clear();
			bool timeout = m_executorStatusEvent.wait(timeoutMSec);
			if (timeout)
			{
				DEBUG("Status did not change in time");
				return true;
			}
			DEBUG("Status changed to " + SPELLdataHelper::executorStatusToString(m_model.getStatus()));
			if (m_model.getStatus() == status) return false;
		}
	}
	return false;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLexecutorStatus SPELLexecutor::getStatus()
{
	return m_model.getStatus();
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::isActive()
{
	switch(getStatus())
	{
	case STATUS_ABORTED:
	case STATUS_FINISHED:
	case STATUS_ERROR:
		return false;
	default:
		break;
	}
	if (processOk())
	{
		DEBUG("Executor " + m_model.getInstanceId() + " active: " + SPELLdataHelper::executorStatusToString(getStatus()));
		return true;
	}
	return false;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::command( const ExecutorCommand& command )
{
	if (processOk())
	{
		std::string commandId = command.id;
		LOG_INFO("Send command to executor: " + commandId );
		SPELLipcMessage cmd( commandId );
		cmd.setType( MSG_TYPE_ONEWAY );
		cmd.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
		//TODO command arguments
		sendMessageToExecutor( cmd );
	}
	else
	{
		LOG_ERROR("Cannot send command: executor process crashed");
	}
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::setControllingClient( SPELLclient* client )
{
	SPELLmonitor m(m_clientLock);
	LOG_INFO("Set controlling client: " + ISTR(client->getClientKey()));
	m_controllingClient = client;

	SPELLipcMessage addClient( MessageId::MSG_ID_ADD_CLIENT );
	addClient.setType( MSG_TYPE_ONEWAY );
	addClient.setSender( "CTX" );
	addClient.setReceiver( m_model.getInstanceId() );
	addClient.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
	addClient.set( MessageField::FIELD_GUI_CONTROL, ISTR(client->getClientKey()));
	addClient.set( MessageField::FIELD_GUI_CONTROL_HOST, client->getClientHost());
	sendMessageToExecutor(addClient);
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::removeControllingClient()
{
	SPELLmonitor m(m_clientLock);
	DEBUG("Removing controlling client");

	int cKey = m_controllingClient->getClientKey();
	std::string cHost = m_controllingClient->getClientHost();

	m_controllingClient = NULL;

	switch(getStatus())
	{
	case STATUS_FINISHED:
	case STATUS_PAUSED:
	case STATUS_ABORTED:
	case STATUS_ERROR:
		break;
	default:
		{
			ExecutorCommand cmd;
			cmd.id = CMD_PAUSE;
			DEBUG("Pausing procedure");
			command(cmd);
			if (getStatus() == STATUS_WAITING)
			{
				bool timedout = waitStatus(STATUS_INTERRUPTED, 10*1000);
				if (timedout)
				{
					LOG_ERROR("Cannot pause procedure, kill it!");
					//kill();
					return;
				}
			}
			else if (getStatus() == STATUS_RUNNING)
			{
				bool timedout = waitStatus(STATUS_PAUSED, 10*1000);
				if (timedout)
				{
					LOG_ERROR("Cannot pause procedure, kill it!");
					//kill();
					return;
				}
			}
			LOG_INFO("Procedure paused");
			break;
		}
	}

	SPELLipcMessage removeClient( MessageId::MSG_ID_REMOVE_CLIENT );
	removeClient.setType( MSG_TYPE_ONEWAY );
	removeClient.setSender( "CTX" );
	removeClient.setReceiver( m_model.getInstanceId() );
	removeClient.set( MessageField::FIELD_PROC_ID, m_model.getInstanceId() );
	removeClient.set( MessageField::FIELD_GUI_CONTROL, ISTR(cKey));
	removeClient.set( MessageField::FIELD_GUI_CONTROL_HOST, cHost);
	sendMessageToExecutor(removeClient);
	DEBUG("Controlling client removed");
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
bool SPELLexecutor::hasControllingClient()
{
	SPELLmonitor m(m_clientLock);
	return (m_controllingClient != NULL);
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLclient* SPELLexecutor::getControllingClient()
{
	SPELLmonitor m(m_clientLock);
	return m_controllingClient;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::forwardMessageToClient( const SPELLipcMessage& msg )
{
	TICK_IN;
	SPELLmonitor m(m_clientLock);
	if (m_controllingClient)
	{
		m_controllingClient->sendMessageToClient(msg);
	}
	else
	{
		LOG_ERROR("Cannot forward message, no controlling client! " + msg.getId());
	}
	TICK_OUT;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLipcMessage SPELLexecutor::forwardRequestToClient( const SPELLipcMessage& msg )
{
	TICK_IN;
	SPELLipcMessage resp = VOID_MESSAGE;
	resp = m_controllingClient->sendRequestToClient(msg);
	TICK_OUT;
	return resp;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::executorStatusChanged( const SPELLexecutorStatus& status,
										   const std::string& error, const std::string& reason )
{
	LOG_INFO("Executor " + m_model.getInstanceId() + " status changed: " + SPELLdataHelper::executorStatusToString(status) );
	if (error != "") LOG_INFO("Error information: " + error + ":" + reason );
	m_model.setStatus(status);
	m_executorStatusEvent.set();

	SPELLcontext::instance().notifyExecutorOperation( m_model.getInstanceId(), m_model.getParentInstanceId(),
													  -1, CLIENT_MODE_UNKNOWN, status,
													  EXEC_OP_STATUS, "", error, reason);
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::registerNotifier( SPELLexecutorListener* listener )
{
	m_ipc.registerExecutorNotifier( listener );
	//TODO check if need to add client in exec (ipc msg)
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::deregisterNotifier( SPELLexecutorListener* listener )
{
	m_ipc.deregisterExecutorNotifier( listener );
	//TODO check if need to remove client in exec (ipc msg)
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
SPELLipcMessage SPELLexecutor::executorLogin( const SPELLipcMessage& msg )
{
	DEBUG("Received executor login: " + m_model.getInstanceId() );

	m_loggedIn = true;

	// Store the information given by the executor
	m_model.setIpcKey(msg.getKey());
	m_model.setStatus(SPELLdataHelper::stringToExecutorStatus( msg.get( MessageField::FIELD_EXEC_STATUS )));
	m_model.setAsRunFilename( msg.get( MessageField::FIELD_ASRUN_NAME ));
	m_model.setLogFilename( msg.get( MessageField::FIELD_LOG_NAME ));
	//TODO
	//m_model.wsFileName = msg.get( MessageField::FIELD_WS_NAME );

	// Create the response
	SPELLipcMessage response = SPELLipcHelper::createResponse( ExecutorMessages::RSP_NOTIF_EXEC_OPEN, msg );
	LOG_INFO("Executor login options: " + m_model.getInstanceId());
	response.set( MessageField::FIELD_ARGS, m_model.getArguments() );
	LOG_INFO("    Arguments: " + m_model.getArguments());
	std::string oMode = SPELLdataHelper::openModeToString(m_model.getOpenMode());
	response.set( MessageField::FIELD_OPEN_MODE, oMode );
	LOG_INFO("    Open mode: " + oMode);
	response.set( MessageField::FIELD_CONDITION, m_model.getCondition() );
	LOG_INFO("    Condition: " + m_model.getCondition());
	response.set( MessageField::FIELD_PARENT_PROC, m_model.getParentInstanceId() );\
	LOG_INFO("    Parent   : " + m_model.getParentInstanceId());

	if (m_controllingClient)
	{
		response.set( MessageField::FIELD_GUI_CONTROL, ISTR(m_controllingClient->getClientKey()) );
		LOG_INFO("    Client   : " + ISTR(m_controllingClient->getClientKey()));
		response.set( MessageField::FIELD_GUI_CONTROL_HOST, m_controllingClient->getClientHost() );
	}
	else
	{
		LOG_WARN("    No controlling client!");
	}
	// The response will be deleted by IPC layer!

	if (m_reconnecting)
	{
		// Attach the process manager to the executor process
		LOG_INFO("Re-attaching to executor process with PID " + ISTR(m_model.getPID()));
		SPELLprocessManager::instance().attachProcess( m_model.getInstanceId(), m_model.getPID() );

		// Retrieve the procedure status
		SPELLipcMessage msg( ExecutorMessages::REQ_EXEC_STATUS );
		msg.setType( MSG_TYPE_REQUEST );
		msg.setSender("CTX");
		msg.setReceiver( m_model.getInstanceId() );
		SPELLipcMessage resp = sendRequestToExecutor( msg );
		if (!resp.isVoid() && resp.getType() != MSG_TYPE_ERROR )
		{
			std::string statusStr = resp.get( MessageField::FIELD_EXEC_STATUS );
			LOG_INFO("Executor status changed: " + statusStr );
			SPELLexecutorStatus st = SPELLdataHelper::stringToExecutorStatus( statusStr );
			if (st == STATUS_ERROR )
			{
				std::string error = resp.get( MessageField::FIELD_ERROR );
				std::string reason = resp.get( MessageField::FIELD_REASON );
				SPELLutils::trim(error);
				SPELLutils::trim(reason);
				if (error != "")
				{
					LOG_INFO("Executor status error information: " + error + ":" + reason );
				}
				executorStatusChanged( st, error, reason );
			}
			else
			{
				executorStatusChanged( st );
			}
		}
	}

	m_executorLoggedInEvent.set();

	DEBUG("Returning login response for " + m_model.getInstanceId() );
	return response;
}

//=============================================================================
// METHOD: SPELLexecutor::
//=============================================================================
void SPELLexecutor::executorNotification( const SPELLipcMessage& msg )
{
	switch( msg.getType() )
	{
	case MSG_TYPE_NOTIFY_ASYNC:
	case MSG_TYPE_NOTIFY:
	{
		DEBUG("Received notification from " + m_model.getInstanceId() );
		std::string dataType = msg.get( MessageField::FIELD_DATA_TYPE );
		if (dataType == MessageValue::DATA_TYPE_STATUS)
		{
			std::string statusStr = msg.get( MessageField::FIELD_EXEC_STATUS );
			LOG_INFO("Executor status changed: " + statusStr );

			SPELLexecutorStatus st = SPELLdataHelper::stringToExecutorStatus( statusStr );
			if (st == STATUS_ERROR )
			{
				std::string error = msg.get( MessageField::FIELD_ERROR );
				std::string reason = msg.get( MessageField::FIELD_REASON );
				SPELLutils::trim(error);
				SPELLutils::trim(reason);
				if (error != "")
				{
					LOG_INFO("Executor status error information: " + error + ":" + reason );
				}
				executorStatusChanged( st, error, reason );
			}
			else
			{
				executorStatusChanged( st );
			}
		}
		break;
	}
	default:
		LOG_ERROR("UNHANDLED EXECUTOR NOTIFICATION" + msg.dataStr());
		break;
	}
}
