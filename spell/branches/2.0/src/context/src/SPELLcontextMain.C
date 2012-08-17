// ################################################################################
// FILE       : SPELLcontextMain.C
// DATE       : Mar 18, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL context main program
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
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLcontext.H"
#include "SPELL_CTX/SPELLcontextParameters.H"

// GLOBALS ///////////////////////////////////////////////////////////////////

// Initialization/configuration variables
static int warm = 0;
static int port = 0;
static std::string configFile = "";
static std::string context = "";

// For POST
char* program_compilation_time = __DATE__ " " __TIME__;
// STATIC ////////////////////////////////////////////////////////////////////


//============================================================================
// Show usage
//============================================================================
void usage( char** argv )
{
    std::cerr << "Syntax:" << std::endl;
    std::cerr << "    " << argv[0] << " -n <ctx name> -s <port> -c <config> [-w]" << std::endl;
    std::cerr << std::endl;
    std::cerr << "         - c : configuration file" <<  std::endl;
    std::cerr << "         - n : context identifier" <<  std::endl;
    std::cerr << "         - s : listener port" <<  std::endl;
    std::cerr << "         - w : use warmstart capabilities" <<  std::endl;
    std::cerr << std::endl;
}


//============================================================================
// Parse program arguments
//============================================================================
int parseArgs( int argc, char** argv )
{
    int code;
    while( ( code = getopt(argc, argv, "wn:c:s:")) != -1)
    {
        switch(code)
        {
        case 'w':
            warm = 1;
            std::cout << "* Enable warmstart" << std::endl;
            break;
        case 'n':
            context = std::string(optarg);
            std::cout << "* Name: " << context << std::endl;
            break;
        case 'c':
            configFile = std::string(optarg);
            break;
        case 's':
            port = atoi(optarg);
            std::cout << "* Port: " << port << std::endl;
            break;
        }
    }
    // We need proc id and context at least
    if (context == "")
    {
        std::cerr << "Error: context name not provided" << std::endl;
        usage(argv);
        return 1;
    }
    if (port == 0)
    {
        std::cerr << "Error: no context port provided" << std::endl;
        usage(argv);
        return 1;
    }
    if (configFile == "")
    {
        std::cerr << "Error: configuration file not provided" << std::endl;
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

    std::string timeId = timestamp();

    SPELLcontextParameters configParameters;
    configParameters.listenerPort = port;
    configParameters.name = context;
    configParameters.port = 0;
    configParameters.timeId = timeId;
    configParameters.warmstart = (warm == 1);
    configParameters.configFile = configFile;

    SPELLlog::instance().setLogFile("Context_" + context,timeId);
    SPELLlog::instance().setProcessName("[         CONTEXT        ]");
    SPELLlog::instance().enableLog(true);
    SPELLlog::instance().enableTraces(true);

    SPELLcontext::instance().configure( configParameters );

    SPELLcontext::instance().start();

    return 0;
}
