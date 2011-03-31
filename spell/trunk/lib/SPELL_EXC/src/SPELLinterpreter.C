// ################################################################################
// FILE       : SPELLinterpreter.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the custom interpreter
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
#include "SPELL_EXC/SPELLinterpreter.H"
#include "SPELL_EXC/SPELLgoto.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_EXC/SPELLcontroller.H"
#include "SPELL_EXC/SPELLscheduler.H"
#include "SPELL_EXC/SPELLcallstack.H"
#include "SPELL_EXC/SPELLexecutorImpl.H"
// Project includes --------------------------------------------------------
#include "SPELL_PRD/SPELLprocedureManager.H"
#include "SPELL_UTIL/SPELLpythonError.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLdriverManager.H"
#include "SPELL_WS/SPELLwsWarmStartImpl.H"
#include "SPELL_CIF/SPELLcif.H"


// GLOBALS /////////////////////////////////////////////////////////////////

// Interpreter singleton instance
static SPELLinterpreter* s_instance = NULL;

// Warmstart location name
#define WS_LOCATION "ws"

//=============================================================================
// CONSTRUCTOR: SPELLinterpreter::SPELLinterpreter
//=============================================================================
SPELLinterpreter::SPELLinterpreter()
{
	m_executor = NULL;
    m_cif = NULL;
    m_controller = NULL;
    m_scheduler = NULL;
    m_callstack = NULL;
    m_frame = NULL;
    m_warmStart = NULL;
    m_procPath = "";
    m_procedure = "";
}

//=============================================================================
// DESTRUCTOR: SPELLinterpreter::~SPELLinterpreter
//=============================================================================
SPELLinterpreter::~SPELLinterpreter()
{
    DEBUG("[*] Cleaning up")
    if (m_warmStart != NULL)
    {
        DEBUG("[*] Cleaning warm start mechanism")
        delete m_warmStart;
        m_warmStart = NULL;
    }
    if (m_callstack != NULL)
    {
        delete m_callstack;
        m_callstack = NULL;
    }
    if (m_controller != NULL)
    {
        delete m_controller;
        m_controller = NULL;
    }
    if (m_scheduler != NULL)
    {
        delete m_scheduler;
        m_scheduler = NULL;
    }
    if (m_executor != NULL)
    {
    	delete m_executor;
    	m_executor = NULL;
    }
    SPELLpythonHelper::instance().finalize();
    DEBUG("[*] End")
}

//=============================================================================
// METHOD    : SPELLinterpreter::instance
//=============================================================================
SPELLinterpreter& SPELLinterpreter::instance()
{
    if (s_instance == NULL)
    {
        s_instance = new SPELLinterpreter();
    }
    return *s_instance;
}

//=============================================================================
// METHOD    : SPELLinterpreter::initialize
//=============================================================================
void SPELLinterpreter::initialize( const SPELLinterpreterConfig& config, SPELLcif* cif )
{
    LOG_INFO("[*] Initializing interpreter")

    m_cif = cif;
    m_procedure = config.procId;
    m_config = config;
}

//=============================================================================
// METHOD    : SPELLinterpreter::mainLoop
//=============================================================================
void SPELLinterpreter::mainLoop()
{
    LOG_INFO("[***] Start main loop")

    // Create and connect all execution control objects
    if (prepareObjects())
    {
        // Now try to initialize (compile) the execution frame.
        if (prepareExecution())
        {
            // If we are in recovery mode:
            if (m_config.recover)
            {
                recover();
            }
            else // Or we are in running mode
            {
                execute();
            }
        }

        // After the execution cycle, wait for the user/server to autorize closure
        DEBUG("[***] Main loop waiting for CIF")
        m_cif->waitClose();

        // Once all is finished, stop controller
        DEBUG("[***] Stopping command controller")
        m_controller->stop();

        DEBUG("[***] Cleaning CIF")
        m_cif->cleanup(false);
    }

    LOG_INFO("[***] End main loop")
}

