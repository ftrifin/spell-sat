// ################################################################################
// FILE       : SPELLipc.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Inter process communication, data definitions
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

#ifndef __SPELL_IPC_H__
#define __SPELL_IPC_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"

/** \defgroup SPELL_IPC Inter-process communications (SPELL_IPC)
 *
 * This module provides mechanisms to establish socket-based communication
 * channels between the SPELL processes.
 *
 * */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
/** Types of SPELL messages */
typedef enum SPELLipcMessageType_
{
    MSG_TYPE_ONEWAY,
    MSG_TYPE_NOTIFY,
    MSG_TYPE_NOTIFY_ASYNC,
    MSG_TYPE_REQUEST,
    MSG_TYPE_PROMPT,
    MSG_TYPE_RESPONSE,
    MSG_TYPE_WRITE,
    MSG_TYPE_ERROR,
    MSG_TYPE_EOC,
    MSG_TYPE_NOTYPE,
    MSG_TYPE_UNKNOWN
}
SPELLipcMessageType;

// DEFINES /////////////////////////////////////////////////////////////////

/** IPC buffer side for reading incoming socket data */
#define IPC_BUFFER_SIZE     65
/** General timeout for receiving request responses */
#define IPC_DEFAULT_REQUEST_TIMEOUT_SEC  10
/** Maximum number of retries for timed-out requests */
#define IPC_REQUEST_MAX_RETRIES 5
/** Error timeouts */
#define IPC_ERROR_TIMEOUT 900
/** Delay for retries */
#define IPC_REQUEST_RETRY_DELAY_USEC 2500

/** Define the message identifiers */
namespace MessageId
{
	/** Write messages */
	static const std::string MSG_ID_WRITE("MSG_WRITE");
	/** Prompt messages */
	static const std::string MSG_ID_PROMPT("MSG_PROMPT");
	/** Prompt start, for monitoring guis */
	static const std::string MSG_ID_PROMPT_START("MSG_PROMPT_START");
	/** Prompt end, for monitoring guis */
	static const std::string MSG_ID_PROMPT_END("MSG_PROMPT_END");
	/** Notifications */
	static const std::string MSG_ID_NOTIFICATION("MSG_NOTIFICATION");
	/** Error messages */
	static const std::string MSG_ID_ERROR("MSG_ERROR");
	/** Cancel prompt messages */
	static const std::string MSG_ID_CANCEL("MSG_CANCEL");
	/** Prompt timeout messages */
	static const std::string MSG_ID_TIMEOUT("MSG_TIMEOUT");
	/** No-client for prompt */
	static const std::string MSG_ID_NOCLIENT("MSG_NOCLIENT");
	/** Close command */
	static const std::string MSG_ID_CLOSE("CMD_CLOSE");
	/** Executor operations */
	static const std::string MSG_ID_EXEC_OP("MSG_EXEC_OP");
	/** Generic identifier */
	static const std::string GENERIC_ID("GEN");
};

/** Defines message fields */
namespace MessageField
{
	/** Message identifier field */
	static const std::string FIELD_ID("Id");
	/** Message root.
	 * \deprecated */
	static const std::string FIELD_TYPE("root");
	/** Notification data type */
	static const std::string FIELD_DATA_TYPE("DataType");
	/** IPC message key */
	static const std::string FIELD_IPC_KEY("IpcKey");
	/** IPC request sequence number */
	static const std::string FIELD_SEQUENCE("IpcSeq");
	/** Sender identifier */
	static const std::string FIELD_SENDER_ID("SenderId");
	/** Receiver identifier */
	static const std::string FIELD_RECEIVER_ID("ReceiverId");
	/** Error message */
	static const std::string FIELD_ERROR("ErrorMsg");
	/** Error reason or explanation */
	static const std::string FIELD_REASON("ErrorReason");
	/** Fatal error flag field */
	static const std::string FIELD_FATAL("FatalError");
	/** Host name */
	static const std::string FIELD_HOST("Host");
	/** Message times */
	static const std::string FIELD_TIME("Time");
	/** Current message chunk, for splitted messages */
	static const std::string FIELD_CHUNK("CurrentChunk");
	/** Total number of chunks, for splitted messages */
	static const std::string FIELD_TOTAL_CHUNKS("TotalChunks");
	/** Scope of the message */
	static const std::string FIELD_SCOPE("Scope");

