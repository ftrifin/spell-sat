// ################################################################################
// FILE       : SPELLipcInterface.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the common interface
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
#include "SPELL_IPC/SPELLipcIncoming.H"
#include "SPELL_SYN/SPELLsyncError.H"
#include "SPELL_IPC/SPELLipcOutput.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLipcInterface::SPELLipcInterface
//=============================================================================
SPELLipcInterface::SPELLipcInterface( std::string name )
    : SPELLthread("SPELLipcInterface"),
      m_trash(name),
      m_mailbox(name)
{
    m_name = name;
    m_listener = NULL;
    m_connected = false;
    m_messageSequence = 0;
    m_requestSequence = 0;
    DEBUG("[IPC-IFC-" + m_name + "] Base interface created");
    m_trash.start();
}

//=============================================================================
// DESTRUCTOR: SPELLipcInterface:~SPELLipcInterface
//=============================================================================
SPELLipcInterface::~SPELLipcInterface()
{
    DEBUG("[IPC-IFC-" + m_name + "] Base interface destroyed");
}

//=============================================================================
// METHOD: SPELLipcInterface:setReady
//=============================================================================
void SPELLipcInterface::setReady()
{
    while(!m_readyEvent.isClear())
    {
        usleep(100);
    }
    m_readyEvent.set();
};

//=============================================================================
// METHOD: SPELLipcInterface:waitReady
//=============================================================================
void SPELLipcInterface::waitReady()
{
    m_readyEvent.clear();
    m_readyEvent.wait();
};

//=============================================================================
// METHOD: SPELLipcInterface:disconnect
//=============================================================================
void SPELLipcInterface::disconnect( bool send_eoc )
{
    m_connected = false;
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface disconnect");
    m_mailbox.shutdown();
    m_trash.shutdown();
    m_outgoingRequests.clear();
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface disconnect done");
}

//=============================================================================
// METHOD: SPELLipcInterface:disconnect
//=============================================================================
void SPELLipcInterface::disconnect( int peerKey, bool send_eoc )
{
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface disconnect peer " + ISTR(peerKey));
    cancelOutgoingRequests(peerKey);
}

//=============================================================================
// METHOD: SPELLipcInterface:disconnectOutput
//=============================================================================
void SPELLipcInterface::disconnectOutput( int peerKey )
{
    getWriter(peerKey).disconnect(false);
}

//=============================================================================
// METHOD: SPELLipcInterface:cancelOutgoingRequests()
//=============================================================================
void SPELLipcInterface::cancelOutgoingRequests( int peerKey )
{
    std::map<std::string,int>::const_iterator it;
    std::map<std::string,int>::const_iterator end = m_outgoingRequests.end();
    for( it = m_outgoingRequests.begin(); it != end; it++)
    {
        if (it->second == peerKey)
        {
            m_mailbox.cancel(it->first);
            endRequest( it->first );
        }
    }
}

//=============================================================================
// METHOD: SPELLipcInterface:incomingRequest
//=============================================================================
void SPELLipcInterface::incomingRequest( SPELLipcMessage* msg )
{
    if (!m_connected) return;

    SPELLmonitor m(m_lock);

    assert(msg != NULL);
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface incoming request " + msg->getId());
    std::string senderId = msg->getSender();
    std::string receiverId = msg->getReceiver();
    std::string reqId = receiverId + "-" + senderId + ":" + msg->getSequenceStr();
    SPELLipcIncomingRequest* ireq = new SPELLipcIncomingRequest( reqId, msg, m_listener, this );
    ireq->start();
    m_trash.place(ireq);
}

//=============================================================================
// METHOD: SPELLipcInterface:incomingMessage
//=============================================================================
void SPELLipcInterface::incomingMessage( SPELLipcMessage* msg )
{
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface incoming message " + msg->getId());

    if (!m_connected) return;

    assert(msg != NULL);
    std::string senderId = msg->getSender();
    std::string receiverId = msg->getReceiver();
    std::string msgId = receiverId + "-" + senderId;

    std::stringstream buffer;
    buffer << m_messageSequence++;
    msgId += "-" + buffer.str();

    SPELLipcIncomingMessage* imsg = new SPELLipcIncomingMessage( msgId, msg, m_listener);
    imsg->start();
    m_trash.place(imsg);
}

