################################################################################

"""
PACKAGE 
    server.executor.importchecker
FILE
    importchecker.py
    
DESCRIPTION
    Checks the import condition during dispatching
    
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

################################################################################        
class ImportChecker(object):
    
    # Importing flag
    _importing = False
    # Main procedure file
    _mainProc = None
    # Current procedure file
    _currentProc = None
    
    #===========================================================================
    def __init__(self):
        self._mainProc = None
        self._currentProc = None
        self._importing = False

    #===========================================================================
    def checkImporting(self, event, file, line, name, frame, args):
        """
        ------------------------------------------------------------------------
        Check whether we are importing a python module. We do not want to
        process dispatch events while importing code.
        ------------------------------------------------------------------------
        """

        if event == "line":
            # Do not process imports, but take into account when the import
            # process finishes (when we go back to main proc)
            if (self._importing) and (file == self._currentProc):
                self._importing = False
    
            if not self._importing:
                self._currentProc = file
        elif event == "call":
            if self._mainProc is None:
                self._mainProc = file
                self._currentProc = file
    
            # We do not want to process imports
            if (name =="<module>") and (file != self._currentProc):
                self._importing = True

        return self._importing
