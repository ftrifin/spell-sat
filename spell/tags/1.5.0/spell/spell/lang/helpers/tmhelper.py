################################################################################

"""
PACKAGE 
    spell.lang.helpers.tmhelper
FILE
    tmhelper.py
    
DESCRIPTION
    Helpers for telemetry wrapper functions. 
    
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

from basehelper import WrapperHelper
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.notification import *
from spell.lib.exception import SyntaxException
from spell.lang.functions import *
from spell.lib.registry import *

################################################################################
class GetTM_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the GetTM wrapper.
    """    
    
    # Name of the parameter to be checked
    __parameter = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self._opName = "Telemetry check"

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        from spell.lib.adapter.tm_item import TmItemClass
        if len(args)==0:
            raise SyntaxException("No parameter name given")
        # Get the TM parameter name/item
        self.__parameter = args[0]
        if type(self.__parameter) != str:
            if not isinstance(self.__parameter,TmItemClass):
                raise SyntaxException("Expected a TM item or name")
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # Check the type
        if type(self.__parameter) == str:
            self.__parameter = REGISTRY['TM'][self.__parameter]

        if self.getConfig(ValueFormat)==ENG:
            self._write("Retrieving engineering value of " + repr(self.__pname()))
        else:
            self._write("Retrieving raw value of " + repr(self.__pname()))

        # Get the value in the desired format from the TM interface
        value = self.__parameter.value(self._config)
        status = self.__parameter._getStatus() 
        
        if self._config.has_key(Wait) and self._config.get(Wait)==True:
            self._write("Last updated value: " + repr(value))
        else:
            self._write("Last recorded value: " + repr(value))
        
        return [False, value]

    #===========================================================================
    def _doRepeat(self):
        self._notifyValue( self.__pname(), "???", NOTIF_STATUS_PR, " ")
        self._write("Retry get parameter " + self.__pname(), {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._notifyValue( self.__pname(), "???", NOTIF_STATUS_SP, " ")
        self._write("Skip get parameter " + self.__pname(), {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def __pname(self):
        if type(self.__parameter) == str: 
            return self.__parameter
        else:
            return self.__parameter.name()
                    
################################################################################
class SetGroundParameter_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the SetGroundParameter wrapper function.
    """    
    __toInject = None
    __value = None
    __useConfig = {}
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Telemetry injection"
        self.__toInject = None
        self.__value = None
        self.__useConfig = {}
    
    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)==0:
            raise SyntaxException("No parameters given")
        # Perform the verification. 
        self.__useConfig = {}
        self.__useConfig.update(self._config)

        # Since args is a tuple we have to convert it to a list         
        if len(args)!=1:
            # The case of giving a simple inject definition
            self.__toInject = args[0]
            self.__value = args[1]
            # Modifiers will go in useConfig 
        else:
            # Givin an inject list
            self.__toInject = args[0]
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        if type(self.__toInject)==list:
            result = REGISTRY['TM'].inject( self.__toInject, self.__useConfig )
            if result == True:
                self._write("Injected values: ")
                for item in self.__toInject:
                    self._write("  - " + str(item[0]) + " = " + str(item[1]))
            else:
                self._write("Failed to inject values", {Severity:ERROR})
        else:
            result = REGISTRY['TM'].inject( self.__toInject, self.__value, self.__useConfig )
            if result == True:
                self._write("Injected value: " + str(self.__toInject) + " = " + str(self.__value))
            else:
                self._write("Failed to inject value", {Severity:ERROR})

        return [False,result]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry inject parameters", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skip inject parameters", {Severity:WARNING} )
        return [False, None]

################################################################################
class Verify_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the Verify wrapper function.
    """    
    __retryAll = False
    __retry = False
    __useConfig = {}
    __vrfDefinition = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__retryAll = False
        self.__retry = False
        self.__useConfig = {}
        self.__vrfDefinition = None
        self._opName = "Verification" 
    
    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        if len(args)==0:
            raise SyntaxException("No arguments given")
        self.__useConfig = {}
        self.__useConfig.update(self._config)
        self.__useConfig[Retry] = self.__retry

        # Since args is a tuple we have to convert it to a list for TM.verify        
        if len(args)!=1:
            # The case of giving a simple step for verification
            self.__vrfDefinition = [ item for item in args ]
        else:
            # Givin a step or a step list
            self.__vrfDefinition = args[0]

    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        
        # Wait some time before verifying if requested
        if self.__useConfig.has_key(Delay):
            delay = self.__useConfig.get(Delay)
            if delay:
                from spell.lang.functions import WaitFor
                self._write("Waiting "+ str(delay) + " seconds before TM verification", {Severity:INFORMATION})
                WaitFor(delay)
        
        result = REGISTRY['TM'].verify( self.__vrfDefinition, self.__useConfig )
        
        return [False,result]

    #===========================================================================
    def _doSkip(self):
        self._write("Verification skipped", {Severity:WARNING} )
        if self.getConfig(OnSkip) == False: 
            return [False,False]
        else:        
            return [False,True]
                
    #===========================================================================
    def _doRecheck(self):
        self._write("Retry verification", {Severity:WARNING} )
        return [True,False]

################################################################################
class SetLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the SetTMparam wrapper function.
    """    
    __useConfig = {}
    __parameter = None
    __limits = {}
     
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__useConfig = {}
        self.__parameter = None
        self.__limits = {}
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No parameters given")
        # Perform the verification. 
        self.__useConfig = {}
        self.__useConfig.update(self._config)

        self.__parameter = args[0]
        
        self.__limits = {}
        if Limits in self.__useConfig:
            llist = self.__useConfig.get(Limits)
            if type(llist)==list:
                if len(llist)==2:
                    self.__limits[LoRed] = llist[0]
                    self.__limits[LoYel] = llist[0]
                    self.__limits[HiRed] = llist[1]
                    self.__limits[HiYel] = llist[1]
                elif len(llist)==4:
                    self.__limits[LoRed] = llist[0]
                    self.__limits[LoYel] = llist[1]
                    self.__limits[HiRed] = llist[2]
                    self.__limits[HiYel] = llist[3]
                else:
                    raise SyntaxException("Malformed limit definition")
            elif type(llist)==dict:
                self.__limits = llist       
            else:
                raise SyntaxException("Expected list or dictionary")
        else:
            if LoRed in self.__useConfig: self.__limits[LoRed] = self.__useConfig.get(LoRed)
            if LoYel in self.__useConfig: self.__limits[LoYel] = self.__useConfig.get(LoYel)
            if HiRed in self.__useConfig: self.__limits[HiRed] = self.__useConfig.get(HiRed)
            if HiYel in self.__useConfig: self.__limits[HiYel] = self.__useConfig.get(HiYel)
            if LoBoth in self.__useConfig: 
                self.__limits[LoYel] = self.__useConfig.get(LoBoth)
                self.__limits[LoRed] = self.__useConfig.get(LoBoth)
            if HiBoth in self.__useConfig: 
                self.__limits[HiYel] = self.__useConfig.get(HiBoth)
                self.__limits[HiRed] = self.__useConfig.get(HiBoth)
            if Expected in self.__useConfig:
                self.__limits[Expected] = self.__useConfig.get(Expected)
        
        if len(self.__limits)==0:
            raise SyntaxException("No limits given")
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        result = REGISTRY['TM'].setLimits( self.__parameter, self.__limits, config = self._config )
        
        return [False,result]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry modify parameters", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skipped modify parameters", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel modify parameters", {Severity:WARNING} )
        return [False, None]

################################################################################
class GetLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the GetTMparam wrapper function.
    """    
    __useConfig = {}
    __parameter = None
    __property = None
     
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__useConfig = {}
        self.__parameter = None
        self.__property = None
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No parameters given")
        # Perform the verification. 
        self.__useConfig = {}
        self.__useConfig.update(self._config)

        self.__parameter = args[0]
        self.__property = args[1]
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        result = None
        limits = REGISTRY['TM'].getLimits( self.__parameter, config = self._config )
        
        if self.__property == LoRed:
            result = limits[0]
        elif self.__property == LoYel:
            result = limits[1]
        elif self.__property == HiYel:
            result = limits[2]
        elif self.__property == HiRed:
            result = limits[3]
        else:
            raise DriverException("Cannot get property", "Unknown property name: " + repr(self.__property))
        return [False,result]

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry get property", {Severity:WARNING} )
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skipped get property", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel get property", {Severity:WARNING} )
        return [False, None]

################################################################################
class LoadLimits_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the GetTMparam wrapper function.
    """    
    __useConfig = {}
    __limitsFile = None
    __retry = False
    __prefix = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__useConfig = {}
        self.__limitsFile = None
        self.__retry = False
        self.__prefix = None
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No limits file URL given")
        
        self.__useConfig = {}
        self.__useConfig.update(self._config)
        
        self.__limitsFile = args[0]
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        result = None
        
        if not self.__retry:
            # Get the database name
            self.__limitsFile = args[0]
            
            if type(self.__limitsFile)!=str:
                raise SyntaxException("Expected a limits file URL")
            if not "://" in self.__limitsFile:
                raise SyntaxException("Limits file name must have URI format")
            idx = self.__limitsFile.find("://")
            self.__prefix = self.__limitsFile[0:idx]
        else:
            self.__retry = False        

        idx = self.__limitsFile.find("//")
        toShow = self.__limitsFile[idx+2:]
        self._notifyValue( "Limits File", repr(toShow), NOTIF_STATUS_PR, "Loading")
        self._write("Loading limits file " + repr(toShow))
        
        result = REGISTRY['TM'].loadLimits( self.__limitsFile, config = self._config )
        
        return [False,result]

    #===========================================================================
    def _doRepeat(self):
        self._write("Load limits file failed, getting new name", {Severity:WARNING} )
        idx = self.__limitsFile.find("//")
        toShow = self.__limitsFile[idx+2:]
        newName = str(self._prompt("Enter new limits file name (previously " + repr(toShow) + "): ", [], {} ))
        if not newName.startswith(self.__prefix):
            newName =  self.__prefix + "://" + newName
        self.__limitsFile = newName
        self.__retry = True
        return [True, None]

    #===========================================================================
    def _doSkip(self):
        self._write("Skipped load limits file", {Severity:WARNING} )
        return [False, None]

    #===========================================================================
    def _doCancel(self):
        self._write("Cancel load limits file", {Severity:WARNING} )
        return [False, None]
