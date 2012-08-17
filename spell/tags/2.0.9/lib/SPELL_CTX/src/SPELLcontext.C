// ################################################################################
// FILE       : SPELLcontext.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the context model
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcClientInterface.H"
#include "SPELL_IPC/SPELLipcServerInterface.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_CFG/SPELLconfiguration.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLexecutorRegistry.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLclientInfo.H"


static SPELLcontext* s_instance = NULL;

//=============================================================================
// CONSTRUCTOR : SPELLcontext::SPELLcontext
//=============================================================================
SPELLcontext::SPELLcontext()
{
    m_execRegistry = NULL;
    m_clientOperations = NULL;
    m_cltIPC = NULL;
    m_lstIPC = NULL;
}

//=============================================================================
// DESTRUCTOR: SPELLcontext::~SPELLcontext
//=============================================================================
SPELLcontext::~SPELLcontext()
{
    delete m_clientOperations;
    delete m_execRegistry;
    if (m_cltIPC != NULL)
    {
        delete m_cltIPC;
    }
    if (m_lstIPC != NULL)
    {
        delete m_lstIPC;
    }
}

//=============================================================================
// METHOD :    SPELLcontext::instance
//=============================================================================
SPELLcontext& SPELLcontext::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLcontext();
    }
    return *s_instance;
}

//=============================================================================
// METHOD :    SPELLcontext::start
//=============================================================================
void SPELLcontext::start()
{
    LOG_INFO("Starting context")
    m_execRegistry = new SPELLexecutorRegistry( *this );
    m_clientOperations = new SPELLclientOperations( *this );
    m_listenerOperations = new SPELLlistenerOperations( *this );

    try
    {
        DEBUG("[CTX] Setting up server interface for clients")
		/** \todo handle connection errors here */
        m_cltIPC = new SPELLipcServerInterface( "CTX-GUI", 999, 0 );
        m_cltIPC->initialize( m_clientOperations );
        DEBUG("[CTX] Connecting server")
        m_cltIPC->connectIfc();
        m_parameters.port = m_cltIPC->getPort();
        m_cltIPC->start();
        DEBUG("[CTX] Server ready")
    }
    catch(SPELLipcError& ex)
    {
        LOG_ERROR("[CTX] Error while trying to setup server for clients")
        LOG_ERROR("      " + std::string(ex.what()))
        return;
    }

    try
    {
        DEBUG("[CTX] Setting up listener interface")
        m_lstIPC = new SPELLipcClientInterface( "CTX-LST", "localhost", m_parameters.listenerPort );
        m_lstIPC->initialize( m_listenerOperations );
        m_lstIPC->connectIfc();
        DEBUG("[CTX] Login into listener")
        loginIntoListener();
    }
    catch(SPELLipcError& ex)
    {
        LOG_ERROR("[CTX] Error while trying to connect to listener port")
        LOG_ERROR("      " + std::string(ex.what()))
        return;
    }

    try
    {
        LOG_INFO("Loading configuration")
        // Load the SPELL configuration (will fail with exception if there is an error)
        SPELLconfiguration::instance().loadConfig(m_parameters.configFile);
    }
    catch(SPELLcoreException& ex )
    {
        LOG_ERROR("[CTX] Error while trying to load configuration")
        LOG_ERROR("      " + std::string(ex.what()))
        /** \todo send error to listener */
        return;
    }

    try
    {
        LOG_INFO("Preparing procedures")
        SPELLprocedureManager::instance().setup(m_parameters.name);
    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[CTX] Error while trying to prepare procedures")
        LOG_ERROR("      " + std::string(ex.what()))
        /** \todo send error to listener */
        return;
    }

    LOG_INFO("[CTX] Context ready")
    std::cerr << "##################################################" << std::endl;
    std::cerr << "##      CONTEXT READY: " << m_parameters.name << std::endl;
    std::cerr << "##################################################" << std::endl;
    m_closeEvent.clear();
    m_closeEvent.wait();
    LOG_INFO("[CTX] Context process finishing")
    std::cerr << "##################################################" << std::endl;
    std::cerr << "##      CONTEXT STOPPED: " << m_parameters.name << std::endl;
    std::cerr << "##################################################" << std::endl;
}

