// ################################################################################
// FILE       : SPELLipcClientInterface.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the interface for clients
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
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcClientInterface.H"
#include "SPELL_IPC/SPELLipcIncoming.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLipcClientInterface::SPELLipcClientInterface
//=============================================================================
SPELLipcClientInterface::SPELLipcClientInterface( std::string name, std::string host, int port )
    : SPELLipcInterface(name)
{
    m_clientKey = 0;
    m_serverHost = host;
    m_serverPort = port;

    m_writer = NULL;
    m_reader = NULL;

    m_socket = NULL;

    DEBUG("[IPC-CLI-" + m_name + "] Client interface created");
}

//=============================================================================
// DESTRUCTOR: SPELLipcClientInterface:~SPELLipcClientInterface
//=============================================================================
SPELLipcClientInterface::~SPELLipcClientInterface()
{
    if (m_writer != NULL)
    {
        delete m_writer;
    }
    if (m_reader != NULL)
    {
        delete m_reader;
    }
    if (m_socket)
    {
        delete m_socket;
    }
    DEBUG("[IPC-CLI-" + m_name + "] Client interface destroyed");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:connectIfc
//=============================================================================
void SPELLipcClientInterface::connectIfc()
{
    DEBUG("[IPC-CLI-" + m_name + "] Connecting client interface");

    m_socket = SPELLsocket::connect( m_serverHost, m_serverPort );

    readMyKey();

    m_connected = true;

    m_writer = new SPELLipcOutput(m_name, m_socket, m_clientKey, this);
    m_reader = new SPELLipcInput(m_name, m_socket, m_clientKey, this);
    m_reader->start();
    waitReady();
    DEBUG("[[IPC-CLI-" + m_name + "] Ready");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:disconnect
//=============================================================================
void SPELLipcClientInterface::disconnect( bool send_eoc )
{
    if (!isConnected()) return;
    SPELLipcInterface::disconnect(send_eoc);
    DEBUG("[IPC-CLI-" + m_name + "] Client interface disconnect (eoc: " + BSTR(send_eoc) + ")");
    // Shutdown interfaces
    m_writer->disconnect(send_eoc);
    m_socket->shutdownRead();
    m_reader->disconnect();
    try
    {
        m_reader->wait();
    }
    catch(...) {};
    m_socket->shutdownWrite();
    DEBUG("[IPC-CLI-" + m_name + "] Client interface disconnected");
}

//=============================================================================
// METHOD: SPELLipcClientInterface:disconnect
//=============================================================================
void SPELLipcClientInterface::disconnect( int peerKey, bool send_eoc )
{
    SPELLipcInterface::disconnect(peerKey,send_eoc);
    disconnect(send_eoc);
}

//=============================================================================
// METHOD: SPELLipcClientInterface:connectionClosed
//=============================================================================
void SPELLipcClientInterface::connectionClosed( int peerKey )
{
    DEBUG("[IPC-CLI-" + m_name + "] Client interface connection closed");
    disconnect(false);
}

//=============================================================================
// METHOD: SPELLipcClientInterface:connectionLost
//=============================================================================
void SPELLipcClientInterface::connectionLost( int peerKey )
{
    DEBUG("[IPC-CLI-" + m_name + "] Client interface connection lost");
    disconnect(false);
    if (m_listener != NULL)
    {
        m_listener->processError("Connection lost", "Connection lost");
    }
}

//=============================================================================
// METHOD: SPELLipcClientInterface:sendMessage
//=============================================================================
void SPELLipcClientInterface::sendMessage( SPELLipcMessage* msg )
{
    SPELLmonitor m(m_lock);
    DEBUG("[IPC-CLI] Client interface send message");
    m_writer->send(msg);
}

//=============================================================================
// METHOD: SPELLipcClientInterface::sendRequest
//=============================================================================
SPELLipcMessage* SPELLipcClientInterface::sendRequest( SPELLipcMessage* msg, unsigned long timeoutSec )
{
    DEBUG("[IPC-CLI-" + m_name + "] Send request to server");
    msg->setKey(m_clientKey);
    return performRequest( *m_writer, msg, timeoutSec );
}

//=============================================================================
// METHOD: SPELLipcClientInterface::getWriter
//=============================================================================
SPELLipcOutput& SPELLipcClientInterface::getWriter( int peerKey )
{
    return *m_writer;
}

//=============================================================================
// METHOD: SPELLipcClientInterface::getReader
//=============================================================================
SPELLipcInput& SPELLipcClientInterface::getReader( int peerKey )
{
    return *m_reader;
}

//=============================================================================
// METHOD: SPELLipcClientInterface::readMyKey
//=============================================================================
void SPELLipcClientInterface::readMyKey()
{
    unsigned char byte1[1];
    unsigned char byte2[1];
    int read = m_socket->receiveAll(byte1,1);
    if (read != 1)
    {
        throw SPELLipcError("Could not read first key byte", -1);
    }
    read = m_socket->receiveAll(byte2,1);
    if (read != 1)
    {
        throw SPELLipcError("Could not read second key byte", -1);
    }
    m_clientKey = -1;
    m_clientKey = (byte1[0] << 8) | (byte2[0]);
    DEBUG("[IPC-CLI-" + m_name + "] Read key: " + ISTR(m_clientKey));
}
