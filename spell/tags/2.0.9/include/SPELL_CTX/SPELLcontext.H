// ################################################################################
// FILE       : SPELLcontext.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Context model
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

#ifndef __SPELL_CONTEXT_H__
#define __SPELL_CONTEXT_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_CIF/SPELLnotifications.H"
#include "SPELL_SYN/SPELLmutex.H"
#include "SPELL_SYN/SPELLevent.H"
#include "SPELL_IPC/SPELLipcServerInterface.H"
#include "SPELL_IPC/SPELLipcClientInterface.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorRegistry.H"
#include "SPELL_CTX/SPELLexecutorManager.H"
#include "SPELL_CTX/SPELLclientRegistry.H"
#include "SPELL_CTX/SPELLclientInfo.H"
#include "SPELL_CTX/SPELLclientOperations.H"
#include "SPELL_CTX/SPELLexecutorOperations.H"
#include "SPELL_CTX/SPELLlistenerOperations.H"
#include "SPELL_CTX/SPELLcontextParameters.H"
#include "SPELL_CTX/SPELLexecutorInformation.H"
#include "SPELL_CTX/SPELLserverFile.H"

/** \addtogroup SPELL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////



//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief SPELL context model
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcontext
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    ~SPELLcontext();

    // STATIC ////////////////////////////////////////////////////////////////

    static SPELLcontext& instance();

    // METHODS ///////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Configure the context
    **////////////////////////////////////////////////////////////////////
    void configure( SPELLcontextParameters params ) {
        m_parameters = params;
    };

    //////////////////////////////////////////////////////////////////////
    /** Start the context
    **////////////////////////////////////////////////////////////////////
    void start();

    //////////////////////////////////////////////////////////////////////
    /** Stop the context
    **////////////////////////////////////////////////////////////////////
    void close();

    //////////////////////////////////////////////////////////////////////
    /** Obtain context name
    **////////////////////////////////////////////////////////////////////
    std::string getName();

    //////////////////////////////////////////////////////////////////////
    /** Obtain context listening port
    **////////////////////////////////////////////////////////////////////
    int getPort();

    //========================================================================
    // Operations on executors
    //========================================================================

    void openExecutor( const std::string& procId, const SPELLexecutorManagerConfig& config );
    void closeExecutor( const std::string& instanceId );
    void killExecutor( const std::string& instanceId );
    void attachExecutor( const std::string& instanceId, const int& clientKey, const std::string& clientHost, const SPELLclientMode& clientMode );
    void detachExecutor( const std::string& instanceId, const int& clientKey, const SPELLclientMode& clientMode );
    std::string getInstanceId( const std::string& procId );

    //========================================================================
    // Information requests
    //========================================================================

    SPELLexecutorList getExecutorList();
    SPELLprocedureManager::ProcList getProcedureList();
    SPELLexecutorInformation getExecutorInformation( const std::string& procId );
    SPELLserverFile getServerFile( const std::string& procId, const SPELLserverFileType& type );

    //========================================================================
    // Notifications regarding clients
    //========================================================================

    void notifyExecutorError( const std::string& procId, const std::string& error, const std::string& reason );
    void notifyExecutorStatus( const std::string& procId, const SPELLexecutorStatus& status );
    void notifyExecutorOperation( const std::string& procId, const SPELLexecutorOperation& operation,
                                  const SPELLexecutorStatus& status,
                                  const int& clientKey, const SPELLclientMode& mode );


    //========================================================================
    // Forward executor msg/rqst to GUI clients
    //========================================================================

    SPELLipcMessage* forwardRequestToExecutorClients( const std::string& procId, SPELLipcMessage* message, const SPELLwhichClients& who = WHICH_ALL );
    void messageToExecutorClients( const std::string& procId, SPELLipcMessage* message, const SPELLwhichClients& who = WHICH_ALL );

    //========================================================================
    // Messages going to clients
    //========================================================================

    SPELLipcMessage* forwardRequestToClients( SPELLclientKeyList clients, SPELLipcMessage* request );
    void messageToClients( SPELLclientKeyList clients, SPELLipcMessage* message );

    //========================================================================
    // Messages going to executors
    //========================================================================

    void messageToExecutors( SPELLexecutorList executors, SPELLipcMessage* message );
    SPELLipcMessage* requestToExecutors( SPELLexecutorList executors, SPELLipcMessage* request, bool keepResponse );

    //========================================================================
    // Message and request processing from clients
    //========================================================================

    SPELLipcMessage* processClientRequest( SPELLipcMessage* msg );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor.
    **////////////////////////////////////////////////////////////////////
    SPELLcontext();

    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Login into listener process
    **////////////////////////////////////////////////////////////////////
    void loginIntoListener();

    //////////////////////////////////////////////////////////////////////
    /** Logout from listener process
    **////////////////////////////////////////////////////////////////////
    void logoutFromListener();

    //////////////////////////////////////////////////////////////////////
    /** Wait the close event
    **////////////////////////////////////////////////////////////////////
    void waitCloseEvent();

    // DATA MEMBERS //////////////////////////////////////////////////////////
    /** Holds the configuration parameters for the context */
    SPELLcontextParameters        m_parameters;
    /** This class processes messages coming from clients */
    SPELLclientOperations*        m_clientOperations;
    /** This class processes messages coming from listener */
    SPELLlistenerOperations*      m_listenerOperations;
    /** Registry of logged executors */
    SPELLexecutorRegistry*        m_execRegistry;
    /** IPC interface for clients */
    SPELLipcServerInterface*      m_cltIPC;
    /** IPC interface for listener */
    SPELLipcClientInterface*      m_lstIPC;
    SPELLmutex                    m_notificationsLock;
    SPELLmutex                    m_operationsLock;
    SPELLevent					  m_closeEvent;
};

#endif
