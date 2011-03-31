################################################################################

"""
PACKAGE 
    server.executor.callstack
FILE
    callstack.py
    
DESCRIPTION
    Executor call stack manager
    
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
from spell.lib.registry import *
from server.core.messages.base import *
from server.procedures.manager import ProcedureManager

#*******************************************************************************
# Local Imports
#*******************************************************************************
import mode, status

#*******************************************************************************
# System Imports
#*******************************************************************************
import sys

#*******************************************************************************
# Module globals
#*******************************************************************************

MARKER="$"

################################################################################        
class CallStack(object):
    
    # Call stack
    __stack = []
    # Call stack string representation
    __stackStr = ""
    # Position within the stack (marker for the GUI)
    __csp = None
    # Current stage [id,title]
    __stage = None
    # "Step over" sticky flag. When active, the marker for the GUI shall not be moved
    # when the stack increases.
    __sticky = False
    # True if the sicky flag should be permanent, that is, not deactivated
    # when the csp level returns to zero.
    __permanent = False
    # Keeps the list of notified code (procedures known by the GUI)
    __knownProcedures = []
    # Holds the source lines for each procedure
    __sources = {}
    # Cache of the last known callstack string
    __lastStackStr = ""
    
    #===========================================================================
    def __init__(self):
        self.reset()

    #===========================================================================
    def reset(self):
        self.__stack = []
        self.__sources = {}
        self.__stackStr = ""
        self.__csp = 0
        self.__sticky = False
        self.__permanent = False
        self.__knownProcedures = []
        self.__lastStackStr = ""
        self.__stage = None

    #===========================================================================
    def getStack(self):
        return self.__stackStr

    #===========================================================================
    def getLevel(self):
        return len(self.__stack)/2

    #===========================================================================
    def setSticky(self):
        self.__sticky = True

    #===========================================================================
    def unsetSticky(self):
        if not self.__permanent:
            self.__sticky = False

    #===========================================================================
    def setStage(self, id,title):
        self.__stage = [id,title]

    #===========================================================================
    def setPermanent(self, permanent):
        self.__permanent = permanent

    #===========================================================================
    def toTop(self):
        self.unsetSticky()
        self.__csp = len(self.__stack)    
        self.__updateStack()    
        REGISTRY['CIF'].notifyLine( self.__stackStr, self.__stage )
        
    #===========================================================================
    def isSteppingOver(self):
        length = len(self.__stack)
        return (self.__csp < length)

    #===========================================================================
    def isCurrentProc(self, file):
        return (self.__stack[-2] == file)

    #===========================================================================
    def event_line(self, file, line, name, frame, args):

        if len(self.__stack) == 0:
            raise BaseException()
        else:
            self.__stack[-1] = str(line)
            self.__updateStack(True)
            REGISTRY['CIF'].notifyLine( self.__stackStr, self.__stage )

        # Disable the sticky flag is appropiate
        if self.__sticky and (not self.__permanent) and (self.__csp == len(self.__stack)):
            self.__sticky = False 

    #===========================================================================
    def event_call(self, file, line, name, frame, args):
        
        # The procedure is loaded (first time)
        if len(self.__stack) == 0:
            self.__stack = [ file, str(line) ]
            self.__csp = 0
            self.__knownProcedures += [file]
            self.__updateStack() 
        else:
            # Call to a function, so increase the stack. 
            self.__stack += [ file, str(line) ]
            doNotifyCode = False
            if not file in self.__knownProcedures:
                self.__knownProcedures += [file]
                doNotifyCode = True
                
            # If the permanent flag is on and sticky is false, enable it
            if self.__permanent and not self.__sticky:
                self.setSticky(True)
                
            # If we are stepping over, do not change the marker position
            # otherwise move the marker up.
            if not self.__sticky:
                self.__csp = len(self.__stack)
                
            self.__updateStack() 
            if doNotifyCode:
                
                if file in self.__sources:
                    code = self.__sources[file]
                else:
                    sourceLines = ProcedureManager.instance().getSource(file)
                    code = ""
                    for line in sourceLines:
                        sep = CODE_SEPARATOR
                        add = ""
                        if len(code)==0: sep = ""
                        if len(line)==0: add = " "
                        code += sep + line + add
                    self.__sources[file] = code
                    
                REGISTRY['CIF'].notifyCode( code, self.__stackStr )
            else:
                REGISTRY['CIF'].notifyLine( self.__stackStr, self.__stage )
                
    #===========================================================================
    def event_return(self, file, line, name, frame, args):

        if name=="<module>" and file == self.__stack[0]:
            # We are finishing the execution, ignore it
            return
        
        # Return from a function, so decrease the stack. 
        self.__stack = self.__stack[0:-2]

        # If we were not sticky, move down the marker position
        if not self.__sticky or (self.__csp > len(self.__stack)): 
            self.__csp = len(self.__stack)
            self.__updateStack() 
            REGISTRY['CIF'].notifyLine( self.__stackStr, self.__stage )
        else:
            self.__updateStack() 
        
    #===========================================================================
    def __updateStack(self, isLineNoChangeOnly = False):
        if isLineNoChangeOnly:
            self.__stackStr = self.__lastStackStr + ':' + self.__stack[-1]
        else:
            stack = ""
            toInsert = self.__csp - 2
            if toInsert<0: toInsert=0
            copy = self.__stack[:-1]
            copy[toInsert] = "$" + copy[toInsert]
            self.__lastStackStr = ':'.join(copy)
            stack = self.__lastStackStr + ':' + self.__stack[-1]
            self.__stackStr = stack
