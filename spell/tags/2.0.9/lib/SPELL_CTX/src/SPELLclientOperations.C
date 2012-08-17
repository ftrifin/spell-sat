// ################################################################################
// FILE       : SPELLclientOperations.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of client operations
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
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLclientInfo.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLclientOperations.H"
#include "SPELL_CTX/SPELLexecutorInformation.H"

#define DEFAULT_DATA_CHUNK_SIZE 2000

//=============================================================================
// CONSTRUCTOR : SPELLclientOperations::SPELLclientOperations
//=============================================================================
SPELLclientOperations::SPELLclientOperations( SPELLcontext& context )
    : SPELLclientRegistry(),
      m_context(context),
      m_dataChunker( DEFAULT_DATA_CHUNK_SIZE )
{
}

//=============================================================================
// DESTRUCTOR: SPELLclientOperations::~SPELLclientOperations
//=============================================================================
SPELLclientOperations::~SPELLclientOperations()
{
}

//=============================================================================
// METHOD :    SPELLclientOperations::processMessage
//=============================================================================
void SPELLclientOperations::processMessage( SPELLipcMessage* msg )
{
    int clientKey = msg->getKey();
    DEBUG("[CLT-OP] Received GUI message: " + msg->getId() + " from client " + ISTR(clientKey));
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);
    if (msg->getType() == MSG_TYPE_ONEWAY)
    {
        if (msg->getId() == ContextMessages::MSG_GUI_LOGIN)
        {
            std::string host = msg->get(MessageField::FIELD_HOST);
            LOG_INFO("GUI login: " + ISTR(clientKey) + " from " + host);
            SPELLclientInfo* info = new SPELLclientInfo( clientKey, host );
            createClient(info);
        }
        else if (msg->getId() == ContextMessages::MSG_GUI_LOGOUT)
        {
            std::string host = msg->get(MessageField::FIELD_HOST);
            LOG_INFO("GUI logout: " + ISTR(clientKey) + " from " + host);
            removeClient(clientKey);
        }
        else
        {
            // Forward message to executor
            SPELLexecutorList list;
            list.push_back( procId );
            m_context.messageToExecutors(list, msg);
        }
    }
    else
    {
        LOG_ERROR("[CLT-OP] Unprocessed request: " + msg->getId());
    }
}

