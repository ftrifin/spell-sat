################################################################################

"""
PACKAGE 
    spell.lang.helpers.rschelper
FILE
    rschelper.py
    
DESCRIPTION
    Helpers for resource management wrapper functions. 
    
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
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.core import *
from spell.lib.adapter.constants.notification import *
from spell.utils.ttime import TIME
from spell.lib.registry import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import WrapperHelper

#*******************************************************************************
# System Imports
#*******************************************************************************


################################################################################
class SetResource_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the SetConfig wrapper.
    """    
    
    # Name of the parameter to be checked
    __parameter = None
    __value = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__parameter = None
        self.__value = ""
        self._opName = "Set resource" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        """
        DESCRIPTION:
        
        ARGUMENTS:
                
        RETURNS:
            
        RAISES:
            DriverException if there is a problem in the driver layer
            SyntaxException if the parameter name is not given.
        """
        if len(args)==0:
            raise SyntaxException("Expected resource name and value")
        elif len(args)==1:
            raise SyntaxException("Expected resource value")
        
        if type(args[0])!=str:
            raise SyntaxException("Expected a string")
        
        self.__parameter = args[0]
        if type(self.__parameter)!=str:
            self.__parameter = self.__parameter.name()
        self.__value = args[1]
        
        self._notifyValue( repr(self.__parameter), repr(self.__value), NOTIF_STATUS_PR, "Injecting")
        
        self._write("Setting ground resource " + repr(self.__parameter) + " value to " + repr(self.__value))
        
        result = REGISTRY['RSC'].setResource( self.__parameter, self.__value, self._config )
        if result:
            self._notifyValue( repr(self.__parameter), repr(self.__value), NOTIF_STATUS_OK, "Injected")
            self._write("Ground resource changed")
        else:
            self._notifyValue( repr(self.__parameter), repr(self.__value), NOTIF_STATUS_FL, "Failed")
            self._write("Failed to change ground resource", {Severity:ERROR})

        return [False,result]
                
        #===========================================================================
        def _doRepeat(self):
            Display("Retry set resource value " + repr(self.__parameter), WARNING)
            return [True, None]
    
        #===========================================================================
        def _doSkip(self):
            Display("Skip set resource value " + repr(self.__parameter), WARNING)
            self._notifyValue( repr(self.__parameter), repr(self.__value), NOTIF_STATUS_SP, "Skipped")
            return [False, None]
                                    
################################################################################
class GetResource_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the GetConfig wrapper.
    """    
    
    # Name of the parameter to be checked
    __parameter = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__parameter = None
        self._opName = "Get resource" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        """
        DESCRIPTION:
        
        ARGUMENTS:
                
        RETURNS:
            
        RAISES:
            DriverException if there is a problem in the driver layer
            SyntaxException if the parameter name is not given.
        """
        if len(args)==0 or type(args[0])!=str:
            raise SyntaxException("Expected resource name as string")

        self.__parameter = args[0]
        if type(self.__parameter)!=str:
            self.__parameter = self.__parameter.name()
        
        self._notifyValue( repr(self.__parameter), "", NOTIF_STATUS_PR, "Reading")
        result = REGISTRY['RSC'].getResource( self.__parameter, self._config )
        if result is None:
            self._notifyValue( repr(self.__parameter), "???", NOTIF_STATUS_FL, "Failed")
            self._write("Unable to read ground resource " + repr(self.__parameter), {Severity:ERROR})
        else:
            self._notifyValue( repr(self.__parameter), repr(result), NOTIF_STATUS_OK, "Read")
            self._write("Ground resource " + repr(self.__parameter) + " has a value of " + repr(result))

        return [False,result]
                
        #===========================================================================
        def _doRepeat(self):
            Display("Retry get resource value " + repr(self.__parameter), WARNING)
            return [True, None]
    
        #===========================================================================
        def _doSkip(self):
            Display("Skip get resource value " + repr(self.__parameter), WARNING)
            self._notifyValue( repr(self.__parameter), "???", NOTIF_STATUS_SP, "Skipped")
            return [False, None]
                                    
################################################################################
class SetLink_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the SetLink wrapper.
    """    
    
    # Name of the link
    __name = None
    __enable = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__name = None
        self.__enable = None
        self._opName = "Set link" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        """
        DESCRIPTION:
        
        ARGUMENTS:
                
        RETURNS:
            
        RAISES:
            DriverException if there is a problem in the driver layer
        """

        if len(args)==0:
            raise SyntaxException("Expected link identifier and status")
        elif len(args)==1:
            raise SyntaxException("Expected link status")
        
        if type(args[0])!=str:
            raise SyntaxException("Expected a string")
        

        self.__name = args[0]
        self.__enable = args[1]
        
        result = REGISTRY['RSC'].setLink( self.__name, self.__enable, self._config )

        return [False,result]
                
        #===========================================================================
        def _doRepeat(self):
            Display("Retry set link status " + repr(self.__name), WARNING )
            return [True, None]
    
        #===========================================================================
        def _doSkip(self):
            Display("Skip set link status " + repr(self.__name), WARNING )
            return [False, None]
                                    
################################################################################
class CheckLink_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the CheckLink wrapper.
    """    
    
    # Name of the link
    __name = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__name = None
        self._opName = "Check link" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        """
        DESCRIPTION:
        
        ARGUMENTS:
                
        RETURNS:
            
        RAISES:
            DriverException if there is a problem in the driver layer
        """

        if len(args)==0:
            raise SyntaxException("Expected link identifier")
        if type(args[0])!=str:
            raise SyntaxException("Expected a string")

        self.__name = args[0]
        
        result = REGISTRY['RSC'].checkLink( self.__name, self._config )

        return [False,result]
                
        #===========================================================================
        def _doRepeat(self):
            Display("Retry get link status " + repr(self.__name), WARNING )
            return [True, None]
    
        #===========================================================================
        def _doSkip(self):
            Display("Skip get link status " + repr(self.__name), WARNING )
            return [False, None]
                                    