	/** Holds a comma separated list of executor names */
	static const std::string  FIELD_EXEC_LIST("ExecutorList");
	/** Holds a comma separated list of procedure names */
	static const std::string  FIELD_PROC_LIST("ProcList");
	/** Holds the procedure id */
	static const std::string  FIELD_PROC_ID("ProcId");
	/** Holds the instance id, used in the instance request */
	static const std::string  FIELD_INSTANCE_ID("InstanceId");
	/** Holds the code name string */
	static const std::string  FIELD_CODE_NAME("CodeName");
	/** Holds the procedure code */
	static const std::string  FIELD_PROC_CODE("ProcCode");
	/** Holds a server file lines (data) */
	static const std::string  FIELD_SERVER_FILE("ServerFile");
	/** Holds the GUI connection mode (commanding, monitoring) */
	static const std::string  FIELD_GUI_MODE("GuiMode");
	/** Holds client key */
	static const std::string  FIELD_GUI_KEY("GuiKey");              // Client and exec operations
	/** Executor operation */
	static const std::string  FIELD_EXOP("ExecOp");              // Exec operations
	/** Client operation */
	static const std::string  FIELD_CLOP("CltOp");               // Client operations
	/** Subprocedure identifier */
	static const std::string  FIELD_SPROC_ID("SprocId");             // Open request from executor
	/** Open mode */
	static const std::string  FIELD_OPEN_MODE("OpenMode");            // Open requests
	/** Boolean field */
	static const std::string  FIELD_BOOL("Bool");                // Question/confirmation requests
	/** Sequence number for oneway messages */
	static const std::string FIELD_MSG_SEQUENCE("Sequence");

//###############################################################################
// Executor messages fields
//###############################################################################

	/** Executor name */
	static const std::string FIELD_PROC_NAME("name");
	/** Executor parent proc if any */
	static const std::string FIELD_PARENT_PROC("ParentId");
	/** Call stack position */
	static const std::string FIELD_CSP("Csp");
	/** Stage id */
	static const std::string FIELD_STAGE_ID("StageId");
	/** Stage title */
	static const std::string FIELD_STAGE_TL("StageTl");
	/** Holds the executor port */
	static const std::string FIELD_EXEC_PORT("ExecutorPort");        // Exec info, Open exec, Attach exec
	/** Holds the executor port */
	static const std::string FIELD_EXEC_STATUS("ExecutorStatus");      // Exec info, Open exec, Attach exec
	/** Holds the controlling gui */
	static const std::string FIELD_GUI_CONTROL("GuiControl");
	/** Holds the list of monitoring guis */
	static const std::string FIELD_GUI_LIST("GuiList");
	/** Holds the executor arguments (procedure arguments) */
	static const std::string FIELD_ARGS("Arguments");
	/** Field holding an arbitrary python script (script command) */
	static const std::string FIELD_SCRIPT("Script");
	/** Force flag for several operations */
	static const std::string FIELD_FORCE("Force");
	/** Field holding the step over flag (run command) */
	static const std::string FIELD_SO("So");
	/** Field holding the target line number (goto on gui) */
	static const std::string FIELD_GOTO_LINE("GotoLine");
	/** Field holding the target label (goto on gui) */
	static const std::string FIELD_GOTO_LABEL("GotoLabel");
	/** Holds the executor condition */
	static const std::string FIELD_CONDITION("Condition");
	/** Holds the controlling gui host */
	static const std::string FIELD_GUI_CONTROL_HOST("GuiControlHost");
	/** Holds the list of monitoring guis hosts */
	static const std::string FIELD_GUI_HOST_LIST("GuiHostList");
	/** Execution mode */
	static const std::string FIELD_EXECUTION_MODE("ExecutorMode");
	/** Toggle breakpoint code id */
	static const std::string FIELD_BREAKPOINT_PROC("BreakpointProc");
	/** Toggle breakpoint target line */
	static const std::string FIELD_BREAKPOINT_LINE("BreakpointLine");
	/** Breakpoint type */
	static const std::string FIELD_BREAKPOINT_TYPE("BreakpointType");
	/** Type of messages for write messages */
	static const std::string FIELD_MSGTYPE("MsgType");
	/** Message text */
	static const std::string FIELD_TEXT("Text");
	/** Message level */
	static const std::string FIELD_LEVEL("Level");
	/** Prompt messages, expected values */
	static const std::string FIELD_EXPECTED("ExpectedValues");
	/** Prompt return value */
	static const std::string FIELD_RVALUE("ReturnValue");
	/** Prompt options */
	static const std::string FIELD_OPTIONS("OptionValues");
	/** Line in the subprocedure */
	static const std::string FIELD_SLINE("SubprocLine");

