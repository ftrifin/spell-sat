// ################################################################################
// FILE       : SPELLserverFile.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the server file model
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
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_IPC/SPELLipc.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLserverFile.H"

//=============================================================================
// CONSTRUCTOR:  SPELLserverFile::SPELLserverFile
//=============================================================================
SPELLserverFile::SPELLserverFile( SPELLserverFileType type )
{
    m_type = type;
}

//=============================================================================
// METHOD:  SPELLserverFile::getLines
//=============================================================================
std::list<std::string> SPELLserverFile::getLines()
{
    return m_lines;
}

//=============================================================================
// METHOD:  SPELLserverFile::loadFromPath
//=============================================================================
void SPELLserverFile::loadFromPath( const std::string& filePath )
{
    DEBUG("[SRVF] Loading from path: " + filePath);
    std::ifstream file;
    file.open( filePath.c_str() );
    if (!file.is_open())
    {
        throw SPELLcoreException("Cannot parse file " + filePath, "Unable to open");
    }
    m_lines.clear();
    while(!file.eof())
    {
        std::string line = "";
        std::getline(file,line);
        m_lines.push_back(line);
    }
    DEBUG("[SRVF] Loaded " + ISTR(m_lines.size()) + " lines");
}

//=============================================================================
// METHOD:  SPELLserverFile::loadFromContents
//=============================================================================
void SPELLserverFile::loadFromContents( const std::string& contents )
{
    DEBUG("[SRVF] Loading from contents");
    m_lines.clear();
    std::vector<std::string> lines = tokenized( contents, "\r\n" );
    std::vector<std::string>::iterator it;
    std::vector<std::string>::iterator end = lines.end();
    for( it = lines.begin(); it != end; it++)
    {
        m_lines.push_back( (*it) );
    }
    DEBUG("[SRVF] Loaded " + ISTR(m_lines.size()) + " lines");
}

//=============================================================================
// STATIC:  SPELLserverFile::stringToType
//=============================================================================
SPELLserverFileType SPELLserverFile::stringToType( const std::string& type )
{
    static const std::string DATA_FILE_ASRUN("ASRUN");
    static const std::string DATA_FILE_EXEC_LOG("EXECUTOR_LOG");

    if (type == MessageValue::DATA_FILE_ASRUN) return FILE_ASRUN;
    if (type == MessageValue::DATA_FILE_EXEC_LOG) return FILE_LOG;
    return FILE_ANY;
}

