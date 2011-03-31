// ################################################################################
// FILE       : SPELLexecutorManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor manager
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
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
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLclientInfo.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_IPC/SPELLipcMessage.H"
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_UTIL/SPELLlog.H"


using namespace LanguageModifiers;
using namespace PythonConstants;


#define EXECUTOR_OPEN_TIMEOUT_SEC  5
#define EXECUTOR_CLOSE_TIMEOUT_SEC 5


//=============================================================================
// CONSTRUCTOR : SPELLexecutorManager::SPELLexecutorManager
//=============================================================================
SPELLexecutorManager::SPELLexecutorManager( const std::string& instanceId, SPELLcontext& context )
    : SPELLipcInterfaceListener(),
      m_ctx(context)
{
    m_procId = instanceId;
    m_executorKey = -1;
    m_csp = "";
    m_status = STATUS_UNINIT;
    m_condition = "";
    m_parentProc = "";
    m_running = false;
    m_executorCommand = "";
    m_ignore = false;

    m_config.clientKey = -1;
    m_config.automatic = false;
    m_config.visible = false;
    m_config.blocking = false;
    m_config.clientMode = CLT_MODE_UNKNOWN;
    m_config.arguments = "";
    m_config.condition = "";
    SPELLprocessManager::instance().addListener( instanceId, this);

    m_ipc = new SPELLipcServerInterface( "EXM-EXC-" + m_procId, 888, 0);
    m_ipc->initialize(this);

    m_readyToStartEvent.clear();
    DEBUG("## [EXM] Created manager for '" + m_procId + "'")
}

