// ################################################################################
// FILE       : SPELLexecutorManagerConfig.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor manager configuration
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

#ifndef __SPELL_OPEN_OPTIONS_H__
#define __SPELL_OPEN_OPTIONS_H__

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
 ** \brief Executor open options
 **
 ** \par Description:
 **
 ** \par Usage:
 **
**////////////////////////////////////////////////////////////////////////////
typedef struct ExecutorManagerConfig_
{
    std::string 	 arguments;    // Executor initial arguments
    std::string 	 condition;    // Executor initial condition
    int         	 clientKey;    // First client
    std::string      clientHost;   // First client host
    SPELLclientMode  clientMode;   // First client mode
    bool        	 automatic;    // Automatic mode flag
    bool        	 visible;      // Visible mode flag
    bool        	 blocking;     // Blocking mode flag
}
SPELLexecutorManagerConfig;

#endif
