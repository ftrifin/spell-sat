// ################################################################################
// FILE       : SPELLautomaticCif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the automatic (non-interactive) CIF
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
#include "SPELL_CIFC/SPELLautomaticCif.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_EXC/SPELLcommand.H"



// DEFINES /////////////////////////////////////////////////////////////////


//=============================================================================
// CONSTRUCTOR: SPELLautomaticCif::SPELLautomaticCif
//=============================================================================
SPELLautomaticCif::SPELLautomaticCif()
    : SPELLcif()
{
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
void SPELLautomaticCif::setup( const std::string& procId, const std::string& ctxName, int ctxPort, const std::string& timeId )
{
    SPELLcif::setup(procId, ctxName, ctxPort, timeId);
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
    return "";
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
    std::cout << "STATUS: " << StatusToString(st.status) << std::endl;
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
std::string SPELLautomaticCif::prompt( const std::string& message, PromptOptions options, unsigned int type, unsigned int scope )
{
    std::string answer = "A";
    bool keepTrying = true;
	std::list<std::string> expected;
	unsigned int count = 0;
    while(keepTrying)
    {
        std::cout << message << std::endl;
        if (options.size()>0)
        {
			std::cout << "    Options: ";
			PromptOptions::const_iterator it;
			expected.clear();
			count = 0;
			for( it = options.begin(); it != options.end(); it++)
			{
				std::string key = "";
				std::vector<std::string> tokens = tokenize( (*it), ":" );
				key = tokens[0];
				trim(key);
				expected.push_back(key);
				std::cout << (*it);
				if (count<options.size()-1) std::cout << ", ";
				count++;
			}
        }
        std::cout << std::endl << ">> ";
        char answ[512];
        std::cin.getline(answ, '\n');
        std::cout << std::endl;
        answer = answ;
        trim(answer);
        if (options.size()>0)
        {
			std::list<std::string>::const_iterator eit;
			for( eit = expected.begin(); eit != expected.end(); eit++)
			{
				if (*eit == answer )
				{
					keepTrying = false;
				}
			}
			if (keepTrying)
			{
				std::cout << "[ERROR] Expected one of the following: ";
				count = 0;
				for( eit = expected.begin(); eit != expected.end(); eit++)
				{
					std::cout << (*eit);
					if (count<expected.size()-1) std::cout << ", ";
					count++;
				}
				std::cout << std::endl;
			}
        }
        else
        {
        	keepTrying = false;
        }
    }
    return answer;
}



