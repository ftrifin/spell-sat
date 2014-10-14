// ################################################################################
// FILE       : SPELLthread.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the thread mechanism
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2014 SES ENGINEERING, Luxembourg S.A.R.L.
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
#include "SPELL_SYN/SPELLthread.H"
#include "SPELL_SYN/SPELLsyncError.H"
#include "SPELL_SYN/SPELLmonitor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------


// Bridge for the thread class run() method
void* do_run(void *args)
{
    SPELLthread* thr = reinterpret_cast<SPELLthread*>( args );
    thr->doRun();
    pthread_exit(NULL);
    return 0;
}

//=============================================================================
// CONSTRUCTOR : SPELLthread::SPELLthread
//=============================================================================
SPELLthread::SPELLthread( std::string id )
{
    m_threadId = id;
    m_thread = 0;
    m_running = false;
    DEBUG("[THR] Thread " + m_threadId + " created")
}

//=============================================================================
// DESTRUCTOR : SPELLthread::~SPELLthread
//=============================================================================
SPELLthread::~SPELLthread()
{
    DEBUG("[THR] Thread " + m_threadId + " do destroy");
    m_running = false;
}

//=============================================================================
// METHOD: SPELLthread::start
//=============================================================================
void SPELLthread::start()
{
    pthread_attr_t attr;
    pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
    int result = pthread_create(&m_thread, (const pthread_attr_t*) &attr, do_run, this );
    //std::cerr << "==== create thread " << m_thread << " " << m_threadId << std::endl;
    if( result != 0)
    {
        throw SPELLsyncError("Unable to start thread", "Cannot create thread: " + ISTR(result));
    }
    pthread_attr_destroy(&attr);
}

//=============================================================================
// METHOD: SPELLthread::setRunning
//=============================================================================
void SPELLthread::setRunning( bool running )
{
	SPELLmonitor m(m_lock);
	m_running = running;
}

//=============================================================================
// METHOD: SPELLthread::isRunning
//=============================================================================
bool SPELLthread::isRunning()
{
    SPELLmonitor m(m_lock);
    return (m_running==true);
}

//=============================================================================
// METHOD: SPELLthread::doRun
//=============================================================================
void SPELLthread::doRun()
{
    setRunning(true);
    run();
    //std::cerr << "==== finished " << m_thread << " " << m_threadId << std::endl;
}

//=============================================================================
// METHOD: SPELLthread::join
//=============================================================================
void SPELLthread::join()
{
    SPELLmonitor m(m_lock);
	if (m_thread != 0)
	{
	    //std::cerr << "==== before join " << m_thread << " " << m_threadId << std::endl;
		pthread_join(m_thread, NULL);
	    //std::cerr << "==== after join " << m_thread << " " << m_threadId << std::endl;
		m_thread = 0;
	}
}