//=============================================================================
// METHOD :    SPELLcontext::getName
//=============================================================================
std::string SPELLcontext::getName()
{
    return m_parameters.name;
}

//=============================================================================
// METHOD :    SPELLcontext::getPort
//=============================================================================
int SPELLcontext::getPort()
{
    return m_parameters.port;
}

//=============================================================================
// METHOD :    SPELLcontext::close
//=============================================================================
void SPELLcontext::close()
{
    DEBUG("[CTX] Stopping all remamining executors" )
	/** \todo close executors here by invoking closeAll() */
    DEBUG("[CTX] Login out from listener" )
    logoutFromListener();
    DEBUG("[CTX] Disconnect interface with listener" )
    m_lstIPC->disconnect(true);
    DEBUG("[CTX] Disconnect interface with GUIs" )
    m_cltIPC->disconnect(true);
    DEBUG("[CTX] Closing now" )
    m_closeEvent.set();
}

//=============================================================================
// METHOD :    SPELLcontext::loginIntoListener
//=============================================================================
void SPELLcontext::loginIntoListener()
{
    SPELLipcMessage login( ListenerMessages::MSG_CONTEXT_OPEN );
    login.setType(MSG_TYPE_ONEWAY);
    login.set( MessageField::FIELD_CTX_NAME, m_parameters.name );
    login.set( MessageField::FIELD_CTX_PORT, ISTR(m_parameters.port) );
    login.setSender("CTX");
    login.setReceiver("LST");
    m_lstIPC->sendMessage( &login );
}

//=============================================================================
// METHOD :    SPELLcontext::logoutFromListener
//=============================================================================
void SPELLcontext::logoutFromListener()
{
    SPELLipcMessage logout( ListenerMessages::MSG_CONTEXT_CLOSED );
    logout.setType(MSG_TYPE_ONEWAY);
    logout.set( MessageField::FIELD_CTX_NAME, m_parameters.name );
    logout.set( MessageField::FIELD_CTX_PORT, ISTR(m_parameters.port) );
    logout.setSender("CTX");
    logout.setReceiver("LST");
    m_lstIPC->sendMessage( &logout );
}

//=============================================================================
// METHOD :    SPELLcontext::openExecutor
//=============================================================================
void SPELLcontext::openExecutor( const std::string& procId, const SPELLexecutorManagerConfig& config )
{
    // Prevent further operations meanwhile this one is being performed
    SPELLmonitor m(m_operationsLock);

    DEBUG("[CTX] Opening executor for '" + procId + "'" )
    // Create the executor manager and start it
    SPELLexecutorManager& mgr = m_execRegistry->createExecutor(procId);
    mgr.setConfiguration(config);
    // Will setup IPC
    mgr.start();

    try
    {
        // Will wait until executor logs in or raise an exception on failure
        mgr.startExecutor();
        LOG_INFO("[CTX] Executor '" + procId + "' open successfully")
        /** \todo determine client mode here */

    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[CTX] Failed to open executor: " + std::string(ex.what()));
        m_execRegistry->removeExecutor(procId);
        // Re-throw the exception
        throw;
    }
    DEBUG("[CTX] Opening executor for '" + procId + "' finished" )
}

