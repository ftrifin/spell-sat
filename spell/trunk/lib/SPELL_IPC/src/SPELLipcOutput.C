// ################################################################################
// FILE       : SPELLipcOutput.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data writer
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
#include "SPELL_IPC/SPELLipcOutput.H"
#include "SPELL_IPC/SPELLipcError.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////

#define NAME std::string("[ IPC-OUT-") + ISTR(m_key) + "-" + m_name + " ] "
#define SIZE sizeof(unsigned int)

//=============================================================================
// FUNCTION: encodeLength
// DESCRIPTION: encode the message length into bytes
//=============================================================================
char* encodeLength( int length )
{
    char* bytes = new char[4];
    for( int i=3; i>=0; i--)
    {
        bytes[i] = (char) (length & 0xff);
        length >>= 8;
    }
    return bytes;
};


//=============================================================================
// CONSTRUCTOR: SPELLipcOutput::SPELLipcOutput
//=============================================================================
SPELLipcOutput::SPELLipcOutput( std::string name, SPELLsocket* skt, int myKey, SPELLipcInterface* ifc )
    : m_socket(skt),
      m_interface(ifc)
{
    m_name = name;
    m_key = myKey;
    m_connected = true;
    DEBUG( NAME + "SPELLipcOutput created");
}

//=============================================================================
// DESTRUCTOR: SPELLipcOutput:SPELLipcOutput
//=============================================================================
SPELLipcOutput::~SPELLipcOutput()
{
}

//=============================================================================
// METHOD: SPELLipcOutput::send
//=============================================================================
void SPELLipcOutput::send( SPELLipcMessage* msg )
{
    //DEBUG( NAME << "SPELLipcOutput send message");
    assert( msg != NULL );
    msg->setKey( m_key );
    try
    {
        writeData( msg->data() );
    }
    catch(SPELLipcError& err)
    {
        if (m_connected)
        {
            LOG_ERROR( NAME + " Connection lost at output " + std::string(err.what()) );
            LOG_ERROR( NAME + " Data being sent: " + msg->data());
            m_interface->connectionLost(m_key);
        }
    }
    catch(...)
    {
        if (m_connected)
        {
            LOG_ERROR( NAME + " Connection lost at output, unknown error" );
            LOG_ERROR( NAME + " Data being sent: " + msg->data());
            m_interface->connectionLost(m_key);
        }
    }
}

//=============================================================================
// METHOD: SPELLipcOutput::disconnect
//=============================================================================
void SPELLipcOutput::disconnect( bool send_eoc )
{
    if (!m_connected) return;

    DEBUG( NAME + "SPELLipcOutput disconnect");

    if (send_eoc)
    {
        DEBUG( NAME + "Sending EOC");
        SPELLipcMessage* eoc = new SPELLipcMessage("EOC");
        eoc->setType( MSG_TYPE_EOC );
        eoc->setKey(m_key);
        send(eoc);
        DEBUG( NAME + "EOC sent");
    }

    m_connected = false;
}

//=============================================================================
// METHOD: SPELLipcOutput::writeData
//=============================================================================
void SPELLipcOutput::writeData( std::string data )
{
    SPELLmonitor m(m_lock);
    if (m_connected)
    {
        unsigned int sizeData = data.size();
        //DEBUG( NAME << "Write data: " + data << " (length " << sizeData << ")");
        char* msg = const_cast<char*>(data.c_str());

        int success = 0;

        // Take into account endianess
        char bytes[SIZE];
        unsigned int lc = sizeData;
        for( int i=SIZE-1; i>=0; i-- )
        {
            bytes[i] = (char)(lc & 0xFF);
            lc >>= 8;
        }

        success = m_socket->send(bytes,SIZE);
        if (success == -1)
        {
            throw SPELLipcError("Cannot send length", -1);
        }

        int numBytes;
        char* pointer = msg;
        numBytes = sizeData;
        success = 0;
        while( true )
        {
            success = m_socket->send(pointer,numBytes);
            if (success == -1 || errno != 0)
            {
                throw SPELLipcError("Error while sending data", errno);
            }
            else if (success != numBytes)
            {
                // If there are remaining bytes, move the pointer to continue;
                numBytes = numBytes - success;
                pointer += numBytes;
            }
            else
            {
                break;
            }
        }
    }
}
