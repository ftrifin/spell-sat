// ################################################################################
// FILE       : SPELLcontextManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Context model implementation
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
#include "SPELL_LST/SPELLcontextManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_IPC/SPELLipcMessage.H"
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_UTIL/SPELLlog.H"

//=============================================================================
// CONSTRUCTOR : SPELLcontextManager::SPELLcontextManager
//=============================================================================
SPELLcontextManager::SPELLcontextManager( const std::string& instanceId, SPELLcontext& context )
    : SPELLthread("ctxmanager-" + instanceId)
    //SPELLipcInterfaceListener()
{
}

//=============================================================================
// DESTRUCTOR: SPELLcontextManager::~SPELLcontextManager
//=============================================================================
SPELLcontextManager::~SPELLcontextManager()
{
}

