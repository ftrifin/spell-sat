// ################################################################################
// FILE       : SPELLclientInfo.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Client GUI information
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

#ifndef __SPELL_CLIENTINFO_H__
#define __SPELL_CLIENTINFO_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Local includes ----------------------------------------------------------

/** \addtogroup SPELL */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Client operation mode
 **
 ** \par Description:
 **
 ** \par Usage:
 **
**////////////////////////////////////////////////////////////////////////////
typedef enum ClientMode_
{
    CLT_MODE_CONTROL,
    CLT_MODE_MONITOR,
    CLT_MODE_NONE,
    CLT_MODE_UNKNOWN
}
SPELLclientMode;

typedef enum WhichClients_
{
    WHICH_ALL,
    WHICH_CONTROLLING,
    WHICH_MONITORING
}
SPELLwhichClients;

typedef std::vector<std::string>  			     SPELLexecutorList;
typedef std::vector<int>  		          	     SPELLclientKeyList;

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Client information
 **
 ** \par Description:
 **
 ** \par Usage:
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLclientInfo
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    SPELLclientInfo( const int& key, const std::string& host );

    // STATIC ////////////////////////////////////////////////////////////////

    static SPELLclientMode clientModeFromString( const std::string& modeStr );
    static std::string clientModeToString( const SPELLclientMode& mode );

    // METHODS ///////////////////////////////////////////////////////////////

    std::string getHost() {
        return m_host;
    };
    int getKey() {
        return m_key;
    };
    void addExecutor( const std::string& procId, const SPELLclientMode& mode );
    void removeExecutor( const std::string& procId );
    SPELLexecutorList getExecutors();

    SPELLclientMode getMode( const std::string& procId );

    bool isControlling( const std::string& procId );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    typedef struct ClientExecutorInfo_
    {
        std::string       procId;
        SPELLclientMode   mode;
    }
    SPELLclientExecutorInfo;
    typedef std::map<std::string,SPELLclientExecutorInfo*> SPELLclientExecutorMap;
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
    int                      m_key;            // Holds the client key
    std::string              m_host;           // Holds the client host name
    SPELLclientExecutorMap   m_executors;      // Holds the client executors information
};

typedef std::map<int,SPELLclientInfo*>           SPELLclientInfoMap;

#endif
