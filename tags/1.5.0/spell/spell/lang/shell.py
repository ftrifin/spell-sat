################################################################################

"""
PACKAGE 
    spell.lang.shell 
FILE
    shell.py
    
DESCRIPTION
    Command line shell for SPELL 
    
PROJECT: SPELL

 Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 This library is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, either
 version 3 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License and GNU General Public License (to which the GNU Lesser
 General Public License refers) along with this library.
 If not, see <http://www.gnu.org/licenses/>.
 
"""

################################################################################

#*******************************************************************************
# SPELL imports
#*******************************************************************************
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lang.functions import *
from spell.utils.ttime import *
from spell.lang.user import *
from spell.utils.log import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import sys,threading

LOG.showlog = False

################################################################################
class FakeExecutor(object):

    #==========================================================================
    def setup(self):
        self.__checkCallback = None
        self.__checkTimer = None
        self.__checkPeriod = None
        self.__waitingLock = threading.Event()
        self.__waitingLock.set()
        REGISTRY['EXEC'] = self
        print "Ready"
            
    #==========================================================================
    def cleanup(self):
        print "Executor finished"
        sys.exit(0)

    #==========================================================================
    def connectionLost(self, contextKey):
        pass

    #==========================================================================
    def getAsRunFile(self):
        return None

    #==========================================================================
    def getEnvironment(self):
        return globals()

    #==========================================================================
    def processLock(self):
        pass

    #==========================================================================
    def processUnlock(self):
        pass

    #==========================================================================
    def startWait(self, checkCallback = None, period = 0.5 ):
        # If there is no callback to be used, just block the execution
        self.__waitingLock.clear()
        if self.__checkTimer:
            self.__checkTimer.cancel()
            self.__checkTimer = None
        if checkCallback is None:
            self.__checkCallback = None
            self.__checkPeriod = None
        else:
            # If there is a callback given, prepare and start the timer
            self.__checkCallback = checkCallback
            self.__checkPeriod = period
            self.__checkTimer = threading.Timer(period, self._wait_callback)
            self.__checkTimer.start()


    #==========================================================================
    def wait(self):
        self.__waitingLock.wait()

    #==========================================================================
    def finishWait(self, setStatus = True, keepLock = False):
        # Release the wait lock on demand 
        if not keepLock: 
            self.__waitingLock.set()
            self.__checkCallback = None
            self.__checkPeriod = None
            if self.__checkTimer:
                self.__checkTimer.cancel()
            self.__checkTimer = None

    #==========================================================================
    def _wait_callback(self):
        # Call the callback function 
        if self.__checkCallback() == True:
            # Finish waiting if the callback returns True
            self.finishWait()
        else:
            # Otherwise reset the timer to continue checking
            self.__checkTimer.cancel()
            self.__checkTimer = threading.Timer(self.__checkPeriod, self._wait_callback)
            self.__checkTimer.start()

    #==========================================================================
    def abort(self):
        pass

    #==========================================================================
    def pause(self):
        pass

    #==========================================================================
    def command(self, commandData):
        pass

    #===========================================================================
    def setExecutionDelay(self, delay):
        pass

    #===========================================================================
    def loadSubProc(self, procId):
        pass

    #===========================================================================
    def getFullStackPosition(self):
        return None 

    #===========================================================================
    def getStatus(self):
        return UNKNOWN

################################################################################
def Setup( ctxName, showProgress = True ):
    import __main__
    from spell.lib.drivermgr import DriverManager
    from spell.lib.registry import REGISTRY 
    from server.ui.cmdline import ClientIF
    from spell.config.reader import Config

    try:
        DriverManager.instance().setup(ctxName)
    except SpellException,ex:
        print "Unable to setup driver: ",ex.message,ex.reason
        DriverManager.instance().cleanup( force = True )
        return False

    FakeExecutor().setup()

    for ifc in REGISTRY.interfaces():
        __main__.__dict__[ifc] = REGISTRY[ifc]
        
    ClientIF.setup(ctxName)
    
    # Get builtin databases
    __main__.__dict__['SCDB'] = REGISTRY['DBMGR']['SCDB']
    __main__.__dict__['GDB'] = REGISTRY['DBMGR']['GDB']

    if showProgress:
        print 
        print
        print "Importing interfaces"
         
    g_ctx = globals()

    ifcs = ['TM','TC','EV','RSC','TASK','USER','DBMGR','SCDB','GDB']
    for ifc in ifcs:
        try:
            exec("from __main__ import " + ifc, g_ctx)
            print " - Interface",ifc
        except BaseException,ex:
            print "ERROR: ",ex

    if showProgress:
        print 
        print
        print "Importing driver specifics"

    try:
        import constants
        for c in dir(constants):
            if not c.startswith("__"):
                exec("from constants import " + c, g_ctx)
    except BaseException,ex:
        print "ERROR: ",ex
    
    try:
        import modifiers
        for c in dir(modifiers):
            if not c.startswith("__"):
                exec("from modifiers import " + c, g_ctx)
    except BaseException,ex:
        print "ERROR: ",ex
    
    if showProgress: 
        print 
        print
        print "READY"
        print "===================================================================="
        print 
    return True
    
