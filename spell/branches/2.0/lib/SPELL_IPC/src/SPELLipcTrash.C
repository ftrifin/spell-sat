// ################################################################################
// FILE       : SPELLipcTrash.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the request cleaner
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
#include "SPELL_IPC/SPELLipcInput.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------
#include <unistd.h>


// DEFINES /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLipcTrash::SPELLipcTrash
//=============================================================================
SPELLipcTrash::SPELLipcTrash( std::string name )
    : SPELLthread("trash")
{
    m_name = name;
    m_working = true;
    m_finishEvent.clear();
    DEBUG( "[IPCT-" + m_name + "] SPELLipcTrash created" );
}

//=============================================================================
// DESTRUCTOR: SPELLipcTrash::~SPELLipcTrash
//=============================================================================
SPELLipcTrash::~SPELLipcTrash()
{
    clearData();
    DEBUG( "## [IPCT-" + m_name + "] SPELLipcTrash destroyed" );
}

//=============================================================================
// METHOD: SPELLipcTrash::shutdown
//=============================================================================
void SPELLipcTrash::shutdown()
{
    SPELLmonitor m(m_lock);
    m_working = false;
    SPELLipcTrashList::iterator it;
    for( it = m_messages.begin(); it != m_messages.end(); it++)
    {
        (*it)->cancel();
    }
}

//=============================================================================
// METHOD: SPELLipcTrash::place
//=============================================================================
void SPELLipcTrash::place( SPELLipcIncomingBase* msg )
{
    SPELLmonitor m(m_lock);
    if (m_working)
    {
        m_messages.push_back(msg);
    }
}

//=============================================================================
// METHOD: SPELLipcTrash::run
//=============================================================================
void SPELLipcTrash::run()
{
    while(isWorking())
    {
        if (haveData()) clearData();
        usleep(250000);
    }
    if (haveData()) clearData();
    m_finishEvent.set();
}

//=============================================================================
// METHOD: SPELLipcTrash::isWorking
//=============================================================================
bool SPELLipcTrash::isWorking()
{
    SPELLmonitor m(m_lock);
    return m_working;
}

//=============================================================================
// METHOD: SPELLipcTrash::haveData
//=============================================================================
bool SPELLipcTrash::haveData()
{
    SPELLmonitor m(m_lock);
    return (m_messages.size()>0);
}

//=============================================================================
// METHOD: SPELLipcTrash::haveData
//=============================================================================
void SPELLipcTrash::clearData()
{

    SPELLmonitor m(m_lock);
    SPELLipcTrashList::iterator it;
    for( it = m_messages.begin(); it != m_messages.end(); it++)
    {
        bool timedout = (*it)->wait(1);
        if (!timedout) delete (*it);
    }
    m_messages.clear();
}
