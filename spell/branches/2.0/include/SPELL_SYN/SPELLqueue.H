// ################################################################################
// FILE       : SPELLqueue.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Queue mechanism
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

#ifndef __SPELL_QUEUE_H__
#define __SPELL_QUEUE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_SYN/SPELLevent.H"
#include "SPELL_SYN/SPELLsyncError.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \addtogroup SPELL_SYN */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Encapsulates a queue mechanism
 **
 ** \par Description: the Queue class implements a blocking FIFO queue with
 ** a fixed size.
 **
 ** \par Usage: items of type T can be pushed into the queue until the limit
 ** size is reached. At that moment, any subsequent call to push will block
 ** the caller until the pull method is called to retrieve one object from
 ** the queue. On the other hand, any call to pull method when the queue
 ** is empty will block the caller until push method is called to put
 ** one item in the queue.
 **
**////////////////////////////////////////////////////////////////////////////
template <class T>
class SPELLqueue
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor.
     *
     * \param size IN: Size of the queue.
    **////////////////////////////////////////////////////////////////////
    SPELLqueue( int size );

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    ~SPELLqueue();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Push an item on the queue. Will block the caller if the queue
     * is full, until one item is removed from it.
     *
     * \param item IN: the item to be put on the queue.
    **////////////////////////////////////////////////////////////////////
    void push( T item );

    //////////////////////////////////////////////////////////////////////
    /** Pull an item from the queue. Will block the caller if the queue
     * is empty, until one item is put in the queue.
     *
     * \param timeout IN: timeout in seconds
     *
     * \returns The item on queue top.
    **////////////////////////////////////////////////////////////////////
    T pull( unsigned long timeout );

    //////////////////////////////////////////////////////////////////////
    /** Pull an item from the queue. Will block the caller if the queue
     * is empty, until one item is put in the queue.
     *
     * \returns The item on queue top.
    **////////////////////////////////////////////////////////////////////
    T pull();

    //////////////////////////////////////////////////////////////////////
    /** Check if the queue empty
     *
     * \returns True if the queue is empty.
    **////////////////////////////////////////////////////////////////////
    bool empty() {
        return m_queue.empty();
    };

    //////////////////////////////////////////////////////////////////////
    /** Clear the queue
    **////////////////////////////////////////////////////////////////////
    void clear()
    {
    	for ( unsigned int index = 0; index < m_queue.size(); index++ ) m_queue.pop();
    }

    //////////////////////////////////////////////////////////////////////
    /** Get the size of the queue
    **////////////////////////////////////////////////////////////////////
    unsigned int size() { return m_queue.size(); };

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
    // Internal queue representation
    std::queue<T>  m_queue;
    // Maximum number of elements
    unsigned int m_max;
    // Event triggered when an item is pushed in the queue
    SPELLevent  m_inEvent;
    // Event triggered when an item is pulled from the queue
    SPELLevent  m_outEvent;
};

//============================================================================
// CONSTRUCTOR:        Queue::Queue
//============================================================================
template <class T>
SPELLqueue<T>::SPELLqueue( int size )
    : m_inEvent(),
      m_outEvent()
{
    m_max = size;
}

//============================================================================
// DESTRUCTOR:        Queue::~Queue
//============================================================================
template <class T>
SPELLqueue<T>::~SPELLqueue()
{
    //while(not m_queue.empty()) m_queue.pop();
}

//============================================================================
// METHOD:        Queue::push
//============================================================================
template <class T>
void SPELLqueue<T>::push( T item )
{
    if (m_queue.size() == m_max)
    {
        m_inEvent.clear();
        m_inEvent.wait();
    }
    m_queue.push(item);
    m_outEvent.set();
}

//============================================================================
// METHOD:        Queue::pull
//============================================================================
template <class T>
T SPELLqueue<T>::pull( unsigned long timeout )
{
    bool timedout = false;
    if (m_queue.size() == 0)
    {
        m_outEvent.clear();
        timedout = m_outEvent.wait(timeout);
    }
    if (timedout)
    {
        throw SPELLsyncError("Unable to pull data", "Event timed out", SPELL_SYNC_ERROR_TIMEOUT);
    }
    T item = m_queue.front();
    m_queue.pop();
    m_inEvent.set();
    return item;
}

//============================================================================
// METHOD:        Queue::pull
//============================================================================
template <class T>
T SPELLqueue<T>::pull()
{
    if (m_queue.size() == 0)
    {
        m_outEvent.clear();
        m_outEvent.wait();
    }
    T item = m_queue.front();
    m_queue.pop();
    m_inEvent.set();
    return item;
}

/*@}*/
#endif
