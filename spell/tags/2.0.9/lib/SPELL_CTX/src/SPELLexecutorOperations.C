// ################################################################################
// FILE       : SPELLexecutorOperations.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor operations utilities
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_IPC/SPELLipc.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorOperations.H"

//=============================================================================
// STATIC: SPELLexecutorOperations::executorOperationToString
//=============================================================================
std::string SPELLexecutorOperations::executorOperationToString( const SPELLexecutorOperation& op )
{
    switch(op)
    {
    case EXOP_OPEN:
        return MessageValue::DATA_EXOP_OPEN;
    case EXOP_CLOSE:
        return MessageValue::DATA_EXOP_CLOSE;
    case EXOP_KILL:
        return MessageValue::DATA_EXOP_KILL;
    case EXOP_ATTACH:
        return MessageValue::DATA_EXOP_ATTACH;
    case EXOP_DETACH:
        return MessageValue::DATA_EXOP_DETACH;
    case EXOP_ERROR:
        return MessageValue::DATA_EXOP_ERROR;
    }
    return "UNKNOWN";
}
