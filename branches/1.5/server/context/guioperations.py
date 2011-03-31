###############################################################################

"""
PACKAGE 
    server.context.guioperations 
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
import server.core.messages.context
import server.core.messages.executor
from server.core.messages.base import *
from server.core.ipc.xmlmsg import *
from server.core.messages.msghelper import *
from spell.lang.modifiers import *
from server.procedures.manager import *
from server.executor.status import *
from server.context.clientinfo import CLIENT_MODE_CONTROL
from server.core.ipc.bigdata import DataChunker
import server.context.context 

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import traceback,sys,os,thread
from Queue import Empty

################################################################################

# Shorcut to context messages
CtxMessages = server.core.messages.context
ExcMessages = server.core.messages.executor

################################################################################
class GUIOperations(object):
    
    context = None
    
    # Protection against big data mechanism
    __dataChunker = None
    
    #===========================================================================
    def __init__(self, ctx):
        self.context = ctx
        self.__dataChunker = DataChunker()
        
    #===========================================================================
    def processMessage(self, msg):    
        guiKey = int(msg.getKey())
        LOG("Received GUI message: " + msg.getId() + " from client " + str(guiKey), level = LOG_COMM)
        procId = msg[ExcMessages.FIELD_PROC_ID]
        if msg.getType() == MSG_TYPE_COMMAND:
            if msg.getId() == CtxMessages.MSG_GUI_LOGIN:
                host = msg[FIELD_HOST]
                mode = CtxMessages.DATA_GUI_MODE_C 
                LOG("GUI login: " + str(guiKey) + " from host " + str(host), level = LOG_PROC)
                self.context.createClient(guiKey,host,mode)
                self.context.notifyClientOperation(guiKey, mode, host, CtxMessages.DATA_CLOP_LOGIN)
                return True
            elif msg.getId() == CtxMessages.MSG_GUI_LOGOUT:
                host = msg[FIELD_HOST]
                LOG("GUI logout: " + str(guiKey) + " from host " + str(host), level = LOG_PROC)
                self.context.removeClient(guiKey)
                self.context.notifyClientOperation(guiKey, "", host, CtxMessages.DATA_CLOP_LOGOUT)
                return True
            elif msg.getId() == CtxMessages.MSG_CLOSE_CTX:
                LOG("Closing this context", level = LOG_PROC)
                self.context.stop()
                return True
        return False
    
    #===========================================================================
    def processRequest(self, msg):
        guiKey = int(msg.getKey())
        #LOG("Received GUI request: " + msg.getId() + " from client " + str(guiKey), level = LOG_COMM)
        response = None 
        #-----------------------------------------------------------------------
        # Request executor list        
        #-----------------------------------------------------------------------
        if msg.getId() == CtxMessages.REQ_EXEC_LIST:
            response = self.request_ExecutorList(msg)
        #-----------------------------------------------------------------------
        # Request opening an executor        
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_OPEN_EXEC:
            response = self.request_OpenExecutor(msg)
        #-----------------------------------------------------------------------
        # Request closing an executor         
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_CLOSE_EXEC:
            response = self.request_CloseExecutor(msg)
        #-----------------------------------------------------------------------
        # Request killing an executor         
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_KILL_EXEC:
            response = self.request_KillExecutor(msg)
        #-----------------------------------------------------------------------
        # Request attaching to an executor         
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_ATTACH_EXEC:
            response = self.request_AttachExecutor(msg)
        #-----------------------------------------------------------------------
        # Request detaching from an executor         
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_DETACH_EXEC:
            response = self.request_DetachExecutor(msg)
        #-----------------------------------------------------------------------
        # Request executor information
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_EXEC_INFO:
            response = self.request_ExecutorInfo(msg)
        #-----------------------------------------------------------------------
        # Request client information
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_CLIENT_INFO:
            response = self.request_ClientInfo(msg)
        #-----------------------------------------------------------------------
        # Request procedure list
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_PROC_LIST:
            response = self.request_ProcedureList(msg)
        #-----------------------------------------------------------------------
        # Request procedure properties
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_PROC_PROP:
            response = self.request_ProcedureProperties(msg)        
        #-----------------------------------------------------------------------
        # Request procedure code
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_PROC_CODE:
            response = self.request_ProcedureCode(msg)        
        #-----------------------------------------------------------------------
        # Request server file
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_SERVER_FILE:
            response = self.request_ServerFile(msg)        
        #-----------------------------------------------------------------------
        # Request available instance id
        #-----------------------------------------------------------------------
        elif msg.getId() == CtxMessages.REQ_INSTANCE_ID:
            response = self.request_InstanceId(msg)        
        return response
        
    #===========================================================================                
    def request_ExecutorList(self,msg):
        LOG("Requested list of executors", level = LOG_PROC)
        list = self.context.getExecutors()
        exList = ""
        if list is not None:
            for ex in list:
                # Add the procedure id to the executor list, comma separated
                if len(exList)>0: exList = exList + ","
                exList = exList + str(ex)
        resp = MsgHelper.createResponse(CtxMessages.RSP_EXEC_LIST, msg)
        resp[CtxMessages.FIELD_EXEC_LIST] = exList
        return resp

    #===========================================================================                
    def request_InstanceId(self,msg):
        clientKey = int(msg.getKey())
        procId = msg[ExcMessages.FIELD_PROC_ID]
        
        instanceId = self.context.getInstanceId( procId )
        
        resp = MsgHelper.createResponse(CtxMessages.RSP_INSTANCE_ID, msg)
        resp[CtxMessages.FIELD_INSTANCE_ID] = instanceId
        return resp

    #===========================================================================                
    def request_OpenExecutor(self, msg):
        clientKey = int(msg.getKey())
        procId = msg[ExcMessages.FIELD_PROC_ID]
        if procId is None:
            txt = "Incomplete request"
            reason = "No executor name given"
            resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)
        else:
            # Process executor arguments, if any
            arguments = {}
            argStr = msg[ExcMessages.FIELD_ARGS]
            if argStr is not None:
                args = argStr.split(ARG_SEP)
                for arg in args:
                    var,value = arg.split("=")
                    arguments[var] = value
    
            # Obtain executor condition
            condition = msg[ExcMessages.FIELD_CONDITION]
    
            # Obtain executor mode
            clientMode = msg[CtxMessages.FIELD_GUI_MODE]
        
            # Obtain executor background flag
            openStr = msg[CtxMessages.FIELD_OPEN_MODE]
            if openStr is None:
                openMode = {}
            else:
                openMode = eval(openStr)
            
            try:
                
                self.context.openExecutor(procId,clientKey,clientMode,openMode,arguments,condition)
                
                resp = MsgHelper.createResponse(CtxMessages.RSP_OPEN_EXEC, msg)
                resp = self.context.buildExecutorInfo(procId,resp)
            
            except Empty,ex:
                txt = "Failed executor start"
                reason = "Did not receive startup confirmation for '" + procId + "'"
                traceback.print_exc()
                resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)
            except ProcError,ex:
                txt = "Unable to open executor for '" + procId + "'"
                reason = repr(ex)
                traceback.print_exc()
                resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)
            except server.context.context.ContextError,ex:
                traceback.print_exc()
                resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, ex.message, ex.reason)
            except BaseException,ex:
                txt = "Unable to open executor for '" + procId + "'"
                reason = repr(ex)
                traceback.print_exc()
                resp = MsgHelper.createError(CtxMessages.REQ_OPEN_EXEC, msg, txt, reason)

        return resp

    #===========================================================================                
    def request_CloseExecutor(self, msg):
        procId = msg[ExcMessages.FIELD_PROC_ID]
        LOG("Requested closing executor " + repr(procId), level = LOG_PROC)
        try:
            
            self.context.closeExecutor(procId)
    
            resp= MsgHelper.createResponse(CtxMessages.RSP_CLOSE_EXEC, msg)

        except server.context.context.ContextError,ex:
            traceback.print_exc()
            resp = MsgHelper.createError(CtxMessages.REQ_CLOSE_EXEC, msg, ex.message, ex.reason)
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to close executor for '" + procId + "'"
            reason = repr(ex)
            traceback.print_exc()
            resp = MsgHelper.createError(CtxMessages.REQ_CLOSE_EXEC, msg, txt, reason)
        
        return resp

    #===========================================================================                
    def request_KillExecutor(self, msg):
        procId = msg[ExcMessages.FIELD_PROC_ID]
        LOG("Requested killing executor " + repr(procId), level = LOG_PROC)
        try:
            if self.context.killExecutor(procId):
                resp= MsgHelper.createResponse(CtxMessages.RSP_KILL_EXEC, msg)
            else:
                executor = self.context.getExecutor(procId)
                error,reason = self.context.getExecutor(procId).getError()
                txt = "Could not kill executor: " + error
                resp = MsgHelper.createError(CtxMessages.REQ_KILL_EXEC, msg, txt, reason)
            self.context.clearExecutor(procId)
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to close executor for '" + procId + "'"
            reason = ex.message
            resp = MsgHelper.createError(CtxMessages.REQ_KILL_EXEC, msg, txt, reason)
        finally:
            return resp

    #===========================================================================                
    def request_AttachExecutor(self, msg):
        procId = msg[ExcMessages.FIELD_PROC_ID]
        mode = msg[CtxMessages.FIELD_GUI_MODE]
        clientKey = int(msg.getKey())
        try:
            LOG("Client " + repr(clientKey) + " requested attaching executor " + repr(procId) + " in mode: "+ repr(mode))
            self.context.clientAttachExecutor(procId, clientKey, mode, False)
            resp = MsgHelper.createResponse(CtxMessages.RSP_ATTACH_EXEC, msg)
            executor = self.context.getExecutor(procId)
            resp = self.context.buildExecutorInfo(procId,resp)
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to attach to executor '" + procId + "'"
            reason = ex.message
            resp = MsgHelper.createError(CtxMessages.REQ_ATTACH_EXEC, msg, txt, reason)
        finally: 
            return resp

    #===========================================================================                
    def request_DetachExecutor(self, msg):
        procId = msg[ExcMessages.FIELD_PROC_ID]
        clientKey = int(msg.getKey())
        try:
            LOG("Client " + repr(clientKey) + " requested detaching executor "+ repr(procId))
            self.context.clientDetachExecutor(procId, clientKey)
            resp = MsgHelper.createResponse(CtxMessages.RSP_DETACH_EXEC, msg)
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to detach from executor '" + procId + "'"
            reason = ex.message
            resp = MsgHelper.createError(CtxMessages.REQ_DETACH_EXEC, msg, txt, reason)
        finally: 
            return resp
        
    #===========================================================================                
    def request_ExecutorInfo(self,msg):
        procId = msg[ExcMessages.FIELD_PROC_ID]
        try:
            resp = MsgHelper.createResponse(CtxMessages.RSP_EXEC_INFO, msg)
            resp = self.context.buildExecutorInfo(procId,resp)
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to get information of executor " + repr(procId)
            reason = ex.message
            resp = MsgHelper.createError(CtxMessages.REQ_EXEC_INFO, msg, txt, reason)
        finally:
            return resp

    #===========================================================================
    def request_ServerFile(self, msg):
        procId = msg[ExcMessages.FIELD_PROC_ID]
        serverFileId = msg[ExcMessages.FIELD_SERVER_FILE_ID]
        try:
            resp = MsgHelper.createResponse(CtxMessages.RSP_SERVER_FILE, msg)
            
            # If the request contains chunk info
            chunkNo = msg[CtxMessages.FIELD_CHUNK] 
            if chunkNo:
                chunkNo = int(chunkNo)
                # Get the next data chunk
                data = self.__dataChunker.getChunk(procId,chunkNo)
            else:
                data = None

            # Get the chunk
            if data is None:
                LOG("Obtaining proc file for: " + repr(procId) + ": " + repr(serverFileId), level = LOG_PROC)
                lines = self.context.getFileData(procId,serverFileId)
                totalChunks = self.__dataChunker.startChunks( procId, lines )
                if totalChunks == 0: # No need to split in chunks
                    resp[CtxMessages.FIELD_CHUNK] = 0
                    resp[CtxMessages.FIELD_TOTAL_CHUNKS] = 0
                    data = lines
                else:
                    # Put the first chunk
                    resp[CtxMessages.FIELD_CHUNK] = 0
                    resp[CtxMessages.FIELD_TOTAL_CHUNKS] = totalChunks
                    data = self.__dataChunker.getChunk(procId,0)
            else:
                # Current chunk
                totalChunks = self.__dataChunker.getSize(procId)
                resp[CtxMessages.FIELD_CHUNK] = chunkNo
                resp[CtxMessages.FIELD_TOTAL_CHUNKS] = totalChunks
                
                if chunkNo == totalChunks-1: self.__dataChunker.endChunks(procId)
            
            code = self.__linesToString(data)
            resp[CtxMessages.FIELD_PROC_ID] = procId
            resp[CtxMessages.FIELD_SERVER_FILE] = code
            
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to get " + serverFileId + " file for " + repr(procId)
            reason = ex.message
            resp = MsgHelper.createError(CtxMessages.REQ_SERVER_FILE, msg, txt, reason)
        finally:
            return resp

    #===========================================================================                
    def request_ClientInfo(self,msg):
        clientKey = int(msg[CtxMessages.FIELD_GUI_KEY])
        LOG("Request info for client " + repr(clientKey))
        try:
            resp = MsgHelper.createResponse(CtxMessages.RSP_CLIENT_INFO, msg)
            
            info = self.context.getClient(clientKey)
            
            if info is None:
                txt = "No such client: " + repr(clientKey)
                reason = " "
                resp[CtxMessages.FIELD_GUI_MODE] = "UNKNOWN"
                resp[FIELD_HOST] = "UNKNOWN"
                resp[CtxMessages.FIELD_GUI_KEY] = clientKey
            else:
                resp[CtxMessages.FIELD_GUI_MODE] = info.getMode()
                resp[FIELD_HOST] = info.getHost()
                resp[CtxMessages.FIELD_GUI_KEY] = clientKey
            
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to get information of client " + repr(clientKey)
            reason = ex.message
            resp = MsgHelper.createError(CtxMessages.REQ_CLIENT_INFO, msg, txt, reason)
        finally:
            return resp
        
    #===========================================================================                
    def request_ProcedureList(self,msg):
        try:
            resp = MsgHelper.createResponse(CtxMessages.RSP_PROC_LIST, msg)
            list = ",".join(ProcedureManager.instance().getProcList())
            resp[CtxMessages.FIELD_PROC_LIST] = list
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to get procedure list"
            reason = ex.message
            resp = MsgHelper.createError(CtxMessages.REQ_PROC_LIST, msg, txt, reason)
        finally:
            return resp
        
    #===========================================================================                
    def request_ProcedureProperties(self, msg):
        try:
            resp = MsgHelper.createResponse(CtxMessages.RSP_PROC_PROP, msg)
            procId = msg[ExcMessages.FIELD_PROC_ID]
            LOG("Obtaining proc properties: " + repr(procId), level = LOG_PROC)
            properties = ProcedureManager.instance().getProcedure(procId).properties()
            for key in properties.keys():
                resp[key] = properties.get(key)
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to get procedure properties for " + repr(procId)
            reason = ex.message
            resp = MsgHelper.createError(CtxMessages.REQ_PROC_LIST, msg, txt, reason)
        finally:
            return resp

    #===========================================================================                
    def request_ProcedureCode(self, msg):
        try:        
            resp = MsgHelper.createResponse(CtxMessages.RSP_PROC_CODE, msg)
            procId = msg[CtxMessages.FIELD_PROC_ID]

            # If the request contains chunk info
            chunkNo = msg[CtxMessages.FIELD_CHUNK] 
            if chunkNo:
                chunkNo = int(chunkNo)
                # Get the next data chunk
                data = self.__dataChunker.getChunk(procId,chunkNo)
            else:
                data = None

            # Get the chunk
            if data is None:
                LOG("Obtaining proc code: " + repr(procId), level = LOG_PROC)
                lines = ProcedureManager.instance().getSource(procId)
                totalChunks = self.__dataChunker.startChunks( procId, lines )
                if totalChunks == 0: # No need to split in chunks
                    resp[CtxMessages.FIELD_CHUNK] = 0
                    resp[CtxMessages.FIELD_TOTAL_CHUNKS] = 0
                    data = lines
                else:
                    # Put the first chunk
                    resp[CtxMessages.FIELD_CHUNK] = 0
                    resp[CtxMessages.FIELD_TOTAL_CHUNKS] = totalChunks
                    data = self.__dataChunker.getChunk(procId,0)
            else:
                # Current chunk
                totalChunks = self.__dataChunker.getSize(procId)
                resp[CtxMessages.FIELD_CHUNK] = chunkNo
                resp[CtxMessages.FIELD_TOTAL_CHUNKS] = totalChunks
                
                if chunkNo == totalChunks-1: self.__dataChunker.endChunks(procId)
                
            code = self.__codeToString(data)
            resp[CtxMessages.FIELD_PROC_ID] = procId
            resp[CtxMessages.FIELD_PROC_CODE] = code
            
        except BaseException,ex:
            traceback.print_exc()
            txt = "Unable to get procedure code for " + repr(procId) 
            reason = ex.message
            if reason is None or len(reason)==0:
                reason = repr(ex)
            resp = MsgHelper.createError(CtxMessages.REQ_PROC_CODE, msg, txt, reason)
        finally:
            return resp

    #===========================================================================                
    def __codeToString(self, lines):
        code = ""
        for line in lines:
            sep = CODE_SEPARATOR
            add = ""
            if len(code)==0: sep = ""
            if len(line)==0: add = " "
            code = code + sep + line + add
        return code

    #===========================================================================                
    def __linesToString(self, lines):
        code = ""
        for line in lines:
            add = ""
            if len(line)==0: add = " "
            code = code + line + add
        return code

