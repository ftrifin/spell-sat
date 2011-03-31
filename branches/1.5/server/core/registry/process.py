################################################################################

"""
PACKAGE 
    server.core.registry
FILE
    process.py
    
DESCRIPTION
    process info
    
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
class ProcessInfo():
    '''
    Process info will store all the information related to process launched
    '''
    
    # Program name
    __name = None
    # Program argument
    __args = None
    # Program pid
    __pid = None
    
    def __init__(self, name, args, pid):
        '''
        Constructor
        '''
        self.__name = name
        self.__args = args
        self.__pid  = pid
        
    def getName(self):
        '''
        Process name
        '''
        return self.__name

    def getArgs(self):
        return self.__args
    
    def getPID(self):
        return self.__pid
        
    def __str__(self):
        '''
        Return the command line to launch the process
        '''
        return self.__name + "[" + str(self.__pid) + "]" + self.__args
