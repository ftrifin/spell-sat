// ################################################################################
// FILE       : SPELLipcTestClient.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Test program for IPC clients
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
#include "SPELL_SYN/SPELLthread.H"
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcHelper.H"
#include "SPELL_IPC/SPELLipcClientInterface.H"
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
        std::cout << "request received" << std::endl;
        return SPELLipcHelper::createResponse("dummy", msg );
    };

    void processError( std::string error, std::string reason )
    {
        std::cout << "error" << std::endl;
    };
};

//============================================================================
// Test class
//============================================================================
class Sender : public SPELLthread
{
public:
    Sender( SPELLipcClientInterface* clt ) : SPELLthread("sender") {
        m_clt = clt;
    };

    void run()
    {
        try
        {
            int n = 0;
            while(n<1000)
            {
                SPELLipcMessage* msg = new SPELLipcMessage("dummy");
                std::cout << "send request " << n << std::endl;
                msg->set("NUM", ISTR(n));
                msg->setType(MSG_TYPE_REQUEST);
                SPELLipcMessage* resp = m_clt->sendRequest(msg);
                std::cout << "received response " << n << ":" << resp->get("NUM") << std::endl;
                n++;
            }
        }
        catch(SPELLcoreException& ex)
        {
            std::cerr << "SEND ERROR: " << ex.what() << std::endl;
        }
    };
private:
    SPELLipcClientInterface* m_clt;
};

int main( int argc, char** argv )
{
    if ( parseArgs(argc,argv) != 0 ) return 1;

    Proxy proxy;
    SPELLipcClientInterface client("CLT", "localhost", port );
    Sender sender( &client );

    try
    {
        std::cout << "initializing" << std::endl;
        client.initialize(&proxy);

        std::cout << "connecting" << std::endl;
        client.connectIfc();
        std::cout << "starting interface" << std::endl;
        client.start();

        usleep(10000);

        std::cout << "start sending" << std::endl;
        sender.start();
        sender.join();
        std::cout << "end sending" << std::endl;

        client.disconnect(true);

    }
    catch(SPELLcoreException& ex)
    {
        std::cerr << "ERROR: " << ex.what() << std::endl;
    }

    return 0;
}


