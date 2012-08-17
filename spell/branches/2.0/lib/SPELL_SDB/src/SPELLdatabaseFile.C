// ################################################################################
// FILE       : SPELLdatabaseFile.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of database based on local files
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
#include "SPELL_SDB/SPELLdatabaseFile.H"
// System includes ---------------------------------------------------------
#include <algorithm>

// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////
// GLOBALS /////////////////////////////////////////////////////////////////

//=============================================================================
// CONSTRUCTOR: SPELLdatabaseFile::SPELLdatabaseFile()
//=============================================================================
SPELLdatabaseFile::SPELLdatabaseFile( const std::string& name, const std::string& filename, const std::string& defExt )
: SPELLdatabase(name,filename,defExt)
{
	std::string dataHomeStr = "";
	char* dataHome = getenv("SPELL_DATA");
	if (dataHome == NULL)
	{
		dataHome = getenv("SPELL_HOME");
		if (dataHome == NULL)
		{
			throw SPELLdatabaseError("SPELL_HOME environment variables not defined");
		}
		dataHomeStr = dataHome + std::string(PATH_SEPARATOR) + "data";
	}
	else
	{
		dataHomeStr = dataHome;
	}

	std::string thePath = dataHomeStr + PATH_SEPARATOR + m_filename;
	std::size_t pos = filename.find_last_of(".");
	if (pos == std::string::npos)
	{
		thePath += "." + m_defaultExt;
	}
	m_filename = thePath;
}

//=============================================================================
// DESTRUCTOR: SPELLdatabaseFile::~SPELLdatabaseFile()
//=============================================================================
SPELLdatabaseFile::~SPELLdatabaseFile()
{
}

//=============================================================================
// METHOD: SPELLdatabaseFile::create()
//=============================================================================
void SPELLdatabaseFile::create()
{
	std::ofstream file;
	file.open( m_filename.c_str(), std::ios::out);
	if (!file.is_open())
	{
		throw SPELLdatabaseError("Unable to create database file '" + m_filename + "'");
	}
	file.close();
}

//=============================================================================
// METHOD: SPELLdatabaseFile::load()
//=============================================================================
void SPELLdatabaseFile::load()
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
    std::string lineToProcess = "";
    unsigned int count = 0;
    for( it = lines.begin(); it != lines.end(); it++)
    {
    	count++;
    	// If there is something in the buffer, append the next line
    	if (lineToProcess != "")
    	{
    		// But remove backslash and spaces first
    		lineToProcess = lineToProcess.substr(0, lineToProcess.size()-1);
        	trim(lineToProcess);
        	std::string line = *it;
        	trim(line);
        	lineToProcess += line;
    	}
    	// Otherwise add the current line
    	else
    	{
        	std::string line = *it;
        	trim(line);
    		lineToProcess = line;
    	}

    	// Remove \r
    	lineToProcess.erase(std::remove( lineToProcess.begin(), lineToProcess.end(), '\r'),lineToProcess.end());

    	// Remove spaces
    	trim(lineToProcess);

    	// Ignore empty lines
    	if (lineToProcess == "") continue;

    	// Ignore comment lines
    	if (lineToProcess.find("#")==0)
    	{
    		lineToProcess = "";
    		continue;
    	}

    	// If the line ends with backslash we need to concatenate with next line
    	if (lineToProcess.at(lineToProcess.size()-1) == '\\')
    	{
    		continue;
    	}

    	// Replace tabs
    	replace(lineToProcess,"\t"," ");

    	// Now process line data
    	std::vector<std::string> tokens = tokenized(lineToProcess," ");
    	std::string origValue = "";
    	for (unsigned int index=1; index<tokens.size(); index++)
    	{
    		if (origValue != "") origValue += " ";
    		origValue += tokens[index];
    	}
    	std::string key = tokens[0];

    	// Remove spaces
    	trim(key);

    	if (key == "")
    	{
        	std::cerr << "ERROR: bad key on dictionary " + m_name + ", line " << count << std::endl;
        	lineToProcess = "";
    		continue;
    	}

    	PyObject* value = NULL;
    	//std::string vtype = "";
    	value = importValue(origValue);

    	if (value != NULL)
    	{
    		m_values.insert( std::make_pair(key,value) );
    		//TODO if types needed to commit: m_types.insert( std::make_pair(key,vtype));
    	}
    	else
    	{
    		std::cerr << "ERROR: unable to resolve value on line " << count << " in " << m_name << std::endl;
    	}
    	lineToProcess = "";
    }
}

//=============================================================================
// METHOD: SPELLdatabaseFile::importValue()
//=============================================================================
PyObject* SPELLdatabaseFile::importValue( const std::string& origValue )
{
	PyObject* pyOrigValue = SSTRPY(origValue);
	Py_INCREF(pyOrigValue);
	PyObject* result = SPELLpythonHelper::instance().callFunction("spell.utils.vimport", "ImportValue", pyOrigValue, NULL );
	Py_XDECREF(pyOrigValue);
	if (result != NULL)
	{
		return PyList_GetItem(result,0);
	}
	else
	{
		return NULL;
	}
}

//=============================================================================
// METHOD: SPELLdatabaseFile::reload()
//=============================================================================
void SPELLdatabaseFile::reload()
{
	m_values.clear();
	load();
}

//=============================================================================
// METHOD: SPELLdatabaseFile::id()
//=============================================================================
std::string SPELLdatabaseFile::id()
{
	return m_filename;
}

//=============================================================================
// METHOD: SPELLdatabaseFile::commit()
//=============================================================================
void SPELLdatabaseFile::commit()
{

}
