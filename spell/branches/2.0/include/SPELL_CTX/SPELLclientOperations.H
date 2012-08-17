// ################################################################################
// FILE       : SPELLclientOperations.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Operations and coordination of clients
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

#ifndef __SPELL_CLIENT_OPERATIONS_H__
#define __SPELL_CLIENT_OPERATIONS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"
#include "SPELL_IPC/SPELLipcServerInterface.H"
#include "SPELL_IPC/SPELLipcDataChunk.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLclientRegistry.H"
#include "SPELL_CTX/SPELLclientInfo.H"
#include "SPELL_CTX/SPELLexecutorOperations.H"


/** \addtogroup SPELL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////



//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Operations and coordination of clients
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLclientOperations : public SPELLipcInterfaceListener, public SPELLclientRegistry
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor.
    **////////////////////////////////////////////////////////////////////
    SPELLclientOperations( SPELLcontext& ctx );

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    ~SPELLclientOperations();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // Gives access to the IPC listener methods
    friend class SPELLipcServerInterface;

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    std::string linesToString( std::list<std::string> lines );

    //========================================================================
    // Messages coming from clients
    //========================================================================

    void processMessage( SPELLipcMessage* msg );
    SPELLipcMessage* processRequest( SPELLipcMessage* msg );
    void processError( std::string error, std::string reason );

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    SPELLipcMessage* request_ExecutorList( SPELLipcMessage* msg );
    SPELLipcMessage* request_OpenExecutor( SPELLipcMessage* msg );
    SPELLipcMessage* request_CloseExecutor( SPELLipcMessage* msg );
    SPELLipcMessage* request_KillExecutor( SPELLipcMessage* msg );
    SPELLipcMessage* request_AttachExecutor( SPELLipcMessage* msg );
    SPELLipcMessage* request_DetachExecutor( SPELLipcMessage* msg );
    SPELLipcMessage* request_ExecutorInfo( SPELLipcMessage* msg );
    SPELLipcMessage* request_ClientInfo( SPELLipcMessage* msg );
    SPELLipcMessage* request_ProcedureList( SPELLipcMessage* msg );
    SPELLipcMessage* request_ProcedureProperties( SPELLipcMessage* msg );
    SPELLipcMessage* request_ProcedureCode( SPELLipcMessage* msg );
    SPELLipcMessage* request_ServerFile( SPELLipcMessage* msg );
    SPELLipcMessage* request_InstanceId( SPELLipcMessage* msg );

    void processExecutorInfo( const SPELLexecutorInformation& info, SPELLipcMessage* response );

    // DATA MEMBERS //////////////////////////////////////////////////////////
    /** Reference to context */
    SPELLcontext&                 m_context;
    SPELLipcDataChunk			  m_dataChunker;
};

#endif