	/** AsRUN file name */
	static const std::string FIELD_ASRUN_NAME("AsRunName");
	/** Log file name */
	static const std::string FIELD_LOG_NAME("LogName");
	/** Server file type identifier */
	static const std::string FIELD_SERVER_FILE_ID("ServerFileId");

	/** User action label */
	static const std::string FIELD_ACTION_LABEL("ActionLabel");
	/** User action enabled flag */
	static const std::string FIELD_ACTION_ENABLED("ActionEnabled");
	/** User action severity */
	static const std::string FIELD_ACTION_SEVERITY("ActionSeverity");

	/** Which variable to get */
	static const std::string FIELD_VARIABLE_GET("VarGet");
	/** Variable name for watch */
	static const std::string FIELD_VARIABLE_NAME("VarName");
	/** Variable value for watch */
	static const std::string FIELD_VARIABLE_VALUE("VarValue");
	/** Variable type for watch */
	static const std::string FIELD_VARIABLE_TYPE("VarType");
	/** Variable global flag for watch */
	static const std::string FIELD_VARIABLE_GLOBAL("VarGlobal");
	/** Variable registered flag for watch */
	static const std::string FIELD_VARIABLE_REGISTERED("VarRegistered");

//###############################################################################
//# UI notification fields
//###############################################################################

	/** Subprocedure identifier for notifications */
	static const std::string FIELD_NOTIF_SUBPROC_ID("SubProcId");
	/** Item type in notifications */
	static const std::string FIELD_NOTIF_ITEM_TYPE("ItemType");
	/** Item name in notifications */
	static const std::string FIELD_NOTIF_ITEM_NAME("ItemName");
	/** Item value in notifications */
	static const std::string FIELD_NOTIF_ITEM_VALUE("ItemValue");
	/** Item status in notifications */
	static const std::string FIELD_NOTIF_ITEM_STATUS("ItemStatus");
	/** Item comments in notifications */
	static const std::string FIELD_NOTIF_ITEM_REASON("ItemReason");
	/** Item time in notifications */
	static const std::string FIELD_NOTIF_ITEM_TIME("ItemTime");
	/** Status notification */
	static const std::string FIELD_NOTIF_STATUS("Status");
	/** Success items count */
	static const std::string FIELD_NOTIF_ITEM_SCOUNT("SCount");

//###############################################################################
//# Listener-related fields
//###############################################################################
	/** Context name */
	static const std::string FIELD_CTX_NAME("ContextName");
	/** Context port */
	static const std::string FIELD_CTX_PORT("ContextPort");
};

