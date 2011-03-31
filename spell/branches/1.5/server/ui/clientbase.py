################################################################################

"""
PACKAGE 
    server.ui.clientbase
FILE
    clientbase.py
    
DESCRIPTION
    Interface definition for SPEL clients
    
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
from spell.utils.ttime import *

from spell.lib.adapter.config import Configurable
from spell.config.reader import *
from spell.lib.adapter.constants.notification import *
from spell.lib.adapter.constants.core import *

from spell.lib.registry import *
from spell.lib.exception import *

# Messaging and comm
from server.core.messages.base import *
from server.core.ipc.xmlmsg import *
import server.core.messages.executor
import server.core.messages.context
import server.executor.status

from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import time,sys,os
import re
from datetime import datetime

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************
# Shortcuts
ExcMessages = server.core.messages.executor
CtxMessages = server.core.messages.context

# Defaults
DISPLAY_DEFAULTS = { Severity:INFORMATION, 
                       Scope:SCOPE_PROC,
                       Type:DISPLAY }

# Used from UI only
SCRIPT = "SCRIPT"

PROMPT_DEFAULTS = { Type:OK_CANCEL }

__all__ = ['ClientInterfaceBase']

################################################################################
class ClientInterfaceBase(Configurable):

    """
    DESCRIPTION:
        TODO description
    """

    # Ready flag
    ready = False
    # Holds the asrun file handle
    __asRUN = None
    # Manual mode flag
    __manualMode = False
    # Optimization: message to reuse for line notifications
    __lineNotifyMsg = None
    # Optimization: message to reuse for item notifications
    __itemNotifyMsg = None
    # Optimization: message to reuse for item notifications
    __statusNotifyMsg = None
    # Optimization: message to reuse for code notifications
    __codeNotifyMsg = None
    # Optimization: message to reuse for write messages
    __writeMsg = None
    # Verbosity filter level
    __vFilter = 10
    # Currently used verbosity
    __verbosity = None

    
    #==========================================================================
    def __init__(self):
        Configurable.__init__(self)
        self.__asRUN = None
        self.__manualMode = False
        self.ready = False
        self.__lineData = None
        self.__itemData = None
        self.__statusData = None
        self.__codeData = None
        self.__lineNotifyMsg = None
        self.__itemNotifyMsg = None
        self.__statusNotifyMsg = None
        self.__codeNotifyMsg = None
        self.__writeMsg = None
        self.__verbosity = None
        self.__vFilter = 10
            
    #==========================================================================
    def setup(self, ctxName, failMode = False ):
        LOG("Setting up base UI client interface")

        ctxConfig = Config.instance().getContextConfig(ctxName)
        execConfig = ctxConfig.getExecutorConfig()
        self.__vFilter = execConfig.get("MaxVerbosity")
        if self.__vFilter is None:
            self.__vFilter = 10
        LOG("Maximum verbosity: " + repr(self.__vFilter))

        if not failMode:
            LOG("Add to registry")
            if REGISTRY.exists('RSC'):
                REGISTRY['RSC'].addResourceStatusCallback(self)
    
            self._prepareAsRun()
            
            self._toAsRun( str(datetime.now())[:-3], "INIT")

        REGISTRY['CIF'] = self
        LOG("Client interface ready")
            
    #==========================================================================
    def cleanup(self):
        LOG("Client interface cleaned")

    #==========================================================================
    def setManualMode(self, inManualMode):
        self.__manualMode = inManualMode

    #==========================================================================
    def notifyLine(self, stack = None, stage = None ):
        if not self.ready: return
        
        notificationTime = str(datetime.now())[:-3]
        csp = self._getCSP()

        if self.__lineNotifyMsg is None:
            self.__lineNotifyMsg = MessageClass()
            self.__lineNotifyMsg.setType(MSG_TYPE_NOTIFY)
            self.__lineNotifyMsg.setId(MSG_TYPE_NOTIFY)
            self.__lineNotifyMsg[ExcMessages.FIELD_PROC_ID] = self._getProcId()
            self.__lineNotifyMsg[FIELD_DATA_TYPE] = ExcMessages.DATA_TYPE_LINE  
        
        self.__lineNotifyMsg[ExcMessages.FIELD_CSP] = csp
        
        if stage:
            self.__lineNotifyMsg[ExcMessages.FIELD_STAGE_ID] = stage[0]
            self.__lineNotifyMsg[ExcMessages.FIELD_STAGE_TL] = stage[1]
            self._toAsRun( notificationTime, "STAGE", stage[0] + "," + stage[1] )
        else:
            self.__lineNotifyMsg[ExcMessages.FIELD_STAGE_ID] = ""
            self.__lineNotifyMsg[ExcMessages.FIELD_STAGE_TL] = ""
            
        self.__lineNotifyMsg[FIELD_TIME] = notificationTime

        if stack:
            self._toAsRun( notificationTime, "LINE", stack )
        else:
            self._toAsRun( notificationTime, "LINE", csp )
        
        if self.__manualMode:
            self.__lineNotifyMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_MANUAL
        else:
            self.__lineNotifyMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_PROCEDURE
        
        #LOG("Notify LINE to peer (" + procId + ")", level = LOG_COMM)
        self._sendNotificationMessage(self.__lineNotifyMsg)

    #==========================================================================
    def notifyStatus(self, status_code, condition = None ):
        if not self.ready: return

        notificationTime = str(datetime.now())[:-3]

        if self.__statusNotifyMsg is None:
            self.__statusNotifyMsg = MessageClass()
            self.__statusNotifyMsg.setType(MSG_TYPE_NOTIFY)
            self.__statusNotifyMsg.setId(MSG_TYPE_NOTIFY)
            self.__statusNotifyMsg[ExcMessages.FIELD_PROC_ID] = self._getProcId()
            self.__statusNotifyMsg[FIELD_DATA_TYPE] = ExcMessages.DATA_TYPE_STATUS 
             
        self.__statusNotifyMsg[ExcMessages.FIELD_EXEC_STATUS] = status_code 
        self.__statusNotifyMsg[FIELD_TIME] = notificationTime 
        
        if condition:
            self.__statusNotifyMsg[ExcMessages.FIELD_CONDITION] = str(condition)

        self._toAsRun( notificationTime, "STATUS", status_code )

        if self.__manualMode:
            self.__statusNotifyMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_MANUAL
        else:
            self.__statusNotifyMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_PROCEDURE

        LOG("Notify STATUS " + repr(status_code), level = LOG_COMM)
        self._sendNotificationMessage(self.__statusNotifyMsg)
        
    #===========================================================================
    def notifyCode(self, script, stack ):
        if not self.ready: return
    
        notificationTime = str(datetime.now())[:-3]
        csp = self._getCSP()
        
        if self.__codeNotifyMsg is None:
            self.__codeNotifyMsg = MessageClass()
            self.__codeNotifyMsg.setType(MSG_TYPE_NOTIFY)
            self.__codeNotifyMsg.setId(MSG_TYPE_NOTIFY)
            self.__codeNotifyMsg[ExcMessages.FIELD_PROC_ID] = self._getProcId()
            self.__codeNotifyMsg[FIELD_DATA_TYPE] = ExcMessages.DATA_TYPE_CODE 
                 
        self.__codeNotifyMsg[ExcMessages.FIELD_PROC_CODE] = script
        self.__codeNotifyMsg[ExcMessages.FIELD_CSP] = csp
        self.__codeNotifyMsg[FIELD_TIME] = notificationTime 
        
        self._toAsRun( notificationTime, "CODE", stack )
        
        if self.__manualMode:
            self.__codeNotifyMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_MANUAL
        else:
            self.__codeNotifyMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_PROCEDURE

        LOG("Notify CODE", level = LOG_COMM)
        self._sendNotificationMessage(self.__codeNotifyMsg)

    #===========================================================================
    def notify(self, type, name, value, status, reason = " ", time = " "):
        if not self.ready: return
        if reason == "": reason = " "

        notificationTime = str(datetime.now())[:-3]
        csp = self._getCSP()

        # Multiple notifications
        if ITEM_SEP in status:
            statusList = status.split(ITEM_SEP)
            sCount = len(filter( lambda x: x == NOTIF_STATUS_OK, statusList ))
            # Ensure the list of comments is correct
            if reason.strip(ITEM_SEP).strip() == "":
                rList = [" "] * len(statusList)
                reason = ITEM_SEP.join(rList)
            # Ensure the list of times is correct
            if time.strip(ITEM_SEP).strip() == "":
                tList = [notificationTime] * len(statusList)
                time = ITEM_SEP.join(tList)
        else:
            if status == NOTIF_STATUS_OK:
                sCount = 1 
            else:
                sCount = 0

        if self.__itemNotifyMsg is None:
            self.__itemNotifyMsg = MessageClass()
            self.__itemNotifyMsg.setType(MSG_TYPE_NOTIFY)
            self.__itemNotifyMsg.setId(MSG_TYPE_NOTIFY)
            self.__itemNotifyMsg[ExcMessages.FIELD_PROC_ID] = self._getProcId()
            self.__itemNotifyMsg[FIELD_DATA_TYPE] = ExcMessages.DATA_TYPE_ITEM 

        self.__itemNotifyMsg[NOTIF_ITEM_TYPE]       = type
        self.__itemNotifyMsg[NOTIF_ITEM_NAME]       = name
        self.__itemNotifyMsg[NOTIF_ITEM_VALUE]      = value
        self.__itemNotifyMsg[NOTIF_ITEM_STATUS]     = status 
        self.__itemNotifyMsg[NOTIF_ITEM_REASON]     = reason
        self.__itemNotifyMsg[NOTIF_ITEM_TIME]       = time
        self.__itemNotifyMsg[FIELD_SCOUNT]          = sCount
        self.__itemNotifyMsg[ExcMessages.FIELD_CSP] = csp
        self.__itemNotifyMsg[FIELD_TIME]            = notificationTime 
        
        self._toAsRun( notificationTime, "ITEM", csp, type, name, value, status, reason, time )
        
        if self.__manualMode:
            self.__itemNotifyMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_MANUAL
        else:
            self.__itemNotifyMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_PROCEDURE
        
        LOG("Notify " + type, level = LOG_COMM)
        self._sendNotificationMessage(self.__itemNotifyMsg)

    #===========================================================================
    def notifyError(self, msg, reason):
        if not self.ready: return
        
        data = { ExcMessages.FIELD_PROC_ID: self._getProcId(), 
                 FIELD_DATA_TYPE: ExcMessages.DATA_TYPE_STATUS, 
                 ExcMessages.FIELD_EXEC_STATUS: 'ERROR',
                 FIELD_ERROR: msg,
                 FIELD_REASON: reason }
        
        notificationTime = str(datetime.now())[:-3]
        
        self._toAsRun( notificationTime, "ERROR", self._getCSP(), msg, "", "", reason )
        
        LOG("Notify error to peer (" + self._getProcId() + ")", level = LOG_COMM)

        errormsg = MessageClass()
        errormsg.setProps(data)
        errormsg.setType(MSG_TYPE_ERROR)
        errormsg.setId(MSG_TYPE_ERROR)
        errormsg[FIELD_TIME] = notificationTime
        errormsg[ExcMessages.FIELD_CSP] = self._getCSP()
        
        if self.__manualMode:
            errormsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_MANUAL
        else:
            errormsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_PROCEDURE
        
        self._sendMessage(errormsg)

    #===========================================================================
    def write(self, message, config = {} ):
        if not self.ready: return

        if self.__verbosity is not None:
            if self.__verbosity > self.__vFilter: return

        csp = self._getCSP()

        if self.__writeMsg is None:
            self.__writeMsg = MessageClass()
            self.__writeMsg.setType(MSG_TYPE_WRITE)
            self.__writeMsg.setId(MSG_TYPE_WRITE)
            self.__writeMsg[ExcMessages.FIELD_PROC_ID] = self._getProcId()
            
        level = config.get(Severity)
        if level is None:
            level = DISPLAY_DEFAULTS[Severity]
            
        # Set the message type. If we are in manual mode,
        # the type is forced to be SCRIPT.
        if (self.__manualMode):
            dtype = SCRIPT
        else:
            dtype  = config.get(Type)

        if dtype is None:
            dtype = DISPLAY_DEFAULTS[Type]
        
        if level == INFORMATION: level = "INFO"
        elif level == WARNING:   level = "WARN"
        elif level == ERROR:     level = "ERROR"
        elif level == FATAL:     level = "FATAL"
        else:                    level = "UKN"
            
        # If the message is multiline, replace the \n codes
        message = message.replace("\r\n","%C%")
        message = message.replace("\n","%C%")
            
        self.__writeMsg[ExcMessages.FIELD_TEXT] = message
        self.__writeMsg[ExcMessages.FIELD_LEVEL] = level
        self.__writeMsg[ExcMessages.FIELD_MSGTYPE] = dtype
        self.__writeMsg[ExcMessages.FIELD_CSP] = csp
        
        self._toAsRun( str(datetime.now())[:-3], "DISPLAY", csp, message, level )
        
        if self.__manualMode:
            self.__writeMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_MANUAL
        else:
            self.__writeMsg[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_PROCEDURE
        
        self._sendWriteMessage(self.__writeMsg)

    #===========================================================================
    def prompt(self, message, options = [], config = {} ):
        if not self.ready: return
        LOG("Send prompt message to peer", level = LOG_COMM)
        
        useConfig = self.buildConfig( [], config, {}, PROMPT_DEFAULTS )
        
        ptype  = useConfig.get(Type)
        
        tosend = MessageClass()
        tosend.setType(MSG_TYPE_PROMPT)
        tosend.setId(MSG_TYPE_PROMPT)
        tosend[ExcMessages.FIELD_PROC_ID] = self._getProcId()
        tosend[ExcMessages.FIELD_TEXT] = message
        tosend[FIELD_DATA_TYPE] = ptype

        # Build the expected options list if required 
        if (ptype == NUM) or (ptype == ALPHA) or (ptype == DATE):
            tosend[ExcMessages.FIELD_EXPECTED] = ''
        elif (ptype & LIST)>0: 
            if len(options)>0:
                separatorPosition = None
                expected = []
                texts = []
                keyCount = 0
                for opt in options:
                    keyCount += 1
                    separator = opt.find(KEY_SEPARATOR)
                    if separatorPosition is None:
                        separatorPosition = separator
                    # This means some options doesn't have : and some other do
                    elif (separator * separatorPosition) < 0:
                        raise SyntaxException("Prompt error", "Options shall have the same syntax structure")
                    if separator<0:
                        # Put internal keys in this case
                        pair = [str(keyCount),opt]
                    else:
                        pair = opt.split(KEY_SEPARATOR,1)
                    if (ptype & ALPHA)>0:
                        expected.append(pair[1])
                    else:
                        expected.append(pair[0])
                    texts.append(pair[0] + KEY_SEPARATOR + pair[1])
                tosend[ExcMessages.FIELD_EXPECTED] = '|'.join(expected)
                tosend[ExcMessages.FIELD_OPTIONS] = '|'.join(texts)
        else:
            if ptype == OK:
                tosend[ExcMessages.FIELD_EXPECTED] = 'O'
                tosend[ExcMessages.FIELD_OPTIONS] = 'O' + KEY_SEPARATOR +' Ok'
            elif ptype == CANCEL:
                tosend[ExcMessages.FIELD_EXPECTED] = 'C'
                tosend[ExcMessages.FIELD_OPTIONS] = 'C' + KEY_SEPARATOR +' Cancel'
            elif ptype == YES:
                tosend[ExcMessages.FIELD_EXPECTED] = 'Y'
                tosend[ExcMessages.FIELD_OPTIONS] = 'Y' + KEY_SEPARATOR +' Yes'
            elif ptype == NO:
                tosend[ExcMessages.FIELD_EXPECTED] = 'N'
                tosend[ExcMessages.FIELD_OPTIONS] = 'N' + KEY_SEPARATOR +' No'
            elif ptype == YES_NO:
                tosend[ExcMessages.FIELD_EXPECTED] = 'Y|N'
                tosend[ExcMessages.FIELD_OPTIONS] = 'Y' + KEY_SEPARATOR +' Yes|N' + KEY_SEPARATOR +' No'
            elif ptype == OK_CANCEL:
                tosend[ExcMessages.FIELD_EXPECTED] = 'O|C'
                tosend[ExcMessages.FIELD_OPTIONS] = 'O' + KEY_SEPARATOR +' Ok|C' + KEY_SEPARATOR +' Cancel'
        
        #Timeout
        tosend[ExcMessages.FIELD_PROC_ID] = self._getProcId()
        to = config.get(Timeout)
                
        LOG("Prompt timeout: " + str(to))

        asRunMessage = message.strip("\n")
        asRunMessage = asRunMessage.replace("\t", "")
        breakPos = asRunMessage.find("\n")
        if breakPos != -1: #If there is a break
            asRunMessage = asRunMessage[0:breakPos] + "..."
        self._toAsRun( str(datetime.now())[:-3], "PROMPT", self._getCSP(), asRunMessage )
    
        if self.__manualMode:
            tosend[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_MANUAL
        else:
            tosend[ExcMessages.FIELD_EXECUTION_MODE] = ExcMessages.DATA_EXEC_MODE_PROCEDURE
        
        LOG("Sending prompt now", level = LOG_COMM)
    
        toProcess = self._sendPromptMessage(tosend, timeout = to )

        if toProcess is None:
            self._toAsRun(tosend,"None")
            LOG("No response obtained")
            return "<CANCEL>"
        elif toProcess in ["<CANCEL>","<ERROR>","<TIMEOUT>"]:
            LOG("Prompt cancelled by user or due to an error")
            return "<CANCEL>"
        
        LOG("Obtained value: " + repr(toProcess))

        toShow = toProcess
        processedValue = toProcess
        # Process received message
        if (ptype & NUM)>0:
            try:
                processedValue = int(toProcess)
            except ValueError:
                processedValue = float(toProcess)
        elif (ptype & DATE):
            LOG("Processing datetime value")
            if type(toProcess)==str:
                processedValue = TIME(toProcess)
                toShow = str(processedValue)
            elif type(toProcess) in [int,float]:
                processedValue = TIME(toProcess)
                toShow = str(processedValue)
            else:
                raise SyntaxException("Invalid value for date/time")
            LOG("Processed datetime: " + repr(toProcess))
        elif((ptype == YES_NO)
                or (ptype == YES)
                or (ptype == NO)
                or (ptype == OK)
                or (ptype == CANCEL)
                or (ptype == OK_CANCEL)):
            LOG("Processing boolean value")
            if toProcess.upper() == "TRUE": 
                processedValue=True
                toShow = "TRUE"
            elif toProcess.upper() == "Y": 
                processedValue=True
                toShow = "YES"
            elif toProcess.upper() == "O": 
                processedValue=True
                toShow = "OK"
            elif toProcess.upper() == "C": 
                processedValue=False
                toShow = "CANCEL"
            elif toProcess.upper() == "N": 
                processedValue=False
                toShow = "NO"
            elif toProcess.upper() == "FALSE": 
                processedValue=False
                toShow = "FALSE"
            else: processedValue=False

        self._toAsRun( str(datetime.now())[:-3], "ANSWER", self._getCSP(), repr(processedValue) )

        LOG("Processed value: " + repr(processedValue))
        self.write("User answer: " + repr(toShow))

        return processedValue

    #===========================================================================
    def setVerbosity(self, verbosity = None):
        if verbosity is None:
            self.__verbosity = self.__vFilter
        else:
            self.__verbosity = verbosity

    #===========================================================================
    def resetVerbosity(self):
        self.__verbosity = None

    #===========================================================================
    def getAsRun(self):
        if self.__asRUN:
            return self.__asRUN.getFilename()
        return None

    #===========================================================================
    def clearAsRun(self):
        if self.__asRUN:
            theFile = self.__asRUN.getFilename()
            fd = os.open(theFile, os.O_CREAT | os.O_RDWR | os.O_TRUNC )
            os.close(fd)
            self._toAsRun( str(datetime.now())[:-3], "INIT")
        return None

    #===========================================================================
    def sendRequest(self, msg):
        return self._sendRequest(msg)

    #===========================================================================
    def _prepareAsRun(self):
        try:
            timestamp = self._getTimeID()
            aname = self._getProcId().replace("\\","_").replace("/","_")
            asRunName = "ar://" + aname + "_" + timestamp
            self.__asRUN = REGISTRY['DBMGR'].createDatabase( asRunName ) 
        except Exception,ex:
            LOG("Unable to create AsRun file: " + str(ex), LOG_ERROR )
            self.__asRUN = None
        
    #===========================================================================
    def _toAsRun(self, *args, **kwargs):
        if self.__asRUN:
            self.__asRUN.write(*args,**kwargs)

    #===========================================================================
    def _sendNotificationMessage(self, msg):
        raise NotImplemented()
    
    #===========================================================================
    def _sendWriteMessage(self, msg):
        raise NotImplemented()
    
    #===========================================================================
    def _sendPromptMessage(self, msg):
        raise NotImplemented()

    #===========================================================================
    def _sendMessage(self, msg):
        raise NotImplemented()

    #===========================================================================
    def _sendRequest(self, msg):
        raise NotImplemented()

    #===========================================================================
    def _getProcId(self):
        raise NotImplemented()

    #===========================================================================
    def _getCSP(self):
        raise NotImplemented()

    #===========================================================================
    def _getTimeID(self):
        raise NotImplemented()

    #===========================================================================
    def _getAsRun(self):
        if self.__asRUN:
            return self.__asRUN.getFilename()
        else:
            return " " 
    
    #===========================================================================
    def _notificationCmdline(self, msg):
        type = msg[FIELD_DATA_TYPE]
        if type == ExcMessages.DATA_TYPE_ITEM:
            name =   msg[NOTIF_ITEM_NAME]
            value =  msg[NOTIF_ITEM_VALUE]
            status = msg[NOTIF_ITEM_STATUS]
            reason = msg[NOTIF_ITEM_REASON]
            if ",," in name:
                nameItems   = name.split(",,")
                valueItems  = value.split(",,")
                statusItems = status.split(",,")
                reasonItems = reason.split(",,")
                print "---------------------------------------------------------------------"
                print "[!] MULTIPLE ITEM"
                for idx in range(0,len(nameItems)):
                    name   = nameItems[idx]
                    value  = valueItems[idx]
                    status = statusItems[idx]
                    reason = reasonItems[idx] 
                    print "         %-15s %-25s %-15s %s" % (name,value,status,reason)
                print "---------------------------------------------------------------------"
            else:
                print "---------------------------------------------------------------------"
                print "[!] NOTIFY ITEM:         %-15s %-25s %-15s %s" % (name,value,status,reason)
                print "---------------------------------------------------------------------"
        elif type == ExcMessages.DATA_TYPE_LINE:
            print "[!] CURRENT LINE ",msg[ExcMessages.FIELD_CSP]
        elif type == ExcMessages.DATA_TYPE_STATUS:
            print "[!] STATUS ",msg[ExcMessages.FIELD_EXEC_STATUS]
        elif type == ExcMessages.DATA_TYPE_CODE:
            print "[!] CODE ",msg[ExcMessages.FIELD_CSP]
        else:
            print "---------------------------------------------------------------------"
            print "[!] NOTIFY: ",msg.data() 
            print "---------------------------------------------------------------------"

    #===========================================================================
    def _writeCmdLine(self,msg):
        print msg[ExcMessages.FIELD_TEXT]

    #===========================================================================
    def _promptCmdLine(self, msg):
        print "---------------------------------------------------------------------"
        print "[?] PROMPT ", msg[ExcMessages.FIELD_TEXT]
        print "    Options : ",msg[ExcMessages.FIELD_OPTIONS]
        print "    Expected: ",msg[ExcMessages.FIELD_EXPECTED]
        print
        toProcess = None
        try:
            toProcess = raw_input(">")
        except:
            toProcess = None
        print "---------------------------------------------------------------------"
        print "User input: ",repr(toProcess)
        print "---------------------------------------------------------------------"
        return toProcess
        
################################################################################