//=============================================================================
// METHOD :    SPELLcontext::closeExecutor
//=============================================================================
void SPELLcontext::closeExecutor( const std::string& instanceId )
{
    // Prevent further operations meanwhile this one is being performed
    SPELLmonitor m(m_operationsLock);

    DEBUG("[CTX] Closing executor '" + instanceId + "'")

    // Obtain the executor
    SPELLexecutorManager& mgr = m_execRegistry->getExecutor(instanceId);

    try
    {
        // Close it and wait cleanup
        // Will wait until executor logs out or raise an exception on failure
        mgr.closeExecutor();

        // Remove it from registry
        m_execRegistry->removeExecutor( instanceId );

        LOG_INFO("[CTX] Executor '" + instanceId + "' closed successfully")

    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[CTX] Failed to close executor: " + std::string(ex.what()));
        // Remove it from registry
        m_execRegistry->removeExecutor( instanceId );
        // Re-throw the exception
        throw;
    }
    DEBUG("[CTX] Executor closed")
}

//=============================================================================
// METHOD :    SPELLcontext::killExecutor
//=============================================================================
void SPELLcontext::killExecutor( const std::string& instanceId )
{
    // Prevent further operations meanwhile this one is being performed
    SPELLmonitor m(m_operationsLock);

    DEBUG("[CTX] Killing executor '" + instanceId + "'")

    // Obtain the executor
    SPELLexecutorManager& mgr = m_execRegistry->getExecutor(instanceId);

    // Kill it and wait cleanup
    mgr.killExecutor();

    // Remove it from registry
    m_execRegistry->removeExecutor( instanceId );

    LOG_INFO("[CTX] Executor '" + instanceId + "' killed successfully")
}

//=============================================================================
// METHOD :    SPELLcontext::attachExecutor
//=============================================================================
void SPELLcontext::attachExecutor( const std::string& instanceId, const int& clientKey, const std::string& clientHost, const SPELLclientMode& clientMode )
{
    // Prevent further operations meanwhile this one is being performed
    SPELLmonitor m(m_operationsLock);

    std::string modes = (clientMode == CLT_MODE_CONTROL) ? " controlling " : " monitoring ";

    DEBUG("[CTX] Client " + ISTR(clientKey) + " attaching to '" + instanceId + "' in" + modes + "mode" );

    // Get the executor
    SPELLexecutorManager& mgr = m_execRegistry->getExecutor(instanceId);

    // Attach the client to it
    mgr.attach( clientKey, clientHost, clientMode );

    LOG_INFO("[CTX] Client " + ISTR(clientKey) + " attached to '" + instanceId + "' in" + modes + "mode" );

}

//=============================================================================
// METHOD :    SPELLcontext::detachExecutor
//=============================================================================
void SPELLcontext::detachExecutor( const std::string& instanceId, const int& clientKey, const SPELLclientMode& clientMode )
{
    // Prevent further operations meanwhile this one is being performed
    SPELLmonitor m(m_operationsLock);

    DEBUG("[CTX] Client " + ISTR(clientKey) + " detaching from '" + instanceId + "'" );

    // Get the executor
    SPELLexecutorManager& mgr = m_execRegistry->getExecutor(instanceId);

    // Detach the client from it
    mgr.detach( clientKey, clientMode );

    LOG_INFO("[CTX] Client " + ISTR(clientKey) + " detached from '" + instanceId);
}

//=============================================================================
// METHOD :    SPELLcontext::getExecutorList
//=============================================================================
SPELLexecutorList SPELLcontext::getExecutorList()
{
    return m_execRegistry->getExecutorList();
}

//=============================================================================
// METHOD :    SPELLcontext::getProcedureList
//=============================================================================
SPELLprocedureManager::ProcList SPELLcontext::getProcedureList()
{
    SPELLprocedureManager::ProcList list;
    list = SPELLprocedureManager::instance().getProcList();
    return list;
}

//=============================================================================
// METHOD :    SPELLcontext::getInstanceId
//=============================================================================
std::string SPELLcontext::getInstanceId( const std::string& procId )
{
    return m_execRegistry->getInstanceId(procId);
}

