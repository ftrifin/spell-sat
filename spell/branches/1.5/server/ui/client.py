################################################################################

"""
PACKAGE 
    server.ui.client
FILE
    client.py
    
DESCRIPTION
    Interface to SPEL clients
    
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
from spell.utils.log import *

# Messaging and comm
from server.core.messages.base import *
from server.core.messages.msghelper import MsgHelper
from server.core.ipc.xmlmsg import *
from server.core.ipc.interfaceclient import IPCinterfaceClient
import server.core.messages.executor
import server.executor.status
from spell.lib.adapter.constants.notification import *

from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.core import *
from spell.lib.registry import *
from spell.lib.adapter.config import Configurable
from spell.lib.exception import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from clientbase import ClientInterfaceBase
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import sys,threading,traceback,time

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************
ExcMessages = server.core.messages.executor

__all__ = ['ClientIF']

################################################################################
class CommandShell( threading.Thread ):
    
    #==========================================================================
    def __init__(self):
        threading.Thread.__init__(self)
    
    #==========================================================================
    def run(self):
        while True:
            #print "[  R:Run  C:Run Over  P:Pause  S:Step  O:Step over  K:Skip  G:Goto  A:Abort  E:Script]\n"
            try:
                opt = raw_input()
                opt = opt.strip("\n\r").upper()
                if   opt == "P": 
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_PAUSE)
                elif opt == "R": 
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_RUN)
                    cmd[ExcMessages.FIELD_SO] = False
                elif opt == "C": 
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_RUN)
                    cmd[ExcMessages.FIELD_SO] = True
                elif opt == "S": 
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_STEP)
                elif opt == "O": 
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_STEP_OVER)
                elif opt == "K": 
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_SKIP)
                elif opt == "A": 
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_ABORT)
                elif opt == "L": 
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_RELOAD)
                elif opt == "G":
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_GOTO)
                    try:
                        line = raw_input("line:")
                        cmd[ExcMessages.FIELD_GOTO_LINE] = line
                    except:
                        raise BaseException()
                elif opt == "E":
                    cmd = MsgHelper.createMessage(ExcMessages.CMD_SCRIPT)
                    try:
                        sc = raw_input("script: ")
                        cmd[ExcMessages.FIELD_SCRIPT] = sc
                    except:
                        raise BaseException()
                else:
                    if REGISTRY['EXEC'].getStatus() in \
                        [server.executor.status.FINISHED,server.executor.status.ABORTED]:
                        print "CLEANUP"
                        REGISTRY['EXEC'].cleanup()
                        return
                    print "Unknown command:",opt
                    cmd = None
                if cmd:
                    REGISTRY['EXEC'].processMessage( cmd )
            except SystemExit:
                return
            except:
                traceback.print_exc()
                REGISTRY['EXEC'].cleanup()
                return

################################################################################
class ClientInterface(ClientInterfaceBase,Configurable):

    """
    DESCRIPTION:
        TODO description
    """
    ready       = False
    executor    = None
    useContext  = True
    contextPort = None
    # IPC context interface
    __ctxIFC = None
        
    #==========================================================================
    def __init__(self):
        ClientInterfaceBase.__init__(self)
        self.executor = None
        self.useContext = True
        self.ready = False
        self.__ctxIFC = IPCinterfaceClient("EXC-CTX")
            
    #==========================================================================
    def setup(self, executor, initialStatus, ctxName = None, contextPort = None ):
        self.executor = executor
        self.useContext = (ctxName != None)
        self.contextPort = contextPort
        failMode = (initialStatus == server.executor.status.ERROR)
        LOG("Setting up UI remote client interface (fail mode = " + repr(failMode) + ", useContext=" + repr(self.useContext) + ")")
        ClientInterfaceBase.setup(self, ctxName, failMode)

        if self.useContext:
            LOG("Setting up controller IO channel")
            self.__ctxIFC.connect("localhost", self.contextPort, self.executor.processMessage, self.executor.processRequest, self.executor.connectionLost )
        
        csp = executor.procId
        if "#" in csp:
            idx = csp.find("#")
            csp = csp[0:idx]
        
        response = None
        if self.useContext:                
            LOG("Send login message to context")
            msg = MessageClass()
            msg.setType(MSG_TYPE_REQUEST)
            msg.setId(ExcMessages.REQ_NOTIF_EXEC_OPEN)
            msg.setSender(executor.procId)
            msg.setReceiver("CTX")
            msg[ExcMessages.FIELD_PROC_ID] = executor.procId
            msg[ExcMessages.FIELD_CSP] = csp
            msg[ExcMessages.FIELD_EXEC_STATUS] = initialStatus
            msg[ExcMessages.FIELD_EXEC_PORT] = 0
            msg[ExcMessages.FIELD_ASRUN_NAME] = self._getAsRun()
            msg[ExcMessages.FIELD_LOG_NAME] = LOG.getLogFile().name
            if failMode:
                msg[FIELD_ERROR] = self.executor.errorMessage
                msg[FIELD_REASON] = self.executor.errorReason
            response = self.__ctxIFC.sendRequest(msg)
            LOG("Logged in context")
        else:
            LOG("Creating command line interface")
            cmdline = CommandShell()
            cmdline.start()

        self.ready = True
        return response
            
    #==========================================================================
    def cleanup(self, force = False):
        if not self.ready: return
        self.ready = False
        if self.useContext:
            if not force:
                LOG("Send logout message to context")
                msg = MessageClass()
                msg.setType(MSG_TYPE_COMMAND)
                msg.setId(ExcMessages.MSG_NOTIF_EXEC_CLOSE)
                msg.setSender(self.executor.procId)
                msg.setReceiver("CTX")
                msg[ExcMessages.FIELD_PROC_ID] = self.executor.procId
                msg[ExcMessages.FIELD_EXEC_PORT] = 0
                self.__ctxIFC.sendMessage(msg)
            LOG("Disconnecting context channel")
            self.__ctxIFC.disconnect()
        ClientInterfaceBase.cleanup(self)

    #===========================================================================
    def _sendNotificationMessage(self, msg):
        if not self.ready and self.useContext: return
        msg[FIELD_TIME] = str(time.time())
        if self.useContext:
            msg.setSender(self.executor.procId)
            msg.setReceiver("GUI")
            responseMsg = self.__ctxIFC.sendRequest(msg)
        else:
            self._notificationCmdline(msg)
            responseMsg = msg
            responseMsg.setType(MSG_TYPE_RESPONSE)
        return responseMsg

    #===========================================================================
    def _sendWriteMessage(self, msg ):
        if not self.ready and self.useContext: return
        msg[FIELD_TIME] = str(time.time())
        if self.useContext:
            msg.setSender(self.executor.procId)
            msg.setReceiver("GUI")
            self.__ctxIFC.sendMessage(msg)
        else:
            self._writeCmdLine(msg)

    #===========================================================================
    def _sendPromptMessage(self, msg, timeout = 150000 ):
        if not self.ready and self.useContext: return
        msg[FIELD_TIME] = str(time.time())
        toProcess = None
        if self.useContext:            
            msg.setSender(self.executor.procId)
            msg.setReceiver("GUI")
            responseMsg = self.__ctxIFC.sendRequest(msg, timeout)
            if responseMsg.getId() == MSG_ID_CANCEL:
                self.write("Prompt cancelled", {Severity:WARNING})
                REGISTRY['EXEC'].abort()
                toProcess = "<CANCEL>"
            elif responseMsg.getId() == MSG_ID_TIMEOUT:
                self.write("Prompt timed out", {Severity:ERROR})
                REGISTRY['EXEC'].abort()
                toProcess = "<TIMEOUT>"
            elif responseMsg.getType() == MSG_TYPE_ERROR:
                sys.stderr.write("\n\n" + str(responseMsg) + "\n\n")
                self.write("Prompt error: " + str(responseMsg[FIELD_ERROR]), {Severity:WARNING})
                REGISTRY['EXEC'].abort()
                toProcess = "<ERROR>"
            else:
                toProcess = responseMsg['ReturnValue']
        else:
            toProcess = self._promptCmdLine(msg)
        return toProcess

    #===========================================================================
    def _sendMessage(self, msg):
        if not self.ready: return
        msg.setSender(self.executor.procId)
        msg.setReceiver("CTX")
        msg[ExcMessages.FIELD_PROC_ID] = self.executor.procId
        self.__ctxIFC.sendMessage(msg)
    
    #===========================================================================
    def _sendRequest(self, msg):
        if not self.ready: return
        msg.setSender(self.executor.procId)
        msg.setReceiver("CTX")
        msg[ExcMessages.FIELD_PROC_ID] = self.executor.procId
        responseMsg = self.__ctxIFC.sendRequest(msg)
        return responseMsg
    
    #===========================================================================
    def _getProcId(self):
        return self.executor.procId

    #===========================================================================
    def _getCSP(self):
        return self.executor.getStackPosition()
    
    #===========================================================================
    def _getTimeID(self):
        return self.executor.timeID

    
################################################################################
ClientIF = ClientInterface()

