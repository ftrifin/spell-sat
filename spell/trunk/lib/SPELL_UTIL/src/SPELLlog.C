// ################################################################################
// FILE       : SPELLlog.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the logger
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2014 SES ENGINEERING, Luxembourg S.A.R.L.
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
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"

// GLOBALS /////////////////////////////////////////////////////////////////

// Log singleton instance
static SPELLlog* s_instance = 0;



//=============================================================================
// CONSTRUCTOR : SPELLlog::SPELLlog()
//=============================================================================
SPELLlog::SPELLlog()
{
    m_maxLevel = LOG_LEV_MAX;
    m_enabled = true;
    m_traceEnabled = true;
}

//=============================================================================
// DESTRUCTOR : SPELLlog::~SPELLlog
//=============================================================================
SPELLlog::~SPELLlog()
{
}

//=============================================================================
// METHOD    : SPELLlog::instance()
//=============================================================================
SPELLlog& SPELLlog::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLlog();
    }
    return *s_instance;
}

//=============================================================================
// METHOD: originStr
//=============================================================================
std::string SPELLlog::originStr( const std::string& origin )
{
    std::string result = origin;
    std::size_t idx = result.find_last_of(PATH_SEPARATOR) + 1;
    if (idx!=std::string::npos)
    {
    	result = result.substr(idx, result.size()-idx);
    }
    const std::string fill("                              ");
    unsigned int size = result.size();
    if (size>LOG_MAX_LEN)
    {
        result = result.substr(0, LOG_MAX_LEN-3) + "...";
    }
    else
    {
    	result += fill.substr(0,LOG_MAX_LEN-size);
    }
    result = "[ " + result + " ]";
    return result;
}

//=============================================================================
// METHOD    : SPELLlog::setLogFile
//=============================================================================
void SPELLlog::setLogFile( const std::string& filename, const std::string timestamp )
{
    SPELLmonitor m(m_logMutex);
    m_logFile.close();
    char* home = getenv("SPELL_LOG");
    std::string logbase = ( home == NULL) ? "." : home;

    std::string logfilename = timestamp  + filename + ".log";
    // Remove possible ".." and ".py" from file
    SPELLutils::replace( logfilename, ".py", "" );
    SPELLutils::replace( logfilename, "..", "" );
    SPELLutils::replace( logfilename, "//", "/" );

    std::string::size_type pos =0;
    while(true)
    {
        pos = logfilename.find_first_of("/",pos);
        if (pos != std::string::npos )
        {
            logfilename.replace( pos, 1, "_" );
        }
        else
        {
            break;
        }
    }
    logfilename = logbase + "/" + logfilename;
    m_logFileName = logfilename;
    m_logFile.open( logfilename.c_str() ,std::ios::out);
    m_logFile << "Created" << std::endl;
    m_logFile.flush();
}

//=============================================================================
// METHOD    : SPELLlog::showLevel
//=============================================================================
void SPELLlog::showLevel( LogLevel level )
{
    SPELLmonitor m(m_logMutex);
    m_maxLevel = level;
}

//=============================================================================
// METHOD    : SPELLlog::enableLog()
//=============================================================================
void SPELLlog::enableLog( bool enable )
{
    SPELLmonitor m(m_logMutex);
	m_enabled = enable;
}

//=============================================================================
// METHOD    : SPELLlog::enableTraces()
//=============================================================================
void SPELLlog::enableTraces( bool enable )
{
    SPELLmonitor m(m_logMutex);
	m_traceEnabled = enable;
}

//=============================================================================
// METHOD    : SPELLlog::log
//=============================================================================
void SPELLlog::logInternal( const std::string& location, const std::string& msg, const LogSeverity& sev, const LogLevel& lev )
{
    SPELLmonitor m(m_logInternalMutex);
	if (!m_enabled) return;
    if (sev == LOG_WARN || sev == LOG_ERROR || (lev <= m_maxLevel))
    {
        std::string timestr = "[" + SPELLutils::timestamp() + "]";
        std::string sevstr = LOG_SEVERITY_STR[sev];
        std::string levstr = LOG_LEVEL_STR[lev];
        m_logFile << originStr(location) << "\t" << sevstr << "\t" << timestr
                  << "\t" << msg << std::endl;
        std::cout << originStr(location) << "  " << sevstr << "  " << timestr
                  << "  " << msg << std::endl;
    }
}

//=============================================================================
// METHOD    : SPELLlog::trace
//=============================================================================
void SPELLlog::trace( const std::string& location, const std::string& msg )
{
    SPELLmonitor m(m_logInternalMutex);
    std::istringstream iss(msg);
    std::string line;

	if (!m_traceEnabled) return;
    std::string timestr = "[" + SPELLutils::timestamp() + "]";
    std::string sevstr = "[ DEBUG ]";
    std::string levstr = "[ TRAC ]";
    std::string origin = originStr(location);


    while(getline(iss, line, '\n'))
    {
        if(line != "")
        {
            m_logFile << origin << "\t" << sevstr << "\t" << timestr
                      << "\t" << line << std::endl;
            std::cout << origin << "  " << sevstr << "  " << timestr
                      << "  " << line << std::endl;
        }
    }
}

//=============================================================================
// METHOD    : SPELLlog::log
//=============================================================================
void SPELLlog::log( const std::string& msg, LogSeverity sev, LogLevel lev )
{
	if (!m_enabled) return;
    if (sev == LOG_WARN || sev == LOG_ERROR || (lev <= m_maxLevel))
    {
        std::string timestr = "[" + SPELLutils::timestamp() + "]";
        std::string sevstr = LOG_SEVERITY_STR[sev];
        std::string levstr = LOG_LEVEL_STR[lev];
        std::string origin = "[       PYTHON       ]";
        {
            SPELLmonitor m(m_logMutex);
            m_logFile << origin << "\t" << sevstr << "\t" << timestr
                      << "\t" << msg << std::endl;
        }
    }
}

