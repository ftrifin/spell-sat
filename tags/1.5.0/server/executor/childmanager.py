################################################################################

"""
PACKAGE 
    server.executor.childmanager
FILE
    childmanager.py
    
DESCRIPTION
    Manages child executor processes
    
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
LOG.showlog = False

# Procedure language imports
from spell.lib.adapter.value import *
from spell.lang.modifiers import *

# Messages
from server.core.messages.base import *
from server.core.messages.msghelper import MsgHelper
from server.core.ipc.xmlmsg import *
import server.core.messages.executor as ExcMessages
import server.core.messages.context as CtxMessages

# Control
from server.procedures.manager import *
from spell.config.reader import *

# Other
from spell.lib.exception import DriverException,CoreException
import server.core.messages.executor as Messages
import server.core.messages.context as CtxMessages
from spell.lib.registry import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from status import *
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import os,re,sys,traceback,time,thread
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = ['ChildManager']

################################################################################
class ChildManager(object):

    # Holds the child identifier
    childProc = None
    # Holds the child status
    childStatus = None
    # Holds the child alive flag
    childAlive = False
    # Hold error information, if any
    childError = None
    childReason = None
    # Sync lock
    lock = None

    #===========================================================================
    def __init__(self):
        self.lock = thread.allocate_lock()
        self.reset()
        
    #===========================================================================
    def reset(self):
        self.childProc = None
        self.childStatus = None
        self.childAlive = False
        self.childError = None
        self.childReason = None

    #===========================================================================
    def processChildRequest(self, msg):
        response = MsgHelper.createResponse( msg.getId(), msg )
        if msg.getType() == MSG_TYPE_NOTIFY:
            if msg[FIELD_DATA_TYPE]==ExcMessages.DATA_TYPE_STATUS:
                self.lock.acquire()
                try:
                    self.childStatus = msg[ExcMessages.FIELD_EXEC_STATUS]
                    if self.childStatus == ERROR:
                        self.childError  = msg[FIELD_ERROR]
                        self.childReason = msg[FIELD_REASON]
                    elif self.childStatus == ABORTED:
                        self.childError = "Execution did not finish"
                        self.childError = "Procedure was aborted"
                finally:
                    self.lock.release()
                LOG("Current child status: " + repr(self.childStatus), level = LOG_PROC)
        return response

    #===========================================================================
    def processChildMessage(self, msg):
        if msg.getId() == CtxMessages.MSG_EXEC_OP:
            operation = msg[CtxMessages.FIELD_EXOP]
            if operation in [CtxMessages.DATA_EXOP_CLOSE,
                                               CtxMessages.DATA_EXOP_KILL]:
                self.lock.acquire()
                try:
                    if self.childAlive: 
                        self.childStatus = ERROR
                        self.childError = "Subprocedure lost"
                        if operation == CtxMessages.DATA_EXOP_CLOSE:
                            self.childReason = "Closed by somebody else"
                        else:
                            self.childReason = "Killed by somebody else"
                    self.childAlive = False
                finally:
                    self.lock.release()
        elif msg.getType() == MSG_TYPE_ERROR:
            self.lock.acquire()
            try:
                self.childError  = msg[FIELD_ERROR]
                self.childReason = msg[FIELD_REASON]
                self.childStatus = ERROR
            finally:
                self.lock.release()
            

    #===========================================================================
    def openChildProcedure(self, procId, arguments = {}, config = {} ):

        procList = ProcedureManager.instance().getProcList()

        # We assume that the list of procedures is ordered by priority already
        procRe = re.compile("(.*" + re.escape(os.sep) + ")*" + procId + "\Z")
        suitableProc = filter( lambda x: (procId == x.split("|")[1]) or (procRe.match(x.split("|")[0]) is not None), procList )

        if len(suitableProc) == 0:
            raise DriverException("Unable to launch subprocedure " + procId,
                                "No suitable identifier found")

        # Get the proc id of the first suitable proc
        suitableProc = suitableProc[0].split("|")[0]

        #TODO: these shall be in config files
        if not Automatic in config: config[Automatic] = True
        if not Blocking in config: config[Blocking] = True
        if not Visible in config: config[Visible] = True

        LOG("Starting subprocedure " + suitableProc)
        LOG("Open mode: " + repr(config))

        # Open the procedure
        openmsg = MessageClass()
        openmsg.setType(MSG_TYPE_REQUEST)
        openmsg.setId(CtxMessages.REQ_OPEN_EXEC)
        openmsg[CtxMessages.FIELD_SPROC_ID] = suitableProc
        openmsg[CtxMessages.FIELD_OPEN_MODE] = str(config)
        openmsg[ExcMessages.FIELD_ARGS] = str(arguments)

        resp = REGISTRY['CIF'].sendRequest(openmsg)
        if resp.getType() == MSG_TYPE_ERROR:
            self.childError  = resp[FIELD_ERROR]
            self.childReason = resp[FIELD_REASON]
            raise CoreException(self.childError,self.childReason)
        
        self.childProc = suitableProc
        self.childAlive = True
        self.childStatus = LOADED
        
        return True

    #===========================================================================
    def closeChildProcedure(self):
        # Close the procedure
        closemsg = MessageClass()
        closemsg.setType(MSG_TYPE_REQUEST)
        closemsg.setId(CtxMessages.REQ_CLOSE_EXEC)
        closemsg[CtxMessages.FIELD_PROC_ID] = self.childProc
        self.reset()
        resp = REGISTRY['CIF'].sendRequest(closemsg)
        if resp.getType() == MSG_TYPE_ERROR:
            self.childError  = resp[FIELD_ERROR]
            self.childReason = resp[FIELD_REASON]
            raise CoreException(self.childError,self.childReason)
        return True
        
    #===========================================================================
    def killChildProcedure(self):
        # Close the procedure
        killmsg = MessageClass()
        killmsg.setType(MSG_TYPE_REQUEST)
        killmsg.setId(CtxMessages.REQ_KILL_EXEC)
        killmsg[CtxMessages.FIELD_PROC_ID] = self.childProc
        self.reset()
        resp = REGISTRY['CIF'].sendRequest(killmsg)
        if resp.getType() == MSG_TYPE_ERROR:
            self.childError  = resp[FIELD_ERROR]
            self.childReason = resp[FIELD_REASON]
            raise CoreException(self.childError,self.childReason)
        return True

    #===========================================================================
    def messageToChildProcedure(self, msg ):
        #TODO: implement
        pass

    #===========================================================================
    def requestToChildProcedure(self, msg ):
        #TODO: implement
        pass

    #===========================================================================
    def hasChild(self):
        return (self.childProc is not None)
        
    #===========================================================================
    def getChildStatus(self):
        self.lock.acquire()
        st = self.childStatus
        self.lock.release()
        return st

    #===========================================================================
    def getChildError(self):
        self.lock.acquire()
        error = self.childError
        reason = self.childReason
        self.lock.release()
        return [error,reason]

    #===========================================================================
    def isAlive(self):
        self.lock.acquire()
        alive = self.childAlive
        self.lock.release()
        return alive