/** Defines values for the message fields */
namespace MessageValue
{
	/** Control client mode */
	static const std::string  DATA_GUI_MODE_C("CONTROL");
	/** Monitor client mode */
	static const std::string  DATA_GUI_MODE_M("MONITOR");
	/** Background execution mode */
	static const std::string  DATA_GUI_MODE_B("BACKGROUND");
	/** Schedule execution mode */
	static const std::string  DATA_GUI_MODE_S("SCHEDULE");

	// Executor operation codes (FIELD_EXOP)
	/** Executor open */
	static const std::string  DATA_EXOP_OPEN("OPEN");
	/** Executor closed */
	static const std::string  DATA_EXOP_CLOSE("CLOSE");
	/** Executor killed */
	static const std::string  DATA_EXOP_KILL("KILL");
	/** Executor attached */
	static const std::string  DATA_EXOP_ATTACH("ATTACH");
	/** Executor detached */
	static const std::string  DATA_EXOP_DETACH("DETACH");
	/** Executor failed */
	static const std::string  DATA_EXOP_ERROR("ERROR");

	// Client operation codes (FIELD_CLOP)
	/** Client logged in */
	static const std::string  DATA_CLOP_LOGIN     = "LOGIN";
	/** Client logged out */
	static const std::string  DATA_CLOP_LOGOUT    = "LOGOUT";

	// Executor messages field values
	/** Current line data type for notifications */
	static const std::string DATA_TYPE_LINE("CURRENT_LINE");
	/** Call data type for notifications */
	static const std::string DATA_TYPE_CALL("CALL");
	/** Return data type for notifications */
	static const std::string DATA_TYPE_RETURN("RETURN");
	/** Item data type for notifications */
	static const std::string DATA_TYPE_ITEM("ITEM");
	/** Status data type for notifications */
	static const std::string DATA_TYPE_STATUS("STATUS");

	// Executor status codes (FIELD_EXEC_STATUS)
	/** Status code aborted */
	static const std::string DATA_STATUS_ABORTED("ABORTED");
	/** Status code running */
	static const std::string DATA_STATUS_RUNNING("RUNNING");
	/** Status code loaded */
	static const std::string DATA_STATUS_LOADED("LOADED");
	/** Status code paused */
	static const std::string DATA_STATUS_PAUSED("PAUSED");
	/** Status code uninit */
	static const std::string DATA_STATUS_UNINIT("UNINIT");
	/** Status code finished */
	static const std::string DATA_STATUS_FINISHED("FINISHED");
	/** Status code waiting */
	static const std::string DATA_STATUS_WAITING("WAITING");
	/** Status code error */
	static const std::string DATA_STATUS_ERROR("ERROR");
	/** Status code interrupted */
	static const std::string DATA_STATUS_INTERRUPTED("INTERRUPTED");
	/** Status code reloading */
	static const std::string DATA_STATUS_RELOADING("RELOADING");
	/** Status code unknown */
	static const std::string DATA_STATUS_UNKNOWN("UNKNOWN");

