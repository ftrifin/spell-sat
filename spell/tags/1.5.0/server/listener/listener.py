################################################################################

"""
PACKAGE 
    server.listener.listener
FILE
    listener.py
    
DESCRIPTION
    SPEL Listener process main class
    
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
from spell.config.reader import *
from spell.config.constants import LISTENER,COMMON
from server.core.ipc.interfaceserver import IPCinterfaceServer
from server.core.process.manager import *
from server.core.messages.base import *
from server.core.messages.msghelper import MsgHelper
from server.core.ipc.xmlmsg import *
from server.core.registry.processRegistry import ProcessRegistry
import server.core.messages.listener
import server.core.messages.context
from spell.utils.log import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from guioperations import *

#*******************************************************************************
# System Imports
#*******************************************************************************
import getopt,sys,os,thread,traceback
from Queue import Queue,Empty
import signal
import time

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

# Shortcut to listener messages group
LstMessages = server.core.messages.listener
CtxMessages = server.core.messages.context
# Timeout for starting a context
CONTEXT_START_TIMEO = 10.0
# Listener instance
LST = None

#For registering contexts which are opened
__contextRegistry = None

################################################################################
def signalHandler( signum, frame ):
    global LST
    if LST is not None:
        LST.stop()

################################################################################
class ListenerClass(object):
    
    """
    Main class of the SPEL Listener entity. Listens at a given port waiting for
    GUI connections. Manages the SPEL Context processes on GUI requests.
    """
    
    # Client key list
    clientKeys = []
    # Context list
    contexts = []
    # Holds contextID / key pairs
    contextKeys = {}
    # Holds contextID / port pairs
    contextPorts = {}
    # Context status (available/running)
    contextStatus = {}
    # GUI operations delegate
    GUI_Operations = None
    # Synchronization issues
    buffer = None
    # IPC context interface
    __ctxIFC = None 
    # IPC gui interface
    __guiIFC = None 
    # Client lock
    __clientLock = None
    # Context lock
    __ctxLock = None
    
    #===========================================================================
    def __init__(self, warmStart=False):
        self.openContexts = {}
        self.buffer = Queue(1)
        self.GUI_Operations = GUIOperations(self)
        # Obtain the list of available contexts from cfg file
        self.contexts = Config.instance().getAvailableContexts()
        # Update the context status map (all available)
        for ctx in self.contexts:
            self.contextPorts[ctx] = 0
            self.contextStatus[ctx] = LstMessages.DATA_CTX_AVAILABLE
        self.__ctxIFC = IPCinterfaceServer("LST-CTX")
        self.__guiIFC = IPCinterfaceServer("LST-GUI")
        self.__clientLock = thread.allocate_lock()
        self.__ctxLock = thread.allocate_lock()
        self.__contextRegistry = ProcessRegistry("LST", warmStart)
    
    #===========================================================================
    def start(self):
        
        # Get the port number from configuration
        port = Config.instance().getProperty(LISTENER, "ListenerPort")
        LOG("Listening at port: " + str(port), level = LOG_INIT )
        
        # Setup the GUI io channel
        LOG("Setting up GUI channel", level = LOG_INIT)
        self.__guiIFC.connect(999, int(port), self.processGuiMessage, self.processGuiRequest, self.clientConnectionLost)
        self.__guiIFC.start()

        # Setup the Context io channel
        LOG("Setting up CTX channel", level = LOG_INIT)
        self.__ctxIFC.connect(998,0,self.processCtxMessage, self.processCtxRequest, self.contextConnectionLost)
        self.__ctxIFC.start()
        LOG("Ready", level = LOG_INIT )
        
        #Restoring warm context
        for contextInfo in self.__contextRegistry.getProcesses():
            #Only during startup we open contexts in warm mode
            self.openContext(contextInfo.getName(), None, True) 
   
    #===========================================================================
    def stop(self):
        LOG("Stopping, closing contexts", level = LOG_PROC )
        for contextName in self.contexts:
            if self.isContextRunning(contextName):
                self.closeContext(contextName)
        LOG("Killing remaining processes", level = LOG_PROC )
        ProcessManager.instance().killAll()
        self.__guiIFC.disconnect()
        self.__ctxIFC.disconnect()
        LOG("Disconnected", level = LOG_INIT )
        
    #===========================================================================
    def clientConnectionLost(self, clientKey):
        if clientKey in self.clientKeys:
            LOG("Listener lost connection with client: " + repr(clientKey), LOG_ERROR )
            self.clientKeys.remove(clientKey)

    #===========================================================================
    def contextConnectionLost(self, contextKey):
        # Obtain the corresponding name from the key
        for key in self.contextKeys:
            if self.contextKeys[key] == contextKey:
                contextName = key
        originalStatus = self.contextStatus[contextName]
        self.contextStatus[contextName] = LstMessages.DATA_CTX_ERROR
        if not originalStatus == LstMessages.DATA_CTX_STARTING:
            LOG("Listener lost connection with starting context " + repr(contextName), LOG_ERROR)
        else:
            LOG("Listener lost connection with context " + repr(contextName), LOG_ERROR)
        self.notifyContextCrash(contextName)
        self.clearContext(contextName)
        self.__ctxIFC.disconnect(contextKey, eoc = False)
        LOG("Context-lost done")
        
    #===========================================================================
    def contextProcessLost(self, contextName, retCode):
        if (retCode == 0) and not contextName in self.contextKeys:
            LOG("Context finished with code 0")
            return
        self.contextStatus[contextName] = LstMessages.DATA_CTX_ERROR
        if contextName in self.contexts:
            LOG("Listener lost track of context " + repr(contextName), LOG_ERROR)
            self.notifyContextCrash(contextName)
            self.clearContext(contextName)
            if contextName in self.contextKeys:
                contextKey = self.contextKeys[contextName]
                self.__ctxIFC.disconnect(contextKey, eoc = False)
            LOG("Done")
        
    #===========================================================================
    def processGuiMessage(self, msg):
        self.__clientLock.acquire()
        LOG("Received GUI message: " + msg.getId(), level = LOG_COMM)
        guiKey = int(msg.getKey())
        if msg.getId() == LstMessages.MSG_GUI_LOGIN:
            LOG("Client logged in: " + str(guiKey), level = LOG_PROC)
            self.clientKeys.append(guiKey)
        elif msg.getId() == LstMessages.MSG_GUI_LOGOUT:
            self.clientKeys.remove(guiKey)
            LOG("Client logged out: " + str(guiKey), level = LOG_PROC)
        else:
            LOG("ERROR: unknown message from client: " + str(msg.getId()), LOG_ERROR)
        self.__clientLock.release()

    #===========================================================================
    def processCtxMessage(self, msg):
        self.__ctxLock.acquire()
        LOG("Received Context message: " + msg.getId(), level = LOG_COMM)
        if msg.getId() == LstMessages.MSG_CONTEXT_OPEN:
            contextName = msg[LstMessages.FIELD_CTX_NAME]
            LOG("Context logged in: " + contextName, level = LOG_PROC)
            contextKey = int(msg.getKey())
            ctxPort = msg[LstMessages.FIELD_CTX_PORT]
            self.contextPorts[contextName] = ctxPort
            self.contextStatus[contextName] = LstMessages.DATA_CTX_RUNNING
            self.contextKeys[contextName] = contextKey
            self.buffer.put(ctxPort, True)
        elif msg.getId() == LstMessages.MSG_CONTEXT_CLOSED:
            contextName = msg[LstMessages.FIELD_CTX_NAME]
            contextKey = int(msg.getKey())
            LOG("Context logged out: " + contextName + ":" + repr(contextKey), level = LOG_PROC)
            self.contextStatus[contextName] = LstMessages.DATA_CTX_AVAILABLE
            self.buffer.put(contextName, True)
        else:
            LOG("ERROR: unknown message from context:" + str(msg.getId()), LOG_ERROR)
        self.__ctxLock.release()
            
    #===========================================================================
    def processGuiRequest(self, msg):
        self.__clientLock.acquire()
        resp = self.GUI_Operations.processRequest(msg)
        self.__clientLock.release()
        return resp

    #===========================================================================
    def processCtxRequest(self, msg):
        self.__ctxLock.acquire()
        resp = self.createResponse("NONE", msg)
        self.__ctxLock.release()
        return resp
            
    #===========================================================================
    def openContext(self, contextName, clientKey = None, warm = False ):
        
        ctxScript = Config.instance().getProperty(LISTENER, "ContextScript")
        ctxScript = Config.instance().getHome() + os.sep + ctxScript
        arguments = "-c \"" + Config.instance().filename + "\" -n \"" + contextName + "\""
        arguments += " -s " + str(self.__ctxIFC.port)
        if warm == True:
            arguments += " -w"
        pythonBin = os.getenv("PYTHON", "python")
        ctxScript = pythonBin + " \"" + ctxScript + "\" " + arguments
        
        # Set the status as starting (internal)
        self.contextStatus[contextName] = LstMessages.DATA_CTX_STARTING
        
        LOG("Starting context: '" + contextName + "'", level = LOG_PROC )
        pid = ProcessManager.instance().startProcess( contextName, ctxScript, self.contextProcessLost )
        LOG("Context started with pid " + str(pid), level = LOG_PROC )

        LOG("Waiting context port", level = LOG_PROC )
        try:
            ctxPort = self.buffer.get(True,CONTEXT_START_TIMEO)
            LOG("Context port is " + str(ctxPort), level = LOG_PROC )
            # Set the status as started
            self.contextStatus[contextName] = LstMessages.DATA_CTX_RUNNING
            self.notifyContextUpdate(contextName,clientKey)
            self.__contextRegistry.addProcess(pid,contextName)
            return ctxPort
        except BaseException,ex:
            txt = "Failed to open context"
            reason = "Unable to get context listening port"
            LOG(txt + ": " + reason, LOG_ERROR)
            self.killContext(contextName)
            # Set the status as error
            self.contextStatus[contextName] = LstMessages.DATA_CTX_ERROR
            self.notifyContextUpdate(contextName,clientKey,txt,reason)
            return None

    #===========================================================================
    def closeContext(self, contextName, clientKey = None ):
        LOG("Closing context '" + contextName + "'", level = LOG_PROC )
        
        # Set the status as starting (internal)
        self.contextStatus[contextName] = LstMessages.DATA_CTX_STOPPING

        contextKey = self.contextKeys[contextName]
        closeMsg = MsgHelper.createMessage(CtxMessages.MSG_CLOSE_CTX)
        closeMsg.setSender("LST")
        closeMsg.setReceiver(contextName)
        self.__ctxIFC.sendMessage(closeMsg,contextKey)

        # Wait context to confirm closure
        try:
            LOG("Waiting context logout", level = LOG_PROC )
            self.buffer.get(True)
            LOG("Context logged out, waiting process to finish")
            ret = ProcessManager.instance().waitProcess( contextName )
            LOG("Context process closed with value " + str(ret), level = LOG_PROC )
            # Set the status as available
            self.contextStatus[contextName] = LstMessages.DATA_CTX_AVAILABLE
            self.notifyContextUpdate(contextName,clientKey)
            self.clearContext(contextName)
            return True
        except:
            LOG("Cannot close context, killing it", level = LOG_PROC )
            self.killContext(contextName)
            self.notifyContextUpdate(contextName,clientKey)
            self.clearContext(contextName)
            return None

    #===========================================================================
    def killContext(self, contextName):
        contextKey = self.contextKeys[contextName]
        LOG("Killing context '" + contextName + "':" + repr(contextKey), level = LOG_PROC )
        self.__ctxIFC.disconnect( contextKey, eoc = False )
        ProcessManager.instance().killProcess( contextName )
        LOG("Context killed", level = LOG_PROC )
        self.contextStatus[contextName] = LstMessages.DATA_CTX_AVAILABLE
        self.clearContext(contextName)
        # Now kill all associated executors
        try:
            registry = ProcessRegistry("CTX_" + contextName, True)
            executors = registry.getProcesses()
            for ex in executors:
                LOG("Killing child executor " + str(ex))
                ProcessManager.instance().killPID(ex.getPID())
                registry.removeProcess(ex.getName())
        except BaseException,ex:
            LOG("Unable to kill associated executors in context " + contextName + ": " + str(ex), LOG_ERROR )


    #===========================================================================
    def clearContext(self, contextName):
        LOG("Clearing context process " + repr(contextName))
        self.contextPorts[contextName] = 0
        if contextName in self.contextKeys:
            del self.contextKeys[contextName]  
        self.__contextRegistry.removeProcess(contextName)
        ProcessManager.instance().removeProcess( contextName )

    #===========================================================================
    def isContextRunning(self, contextName):
        return (self.contextStatus[contextName] == LstMessages.DATA_CTX_RUNNING)

    #===========================================================================
    def isContextBusy(self, contextName):
        contextKey = self.contextKeys[contextName]
        checkMsg = MsgHelper.createRequest(CtxMessages.REQ_CAN_CLOSE,"LST","CTX")
        checkMsg.setSender("LST")
        checkMsg.setReceiver(contextName)
        response = self.__ctxIFC.sendRequest(checkMsg, contextKey)
        try:
            canClose = eval(response[CtxMessages.FIELD_BOOL])
        except:
            canClose = True
        return canClose

    #===========================================================================
    def getContextStatus(self, contextName):
        return self.contextStatus[contextName]

    #===========================================================================
    def getContextPort(self, contextName):
        if not self.contextPorts.has_key(contextName):
            return 0
        return self.contextPorts[contextName]

    #===========================================================================
    def getContextList(self):
        return self.contexts

    #===========================================================================
    def notifyContextUpdate(self, ctxName, clientKey = None, errorTxt = None, errorReason = None ):
        # Build the notification message
        msg = MsgHelper.createMessage(LstMessages.MSG_CONTEXT_OP)
        msg[LstMessages.FIELD_CTX_NAME] = ctxName
        msg[LstMessages.FIELD_CTX_STATUS] = self.contextStatus[ctxName]
        if errorTxt:
            msg[FIELD_ERROR] = errorTxt
        if errorReason:
            msg[FIELD_REASON] = errorReason
        # Get all connected clients but the clients of the
        # passed executor
        clientKeys = []
        
        # Notify to everybody if its the listener who closes
        if clientKey is None:
            clientKey = -1
        else:
            clientKey = int(clientKey)
        for client in self.clientKeys:
            client = int(client)
            if client != clientKey:
                clientKeys.append(client)
        
        if len(clientKeys)>0:
            LOG("Notifying context operation done by " + str(clientKey) 
                + " on " + repr(ctxName) + " to " + repr(clientKeys))
            self.messageToClients(clientKeys, msg)

    #===========================================================================
    def notifyContextCrash(self, ctxName ):
        # Build the notification message
        msg = MsgHelper.createMessage(LstMessages.MSG_CONTEXT_LOST)
        msg.setType(MSG_TYPE_ERROR)
        msg[LstMessages.FIELD_CTX_NAME] = ctxName
        msg[LstMessages.FIELD_CTX_STATUS] = self.contextStatus[ctxName]
        msg[FIELD_ERROR] = "Context " + ctxName + " connection lost"
        msg[FIELD_REASON] = " " 
        
        clientKeys = []
        for client in self.clientKeys:
            clientKeys.append(int(client))
        
        if len(clientKeys)>0:
            LOG("Notifying context crash " + repr(ctxName) + " to " + repr(clientKeys))
            self.messageToClients(clientKeys, msg)
            
    #===========================================================================
    def messageToClients(self, clientKeys, msg):
        for clientKey in clientKeys:
            self.__guiIFC.sendMessage(msg,clientKey)
            

################################################################################
# Entry point of the SPEL Listener application
################################################################################
if __name__ == "__main__":

    def sleepLoop():
        while True:
            time.sleep(1)

    try:
        LOG.setLogFile( "SPEL_Listener" )
        # Warm option -w does not require a value
        shortopts = 'c:w'
        longopts  = [ 'config=' ]
        options, trailing = getopt.getopt(sys.argv[1:], shortopts, longopts)
        
        configFile = None
        warmStart = False
        for option,value in options:
            if option in [ '-c', '--config' ]:
                configFile = value
            # Warm start
            if option in ['-w']:
                warmStart = True
                
        if configFile is None:
            os.write(sys.stderr.fileno(),"ERROR: no configuration file given\n")
            os.write(sys.stderr.fileno(),"  Use argument: --config <path to file>\n")
            sys.exit(1)
        
        # Load the configuration file
        Config.instance().load(configFile)
        if not Config.instance().validate():
            os.write(sys.stderr.fileno(),"ERROR: please check configuration file\n")
            sys.exit(1)
    
        signal.signal( signal.SIGABRT, signalHandler )
        signal.signal( signal.SIGILL, signalHandler )
        signal.signal( signal.SIGINT, signalHandler )
        signal.signal( signal.SIGTERM, signalHandler )
    
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
    
        # Create the listener and start it
        LST = ListenerClass(warmStart)
        LST.start()
        
        os.write(sys.stderr.fileno(),"*********************************************\n")
        os.write(sys.stderr.fileno(),"             SPEL Listener Ready \n")
        os.write(sys.stderr.fileno(),"*********************************************\n")
        os.write(sys.stderr.fileno(),"\nPress enter to shutdown\n")
        try:
            if sys.stdin.isatty():
                raw_input()
            else:
                sleepLoop()
        except:pass
        
        LST.stop()
        os.write(sys.stderr.fileno(),"\nDone\n")
        sys.exit(0)
    except SystemExit,ex:
        raise ex
    except BaseException,ex:
        traceback.print_exc( file = sys.stderr )
        os.write(sys.stderr.fileno(),"==================================================\n")
        os.write(sys.stderr.fileno(),"UNHANDLED EXCEPTION:\n ")
        os.write(sys.stderr.fileno(),repr(ex.message) + "\n")
        os.write(sys.stderr.fileno(),repr(ex) + "\n")
        os.write(sys.stderr.fileno(),"==================================================\n")

