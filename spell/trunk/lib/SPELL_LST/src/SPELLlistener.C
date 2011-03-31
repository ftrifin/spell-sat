// ################################################################################
// FILE       : SPELLlistener.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Listener implementation
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
// Local includes ----------------------------------------------------------
#include "SPELL_LST/SPELLlistener.H"

static SPELLlistener* s_instance = NULL;

//=============================================================================
// CONSTRUCTOR : SPELLcontext::SPELLcontext
//=============================================================================
SPELLlistener::SPELLlistener(bool warmstart = false)
{

}

//=============================================================================
// DESTRUCTOR: SPELLcontext::~SPELLcontext
//=============================================================================
SPELLlistener::~SPELLlistener()
{
}

//=============================================================================
// METHOD :    SPELLcontext::instance
//=============================================================================
SPELLlistener& SPELLlistener::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLlistener();
    }
    return *s_instance;
}

//=============================================================================
// METHOD :    SPELLlistener::<methodname>
//=============================================================================



