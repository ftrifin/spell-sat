################################################################################

"""
PACKAGE 
    server.core.sync.syncthread
FILE
    syncthread.py
    
DESCRIPTION
    
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
from syncbase import *
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import threading
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class SyncThreadClass(SyncBaseClass):
    __sync = None
    __name = None
    
    #===========================================================================
    def __init__(self, name):
        self.__name = name
        self.__sync = threading.Event()
    
    #===========================================================================
    def wait(self, prefix = None):
        if prefix is not None:
            LOG(repr(prefix) + ' waiting for ' + repr(self.__name))

        res = self.__sync.wait()
        
        if prefix is not None:
            LOG(repr(prefix) + ' waited ' + repr(self.__name))
            
        return res

    #===========================================================================
    def release(self, prefix = None):
        if prefix is not None:
            LOG(repr(prefix) + ' released ' + repr(self.__name))

        return self.__sync.set()

    #===========================================================================
    def acquire(self, prefix = None):
        if prefix is not None:
            LOG(repr(prefix) + ' acquiring ' + repr(self.__name))
        
        res = self.__sync.clear()
       
        if prefix is not None:
            LOG(repr(prefix) + ' acquired ' + repr(self.__name))
        
    #===========================================================================
    def locked(self, prefix = None):
        if prefix is not None:
            LOG(repr(prefix) + ' checking ' + repr(self.__name))
        
        res = self.__sync.isSet()
       
        return res
