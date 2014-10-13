// ################################################################################
// FILE       : SPELLserverCif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the CIF for server environment
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
// Local includes ----------------------------------------------------------
#include "SPELL_CIFS/SPELLserverCif.H"
// Project includes --------------------------------------------------------
#include "SPELL_CIF/SPELLcifHelper.H"
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLexecutorUtils.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcMessage.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_SYN/SPELLsyncError.H"
#include "SPELL_UTIL/SPELLtime.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
#include "SPELL_IPC/SPELLtimeoutValues.H"
#include "SPELL_CFG/SPELLconfiguration.H"
#include "SPELL_IPC/SPELLipc_Context.H"
#include "SPELL_IPC/SPELLipc_Executor.H"
// System includes ---------------------------------------------------------



// DEFINES /////////////////////////////////////////////////////////////////
#define MAX_MSEC_MSG_DELAY 10
#define ACKNOWLEDGE_TIMEOUT_SEC 10
// GLOBALS /////////////////////////////////////////////////////////////////
SPELLserverCif* s_handle = NULL;


//=============================================================================
// CONSTRUCTOR: SPELLserverCif::SPELLserverCif
//=============================================================================
SPELLserverCif::SPELLserverCif()
    : SPELLcif(),
      SPELLipcInterfaceListener(),
      m_wrMessage( MessageId::MSG_ID_WRITE ),
      m_lnMessage( MessageId::MSG_ID_NOTIFICATION ),
      m_ntMessage( MessageId::MSG_ID_NOTIFICATION ),
      m_stMessage( MessageId::MSG_ID_NOTIFICATION ),
      m_lineTimer( 500, *this )
{
    DEBUG("[CIF] Created server CIF");
    m_ifc = NULL;
    m_buffer = NULL;
    m_visible = true;
    m_automatic = false;
    m_blocking = true;
    m_controlHost = "";
    m_condition = "";
    m_arguments = "";
    m_ready = false;
    m_finishEvent.clear();
    m_errorState = false;
    m_sequence = 0;
    m_sequenceStack = 0;

    m_stMessage.setType(MSG_TYPE_NOTIFY);
    m_stMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_STATUS );

    m_ntMessage.setType(MSG_TYPE_NOTIFY);
    m_ntMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_ITEM );

    m_wrMessage.setType(MSG_TYPE_WRITE);

    m_promptMessage = VOID_MESSAGE;

    s_handle = this;
    m_closing = false;

    m_lastStack = "";

	m_ipcTimeoutGuiRequestMsec = SPELLconfiguration::instance().commonOrDefault( "GuiRequestTimeout", IPC_GUIREQUEST_DEFAULT_TIMEOUT_MSEC );
	m_ipcTimeoutCtxRequestMsec = SPELLconfiguration::instance().commonOrDefault( "CtxRequestTimeout", IPC_CTXREQUEST_DEFAULT_TIMEOUT_MSEC );
	m_timeoutOpenProcMsec = SPELLconfiguration::instance().commonOrDefault( "OpenProcTimeout", IPC_OPENPROC_DEFAULT_TIMEOUT_MSEC );
	m_timeoutExecLoginMsec = SPELLconfiguration::instance().commonOrDefault( "ExecutorLoginTimeout", IPC_EXLOGIN_DEFAULT_TIMEOUT_MSEC );
}

//=============================================================================
// DESTRUCTOR: SPELLserverCif::~SPELLserverCif
//=============================================================================
SPELLserverCif::~SPELLserverCif()
{
    if (m_ifc != NULL)
	{
    	delete m_ifc;
    	m_ifc = NULL;
	}
    if (m_buffer != NULL)
    {
    	delete m_buffer;
    	m_buffer = NULL;
	}
}

//=============================================================================
// METHOD: SPELLserverCif::setup
//=============================================================================
void SPELLserverCif::setup( const SPELLcifStartupInfo& info )
{
    SPELLcif::setup(info);

    m_wrMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_stMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_lnMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_ntMessage.set( MessageField::FIELD_PROC_ID, getProcId() );

    DEBUG("[CIF] Setup server CIF");
    m_ifc = new SPELLipcClientInterface( "EXEC-TO-CTX", "0.0.0.0", info.contextPort );
    m_buffer = new SPELLdisplayBuffer( getProcId(), *this );
    m_buffer->start();

    // Initialize message sequencer
    m_sequence = 0;
    m_sequenceStack = 0;
    m_errorState = false;

    m_ifc->initialize( this );

    DEBUG("[CIF] Connecting to context");
    m_ifc->connect();

    // Perform login
    SPELLipcMessage response = login();
    processLogin(response);

    m_ipcInterruptionNotified = false;

    m_ready = true;
    DEBUG("[CIF] Ready to go");
}

//=============================================================================
// METHOD: SPELLserverCif::cleanup
//=============================================================================
void SPELLserverCif::cleanup( bool force )
{
    SPELLcif::cleanup(force);
    m_ready = false;
    DEBUG("[CIF] Cleaning server CIF");
    cancelPrompt();
    m_buffer->stop();
    m_buffer->join();
    DEBUG("[CIF] Disconnecting server CIF");
    if (!force)
    {
        DEBUG("[CIF] Send logout message to context");
        logout();
    }

    DEBUG("[CIF] Disconnecting IPC");

    m_ifc->disconnect();
    // Release pending requests
    m_ipcLock.unlock();
    DEBUG("[CIF] Cleanup server CIF finished");
}

//=============================================================================
// METHOD: SPELLserverCif::canClose
//=============================================================================
void SPELLserverCif::canClose()
{
    m_finishEvent.set();
}

//=============================================================================
// METHOD: SPELLserverCif::waitClose
//=============================================================================
void SPELLserverCif::waitClose()
{
    m_finishEvent.wait();
}

//=============================================================================
// METHOD: SPELLserverCif::resetClose
//=============================================================================
void SPELLserverCif::resetClose()
{
    // Reset the last notified info. In case of recovery we want
    // the initial line notification.
    m_lastStack = "";
    m_errorState = false;
    // Clear the finish event so that we can wait for final commands next time
    if (!m_finishEvent.isClear())
    {
        m_finishEvent.clear();
    }
}

//=============================================================================
// METHOD: SPELLserverCif::timerCallback
//=============================================================================
bool SPELLserverCif::timerCallback( unsigned long usecs )
{
	return true;
}

//=============================================================================
// METHOD: SPELLserverCif::logout
//=============================================================================
void SPELLserverCif::logout()
{
    DEBUG("[CIF] Sending logout message");

    SPELLipcMessage logoutmsg( ExecutorMessages::MSG_NOTIF_EXEC_CLOSE);
    logoutmsg.setType(MSG_TYPE_ONEWAY);
    logoutmsg.setSender(getProcId());
    logoutmsg.setReceiver("CTX");
    logoutmsg.set( MessageField::FIELD_PROC_ID, getProcId());

    m_ifc->sendMessage(logoutmsg);
}

