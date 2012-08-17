// ################################################################################
// FILE       : SPELLexecutorConfiguration.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor configuration
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutorConfiguration.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"

// GLOBALS ////////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR : SPELLexecutorConfig::SPELLexecutorConfig
//=============================================================================
SPELLexecutorConfig::SPELLexecutorConfig()
{
    reset();
    DEBUG("[E] SPELLexecutorConfig created")
}

//=============================================================================
// DESTRUCTOR : SPELLexecutorConfig::~SPELLexecutorConfig
//=============================================================================
SPELLexecutorConfig::~SPELLexecutorConfig()
{
    DEBUG("[E] SPELLexecutorConfig destroyed")
}

//=============================================================================
// METHOD    : SPELLexecutorConfig::reset()
//=============================================================================
void SPELLexecutorConfig::reset()
{
    m_byStep             = false;
    m_execDelay          = 0;
    m_runInto            = true;
    m_visible            = true;
    m_automatic          = false;
    m_blocking           = true;
    m_arguments          = "";
    m_condition          = "";
}

