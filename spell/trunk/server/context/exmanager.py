###############################################################################

"""
PACKAGE 
    server.context.exmanager 
FILE
    context.py
    
DESCRIPTION
    Executor manager class. Interfaces the executor process.
    
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
from server.core.ipc.interfaceserver import IPCinterfaceServer
from server.core.process.manager import ProcessManager
from spell.utils.log import *
from server.core.messages.base import *
from server.core.messages.msghelper import *
import server.core.messages.executor as ExcMessages
import server.core.messages.context as CtxMessages
import server.executor.status
from server.procedures.manager import ProcedureManager
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import getopt,sys,os,traceback,Queue,thread,threading

################################################################################

# Visibility for imports
__all__ = [ 'ExecutorManagerClass' ]

EXECUTOR_OPEN_TIMEOUT  = 20
EXECUTOR_CLOSE_TIMEOUT = 10
START_TIMEOUT  = 15
STOP_TIMEOUT = 15
NO_CLIENT = "NO_CLIENT"

################################################################################
class ExecutorManagerClass(threading.Thread):
    
    # Context data -------------------------------------------------------------
    # Holds the context reference
    __context = None
    
    # Executor data ------------------------------------------------------------
    # Holds the procedure id
    __procId = None
    __execStatus    = server.executor.status.UNINIT
    __execError = " "
    __execReason = " "
    __execKey = None
    __execPort = None
    __execScript = None
    __execAsRun = None
    __execPID = None
    # Holds the parent procedure identifier, if any
    __parentProc = None
    __execLog = None
    __csp = None
    # Context channel key for controlling client
    __controllingKey = NO_CLIENT
    # Context channel keys for monitoring clients
    __monitoringKeys = []
    # Ready flag
    __ready = False
    # Executor arguments
    __arguments = {}
    # Executor condition
    __condition = None
    # Open mode
    __openMode = {}
    
    # Internal processing ------------------------------------------------------
    # For locking execution during executor process operations
    __execLock = None
    __closeLock = None
    __contextLock = None
    
    # IPC interface with executor
    __execIFC = None
    
    #===========================================================================
    def __init__(self, context, procId):
        threading.Thread.__init__(self)
        # Store context reference
        self.__context = context
        # Initialize executor status
        self.__procId = procId
        self.__execStatus = server.executor.status.UNINIT
        self.__execError = " "
        self.__execReason = " "
        self.__execKey = None
        self.__execPort = None
        self.__execAsRun = None
        self.__execPID = None
        self.__parentProc = None
        self.__execLog = None
        self.__controllingKey = NO_CLIENT
        self.__monitoringKeys = []
        self.__execLock = Queue.Queue(1)
        self.__closeLock = Queue.Queue(1)
        self.__contextLock = Queue.Queue(1)
        self.__csp = None
        self.__ready = False
        self.__arguments = {}
        self.__openMode = {}
        self.__condition = None
        # IPC interface for executors
        self.__execIFC = IPCinterfaceServer("EXM-EXC")
        # Obtain the executor start command
        execScript = Config.instance().getProperty(CONTEXT, "ExecutorScript")
        pythonBin = os.getenv("PYTHON", "python")
        arguments = "-c \"" + Config.instance().filename + "\" -n \"" +\
                    self.__context.ctxName + "\"" + " -p \"" + self.__procId + "\""
        self.__execScript = pythonBin + " \"" + execScript + "\" " + arguments

    #===========================================================================
    def run(self):
        
        try:
            LOG("Setting up Executors channel", level = LOG_INIT)
            # Setup the Executor io channel
            self.__execIFC.connect(777,0,self.processExecutorMessage, self.processExecutorRequest, self.execConnectionLost)
            self.__execIFC.start()
            
            # Start executor process
            self.__startExecutor()
            # Wait until a close command arrives
            force = self.__waitStop()
            # Close the executor and finish thread
            self.__closeExecutor(force)
        except Exception,ex:
            print ex
            self.__context.executorError(self.__procId,repr(ex))
        LOG("Executor manager " + self.__procId + " finished")

    #===========================================================================
    def __startExecutor(self):
        
        LOG("Starting executor for procedure: '" + self.__procId + "'", level = LOG_PROC)
        command = self.__execScript + " -s " + str(self.__execIFC.port)
        self.__execPID = ProcessManager.instance().startProcess( self.__procId, command )
        
        try:
            # Wait until executor starts properly
            self.__waitExecutorOpen()
            self.__ready = True
        except Exception,ex:
            # Kill the process on error
            ProcessManager.instance().killProcess(self.__procId)
            self.setError("Unable to start executor",repr(ex))
        
    #===========================================================================
    def __waitExecutorOpen(self):
        # Wait until the open notification from executor arrives
        # see __execProcessLogin method
        LOG("Executor launched, waiting confirmation", level = LOG_PROC)
        confirm = self.__execLock.get(True, START_TIMEOUT)
        if confirm is None or confirm != self.__procId:
            self.setError("Could not get executor confirmation","Executor failed to start")
            return
        LOG("Executor started", level = LOG_PROC)
        self.__contextLock.put(1)

    #===========================================================================
    def __waitExecutorClose(self):
        # Wait until the open notification from executor arrives
        # see __execProcessLogout method
        LOG("Executor closed, waiting confirmation", level = LOG_PROC)
        confirm = self.__execLock.get(True, STOP_TIMEOUT)
        if confirm is None or confirm != self.__procId:
            LOG("Did not get close confirmation on time")
            self.setError("Could not get executor confirmation","Executor failed to close")
            return False
        LOG("Got confirmation, waiting process to finish", level = LOG_PROC)
        ret = ProcessManager.instance().waitProcess( self.__procId )
        LOG("Process finished with value " + str(ret))
        ProcessManager.instance().removeProcess( self.__procId )
        self.__contextLock.put(1)
        return True

    #===========================================================================
    def __waitStop(self):
        # Wait until stop method is called
        return self.__closeLock.get(True)

    #===========================================================================
    def waitForOpen(self):
        try:
            self.__contextLock.get(True,EXECUTOR_OPEN_TIMEOUT)
        except:
            return False
        return True

    #===========================================================================
    def waitForClose(self):
        if not self.__ready: return True
        try:
            self.__contextLock.get(True,EXECUTOR_CLOSE_TIMEOUT)
        except:
            return False
        return True

    #===========================================================================
    def getExecutorPid(self):
        return self.__execPID

    #===========================================================================
    def stop(self, force = False):
        # Provokes the manager thread to stop
        self.__closeLock.put(force)
        
    #===========================================================================
    def __closeExecutor(self, force):
        if not self.__ready or force:
            # When forcing
            self.__ready = False
            self.__execIFC.disconnect( eoc = False ) 
            ProcessManager.instance().killProcess(self.__procId)
            LOG("Executor closed (force=True)", level = LOG_PROC)
            return
        # Send the close command to the executor
        msg = MsgHelper.createMessage(ExcMessages.CMD_CLOSE)
        msg[ExcMessages.FIELD_PROC_ID] = self.__procId
        msg.setSender("CTX")
        msg.setReceiver(self.__procId)
        LOG("Send close command to executor " + self.__procId, level = LOG_COMM )
        self.__execIFC.sendMessage(msg,self.__execKey)
    
        try:
            # Wait until executor stops properly
            self.__waitExecutorClose()
        except Exception,ex:
            # Remove the process from process manager
            LOG("Unable to close executor (" + repr(ex) + "), killing it")
            ProcessManager.instance().killProcess(self.__procId)
            self.setError("Unable to stop executor",repr(ex))
        finally:
            self.__ready = False
            # Disconnect the interface
            LOG("Disconnecting executor interface", level = LOG_PROC)
            self.__execIFC.disconnect() 
            LOG("Executor closed", level = LOG_PROC)

    #===========================================================================
    def __execProcessLogin(self, msg):
        LOG("Executor process logged in: " + self.__procId, level = LOG_PROC)
        LOG("Executor key: " + repr(msg.getKey()), level = LOG_COMM)
        
        self.__execKey       = int(msg.getKey())
        self.__execPort      = int(msg[ExcMessages.FIELD_EXEC_PORT])
        self.__execStatus    = msg[ExcMessages.FIELD_EXEC_STATUS]
        
        # ----------------------------------------------------------------------
        # Parse status
        # ----------------------------------------------------------------------
        if self.__execStatus != server.executor.status.LOADED:
            error = msg[FIELD_ERROR]
            reason = msg[FIELD_REASON]
            self.setError(error,reason)
            resp = MsgHelper.createError(ExcMessages.RSP_NOTIF_EXEC_OPEN, msg, error, reason)
        else:
            # ----------------------------------------------------------------------
            # Parse AsRUN info
            # ----------------------------------------------------------------------
            asRun = msg[ExcMessages.FIELD_ASRUN_NAME]
            if asRun is None or len(asRun.strip())==0:
                LOG("No AsRUN name given", LOG_WARN)
                self.__execAsRun = None
            else:
                self.__execAsRun = asRun
                LOG("Got AsRUN name: " + repr(asRun))
     
            resp = MsgHelper.createResponse(ExcMessages.RSP_NOTIF_EXEC_OPEN, msg)

            # ----------------------------------------------------------------------
            # Parse Log info
            # ----------------------------------------------------------------------
            logFileName = msg[ExcMessages.FIELD_LOG_NAME]
            if logFileName is None or len(logFileName.strip())==0:
                LOG("No log name given", LOG_WARN)
                self.__execLog = None
            else:
                self.__execLog = logFileName
                LOG("Got log name: " + repr(logFileName))

            # ----------------------------------------------------------------------
            # Executor arguments
            # ----------------------------------------------------------------------
            # Assign the arguments to be used if needed
            if self.__arguments:
                argStr = str(self.__arguments)
                resp[ExcMessages.FIELD_ARGS] = argStr
                LOG("Executor arguments: " + argStr, level = LOG_PROC)
            else:
                LOG("No arguments given")

            # ----------------------------------------------------------------------
            # Tell the executor the open mode information
            # ----------------------------------------------------------------------
            LOG("Setting open mode: " + repr(self.getOpenMode()))
            resp[CtxMessages.FIELD_OPEN_MODE] = str(self.getOpenMode())
            
            # ----------------------------------------------------------------------
            # Client information
            # ----------------------------------------------------------------------
            # Insert client data to the message if there is such a client
            if self.__controllingKey != NO_CLIENT:
                clientInfo = self.__context.getClient(self.__controllingKey)
                resp[ExcMessages.FIELD_GUI_CONTROL] = self.__controllingKey
                resp[ExcMessages.FIELD_GUI_CONTROL_HOST] = clientInfo.getHost()
                LOG("Controlling GUI: " + repr(self.__controllingKey))
            else:
                LOG("No controlling GUI")
                resp[ExcMessages.FIELD_GUI_CONTROL] = " "
                resp[ExcMessages.FIELD_GUI_CONTROL_HOST] = " "
            resp[ExcMessages.FIELD_PARENT_PROC] = self.getParent()
            
            # ----------------------------------------------------------------------
            # Executor condition
            # ----------------------------------------------------------------------
            if self.__condition is not None and len(self.__condition)>0:
                resp[ExcMessages.FIELD_CONDITION] = self.__condition
                LOG("Executor condition: " + repr(self.__condition), level = LOG_PROC)
    
            LOG("Executor login done: " + self.__procId, level = LOG_PROC)
            
        # Release the lock
        self.__execLock.put(self.__procId)
        
        return resp
    
    #===========================================================================
    def __execProcessLogout(self, msg):
        LOG("Executor process logged out", level = LOG_PROC)
        # Update the status
        self.__execStatus = server.executor.status.UNINIT
        # Release the lock
        self.__execLock.put(self.__procId)

    #===========================================================================
    def __executorRequest(self, msg):
        response = None
        if (msg.getType() == MSG_TYPE_NOTIFY):
            # For notify status messages, update the executor status
            if msg[FIELD_DATA_TYPE]==ExcMessages.DATA_TYPE_STATUS:
                self.__execStatus = msg[ExcMessages.FIELD_EXEC_STATUS]
                self.__condition = msg[ExcMessages.FIELD_CONDITION]
                LOG("Current executor status: " + repr(self.__execStatus), level = LOG_PROC)
                LOG("Condition: " + repr(self.__condition), level = LOG_PROC)
                self.notifyStatus()
            elif msg[FIELD_DATA_TYPE] == ExcMessages.DATA_TYPE_LINE:
                # Get the line information
                self.__csp = msg[ExcMessages.FIELD_CSP]
            LOG("Forward executor " + self.__procId + " request " + msg.getId() + " to ALL clients", level = LOG_COMM)
            # Forward notification to ALL clients, including parent procedure if any
            if self.getParent() is None:
                response = self.requestToClients(msg)
            else:
                clientList = [self.getParent()] + self.getClients()
                response = self.requestToClients(msg, clientList)
                
        elif (msg.getType() == MSG_TYPE_PROMPT):
            
            # Messages to the monitoring clients
            notifyStart = MsgHelper.createMessage(MSG_ID_PROMPT_START)
            notifyStart[ExcMessages.FIELD_PROC_ID] = self.__procId
            notifyStart[ExcMessages.FIELD_TEXT] = msg[ExcMessages.FIELD_TEXT]
            notifyStart[FIELD_DATA_TYPE] = msg[FIELD_DATA_TYPE]
            notifyStart[ExcMessages.FIELD_EXPECTED] = msg[ExcMessages.FIELD_EXPECTED]
            notifyStart[ExcMessages.FIELD_OPTIONS] = msg[ExcMessages.FIELD_OPTIONS]

            notifyEnd = MsgHelper.createMessage(MSG_ID_PROMPT_END)
            notifyEnd[ExcMessages.FIELD_PROC_ID] = self.__procId
            notifyEnd[ExcMessages.FIELD_TEXT] = msg[ExcMessages.FIELD_TEXT]
            notifyEnd[FIELD_DATA_TYPE] = msg[FIELD_DATA_TYPE]
            notifyEnd[ExcMessages.FIELD_EXPECTED] = msg[ExcMessages.FIELD_EXPECTED]
            notifyEnd[ExcMessages.FIELD_OPTIONS] = msg[ExcMessages.FIELD_OPTIONS]

            cClient = self.getControllingClient()
            
            if cClient is not None:
                
                # Get the monitoring client list
                fc = lambda x : x != cClient
                monitoringClientList = filter( fc, self.getClients() )
                
                # Notify monitoring clients about the prompt start
                self.messageToClients(notifyStart, monitoringClientList)
                
                # Now send the prompt to controlling client
                LOG("Forward executor " + self.__procId + " prompt to CONTROLLING client", level = LOG_COMM)
                response = self.requestToClients(msg,[cClient])
                
                # Now notify the monitoring clients about the prompt end
                self.messageToClients(notifyEnd, monitoringClientList)
            else:
                LOG("No controlling client", LOG_ERROR)
                response = MsgHelper.createError( msg.getId(), msg, "No controlling client")
        else:
            # Forward request to controlling client
            LOG("Forward executor " + self.__procId + " request " + msg.getId() + " to CONTROLLING client", level = LOG_COMM)
            cClient = self.getControllingClient()
            if cClient is not None:
                response = self.requestToClients(msg,[cClient])
            else:
                LOG("No controlling client", LOG_ERROR)
                response = MsgHelper.createError( msg.getId(), msg, "No controlling client")
        
        return response
        
    #===========================================================================
    def __pauseExecutor(self):
        LOG("Pausing executor " + self.__procId, level = LOG_PROC )
        msg = MsgHelper.createMessage(ExcMessages.CMD_PAUSE)
        msg.setType(MSG_TYPE_COMMAND)
        msg[ExcMessages.FIELD_PROC_ID] = self.__procId
        msg.setReceiver(self.__procId)
        msg.setSender("CTX")
        self.__execIFC.sendMessage(msg,self.__execKey)
        self.__execStatus = server.executor.status.PAUSED

    #===========================================================================
    def setArguments(self, arguments):
        self.__arguments = arguments

    #===========================================================================
    def setCondition(self, condition):
        self.__condition = condition

    #===========================================================================
    def getCondition(self):
        return self.__condition

    #===========================================================================
    def setOpenMode(self, mode):
        for key in [Automatic,Visible,Blocking]:
            if key in mode:
                self.__openMode[key] = mode[key]
            else:
                self.__openMode[key] = True

    #===========================================================================
    def getOpenMode(self):
        return self.__openMode
        
    #===========================================================================
    def processExecutorRequest(self, msg):
        LOG("Process executor " + repr(self.__procId) + " request: " + msg.getId(), level = LOG_COMM)
        if msg.getId() == ExcMessages.REQ_NOTIF_EXEC_OPEN:
            resp = self.__execProcessLogin(msg)
        elif msg.getId() == CtxMessages.REQ_OPEN_EXEC:
            LOG("Launching subprocedure from executor " + self.__procId, level = LOG_PROC )
            resp = self.__context.processExecutorRequest(msg)
        else:
            resp = self.__executorRequest(msg)
        LOG("Finished executor " + repr(self.__procId) + " request: " + msg.getId(), level = LOG_COMM)
        return resp
        
    #===========================================================================
    def processExecutorMessage(self, msg):
        LOG("Received executor " + self.__procId + " message: " + msg.getId(), level = LOG_COMM)
        if msg.getId() == ExcMessages.MSG_NOTIF_EXEC_CLOSE:
            resp = self.__execProcessLogout(msg)
        else:
            # Forward notification to ALL clients, including parent procedure if any
            if self.getParent() is None:
                return self.messageToClients(msg)
            else:
                clientList = [self.getParent()] + self.getClients()
                return self.messageToClients(msg, clientList)

    #===========================================================================
    def clientLost(self, clientKey):
        # If the client lost is a monitoring one, it does not matter, just remove it
        controlLost = self.__controllingKey == clientKey
        if not controlLost:
            LOG("Monitoring client lost: " + repr(clientKey), level = LOG_PROC)
            self.removeClient(clientKey)
            return
        
        LOG("Control client lost: " + repr(clientKey), level = LOG_PROC)
        
        # If the status is in one of these status, do nothing
        noOpStatus = [server.executor.status.PAUSED,
                      server.executor.status.ERROR,
                      server.executor.status.ABORTED,
                      server.executor.status.FINISHED]

        # If the client lost is the controlling one and the procedure
        # is not controlled by another one, pause the execution
        if not (self.__execStatus in noOpStatus) and (self.getParent() is None):
            self.__pauseExecutor()

        # Remove client
        self.removeClient(clientKey)

    #===========================================================================
    def messageToExecutor(self, clientKey, msg):
        LOG("Forward message " + msg.getId() + " from client " + repr(clientKey) + " to executor " + self.__procId, level = LOG_COMM )
        msg.setSender("GUI-"+ str(clientKey))
        msg.setReceiver(self.__procId)
        self.__execIFC.sendMessage(msg,self.__execKey)

    #===========================================================================
    def requestToExecutor(self, clientKey, msg):
        LOG("Forward request " + msg.getId() + " from client " + repr(clientKey) + "==" + repr(msg.getKey) + " to executor " + self.__procId, level = LOG_COMM )
        msg.setSender("Client-"+ str(clientKey))
        msg.setReceiver(self.__procId)
        resp = self.__execIFC.forwardRequest(msg,self.__execKey)
        LOG("Response from executor obtained: " + self.__procId, level = LOG_COMM )
        return resp

    #===========================================================================
    def messageToClients(self, msg, clientList = None):
        if clientList is None:
            self.__context.messageToClients(self.__procId, self.getClients(), msg)
        else:
            self.__context.messageToClients(self.__procId, clientList, msg)

    #===========================================================================
    def requestToClients(self, msg, clientList = None):
        if clientList is None:
            return self.__context.requestToClients( self.__procId, self.getClients(), msg)
        else:
            return self.__context.requestToClients( self.__procId, clientList, msg)

    #===========================================================================
    def notifyOpen(self):
        LOG("Notifying executor open", level = LOG_PROC)
        self.__context.notifyExecutorOperation(self.__procId, CtxMessages.DATA_EXOP_OPEN, 
                                     self.__controllingKey, CtxMessages.DATA_GUI_MODE_C)

    #===========================================================================
    def notifyStatus(self):
        self.__context.notifyExecutorOperation(self.__procId, ExcMessages.DATA_TYPE_STATUS)
        
    #===========================================================================
    def execConnectionLost(self,key):
        if not self.__ready: return
        LOG("Executor manager lost connection with executor: " + repr(self.__procId), LOG_WARN)
        
        self.__execStatus = server.executor.status.ERROR
        
        # Notify error to clients
        msg = MessageClass()
        msg.setReceiver(key)
        msg = MsgHelper.createError(MSG_TYPE_ERROR, msg, "Execution aborted", 
                                " lost connection with executor " + repr(self.__procId))
        msg[ExcMessages.FIELD_PROC_ID] = self.__procId
        
        if self.getParent() is not None:
            clientList = [self.getParent()] + self.getClients()
        else:
            clientList = self.getClients()
            
        self.messageToClients(msg, clientList)
        
        # Notify the procedure status to clients
        msg = MessageClass()
        data = { ExcMessages.FIELD_PROC_ID: self.__procId, 
                 ExcMessages.FIELD_CSP: self.__procId,
                 FIELD_DATA_TYPE: ExcMessages.DATA_TYPE_STATUS, 
                 ExcMessages.FIELD_EXEC_STATUS: self.__execStatus,
                 ExcMessages.FIELD_LINE: 0 } 
        msg.setProps(data)
        msg.setId(MSG_TYPE_NOTIFY)
        msg.setType(MSG_TYPE_NOTIFY)
        
        self.messageToClients(msg, clientList)
        
        #Remove process instance from the ProcessManager
        ProcessManager.instance().removeProcess( self.__procId )
        
        self.__ready = False

    #===========================================================================
    def setParent(self, parentProcId):
        self.__parentProc = parentProcId

    #===========================================================================
    def getParent(self):
        return self.__parentProc

    #===========================================================================
    def addClient(self, clientInfo, firstClient):
        clientKey = clientInfo.getKey()
        mode = clientInfo.getMode()
        host = clientInfo.getHost()
        msg = MsgHelper.createMessage(ExcMessages.MSG_ADD_CLIENT)
        if mode in [CtxMessages.DATA_GUI_MODE_C,CtxMessages.DATA_GUI_MODE_S]:
            if self.__controllingKey != NO_CLIENT:
                LOG("Client " + repr(self.__controllingKey) + " already in control", LOG_ERROR)
                return False
            LOG("Registering client "+ repr(clientKey) + " in CONTROL/SCHEDULE mode")
            self.__controllingKey = clientKey
            msg[ExcMessages.FIELD_GUI_CONTROL] = clientKey
            msg[ExcMessages.FIELD_GUI_CONTROL_HOST] = host
        elif CtxMessages.DATA_GUI_MODE_M:
            LOG("Registering client "+ repr(clientKey) + " in MONITOR mode")
            self.__monitoringKeys.append(clientKey)
            msg[ExcMessages.FIELD_GUI_LIST] = clientKey
            msg[ExcMessages.FIELD_GUI_HOST_LIST] = host
        else:
            LOG("Unrecognised client mode: " + repr(mode), LOG_ERROR)
            return False
        if not firstClient:
            # Send a message to the executor
            msg[ExcMessages.FIELD_PROC_ID] = self.__procId
            LOG("Send command to add client " + repr(clientKey)  + " to executor " +
                repr(self.__procId), level = LOG_COMM )
            self.messageToExecutor(clientKey, msg)
        return True
        
    #===========================================================================
    def removeClient(self, clientKey):
        msg = MsgHelper.createMessage(ExcMessages.MSG_REMOVE_CLIENT)
        if self.__controllingKey == clientKey:
            LOG("Unregistering CONTROLLING client "+ repr(clientKey))
            self.__pauseExecutor()
            self.__controllingKey = NO_CLIENT
            msg[ExcMessages.FIELD_GUI_CONTROL] = clientKey
        elif clientKey in self.__monitoringKeys:
            LOG("Unregistering MONITORING client "+ repr(clientKey))
            self.__monitoringKeys.remove(clientKey)        
            msg[ExcMessages.FIELD_GUI_LIST] = clientKey
        # Send a message to executor
        msg[ExcMessages.FIELD_PROC_ID] = self.__procId
        msg.setReceiver(self.__procId)
        msg.setSender("CTX")
        LOG("Send command to remove client " + repr(clientKey)  + " to executor " +
            self.__procId, level = LOG_COMM )
        self.__execIFC.sendMessage(msg,self.__execKey)

    #===========================================================================
    def getClients(self):
        list = []
        if self.__controllingKey != NO_CLIENT:
            list.append(self.__controllingKey)
        list = list + self.__monitoringKeys
        return list

    #===========================================================================
    def getControllingClient(self):
        if self.__controllingKey == NO_CLIENT:
            return 
        return self.__controllingKey
        
    #===========================================================================
    def getMonitoringClients(self):
        return self.__monitoringKeys

    #===========================================================================
    def getPort(self):
        return self.__execPort

    #===========================================================================
    def setStatus(self, status):
        self.__execStatus = status

    #===========================================================================
    def getStatus(self):
        return self.__execStatus

    #===========================================================================
    def isActive(self):
        return (self.__execStatus in [server.executor.status.PAUSED,
                                      server.executor.status.RUNNING,
                                      server.executor.status.WAITING,
                                      server.executor.status.LOADED,
                                      server.executor.status.STEPPING])
                                 

    #===========================================================================
    def setError(self, error, reason = " "):
        self.__execError = error
        self.__execReason = reason

    #===========================================================================
    def getError(self):
        return [self.__execError,self.__execReason]
                    
    #===========================================================================
    def getKey(self):
        return self.__execKey

    #===========================================================================
    def getProcId(self):
        return self.__procId

    #===========================================================================
    def getStackPosition(self):
        return self.__csp

    #===========================================================================
    def getLogFile(self):
        return self.__execLog

    #===========================================================================
    def getAsRunFile(self):
        return self.__execAsRun

                    
