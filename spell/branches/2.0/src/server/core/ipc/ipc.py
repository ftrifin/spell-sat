###################################################################################
## MODULE     : core.ipc.ipc
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: IPC core module
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

#*******************************************************************************
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from server.core.messages.msghelper import MsgHelper
from server.core.messages.base import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import thread,sys,time
from threading import Thread
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
class IPCerror(BaseException): pass
class IPCconnectionLost(BaseException): 
    
    message = None
    def __init__(self, msg):
        self.message = msg
 
#*******************************************************************************
# Module globals
#*******************************************************************************

IPC_BUFFER_SIZE  = 65
IPC_ENCODING     = "!I"
IPC_KEY_ENCODING = "!h"
IPC_DEBUG        = False
IPC_MSG_DEBUG    = False

################################################################################
class IPCworker(Thread):
    
    name = None
    isWorking = True
    __lock = None
    
    #===========================================================================
    def __init__(self, name):
        Thread.__init__(self)
        self.__isWorking = True
        self.name = name
        self.__lock = thread.allocate_lock()
        
    #===========================================================================
    def work(self):
        raise NotImplemented
        
    #===========================================================================
    def working(self, w = None):
        self.__lock.acquire()
        if w is not None:
            if IPC_DEBUG: LOG( self.name + ": Set working flag to " + str(w), level = LOG_COMM)
            self.__isWorking = w
        else:
            w = self.__isWorking
        self.__lock.release()
        return w
    
    #===========================================================================
    def run(self):
        if IPC_DEBUG: LOG( self.name + ": ################# Starting comm loop " + self.getName(), level = LOG_COMM)
        while self.working(): self.work()  
        if IPC_DEBUG: LOG( self.name + ": ################# Finished comm loop " + self.getName(), level = LOG_COMM)
