// ################################################################################
// FILE       : SPELLexecutorModel.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor model
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
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLexecutorModel.H"
#include "SPELL_CTX/SPELLdataHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// System includes ---------------------------------------------------------


// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLexecutorModel::SPELLexecutorModel()
//=============================================================================
SPELLexecutorModel::SPELLexecutorModel( const SPELLexecutorConfiguration& config )
{
	m_procId = config.getProcId();
	m_instanceId = config.getInstanceId();
	m_timeId = config.getTimeId();
	m_instanceNum = config.getInstanceNum();
	m_parentProcId = config.getParentInstanceId();
	m_arguments = config.getArguments();
	m_condition = config.getCondition();
	m_openMode = config.getOpenMode();
	m_configFile = config.getConfigFile();
	m_contextName = config.getContextName();
	m_ipcKey = -1;
	m_ipcPort = config.getIpcPort();
	m_PID = config.getPID();
	m_status = STATUS_UNKNOWN;
	m_logFileName = "";
	m_wsFileName = config.getRecoveryFile();
}

//=============================================================================
// DESTRUCTOR: SPELLexecutorModel::~SPELLexecutorModel()
//=============================================================================
SPELLexecutorModel::~SPELLexecutorModel()
{

}

//=============================================================================
// METHOD: SPELLexecutorModel::getStatus()
//=============================================================================
SPELLexecutorStatus SPELLexecutorModel::getStatus()
{
	SPELLmonitor m(m_lock);
	return m_status;
}

//=============================================================================
// METHOD: SPELLexecutorModel::setStatus()
//=============================================================================
void SPELLexecutorModel::setStatus( const SPELLexecutorStatus& status )
{
	SPELLmonitor m(m_lock);
	m_status = status;
}