//=============================================================================
// METHOD: SPELLserverCif::login
//=============================================================================
SPELLipcMessage SPELLserverCif::login()
{
    DEBUG("[CIF] Sending login message");

    // Create the login message
    SPELLipcMessage loginmsg( ExecutorMessages::REQ_NOTIF_EXEC_OPEN);
    loginmsg.setType(MSG_TYPE_REQUEST);
    loginmsg.setSender(getProcId());
    loginmsg.setReceiver("CTX");
    loginmsg.set( MessageField::FIELD_PROC_ID, getProcId());
    loginmsg.set( MessageField::FIELD_CSP, getProcId());

    // status loaded is not correct if the procedure did not compile, but the error will be
    // notified afterwards anyhow.
    loginmsg.set( MessageField::FIELD_EXEC_STATUS, SPELLexecutorUtils::statusToString(STATUS_LOADED));
    loginmsg.set( MessageField::FIELD_ASRUN_NAME, getAsRunName() );
    loginmsg.set( MessageField::FIELD_LOG_NAME, SPELLlog::instance().getLogFile() );

    LOG_INFO("Login information:");
    LOG_INFO("Proc  : " + getProcId() );
    LOG_INFO("Status: LOADED");
    LOG_INFO("AsRun : " + getAsRunName() );
    LOG_INFO("Log   : " + SPELLlog::instance().getLogFile() );

    // Send the login message.
    // ERRORS: if there is a timeout in this request, an SPELLipcError exception will
    // be thrown and will make the executor die
    SPELLipcMessage response = m_ifc->sendRequest(loginmsg, m_timeoutExecLoginMsec);

    DEBUG("[CIF] Login done");
    return response;
}

//=============================================================================
// METHOD: SPELLserverCif::getTimestampUsec
//=============================================================================
std::string SPELLserverCif::getTimestampUsec()
{
	const std::string complete("000000");
	struct timespec abstime;
	clock_gettime(CLOCK_REALTIME, &abstime);
    std::string sec  = ISTR(abstime.tv_sec);
    std::string usec = ISTR(abstime.tv_nsec/1000);
    if (usec.length()<6)
    {
    	usec += complete.substr(0,6-usec.length());
    }
    return (sec + usec);
}

//=============================================================================
// METHOD: SPELLserverCif::processLogin
//=============================================================================
void SPELLserverCif::processLogin( const SPELLipcMessage& loginResp )
{
    DEBUG("[CIF] Processing login response");

    m_visible = true;
    m_automatic = false;
    m_blocking = true;
    std::string openMode = loginResp.get(MessageField::FIELD_OPEN_MODE);
    if (openMode != "")
    {
    	openMode = openMode.substr(1, openMode.size()-2);
        std::vector<std::string> pairs = SPELLutils::tokenize( openMode, ",");
        std::vector<std::string>::iterator it;
        for( it = pairs.begin(); it != pairs.end(); it++)
        {
            std::vector<std::string> kv = SPELLutils::tokenize( *it, ":" );
            if (kv.size()!=2) continue;
            std::string modifier = kv[0];
            std::string value = kv[1];
            SPELLutils::trim(modifier);
            SPELLutils::trim(value);
            if (modifier.find(LanguageModifiers::Automatic) != std::string::npos)
            {
                if (value == PythonConstants::True) m_automatic = true;
            }
            else if (modifier.find(LanguageModifiers::Visible) != std::string::npos)
            {
                if (value == PythonConstants::False) m_visible = false;
            }
            else if (modifier.find(LanguageModifiers::Blocking) != std::string::npos)
            {
                if (value == PythonConstants::False) m_blocking = false;
            }
        }
    }
    else
    {
    	LOG_WARN("Open mode not set, using defaults");
    }

    if (loginResp.hasField(MessageField::FIELD_GUI_CONTROL))
    {
        m_controlGui = loginResp.get(MessageField::FIELD_GUI_CONTROL);
    }

    if (loginResp.hasField(MessageField::FIELD_GUI_CONTROL_HOST))
    {
        m_controlHost = loginResp.get(MessageField::FIELD_GUI_CONTROL_HOST);
    }

    if (loginResp.hasField(MessageField::FIELD_CONDITION))
    {
        m_condition   = loginResp.get(MessageField::FIELD_CONDITION);
    }

    if (loginResp.hasField(MessageField::FIELD_ARGS))
    {
        m_arguments   = loginResp.get(MessageField::FIELD_ARGS);
    }

    LOG_INFO("Automatic mode: " + (m_automatic ? STR("yes") : STR("no")));
    LOG_INFO("Blocking mode : " + (m_blocking ? STR("yes") : STR("no")));
    LOG_INFO("Visible mode  : " + (m_visible ? STR("yes") : STR("no")));
    LOG_INFO("Arguments     : " + m_arguments);
    LOG_INFO("Browsable lib : " + BSTR(m_browsableLib));
    LOG_INFO("Condition     : " + m_condition);
    LOG_INFO("Control gui   : " + m_controlGui);
    LOG_INFO("Control host  : " + m_controlHost);
    //DEBUG("LOGIN MSG: " + loginResp.data())
}

//=============================================================================
// METHOD: SPELLserverCif::sendGUIRequest
//=============================================================================
SPELLipcMessage SPELLserverCif::sendGUIRequest( const SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    static std::string procId = getProcId();
    SPELLipcMessage response = VOID_MESSAGE;
	SPELLmonitor m(m_ipcLock);
    if (m_ready)
    {
        //DEBUG("[CIF] Sending GUI request: " + msg.dataStr());
        SPELLipcMessage toSend(msg);
        toSend.setSender(procId);
        toSend.setReceiver("GUI");
        try
        {
        	//DEBUG("[CIF] Request timeout is " + ISTR(timeoutMsec) + " ms");
        	//RACC 15-MAY SPELLsafeThreadOperations ops;
        	response = m_ifc->sendRequest(toSend, timeoutMsec);

        	if (!response.isVoid())
        	{
        		if ((response.getType() == MSG_TYPE_ERROR)&&( response.getId() == MessageId::MSG_PEER_LOST ))
        		{
        			LOG_ERROR("Unable to communicate with GUI: " + msg.getId());
        		}
        	}
        }
        catch(SPELLipcError& ex)
        {
        	LOG_ERROR("Unable to communicate with GUI: " + std::string(ex.what()));
        }
    }
    else
    {
    	LOG_ERROR("GUI request not sent: not ready");
    }
    return response;
}

