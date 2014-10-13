// ################################################################################
// FILE       : SPELLexecutionModel.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the execution model
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
#include "SPELL_EXC/SPELLexecutionModel.H"
#include "SPELL_EXC/SPELLvarInfo.H"
#include "SPELL_EXC/SPELLscopeInfo.H"
#include "SPELL_EXC/SPELLexecutor.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"

// GLOBALS ////////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR : SPELLexecutionModel::SPELLexecutionModel
//=============================================================================
SPELLexecutionModel::SPELLexecutionModel( const std::string& modelId,
		                                  const std::string& filename,
		                                  PyFrameObject* frame,
										  bool monitorVars,
										  std::set<std::string>& initialVariables )
: SPELLgoto(frame->f_code),
  SPELLbytecode(frame->f_code),
  SPELLlnotab(frame->f_code),
  SPELLvariableChangeListener(),
  m_frame(frame),
  m_varMonitor(this,frame,initialVariables)
{
	m_modelId = modelId;
	m_monitorVars = monitorVars;
}

//=============================================================================
// DESTRUCTOR : SPELLexecutionModel::~SPELLexecutionModel
//=============================================================================
SPELLexecutionModel::~SPELLexecutionModel()
{
}

//=============================================================================
// METHOD: SPELLexecutionModel::update()
//=============================================================================
void SPELLexecutionModel::update()
{
	if (m_monitorVars)
	{
		m_varMonitor.analyze();
	}
}

//=============================================================================
// METHOD: SPELLexecutionModel::inScope()
//=============================================================================
void SPELLexecutionModel::inScope()
{
	if (m_monitorVars)
	{
		SPELLscopeInfo info;

		info.globalRegisteredVariables = m_varMonitor.getRegisteredGlobalVariables();
		info.localRegisteredVariables = m_varMonitor.getRegisteredLocalVariables();

		if ((info.globalRegisteredVariables.size()>0)||(info.localRegisteredVariables.size()>0))
		{
			SPELLexecutor::instance().getCIF().notifyVariableScopeChange(info);
		}
	}
}

//=============================================================================
// METHOD: SPELLexecutionModel::variableChanged()
//=============================================================================
void SPELLexecutionModel::variableChanged( const std::vector<SPELLvarInfo>& changed )
{
	if (m_monitorVars)
	{
		SPELLexecutor::instance().getCIF().notifyVariableChange( changed );
	}
}
