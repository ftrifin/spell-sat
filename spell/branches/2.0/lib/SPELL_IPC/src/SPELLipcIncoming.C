// ################################################################################
// FILE       : SPELLipcIncoming.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the incoming message handler
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
#include "SPELL_IPC/SPELLipcIncoming.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLipcIncomingBase::SPELLipcIncomingBase
//=============================================================================
SPELLipcIncomingBase::SPELLipcIncomingBase( std::string id, SPELLipcMessage* msg,
        SPELLipcInterfaceListener* listener )
    : SPELLthread(id,true),
      m_listener(listener),
      m_message(msg)
{
    //DEBUG("## SPELLipcIncomingBase created");
    m_finishEvent.clear();
    m_cancel = false;
}

//=============================================================================
// DESTRUCTOR: SPELLipcIncomingBase::~SPELLipcIncomingBase
//=============================================================================
SPELLipcIncomingBase::~SPELLipcIncomingBase()
{
    //DEBUG("## SPELLipcIncomingBase destroyed");
}


//=============================================================================
// METHOD: SPELLipcIncomingBase:wait
//=============================================================================
bool SPELLipcIncomingBase::wait( long timeout )
{
    bool timedout = false;
    if (m_finishEvent.isClear())
    {
        timedout = m_finishEvent.wait( timeout );
    }
    return timedout;
}

//=============================================================================
// METHOD: SPELLipcIncomingBase:finish
//=============================================================================
void SPELLipcIncomingBase::finish()
{
    m_listener = NULL;
    m_finishEvent.set();
}

//=============================================================================
// CONSTRUCTOR: SPELLipcIncomingMessage::SPELLipcIncomingMessage
//=============================================================================
SPELLipcIncomingMessage::SPELLipcIncomingMessage( std::string msgId, SPELLipcMessage* msg,
        SPELLipcInterfaceListener* listener)
    : SPELLipcIncomingBase(msgId,msg,listener)
{
    //DEBUG("## SPELLipcIncomingMessage " + getThreadId() + " created");
    assert( m_listener != NULL );
}

//=============================================================================
// DESTRUCTOR: SPELLipcIncomingMessage:~SPELLipcIncomingMessage
//=============================================================================
SPELLipcIncomingMessage::~SPELLipcIncomingMessage()
{
    //DEBUG("SPELLipcIncomingMessage " + getThreadId() + " destroyed");
}

//=============================================================================
// METHOD: SPELLipcIncomingMessage:run
//=============================================================================
void SPELLipcIncomingMessage::run()
{
    if (!m_cancel)
    {
        m_listener->processMessage(m_message);
    }
    finish();
    delete m_message;
}

//=============================================================================
// METHOD: SPELLipcIncomingMessage::cancel
//=============================================================================
void SPELLipcIncomingMessage::cancel()
{
    m_cancel = true;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcIncomingRequest::SPELLipcIncomingRequest
//=============================================================================
SPELLipcIncomingRequest::SPELLipcIncomingRequest( std::string requestId, SPELLipcMessage* msg,
        SPELLipcInterfaceListener* listener, SPELLipcInterface* ifc)

    : SPELLipcIncomingBase(requestId,msg,listener),
      m_interface(ifc)

{
    assert( m_listener != NULL );
}

//=============================================================================
// DESTRUCTOR: SPELLipcIncomingRequest:~SPELLipcIncomingRequest
//=============================================================================
SPELLipcIncomingRequest::~SPELLipcIncomingRequest()
{
    //DEBUG("Incoming request " << getThreadId() << " destroyed");
}

//=============================================================================
// METHOD: SPELLipcIncomingRequest:run
//=============================================================================
void SPELLipcIncomingRequest::run()
{
    std::string senderId = m_message->getSender();
    std::string receiverId = m_message->getReceiver();
    // If the request has been canceled beforehand
    if (m_cancel) return;
    SPELLipcMessage* response = m_listener->processRequest(m_message);
    if (response && !m_cancel)
    {
        response->setSender(receiverId);
        response->setReceiver(senderId);
        // Do not set sequence number if it is already set.
        // This happens when forwarding requests.
        if (response->getSequence() == -1)
        {
            response->setSequence( m_message->getSequence() );
        }
        try
        {
            if (m_interface->isConnected())
            {
                SPELLipcOutput& writer = m_interface->getWriter(m_message->getKey());
                writer.send(response);
                m_interface->endRequest( getThreadId() );
            }
        }
        catch(...) {};
        delete response;
    }
    finish();
    delete m_message;
}

//=============================================================================
// METHOD: SPELLipcIncomingRequest::cancel
//=============================================================================
void SPELLipcIncomingRequest::cancel()
{
    m_cancel = true;
    m_interface = NULL;
}
