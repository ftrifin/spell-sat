// ################################################################################
// FILE       : SPELLserverCif.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the CIF for server environment
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
// Local includes ----------------------------------------------------------
#include "SPELL_CIFS/SPELLserverCif.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLexecutor.H"
#include "SPELL_EXC/SPELLcommand.H"
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcMessage.H"
#include "SPELL_WRP/SPELLconstants.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
#include "SPELL_UTIL/SPELLpythonMonitors.H"
// System includes ---------------------------------------------------------



// DEFINES /////////////////////////////////////////////////////////////////
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
      m_stMessage( MessageId::MSG_ID_NOTIFICATION )
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

    m_stMessage.setType(MSG_TYPE_NOTIFY);
    m_stMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_STATUS );

    m_ntMessage.setType(MSG_TYPE_NOTIFY);
    m_ntMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_ITEM );

    m_wrMessage.setType(MSG_TYPE_WRITE);

    s_handle = this;

    m_lastStack = "";
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
void SPELLserverCif::setup( const std::string& procId, const std::string& ctxName, int ctxPort, const std::string& timeId)
{
    SPELLcif::setup(procId, ctxName, ctxPort, timeId);

    m_wrMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_stMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_lnMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    m_ntMessage.set( MessageField::FIELD_PROC_ID, getProcId() );

    DEBUG("[CIF] Setup server CIF");
    m_ifc = new SPELLipcClientInterface( "CIF", "0.0.0.0", ctxPort );
    m_buffer = new SPELLdisplayBuffer( procId, *this );
    m_buffer->start();

    // Initialize message sequencer
    m_sequence = 0;
    m_sequenceStack = 0;

    m_ifc->initialize( this );

    DEBUG("[CIF] Connecting to context");
    m_ifc->connectIfc();

    // Perform login
    SPELLipcMessage* response = login();
    processLogin(response);

    m_ready = true;
    m_useGUI = true;
    DEBUG("[CIF] Ready to go");
}

//=============================================================================
// METHOD: SPELLserverCif::cleanup
//=============================================================================
void SPELLserverCif::cleanup( bool force )
{
    SPELLcif::cleanup(force);
    DEBUG("[CIF] Cleaning server CIF");
    m_buffer->stop();
    m_buffer->join();
    DEBUG("[CIF] Disconnecting server CIF");
    if (!force)
    {
        DEBUG("[CIF] Send logout message to context");
        logout();
    }
    m_ifc->disconnect(true);
    m_ready = false;
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
    // Clear the finish event so that we can wait for final commands next time
    if (!m_finishEvent.isClear())
    {
        m_finishEvent.clear();
    }
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
    logoutmsg.set( MessageField::FIELD_EXEC_PORT, "0"); // DEPRECATED

    m_ifc->sendMessage(&logoutmsg);
}

