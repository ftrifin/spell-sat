// ################################################################################
// FILE       : SPELLasRun.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the As-RUN interface
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
#include "SPELL_CIF/SPELLasRun.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_CFG/SPELLconfiguration.H"



// DEFINES /////////////////////////////////////////////////////////////////
static const std::string ASRUN_LOCATION = "ar";


//=============================================================================
// CONSTRUCTOR: SPELLasRun::SPELLasRun
//=============================================================================
SPELLasRun::SPELLasRun( const SPELLcontextConfig& ctxConfig, const std::string& time, const std::string& procId )
{
    std::string home = getSPELL_DATA();

    // Character replacements
    std::string theId = procId;
    replace( theId, ".py", "" );
    replace( theId, "..", "" );
    replace( theId, "//", "/" );
    replace( theId, PATH_SEPARATOR, "_" );

    // Get the location of AsRUN files
    std::string ddir = ctxConfig.getLocationPath( ASRUN_LOCATION );

    // Build the full file path
    m_fileName = home + PATH_SEPARATOR + ddir + PATH_SEPARATOR;
    m_fileName = m_fileName + time + "_Executor_" + theId + ".ASRUN";
    LOG_INFO("[ASRUN] SPELLasRun file: " + m_fileName)

    m_file.open( m_fileName.c_str(), std::ios::out);
    toAsRun( "INIT" );
}

//=============================================================================
// DESTRUCTOR: SPELLasRun::~SPELLasRun
//=============================================================================
SPELLasRun::~SPELLasRun()
{

}

//=============================================================================
// METHOD:    SPELLasRun::toAsRun()
//=============================================================================
void SPELLasRun::toAsRun( const std::string& info )
{
    std::string line = timestamp() + "\t" + info;
    m_file << line << std::endl;
    m_file.flush();
}

//=============================================================================
// METHOD:    SPELLasRun::toAsRun()
//=============================================================================
void SPELLasRun::toAsRun( const std::string& info, long sequence )
{
    std::string line = timestamp() + "\t" + info + "\t" + ISTR(sequence);
    m_file << line << std::endl;
    m_file.flush();
}

//=============================================================================
// METHOD:    SPELLasRun::clear()
//=============================================================================
void SPELLasRun::clear()
{
    m_file.close();
    m_file.open( m_fileName.c_str(), std::ios::trunc | std::ios::out );
    toAsRun( "INIT" );
}

//=============================================================================
// METHOD:    SPELLasRun::writeStatus()
//=============================================================================
void SPELLasRun::writeStatus( const SPELLexecutorStatus st )
{
    toAsRun( STR("STATUS") + "\t\t\t" + StatusToString(st) );
}

//=============================================================================
// METHOD:    SPELLasRun::writeInfo()
//=============================================================================
void SPELLasRun::writeInfo( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    replace( message, "\n", "%C%");
    replace( message, "\t", "%T%");
    toAsRun( STR("DISPLAY") + "\tINFO\t" + stack + "\t" + message + "\t" + ISTR(scope));
}

//=============================================================================
// METHOD:    SPELLasRun::writeWarning
//=============================================================================
void SPELLasRun::writeWarning( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    replace( message, "\n", "%C%");
    replace( message, "\t", "%T%");
    toAsRun( STR("DISPLAY") + "\tWARN\t" + stack + "\t" + message + "\t" + ISTR(scope));
}

//=============================================================================
// METHOD:    SPELLasRun::writeError
//=============================================================================
void SPELLasRun::writeError( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    replace( message, "\n", "%C%");
    replace( message, "\t", "%T%");
    toAsRun( STR("DISPLAY") + "\tERROR\t" + stack + "\t" + message + "\t" + ISTR(scope));
}

//=============================================================================
// METHOD:    SPELLasRun::writePrompt
//=============================================================================
void SPELLasRun::writePrompt( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    replace( message, "\n", "%C%");
    replace( message, "\t", "%T%");
    toAsRun( STR("PROMPT") + "\t\t" + stack + "\t" + message + "\t" + ISTR(scope));
}

//=============================================================================
// METHOD:    SPELLasRun::writeAnswer
//=============================================================================
void SPELLasRun::writeAnswer( const std::string& stack, const std::string& msg, unsigned int scope )
{
    std::string message = msg;
    replace( message, "\n", "%C%");
    replace( message, "\t", "%T%");
    toAsRun( STR("ANSWER") + "\t\t" + stack + "\t" + message + "\t" + ISTR(scope));
}

//=============================================================================
// METHOD:    SPELLasRun::writeItem
//=============================================================================
void SPELLasRun::writeItem( const std::string& stack,
							const std::string& type, const std::string& name,
                            const std::string& value, const std::string& status,
                            const std::string& comment, const std::string& timestamp )
{
    std::string theComment = " ";
    std::string theTimestamp = " ";
    if (comment != "") theComment = comment;
    if (timestamp != "" ) theTimestamp = timestamp;
    toAsRun( STR("ITEM") + "\t" + type + "\t" + stack + "\t" + name + "\t" + value + "\t" + status + "\t" + theTimestamp + "\t" + theComment);
}

//=============================================================================
// METHOD:    SPELLasRun::writeLine
//=============================================================================
void SPELLasRun::writeLine( const std::string& stack, long sequence )
{
    toAsRun( STR("LINE") + "\t\t" + stack, sequence );
}

//=============================================================================
// METHOD:    SPELLasRun::writeCall
//=============================================================================
void SPELLasRun::writeCall( const std::string& stack, long sequence )
{
    toAsRun( STR("CALL") + "\t\t" + stack, sequence );
}

//=============================================================================
// METHOD:    SPELLasRun::writeReturn
//=============================================================================
void SPELLasRun::writeReturn( long sequence )
{
    toAsRun( STR("RETURN") + "\t\t", sequence );
}

//=============================================================================
// METHOD:    SPELLasRun::writeErrorInfo
//=============================================================================
void SPELLasRun::writeErrorInfo( const std::string& error, const std::string& reason )
{
    toAsRun( STR("ERROR") + "\t\t\t" + error + "\t" + reason );
}
