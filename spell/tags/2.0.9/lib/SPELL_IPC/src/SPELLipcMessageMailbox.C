// ################################################################################
// FILE       : SPELLipcMessageMailbox.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the message mailbox
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
#include "SPELL_IPC/SPELLipcMessageMailbox.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLipcMessageMailbox::SPELLipcMessageMailbox
//=============================================================================
SPELLipcMessageMailbox::SPELLipcMessageMailbox( const std::string& name )
{
    m_name = name;
    m_working = true;
}

//=============================================================================
// DESTRUCTOR: SPELLipcMessageMailbox::~SPELLipcMessageMailbox
//=============================================================================
SPELLipcMessageMailbox::~SPELLipcMessageMailbox()
{
    SPELLipcMessageQueueMap::iterator mit;
    for( mit = m_queueMap.begin(); mit != m_queueMap.end(); mit++)
    {
        SPELLipcMessageQueue* queue = (*mit).second;
        delete queue;
    }
    m_queueMap.clear();
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::retrieve
//=============================================================================
SPELLipcMessage* SPELLipcMessageMailbox::retrieve( std::string id )
{
	return retrieve(id, 0);
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::retrieve
//=============================================================================
SPELLipcMessage* SPELLipcMessageMailbox::retrieve( std::string id, unsigned long timeout )
{
    DEBUG("[Mailbox-" + m_name + "] Retrieve message with id " + id)
    SPELLipcMessageQueue* queue = getQueue(id);

    SPELLipcMessage* msg = NULL;
    try
    {
    	if (timeout == 0)
    	{
    		msg = queue->pull();
    	}
    	else
    	{
    		msg = queue->pull(timeout);
    	}
        remove(id);
    }
    catch(SPELLsyncError& timeout)
    {
        LOG_ERROR("Request response timed out: " + id);
        std::cerr << "####################################################" << std::endl;
        std::cerr << "TIMEOUT!!" << id << std::endl;
        std::cerr << "####################################################" << std::endl;
        remove(id);
        delete msg;
        // Re-throw the exception
        throw SPELLipcError("Response timed out", IPC_ERROR_TIMEOUT);
    }
    DEBUG("[Mailbox-" + m_name + "] Retrieve message with id " + id + " done")
    return msg;
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::shutdown
//=============================================================================
void SPELLipcMessageMailbox::shutdown()
{
    {
        SPELLmonitor m(m_lock);
        DEBUG("[Mailbox-" + m_name + "] Shutdown")
        m_working = false;
    }
    cancelAll();
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::cancelAll()
//=============================================================================
void SPELLipcMessageMailbox::cancelAll()
{
    SPELLipcMessageQueueMap::iterator mit;
    for( mit = m_queueMap.begin(); mit != m_queueMap.end(); mit++)
    {
        SPELLipcMessage* dummyResponse = new SPELLipcMessage("dummy");
        dummyResponse->setType( MSG_TYPE_RESPONSE );
        SPELLipcMessageQueue* queue = (*mit).second;
        queue->push(dummyResponse);
    }
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::cancel()
//=============================================================================
void SPELLipcMessageMailbox::cancel( std::string id )
{
    SPELLipcMessageQueueMap::iterator mit;
    for( mit = m_queueMap.begin(); mit != m_queueMap.end(); mit++)
    {
        if (mit->first == id)
        {
            SPELLipcMessage* dummyResponse = new SPELLipcMessage("dummy");
            dummyResponse->setType( MSG_TYPE_RESPONSE );
            SPELLipcMessageQueue* queue = mit->second;
            queue->push(dummyResponse);
            break;
        }
    }
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::prepare
//=============================================================================
void SPELLipcMessageMailbox::prepare( std::string id )
{
    SPELLmonitor m(m_lock);
    DEBUG("[Mailbox-" + m_name + "] Create queue with id " + id)
    m_queueMap[id] = new SPELLipcMessageQueue(1);
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::place
//=============================================================================
void SPELLipcMessageMailbox::place( std::string id, SPELLipcMessage* msg )
{
    DEBUG("[Mailbox-" + m_name + "] Place message on queue with id " + id)
    SPELLipcMessageQueue* queue = getQueue(id);
    if(queue) queue->push(msg);
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::getQueue
//=============================================================================
SPELLipcMessageMailbox::SPELLipcMessageQueue* SPELLipcMessageMailbox::getQueue( std::string id )
{
    SPELLmonitor m(m_lock);
    SPELLipcMessageQueueMap::iterator mit = m_queueMap.find(id);
    if (mit == m_queueMap.end()) return NULL;
    return (*mit).second;
}

//=============================================================================
// METHOD: SPELLipcMessageMailbox::remove
//=============================================================================
void SPELLipcMessageMailbox::remove( std::string id )
{
    SPELLmonitor m(m_lock);
    SPELLipcMessageQueueMap::iterator mit = m_queueMap.find(id);
    assert(mit != m_queueMap.end());
    DEBUG("[Mailbox-" + m_name + "] Remove queue with id " + id)
    SPELLipcMessageQueue* queue = (*mit).second;
    m_queueMap.erase(id);
    delete queue;
}
