// ################################################################################
// FILE       : SPELLipcTestServer.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Test program for IPC server
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLerror.H"
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipcServerInterface.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"
// System includes ---------------------------------------------------------

// GLOBALS /////////////////////////////////////////////////////////////////

// For POST
char* program_compilation_time = __DATE__ " " __TIME__;

static int port = -1;

//============================================================================
// Show usage
//============================================================================
void usage( char** argv )
{
    std::cerr << "Syntax:" << std::endl;
    std::cerr << "    " << argv[0] << " -p <port>" << std::endl;
    std::cerr << std::endl;
}

//============================================================================
// Parse program arguments
//============================================================================
int parseArgs( int argc, char** argv )
{
    int code;
    while( ( code = getopt(argc, argv, "p:")) != -1)
    {
        switch(code)
        {
        case 'p':
            port = atoi(optarg);
            break;
        }
    }
    if (port == -1)
    {
        std::cerr << "Error: port not provided" << std::endl;
        usage(argv);
        return 1;
    }
    return 0;
}

//============================================================================
// Test class
//============================================================================
class Proxy : public SPELLipcInterfaceListener
{
public:
    Proxy() : SPELLipcInterfaceListener() {
        ;
    };
    void processMessage( SPELLipcMessage* msg )
    {
        std::cout << "message received" << std::endl;
    };

    SPELLipcMessage* processRequest( SPELLipcMessage* msg )
    {
        SPELLipcMessage* resp = SPELLipcHelper::createResponse("dummy", msg);
        try
        {
            std::cout << "request received from " << msg->getKey() << ": " << msg->get("NUM") << std::endl;
            resp->setId("resp");
            resp->setType(MSG_TYPE_RESPONSE);
            resp->set("NUM", msg->get("NUM"));
            usleep(14000);
        }
        catch(SPELLcoreException& ex)
        {
            std::cerr << "PROCESS ERROR: " << ex.what() << std::endl;
        }
        return resp;
    };

    void processError( std::string error, std::string reason )
    {
        std::cout << "error" << std::endl;
    };
};

int main( int argc, char** argv )
{
    if ( parseArgs(argc,argv) != 0 ) return 1;

    Proxy proxy;
    SPELLipcServerInterface server("SRV", 999, port );

    try
    {
        std::cout << "initializing" << std::endl;
        server.initialize(&proxy);
        server.connectIfc();
        std::cout << "starting interface" << std::endl;
        server.start();
        std::cout << "ready" << std::endl;
        server.join();
    }
    catch(SPELLcoreException& ex)
    {
        std::cerr << "ERROR: " << ex.what() << std::endl;
    }

    return 0;
}


