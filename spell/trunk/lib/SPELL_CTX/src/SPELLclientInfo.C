// ################################################################################
// FILE       : SPELLclientInfo.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the client model
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
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipcHelper.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLclientInfo.H"

//=============================================================================
// CONSTRUCTOR:  SPELLclientInfo::SPELLclientInfo
//=============================================================================
SPELLclientInfo::SPELLclientInfo( const int& key, const std::string& host )
{
    m_key = key;
    m_host = host;
}

//=============================================================================
// STATIC:  SPELLclientInfo::clientModeFromString
//=============================================================================
SPELLclientMode SPELLclientInfo::clientModeFromString( const std::string& modeStr )
{
    if (modeStr == MessageValue::DATA_GUI_MODE_C)
    {
        return CLT_MODE_CONTROL;
    }
    else if (modeStr == MessageValue::DATA_GUI_MODE_M)
    {
        return CLT_MODE_MONITOR;
    }
    else
    {
        return CLT_MODE_NONE;
    }
}

//=============================================================================
// STATIC:  SPELLclientInfo::clientModeToString
//=============================================================================
std::string SPELLclientInfo::clientModeToString( const SPELLclientMode& mode )
{
    switch(mode)
    {
    case CLT_MODE_CONTROL:
        return MessageValue::DATA_GUI_MODE_C;
    case CLT_MODE_MONITOR:
        return MessageValue::DATA_GUI_MODE_M;
    default:
        return "None";
    }
}

//=============================================================================
// METHOD:  SPELLclientInfo::addExecutor
//=============================================================================
void SPELLclientInfo::addExecutor( const std::string& procId, const SPELLclientMode& mode )
{
    SPELLclientExecutorInfo* info = new SPELLclientExecutorInfo();
    info->procId = procId;
    info->mode = mode;
    m_executors.insert( std::make_pair( procId, info ));
    DEBUG("[CI] Associate executor '" + procId + "' with client " + ISTR(m_key) )
}

//=============================================================================
// METHOD:  SPELLclientInfo::removeExecutor
//=============================================================================
void SPELLclientInfo::removeExecutor( const std::string& procId )
{
    SPELLclientExecutorMap::iterator it = m_executors.find(procId);
    if (it != m_executors.end())
    {
        delete (*it).second;
        m_executors.erase(it);
    }
    DEBUG("[CI] Remove executor '" + procId + "' from client " + ISTR(m_key) )
}

//=============================================================================
// METHOD:  SPELLclientInfo::getExecutors
//=============================================================================
SPELLexecutorList SPELLclientInfo::getExecutors()
{
    SPELLexecutorList list;
    SPELLclientExecutorMap::iterator it;
    SPELLclientExecutorMap::iterator end = m_executors.end();
    DEBUG("[CI] Executors associated with client " + ISTR(m_key) + ":" )
    for( it = m_executors.begin(); it != end; it++)
    {
        DEBUG("[CI]     - '" + (*it).first + "'" )
        list.push_back( (*it).first );
    }
    return list;
}

//=============================================================================
// METHOD:  SPELLclientInfo::getMode
//=============================================================================
SPELLclientMode SPELLclientInfo::getMode( const std::string& procId )
{
    SPELLclientExecutorMap::iterator it;
    SPELLclientExecutorMap::iterator end = m_executors.end();
    SPELLclientMode mode = CLT_MODE_UNKNOWN;
    for( it = m_executors.begin(); it != end; it++)
    {
        if (it->first == procId)
        {
            mode = it->second->mode;
        }
    }
    return mode;
}

//=============================================================================
// METHOD:  SPELLclientInfo::isControlling
//=============================================================================
bool SPELLclientInfo::isControlling( const std::string& procId )
{
    SPELLclientExecutorMap::iterator it = m_executors.find(procId);
    if (it != m_executors.end() )
    {
        if (it->second->mode == CLT_MODE_CONTROL)
        {
            return true;
        }
    }
    return false;
}
