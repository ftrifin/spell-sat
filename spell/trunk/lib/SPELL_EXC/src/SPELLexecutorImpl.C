// ################################################################################
// FILE       : SPELLexecutorImpl.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor
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
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorImpl.H"
#include "SPELL_PRD/SPELLprocedureManager.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_WRP/SPELLdriverManager.H"
#include "SPELL_WRP/SPELLdatabaseManager.H"
#include "SPELL_WRP/SPELLregistry.H"
#include "SPELL_WRP/SPELLconstants.H"
using namespace PythonConstants;

// GLOBALS ////////////////////////////////////////////////////////////////////

// Bridge for C-API dispatching callback
static int static_dispatch( PyObject* obj, PyFrameObject* frame, int what, PyObject* args )
{
    SPELLexecutor::instance().dispatch(obj, frame, what, args);
    return 0;
}

//=============================================================================
// CONSTRUCTOR : SPELLexecutorImpl::SPELLexecutorImpl
//=============================================================================
SPELLexecutorImpl::SPELLexecutorImpl()
    : SPELLexecutorIF(),
      m_monitoringClients(),
      m_importChecker()
{
    m_initialized        = false;
    m_procId             = "";
    m_parentId           = "";
    m_childId            = "";
    m_contextName        = "";
    m_controllingClient  = "";
    m_procPath = "";

    m_config = new SPELLexecutorConfig();

    m_scheduler          = NULL;
    m_callstack          = NULL;
    m_controller         = NULL;
    m_childMgr           = NULL;

    m_userActionLabel = "";
    m_userActionEnabled = false;
    m_userActionFunction = "";
    m_gotoTarget = "";

    m_childMgr = new SPELLchildManager();

    DEBUG("[E] SPELLexecutor created")
}

