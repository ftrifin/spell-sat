###################################################################################
## MODULE     : core.registry.processRegistry
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Process registry
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
# System Imports
#*******************************************************************************
import os
import cPickle #Object persistency

from process import ProcessInfo

class ProcessRegistry():
    
    #Name to access the file
    __name = None
    __registryDirectory = None
    #ProcessInfo collection
    __processMap = {}
    
    def __init__(self, name, warm = False):
        '''
        Constructor
        '''
        try:
            warmDir = os.environ['SPELL_SYS_DATA'] + os.sep + 'Runtime'
        except KeyError, keyErr: #SPELL_SYS_DATA has not been defined
            raise Exception('SPELL_WARM variable undefined')
        
        self.__name = warmDir + os.sep + name
        # If start is not warm, we clear the previous registry
        if (warm == False):
            self.__clear()
        
    def addProcess(self, pid, name, arguments = ""):
        '''
        Add a process to the registry
        '''
        info = ProcessInfo(name, arguments, pid)
        #open the process
        try:
            map = self.__load()
        except:
            map = {}
        map[info.getName()] = info
        #Store the new process
        self.__save(map)
    
    def removeProcess(self, name):
        '''
        Remove process from the registry
        '''
        #open the shelf
        try:
            map = self.__load()
        except:
            map = {}
        if name in map:
            del map[name]
            self.__save(map)

    def getProcesses(self):
        '''
        Return ProcessInfo objects contained in the persistent element
        '''
        try:
            #open the process
            map = self.__load()
            return map.values()
        except RegistryLoadException, rle: #File does not exist
            return []
         
    def __load(self):
        '''
        Load the process map from the file
        '''
        try:
            processMapFile = open(self.__name, 'rb')
        except IOError, io:
            raise RegistryLoadException('File ' + self.__name + ' does not exist')
        processMap  = cPickle.load(processMapFile)
        processMapFile.close()
        return processMap
    
    def __save(self, processMap):
        '''
        Dump the processes map to the file
        '''
        processMapFile = open(self.__name, 'wb')
        cPickle.dump(processMap, processMapFile)
        processMapFile.close()
    
    def __clear(self):
        '''
        Remove all the elements from the persistent layer
        '''
        if os.path.exists(self.__name):
            os.remove(self.__name)
           
class RegistryLoadException(Exception):
    '''
    RegistryLoadexception
    '''
    
    #Message to show
    __message = None
    
    def __init__(self, message):
        '''
        Constructor
        '''
        self.__message = message
        
    def __str__(self):
        '''
        String representation for the exception
        '''
        return "[RegistryLoadException]\t" + self.__message
