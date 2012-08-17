###################################################################################
## MODULE     : spell.lang.helpers.genhelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Helpers for generic features
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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

#*******************************************************************************
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.core import *
from spell.lib.adapter.constants.notification import *
from spell.lib.adapter.utctime import *
from spell.lib.registry import *
from spell.lib.adapter.interface import Interface
from spell.lib.adapter.databases.database import Database

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import *

#*******************************************************************************
# System Imports
#*******************************************************************************
import time,sys,threading
import inspect


################################################################################
class Prompt_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the Prompt wrapper.
    """    
    __msg = None
    __pType = None
    __options = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__msg = None
        self.__pType = None
        self.__options = None
        self._opName = None 
        
    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        if len(args)==0:
            raise SyntaxException("No message given")

        # Get the prompt message
        self.__msg = args[0]
        if type(self.__msg)!=str:
            raise SyntaxException("Expected a message string")

        self.buildConfig(args, kargs, {}, self._getDefaults())

        # If there are extra arguments process options or type
        self.__options = []
        if len(args)==2:
            if type(args[1])==list:
                self.__options = args[1]
            elif type(args[1])==int:
                self.addConfig(Type,args[1])
            else:
                raise SyntaxException("Unexpected argument: ", repr(args[1]))
        elif len(args)==3:
            if type(args[1])!=list:
                raise SyntaxException("Expected a list of options: " + repr(args[1]))
            if type(args[2])!=int:
                raise SyntaxException("Expected prompt type: " + repr(args[2]))
            self.__options = args[1]
            self.setConfig({Type:args[2]})
        elif len(args)>3:
            raise SyntaxException("Too many arguments")

        # Check if options where provided using keyword
        if kargs.has_key('options'):
            self.__options = kargs.get('options')
            if type(self.__options)!=list:       
                raise SyntaxException("Expected an option list")
            
        #Check prompt type   
        ptype = self.getConfig(Type)
        if any(ptype == x for x in [LIST, LIST|ALPHA, LIST|NUM, LIST|COMBO, LIST|COMBO|ALPHA, LIST|COMBO|NUM]):
            options = self.__options
            if type(options) == list and len(options)>0:
                self.addConfig(Type,ptype)
            else:
                raise SyntaxException("Expected a list of options")
        elif not any( (ptype & x > 0) for x in [OK,CANCEL,YES,NO,YES_NO,OK_CANCEL,NUM,ALPHA,DATE]):
                raise SyntaxException("Unknown prompt type")    
            
        # Check timeout value
        defaultTimeout = 0
        if (self.hasConfig(Timeout)):
            tov = self.getConfig(Timeout)
            if isinstance(tov,TIME):
                if tov.isRel():
                    defaultTimeout = tov.rel()
                else:
                    raise SyntaxException("Cannot accept absolute times", str(tov))
            elif type(tov) in [int,float]:
                defaultTimeout = tov
            else:
                raise SyntaxException("Ignored timeout malformed value", repr(tov))
        else:
            self.addConfig(Timeout,defaultTimeout)

        # Mark procedure prompts
        self.addConfig(Scope,SCOPE_PROC)

    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        
        answer = self._prompt( self.__msg, self.__options, self.getConfig() )
        if answer is None or str(answer) == "<CANCEL>":
            return [False,None,None,None]
        
        # Cast answer if required
        vtype = self.getConfig(ValueType) 
        if vtype is not None:
            try:
                if vtype == LONG:
                    answer = int(answer)
                elif vtype == FLOAT:
                    answer = float(answer)
                elif vtype == STRING:
                    answer = str(answer)
                elif vtype == DATETIME or vtype == RELTIME:
                    answer = TIME(answer)
                else:
                    raise SyntaxException("Unknown value type: " + repr(vtype))
            except:
                raise SyntaxException("Failed casting the prompt answer")
    
        # Send the answer to the GUI if required  
        if self.getConfig(Notify) != False:
            REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, "Prompt", answer, "SUCCESS" )            
          
        return [False,answer,None,None]

    #===========================================================================
    def _doRepeat(self):
        self._write("Repeating prompt", {Severity:WARNING} )
        return [True,False]

################################################################################
class Display_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the Display wrapper.
    """    
    
    __msg = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__msg = None
        self._opName = None
    
    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        
        # Parse arguments
        if len(args)==0:
            self.__msg = ""
        else:
            self.__msg = str(args[0])
            
        if type(self.__msg)!=str:
            raise SyntaxException("Expected a message string")
        
        # Parse the severity if passed as an argument
        if len(args)==2:
            severity = args[1]
            if severity not in [INFORMATION,WARNING,ERROR,FATAL]:
                raise SyntaxException("Unknown severity given")
            self.addConfig(Severity,severity)
            
        # Mark procedure messages
        self.addConfig(Scope,SCOPE_PROC)
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._write( self.__msg, self.getConfig() )
            
        return [False,None,None,None]