	// Typs of server files
	/** Server file type AsRUN **/
	static const std::string DATA_FILE_ASRUN("ASRUN");
	/** Server file type LOG */
	static const std::string DATA_FILE_EXEC_LOG("EXECUTOR_LOG");

//###############################################################################
//# UI notification type values
	/** Notification data type item */
	static const std::string DATA_NOTIF_TYPE_ITEM("ITEM");
	/** Notification data type value */
	static const std::string DATA_NOTIF_TYPE_VAL("VALUE");
	/** Notification data type verification */
	static const std::string DATA_NOTIF_TYPE_VERIF("VERIFICATION");
	/** Notification data type execution */
	static const std::string DATA_NOTIF_TYPE_EXEC("EXECUTION");
	/** Notification data type system */
	static const std::string DATA_NOTIF_TYPE_SYS("SYSTEM");
	/** Notification data type time */
	static const std::string DATA_NOTIF_TYPE_TIME("TIME");

//###############################################################################
//# UI notification status values
	/** Success operation */
	static const std::string DATA_NOTIF_STATUS_OK("SUCCESS");
	/** In progress operation */
	static const std::string DATA_NOTIF_STATUS_PR("IN PROGRESS");
	/** Failed operation */
	static const std::string DATA_NOTIF_STATUS_FL("FAILED");
	/** Skipped operation */
	static const std::string DATA_NOTIF_STATUS_SP("SKIPPED");

//###############################################################################
//# Execution mode values
	/** Manual execution */
	static const std::string DATA_EXEC_MODE_MANUAL("MANUAL");
	/** Procedure execution */
	static const std::string DATA_EXEC_MODE_PROCEDURE("PROCEDURE");

//###############################################################################
//# Other values
	/** True value */
	static const std::string DATA_TRUE("True");
	/** False value */
	static const std::string DATA_FALSE("False");

//###############################################################################
//# Severities
	static const std::string DATA_SEVERITY_INFO("INFO");
	static const std::string DATA_SEVERITY_WARN("WARN");
	static const std::string DATA_SEVERITY_ERROR("ERROR");

//###############################################################################
//# Breakpoint types
	/** True value */
	static const std::string BREAKPOINT_PERMANENT("PERMANENT");
	/** False value */
	static const std::string BREAKPOINT_TEMPORARY("TEMPORARY");
	/** False value */
	static const std::string BREAKPOINT_UNKNOWN("UNKNOWN");

//###############################################################################
//# Variable retrieval types
	static const std::string AVAILABLE_ALL("AVAILABLE_ALL");
	static const std::string AVAILABLE_GLOBALS("AVAILABLE_GLOBALS");
	static const std::string AVAILABLE_LOCALS("AVAILABLE_LOCALS");
	static const std::string REGISTERED_ALL("REGISTERED_ALL");
	static const std::string REGISTERED_GLOBALS("REGISTERED_GLOBALS");
	static const std::string REGISTERED_LOCALS("REGISTERED_LOCALS");
};

/** Defines listener process messages */
namespace ListenerMessages
{
	// Login message for listener
	/** Context open notification */
	static const std::string  MSG_CONTEXT_OPEN("MSG_CONTEXT_OPEN");
	/** Contex closed notification */
	static const std::string  MSG_CONTEXT_CLOSED("MSG_CONTEXT_CLOSED");
}

/** Defines context process messages */
namespace ContextMessages
{
	/** Request list of available executors (running procedures) */
	static const std::string  REQ_EXEC_LIST("REQ_EXEC_LIST");
	/** Response to REQ_EXEC_LIST */
	static const std::string  RSP_EXEC_LIST("RSP_EXEC_LIST");

	/** Request executor (procedure) information */
	static const std::string  REQ_EXEC_INFO("REQ_EXEC_INFO");
	/** Response to REQ_EXEC_INFO */
	static const std::string  RSP_EXEC_INFO("RSP_EXEC_INFO");

	/** Request opening a new executor (procedure)
	 This request may be sent by executors as well, when starting a procedure
	 by using StartProc function. */
	static const std::string  REQ_OPEN_EXEC("REQ_OPEN_EXEC");
	/** Response to REQ_OPEN_EXEC */
	static const std::string  RSP_OPEN_EXEC("RSP_OPEN_EXEC");

	/** Request closing an executor */
	static const std::string  REQ_CLOSE_EXEC("REQ_CLOSE_EXEC");
	/** Response to REQ_CLOSE_EXEC */
	static const std::string  RSP_CLOSE_EXEC("RSP_CLOSE_EXEC");

	/** Request killing an executor */
	static const std::string  REQ_KILL_EXEC("REQ_KILL_EXEC");
	/** Response to REQ_KILL_EXEC */
	static const std::string  RSP_KILL_EXEC("RSP_KILL_EXEC");

	/** Request attaching to an executor */
	static const std::string  REQ_ATTACH_EXEC("REQ_ATTACH_EXEC");
	/** Response to REQ_ATTACH_EXEC */
	static const std::string  RSP_ATTACH_EXEC("RSP_ATTACH_EXEC");