################################################################################
def Cleanup():
    import __main__
    from spell.lib.drivermgr import DriverManager
    from spell.lib.registry import REGISTRY
    from server.ui.cmdline import ClientIF
    DriverManager.instance().cleanup( shutdown = True )
    for ifc in REGISTRY.interfaces():
        REGISTRY.remove(ifc)
        if __main__.__dict__.has_key(ifc):
            __main__.__dict__.pop(ifc)
    if 'CIF' in REGISTRY.interfaces():
	    ClientIF.cleanup()

################################################################################
def EnableLog():
    from spell.utils.log import LOG
    import sys
    LOG.showlog = True
    sys.stderr.write("Log traces enabled: " + repr(LOG.showlog)+"\n")

################################################################################
def DisableLog():
    from spell.utils.log import LOG
    import sys
    LOG.showlog = False
    sys.stderr.write("Log traces enabled: " + repr(LOG.showlog)+"\n")

################################################################################
if __name__ == "__main__":
    
    from spell.utils.log import *
    import os,sys,traceback,getopt
    from spell.config.reader import *
    haveReadLine = True

    try:
        import atexit
        import readline
        import rlcompleter
    except ImportError,e:
        haveReadLine = False
    
    # where is history saved
    historyPath = os.path.expanduser("~/.pyhistory")
    
    # handler for saving history
    def save_history(historyPath=historyPath):
        if not haveReadLine: return
        import readline
        readline.write_history_file(historyPath)
    
    if haveReadLine and os.path.exists(historyPath):
        # read history, if it exists
        readline.read_history_file(historyPath)
        # register saving handler
        atexit.register(save_history)
        # enable completion
        readline.parse_and_bind('tab: complete')
    
    # cleanup
    del os, save_history, historyPath
    if haveReadLine:
        atexit.register(Cleanup)
        del atexit, readline, rlcompleter 

    try:
        shortopts = 'n:c:p:s'
        options, trailing = getopt.getopt(sys.argv[1:], shortopts)
        
        configFile = None
        ctxName = None
        for option,value in options:
            if option == '-c':
                configFile = value
            elif option == '-n':
                ctxName = value
                
        if configFile is None:
            sys.stderr.write("ERROR: no configuration file given\n")
            sys.stderr.write("  Use argument: -c <path to file>\n")
            sys.exit(1)

        LOG.setLogFile( "SPEL_Shell" )

        # Load the configuration file
        Config.instance().load(configFile)
        if not Config.instance().validate():
            sys.stderr.write("ERROR: please check configuration file\n")
            sys.exit(1)
    
        if ctxName:
            Setup(ctxName)
        else:
            Setup("STD")
            
        del LOG,LOG_CNFG,LOG_COMM,LOG_DEBUG,LOG_ERROR,LOG_INFO,LOG_INIT,LOG_MAIN
        del LOG_PROC,LOG_WARN,getopt
        del shortopts,options,option,trailing,value
        del configFile,ctxName,traceback,sys
        
    except SystemExit,ex:
        Cleanup()
    except BaseException,ex:
        sys.stderr.write("==================================================\n")
        sys.stderr.write("UNHANDLED EXCEPTION:\n ")
        sys.stderr.write(ex.message + "\n")
        sys.stderr.write(repr(ex) + "\n")
        traceback.print_exc( file = sys.stderr )
        sys.stderr.write("==================================================\n")

    
