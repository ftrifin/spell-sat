// ################################################################################
// FILE       : SPELLcontextRegistry.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Registry of available contexts
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

#ifndef __SPELL_CTXREGISTRY_H__
#define __SPELL_CTXREGISTRY_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_SYN/SPELLmutex.H"
// Local includes ----------------------------------------------------------
#include "SPELL_LST/SPELLcontextManager.H"


/** \addtogroup SPELL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLlistener;
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////



//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Context registry
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcontextRegistry
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor.
    **////////////////////////////////////////////////////////////////////
    SPELLcontextRegistry( SPELLlistener& );

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    ~SPELLcontextRegistry();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    SPELLcontextManager& createContext( std::string procId );
    SPELLcontextManager& getContext( std::string instanceId );
    void removeContext( std::string instanceId );

    int getNumContexts();
    std::vector<std::string> getContextList();
    int getNumInstances( std::string procId );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    typedef std::list<int> InstanceList;
    typedef std::map< std::string, InstanceList > ContextInstanceMap;
    typedef std::map< std::string, SPELLcontextManager* > ContextMap;
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    std::string getInstanceId( std::string procId );
    void removeInstance( std::string instanceId );

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
    SPELLlistener&            m_lst;
    ContextMap                m_contexts;
    ContextInstanceMap        m_contextInstances;
    SPELLmutex                m_lock;
};

#endif