	/** Request detaching from an executor */
	static const std::string  REQ_DETACH_EXEC("REQ_DETACH_EXEC");
	/** Response to REQ_DETACH_EXEC */
	static const std::string  RSP_DETACH_EXEC("RSP_DETACH_EXEC");

	/** Request for procedure list */
	static const std::string  REQ_PROC_LIST("REQ_PROC_LIST");
	/** Response to REQ_PROC_LIST */
	static const std::string  RSP_PROC_LIST("RSP_PROC_LIST");

	/** Request for procedure properties */
	static const std::string  REQ_PROC_PROP("REQ_PROC_PROP");
	/** Response to REQ_PROC_PROP */
	static const std::string  RSP_PROC_PROP("RSP_PROC_PROP");

	/** Request for procedure code */
	static const std::string  REQ_PROC_CODE("REQ_PROC_CODE");
	/** Response to REQ_PROC_CODE */
	static const std::string  RSP_PROC_CODE("RSP_PROC_CODE");

	/** Request for client info */
	static const std::string  REQ_CLIENT_INFO("REQ_CLIENT_INFO");
	/** Response to REQ_CLIENT_INFO */
	static const std::string  RSP_CLIENT_INFO("RSP_CLIENT_INFO");

	/** Request for a server file */
	static const std::string  REQ_SERVER_FILE("REQ_SERVER_FILE");
	/** Response to REQ_SERVER_FILE */
	static const std::string  RSP_SERVER_FILE("RSP_SERVER_FILE");

	/** Request for available instance id */
	static const std::string  REQ_INSTANCE_ID("REQ_INSTANCE_ID");
	/** Response to REQ_INSTANCE_ID */
	static const std::string  RSP_INSTANCE_ID("RSP_INSTANCE_ID");

	/** Request for closure condition */
	static const std::string  REQ_CAN_CLOSE("REQ_CAN_CLOSE");
	/** Response to REQ_CAN_CLOSE */
	static const std::string  RSP_CAN_CLOSE("RSP_CAN_CLOSE");

	// ################################################################################
	// MESSAGES FROM LISTENER
	// ################################################################################

	/** Message for closing the context */
	static const std::string  MSG_CLOSE_CTX("MSG_CLOSE_CTX");

	// ################################################################################
	// # MESSAGES TO SPELL CLIENTS
	// ################################################################################

	/** Sent by GUIs when starting a session */
	static const std::string  MSG_GUI_LOGIN("MSG_GUI_LOGIN");
	/** Sent by GUIs when ending a session */
	static const std::string  MSG_GUI_LOGOUT("MSG_GUI_LOGOUT");
	/** Sent by context to GUIs when a client logs in/out */
	static const std::string  MSG_CLIENT_OP("MSG_CLIENT_OP");
	/** Sent by context to GUIs when a executor changes the status, is open/closed... */
	static const std::string  MSG_EXEC_OP("MSG_EXEC_OP");
	/** Sent by context to GUIs when there is an executor control error in context side */
	static const std::string  MSG_EXEC_ERROR("MSG_EXEC_ERROR");
	/** Sent by context to GUIs when the listener connection is lost */
	static const std::string  MSG_LISTENER_LOST("MSG_LISTENER_LOST");
};

/** Defines requests and messages regarding the executor process */
namespace ExecutorMessages
{
	/** Request for executor status */
	static const std::string REQ_EXEC_STATUS("REQ_EXEC_STATUS");
	/** Response to REQ_EXEC_STATUS */
	static const std::string RSP_EXEC_STATUS("RSP_EXEC_STATUS");

	/** Request changing configuration */
	static const std::string REQ_SET_CONFIG("REQ_SET_CONFIG");
	/** Response to REQ_SET_CONFIG*/
	static const std::string RSP_SET_CONFIG("RSP_SET_CONFIG");
	/** Request the current configuration */
	static const std::string REQ_GET_CONFIG("REQ_GET_CONFIG");
	/** Response to REQ_GET_CONFIG */
	static const std::string RSP_GET_CONFIG("RSP_GET_CONFIG");