//=============================================================================
// METHOD    : SPELLinterpreter::prepareWarmStart
//=============================================================================
void SPELLinterpreter::prepareWarmStart( const SPELLcontextConfig& ctxConfig )
{
    // Prepare warmstart file if applicable
    if (m_config.warmstart)
    {
        char* home = getenv("SPELL_DATA");
        if (home == NULL)
        {
            std::string msg = "Unable to setup persistent file, no SPELL_DATA environment variable defined";
            LOG_ERROR("    " + msg);
            m_cif->error(msg, LanguageConstants::SCOPE_SYS );
        }
        else
        {
            // Character replacements
            std::string theId = m_procedure;
            replace( theId, ".py", "" );
            replace( theId, "..", "" );
            replace( theId, "//", "/" );
            replace( theId, PATH_SEPARATOR, "_" );

            try
            {
                // Get the location of AsRUN files
                std::string wsdir = ctxConfig.getLocationPath( WS_LOCATION );
                // Build the full persistent file unique name
                std::string persistentFile = STR(home) + PATH_SEPARATOR + wsdir + PATH_SEPARATOR;
                // Check that the directory exists
                if (!pathExists(persistentFile))
                {
                	throw SPELLcoreException("Cannot create persistent file", "Directory not found: " + persistentFile);
                }
                persistentFile = persistentFile + m_config.timeId + "_Executor_" + theId;
                LOG_INFO("    Persistent files: " + persistentFile)
                // Create the warm start support
                m_warmStart = new SPELLwsWarmStartImpl();
                // Initialize it, working mode on-line
                m_warmStart->initialize( persistentFile, MODE_ON_HOLD );
                // Set the warm start support in the execution frame
                m_frame->setWarmStart( m_warmStart );
            }
            catch(SPELLcoreException& ex)
            {
                if (m_warmStart != NULL) delete m_warmStart;
                m_warmStart = NULL;
                throw ex;
            }
        }
    }
    else
    {
        m_cif->warning("ATTENTION: no warmstart mechanism is being used", LanguageConstants::SCOPE_SYS );
        LOG_WARN("[***] No warmstart mechanism will be used")
    }
}

