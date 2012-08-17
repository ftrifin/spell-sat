###################################################################################
## MODULE     : core.messages.executor
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Executor messages
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with SPELL. If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################

#*******************************************************************************
# SPELL Imports
#*******************************************************************************

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
# REQUESTS FROM SPELL CLIENTS (coming through context)
################################################################################

# Request for the current line
REQ_CLINE        = "REQ_CLINE"
RSP_CLINE        = "RSP_CLINE"

# Request for executor status
REQ_EXEC_STATUS  = "REQ_EXEC_STATUS"
RSP_EXEC_STATUS  = "RSP_EXEC_STATUS"

# Request for configuration management
REQ_SET_CONFIG   = 'REQ_SET_CONFIG'
RSP_SET_CONFIG   = 'RSP_SET_CONFIG'
REQ_GET_CONFIG   = 'REQ_GET_CONFIG'
RSP_GET_CONFIG   = 'RSP_GET_CONFIG'

################################################################################
# REQUESTS TO CONTEXT
################################################################################

# Sent by the executor to the context when ready to work
REQ_NOTIF_EXEC_OPEN    = "REQ_NOTIF_EXEC_OPEN"
RSP_NOTIF_EXEC_OPEN    = "RSP_NOTIF_EXEC_OPEN"
# Sent by executor when closing
MSG_NOTIF_EXEC_CLOSE   = "MSG_NOTIF_EXEC_CLOSE"
# Sent by the executor to the context when a client is (de)attached to the executor
MSG_ADD_CLIENT         = "MSG_ADD_CLIENT"
MSG_REMOVE_CLIENT      = "MSG_REMOVE_CLIENT"

################################################################################
# COMMANDS FROM SPELL CLIENTS (coming through context)
################################################################################

# Commands sent by the GUI
CMD_RUN          = 'CMD_RUN'
CMD_RELOAD       = 'CMD_RELOAD'
CMD_STEP         = 'CMD_STEP'
CMD_STEP_OVER    = 'CMD_STEP_OVER'
CMD_SKIP         = 'CMD_SKIP'
CMD_GOTO         = 'CMD_GOTO'
CMD_PAUSE        = 'CMD_PAUSE'
CMD_ABORT        = 'CMD_ABORT'
CMD_CLOSE        = 'CMD_CLOSE'
CMD_SCRIPT       = 'CMD_SCRIPT'
CMD_ACTION       = 'CMD_ACTION'

# Oneway messages
MSG_ID_SETUACTION = "MSG_SET_UACTION"
MSG_ID_ENABLEUACTION = "MSG_ENABLE_UACTION"
MSG_ID_DISABLEUACTION = "MSG_DISABLE_UACTION"
MSG_ID_DISMISSUACTION = "MSG_DISMISS_UACTION"

################################################################################
# MESSAGE FIELDS
################################################################################

# Executor messages fields
FIELD_PROC_ID       = 'ProcId'
# Executor name
FIELD_PROC_NAME     = 'ProcName'
# Executor parent proc if any
FIELD_PARENT_PROC   = 'ParentId'
# Call stack position
FIELD_CSP           = "Csp"
# Stage id
FIELD_STAGE_ID      = "StageId"
# Stage title
FIELD_STAGE_TL      = "StageTl"
# Holds the executor port
FIELD_EXEC_PORT     = "ExecutorPort"        # Exec info, Open exec, Attach exec
# Holds the executor port
FIELD_EXEC_STATUS   = "ExecutorStatus"      # Exec info, Open exec, Attach exec
# Holds the controlling gui 
FIELD_GUI_CONTROL   = "GuiControl"
# Holds the list of monitoring guis
FIELD_GUI_LIST      = "GuiList"
# Holds the executor arguments (procedure arguments)
FIELD_ARGS          = "Arguments"
# Field holding an arbitrary python script (script command)
FIELD_SCRIPT        = "Script"
# Force flag for several operations
FIELD_FORCE         = "Force"
# Field holding the step over flag (run command)
FIELD_SO            = "So"
# Field holding the target line number (goto on gui)
FIELD_GOTO_LINE     = "GotoLine"
# Field holding the target label (goto on gui)
FIELD_GOTO_LABEL    = "GotoLabel"
# Holds the executor condition
FIELD_CONDITION     = "Condition"
# Holds the bacgrkound flag
FIELD_BACKGROUND    = "Background"
# Holds the controlling gui host
FIELD_GUI_CONTROL_HOST  = "GuiControlHost"
# Holds the list of monitoring guis hosts
FIELD_GUI_HOST_LIST     = "GuiHostList"

# Write messages
FIELD_MSGTYPE       = 'MsgType'
FIELD_TEXT          = 'Text'
FIELD_LEVEL         = 'Level'
# Prompt messages
FIELD_EXPECTED      = 'ExpectedValues'
FIELD_RVALUE        = 'ReturnValue'
FIELD_OPTIONS       = 'OptionValues'
# Line notifications (deprecated)
FIELD_LINE          = 'CurrentLine'
FIELD_SLINE         = 'SubprocLine'
FIELD_EOS           = 'EndOfScript'
# Code notifications
FIELD_PROC_CODE     = "ProcCode"
FIELD_ASRUN_NAME    = "AsRunName"            
FIELD_LOG_NAME      = "LogName"            
FIELD_SERVER_FILE_ID= "ServerFileId"

FIELD_ITEM_NAME     = 'ItemName'
FIELD_ITEM_TYPE     = 'ItemType'
FIELD_ITEM_STATUS   = 'ItemStatus'

FIELD_EXECUTION_MODE= "ExecutorMode"

FIELD_ACTION_LABEL  = "ActionLabel"
FIELD_ACTION_ENABLED = "ActionEnabled"
FIELD_ACTION_SEV     = "ActionSeverity"
################################################################################
# FIELD VALUES
################################################################################

# Executor messages field values (FIELD_DATA_TYPE, see base)
DATA_TYPE_LINE      = 'CURRENT_LINE'
DATA_TYPE_ITEM      = 'ITEM'
DATA_TYPE_STATUS    = 'STATUS'
DATA_TYPE_CODE      = 'CODE'

# Execution mode
DATA_EXEC_MODE_MANUAL = 'MANUAL'
DATA_EXEC_MODE_PROCEDURE = 'PROCEDURE'

# Executor status codes (FIELD_EXEC_STATUS)
DATA_STATUS_ABORTED  = 'ABORTED' # Aborted
DATA_STATUS_RUNNING  = 'RUNNING' # Running (play)
DATA_STATUS_STEPPING = 'STEPPING'# Stepping
DATA_STATUS_LOADED   = 'LOADED'  # Loaded and ready
DATA_STATUS_PAUSED   = 'PAUSED'  # Paused by user
DATA_STATUS_UNINIT   = 'UNINIT'  # Uninit status
DATA_STATUS_FINISHED = 'FINISHED'# Finished nominal
DATA_STATUS_HOLD     = 'HOLD'    # Blocked processing something on driver
DATA_STATUS_WAITING  = 'WAITING' # Waiting request response
DATA_STATUS_ERROR    = 'ERROR'   # Error found

# Typs of server files
DATA_FILE_ASRUN      = 'ASRUN'
DATA_FILE_EXEC_LOG   = 'EXECUTOR_LOG'