//=============================================================================
// METHOD: SPELLserverCif::sendGUIMessage
//=============================================================================
void SPELLserverCif::sendGUIMessage( const SPELLipcMessage& msg )
{
    static std::string procId = getProcId();
    if (m_ready)
    {
    	try
    	{
			//DEBUG("[CIF] Sending GUI message");
			SPELLipcMessage toSend(msg);
			toSend.setSender(procId);
			toSend.setReceiver("GUI");
			m_ifc->sendMessage(toSend);
    	}
        catch(SPELLipcError& ex)
        {
        	LOG_ERROR("Unable to communicate with Context: " + std::string(ex.what()));
        	SPELLexecutor::instance().pause();
        }
    }
}

//=============================================================================
// METHOD: SPELLserverCif::sendCTXRequest
//=============================================================================
SPELLipcMessage SPELLserverCif::sendCTXRequest( const SPELLipcMessage& msg, unsigned long timeoutMsec )
{
    static std::string procId = getProcId();
    SPELLipcMessage response = VOID_MESSAGE;
	SPELLmonitor m(m_ipcLock);
    if (m_ready)
    {
        try
        {
			//DEBUG("[CIF] Sending CTX request: " + msg.dataStr());
			SPELLipcMessage toSend(msg);
			toSend.setSender(procId);
			toSend.setReceiver("CTX");
			response = m_ifc->sendRequest(toSend,timeoutMsec);
			//DEBUG("[CIF] Got CTX response");
        	if ((response.getType() == MSG_TYPE_ERROR)&&( response.getId() == MessageId::MSG_PEER_LOST ))
        	{
            	LOG_ERROR("Unable to communicate with Context: " + msg.getId());
        	}
        }
        catch(SPELLipcError& ex)
        {
        	LOG_ERROR("Unable to communicate with Context: " + std::string(ex.what()));
        	if (SPELLexecutor::instance().getStatus() != STATUS_PAUSED)
        	{
        		SPELLexecutor::instance().pause();
        	}
        }
    }
    return response;
}

//=============================================================================
// METHOD: SPELLserverCif::sendCTXMessage
//=============================================================================
void SPELLserverCif::sendCTXMessage( const SPELLipcMessage& msg )
{
    static std::string procId = getProcId();
    if (m_ready)
    {
        //DEBUG("[CIF] Sending CTX message");
        SPELLipcMessage toSend(msg);
        toSend.setSender(procId);
        toSend.setReceiver("CTX");
        m_ifc->sendMessage(toSend);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::completeMessage
//=============================================================================
void SPELLserverCif::completeMessage( SPELLipcMessage& msg )
{
    msg.set(MessageField::FIELD_TIME, SPELLutils::timestamp() );
	msg.set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequence));
	m_sequence++;

    std::string stack = SPELLexecutor::instance().getCallstack().getFullStack();

    if (stack == "")
    {
    	stack = SPELLexecutor::instance().getCallstack().getStack();
    }

    msg.set(MessageField::FIELD_CSP, stack + "/" + ISTR(getNumExecutions()) );

    if (isManual())
    {
    	msg.set(MessageField::FIELD_EXECUTION_MODE, MessageValue::DATA_EXEC_MODE_MANUAL);
    }
    else
    {
    	msg.set(MessageField::FIELD_EXECUTION_MODE, MessageValue::DATA_EXEC_MODE_PROCEDURE);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::notifyLine
//=============================================================================
void SPELLserverCif::notifyLine()
{
	TICK_IN;

	SPELLmonitor m(m_lineLock);

    m_lnMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_LINE );
	m_lnMessage.setType(MSG_TYPE_NOTIFY_ASYNC);

    completeMessage( m_lnMessage );
    m_lnMessage.set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequenceStack));
	m_sequenceStack++;
	m_lnMessage.set(MessageField::FIELD_CODE_NAME, getCodeName() );

	std::string stack = m_lnMessage.get(MessageField::FIELD_CSP);

    if (m_lastStack == stack)
	{
    	return;
	}

    if (m_errorState)
    {
		return;
    }

    m_lastStack = stack;
    std::string stage = getStage();

    if (stage.find(":") != std::string::npos)
    {
        std::vector<std::string> stage_title = SPELLutils::tokenize(stage,":");
        if (stage_title.size()==2)
        {
        	m_lnMessage.set(MessageField::FIELD_STAGE_ID,stage_title[0]);
        	m_lnMessage.set(MessageField::FIELD_STAGE_TL,stage_title[1]);
        }
        else
        {
            m_lnMessage.set(MessageField::FIELD_STAGE_ID,"(none)");
            m_lnMessage.set(MessageField::FIELD_STAGE_TL,"(none)");
        }
    }
    else
    {
        m_lnMessage.set(MessageField::FIELD_STAGE_ID,stage);
        m_lnMessage.set(MessageField::FIELD_STAGE_TL,stage);
    }

    m_asRun->writeLine( stack, (m_sequence-1) );

	sendGUIMessage(m_lnMessage);

	TICK_OUT;
}

