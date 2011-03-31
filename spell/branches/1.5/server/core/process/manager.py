################################################################################

"""
PACKAGE 
    server.core.process.manager
FILE
    manager.py
    
DESCRIPTION
    Implementation of a process manager capable of start/kill/wait for and
    communicate with subprocesses
    
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import subprocess,os
import threading,sys,time,thread

#*******************************************************************************
# Exceptions 
#*******************************************************************************
class ProcError(BaseException): pass
 
#*******************************************************************************
# Module globals
#*******************************************************************************

# Package visibility
__all__ = [ 'ProcessManager', 'ProcError' ]

__instance__ = None

################################################################################
# RATIONALE: due to a bug in the subprocess.Popen class, open sockets on a 
# killed subprocess remain open after the process disappears.
################################################################################
class ProcessMonitor(threading.Thread):

    working         = False
    process         = None
    processId       = None
    processCallback = None
    processLock     = None
    
    #===========================================================================
    def __init__(self, process, identifier, callback):
        threading.Thread.__init__(self)
        self.process = process
        self.processId = identifier
        self.processCallback = callback
        self.working = True
        self.processLock = thread.allocate_lock()
        self.processLock.acquire()
    
    #===========================================================================
    def run(self):
        while self.working:
            status = self.process.poll()
            if status is not None:
                self.processLock.release()
                self.processCallback(self.processId,status)
                return
            time.sleep(0.75)
        self.processLock.release()
        
    #===========================================================================
    def waitFinish(self):
        self.working = False
        self.processLock.acquire()
        self.processLock.release()

################################################################################
class ProcessManagerClass(object):
    
    processObject = {}
    processMonitor = {}

    #===========================================================================
    def __init__(self):
        self.processObject = {}
        self.processMonitor = {}
    
    #==========================================================================
    @staticmethod
    def instance():
        global __instance__
        if __instance__ is None:
            __instance__ = ProcessManagerClass()
        return __instance__
    
    #===========================================================================
    def startProcess(self, identifier, cmd, callback = None ):
        if self.processObject.has_key(identifier):
            LOG("Error: process already exists")
            raise ProcError("Process '" + identifier + "' already exists")
        
        LOG("Starting process " + identifier)
        
        LOG("Command: " + cmd)
        try:
            if os.sys.platform == 'win32':
                proc = subprocess.Popen( args = cmd )
            else:
                proc = subprocess.Popen( executable="/bin/sh", shell=True, args = cmd )
        except BaseException,ex:
            LOG("Unable to start process: " + repr(ex))
            raise ProcError("Unable to create process object: " + str(ex))

        LOG("Process object created")

        self.processObject[identifier] = proc
        if callback:
            LOG("Assign process monitor")
            self.processMonitor[identifier] = ProcessMonitor(proc,identifier,callback)
            self.processMonitor[identifier].start()
        
        LOG("Process started with pid " + repr(proc.pid))
        return proc.pid

    #===========================================================================
    def processOut(self, identifier):
        if not self.processObject.has_key(identifier):
            raise ProcError("Process '" + identifier + "' not found")
        
        out = self.processObject[identifier].stdout
        return self.processObject[identifier].stdout

    #===========================================================================
    def processIn(self, identifier):
        if not self.processObject.has_key(identifier):
            raise ProcError("Process '" + identifier + "' not found")
        
        return self.processObject[identifier].stdin

    #===========================================================================
    def waitProcess(self, identifier):
        if not self.processObject.has_key(identifier):
            raise ProcError("Process '" + identifier + "' not found")
        
        LOG("Waiting process '" + identifier + "' to terminate")
        return self.processObject[identifier].wait()

    #===========================================================================
    def killPID(self, pid):
        if os.name == 'nt' or os.name == 'dos':
            try:
                import win32api
                win32api.TerminateProcess(pid, 0)
            except BaseException,ex:
                raise ProcError("WIN32 API not available!" + str(ex))
        elif os.name == 'posix':
            import signal
            os.kill(pid,signal.SIGTERM)
        else:
            raise ProcError("OS kill API not available!")

    #===========================================================================
    def killProcess(self, identifier):
        if not self.processObject.has_key(identifier): return
        
        proc = self.processObject.get(identifier) 

        if proc.poll() is not None:
            LOG("Process '" + identifier + "' already terminated" )
            self.removeProcess(identifier)
            return

        LOG("Killing process '" + identifier + "' with pid " + repr(proc.pid) )

        try:
            if os.name == 'nt' or os.name == 'dos':
                self.killPID(int(proc._handle))
            elif os.name == 'posix':
                self.killPID(int(proc.pid))
            else:
                raise ProcError("OS kill API not available!")
        finally:
            self.removeProcess(identifier)
        LOG("Process killed")

        
    #===========================================================================
    def removeProcess(self, identifier):
        if self.processObject.has_key(identifier):
            LOG("Removing process: " + repr(identifier))
            self.processObject.pop(identifier)
            self.__stopMonitor(identifier)

    #===========================================================================
    def killAll(self):
        LOG("Killing all processes")
        for key in self.processObject.keys():
            self.killProcess(key)
        self.processObject.clear()
        LOG("All processes killed")

    #===========================================================================
    def __stopMonitor(self, identifier):
        LOG("Stopping process monitor for " + repr(identifier))
        if identifier in self.processMonitor:
            self.processMonitor[identifier].waitFinish()
            del self.processMonitor[identifier]
            LOG("Monitor removed")
        else:
            LOG("No monitor to remove for " + repr(identifier))
        
################################################################################
ProcessManager = ProcessManagerClass        