// ################################################################################
// FILE       : SPELLconfiguration.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the configuration reader
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
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_CFG/SPELLxmlConfigReaderFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"


// GLOBALS /////////////////////////////////////////////////////////////////
// Holds the singleton instance
static SPELLconfiguration* s_instance = 0;

//=============================================================================
// CONSTRUCTOR : SPELLconfiguration::SPELLconfiguration()
//=============================================================================
SPELLconfiguration::SPELLconfiguration()
    : m_contexts()
{
    m_reader = SPELLxmlConfigReaderFactory::createXMLConfigReader();
    m_fileName = "";
}

//=============================================================================
// DESTRUCTOR : SPELLconfiguration::~SPELLconfiguration
//=============================================================================
SPELLconfiguration::~SPELLconfiguration()
{
    if (m_contexts.size()>0)
    {
        std::map<std::string, SPELLcontextConfig*>::iterator it;
        std::map<std::string, SPELLcontextConfig*>::iterator end = m_contexts.end();
        for( it = m_contexts.begin(); it != end; it++)
        {
            delete (*it).second;
        }
        m_contexts.clear();
    }
    if (m_reader != NULL)
    {
        delete m_reader;
        m_reader = NULL;
    }
    m_contextConfig.clear();
    m_listenerConfig.clear();
    m_executorsConfig.clear();
}

//=============================================================================
// METHOD    : SPELLconfiguration::instance()
//=============================================================================
SPELLconfiguration& SPELLconfiguration::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLconfiguration();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadConfig
//=============================================================================
void SPELLconfiguration::loadConfig( std::string fileName )
{
    LOG_INFO("[CFG] Loading configuration from " + fileName);
    m_reader->parseFile( fileName );
    m_fileName = fileName;
    m_contextConfig.clear();
    m_listenerConfig.clear();
    m_executorsConfig.clear();

    SPELLxmlNodeList nodes = m_reader->findElementsByName( XMLTags::TAG_CONTEXTS_SECTION );

    std::list<std::string> contextFiles;
    for( SPELLxmlNodeList::iterator it = nodes.begin(); it!=nodes.end(); it++)
    {
        if ((*it)->hasChildren())
        {
            SPELLxmlNodeList children = (*it)->getChildren();
            for( SPELLxmlNodeList::iterator cit = children.begin(); cit!=children.end(); cit++)
            {
                if ( (*cit)->getName() == XMLTags::TAG_CONTEXT )
                {
                    std::string contextFile = (*cit)->getValue();
                    contextFiles.push_back(contextFile);
                    LOG_INFO("[CFG] Found context configuration: " + contextFile);
                }
            }
        }
    }
    std::string baseName = basePath( basePath( fileName ) );
    for( std::list<std::string>::iterator it = contextFiles.begin(); it != contextFiles.end(); it++)
    {
        loadContextConfiguration( baseName + + PATH_SEPARATOR + Locations::CONTEXT_DIR + PATH_SEPARATOR + (*it) );
    }

    // Get server specific configuration for contexts
    loadSpecificConfiguration( XMLTags::TAG_CONTEXT_SECTION, m_contextConfig );

    // Get server specific configuration for executors
    loadSpecificConfiguration( XMLTags::TAG_EXECUTOR_SECTION, m_executorsConfig );

    // Get server specific configuration for listener
    loadSpecificConfiguration( XMLTags::TAG_LISTENER_SECTION, m_listenerConfig );

    dealloc_list(nodes);
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadSpecificConfiguration
//=============================================================================
void SPELLconfiguration::loadSpecificConfiguration( std::string section, Properties& properties )
{
    SPELLxmlNodeList sectionNodes = m_reader->findElementsByName( section );
    SPELLxmlNode* node = *(sectionNodes.begin());
    SPELLxmlNodeList children = node->getChildren();
    for( SPELLxmlNodeList::iterator nit = children.begin(); nit != children.end(); nit++)
    {
        if ((*nit)->getName() == XMLTags::TAG_PROPERTY)
        {
            std::string pname = (*nit)->getAttributeValue(XMLTags::TAG_ATTR_NAME);
            std::string pvalue = (*nit)->getValue();
            properties.insert( std::make_pair( pname, pvalue ));
        }
    }

    dealloc_list(sectionNodes);
    dealloc_list(children);
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadContextConfiguration
//=============================================================================
void SPELLconfiguration::loadContextConfiguration( std::string contextFile )
{
    try
    {
        SPELLcontextConfig* context = new SPELLcontextConfig( contextFile );
        m_contexts[ context->getName() ] = context;
    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[CFG] Unable to read context configuration from " + contextFile)
        LOG_ERROR("[CFG] Read error: " + STR(ex.what()))
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::getContext
//=============================================================================
SPELLcontextConfig& SPELLconfiguration::getContext( std::string ctxName )
{
    if (m_contexts.find(ctxName) == m_contexts.end())
    {
        throw SPELLcoreException("Cannot find context " + ctxName, "No such context");
    }
    return *m_contexts[ctxName];
}

//=============================================================================
// METHOD    : SPELLconfiguration::getListenerParameter
//=============================================================================
std::string SPELLconfiguration::getListenerParameter( std::string parameter )
{
    Properties::iterator it = m_listenerConfig.find(parameter);
    if ( it != m_listenerConfig.end())
    {
        return (*it).second;
    }
    else
    {
        return "";
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::getContextParameter
//=============================================================================
std::string SPELLconfiguration::getContextParameter( std::string parameter )
{
    Properties::iterator it = m_contextConfig.find(parameter);
    if ( it != m_contextConfig.end())
    {
        return (*it).second;
    }
    else
    {
        return "";
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::getExecutorParameter
//=============================================================================
std::string SPELLconfiguration::getExecutorParameter( std::string parameter )
{
    Properties::iterator it = m_executorsConfig.find(parameter);
    if ( it != m_executorsConfig.end())
    {
        return (*it).second;
    }
    else
    {
        return "";
    }
}

//=============================================================================
// METHOD    : SPELLconfiguration::loadPythonConfig
//=============================================================================
void SPELLconfiguration::loadPythonConfig( std::string fileName )
{
    DEBUG("[CFG] Importing python config module")
    PyObject* config = SPELLpythonHelper::instance().getObject( "spell.config.reader", "Config" );
    DEBUG("[CFG]    - Getting instance")
    PyObject* instance = SPELLpythonHelper::instance().callMethod( config, "instance", NULL );
    DEBUG("[CFG]    - Loading configuration on python side")
    SPELLpythonHelper::instance().callMethod( instance, "load", SSTRPY(fileName), NULL);
    DEBUG("[CFG]    -Done")
}
