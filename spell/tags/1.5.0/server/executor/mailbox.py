################################################################################

"""
PACKAGE 
    server.executor.mailbox
FILE
    mailbox.py
    
DESCRIPTION
    Command mailbox for the executor
    
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

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
from Queue import Queue,Empty,Full
import threading

################################################################################
class CommandMailBox(object):

    #===========================================================================
    def __init__(self):
        self.__queue = Queue(10)
        self.__hqueue = Queue(3)
        self.__lock = threading.Event()
    
    #===========================================================================
    def push(self, item, high_priority = False ):
        """
        ------------------------------------------------------------------------
        Push a command in the mailbox. There are two queues, high priority and
        normal priority queues. 
        ------------------------------------------------------------------------
        """
        if high_priority:
            self.__hqueue.put(item, True)
        else:
            self.__queue.put(item, True)
        self.__lock.set()
        
    #===========================================================================
    def pull(self):
        """
        ------------------------------------------------------------------------
        Pull a command from the mailbox.
        ------------------------------------------------------------------------
        """
        # If there is a priority item, return it
        item = None
        while item is None:
            self.__lock.wait()
            if item is None:
                try:
                    item = self.__hqueue.get(False)
                except Empty:
                    # Otherwise wait for the next item
                    try:
                        item = self.__queue.get(False)
                    except Empty:pass
            if item is None:
                self.__lock.clear()
        return item
            
    