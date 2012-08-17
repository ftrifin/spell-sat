//============================================================================
// Name        : SPELLlistenerMain.C
// Author      : Fabien Bouleau (SES)
// Version     :
// Copyright   : This is a copyrighted work of SES Astra (c) 2009
// Description : Listener main program
//============================================================================

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLutils.H"
// Local includes ----------------------------------------------------------
#include "SPELL_LST/SPELLlistener.H"

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
    std::cerr << "    " << argv[0] << " -s <port> -c <config> [-w]" << std::endl;
    std::cerr << std::endl;
    std::cerr << "         - c : configuration file" <<  std::endl;
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
        case 'c':
            configFile = std::string(optarg);
        case 's':
            port = atoi(optarg);
            break;
        }
    }
    if (port == 0)
    {
        std::cerr << "Error: no listener port provided" << std::endl;
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

    //std::string timeId = timestamp();

//	SPELLcontextParameters configParameters;
//	config.listenerPort = port;
//	config.name = context;
//	config.port = 0;
//	config.timeId = timeId;
//	config.warmstart = (warm == 1);
//	config.configFile = configFile;
//
//	SPELLlog::instance().setLogFile("Context_" + context,timeId);
//
//	SPELLcontext::instance().configure( configParameters );

    return 0;
}
