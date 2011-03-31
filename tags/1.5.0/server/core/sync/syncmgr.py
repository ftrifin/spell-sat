################################################################################

"""
PACKAGE 
    server.core.sync.syncmgr
FILE
    syncmgr.py
    
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
from syncthread import SyncThreadClass
from syncpeer import SyncPeerClass
 
#*******************************************************************************
# System Imports
#*******************************************************************************
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = [ 'MxMgr' ]

SYNC_THREAD = "SYNC_THREAD"

__instance__ = None

################################################################################
class SyncMgrClass(object):

    __syncList = None
    __syncType = None
    
    #===========================================================================
    def __init__(self):
        self.__syncList = {}
        self.__syncType = SYNC_THREAD
    
    #==========================================================================
    @staticmethod
    def instance():
        global __instance__
        if __instance__ is None:
            __instance__ = SyncMgrClass()
        return __instance__

    #===========================================================================
    def setup(self, syncType):
        self.__syncList = {}
        self.__syncType = syncType

    #===========================================================================
    def cleanup(self):
        pass

    #===========================================================================
    def allocateLock(self, syncName):
        if syncName not in self.__syncList:
            if self.__syncType == SYNC_THREAD:
                lock = SyncThreadClass(syncName)
            else:
                lock = SyncPeerClass(syncName)
            self.__syncList[syncName] = lock
        return self.__syncList[syncName]
    
    #===========================================================================
    def deallocateLock(self, syncName):
        if syncName in self.__syncList:
            self.__syncList.remove(syncName)
    
    #===========================================================================
    def getLockIndex(self, sync):
        return self.__syncList.index(sync)

################################################################################
MxMgr = SyncMgrClass
