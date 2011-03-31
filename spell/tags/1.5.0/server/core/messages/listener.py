################################################################################

"""
PACKAGE 
    server.core.messages.listener
FILE
    listener.py
    
DESCRIPTION
    Message definitions, fields and values for SPELL messages exchanged with
    SPEL Listener process
    
PROJECT: SPELL

 Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 SPELL is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SPELL is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with SPELL. If not, see <http://www.gnu.org/licenses/>.

"""

################################################################################

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
# REQUESTS COMING FROM SPELL CLIENTS
################################################################################

# Request list of available contexts 
REQ_CTX_LIST        = "REQ_CTX_LIST"
RSP_CTX_LIST        = "RSP_CTX_LIST"

# Request context information
REQ_CTX_INFO        = "REQ_CTX_INFO"
RSP_CTX_INFO        = "RSP_CTX_INFO"

# Request opening a context 
REQ_OPEN_CTX        = "REQ_OPEN_CTX"
RSP_OPEN_CTX        = "RSP_OPEN_CTX"

# Request closing a context (GUI)
REQ_CLOSE_CTX       = "REQ_CLOSE_CTX"
RSP_CLOSE_CTX       = "RSP_CLOSE_CTX"

# Request attaching to a context 
REQ_ATTACH_CTX      = "REQ_ATTACH_CTX"      
RSP_ATTACH_CTX      = "RSP_ATTACH_CTX"      

# Request attaching to a context 
REQ_DESTROY_CTX      = "REQ_DESTROY_CTX"      
RSP_DESTROY_CTX      = "RSP_DESTROY_CTX"      

################################################################################
# MESSAGES SENT BY CONTEXTS
################################################################################

# Sent by contexts when started
MSG_CONTEXT_OPEN    = "MSG_CONTEXT_OPEN"
# Sent by contexts when closed
MSG_CONTEXT_CLOSED  = "MSG_CONTEXT_CLOSED"
# Sent by listenerto GUIs when the context connection is lost
MSG_CONTEXT_LOST    = "MSG_CONTEXT_LOST"

################################################################################
# MESSAGES SENT BY SPELL CLIENTS
################################################################################

# Sent by GUIs when connecting
MSG_GUI_LOGIN       = "MSG_GUI_LOGIN"
# Sent by GUIs when disconnecting
MSG_GUI_LOGOUT      = "MSG_GUI_LOGOUT"

################################################################################
# MESSAGES SENT TO SPELL CLIENTS
################################################################################

# Sent to GUIs to notify context updates
MSG_CONTEXT_OP      = "MSG_CONTEXT_OP"

################################################################################
# MESSAGE FIELDS
################################################################################

# Holds the context name
FIELD_CTX_NAME      = "ContextName"         # Ctx info, Open ctx, Attach ctx
# Holds the S/C name
FIELD_CTX_SC        = "ContextSC"           # Ctx info
# Holds a comma separated list of context names
FIELD_CTX_LIST      = "ContextList"         # Ctx list
# Holds the context status (AVAILABLE/RUNNING)
FIELD_CTX_STATUS    = "ContextStatus"       # Ctx info
# Holds the context listening port
FIELD_CTX_PORT      = "ContextPort"         # Ctx info, Open ctx resp, Ctx open
# Holds the context driver
FIELD_CTX_DRV       = "ContextDriver"       # Ctx info
# Holds the context family
FIELD_CTX_FAM       = "ContextFamily"       # Ctx info
# Holds the context GCS
FIELD_CTX_GCS       = "ContextGCS"          # Ctx info
# Holds the context interfaces
FIELD_CTX_IFCS      = "ContextInterfaces"   # Ctx info
# Holds the context description
FIELD_CTX_DESC      = "ContextDescription"  # Ctx info
# Holds the maximum number of procedures
FIELD_CTX_MAXPROC   = "MaxProc"             # Ctx info

################################################################################
# FIELD VALUES
################################################################################

# For context status (FIELD_CTX_STATUS)
DATA_CTX_AVAILABLE  = "AVAILABLE"
DATA_CTX_RUNNING    = "RUNNING"
DATA_CTX_ERROR      = "ERROR"
DATA_CTX_STARTING   = "STARTING"
DATA_CTX_STOPPING   = "STOPPING"