//=============================================================================
// METHOD: SPELLserverCif::notifyCall
//=============================================================================
void SPELLserverCif::notifyCall()
{
	if (m_errorState) return;

    m_lnMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_CALL );
	m_lnMessage.setType(MSG_TYPE_NOTIFY);

    completeMessage( m_lnMessage );
    m_lnMessage.set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequenceStack));
	m_sequenceStack++;
	m_lnMessage.set(MessageField::FIELD_CODE_NAME, getCodeName() );

    std::string stack = m_lnMessage.get(MessageField::FIELD_CSP);
    m_asRun->writeCall( stack, (m_sequence-1) );

    DEBUG("[CIF] Procedure call: " + stack );

	sendGUIMessage(m_lnMessage);
    waitAcknowledge(m_lnMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyReturn
//=============================================================================
void SPELLserverCif::notifyReturn()
{
    DEBUG("[CIF] Procedure return");

    if (m_errorState) return;

    m_lnMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_RETURN );
	m_lnMessage.setType(MSG_TYPE_NOTIFY);

    completeMessage( m_lnMessage );
	m_lnMessage.set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequenceStack));
	m_sequenceStack++;
	m_lnMessage.set(MessageField::FIELD_CODE_NAME, getCodeName() );

    m_asRun->writeReturn( (m_sequence-1) );

    sendGUIMessage(m_lnMessage);
    waitAcknowledge(m_lnMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyStatus
//=============================================================================
void SPELLserverCif::notifyStatus( const SPELLstatusInfo& st )
{
    DEBUG("Status notification: " + SPELLexecutorUtils::statusToString(st.status) + " (" + st.condition + ")");

    m_stMessage.set(MessageField::FIELD_EXEC_STATUS, SPELLexecutorUtils::statusToString(st.status));

    completeMessage( m_stMessage );

    // Condition information
    if (st.condition.size()>0)
    {
        m_stMessage.set( MessageField::FIELD_CONDITION, st.condition );
    }

    // Action information
    if (st.actionLabel != "")
    {
    	m_stMessage.set( MessageField::FIELD_ACTION_LABEL, st.actionLabel );
    	m_stMessage.set( MessageField::FIELD_ACTION_ENABLED, st.actionEnabled ? MessageValue::DATA_TRUE : MessageValue::DATA_FALSE );
    }

    m_asRun->writeStatus( st.status );

    sendGUIMessage(m_stMessage);
    waitAcknowledge(m_stMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyUserActionSet
//=============================================================================
void SPELLserverCif::notifyUserActionSet( const std::string& label, const unsigned int severity )
{
    SPELLipcMessage actionMessage(MessageId::MSG_ID_SET_UACTION);
    actionMessage.setType(MSG_TYPE_ONEWAY);
    actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    actionMessage.set( MessageField::FIELD_ACTION_LABEL, label );
    switch(severity)
    {
    case LanguageConstants::INFORMATION:
		actionMessage.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_INFO );
		break;
    case LanguageConstants::WARNING:
		actionMessage.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_WARN );
		break;
    case LanguageConstants::ERROR:
		actionMessage.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_ERROR );
		break;
    default:
		actionMessage.set( MessageField::FIELD_ACTION_SEVERITY, MessageValue::DATA_SEVERITY_INFO );
		break;
    }
    sendGUIMessage(actionMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyUserActionUnset
//=============================================================================
void SPELLserverCif::notifyUserActionUnset()
{
    SPELLipcMessage actionMessage(MessageId::MSG_ID_DISMISS_UACTION);
    actionMessage.setType(MSG_TYPE_ONEWAY);
    actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    sendGUIMessage(actionMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyUserActionEnable
//=============================================================================
void SPELLserverCif::notifyUserActionEnable( bool enable )
{
    if (enable)
    {
        SPELLipcMessage actionMessage(MessageId::MSG_ID_ENABLE_UACTION);
        actionMessage.setType(MSG_TYPE_ONEWAY);
        actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
        sendGUIMessage(actionMessage);
    }
    else
    {
        SPELLipcMessage actionMessage(MessageId::MSG_ID_DISABLE_UACTION);
        actionMessage.setType(MSG_TYPE_ONEWAY);
        actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
        sendGUIMessage(actionMessage);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::notify
//=============================================================================
void SPELLserverCif::notify( ItemNotification notification )
{
    //DEBUG("[CIF] Item notification");
    if (!notificationsEnabled()) return;

    completeMessage( m_ntMessage );

    //DEBUG("[CIF] Processing status");
    int sCount = 0;
    if (notification.status.find(IPCinternals::ARG_SEPARATOR) != std::string::npos )
    {
        std::vector<std::string> statusList = SPELLutils::tokenize(notification.status,IPCinternals::ARG_SEPARATOR);
        std::vector<std::string>::iterator it;
        for( it = statusList.begin(); it != statusList.end(); it++)
        {
            if (*it == MessageValue::DATA_NOTIF_STATUS_OK) sCount++;
        }
        std::vector<std::string> commentList = SPELLutils::tokenize(notification.comment,IPCinternals::ARG_SEPARATOR);
        if (commentList.size() == 1)
        {
            notification.comment = "";
            for( unsigned int count=0; count<statusList.size(); count++)
            {
                if (notification.comment.size()>0) notification.comment += IPCinternals::ARG_SEPARATOR;
                notification.comment += " ";
            }
        }
        std::vector<std::string> timeList = SPELLutils::tokenize(notification.time, IPCinternals::ARG_SEPARATOR);
        if (timeList.size() == 1)
        {
            notification.time = "";
            std::string tstamp = SPELLutils::timestamp();
            for( unsigned int count=0; count<statusList.size(); count++)
            {
                if (notification.time.size()>0) notification.time += IPCinternals::ARG_SEPARATOR;
                notification.time += tstamp;
            }
        }
    }
    else
    {
        sCount = (notification.status == MessageValue::DATA_NOTIF_STATUS_OK) ? 1 : 0;
    }

    std::stringstream buffer;
    buffer << sCount;

    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_TYPE, NOTIF_TYPE_STR[notification.type]);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_NAME, notification.name);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_VALUE, notification.value);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_STATUS, notification.status);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_REASON, notification.comment);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_TIME, notification.time);
    m_ntMessage.set(MessageField::FIELD_NOTIF_ITEM_SCOUNT, buffer.str());

    //DEBUG("[CIF] Message prepared, sending");

    m_asRun->writeItem( m_ntMessage.get(MessageField::FIELD_CSP),
						NOTIF_TYPE_STR[notification.type],
                        notification.name,
                        notification.value,
                        notification.status,
                        notification.comment,
                        notification.time );

    sendGUIMessage(m_ntMessage);
    waitAcknowledge(m_ntMessage);
    //DEBUG("[CIF] Notification sent");
}

//=============================================================================
// METHOD: SPELLserverCif::waitAcknowledge()
//=============================================================================
void SPELLserverCif::waitAcknowledge( const SPELLipcMessage& msg )
{
    if (m_ready && !m_ipcInterruptionNotified)
    {
    	bool found = false;
    	std::string seq = msg.get(MessageField::FIELD_MSG_SEQUENCE);
    	SPELLtime waitStart;
    	while (!found)
    	{
    		std::vector<std::string>::iterator it;
    		std::map<std::string,SPELLtime>::iterator mit;
    		{
        		SPELLmonitor m(m_ackLock);

        		it = std::find(m_ackSequences.begin(), m_ackSequences.end(), seq);

        		found = it != m_ackSequences.end();
        		if (found)
        		{
        			m_ackSequences.erase(it);
        		}
        		else
        		{
        			SPELLtime now;
					SPELLtime delta = now - waitStart;
					if (delta.getSeconds()>ACKNOWLEDGE_TIMEOUT_SEC)
					{
						LOG_WARN("#########################");
						LOG_WARN("Acknowledge not received!");
						LOG_WARN(msg.dataStr());
						LOG_WARN("#########################");
						if (!m_ipcInterruptionNotified)
						{
							m_ipcInterruptionNotified = true;
							SPELLexecutorStatus st = SPELLexecutor::instance().getStatus();
							if (st == STATUS_WAITING || st == STATUS_RUNNING )
							{
								ExecutorCommand cmd;
								cmd.id = CMD_PAUSE;
								warning("Communication with controlling GUI interrupted. Procedure paused for safety.", LanguageConstants::SCOPE_SYS);
								warning("You may try to resume execution.", LanguageConstants::SCOPE_SYS);
								SPELLexecutor::instance().command(cmd,false);
							}
						}
						return;
					}
        		}
    		}
    		if (!m_ready) return;
    		usleep(52000);
    	}
    }
}

