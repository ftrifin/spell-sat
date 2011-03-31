################################################################################

"""
PACKAGE 
    server.executor.execthread
FILE
    execthread.py
    
DESCRIPTION
    Execution thread for procedures
    
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
from spell.lib.adapter.value import *
from spell.lang import *
from spell.utils.log import *
from spell.lib.adapter.constants.core import *
from server.core.messages.base import *
from spell.lang.functions import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.exception import *
from spell.lib.registry import *
from spell.config.reader import *
from spell.lib.exception import CoreException,SpellException
from server.procedures.manager import *
from spell.lib.goto import GotoMgr,GotoClass

#*******************************************************************************
# Local Imports
#*******************************************************************************
import status
from controller import ExecutionAborted,ExecutionFailed
from importchecker import ImportChecker
from initstepchecker import InitStepChecker
from procdata import ProcData

#*******************************************************************************
# System Imports
#*******************************************************************************
import threading,types,os.path,traceback,os
import sys,string,time
from inspect import isfunction
from inspect import ismodule

#*******************************************************************************
# Module globals
#*******************************************************************************
IsProcDir = ProcedureManager.instance().isProcDir
GetProcId = ProcedureManager.instance().getProcId

CheckAborted = None
CheckInit = None
CheckImport = None
SetupEnvironment = None

################################################################################
class ExecThread(threading.Thread):
    
    # Holds the reference to the executor
    executor = None
    # Holds the driver name
    driverName = None
    # Holds the procedure identifier
    procId = None
    # Holds the reference to the controller
    controller = None
    # Holds the import checker 
    importchecker = None
    # Holds the callstack manager
    callstack = None
    # Holds the reference to the scheduler
    scheduler = None
    # Holds the procedure arguments
    arguments = {}
    # Holds the current frame
    currentFrame = None
    # Holds the current line
    currentLine = None
    # Holds the user library path
    libpath  = None
    # Library cache
    libcache = []
    # Is library browsable or a black box?
    isbrowsablelib = False
    # Execution environment imported globals
    exec_environment = {}
    # Holds the initialization error 
    error = None
    # Reference to globals
    global_ctx = {}
    # Copy of initial global keys
    initial_ctx = []
    # Step INIT checker
    initstepchecker = None
    checkinitstep = True

    #===========================================================================
    def __init__(self, executor):
        threading.Thread.__init__(self)
        self.executor = executor
        self.__resetObjects()
        
        isbrowsablelib = False
        cfg = Config.instance().getContextConfig(executor.contextName).getExecutorConfig()
        
        if cfg.has_key('BrowsableLib'):
            isbrowsablelib = cfg['BrowsableLib']
        else:
            isbrowsablelib = False

	self.spellhome = os.getenv("SPELL_HOME")

        # Append the procedure paths to the python path
        LOG("Adding procedure base path")
        self.libpath  = ProcedureManager.instance().getLibPath()
        self.isbrowsablelib = isbrowsablelib
        self.libcache = []
        
        LOG("Library is browsable: " + repr(self.isbrowsablelib))

        try:
            # Compile the procedure code
            LOG("Loading code for " + repr(self.procId))
            self.__bytecode = ProcedureManager.instance().getCode(self.procId)

            # Reset the goto mechanism
            LOG("Resetting goto mechanism")
            GotoMgr.instance().reset()
            
            # Obtain the driver name
            self.driverName = Config.instance().getContextConfig(executor.contextName).getDriver()

            # Setup global objects
            self.global_ctx = globals()
            
            # Initialize the execution environment
            LOG("Initializing execution environment")
            self.__setupExecutionEnvironment( self.exec_environment, True )
            
            # Now update globals
            LOG("Updating globals")
            self.__setupExecutionEnvironment( self.global_ctx )
            LOG("Execution environment ready")
            
            # Store the initial context for reset on reload
            self.initial_ctx = self.global_ctx.keys()[:]
            
            LOG("Ready")
        except CoreException, ce:
            self.__bytecode = None    
            self.error = ce
            LOG(str(ce))
            LOG("Setup failed (Core Exception)")
        except BaseException,ex:
            self.__bytecode = None    
            self.error = CoreException("Compilation Error", str(ex))
            LOG(str(ex))
            LOG("Setup failed")

    #===========================================================================
    def __resetObjects(self):
        """
        ------------------------------------------------------------------------
        Initialize attributes
        ------------------------------------------------------------------------
        """
        self.procId = self.executor.procId
        self.controller = None
        self.driverName = None
        self.callstack = None
        self.scheduler = None
        self.currentFrame = None
        self.currentLine = None
        self.arguments = {}
        self.libcache = []
        self.exec_environment = {}
        self.error = None

    #===========================================================================
    def __setupExecutionEnvironment(self, environment, initialize = False ):
        """
        ------------------------------------------------------------------------
        Add the global objects, language and interfaces to the given environment
        ------------------------------------------------------------------------
        """
        if initialize:
            LOG("Importing user libraries (%s)" % repr(self.libpath))
            if self.libpath is not None and len(self.libcache)==0:
                for libpath in self.libpath:
                    if os.path.exists(libpath):
                        for item in os.listdir(libpath):
                            if not item.endswith('.py'): continue
                            name = item[:-3]
                            self.libcache += [ "from %s import *" % name ]
            for name in self.libcache:
                try:
                    exec(name, environment)
                    LOG("Imported " + name)
                except: 
                    LOG("Could not import " + name, LOG_ERROR)
    
            # Add the objects to globals
            LOG("Importing interfaces")
            
            ifcs = ['TM','TC','EV','RSC','TASK','USER','DBMGR']
            for ifc in ifcs:
                if REGISTRY.exists(ifc):
                    environment[ifc] = REGISTRY[ifc]

            environment['SCDB']  = REGISTRY['DBMGR']['SCDB']
            environment['GDB']   = REGISTRY['DBMGR']['GDB']
            
            #ProcData attributes
            name = self.executor.procId
            args = self.executor.arguments
            mode = self.executor.openMode
            parent = self.executor.parentId
            environment['PROC']  = ProcData(name, args,mode, parent)
            
            LOG("Importing language")
            exec("from spell.lang.functions import *", environment)
            exec("from spell.lang.constants import *", environment)
            exec("from spell.lang.modifiers import *", environment)
            exec("from spell.lang.user import *", environment)
            exec("from spell.lib.goto import *", environment)
            exec("from spell.utils.ttime import *", environment)
            exec("from math import *", environment)
    
            LOG("Importing driver specifics")
            
            try:
                import constants
                for c in dir(constants):
                    if not c.startswith("__"):
                        exec("from constants import " + c, environment)
            except BaseException,ex:
                print "ERROR: ",ex
            
            try:
                import modifiers
                for c in dir(modifiers):
                    if not c.startswith("__"):
                        exec("from modifiers import " + c, environment)
            except BaseException,ex:
                print "ERROR: ",ex
            
        else:
            environment.update(self.exec_environment)

    #===========================================================================
    def getEnvironment(self):
        return self.exec_environment

    #===========================================================================
    def setController(self, controller):
        """
        ------------------------------------------------------------------------
        Assign the controller reference
        ------------------------------------------------------------------------
        """
        global CheckAborted
        self.controller = controller
        CheckAborted = self.controller.checkAborted
        self.controller.setThread(self)

    #===========================================================================
    def setCallstack(self, callstack):
        """
        ------------------------------------------------------------------------
        Assign the call stack manager reference
        ------------------------------------------------------------------------
        """
        self.callstack = callstack

    #===========================================================================
    def setScheduler(self, scheduler):
        """
        ------------------------------------------------------------------------
        Assign the scheduler reference
        ------------------------------------------------------------------------
        """
        self.scheduler = scheduler

    #===========================================================================
    def setArguments(self, arguments):
        """
        ------------------------------------------------------------------------
        Assign the procedure arguments
        ------------------------------------------------------------------------
        """
        self.arguments = arguments

    #===========================================================================
    def setBrowsableLib(self, enabled):
        """
        ------------------------------------------------------------------------
        Changes the browsable library flag
        ------------------------------------------------------------------------
        """
        self.isbrowsablelib = enabled

    #===========================================================================
    def stop(self):
        """
        ------------------------------------------------------------------------
        Stop the controller
        ------------------------------------------------------------------------
        """
        # Reset globals
        for key in self.global_ctx.keys()[:]:
            if not key in self.initial_ctx:
                self.global_ctx.pop(key)
        # Abort the thread
        self.controller.abort()
        # Stop the controller loop
        self.controller.stop()
                
    #===========================================================================
    def run(self):
        """
        ------------------------------------------------------------------------
        Main execution thread
        ------------------------------------------------------------------------
        """
        global CheckImport
        global CheckInit
        global SetupEnvironment
        
        # Assign the main dispatcher method
        sys.settrace(self.trace_dispatch)
        # Create the import status checker
        self.importchecker = ImportChecker()
        self.initstepchecker = InitStepChecker(self.procId)
        self.checkinitstep = True
        
        # Optimization
        CheckImport = self.importchecker.checkImporting
        CheckInit   = self.initstepchecker.checkInitStep
        SetupEnvironment = self.__setupExecutionEnvironment
        
        reExecute = True  # FIXME: to be used in error recovery
        repeatCount = 0   # FIXME: to be used in error recovery
        while reExecute:
            reExecute = False
            try:
                if not self.__bytecode:
                    raise CoreException( self.error.message, self.error.reason )

                # Put any passed argument in globals
                self.global_ctx['ARGS'] = self.arguments
              
                # Start the execution controller first
                self.controller.start()
                
                # Actually launch the procedure
                exec self.__bytecode
                
                self.controller.setFinished()
                
            except CoreException,ex:
                sys.stderr.write("\n\n####################\n")
                sys.stderr.write("COMPILATION ERROR " + str(ex) + "\n")
                sys.stderr.write("####################\n\n")
                self.controller.codeException(ex)
            except SpellException,ex:
                sys.stderr.write("\n\n####################\n")
                sys.stderr.write("SPELL EXCEPTION\n")
                sys.stderr.write("####################\n\n")
                self.controller.raiseException(ex)
                traceback.print_exc()
                LOG("Error in driver, exiting thread")
            except ExecutionAborted,ex:
                sys.stderr.write("\n\n####################\n")
                sys.stderr.write("ABORTED\n")
                sys.stderr.write("####################\n\n")
                LOG("Execution aborted, exiting thread")
            except ExecutionFailed,ex:
                sys.stderr.write("\n\n####################\n")
                sys.stderr.write("FAILED\n")
                sys.stderr.write("####################\n\n")
                LOG("Execution failed, exiting thread")
            except Exception,ex:
                sys.stderr.write("\n\n####################\n")
                sys.stderr.write("UNCAUGHT EXCEPTION\n")
                sys.stderr.write("####################\n\n")
                self.controller.raiseException(ex)
                traceback.print_exc()
        
        self.controller.setProcEndTime()

        sys.settrace(None)

        return

    #===========================================================================
    def trace_dispatch(self, frame, event, args):
        """
        ------------------------------------------------------------------------
        Main dispatcher method
        ------------------------------------------------------------------------
        """
        
        # Obtain the procedure file path
        path = frame.f_code.co_filename

        # FIXME put this in a single check function
        # Optimization: do not trace in these paths
        if "python" in path: return self.trace_dispatch
        if self.spellhome + os.sep + "server" in path: return self.trace_dispatch
        if self.spellhome + os.sep + "spell"  in path: return self.trace_dispatch
        if path == "<string>": return self.trace_dispatch
        
        if "UserLib" in path and event == 'call': 
            SetupEnvironment(frame.f_globals)
            if not self.isbrowsablelib: return self.trace_dispatch
        
        # Check the aborted condition
        CheckAborted()

        if not IsProcDir(path) and not (self.isbrowsablelib and "UserLib" in path): 
            return self.trace_dispatch

        #FIXME this does not work if Goto is at the very end of the procedure
        # to be tested: make Goto a normal function which calls EXEC.goLabel
        # the label is retrieved from the GotoMgr which parses the proc in 
        # advance.
        GotoClass.trace_dispatch(frame, event, args)

        # Store the current frame and update locals
        self.__updateFrame(frame,event)
        
        # Extract relevant values
        name = frame.f_code.co_name
        lineno = frame.f_lineno

        # FIXME the following InitStep processing check might be misplaced.
        if self.checkinitstep:
            res = CheckInit(event, path, lineno, name, frame, args)
            if res == False: return self.trace_dispatch
            if res is not None: self.checkinitstep = False
        
        # We do not want to process during import statements
        if CheckImport(event, path, lineno, name, frame, args): 
            return self.trace_dispatch

        # We need this while in order to recall the stack/controller dispatchers
        # after calculating a jump in the execution (skip or goto), so that
        # the clients are notified with the proper stack position. Returning
        # from trace_dispatch right after doing the jump would result on the
        # unwanted execution of the target line.
        repeat = True
        notifyCode = False
        while repeat: 

            # Normally don't reexecute (only for jumps)
            repeat = False
            procId = GetProcId(path)

            # Call the stack manager: the call stack is kept up to date 
            if event == "line":
                self.callstack.event_line( procId, lineno, name, frame, args )
            elif event == "call":
                self.callstack.event_call( procId, lineno, name, frame, args )
            elif event == "return":
                self.callstack.event_return( procId, lineno, name, frame, args )

            # Recheck the aborted flag 
            CheckAborted()
            
            # Now the controller will determine the execution flow
            # depending on the mode (play, stepping, paused) and will set
            # the procedure status.
            if event == "line":
                repeat = self.controller.event_line( path, lineno, name, frame, args )
            elif event == "call":
                self.controller.event_call( path, lineno, name, frame, args )
            elif event == "return":
                self.controller.event_return( path, lineno, name, frame, args )
            # We need to re-acquire lineno always to take into account possible
            # skips and gotos done by the controller
            lineno = frame.f_lineno
            
            # Recheck the aborted flag 
            CheckAborted()
        # End-While ------------------------------------------------------------
            
        # Check if this is a call to external function. If so, we should
        # reimport the environment in the executor thread. 
        if (event =='call') and not self.callstack.isCurrentProc( path ): 
            SetupEnvironment(frame.f_globals)

        return self.trace_dispatch

    #===========================================================================
    def __updateFrame(self, frame, event):
        """
        ------------------------------------------------------------------------
        Update the current frame. If the frame changes, update the global 
        definitions with all function definitions to ease function calls in
        procedures. 
        ------------------------------------------------------------------------
        """
        if self.currentFrame is None: 
            self.currentFrame = frame
        elif (self.currentFrame != frame) and (event == 'call'):
            for key in self.currentFrame.f_locals.keys():
                local = self.currentFrame.f_locals[key]
                if isfunction(local) or ismodule(local):
                    frame.f_globals[key] = local
        self.currentFrame = frame

    #===========================================================================
    def waitSchedule(self, condition):
        """
        ------------------------------------------------------------------------
        Execute any scheduling condition
        ------------------------------------------------------------------------
        """
        LOG("Setting procedure launch condition: " + repr(condition))
        try:
            condition = eval(condition)
            if type(condition)!=dict:
                raise BaseException()

            config = condition.copy()
            config[Notify] = False
            config[HandleError] = False
            
            REGISTRY['CIF'].setManualMode(True)
            
            # Process telemetry conditions
            if condition.has_key('verify'):
                verify = config.pop('verify')
                REGISTRY['CIF'].write("Execution scheduled using telemetry condition: " + repr(verify))
                WaitFor( verify, config )
            # Process time conditions
            elif condition.has_key(Until):
                until_time = TIME(condition.pop(Until))
                REGISTRY['CIF'].write("Execution scheduled for time: " + str(until_time))
                WaitFor( Until=until_time, Notify=False )
            elif condition.has_key(Delay):
                delay_time = TIME(condition.pop(Delay))
                REGISTRY['CIF'].write("Execution scheduled during: " + str(delay_time))
                WaitFor( Delay=delay_time, Notify=False )
            else:
                raise BaseException()
            # Make controller go autorun
            return True
        except (BaseException,Exception),ex:
            REGISTRY['CIF'].setManualMode(False)
            LOG("Bad condition: %s" % str(ex))
            REGISTRY['CIF'].write("Could not schedule the procedure, bad condition: "+ repr(condition), {Severity:ERROR})
            REGISTRY['CIF'].write("Error is: "+ str(ex), {Severity:ERROR})
        finally:
            REGISTRY['CIF'].setManualMode(False)
        # Do not autorun
        return False

    #===========================================================================
    def goNextLine(self):
        """
        ------------------------------------------------------------------------
        Go to the next line in the execution flow. 
        ------------------------------------------------------------------------
        """
        # Get the corresponding line offsets from the code lnotab
        linesOffset = [ord(c) for c in self.currentFrame.f_code.co_lnotab[1::2]]
        # Index of the line offset currently used
        index = 0
        # Will hold the next line number
        nextLine = self.currentFrame.f_code.co_firstlineno
        # The line where we are at this moment
        currentLine = self.currentFrame.f_lineno
        
        # Position nextLine number on the current line offset
        while index < len(linesOffset) and nextLine <= self.currentFrame.f_lineno:
            nextLine = nextLine + linesOffset[index]
            index += 1
        
        # Try to set the next line incrementing by one. If the settline method
        # fails, it means that we are trying to go into a different block (if
        # else block, while block, try except block, etc. In that case we
        # continue skipping until we pass the block.
        repeat = True
        while repeat:
            try:
                if nextLine > self.currentFrame.f_lineno:
                    self.currentFrame.f_lineno = nextLine
                    self.currentLine = nextLine
                repeat = False
            except ValueError:
                nextLine += 1
        
        # If the lineno does not change, it means that we are in the last
        # available line of the current code block. Then we shall issue
        # a step on the dispatcher to go back to the previous code block
        # or to finish the procedure.
        if (currentLine == nextLine):
            return True
    
        # Otherwise, no step should be done
        return False

    #===========================================================================
    def goLine(self, lineno):
        """
        ------------------------------------------------------------------------
        Go to the given line in the procedure.
        ------------------------------------------------------------------------
        """
        try:
            self.currentFrame.f_lineno = lineno
            self.currentLine = lineno
        except Exception,ex:
            REGISTRY['CIF'].write( str(ex), {Severity:WARNING})
            REGISTRY['CIF'].write("Unable to go to line " + repr(lineno), {Severity:WARNING})
            return False
        return True

    #===========================================================================
    def goLabel(self, label):
        """
        ------------------------------------------------------------------------
        Go to the given step label in the procedure.
        ------------------------------------------------------------------------
        """
        try:
            lineno = GotoMgr.instance().getStepLine(label)
            title  = GotoMgr.instance().getStepTitle(label)
            self.currentFrame.f_lineno = lineno
            self.currentLine = lineno
            self.executor.stage(label,title)
        except Exception,ex:
            REGISTRY['CIF'].write( str(ex), {Severity:WARNING})
            REGISTRY['CIF'].write("Unable to go to label " + repr(label), {Severity:WARNING})
            return False
        return True

    #===========================================================================
    def runScript(self, script):
        """
        ------------------------------------------------------------------------
        Execute the given script in the procedure context.
        ------------------------------------------------------------------------
        """
        try:
            bytecode = compile(script,"script",'exec')
        except (BaseException,Exception),ex:
            REGISTRY['CIF'].write("Unable to compile script: " + str(ex), {Severity:WARNING})
            return False
        
        # The exec statement does not affect global values for those
        # variables already defined in that scope, but if a new variable
        # is defined in the script, this variable goes into globals as well.
        # Since we want the new variable to remain local, we shall compare
        # globals and remove it.
        
        # IMPORTANT: if a variable is declared as GLOBAL within a function,
        # modifying it within the scope of the function WILL affect the global
        # value anyway!!
        
        # We shall use global context to execute in order to have any 
        # variable declaration within the function scopes.

        # Make a copy of the original globals and code names
        frame = self.currentFrame
        names = set(frame.f_code.co_names)

        REGISTRY['CIF'].write("Executing script: " + repr(script))
        REGISTRY['CIF'].setManualMode(True)

        original_locals = frame.f_locals.keys()[:]
        locals = frame.f_locals.copy()
        
        try:
            # Execute the script
            exec bytecode in frame.f_globals,locals
            
            # Adapt the environment for variables:
            for key in locals.keys():
                # If the locally created key is in the frame locals, update it
                if key in frame.f_locals:
                    frame.f_locals[key] = locals[key]
                    
                # If the locally created key is in the frame globals
                elif key in frame.f_globals:
                    
                    # If it was originally in the frame locals, update the frame locals
                    if key in original_locals:
                        #TODO: this does not work! does not change the value!
                        #TODO: maybe we need to modify CO_NAMES here!
                        frame.f_locals[key] = locals[key]
                    # If it was not originally in locals change it in globals
                    # provided that it is not a constant in code names
                    # (we do not want to modify a global variable which is not
                    # declared as global)
                    elif not key in names:
                        frame.f_globals[key] = locals[key]
                        
                # If the locally created key is nowere in the frame, put it in globals
                else:
                    # If we are within a function declare it in locals
                    if self.callstack.getLevel()>1:
                        #TODO: this does not work! does not change the value!
                        #TODO: maybe we need to modify CO_NAMES here!
                        frame.f_locals[key] = locals[key]
                    else:
                        frame.f_globals[key] = locals[key]

        except (BaseException,Exception),ex:
            REGISTRY['CIF'].write("Unable to execute script: " + str(ex), {Severity:WARNING})
            return False
        finally:
            REGISTRY['CIF'].setManualMode(False)
        
        return True
        
