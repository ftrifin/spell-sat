###############################################################################

"""
PACKAGE 
    server.listener.guioperations 
FILE
    guioperations.py
    
DESCRIPTION
    Implementation of all GUI requests
    
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
import server.core.messages.listener
from server.core.messages.base import *
from server.core.messages.msghelper import *
from server.core.ipc.xmlmsg import *
from spell.config.reader import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import sys

################################################################################

# Shorcut to context messages
Messages = server.core.messages.listener

################################################################################
class GUIOperations(object):
    
    listener = None
    
    #===========================================================================
    def __init__(self, lst):
        self.listener = lst
        
    #===========================================================================
    def processRequest(self, msg):
        guiKey = msg.getKey()
        #LOG("Received GUI request: " + msg.getId() + " from " + guiKey, level = LOG_COMM)
        response = None
        #-----------------------------------------------------------------------
        # Request the list of available contexts
        #-----------------------------------------------------------------------
        if msg.getId() == Messages.REQ_CTX_LIST:
            response = self.request_ContextList(msg)
        #-----------------------------------------------------------------------
        # Request opening a context
        #-----------------------------------------------------------------------
        elif msg.getId() == Messages.REQ_OPEN_CTX:
            response = self.request_OpenContext(msg)
        #-----------------------------------------------------------------------
        # Request closing a context
        #-----------------------------------------------------------------------
        elif msg.getId() == Messages.REQ_CLOSE_CTX:
            response = self.request_CloseContext(msg)
        #-----------------------------------------------------------------------
        # Request attaching to a context
        #-----------------------------------------------------------------------
        elif msg.getId() == Messages.REQ_ATTACH_CTX:
            response = self.request_AttachContext(msg)
        #-----------------------------------------------------------------------
        # Request destroying a context
        #-----------------------------------------------------------------------
        elif msg.getId() == Messages.REQ_DESTROY_CTX:
            response = self.request_DestroyContext(msg)
        #-----------------------------------------------------------------------
        # Request context information
        #-----------------------------------------------------------------------
        elif msg.getId() == Messages.REQ_CTX_INFO:
            response = self.request_ContextInfo(msg)
        #-----------------------------------------------------------------------
        # Unknown request id
        #-----------------------------------------------------------------------
        else:
            response = self.request_Error(msg)

        response.setSender("LST")
        response.setReceiver("CLT")
        return response

    #===========================================================================        
    def request_ContextList(self, msg):
        list = ','.join(self.listener.getContextList())
        resp = MsgHelper.createResponse(Messages.RSP_CTX_LIST, msg)
        resp[Messages.FIELD_CTX_LIST] = list
        return resp
    
    #===========================================================================        
    def request_OpenContext(self, msg):
        ctxName = msg[Messages.FIELD_CTX_NAME]
        clientKey = int(msg.getKey())
        resp = None
        if self.listener.isContextRunning(ctxName):
            txt = "Context '" + ctxName + "' already open" 
            resp = MsgHelper.createError(Messages.REQ_OPEN_CTX, msg, txt)
        else:
            ctxPort = self.listener.openContext(ctxName,clientKey)
            if ctxPort:
                resp= MsgHelper.createResponse(Messages.RSP_OPEN_CTX, msg)
                resp[Messages.FIELD_CTX_PORT] = ctxPort
            else:
                txt = "Unable to open context '" + ctxName + "'"
                reason = "Process failed startup"
                LOG(txt + ": " + reason, LOG_ERROR)
                resp = MsgHelper.createError(Messages.REQ_OPEN_CTX, msg, txt, reason)
        return resp
        
    #===========================================================================        
    def request_CloseContext(self, msg):
        clientKey = int(msg.getKey())
        ctxName = msg[Messages.FIELD_CTX_NAME]
        if not self.listener.isContextRunning(ctxName):
            txt = "Context '" + ctxName + "' is not open" 
            resp = MsgHelper.createError(Messages.REQ_CLOSE_CTX, msg, txt)
        elif not self.listener.isContextBusy(ctxName):
            txt = "Context '" + ctxName + "' is busy, cannot be closed now.\n\n" +\
                  "(There are clients connected and/or active procedures)" 
            resp = MsgHelper.createError(Messages.REQ_CLOSE_CTX, msg, txt)
        else:
            if self.listener.closeContext(ctxName,clientKey):
                resp= MsgHelper.createResponse(Messages.RSP_CLOSE_CTX, msg)
            else:
                txt = "Unable to close context '" + ctxName + "'"
                reason = "Process is not responding"
                LOG(txt + ": " + reason, LOG_ERROR)
                resp = MsgHelper.createError(Messages.REQ_CLOSE_CTX, msg, txt, reason)
        return resp

    #===========================================================================        
    def request_DestroyContext(self, msg):
        clientKey = int(msg.getKey())
        ctxName = msg[Messages.FIELD_CTX_NAME]
        if not self.listener.isContextRunning(ctxName):
            txt = "Context '" + ctxName + "' is not open" 
            resp = MsgHelper.createError(Messages.REQ_DESTROY_CTX, msg, txt)
        else:
            self.listener.killContext(ctxName)
            resp = MsgHelper.createResponse(Messages.RSP_DESTROY_CTX, msg)
        return resp
        
    #===========================================================================        
    def request_AttachContext(self, msg):
        ctxName = msg[Messages.FIELD_CTX_NAME]
        try:
            LOG("Checking context status")
            if not self.listener.isContextRunning(ctxName):
                txt = "Context '" + ctxName + "' is not open, cannot attach" 
                resp = MsgHelper.createError(Messages.REQ_OPEN_CTX, msg, txt)
            else:
                LOG("Retrieving context info")
                ctxInfo = Config.instance().getContextConfig(ctxName)
                driverName = ctxInfo.getDriver()
                drvInfo = Config.instance().getDriverConfig(driverName)
                ctxPort = self.listener.getContextPort(ctxName) 
                resp = MsgHelper.createResponse(Messages.RSP_ATTACH_CTX, msg)
                resp[Messages.FIELD_CTX_PORT]   = ctxPort
                resp[Messages.FIELD_CTX_NAME]   = ctxName
                resp[Messages.FIELD_CTX_SC]     = ctxInfo.getSC()
                resp[Messages.FIELD_CTX_STATUS] = self.listener.getContextStatus(ctxName)
                resp[Messages.FIELD_CTX_DRV]    = driverName
                resp[Messages.FIELD_CTX_FAM]    = ctxInfo.getFamily()
                resp[Messages.FIELD_CTX_GCS]    = ctxInfo.getGCS()        
                resp[Messages.FIELD_CTX_DESC]   = ctxInfo.getDescription()        
                resp[Messages.FIELD_CTX_MAXPROC] = drvInfo.getMaxProcs()
        except BaseException,ex:
            txt = "Unable to attach to context '" + ctxName + "'"
            reason = repr(ex)
            resp = MsgHelper.createError(Messages.REQ_ATTACH_CTX, msg, txt, reason)
        return resp

    #===========================================================================        
    def request_ContextInfo(self, msg):
        ctxName = msg[Messages.FIELD_CTX_NAME]
        try:
            LOG("Getting context information: " + str(ctxName))
            ctxInfo = Config.instance().getContextConfig(ctxName)
            driverName = ctxInfo.getDriver()
            drvInfo = Config.instance().getDriverConfig(driverName)
            resp = MsgHelper.createResponse(Messages.RSP_CTX_INFO, msg)
            resp[Messages.FIELD_CTX_NAME] = ctxName
            resp[Messages.FIELD_CTX_SC] = ctxInfo.getSC()
            resp[Messages.FIELD_CTX_STATUS] = self.listener.getContextStatus(ctxName)
            resp[Messages.FIELD_CTX_DRV] = driverName
            resp[Messages.FIELD_CTX_FAM] = ctxInfo.getFamily()
            resp[Messages.FIELD_CTX_GCS] = ctxInfo.getGCS()        
            resp[Messages.FIELD_CTX_DESC] = ctxInfo.getDescription()        
            resp[Messages.FIELD_CTX_MAXPROC] = drvInfo.getMaxProcs()
            resp[Messages.FIELD_CTX_PORT] = self.listener.getContextPort(ctxName)
        except BaseException,ex:
            txt = "Unable to get information for context '" + ctxName + "'"
            reason = repr(ex) 
            resp = MsgHelper.createError(Messages.REQ_CTX_INFO, msg, txt, reason)
        return resp

    #===========================================================================                
    def request_Error(self,msg):
        error = "Cannot process request"
        reason = "Unknown message id: " + repr(msg.getId())
        resp = MsgHelper.createError(MSG_ID_UNKNOWN, msg, error, reason)
        return resp
        