//=============================================================================
// METHOD: SPELLserverCif::notifyError
//=============================================================================
void SPELLserverCif::notifyError( const std::string& error, const std::string& reason, bool fatal )
{
    LOG_ERROR("[CIF] Error notification: " + error + " (" + reason + ")");


    m_errorState = true;

    SPELLipcMessage errorMsg( MessageId::MSG_ID_ERROR);
    errorMsg.setType(MSG_TYPE_ERROR);
    errorMsg.set( MessageField::FIELD_PROC_ID, getProcId() );
    errorMsg.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_STATUS );
    errorMsg.set( MessageField::FIELD_EXEC_STATUS, SPELLexecutorUtils::statusToString(STATUS_ERROR) );
    errorMsg.set( MessageField::FIELD_ERROR, error );
    errorMsg.set( MessageField::FIELD_REASON, reason );
    if (fatal)
    {
        errorMsg.set( MessageField::FIELD_FATAL, PythonConstants::True );
    }
    else
    {
        errorMsg.set( MessageField::FIELD_FATAL, PythonConstants::False );
    }

    completeMessage( errorMsg );

    m_asRun->writeErrorInfo( error, reason );

    sendGUIMessage(errorMsg);
}

//=============================================================================
// METHOD: SPELLserverCif::write
//=============================================================================
void SPELLserverCif::write( const std::string& msg, unsigned int scope )
{
    if ( getVerbosity() > getVerbosityFilter() ) return;

    if (m_errorState && scope != LanguageConstants::SCOPE_SYS) return;

    // We shall not bufferize in manual mode
    if (isManual())
    {
        completeMessage( m_wrMessage );
        std::string timeStr = getTimestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_INFO);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(m_wrMessage);
    }
    else
    {
    	m_buffer->write( msg, scope );
    }

    m_asRun->writeInfo( getStack(), msg, scope );
}

//=============================================================================
// METHOD: SPELLserverCif::warning
//=============================================================================
void SPELLserverCif::warning( const std::string& msg, unsigned int scope )
{
    if ( getVerbosity() > getVerbosityFilter() ) return;

    //DEBUG("[CIF] Warning message: " + msg);

    // We shall not bufferize in manual mode
    if (isManual())
    {
        completeMessage( m_wrMessage );
        std::string timeStr = getTimestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_WARN);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(m_wrMessage);
    }
    else
    {
    	m_buffer->warning( msg, scope );
    }

    m_asRun->writeWarning( getStack(), msg, scope );
}

//=============================================================================
// METHOD: SPELLserverCif::error
//=============================================================================
void SPELLserverCif::error( const std::string& msg, unsigned int scope )
{
    if ( getVerbosity() > getVerbosityFilter() ) return;

    //DEBUG("[CIF] Error message: " + msg);

    // We shall not bufferize in manual mode
    if (isManual())
    {
        completeMessage( m_wrMessage );
        std::string timeStr = getTimestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_ERROR);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(m_wrMessage);
    }
    else
    {
    	m_buffer->error( msg, scope );
    }

    m_asRun->writeError( getStack(), msg, scope );
}

