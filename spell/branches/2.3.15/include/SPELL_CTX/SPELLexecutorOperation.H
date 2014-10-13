// ################################################################################
// FILE       : SPELLexecutorOperation.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Definition of the executor operations
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

#ifndef __SPELL_EXECUTOR_OPERATION_H__
#define __SPELL_EXECUTOR_OPERATION_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
// Project includes ----------------------------------------------------------

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
typedef enum SPELLexecutorOperation_
{
	EXEC_OP_OPEN,
	EXEC_OP_CLOSE,
	EXEC_OP_KILL,
	EXEC_OP_ATTACH,
	EXEC_OP_DETACH,
	EXEC_OP_STATUS,
	EXEC_OP_CRASH
}
SPELLexecutorOperation;

/*@}*/
#endif
