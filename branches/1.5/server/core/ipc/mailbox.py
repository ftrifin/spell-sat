################################################################################

"""
PACKAGE 
    server.core.ipc.mailbox
FILE
    mailbox.py
    
DESCRIPTION
    Mailbox for storing messages
    
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

#*******************************************************************************
# Local Imports
#*******************************************************************************

from Queue import Queue,Empty
import thread
import sys

#*******************************************************************************
# System Imports
#*******************************************************************************

#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class Mailbox(object):
    
    __name__     = None
    __queueMap__ = None
    __lock__     = None
    
    #===========================================================================
    def __init__(self, name):
        self.__name__ = name
        self.__lock__ = thread.allocate_lock()
        self.__queueMap__ = {}

    #===========================================================================
    def prepare(self, id):
        # Create a blocking queue of one element for storing the request
        # response when it arrives 
        self.__lock__.acquire()
        try:
            if not id in self.__queueMap__:
                self.__queueMap__[id] = Queue(1)
        finally:
            self.__lock__.release()

    #===========================================================================
    def place(self, id, message):
        # Place an incomming response in the queue. If the ID does not match,
        # it is because there was a timeout and the queue was removed before
        # this response came in.
        q = self.__getQueue(id)
        if (q != None): 
            try:
                q.put(message,True)
            except:
                err = "Error when setting response: " + id
                sys.stderr.write("\n\n" + err + "\n\n")
                LOG(err, LOG_ERROR )
        else:
            err = "Discarding outdated response: " + id
            sys.stderr.write("\n\n" + err + "\n\n")
            LOG(err, LOG_ERROR, level = LOG_COMM )
        
    #===========================================================================
    def retrieve(self, id, timeout):
        # Block the caller until the response with the given ID arrives
        # Do not wait more than the given timeout. 
        q = self.__getQueue(id)
        message = None
        if (q != None):
            try:
                if timeout == 0:
                    # None value makes to wait 
                    # until a new message comes
                    timeout = None 
                message = q.get(True,timeout)
                self.__clean(id)
            except:
                err = "Timeout when recovering message"
                sys.stderr.write("\n" + err + "\n")
                LOG(err, LOG_ERROR, level = LOG_COMM )
                message = None
            if message == False: return None
        return message
        
    #===========================================================================
    def shutdown(self):
        # Clear and remove all queues. By putting False on each open queue
        # we tell the request processor that the request has been cancelled.
        self.__lock__.acquire()
        for id in self.__queueMap__:
            self.__queueMap__[id].put(False)
        self.__lock__.release()
        
    #===========================================================================
    def __getQueue(self, id):
        # Obtain an existing queue using the given ID. Will return None if
        # the queue does not exist.
        q = None
        self.__lock__.acquire()
        try:
            if id in self.__queueMap__:
                q = self.__queueMap__.get(id)
            else:
                err = "No such queue: " + id
                LOG(err, LOG_ERROR, level = LOG_COMM)
                sys.stderr.write(err + "\n")
        finally:
            self.__lock__.release()
        return q
        
    #===========================================================================
    def __clean(self, id):
        # Remove the given queue
        self.__lock__.acquire()
        try:
            if id in self.__queueMap__:
                del self.__queueMap__[id]
        finally:
            self.__lock__.release()

        
        
        
        
        
