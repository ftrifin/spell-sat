// ################################################################################
// FILE       : SPELLprocedureManager.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of procedure manager
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
#include "SPELL_PRD/SPELLprocedureManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"



static SPELLprocedureManager* s_instance = 0;

//=============================================================================
// CONSTRUCTOR : SPELLprocedureManager::SPELLprocedureManager()
//=============================================================================
SPELLprocedureManager::SPELLprocedureManager()
{
    m_ctxName = "";
    m_procPath = "";
    m_libPath = "";
}

//=============================================================================
// DESTRUCTOR : SPELLprocedureManager::~SPELLprocedureManager
//=============================================================================
SPELLprocedureManager::~SPELLprocedureManager()
{
    m_idToFilename.clear();
    m_filenameToId.clear();
    m_idToName.clear();
    m_nameToId.clear();
}

//=============================================================================
// METHOD    : SPELLprocedureManager::instance()
//=============================================================================
SPELLprocedureManager& SPELLprocedureManager::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLprocedureManager();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::setup
//=============================================================================
void SPELLprocedureManager::setup( const std::string& ctxName )
{
	DEBUG("[PRCM] Setup for context " + ctxName);
    m_ctxName = ctxName;
    SPELLcontextConfig& ctx = SPELLconfiguration::instance().getContext(ctxName);
    m_procPath = ctx.getProcPath();
    m_libPath = ctx.getLibPath();
    // Resolve the path
    m_procPath = resolvePath(m_procPath);
    m_libPath = resolvePath(m_libPath);
    trim(m_procPath, "\r\n");
    trim(m_libPath, "\r\n");
	DEBUG("[PRCM] Using procpath " + m_procPath)
	DEBUG("[PRCM] Using libpath " + m_libPath)
    if (!isDirectory(m_procPath)) throw SPELLcoreException("Cannot setup procedure manager", "Procedure path not found or not a directory: " + m_procPath);
    if (m_libPath != "" && m_libPath != "None")
    {
        if (!isDirectory(m_libPath)) throw SPELLcoreException("Cannot setup procedure manager", "Library path not found or not a directory: " + m_libPath);
    }
    LOG_INFO("[PRCM] Procedure path: " + m_procPath);
    LOG_INFO("[PRCM] Library path: " + m_libPath);
    /** \todo get from config the expected procedure properties. The minimum set is name. */
    refresh();
}