//=============================================================================
// DESTRUCTOR : SPELLexecutorImpl::~SPELLexecutorImpl
//=============================================================================
SPELLexecutorImpl::~SPELLexecutorImpl()
{
    DEBUG("[E] SPELLexecutor destroyed")
    if (m_childMgr)
    {
        delete m_childMgr;
    }
    delete m_config;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::initialize()
//=============================================================================
void SPELLexecutorImpl::initialize( SPELLcif* cif,
                                SPELLcontrollerIF* controller,
                                SPELLschedulerIF* scheduler,
                                SPELLcallstackIF* callstack,
                                SPELLexecutionFrame* frame )
{
    assert( cif != NULL );
    assert( controller != NULL );
    assert( scheduler != NULL );
    assert( callstack != NULL );
    assert( frame != NULL );
    m_cif = cif;
    m_controller = controller;
    m_scheduler = scheduler;
    m_callstack = callstack;
    m_frame = frame;
    m_varManager = new SPELLvariableManager(*frame);
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::getStatus()
//=============================================================================
const SPELLexecutorStatus SPELLexecutorImpl::getStatus() const
{
    if (m_controller != NULL)
    {
        return m_controller->getStatus();
    }
    return STATUS_UNKNOWN;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::getContextName
//=============================================================================
const std::string SPELLexecutorImpl::getContextName() const
{
    return m_contextName;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::getProcId
//=============================================================================
const std::string SPELLexecutorImpl::getProcId() const
{
    return m_procId;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::prepare
//=============================================================================
void SPELLexecutorImpl::prepare( const std::string& procId, const SPELLcontextConfig& ctxConfig )
{
	assert( m_cif != NULL );
	assert( m_controller != NULL );
	assert( m_scheduler != NULL );
	assert( m_callstack != NULL );
	assert( m_frame != NULL );

    LOG_INFO("[E] Preparing execution")

    // See constants.H
    // We do this on preparation stage because these values shall not be reset
    // on procedure reload. The user may have changed them in the meantime.
    if (!m_initialized)
    {
		setProcId(procId);
		setContextName( ctxConfig.getName());

        // Get the executor configuration from the CIF. The CIF
        // has taken this config from the SPELL context via the login message.
        m_config->setArguments( m_cif->getArguments() );
        m_config->setCondition( m_cif->getCondition() );
        m_config->setAutomatic( m_cif->isAutomatic() );
        m_config->setVisible ( m_cif->isVisible() );
        m_config->setBlocking( m_cif->isBlocking() );
        m_config->setBrowsableLib( m_cif->isBrowsableLib() );
        m_config->setRunInto( (ctxConfig.getExecutorParameter(ExecutorConstants::RunInto) == True) );
        m_config->setByStep( (ctxConfig.getExecutorParameter(ExecutorConstants::ByStep) == True) );
        m_config->setExecDelay( atoi((ctxConfig.getExecutorParameter(ExecutorConstants::ExecDelay).c_str())) );
        m_config->setBrowsableLib( (ctxConfig.getExecutorParameter(ExecutorConstants::BrowsableLib) == True) );
        std::string saveMode = ctxConfig.getExecutorParameter(ExecutorConstants::SaveStateMode);
        m_config->setSaveStateMode( saveMode );
        std::string wvMode = ctxConfig.getExecutorParameter(ExecutorConstants::WatchVariables);
        bool wvEnabled = wvMode == ExecutorConstants::ENABLED;
        m_config->setWatchEnabled( wvEnabled );
        m_varManager->setEnabled( wvEnabled );
        LOG_INFO("[E] Arguments       : " + m_config->getArguments()  )
        LOG_INFO("[E] Condition       : " + m_config->getCondition()  )
        LOG_INFO("[E] Automatic mode  : " + BSTR( m_config->getAutomatic() ))
        LOG_INFO("[E] Visible mode    : " + BSTR( m_config->getVisible()   ))
        LOG_INFO("[E] Blocking mode   : " + BSTR( m_config->getBlocking()  ))
        LOG_INFO("[E] Browsable lib   : " + BSTR( m_config->getBrowsableLib()  ))
        LOG_INFO("[E] Save state mode : " + saveMode );
        LOG_INFO("[E] Watch variables : " + wvMode );

        m_initialized = true;
    }

    // Setup extra environment stuff
    // 1. Procedure arguments. If there are arguments available we need to evaluate
    // them in the Python environment and to install them in the execution environment
    // via the ARGS global object.
    PyObject* argDict = NULL;
    if (m_config->getArguments() != "")
    {
        try
        {
            // Evaluate the argument string. It is expected to be a Python dictionary.
            // No error check is done for this.
            argDict = SPELLpythonHelper::instance().eval(m_config->getArguments(),false);
        }
        catch(SPELLcoreException& ex)
        {
            m_cif->error( "Unable to install arguments data holder: " + STR(ex.what()), LanguageConstants::SCOPE_SYS );
            argDict = PyDict_New();
        }
    }
    else
    {
        // In case of no arguments given, we install an empty Python dictionary.
        argDict = PyDict_New();
    }
    Py_INCREF(argDict);
    SPELLpythonHelper::instance().install(argDict,"ARGS");

    // 2. Process condition and open mode
    // If there is a condition available, we set it to the controller. The controller
    // will read and evaluate it and hold the execution until the condition is
    // fullfilled.
    if (m_config->getCondition() != "")
    {
        m_controller->setCondition( m_config->getCondition() );
    }

    // Reset any user action function previously set
    m_userActionFunction = "";
    m_userActionLabel = "";
    m_userActionEnabled = false;

    // Configure the initial step over mode in the callstack
    if (m_config->getRunInto()==true)
    {
    	m_callstack->stepOver( SO_ALWAYS_INTO );
    }
    else
    {
    	m_callstack->stepOver( SO_ALWAYS_OVER );
    }

    LOG_INFO("[E] Executor prepared")
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::execute
//=============================================================================
void SPELLexecutorImpl::execute()
{
    DEBUG("[E] Starting execution")

    // This flag allows us to re-execute after recovery or reload
    bool continueExecuting = false;

    // Load the SPELL driver
    loadDriver();

    // Load the builtin databases, etc.
    loadExecutionEnvironment();

    // Initial flags when loading (not reloading/recovering)
    m_initStepDone = false;
    // Reset flag: we shall not reset executor entities if we are recovering
    bool resetEntities = true;

    do
    {
        // Setup the dispatching mechanism
        PyEval_SetTrace( static_dispatch, NULL );

        // Reset execution status
        if (resetEntities)
        {
			m_callstack->reset();
        }
		DEBUG("[E] Reset status");
		m_controller->reset();
		m_importChecker.reset();
        // Reset CIF closure lock always
		m_cif->resetClose();

        // Establish initial run-into value now (after reset)
        m_controller->enableRunInto( m_config->getRunInto() );

        // Mark the procedure start time
        m_controller->setStartTime();

        // If in automatic and backgorund mode, set the controller in play mode accordingly
        // IMPORTANT If in automatic and foreground mode, it is the GUI who sends the run command
        if (m_config->getAutomatic() && (!m_config->getVisible()))
		{
        	m_controller->setAutoRun();
		}

        DEBUG("[E] Launching execution");
        // Execute the procedure/script. This triggers the procedure
        // execution under dispatcher control.
        SPELLexecutionResult result = m_frame->execute();

        DEBUG("[E] Execution done, checking result");

        // Mark the procedure end time
        m_controller->setEndTime();

        // Check execution result and proceed accordingly.
        // Frame will be reset and environment unloaded if needed, depending on the case.

        switch(result)
        {
        case EXECUTION_ERROR:
			{
		        m_initStepDone = true;
				continueExecuting = executorFinishedWithErrors();
				if (continueExecuting)
				{
					// We will recover, so do not reset entities
					resetEntities = false;
				}
				break;
			}
        case EXECUTION_SUCCESS:
        case EXECUTION_TERMINATED:
			{
		        m_initStepDone = false;
        		continueExecuting = executorFinishedRight();
        		break;
        	}
        case EXECUTION_ABORTED:
			{
		        m_initStepDone = false;
				continueExecuting = executorAborted();
				break;
			}
        default:
			{
		        DEBUG("[E] Execution result unknown: " + ISTR(result));
		        m_initStepDone = false;
		        unloadDriver(true);
				continueExecuting = false;
				break;
			}
        }
    }
    while(continueExecuting);

    // This may be redundant, but does not harm.
    PyEval_SetTrace( NULL, NULL );

    DEBUG("[E] Execution finished")
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::reloadOrClose
//=============================================================================
bool SPELLexecutorImpl::reloadOrClose()
{
	DEBUG("[E] Waiting for CIF closure");
	// Wait the close or reload command from the user
	m_cif->waitClose();
	// This tells us if user wants to reload, not close
	if (m_controller->shouldReload())
	{
	    DEBUG("[E] Clear asrun");
	    m_cif->clearAsRun();
	    DEBUG("[E] Reload driver");
		// We are reloading, load again the execution environment and reset the frame
		loadDriver();
	    DEBUG("[E] Reset frame");
		m_frame->reset();
	    DEBUG("[E] Reload environment");
		loadExecutionEnvironment();
		return true;
	}
	else
	{
		// Completely unload execution environment if we are not reloading
		unloadDriver(true);
		return false;
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executorAborted
//=============================================================================
bool SPELLexecutorImpl::executorAborted()
{
	DEBUG("[E] Execution finished due to abort");
	// Procedure has aborted, so unload the driver and execution environment right away
	unloadDriver(false);
	// Set the status aborted here, not before. Otherwise, the GUI would be notified
	// before the driver actually unloads, and this may take time.
	getController().setStatus(STATUS_ABORTED);
	// Wait until the user decides to close or to reload
	return reloadOrClose();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executorFinishedRight
//=============================================================================
bool SPELLexecutorImpl::executorFinishedRight()
{
	DEBUG("[E] Execution finished successfully");
    // Mark the procedure as finished
    m_controller->setFinished();
	// Procedure has finished, so unload the driver and execution environment right away
	unloadDriver(false);
    // This call may be misleading but the behavior at this point is the same as the aborted state
	return reloadOrClose();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executorFinishedWithErrors
//=============================================================================
bool SPELLexecutorImpl::executorFinishedWithErrors()
{
	DEBUG("[E] Execution finished with errors");
    // Will return true if the error was recoverable AND
    // the user chooses to recover.
    AfterError errorHandlingResult = handleExecutionError();
    bool continueExecuting = false;

    // If the user does not want to (either closing or reloading)
    switch(errorHandlingResult)
    {
    case CANCEL_RECOVER:
		{
	        // This tells us if user wants to reload, not close
	        if(m_controller->shouldReload())
	        {
	        	// If we are reloading, just dont unload the execution environment, reset the
	        	// frame and start over
	        	m_frame->reset();
	        	continueExecuting = true;
	        }
	        else
	        {
	        	// Unload the environment if we dont recover
	        	unloadDriver(true);
	        }
	        // Else will close and not reload/recover
	        break;
		}
    case RECOVER_SUCCESS:
		{
			// To re-execute. But do not reset the frame and dont unload the environment.
			continueExecuting = true;
			break;
		}
    case RECOVER_FAILED:
    case CANNOT_RECOVER:
		{
			// Unload the environment, could not recover it
			unloadDriver(false);
			// We will need to wait users choice
			DEBUG("[E] Waiting for CIF closure (recovery failed)")
			// Wait the close or reload command from the user
			m_cif->resetClose();
			m_cif->waitClose();
			// This may be misleading, but the behavior at this point is the same as aborted
			continueExecuting = reloadOrClose();
			break;
		}
    default:
    	break;
    }
    return continueExecuting;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::recover
//=============================================================================
void SPELLexecutorImpl::recover()
{
    DEBUG("[E] Recovering execution")

    try
    {
        // Restore the state from persistent file
        m_frame->restoreState();
    }
    catch(SPELLcoreException& ex)
    {
    	std::string msg = "Recovery failed, could not restore state";
        m_cif->notifyError( msg, ex.what(), true);
        DEBUG("[E] Failed to recover: " + STR(ex.what()));
        PyEval_SetTrace( NULL, NULL );
        DEBUG("[E] Waiting for CIF closure")
        m_cif->waitClose();
        return;
    }
    DEBUG("[E] Execution recovery finished")
    execute();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::finalize
//=============================================================================
void SPELLexecutorImpl::finalize()
{
    DEBUG("[E] Finalizing, user request closure")
    // Release the CIF lock, the controller received the close command
    // so we can proceed
    m_cif->canClose();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::loadExecutionEnvironment
//=============================================================================
void SPELLexecutorImpl::loadExecutionEnvironment()
{
	// Load SCDB and GDB
	SPELLdatabaseManager::instance().loadBuiltinDatabases();

	// Create proc dictionary object and install it
	PyObject* procObj = PyDict_New();
	PyObject* pname = SSTRPY(m_procId);
	PyObject* arguments = SSTRPY(m_config->getArguments());

	PyDict_SetItemString( procObj, "NAME", pname);
	PyDict_SetItemString( procObj, "ARGS", arguments);

	SPELLpythonHelper::instance().install( procObj,  "PROC" );

	// Tell the execution frame that the environment has been updated
	m_frame->filterDictUpdated();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::loadDriver
//=============================================================================
void SPELLexecutorImpl::loadDriver()
{
    try
    {
    	DEBUG("[E] Load driver - start");

    	SPELLnoCommandProcessing nc;

        LOG_INFO("Loading driver")
        // Prepare and load the SPELL driver
        SPELLdriverManager::instance().setup( m_contextName );

    	DEBUG("[E] Load driver - end");
    }
    catch(SPELLcoreException& ex)
    {
        throw ex;
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::unloadDriver
//=============================================================================
void SPELLexecutorImpl::unloadDriver( bool shutdown )
{
    try
    {
    	DEBUG("[E] Unload driver - start");

    	SPELLnoCommandProcessing nc;

        // Just cleanup driver if reload/recover will be done
        SPELLdriverManager::instance().cleanup(shutdown);

    	DEBUG("[E] Unload driver - end");
    }
    catch(SPELLcoreException& ex)
    {
        throw ex;
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::handleExecutionError
//=============================================================================
const SPELLexecutorImpl::AfterError SPELLexecutorImpl::handleExecutionError()
{
    AfterError result = CANNOT_RECOVER;
    SPELLcoreException* exc = m_frame->getError();
    bool canRestore = m_frame->haveWarmStart();
    bool fatalError = exc->isFatal() || (!canRestore);
    LOG_ERROR("[E] Execution failed: " + STR(exc->what()));

    // If the error is inside user library, we do not allow recovery
    // to browsable lib ON to show where the error was.
    std::string location = m_frame->getErrorLocation();
    if ((location != "")&& (m_libPath != ""))
    {
    	if (location.find(m_libPath) == 0)
    	{
    	    m_controller->setError( "Error happened in user library: " + exc->getError(), exc->getReason(), true );
    		return CANNOT_RECOVER;
    	}
    }

    // Otherwise notify the error normally
    m_controller->setError( "Execution aborted: " + exc->getError(), exc->getReason(), fatalError  );

    // If the execution frame has warmstart information
    if ( canRestore )
    {
		// Wait user request to reload or abort
		LOG_INFO("[E] Waiting for recover command");
		DEBUG("[E] Waiting for recover command");
		m_cif->waitClose();
		// This will tell us if the user wants recovery or not
		bool doRecover = m_controller->shouldRecover();
		LOG_INFO("[E] Recovery flag: " + ( doRecover ? STR("enabled") : STR("disabled")))
    	DEBUG("Do recover flag: " + BSTR(doRecover));
        if (doRecover)
        {
            LOG_INFO("[E] Recovering execution");
            m_cif->warning("Recovering execution from failure", LanguageConstants::SCOPE_SYS );
            try
            {
                try
                {
                    // No commands processed in the meantime
                	SPELLnoCommandProcessing nc;
                    // Recover the state in the frame
                    m_frame->fixState();
                }
                catch(SPELLcoreException& ex)
                {
                    throw ex;
                }

                // Now re-invoke execution
                DEBUG("[E] Re-executing");
                PyErr_Clear();
                result = RECOVER_SUCCESS;
            }
            catch(SPELLcoreException& ex)
            {
            	std::string msg = "[E] Recovery failed: " + STR(ex.what());
                LOG_ERROR(msg);
                DEBUG(msg);
                m_cif->notifyError("Recovery failed", ex.what(), true);
                result = RECOVER_FAILED;
            }
        }
        else
        {
            LOG_WARN("[E] Recovery cancelled by user");
            m_cif->warning("Recovery cancelled, procedure will be aborted", LanguageConstants::SCOPE_SYS );
            result = CANCEL_RECOVER;
        }
    }
    else
    {
        LOG_ERROR("Recovery failed, warm start mechanism disabled");
        m_cif->notifyError("Recovery failed", "Warm start mechanism disabled", true);
        // result is already CANNOT_RECOVER
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::dispatch
//=============================================================================
void SPELLexecutorImpl::dispatch( PyObject* obj, PyFrameObject* frame, int what, PyObject* args )
{
    SPELLmonitor m(m_dspLock);

    // Do not dispatch if the notified code is not a procedure
    std::string file = PYSTR(frame->f_code->co_filename);

//    DEBUG("[DISPATCH] Processing file '" + file + "'")
//    DEBUG("[DISPATCH] Proc path: '" + m_procPath + "'")
//    DEBUG("[DISPATCH] Lib path: '" + m_libPath + "'")

    bool notProc = (file.find(m_procPath) == std::string::npos);
    bool notUserLib = true;
    if (notProc)
    {
		notUserLib = (m_libPath == "") || (file.find(m_libPath) == std::string::npos);
		if (notUserLib) return;
    }

    // Filter importing state
    std::string name = PYSTR(frame->f_code->co_name);
    // Do not dispatch if the interpreter is importing a file
    if (m_importChecker.isImporting(what,file,frame->f_lineno,name)) return;

    // Extract the rest of information
    std::string path = PYSTR(frame->f_code->co_filename);

    // Will provide procedure or user library id
    std::string procId = SPELLprocedureManager::instance().getProcId(path);

    int lineno = frame->f_lineno;

    std::string etype = "other";
    switch(what)
    {
    case PyTrace_EXCEPTION:
    	etype = "EXCEPTION";
    	break;
    case PyTrace_LINE:
    	etype = "LINE";
    	break;
    case PyTrace_CALL:
    	etype = "CALL";
    	break;
    case PyTrace_RETURN:
    	etype = "RETURN";
    	break;
    default:
    	break;
    }

    DEBUG("[DISPATCH] Dispatch event in " + procId + ":" + ISTR(lineno));
    DEBUG("           (" + name + ") at " + PYCREPR(frame));
    DEBUG("           event type " + etype);
    //std::cerr << "[DISPATCH] Dispatch event in " + procId + ":" + ISTR(lineno) + " (" + name + ") at " + PYCREPR(frame) << std::endl;

    // Notify the frame manager to update the frame. This way, the internal models are
    // created/updated at the proper time following the procedure execution. This call
    // must be done before any other call to the frame manager that makes use of the
    // 'procedure execution model object', because it is first created at this point.
	m_frame->updateCurrentFrame(frame);


    // Init step feature
    if ( (what == PyTrace_LINE) &&
            (!m_initStepDone)      &&
            checkInitStep( lineno )   )
    {
        DEBUG("[DISPATCH] Override dispatch event " + procId + ":" + ISTR(lineno) + " (" + name + ") at " + PYCREPR(frame))
        return;
    }

    // Browsable lib feature: if the flag is enabled, allow browsing inside the user library.
    // this means that dispatch shall continue when notUserLib is false --> we make it true.
    notUserLib = notUserLib | m_config->getBrowsableLib();

    // By-step feature: if enabled, pause on each Step statement
    if (m_config->getByStep()) checkByStep(lineno);

    // Breakpoint feature: pause on the breakpoints
    checkBreakpoint(procId,lineno);

    // Repeat flag is used for certain cases of skip mechanism.
    bool repeat = true;
    // Will be false if the execution has been aborted (the controller knows this)
    // if the execution has been aborted, dispatching wont be done in control objects below.
    bool statusOk = true;
    while(repeat)
    {
        // Check aborted state beforehand
        statusOk = m_controller->checkAborted();
        repeat = false;

        // Here is the in-language goto mechanism implementation. Whenever the current line is
        // a line with a programmed goto (there is a target line in the Goto model) the frame
        // lineno will be changed accordingly.
        if (m_gotoTarget != "")
        {
             repeat = repeat || m_frame->goLabel(m_gotoTarget);
             lineno = frame->f_lineno;
             m_gotoTarget = "";
        }

        // If not aborted and not in User library, perform the data dispatch to each control object
        if (statusOk && notUserLib)
        {
            switch(what)
            {
            case PyTrace_EXCEPTION:
            {
                DEBUG("[DISPATCH] Exception " + procId + ":" + ISTR(lineno) );
				DEBUG("[DISPATCH] Going to error state");

				// Check if it is a SPELL exception (syntax, driver, etc). If so,
				// we want to KEEP the dispatching mechanism on because we still are
				// executing a procedure normally.
				// Handle name errors
				PyObject* errValue = PyTuple_GetItem(args,1);
				bool isSpell = SPELLpythonHelper::instance().isInstance( errValue, "SpellException", "spell.lib.exception" );

				// If we are in a try block there will be no error report,
				// but inform the user about the catched exception
				if (m_frame->getModel().isInTryBlock( lineno ))
				{
					PyObject* errType = PyTuple_GetItem(args,0);
					PyObject* errTb = PyTuple_GetItem(args,2);
					SPELLcoreException* ex = SPELLpythonHelper::instance().errorToException( errType, errType, errValue, errTb );
					if (isSpell)
					{
						m_cif->write( "SPELL exception: " + PYREPR(errValue), LanguageConstants::SCOPE_SYS);
					}
					else if (ex)
					{
						m_cif->write( ex->what(), LanguageConstants::SCOPE_SYS );
					}
				}

				if ( isSpell )
				{
					DEBUG("[DISPATCH] Exception is SPELL");
					return;
				}

				DEBUG("[DISPATCH] Python exception");

                statusOk = false;
                break;
            }
            case PyTrace_LINE:
            {
                DEBUG("[DISPATCH] Line " + procId + ":" + ISTR(lineno) );
                // Notify the event to the excecution frame
                m_frame->eventLine(frame);
                // If the line is executable
                bool executable = true; /** \todo m_frame->isExecutable(lineno); */
				m_callstack->event_line( procId, lineno, name );
				repeat = m_controller->event_line( procId, lineno, name, executable );
                break;
            }
            case PyTrace_CALL:
            {
                DEBUG("[DISPATCH] Call " + procId + ":" + ISTR(lineno) );
				// Notify the event to the excecution frame
				m_frame->eventCall(frame);
                m_callstack->event_call( procId, lineno, name );
                m_controller->event_call( procId, lineno, name );
                break;
            }
            case PyTrace_RETURN:
            {
                DEBUG("[DISPATCH] Return " + procId + ":" + ISTR(lineno) );
				// Notify the event to the excecution frame
				m_frame->eventReturn(frame);
                m_callstack->event_return( procId, lineno, name );
                m_controller->event_return( procId, lineno, name );
                break;
            }
            default:
                LOG_ERROR("[DISPATCH] Uncontrolled event " + procId + ":" + ISTR(lineno) )
                break;
            }
        }
        else
        {
            DEBUG("[DISPATCH] Wont dispatch");
        }
		lineno = frame->f_lineno;
    }

    // If we are terminating/aborting the execution, tell the frame to move to the end of
    // the bytecode. This way we guarantee nothing will be executed.
    if(!m_controller->checkAborted())
    {
        DEBUG("[DISPATCH] Status NOK, terminate frame");
        m_frame->terminate();
    }

    // Hold the dispatching mechanism if there are commmands to be processed,
    // and until the command finishes
	m_controller->waitCommand();

    DEBUG("[DISPATCH] Exit dispatch event in " + procId + ":" + ISTR(frame->f_lineno));
    DEBUG("           (" + name + ") at " + PYCREPR(frame));
    DEBUG("           event type " + etype);
    //std::cerr << "[DISPATCH] Exit dispatch event " + procId + ":" + ISTR(lineno) + " (" + name + ") at " + PYCREPR(frame) << std::endl;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setRunInto
//=============================================================================
void SPELLexecutorImpl::setRunInto( const bool enabled )
{
    if (m_config->getRunInto() != enabled)
    {
        LOG_INFO("[EXEC] Run into flag set to " + (enabled ? STR("ENABLED") : STR("DISABLED")))
        m_config->setRunInto(enabled);
        m_controller->enableRunInto(enabled);
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setByStep
//=============================================================================
void SPELLexecutorImpl::setByStep( const bool enabled )
{
    if (m_config->getByStep() != enabled)
    {
        LOG_INFO("[EXEC] By step flag set to " + (enabled ? STR("ENABLED") : STR("DISABLED")))
        m_config->setByStep(enabled);
        /** \todo configure for dispatch */
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setBrowsableLib
//=============================================================================
void SPELLexecutorImpl::setBrowsableLib( const bool enabled )
{
    if (m_config->getBrowsableLib() != enabled)
    {
        LOG_INFO("[EXEC] Browsable lib flag set to " + (enabled ? STR("ENABLED") : STR("DISABLED")))
        m_config->setBrowsableLib(enabled);
        /** \todo configure for dispatch */
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setExecDelay
//=============================================================================
void SPELLexecutorImpl::setExecDelay( const int delay )
{
    if (m_config->getExecDelay() != delay)
    {
        LOG_INFO("[EXEC] Execution delay set to " + ISTR(delay))
        m_config->setExecDelay(delay);
        m_controller->setExecutionDelay(delay);
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::command
//=============================================================================
void SPELLexecutorImpl::command( const ExecutorCommand& cmd, const bool high_priority )
{
    SPELLmonitor m(m_cmdLock);
    DEBUG("[E] Issuing command " + cmd.id);
	m_controller->command( cmd, true, high_priority );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::abort
//=============================================================================
void SPELLexecutorImpl::abort( const std::string& message, bool systemAborted )
{
    SPELLmonitor m(m_cmdLock);

    // Send the finish message if any
    if (message != "")
    {
    	m_cif->warning(message, systemAborted ? LanguageConstants::SCOPE_SYS : LanguageConstants::SCOPE_PROC );
    }

    // Abort command is a special case which needs this method to process
    // abort requests coming from the SPELL framework, not the user (in the
    // former case abort shall be immediate, whereas in the latter the abort
    // command shall wait till the language execution lock is released)
	ExecutorCommand cmd_abort;
	cmd_abort.id = CMD_ABORT;
	m_controller->command( cmd_abort, false, true );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::pause
//=============================================================================
void SPELLexecutorImpl::pause()
{
    SPELLmonitor m(m_cmdLock);

    // Pause command is a special case which needs this method to process
    // pause requests coming from the SPELL framework, not the user.
	ExecutorCommand cmd_pause;
	cmd_pause.id = CMD_PAUSE;
	m_controller->command( cmd_pause, false, true );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::finish
//=============================================================================
void SPELLexecutorImpl::finish( const std::string& message )
{
    SPELLmonitor m(m_cmdLock);

    // Send the finish message if any
    if (message != "")
    {
    	m_cif->warning(message, LanguageConstants::SCOPE_PROC );
    }
    // Finish command is a special case which needs this method to process
    // finish requests coming from the SPELL framework, not the user.
	ExecutorCommand cmd_finish;
	cmd_finish.id = CMD_FINISH;
	m_controller->command( cmd_finish, false, true );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::processLock
//=============================================================================
void SPELLexecutorImpl::processLock()
{
    SPELLmonitor m(m_cmdLock);
    m_controller->executionLock();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::processUnlock
//=============================================================================
void SPELLexecutorImpl::processUnlock()
{
    SPELLmonitor m(m_cmdLock);
    m_controller->executionUnlock();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::canSkip
//=============================================================================
const bool SPELLexecutorImpl::canSkip()
{
	// Allow skipping if in wait or interrupted state
	SPELLexecutorStatus status = m_controller->getStatus();
	if ((status == STATUS_WAITING)||(status == STATUS_INTERRUPTED)) return true;
	return m_frame->canSkip();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::goNextLine
//=============================================================================
const bool SPELLexecutorImpl::goNextLine()
{
	unsigned int currentLine = m_frame->getCurrentLine();
	if (m_frame->getModel().isBlockStart(currentLine))
	{
		DEBUG("[E] Skipping entire code block");
		unsigned int nextLine = m_frame->getModel().getBlockEnd(currentLine);
		return goLine(nextLine);
	}
	else
	{
		DEBUG("[E] Skipping single line");
		// When we skip the line, we dont want the callstack and trace model to register the current line
		m_callstack->skipCurrentLine();
		return m_frame->goNextLine();
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::goLabel
//=============================================================================
const bool SPELLexecutorImpl::goLabel( const std::string& label, bool programmed )
{
	if (programmed)
	{
		m_gotoTarget = label;
		return true;
	}
	else
	{
		m_gotoTarget = "";
		bool result = m_frame->goLabel(label);
		if (result)
		{
			// When we skip the line, we dont want the callstack and trace model to register the current line
			m_callstack->skipCurrentLine();
		}
		else
		{
			m_cif->warning("Unable to go to label '" + label + "'", LanguageConstants::SCOPE_SYS );
		}
		return result;
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::goLine
//=============================================================================
const bool SPELLexecutorImpl::goLine( const int new_lineno )
{
	if (m_frame->getModel().isInsideBlock(new_lineno))
	{
		DEBUG("[E] Cannot go to line " + ISTR(new_lineno));
        m_cif->warning("Unable to go to line '" + ISTR(new_lineno) + "'", LanguageConstants::SCOPE_SYS );
		return false;
	}
    bool result = m_frame->goLine( new_lineno );
    if (result)
    {
    	// When we skip the line, we dont want the callstack and trace model to register the current line
    	m_callstack->skipCurrentLine();
    }
    else
    {
        m_cif->warning("Unable to go to line '" + ISTR(new_lineno) + "'", LanguageConstants::SCOPE_SYS );
    }
    return result;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setBreakpoint
//=============================================================================
const bool SPELLexecutorImpl::setBreakpoint( const std::string& file,
		                                     const unsigned int line,
		                                     const SPELLbreakpointType type )
{
	return m_frame->getBreakpoints().setBreakpoint( file, line, type );
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::checkBreakpoint
//=============================================================================
void SPELLexecutorImpl::checkBreakpoint( const std::string& file, const unsigned int line )
{
	if (m_controller->getStatus() == STATUS_RUNNING)
	{
		if (m_frame->getBreakpoints().checkBreakpoint(file,line))
		{
			DEBUG("[BYSTEP] Pausing procedure on breakpoint");
			LOG_INFO("Breakpoint on line " + ISTR(line));
			pause();
		}
	}
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::clearBreakpoints
//=============================================================================
void SPELLexecutorImpl::clearBreakpoints()
{
	m_frame->getBreakpoints().clearBreakpoints();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::checkByStep
//=============================================================================
const bool SPELLexecutorImpl::checkByStep( const int& frameLine )
{
    if (m_frame->getModel().isLabel(frameLine))
    {
        DEBUG("[BYSTEP] Pausing procedure");
        pause();
        return true;
    }
    return false;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::checkInitStep
//=============================================================================
const bool SPELLexecutorImpl::checkInitStep( const int& frameLine )
{
    bool abortDispatching = false;
    // Only if there is an INIT step in the code
    if (m_frame->getModel().hasInitStep())
    {
        DEBUG("[EXEC] Check init step on " + ISTR(frameLine));
        // Abort the dispatching unless the current line is the INIT line
        abortDispatching = true;
        if(m_frame->getModel().isInitStep( frameLine ))
        {
            LOG_INFO("Pausing on INIT step")
            m_initStepDone = true;
            pause();
            // Continue dispatching so that the
            // controller holds the execution in PAUSE
            abortDispatching = false;
        }
    }
    return abortDispatching;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::runScript
//=============================================================================
const bool SPELLexecutorImpl::runScript( const std::string& script )
{
    m_cif->setManualMode(true);
    bool result = true;
    try
    {
        m_frame->runScript( script );
    }
    catch(SPELLcoreException& ex)
    {
        result = false;
        m_cif->warning("Failed to execute script: " + STR(ex.what()), LanguageConstants::SCOPE_SYS );
    }
    m_cif->setManualMode(false);
    return result;
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::executeUserAction()
//=============================================================================
void SPELLexecutorImpl::executeUserAction()
{
    if (m_userActionFunction != "")
    {
        try
        {
            m_cif->write("Running user action " + m_userActionFunction, LanguageConstants::SCOPE_SYS );
            std::string actionScript = m_userActionFunction + "()";
            m_frame->runScript( actionScript );
        }
        catch(SPELLcoreException& ex)
        {
            m_cif->error("Failed to execute user action: " + STR(ex.what()), LanguageConstants::SCOPE_SYS );
        }
    }
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::setUserAction()
//=============================================================================
void SPELLexecutorImpl::setUserAction( const std::string& functionName, const std::string& actionLabel, const unsigned int severity )
{
    m_userActionFunction = functionName;
    m_userActionLabel = actionLabel;
    m_userActionEnabled = true;
    m_userActionSeverity = severity;
    m_cif->notifyUserActionSet(actionLabel,severity);
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::enableUserAction()
//=============================================================================
void SPELLexecutorImpl::enableUserAction( bool enable )
{
    m_userActionEnabled = enable;
    m_cif->notifyUserActionEnable(enable);
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::dismissUserAction()
//=============================================================================
void SPELLexecutorImpl::dismissUserAction()
{
    m_userActionFunction = "";
    m_userActionLabel = "";
    m_userActionEnabled = false;
    m_userActionSeverity = LanguageConstants::INFORMATION;
    m_cif->notifyUserActionUnset();
}

//=============================================================================
// METHOD    : SPELLexecutorImpl::stage
//=============================================================================
void SPELLexecutorImpl::stage( const std::string& id, const std::string& title )
{
	if (title != "")
	{
		m_cif->write("Step " + id + ": " + title, LanguageConstants::SCOPE_STEP );
	}
	else
	{
		m_cif->write("Step " + id, LanguageConstants::SCOPE_STEP );
	}
	m_callstack->setStage(id,title);
}