//=============================================================================
// DESTRUCTOR: SPELLexecutorManager::~SPELLexecutorManager
//=============================================================================
SPELLexecutorManager::~SPELLexecutorManager()
{
    DEBUG("## [EXM] Destroyed manager for '" + m_procId + "'")
    SPELLprocessManager::instance().removeListener( m_procId, this );
    delete m_ipc;
    m_ipc = NULL;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::processStarted
//=============================================================================
void SPELLexecutorManager::processStarted( const std::string& identifier )
{
    DEBUG("[EXM] Executor process started: '" + identifier + "'")
    m_running = true;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::processFinished
//=============================================================================
void SPELLexecutorManager::processFinished( const std::string& identifier, const int& retValue )
{
    DEBUG("[EXM] Executor process finished: '" + identifier + "' (" + ISTR(retValue) + ")")
    m_running = false;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::processKilled
//=============================================================================
void SPELLexecutorManager::processKilled( const std::string& identifier )
{
    DEBUG("[EXM] Executor process killed: '" + identifier + "'")
    m_running = false;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::processFailed
//=============================================================================
void SPELLexecutorManager::processFailed( const std::string& identifier )
{
    DEBUG("[EXM] Executor process failed: '" + identifier + "'")
    m_running = false;
    /** \todo determine client mode here */
}

//=============================================================================
// METHOD :    SPELLexecutorManager::setConfiguration
//=============================================================================
void SPELLexecutorManager::setConfiguration( const SPELLexecutorManagerConfig& config )
{
    m_config = config;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::startExecutor
//=============================================================================
void SPELLexecutorManager::startExecutor()
{
    DEBUG("[EXM] Starting executor process now");
    if (m_executorCommand == "")
    {
        std::string script = SPELLconfiguration::instance().getContextParameter( ContextConstants::ExecutorScript );
        m_executorCommand = resolvePath( script );
    }
    std::string command = m_executorCommand;

    DEBUG("[EXM] Initial command: " + command);

    DEBUG("[EXM] Waiting IPC to be ready");
    m_readyToStartEvent.wait();

    std::string portStr = ISTR(m_ipc->getPort());

    command += " -p " + m_procId  + " -c " + SPELLconfiguration::instance().getFile() + " -n " + m_ctx.getName() + " -s " + portStr + " -w ";

    DEBUG("[EXM] Launch command: '" + command + "'");
    SPELLprocessManager::instance().startProcess( m_procId, command );
    bool timedout = waitLogin();

    if (!timedout)
    {
        DEBUG("[EXM] Login done");
        m_ctx.notifyExecutorOperation( m_procId, EXOP_OPEN, m_status, m_config.clientKey, m_config.clientMode );
    }
    else
    {
        // Let the control thread to finish
        throw SPELLcoreException("Cannot start executor", "Executor did not log in");
    }
}

//=============================================================================
// METHOD :    SPELLexecutorManager::closeExecutor
//=============================================================================
void SPELLexecutorManager::closeExecutor()
{
    // Ignore executor requests from now on
    m_ignore = true;
    m_running = false;

    DEBUG("[EXM] Closing executor process now")
    SPELLipcMessage closeMessage( MessageId::MSG_ID_CLOSE );
    closeMessage.setType( MSG_TYPE_ONEWAY );
    closeMessage.set( MessageField::FIELD_PROC_ID, m_procId );
    closeMessage.setSender( "CTX" );
    closeMessage.setReceiver( m_procId );
    m_ipc->sendMessage( m_executorKey, &closeMessage );
    DEBUG("[EXM] Close message sent")

    // Disconnect the output so that cancelled outgoing requests do not try
    // to send the response
    m_ipc->disconnectOutput( m_executorKey );
    // Cancel any pending requests. This is required in case the close
    // operation is performed in the middle of a notification for example.
    m_ipc->cancelOutgoingRequests( m_executorKey );

    bool timedout = waitLogout();
    if (!timedout)
    {
        DEBUG("[EXM] Logout event received, disconnecting")
        m_ipc->disconnect( false );
        DEBUG("[EXM] Executor process is closed")

        m_ctx.notifyExecutorOperation( m_procId, EXOP_CLOSE, m_status, m_config.clientKey, m_config.clientMode );
    }
    else
    {
        LOG_ERROR("[EXM] Failed to receive executor logout, killing the process")
        killExecutor();
        throw SPELLcoreException("Cannot close executor", "Executor did not log out");
    }
}

//=============================================================================
// METHOD :    SPELLexecutorManager::killExecutor
//=============================================================================
void SPELLexecutorManager::killExecutor()
{
    // Ignore executor requests from now on
    m_ignore = true;
    m_running = false;

    m_ipc->disconnect( false );

    DEBUG("[EXM] Request process manager to kill process")
    SPELLprocessManager::instance().killProcess( m_procId );

    m_ctx.notifyExecutorOperation( m_procId, EXOP_KILL, m_status, m_config.clientKey, m_config.clientMode );

}

//=============================================================================
// METHOD :    SPELLexecutorManager::attach
//=============================================================================
void SPELLexecutorManager::attach( const int& clientKey, const std::string& host, const SPELLclientMode& mode )
{
    DEBUG("[EXM] Attaching client " + ISTR(clientKey) + " to executor process")

    SPELLipcMessage* attachMsg = new SPELLipcMessage(MessageId::MSG_ID_ADD_CLIENT);
    attachMsg->setType( MSG_TYPE_ONEWAY );

    switch(mode)
    {
    case CLT_MODE_CONTROL:
        attachMsg->set( MessageField::FIELD_GUI_CONTROL, ISTR(clientKey) );
        attachMsg->set( MessageField::FIELD_GUI_CONTROL_HOST, host );
        break;
    case CLT_MODE_MONITOR:
        attachMsg->set( MessageField::FIELD_GUI_LIST, ISTR(clientKey) );
        attachMsg->set( MessageField::FIELD_GUI_HOST_LIST, host );
        break;
    default:
        throw SPELLcoreException("Cannot attach executor", "Invalid client mode");
    }

    sendMessage(attachMsg);
    delete attachMsg;

    m_ctx.notifyExecutorOperation( m_procId, EXOP_ATTACH, m_status, clientKey, mode );
}

//=============================================================================
// METHOD :    SPELLexecutorManager::detach
//=============================================================================
void SPELLexecutorManager::detach( const int& clientKey, const SPELLclientMode& clientMode )
{
    DEBUG("[EXM] Detaching client " + ISTR(clientKey) + " from executor process")

    SPELLipcMessage* detachMsg = new SPELLipcMessage(MessageId::MSG_ID_REMOVE_CLIENT);
    detachMsg->setType( MSG_TYPE_ONEWAY );

    switch(clientMode)
    {
    case CLT_MODE_CONTROL:
        detachMsg->set( MessageField::FIELD_GUI_CONTROL, ISTR(clientKey) );
        break;
    case CLT_MODE_MONITOR:
        detachMsg->set( MessageField::FIELD_GUI_LIST, ISTR(clientKey) );
        break;
    default:
        throw SPELLcoreException("Cannot detach executor", "Invalid client mode");
    }

    sendMessage(detachMsg);
    delete detachMsg;

    m_ctx.notifyExecutorOperation( m_procId, EXOP_DETACH, m_status, clientKey, CLT_MODE_NONE );
}

//=============================================================================
// METHOD :    SPELLexecutorManager::start()
//=============================================================================
void SPELLexecutorManager::start()
{
    DEBUG("[EXM] Starting executor control")
    m_ipc->connectIfc();
    m_ipc->start();
    // We can proceed to launch the executor process
    m_readyToStartEvent.set();
}

//=============================================================================
// METHOD :    SPELLexecutorManager::processMessage
//=============================================================================
void SPELLexecutorManager::processMessage( SPELLipcMessage* msg )
{
    if (msg->getId() == ExecutorMessages::MSG_NOTIF_EXEC_CLOSE )
    {
        LOG_INFO("[EXM] Executor logged out: " + msg->get( MessageField::FIELD_PROC_ID ) );
        executorLoggedOut();
    }
    else
    {
        DEBUG("[EXM] Forwarding message to clients")
        m_ctx.messageToExecutorClients( m_procId, msg );
    }
    // WARNING: incoming message is deleted by IncomingBase
}

//=============================================================================
// METHOD :    SPELLexecutorManager::processRequest
//=============================================================================
SPELLipcMessage* SPELLexecutorManager::processRequest( SPELLipcMessage* msg )
{
    SPELLipcMessage* response = NULL;

    if (m_ignore)
    {
        return SPELLipcHelper::createResponse( msg->getId(), msg );
    }

    if (msg->getId() == ExecutorMessages::REQ_NOTIF_EXEC_OPEN )
    {
        LOG_INFO("[EXM] Executor logged in: " + msg->get( MessageField::FIELD_PROC_ID ) );
        response = executorLoggedIn( msg );
    }
    else if (msg->getId() == MessageId::MSG_ID_PROMPT )
    {
        std::string procId = msg->get( MessageField::FIELD_PROC_ID );
        SPELLipcMessage* notifyStart = new SPELLipcMessage( MessageId::MSG_ID_PROMPT_START );
        notifyStart->setType( MSG_TYPE_ONEWAY );
        notifyStart->set( MessageField::FIELD_PROC_ID, procId );
        notifyStart->set( MessageField::FIELD_TEXT, msg->get( MessageField::FIELD_TEXT ));
        notifyStart->set( MessageField::FIELD_DATA_TYPE, msg->get( MessageField::FIELD_DATA_TYPE ));
        notifyStart->set( MessageField::FIELD_EXPECTED, msg->get( MessageField::FIELD_EXPECTED ));
        notifyStart->set( MessageField::FIELD_OPTIONS, msg->get( MessageField::FIELD_OPTIONS ));

        SPELLipcMessage* notifyEnd = new SPELLipcMessage( MessageId::MSG_ID_PROMPT_END );
        notifyEnd->set( MessageField::FIELD_PROC_ID, procId );
        notifyEnd->setType( MSG_TYPE_ONEWAY );

        m_ctx.messageToExecutorClients( procId, notifyStart, WHICH_MONITORING );
        response = m_ctx.forwardRequestToExecutorClients( procId, msg, WHICH_CONTROLLING );
        m_ctx.messageToExecutorClients( procId, notifyEnd, WHICH_MONITORING );

        delete notifyStart;
        delete notifyEnd;

        /** \todo what if there is no controlling client?? pause the proc! */
    }
    else if (msg->getType() == MSG_TYPE_NOTIFY )
    {
        if (msg->get( MessageField::FIELD_DATA_TYPE) == MessageValue::DATA_TYPE_STATUS)
        {
            m_status = StringToStatus( msg->get( MessageField::FIELD_EXEC_STATUS ));
            LOG_INFO("[EXM] Executor status: " + msg->get( MessageField::FIELD_EXEC_STATUS ));
            m_condition = msg->get( MessageField::FIELD_CONDITION );
            LOG_INFO("[EXM] Executor condition: " + m_condition );
            m_ctx.notifyExecutorStatus( m_procId, m_status );
        }
        else if (msg->get( MessageField::FIELD_DATA_TYPE) == MessageValue::DATA_TYPE_LINE )
        {
            m_csp = msg->get( MessageField::FIELD_CSP );
            LOG_INFO("[EXM] Executor csp: " + m_csp);
        }

        DEBUG("[EXM] @@@@@@@@@@@@@@@@@@@@@ Forwarding request to clients, " + msg->getId())
        response = m_ctx.forwardRequestToExecutorClients( m_procId, msg, WHICH_ALL );
        DEBUG("[EXM] @@@@@@@@@@@@@@@@@@@@@ Forwarding request to clients done, response " + response->data())
    }

    if (response == NULL)
    {
        LOG_ERROR("[EXM] Unprocessed request: " + msg->getId());
        response = SPELLipcHelper::createResponse( msg->getId(), msg );
    }

    // WARNING: incoming message is deleted by IncomingBase
    return response;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::processError
//=============================================================================
void SPELLexecutorManager::processError( std::string error, std::string reason )
{
    LOG_ERROR("[EXM] Unprocessed executor IPC error " + error + " (" + reason + ")");
    /** \todo Implement response to IPC executor error */
}

//=============================================================================
// METHOD :    SPELLexecutorManager::sendMessage
//=============================================================================
void SPELLexecutorManager::sendMessage( SPELLipcMessage* msg )
{
    if (m_running)
    {
        DEBUG("[EXM] Sending message to executor process")
        m_ipc->sendMessage( m_executorKey, msg );
    }
}

//=============================================================================
// METHOD :    SPELLexecutorManager::sendRequest
//=============================================================================
SPELLipcMessage* SPELLexecutorManager::sendRequest( SPELLipcMessage* msg, unsigned long timeoutSec )
{
    if (m_running)
    {
        DEBUG("[EXM] Forwarding request to executor process")
        return m_ipc->forwardRequest( m_executorKey, msg, timeoutSec );
    }
    return NULL;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::waitLogin
//=============================================================================
bool SPELLexecutorManager::waitLogin()
{
    DEBUG("[EXM] Waiting executor login")
    m_execLoginEvent.clear();
    bool timedout = m_execLoginEvent.wait(EXECUTOR_OPEN_TIMEOUT_SEC);
    if (!timedout)
    {
        DEBUG("[EXM] Executor logged in")
    }
    else
    {
        DEBUG("[EXM] Executor did not log in before timeout")
    }
    return timedout;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::waitLogout
//=============================================================================
bool SPELLexecutorManager::waitLogout()
{
    DEBUG("[EXM] Waiting executor logout")
    m_execLogoutEvent.clear();
    bool timedout = m_execLogoutEvent.wait(EXECUTOR_CLOSE_TIMEOUT_SEC);
    if (!timedout)
    {
        DEBUG("[EXM] Executor logged out")
    }
    else
    {
        DEBUG("[EXM] Executor did not logged out before timeout")
    }
    return timedout;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::executorLoggedIn
//=============================================================================
SPELLipcMessage* SPELLexecutorManager::executorLoggedIn( SPELLipcMessage* msg )
{
    DEBUG("[EXM] Received executor login message");
    SPELLipcMessage* response = SPELLipcHelper::createResponse( ExecutorMessages::RSP_NOTIF_EXEC_OPEN, msg );
    m_executorKey = msg->getKey();
    DEBUG("[EXM] Executor IPC key is " + ISTR(m_executorKey))

    std::string statusStr = msg->get( MessageField::FIELD_EXEC_STATUS );
    m_status = StringToStatus( statusStr );

    DEBUG("[EXM] Executor initial status is " + statusStr );

    if (m_status != STATUS_LOADED)
    {
        m_status = STATUS_ERROR;
        /** \todo set executor startup error info */
    }
    else
    {
        m_asrunFileName = msg->get( MessageField::FIELD_ASRUN_NAME );
        m_logFileName = msg->get( MessageField::FIELD_LOG_NAME );

        LOG_INFO("[EXM] Got ASRUN name: " + m_asrunFileName);

        /** \todo (MED) Parse asrun information */
        /** \todo Parse log information */
        /** \todo Parse wstart file information */

        // Set executor arguments
        if (m_config.arguments != "")
        {
            response->set( MessageField::FIELD_ARGS, m_config.arguments );
        }

        // Set executor open mode
        std::string openModeStr = "{";
        DEBUG("[EXM] Open procedure options: " + BSTR(m_config.automatic) + "," + BSTR(m_config.blocking) + "," + BSTR(m_config.visible));
        openModeStr += (m_config.automatic ? (Automatic + ":" + True) : (Automatic + ":" + False)) + ",";
        openModeStr += (m_config.blocking ? (Blocking + ":" + True) : (Blocking+ ":" + False)) + ",";
        openModeStr += (m_config.visible ? (Visible + ":" + True) : (Visible + ":" + False)) + "}";
        response->set( MessageField::FIELD_OPEN_MODE, openModeStr );
        response->set( MessageField::FIELD_GUI_CONTROL, ISTR(m_config.clientKey));
        response->set( MessageField::FIELD_GUI_CONTROL_HOST, m_config.clientHost );

        // Set executor condition
        if (m_config.condition != "")
        {
            response->set( MessageField::FIELD_CONDITION, m_config.condition );
        }
    }

    m_execLoginEvent.set();
    return response;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::executorLoggedOut
//=============================================================================
void SPELLexecutorManager::executorLoggedOut()
{
    m_execLogoutEvent.set();
}

//=============================================================================
// METHOD :    SPELLexecutorManager::isActive
//=============================================================================
bool SPELLexecutorManager::isActive()
{
    switch(m_status)
    {
    case STATUS_PAUSED:
    case STATUS_RUNNING:
    case STATUS_WAITING:
    case STATUS_LOADED:
    case STATUS_STEPPING:
        return true;
    default:
        return false;
    }
}

//=============================================================================
// METHOD :    SPELLexecutorManager::getStatus
//=============================================================================
SPELLexecutorStatus SPELLexecutorManager::getStatus()
{
    return m_status;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::getCSP
//=============================================================================
std::string SPELLexecutorManager::getCSP()
{
    return m_csp;
}

//=============================================================================
// METHOD :    SPELLexecutorManager::getStatusInformation()
//=============================================================================
SPELLexecutorInformation SPELLexecutorManager::getStatusInformation()
{
    SPELLexecutorInformation info;
    info.status = m_status;
    info.csp = m_csp;
    info.parentProc = m_parentProc;
    info.condition = m_condition;
    info.procId = m_procId;
    info.asrunName = m_asrunFileName;
    info.logName = m_logFileName;
    return info;
}

