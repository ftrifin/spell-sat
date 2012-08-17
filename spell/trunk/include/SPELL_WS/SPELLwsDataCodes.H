// ################################################################################
// FILE       : SPELLwsDataCodes.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: WS data types
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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

#ifndef __SPELL_WS_DATA_CODES_H__
#define __SPELL_WS_DATA_CODES_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_WS */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Codes identifying different data types
 **
 ** \par Description and usage:
 **
 **		Used by data handlers to identify the marshalled data in the
 **		warmstart data files.
 **
**////////////////////////////////////////////////////////////////////////////

namespace SPELLwsData
{

typedef enum SPELLwsDataCode_
{
	DATA_GENERIC     = 0,
	DATA_LIST        = 1,
	DATA_DICTIONARY  = 2,
	DATA_CLASS       = 3,
	DATA_INSTANCE    = 4,
	DATA_BYTECODE    = 5,
	DATA_SPELL_DICT  = 6,
	DATA_SPELL_TIME  = 7,
	DATA_CUSTOM_TYPE = 8,
	DATA_SPELL_BUILTIN = 9,
	DATA_TM_ITEM     = 10,
	DATA_TC_ITEM     = 11,
	DATA_NONE        = 12
}
Code;

std::string codeStr( Code code );
SPELLwsData::Code codeOf( PyObject* object );

};

/*@}*/
#endif
