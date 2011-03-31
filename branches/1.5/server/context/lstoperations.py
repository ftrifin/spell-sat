###############################################################################

"""
PACKAGE 
    server.context.lstoperations 
FILE
    lstoperations.py
    
DESCRIPTION
    Implementation of all listener requests
    
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

###############################################################################

#*******************************************************************************
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
import server.core.messages.context
import server.core.messages.listener
from server.core.messages.base import *
from server.core.ipc.xmlmsg import *
from server.core.messages.msghelper import *
from server.procedures.manager import *
import server.executor.status as ExecStatus

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import traceback,sys

################################################################################

# Shorcut to context messages
CtxMessages = server.core.messages.context
LstMessages = server.core.messages.listener

################################################################################
class LSTOperations(object):
    
    context = None
    
    #===========================================================================
    def __init__(self, ctx):
        self.context = ctx
        
    #===========================================================================
    def processRequest(self, msg):
        LOG("Received LST request: " + msg.getId(), level = LOG_COMM)
        
        #-----------------------------------------------------------------------
        # Request can close
        #-----------------------------------------------------------------------
        if msg.getId() == CtxMessages.REQ_CAN_CLOSE:
            return self.request_CanClose(msg)
        
        return MsgHelper.createError(msg.getId(), msg, "Cannot process request", "Unknown request ID")

    #===========================================================================
    def processMessage(self, msg):
        LOG("Received LST message: " + msg.getId(), level = LOG_COMM)
        
        #-----------------------------------------------------------------------
        # Request executor list        
        #-----------------------------------------------------------------------
        if msg.getId() == CtxMessages.MSG_CLOSE_CTX:
            self.context.stop()
        #-----------------------------------------------------------------------
        # Unknown request id, forward to executor
        #-----------------------------------------------------------------------
        else:
            LOG("Cannot process message, unknown ID: " + msg.getId(), LOG_ERROR)

    #===========================================================================
    def request_CanClose(self, msg):
        resp = MsgHelper.createResponse(CtxMessages.RSP_CAN_CLOSE, msg)
        numClients = len(self.context.getClients())
        numActiveProcedures = 0
        for key in self.context.getExecutors():
            executor = self.context.getExecutor(key)
            status = executor.getStatus()
            if status in [ExecStatus.PAUSED,ExecStatus.RUNNING,ExecStatus.WAITING,ExecStatus.STEPPING]:
                numActiveProcedures += 1
        canClose = (numClients==0) and (numActiveProcedures==0)
        resp[CtxMessages.FIELD_BOOL] = canClose  
        return resp
    
