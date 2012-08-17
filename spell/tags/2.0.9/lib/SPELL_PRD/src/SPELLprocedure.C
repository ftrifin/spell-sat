// ################################################################################
// FILE       : SPELLprocedure.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of procedure model
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
#include "SPELL_PRD/SPELLprocedure.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"

#define KEY_NAME "NAME"


//=============================================================================
// CONSTRUCTOR : SPELLprocedure::SPELLprocedure()
//=============================================================================
SPELLprocedure::SPELLprocedure( const std::string& procPath, const std::string& filename )
: m_source(filename)
{
    // Remove the .py extension first, if any
    std::size_t pos = filename.find_last_of(".py");

    if (pos != std::string::npos )
    {
        m_procId = filename.substr(0,pos-2);
    }

    // The remove the proc base path, not the subfolders
    m_procId = m_procId.substr( procPath.size()+1, m_procId.size()-procPath.size()-1 );

    // Will be parsed
    m_name = "";

    // Parse the file to get properties and source code
    parseFile(filename);
}

//=============================================================================
// DESTRUCTOR : SPELLprocedure::~SPELLprocedure
//=============================================================================
SPELLprocedure::~SPELLprocedure()
{
    m_properties.clear();
}

//=============================================================================
// METHOD    : SPELLprocedure::parseFile
//=============================================================================
void SPELLprocedure::parseFile( const std::string& path )
{
    // Will be composed during parsing
    m_source.clear();

    std::ifstream file;
    file.open( path.c_str() );
    if (!file.is_open())
    {
        throw SPELLcoreException("Cannot parse file " + path, "Unable to open");
    }
    // Will be true while in comment lines
    bool isComment = false;
    // Will be true while inside the header
    bool inHeader = false;
    // Wil lbe true when finished parsing the header
    bool headerDone = false;

    // Stores the last key found
    std::string lastKey;
    while(!file.eof())
    {
        std::string line = "";
        std::getline(file,line);
        m_source.addSourceCodeLine(line);
        if ((line.size()>0)&&(!headerDone))
        {
            isComment = (line[0] == '#');
            if (isComment && (!inHeader))
            {
                // If it is a header delimiter line we are entering the header
                if (isLimitLine(line))
                {
                    inHeader = true;
                }
            }
            else if (isComment && inHeader)
            {
                // If we are in header and another limit line comes, we have finished
                if (isLimitLine(line))
                {
                    headerDone = true;
                }
                else if (isPropertyLine(line))
                {
                    // Remove the leading hash #
                    line = line.substr(1,line.size()-1);
                    // Split the line in key / value
                    std::vector<std::string> tokens = tokenize(line, ":");
                    lastKey = tokens[0];
                    std::string value;
                    if (tokens.size()==2)
                    {
                        value = tokens[1];
                    }
                    else
                    {
                        value = "<?>";
                    }
                    // Remove unwanted characters
                    trim(lastKey);
                    trim(value);
                    trim(lastKey, "\r\n");
                    trim(value, "\r\n");
                    m_properties.insert( std::make_pair(lastKey, value));
                }
                else if (isPropertyContinued(line))
                {
                    // This adds more lines to the multiline properties
                    line = line.substr(1,line.size()-1);
                    trim(line);
                    trim(line, "\r\n");
                    m_properties[lastKey] = (m_properties[lastKey] + " " + line);
                }
            }
        }
    }
    file.close();
    // If the name was not found in the properties, take the proc id as the procedure name
    std::map<std::string,std::string>::const_iterator it;
    it = m_properties.find(KEY_NAME);
    if (it == m_properties.end())
    {
        m_name = m_procId;
    }
    else
    {
        m_name = m_properties[KEY_NAME];
    }
}

//=============================================================================
// METHOD    : SPELLprocedure::isPropertyLine
//=============================================================================
const bool SPELLprocedure::isPropertyLine( const std::string& line )
{
    std::size_t pos = line.find_first_of(":");
    return ( (line[0] == '#' ) && ( pos != std::string::npos) );
}

//=============================================================================
// METHOD    : SPELLprocedure::isLimitLine
//=============================================================================
const bool SPELLprocedure::isLimitLine( const std::string& line )
{
    std::size_t pos = line.find_first_not_of("#\n\r");
    return ((pos == std::string::npos) && (line.size()>5));
}

//=============================================================================
// METHOD    : SPELLprocedure::isPropertyContinued
//=============================================================================
const bool SPELLprocedure::isPropertyContinued( const std::string& line )
{
    std::size_t pos = line.find_first_not_of("# \n\r");
    return ( (line[0] == '#' ) && (pos != std::string::npos));
}
