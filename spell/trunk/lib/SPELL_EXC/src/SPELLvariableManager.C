// ################################################################################
// FILE       : SPELLvariableManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the procedure variable manager
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
#include "SPELL_EXC/SPELLvariableManager.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"

// GLOBALS ////////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR : SPELLvariableManager::SPELLvariableManager()
//=============================================================================
SPELLvariableManager::SPELLvariableManager( SPELLframeManager& frame )
: m_frameManager(frame)
{
	DEBUG("[VMGR] Created");
}

//=============================================================================
// DESTRUCTOR: SPELLvariableManager::~SPELLvariableManager()
//=============================================================================
SPELLvariableManager::~SPELLvariableManager()
{
	DEBUG("[VMGR] Destroyed");
}

//=============================================================================
// METHOD    : SPELLvariableManager::getAllVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableManager::getAllVariables()
{
	DEBUG("[VMGR] Retrieve all variables");
	std::vector<SPELLvarInfo> vars;

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return vars;
	}

	if(isStatusValid())
	{
		vars = m_frameManager.getModel().getVariableMonitor().getAllVariables();
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
	return vars;
}

//=============================================================================
// METHOD    : SPELLvariableManager::getLocalVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableManager::getLocalVariables()
{
	DEBUG("[VMGR] Retrieve all local variables");
	std::vector<SPELLvarInfo> vars;

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return vars;
	}

	if(isStatusValid())
	{
		vars = m_frameManager.getModel().getVariableMonitor().getLocalVariables();
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
	return vars;
}

//=============================================================================
// METHOD    : SPELLvariableManager::getGlobalVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableManager::getGlobalVariables()
{
	DEBUG("[VMGR] Retrieve all global variables");
	std::vector<SPELLvarInfo> vars;

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return vars;
	}

	if(isStatusValid())
	{
		vars = m_frameManager.getModel().getVariableMonitor().getGlobalVariables();
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
	return vars;
}

//=============================================================================
// METHOD    : SPELLvariableManager::getRegisteredVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableManager::getRegisteredVariables()
{
	DEBUG("[VMGR] Retrieve all registered variables");
	std::vector<SPELLvarInfo> vars;

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return vars;
	}

	if(isStatusValid())
	{
		vars = m_frameManager.getModel().getVariableMonitor().getRegisteredVariables();
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
	return vars;
}

//=============================================================================
// METHOD    : SPELLvariableManager::getRegisteredLocalVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableManager::getRegisteredLocalVariables()
{
	DEBUG("[VMGR] Retrieve registered local variables");
	std::vector<SPELLvarInfo> vars;

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return vars;
	}

	if(isStatusValid())
	{
		vars = m_frameManager.getModel().getVariableMonitor().getRegisteredLocalVariables();
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
	return vars;
}

//=============================================================================
// METHOD    : SPELLvariableManager::getRegisteredGlobalVariables()
//=============================================================================
std::vector<SPELLvarInfo> SPELLvariableManager::getRegisteredGlobalVariables()
{
	DEBUG("[VMGR] Retrieve registered global variables");
	std::vector<SPELLvarInfo> vars;

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return vars;
	}

	if(isStatusValid())
	{
		vars = m_frameManager.getModel().getVariableMonitor().getRegisteredGlobalVariables();
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
	return vars;
}

//=============================================================================
// METHOD    : SPELLvariableManager::registerVariable()
//=============================================================================
bool SPELLvariableManager::registerVariable( SPELLvarInfo& var )
{
	DEBUG("[VMGR] Register variable " + var.varName);

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return false;
	}

	if(isStatusValid())
	{
		return m_frameManager.getModel().getVariableMonitor().registerVariable(var);
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
	return false;
}

//=============================================================================
// METHOD    : SPELLvariableManager::unregisterVariable()
//=============================================================================
void SPELLvariableManager::unregisterVariable( SPELLvarInfo& var )
{
	DEBUG("[VMGR] Unregister variable " + var.varName);

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return;
	}

	if(isStatusValid())
	{
		return m_frameManager.getModel().getVariableMonitor().unregisterVariable(var);
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
}

//=============================================================================
// METHOD    : SPELLvariableManager::changeVariable()
//=============================================================================
void SPELLvariableManager::changeVariable( SPELLvarInfo& var )
{
	DEBUG("[VMGR] Change variable " + var.varName);

	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return;
	}

	if(isStatusValid())
	{
		try
		{
			m_frameManager.getModel().getVariableMonitor().changeVariable(var);
			LOG_INFO("Variable modified: " + var.varName + " = " + var.varValue);
		}
		catch( SPELLcoreException& ex )
		{
			LOG_ERROR("Unable to assign variable value: " + ex.what());
		}
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
}

//=============================================================================
// METHOD    : SPELLvariableManager::unregisterAll()
//=============================================================================
void SPELLvariableManager::unregisterAll()
{
	if (!m_enabled)
	{
		LOG_WARN("Variable analysis is disabled");
		return;
	}

	if(isStatusValid())
	{
		m_frameManager.getModel().getVariableMonitor().unregisterAll();
	}
	else
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		std::string status = SPELLexecutorUtils::statusToString(st);
		LOG_WARN("Status is not valid for variable analysis: " + status);
	}
}

//=============================================================================
// METHOD    : SPELLvariableManager::isStatusValid()
//=============================================================================
bool SPELLvariableManager::isStatusValid()
{
	bool valid = false;
	if (m_frameManager.isReady())
	{
		SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
		switch(st)
		{
		case STATUS_PAUSED:
		case STATUS_WAITING:
		case STATUS_PROMPT:
		case STATUS_INTERRUPTED:
			valid = true;
			break;
		default:
			break;
		}
	}
	else
	{
		LOG_WARN("Frame manager is not ready for variable analysis");
	}
	return valid;
}

//=============================================================================
// METHOD    : SPELLvariableManager::isStatusValid()
//=============================================================================
PyObject* SPELLvariableManager::getVariableRef( const std::string& name )
{
	return m_frameManager.getModel().getVariableMonitor().getVariableRef(name);
}
