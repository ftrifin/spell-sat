###############################################################################

"""
PACKAGE 
    spell.lang.helpers.evhelper 
FILE
    evhelper.py
    
DESCRIPTION
    Helpers for event management
    
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

###############################################################################

#*******************************************************************************
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lang.functions import *
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import WrapperHelper

#*******************************************************************************
# System Imports
#*******************************************************************************


###############################################################################
# Module import definition

__all__ = ['Event_Helper']

################################################################################
class Event_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the Event wrapper.
    """
    __msg = ""    
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "EV")
        self.__msg = ""
        self._opName = "Event Injection" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        # Parse arguments
        if len(args)==0:
            raise SyntaxException("No message given")
        self.__msg = args[0]
        if type(self.__msg)!=str:
            raise SyntaxException("Expected a message string")
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        self._write( self.__msg, self._config )
        REGISTRY['EV'].raiseEvent(self.__msg, self._config)            
        return [False,True]

