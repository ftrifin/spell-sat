// ################################################################################
// FILE       : SPELLwsInspector.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Utility program to inspect and dump data from persistent files
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
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WS/SPELLwsStorage.H"
// System includes ---------------------------------------------------------

// GLOBALS ///////////////////////////////////////////////////////////////////

// Initialization variables
static std::string persisFile = "";

// STATIC ////////////////////////////////////////////////////////////////////

//============================================================================
// Show usage
//============================================================================
void usage( char** argv )
{
    std::cerr << "Syntax:" << std::endl;
    std::cerr << "    " << argv[0] << " -f <persistent wsp file>" << std::endl;
    std::cerr << std::endl;
    std::cerr << "         - f : persistent wsp file" <<  std::endl;
    std::cerr << std::endl;
}

//============================================================================
// Parse program arguments
//============================================================================
int parseArgs( int argc, char** argv )
{
    int code;
    while( ( code = getopt(argc, argv, "f:")) != -1)
    {
        switch(code)
        {
        case 'f':
            persisFile = std::string(optarg);
            std::cout << "* Reading persistent file " << persisFile << std::endl;
            break;
        }
    }
    // We need proc id and context at least
    if (persisFile == "")
    {
        std::cerr << "Error: persistent file not provided" << std::endl;
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

    if (!SPELLutils::pathExists(persisFile))
    {
    	std::cerr << "ERROR: cannot find persistent file: '" << persisFile << "'" << std::endl;
    	return 1;
    }

    SPELLlog::instance().enableLog(false);
    SPELLlog::instance().enableTraces(false);

    SPELLpythonHelper::instance().initialize();

    SPELLwsStorage storageStatic(persisFile, SPELLwsStorage::MODE_READ );

    std::cout << "Main interpreter data ------------------------------" << std::endl;
	int recursion_depth = storageStatic.loadLong();
	std::cout << "    Recursion depth  : " + ISTR(recursion_depth) << std::endl;
	int tracing = storageStatic.loadLong();
	std::cout << "    Tracing flag     : " + ISTR(tracing) << std::endl;
	int use_tracing = storageStatic.loadLong();
	std::cout << "    Use tracing flag : " + ISTR(use_tracing) << std::endl;
	int tick_counter = storageStatic.loadLong();
	std::cout << "    Tick counter     : " + ISTR(tick_counter) << std::endl;
	int gilstate_counter = storageStatic.loadLong();
	std::cout << "    GIL state counter: " + ISTR(gilstate_counter) << std::endl;
	int numFrames = storageStatic.loadLong();
	std::cout << "    Number of frames : " + ISTR(numFrames) << std::endl;

	for(int index = 0; index<numFrames; index++)
	{
		std::cout << "Frame level " << index << " data ---------------------------------" << std::endl;
		std::string dynFile = persisFile.substr(0,persisFile.length()-4) + "_" + ISTR(index) + ".wsd";

	    if (!SPELLutils::pathExists(dynFile))
	    {
	    	std::cerr << "ERROR: cannot find persistent file: '" << dynFile << "'" << std::endl;
	    	return 1;
	    }

		SPELLwsStorage storageDynamic(dynFile, SPELLwsStorage::MODE_READ );
		int lineno = storageDynamic.loadLong();
		std::cout << "     Lineno     : " + ISTR(lineno) << std::endl;
		int lasti  = storageDynamic.loadLong();
		std::cout << "     Instruction: " + ISTR(lasti) << std::endl;
		int iblock = storageDynamic.loadLong();
		std::cout << "     IBlocks    : " + ISTR(iblock) << std::endl;
	}
	std::cout << "done." << std::endl;

    SPELLpythonHelper::instance().finalize();
    return 0;
}
