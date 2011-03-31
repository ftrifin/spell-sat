###############################################################################

"""
PACKAGE 
    spell.lang.helpers.genhelper 
FILE
    genhelper.py
    
DESCRIPTION
    Helpers for general features
    
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
from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.adapter.constants.core import *
from spell.lib.adapter.constants.notification import *
from spell.utils.ttime import *
from spell.lib.registry import *
from spell.lib.adapter.database import Database
from spell.lib.adapter.interface import Interface

#*******************************************************************************
# Local Imports
#*******************************************************************************
from basehelper import WrapperHelper

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
        self._opName = "User Input" 
        
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
        ptype = self._config.get(Type)
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
        if (self._config.has_key(Timeout)):
            tov = self._config.get(Timeout)
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

    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        # Parse arguments
        from spell.lang.functions import Abort
        
        answer = self._prompt( self.__msg, self.__options, self._config )
        if answer is None or str(answer) == "<CANCEL>":
            return [False,None]
        
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
                elif vtype == DATETIME:
                    answer = TIME(answer)
                else:
                    raise SyntaxException("Unknown value type: " + repr(vtype))
            except:
                raise SyntaxException("Failed casting the prompt answer")
    
        # Send the answer to the GUI if required  
        if self.getConfig(Notify) != False:
            REGISTRY['CIF'].notify( NOTIF_TYPE_VAL, "Prompt", answer, "SUCCESS" )            
          
        return [False,answer]

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
    __severity = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__msg = None
        self.__severity = None
        self._opName = "Message display" 
    
    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        # Parse arguments
        if len(args)==0:
            raise SyntaxException("No message given")
        self.__msg = args[0]
        if type(self.__msg)!=str:
            raise SyntaxException("Expected a message string")
        # Parse the severity if passed as an argument
        if len(args)==2:
            self.__severity = args[1]
            if self.__severity not in [INFORMATION,WARNING,ERROR,FATAL]:
                raise SyntaxException("Unknown severity given")
            self._config.update({Severity:self.__severity})
        
        
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        self._write( self.__msg, self._config )
            
        return [False,None]

    
################################################################################
class WaitFor_Helper(WrapperHelper):
    
    # For time wait condition --------------------------------------------------
    
    # Stores the time where the wait started. May change if the user pauses and retries
    __startTime = None
    # Stores the time where the wait shall finish. May change if the user pauses and retries
    __finalTime = None
    
    # Notification data
    __intervalList = None
    __notificationMessage = None
    __lastNotificationTime = None
    __timeToShowFormat = None
    
    # True if we are using relative times
    __relativeTime = False
    
    # For TM wait condition ----------------------------------------------------
    __verifyList = None
    __verifyDelay = None
    __verifyTimeout = None
    __verifyFailure = None

    __doUpdates = True
    
    # Stores the condition to check
    __condition = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self._opName = "Wait"

        self.__condition = None
        self.__doUpdates = True
        
        self.__intervalList = None
        self.__startTime = None
        self.__finalTime = None
        self.__lastNotificationTime = None
        self.__timeToShowFormat = None
        self.__notificationMessage = None
        self.__relativeTime = False
        
        self.__verifyList = None
        self.__verifyDelay = None
        self.__verifyTimeout = None
        self.__verifyFailure = None

    #===========================================================================
    def __waitTime(self, theTime ):

        self.__startTime = TIME(NOW)
        # This time is used for interval notifications
        self.__lastNotificationTime = self.__startTime
        
        # (1) SANITY CHECKS ----------------------------------------------------
        # If it is a string, try to cast to time instance
        if type(theTime) in [str, int, float]:
            theTime = TIME(theTime)
            
        # If the given param is a time instance
        if isinstance(theTime,TIME):
            if theTime.isRel():
                self.__relativeTime = True
                self.__finalTime = self.__startTime + theTime.rel()
                remaining = theTime.rel()
                self.__timeToShowFormat = "COUNTDOWN"
                timeToShow = str(TIME(remaining)).split(".")[0]
                targetTime = str(self.__finalTime).split(".")[0]
                comment = "Target time: " + targetTime 
                self._write("Starting countdown, " + timeToShow + " seconds to " + targetTime, {Severity:INFORMATION} )
            else:
                self.__relativeTime = False
                self.__finalTime = theTime
                remaining = (self.__finalTime - self.__startTime).rel()
                self.__timeToShowFormat = "ETA"
                # Define the target time string
                timeToShow = str(self.__finalTime).split(".")[0]
                targetTime = str(TIME(remaining)).split(".")[0]
                comment = "Remaining time: " + targetTime 
                self._write("Waiting until " + timeToShow + ", " + targetTime + " seconds left", {Severity:INFORMATION} )
        # Error
        else:
            raise SyntaxException("Malformed time argument")

        self._notifyTime(self.__timeToShowFormat, timeToShow, "WAITING", comment)

        # (3) START THE WAIT ---------------------------------------------------

        # Start the time condition wait
        self._startWait( self.time_callback )

        # Wait the finish event
        self._wait()

        # (4) LAST USER FEEDBACK -----------------------------------------------

        # Once finished, if it is a relative time, ensure we display a remaining time of zero
        if self.__relativeTime:
            timeToShow = str(TIME(0)).split(".")[0]
        # Notify the end of the operation
        self._notifyTime(self.__timeToShowFormat, timeToShow, "SUCCESS", "Target time reached")
        self._write("Target time reached", {Severity:INFORMATION})

        return True

    #===========================================================================
    def __waitVerify(self, verifyList, delay = None ):

        self.__verifyList = verifyList
        self.__verifyDelay = delay
        self.__startTime = TIME(NOW)
        # This time is used for interval notifications
        self.__lastNotificationTime = self.__startTime
        
        self._notifyValue( "TM CONDITION", "Waiting", NOTIF_STATUS_PR, "Waiting telemetry condition(s)")
        self._write("Waiting for telemetry condition(s)")
        
        # Start the time condition wait if first try does not succeed
        if (self.verify_callback() != True):
            self._startWait( self.verify_callback, period = 0 )
        
        # Wait the finish event
        self._wait()

        if self.__verifyTimeout:
            self._notifyValue( "TM CONDITION", "Failed", NOTIF_STATUS_FL, "Condition not fullfilled on time")
            self._write("Telemetry condition(s) not fullfilled, time limit exceeded", {Severity:ERROR})
            return False
        if self.__verifyFailure:
            self._notifyValue( "TM CONDITION", "Failed", NOTIF_STATUS_FL, "Condition not fullfilled")
            self._write("Telemetry condition(s) not fullfilled due to error", {Severity:ERROR})
            raise DriverException(self.__verifyFailure.message, self.__verifyFailure.reason)

        #Notify the success
        self._notifyValue( "TM CONDITION", "Fullfilled", NOTIF_STATUS_OK, "Condition fullfilled")
        if (isinstance(verifyList[0], list)):
            for condition in verifyList:
                self._notifyValue( condition[0], str(condition[1]) + " " + str(condition[2]), NOTIF_STATUS_OK, "Condition fullfilled")
        else:
            self._notifyValue( verifyList[0], str(verifyList[1]) + " " + str(verifyList[2]), NOTIF_STATUS_OK, "Condition fullfilled")
        self._write("Telemetry condition(s) fullfilled")
        
        return True

    #===========================================================================
    def time_callback(self):
        now = NOW.abs()
        remaining = self.__finalTime - NOW
        
        if remaining <= 0:
            return True

        # Search the applicable interval
        for intv in self.__intervalList:
            if isinstance(intv,TIME):
                intTime = intv.rel()
            else:
                intTime = intv
            # If the remaining time is greater than interval limit 
            if remaining >= intTime:
                # Then notify if the time since last notification is gt/equal to intTime
                if (now - self.__lastNotificationTime >= intTime):
                    if self.__relativeTime:
                        timeToShow = str(remaining).split(".")[0]
                        comment = "Target time: " + str(self.__finalTime).split(".")[0] 
                        self._notifyTime(self.__timeToShowFormat, timeToShow, "WAITING", comment)
                    else:
                        timeToShow = str(self.__finalTime).split(".")[0]
                        comment = "Remaining time: " + str(remaining).split(".")[0] 
                        self._notifyTime(self.__timeToShowFormat, timeToShow, "WAITING", comment)
                    self.__lastNotificationTime = now
                    
                    if self.__notificationMessage:
                        self._write(self.__notificationMessage, {Severity:INFORMATION} )
                break
        
        return False

    #===========================================================================
    def verify_callback(self):
        configDict = self._config.copy()
        configDict[Retries]        = 1
        configDict[Wait]           = True
        configDict[Notify]         = False
        configDict[OnFalse]        = NOACTION
        if configDict.has_key(Delay): configDict.pop(Delay)

        now = NOW.abs() 
        nowStr = str(NOW).split(".")[0]
        
        interval = self.getConfig(Interval)
        if interval is None:
            interval = 2
        elif type(interval)==list:
            interval = interval[0]
        
        if (NOW - self.__lastNotificationTime >= interval):
            if self.getConfig(Message) is None:
                self._write("Waiting for telemetry condition(s), current time: " + nowStr)            
            else:
                self._write(self.getConfig(Message))
            self.__lastNotificationTime = NOW
        
        try:
            # Ensure OnFalse/OnTrue behaviors are NOACTION
            configDict[OnFalse] = NOACTION
            configDict[OnTrue]  = NOACTION
            
            # We need to manually setup verbosity here (not using a helper)
            REGISTRY['CIF'].setVerbosity(999)
            
            # If the verification is success, finish the check 
            if REGISTRY['TM'].verify( self.__verifyList, configDict ):
                self.__verifyTimeout = False
                self.__verifyFailure = None
                self._finishWait()
                return True
        except DriverException,ex:
            # If there is a failure in verification 
            self.__verifyTimeout = False
            self.__verifyFailure = ex
            return True
        finally:
            REGISTRY['CIF'].setVerbosity(configDict.get(Verbosity))

        # Check if the verification goes to timeout, if there is a delay
        if self.__verifyDelay:
            if (NOW-self.__startTime)>=self.__verifyDelay:
                self.__verifyFailure = None
                self.__verifyTimeout = True
                return True
        
        return False

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        if len(args)==0 and len(kargs)==0:
            raise SyntaxException("No arguments given")
        if len(args)>1:
            raise SyntaxException("Too many arguments given")
        
        useConfig = {}
        useConfig.update(self._config)

        self.__condition = None
        
        # Parse condition if given with positional argument
        if len(args)==1:
            self.__condition = args[0]
        # Using keyword arguments
        else:
            delayCondition = self.getConfig(Delay)
            untilCondition = self.getConfig(Until) 
            
            # Check that at one of the two is given
            if delayCondition is None and untilCondition is None:
                raise SyntaxError("Expected Delay or Until")

            # Check that not both are given
            if delayCondition and untilCondition:
                raise SyntaxError("Cannot use both Delay and Until")

            # Check Until condition correctness
            elif untilCondition:
                if not isinstance(untilCondition,TIME):
                    raise SyntaxError("Malformed time condition for Until")
                if untilCondition.isRel():
                    raise SyntaxError("Cannot use relative times with Until")

            # Check Delay condition correctness
            elif delayCondition:
                if isinstance(delayCondition, TIME):
                    if not delayCondition.isRel():
                        raise SyntaxError("Cannot use absolute times with Delay")
                elif type(delayCondition) not in [int,float]:
                    raise SyntaxError("Malformed time condition for Delay")

            # Assign the condition
            if delayCondition: self.__condition = delayCondition
            elif untilCondition: self.__condition = untilCondition
            else:
                raise SyntaxError("Malformed condition")
            
        # Check if we have interval definition 
        if self._config.has_key(Interval):
            self.__intervalList = self._config.get(Interval)
            # If the interval is not actually a list, it shall be a TIME instance
            # or an integer/float:
            if (type(self.__intervalList) != list):
                if (not type(self.__intervalList) in [int,float]) and\
                   (not isinstance(self.__intervalList,TIME)):
                    raise SyntaxException("Malformed interval definition")
                self.__intervalList = [self.__intervalList]
            
            # Get the message, if any
            if self._config.has_key(Message):
                self.__notificationMessage = self._config.get(Message)
            else:
                self.__notificationMessage = None
        else:
            # If there is no interval defined, we want updates each second
            self.__intervalList = [1]
        
            
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        # Now parse the condition
        if (type(self.__condition) in [str,int,float]) or isinstance(self.__condition,TIME):
            result = self.__waitTime(self.__condition)
        elif type(self.__condition)==list:
            delay = self.getConfig(Delay)
            if delay and isinstance(delay,TIME): 
                if not delay.isRel():
                    raise SyntaxError("Cannot use absolute times with Delay")
                delay = delay.rel()
            result = self.__waitVerify(self.__condition,delay)
        else:
            raise SyntaxError("Expected a time value or a verification list")

        return [False,result]

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
        self._write("Retry WaitFor", {Severity:WARNING} )
        return [True,False]


################################################################################
class Pause_Helper(WrapperHelper):
    
    __condition = None
    __timeout = None
    __configDict = {}
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self, "TM")
        self.__condition = None
        self.__timeout = None
        self.__configDict = {}
        self._opName = "Pause" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):

        if len(args)==0:
            return
        
        useConfig = {}
        useConfig.update(self._config)

        # Since args is a tuple we have to convert it to a list for TM.verify        
        if len(args)!=1:
            # The case of giving a simple step for verification
            self.__condition = [ item for item in args ]
        else:
            # Givin a step or a step list
            self.__condition = args[0]

        self.__timeout = self.getConfig(Timeout)
        if self.__timeout is None:
            raise SyntaxException("Timeout is required")
            
        self.__configDict = {}
        self.__configDict.update(self._config)
        self.__configDict[Wait] = False
        self.__configDict[HandleError] = False
        self.__configDict[AllowInterrupt] = True
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):

        if len(args)==0:
            if REGISTRY.exists('EXEC'):
                REGISTRY['EXEC'].pause()
            return [False,True]
        
        startTime = NOW.abs() #time.time()

        from spell.lang.functions import Verify
        while(True):
            result = Verify( self.__condition, self.__configDict )
            if result and REGISTRY.exists('EXEC'):
                REGISTRY['EXEC'].pause() 
                return [False,True]

            time.sleep(0.5)

            currTime = NOW.abs() #time.time()
            if (currTime-startTime)>self.__timeout:
                self._write("Not pausing, condition timed-out", {Severity:WARNING})
                return [False,False]

    #===========================================================================
    def _doSkip(self):
        self._write("Pause SKIPPED", {Severity:WARNING} )
        return [False,True]        
                
    #===========================================================================
    def _doRepeat(self):
        self._write("Retry Pause", {Severity:WARNING} )
        return [True,False]

################################################################################
class SetExecDelay_Helper(WrapperHelper):

    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self._opName = "Exec Delay" 
    
    #===========================================================================
    def _doOperation(self, *args, **kargs):
        
        if not REGISTRY.exists('EXEC'): return
        delay = None
        delaySO = None
        if len(args)==0:
            if not kargs.has_key('delay'): 
                raise SyntaxException("No arguments given")
            if kargs.has_key('delay'):
                delay = kargs.get('delay')
        else:
            delay = args[0]
        
        if delay is not None:
            self._write("Setting execution delay to " + str(delay), {Severity:WARNING})
            REGISTRY['EXEC'].setExecutionDelay(delay)
            
        return [False,None]
        
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
        self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_PR, "Loading")
        self._write("Loading database " + repr(toShow))
        db = REGISTRY['DBMGR'].loadDatabase(self.__database)
        if db:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "Loaded")
        else:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "Failed")
            self._write("Failed to load database " + repr(toShow), {Severity:ERROR})
            return [True, None]
        
        return [False,db]

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
        failed = False
        
        try:
            self.__database.commit()
        except:
            failed = True
            
        if failed:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "FAILED")
            self._write("Failed to save database " + repr(toShow) , {Severity:ERROR})
            return [True, False]
        else:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "SUCCESS")
            self._write("Database " + repr(toShow) + " saved")
        
        return [False, True]

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
        self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_PR, "Creating")
        db = REGISTRY['DBMGR'].createDatabase(self.__database)
        if db:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_OK, "Created")
        else:
            self._notifyValue( "Database", repr(toShow), NOTIF_STATUS_FL, "Failed")
        
        return [False,db]

################################################################################
class Script_Helper(WrapperHelper):

    """
    DESCRIPTION:
        Helper for the Script wrapper.
    """    
    
    __code = None
    
    #===========================================================================
    def __init__(self):
        WrapperHelper.__init__(self)
        self.__code = None
        self._opName = "Script" 

    #===========================================================================
    def _doPreOperation(self, *args, **kargs ):
        
        if len(args)==0:
            raise SyntaxError("No arguments given")
        
        if type(args[0])!=str:
            raise SyntaxError("Expected a source code string")
        
        self.__code = args[0]
    
    #===========================================================================
    def _doOperation(self, *args, **kargs ):
        result = REGISTRY['EXEC'].script(self.__code)
        return [False,result]

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
        self._config = kargs
        
        if self.__isInterface:
            ifcName = self.__configurable.getInterfaceName()
            self._write( "Changing interface " + ifcName + " configuration:")
            for modifier in self._config:
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
            for modifier in self._config:
                value = self.getConfig(modifier)
                # Change the configuration source
                REGISTRY['CTX'].changeFunctionConfig( funName, modifier, value )
                self._write( "    - " + modifier + "=" + repr(value))
                self._notifyValue(funName + ":" + modifier, repr(value), NOTIF_STATUS_OK, " ")

        return [False,True]

