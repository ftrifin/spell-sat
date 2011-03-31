// ################################################################################
// FILE       : SPELLexecutorRegistry.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor registry
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

#ifndef __SPELL_EXECREGISTRY_H__
#define __SPELL_EXECREGISTRY_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_SYN/SPELLmutex.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorManager.H"


/** \addtogroup SPELL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLcontext;
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////



//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Executor registry
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLexecutorRegistry
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor.
    **////////////////////////////////////////////////////////////////////
    SPELLexecutorRegistry( SPELLcontext& context );

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    ~SPELLexecutorRegistry();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    std::string getInstanceId( std::string procId );
    SPELLexecutorManager& createExecutor( std::string procId );
    SPELLexecutorManager& getExecutor( std::string instanceId );
    void removeExecutor( std::string instanceId );

    int getNumExecutors();
    std::vector<std::string> getExecutorList();
    int getNumInstances( std::string procId );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    typedef std::list<int> InstanceList;
    typedef std::map< std::string, InstanceList > ExecutorInstanceMap;
    typedef std::map< std::string, SPELLexecutorManager* > ExecutorMap;
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    void removeInstance( std::string instanceId );

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
    SPELLcontext&            m_ctx;
    ExecutorMap              m_executors;
    ExecutorInstanceMap      m_executorInstances;
    SPELLmutex               m_lock;
};

#endif
