// ################################################################################
// FILE       : SPELLcontroller.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor controller
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
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonError.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_WRP/SPELLconstants.H"
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLcontroller.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
#include "SPELL_EXC/SPELLcommand.H"



//=============================================================================
// CONSTRUCTOR    : SPELLcontroller::SPELLcontroller
//=============================================================================
SPELLcontroller::SPELLcontroller()
    : SPELLthread("controller"),
      SPELLcontrollerIF()
{
    m_mode = MODE_STEP;
    m_condition = "";
    reset();
    DEBUG("[C] Controller created");

}

//=============================================================================
// DESTRUCTOR    : SPELLcontroller::~SPELLcontroller
//=============================================================================
SPELLcontroller::~SPELLcontroller()
{
    DEBUG("[C] Controller destroyed");
}

//=============================================================================
// METHOD    : SPELLcontroller::reset
//=============================================================================
void SPELLcontroller::reset()
{
    m_status = STATUS_UNINIT;
    m_mainProc = "";
    m_currentProc = "";
    m_skipping = false;
    m_execDelay = 0;

    m_abort = false;
    m_error = false;
    m_finished = false;

    m_recover = false;
    m_reload = false;
    m_wantPause = false;

    m_recoverEvent.set();
    m_controllerLock.set();
    m_execLock.set();
    m_goCommand.set();
    m_commandExecuted.set();

    m_mailbox.reset();

    DEBUG("[C] SPELLcontroller reset");
}

//=============================================================================
// METHOD    : SPELLcontroller::command
//=============================================================================
void SPELLcontroller::command( const ExecutorCommand& cmd, const bool queueIt, const bool high_priority )
{
	if (queueIt)
	{
		DEBUG("[C] Pushing command " + cmd.id);
		m_mailbox.push(cmd, high_priority);
	}
	else
	{
		if (cmd.id == CMD_ABORT)
		{
			executeCommand(cmd);
		}
		else if (cmd.id == CMD_FINISH)
		{
			executeCommand(cmd);
		}
		else if (cmd.id == CMD_PAUSE)
		{
			// This call occurs when the executor itself wants to pause, not the client
			doPause(false);
		}
	}
}

