################################################################################

"""
PACKAGE 
    server.executor.controller
FILE
    controller.py
    
DESCRIPTION
    Executor controller
    
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
from spell.utils.ttime import *
from spell.lib.registry import *
from spell.lang.modifiers import Severity
from spell.lang.constants import WARNING
import server.core.messages.executor as Messages
from server.procedures.manager import ProcedureManager

#*******************************************************************************
# Local Imports
#*******************************************************************************
import mode, status

#*******************************************************************************
# System Imports
#*******************************************************************************
import thread,threading,sys,time,os

#*******************************************************************************
# Module globals
#*******************************************************************************

# Used to stop the controller
STOP_TAG='<STOP>'

################################################################################        
class ExecutionAborted(BaseException): pass

################################################################################        
class ExecutionFailed(BaseException): pass

################################################################################        
class Controller( threading.Thread ):
    
    # Execution mode
    __mode = mode.MODE_STEP
    # Execution lock
    __execLock = None
    # Controller lock. Used for blocking incomming commands while a language
    # sentence is being processed.
    __controllerLock = None
    # Execution status
    __status = status.UNINIT
    # Main proc filename
    __mainProc = None
    # Current proc filename
    __currentProc = None
    
    # Request to do step over flag
    __doStepOver = False
    # Doing step over flag
    __stepOver = False
    
    # Abort flag
    __abort = False
    __aborting = False
    # Error flag
    __error = False
    # AutoRun flag. If true, the controller goes to play directly at startup
    __autorun = False
    # Schedule condition
    __condition = None
    
    # Thread reference
    __thread = None
    # Call stack reference
    __callstack = None
    # Command mailbox
    __mailbox = None
    # Scheduler
    __scheduler = None
    
    # Moving line flag (skipping or goto on gui)
    __skipping = False
    
    # Execution delay
    __execDelay = 0

    # Procedure start time
    __procStartTime = None

    #===========================================================================
    def __init__(self):
        threading.Thread.__init__(self)
        self.reset()

    #===========================================================================
    def reset(self):
        """
        ------------------------------------------------------------------------
        Reset all attributes. 
        ------------------------------------------------------------------------
        """
        self.__mainProc = None
        self.__currentProc = None
        
        self.__execLock = threading.Event()
        self.__controllerLock = threading.Event()
        self.__controllerLock.set()

        self.__mode = mode.MODE_STEP
        self.__status = status.UNINIT
        self.__execDelay = 0
        
        self.__stepOver = False
        self.__doStepOver = False
        self.__skipping = False
        
        self.__abort = False
        self.__aborting = False
        self.__error = False
        self.__autorun = False
        self.__condition = None

        self.__thread = None
        self.__callstack = None
        self.__mailbox = None
        self.__sheduler = None

        self.__procStartTime = None
    
    #===========================================================================
    def _setStatus(self, st):
        """
        ------------------------------------------------------------------------
        Set the execution status. If the status has changed, notify it to
        clients. 
        ------------------------------------------------------------------------
        """
        newStatus = (st != self.__status)
        firstStatus = (self.__status == status.LOADED) and newStatus
        if firstStatus:
            REGISTRY['CIF'].write("Procedure ready")
        self.__status = st
        if newStatus or (st == status.WAITING):
            REGISTRY['CIF'].notifyStatus(st,self.getCondition())

    #===========================================================================
    def _setMode(self, mode):
        """
        ------------------------------------------------------------------------
        Set the execution mode (mode.MODE_PLAY or mode.MODE_STEP) 
        ------------------------------------------------------------------------
        """
        self.__mode = mode

    #===========================================================================
    def setCondition(self, condition):
        """
        ------------------------------------------------------------------------
        Assign the procedure launch condition
        ------------------------------------------------------------------------
        """
        self.__condition = condition

    #===========================================================================
    def setAutoRun(self):
        """
        ------------------------------------------------------------------------
        Set the autorun flag. This will make the procedure going to RUN
        directly after loading, instead of pausing it
        ------------------------------------------------------------------------
        """
        self.__autorun = True
        self.__mode = mode.MODE_PLAY

    #===========================================================================
    def enableRunInto(self):
        """
        ------------------------------------------------------------------------
        Make the call stack manager to disable the sticky flag
        ------------------------------------------------------------------------
        """
        self.__doStepOver = False
        self.__callstack.setPermanent(False)
        self.__callstack.unsetSticky()

    #===========================================================================
    def disableRunInto(self):
        """
        ------------------------------------------------------------------------
        Make the call stack manager to enable the sticky flag
        ------------------------------------------------------------------------
        """
        self.__doStepOver = True
        self.__callstack.setSticky()
        self.__callstack.setPermanent(True)

    #===========================================================================
    def checkAborted(self):
        """
        ------------------------------------------------------------------------
        Check if the execution has been aborted (see ExecThread::trace_dispatch) 
        ------------------------------------------------------------------------
        """
        if self.__abort:
            raise ExecutionAborted()

    #===========================================================================
    def isAborting(self):
        return self.__aborting
        
    #===========================================================================
    def getMode(self):
        """
        ------------------------------------------------------------------------
        Get the execution mode 
        ------------------------------------------------------------------------
        """
        return self.__mode
        
    #===========================================================================
    def getStatus(self):
        """
        ------------------------------------------------------------------------
        Get the execution status. 
        ------------------------------------------------------------------------
        """
        return self.__status

    #===========================================================================
    def getCondition(self):
        """
        ------------------------------------------------------------------------
        Get the waiting condition, if any 
        ------------------------------------------------------------------------
        """
        if self.__condition is None:
            return "N/A"
        else:
            cond = "???"
            if "verify" in self.__condition:
                idx1 = self.__condition.index("[")
                idx2 = self.__condition.rindex("]")
                cond = self.__condition[idx1:idx2]
            elif "Delay" in self.__condition or "Until" in self.__condition:
                idx1 = self.__condition.index(":")+1
                cond = self.__condition[idx1:-1]
            return cond

    #===========================================================================
    def setMailbox(self, mailbox ):
        """
        ------------------------------------------------------------------------
        Assign the command mailbox. The controller peeks the commands from it. 
        ------------------------------------------------------------------------
        """
        self.__mailbox = mailbox

    #===========================================================================
    def setScheduler(self, scheduler ):
        """
        ------------------------------------------------------------------------
        Assign the scheduler. Wait condition checks affect the execution flow. 
        ------------------------------------------------------------------------
        """
        self.__scheduler = scheduler

    #===========================================================================
    def setThread(self, thread ):
        """
        ------------------------------------------------------------------------
        Assign the execution thread. 
        ------------------------------------------------------------------------
        """
        self.__thread = thread

    #===========================================================================
    def setCallstack(self, callstack ):
        self.__callstack = callstack

    #===========================================================================
    def setExecutionDelay(self, delay):
        """
        ------------------------------------------------------------------------
        Set the execution delay 
        ------------------------------------------------------------------------
        """
        self.__execDelay = delay

    #===========================================================================
    def stop(self):
        """
        ------------------------------------------------------------------------
        Stop the controller. 
        ------------------------------------------------------------------------
        """
        self.__mailbox.push(STOP_TAG, high_priority = True)

    #==========================================================================
    def executionLock(self):
        """
        ------------------------------------------------------------------------
        Lock the language processing protection. When this lock is used, no 
        commands can be processed (commands coming from any external user, clients
        or language) 
        ------------------------------------------------------------------------
        """
        # We need this in order to keep control of the execution once the
        # procedure is aborted. Nothing shall be done until the
        # execution thread actually aborts.
        if self.__abort:
            self.__wait()
        self.__controllerLock.clear()

    #==========================================================================
    def executionUnlock(self):
        """
        ------------------------------------------------------------------------
        Unlock the language processing protection. 
        ------------------------------------------------------------------------
        """
        self.__controllerLock.set()

    #===========================================================================
    def run(self):
        """
        ------------------------------------------------------------------------
        Main thread of the controller. Pulls out commands from the mailbox,
        and processes them provided that the processing protection lock is not
        being used. 
        ------------------------------------------------------------------------
        """
        LOG("Starting command processing loop")
        while True:
            cmd = self.__mailbox.pull()
            if cmd == STOP_TAG: return
            self.__controllerLock.wait()

            if self.__procStartTime is None:
                self.setProcStartTime()
            
            if cmd.getId() == Messages.CMD_ABORT:
                self.abort()
            elif cmd.getId() == Messages.CMD_STEP:
                self.step( False )
            elif cmd.getId() == Messages.CMD_STEP_OVER:
                self.step( True )
            elif cmd.getId() == Messages.CMD_RUN:
                so_flag = cmd[Messages.FIELD_SO] 
                self.play( so_flag == "True" )
            elif cmd.getId() == Messages.CMD_SKIP:
                self.skip()
            elif cmd.getId() == Messages.CMD_GOTO:
                try:
                    # Is the target a line number?
                    isLabel = False  
                    target = cmd[Messages.FIELD_GOTO_LINE]
                    # Should be a label then
                    if target is None:
                        isLabel = True
                        target = cmd[Messages.FIELD_GOTO_LABEL]
                    else:
                        target = int(target)
                    if target is None:
                        REGISTRY['CIF'].write("Unable to get go to target", {Severity:WARNING})
                    else:
                        self.goto( target, isLabel )
                except BaseException,ex:
                    REGISTRY['CIF'].write("Unable to process go to command: " + str(ex), {Severity:WARNING})
            elif cmd.getId() == Messages.CMD_PAUSE:
                self.pause()
            elif cmd.getId() == Messages.CMD_SCRIPT:
                code = cmd[Messages.FIELD_SCRIPT]
                override = False
                try:
                    override = cmd[Messages.FIELD_FORCE]
                    if override: override = eval(override)
                except:
                    override = False
                self.script( code, override )
            else:
                LOG("Unrecognised command: " + cmd.getId(), LOG_ERROR)
        LOG("Stopping command processing loop")
    
    #===========================================================================
    def step(self, stepOver = False ):
        """
        ------------------------------------------------------------------------
        Implementation of the STEP command (including step over) 
        ------------------------------------------------------------------------
        """
        # Check the status condition
        exec_status = self.getStatus()
        if (exec_status != status.PAUSED) or (self.__error):
            return

        # Set the flag to activate the step over in the next dispatch event
        self.__doStepOver = stepOver
        
        # Restart a wait condition check timer if any
        self.__scheduler.restartWait()
        
        # Enable the sticky flag right away
        if stepOver:
            self.__callstack.setSticky()
            self.__callstack.setPermanent(False)
        else:
            self.__callstack.unsetSticky()

        # Go to stepping mode
        self._setMode(mode.MODE_STEP)
        
        # Let the dispatcher to continue processing now. The status PAUSED
        # is set in the next 'line' event by means of the __wait() method. 
        self.__continue()
    
    #===========================================================================
    def play(self, stepOver = False ):
        """
        ------------------------------------------------------------------------
        Implementation of the RUN command (including run over) 
        ------------------------------------------------------------------------
        """
        # Check the status condition
        exec_status = self.getStatus()
        if (exec_status != status.PAUSED) or (self.__error):
            return

        # Set the flag to activate the step over in the next dispatch event
        self.__doStepOver = stepOver
        
        # Restart a wait condition check timer if any
        self.__scheduler.restartWait()
        
        # Enable the sticky flag right away
        if stepOver:
            self.__callstack.setSticky()
            self.__callstack.setPermanent(True)
            
        # Set the mode and status right away
        self._setStatus(status.RUNNING)
        self._setMode(mode.MODE_PLAY)
        
        # Let the dispatcher to continue processing now.
        self.__continue()

    #===========================================================================
    def pause(self):
        """
        ------------------------------------------------------------------------
        Implementation of the PAUSE command  
        ------------------------------------------------------------------------
        """
        # Check the status condition
        exec_status = self.getStatus()
        if exec_status in [status.FINISHED,status.PAUSED,status.ABORTED,status.ERROR]:
            return
        
        # If there is a waiting condition check ongoing, cancel it. 
        # Abort it if it is the initial schedule
        if self.__condition:
            REGISTRY['CIF'].write("Execution schedule has been cancelled",{Severity:WARNING})
            self.__scheduler.abortWait( False )
        else:
            if self.__scheduler.interruptWait():
                REGISTRY['CIF'].write("Wait condition interrupted",{Severity:WARNING})
        
        self._setMode(mode.MODE_STEP)
        # If the procedure goes to pause mode, we want to override any
        # possible ongoing step over and go directly where the call stack points to
        self.__stepOver = False
        
    #===========================================================================
    def abort(self):
        """
        ------------------------------------------------------------------------
        Implementation of the ABORT command  
        ------------------------------------------------------------------------
        """
        
        self.__aborting = True 
        
        # Check the status condition
        exec_status = self.getStatus()
        if exec_status in [status.FINISHED,status.ABORTED]:
            return

        # If there is a waiting condition check ongoing, cancel it
        self.__scheduler.abortWait( False )

        # Set the aborted status
        self._setStatus(status.ABORTED)

        # Flag for raising the ExecutionAborted exception in the execution 
        # thread scope
        self.__abort = True

        # Let the dispatcher continue processing
        self.__continue()

    
    #===========================================================================
    def skip(self):
        """
        ------------------------------------------------------------------------
        Implementation of the ABORT command  
        ------------------------------------------------------------------------
        """
        # Check status condition
        if (self.getStatus() != status.PAUSED) or (self.__error):
            return

        # Cancel any interrupted wait condition if any. If there is actually
        # one wait timer cancelled, we don't want to set the skipping flag
        # a few lines below.
        waitAborted = self.__scheduler.abortWait( False )
        
        # Tell the execution thread to go on 1 line. Take into account the
        # case of returning from functions: in this case we simulate the
        # return by issuing one step. This is to be fixed later, since this
        # implies that we shall EXECUTE the last instruction in the funcion.
        # Because of this, the last instruction shall be a 'return' keyword
        # always. FIXME
        if self.__thread.goNextLine(): 
            self.step() 
        else:
            self.__skipping = (not waitAborted)
            self.__continue()

    #===========================================================================
    def goto(self, target, label = False ):
        """
        ------------------------------------------------------------------------
        Implementation of the GOTO-ON-GUI command  
        ------------------------------------------------------------------------
        """
        # Check the status condition
        if self.getStatus() != status.PAUSED:
            return
        
        # Set the skipping flag
        self.__skipping = True
        
        if label == True:
            success = self.__thread.goLabel(target)
        else:
            # Go to the given line. If success, let the dispatcher to continue 
            # processing.
            success = self.__thread.goLine(target)
        self.__continue()
        return success

    #===========================================================================
    def script(self, code, overrideStatus = False ):
        """
        ------------------------------------------------------------------------
        Implementation of the SCRIPT-ON-GUI command  
        ------------------------------------------------------------------------
        """
        # Check the status condition
        if not overrideStatus and (self.getStatus() not in [status.PAUSED,status.WAITING]):
            return
        # Run the given code on the procedure context.
        success = self.__thread.runScript(code)
        return success

    #===========================================================================
    def __wait(self):
        """
        ------------------------------------------------------------------------
        Implementation of the execution lock. This lock is used to manage
        stepping through the procedure instructions.  
        ------------------------------------------------------------------------
        """
        # Perform the abort/fail checks before blocking
        if self.__abort:
            raise ExecutionAborted()
        if self.__error:
            raise ExecutionFailed()
        # Set status to paused
        self._setStatus(status.PAUSED)
        # Move stack to the top and notify current position
        self.__callstack.toTop()
        # Wait to continue 
        self.__execLock.clear()
        self.__execLock.wait()

    #===========================================================================
    def __continue(self):
        """
        ------------------------------------------------------------------------
        Release the execution lock. This lock is used to manage
        stepping through the procedure instructions.  
        ------------------------------------------------------------------------
        """
        self.__execLock.set()

    #===========================================================================
    def event_line(self, file, line, name, frame, args):
        """
        ------------------------------------------------------------------------
        Dispatcher 'line' event processing. This event takes place each time
        a single line of the procedure is about to be executed.  
        ------------------------------------------------------------------------
        """
        # If we have a scheduling condition, wait for it
        if self.__condition:
            if not self.__thread.waitSchedule( self.__condition ):
                self.__autorun = False
                self.__mode = mode.MODE_STEP
            self.__condition = None
        
        # If we are aborted, block execution
        if self.__abort:
            
            self.__wait()
            
        elif not self.__aborting:
            # If we are not stepping over and the mode is stepping, use the
            # execution lock in order to hold the execution. Otherwise, wait
            # the execution delay and continue.
            if not self.__stepOver:
                if (self.getMode() == mode.MODE_STEP):
                    self.__wait()
                else:
                    time.sleep(self.__execDelay)
            else:
                self._setStatus(status.RUNNING)
    
            # Force the dispatcher to reexecute the event if we are skipping
            if self.__skipping:
                self.__skipping = False
                return True
            
            # Update the current procedure
            self.__currentProc = file
        
        return False

    #===========================================================================
    def event_call(self, file, line, name, frame, args):
        """
        ------------------------------------------------------------------------
        Dispatcher 'call' event processing. This event takes place each time
        a function is called. It is as well the first event that takes place
        when executing a procedure.  
        ------------------------------------------------------------------------
        """

        # Store the main procedure name and set the LOADED status when this is
        # the first call event
        if self.__mainProc is None:
            # Set the initial status
            self._setStatus(status.LOADED)
            self.__mainProc = file

        # If we are calling a module and status is loaded: we are loading the proc
        if (name == "<module>")   and \
           (not self.__autorun)   and \
           (not self.__condition) and \
           (self.getStatus() == status.LOADED):
            # If there is no condition and we are not in autorun, pause
            self.pause()
                
        # We do not want to process imports
        elif (name =="<module>") and (file != self.__currentProc):
            self._importing = True
        else:
            if self.__doStepOver:
                self.__doStepOver = False
                self.__stepOver = True

    #===========================================================================
    def event_return(self, file, line, name, frame, args):
        """
        ------------------------------------------------------------------------
        Dispatcher 'return' event processing. This event takes place each time
        the interpreter returns from a function.  
        ------------------------------------------------------------------------
        """
        # Reset the step over flag
        if not self.__callstack.isSteppingOver():
            self.__stepOver = False
        
    #==========================================================================
    def raiseException(self, ex):
        """
        -----------------------------------------------------------------------
        An exception has been raised by the exec thread
        -----------------------------------------------------------------------
        """
        import traceback
        #We have to retrieve the last TraceBack object in the stack for getting
        # its line
        currentTraceback = sys.exc_info()[2]
        
        # Return if we cannot gather information about the error (not in proc)
        if not currentTraceback:
            self.__error = True
            self._setStatus( status.ERROR )
            REGISTRY['CIF'].notifyError("Uncaught exception: " + repr(ex), ' ')
            return
        
        # We extract the traceback elements to process it our way
        processedTraceback = traceback.extract_tb(currentTraceback)
        # Remove elements from the ptrace which are not part of our procedure
        processedTraceback = filter(lambda x : ProcedureManager.instance().isProcDir(x[0]), processedTraceback)
        lastTb = processedTraceback[-1]
           
        while (currentTraceback.tb_next != None):
            currentTraceback = currentTraceback.tb_next
        separator = ':'
        #TODO Manipulate the stack message for showing the exception messange
        #in a more natural way
        stackMessage = self.__callstack.getStack()
        #stackMessage is represented this way Proc1:23:Proc2:56
        #We want to get the two last elements
        lastInvocation = str(stackMessage).split(separator)[-2:]
        try:
            lastInvocationString = '(Procedure ' + str(lastInvocation[-2]).replace('$','') + ', Line ' + str(lastTb[1]) + ')'
        except:
            lastInvocationString = 'Unknown location: ' + repr(stackMessage)
        #Notify the error and set the status
        message = lastInvocationString + ': ' + str(ex)
        self.__error = True
        self._setStatus( status.ERROR )
        sys.stderr.write(message)
        REGISTRY['CIF'].notifyError(message, ' ')

    #==========================================================================
    def codeException(self, ex):
        """
        -----------------------------------------------------------------------
        An exception has been raised when compiling code
        -----------------------------------------------------------------------
        """
        self.__error = True
        self._setStatus(status.ERROR)
        REGISTRY['CIF'].notifyError( str(ex), '' )

    #===========================================================================
    def setFinished(self):
        if not self.__error:
            # If there is a waiting condition check ongoing, cancel it
            self.__scheduler.abortWait( False )
            # Set the status
            self._setStatus(status.FINISHED)

    #===========================================================================
    def setProcStartTime(self):
        procStartTime = TIME(NOW)
        self.__procStartTime = procStartTime
        msg = ">> Procedure %s started at %s" % (self.__thread.procId, str(procStartTime))
        LOG(msg)
        sys.stderr.write(msg + '\n')

    #===========================================================================
    def setProcEndTime(self):
        if self.__procStartTime:
            procStartTime = self.__procStartTime
            procEndTime = TIME(NOW)
            deltaTime = procEndTime - procStartTime
            msg = ">> Procedure %s ended at %s (duration: %s)" % (self.__thread.procId, str(procEndTime), deltaTime)
        else:
            msg = ">> Procedure %s did not start (duration: 0)" % (self.__thread.procId)

        LOG(msg)
        sys.stderr.write(msg + '\n')
