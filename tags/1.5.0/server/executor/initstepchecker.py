################################################################################

"""
PACKAGE 
    server.executor.initstepchecker
FILE
    initstepchecker.py
    
DESCRIPTION
    ...
    
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
from spell.lib.goto import GotoMgr
from server.procedures.manager import *

################################################################################        
class InitStepChecker(object):
    
    #===========================================================================
    def __init__(self, procId):
        self.procId = procId
        self.fileName = ProcedureManager.instance().getProcedure(procId).getFilename()
        try:
            self.initLineNo = GotoMgr.instance().create(self.fileName).getInitLine()
        except Exception,ex:
            LOG("Unable to find INIT step: " + str(ex))
            self.initLineNo = None
        
        if self.initLineNo is None:
            LOG("No INIT step found for this procedure")
        else:
            LOG("INIT step found at line %i" % self.initLineNo)
        
    #===========================================================================
    def checkInitStep(self, event, file, line, name, frame, args):
        if self.initLineNo is None or (event == 'call' and name == '<module>'):
            return None
        if file == self.fileName and line == self.initLineNo:
            LOG("Breakpoint at ENTRY line")
            self.initLineNo = None
            return True
        return False
         