//=============================================================================
// METHOD :    SPELLcontext::getExecutorInformation
//=============================================================================
SPELLexecutorInformation SPELLcontext::getExecutorInformation( const std::string& procId )
{
    DEBUG("[CTX] Obtain information of " + procId);
    SPELLexecutorInformation info;
    try
    {
        SPELLexecutorManager& mgr = m_execRegistry->getExecutor(procId);
        DEBUG("[CTX] Manager obtained, getting information");
        info = mgr.getStatusInformation();
    }
    catch( SPELLcoreException& ex )
    {
        DEBUG("[CTX] No manager available yet, generating empty information");
        info.procId = procId;
        info.status = STATUS_UNINIT;
        info.condition = "";
        info.asrunName = "";
        info.logName = "";
        info.csp = "";
        info.parentProc = "";
    }
    // Add the client information
    SPELLclientKeyList clientList = m_clientOperations->getClientsForExecutor(procId, WHICH_CONTROLLING );
    if (clientList.size()>0)
    {
        info.cClient = clientList.at(0);
    }
    else
    {
        info.cClient = -1;
    }
    clientList = m_clientOperations->getClientsForExecutor(procId, WHICH_MONITORING );
    SPELLclientKeyList::iterator it;
    SPELLclientKeyList::iterator end = clientList.end();
    for( it = clientList.begin(); it != end; it++)
    {
        info.mClients.push_back( *it );
    }
    DEBUG("[CTX] Executor information built");
    return info;
}

//=============================================================================
// METHOD :    SPELLcontext::getServerFile
//=============================================================================
SPELLserverFile SPELLcontext::getServerFile( const std::string& procId, const SPELLserverFileType& type )
{
    SPELLserverFile file(type);
    switch(type)
    {
    case FILE_CODE:
    {
        std::string source = SPELLprocedureManager::instance().getSourceCode(procId).getSourceCode();
        file.loadFromContents(source);
        break;
    }
    case FILE_ASRUN:
    {
        SPELLexecutorManager& mgr = m_execRegistry->getExecutor(procId);
        std::string fileName = mgr.getAsRunFileName();
        file.loadFromPath(fileName);
        break;
    }
    case FILE_LOG:
    {
        SPELLexecutorManager& mgr = m_execRegistry->getExecutor(procId);
        std::string fileName = mgr.getLogFileName();
        file.loadFromPath(fileName);
        break;
    }
    default:
        break;
    }
    return file;
}

//=============================================================================
// METHOD :    SPELLcontext::notifyExecutorOperation
//=============================================================================
void SPELLcontext::notifyExecutorOperation( const std::string& procId,
        const SPELLexecutorOperation& operation,
        const SPELLexecutorStatus& status,
        const int& clientKey, const SPELLclientMode& mode )
{
    DEBUG("[CTX] ##################### Notifying executor operation for '" + procId + "'")

    SPELLclientKeyList list = m_clientOperations->getAllClients();

    SPELLipcMessage message( MessageId::MSG_ID_EXEC_OP );
    message.setType(MSG_TYPE_ONEWAY);
    message.set( MessageField::FIELD_PROC_ID, procId );
    message.set( MessageField::FIELD_GUI_KEY, ISTR(clientKey) );
    message.set( MessageField::FIELD_GUI_MODE, SPELLclientInfo::clientModeToString(mode) );
    message.set( MessageField::FIELD_EXOP, SPELLexecutorOperations::executorOperationToString(operation) );
    message.set( MessageField::FIELD_EXEC_STATUS, StatusToString(status) );

    DEBUG("[CTX] Operation is : " + SPELLexecutorOperations::executorOperationToString(operation) )

    // Notify all clients
    messageToClients( list, &message );

//    // Notify parent executor, if any
//    std::string parentProcId = mgr.getParent();
//    if ( parentProcId != "")
//    {
//        DEBUG("[CTX] Notifying parent procedure also")
//    	SPELLexecutorList list;
//    	list.push_back(parentProcId);
//        messageToExecutors( list, message );
//    }
    DEBUG("[CTX] ##################### Notifying executor operation done")
}

