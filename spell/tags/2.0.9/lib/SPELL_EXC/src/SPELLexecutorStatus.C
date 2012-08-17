// ################################################################################
// FILE       : SPELLexecutorStatus.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor status codes
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
#include "SPELL_EXC/SPELLexecutorStatus.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"


// GLOBALS /////////////////////////////////////////////////////////////////

// String equivalents for status codes
static const std::string _StatusStr[] =
{
	"UNINIT",
	"LOADED",
	"PAUSED",
	"STEPPING",
	"RUNNING",
	"FINISHED",
	"ABORTED",
	"ERROR",
	"WAITING",
	"INTERRUPTED",
	"RELOADING",
	"UNKNOWN"
};
// Number of status codes
static const int NUM_STATUS = sizeof(_StatusStr) / sizeof(std::string);

//============================================================================
// FUNCTION        : StatusToString
//============================================================================
std::string StatusToString( SPELLexecutorStatus st )
{
    return _StatusStr[st];
}

//============================================================================
// FUNCTION        : StatusToPyString
//============================================================================
PyObject* StatusToPyString( SPELLexecutorStatus st )
{
    return SSTRPY( StatusToString(st) );
}

//============================================================================
// FUNCTION        : StringToStatus
//============================================================================
SPELLexecutorStatus StringToStatus( std::string st )
{
    int idx = 0;
    for( idx = 0; idx<NUM_STATUS; idx++)
    {
        if (st == _StatusStr[idx]) return (SPELLexecutorStatus) idx;
    }
    return STATUS_UNKNOWN;
}

//============================================================================
// FUNCTION        : PyStringToStatus
//============================================================================
SPELLexecutorStatus PyStringToStatus( PyObject* pyst )
{
    std::string equiv = PyString_AsString( pyst );
    return StringToStatus( equiv );
}