//=============================================================================
// METHOD    : SPELLcontroller::setStatus
//=============================================================================
void SPELLcontroller::setStatus( const SPELLexecutorStatus& st )
{
    bool newStatus = (st != m_status);
    m_status = st;
    // We don't want to notify status redundantly
    if (newStatus || st == STATUS_WAITING)
    {
        LOG_INFO("Procedure status: " + StatusToString(st));
        SPELLstatusInfo info(st);
        info.condition = getCondition();
        info.actionLabel = SPELLexecutor::instance().getUserActionLabel();
        info.actionEnabled = SPELLexecutor::instance().getUserActionEnabled();

		SPELLexecutor::instance().getCIF().notifyStatus( info );
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::getStatus
//=============================================================================
const SPELLexecutorStatus SPELLcontroller::getStatus() const
{
    return m_status;
}

//=============================================================================
// METHOD    : SPELLcontroller::setMode
//=============================================================================
void SPELLcontroller::setMode( const SPELLexecutionMode& mode )
{
    m_mode = mode;
}

//=============================================================================
// METHOD    : SPELLcontroller::getMode
//=============================================================================
const SPELLexecutionMode SPELLcontroller::getMode() const
{
    return m_mode;
}

//=============================================================================
// METHOD    : SPELLcontroller::setCondition
//=============================================================================
void SPELLcontroller::setCondition( const std::string& condition )
{
    m_condition = condition;
}

//=============================================================================
// METHOD    : SPELLcontroller::getCondition
//=============================================================================
const std::string& SPELLcontroller::getCondition() const
{
    return m_condition;
}

//=============================================================================
// METHOD    : SPELLcontroller::hasCondition
//=============================================================================
const bool SPELLcontroller::hasCondition() const
{
    return (getCondition() != "");
}

//=============================================================================
// METHOD    : SPELLcontroller::setAutoRun
//=============================================================================
void SPELLcontroller::setAutoRun()
{
    DEBUG("[C] Set autorun");
    setMode( MODE_PLAY );
    setStatus(STATUS_RUNNING);
    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::setExecutionDelay
//=============================================================================
void SPELLcontroller::setExecutionDelay( const long delay )
{
	DEBUG("[C] Set execution delay to " + ISTR(delay) + " msec");
    m_execDelay = delay;
}

//=============================================================================
// METHOD    : SPELLcontroller::enableRunInto
//=============================================================================
void SPELLcontroller::enableRunInto( const bool enable )
{
    DEBUG("[C] Run into enabled: " + (enable ? STR("yes") : STR("no")));
    // Change the step over mode in the call stack manager
    if(enable)
    {
        SPELLexecutor::instance().getCallstack().stepOver( SO_ALWAYS_INTO );
    }
    else
    {
        SPELLexecutor::instance().getCallstack().stepOver( SO_ALWAYS_OVER );
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::checkAborted
//=============================================================================
const bool SPELLcontroller::checkAborted()
{
    bool executionOk = true;
    if (m_finished)
    {
        DEBUG("[C] Set execution terminated");
        Set_Exception_ExecutionTerminated();
        executionOk = false;
    }
    else if (m_abort)
    {
        DEBUG("[C] Set execution aborted");
        Set_Exception_ExecutionAborted();
        executionOk = false;
    }
    return executionOk;
}

//=============================================================================
// METHOD    : SPELLcontroller::shouldRecover()
//=============================================================================
const bool SPELLcontroller::shouldRecover()
{
    m_recoverEvent.wait();
    return m_recover;
}

//=============================================================================
// METHOD    : SPELLcontroller::run
//=============================================================================
void SPELLcontroller::run()
{
    setStartTime();

    while(1)
    {
        DEBUG("[C] SPELLcontroller waiting command");
        ExecutorCommand cmd = m_mailbox.pull();
        DEBUG("[C] SPELLcontroller got command " + cmd.id);
        if (cmd.id == CMD_STOP)
        {
            m_reload = false;
            m_recoverEvent.set();
            m_commandExecuted.set();
            m_goCommand.set();
            DEBUG("[C] Stop done");
            return;
        }

        // Wait until language is processed
        DEBUG("[C] SPELLcontroller waiting language");
        {
        	if (m_controllerLock.isClear() && cmd.id == CMD_PAUSE)
        	{
        		SPELLexecutor::instance().getCIF().warning("Performing driver operation, will pause after", LanguageConstants::SCOPE_SYS );
        	}
        	//SPELLsafeThreadOperations ops;
            m_controllerLock.wait();
        }
        DEBUG("[C] SPELLcontroller done waiting language");
        executeCommand(cmd);
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::executeCommand()
//=============================================================================
void SPELLcontroller::executeCommand( const ExecutorCommand& cmd )
{
	// If a (repeatable) command is being executed, discard this one
	if (m_commandExecuted.isClear())
	{
		return;
	}
	// Will hold dispatching mechanism until command executed
	DEBUG("[C] Now executing command " + cmd.id);
    m_commandExecuted.clear();
    if (cmd.id == CMD_ABORT)
    {
        doAbort();
    }
    else if (cmd.id == CMD_FINISH)
    {
        doFinish();
    }
    else if (cmd.id == CMD_ACTION)
    {
        doUserAction();
    }
    else if (cmd.id == CMD_STEP)
    {
        doStep( false );
    }
    else if (cmd.id == CMD_STEP_OVER)
    {
        doStep( true );
    }
    else if (cmd.id == CMD_RUN)
    {
        doPlay();
    }
    else if (cmd.id == CMD_SKIP)
    {
        doSkip();
    }
    else if (cmd.id == CMD_GOTO)
    {
        if (cmd.earg == "line")
        {
            DEBUG("[C] Processing go-to-line " + cmd.arg);
            try
            {
                int line = atoi(cmd.arg.c_str());
                doGoto( line );
            }
            catch(...) {};
        }
        else if (cmd.earg == "label")
        {
            DEBUG("[C] Processing go-to-label " + cmd.arg);
            doGoto( cmd.arg );
        }
        else
        {
        	SPELLexecutor::instance().getCIF().error("Unable to process Go-To command, no target information", LanguageConstants::SCOPE_SYS );
        }
    }
    else if (cmd.id == CMD_PAUSE)
    {
    	SPELLexecutorStatus st = getStatus();
    	bool synchronizeWithDispatcher = (st != STATUS_WAITING) && (st != STATUS_RUNNING);
        doPause( synchronizeWithDispatcher );
    }
    else if (cmd.id == CMD_SCRIPT)
    {
    	/** \todo determine when to override */
        doScript(cmd.arg,false);
    }
    else if (cmd.id == CMD_CLOSE)
    {
        m_recover = false;
        m_reload = false;
        doClose();
    }
    else if (cmd.id == CMD_RELOAD)
    {
        doReload();
    }
    else if (cmd.id == CMD_RECOVER)
    {
        doRecover();
    }
    else
    {
        LOG_ERROR("[C] UNRECOGNISED COMMAND: " + cmd.id)
    }
	DEBUG("[C] Command execution finished " + cmd.id);
	m_mailbox.commandProcessed();
	// The command has finished, release the dispatcher
	m_commandExecuted.set();
}

//=============================================================================
// METHOD    : SPELLcontroller::run
//=============================================================================
void SPELLcontroller::waitCommand()
{
	// Block the dispatcher is there is a command waiting
	if (m_goCommand.isClear())
	{
		// Let the command run
		m_goCommand.set();
		// Wait for the command to finish
		DEBUG("[C] Dispatching waits for command to finish");
		m_commandExecuted.wait();
		DEBUG("[C] Dispatching proceeds, command finished");
	};
}

//=============================================================================
// METHOD    : SPELLcontroller::run
//=============================================================================
void SPELLcontroller::stop()
{
    ExecutorCommand stop;
    stop.id = CMD_STOP;
    m_mailbox.push( stop, true );
}

//=============================================================================
// METHOD    : SPELLcontroller::executionLock
//=============================================================================
void SPELLcontroller::executionLock()
{
    if (m_abort) doWait();
    DEBUG("[C] Controller process lock");
    m_controllerLock.clear();
}

//=============================================================================
// METHOD    : SPELLcontroller::executionUnlock
//=============================================================================
void SPELLcontroller::executionUnlock()
{
    DEBUG("[C] Controller process unlock");
    m_controllerLock.set();
}

//=============================================================================
// METHOD    : SPELLcontroller::doStep
//=============================================================================
void SPELLcontroller::doStep( bool stepOver )
{
	switch(getStatus())
	{
	case STATUS_PAUSED: // Allowed status
	case STATUS_INTERRUPTED: // Allowed status
		break;
	default:
		return;
	}
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do step ( stepping over: " + BSTR(stepOver) + ")" );

    SPELLexecutor::instance().getScheduler().restartWait();

    // A step will disable the step over
    if (stepOver)
    {
        SPELLexecutor::instance().getCallstack().stepOver( SO_ONCE_OVER );
    }
    else
    {
        SPELLexecutor::instance().getCallstack().stepOver( SO_ONCE_INTO );
    }

    setMode( MODE_STEP );

    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::doPlay
//=============================================================================
void SPELLcontroller::doPlay()
{
	switch(getStatus())
	{
	case STATUS_PAUSED: // Allowed status
	case STATUS_INTERRUPTED: // Allowed status
		break;
	default:
		return;
	}
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do play" );

    SPELLexecutor::instance().getScheduler().restartWait();

    setStatus(STATUS_RUNNING);
    setMode(MODE_PLAY);

    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::doPause
//=============================================================================
void SPELLcontroller::doPause( bool synchronizeWithDispatcher )
{
    switch(getStatus())
    {
    case STATUS_RUNNING: // Allowed status
    case STATUS_WAITING: // Allowed status
        break;
    default:
        DEBUG("[C] Cannot pause in status " + ISTR(getStatus()));
        return;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do pause");

    // If the procedure is running and the stepping mechanism is disabled
	if (synchronizeWithDispatcher && (!m_execLock.isClear()))
	{
	    DEBUG("[C] Synchronizing with dispatcher");
		// Enable the stepping mechanism
	    m_execLock.clear();
	    DEBUG("[C] Wait for command execution");
	    // Wait until command execution is launched
		m_goCommand.clear();
		m_goCommand.wait();
	    DEBUG("[C] Command executed, proceed to pause");
	}

    // Enable the pause flag for the line event to hold the execution
    m_wantPause = true;

    if (hasCondition())
    {
    	SPELLexecutor::instance().getCIF().warning("Execution schedule condition has been cancelled", LanguageConstants::SCOPE_SYS );
    	SPELLexecutor::instance().getScheduler().abortWait(false);
    }
    else
    {
        SPELLexecutor::instance().getScheduler().interruptWait();
    }

    DEBUG("[C] Set step mode");
    setMode( MODE_STEP );

    DEBUG("[C] Do pause done");
}

//=============================================================================
// METHOD    : SPELLcontroller::doAbort
//=============================================================================
void SPELLcontroller::doAbort()
{
    switch(getStatus())
    {
    case STATUS_ERROR: // Disallowed status
    case STATUS_ABORTED: // Disallowed status
    case STATUS_FINISHED: // Disallowed status
    case STATUS_RELOADING: // Disallowed status
        return;
    default:
        break;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do abort");

    // Setting this flags like this will provoke the execution abort thanks to the
    // checkAborted() call in the dispatching mechanism. We shall not do more
    // here (like setting the status) since the status ABORTED shall be set AFTER
    // doing some extra operations in the executor, like unloading the SPELL driver.
    // See SPELLexecutorImpl::executionAborted() method.
    m_abort = true;
    m_recover = false;
    m_reload = false;
    m_recoverEvent.set();

    SPELLexecutor::instance().getScheduler().abortWait( false );
    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::doFinish
//=============================================================================
void SPELLcontroller::doFinish()
{
    switch(getStatus())
    {
    case STATUS_RUNNING: // Allowed status
    case STATUS_PAUSED: // Allowed status
    case STATUS_WAITING: // Allowed status
        break;
    default:
        return;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do finish");

    m_finished = true;
    m_recover = false;
    m_reload = false;
    m_recoverEvent.set();

    SPELLexecutor::instance().getScheduler().abortWait( false );
    setStatus(STATUS_FINISHED);
    doContinue();
}

//=============================================================================
// METHOD    : SPELLcontroller::doClose
//=============================================================================
void SPELLcontroller::doClose()
{
    DEBUG("[C] Do close");
    m_finished = true;
    SPELLexecutorStatus st = getStatus();
    if ( st != STATUS_FINISHED && st != STATUS_ABORTED && st != STATUS_ERROR )
    {
        doContinue();
    }
    m_recoverEvent.set();
    DEBUG("[C] Finalize executor");
    SPELLexecutor::instance().finalize();
}

//=============================================================================
// METHOD    : SPELLcontroller::doReload
//=============================================================================
void SPELLcontroller::doReload()
{
    switch(getStatus())
    {
    case STATUS_ABORTED: // Allowed status
    case STATUS_FINISHED: // Allowed status
    case STATUS_ERROR: // Allowed status
        break;
    default:
        return;
    }

    DEBUG("[C] Begin reloading");
    setStatus( STATUS_RELOADING );
    setMode( MODE_STEP );
    m_recover = false;
    m_reload = true;
    doClose();
}

//=============================================================================
// METHOD    : SPELLcontroller::doRecover
//=============================================================================
void SPELLcontroller::doRecover()
{
	std::cerr << "############ TRY TO RECOVER ###############" << std::endl;
    if ((getStatus() != STATUS_ERROR) && !m_error) return;

    setMode( MODE_STEP );
    m_recover = true;
    m_reload = true;
    doClose();
}

//=============================================================================
// METHOD    : SPELLcontroller::doSkip
//=============================================================================
void SPELLcontroller::doSkip()
{
    switch(getStatus())
    {
    case STATUS_PAUSED: // Allowed status
    case STATUS_INTERRUPTED: // Allowed status
        break;
    default:
        return;
    }
    if (m_error) return; // Do not continue on error

    if (!SPELLexecutor::instance().canSkip())
    {
        DEBUG("[C] Cannot skip this line");
        SPELLexecutor::instance().getCIF().warning("Cannot skip this line", LanguageConstants::SCOPE_SYS );
        return;
    }

    DEBUG("[C] Do skip");

    bool waitAborted = SPELLexecutor::instance().getScheduler().abortWait( false );

    // Either we skip a proc line, or we abort a wait condition (then we dont want to actually skip a line)
    if (waitAborted)
    {
    	SPELLexecutor::instance().getCIF().warning("Wait condition aborted", LanguageConstants::SCOPE_SYS );
    }
    else
    {
        if (SPELLexecutor::instance().goNextLine())
        {
            m_skipping = not waitAborted;
            doContinue();
        }
        else
        {
        	/** \todo Should try to go to upper frame instead of stepping.
            // This is the issue that obligues us to put a return statement
            // at the end of each function. */
            doStep(false);
        }
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::doGoto
//=============================================================================
void SPELLcontroller::doGoto( const std::string& label )
{
    if (getStatus() != STATUS_PAUSED || m_error ) return;

    DEBUG("[C] Do goto label " + label);

    if (SPELLexecutor::instance().goLabel(label,false))
    {
        m_skipping = true;
        doContinue();
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::doGoto
//=============================================================================
void SPELLcontroller::doGoto( const int line )
{
    if (getStatus() != STATUS_PAUSED || m_error ) return;

    DEBUG("[C] Do goto line " + ISTR(line));

    if (SPELLexecutor::instance().goLine(line))
    {
        m_skipping = true;
        doContinue();
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::doScript
//=============================================================================
void SPELLcontroller::doScript( const std::string& script, const bool override )
{
    switch(getStatus())
    {
    case STATUS_PAUSED: // Allowed status
    case STATUS_WAITING: // Allowed status
    case STATUS_INTERRUPTED: // Allowed status
        break;
    default:
        return;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do script: " + script);
    SPELLexecutor::instance().runScript( script );
}

//=============================================================================
// METHOD    : SPELLcontroller::doUserAction
//=============================================================================
void SPELLcontroller::doUserAction()
{
    switch(getStatus())
    {
    case STATUS_ABORTED: // Disallowed status
    case STATUS_ERROR: // Disallowed status
    case STATUS_FINISHED: // Disallowed status
    case STATUS_RELOADING: // Dissallowed status
    case STATUS_INTERRUPTED: // Dissallowed status
        return;
    default:
        break;
    }
    if (m_error) return; // Do not continue on error

    DEBUG("[C] Do user action");
	SPELLexecutor::instance().executeUserAction();
}

//=============================================================================
// METHOD    : SPELLcontroller::doWait
//=============================================================================
void SPELLcontroller::doWait()
{
    if (m_abort || m_error) return;
    DEBUG("[C] Wait in");
	m_wantPause = false;
    setStatus( STATUS_PAUSED );
    {
    	SPELLsafeThreadOperations ops;
    	m_execLock.clear();
    	m_execLock.wait();
    }
    DEBUG("[C] Wait out");
}


//=============================================================================
// METHOD    : SPELLcontroller::doContinue
//=============================================================================
void SPELLcontroller::doContinue()
{
    DEBUG("[C] Continue");
    m_execLock.set();
}

//=============================================================================
// METHOD    : SPELLcontroller::event_line
//=============================================================================
const bool SPELLcontroller::event_line( const std::string& file, const int& line, const std::string& name, bool executable )
{
    DEBUG("[C] Line: " + file + ":" + ISTR(line) + " - executable: " + BSTR(executable));
    // Scheduling condition
    if (hasCondition())
    {
        DEBUG("[C] Waiting for condition");
        if (!SPELLexecutor::instance().getScheduler().waitCondition( m_condition ))
        {
            DEBUG("[C] Condition failed, pausing");
            m_mode = MODE_STEP;
        }
        m_condition = "";
    }
    // Stepping (execution hold) mechanism
    if (m_wantPause || !SPELLexecutor::instance().getCallstack().isSteppingOver())
    {
		if (getMode() == MODE_STEP)
		{
	    	if (executable) doWait();
		}
		else
		{
			usleep(m_execDelay * 1000);
		}
    }
    else
    {
        setStatus(STATUS_RUNNING);
    }
    // Skip-out-function flag
    if (m_skipping)
    {
        m_skipping = false;
        return true;
    }
    // Update the current procedure
    m_currentProc = file;

    return false;
}

//=============================================================================
// METHOD    : SPELLcontroller::event_call
//=============================================================================
void SPELLcontroller::event_call( const std::string& file, const int& line, const std::string& name)
{
    // Detect loaded status
    if (m_mainProc == "")
    {
        setStatus(STATUS_LOADED);
        m_mainProc = file;
    }

    // Detect the initial event for the procedure load. If autorun is not set
    // and there is no scheduling condition, pause the procedure.
    if ((name == "<module>") &&
            (not hasCondition()) &&
            (getStatus() == STATUS_LOADED))
    {
        doPause(false);
    }
    /** \todo review: Nominal case when calling functions, importing status?? */
    else if ( name == "<module>" && file != m_currentProc )
    {
        //Nothing to do
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::event_return
//=============================================================================
void SPELLcontroller::event_return( const std::string& file, const int& line, const std::string& name)
{
	// Nothing to do
}

//=============================================================================
// METHOD    : SPELLcontroller::setStartTime
//=============================================================================
void SPELLcontroller::setStartTime()
{
    LOG_INFO("[C] Procedure start time: " + timestamp());
}

//=============================================================================
// METHOD    : SPELLcontroller::setEndTime
//=============================================================================
void SPELLcontroller::setEndTime()
{
    LOG_INFO("[C] Procedure end time: " + timestamp());
}

//=============================================================================
// METHOD    : SPELLcontroller::setFinished
//=============================================================================
void SPELLcontroller::setFinished()
{
    if (!m_error && !m_finished)
    {
        SPELLexecutor::instance().getScheduler().abortWait( false );
        setStatus(STATUS_FINISHED);
    }
}

//=============================================================================
// METHOD    : SPELLcontroller::setError
//=============================================================================
void SPELLcontroller::setError( const std::string& error, const std::string& reason, const bool fatal )
{
    // Ensure the procedure will remain paused
    setMode( MODE_STEP );

    // Store status
    m_error = true;
    m_status = STATUS_ERROR;

    // Cannot reload/recover if the error is fatal
    m_recoverEvent.clear();
    if (fatal)
    {
        m_reload = false;
        m_recover = false;
    }

    // Abort any possible wait condition ongoing
    SPELLexecutor::instance().getScheduler().abortWait( false );

    // Report the error
    LOG_INFO("Procedure error: " + error + ", " + reason);
    DEBUG("[C] Notifying error to GUI");
    SPELLexecutor::instance().getCIF().notifyError( error, reason, fatal );

    // Allow execution to continue
    m_execLock.clear();
}
