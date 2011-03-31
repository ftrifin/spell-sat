###################################################################################
## MODULE     : context.execoperations
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Executor operations
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
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
import server.core.messages.context
import server.core.messages.executor
from spell.lang.modifiers import *
from server.core.messages.base import *
from server.core.ipc.xmlmsg import *
from server.core.messages.msghelper import *
from server.procedures.manager import *
from server.executor.status import *
import server.context.context
#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import traceback,sys
from Queue import Empty

################################################################################

# Shorcut to context messages
CtxMessages = server.core.messages.context
ExcMessages = server.core.messages.executor

################################################################################
class EXCOperations(object):
    
    context = None
    
    #===========================================================================
    def __init__(self, ctx):
        self.context = ctx
        
    #===========================================================================
    def processMessage(self, msg):    
        return False
    
    #===========================================================================
    def processRequest(self, msg):
        execKey = msg.getKey()
        LOG("Received Executor internal request: " + msg.getId() + " from " + msg.getSender(), level = LOG_COMM)
        #-----------------------------------------------------------------------
        # Request opening an executor        
        #-----------------------------------------------------------------------
        if msg.getId() == CtxMessages.REQ_OPEN_EXEC:
            return self.request_OpenExecutor(msg)
        elif msg.getId() == CtxMessages.REQ_INSTANCE_ID:
            return self.request_InstanceId(msg)
        #-----------------------------------------------------------------------
        # Unknown request id, forward to executor
        #-----------------------------------------------------------------------
        else:
            return None
        
    #===========================================================================                
    def request_OpenExecutor(self, msg):
        execKey   = msg.getKey()
        procId    = msg[ExcMessages.FIELD_PROC_ID]
        sprocId   = msg[CtxMessages.FIELD_SPROC_ID]
        openStr   = msg[CtxMessages.FIELD_OPEN_MODE]
        LOG("Request open subproc: " + repr(sprocId) + ":" + repr(procId) + "[" + repr(openStr) + "]") 
        if openStr is None:
            openMode = {}
        else:
            openMode = eval(openStr)
        success = False
        if procId is None:
            txt = "Incomplete request"
            reason = "No executor name given"
            resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)
        elif sprocId is None:
            txt = "Incomplete request"
            reason = "No subprocedure id given"
            resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)
        else:
            # Process executor arguments, if any
            arguments = {}
            argStr = msg[ExcMessages.FIELD_ARGS]
            if argStr is not None and len(argStr.strip())>0:
                try:
                    arguments = eval(argStr)
                    LOG("Arguments: " + repr(arguments))
                except:
                    arguments = {}
                    LOG("Syntax error in arguments, not processed" , LOG_ERROR)
            else:
                LOG("No arguments given")
    
            # Obtain executor condition
            condition = msg[ExcMessages.FIELD_CONDITION]
    
            # Obtain the controlling client key from the parent
            parentProc = self.context.getExecutor(procId)
            clientKey = parentProc.getControllingClient()
            clientMode = CtxMessages.DATA_GUI_MODE_C
            
            try:
                self.context.openExecutor(sprocId,clientKey,clientMode,
                                          openMode,arguments,condition, procId)

                resp = MsgHelper.createResponse(CtxMessages.RSP_OPEN_EXEC, msg)
                resp = self.context.buildExecutorInfo(sprocId,resp)
            
            except Empty,ex:
                txt = "Failed executor start"
                reason = "Did not receive startup confirmation for '" + sprocId + "'"
                resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)
            except ProcError,ex:
                txt = "Unable to open executor for '" + sprocId + "'"
                reason = repr(ex)
                traceback.print_exc()
                resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)
            except server.context.context.ContextError,ex:
                traceback.print_exc()
                resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, ex.message, ex.reason)
            except BaseException,ex:
                txt = "Unable to open executor for '" + sprocId + "'"
                reason = repr(ex)
                traceback.print_exc()
                resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)
        
        return resp

    #===========================================================================
    def request_InstanceId(self,msg):
        clientKey = int(msg.getKey())
        procId = msg[ExcMessages.FIELD_PROC_ID]

        instanceId = self.context.getInstanceId( procId )

        resp = MsgHelper.createResponse(CtxMessages.RSP_INSTANCE_ID, msg)
        resp[CtxMessages.FIELD_INSTANCE_ID] = instanceId
        return resp

