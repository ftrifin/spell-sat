################################################################################

"""
PACKAGE 
    server.executor.scheduler
FILE
    scheduler.py
    
DESCRIPTION
    Scheduling and waiting mechanisms for executor
    
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
import status,mode

#*******************************************************************************
# system Imports
#*******************************************************************************
import threading,sys,thread,time

################################################################################        
class CheckLoop(threading.Thread):
    
    # Callback function to evaluate a condition
    __checkCallback = None
    # Lock for working flag
    __lock = None
    # Working flag for cancelling the evaluation
    __working = False
    # Scheduler reference
    __scheduler = None
    
    #==========================================================================
    def __init__(self, scheduler, checkCallback):
        threading.Thread.__init__(self)
        self.__scheduler = scheduler
        self.__lock = thread.allocate_lock()
        self.__checkCallback = checkCallback
        self.__working = True
        
    #==========================================================================
    def setWorking(self, working):
        self.__lock.acquire()
        try:
            self.__working = working
        finally:
            self.__lock.release()

    #==========================================================================
    def getWorking(self):
        self.__lock.acquire()
        try:
            working = self.__working
        finally:
            self.__lock.release()
        return working
        
    #==========================================================================
    def cancel(self):
        self.setWorking(False)

    #==========================================================================
    def run(self):
        while (self.getWorking()):
            result = self.__checkCallback()
            if result == True:
                self.__scheduler.finishWait()
                return
        
################################################################################        
class Scheduler(object):

    # Check callback for customized waits
    __checkCallback = None
    # Timer for customized waits
    __checker = None
    # Period for customized waits
    __checkPeriod = None
    # Waiting lock. Used for blocking execution while a waiting process
    # is being executed, say, a time or event condition.
    __waitingLock = None
    # Controller reference
    __controller = None
    
    #==========================================================================
    def __init__(self):
        self.__controller = None
        self.__checkCallback = None
        self.__checker = None
        self.__checkPeriod = None
        self.__waitingLock = threading.Event()
        self.__waitingLock.set()

    #===========================================================================
    def setController(self, controller):
        self.__controller = controller

    #===========================================================================
    def startWait(self, checkCallback = None, period = 0.5 ):
        """
        ------------------------------------------------------------------------
        Starts a wait condition check timer. The given callback function will
        be in charge of performing any type of check, returning True if the 
        condition is fulfilled, False otherwise.
        
        The callback is called with the given period in seconds.
        
        This function is called by the language (e.g. WaitFor) or by the 
        execution thread, to manage procedure scheduling conditions.  
        ------------------------------------------------------------------------
        """
        
        #LOG("Executor gone to WAITING status")
        self.__controller._setStatus(status.WAITING)
        # If there is no callback to be used, just block the execution
        self.__waitingLock.clear()
        if self.__checker:
            self.__checker.cancel()
            del self.__checker
            self.__checker = None
        if checkCallback is None:
            self.__checkCallback = None
            self.__checkPeriod = None
        else:
            # If there is a callback given, prepare and start the timer
            self.__checkCallback = checkCallback
            self.__checkPeriod = period
            self.__launchCheck()

    #==========================================================================
    def __launchCheck(self):
        # If there is a period, use the timer check. Otherwise use the
        # loop check
        if self.__checkPeriod == 0:
            self.__checker = CheckLoop( self, self.__checkCallback )
        else:
            self.__checker = threading.Timer( self.__checkPeriod, self._wait_callback )
        self.__checker.start()

    #==========================================================================
    def getWaitStartTime(self):
        return self.__startTime

    #==========================================================================
    def restartWait(self):
        """
        ------------------------------------------------------------------------
        Restarts a condition check timer if it was interrupted previously. This
        is called each time STEP or RUN commands are issued in a line after
        wait condition was paused previously.   
        ------------------------------------------------------------------------
        """
        if self.__checkCallback is not None:
            LOG("Executor restarted WAITING")
            # Go again to waiting status
            self.__controller._setStatus(status.WAITING)
            # And restart the check
            self.__launchCheck()

    #==========================================================================
    def abortWait(self, setStatus = True ):
        """
        ------------------------------------------------------------------------
        Aborts the condition check timer if any. The returned value (True if
        a timer was aborted) is required for handling interrupted condition
        processing at the execution control level (see controller::skip())   
        ------------------------------------------------------------------------
        """
        aborted = False
        # If there is a timer ongoing, just cancel it and reset attributes
        if self.__checkCallback is not None:
            LOG("Executor aborted WAITING")
            if self.__checker:
                self.__checker.cancel()
                del self.__checker
                self.__checker = None
            self.__checkCallback = None
            aborted = True
        # We wont continue with the condition processing, so release the lock
        self.finishWait( setStatus, keepLock = False )
        return aborted

    #==========================================================================
    def interruptWait(self):
        """
        ------------------------------------------------------------------------
        Cancels a condition checking timer. This is executed each time there is
        a timer ongoing and the PAUSE command is received (see controller::pause
        method). It can be restarted afterwards with STEP command.   
        ------------------------------------------------------------------------
        """
        if self.__checkCallback is not None:
            LOG("Executor interrupted WAITING")
            self.__checker.cancel()
            del self.__checker
            self.__checker = None
            # We may continue with the condition processing, 
            # so don't release the lock
            self.finishWait( setStatus = True, keepLock = True )
            return True
        return False

    #==========================================================================
    def finishWait(self, setStatus = True, keepLock = False):
        """
        ------------------------------------------------------------------------
        Finish a wait condition   
        ------------------------------------------------------------------------
        """
        #LOG("Executor come back from WAITING status")
        # Release the wait lock on demand 
        if not keepLock: 
            self.__waitingLock.set()
            self.__checkCallback = None
            self.__checkPeriod = None
            if self.__checker:
                self.__checker.cancel()
            del self.__checker
            self.__checker = None

        # Change the status only when required
        if setStatus and not self.__controller.isAborting():
            if self.__controller.getMode() == mode.MODE_STEP:
                self.__controller._setStatus(status.PAUSED)
            elif self.__controller.getMode() == mode.MODE_PLAY:
                # When keeplock is true, it means that we are interrupting
                # a wait check. In this case we always want to go in PAUSED status.
                if not keepLock:
                    self.__controller._setStatus(status.RUNNING)
                else:
                    self.__controller._setStatus(status.PAUSED)

    #==========================================================================
    def wait(self):
        """
        ------------------------------------------------------------------------
        Blocks the caller meanwhile a condition check is being performed
        ------------------------------------------------------------------------
        """
        self.__waitingLock.wait()

    #==========================================================================
    def _wait_callback(self):
        """
        ------------------------------------------------------------------------
        Call the callback
        ------------------------------------------------------------------------
        """
        # Call the callback function 
        if self.__checkCallback() == True:
            # Finish waiting if the callback returns True
            self.finishWait()
        else:
            # Otherwise reset the timer to continue checking
            self.__checker.cancel()
            self.__checker = threading.Timer(self.__checkPeriod, self._wait_callback)
            self.__checker.start()

        