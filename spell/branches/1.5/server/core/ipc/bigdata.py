###############################################################################

"""
PACKAGE 
    server.core.ipc.bigdata 
FILE
    bigdata.py
    
DESCRIPTION
    Bid data chunker for messages
    
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

###############################################################################

#*******************************************************************************
# SPELL imports
#*******************************************************************************

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import traceback,sys,os,thread

################################################################################
DATA_CHUNK_SIZE = 2000


################################################################################
class DataChunker(object):

    # Holds the generators mapped by id (procId)
    __chunks = None
    # Synchronization lock
    __lock = None
    
    #===========================================================================
    def __init__(self):
        self.__lock = thread.allocate_lock()
        self.__reset()
        
    #===========================================================================                
    def __reset(self):
        try:
            self.__lock.acquire()
            if self.__chunks: self.__chunks.clear()
            self.__chunks = {}
        finally:
            self.__lock.release()

    #===========================================================================                
    def __createChunks(self, id, dataList, size):
        self.__chunks[id] = []
        for i in xrange(0, len(dataList), size):
            self.__chunks[id] += [dataList[i:i+size]]
        size = len(self.__chunks[id])
        return size
        
    #===========================================================================                
    def startChunks(self, procId, rawData):
        if len(rawData)<=DATA_CHUNK_SIZE: return 0
        totalChunks = 0
        try:
            self.__lock.acquire()
            totalChunks = self.__createChunks( procId, rawData, DATA_CHUNK_SIZE )
        finally:
            self.__lock.release()
        return totalChunks

    #===========================================================================                
    def endChunks(self, procId):
        try:
            self.__lock.acquire()
            del self.__chunks[procId]
        finally:
            self.__lock.release()

    #===========================================================================                
    def getSize(self, procId):
        totalChunks = 0
        try:
            self.__lock.acquire()
            totalChunks = len(self.__chunks[procId])
        finally:
            self.__lock.release()
        return totalChunks

    #===========================================================================                
    def getChunk(self, procId, num):
        data = None
        try:
            self.__lock.acquire()
            chunks = self.__chunks[procId]
            if num>=len(chunks):
                data = None
            else:
                data = chunks[num]
        finally:
            self.__lock.release()
        return data
