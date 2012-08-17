// ################################################################################
// FILE       : SPELLevent.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the event mechanism
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
#include "SPELL_SYN/SPELLevent.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"


//=============================================================================
// CONSTRUCTOR : SPELLevent::SPELLevent
//=============================================================================
SPELLevent::SPELLevent()
    : m_condition(),
      m_mutex()
{
    m_isClear = false;
    DEBUG("[EVE] Event " + PSTR(this) + " created with mutex " + PSTR(&m_mutex))
}

//=============================================================================
// DESTRUCTOR : SPELLevent::~SPELLevent
//=============================================================================
SPELLevent::~SPELLevent()
{
    DEBUG("[EVE] Event " + PSTR(this) + " destroyed")
}

//=============================================================================
// METHOD    : SPELLevent::set
//=============================================================================
void SPELLevent::set()
{
    if (isClear())
    {
        DEBUG("[EVE] Event " + PSTR(this) + " set")
        m_mutex.lock();
        m_isClear = false;
        m_condition.signal();
        m_mutex.unlock();
    }
}

//=============================================================================
// METHOD    : SPELLevent::clear
//=============================================================================
void SPELLevent::clear()
{
    DEBUG("[EVE] Event " + PSTR(this) + " clear")
    m_mutex.lock();
    m_isClear = true;
    m_mutex.unlock();
}

//=============================================================================
// METHOD    : SPELLevent::wait
//=============================================================================
void SPELLevent::wait()
{
    if (isClear())
    {
        DEBUG("[EVE] Event " + PSTR(this) + " wait in with mutex " + PSTR(&m_mutex))
        m_mutex.unlock();
        m_condition.wait(&m_mutex);
        m_mutex.unlock();
        DEBUG("[EVE] Event " + PSTR(this) + " wait out")
    }
}

//=============================================================================
// METHOD    : SPELLevent::wait
//=============================================================================
bool SPELLevent::wait( unsigned long timeout )
{
    bool timedout = false;
    if (isClear())
    {
        DEBUG("[EVE] Event " + PSTR(this) + " wait in with mutex " + PSTR(&m_mutex))
        m_mutex.unlock();
        timedout = m_condition.wait(&m_mutex,timeout);
        m_mutex.unlock();
        DEBUG("[EVE] Event " + PSTR(this) + " wait out")
    }
    return timedout;
}

//=============================================================================
// METHOD    : SPELLevent::isClear
//=============================================================================
bool SPELLevent::isClear()
{
    DEBUG("[EVE] Event " + PSTR(this) + " isclear with mutex " + PSTR(&m_mutex))
    bool isClear = false;
    m_mutex.lock();
    isClear = m_isClear;
    m_mutex.unlock();
    return isClear;
}