################################################################################
class Notification_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the Notify wrapper.
    """    
    
    __name = None
    __value = None
    __status = None
    __items = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__msg = None
        self._opName = None
        self.__items = None
        self.__name = None
        self.__value = None
        self.__status = None
    
    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        
        # Parse arguments
        if len(args)==0:
            raise SyntaxException("Expected item information or a list of item information data, no arguments given")
        
        if type(args[0])==list:
            
            self.__items = args[0]
            
            for item in args[0]:
                if type(item) != list or len(item)!=3:
                    raise SyntaxException("Expected item information list of 3 elements")
        else:
            
            if len(args) != 3:
                raise SyntaxException("Expected item name, value and status as arguments")
            
            self.__name = str(args[0])
            self.__value = str(args[1])
            self.__status = args[2]
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        if self.__name:
            REGISTRY['EXEC'].notify( self.__name, self.__value, self.__status )
            
        elif self.__items:
            
            for item in self.__items:
                REGISTRY['EXEC'].notify( str(item[0]), str(item[1]), item[2] )

        return [False,None,None,None]

################################################################################
class WaitFor_Helper(WrapperHelper):
    
    __args = None                # Holds the arguments
    __config = None              # Holds the current config
    
    def __init__(self):
        WrapperHelper.__init__(self,"TM")
        self._opName = None
        self.__reset()

    #===========================================================================
    def __reset(self):
        self.__args = None
        self.__config = None
        
    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0 and len(kargs)==0:
            raise SyntaxException("No arguments given")
        self.__reset()
        self.__args = args
        self.__config = self.getConfig()

    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        
        self._setActionString( ACTION_SKIP   ,  "Skip the wait statement and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip the wait statement and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Repeat and wait for the condition to be fulfilled again")

        # Start the wait state
        LOG("Starting wait state")
        REGISTRY['EXEC'].startWait( self.__args, self.__config )
        
        # Wait until the scheduler allows to continue or is interrupted
        LOG("Waiting for scheduler")
        result = REGISTRY['EXEC'].wait()
        
        LOG("Waiting finished")
        return [False,result,None,None]
        
    #===========================================================================
    def _doSkip(self):
        self._write("Wait skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Wait cancelled", {Severity:WARNING} )
        return [False,False]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry wait statement", {Severity:WARNING} )
        return [True,False]

       
################################################################################
class LoadDictionary_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the LoadDictionary wrapper.
    """    
    
    __prefix = None
    __database = None
    __retry    = False
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__database = None
        self.__retry = False
        self.__prefix = None
        self._opName = "Database" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No dictionary given")

        if not self.__retry:
            # Get the database name
            self.__database = args[0]
            if type(self.__database)!=str:
                raise SyntaxException("Expected a database name")
            if not "://" in self.__database:
                raise SyntaxException("Database name must have URI format")
            idx = self.__database.find("://")
            self.__prefix = self.__database[0:idx]
        else:
            self.__retry = False
        
        idx = self.__database.find("//")
        toShow = self.__database[idx+2:]
        
        self._setActionString( ACTION_REPEAT ,  "Try to load the database " + repr(toShow) + " again")

        self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_PR, "Loading")
        self._write("Loading database " + repr(toShow))

        db = None
        try:
            db = REGISTRY['DBMGR'].loadDatabase(self.__database)
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "Loaded")
        except DriverException,ex:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "Failed")
            self._write("Failed to load database " + repr(toShow), {Severity:ERROR})
            raise ex
        
        return [False,db,NOTIF_STATUS_OK,"Database loaded"]

    #===========================================================================
    def _doRepeat(self):
        from spell.lang.functions import Prompt,Display
        Display("Load database failed, getting new name", WARNING )
        idx = self.__database.find("//")
        toShow = self.__database[idx+2:]
        newName = str(Prompt("Enter new database name (previously " + repr(toShow) + "): ", Type=ALPHA, Notify=False ))
        if not newName.startswith(self.__prefix):
            newName =  self.__prefix + "://" + newName
        self.__database = newName
        self.__retry = True
        return [True,None]