	/** Request for toggling breakpoints */
	static const std::string REQ_SET_BREAKPOINT("REQ_SET_BREAKPOINT");
	/** Response to REQ_TOGGLE_BREAKPOINT */
	static const std::string RSP_SET_BREAKPOINT("RSP_SET_BREAKPOINT");

	/** Request for clearing breakpoints */
	static const std::string REQ_CLEAR_BREAKPOINT("REQ_CLEAR_BREAKPOINT");
	/** Response to REQ_CLEAR_BREAKPOINT */
	static const std::string RSP_CLEAR_BREAKPOINT("RSP_CLEAR_BREAKPOINT");

	/** Request for retrieving variable names */
	static const std::string REQ_VARIABLE_NAMES("REQ_VARIABLE_NAMES");
	/** Response */
	static const std::string RSP_VARIABLE_NAMES("RSP_VARIABLE_NAMES");

	/** Request for registering variable watch */
	static const std::string REQ_VARIABLE_WATCH("REQ_VARIABLE_WATCH");
	/** Response */
	static const std::string RSP_VARIABLE_WATCH("RSP_VARIABLE_WATCH");

	/** Request for unregistering variable watch */
	static const std::string REQ_VARIABLE_NOWATCH("REQ_VARIABLE_NOWATCH");
	/** Response */
	static const std::string RSP_VARIABLE_NOWATCH("RSP_VARIABLE_NOWATCH");

	/** Request for unregistering all watches */
	static const std::string REQ_WATCH_NOTHING("REQ_WATCH_NOTHING");
	/** Response */
	static const std::string RSP_WATCH_NOTHING("RSP_WATCH_NOTHING");

	/** Request for changing variables */
	static const std::string REQ_CHANGE_VARIABLE("REQ_CHANGE_VARIABLE");
	/** Response */
	static const std::string RSP_CHANGE_VARIABLE("RSP_CHANGE_VARIABLE");

	/** Oneway message for variable change notifications */
	static const std::string MSG_VARIABLE_CHANGE("MSG_VARIABLE_CHANGE");

	/** Oneway message for variable scope change notifications */
	static const std::string MSG_SCOPE_CHANGE("MSG_SCOPE_CHANGE");

	//################################################################################
	//# REQUESTS TO CONTEXT
	//################################################################################

	/** Sent by the executor to the context when ready to work */
	static const std::string REQ_NOTIF_EXEC_OPEN("REQ_NOTIF_EXEC_OPEN");
	/** Response to REQ_NOTIF_EXEC_OPEN */
	static const std::string RSP_NOTIF_EXEC_OPEN("RSP_NOTIF_EXEC_OPEN");
	/** Sent by executor when closing */
	static const std::string MSG_NOTIF_EXEC_CLOSE("MSG_NOTIF_EXEC_CLOSE");
	/** Sent by the executor to the context to open a child procedure */
	static const std::string REQ_OPEN_EXEC("REQ_OPEN_EXEC");
	/** Sent by the executor to the context to kill a child procedure */
	static const std::string REQ_KILL_EXEC("REQ_KILL_EXEC");
	/** Sent by the executor to the context to close a child procedure */
	static const std::string REQ_CLOSE_EXEC("REQ_CLOSE_EXEC");
	/** To acquire an instance id */
	static const std::string REQ_INSTANCE_ID("REQ_INSTANCE_ID");

	//################################################################################
	//# COMMANDS FROM SPELL CLIENTS (coming through context)
	//################################################################################

	// Commands sent by the GUI

