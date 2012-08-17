// ################################################################################
// FILE       : SPELLscheduler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the executor scheduler
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
#include "SPELL_EXC/SPELLscheduler.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorStatus.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_WRP/SPELLregistry.H"
using namespace LanguageModifiers;


//=============================================================================
// CONSTRUCTOR    : SPELLscheduler::SPELLscheduler
//=============================================================================
SPELLscheduler::SPELLscheduler( bool useSafeCalls )
: SPELLschedulerIF()
{
    // Set the event, we do not want to block anything yet
    m_waitingEvent.set();

    m_useSafeCalls = useSafeCalls;

    // Initial values
    m_timer = NULL;
    m_condition.reset();
    m_result.reset();
    m_silentCheck = false;

    DEBUG("[SCH] Scheduler created");
}

//=============================================================================
// DESTRUCTOR    : SPELLscheduler::~SPELLscheduler
//=============================================================================
SPELLscheduler::~SPELLscheduler()
{
    m_pyTM = NULL;
    delete m_timer;
}

//=============================================================================
// METHOD    : SPELLscheduler::startWait
//=============================================================================
void SPELLscheduler::startWait( const SPELLscheduleCondition& condition )
{
    DEBUG("[SCH] Starting wait");

	// If there is a condition already ongoing, cancel it
	if (m_condition.type != SPELLscheduleCondition::SCH_NONE)
	{
		finishWait(false,false);
	}

    // Store the condition data
    m_condition = condition;

    DEBUG("		Condition type: " + ISTR(m_condition.type));
    DEBUG("		Expression    : " + m_condition.expression);
    DEBUG("		Message       : " + m_condition.message);
    DEBUG("		Period        : " + m_condition.period.toString());
    DEBUG("		Timeout       : " + m_condition.timeout.toString());
    DEBUG("		List          : " + PYREPR(m_condition.list));
    DEBUG("		Configuration : " + PYREPR(m_condition.config));
    DEBUG("		Prompt user   : " + BSTR(m_condition.promptUser));


	// Reset filters
	m_lastNotifiedItem.name = "";
	m_lastNotifiedItem.value ="";

	// Reset the condition result
    m_result.reset();

    // Set the language lock so that command reception is on hold
    setLanguageLock();

    // Store the start time and last notification time
    m_checkStartTime.setCurrent();
    m_lastNotificationTime.setCurrent();

    // Read the first notification period if there is any, otherwise use
    // the default 0
    if (m_condition.interval.size()>0)
    {
        m_notificationPeriodIndex = 0;
        m_notificationPeriod = *m_condition.interval[0];
        DEBUG("[SCH] Initial notification period: " + m_notificationPeriod.toString());
    }
    else
    {
        DEBUG("[SCH] No notification period");
        m_notificationPeriod = SPELLtime(0,0,true);
    }

    // Depending on the condition type
    if(m_condition.type == SPELLscheduleCondition::SCH_VERIFICATION)
    {
        DEBUG("[SCH] Using verification condition");
        message("Waiting for telemetry condition");
        // Obtain the TM interface handle
        m_pyTM = SPELLregistry::instance().get("TM");
        Py_INCREF(m_pyTM);
        // Start the checking timer
        startTimer();
    }
    else if (m_condition.type == SPELLscheduleCondition::SCH_EXPRESSION)
    {
        DEBUG("[SCH] Using expression condition");
        message("Waiting for expression condition");
        // Start the checking timer
        startTimer();
    }
    else if (m_condition.type == SPELLscheduleCondition::SCH_CHILD)
    {
        DEBUG("[SCH] Using child condition");
        message("Waiting for child procedure");
        // Start the checking timer
        startTimer();
    }
    else if (m_condition.type == SPELLscheduleCondition::SCH_TIME)
    {
        // Start messages are different for relative and absolute time conditions
        if (m_condition.targetTime.isDelta())
        {
            DEBUG("[SCH] Using relative time condition");
            SPELLtime currentTime;
            m_checkTargetTime = currentTime + m_condition.targetTime;
            std::string remainingStr = (m_checkTargetTime - m_checkStartTime).toString();
            message("Starting countdown. Time left " + remainingStr);
        }
        else
        {
            DEBUG("[SCH] Using absolute time condition");
            m_checkTargetTime = m_condition.targetTime;
            std::string remainingStr = (m_checkTargetTime - m_checkStartTime).toString();
            std::string targetStr = m_checkTargetTime.toString();
            message("Waiting until " + targetStr + ", time left: " + remainingStr );
        }
        startTimer();
    }
    else if (m_condition.type == SPELLscheduleCondition::SCH_FIXED)
    {
    	DEBUG("[SCH] Using fixed condition");
        // Result will be success
        m_result.type = SPELLscheduleResult::SCH_SUCCESS;
    }
    else
    {
    	DEBUG("[SCH] Using NO condition");
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::startTimer
//=============================================================================
void SPELLscheduler::startTimer()
{
    m_abortTimer = false;
    delete m_timer;
    // Start a timer to check the condition
    // If a period is given, use it. Otherwise use a default
    if (m_condition.period>0)
    {
        m_timer = new SPELLtimer( m_condition.period.getSeconds()*1000000 +
                                  m_condition.period.getMilliseconds()*1000, *this );
    }
    else
    {
        SPELLtime defaultPeriod(1,0,true);
        m_timer = new SPELLtimer( 200, *this );
    }
    m_timer->start();
}

//=============================================================================
// METHOD    : SPELLscheduler::timerCallback
//=============================================================================
bool SPELLscheduler::timerCallback( unsigned long elapsed )
{
    bool abortCheck = false;
    // If abort has been requested, just return true at the end of the method
    if (m_abortTimer)
    {
        DEBUG("[SCH] Timer callback: abort timer");
        abortCheck = true;
    }
    else
    {
        // Depending on the condition type, we perform one check or another
        switch(m_condition.type)
        {
        case SPELLscheduleCondition::SCH_TIME:
        {
            abortCheck = checkTime();
            break;
        }
        case SPELLscheduleCondition::SCH_EXPRESSION:
        {
            DEBUG("[SCH] Timer callback: check expression ");
            abortCheck = checkExpression();
            break;
        }
        case SPELLscheduleCondition::SCH_CHILD:
        {
            DEBUG("[SCH] Timer callback: check child procedure ");
            abortCheck = checkChildProcedure();
            break;
        }
        case SPELLscheduleCondition::SCH_VERIFICATION:
        {
            DEBUG("[SCH] Timer callback: check TM verification");
            abortCheck = checkVerification();
            break;
        }
        default:
            break;
        }

        // Periodic notifications with message and interval. We shall not be
        // aborting, there shall be a defined notification period, a message
        // to show shall be available and the time since the last notification
        // shall be greater than the current period.
        SPELLtime currentTime;
        if (not abortCheck &&
                (m_notificationPeriod>0) &&
                (m_condition.message != "") &&
                (currentTime - m_lastNotificationTime)>=m_notificationPeriod )
        {
            DEBUG("[SCH] Issuing user message");
        	SPELLexecutor::instance().getCIF().write(m_condition.message, LanguageConstants::SCOPE_PROC );

            // This is the last notification time now
            m_lastNotificationTime.setCurrent();

            // Depending on the remaining time, we search for the next
            // notification period (Interval modifier) if any
            SPELLtime remainingTime = m_checkTargetTime - currentTime;
            if (remainingTime<=m_notificationPeriod)
            {
                DEBUG("[SCH] Go to next notification period");
                m_notificationPeriodIndex++;
                if (m_condition.interval.size()>m_notificationPeriodIndex)
                {
                    m_notificationPeriod = *m_condition.interval[m_notificationPeriodIndex];
                    DEBUG("[SCH] Next notification period: " + m_notificationPeriod.toString());
                }
            }
        }
    }
    if (abortCheck)
    {
        // Declare the condition as fullfilled
        finishWait(true,false);
    }
    return abortCheck;
}

//=============================================================================
// METHOD    : SPELLscheduler::notifyTime
//=============================================================================
void SPELLscheduler::notifyTime( bool finished, bool success )
{
    if (m_silentCheck) return;

    if (!finished)
    {
        SPELLtime currentTime;
        std::string remaining = (m_checkTargetTime - currentTime).toString();
        ItemNotification item;
        item.type = NOTIFY_TIME;
        item.name = "COUNTDOWN";
        item.value = remaining;
        item.comment = "Target time: " + m_checkTargetTime.toString();
        item.status = "WAITING";
        notify( item );
    }
    else if (success)
    {
        message("Target time reached");
        ItemNotification item;
        item.type = NOTIFY_TIME;
        item.name = "COUNTDOWN";
        item.value = "0";
		item.comment = "";
        item.status = "SUCCESS";
        notify( item );
    }
    else
    {
        message("Time condition not fullfilled");
        ItemNotification item;
        item.type = NOTIFY_TIME;
        item.name = "COUNTDOWN";
        item.value = "0";
        item.comment = "Target time: " + m_checkTargetTime.toString();
        item.status = "FAILED";
        notify( item );
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::checkTime
//=============================================================================
bool SPELLscheduler::checkTime()
{
	SPELLmonitor m(m_checkLock);
    bool abortCheck = false;

    // Get the current time and compare it against the target time
    SPELLtime currentTime;
    if ( currentTime >= m_checkTargetTime )
    {
        DEBUG("[SCH] Timer callback: condition fullfilled");
        // Notify that the countdown has finished successfully
        notifyTime(true,true);
        // Cancel the timer loop
        abortCheck = true;
        // Set the result
        m_result.type = SPELLscheduleResult::SCH_SUCCESS;
    }
    else
    {
        // Time evolution notification
        notifyTime(false,false);
    }
    return abortCheck;
}

//=============================================================================
// METHOD    : SPELLscheduler::checkExpression
//=============================================================================
bool SPELLscheduler::checkExpression()
{
	SPELLmonitor m(m_checkLock);
    // If there is a timeout defined, check it first
    SPELLtime currentTime;
    if (m_condition.timeout>0)
    {
        SPELLtime checkTime = currentTime - m_checkStartTime;
        if (checkTime>m_condition.timeout)
        {
            DEBUG("[SCH] Checking expression timed out");
			// If PromptUser is false, do not mark it as failed but timeout
			if (m_condition.promptUser == false)
			{
	            DEBUG("[SCH] Set as timeout, no failure");
	            m_result.type = SPELLscheduleResult::SCH_TIMEOUT;
			}
			else
			{
	            DEBUG("[SCH] Set as timeout, failure");
	            m_result.type = SPELLscheduleResult::SCH_FAILED;
			}
            m_result.error = "Condition not fullfilled";
            m_result.reason = "Timed out";
            return true;
        }
    }
    DEBUG("[SCH] Checking expression " + m_condition.expression);
    PyObject* result = SPELLpythonHelper::instance().eval(m_condition.expression,false);
    SPELLpythonHelper::instance().checkError();
    DEBUG("[SCH] Expression result " + PYREPR(result));
    /** \todo protect errors and check result null */
    return (result == Py_True);
}

//=============================================================================
// METHOD    : SPELLscheduler::checkChildProcedure
//=============================================================================
bool SPELLscheduler::checkChildProcedure()
{
	SPELLmonitor m(m_checkLock);
    // If there is a timeout defined, check it first
    SPELLtime currentTime;
    if (m_condition.timeout>0)
    {
        SPELLtime checkTime = currentTime - m_checkStartTime;
        if (checkTime>m_condition.timeout)
        {
            DEBUG("[SCH] Checking child procedure timed out");
			if (m_condition.promptUser == false)
			{
	            DEBUG("[SCH] Set as timeout, no failure");
				m_result.type = SPELLscheduleResult::SCH_TIMEOUT;
			}
			else
			{
	            DEBUG("[SCH] Set as timeout, failure");
				m_result.type = SPELLscheduleResult::SCH_FAILED;
			}
            m_result.error = "Child procedure did not finish in time";
            m_result.reason = "Timed out";
            return true;
        }
    }
    DEBUG("[SCH] Checking child procedure");

    SPELLexecutorStatus st = SPELLexecutor::instance().getChildManager().getChildStatus();
    switch(st)
    {
    case STATUS_ERROR:
    	{
    		DEBUG("[SCH] Child procedure failed");
    		m_result.type = SPELLscheduleResult::SCH_FAILED;
    		m_result.error = SPELLexecutor::instance().getChildManager().getChildError();
    		m_result.reason = SPELLexecutor::instance().getChildManager().getChildErrorReason();
    		return true;
    	}
    case STATUS_ABORTED:
		{
    		DEBUG("[SCH] Child procedure aborted");
    		m_result.type = SPELLscheduleResult::SCH_FAILED;
    		m_result.error = "Child procedure did not finish";
    		m_result.reason = "Status is aborted";
    		return true;
		}
    case STATUS_FINISHED:
		{
			DEBUG("[SCH] Child procedure finished");
			m_result.type = SPELLscheduleResult::SCH_SUCCESS;
			return true;
		}
    default:
		{
			break;
		}
    }
    return false;
}

//=============================================================================
// METHOD    : SPELLscheduler::checkVerification
//=============================================================================
bool SPELLscheduler::checkVerification()
{
	SPELLmonitor m(m_checkLock);
    // If there is a timeout defined, check it first
    SPELLtime currentTime;
    if (m_condition.timeout>0)
    {
        SPELLtime checkTime = currentTime - m_checkStartTime;
        DEBUG("[SCH] Checking condition timeout (elapsed: " + checkTime.toString() + "), timeout " + m_condition.timeout.toString());
        if (checkTime>m_condition.timeout)
        {
            DEBUG("[SCH] Checking verification timed out");
            // Notify to clients
            if (!m_silentCheck)
            {
                ItemNotification item;
                item.type = NOTIFY_VERIFICATION;
                item.name = "TM Condition";
                item.value = "";
                item.comment = "Condition timed out";
                item.status = "FAILED";
                notify( item );
            }

			if (m_condition.promptUser == false)
			{
	            DEBUG("[SCH] Set as timeout, no failure");
	            m_result.type = SPELLscheduleResult::SCH_TIMEOUT;
			}
			else
			{
	            DEBUG("[SCH] Set as timeout, failure");
	            m_result.type = SPELLscheduleResult::SCH_FAILED;
			}
            m_result.error = "Condition not fullfilled";
            m_result.reason = "Timed out";
            return true;
        }
    }
    bool success = false;
    DEBUG("[SCH] Checking verification");
    DEBUG("[SCH] Using TM list " + PYREPR(m_condition.list));
    DEBUG("[SCH] Using config " + PYREPR(m_condition.config));
    // Modify the configuration: we do not want to have a DriverException if the verification fails
    PyObject* verifyConfig = PyDict_Copy(m_condition.config);
    Py_INCREF(verifyConfig);

    PyDict_SetItemString( verifyConfig, LanguageModifiers::PromptUser.c_str(), Py_False );
    PyDict_SetItemString( verifyConfig, LanguageModifiers::OnFalse.c_str(), PyLong_FromLong(LanguageConstants::CANCEL) );
    PyDict_SetItemString( verifyConfig, LanguageModifiers::Retries.c_str(), PyLong_FromLong(0) );

    // Perform the actual TM verification with the driver interface
    DEBUG("[SCH] Calling verify");
    SPELLexecutor::instance().getCIF().setManualMode(true);
    SPELLexecutor::instance().getCIF().setVerbosity(999);
    PyObject* tmResult = NULL;

    // We need to use the safe lock when using the shell
    PyGILState_STATE GILstate;
    if (m_useSafeCalls)
    {
    	GILstate = SPELLpythonHelper::instance().acquireGIL();
    }

    try
    {
		tmResult = SPELLpythonHelper::instance().callMethod(m_pyTM,"verify",m_condition.list,verifyConfig,NULL);
		// Check Python errors
		SPELLpythonHelper::instance().checkError();

		if (tmResult != NULL) Py_INCREF(tmResult);

	    SPELLexecutor::instance().getCIF().setManualMode(false);
	    SPELLexecutor::instance().getCIF().resetVerbosity();
    }
    catch(SPELLcoreException& ex)
    {
        SPELLexecutor::instance().getCIF().setManualMode(false);
        SPELLexecutor::instance().getCIF().resetVerbosity();
    	tmResult = NULL;
        if (!m_silentCheck)
        {
			// Notify to clients
			ItemNotification item;
			item.type = NOTIFY_VERIFICATION;
			item.name = "TM Condition";
			item.value = "";
			item.comment = "Unable to check condition";
			item.status = "FAILED";
			notify( item );
        }
		m_result.type = SPELLscheduleResult::SCH_FAILED;
		m_result.error = "Unable to check condition";
		m_result.reason = ex.what();
        // To finish the verification
        return true;
    }

    Py_XDECREF(verifyConfig);

    if (tmResult)
    {
        DEBUG("[SCH] Checking result");
        PyObject* result = SPELLpythonHelper::instance().callMethod(tmResult,"__nonzero__",NULL);
        Py_XDECREF(tmResult);
        Py_INCREF(result);
        SPELLpythonHelper::instance().checkError();
        DEBUG("[SCH] Evaluation result " + PYREPR(result) + PYREPR(PyObject_Type(result)));
        success = (result==Py_True);
        Py_XDECREF(result);
    }
    DEBUG("[SCH] Final result " + BSTR(success));

    if (m_useSafeCalls)
    {
    	SPELLpythonHelper::instance().releaseGIL(GILstate);
    }

    if (!m_silentCheck)
    {
        if (success)
        {
            SPELLexecutor::instance().getCIF().setManualMode(false);
            SPELLexecutor::instance().getCIF().resetVerbosity();
            // Notify to clients
            ItemNotification item;
            item.type = NOTIFY_VERIFICATION;
            item.name = "TM Condition";
            item.value = "";
            item.comment = "Condition fullfilled";
            item.status = "SUCCESS";
            notify( item );
            message("TM condition fullfilled");
            m_result.type = SPELLscheduleResult::SCH_SUCCESS;
        }
        else
        {
            // Notify to clients
            ItemNotification item;
            item.type = NOTIFY_VERIFICATION;
            item.name = "TM Condition";
            item.value = "";
            item.comment = "Elapsed time: " + (currentTime-m_checkStartTime).toString();
            item.status = "IN PROGRESS";
            notify( item );
        }
    }
    else
    {
    	if (success)
    	{
    		m_result.type = SPELLscheduleResult::SCH_SUCCESS;
    	}
    }
    return success;
}

//=============================================================================
// METHOD    : SPELLscheduler::abortWait
//=============================================================================
bool SPELLscheduler::abortWait( bool setStatus )
{
    DEBUG("[SCH] Aborting wait process");
	SPELLmonitor m(m_checkLock);
    bool aborted = false;
    bool isWaiting = waiting();
    if (setStatus) resetControllerStatus( isWaiting );
    if (isWaiting)
    {
        DEBUG("[SCH] Aborting while waiting");
        if (m_condition.type != SPELLscheduleCondition::SCH_FIXED)
        {
            DEBUG("[SCH] Check aborted, cancelling timer");
            m_abortTimer = true;
            SPELLexecutor::instance().getCIF().resetVerbosity();
            m_result.type = SPELLscheduleResult::SCH_ABORTED;
        }
        aborted = true;
        m_condition.reset();
        // Releasing language lock shall be the last thing done
        releaseLanguageLock();
        DEBUG("[SCH] Wait aborted");
    }
    return aborted;
}

//=============================================================================
// METHOD    : SPELLscheduler::restartWait
//=============================================================================
void SPELLscheduler::restartWait()
{
	SPELLmonitor m(m_checkLock);
    if (waiting())
    {
        DEBUG("[SCH] Restarting wait process");
        SPELLexecutor::instance().getController().setStatus(STATUS_WAITING);
        if (m_condition.type != SPELLscheduleCondition::SCH_FIXED) m_timer->cont();
        DEBUG("[SCH] Wait restarted");
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::interruptWait
//=============================================================================
bool SPELLscheduler::interruptWait()
{
	SPELLmonitor m(m_checkLock);
    // We may continue condition processing, do not release the lock
    bool interrupted = false;
    if (waiting())
    {
        DEBUG("[SCH] Interrupting wait process");
		// We do not want to report any interruption if the condition is of type FIXED.
		// Also, if it is not fixed, we want to stop the checker timer.
        if (m_condition.type != SPELLscheduleCondition::SCH_FIXED)
		{
        	m_timer->stop();
            SPELLexecutor::instance().getController().setStatus(STATUS_INTERRUPTED);
        	SPELLexecutor::instance().getCIF().warning("Wait condition interrupted", LanguageConstants::SCOPE_SYS );
		}
        else
        {
        	SPELLexecutor::instance().getCIF().warning("Procedure will pause", LanguageConstants::SCOPE_SYS );
        }
        interrupted = true;
        DEBUG("[SCH] Wait interrupted");
    }
    return interrupted;
}

//=============================================================================
// METHOD    : SPELLscheduler::finishWait
//=============================================================================
void SPELLscheduler::finishWait( bool setStatus, bool keepLock )
{
	SPELLmonitor m(m_checkLock);
    if (waiting())
    {
        DEBUG("[SCH] Finish wait status");
        m_condition.reset();
        SPELLexecutor::instance().getCIF().resetVerbosity();
        if (setStatus) resetControllerStatus( keepLock );
        // Releasing language lock shall be the last thing done
        if (!keepLock) releaseLanguageLock();
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::waiting
//=============================================================================
bool SPELLscheduler::waiting()
{
    SPELLmonitor m(m_lock);
    return (m_condition.type != SPELLscheduleCondition::SCH_NONE);
}

//=============================================================================
// METHOD    : SPELLscheduler::setLanguageLock
//=============================================================================
void SPELLscheduler::setLanguageLock()
{
    SPELLmonitor m(m_lock);
    DEBUG("[SCH] Setting language lock");
    SPELLexecutor::instance().getController().setStatus(STATUS_WAITING);
    m_waitingEvent.clear();
}


//=============================================================================
// METHOD    : SPELLscheduler::releaseLanguageLock
//=============================================================================
void SPELLscheduler::releaseLanguageLock()
{
    SPELLmonitor m(m_lock);
    DEBUG("[SCH] Releasing language lock");
    m_waitingEvent.set();
}

//=============================================================================
// METHOD    : SPELLscheduler::resetControllerStatus
//=============================================================================
void SPELLscheduler::resetControllerStatus( bool keepLock )
{
    SPELLmonitor m(m_lock);
    DEBUG("[SCH] Resetting controller status");
    SPELLexecutionMode cmode = SPELLexecutor::instance().getController().getMode();
    if (cmode == MODE_STEP)
    {
        DEBUG("[SCH] Set controller status to paused");
		SPELLexecutor::instance().getController().setStatus(STATUS_PAUSED);
    }
    else
    {
        DEBUG("[SCH] Set controller status to " + ( keepLock ? STR("paused") : STR("running")));
		SPELLexecutor::instance().getController().setStatus( keepLock ? STATUS_PAUSED : STATUS_RUNNING );
    }
}

//=============================================================================
// METHOD    : SPELLscheduler::waitCondition
//=============================================================================
bool SPELLscheduler::waitCondition( std::string condition )
{
    LOG_INFO("[SCH] Evaluating procedure launch condition: " + condition)
    bool success = false;
    m_silentCheck = true;
    try
    {
        PyObject* pycond = SPELLpythonHelper::instance().eval(condition,false);
        if (pycond == NULL)
        {
            SPELLexecutor::instance().getCIF().error("Unable to process launch condition: '" + condition + "'", LanguageConstants::SCOPE_SYS );
            LOG_ERROR("[SCH] Unable to process launch condition: '" + condition + "'");
        }
        else
        {
            if (!PyDict_Check(pycond))
            {
                SPELLexecutor::instance().getCIF().error("Unable to process launch condition, not a dictionary: '" + condition + "'", LanguageConstants::SCOPE_SYS );
                LOG_ERROR("[SCH] Unable to process launch condition, not a dictionary: '" + condition + "'");
            }
            else
            {
                SPELLexecutor::instance().getCIF().setManualMode(true);
                SPELLexecutor::instance().getCIF().setVerbosity(999);

                if (PyDict_Contains( pycond, STRPY("verify") ))
                {
                    PyObject* verify = PyDict_GetItemString( pycond, "verify" );
                    Py_INCREF(verify);

                    if (PyList_Check(verify))
                    {
                        SPELLexecutor::instance().getCIF().write("Execution scheduled using telemetry condition: " + PYREPR(verify), LanguageConstants::SCOPE_SYS );

                        SPELLscheduleCondition condition;
                        condition.type = SPELLscheduleCondition::SCH_VERIFICATION;
                        condition.list = verify;
                        condition.config = pycond;

                        startWait( condition );
                        wait();
                        success = true;
                    }
                    else
                    {
                        SPELLexecutor::instance().getCIF().error("Invalid verification: " + PYREPR(verify), LanguageConstants::SCOPE_SYS );
                    }
                }
                else if (PyDict_Contains( pycond, SSTRPY(Until) ))
                {
                    std::string until_time = PYREPR(PyDict_GetItemString( pycond, Until.c_str()));
                    until_time = "TIME(" + until_time + ").abs()";
                    PyObject* theTime = SPELLpythonHelper::instance().eval(until_time,false);

                    if (theTime != NULL)
                    {
                        SPELLexecutor::instance().getCIF().write("Execution scheduled until time: " + PYREPR(theTime), LanguageConstants::SCOPE_SYS );

                        SPELLscheduleCondition condition;
                        condition.type = SPELLscheduleCondition::SCH_TIME;
                        condition.targetTime = SPELLtime( PyLong_AsLong(theTime),0,false );

                        startWait( condition );
                        wait();
                        success = true;
                    }
                    else
                    {
                        SPELLexecutor::instance().getCIF().error("Invalid time value: " + until_time, LanguageConstants::SCOPE_SYS );
                    }
                }
                else if (PyDict_Contains( pycond, SSTRPY(Delay) ))
                {
                    std::string delay_time = PYREPR(PyDict_GetItemString( pycond, Delay.c_str()));
                    delay_time = "TIME(" + delay_time + ").rel()";
                    PyObject* theTime = SPELLpythonHelper::instance().eval(delay_time,false);

                    if (theTime != NULL)
                    {
                        SPELLexecutor::instance().getCIF().write("Execution scheduled until time: " + PYREPR(theTime), LanguageConstants::SCOPE_SYS );

                        SPELLscheduleCondition condition;
                        condition.type = SPELLscheduleCondition::SCH_TIME;
                        condition.targetTime = SPELLtime( PyLong_AsLong(theTime),0,true );

                        startWait( condition );
                        wait();
                        success = true;
                    }
                    else
                    {
                        SPELLexecutor::instance().getCIF().error("Invalid time value: " + delay_time, LanguageConstants::SCOPE_SYS );
                    }
                }
                else
                {
                    SPELLexecutor::instance().getCIF().error("Unable to process launch condition, not a dictionary: '" + condition + "'", LanguageConstants::SCOPE_SYS );
                    LOG_ERROR("[SCH] Unable to process launch condition, not a dictionary: '" + condition + "'");
                }

                SPELLexecutor::instance().getCIF().setManualMode(false);
                SPELLexecutor::instance().getCIF().resetVerbosity();
            }
        }
    }
    catch(SPELLcoreException& ex)
    {
        SPELLexecutor::instance().getCIF().error("Unable to process launch condition (ex): '" + condition + "'", LanguageConstants::SCOPE_SYS );
        LOG_ERROR("[SCH] Unable to process launch condition (ex): '" + condition + "'");
    }
    m_silentCheck = false;
    return success;
}

//=============================================================================
// METHOD    : SPELLscheduler::notify()
//=============================================================================
void SPELLscheduler::notify( const ItemNotification& item )
{
    if (m_silentCheck) return;
    if (m_lastNotifiedItem.name  == item.name &&
    	m_lastNotifiedItem.value == item.value &&
    	m_lastNotifiedItem.status == item.status &&
    	m_lastNotifiedItem.comment == item.comment) return;
    m_lastNotifiedItem = item;
    SPELLexecutor::instance().getCIF().notify(item);
}

//=============================================================================
// METHOD    : SPELLscheduler::message()
//=============================================================================
void SPELLscheduler::message( const std::string& message )
{
    if (m_silentCheck) return;
    SPELLexecutor::instance().getCIF().write(message, LanguageConstants::SCOPE_SYS );
}
