// ################################################################################
// FILE       : SPELLprocessManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of process manager
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
// Local includes ----------------------------------------------------------
#include "SPELL_PRC/SPELLprocessManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"


// GLOBALS /////////////////////////////////////////////////////////////////


SPELLprocessManager* s_instance = NULL;


//=============================================================================
// CONSTRUCTOR: SPELLprocessManager::SPELLprocessManager
//=============================================================================
SPELLprocessManager::SPELLprocessManager()
    : m_lock()
{

}

//=============================================================================
// DESTRUCTOR : SPELLprocessManager::~SPELLprocessManager
//=============================================================================
SPELLprocessManager::~SPELLprocessManager()
{
    m_processes.clear();
    m_listeners.clear();
}

//=============================================================================
// METHOD     : SPELLprocessManager::instance
//=============================================================================
SPELLprocessManager& SPELLprocessManager::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLprocessManager();
    }
    return (*s_instance);
}

//=============================================================================
// METHOD     : SPELLprocessManager::startProcess()
//=============================================================================
void SPELLprocessManager::startProcess( const std::string& identifier, const std::string& command )
{
    SPELLmonitor m(m_lock);
    DEBUG("[PRCMGR] Starting process " + identifier);
    SPELLprocess* process = new SPELLprocess(identifier,command,this);
    process->start();
    if (m_processes.find(identifier) != m_processes.end() )
    {
        throw SPELLcoreException("Cannot launch process " + identifier, "It already exists");
    }
    m_processes.insert( std::make_pair( identifier, process ));
    DEBUG("[PRCMGR] Starting process " + identifier + " done");
}

//=============================================================================
// METHOD     : SPELLprocessManager::killProcess()
//=============================================================================
void SPELLprocessManager::killProcess( const std::string& identifier )
{
    SPELLmonitor m(m_lock);
    DEBUG("[PRCMGR] Killing process " + identifier);
    SPELLprocessMap::iterator it = m_processes.find(identifier);
    if (it == m_processes.end() )
    {
        throw SPELLcoreException("Cannot kill process " + identifier, "Not found");
    }
    (*it).second->kill();
    DEBUG("[PRCMGR] Kill process " + identifier + " done");
}

//=============================================================================
// METHOD     : SPELLprocessManager::waitProcess()
//=============================================================================
int SPELLprocessManager::waitProcess( const std::string& identifier )
{
    SPELLprocessMap::iterator it = m_processes.find(identifier);
    if (it == m_processes.end() )
    {
        throw SPELLcoreException("Cannot wait process " + identifier, "Not found");
    }
    return (*it).second->wait();
}

//=============================================================================
// METHOD     : SPELLprocessManager::clearProcess()
//=============================================================================
void SPELLprocessManager::clearProcess( const std::string& identifier )
{
    SPELLprocessMap::iterator pit = m_processes.find(identifier);
    if (pit != m_processes.end())
    {
        delete (*pit).second;
        m_processes.erase(pit);
    }
}

//=============================================================================
// METHOD     : SPELLprocessManager::addListener()
//=============================================================================
void SPELLprocessManager::addListener( const std::string& identifier, SPELLprocessListener* listener )
{
    SPELLmonitor m(m_lock);
    SPELLprocessListenersMap::iterator mit = m_listeners.find(identifier);
    if (mit == m_listeners.end())
    {
        SPELLprocessListeners list;
        list.push_back(listener);
        m_listeners.insert( std::make_pair( identifier, list ));
    }
    else
    {
        SPELLprocessListeners& list = (*mit).second;
        list.push_back(listener);
    }
}

//=============================================================================
// METHOD     : SPELLprocessManager::removeListener()
//=============================================================================
void SPELLprocessManager::removeListener( const std::string& identifier, SPELLprocessListener* listener )
{
    SPELLmonitor m(m_lock);
    SPELLprocessListenersMap::iterator mit = m_listeners.find(identifier);
    if (mit != m_listeners.end())
    {
        SPELLprocessListeners& list = (*mit).second;
        list.remove(listener);
    }
}

//=============================================================================
// METHOD     : SPELLprocessManager::fireProcessStarted()
//=============================================================================
void SPELLprocessManager::fireProcessStarted( const std::string& identifier )
{
    SPELLmonitor m(m_lock);
    DEBUG("[PRCMGR] Firing process started: " + identifier);
    SPELLprocessListenersMap::iterator mit = m_listeners.find(identifier);
    if (mit != m_listeners.end())
    {
        SPELLprocessListeners& list = (*mit).second;
        SPELLprocessListeners::iterator it;
        for( it = list.begin(); it != list.end(); it++)
        {
            if (*it != NULL)
            {
                (*it)->processStarted(identifier);
            }
        }
    }
}

//=============================================================================
// METHOD     : SPELLprocessManager::fireProcessFinished()
//=============================================================================
void SPELLprocessManager::fireProcessFinished( const std::string& identifier, int retValue )
{
    SPELLmonitor m(m_lock);
    DEBUG("[PRCMGR] Firing process finished: " + identifier);
    SPELLprocessListenersMap::iterator mit = m_listeners.find(identifier);
    if (mit != m_listeners.end())
    {
        SPELLprocessListeners& list = (*mit).second;
        SPELLprocessListeners::iterator it;
        for( it = list.begin(); it != list.end(); it++)
        {
            if (*it != NULL)
            {
                (*it)->processFinished(identifier, retValue);
            }
        }
    }
}

//=============================================================================
// METHOD     : SPELLprocessManager::fireProcessKilled()
//=============================================================================
void SPELLprocessManager::fireProcessKilled( const std::string& identifier )
{
    SPELLmonitor m(m_lock);
    DEBUG("[PRCMGR] Firing process killed: " + identifier);
    SPELLprocessListenersMap::iterator mit = m_listeners.find(identifier);
    if (mit != m_listeners.end())
    {
        SPELLprocessListeners& list = (*mit).second;
        SPELLprocessListeners::iterator it;
        for( it = list.begin(); it != list.end(); it++)
        {
            if (*it != NULL)
            {
                (*it)->processKilled(identifier);
            }
        }
    }
}

//=============================================================================
// METHOD     : SPELLprocessManager::fireProcessFailed()
//=============================================================================
void SPELLprocessManager::fireProcessFailed( const std::string& identifier )
{
    SPELLmonitor m(m_lock);
    DEBUG("[PRCMGR] Firing process failed: " + identifier);
    SPELLprocessListenersMap::iterator mit = m_listeners.find(identifier);
    if (mit != m_listeners.end())
    {
        SPELLprocessListeners& list = (*mit).second;
        SPELLprocessListeners::iterator it;
        for( it = list.begin(); it != list.end(); it++)
        {
            if (*it != NULL)
            {
                (*it)->processFailed(identifier);
            }
        }
    }
}
