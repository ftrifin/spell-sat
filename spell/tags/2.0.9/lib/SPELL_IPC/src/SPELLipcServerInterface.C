// ################################################################################
// FILE       : SPELLipcServerInterface.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the interface for servers
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
#include "SPELL_IPC/SPELLipcServerInterface.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLipcServerInterface::SPELLipcServerInterface
//=============================================================================
SPELLipcServerInterface::SPELLipcServerInterface( std::string name, int key, int port )
    : SPELLipcInterface(name)
{
    m_lastClientKey = 0;
    m_serverKey = key;
    m_serverPort = port;

    m_clients.clear();

    m_socket = NULL;

    DEBUG("[IPC-SRV-" + m_name + "] Server interface created");
}

//=============================================================================
// DESTRUCTOR: SPELLipcServerInterface:~SPELLipcServerInterface
//=============================================================================
SPELLipcServerInterface::~SPELLipcServerInterface()
{
    SPELLipcClientMap::iterator cit;
    for( cit = m_clients.begin(); cit != m_clients.end(); cit++ )
    {
    	delete cit->second;
    }
    m_clients.clear();
    DEBUG("[IPC-SRV-" + m_name + "] Server interface destroyed");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:connect
//=============================================================================
void SPELLipcServerInterface::connectIfc()
{
    DEBUG("[IPC-SRV-" + m_name + "] Connecting server interface");

    m_socket = SPELLsocket::listen( &m_serverPort );

    m_connected = true;

    DEBUG("[IPC-SRV-" + m_name + "] Server interface ready");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:run
//=============================================================================
void SPELLipcServerInterface::run()
{
    DEBUG("[IPC-SRV-" + m_name + "] Server interface start");

    DEBUG("  - Entering select loop");
    while(isConnected())
    {
        bool disconnected = false;
        SPELLsocket* clientSocket = m_socket->acceptClient( &disconnected );

        if ((!disconnected)&&clientSocket != NULL)
        {
            DEBUG("  - Accepted new client connection");
            addClient(m_lastClientKey, clientSocket);
            m_lastClientKey++;
            DEBUG("  - Waiting interface to be ready");
            waitReady();
            DEBUG("  - New connection ready");
        }

        if (disconnected) return;
    }
    DEBUG("[IPC-SRV-" + m_name + "] Server interface stop");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:hasClient
//=============================================================================
bool SPELLipcServerInterface::hasClient( int key )
{
    SPELLmonitor m(m_lock);
    return ( m_clients.find(key) != m_clients.end() );
}

//=============================================================================
// METHOD: SPELLipcServerInterface:addClient
//=============================================================================
void SPELLipcServerInterface::addClient( int key, SPELLsocket* skt )
{
    DEBUG("  - Assigned key " + ISTR(key) );

    SPELLmonitor m(m_lock);

    // Send the key to the client
    writeKey(key,skt);

    SPELLipcClientInfo* info = new SPELLipcClientInfo( m_name, skt, key, this );

    m_clients.insert( std::make_pair( key, info ));
}

//=============================================================================
// METHOD: SPELLipcServerInterface:removeClient
//=============================================================================
void SPELLipcServerInterface::removeClient( int key, bool send_eoc )
{
    if (!hasClient(key))
    {
        LOG_ERROR("[IPC-SRV-" + m_name + "] Remove client: no such client key: " + ISTR(key));
        return;
    }

    SPELLmonitor m(m_lock);

    SPELLipcClientMap::iterator cit;
    cit = m_clients.find(key);
    if (cit == m_clients.end() )
    {
        throw SPELLipcError("No such key", key);
    }
    cit->second->disconnectClient(send_eoc);
}

//=============================================================================
// METHOD: SPELLipcServerInterface:disconnect
//=============================================================================
void SPELLipcServerInterface::disconnect( bool send_eoc )
{
    if (!isConnected()) return;
    SPELLipcInterface::disconnect(send_eoc);

    DEBUG("[IPC-SRV-" + m_name + "] Server interface disconnect");
    // Close the listener connection first
    m_socket->shutdown();
    // In this case, disconnect from everybody
    SPELLipcClientMap::iterator cit;
    for( cit = m_clients.begin(); cit != m_clients.end(); cit++)
    {
        int clientKey = cit->first;
        DEBUG("[IPC-SRV-" + m_name + "] Removing client " + ISTR(clientKey));
        removeClient( clientKey, send_eoc );
        DEBUG("[IPC-SRV-" + m_name + "] Removing client " + ISTR(clientKey) + " done" );
    }
    m_clients.clear();
    DEBUG("[IPC-SRV-" + m_name + "] Server interface disconnect done");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:disconnect
//=============================================================================
void SPELLipcServerInterface::disconnect( int peerKey, bool send_eoc )
{
    if (!isConnected()) return;
    SPELLipcInterface::disconnect(peerKey,send_eoc);
    DEBUG("[IPC-SRV-" + m_name + "] Server interface disconnect client ");
    removeClient( peerKey, send_eoc );
    DEBUG("[IPC-SRV-" + m_name + "] Server interface client disconnection done");
}

//=============================================================================
// METHOD: SPELLipcServerInterface:connectionClosed
//=============================================================================
void SPELLipcServerInterface::connectionClosed( int peerKey )
{
    DEBUG("[IPC-SRV-" + m_name + "] Connection closed by peer ");
    // The connection has been closed by the peer.
    // Close its socket and remove the interface
    SPELLipcClientMap::iterator cit = m_clients.find(peerKey);
    if (cit != m_clients.end())
    {
        cit->second->disconnectClient(false);
        m_clients.erase(cit);
    }
}

//=============================================================================
// METHOD: SPELLipcServerInterface:connectionClosed
//=============================================================================
void SPELLipcServerInterface::connectionLost( int peerKey )
{
    DEBUG("[IPC-SRV-" + m_name + "] Connection lost with peer ");
    connectionClosed(peerKey);
    if (m_listener != NULL)
    {
        m_listener->processError("Connection lost by peer", "Connection lost");
    }
}

//=============================================================================
// METHOD: SPELLipcServerInterface:sendMessage
//=============================================================================
void SPELLipcServerInterface::sendMessage( int peerKey, SPELLipcMessage* msg )
{
    DEBUG("[IPC-SRV-" + m_name + "] Send message to peer ");
    SPELLipcOutput& writer = getWriter(peerKey);
    writer.send(msg);
}

//=============================================================================
// METHOD: SPELLipcServerInterface:sendRequest
//=============================================================================
SPELLipcMessage* SPELLipcServerInterface::sendRequest( int peerKey, SPELLipcMessage* msg, unsigned long timeoutSec )
{
    DEBUG("[IPC-SRV-" + m_name + "] Send request to peer " + ISTR(peerKey) + " " + msg->data());
    msg->setKey(peerKey);
    DEBUG("[IPC-SRV-" + m_name + "] Get writer for peer " + ISTR(peerKey));
    SPELLipcOutput& writer = getWriter(peerKey);
    DEBUG("[IPC-SRV-" + m_name + "] Sending now request to peer " + ISTR(peerKey));
    return performRequest( writer, msg, timeoutSec );
}

//=============================================================================
// METHOD: SPELLipcServerInterface:forwardRequest
//=============================================================================
SPELLipcMessage* SPELLipcServerInterface::forwardRequest( int peerKey, SPELLipcMessage* msg, unsigned long timeoutSec )
{
    DEBUG("[IPC-SRV-" + m_name + "] Forward request to peer ");
    long originalSeq = msg->getSequence();

    SPELLipcMessage* response = sendRequest(peerKey, msg, timeoutSec );

    DEBUG("[IPC-SRV-" + m_name + "] Got response (fwd) from peer " + ISTR(peerKey));
    response->setSequence(originalSeq);

    return response;
}

//=============================================================================
// METHOD: SPELLipcServerInterface:getWriter
//=============================================================================
SPELLipcOutput& SPELLipcServerInterface::getWriter( int peerKey )
{
    SPELLmonitor m(m_lock);
    SPELLipcClientMap::iterator cit = m_clients.find(peerKey);
    if (cit != m_clients.end())
    {
        return cit->second->getWriter();
    }
    throw SPELLipcError("Cannot get writer, no such key: " + ISTR(peerKey),0);
}

//=============================================================================
// METHOD: SPELLipcServerInterface:getReader
//=============================================================================
SPELLipcInput& SPELLipcServerInterface::getReader( int peerKey )
{
    SPELLmonitor m(m_lock);
    SPELLipcClientMap::iterator cit = m_clients.find(peerKey);
    if (cit != m_clients.end())
    {
        return cit->second->getReader();
    }
    throw SPELLipcError("Cannot get reader, no such key: " + ISTR(peerKey),0);
}

//=============================================================================
// METHOD: SPELLipcServerInterface::writeKey
//=============================================================================
void SPELLipcServerInterface::writeKey( int key, SPELLsocket* skt )
{
    unsigned char bytes[1];
    bytes[0] = (unsigned char)((key >> 8) & 0xFF);
    int sent = skt->send( bytes, 1 );
    if (sent != 1)
    {
        throw SPELLipcError("Could not send first key byte", -1);
    }
    bytes[0] = (unsigned char)(key & 0xFF);
    sent = skt->send( bytes, 1 );
    if (sent != 1)
    {
        throw SPELLipcError("Could not send second key byte", -1);
    }
    DEBUG("[IPC-CLI] Key sent: " + ISTR(key));
}