//=============================================================================
// METHOD: SPELLserverCif::login
//=============================================================================
SPELLipcMessage* SPELLserverCif::login()
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
    loginmsg.set( MessageField::FIELD_EXEC_STATUS, StatusToString(STATUS_LOADED));
    loginmsg.set( MessageField::FIELD_ASRUN_NAME, getAsRunName() );
    loginmsg.set( MessageField::FIELD_LOG_NAME, SPELLlog::instance().getLogFile() );
    loginmsg.set( MessageField::FIELD_EXEC_PORT, "0" ); /** \todo Remove this, is deprecated */

    LOG_INFO("Login information:")
    LOG_INFO("Proc  : " + getProcId() )
    LOG_INFO("Status: LOADED")
    LOG_INFO("AsRun : " + getAsRunName() )
    LOG_INFO("Log   : " + SPELLlog::instance().getLogFile() )

    // Send the login message.
    // ERRORS: if there is a timeout in this request, an SPELLipcError exception will
    // be thrown and will make the executor die
    SPELLipcMessage* response = m_ifc->sendRequest(&loginmsg, SPELL_CIF_LOGIN_TIMEOUT_SEC);

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
void SPELLserverCif::processLogin( SPELLipcMessage* loginResp )
{
    DEBUG("[CIF] Processing login response");

    std::string openMode = loginResp->get(MessageField::FIELD_OPEN_MODE);
    openMode = openMode.substr(1, openMode.size()-2);

    m_visible = true;
    m_automatic = false;
    m_blocking = true;
    std::vector<std::string> pairs = tokenize( openMode, ",");
    std::vector<std::string>::iterator it;
    for( it = pairs.begin(); it != pairs.end(); it++)
    {
        std::vector<std::string> kv = tokenize( *it, ":" );
        if (kv.size()!=2) continue;
        std::string modifier = kv[0];
        std::string value = kv[1];
        trim(modifier);
        trim(value);
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

    if (loginResp->hasField(MessageField::FIELD_GUI_CONTROL))
    {
        m_controlGui = loginResp->get(MessageField::FIELD_GUI_CONTROL);
    }

    if (loginResp->hasField(MessageField::FIELD_GUI_CONTROL_HOST))
    {
        m_controlHost = loginResp->get(MessageField::FIELD_GUI_CONTROL_HOST);
    }

    if (loginResp->hasField(MessageField::FIELD_CONDITION))
    {
        m_condition   = loginResp->get(MessageField::FIELD_CONDITION);
    }

    if (loginResp->hasField(MessageField::FIELD_ARGS))
    {
        m_arguments   = loginResp->get(MessageField::FIELD_ARGS);
    }

    LOG_INFO("Automatic mode: " + (m_automatic ? STR("yes") : STR("no")));
    LOG_INFO("Blocking mode : " + (m_blocking ? STR("yes") : STR("no")));
    LOG_INFO("Visible mode  : " + (m_visible ? STR("yes") : STR("no")));
    LOG_INFO("Arguments     : " + m_arguments);
    LOG_INFO("Browsable lib : " + BSTR(m_browsableLib));
    LOG_INFO("Condition     : " + m_condition);
    LOG_INFO("Control gui   : " + m_controlGui);
    LOG_INFO("Control host  : " + m_controlHost);
    //DEBUG("LOGIN MSG: " + loginResp->data())

    delete loginResp;
}

//=============================================================================
// METHOD: SPELLserverCif::sendGUIRequest
//=============================================================================
SPELLipcMessage* SPELLserverCif::sendGUIRequest( SPELLipcMessage* msg, unsigned long timeoutSec )
{
    static std::string procId = getProcId();
    if (m_ready && m_useGUI)
    {
        DEBUG("[CIF] Sending GUI request: " + msg->data());
        msg->setSender(procId);
        msg->setReceiver("GUI");
        SPELLipcMessage* response = NULL;
        try
        {
        	SPELLsafeThreadOperations ops;
        	response = m_ifc->sendRequest(msg, timeoutSec);
        }
        catch(SPELLipcError& ex)
        {
        	throw SPELLcoreException("Unable to communicate with GUI", STR(ex.what()));
        }
        DEBUG("[CIF] Got GUI response");
        return response;
    }
    return NULL;
}

//=============================================================================
// METHOD: SPELLserverCif::sendGUIMessage
//=============================================================================
void SPELLserverCif::sendGUIMessage( SPELLipcMessage* msg )
{
    static std::string procId = getProcId();
    if (m_ready && m_useGUI)
    {
        DEBUG("[CIF] Sending GUI message");
        msg->setSender(procId);
        msg->setReceiver("GUI");
        m_ifc->sendMessage(msg);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::sendCTXRequest
//=============================================================================
SPELLipcMessage* SPELLserverCif::sendCTXRequest( SPELLipcMessage* msg, unsigned long timeoutSec )
{
    static std::string procId = getProcId();
    if (m_ready)
    {
        DEBUG("[CIF] Sending CTX request: " + msg->data());
        msg->setSender(procId);
        msg->setReceiver("CTX");
        SPELLipcMessage* response = m_ifc->sendRequest(msg,timeoutSec);
        DEBUG("[CIF] Got CTX response");
        return response;
    }
    return NULL;
}

//=============================================================================
// METHOD: SPELLserverCif::sendCTXMessage
//=============================================================================
void SPELLserverCif::sendCTXMessage( SPELLipcMessage* msg )
{
    static std::string procId = getProcId();
    if (m_ready)
    {
        DEBUG("[CIF] Sending CTX message");
        msg->setSender(procId);
        msg->setReceiver("CTX");
        m_ifc->sendMessage(msg);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::completeMessage
//=============================================================================
void SPELLserverCif::completeMessage( SPELLipcMessage* msg )
{
    msg->set(MessageField::FIELD_TIME, timestamp() );
	msg->set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequence));
	m_sequence++;

    msg->set(MessageField::FIELD_CSP, getStack() + "/" + ISTR(getNumExecutions()) );

    if (isManual())
    {
    	msg->set(MessageField::FIELD_EXECUTION_MODE, MessageValue::DATA_EXEC_MODE_MANUAL);
    }
    else
    {
    	msg->set(MessageField::FIELD_EXECUTION_MODE, MessageValue::DATA_EXEC_MODE_PROCEDURE);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::notifyLine
//=============================================================================
void SPELLserverCif::notifyLine()
{
	std::string stack = getStack();

    if (m_lastStack == stack) return;
    m_lastStack = stack;

    std::string stage = getStage();

    DEBUG("[CIF] Procedure line: " + stack + "(" + stage + ")");

    m_lnMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_LINE );
	m_lnMessage.setType(MSG_TYPE_NOTIFY_ASYNC);

    completeMessage( &m_lnMessage );
    m_lnMessage.set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequenceStack));
	m_sequenceStack++;
	m_lnMessage.set(MessageField::FIELD_CODE_NAME, getCodeName() );

    if (stage.find(":") != std::string::npos)
    {
        std::vector<std::string> stage_title = tokenize(stage,":");
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

	sendGUIMessage(&m_lnMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyCall
//=============================================================================
void SPELLserverCif::notifyCall()
{
	std::string stack = getStack();

    DEBUG("[CIF] Procedure call: " + stack );

    m_lnMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_CALL );
	m_lnMessage.setType(MSG_TYPE_NOTIFY);

    completeMessage( &m_lnMessage );
    m_lnMessage.set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequenceStack));
	m_sequenceStack++;
	m_lnMessage.set(MessageField::FIELD_CODE_NAME, getCodeName() );

    m_asRun->writeCall( stack, (m_sequence-1) );

	SPELLipcMessage* response = sendGUIRequest(&m_lnMessage, SPELL_CIF_NOTIFICATION_TIMEOUT_SEC);
	if (response) delete response;
}

//=============================================================================
// METHOD: SPELLserverCif::notifyReturn
//=============================================================================
void SPELLserverCif::notifyReturn()
{
    DEBUG("[CIF] Procedure return");

    m_lnMessage.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_RETURN );
	m_lnMessage.setType(MSG_TYPE_NOTIFY);

    completeMessage( &m_lnMessage );
	m_lnMessage.set(MessageField::FIELD_MSG_SEQUENCE, ISTR(m_sequenceStack));
	m_sequenceStack++;
	m_lnMessage.set(MessageField::FIELD_CODE_NAME, getCodeName() );

    m_asRun->writeReturn( (m_sequence-1) );

	SPELLipcMessage* response = sendGUIRequest(&m_lnMessage, SPELL_CIF_NOTIFICATION_TIMEOUT_SEC);
	if (response) delete response;
}

//=============================================================================
// METHOD: SPELLserverCif::notifyStatus
//=============================================================================
void SPELLserverCif::notifyStatus( const SPELLstatusInfo& st )
{
    DEBUG("[CIF] Status notification: " + StatusToString(st.status) + " (" + st.condition + ")");

    m_stMessage.set(MessageField::FIELD_EXEC_STATUS, StatusToString(st.status));

    completeMessage( &m_stMessage );

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

    SPELLipcMessage* response = sendGUIRequest(&m_stMessage, SPELL_CIF_NOTIFICATION_TIMEOUT_SEC);
    if (response) delete response;
}

//=============================================================================
// METHOD: SPELLserverCif::notifyUserActionSet
//=============================================================================
void SPELLserverCif::notifyUserActionSet( const std::string& label, const unsigned int severity )
{
    SPELLipcMessage actionMessage(ExecutorMessages::MSG_ID_SET_UACTION);
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
    sendGUIMessage(&actionMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyUserActionUnset
//=============================================================================
void SPELLserverCif::notifyUserActionUnset()
{
    SPELLipcMessage actionMessage(ExecutorMessages::MSG_ID_DISMISS_UACTION);
    actionMessage.setType(MSG_TYPE_ONEWAY);
    actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
    sendGUIMessage(&actionMessage);
}

//=============================================================================
// METHOD: SPELLserverCif::notifyUserActionEnable
//=============================================================================
void SPELLserverCif::notifyUserActionEnable( bool enable )
{
    if (enable)
    {
        SPELLipcMessage actionMessage(ExecutorMessages::MSG_ID_ENABLE_UACTION);
        actionMessage.setType(MSG_TYPE_ONEWAY);
        actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
        sendGUIMessage(&actionMessage);
    }
    else
    {
        SPELLipcMessage actionMessage(ExecutorMessages::MSG_ID_DISABLE_UACTION);
        actionMessage.setType(MSG_TYPE_ONEWAY);
        actionMessage.set( MessageField::FIELD_PROC_ID, getProcId() );
        sendGUIMessage(&actionMessage);
    }
}

//=============================================================================
// METHOD: SPELLserverCif::notify
//=============================================================================
void SPELLserverCif::notify( ItemNotification notification )
{
    DEBUG("[CIF] Item notification");

    completeMessage( &m_ntMessage );

    DEBUG("[CIF] Processing status");
    int sCount = 0;
    if (notification.status.find(IPCinternals::ARG_SEPARATOR) != std::string::npos )
    {
        std::vector<std::string> statusList = tokenize(notification.status,IPCinternals::ARG_SEPARATOR);
        std::vector<std::string>::iterator it;
        for( it = statusList.begin(); it != statusList.end(); it++)
        {
            if (*it == MessageValue::DATA_NOTIF_STATUS_OK) sCount++;
        }
        std::vector<std::string> commentList = tokenize(notification.comment,IPCinternals::ARG_SEPARATOR);
        if (commentList.size() == 1)
        {
            notification.comment = "";
            for( unsigned int count=0; count<statusList.size(); count++)
            {
                if (notification.comment.size()>0) notification.comment += IPCinternals::ARG_SEPARATOR;
                notification.comment += " ";
            }
        }
        std::vector<std::string> timeList = tokenize(notification.time, IPCinternals::ARG_SEPARATOR);
        if (timeList.size() == 1)
        {
            notification.time = "";
            std::string tstamp = timestamp();
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

    DEBUG("[CIF] Message prepared, sending");

    m_asRun->writeItem( getStack(),
						NOTIF_TYPE_STR[notification.type],
                        notification.name,
                        notification.value,
                        notification.status,
                        notification.comment,
                        notification.time );

    SPELLipcMessage* response = sendGUIRequest(&m_ntMessage, SPELL_CIF_NOTIFICATION_TIMEOUT_SEC);
    if (response) delete response;
    DEBUG("[CIF] Notification sent");
}

//=============================================================================
// METHOD: SPELLserverCif::notifyError
//=============================================================================
void SPELLserverCif::notifyError( const std::string& error, const std::string& reason, bool fatal )
{
    DEBUG("[CIF] Error notification: " + error + " (" + reason + ")");

    SPELLipcMessage errorMsg( MessageId::MSG_ID_ERROR);
    errorMsg.setType(MSG_TYPE_ERROR);
    errorMsg.set( MessageField::FIELD_PROC_ID, getProcId() );
    errorMsg.set( MessageField::FIELD_DATA_TYPE, MessageValue::DATA_TYPE_STATUS );
    errorMsg.set( MessageField::FIELD_EXEC_STATUS, StatusToString(STATUS_ERROR) );
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

    completeMessage( &errorMsg );

    m_asRun->writeErrorInfo( error, reason );

    sendGUIMessage(&errorMsg);
}

//=============================================================================
// METHOD: SPELLserverCif::write
//=============================================================================
void SPELLserverCif::write( const std::string& msg, unsigned int scope )
{
    if ( getVerbosity() > getVerbosityFilter() ) return;

    DEBUG("[CIF] Write message: " + msg);

    // We shall not bufferize in manual mode
    if (isManual())
    {
        completeMessage( &m_wrMessage );
        std::string timeStr = getTimestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_INFO);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(&m_wrMessage);
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

    DEBUG("[CIF] Warning message: " + msg);

    // We shall not bufferize in manual mode
    if (isManual())
    {
        completeMessage( &m_wrMessage );
        std::string timeStr = getTimestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_WARN);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(&m_wrMessage);
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

    DEBUG("[CIF] Error message: " + msg);

    // We shall not bufferize in manual mode
    if (isManual())
    {
        completeMessage( &m_wrMessage );
        std::string timeStr = getTimestampUsec();
        m_wrMessage.set(MessageField::FIELD_TEXT,msg);
        m_wrMessage.set(MessageField::FIELD_LEVEL,MessageValue::DATA_SEVERITY_ERROR);
        m_wrMessage.set(MessageField::FIELD_MSGTYPE,LanguageConstants::DISPLAY);
        m_wrMessage.set(MessageField::FIELD_TIME, timeStr);
        m_wrMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));
        sendGUIMessage(&m_wrMessage);
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
			names += ",,";
			types += ",,";
			values += ",,";
			globals += ",,";
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

	sendGUIMessage(&notifyMsg);
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
			varNames += ",,";
			varTypes += ",,";
			varValues += ",,";
			varGlobals += ",,";
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
			varNames += ",,";
			varTypes += ",,";
			varValues += ",,";
			varGlobals += ",,";
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

	sendGUIMessage(&notifyMsg);
}

//=============================================================================
// METHOD: SPELLserverCif::prompt
//=============================================================================
std::string SPELLserverCif::prompt( const std::string& message, PromptOptions options, unsigned int ptype, unsigned int scope )
{
    DEBUG("[CIF] Prompt message");

    std::string timeStr = getTimestampUsec();

    SPELLipcMessage promptMessage(MessageId::MSG_ID_PROMPT);

    completeMessage( &promptMessage );

    promptMessage.setType(MSG_TYPE_PROMPT);
    promptMessage.set(MessageField::FIELD_PROC_ID, getProcId() );
    promptMessage.set(MessageField::FIELD_TEXT, message);
    promptMessage.set(MessageField::FIELD_DATA_TYPE, ISTR(ptype));
    promptMessage.set(MessageField::FIELD_TIME, timeStr);
    promptMessage.set(MessageField::FIELD_SCOPE, ISTR(scope));

    // Used for the prompt display message
    PromptOptions optionsToShow;

    DEBUG("[CIF] Prompt typecode " + ISTR(ptype));

    if (ptype == LanguageConstants::PROMPT_NUM ||
        ptype == LanguageConstants::PROMPT_ALPHA ||
        ptype == LanguageConstants::PROMPT_DATE )
    {
        DEBUG("[CIF] Prompt is simple");
        promptMessage.set(MessageField::FIELD_EXPECTED, "");
    }
    else if ( ptype & LanguageConstants::PROMPT_LIST )
    {
        DEBUG("[CIF] Prompt is list");
        PromptOptions::iterator it;
        int keyCount = 1;
        std::string optionStr = "";
        std::string expectedStr = "";

        // Iterate over the option list and build the option and expected values strings
        for( it = options.begin(); it != options.end(); it++)
        {
            std::string key;
            std::string value;
            std::string opt = (*it);
            DEBUG("[CIF] Option: " + opt);

            if (optionStr.size()>0) optionStr += IPCinternals::OPT_SEPARATOR;
            if (expectedStr.size()>0) expectedStr += IPCinternals::OPT_SEPARATOR;

            if ( opt.find(IPCinternals::KEY_SEPARATOR) == std::string::npos )
            {
                // Put an internal key in this case
                key = ISTR(keyCount);
                value = opt;
                // Trim the value
                trim(value);
            }
            else
            {
                int idx = opt.find(IPCinternals::KEY_SEPARATOR);
                key = opt.substr(0, idx);
                // Trim the key
                trim(key);
                value = opt.substr(idx+1, opt.size()-idx);
                // Trim the value
                trim(value);
            }
            DEBUG("[CIF] Option key: '" + key + "'");
            DEBUG("[CIF] Option value: '" + value + "'");

            optionStr += key + IPCinternals::KEY_SEPARATOR + value;
            optionsToShow.push_back(key + ": " + value);

            if (ptype & LanguageConstants::PROMPT_ALPHA)
            {
                expectedStr += value;
                DEBUG("[CIF] Expected: " + value);
            }
            else
            {
                expectedStr += key;
                DEBUG("[CIF] Expected: " + key);
            }
            keyCount++;
        }
        promptMessage.set(MessageField::FIELD_EXPECTED, expectedStr);
        promptMessage.set(MessageField::FIELD_OPTIONS, optionStr);
    }
    else if (ptype == LanguageConstants::PROMPT_OK)
    {
        promptMessage.set(MessageField::FIELD_EXPECTED, "O");
        promptMessage.set(MessageField::FIELD_OPTIONS, "O" + IPCinternals::KEY_SEPARATOR + " Ok");
        optionsToShow.push_back("O: Ok");
    }
    else if (ptype == LanguageConstants::PROMPT_CANCEL)
    {
        promptMessage.set(MessageField::FIELD_EXPECTED, "C");
        promptMessage.set(MessageField::FIELD_OPTIONS, "C" + IPCinternals::KEY_SEPARATOR + " Cancel");
        optionsToShow.push_back("C: Cancel");
    }
    else if (ptype == LanguageConstants::PROMPT_YES)
    {
        promptMessage.set(MessageField::FIELD_EXPECTED, "Y");
        promptMessage.set(MessageField::FIELD_OPTIONS, "Y" + IPCinternals::KEY_SEPARATOR + " Yes");
        optionsToShow.push_back("Y: Yes");
    }
    else if (ptype == LanguageConstants::PROMPT_NO)
    {
        promptMessage.set(MessageField::FIELD_EXPECTED, "N");
        promptMessage.set(MessageField::FIELD_OPTIONS, "N" + IPCinternals::KEY_SEPARATOR + " No");
        optionsToShow.push_back("N: No");
    }
    else if (ptype == LanguageConstants::PROMPT_YES_NO)
    {
        promptMessage.set(MessageField::FIELD_EXPECTED, "Y|N");
        promptMessage.set(MessageField::FIELD_OPTIONS, "Y" + IPCinternals::KEY_SEPARATOR + " Yes|N" + IPCinternals::KEY_SEPARATOR + " No");
        optionsToShow.push_back("Y: Yes");
        optionsToShow.push_back("N: No");
    }
    else if (ptype == LanguageConstants::PROMPT_OK_CANCEL)
    {
        promptMessage.set(MessageField::FIELD_EXPECTED, "O|C");
        promptMessage.set(MessageField::FIELD_OPTIONS, "O" + IPCinternals::KEY_SEPARATOR + " Ok|C" + IPCinternals::KEY_SEPARATOR + " Cancel");
        optionsToShow.push_back("O: Ok");
        optionsToShow.push_back("C: Cancel");
    }
    DEBUG("[CIF] Option string: " + promptMessage.get(MessageField::FIELD_OPTIONS));
    DEBUG("[CIF] Expected string: " + promptMessage.get(MessageField::FIELD_EXPECTED));

    // Write the prompt display
    std::string msgToShow = message;
    if (optionsToShow.size()>0)
	{
		msgToShow += "\nAvailable options:\n";
    	for(PromptOptions::iterator pit = optionsToShow.begin(); pit != optionsToShow.end(); pit++)
    	{
    		msgToShow += "   - " + (*pit) + "\n";
    	}
	}

    // Send the display message via the buffer to ensure synchronization
    m_buffer->prompt(msgToShow, LanguageConstants::SCOPE_PROMPT);

    // Ensure buffer is flushed
    m_buffer->flush();

    // Write the prompt in the asrun
    m_asRun->writePrompt( getStack(), message, scope );

    DEBUG("[CIF] Messsage prepared");

    // Send request to client
    SPELLipcMessage* response = sendGUIRequest(&promptMessage, SPELL_CIF_PROMPT_TIMEOUT_SEC);

    DEBUG("[CIF] Prompt response received");

    std::string toProcess = "";
    if (response->getId() == MessageId::MSG_ID_CANCEL)
    {
    	LOG_WARN("Prompt cancelled");
        DEBUG("[CIF] Prompt cancelled");
        toProcess = PROMPT_CANCELLED;
        // Abort execution in this case
        SPELLexecutor::instance().abort("Prompt cancelled",true);
    }
    else if (response->getId() == MessageId::MSG_ID_TIMEOUT)
    {
    	LOG_ERROR("Prompt timed out");
        DEBUG("[CIF] Prompt timed out");
        toProcess = PROMPT_TIMEOUT;
        // Abort execution in this case
        SPELLexecutor::instance().abort("Prompt timed out", true);
    }
    else if (response->getType() == MSG_TYPE_ERROR)
    {
    	std::string errorMsg = response->get( MessageField::FIELD_ERROR );
        DEBUG("[CIF] Prompt error: " + errorMsg );
    	LOG_ERROR("Prompt error: " + errorMsg);
        toProcess = PROMPT_ERROR;
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
    }
    else
    {
        toProcess = response->get(MessageField::FIELD_RVALUE);
        DEBUG("[CIF] Prompt response: " + toProcess);
    }

    // \todo When there is no controlling client we should keep the child procedure in prompt waiting state

    // Send the display message via the buffer to ensure synchronization
    m_buffer->write("Answer: '" + toProcess + "'", LanguageConstants::SCOPE_PROMPT);
    m_buffer->flush();

    m_asRun->writeAnswer( getStack(), toProcess, scope );
    return toProcess;
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

    std::string parent = SPELLexecutor::instance().getProcId();

    // Request first an available instance number
    SPELLipcMessage instanceMsg(ContextMessages::REQ_INSTANCE_ID);
    instanceMsg.setType(MSG_TYPE_REQUEST);
    instanceMsg.set(MessageField::FIELD_PROC_ID, procId);

    SPELLipcMessage* response = sendCTXRequest( &instanceMsg, SPELL_CIF_CTXREQUEST_TIMEOUT_SEC );

    std::string subprocId = "";

    subprocId = response->get(MessageField::FIELD_INSTANCE_ID);
    delete response;

    DEBUG("[CIF] Request context to open subprocedure " + subprocId + " in mode " + openModeStr);

    SPELLipcMessage openMsg(ContextMessages::REQ_OPEN_EXEC);
    openMsg.setType(MSG_TYPE_REQUEST);
    openMsg.set(MessageField::FIELD_PROC_ID, parent);
    openMsg.set(MessageField::FIELD_SPROC_ID, subprocId);
    openMsg.set(MessageField::FIELD_OPEN_MODE, openModeStr);
    openMsg.set(MessageField::FIELD_ARGS, args);

    response = sendCTXRequest( &openMsg, SPELL_CIF_OPENPROC_TIMEOUT_SEC );
    delete response;
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

    SPELLipcMessage* response = sendCTXRequest( &closeMsg, SPELL_CIF_CTXREQUEST_TIMEOUT_SEC );
    delete response;
}

//=============================================================================
// METHOD: SPELLserverCif::killSubprocedure
//=============================================================================
void SPELLserverCif::killSubprocedure( const std::string& procId )
{
    SPELLipcMessage killMsg(ContextMessages::REQ_KILL_EXEC);
    killMsg.setType(MSG_TYPE_REQUEST);
    killMsg.set(MessageField::FIELD_SPROC_ID, procId);

    SPELLipcMessage* response = sendCTXRequest( &killMsg, SPELL_CIF_CTXREQUEST_TIMEOUT_SEC );
    delete response;
}

//=============================================================================
// METHOD: SPELLserverCif::processMessage
//=============================================================================
void SPELLserverCif::processMessage( SPELLipcMessage* msg )
{
    std::string msgId = msg->getId();
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);
    SPELLipcMessageType type = msg->getType();

    // If the message is for a child procedure
    if (procId != SPELLexecutor::instance().getProcId())
    {
        m_processor.processMessageForChild( msg );
    }
    else
    {
        switch(type)
        {
        case MSG_TYPE_ONEWAY:
            if (msgId == ExecutorMessages::MSG_ID_ADD_CLIENT)
            {
            	/** \todo Executor::instance().addClient( clientKey, clientHost ); */
                DEBUG("[CIF] ADD CLIENT @@@@@");
            }
            else if (msgId == ExecutorMessages::MSG_ID_REMOVE_CLIENT)
            {
            	/** \todo Executor::instance().removeClient( clientKey ); */
                DEBUG("[CIF] Remove client @@@@@");
            }
            else if (msgId == ExecutorMessages::MSG_ID_NODE_DEPTH)
            {
                DEBUG("[CIF] Move stack to level");
            	unsigned int level = atoi( msg->get(MessageField::FIELD_LEVEL).c_str() );
            	SPELLexecutor::instance().getCallstack().moveToLevel(level);
            }
            else
            {
            	processMessageCommand(msg);
            }
            break;
        default:
            LOG_ERROR("[CIF] MESSAGE UNPROCESSED: " + msgId)
        }
    }
}

//=============================================================================
// METHOD: SPELLserverCif::processMessageCommand
//=============================================================================
void SPELLserverCif::processMessageCommand( SPELLipcMessage* msg )
{
	std::string msgId = msg->getId();
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
        // Disable talk to the GUI
        m_ifc->cancelOutgoingRequests( m_ifc->getKey() );
        m_useGUI = false;
        high_priority = true;
    }
    else if (msgId == ExecutorMessages::MSG_CMD_GOTO)
    {
        if (msg->hasField(MessageField::FIELD_GOTO_LINE))
        {
            cmd.earg = "line";
            cmd.arg = msg->get(MessageField::FIELD_GOTO_LINE);
        }
        else if (msg->hasField(MessageField::FIELD_GOTO_LABEL))
        {
            cmd.earg = "label";
            cmd.arg = msg->get(MessageField::FIELD_GOTO_LABEL);
        }
    }
    else if (msgId == ExecutorMessages::MSG_CMD_SCRIPT)
    {
        cmd.arg = msg->get(MessageField::FIELD_SCRIPT);
    }
    SPELLexecutor::instance().command(cmd, high_priority);
}

//=============================================================================
// METHOD: SPELLserverCif::processRequest
//=============================================================================
SPELLipcMessage* SPELLserverCif::processRequest( SPELLipcMessage* msg )
{
    std::string requestId = msg->getId();
    std::string procId = msg->get(MessageField::FIELD_PROC_ID);
    SPELLipcMessage* response = new SPELLipcMessage( msg->getId() );
    response->setType(MSG_TYPE_RESPONSE);
    response->setReceiver( msg->getSender() );
    response->setSender( msg->getReceiver() );

    if (requestId == ExecutorMessages::REQ_GET_CONFIG)
    {
    	m_processor.processGetConfig(msg,response);
    }
    else if (requestId == ExecutorMessages::REQ_SET_CONFIG)
    {
    	m_processor.processSetConfig(msg,response);
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
    else if (msg->getType() == MSG_TYPE_NOTIFY)
    {
        if (procId != SPELLexecutor::instance().getProcId())
        {
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
// METHOD: SPELLserverCif::processError
//=============================================================================
void SPELLserverCif::processError( std::string error, std::string reason )
{
	/** \todo Implement server CIF error callback for connection lost */
}
