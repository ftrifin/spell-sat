################################################################################

"""
PACKAGE 
    server.executor.execthread
FILE
    procdata.py
    
DESCRIPTION
    Procedure data used by the executor
    
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
# Keys
#*******************************************************************************
# Name key
PROC_DATA_NAME = "NAME"
# Args key
PROC_DATA_ARGS = "ARGS"
# Mode key
PROC_DATA_MODE = "MODE"
# Parent key
PROC_DATA_PARENT = "PARENT"

class ProcData:
    '''
    ProcData objects will provide information about the procedure to the 
    executor
    '''
    
    __data = None
    
    def __init__(self, name, args, mode, parent):
        '''
        Constructor
        '''
        self.__data = {}
        # procedure name
        self.__data[PROC_DATA_NAME] = name
        # arguments used for launching the procedure
        self.__data[PROC_DATA_ARGS] = args
        # mode in which the procedure will be launched
        self.__data[PROC_DATA_MODE] = mode
        # procedure's parent, if any
        self.__data[PROC_DATA_PARENT] = parent
    
        #===========================================================================
    def __getitem__(self, key):
        if not self.__data.has_key(key):
            return "Unknown"
        return self.__data.get(key)