// ################################################################################
// FILE       : SPELLdatabaseFileSPB.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation based on local SPB files
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
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// Local includes ----------------------------------------------------------
#include "SPELL_SDB/SPELLdatabaseFileSPB.H"
// System includes ---------------------------------------------------------
#include <algorithm>

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLdatabaseFileSPB::SPELLdatabaseFileSPB()
//=============================================================================
SPELLdatabaseFileSPB::SPELLdatabaseFileSPB( const std::string& name, const std::string& filename, const std::string& defExt )
: SPELLdatabaseFile(name,filename,defExt)
{
}

//=============================================================================
// DESTRUCTOR: SPELLdatabaseFileSPB::~SPELLdatabaseFileSPB()
//=============================================================================
SPELLdatabaseFileSPB::~SPELLdatabaseFileSPB()
{
}

//=============================================================================
// METHOD: SPELLdatabaseFileSPB::load()
//=============================================================================
void SPELLdatabaseFileSPB::load()
{
    std::ifstream file;
    file.open( m_filename.c_str(), std::ios::in );
    if (!file.is_open())
    {
        throw SPELLdatabaseError("Cannot open file '" + m_filename + "'");
    }
    std::vector<std::string> lines;
    while(!file.eof())
    {
        std::string line = "";
        std::getline(file,line);
        lines.push_back(line);
    }
    file.close();
    std::vector<std::string>::iterator it;

    for( it = lines.begin(); it != lines.end(); it++)
    {
    	std::string lineToProcess = *it;

    	// Remove spaces
    	trim(lineToProcess);

    	// Process only lines starting with $, and having :=
    	if ((lineToProcess.find("$")!=0)||(lineToProcess.find(":=")==std::string::npos))
    	{
    		continue;
    	}

    	// Replace tabs
    	replace(lineToProcess,"\t"," ");
    	// Remove \r
    	lineToProcess.erase(std::remove( lineToProcess.begin(), lineToProcess.end(), '\r'),lineToProcess.end());

    	// Now process line data
    	std::vector<std::string> tokens = tokenized(lineToProcess," ");
    	std::string origValue = "";
    	for (unsigned int index=1; index<tokens.size(); index++)
    	{
    		if (origValue != "") origValue += " ";
    		origValue += tokens[index];
    	}
    	std::string key = tokens[0];

    	PyObject* value = NULL;
    	//std::string vtype = "";
    	value = importValue(origValue);

    	if (value != NULL)
    	{
    		m_values.insert( std::make_pair(key,value) );
    		//TODO if types needed to commit: m_types.insert( std::make_pair(key,vtype));
    	}
    }
}