//=============================================================================
// METHOD :    SPELLcontext::notifyExecutorError
//=============================================================================
void SPELLcontext::notifyExecutorError( const std::string& procId,
                                        const std::string& error,
                                        const std::string& reason )
{
    DEBUG("[CTX] Notifying executor error for '" + procId + "'")

    SPELLclientKeyList list = m_clientOperations->getAllClients();
    //SPELLexecutorManager& mgr = m_execRegistry->getExecutor(procId);

    SPELLipcMessage message( MessageId::MSG_ID_ERROR );
    message.setType(MSG_TYPE_ERROR);
    message.set( MessageField::FIELD_PROC_ID, procId );
    message.set( MessageField::FIELD_ERROR, error );
    message.set( MessageField::FIELD_REASON, reason );
    message.set( MessageField::FIELD_FATAL, "False" ); /** \todo complete */

    // Notify all clients
    messageToClients( list, &message );

    /** \todo review this */
//    // Notify parent executor, if any
//    std::string parentProcId = mgr.getParent();
//    if ( parentProcId != "")
//    {
//        DEBUG("[CTX] Notifying parent procedure also")
//    	SPELLexecutorList list;
//    	list.push_back(parentProcId);
//        messageToExecutors( list, message );
//    }
}

//=============================================================================
// METHOD :    SPELLcontext::notifyExecutorStatus
//=============================================================================
void SPELLcontext::notifyExecutorStatus( const std::string& procId, const SPELLexecutorStatus& status )
{
    DEBUG("[CTX] ##################### Notifying executor status for '" + procId + "'")

    SPELLclientKeyList list = m_clientOperations->getAllClients();
    SPELLexecutorManager& mgr = m_execRegistry->getExecutor(procId);

    SPELLipcMessage message( MessageId::MSG_ID_EXEC_OP );
    message.setType(MSG_TYPE_ONEWAY);
    message.set( MessageField::FIELD_PROC_ID, procId );
    message.set( MessageField::FIELD_EXEC_STATUS, StatusToString(mgr.getStatus()) );
    message.set( MessageField::FIELD_CONDITION, mgr.getCondition() );
    message.set( MessageField::FIELD_EXOP, MessageValue::DATA_TYPE_STATUS );
    message.set( MessageField::FIELD_GUI_KEY, " " );
    message.set( MessageField::FIELD_GUI_MODE, " " );

    DEBUG("[CTX] Status is " +  SPELLexecutorManager::statusToString(mgr.getStatus()))

    // Notify all clients
    messageToClients( list, &message );

//    // Notify parent executor, if any
//    std::string parentProcId = mgr.getParent();
//    if ( parentProcId != "")
//    {
//        DEBUG("[CTX] Notifying parent procedure also")
//    	SPELLexecutorList list;
//    	list.push_back(parentProcId);
//        messageToClients( list, message, false );
//    }
    DEBUG("[CTX] ##################### Notifying executor status done")
}

//=============================================================================
// METHOD :    SPELLcontext::forwardRequestToExecutorClients
//=============================================================================
SPELLipcMessage* SPELLcontext::forwardRequestToExecutorClients( const std::string& procId, SPELLipcMessage* request, const SPELLwhichClients& who )
{
    SPELLclientKeyList clients = m_clientOperations->getClientsForExecutor( procId, who );
    return forwardRequestToClients( clients, request );
}

//=============================================================================
// METHOD :    SPELLcontext::messageToExecutorClients
//=============================================================================
void SPELLcontext::messageToExecutorClients( const std::string& procId, SPELLipcMessage* message, const SPELLwhichClients& who )
{
    SPELLclientKeyList list = m_clientOperations->getClientsForExecutor( procId, who );
    messageToClients( list, message );
}