//=============================================================================
// METHOD: SPELLipcInterface:incomingResponse
//=============================================================================
void SPELLipcInterface::incomingResponse( SPELLipcMessage* msg )
{
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface incoming response " + msg->getId());
    assert(msg != NULL);
    std::string senderId = msg->getSender();
    std::string receiverId = msg->getReceiver();
    std::string msgId = ISTR(msg->getKey()) + "-" + senderId + "-" + receiverId;

    if ( (msg->getSequence() == -1) && (msg->getType() == MSG_TYPE_ERROR))
    {
        incomingMessage( msg );
    }
    else
    {
        SPELLmonitor m(m_lock);
        msgId += ":" + msg->getSequenceStr();
        m_mailbox.place( msgId, msg );
    }
}

//=============================================================================
// METHOD: SPELLipcInterface::startRequest()
//=============================================================================
std::string SPELLipcInterface::startRequest( SPELLipcMessage* msg )
{
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface start request");
    SPELLmonitor m(m_lock);
    std::string senderId = msg->getSender();
    std::string receiverId = msg->getReceiver();
    std::string reqId = ISTR(msg->getKey()) + "-" + receiverId + "-" + senderId;

    long seq = m_requestSequence++;
    reqId += ":" + ISTR(seq);
    msg->setSequence(seq);
    m_mailbox.prepare(reqId);
    m_outgoingRequests.insert( std::make_pair( reqId, msg->getKey() ));
    return reqId;
}

//=============================================================================
// METHOD: SPELLipcInterface::endRequest()
//=============================================================================
void SPELLipcInterface::endRequest( std::string reqId )
{
    SPELLmonitor m(m_lock);
    std::map<std::string,int>::iterator it = m_outgoingRequests.find(reqId);
    if (it != m_outgoingRequests.end())
    {
        m_outgoingRequests.erase(it);
    }
}

//=============================================================================
// METHOD: SPELLipcInterface:waitResponse
//=============================================================================
SPELLipcMessage* SPELLipcInterface::waitResponse( std::string reqId, unsigned long timeoutSec )
{
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface wait response");
    // May raise an exception if there is a timeout
    return m_mailbox.retrieve(reqId, timeoutSec);
}

//=============================================================================
// METHOD: SPELLipcInterface:waitResponse
//=============================================================================
SPELLipcMessage* SPELLipcInterface::waitResponse( std::string reqId )
{
    DEBUG("[IPC-IFC-" + m_name + "] IPC interface wait response");
    // May raise an exception if there is a timeout
    return m_mailbox.retrieve(reqId);
}

//=============================================================================
// METHOD: SPELLipcInterface:performRequest
//=============================================================================
SPELLipcMessage* SPELLipcInterface::performRequest( SPELLipcOutput& writer, SPELLipcMessage* msg )
{
	return performRequest( writer, msg, 0 );
}

//=============================================================================
// METHOD: SPELLipcInterface:performRequest
//=============================================================================
SPELLipcMessage* SPELLipcInterface::performRequest( SPELLipcOutput& writer, SPELLipcMessage* msg, unsigned long timeoutSec )
{
    SPELLipcMessage* response = NULL;
    if (m_connected)
    {
        int retries = 0;
        while(1)
        {
            try
            {
                std::string reqId = startRequest(msg);
                DEBUG("[IPC-IFC-" + m_name + "] Sending request now to peer");
                writer.send(msg);
                DEBUG("[IPC-IFC-" + m_name + "] Waiting response from peer");
                if (timeoutSec == 0)
                {
                	// With no timeout, wait forever
                	response = waitResponse(reqId);
                }
                else
                {
                	response = waitResponse(reqId, timeoutSec);
                }
                DEBUG("[IPC-IFC-" + m_name + "] Response obtained");
                retries = 5;
                break;
            }
            catch(SPELLipcError& ex)
            {
                LOG_ERROR("[IPC-IFC-" + m_name + "] Failed to perform request due to timeout");
                std::cerr << "RESPONSE TIMED OUT, RETRYING (" << retries << "/" << IPC_REQUEST_MAX_RETRIES << ")" << std::endl;
                if (retries==IPC_REQUEST_MAX_RETRIES)
                {
                    LOG_ERROR("[IPC-IFC-" + m_name + "] Request: no more retries");
                    throw ex;
                }
                else
                {
                    usleep(IPC_REQUEST_RETRY_DELAY_USEC);
                    LOG_ERROR("[IPC-IFC-" + m_name + "] Retrying request");
                }
                retries++;
            }
        }
    }
    return response;

}
