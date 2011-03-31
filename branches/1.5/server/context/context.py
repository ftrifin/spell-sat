###############################################################################

"""
PACKAGE 
    server.context.context 
FILE
    context.py
    
DESCRIPTION
    Class implementation of SPEL Context and Context process main function
    
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
from spell.config.reader import *
from spell.config.constants import CONTEXT,COMMON
from spell.lib.exception import CoreException
from server.core.ipc.interfaceserver import IPCinterfaceServer
from server.core.ipc.interfaceclient import IPCinterfaceClient
from server.core.process.manager import *
from server.procedures.manager import ProcedureManager
from spell.lang.modifiers import *
from spell.utils.log import *
from server.core.messages.msghelper import *
from server.core.registry.processRegistry import ProcessRegistry
import server.core.messages.listener
import server.core.messages.executor
import server.core.messages.context
import server.executor.status

#*******************************************************************************
# Local imports
#*******************************************************************************
from guioperations import GUIOperations
from lstoperations import LSTOperations
from execoperations import EXCOperations
from clientinfo import *
from exmanager import ExecutorManagerClass

#*******************************************************************************
# System imports
#*******************************************************************************
import getopt,sys,os,traceback,thread
import Queue

################################################################################

# Visibility for imports
__all__ = [ 'ContextClass' ]

# Shortcut to messages
LstMessages = server.core.messages.listener
ExcMessages = server.core.messages.executor
CtxMessages = server.core.messages.context


#*******************************************************************************
# Exceptions
#*******************************************************************************
class ContextError(CoreException): pass

################################################################################
class ContextClass(object):
    
    # Holds the context name
    ctxName = None
    
    # Holds the listening port for GUIs
    port = None
    # Holds the listener port
    listenerPort = None
    
    # Holds the set of instance numbers for each executor id
    __executorInstanceNumbers = {}
    # Holds the set of executor managers
    __executorManagers = {}
    # Holds the set of clients
    __clientInfo = {}
    
    # GUI operations delegate
    GUI_Operations = None
    
    # Listener operations delegate
    LST_Operations = None

    # Executor operations delegate
    EXC_Operations = None
    
    # IPC interface for clients
    __guiIFC = None
    
    # IPC interface for listener
    __lstIFC = None
    
    # Lock for clients
    __clientLock = None
    
    # Lock for executors
    __execLock = None
    # Lock for context process closure
    __closeLock = None
    
    #Process registry for registering opened executors in a file
    __executorsRegistry = None
    
    #===========================================================================
    def __init__(self, ctxName, listenerPort, warmStart = False):
        LOG("Created context", level = LOG_INIT)
        self.ctxName = ctxName
        self.port = None
        self.__executorInstanceNumbers = {}
        self.__executorManagers = {}
        self.__clientInfo = {}
        self.GUI_Operations = GUIOperations(self)
        self.LST_Operations = LSTOperations(self)
        self.EXC_Operations = EXCOperations(self)
        self.__guiIFC = IPCinterfaceServer("CTX-GUI")
        self.__lstIFC = IPCinterfaceClient("CTX-LST")
        self.listenerPort = listenerPort
        self.__clientLock   = thread.allocate_lock()
        self.__execLock   = thread.allocate_lock()
        self.__closeLock = Queue.Queue(1)
        self.__executorsRegistry = ProcessRegistry("CTX_" + ctxName, warmStart)
        
    #===========================================================================
    def start(self):
        LOG("Loading procedures for this context", level = LOG_INIT)
        ProcedureManager.instance().setup(self.ctxName)

        LOG("Setting up GUI channel", level = LOG_INIT)
        # Setup the Gui io channel
        self.__guiIFC.connect( 888, 0, self.processGuiMessage, self.processGuiRequest, self.guiConnectionLost)
        self.__guiIFC.start()

        LOG("Connecting to listener", level = LOG_INIT)
        # Setup the Listener io channel
        self.__lstIFC.connect("localhost",self.listenerPort, self.processListenerMessage, self.processListenerRequest, self.lstConnectionLost)

        LOG("Using listening port: " + str(self.__guiIFC.port), level = LOG_INIT)
        portMsg = MsgHelper.createMessage(LstMessages.MSG_CONTEXT_OPEN)
        portMsg[LstMessages.FIELD_CTX_NAME] = self.ctxName
        portMsg[LstMessages.FIELD_CTX_PORT] = str(self.__guiIFC.port)
        portMsg.setSender("CTX")
        portMsg.setReceiver("LST")
        self.__lstIFC.sendMessage(portMsg)
        LOG("Ready", level = LOG_INIT)
        sys.stderr.write("*********************************************\n")
        sys.stderr.write("      SPEL Context (" + ctxName + ") Ready \n")
        sys.stderr.write("*********************************************\n")
        
        #for executorInfo in self.__executorRegistry.getProcesses():
        #    #Launch executors stored in warm file
        #    self.openExecutor(executorInfo.getName(), clientKey, clientMode)
        
    #===========================================================================
    def stop(self):
        # Force closing all executors
        LOG("Closing executors", level = LOG_PROC)
        self.closeAll()
        # Disconnect comm interfaces
        LOG("Disconnecting from GUI", level = LOG_PROC)
        self.__guiIFC.disconnect()

        LOG("Logout from listener", level = LOG_PROC)
        logoutMsg = MsgHelper.createMessage(LstMessages.MSG_CONTEXT_CLOSED)
        logoutMsg[LstMessages.FIELD_CTX_NAME] = self.ctxName
        logoutMsg.setSender("CTX")
        logoutMsg.setReceiver("LST")
        self.__lstIFC.sendMessage(logoutMsg)
        self.__lstIFC.disconnect()
        LOG("Disconnected", level = LOG_PROC)
        self.__closeLock.put(1)

    #===========================================================================
    def closeAll(self):
        if len(self.getExecutors())==0:
            LOG("No executors to be closed")
            return
        for procId in self.getExecutors():
            LOG("Close all: " + procId)
            executor = self.getExecutor(procId)
            if executor:
                executor.stop()
                executor.waitForClose()

    #===========================================================================
    def waitForClose(self):
        self.__closeLock.get(True)

    #===========================================================================
    def guiConnectionLost(self, clientKey):
        if clientKey in self.getClients():
            LOG("Context lost connection with client: " + repr(clientKey), LOG_ERROR)
            self.removeClient(clientKey)

    #===========================================================================
    def lstConnectionLost(self, lstKey):
        LOG("Lost connection with listener", LOG_ERROR)
        self.__lstIFC.disconnect( eoc = False )

        LOG("Notifying listener connection lost")
        
        # Build the notification message
        msg = MsgHelper.createMessage(CtxMessages.MSG_LISTENER_LOST)
        msg.setType(MSG_TYPE_ERROR)
        msg[FIELD_ERROR] = "Lost connection with listener"
        msg[FIELD_REASON] = " "
        
        clientKeys = []
        for client in self.getClients():
            clientKeys.append(int(client))
        if len(clientKeys)>0:
            self.messageToClients("CTX", clientKeys, msg)
        LOG("Done")
        return

    #===========================================================================
    def processGuiMessage(self, msg):
        if not self.GUI_Operations.processMessage(msg):
            # If the message is not processed locally, forward it to executors
            procId = msg[ExcMessages.FIELD_PROC_ID]
            # Forward message to the corresponding executor
            clientKey = int(msg.getKey())
            self.messageToExecutor(clientKey, procId, msg)
        return
        
    #===========================================================================
    def processGuiRequest(self, msg):
        LOG("Process GUI request: " + msg.getId(), level = LOG_COMM)
        resp = None
        resp = self.GUI_Operations.processRequest(msg)
        if resp is None:
            # if the request is not processed locally, forward it to executors
            procId = msg[ExcMessages.FIELD_PROC_ID]
            if not procId in self.getExecutors():
                resp = MsgHelper.createError(msg.getId(), msg, "Unable to forward request " + msg.getId(), "No such executor: " + repr(procId))
            else:
                clientKey = int(msg.getKey())
                LOG("Send GUI request to executor: " + msg.getId(), level = LOG_COMM)
                resp = self.requestToExecutor(clientKey, procId, msg)
        LOG("Process GUI request: " + msg.getId() + " finished", level = LOG_COMM)
        return resp
        
    #===========================================================================
    def processListenerMessage(self, msg):
        self.LST_Operations.processMessage(msg)

    #===========================================================================
    def processListenerRequest(self, msg):
        resp = self.LST_Operations.processRequest(msg)
        return resp

    #===========================================================================
    def processExecutorMessage(self, msg):
        self.EXC_Operations.processMessage(msg)

    #===========================================================================
    def processExecutorRequest(self, msg):
        resp = self.EXC_Operations.processRequest(msg)
        return resp

    #===========================================================================
    def messageToClients(self, procId, clientKeys, msg):
        for clientKey in clientKeys:
            LOG("Forwarding message " + msg.getId() + " from executor " + procId + " to client "+ repr(clientKey), level = LOG_COMM)
            # If the client key is a procedure id, it means the request is to
            # be sent to another procedure
            msg.setSender(procId)
            if type(clientKey)==int:
                msg.setReceiver("GUI-" + str(clientKey))
                resp = self.__guiIFC.sendMessage(msg,clientKey)
            else:
                parentProc = clientKey
                msg.setReceiver(parentProc)
                resp = self.messageToExecutor( procId, parentProc, msg)

    #===========================================================================
    def requestToClients(self, procId, clientKeys, msg):
        LOG("Forward executor " + procId + " request " + msg.getId() + " to clients", level = LOG_COMM)
        firstResp = None
        if len(clientKeys)==0:
            LOG("No clients to attend request. Discarding it", LOG_WARN, level = LOG_COMM)
            firstResp = MsgHelper.createError(msg.getId(), msg, "Cannot dispatch request", "No clients connected")
        for clientKey in clientKeys:
            msg.setSender(procId)
            # If the client key is a procedure id, it means the request is to
            # be sent to another procedure
            if type(clientKey)==int:
                msg.setReceiver("GUI-" + str(clientKey))
                resp = self.__guiIFC.forwardRequest(msg,clientKey)
            else:
                parentProc = clientKey
                msg.setReceiver(parentProc)
                resp = self.requestToExecutor( procId, parentProc, msg)
            if firstResp is None: firstResp = resp
        LOG("Request to clients '" + msg.getId() + "' finished", level = LOG_COMM)
        return firstResp

    #===========================================================================
    def messageToExecutor(self, clientKey, procId, msg):
        if procId in self.getExecutors():
            exc = self.getExecutor(procId)
            if exc: exc.messageToExecutor(clientKey,msg)

    #===========================================================================
    def requestToExecutor(self, clientKey, procId, msg):
        LOG("Forward client " + repr(clientKey) + " request " + msg.getId() + " to executor " + procId, level = LOG_COMM)
        resp = None
        if procId in self.getExecutors():
            exc = self.getExecutor(procId)
            if exc: resp = exc.requestToExecutor(clientKey,msg)
        if resp is None:
            resp = MsgHelper.createError(msg.getId(), msg, "Unable to forward request", "No such executor")
        LOG("Request to executor " + msg.getId() + " finished", level = LOG_COMM)
        return resp

    #===========================================================================
    def createExecutor(self, procId):
        self.__execLock.acquire()
        try:
            LOG("Creating executor manager for " + repr(procId))
            manager = ExecutorManagerClass(self,procId)
            self.__executorManagers[procId] = manager
            LOG("Manager created")
        finally:
            self.__execLock.release()
        return manager

    #===========================================================================
    def getInstanceId(self, procId):
        self.__execLock.acquire()
        try:
            instance = 0
            LOG("Obtain available instance for " + repr(procId))
            if procId in self.__executorInstanceNumbers:
                for i in range(0,50):
                    if not i in self.__executorInstanceNumbers[procId]:
                        instance = i
                        break
                self.__executorInstanceNumbers[procId].append(instance)
            else:
                self.__executorInstanceNumbers[procId] = [instance]
            procId = procId + "#" + str(instance)
            LOG("Instance is " + repr(procId))
        finally:
            self.__execLock.release()
        return procId
        
    #===========================================================================
    def openExecutor(self, procId, clientKey, clientMode, openMode = {},
                     arguments = None, condition = None, parentId = None):
        ctxInfo = Config.instance().getContextConfig(self.ctxName)
        driverName = ctxInfo.getDriver()
        maxProcs = int(Config.instance().getDriverConfig(driverName).getMaxProcs())
        if maxProcs>0:
            activeProcs = self.getNumActiveProcs()
            if (activeProcs >= maxProcs):
                raise ContextError("Could not launch executor. Maximum number of processes reached (" + str(maxProcs) + ")")
        
        success = False
        LOG("Requested opening executor " + repr(procId), level = LOG_PROC)
        executor = self.createExecutor(procId)
        
        # Update the proc id with the instance number
        procId = executor.getProcId()
        
        # Get configuration defaults for open mode and update it
        #TODO: get open mode defaults from config
        useOpenMode = {Automatic:False,Visible:True,Blocking:True}
        useOpenMode.update(openMode)

        # Set the controlling client key if the procedure is visible
        if useOpenMode[Visible] == True:
            clientInfo = self.getClient(clientKey)
            executor.addClient(clientInfo, True)
            clientInfo.addExecutor(procId) 
            clientInfo.setMode(clientMode)
        
        LOG("Launching executor " + repr(procId) + " in mode " + repr(useOpenMode), level = LOG_PROC)

        if arguments:
            executor.setArguments(arguments)
        if condition:
            executor.setCondition(condition)
        if parentId:
            executor.setParent(parentId)
            
        # Set the open mode and open the executor
        executor.setOpenMode(useOpenMode)
        
        executor.start()
        success = executor.waitForOpen()
        LOG("Executor launched (" + repr(success) + ")", level = LOG_PROC)

        if not success:
            error,reason = executor.getError()
            self.notifyExecutorOperation(procId, CtxMessages.DATA_EXOP_CLOSE)
            self.killExecutor(procId)
            self.clearExecutor(procId)
            raise ContextError("Could not launch executor",error + ":" + reason)
        else:
            initialStatus = executor.getStatus()
            if initialStatus == server.executor.status.ERROR:
                error,reason = executor.getError()
                self.notifyExecutorOperation(procId, CtxMessages.DATA_EXOP_CLOSE)
                self.killExecutor(procId)
                self.clearExecutor(procId)
                raise ContextError("Executor failed startup",error + ":" + reason)
            else:
                pid = executor.getExecutorPid()
                self.__executorsRegistry.addProcess(pid,procId)
                
                LOG("Notifying executor open", level = LOG_PROC)
                self.notifyExecutorOperation(procId, CtxMessages.DATA_EXOP_OPEN, clientKey, clientMode)
                LOG("Open finished")
        return 

    #===========================================================================
    def closeExecutor(self, procId):
        LOG("Requested closing executor " + repr(procId))

        executor = self.getExecutor(procId)
        
        if executor is None:
            raise ContextError("No such executor: " + repr(procId))
        
        LOG("Closing executor")
        executor.stop()
        success = executor.waitForClose()
        LOG("Executor closed (" + repr(success) + ")")
        
        if not success: 
            error,reason = executor.getError()
            self.notifyExecutorOperation(procId, CtxMessages.DATA_EXOP_CLOSE)
            self.killExecutor(procId)
            self.clearExecutor(procId)
            raise ContextError("Could not close executor",error + ":" + reason)
        else:
            LOG("Notifying executor close", level = LOG_PROC)
            self.notifyExecutorOperation(procId, CtxMessages.DATA_EXOP_CLOSE)
            self.clearExecutor(procId)
            LOG("Close finished")
            
        return 

    #===========================================================================
    def clearExecutor(self, procId):
        LOG("Clearing executor for " + repr(procId))
        
        executor = self.getExecutor(procId)
        
        if executor is None:
            raise ContextError("No such executor: " + repr(procId))
        # Remove executor from client infos
        clients = executor.getClients()
        for client in clients:
            self.getClient(client).delExecutor(procId)
            
        self.__execLock.acquire()
        try:
            # Remove executor manager
            del self.__executorManagers[procId]
            # Remove the corresponding instance from the list
            idx = procId.find("#")
            id = procId[0:idx]
            instance = int(procId[idx+1:])
            # Remove from the registry
            self.__executorsRegistry.removeProcess(procId)
            if id in self.__executorInstanceNumbers:
                self.__executorInstanceNumbers[id].remove(instance)
                if len(self.__executorInstanceNumbers[id])==0:
                    self.__executorInstanceNumbers.pop(id)
        finally:
            self.__execLock.release()
        return
            
    #===========================================================================
    def killExecutor(self, procId):
        LOG("Killing executor for " + repr(procId))
        
        executor = self.getExecutor(procId)
        
        if executor is None:
            raise ContextError("No such executor: " + repr(procId))
        
        executor.stop(True)
        
        self.notifyExecutorOperation(procId, CtxMessages.DATA_EXOP_KILL)
        return True

    #===========================================================================
    def clientAttachExecutor(self, procId, clientKey, clientMode, firstClient):
        
        executor = self.getExecutor(procId)
        
        if executor is None:
            raise ContextError("No such executor: " + repr(procId))
        
        LOG("Client " + str(clientKey) + " requested attaching executor " + procId)
        clientKey = int(clientKey)
        clientInfo = self.getClient(clientKey)
        if not procId in clientInfo.getExecutors():
            clientInfo.addExecutor(procId) 
            clientInfo.setMode(clientMode)
            
            if not executor.addClient(clientInfo, firstClient):
                raise ContextError("Cannot add client in mode " + repr(clientMode))
                    
            self.notifyExecutorOperation(procId, CtxMessages.DATA_EXOP_ATTACH, clientKey, clientMode)

    #===========================================================================
    def clientDetachExecutor(self, procId, clientKey):
        
        executor = self.getExecutor(procId)
        
        if executor is None:
            raise ContextError("No such executor: " + repr(procId))
        
        LOG("Client " + str(clientKey) + " requested detaching executor " + procId)
        clientKey = int(clientKey)
        clientInfo = self.getClient(clientKey)
        if procId in clientInfo.getExecutors():
            self.getClient(clientKey).delExecutor(procId)
            executor.removeClient(clientKey)
            self.notifyExecutorOperation(procId, CtxMessages.DATA_EXOP_DETACH, clientKey)

    #===========================================================================
    def createClient(self, clientKey, host, clientMode):
        self.__clientLock.acquire()
        try:
            self.__clientInfo[clientKey] = ClientInfo(clientKey,host,clientMode)
        finally: 
            self.__clientLock.release()
        return

    #===========================================================================
    def removeClient(self, clientKey):
        # Process orphan executors
        clientInfo = self.getClient(clientKey)
        
        self.__clientLock.acquire()
        try:
            if clientInfo:
                execs = clientInfo.getExecutors()
                # Disconnect the interface
                self.__guiIFC.disconnect(clientKey, eoc = False)
                if len(execs)==0:
                    LOG("No executors linked to client " + repr(clientKey), level = LOG_PROC )
                else:
                    for procId in execs:
                        executor = self.getExecutor(procId)
                        if executor is not None:
                            executor.clientLost(clientKey)
                        else:
                            LOG("Unknown executor:" + procId)
                LOG("Unregistering client: " + repr(clientKey))
                host = self.__clientInfo[clientKey].getHost()
                del self.__clientInfo[clientKey]
        finally:
            self.__clientLock.release()
        return

    #===========================================================================
    def getExecutors(self):
        self.__execLock.acquire()
        keys = []
        try:
            keys = self.__executorManagers.keys()
        finally:
            self.__execLock.release()
        return keys

    #===========================================================================
    def getExecutor(self, procId):
        self.__execLock.acquire()
        exc = None
        try:
            exc = self.__executorManagers.get(procId)
        finally:
            self.__execLock.release()
        return exc

    #===========================================================================
    def getClients(self):
        self.__clientLock.acquire()
        keys = []
        try:
            keys = self.__clientInfo.keys()
        finally:
            self.__clientLock.release()
        return keys

    #===========================================================================
    def getClient(self, clientKey):
        self.__clientLock.acquire()
        clt = None
        try:
            if clientKey == NO_CLIENT: 
                clt = None
            else:
                clt = self.__clientInfo[clientKey]
        finally:
            self.__clientLock.release()
        return clt

    #===========================================================================
    def notifyClientOperation(self, clientKey, clientMode, host, operation):

        LOG("Notifying client operation " + repr(operation) + ", " + repr(clientKey))
        
        # Build the notification message
        msg = MsgHelper.createMessage(CtxMessages.MSG_CLIENT_OP)
        msg[CtxMessages.FIELD_GUI_KEY] = clientKey
        msg[CtxMessages.FIELD_GUI_MODE] = clientMode
        msg[FIELD_HOST] = host
        msg[CtxMessages.FIELD_CLOP] = operation
        
        # Get all connected clients but the logged in one
        clientKey = int(clientKey)

        clientKeys = []
        for client in self.getClients():
            client = int(client)
            if client != clientKey:
                clientKeys.append(client)

        if len(clientKeys)>0:
            self.messageToClients("CTX", clientKeys, msg)
        LOG("Notify done")

    #===========================================================================
    def notifyExecutorOperation(self, procId, operation, clientKey = "", clientMode = "" ):
        
        LOG("Notifying executor operation " + repr(operation) + ", " + repr(procId))
        
        # Build the notification message
        msg = MsgHelper.createMessage(CtxMessages.MSG_EXEC_OP)
        msg[CtxMessages.FIELD_PROC_ID] = procId
        msg[CtxMessages.FIELD_EXOP] = operation
        msg[CtxMessages.FIELD_GUI_KEY] = clientKey
        msg[CtxMessages.FIELD_GUI_MODE] = clientMode
        msg[ExcMessages.FIELD_EXEC_STATUS] = self.getExecutor(procId).getStatus()
        msg[ExcMessages.FIELD_CONDITION] = self.getExecutor(procId).getCondition()
        msg[CtxMessages.FIELD_OPEN_MODE] = self.getExecutor(procId).getOpenMode()

        clientKeys = self.getClients()
        LOG("All clients: " + repr(clientKeys))
        
        # Notify parent procedure also, if it is present
        executor = self.getExecutor(procId)
        if executor:
            parent = executor.getParent()
            if parent is not None:
                clientKeys += [parent]
        
        LOG("Notifying clients: " + repr(clientKeys))
        if len(clientKeys)>0:
            self.messageToClients(procId, clientKeys, msg)
            
        LOG("Notifying executor operation done")

    #===========================================================================
    def executorError(self, procId, msg):
        errorText = "Executor fatal error"  
        errorMsg = MsgHelper.createError2(CtxMessages.MSG_EXEC_ERROR, procId, "CLT", errorText, msg)
        errorMsg[CtxMessages.FIELD_PROC_ID] = procId
        
        executor = self.getExecutor(procId)
        if executor:
            procClients = executor.getClients()
            self.messageToClients(procId, procClients, errorMsg)
        else:
            LOG("No executor found to notify error", LOG_ERROR)
        
    #===========================================================================                
    def buildExecutorInfo(self, procId, resp ):
        
        executor = self.getExecutor(procId)

        pname = ProcedureManager.instance().getProcedure(procId).name()
        
        if executor is None:
            txt = "No such executor: " + repr(procId)
            reason = " "
            resp[ExcMessages.FIELD_PROC_ID] = procId
            resp[ExcMessages.FIELD_PARENT_PROC] = " "
            resp[ExcMessages.FIELD_PROC_NAME] = pname
            resp[ExcMessages.FIELD_ASRUN_NAME] = " "
            resp[ExcMessages.FIELD_LOG_NAME] = " "
            resp[ExcMessages.FIELD_EXEC_PORT] = "0"
            resp[ExcMessages.FIELD_EXEC_STATUS] = server.executor.status.UNINIT
            resp[ExcMessages.FIELD_CONDITION] = ""
            resp[ExcMessages.FIELD_GUI_LIST] = " "
            resp[ExcMessages.FIELD_GUI_CONTROL] = " "
            resp[CtxMessages.FIELD_OPEN_MODE] = " "
            resp[ExcMessages.FIELD_LINE] = "0"
            resp[ExcMessages.FIELD_CSP] = procId
        else:
            control = executor.getControllingClient()
            if control is None:
                control = " "

            guiList = ""
            for gui in executor.getMonitoringClients():
                if len(guiList)>0: guiList = guiList + ","
                guiList = guiList + str(gui)
            
            resp[ExcMessages.FIELD_PROC_ID] = procId
            resp[ExcMessages.FIELD_PARENT_PROC] = executor.getParent()
            resp[ExcMessages.FIELD_PROC_NAME] = pname
            resp[ExcMessages.FIELD_ASRUN_NAME] = executor.getAsRunFile()
            resp[ExcMessages.FIELD_LOG_NAME] = executor.getLogFile()
            resp[ExcMessages.FIELD_EXEC_PORT] = executor.getPort()
            resp[ExcMessages.FIELD_EXEC_STATUS] = executor.getStatus()
            resp[ExcMessages.FIELD_CONDITION] = executor.getCondition()
            resp[ExcMessages.FIELD_GUI_LIST] = guiList
            resp[ExcMessages.FIELD_GUI_CONTROL] = control
            resp[CtxMessages.FIELD_OPEN_MODE] = executor.getOpenMode()
            resp[ExcMessages.FIELD_CSP] = executor.getStackPosition()

        return resp
    
    #===========================================================================                
    def getFileData(self, procId, logId):
        executor = self.getExecutor(procId)
        if executor is None:
            return resp
        code = " "
        if (logId == ExcMessages.DATA_FILE_ASRUN):
            filename = executor.getAsRunFile()
        else:
            filename = executor.getLogFile()

        lines = []
        if filename and os.path.exists(filename):
            f = file(filename)
            lines = f.readlines()
        return lines

    #===========================================================================                
    def getNumActiveProcs(self):
        activeProcs = 0
        for key in self.getExecutors():
            executor = self.getExecutor(key)
            if executor and executor.isActive(): activeProcs += 1
        return activeProcs
        
################################################################################
if __name__ == "__main__":
    
    try:
        # Warm option -w does not require a value
        shortopts = 'c:n:s:w'
        
        options, trailing = getopt.getopt(sys.argv[1:], shortopts)
        
        configFile = None
        ctxName = None
        lstPort = None
        warmStart = False
        for option,value in options:
            if option == '-c':
                configFile = value
            if option == '-n':
                ctxName = value
            if option == '-s':
                lstPort = int(value)
            if option == '-w':
                warmStart = True
                
        if configFile is None:
            os.write(sys.stderr.fileno(),"ERROR: no configuration file given\n")
            os.write(sys.stderr.fileno(),"  Use argument: -c <path to file>\n")
            sys.exit(1)
    
        if ctxName is None:
            os.write(sys.stderr.fileno(),"ERROR: no context name given\n")
            os.write(sys.stderr.fileno(),"  Use argument: -n <name>\n")
            sys.exit(1)

        if lstPort is None:
            os.write(sys.stderr.fileno(),"ERROR: no listener port given\n")
            os.write(sys.stderr.fileno(),"  Use argument: -s <port>\n")
            sys.exit(1)
        
        LOG.setLogFile( "SPEL_Context_" + ctxName )
        
        Config.instance().load(configFile)
        if not Config.instance().validate():
            os.write(sys.stderr.fileno(),"ERROR: please check configuration file\n")
            sys.exit(1)
    
        level = Config.instance().getProperty( COMMON, "LogDetail" )
        if level:
            level = level.upper()
            if level == "COMM":
                level = LOG_COMM
            elif level == "PROC":
                level = LOG_PROC
            elif level == "CNFG":
                level = LOG_CNFG
            elif level == "INIT":
                level = LOG_INIT
            elif level == "MAIN":
                level = LOG_MAIN
            else:
                level = LOG_COMM
        else:
            level = LOG_COMM
        LOG.showLevel(level)
    
        CTX = ContextClass(ctxName,lstPort, warmStart)
        CTX.start()
        
        CTX.waitForClose()
        
        sys.stderr.write("*********************************************\n")
        sys.stderr.write("      SPEL Context (" + ctxName + ") Closed  \n")
        sys.stderr.write("*********************************************\n")
        
    except SystemExit,ex:
        raise ex
    except BaseException,ex:
        os.write(sys.stderr.fileno(),"==================================================\n")
        os.write(sys.stderr.fileno(),"UNHANDLED EXCEPTION IN CONTEXT:\n ")
        os.write(sys.stderr.fileno(),ex.message + "\n")
        os.write(sys.stderr.fileno(),repr(ex) + "\n")
        traceback.print_exc(file=sys.stderr)
        os.write(sys.stderr.fileno(),"==================================================\n")