//=============================================================================
// METHOD :    SPELLcontext::messageToClients
//=============================================================================
void SPELLcontext::messageToClients( SPELLclientKeyList clients, SPELLipcMessage* message )
{
    SPELLclientKeyList::iterator it;
    DEBUG("[CTX] 	Start message-to-clients")
    for( it = clients.begin(); it != clients.end(); it++)
    {
        DEBUG("[CTX]        - sending message " + message->getId() + " to client " + ISTR( (*it) ))
        m_cltIPC->sendMessage( (*it), message );
    }
    DEBUG("[CTX] 	End message-to-clients")
}

//=============================================================================
// METHOD :    SPELLcontext::forwardRequestToClients
//=============================================================================
SPELLipcMessage* SPELLcontext::forwardRequestToClients( SPELLclientKeyList clients, SPELLipcMessage* request )
{
    SPELLipcMessage* firstResponse = NULL;
    bool first = true;
    DEBUG("[CTX] 	Start forward-request-to-clients")
    for( SPELLclientKeyList::iterator it = clients.begin(); it != clients.end(); it++)
    {
        DEBUG("[CTX]        - forwarding request " + request->getId() + " to client " + ISTR( (*it) ))
        SPELLipcMessage* response = m_cltIPC->forwardRequest( (*it), request, 15 );
        if (first)
        {
            first = false;
            firstResponse = response;
        }
        else
        {
            delete response;
        }
    }
    DEBUG("[CTX] 	End forward-request-to-clients, response: " + firstResponse->data())
    return firstResponse;
}

//=============================================================================
// METHOD :    SPELLcontext::messageToExecutors
//=============================================================================
void SPELLcontext::messageToExecutors( SPELLexecutorList executors, SPELLipcMessage* message )
{
    DEBUG("[CTX] 	Start message-to-executors")
    for( SPELLexecutorList::iterator it = executors.begin(); it != executors.end(); it++)
    {
        SPELLexecutorManager& mgr = m_execRegistry->getExecutor((*it));
        DEBUG("[CTX]        - sending message " + message->getId() + " to '" + (*it)  + "'")
        mgr.sendMessage( message );
    }
    DEBUG("[CTX] 	End message-to-executors")
}

//=============================================================================
// METHOD :    SPELLcontext::requestToExecutors
//=============================================================================
SPELLipcMessage* SPELLcontext::requestToExecutors( SPELLexecutorList executors, SPELLipcMessage* request, bool keepResponse )
{
    DEBUG("[CTX] 	Start request-to-executors")
    SPELLipcMessage* firstResponse = NULL;
    bool first = true;
    for( SPELLexecutorList::iterator it = executors.begin(); it != executors.end(); it++)
    {
        SPELLexecutorManager& mgr = m_execRegistry->getExecutor((*it));
        DEBUG("[CTX]        - sending request " + request->getId() + " to '" + (*it)  + "'")
        SPELLipcMessage* response = mgr.sendRequest( request, 15 );
        if (keepResponse && first)
        {
            first = false;
            firstResponse = response;
        }
        else
        {
            delete response;
        }
    }
    DEBUG("[CTX] 	End request-to-executors")
    return firstResponse;
}

//=============================================================================
// METHOD :    SPELLcontext::processClientRequest
//=============================================================================
SPELLipcMessage* SPELLcontext::processClientRequest( SPELLipcMessage* msg )
{
    SPELLipcMessage* response = NULL;
    try
    {
        // Forward the request to the given executor
        std::string procId = msg->get( MessageField::FIELD_PROC_ID );
        DEBUG("[CTX] 	Forward request " + msg->getId() + " to executor " + procId)
        SPELLexecutorList list;
        list.push_back(procId);
        response = requestToExecutors( list, msg, true );
        DEBUG("[CTX] 	Response obtained from executor " + procId)
    }
    catch(SPELLcoreException& ex)
    {
        response = SPELLipcHelper::createErrorResponse( msg->getId(), msg );
        response->set( MessageField::FIELD_ERROR, "Cannot process request");
        response->set( MessageField::FIELD_REASON, ex.what() );
    }
    return response;
}
