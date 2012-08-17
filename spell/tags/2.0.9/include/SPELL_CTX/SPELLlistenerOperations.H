// ################################################################################
// FILE       : SPELLlistenerOperations.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Operations requested from listener process
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

#ifndef __SPELL_LISTENER_OPERATIONS_H__
#define __SPELL_LISTENER_OPERATIONS_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------


/** \addtogroup SPELL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////



//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Operations for listener
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLlistenerOperations : public SPELLipcInterfaceListener
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor.
    **////////////////////////////////////////////////////////////////////
    SPELLlistenerOperations( SPELLcontext& ctx );

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    ~SPELLlistenerOperations();

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

    SPELLipcMessage* request_CanClose( SPELLipcMessage* msg );

    // DATA MEMBERS //////////////////////////////////////////////////////////
    /** Reference to context */
    SPELLcontext&                 m_context;
};

#endif