//=============================================================================
// METHOD: SPELLserverCif::notifyVariableChange()
//=============================================================================
void SPELLserverCif::notifyVariableChange( const std::vector<SPELLvarInfo>& changed )
{
	SPELLipcMessage notifyMsg(ExecutorMessages::MSG_VARIABLE_CHANGE);
	notifyMsg.setType(MSG_TYPE_ONEWAY);

	std::string names = "";
	std::string types = "";
	std::string values = "";
	std::string globals = "";

	for( unsigned int index = 0; index<changed.size(); index++)
	{
		if (names != "")
		{
			names += VARIABLE_SEPARATOR;
			types += VARIABLE_SEPARATOR;
			values += VARIABLE_SEPARATOR;
			globals += VARIABLE_SEPARATOR;
		}
		names += changed[index].varName;
		types += changed[index].varType;
		values += changed[index].varValue;
		globals += changed[index].isGlobal ? "True" : "False";
	}

    notifyMsg.set( MessageField::FIELD_PROC_ID, getProcId() );
	notifyMsg.set(MessageField::FIELD_VARIABLE_NAME,   names);
	notifyMsg.set(MessageField::FIELD_VARIABLE_TYPE,   types);
	notifyMsg.set(MessageField::FIELD_VARIABLE_VALUE,  values);
	notifyMsg.set(MessageField::FIELD_VARIABLE_GLOBAL, globals);

	sendGUIMessage(notifyMsg);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyVariableScopeChange()
//=============================================================================
void SPELLserverCif::notifyVariableScopeChange( const SPELLscopeInfo& info )
{
	SPELLipcMessage notifyMsg(ExecutorMessages::MSG_SCOPE_CHANGE);
	notifyMsg.setType(MSG_TYPE_ONEWAY);
    notifyMsg.set( MessageField::FIELD_PROC_ID, getProcId() );
	std::string varNames  = "";
	std::string varTypes  = "";
	std::string varValues = "";
	std::string varGlobals = "";

	for( unsigned int index = 0; index < info.globalRegisteredVariables.size(); index++)
	{
		if (varNames != "")
		{
			varNames += VARIABLE_SEPARATOR;
			varTypes += VARIABLE_SEPARATOR;
			varValues += VARIABLE_SEPARATOR;
			varGlobals += VARIABLE_SEPARATOR;
		}
		varNames += info.globalRegisteredVariables[index].varName;
		varTypes += info.globalRegisteredVariables[index].varType;
		varValues += info.globalRegisteredVariables[index].varValue;
		varGlobals += "True";
	}

	for( unsigned int index = 0; index < info.localRegisteredVariables.size(); index++)
	{
		if (varNames != "")
		{
			varNames += VARIABLE_SEPARATOR;
			varTypes += VARIABLE_SEPARATOR;
			varValues += VARIABLE_SEPARATOR;
			varGlobals += VARIABLE_SEPARATOR;
		}
		varNames += info.localRegisteredVariables[index].varName;
		varTypes += info.localRegisteredVariables[index].varType;
		varValues += info.localRegisteredVariables[index].varValue;
		varGlobals += "False";
	}

	notifyMsg.set(MessageField::FIELD_VARIABLE_NAME,   varNames);
	notifyMsg.set(MessageField::FIELD_VARIABLE_TYPE,   varTypes);
	notifyMsg.set(MessageField::FIELD_VARIABLE_VALUE,  varValues);
	notifyMsg.set(MessageField::FIELD_VARIABLE_GLOBAL, varGlobals);

	sendGUIMessage(notifyMsg);
}

//=============================================================================
// METHOD: SPELLserverCif::prompt
//=============================================================================
std::string SPELLserverCif::prompt( const SPELLpromptDefinition& def )
{
    DEBUG("[CIF] Prompt message");

    std::string timeStr = getTimestampUsec();

    m_promptMessage = SPELLipcMessage(MessageId::MSG_ID_PROMPT);

    completeMessage( m_promptMessage );

    m_promptMessage.setType(MSG_TYPE_PROMPT);
    m_promptMessage.set(MessageField::FIELD_PROC_ID, getProcId() );
    m_promptMessage.set(MessageField::FIELD_TEXT, def.message);
    m_promptMessage.set(MessageField::FIELD_DATA_TYPE, ISTR(def.typecode));
    m_promptMessage.set(MessageField::FIELD_TIME, timeStr);
    m_promptMessage.set(MessageField::FIELD_SCOPE, ISTR(def.scope));

    DEBUG("[CIF] Prompt typecode " + ISTR(def.typecode));

    // the prompt display
    std::string msgToShow = def.message;

    if ( def.options.size() == 0 )
    {
        DEBUG("[CIF] Prompt is simple");
        m_promptMessage.set(MessageField::FIELD_EXPECTED, "");
    }
    else
    {
		msgToShow += "\nAvailable options:\n";

        DEBUG("[CIF] Prompt has options");
        int keyCount = 0;
        std::string optionStr = "";
        std::string expectedStr = "";

        // Iterate over the option list and build the option and expected values strings
        SPELLpromptDefinition::Options::const_iterator it;
        for( it = def.options.begin(); it != def.options.end(); it++)
        {
            if (optionStr.size()>0) optionStr += IPCinternals::OPT_SEPARATOR;
            if (expectedStr.size()>0) expectedStr += IPCinternals::OPT_SEPARATOR;
            optionStr += (*it);
            expectedStr += def.expected[keyCount];
            keyCount++;

            // For the display message
    		msgToShow += "   - " + (*it) + "\n";
        }
        m_promptMessage.set(MessageField::FIELD_EXPECTED, expectedStr);
        m_promptMessage.set(MessageField::FIELD_OPTIONS, optionStr);
    }
    DEBUG("[CIF] Option string: " + m_promptMessage.get(MessageField::FIELD_OPTIONS));
    DEBUG("[CIF] Expected string: " + m_promptMessage.get(MessageField::FIELD_EXPECTED));

    // Send the display message via the buffer to ensure synchronization
    m_buffer->prompt(msgToShow, LanguageConstants::SCOPE_PROMPT);

    // Ensure buffer is flushed
    m_buffer->flush();

    // Write the prompt in the asrun
    m_asRun->writePrompt( getStack(), def );

    DEBUG("[CIF] Messsage prepared");

    // Start prompt status and send request to client
    startPrompt();
    waitPromptAnswer();

    DEBUG("[CIF] Prompt response received");

    // Process prompt response
    std::string toProcess = "";
    if (m_promptAnswer.getId() == MessageId::MSG_ID_CANCEL)
    {
    	LOG_WARN("Prompt cancelled");
        DEBUG("[CIF] Prompt cancelled");
        // Abort execution in this case
        SPELLexecutor::instance().abort("Prompt cancelled",true);
        return PROMPT_CANCELLED;
    }
    else if (m_promptAnswer.getId() == MessageId::MSG_ID_TIMEOUT)
    {
    	LOG_ERROR("Prompt timed out");
        DEBUG("[CIF] Prompt timed out");
        // Abort execution in this case
        SPELLexecutor::instance().abort("Prompt timed out", true);
        return PROMPT_TIMEOUT;
    }
    else if (m_promptAnswer.getType() == MSG_TYPE_ERROR)
    {
    	std::string errorMsg = m_promptAnswer.get( MessageField::FIELD_ERROR );
        DEBUG("[CIF] Prompt error: " + errorMsg );
    	LOG_ERROR("Prompt error: " + errorMsg);
        // \todo Should fix this and use an error code
        if (errorMsg == "No controlling client")
        {
        	warning("No controlling client to issue prompt!", LanguageConstants::SCOPE_SYS );
        	SPELLexecutor::instance().pause();
        }
        else
        {
        	error( "Prompt error: " + errorMsg, LanguageConstants::SCOPE_SYS  );
        	// Abort execution in this case
        	SPELLexecutor::instance().abort("Prompt error",true);
        }
        return PROMPT_ERROR;
    }
    else
    {
		toProcess = m_promptAnswer.get(MessageField::FIELD_RVALUE);
	}

	DEBUG("[CIF] Prompt response: " + toProcess);

	std::string toShow = SPELLcifHelper::getResult( toProcess, def );

	DEBUG("[CIF] Translated prompt response: " + toProcess);

    // \todo When there is no controlling client we should keep the child procedure in prompt waiting state

    // Send the display message via the buffer to ensure synchronization
    m_buffer->write("Answer: '" + toShow + "'", LanguageConstants::SCOPE_PROMPT);
    m_buffer->flush();

    m_asRun->writeAnswer( getStack(), toProcess, def.scope );
    return toShow;
}

//=============================================================================
// METHOD: SPELLserverCif::openSubprocedure
//=============================================================================
std::string SPELLserverCif::openSubprocedure( const std::string& procId, const std::string& args, bool automatic, bool blocking, bool visible )
{
    std::string openModeStr = "{";
    DEBUG("[CIF] Open subprocedure options: " + BSTR(automatic) + "," + BSTR(blocking) + "," + BSTR(visible));
    openModeStr += (automatic ? (LanguageModifiers::Automatic + ":" + PythonConstants::True) : (LanguageModifiers::Automatic + ":" + PythonConstants::False)) + ",";
    openModeStr += (blocking ? (LanguageModifiers::Blocking + ":" + PythonConstants::True) : (LanguageModifiers::Blocking+ ":" + PythonConstants::False)) + ",";
    openModeStr += (visible ? (LanguageModifiers::Visible + ":" + PythonConstants::True) : (LanguageModifiers::Visible + ":" + PythonConstants::False)) + "}";

    std::string parent = getProcId();

    // Request first an available instance number
    SPELLipcMessage instanceMsg(ContextMessages::REQ_INSTANCE_ID);
    instanceMsg.setType(MSG_TYPE_REQUEST);
    instanceMsg.set(MessageField::FIELD_PROC_ID, procId);

    SPELLipcMessage response = sendCTXRequest( instanceMsg, m_ipcTimeoutCtxRequestMsec );

    std::string subprocId = "";

    subprocId = response.get(MessageField::FIELD_INSTANCE_ID);

    DEBUG("[CIF] Request context to open subprocedure " + subprocId + " in mode " + openModeStr);

    SPELLipcMessage openMsg(ContextMessages::REQ_OPEN_EXEC);
    openMsg.setType(MSG_TYPE_REQUEST);
    openMsg.set(MessageField::FIELD_PROC_ID, parent);
    openMsg.set(MessageField::FIELD_SPROC_ID, subprocId);
    openMsg.set(MessageField::FIELD_OPEN_MODE, openModeStr);
    openMsg.set(MessageField::FIELD_ARGS, args);

    response = sendCTXRequest( openMsg, m_timeoutOpenProcMsec );
    /** \todo failure handle */

    return subprocId;
}

//=============================================================================
// METHOD: SPELLserverCif::closeSubprocedure
//=============================================================================
void SPELLserverCif::closeSubprocedure( const std::string& procId )
{
    SPELLipcMessage closeMsg(ContextMessages::REQ_CLOSE_EXEC);
    closeMsg.setType(MSG_TYPE_REQUEST);
    closeMsg.set(MessageField::FIELD_SPROC_ID, procId);

    sendCTXRequest( closeMsg, m_ipcTimeoutCtxRequestMsec );
}

//=============================================================================
// METHOD: SPELLserverCif::killSubprocedure
//=============================================================================
void SPELLserverCif::killSubprocedure( const std::string& procId )
{
    SPELLipcMessage killMsg(ContextMessages::REQ_KILL_EXEC);
    killMsg.setType(MSG_TYPE_REQUEST);
    killMsg.set(MessageField::FIELD_SPROC_ID, procId);

    sendCTXRequest( killMsg, m_ipcTimeoutCtxRequestMsec );
}

//=============================================================================
// METHOD: SPELLserverCif::processMessage
//=============================================================================
void SPELLserverCif::processMessage( const SPELLipcMessage& msg )
{
    std::string msgId = msg.getId();
    std::string procId = msg.get(MessageField::FIELD_PROC_ID);
    std::string parentProcId = "";

    if (m_ready && msgId == ExecutorMessages::ACKNOWLEDGE)
    {
    	std::string seq = msg.get(MessageField::FIELD_MSG_SEQUENCE);
    	{
    		SPELLmonitor m(m_ackLock);
    		m_ackSequences.push_back(seq);
    	}
    	return;
    }

    // Reset the IPC interruption flag so that normal
    // communication is restored
    m_ipcInterruptionNotified = false;

    // Prompt answers
    if ( msgId == MessageId::MSG_ID_PROMPT_ANSWER )
    {
    	DEBUG("[CIF] Got prompt answer");
    	m_promptAnswer = msg;
    	m_promptAnswerEvent.set();
    	return;
    }
    else if ( msgId == MessageId::MSG_ID_CANCEL )
    {
    	LOG_WARN("Prompt has been cancelled by client");
    	cancelPrompt();
    	return;
    }

    // Other messages...

    if (msg.hasField(MessageField::FIELD_PARENT_PROC))
    {
    	parentProcId = msg.get(MessageField::FIELD_PARENT_PROC);
    }
    SPELLipcMessageType type = msg.getType();

    // If the message is for a child procedure of our own
    if (parentProcId == getProcId())
    {
    	DEBUG("[CIF] Message is for child ( " + procId + " | " + getProcId() + "=" + parentProcId + ")");
        m_processor.processMessageForChild( msg );
    }
    // If it is directed to this procedure
    else if (procId == getProcId() )
    {
        switch(type)
        {
        case MSG_TYPE_ONEWAY:
            if (msgId == MessageId::MSG_ID_ADD_CLIENT)
            {
            	LOG_INFO("Add controlling client");
            }
            else if (msgId == MessageId::MSG_ID_REMOVE_CLIENT)
            {
            	LOG_WARN("Controlling client removed");
            	switch(SPELLexecutor::instance().getStatus())
            	{
            	case STATUS_FINISHED:
            	case STATUS_PAUSED:
            	case STATUS_ABORTED:
            	case STATUS_ERROR:
            		break;
            	default:
            		SPELLexecutor::instance().pause();
            	}
            }
            else if (msgId == MessageId::MSG_ID_NODE_DEPTH)
            {
                DEBUG("[CIF] Move stack to level");
            	unsigned int level = STRI( msg.get(MessageField::FIELD_LEVEL) );
            	SPELLexecutor::instance().getCallstack().moveToLevel(level);
            }
            else if ( msgId == ExecutorMessages::MSG_DUMP_INTERPRETER )
            {
                LOG_INFO("Dump interpreter information");
            	SPELLutils::dumpInterpreterInfo("USER_REQUEST");
            }
            else
            {
            	processMessageCommand(msg);
            }
            break;
        default:
            LOG_ERROR("[CIF] MESSAGE UNPROCESSED: " + msgId);
            break;
        }
    }
    else
    {
    	LOG_ERROR("[CIF] MESSAGE UNPROCESSED: " + msgId)
    }
}

//=============================================================================
// METHOD: SPELLserverCif::setClosing
//=============================================================================
void SPELLserverCif::setClosing()
{
	SPELLmonitor m(m_closeLock);
	m_closing = true;
}

//=============================================================================
// METHOD: SPELLserverCif::isClosing
//=============================================================================
bool SPELLserverCif::isClosing()
{
	SPELLmonitor m(m_closeLock);
	return m_closing;
}

//=============================================================================
// METHOD: SPELLserverCif::processMessageCommand
//=============================================================================
void SPELLserverCif::processMessageCommand( const SPELLipcMessage& msg )
{
	std::string msgId = msg.getId();
    ExecutorCommand cmd;
    bool high_priority = false;

    cmd.id = msgId;

    LOG_INFO("[CIF] Command received: " + cmd.id)
    if (msgId == ExecutorMessages::MSG_CMD_ABORT)
    {
        high_priority = true;
    }
    else if (msgId == ExecutorMessages::MSG_CMD_CLOSE)
    {
    	// Retain further outgoing requests
    	m_ipcLock.lock();
    	setClosing();
        high_priority = true;
    }
    else if (msgId == ExecutorMessages::MSG_CMD_BLOCK)
    {
    	// Retain further outgoing requests
    	m_ipcLock.lock();
    	setClosing();
        // Do not forward to executor, this is a command for the CIF
    	return;
    }
    else if (msgId == ExecutorMessages::MSG_CMD_GOTO)
    {
        if (msg.hasField(MessageField::FIELD_GOTO_LINE))
        {
            cmd.earg = "line";
            cmd.arg = msg.get(MessageField::FIELD_GOTO_LINE);
        }
        else if (msg.hasField(MessageField::FIELD_GOTO_LABEL))
        {
            cmd.earg = "label";
            cmd.arg = msg.get(MessageField::FIELD_GOTO_LABEL);
        }
    }
    else if (msgId == ExecutorMessages::MSG_CMD_SCRIPT)
    {
        cmd.arg = msg.get(MessageField::FIELD_SCRIPT);
    }
    SPELLexecutor::instance().command(cmd, high_priority);
}

//=============================================================================
// METHOD: SPELLserverCif::processRequest
//=============================================================================
SPELLipcMessage SPELLserverCif::processRequest( const SPELLipcMessage& msg )
{
    std::string requestId = msg.getId();
    std::string procId = msg.get(MessageField::FIELD_PROC_ID);
    SPELLipcMessage response( msg.getId() );
    response.setType(MSG_TYPE_RESPONSE);
    response.setReceiver( msg.getSender() );
    response.setSender( msg.getReceiver() );

	DEBUG("[CIF] Request: " + msg.getId());

    if (requestId == ExecutorMessages::REQ_GET_CONFIG)
    {
    	m_processor.processGetConfig(msg,response);
    }
    else if (requestId == ExecutorMessages::REQ_SET_CONFIG)
    {
    	m_processor.processSetConfig(msg,response);
    }
    else if (requestId == ExecutorMessages::REQ_EXEC_STATUS)
    {
    	m_processor.processGetStatus(msg,response);
    }
    else if (requestId == ExecutorMessages::REQ_SET_BREAKPOINT)
    {
        m_processor.processSetBreakpoint(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_CLEAR_BREAKPOINT)
    {
        m_processor.processClearBreakpoints(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_VARIABLE_NAMES)
    {
    	m_processor.processGetVariables(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_VARIABLE_WATCH)
    {
    	m_processor.processVariableWatch(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_VARIABLE_NOWATCH)
    {
    	m_processor.processVariableNoWatch(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_WATCH_NOTHING)
    {
    	m_processor.processWatchNothing(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_CHANGE_VARIABLE)
    {
    	m_processor.processChangeVariable(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_GET_DICTIONARY)
    {
    	m_processor.processGetDictionary(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_UPD_DICTIONARY)
    {
    	m_processor.processUpdateDictionary(msg, response);
    }
    else if (requestId == ExecutorMessages::REQ_SAVE_STATE)
    {
    	m_processor.processSaveState(msg, response);
    }
    else if (requestId == ContextMessages::MSG_EXEC_OP)
    {
    	DEBUG("[CIF] Received executor operation notification from context");
        if (procId != getProcId())
        {
        	DEBUG("[CIF] Notification is for child");
        	m_processor.processNotificationForChild(msg,response);
        }
        else
        {
            LOG_ERROR("[CIF] Unexpected message for executor " + procId);
        }
    }
    else
    {
        LOG_ERROR("[CIF] Unprocessed request: " + requestId)
    }
    return response;
}

//=============================================================================
// METHOD: SPELLserverCif::processConnectionError
//=============================================================================
void SPELLserverCif::processConnectionError( int clientKey, std::string error, std::string reason )
{
	if (!isClosing())
	{
	    m_ready = false;
		LOG_ERROR("IPC error: " + error + ": " + reason);
		SPELLexecutor::instance().pause();
		while(!m_ready)
		{
			try
			{
				LOG_WARN("Trying to reconnect...");
				m_ifc->connect(true);

				LOG_WARN("Reconnected, login");

				// Perform login
			    SPELLipcMessage response = login();
			    processLogin(response);

			    m_ready = true;

				LOG_INFO("Successfully reconnected to context");
			}
			catch(SPELLipcError& err)
			{
				LOG_ERROR("Failed to reconnect: " + err.what());
				usleep(5000000);
			}
		}
	}
}

//=============================================================================
// METHOD: SPELLserverCif::processConnectionClosed
//=============================================================================
void SPELLserverCif::processConnectionClosed( int clientKey )
{
	if (!isClosing())
	{
		LOG_WARN("Connection closed by context");
		SPELLexecutor::instance().pause();
	}
}

//=============================================================================
// METHOD: SPELLserverCif::startPrompt
//=============================================================================
void SPELLserverCif::startPrompt()
{
    // Send notification of prompt start
    SPELLipcMessage promptStart( m_promptMessage );
    promptStart.setId( MessageId::MSG_ID_PROMPT_START );
    promptStart.setType( MSG_TYPE_ONEWAY );
    sendGUIMessage(&promptStart);

    // Put the scheduler in wait
    SPELLexecutor::instance().getScheduler().startPrompt();

    // Send actual prompt message
	sendGUIMessage(m_promptMessage);

	// Reset the event that will trigger the answer
	m_promptAnswerEvent.clear();
	m_promptAnswer = VOID_MESSAGE;
}

//=============================================================================
// METHOD: SPELLserverCif::cancelPrompt
//=============================================================================
void SPELLserverCif::cancelPrompt()
{
	if (!m_promptMessage.isVoid())
	{
		m_promptAnswer.setId(MessageId::MSG_ID_CANCEL);
		m_promptAnswerEvent.set();

		// Send notification of prompt end
		SPELLipcMessage promptEnd( m_promptMessage );
		promptEnd.setId( MessageId::MSG_ID_PROMPT_END );
		promptEnd.setType( MSG_TYPE_ONEWAY );
		sendGUIMessage(&promptEnd);

		// Finish the prompt state
		SPELLexecutor::instance().getScheduler().finishPrompt();

		m_promptMessage = VOID_MESSAGE;
	}
}

//=============================================================================
// METHOD: SPELLserverCif::waitPromptAnswer
//=============================================================================
void SPELLserverCif::waitPromptAnswer()
{
	// Since this wait will block for an unknown amount of time, we need to provide
	// access to other Python threads to work in the meantime
	SPELLsafeThreadOperations ops("SPELLserverCif::waitPromptAnswer()");
	m_promptAnswerEvent.wait();

	// Send notification of prompt end
    SPELLipcMessage promptEnd( m_promptMessage );
    promptEnd.setId( MessageId::MSG_ID_PROMPT_END );
    promptEnd.setType( MSG_TYPE_ONEWAY );
    sendGUIMessage(&promptEnd);

    // Finish the prompt state
    SPELLexecutor::instance().getScheduler().finishPrompt();

    m_promptMessage = VOID_MESSAGE;
}
