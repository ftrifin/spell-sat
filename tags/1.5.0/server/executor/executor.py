################################################################################

"""
PACKAGE 
    server.executor.executor
FILE
    executor.py
    
DESCRIPTION
    Main class of the SPEL Executor process.
    
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
LOG.showlog = False

# Procedure language imports
from spell.lib.adapter.value import *
from spell.lang import *
from spell.lang.functions import *
from spell.lang.modifiers import *
from spell.lib.goto import *

# Messages
from server.core.messages.base import *
from server.core.messages.msghelper import MsgHelper
from server.core.ipc.xmlmsg import *
from server.core.messages.executor import *

# Communications
from server.ui.client import *

# Control
from server.core.sync.syncmgr import *
from server.procedures.manager import *
from spell.config.reader import *
from spell.config.constants import COMMON
from spell.lib.drivermgr import *
from server.procedures.manager import *

# Other
from spell.lib.exception import DriverException,CoreException
import server.core.messages.executor as Messages
import server.core.messages.context as CtxMessages
from spell.lib.registry import *
from spell.utils.vimport import ImportValue

#*******************************************************************************
# Local Imports
#*******************************************************************************
from controller import Controller
from mailbox import CommandMailBox
from execthread import ExecThread
from callstack import CallStack
from scheduler import Scheduler
from childmanager import ChildManager
from status import *
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import sys, types,getopt,traceback,time,thread
import Queue

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = ['Executor']

################################################################################
class Executor(object):

    """
    DESCRIPTION:
        TODO description
    """
    
    # Initialized flag
    initialized = False
    # Holds the current context name
    contextName = None
    # Holds the current context port
    contextPort = None
    # Holds the error message, if any
    errorMessage = None
    # Holds the error reason, if any
    errorReason  = None
    # Holds the procedure identifier
    procId = None
    #Holds the parent procedure identifier
    parentId = None
    # Holds the time identifier used for naming the LOG and AsRUN files
    timeID = ""
    # Holds the list of controlling clients
    controllingClient = {}
    # Holds the list of monitoring clients
    monitoringClients = {}
    # Last notified stage
    lastStage = None
    
    #---------------------------------------------------------------------------
    # MAIN CLASSES OF THE EXECUTOR
    #---------------------------------------------------------------------------
    # Holds the execution thread 
    thread = None
    # Holds the execution controller
    controller = None
    # Holds the call stack manager
    callstack = None
    # Holds the command mailbox
    mailbox = None
    # Holds the scheduler
    scheduler = None
    # Manager of child procedures
    childManager = None
    #---------------------------------------------------------------------------
    
    #---------------------------------------------------------------------------
    # Executor configuration
    #---------------------------------------------------------------------------
    # If True, will pause on each Step call
    stepByStep = False
    # Holds the execution delay
    execDelay = 0
    # Holds the RunInto status
    runInto = False
    # Holds the procedure arguments, if any
    arguments = {}
    # Holds the procedure condition, if any
    condition = None
    # Holds the executor open mode
    openMode = {}
    # Holds the list of initial loaded modules
    initialLoadedModules = None
    
    #==========================================================================
    def __init__(self):
        self.initialized = False
        self.contextName = None
        self.contextPort = None
        self.procId = None
        self.parentId = None
        self.timeID = " "
        self.errorMessage = None
        self.errorReason  = None
        self.controllingClient = {}
    	self.monitoringClients = {}
        self.thread = None
        self.controller = None
        self.callstack = None
        self.mailbox = None
        self.lastStage = None
        
        self.arguments = {}
        self.condition = None
        self.stepByStep = False
        self.execDelay = 0
        self.runInto = False
        self.openMode = {}
        self.initialLoadedModules = None
        
        # For executor process closure
        self.__closeLock = Queue.Queue(1)
        
    #==========================================================================
    def setup(self, procId, ctxName, contextPort = None, useContext = True, timeID = " "):
        """
        ------------------------------------------------------------------------
        Setup the procedure manager and the SPELL driver. If everything goes
        fine, then initialize the client interface. Finally, parse any extra
        arguments of the procedure and load it. 
        ------------------------------------------------------------------------
        """
        LOG("Setting up executor (use context=" + repr(useContext) + ")")
        self.contextName = ctxName
        self.contextPort = contextPort
        self.procId = procId
        self.useContext = useContext

        # Configure the executor as specified in the context config file
        cfg = Config.instance().getContextConfig(ctxName).getExecutorConfig()
        if RunInto in cfg:
            LOG("Run-into enabled: " + repr(cfg[RunInto]))
            self.runInto = cfg[RunInto]
        else:
            LOG("Run-into disabled by default")
            self.runInto = False

        if ExecDelay in cfg:
            LOG("ExecDelay configured: " + repr(cfg[ExecDelay]))
            self.execDelay = cfg[ExecDelay]
        else:
            LOG("ExecDelay 0 by default")
            self.execDelay = 0

        if ByStep in cfg:
            LOG("ByStep enabled: " + repr(cfg[ByStep]))
            self.stepByStep = cfg[ByStep]
        else:
            LOG("ByStep disabled by default")
            self.stepByStep = False

        LOG("Executor time ID: " + repr(timeID))
        LOG("Use context: " + repr(useContext))
        self.timeID = timeID
        REGISTRY['EXEC'] = self

        try:
            # Load procedure manager
            self.setupProcedures()

            # Load driver manager, procedure manager and other resources
            self.setupResources()
            
        except SpellException,ex:
            ClientIF.setup(self, ERROR, ctxName, contextPort )
            raise ex 

        LOG("Setting up client interface")
        loginMessage = ClientIF.setup(self, LOADED, ctxName, contextPort ) # FIXME: remove the useContext dependency
        self.parseArguments(loginMessage)

        # Load execution control infrastructure
        self.prepareExecution()
                
        LOG("Ready")
            
    #==========================================================================
    def prepareExecution(self):
        """
        ------------------------------------------------------------------------
        Create the main executor classes and establish their relationships.
        Then start the execution thread to launch the procedure execution 
        ------------------------------------------------------------------------
        """
        try:
            LOG("Setting up procedure controller for " + repr(self.procId))
            if self.thread is not None:
                del self.thread
                self.thread = None
            if self.mailbox is not None:
                del self.mailbox
            if self.controller is not None:
                del self.controller
            if self.callstack is not None:
                del self.callstack
            if self.scheduler is not None:
                del self.scheduler
            if self.childManager is not None:
                del self.childManager
            
            # Create the command processing mailbox    
            self.mailbox = CommandMailBox()
            # Create the execution controller
            self.controller = Controller()
            # Create the execution thread
            self.thread = ExecThread(self)
            # Create the call stack manager
            self.callstack = CallStack()
            # Create the execution scheduler
            self.scheduler = Scheduler()
            # Create the child procedure manager
            self.childManager = ChildManager()
            
            # Assign relationships
            self.controller.setMailbox( self.mailbox )
            self.controller.setCallstack( self.callstack )
            self.controller.setScheduler( self.scheduler )
            
            self.thread.setController( self.controller )
            self.thread.setCallstack( self.callstack )
            self.thread.setScheduler( self.scheduler )
            
            self.scheduler.setController( self.controller )
            
            # Pass the procedure arguments
            self.thread.setArguments( self.arguments )
            # Pass the procedure condition
            if self.condition is not None and len(self.condition)>0:
                self.controller.setCondition( self.condition )
                self.controller.setAutoRun()
    
            # Start the execution right away if the procedure is automatic
            # and hidden. If it is automatic and visible it is the client
            # who shall trigger the execution when it is ready.
            if self.openMode.get(Automatic) == True and\
               self.openMode.get(Visible) == False:
                self.controller.setAutoRun() 
    
            # Reset flags
            self.lastStage = None

            # Store the initial list of loaded modules (before the first execution)
            if self.initialLoadedModules is None:
               self.initialLoadedModules = sys.modules.keys()
            else:
               # Compare the initial list of loaded modules with the current list
               # of loaded modules. The new modules that are located in the procedures
               # path, are modules imported by the procedure. They have to be unloaded.
               currentLoadedModules = sys.modules.keys()
               procPath = Config.instance().getContextConfig(ctxName).getProcPath()
               for newLoadedModule in currentLoadedModules:
                  if not (newLoadedModule in self.initialLoadedModules):
                     try:
                        if (procPath in sys.modules[newLoadedModule].__file__):
                           del sys.modules[newLoadedModule]
                           LOG("Module unloaded: " + repr(newLoadedModule))
                     except: pass
    
            # Start the execution threads
            self.thread.start()
        except SpellException,ex:
            REGISTRY['CIF'].notifyError(ex.message,ex.reason)
            raise ex

    #==========================================================================
    def parseArguments(self, loginMessage):
        """
        ------------------------------------------------------------------------
        Parse the arguments to the procedure, if any 
        ------------------------------------------------------------------------
        """
        LOG("Check arguments")
        self.arguments = {}
        if loginMessage and (loginMessage[Messages.FIELD_ARGS] is not None):
            args = loginMessage[Messages.FIELD_ARGS]
            LOG("Processing executor arguments: " + args)
            try:
                self.arguments = eval(args)
                LOG("Processed arguments " + repr(self.arguments))
            except:
                self.arguments = {}
                LOG("Could not process arguments", LOG_ERROR)
          
        LOG("Check controlling client")
        if loginMessage:  
            self.controllingClient.clear()
            guiControlClientId = loginMessage[Messages.FIELD_GUI_CONTROL]
            if guiControlClientId and len(guiControlClientId.strip())>0:
                self.controllingClient[guiControlClientId] = loginMessage[Messages.FIELD_GUI_CONTROL_HOST]
                LOG("Stored controlling client: "  + repr(guiControlClientId) + " - " +
                        repr(self.controllingClient[guiControlClientId]))
            else:
                LOG("No controlling client available")
                
        LOG("Check condition")
        self.condition = None
        if loginMessage and (loginMessage[Messages.FIELD_CONDITION] is not None):
            condition = loginMessage[Messages.FIELD_CONDITION]
            self.condition = condition
            LOG("Processed condition " + repr(len(self.condition)))
        else:
            LOG("No condition set")

        LOG("Check open mode")
        self.openMode = {}
        if loginMessage and (loginMessage[CtxMessages.FIELD_OPEN_MODE] is not None):
            self.openMode = eval(loginMessage[CtxMessages.FIELD_OPEN_MODE])
            LOG("Processed open mode " + repr(self.openMode))
        else:
            LOG("No open mode set")
            
        LOG("Check parent process")
        self.parentId = None
        if loginMessage and (loginMessage[Messages.FIELD_PARENT_PROC] is not None):
            self.parentId = loginMessage[Messages.FIELD_PARENT_PROC]
            LOG("Processed parent procedure " + repr(self.parentId))
        else:
            LOG("No parent procedure id is set")
        

    #==========================================================================
    def setupProcedures(self):
        """
        ------------------------------------------------------------------------
        Setup procedure manager before the procedure execution 
        ------------------------------------------------------------------------
        """
        try:
            LOG("Setting up procedure manager")
            ProcedureManager.instance().setup(ctxName)
        except SpellException,ex:
            traceback.print_exc( file = sys.stderr )
            LOG("Could not setup procedure manager: " + repr(ex), LOG_ERROR)
            self.errorMessage = ex.message
            self.errorReason  = ex.reason
            raise ex

    #==========================================================================
    def setupResources(self):
        """
        ------------------------------------------------------------------------
        Setup all resources before the procedure execution 
        ------------------------------------------------------------------------
        """
        if self.initialized == True: return
        try:
            LOG("Configuring driver for context " + repr(self.contextName))
            DriverManager.instance().setup(self.contextName)
            self.initialized = True
            
        except SpellException,ex:
            traceback.print_exc( file = sys.stderr )
            LOG("Could not setup driver: " + repr(ex), LOG_ERROR)
            self.errorMessage = ex.message
            self.errorReason  = ex.reason
            raise ex

    #==========================================================================
    def cleanResources(self, shuttingDown = False ):
        """
        ------------------------------------------------------------------------
        Cleanup all resources after the procedure execution 
        ------------------------------------------------------------------------
        """
        try:
            if self.initialized == False: return
            LOG("Cleaning up driver")
            DriverManager.instance().cleanup( shutdown = shuttingDown )
            self.initialized = False
        except SpellException,ex:
            traceback.print_exc( file = sys.stderr )
            LOG("Could not cleanup driver: " + repr(ex), LOG_ERROR)
            self.errorMessage = ex.message
            self.errorReason  = ex.reason
            if not shuttingDown: raise ex

    #==========================================================================
    def cleanup(self, executionOnly = False ):
        """
        ------------------------------------------------------------------------
        Perform the cleanup. If executionOnly is True, only the execution
        thread is stopped. This implies that the execution context is reset,
        and this is used tipically for reloading. If executionOnly is False,
        an entire cleanup is performed, including the SPELL driver.  
        ------------------------------------------------------------------------
        """
        LOG("Stopping execution")
        if not executionOnly:
            LOG("Stopping client interface")
            ClientIF.cleanup()
        self.thread.stop()
        self.thread.join()
        self.cleanResources(not executionOnly)
        if not executionOnly:
            LOG("Executor finished")
            self.__closeLock.put(1)

    #==========================================================================
    def waitForClose(self):
        LOG("Waiting for close")
        self.__closeLock.get(True)

    #==========================================================================
    def connectionLost(self, contextKey):
        """
        ------------------------------------------------------------------------
        Called when the connection with the context is lost. 
        ------------------------------------------------------------------------
        """
        LOG("Lost connection with context")
        ClientIF.cleanup( force = True )
        LOG("Pausing executor")
        self.pause()
        LOG("Paused")

    #==========================================================================
    def getAsRunFile(self):
        """
        ------------------------------------------------------------------------
        Obtain the absolute path to the AsRun file. 
        ------------------------------------------------------------------------
        """
        return ClientIF.getAsRun()

    #==========================================================================
    def getEnvironment(self):
        return self.thread.getEnvironment()

    #==========================================================================
    def getLogFile(self):
        """
        ------------------------------------------------------------------------
        Obtain the absolute path to the Log file. 
        ------------------------------------------------------------------------
        """
        return LOG.getLogFile().name

    #==========================================================================
    def processLock(self):
        """
        ------------------------------------------------------------------------
        Activate the language processing protection in the controller. Used
        by language helpers to indicate that no command can be processed 
        meanwhile they are working. 
        ------------------------------------------------------------------------
        """
        self.controller.executionLock()

    #==========================================================================
    def processUnlock(self):
        """
        ------------------------------------------------------------------------
        Deactivate the language processing protection in the controller. Used
        by language helpers to indicate that commands can be processed 
        again.
        ------------------------------------------------------------------------
        """
        self.controller.executionUnlock()

    #==========================================================================
    def startWait(self, checkCallback = None, period = 0.5 ):
        """
        ------------------------------------------------------------------------
        Start a wait condition processing in the executor. If no callback is
        given, the executor simply goes to WAITING status. If a callback is
        provided, it is called periodically to check wether the condition
        (whatever it is) is fulfilled or not (this is indicated by returning
        True or False in the callback, respectively). Once the condition
        is fulfilled, the WAITING status is automatically reset.
        ------------------------------------------------------------------------
        """
        self.scheduler.startWait(checkCallback,period)

    #==========================================================================
    def wait(self):
        """
        ------------------------------------------------------------------------
        Wait for a condition to be fullfilled. This call will block the caller
        until the condition check is finished, whatever the result is. 
        ------------------------------------------------------------------------
        """
        self.scheduler.wait()

    #==========================================================================
    def isWaiting(self):
        """
        ------------------------------------------------------------------------
        Check if there is a condition check ongoing in the executor.
        ------------------------------------------------------------------------
        """
        return self.scheduler.isWaiting()

    #==========================================================================
    def finishWait(self):
        """
        ------------------------------------------------------------------------
        Finish a condition check in the executor
        ------------------------------------------------------------------------
        """
        self.scheduler.finishWait()

    #==========================================================================
    def abort(self):
        """
        ------------------------------------------------------------------------
        Abort the execution of the procedure. Tipically used by SPELL language
        wrappers.
        ------------------------------------------------------------------------
        """
        LOG("Aborting execution")
        self.controller.abort()

    #==========================================================================
    def pause(self):
        """
        ------------------------------------------------------------------------
        Pause the execution of the procedure. Tipically used by SPELL language
        wrappers.
        ------------------------------------------------------------------------
        """
        cmd = MsgHelper.createMessage(Messages.CMD_PAUSE)
        self.mailbox.push( cmd, high_priority = True )

    #==========================================================================
    def script(self, code):
        """
        ------------------------------------------------------------------------
        Execute the given script in the current context
        ------------------------------------------------------------------------
        """
        LOG("Executing script " + repr(code))
        cmd = MsgHelper.createMessage(Messages.CMD_SCRIPT)
        cmd[Messages.FIELD_SCRIPT] = code
        cmd[Messages.FIELD_FORCE] = True
        self.mailbox.push( cmd, high_priority = False )

    #==========================================================================
    def stage(self, id, title = None):
        """
        ------------------------------------------------------------------------
        Called whenever a Step is found
        ------------------------------------------------------------------------
        """
        if id != self.lastStage:
            if title:
                REGISTRY['CIF'].write('Step %s: %s' % (id, title))
                self.callstack.setStage(id,title)
            else:
                REGISTRY['CIF'].write('Step %s' % id)
                self.callstack.setStage(id," ")
    
            if self.stepByStep:
                cmd = MsgHelper.createMessage(Messages.CMD_PAUSE)
                self.mailbox.push( cmd, high_priority = True )
            self.lastStage = id

    #===========================================================================
    def processMessage(self, msg):
        """
        ------------------------------------------------------------------------
        Process an incomming message from the clients.
        ------------------------------------------------------------------------
        """
        LOG("Received message: " + msg.getId())
        
        # Process messages incoming from child executor, if any
        procId = msg[FIELD_PROC_ID]
        if procId != self.procId:
            if self.childManager.hasChild():
                self.childManager.processChildMessage(msg)
            else:
                LOG("Unexpected child message: " + msg.getId(), LOG_ERROR)
        elif msg.getType() == MSG_TYPE_COMMAND:
            if msg.getId() == Messages.MSG_ADD_CLIENT:
                self.addClient(msg)
            elif msg.getId() == Messages.MSG_REMOVE_CLIENT:
                self.removeClient(msg)
            elif msg.getId() == Messages.CMD_CLOSE:
                self.cleanup()
            elif msg.getId() == Messages.CMD_RELOAD:
                REGISTRY['CIF'].clearAsRun()
                self.cleanup( executionOnly = True )
                self.setupResources()
                self.prepareExecution()
            else:
                cmdId = msg["Id"]
                if cmdId in [ Messages.CMD_ABORT, Messages.CMD_PAUSE ]:
                    self.mailbox.push( msg, high_priority = True )
                else:
                    self.mailbox.push( msg )
        else:
            LOG("Unexpected message: " + msg.getId() + "/" + msg.getType(), LOG_ERROR)
    
    #===========================================================================
    def processRequest(self, msg):
        """
        ------------------------------------------------------------------------
        Process an incomming request from the clients.
        ------------------------------------------------------------------------
        """
        LOG("Received request: " + msg.getId())
        response = None
        if msg.getId() == Messages.REQ_SET_CONFIG:
            response = self.processSetConfig(msg)
        elif msg.getId() == Messages.REQ_GET_CONFIG:
            response = self.processGetConfig(msg)
        elif msg.getType() == MSG_TYPE_NOTIFY and (self.childManager.hasChild()):
            response = self.childManager.processChildRequest(msg)
        else:
            LOG("Unexpected request: " + msg.getId(), LOG_ERROR)
            sys.stderr.write("\nUnexpected request:\n" + msg.getId() + "\n")
            response = MsgHelper.createError(msg.getId(), msg, "Cannot process", "Unknown request")
        return response

    #===========================================================================
    def getStackPosition(self):
        """
        ------------------------------------------------------------------------
        Obtain the current stack position.
        ------------------------------------------------------------------------
        """
        return self.callstack.getStack()

    #===========================================================================
    def getStatus(self):
        """
        ------------------------------------------------------------------------
        Obtain the current execution status.
        ------------------------------------------------------------------------
        """
        return self.controller.getStatus()

    #===========================================================================
    def getCondition(self):
        """
        ------------------------------------------------------------------------
        Obtain the current wait condition if any
        ------------------------------------------------------------------------
        """
        return self.controller.getCondition()

    #===========================================================================
    def getControllingHost(self):
        """
        ------------------------------------------------------------------------
        Obtain the host of the controlling client. 
        ------------------------------------------------------------------------
        """
        if len(self.controllingClient) > 0:
            return self.controllingClient.values()[0]
        else:
            return None
    
    #===========================================================================
    def getMonitoringHosts(self):
        """
        ------------------------------------------------------------------------
        Obtain the host of the monitoring clients.
        ------------------------------------------------------------------------
        """
        return self.monitoringClients.values()
    
    #===========================================================================
    def addClient(self, msg):
        """
        ------------------------------------------------------------------------
        Add a client to the executor
        ------------------------------------------------------------------------
        """
        guiControlClientId = msg[Messages.FIELD_GUI_CONTROL]
        if guiControlClientId != None:
            self.controllingClient.clear()
            self.controllingClient[guiControlClientId] = msg[Messages.FIELD_GUI_CONTROL_HOST]
            LOG("Set a new controlling client: " + repr(guiControlClientId) + " - " +
                repr(self.controllingClient[guiControlClientId]))
        guiMonitoringClientId = msg[Messages.FIELD_GUI_LIST]
        # This list only contain one client reference
        if guiMonitoringClientId != None:
            self.monitoringClients[guiMonitoringClientId] = msg[Messages.FIELD_GUI_HOST_LIST]
            LOG("Added a new monitoring client: " + repr(guiMonitoringClientId) + " - " +
                repr(self.monitoringClients[guiMonitoringClientId]))

    #===========================================================================
    def removeClient(self, msg):
        """
        ------------------------------------------------------------------------
        Remove a client from the executor
        ------------------------------------------------------------------------
        """
        guiControlClientId = msg[Messages.FIELD_GUI_CONTROL]
        if guiControlClientId != None:
            self.controllingClient.clear()
            LOG("Removed the controlling client: " + repr(guiControlClientId) + " - " +
                repr(msg[Messages.FIELD_GUI_CONTROL_HOST]))
        guiMonitoringClientId = msg[Messages.FIELD_GUI_LIST]
        if self.monitoringClients.has_key(guiMonitoringClientId):
            del self.monitoringClients[guiMonitoringClientId]
            LOG("Removed the monitoring client: " + repr(guiMonitoringClientId) + " - " +
                repr(msg[Messages.FIELD_GUI_HOST_LIST]))

    #===========================================================================
    def processSetConfig(self, msg):
        """
        ------------------------------------------------------------------------
        Process a configuration change request
        ------------------------------------------------------------------------
        """
        try:
            #---------------------------------------------------------------
            # RunInto config
            #---------------------------------------------------------------
            value = eval(msg[RunInto])
            if value is not None and type(value)==bool:
                self.runInto = value
                if value == True:
                    LOG("Enabled RunInto")
                    self.controller.enableRunInto()
                elif value == False:
                    LOG("Disabled RunInto")
                    self.controller.disableRunInto()
            #---------------------------------------------------------------
            # ExecDelay config
            #---------------------------------------------------------------
            value = eval(msg[ExecDelay])
            if value is not None and type(value) in [int,float]:
                LOG("Set execution delay: " + repr(value))
                self.execDelay = value
                self.controller.setExecutionDelay(value)
            #---------------------------------------------------------------
            # ByStep config
            #---------------------------------------------------------------
            value = eval(msg[ByStep])
            if value is not None and type(value)==bool:
                LOG("Set step-by-step: " + repr(value))
                self.stepByStep = value
        except BaseException,ex:
            LOG("Could not parse configuration: " + repr(cfg), LOG_ERROR)
        resp = MsgHelper.createResponse(Messages.RSP_SET_CONFIG, msg)
        #TODO: send notification EXECUTOR CONFIGURED
        return resp
        
    #===========================================================================
    def processGetConfig(self, msg):
        """
        ------------------------------------------------------------------------
        Process a configuration retrieve request
        ------------------------------------------------------------------------
        """
        resp = MsgHelper.createResponse(Messages.RSP_GET_CONFIG, msg)
        resp[RunInto]   = self.runInto
        resp[ExecDelay] = self.execDelay
        resp[ByStep]    = self.stepByStep
        return resp

    #===========================================================================
    def setExecutionDelay(self, delay):
        """
        ------------------------------------------------------------------------
        Used by the language to change the execution delay
        ------------------------------------------------------------------------
        """
        LOG("Set execution delay: " + repr(delay))
        self.execDelay = delay
        self.controller.setExecutionDelay(delay)
        #TODO: send notification EXECUTOR CONFIGURED

    #===========================================================================
    def setRunInto(self, enabled):
        """
        ------------------------------------------------------------------------
        Used by the language to change the run-into flag
        ------------------------------------------------------------------------
        """
        if enabled == True:
            LOG("Enabled RunInto")
            self.controller.enableRunInto()
        elif enabled == False:
            LOG("Disabled RunInto")
            self.controller.disableRunInto()
        #TODO: send notification EXECUTOR CONFIGURED

    #===========================================================================
    def setStepByStep(self, enabled):
        """
        ------------------------------------------------------------------------
        Used by the language to change the by-step flag
        ------------------------------------------------------------------------
        """
        if enabled == True:
            LOG("Enabled ByStep")
            self.stepByStep = True
        elif enabled == False:
            LOG("Disabled ByStep")
            self.stepByStep = False
        #TODO: send notification EXECUTOR CONFIGURED

    #===========================================================================
    def setBrowsableLib(self, enabled):
        """
        ------------------------------------------------------------------------
        Used by the language to change the browsablelib flag
        ------------------------------------------------------------------------
        """
        if enabled == True:
            LOG("Enabled browsable lib")
            self.thread.setBrowsableLib(True)
        elif enabled == False:
            LOG("Disabled browsable lib")
            self.thread.setBrowsableLib(False)
        #TODO: send notification EXECUTOR CONFIGURED

    #==========================================================================
    def openSubProcedure(self, procId, arguments = None, config = {} ):
        
        if self.childManager.hasChild():
            raise CoreException("Cannot open subprocedure","Child procedure already under control")
        
        return self.childManager.openChildProcedure( procId, arguments, config )

    #==========================================================================
    def closeSubProcedure(self):
        if self.childManager.hasChild():
            return self.childManager.closeChildProcedure()
        return True

    #==========================================================================
    def killSubProcedure(self):
        if self.childManager.hasChild():
            return self.childManager.killChildProcedure()
        return True

    #==========================================================================
    def isChildFinished(self):
        return (self.childManager.getChildStatus() == FINISHED)

    #==========================================================================
    def isChildError(self):
        return (self.childManager.getChildStatus() in [ERROR,ABORTED])

    #==========================================================================
    def getChildStatus(self):
        return self.childManager.getChildStatus()

    #==========================================================================
    def getChildError(self):
        return self.childManager.getChildError()

################################################################################

################################################################################
if __name__ == "__main__":

    try:
        # Process the command line arguments
        shortopts = 'n:c:p:s:u'
        options, trailing = getopt.getopt(sys.argv[1:], shortopts)
        
        configFile = None
        procId = None
        ctxName = None
        ctxPort = None
        for option,value in options:
            if option == '-c':
                configFile = value
            elif option == '-p':
                procId = value
            elif option == '-n':
                ctxName = value
            elif option == '-s':
                ctxPort = int(value)
                
        # Configuration file path
        if configFile is None:
            sys.stderr.write("ERROR: no configuration file given\n")
            sys.stderr.write("  Use argument: -c <path to file>\n")
            sys.exit(1)

        # Procedure identifier
        if procId is None:
            sys.stderr.write("ERROR: no procedure ID given\n")
            sys.stderr.write("  Use argument: -p <id>\n")
            sys.exit(1)

        # Context name
        if ctxName is None:
            sys.stderr.write("ERROR: no context name given\n")
            sys.stderr.write("  Use argument: -n <name>\n")
            sys.exit(1)

        # Context port
        if ctxPort is None:
            sys.stderr.write("ERROR: no context port given\n")
            sys.stderr.write("  Use argument: -s <port>\n")
            sys.exit(1)

        # Generate an unique time id for this executor process
        timeID = time.strftime('%Y-%m-%d_%H%M%S')
        
        logname = procId.replace("\\","_").replace("/","_")
        LOG.setLogFile( "SPEL_Executor_" + logname, timeID )
        
        # Load the configuration file
        Config.instance().load(configFile)
        if not Config.instance().validate():
            sys.stderr.write("ERROR: please check configuration file\n")
            sys.exit(1)
            
        # Set the log level
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
            
        try:
            # Setup the executor
            EXEC = Executor()
            EXEC.setup(procId,ctxName,ctxPort,True,timeID)

            sys.stderr.write("*********************************************\n")
            sys.stderr.write("             SPEL Executor Ready \n")
            sys.stderr.write("             PROC   : " + procId + "\n")
            sys.stderr.write("*********************************************\n")
            
            EXEC.waitForClose()
            
            sys.exit(0)
            
        except ProcError,ex:
            sys.stderr.write("*********************************************\n")
            sys.stderr.write(" ERROR: procedure " + repr(procId) + " not found\n")
            sys.stderr.write("*********************************************\n")
            LOG("Error: procedure " + repr(procId) + " not found", severity = LOG_ERROR)
            sys.exit(1)
        
        except SpellException,ex:
            sys.stderr.write("*********************************************\n")
            sys.stderr.write("             SPEL Executor Failed \n")
            sys.stderr.write("             PROC   : " + procId + "\n")
            sys.stderr.write("             ERROR  : " + ex.message + "\n")
            sys.stderr.write("             REASON : " + ex.reason + "\n")
            sys.stderr.write("*********************************************\n")
            
    except SystemExit,ex:
        os.write(sys.stderr.fileno(),"*********************************************\n")
        os.write(sys.stderr.fileno(),"             SPEL Executor Closed \n")
        os.write(sys.stderr.fileno(),"*********************************************\n")
        sys.exit(0)
    except BaseException,ex:
        sys.stderr.write("==================================================\n")
        sys.stderr.write("UNHANDLED EXCEPTION IN EXECUTOR:\n ")
        sys.stderr.write(ex.message + "\n")
        sys.stderr.write(repr(ex) + "\n")
        traceback.print_exc( file = sys.stderr )
        sys.stderr.write("==================================================\n")
        sys.exit(1)

