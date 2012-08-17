// ################################################################################
// FILE       : SPELLipcClientInfo.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the client information model
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
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcClientInfo.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLipcClientInfo::SPELLipcClientInfo
//=============================================================================
SPELLipcClientInfo::SPELLipcClientInfo( const std::string& name, SPELLsocket* socket, int key, SPELLipcInterface* ifc )
    : m_input( name, socket, key, ifc ),
      m_output( name, socket, key, ifc )
{
    DEBUG("[SCI] Client info for client key " + ISTR(key) + " created");
    m_socket = socket;
    m_input.start();
};

//=============================================================================
// DESTRUCTOR: SPELLipcClientInfo::~SPELLipcClientInfo
//=============================================================================
SPELLipcClientInfo::~SPELLipcClientInfo()
{

}

//=============================================================================
// METHOD: SPELLipcClientInfo::disconnectClient
//=============================================================================
void SPELLipcClientInfo::disconnectClient( bool send_eoc )
{
    DEBUG("[SCI] Client info disconnect client");
    m_output.disconnect(send_eoc);
    if (send_eoc)
    {
        m_socket->shutdown();
    }
    else
    {
        m_socket->close();
    }
    if (m_input.isConnected())
    {
        m_input.disconnect();
        m_input.wait();
    }
    DEBUG("[SCI] Client info disconnect client finished");
}
