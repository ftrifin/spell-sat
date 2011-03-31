// ################################################################################
// FILE       : SPELLexecutorManager.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor manager
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

#ifndef __SPELL_EXMANAGER_H__
#define __SPELL_EXMANAGER_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_IPC/SPELLipcServerInterface.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_PRC/SPELLprocessManager.H"
#include "SPELL_SYN/SPELLthread.H"
#include "SPELL_SYN/SPELLevent.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLclientInfo.H"
#include "SPELL_CTX/SPELLexecutorManagerConfig.H"
#include "SPELL_CTX/SPELLexecutorInformation.H"

/** \addtogroup SPELL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLcontext;
// TYPES ///////////////////////////////////////////////////////////////////

// DEFINES /////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Executor interface for SPELL context
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutorManager : public SPELLprocessListener, public SPELLipcInterfaceListener
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor.
    **////////////////////////////////////////////////////////////////////
    SPELLexecutorManager( const std::string& instanceId, SPELLcontext& context );

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    ~SPELLexecutorManager();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Get the procedure identifier (with instance number)
    **////////////////////////////////////////////////////////////////////
    std::string getProcId() {
        return m_procId;
    };

    //////////////////////////////////////////////////////////////////////
    /** Configure the executor
    **////////////////////////////////////////////////////////////////////
    void setConfiguration( const SPELLexecutorManagerConfig& config );

    //////////////////////////////////////////////////////////////////////
    /** Get the executor status information
    **////////////////////////////////////////////////////////////////////
    SPELLexecutorInformation getStatusInformation();

    //////////////////////////////////////////////////////////////////////
    /** Start the executor process with the given configuration
    **////////////////////////////////////////////////////////////////////
    void startExecutor();

    //////////////////////////////////////////////////////////////////////
    /** Close the executor process
    **////////////////////////////////////////////////////////////////////
    void closeExecutor();

    //////////////////////////////////////////////////////////////////////
    /** Kill the executor process
    **////////////////////////////////////////////////////////////////////
    void killExecutor();

    //////////////////////////////////////////////////////////////////////
    /** Attach a client to the executor
    **////////////////////////////////////////////////////////////////////
    void attach( const int& clientKey, const std::string& host, const SPELLclientMode& mode );

    //////////////////////////////////////////////////////////////////////
    /** Detach a client from the executor
    **////////////////////////////////////////////////////////////////////
    void detach( const int& clientKey, const SPELLclientMode& clientMode );

    //////////////////////////////////////////////////////////////////////
    /** Process manager callback
    **////////////////////////////////////////////////////////////////////
    void processStarted( const std::string& identifier );

    //////////////////////////////////////////////////////////////////////
    /** Process manager callback
    **////////////////////////////////////////////////////////////////////
    void processFinished( const std::string& identifier, const int& retValue );

    //////////////////////////////////////////////////////////////////////
    /** Process manager callback
    **////////////////////////////////////////////////////////////////////
    void processKilled( const std::string& identifier );

    //////////////////////////////////////////////////////////////////////
    /** Process manager callback
    **////////////////////////////////////////////////////////////////////
    void processFailed( const std::string& identifier );

    //////////////////////////////////////////////////////////////////////
    /** Start the working loop
    **////////////////////////////////////////////////////////////////////
    void start();

    //////////////////////////////////////////////////////////////////////
    /** IPC callbacks
    **////////////////////////////////////////////////////////////////////
    void processMessage( SPELLipcMessage* msg );
    SPELLipcMessage* processRequest( SPELLipcMessage* msg );
    void processError( std::string error, std::string reason );

    bool isActive();
    SPELLexecutorStatus getStatus();
    std::string getCSP();
    std::string getCondition() {
        return m_condition;
    };
    std::string getParent() {
        return m_parentProc;
    };
    void setParent( std::string procId ) {
        m_parentProc = procId;
    };

    std::string getAsRunFileName() const {
        return m_asrunFileName;
    };
    std::string getLogFileName() const {
        return m_logFileName;
    };

    void sendMessage( SPELLipcMessage* msg );
    SPELLipcMessage* sendRequest( SPELLipcMessage* msg, unsigned long timeoutSec );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    SPELLipcMessage* executorLoggedIn( SPELLipcMessage* msg );
    void executorLoggedOut();
    bool waitLogin();
    bool waitLogout();

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
    bool                           m_running;
    SPELLexecutorStatus            m_status;
    std::string                    m_csp;
    std::string                    m_parentProc;
    std::string                    m_condition;
    int                            m_executorKey;
    std::string                    m_executorCommand;
    SPELLipcServerInterface*       m_ipc;
    SPELLexecutorManagerConfig     m_config;
    std::string                    m_procId;
    SPELLevent                     m_execLoginEvent;
    SPELLevent                     m_execLogoutEvent;
    SPELLevent                     m_readyToStartEvent;
    SPELLcontext&                  m_ctx;
    /** True if executor is about to be closed, ignore requests */
    bool                           m_ignore;
    std::string                    m_asrunFileName;
    std::string                    m_logFileName;
};

#endif
