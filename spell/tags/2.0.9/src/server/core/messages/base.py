###################################################################################
## MODULE     : core.messages.base
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Base message definitions
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
# Types of messages
MSG_TYPE_COMMAND  = 'command'
MSG_TYPE_NOTIFY   = 'notify'
MSG_TYPE_NOTIFY_ASYNC   = 'notify_async'
MSG_TYPE_REQUEST  = 'request'
MSG_TYPE_PROMPT   = 'prompt'
MSG_TYPE_RESPONSE = 'response'
MSG_TYPE_WRITE    = 'write'
MSG_TYPE_ERROR    = 'error'
# End of conversation message
MSG_TYPE_EOC      = "eoc"

################################################################################
# Common msg id
MSG_ID_UNKNOWN       = 'MSG_UNKNOWN'
MSG_ID_CANCEL        = 'MSG_CANCEL'
MSG_ID_TIMEOUT       = 'MSG_TIMEOUT'
MSG_ID_PING          = 'MSG_PING'
MSG_ID_PROMPT_START  = 'MSG_PROMPT_START'
MSG_ID_PROMPT_END    = 'MSG_PROMPT_END'
# Notification type
FIELD_DATA_TYPE     = 'DataType'

################################################################################
# Fields appearing in basic messages (as well as 'Id'= message id, 
# and 'Src' = message source process)

# The IPC key of the originator
FIELD_IPC_KEY     = 'IpcKey'
# The IPC sequence counter
FIELD_SEQUENCE    = 'IpcSeq'
# The sender of the message 
FIELD_SENDER_ID   = 'SenderId'
# The target of the message
FIELD_RECEIVER_ID = 'ReceiverId'
# Error description
FIELD_ERROR       = 'ErrorMsg'
# Error reason
FIELD_REASON      = 'ErrorReason'
# Fatal flag
FIELD_FATAL       = 'FatalError'
# Host
FIELD_HOST        = 'Host'
# Message time
FIELD_TIME        = 'Time'
# Success items   
FIELD_SCOUNT      = 'SCount'

################################################################################
# Used when no sender/receiver are given
GENERIC_ID        = "GEN"

################################################################################
# Code lines separator
CODE_SEPARATOR="%C%"
# Argument separator
ARG_SEP = ",,"