	/** Run command */
	static const std::string MSG_CMD_RUN("CMD_RUN");
	/** Reload comand */
	static const std::string MSG_CMD_RELOAD("CMD_RELOAD");
	/** Recover command */
	static const std::string MSG_CMD_RECOVER("CMD_RECOVER");
	/** Step command */
	static const std::string MSG_CMD_STEP("CMD_STEP");
	/** Step over command */
	static const std::string MSG_CMD_STEP_OVER("CMD_STEP_OVER");
	/** Skip command */
	static const std::string MSG_CMD_SKIP("CMD_SKIP");
	/** Goto command */
	static const std::string MSG_CMD_GOTO("CMD_GOTO");
	/** Pause command */
	static const std::string MSG_CMD_PAUSE("CMD_PAUSE");
	/** Abort command */
	static const std::string MSG_CMD_ABORT("CMD_ABORT");
	/** Close command */
	static const std::string MSG_CMD_CLOSE("CMD_CLOSE");
	/** Script command */
	static const std::string MSG_CMD_SCRIPT("CMD_SCRIPT");
	/** Action command */
	static const std::string MSG_CMD_ACTION("CMD_ACTION");

	// User action

	/** Set user action */
	static const std::string MSG_ID_SET_UACTION("MSG_SET_UACTION");
	/** Enable user action */
	static const std::string MSG_ID_ENABLE_UACTION("MSG_ENABLE_UACTION");
	/** Disable user action */
	static const std::string MSG_ID_DISABLE_UACTION("MSG_DISABLE_UACTION");
	/** Dismiss user action */
	static const std::string MSG_ID_DISMISS_UACTION("MSG_DISMISS_UACTION");
	/** Add client to the executor */
	static const std::string MSG_ID_ADD_CLIENT("MSG_ADD_CLIENT");
	/** Remove client from executor */
	static const std::string MSG_ID_REMOVE_CLIENT("MSG_REMOVE_CLIENT");
	/** Set callstack node depth */
	static const std::string MSG_ID_NODE_DEPTH("MSG_SHOW_NODE_DEPTH");
};

/** Defines the message type tags */
namespace MessageType
{
	/** Oneway (asynchronous) messages */
	static const std::string MSG_TYPE_ONEWAY("command");
	/** Notification messages */
	static const std::string MSG_TYPE_NOTIFY("notify");
	/** Asynchronous notification messages */
	static const std::string MSG_TYPE_NOTIFY_ASYNC("notify_async");
	/** Generic request */
	static const std::string MSG_TYPE_REQUEST("request");
	/** Prompt request */
	static const std::string MSG_TYPE_PROMPT("prompt");
	/** Generic response */
	static const std::string MSG_TYPE_RESPONSE("response");
	/** Write oneway message */
	static const std::string MSG_TYPE_WRITE("write");
	/** Error message */
	static const std::string MSG_TYPE_ERROR("error");
	/** End-of-conversation message */
	static const std::string MSG_TYPE_EOC("eoc");
	/** No type */
	static const std::string MSG_TYPE_NOTYPE("NOTYPE");
	/** Unknown */
	static const std::string MSG_TYPE_UNKNOWN("UNKNOWN");
	/** For mapping message types to tag names */
	static const std::string TypeStr[] =
	{
		MSG_TYPE_ONEWAY,
		MSG_TYPE_NOTIFY,
		MSG_TYPE_NOTIFY_ASYNC,
		MSG_TYPE_REQUEST,
		MSG_TYPE_PROMPT,
		MSG_TYPE_RESPONSE,
		MSG_TYPE_WRITE,
		MSG_TYPE_ERROR,
		MSG_TYPE_EOC,
		MSG_TYPE_NOTYPE,
		MSG_TYPE_UNKNOWN
	};
};

/** Special IPC internal parameters */
namespace IPCinternals
{
	/** Separator for file and data lines */
	static const std::string CODE_SEPARATOR("%C%");
	/** Separator for items in notifications */
	static const std::string ARG_SEPARATOR(",,");
	/** Separator for keys and values in prompts */
	static const std::string KEY_SEPARATOR(":");
	/** Separator for options in prompts */
	static const std::string OPT_SEPARATOR("|");
};

/*@}*/
#endif

