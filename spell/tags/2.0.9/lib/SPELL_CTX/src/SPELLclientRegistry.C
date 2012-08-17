// ################################################################################
// FILE       : SPELLclientRegistry.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Client registry implementation
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
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_SYN/SPELLmonitor.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLclientRegistry.H"


//=============================================================================
// CONSTRUCTOR : SPELLclientRegistry::SPELLclientRegistry
//=============================================================================
SPELLclientRegistry::SPELLclientRegistry()
{
}

//=============================================================================
// DESTRUCTOR: SPELLclientRegistry::~SPELLclientRegistry
//=============================================================================
SPELLclientRegistry::~SPELLclientRegistry()
{
    m_clients.clear();
}

//=============================================================================
// METHOD :    SPELLclientRegistry::getNumExecutors
//=============================================================================
int SPELLclientRegistry::getNumClients()
{
    SPELLmonitor m(m_lock);
    return m_clients.size();
}

//=============================================================================
// METHOD :    SPELLclientRegistry::createClient
//=============================================================================
void SPELLclientRegistry::createClient( SPELLclientInfo* info )
{
    SPELLmonitor m(m_lock);
    DEBUG("[CLT-REG] Creating client " + ISTR(info->getKey()) + " on host " + info->getHost())
    m_clients.insert( std::make_pair( info->getKey(), info ));
}

//=============================================================================
// METHOD :    SPELLclientRegistry::getClient
//=============================================================================
SPELLclientInfo& SPELLclientRegistry::getClient( int clientKey )
{
    SPELLmonitor m(m_lock);
    SPELLclientInfoMap::iterator it = m_clients.find(clientKey);
    if (it != m_clients.end())
    {
        return *((*it).second);
    }
    else
    {
        throw SPELLcoreException("Cannot get client " + ISTR(clientKey), "No such client key in registry");
    }
}

//=============================================================================
// METHOD :    SPELLclientRegistry::removeClient
//=============================================================================
void SPELLclientRegistry::removeClient( int clientKey )
{
    SPELLmonitor m(m_lock);
    SPELLclientInfoMap::iterator it = m_clients.find(clientKey);
    if (it != m_clients.end())
    {
        delete (*it).second;
        m_clients.erase(it);
    }
    else
    {
        throw SPELLcoreException("Cannot remove client " + ISTR(clientKey), "No such client key in registry");
    }
}

//=============================================================================
// METHOD :    SPELLclientRegistry::addExecutor
//=============================================================================
void SPELLclientRegistry::addExecutor( int clientKey, std::string procId, SPELLclientMode mode )
{
    SPELLmonitor m(m_lock);
    SPELLclientInfoMap::iterator it = m_clients.find(clientKey);
    if (it != m_clients.end())
    {
        (*it).second->addExecutor( procId, mode );
    }
    else
    {
        throw SPELLcoreException("Cannot attach executor to client " + ISTR(clientKey), "No such client key in registry");
    }
}

//=============================================================================
// METHOD :    SPELLclientRegistry::removeExecutor
//=============================================================================
void SPELLclientRegistry::removeExecutor( int clientKey, std::string procId )
{
    SPELLmonitor m(m_lock);
    SPELLclientInfoMap::iterator it = m_clients.find(clientKey);
    if (it != m_clients.end())
    {
        (*it).second->removeExecutor(procId);
    }
    else
    {
        throw SPELLcoreException("Cannot detach executor from client " + ISTR(clientKey), "No such client key in registry");
    }
}

//=============================================================================
// METHOD :    SPELLclientRegistry::getExecutorsForClient
//=============================================================================
SPELLexecutorList SPELLclientRegistry::getExecutorsForClient( int clientKey )
{
    SPELLmonitor m(m_lock);
    DEBUG("[CLT-REG] Get executors for client " + ISTR(clientKey))
    SPELLclientInfoMap::iterator it = m_clients.find(clientKey);
    SPELLexecutorList list;
    if (it != m_clients.end())
    {
        list = (*it).second->getExecutors();
    }
    else
    {
        throw SPELLcoreException("Cannot get executors from client " + ISTR(clientKey), "No such client key in registry");
    }
    return list;
}

//=============================================================================
// METHOD :    SPELLclientRegistry::getClientsForExecutor
//=============================================================================
SPELLclientKeyList SPELLclientRegistry::getClientsForExecutor( const std::string& procId, const SPELLwhichClients& who )
{
    SPELLmonitor m(m_lock);
    DEBUG("[CLT-REG] Get clients for executor '" + procId + "'")
    SPELLclientInfoMap::iterator it;
    SPELLclientKeyList list;
    for( it = m_clients.begin(); it != m_clients.end(); it++)
    {
        SPELLexecutorList executors = it->second->getExecutors();
        SPELLexecutorList::iterator eit;
        SPELLexecutorList::iterator eend = executors.end();
        for( eit = executors.begin(); eit != eend; eit++ )
        {
            if ((*eit)== procId)
            {
                SPELLclientMode mode = it->second->getMode(procId);
                if (who == WHICH_ALL)
                {
                    list.push_back( it->first );
                }
                else if (who == WHICH_CONTROLLING && mode == CLT_MODE_CONTROL )
                {
                    list.push_back( it->first );
                }
                else if (who == WHICH_MONITORING && mode == CLT_MODE_MONITOR )
                {
                    list.push_back( it->first );
                }
            }
        }
    }
    return list;
}

//=============================================================================
// METHOD :    SPELLclientRegistry::getAllClients
//=============================================================================
SPELLclientKeyList SPELLclientRegistry::getAllClients()
{
    SPELLmonitor m(m_lock);
    DEBUG("[CLT-REG] Get all clients")
    SPELLclientInfoMap::iterator it;
    SPELLclientKeyList list;
    for( it = m_clients.begin(); it != m_clients.end(); it++)
    {
        list.push_back( (*it).first );
    }
    return list;
}
