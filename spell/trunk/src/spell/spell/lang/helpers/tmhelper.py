###################################################################################
## MODULE     : spell.lang.helpers.tmhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for telemetry functions
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or modify
## it under the terms of the GNU General Public License as published by
## the Free Software Foundation, either version 3 of the License, or
## (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
## GNU General Public License for more details.
##
## You should have received a copy of the GNU General Public License
## along with SPELL. If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################

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
    __extended = False
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__extended = False
        self._opName = None

    #===========================================================================
    def _doPreOperation(self, *args, **kargs):
        from spell.lib.adapter.tm_item import TmItemClass
        if len(args)==0:
            raise SyntaxException("No parameter name given")

        # Check correctness
        param = args[0]
        if type(param) != str:
            if not isinstance(param,TmItemClass):
                raise SyntaxException("Expected a TM item or name")
            # It is a TmItemClass, store it
            self.__parameter = param
        else:
            # Create the parameter and store it
            self.__parameter = REGISTRY['TM'][param]

        # Store the extended flag if any
        self.__extended = (self.getConfig(Extended) == True)
        
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        if self.getConfig(ValueFormat)==ENG:
            self._write("Retrieving engineering value of " + repr(self.__pname()))
        else:
            self._write("Retrieving raw value of " + repr(self.__pname()))

        value = None
        # Refresh the object and return it
        REGISTRY['TM'].refresh(self.__parameter, self.getConfig() )
        
        if self.__extended == True:
            value = self.__parameter
            self._notifyValue( self.__pname(), "<OBJ>", NOTIF_STATUS_OK, "TM item obtained")
        
        else: # Normal behavior
            # Get the value in the desired format from the TM interface
            value = self.__parameter.value(self.getConfig())
            
            if self.getConfig(Wait)==True:
                self._write("Last updated value of " + repr(self.__pname()) + ": " + str(value))
            else:
                self._write("Last recorded value of " + repr(self.__pname()) + ": " + str(value))

            self._notifyValue( self.__pname(), str(value), NOTIF_STATUS_OK, "")

        return [False, value,None,None]

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
        return self.__parameter.fullName() 
                    
################################################################################
class SetGroundParameter_Helper(WrapperHelper):
    """
    DESCRIPTION:
        Helper for the SetGroundParameter wrapper function.
    """    
    __toInject = None
    __value = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Telemetry injection"
        self.__toInject = None
        self.__value = None
    
    #===========================================================================
    def _doPreOperation(self, *args, **kargs):

        if len(args)==0:
            raise SyntaxException("No parameters given")

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
            result = REGISTRY['TM'].inject( self.__toInject, self.getConfig() )
            if result == True:
                self._write("Injected values: ")
                for item in self.__toInject:
                    self._write("  - " + str(item[0]) + " = " + str(item[1]))
            else:
                self._write("Failed to inject values", {Severity:ERROR})
        else:
            result = REGISTRY['TM'].inject( self.__toInject, self.__value, self.getConfig() )
            if result == True:
                self._write("Injected value: " + str(self.__toInject) + " = " + str(self.__value))
            else:
                self._write("Failed to inject value", {Severity:ERROR})

        return [False,result,NOTIF_STATUS_OK,""]

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
        self.__useConfig.update(self.getConfig())
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
      
        self._notifyOpStatus( NOTIF_STATUS_PR, "Verifying..." )
  
        # Wait some time before verifying if requested
        if self.__useConfig.has_key(Delay):
            delay = self.__useConfig.get(Delay)
            if delay:
                from spell.lang.functions import WaitFor
                self._write("Waiting "+ str(delay) + " seconds before TM verification", {Severity:INFORMATION})
                WaitFor(delay)
        
        result = REGISTRY['TM'].verify( self.__vrfDefinition, self.__useConfig )

        # If we reach here, result can be true or false, but no exception was raised
        # this means that a false verification is considered ok.
        
        return [False,result,NOTIF_STATUS_OK,""]

    #===========================================================================
    def _doSkip(self):
        if self.getConfig(PromptUser)==True:
            self._write("Verification skipped", {Severity:WARNING} )
        return [False,True]

    #===========================================================================
    def _doCancel(self):
        if self.getConfig(PromptUser)==True:
            self._write("Verification cancelled", {Severity:WARNING} )
        return [False,False]
                
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
    __parameter = None
    __limits = {}
     
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__limits = {}
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No parameters given")

        self.__parameter = args[0]
        
        self.__limits = {}
        if self.hasConfig(Limits):
            llist = self.getConfig(Limits)
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
            if self.hasConfig(LoRed): self.__limits[LoRed] = self.getConfig(LoRed)
            if self.hasConfig(LoYel): self.__limits[LoYel] = self.getConfig(LoYel)
            if self.hasConfig(HiRed): self.__limits[HiRed] = self.getConfig(HiRed)
            if self.hasConfig(HiYel): self.__limits[HiYel] = self.getConfig(HiYel)

            if self.hasConfig(LoBoth): 
                self.__limits[LoYel] = self.getConfig(LoBoth)
                self.__limits[LoRed] = self.getConfig(LoBoth)
            if self.hasConfig(HiBoth): 
                self.__limits[HiYel] = self.getConfig(HiBoth)
                self.__limits[HiRed] = self.getConfig(HiBoth)
            if self.hasConfig(Expected):
                self.__limits[Expected] = self.getConfig(Expected)
        
        if len(self.__limits)==0:
            raise SyntaxException("No limits given")
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        result = REGISTRY['TM'].setLimits( self.__parameter, self.__limits, config = self.getConfig() )
        
        return [False,result,NOTIF_STATUS_OK,""]

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
    __parameter = None
    __property = None
     
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__parameter = None
        self.__property = None
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No parameters given")

        self.__parameter = args[0]
        self.__property = args[1]
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        result = None
        limits = REGISTRY['TM'].getLimits( self.__parameter, config = self.getConfig() )
        
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
        return [False,result,NOTIF_STATUS_OK,""]

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
    __limitsFile = None
    __retry = False
    __prefix = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__limitsFile = None
        self.__retry = False
        self.__prefix = None
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0:
            raise SyntaxException("No limits file URL given")
        
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
        
        result = REGISTRY['TM'].loadLimits( self.__limitsFile, config = self.getConfig() )
        
        return [False,result,NOTIF_STATUS_OK,""]

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