//=============================================================================
// METHOD    : SPELLinterpreter::prepareObjects
//=============================================================================
const bool SPELLinterpreter::prepareObjects()
{
    LOG_INFO("[***] Preparing objects")
    // Setup the client interface. The standalone one does
    // just a few value initializations, the server one
    // does the login in the context process
    assert(m_cif != NULL);
    bool result = true;
    try
    {
        DEBUG("   Initializing Python interface")
        SPELLpythonHelper::instance().initialize();

        // Install log support asap
        Log_Install();

        DEBUG("   Loading execution framework functions")
        // Setup the execution environment
        SPELLpythonHelper::instance().loadFramework();

        DEBUG("   Loading configuration")
        // Load the SPELL configuration (will fail with exception if there is an error)
        SPELLconfiguration::instance().loadConfig(m_config.configFile);
        // Load the configuration on python side for the language and drivers
        SPELLconfiguration::instance().loadPythonConfig(m_config.configFile);
        // Get the context configuration
        SPELLcontextConfig& ctxConfig = SPELLconfiguration::instance().getContext(m_config.ctxName);

        DEBUG("   Setup client interface")
        m_cif->setup( m_procedure, m_config.ctxName, m_config.ctxPort, m_config.timeId );

        DEBUG("   Creating and linking")

        // Create the executor instance
        m_executor = new SPELLexecutorImpl();
        // Set the reference for the static accessor (Python layer and others)
        SPELLexecutor::setInstance( m_executor );

        m_controller = new SPELLcontroller();
        m_scheduler = new SPELLscheduler(false);
        m_callstack = new SPELLcallstack();
        // Create and initialize the execution frame
        m_frame = new SPELLexecutionFrame();

        // Prepare and configure warm start mechanism now
        DEBUG("   Preparing warm start")
        prepareWarmStart( ctxConfig );

        DEBUG("   Initializing executor")
        // Initialize the executor with the objects
        m_executor->initialize(m_cif, m_controller, m_scheduler, m_callstack, m_frame);
        // Let the executor prepare all the rest it needs for execution. Anything done here
        // shall be independent from reloads/aborts, etc.
        m_executor->prepare( m_procedure, ctxConfig );

        DEBUG("   Starting controller")
        m_controller->begin();

        LOG_INFO("[***] Objects ready")
    }
    catch(SPELLcoreException& ex)
    {
        std::string msg = "[***] Failed to create objects: " + STR(ex.what());
        LOG_ERROR(msg)
        m_controller->setError( "Failed to initialize: " + ex.getError(), ex.getReason(), true);
        result = false;
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLinterpreter::prepareExecution
//=============================================================================
const bool SPELLinterpreter::prepareExecution()
{
    LOG_INFO("[***] Preparing execution")
    try
    {
        DEBUG("   Installing exceptions")
        Exceptions_Install();

        DEBUG("   Installing executor binding")
        Executor_Install();

        DEBUG("   Installing goto bindings")
        Goto_Install();
        Step_Install();

        // Install the CIF object in the SPELL registry
        DEBUG("   Installing client interface")
        ClientIF_Install();

        // Setup procedure manager
        LOG_INFO("   Preparing procedures")
        SPELLprocedureManager::instance().setup(m_config.ctxName);
        m_procPath = SPELLprocedureManager::instance().getProcPath();
        // User library
        m_libPath = SPELLprocedureManager::instance().getLibPath();
        // Load user libraries
        loadUserLibraries();
        SPELLpythonHelper::instance().checkError();

        if (m_config.script)
        {
            // In this case the proc path is just the script
            m_procPath = m_procedure;
            LOG_INFO("   Compiling script")
            // Compile and create initial execution frame
            m_frame->initialize( m_procedure );
        }
        else
        {
            LOG_INFO("   Compiling procedure")
            // Compile and create initial execution frame
            m_frame->initialize( SPELLprocedureManager::instance().getProcFile(m_procedure) );
        }

        // Set the procedures path
        m_executor->setProcedurePath( m_procPath );
        // Set the user library path
        m_executor->setLibraryPath( m_libPath );

        LOG_INFO("Executor procedure path: " + m_procPath);
        LOG_INFO("Executor library path: " + m_libPath);

    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("   Catched error during preparation: " + STR(ex.what()))
        m_controller->setError( "Error during preparation: " + ex.getError(), ex.getReason(), true);
        return false;
    }
    LOG_INFO("[***] Execution ready")
    return true;
}

//=============================================================================
// METHOD    : SPELLinterpreter::loadUserLibraries
//=============================================================================
void SPELLinterpreter::loadUserLibraries()
{
    if (m_libPath != "" && m_libPath != "None" )
    {
        if (isDirectory(m_libPath))
        {
            LOG_INFO("   Loading user libraries")
            SPELLpythonHelper::instance().addToPath(m_libPath);
            std::list<std::string> files = getFilesInDir(m_libPath);
            std::list<std::string>::iterator it;
            std::list<std::string>::iterator end = files.end();
            for( it = files.begin(); it != end; it++)
            {
                std::string filename = (*it);
                if (filename == "__init__.py" ) continue;
                std::size_t idx = filename.find(".py");
                if ((idx != std::string::npos) && (idx>0))
                {
                    std::string module = filename.substr(0,idx);
                    LOG_INFO("     - importing user library " + module)
                    SPELLpythonHelper::instance().importAllFrom(module);
                }
            }
        }
        else
        {
            throw SPELLcoreException("Unable to load USER libraries", "Library path '" + m_libPath + "' is not a directory", true);
        }
    }
}

//=============================================================================
// METHOD    : SPELLinterpreter::execute
//=============================================================================
void SPELLinterpreter::execute()
{
    // Setup procedure manager
    LOG_INFO("[*] Starting execution")

    try
    {
        m_executor->execute();
    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[FATAL] Error during execution: " + STR(ex.what()))
		m_controller->setError( "Error during execution: " + ex.getError(), ex.getReason(), ex.isFatal());
    }

    LOG_INFO("[*] Execution finished")
}

//=============================================================================
// METHOD    : SPELLinterpreter::recover
//=============================================================================
void SPELLinterpreter::recover()
{
    // Setup procedure manager
    LOG_INFO("[*] Recovering execution from persistent file")
    try
    {
        m_executor->recover();
    }
    catch(SPELLcoreException& ex)
    {
        LOG_ERROR("[FATAL] Error during recovery: " + STR(ex.what()))
		/** \todo handle exception: send error */
    }
    LOG_INFO("[*] Execution finished")
}
