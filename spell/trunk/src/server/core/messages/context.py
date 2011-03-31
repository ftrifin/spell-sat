###################################################################################
## MODULE     : core.messages.context
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Context messages
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
# REQUESTS FROM SPELL CLIENTS
################################################################################

# Request list of available executors (running procedures)
REQ_EXEC_LIST        = "REQ_EXEC_LIST"
RSP_EXEC_LIST        = "RSP_EXEC_LIST"

# Request executor (procedure) information
REQ_EXEC_INFO        = "REQ_EXEC_INFO"
RSP_EXEC_INFO        = "RSP_EXEC_INFO"

# Request opening a new executor (procedure)
# NOTE: this request may be sent by executors as well, when starting a procedure
# by using StartProc function. 
REQ_OPEN_EXEC        = "REQ_OPEN_EXEC"
RSP_OPEN_EXEC        = "RSP_OPEN_EXEC"

# Request closing an executor 
REQ_CLOSE_EXEC       = "REQ_CLOSE_EXEC"
RSP_CLOSE_EXEC       = "RSP_CLOSE_EXEC"

# Request killing an executor 
REQ_KILL_EXEC        = "REQ_KILL_EXEC"
RSP_KILL_EXEC        = "RSP_KILL_EXEC"

# Request attaching to an executor 
REQ_ATTACH_EXEC      = "REQ_ATTACH_EXEC"      
RSP_ATTACH_EXEC      = "RSP_ATTACH_EXEC"      

# Request detaching from an executor 
REQ_DETACH_EXEC      = "REQ_DETACH_EXEC"      
RSP_DETACH_EXEC      = "RSP_DETACH_EXEC"      

# Request for procedure list 
REQ_PROC_LIST        = "REQ_PROC_LIST"      
RSP_PROC_LIST        = "RSP_PROC_LIST"      

# Request for procedure properties 
REQ_PROC_PROP        = "REQ_PROC_PROP"      
RSP_PROC_PROP        = "RSP_PROC_PROP"      

# Request for procedure code 
REQ_PROC_CODE        = "REQ_PROC_CODE"      
RSP_PROC_CODE        = "RSP_PROC_CODE"      

# Request for client info
REQ_CLIENT_INFO      = "REQ_CLIENT_INFO"      
RSP_CLIENT_INFO      = "RSP_CLIENT_INFO"      

# Request for a server file
REQ_SERVER_FILE         = "REQ_SERVER_FILE"      
RSP_SERVER_FILE         = "RSP_SERVER_FILE"      

# Request for available instance id
REQ_INSTANCE_ID      = "REQ_INSTANCE_ID"      
RSP_INSTANCE_ID      = "RSP_INSTANCE_ID"      

# Request for closure condition
REQ_CAN_CLOSE        = "REQ_CAN_CLOSE"
RSP_CAN_CLOSE        = "RSP_CAN_CLOSE"

################################################################################
# MESSAGES FROM LISTENER
################################################################################

# Message for closing the context 
MSG_CLOSE_CTX        = "MSG_CLOSE_CTX"     

################################################################################
# MESSAGES TO SPELL CLIENTS
################################################################################
      
# Sent by GUIs when starting a session
MSG_GUI_LOGIN        = "MSG_GUI_LOGIN"
# Sent by GUIs when ending a session
MSG_GUI_LOGOUT       = "MSG_GUI_LOGOUT"
# Sent by context to GUIs when a client logs in/out
MSG_CLIENT_OP        = "MSG_CLIENT_OP"
# Sent by context to GUIs when a executor changes the status, is open/closed...
MSG_EXEC_OP          = "MSG_EXEC_OP"
# Sent by context to GUIs when there is an executor control error in context side
MSG_EXEC_ERROR       = "MSG_EXEC_ERROR"
# Sent by context to GUIs when the listener connection is lost
MSG_LISTENER_LOST    = "MSG_LISTENER_LOST"

################################################################################
# MESSAGE FIELDS
################################################################################

# Holds a comma separated list of executor names
FIELD_EXEC_LIST     = "ExecutorList"        # Exec list
# Holds a comma separated list of procedure names
FIELD_PROC_LIST     = "ProcList"            # Proc list
# Holds the procedure id
FIELD_PROC_ID       = "ProcId"              # Proc properties
# Holds the instance id
FIELD_INSTANCE_ID   = "InstanceId"          # When opening a new proc
# Holds the procedure code
FIELD_PROC_CODE     = "ProcCode"            # Request code
# Holds a server file data
FIELD_SERVER_FILE   = "ServerFile"              # Request log
# Holds the GUI connection mode (commanding, monitoring)
FIELD_GUI_MODE      = "GuiMode"             # Open exec, Attach exec
# Holds client key
FIELD_GUI_KEY       = "GuiKey"              # Client and exec operations
# Executor operation
FIELD_EXOP          = "ExecOp"              # Exec operations
# Client operation
FIELD_CLOP          = "CltOp"               # Client operations
# Subprocedure identifier                 
FIELD_SPROC_ID      = "SprocId"             # Open request from executor
# Open mode
FIELD_OPEN_MODE     = "OpenMode"            # Open requests
# Boolean field
FIELD_BOOL          = "Bool"                # Question/confirmation requests
# Chunk mechanism: requested chunk
FIELD_CHUNK         = "CurrentChunk"
# Chunk mechanism: total chunks
FIELD_TOTAL_CHUNKS  = "TotalChunks"

################################################################################
# FIELD VALUES
################################################################################

# For gui mode
DATA_GUI_MODE_C      = "CONTROL"
DATA_GUI_MODE_M      = "MONITOR"
DATA_GUI_MODE_B      = "BACKGROUND"
DATA_GUI_MODE_S      = "SCHEDULE"

# Executor operation codes (FIELD_EXOP)
DATA_EXOP_OPEN       = 'OPEN'    # Executor open by somebody
DATA_EXOP_CLOSE      = 'CLOSE'   # Executor open by somebody
DATA_EXOP_KILL       = 'KILL'    # Executor killed by somebody
DATA_EXOP_ATTACH     = 'ATTACH'  # Executor attached by somebody
DATA_EXOP_DETACH     = 'DETACH'  # Executor released by somebody
DATA_EXOP_CRASH      = 'CRASH'   # Executor crashed

# Client operation codes (FIELD_CLOP)
DATA_CLOP_LOGIN      = 'LOGIN'   # A client logged in
DATA_CLOP_LOGOUT     = 'LOGOUT'  # A client logged out