################################################################################
class SaveDictionary_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the SaveDictionary wrapper.
    """    
    
    __prefix = None
    __database = None
    __retry    = False
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__database = None
        self.__retry = False
        self.__prefix = None
        self._opName = "Database" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No dictionary given")

        if not self.__retry:
            self.__database = args[0]
            if not isinstance(self.__database, Database):
                raise SyntaxException("Expected a database object (%s)" % repr(self.__database))
        else:
            self.__retry = False
        
        toShow = self.__database.id()
        
        self._setActionString( ACTION_SKIP   ,  "Skip saving the dictionary " + repr(toShow) + " and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip saving the dictionary " + repr(toShow) + " and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to save the dictionary " + repr(toShow) + " again")

        try:
            self.__database.commit()
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "SUCCESS")
            self._write("Database " + repr(toShow) + " saved")
        except BaseException,ex:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "FAILED")
            self._write("Failed to save database " + str(ex) , {Severity:ERROR})
            raise DriverException(str(ex))
        
        return [False, True, NOTIF_STATUS_OK, "Database saved"]

    #===========================================================================
    def _doSkip(self):
        self._write("Save dictionary skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Save dictionary skipped", {Severity:WARNING} )
        return [False,False]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry saving dictionary", {Severity:WARNING} )
        return [True,None]


################################################################################
class CreateDictionary_Helper(LoadDictionary_Helper):

    """
    DESCRIPTION:
        Helper for the CreateDictionary wrapper.
    """    
    
    #===========================================================================
    def __init__(self):
        super(CreateDictionary_Helper, self).__init__()
        self.__retry    = False
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        
        if len(args)==0:
            raise SyntaxException("No dictionary given")

        if not self.__retry:
            # Get the database name
            self.__database = args[0]
            if type(self.__database)!=str:
                raise SyntaxException("Expected a database name")
            if not "://" in self.__database:
                raise SyntaxException("Database name must have URI format")
            idx = self.__database.find("://")
            self.__prefix = self.__database[0:idx]
        else:
            self.__retry = False
            
        idx = self.__database.find("//")
        toShow = self.__database[idx+2:]
        
        self._setActionString( ACTION_SKIP   ,  "Skip creating the dictionary " + repr(toShow) + " and return None")
        self._setActionString( ACTION_REPEAT ,  "Try to create the dictionary " + repr(toShow) + " again")

        self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_PR, "Creating")
        db = None

        try:
            db = REGISTRY['DBMGR'].createDatabase(self.__database)
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "Created")
        except DriverException,ex:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "Failed")
            raise ex
        
        return [False,db,NOTIF_STATUS_OK,"Database created"]

    #===========================================================================
    def _doSkip(self):
        self._write("Create dictionary skipped", {Severity:WARNING} )
        self._write("CAUTION: procedure logic may become invalid!", {Severity:WARNING} )
        return [False,None]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry creating dictionary", {Severity:WARNING} )
        return [True,None]

################################################################################
class ChangeLanguageConfig_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the ChangeLanguageConfig wrapper.
    """    

    __configurable = None
    __isInterface = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__configurable = None
        self.__isInterface = None
        self._opName = "" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        
        if len(args)==0:
            raise SyntaxError("No arguments given")
        
        self.__configurable = args[0]
        
        if isinstance(self.__configurable, Interface):
            self.__isInterface = True
        elif inspect.isfunction(self.__configurable):
            self.__isInterface = False
        else:
            raise SyntaxError("Expected a driver interface or a language function")
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # We need to reparse configuration
        self.setConfig(kargs)
        
        self._setActionString( ACTION_SKIP   ,  "Skip modifying configuration and return success (True)")
        self._setActionString( ACTION_CANCEL ,  "Skip modifying configuration and return failure (False)")
        self._setActionString( ACTION_REPEAT ,  "Try to modify the configuration again")
        
        if self.__isInterface:
            ifcName = self.__configurable.getInterfaceName()
            self._write( "Changing interface " + ifcName + " configuration:")
            for modifier in self.getConfig():
                value = self.getConfig(modifier)
                # Change the configuration source
                REGISTRY['CTX'].changeInterfaceConfig( ifcName, modifier, value )
                # Refresh the interface itself
                self.__configurable.refreshConfig()
                self._write( "    - " + modifier + "=" + repr(value))
                self._notifyValue(ifcName + ":" + modifier, repr(value), NOTIF_STATUS_OK, " ")
        else:
            funName = self.__configurable.__name__
            self._write( "Changing function " + funName + " configuration:")
            for modifier in self.getConfig():
                value = self.getConfig(modifier)
                # Change the configuration source
                REGISTRY['CTX'].changeFunctionConfig( funName, modifier, value )
                self._write( "    - " + modifier + "=" + repr(value))
                self._notifyValue(funName + ":" + modifier, repr(value), NOTIF_STATUS_OK, " ")

        return [False,True,NOTIF_STATUS_OK,"Configuration changed"]

    #===========================================================================
    def _doSkip(self):
        self._write("Configuration change skipped", {Severity:WARNING} )
        return [False,True]        

    #===========================================================================
    def _doCancel(self):
        self._write("Configuration change skipped", {Severity:WARNING} )
        return [False,False]        

    #===========================================================================
    def _doRepeat(self):
        self._write("Retry configuration change", {Severity:WARNING} )
        return [True,None]

