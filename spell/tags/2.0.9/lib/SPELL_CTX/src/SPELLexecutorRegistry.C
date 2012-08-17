// ################################################################################
// FILE       : SPELLexecutorRegistry.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor registry
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
#include "SPELL_CTX/SPELLexecutorRegistry.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"


#define INSTANCE_SEPARATOR "#"

//============================================================================
// FUNCTION:    inlist
// DESCRIPTION:    Check if an item is in a list
//============================================================================
inline bool inlist( const std::list<int>& list, const int& instance )
{
    std::list<int>::const_iterator it;
    std::list<int>::const_iterator end = list.end();
    for( it = list.begin(); it != end; it++)
    {
        if ( (*it) == instance ) return true;
    }
    return false;
}

//=============================================================================
// CONSTRUCTOR : SPELLexecutorRegistry::SPELLexecutorRegistry
//=============================================================================
SPELLexecutorRegistry::SPELLexecutorRegistry( SPELLcontext& context )
    : m_ctx(context)
{
}

//=============================================================================
// DESTRUCTOR: SPELLexecutorRegistry::~SPELLexecutorRegistry
//=============================================================================
SPELLexecutorRegistry::~SPELLexecutorRegistry()
{
    m_executorInstances.clear();
}

//=============================================================================
// METHOD :    SPELLexecutorRegistry::getInstanceId
//=============================================================================
std::string SPELLexecutorRegistry::getInstanceId( std::string procId )
{
    SPELLmonitor m(m_lock);
    int instance = 0;
    if (m_executorInstances.find(procId) != m_executorInstances.end() )
    {
        InstanceList& list = (m_executorInstances.find(procId))->second;
        for(int count=0; count<50; count++)
        {
            if (!inlist(list,count))
            {
                instance = count;
                count = 50;
                break;
            }
        }
        list.push_back(instance);
    }
    else
    {
        InstanceList list;
        list.push_back(0);
        m_executorInstances.insert( std::make_pair( procId, list ));
    }
    return procId + INSTANCE_SEPARATOR + ISTR(instance);
}

//=============================================================================
// METHOD :    SPELLexecutorRegistry::getNumExecutors
//=============================================================================
int SPELLexecutorRegistry::getNumExecutors()
{
    SPELLmonitor m(m_lock);
    int count = 0;
    ExecutorInstanceMap::iterator mit;
    for( mit = m_executorInstances.begin(); mit != m_executorInstances.end(); mit++)
    {
        count += (*mit).second.size();
    }
    return count;
}

//=============================================================================
// METHOD :    SPELLexecutorRegistry::getNumInstances
//=============================================================================
int SPELLexecutorRegistry::getNumInstances( std::string procId )
{
    SPELLmonitor m(m_lock);
    ExecutorInstanceMap::iterator mit = m_executorInstances.find(procId);
    int count = 0;
    if (mit != m_executorInstances.end())
    {
        count = (*mit).second.size();
    }
    return count;

}

//=============================================================================
// METHOD :    SPELLexecutorRegistry::removeInstance
//=============================================================================
void SPELLexecutorRegistry::removeInstance( std::string instanceId )
{
    std::size_t pos = instanceId.find(INSTANCE_SEPARATOR);
    if (pos != std::string::npos)
    {
        std::string procId = instanceId.substr(0,pos);
        std::string instStr = instanceId.substr(pos+1,instanceId.size()-pos+1);
        int instance = atoi( instStr.c_str() );
        ExecutorInstanceMap::iterator mit = m_executorInstances.find(procId);
        if (mit != m_executorInstances.end())
        {
            InstanceList& list = (*mit).second;
            InstanceList::iterator lit;
            for(lit = list.begin(); lit != list.end(); lit++)
            {
                if ((*lit)==instance)
                {
                    list.erase(lit);
                    break;
                }
            }
            if (list.size()==0)
            {
                m_executorInstances.erase(mit);
            }
        }
        else
        {
            throw SPELLcoreException("Cannot remove instance " + instanceId, "No such procedure");
        }
    }
    else
    {
        throw SPELLcoreException("Cannot remove instance " + instanceId, "No instance separator found");
    }
}

//=============================================================================
// METHOD :    SPELLexecutorRegistry::createExecutor
//=============================================================================
SPELLexecutorManager& SPELLexecutorRegistry::createExecutor( std::string instanceId )
{
    SPELLmonitor m(m_lock);
    SPELLexecutorManager* mgr = new SPELLexecutorManager( instanceId, m_ctx );
    m_executors.insert( std::make_pair( instanceId, mgr ));
    return *mgr;
}

//=============================================================================
// METHOD :    SPELLexecutorRegistry::getExecutor
//=============================================================================
SPELLexecutorManager& SPELLexecutorRegistry::getExecutor( std::string instanceId )
{
    SPELLmonitor m(m_lock);
    ExecutorMap::iterator it = m_executors.find(instanceId);
    if (it != m_executors.end())
    {
        return *((*it).second);
    }
    else
    {
        throw SPELLcoreException("Cannot get executor " + instanceId, "No such executor in registry");
    }
}

//=============================================================================
// METHOD :    SPELLexecutorRegistry::removeExecutor
//=============================================================================
void SPELLexecutorRegistry::removeExecutor( std::string instanceId )
{
    SPELLmonitor m(m_lock);
    ExecutorMap::iterator it = m_executors.find(instanceId);
    if (it != m_executors.end())
    {
        delete (*it).second;
        m_executors.erase(it);
        removeInstance(instanceId);
    }
    else
    {
        throw SPELLcoreException("Cannot get executor " + instanceId, "No such executor in registry");
    }
}

//=============================================================================
// METHOD :    SPELLexecutorRegistry::getExecutorList
//=============================================================================
std::vector<std::string> SPELLexecutorRegistry::getExecutorList()
{
    SPELLmonitor m(m_lock);
    std::vector<std::string> list;
    ExecutorMap::iterator it;
    ExecutorMap::iterator end = m_executors.end();
    for( it = m_executors.begin(); it != end; it++)
    {
        list.push_back( (*it).first );
    }
    return list;
}
