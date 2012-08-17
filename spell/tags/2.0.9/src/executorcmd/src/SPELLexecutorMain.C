// ################################################################################
// FILE       : SPELLexecutorMain.C
// DATE       : Mar 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL command line executor main program
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

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// System includes -----------------------------------------------------------
// Local includes ------------------------------------------------------------
// Project includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_EXC/SPELLinterpreter.H"
#include "SPELL_CIFC/SPELLstandaloneCif.H"
#include "SPELL_CIFC/SPELLautomaticCif.H"


// GLOBALS ///////////////////////////////////////////////////////////////////

// Initialization/configuration variables
static int warm = 0;
static int recover = 0;
static int script = 0;
static int interactive = 0;
static int logging = 0;
static std::string context = "";
static std::string configFile = "";
static std::string procId  = "";
static std::string persis  = "";

// For POST
char* program_compilation_time = __DATE__ " " __TIME__;
// STATIC ////////////////////////////////////////////////////////////////////


//============================================================================
// Show usage
//============================================================================
void usage( char** argv )
{
    std::cerr << "Syntax:" << std::endl;
    std::cerr << "    " << argv[0] << " {-p <procedure id>|-f <script file>} -c <config file> -n <context> [-w|-r <persis file>] [-i] [-d]" << std::endl;
    std::cerr << std::endl;
    std::cerr << "         - i : interactive mode" <<  std::endl;
    std::cerr << "         - d : enable logging" <<  std::endl;
    std::cerr << "         - p : identifier of a SPELL procedure" <<  std::endl;
    std::cerr << "         - f : arbitrary script file name" <<  std::endl;
    std::cerr << "         - n : context identifier" <<  std::endl;
    std::cerr << "         - c : configuration file" <<  std::endl;
    std::cerr << "         - w : use warmstart capabilities" <<  std::endl;
    std::cerr << "         - r : recover execution using the given persistent file" <<  std::endl;
    std::cerr << std::endl;
    std::cerr << "Options p/s and w/r are mutually exclusive." << std::endl << std::endl;
}

//============================================================================
// Parse program arguments
//============================================================================
int parseArgs( int argc, char** argv )
{
    int code;
    while( ( code = getopt(argc, argv, "iwdp:f:n:c:r:")) != -1)
    {
        switch(code)
        {
        case 'i':
            interactive = 1;
            break;
        case 'd':
            logging = 1;
            break;
        case 'w':
            warm = 1;
            std::cout << "* Enable warmstart" << std::endl;
            break;
        case 'f':
            script = 1;
            procId = std::string(optarg);
            std::cout << "* Run script " << procId << std::endl;
            break;
        case 'r':
            recover = 1;
            persis = std::string(optarg);
            std::cout << "* Enable recovery with file " << persis << std::endl;
            break;
        case 'p':
            script = 0;
            procId = std::string(optarg);
            std::cout << "* Run procedure " << procId << std::endl;
            break;
        case 'n':
            context = std::string(optarg);
            break;
        case 'c':
            configFile = std::string(optarg);
            break;
        }
    }
    // We need proc id and context at least
    if (procId == "")
    {
        std::cerr << "Error: procedure/script and/or context identifier not provided" << std::endl;
        usage(argv);
        return 1;
    }
    if (configFile == "")
    {
        std::cerr << "Error: configuration file not provided" << std::endl;
        usage(argv);
        return 1;
    }
    if (warm == 1 && recover == 1)
    {
        std::cerr << "Error: cannot use w and r options at the same time" << std::endl;
        usage(argv);
        return 1;
    }
    return 0;
}

//============================================================================
// MAIN PROGRAM
//============================================================================
int main( int argc, char** argv )
{
    if ( parseArgs(argc,argv) != 0 ) return 1;

    showEnvironment();

    SPELLinterpreter& interp = SPELLinterpreter::instance();

    std::string timeId = fileTimestamp();

    if (logging)
    {
        SPELLlog::instance().setLogFile(procId,timeId);
    }
    else
    {
        SPELLlog::instance().enableLog(false);
        SPELLlog::instance().enableTraces(false);
    }

    SPELLinterpreterConfig config;
    config.warmstart   = (warm == 1);
    config.recover     = (recover == 1);
    config.script      = (script == 1);
    config.configFile  = configFile;
    config.procId 	   = procId;
    config.ctxName     = context;
    config.ctxPort     = 0;
    config.timeId      = timeId;
    config.persis      = persis;

    SPELLcif* cif = NULL;
    if (interactive)
    {
        cif = new SPELLstandaloneCif();
    }
    else
    {
        cif = new SPELLautomaticCif();
    }

    interp.initialize( config, cif );

    interp.mainLoop();

    return 0;
}
