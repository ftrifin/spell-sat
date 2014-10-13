// ################################################################################
// FILE       : SPELLautomaticCif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the automatic (non-interactive) CIF
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
#include "SPELL_CIFC/SPELLautomaticCif.H"
#include "SPELL_CIF/SPELLcifHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_EXC/SPELLcommand.H"



// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLautomaticCif::SPELLautomaticCif
//=============================================================================
SPELLautomaticCif::SPELLautomaticCif( const std::string& promptFile, const std::string& procArguments )
: SPELLcif(),
  m_promptFilename(promptFile),
  m_procArguments(procArguments)
{
	loadPromptAnswers();
}

//=============================================================================
// DESTRUCTOR: SPELLautomaticCif::~SPELLautomaticCif
//=============================================================================
SPELLautomaticCif::~SPELLautomaticCif()
{
}

//=============================================================================
// METHOD: SPELLautomaticCif::setup
//=============================================================================
void SPELLautomaticCif::setup( const SPELLcifStartupInfo& info )
{
    SPELLcif::setup(info);
    DEBUG("[CIF] Installed automatic CIF")
}

//=============================================================================
// METHOD: SPELLautomaticCif::cleanup
//=============================================================================
void SPELLautomaticCif::cleanup( bool force )
{
    SPELLcif::cleanup(force);
}

//=============================================================================
// METHOD: SPELLautomaticCif::getArguments
//=============================================================================
std::string SPELLautomaticCif::getArguments()
{
    return m_procArguments;
}

//=============================================================================
// METHOD: SPELLautomaticCif::getCondition
//=============================================================================
std::string SPELLautomaticCif::getCondition()
{
    return "";
}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyLine
//=============================================================================
void SPELLautomaticCif::notifyLine()
{
}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyCall
//=============================================================================
void SPELLautomaticCif::notifyCall()
{
}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyReturn
//=============================================================================
void SPELLautomaticCif::notifyReturn()
{
}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyStatus
//=============================================================================
void SPELLautomaticCif::notifyStatus( const SPELLstatusInfo& st )
{
    std::cout << "STATUS: " << SPELLexecutorUtils::statusToString(st.status) << std::endl;
    if (st.status == STATUS_PAUSED)
    {
    	ExecutorCommand cmd;
    	cmd.id = CMD_RUN;
    	SPELLexecutor::instance().command(cmd,false);
    }
}

//=============================================================================
// METHOD: SPELLautomaticCif::notifyError
//=============================================================================
void SPELLautomaticCif::notifyError( const std::string& error, const std::string& reason, bool fatal )
{
    std::string fatalStr = "(Fatal:no)";
    if (fatal)
    {
        fatalStr = "(Fatal:yes)";
    }
    std::cout << "ERROR: " << error << ": " << reason  << " " << fatalStr << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::write
//=============================================================================
void SPELLautomaticCif::write( const std::string& msg, unsigned int scope  )
{
    std::cout << msg << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::warning
//=============================================================================
void SPELLautomaticCif::warning( const std::string& msg, unsigned int scope  )
{
    std::cout << "WARNING: " << msg << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::error
//=============================================================================
void SPELLautomaticCif::error( const std::string& msg, unsigned int scope  )
{
    std::cout << "ERROR: " << msg << std::endl;
}

//=============================================================================
// METHOD: SPELLautomaticCif::log
//=============================================================================
void SPELLautomaticCif::log( const std::string& msg )
{
}

//=============================================================================
// METHOD: SPELLautomaticCif::prompt
//=============================================================================
std::string SPELLautomaticCif::prompt( const SPELLpromptDefinition& def )
{
	std::string answer = "";
	if (m_promptAnswers.size()>0)
	{
		try
		{
			answer = automaticPrompt(def);
		}
		catch( SPELLcoreException& ex )
		{
			error("Unable to answer the prompt automatically: " + std::string(ex.what()), -1);
			answer = SPELLcifHelper::commandLinePrompt(def);
		}
	}
	else
	{
		answer = SPELLcifHelper::commandLinePrompt(def);
	}
	return answer;
}

//=============================================================================
// METHOD: SPELLautomaticCif::loadPromptAnswers()
//=============================================================================
void SPELLautomaticCif::loadPromptAnswers()
{
	if (m_promptFilename == "") return;
	std::ifstream file;
	if (!SPELLutils::pathExists(m_promptFilename))
	{
		THROW_EXCEPTION("Unable to load prompt answers", "File not found: '" + m_promptFilename + "'", SPELL_ERROR_FILESYSTEM);
	}
	file.open( m_promptFilename.c_str() );
	if (!file.is_open())
	{
		THROW_EXCEPTION("Unable to load prompt answers", "Cannot open file for read: '" + m_promptFilename + "'", SPELL_ERROR_FILESYSTEM);
	}

    while(!file.eof())
    {
        std::string line = "";
        std::getline(file,line);
        SPELLutils::trim(line);
        if (line == "") continue;
        m_promptAnswers.push_back(line);
    }
	m_promptAnswerIndex = 0;
	file.close();
}

//=============================================================================
// METHOD: SPELLautomaticCif::automaticPrompt()
//=============================================================================
std::string SPELLautomaticCif::automaticPrompt( const SPELLpromptDefinition& def )
{
	if (m_promptAnswers.size()==0)
	{
		THROW_EXCEPTION("Cannot perform automatic prompt", "No answers available", SPELL_ERROR_EXECUTION);
	}
	if (m_promptAnswerIndex==m_promptAnswers.size())
	{
		THROW_EXCEPTION("Cannot perform automatic prompt", "No more answers available", SPELL_ERROR_EXECUTION);
	}
	std::string answer = m_promptAnswers[m_promptAnswerIndex];
	m_promptAnswerIndex++;
	return answer;
}