//=============================================================================
// METHOD :    SPELLclientOperations::processRequest
//=============================================================================
SPELLipcMessage* SPELLclientOperations::processRequest( SPELLipcMessage* msg )
{
    SPELLipcMessage* response = NULL;

    if (msg->getId() == ContextMessages::REQ_EXEC_LIST)
    {
        response = request_ExecutorList(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_OPEN_EXEC)
    {
        response = request_OpenExecutor(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_CLOSE_EXEC)
    {
        response = request_CloseExecutor(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_KILL_EXEC)
    {
        response = request_KillExecutor(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_ATTACH_EXEC)
    {
        response = request_AttachExecutor(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_DETACH_EXEC)
    {
        response = request_DetachExecutor(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_EXEC_INFO)
    {
        response = request_ExecutorInfo(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_CLIENT_INFO)
    {
        response = request_ClientInfo(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_PROC_LIST)
    {
        response = request_ProcedureList(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_PROC_PROP)
    {
        response = request_ProcedureProperties(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_PROC_CODE)
    {
        response = request_ProcedureCode(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_SERVER_FILE)
    {
        response = request_ServerFile(msg);
    }
    else if (msg->getId() == ContextMessages::REQ_INSTANCE_ID)
    {
        response = request_InstanceId(msg);
    }
    // If the request is not processed here (not a client-related request), forward it to the context
    if (!response)
    {
        DEBUG("[CLT-OP] Request unprocessed, forwarding to executor")
        response = m_context.processClientRequest( msg );
    }
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::processError
//=============================================================================
void SPELLclientOperations::processError( std::string error, std::string reason )
{
	/** \todo client lost */
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_ExecutorList( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Requested list of executors")
    SPELLexecutorList list = m_context.getExecutorList();
    std::string strList = "";
    SPELLexecutorList::iterator it;
    SPELLexecutorList::iterator end = list.end();
    for( it = list.begin(); it != end; it++)
    {
        if (strList.length()>0) strList += ",";
        strList += (*it);
    }
    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_EXEC_LIST, msg);
    response->set( MessageField::FIELD_EXEC_LIST, strList );
    DEBUG("[CLT-OP] Request list of executors finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_OpenExecutor( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Requested opening executor")
    SPELLipcMessage* response;

    int clientKey = msg->getKey();
    std::string procId       = msg->get(MessageField::FIELD_PROC_ID);
    std::string args         = msg->get(MessageField::FIELD_ARGS);
    std::string cond         = msg->get(MessageField::FIELD_CONDITION);
    std::string clientHost   = msg->get(MessageField::FIELD_GUI_CONTROL_HOST);
    std::string clientMode   = msg->get(MessageField::FIELD_GUI_MODE);
    SPELLclientMode cmode    = SPELLclientInfo::clientModeFromString( clientMode );
    std::string openMode     = msg->get(MessageField::FIELD_OPEN_MODE);

    SPELLexecutorManagerConfig config;

    /** \todo set options from openMode string */
    config.blocking = true;
    config.visible = true;
    config.automatic = false;
    config.arguments = args;
    config.condition = cond;
    config.clientMode = cmode;
    config.clientKey = clientKey;
    config.clientHost = clientHost;

    try
    {
        m_context.openExecutor( procId, config );

        SPELLexecutorInformation info = m_context.getExecutorInformation( procId );

        std::string procName = SPELLprocedureManager::instance().getProcName(procId);

        response = SPELLipcHelper::createResponse( ContextMessages::RSP_OPEN_EXEC, msg );
        response->set( MessageField::FIELD_PROC_ID, procId );
        response->set( MessageField::FIELD_PROC_NAME, procName );
        response->set( MessageField::FIELD_PARENT_PROC, info.parentProc );
        response->set( MessageField::FIELD_ASRUN_NAME, info.asrunName );
        response->set( MessageField::FIELD_LOG_NAME, info.logName );
        response->set( MessageField::FIELD_EXEC_STATUS, StatusToString(info.status) );
        response->set( MessageField::FIELD_CONDITION, info.condition );
        response->set( MessageField::FIELD_GUI_LIST, " "); /** \todo complete */
        response->set( MessageField::FIELD_GUI_CONTROL, " " ); /** \todo complete */
        response->set( MessageField::FIELD_OPEN_MODE, " " ); /** \todo complete */
        response->set( MessageField::FIELD_CSP, info.csp );

        addExecutor( clientKey, procId, cmode );
    }
    catch(SPELLcoreException& ex)
    {
        response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
        response->set( MessageField::FIELD_ERROR, ex.getError() );
        response->set( MessageField::FIELD_FATAL, "True" );
        int errorCode = ex.getCode();
        std::string ecode = "";
        if (errorCode != -1) ecode = "( error code " + ISTR(errorCode) + " )";
        response->set( MessageField::FIELD_REASON, ex.getReason() + ecode );
    }

    DEBUG("[CLT-OP] Request opening executor finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_CloseExecutor( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request closing executor")
    SPELLipcMessage* response = NULL;
    try
    {
        std::string instanceId = msg->get(MessageField::FIELD_PROC_ID);

        m_context.closeExecutor( instanceId );

        // Remove it from the client infos
        SPELLclientKeyList list = getClientsForExecutor( instanceId );
        SPELLclientKeyList::iterator it;
        for( it = list.begin(); it != list.end(); it++)
        {
            removeExecutor( (*it), instanceId );
        }

        response = SPELLipcHelper::createResponse( ContextMessages::RSP_CLOSE_EXEC, msg );
    }
    catch(SPELLcoreException& ex)
    {
        response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
        response->set( MessageField::FIELD_ERROR, ex.getError() );
        response->set( MessageField::FIELD_FATAL, "True" );
        int errorCode = ex.getCode();
        std::string ecode = "";
        if (errorCode != -1) ecode = "( error code " + ISTR(errorCode) + " )";
        response->set( MessageField::FIELD_REASON, ex.getReason() + ecode );
    }
    DEBUG("[CLT-OP] Request closing executor finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_KillExecutor( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request killing executor")
    SPELLipcMessage* response = NULL;
    try
    {
        std::string procId = msg->get(MessageField::FIELD_PROC_ID);

        m_context.killExecutor( procId );

        // Remove it from the client infos
        SPELLclientKeyList list = getClientsForExecutor( procId );
        SPELLclientKeyList::iterator it;
        for( it = list.begin(); it != list.end(); it++)
        {
            removeExecutor( (*it), procId );
        }

        response = SPELLipcHelper::createResponse( ContextMessages::RSP_KILL_EXEC, msg );
    }
    catch(SPELLcoreException& ex)
    {
        response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
        response->set( MessageField::FIELD_ERROR, ex.getError() );
        response->set( MessageField::FIELD_FATAL, "True" );
        int errorCode = ex.getCode();
        std::string ecode = "";
        if (errorCode != -1) ecode = "( error code " + ISTR(errorCode) + " )";
        response->set( MessageField::FIELD_REASON, ex.getReason() + ecode );
    }
    DEBUG("[CLT-OP] Request killing executor finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_AttachExecutor( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request attaching to executor")
    SPELLipcMessage* response;
    try
    {
        int clientKey = msg->getKey();
        std::string procId = msg->get(MessageField::FIELD_PROC_ID);
        std::string clientMode = msg->get(MessageField::FIELD_GUI_MODE);
        std::string clientHost = msg->get(MessageField::FIELD_GUI_CONTROL_HOST);
        SPELLclientMode cmode = SPELLclientInfo::clientModeFromString( clientMode );

        m_context.attachExecutor( procId, clientKey, clientHost, cmode );

        DEBUG("[CLT-OP] Associate client " + ISTR(clientKey) + " to executor " + procId)

        addExecutor( clientKey, procId, cmode );

        response = SPELLipcHelper::createResponse( ContextMessages::RSP_ATTACH_EXEC, msg );

        DEBUG("[CLT-OP] Building executor information" );

        SPELLexecutorInformation info = m_context.getExecutorInformation( procId );
        processExecutorInfo( info, response );

        DEBUG("[CLT-OP] Executor information ready" );
    }
    catch(SPELLcoreException& ex)
    {
        response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
        response->set( MessageField::FIELD_ERROR, ex.getError() );
        response->set( MessageField::FIELD_FATAL, "True" );
        int errorCode = ex.getCode();
        std::string ecode = "";
        if (errorCode != -1) ecode = "( error code " + ISTR(errorCode) + " )";
        response->set( MessageField::FIELD_REASON, ex.getReason() + ecode );
    }
    DEBUG("[CLT-OP] Request attaching executor finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_DetachExecutor( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request detaching from executor")
    SPELLipcMessage* response;
    try
    {
        int clientKey = msg->getKey();
        std::string procId = msg->get(MessageField::FIELD_PROC_ID);

        SPELLclientMode mode = getClient(clientKey).getMode(procId);

        m_context.detachExecutor( procId, clientKey, mode );

        removeExecutor( clientKey, procId );

        response = SPELLipcHelper::createResponse( ContextMessages::RSP_DETACH_EXEC, msg );
    }
    catch(SPELLcoreException& ex)
    {
        response = SPELLipcHelper::createErrorResponse( ContextMessages::RSP_OPEN_EXEC, msg );
        response->set( MessageField::FIELD_ERROR, ex.getError() );
        response->set( MessageField::FIELD_FATAL, "True" );
        int errorCode = ex.getCode();
        std::string ecode = "";
        if (errorCode != -1) ecode = "( error code " + ISTR(errorCode) + " )";
        response->set( MessageField::FIELD_REASON, ex.getReason() + ecode );
    }
    DEBUG("[CLT-OP] Request detaching executor finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_ExecutorInfo( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request executor information")
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);

    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_EXEC_INFO, msg);

    SPELLexecutorInformation info = m_context.getExecutorInformation( procId );

    processExecutorInfo( info, response );

    DEBUG("[CLT-OP] Request executor information finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_ClientInfo( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request client information")
    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_CLIENT_INFO, msg);
    DEBUG("[CLT-OP] Request client information finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_ProcedureList( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Requested list of procedures")
    SPELLprocedureManager::ProcList list = m_context.getProcedureList();
    std::string strList = "";
    SPELLprocedureManager::ProcList::iterator it;
    SPELLprocedureManager::ProcList::iterator end = list.end();
    for( it = list.begin(); it != end; it++)
    {
        if (strList.length()>0) strList += ",";
        strList += (*it);
    }
    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_LIST, msg);
    response->set( MessageField::FIELD_PROC_LIST, strList );
    DEBUG("[CLT-OP] Request list of procedures finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_ProcedureProperties( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request procedure properties")
    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_PROP, msg);
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);
    response->set( MessageField::FIELD_PROC_ID, procId);

    std::string procName = SPELLprocedureManager::instance().getProcName(procId);
    response->set( MessageField::FIELD_PROC_NAME, procName);

    /** \todo get properties */
    DEBUG("[CLT-OP] Request procedure properties finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_ProcedureCode( SPELLipcMessage* msg )
{
	/** \todo move this to request_ServerFile method */
    DEBUG("[CLT-OP] Request procedure code")
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);

    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_PROC_CODE, msg);

    // If the request contains data chunk information
    std::string chunkNo = msg->get(MessageField::FIELD_CHUNK);
    std::string data = "";
    if (chunkNo != "")
    {
    	/** \todo data chunks */
    }
    else
    {
        DEBUG("[CLT-OP] Obtaining procedure code for " + procId )
        SPELLserverFile file = m_context.getServerFile( procId, FILE_CODE );
        /** \todo split in chunks */
        response->set(MessageField::FIELD_CHUNK, "0");
        response->set(MessageField::FIELD_TOTAL_CHUNKS, "0");

        data = linesToString( file.getLines() );
    }

    response->set(MessageField::FIELD_PROC_ID, procId);
    response->set(MessageField::FIELD_PROC_CODE, data);

    DEBUG("[CLT-OP] Request procedure code finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
std::string SPELLclientOperations::linesToString( std::list<std::string> lines )
{
    std::string linesStr = "";
    std::list<std::string>::iterator it;
    std::list<std::string>::iterator end = lines.end();
    for( it = lines.begin(); it != end; it++)
    {
        std::string add = (*it);
        if (add=="") add = " ";
        if (linesStr.length()>0) linesStr += IPCinternals::CODE_SEPARATOR;
        linesStr += add;
    }
    return linesStr;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_ServerFile( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request server file")

    std::string procId = msg->get( MessageField::FIELD_PROC_ID );

    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_SERVER_FILE, msg);

    if (msg->hasField( MessageField::FIELD_CHUNK))
    {
        // Get the corresponding file chunk
        int chunkNo = atoi( msg->get( MessageField::FIELD_CHUNK ).c_str() );
        int totalChunks = m_dataChunker.getSize( procId );

        DEBUG("[CLT-OP] Getting chunk " + ISTR(chunkNo) + " of " + ISTR(totalChunks));

        SPELLipcDataChunk::DataList chunk = m_dataChunker.getChunk( procId, chunkNo );
        response->set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks) );
        response->set( MessageField::FIELD_CHUNK, ISTR(chunkNo) );

        // Convert the chunk to a string
        std::string code = "";
        SPELLipcDataChunk::DataList::const_iterator it;
        SPELLipcDataChunk::DataList::const_iterator end = chunk.end();
        for( it = chunk.begin(); it != end; it++)
        {
            std::string line = *it;
            if ( line.size() == 0 ) continue;
            if (code.length()>0) code += "\n";
            code += line;
        }

        // Asign the chunk data
        response->set( MessageField::FIELD_SERVER_FILE, code );
    }
    else
    {
        DEBUG("[CLT-OP] Requesting file data");

        // Get the type of file requested
        std::string serverFileId = msg->get( MessageField::FIELD_SERVER_FILE_ID );
        SPELLserverFileType type = SPELLserverFile::stringToType(serverFileId);

        // Get the file from the context
        SPELLserverFile file = m_context.getServerFile( procId, type );

        // Get the list of file lines
        std::list<std::string> data = file.getLines();

        // Convert those to a data chunk type, and convert to single string
        // also to save time later
        SPELLipcDataChunk::DataList lines;
        std::list<std::string>::const_iterator it;
        std::string code = "";
        for( it = data.begin(); it != data.end(); it++)
        {
            std::string line = *it;
            if ( line.size() == 0 ) continue;
            lines.push_back( line );
            if (code.length()>0) code += "\n";
            code += line;
        }

        // Start chunks. If returned zero there is no need to split
        int totalChunks = m_dataChunker.startChunks( procId, lines );
        if (totalChunks == 0) // No need to split in chunks
        {
            DEBUG("[CLT-OP] No need to split in chunks");
            response->set( MessageField::FIELD_CHUNK, "0" );
            response->set( MessageField::FIELD_TOTAL_CHUNKS, "0" );
            response->set( MessageField::FIELD_SERVER_FILE, code );
        }
        else
        {
            DEBUG("[CLT-OP] Splitting into " + ISTR(totalChunks) + " chunks");
            response->set( MessageField::FIELD_CHUNK, "0" );
            response->set( MessageField::FIELD_TOTAL_CHUNKS, ISTR(totalChunks) );

            // Get the first chunk
            SPELLipcDataChunk::DataList chunk = m_dataChunker.getChunk( procId, 0 );

            // Convert the chunk to string
            SPELLipcDataChunk::DataList::iterator cit;
            std::string code = "";
            for( cit = chunk.begin(); cit != chunk.end(); cit++)
            {
                std::string line = *cit;
                if ( line.size() == 0 ) continue;
                if (code.length()>0) code += "\n";
                code += line;
            }

            // Assign the chunk to the response
            response->set( MessageField::FIELD_SERVER_FILE, code );
        }
    }
    DEBUG("[CLT-OP] Request server file finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
SPELLipcMessage* SPELLclientOperations::request_InstanceId( SPELLipcMessage* msg )
{
    DEBUG("[CLT-OP] Request instance id")
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);
    std::string instanceId = m_context.getInstanceId( procId );

    DEBUG("[CLT-OP] Obtained instance id: '" + instanceId + "'")

    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_INSTANCE_ID, msg);
    response->set( MessageField::FIELD_INSTANCE_ID, instanceId );
    DEBUG("[CLT-OP] Request instance id finished")
    return response;
}

//=============================================================================
// METHOD :    SPELLclientOperations::
//=============================================================================
void SPELLclientOperations::processExecutorInfo( const SPELLexecutorInformation& info, SPELLipcMessage* response )
{
    std::string cClient = (info.cClient > 0) ? ISTR(info.cClient) : " ";
    std::string mClients = "";
    std::list<int>::const_iterator it;
    for( it = info.mClients.begin(); it != info.mClients.end(); it++)
    {
        if (mClients.length()>0) mClients += ",";
        mClients += ISTR(*it);
    }
    response->set( MessageField::FIELD_PROC_ID, info.procId );
    response->set( MessageField::FIELD_PARENT_PROC, info.parentProc );
    response->set( MessageField::FIELD_ASRUN_NAME, info.asrunName );
    response->set( MessageField::FIELD_LOG_NAME, info.logName );
    response->set( MessageField::FIELD_EXEC_STATUS, StatusToString(info.status) );
    response->set( MessageField::FIELD_CONDITION, info.condition );
    response->set( MessageField::FIELD_GUI_LIST, mClients);
    response->set( MessageField::FIELD_GUI_CONTROL, cClient );
    response->set( MessageField::FIELD_OPEN_MODE, " " ); /** \todo complete */
    response->set( MessageField::FIELD_CSP, info.csp );
}
