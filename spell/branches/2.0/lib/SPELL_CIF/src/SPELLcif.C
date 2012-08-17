// ################################################################################
// FILE       : SPELLcif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the CIF
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
#include "SPELL_CIF/SPELLcif.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_EXC/SPELLexecutor.H"



// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////
// STATIC //////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLcif::SPELLcif
//=============================================================================
SPELLcif::SPELLcif()
{
    m_asRun = NULL;
    m_verbosity = -1;
    m_verbosityFilter = 0;
    m_manualMode = false;
    m_timeId = "";
    m_ctxName = "";
    m_ctxPort = 0;
    m_browsableLib = false;
}

//=============================================================================
// DESTRUCTOR: SPELLcif::~SPELLcif
//=============================================================================
SPELLcif::~SPELLcif()
{
    if (m_asRun != NULL)
    {
        delete m_asRun;
        m_asRun = NULL;
    }
}

//=============================================================================
// METHOD    : SPELLcif::setup
//=============================================================================
void SPELLcif::setup( const std::string& procId, const std::string& ctxName, const int ctxPort, const std::string& timeId )
{
    // Store the configuration values
    m_procId = procId;
    m_timeId = timeId;
    m_ctxName = ctxName;
    m_ctxPort = ctxPort;

    // Retrieve the verbosity value. For that we need to get the context
    // configuration parameters from the XML config file.
    const SPELLcontextConfig& ctx = SPELLconfiguration::instance().getContext(ctxName);

    // Obtain the verbosity value
    std::string max = ctx.getExecutorParameter( ExecutorConstants::MaxVerbosity );
    if (max == PythonConstants::None)
    {
        // Default value
        m_verbosityFilter = 10;
    }
    else
    {
        m_verbosityFilter = atoi(max.c_str());
    }

    // Obtain the borwsable lib value
    std::string browsable = ctx.getExecutorParameter( ExecutorConstants::BrowsableLib );
    m_browsableLib = (browsable == PythonConstants::True );

    // Create the AsRUN file manager
    m_asRun = new SPELLasRun( ctx, m_timeId, m_procId );
}

//=============================================================================
// METHOD    : SPELLcif::cleanup
//=============================================================================
void SPELLcif::cleanup( bool force )
{
    // Nothing to do
}

//=============================================================================
// METHOD    : SPELLcif::clearAsRun
//=============================================================================
void SPELLcif::clearAsRun()
{
    m_asRun->clear();
}

//=============================================================================
// METHOD    : SPELLcif::setManualMode
//=============================================================================
void SPELLcif::setManualMode( bool manual )
{
    m_manualMode = manual;
}

//=============================================================================
// METHOD    : SPELLcif::setVerbosity
//=============================================================================
void SPELLcif::setVerbosity( int verbosity )
{
    m_verbosity = verbosity;
}

//=============================================================================
// METHOD    : SPELLcif::setMaxVerbosity
//=============================================================================
void SPELLcif::setMaxVerbosity()
{
    m_verbosity = 10;
}

//=============================================================================
// METHOD    : SPELLcif::resetVerbosity
//=============================================================================
void SPELLcif::resetVerbosity()
{
    m_verbosity = -1;
}

//=============================================================================
// METHOD    : SPELLcif::getVerbosity
//=============================================================================
const int SPELLcif::getVerbosity() const
{
    return m_verbosity;
}

//=============================================================================
// METHOD    : SPELLcif::isManual
//=============================================================================
const bool SPELLcif::isManual() const
{
    return m_manualMode;
}

//=============================================================================
// METHOD    : SPELLcif::getVerbosityFilter
//=============================================================================
const int SPELLcif::getVerbosityFilter() const
{
    return m_verbosityFilter;
}

//=============================================================================
// METHOD    : SPELLcif::getProcId
//=============================================================================
const std::string SPELLcif::getProcId() const
{
    return m_procId;
}

//=============================================================================
// METHOD    : SPELLcif::getStack
//=============================================================================
const std::string SPELLcif::getStack() const
{
    return SPELLexecutor::instance().getCallstack().getStack();
}

//=============================================================================
// METHOD    : SPELLcif::getCodeName
//=============================================================================
const std::string SPELLcif::getCodeName() const
{
    return SPELLexecutor::instance().getCallstack().getCodeName();
}

//=============================================================================
// METHOD    : SPELLcif::getStage
//=============================================================================
const std::string SPELLcif::getStage() const
{
    return SPELLexecutor::instance().getCallstack().getStage();
}

//=============================================================================
// METHOD    : SPELLcif::toAsRun
//=============================================================================
const std::string SPELLcif::getAsRunName() const
{
    if (m_asRun == NULL) return "";
    return m_asRun->getFileName();
}

//=============================================================================
// METHOD    : SPELLcif::getNumExecutions
//=============================================================================
unsigned int SPELLcif::getNumExecutions() const
{
	return SPELLexecutor::instance().getFrame().getCurrentTraceModel().getCurrentLineExecutions();
}