//=============================================================================
// METHOD    : SPELLprocedureManager::refresh
//=============================================================================
void SPELLprocedureManager::refresh()
{
    DEBUG("[PRCM] Refreshing procedures");
    SPELLcontextConfig& ctx = SPELLconfiguration::instance().getContext(m_ctxName);
    std::string sc = ctx.getSC();
    m_idToFilename.clear();
    m_filenameToId.clear();
    m_idToName.clear();
    m_nameToId.clear();
    ProcModels::iterator it;
    for(it = m_models.begin(); it != m_models.end(); it++)
    {
        delete (*it).second;
    }
    m_models.clear();
    findProcedures( m_procPath, sc );
    if (m_libPath != "" && m_libPath != "None")
    {
        findLibraries( m_libPath );
    }
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getLibPath
//=============================================================================
const std::string SPELLprocedureManager::getLibPath() const
{
    return m_libPath;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcPath
//=============================================================================
const std::string SPELLprocedureManager::getProcPath() const
{
    return m_procPath;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcFile
//=============================================================================
const std::string SPELLprocedureManager::getProcFile( const std::string& procId )
{
    DEBUG("[PRCM] Get procedure file for ID: " + procId )
    std::string theProcId = noInstanceId(procId);
    DEBUG("[PRCM] ID without instance: " + theProcId )
    ProcMap::iterator it = m_idToFilename.find(theProcId);
    if (it == m_idToFilename.end())
    {
        DEBUG("[PRCM] Did not find any file for: " + theProcId )
        throw SPELLcoreException("Cannot get file for procedure/library '" + theProcId + "'", "No such identifier");
    }
    DEBUG("[PRCM] Found procedure file: " + it->second )
    return it->second;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcId
//=============================================================================
const std::string SPELLprocedureManager::getProcId( const std::string& filename )
{
    DEBUG("[PRCM] Get procedure ID for file: " + filename )
    ProcMap::iterator it = m_filenameToId.find(filename);
    if (it == m_filenameToId.end())
    {
        DEBUG("[PRCM] Did not find any ID")
        throw SPELLcoreException("Cannot get identifier for file '" + filename + "'", "No such file");
    }
    DEBUG("[PRCM] Found proc ID: " + it->second )
    return it->second;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcList
//=============================================================================
const SPELLprocedureManager::ProcList SPELLprocedureManager::getProcList()
{
    ProcList list;
    ProcMap::iterator it;
    for( it = m_idToName.begin(); it != m_idToName.end(); it++)
    {
        list.push_back( (*it).first + "|" + (*it).second );
    }
    return list;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getProcName
//=============================================================================
const std::string SPELLprocedureManager::getProcName( const std::string& procId )
{
    DEBUG("[PRCM] Get procedure name for ID: " + procId )
    std::string theProcId = noInstanceId(procId);
    DEBUG("[PRCM] ID without instance: " + theProcId )
    ProcMap::iterator it = m_idToName.find(theProcId);
    if (it == m_idToName.end())
    {
        DEBUG("[PRCM] Did not find any name for: " + theProcId )
        throw SPELLcoreException("Cannot get name for procedure '" + theProcId + "'", "No such identifier");
    }
    DEBUG("[PRCM] Found procedure name: " + it->second )
    return it->second;
}

//=============================================================================
// METHOD    : SPELLprocedureManager::findProcedures
//=============================================================================
void SPELLprocedureManager::findProcedures( const std::string& basePath, const std::string& sc )
{
    if (!isDirectory(basePath)) throw SPELLcoreException("Cannot find procedures at " + basePath, "Path not found or not a directory");

    std::list<std::string> files = getFilesInDir( basePath );
    std::list<std::string>::iterator it;
    std::list<std::string>::iterator end = files.end();

    std::string procPath = getProcPath();

    for( it = files.begin(); it != end; it++ )
    {
        if ((*it) == "__init__.py") continue;
        std::size_t idx = (*it).find(".py");
        if (idx != std::string::npos && idx > 0 )
        {
            std::string procFile = basePath + PATH_SEPARATOR + (*it);
            SPELLprocedure* proc = NULL;
            try
            {
                DEBUG("[PRCM] Creating model for " + procFile )
                proc = new SPELLprocedure( procPath, procFile );
                DEBUG("[PRCM] Adding model for " + proc->getProcId() )
                m_models.insert( std::make_pair( proc->getProcId(), proc ));
                m_idToName.insert( std::make_pair( proc->getProcId(), proc->getName() ));
                m_nameToId.insert( std::make_pair( proc->getName(), proc->getProcId() ));
                m_filenameToId.insert( std::make_pair( procFile, proc->getProcId() ));
                m_idToFilename.insert( std::make_pair( proc->getProcId(), procFile ));
            }
            catch(SPELLcoreException& ex)
            {
                if (proc != NULL) delete proc;
                LOG_ERROR("[PRC] Unable to parse procedure: " + procFile)
            }
        }
    }

    // Add the current path to python path
    if (SPELLpythonHelper::instance().isInitialized())
    {
        SPELLpythonHelper::instance().addToPath( basePath );
    }

    std::list<std::string> subdirs = getSubdirs( basePath );
    end = subdirs.end();
    for( it = subdirs.begin(); it != end; it++ )
    {
        findProcedures( basePath + PATH_SEPARATOR + (*it), sc );
    }
}

//=============================================================================
// METHOD    : SPELLprocedureManager::findLibraries
//=============================================================================
void SPELLprocedureManager::findLibraries( const std::string& basePath )
{
    if (!isDirectory(basePath)) throw SPELLcoreException("Cannot find libraries at " + basePath, "Path not found or not a directory");

    std::list<std::string> files = getFilesInDir( basePath );
    std::list<std::string>::iterator it;
    std::list<std::string>::iterator end = files.end();

    for( it = files.begin(); it != end; it++ )
    {
        if ((*it) == "__init__.py") continue;
        std::size_t idx = (*it).find_last_of(".py");
        if (idx != std::string::npos )
        {
            std::string libFile = basePath + PATH_SEPARATOR + (*it);
            std::string libId = (*it).substr(0,idx-2);
            DEBUG("[PRCM] Adding library mapping for " + libId )
            // Just append the library file-id mappings
            m_filenameToId.insert( std::make_pair( libFile, libId ));
            m_idToFilename.insert( std::make_pair( libId, libFile ));
        }
    }

    std::list<std::string> subdirs = getSubdirs( basePath );
    end = subdirs.end();
    for( it = subdirs.begin(); it != end; it++ )
    {
        findLibraries( basePath + PATH_SEPARATOR + (*it) );
    }
}

//=============================================================================
// METHOD    : SPELLprocedureManager::getSourceCode
//=============================================================================
SPELLprocedureSourceCode SPELLprocedureManager::getSourceCode( const std::string& procId )
{
    ProcModels::iterator it;
    std::string theProcId = noInstanceId(procId);
    it = m_models.find(theProcId);
    if (it == m_models.end()) throw SPELLcoreException("Cannot get source code for " + theProcId, "Procedure not found");
    SPELLprocedure* proc = (*it).second;
    proc->refresh();
    return proc->getSourceCode();
}

//=============================================================================
// METHOD    : SPELLprocedureManager::noInstanceId
//=============================================================================
const std::string SPELLprocedureManager::noInstanceId( const std::string& procId )
{
    std::string noIId = procId;
    std::size_t pos = procId.find_first_of("#");
    if (pos != std::string::npos )
    {
        noIId = procId.substr(0, pos);
    }
    return noIId;
}
