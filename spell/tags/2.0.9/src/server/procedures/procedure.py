###################################################################################
## MODULE     : procedures.procedure
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Procedure model
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
from properties import *

#*******************************************************************************
# System Imports
#*******************************************************************************
import os.path,sys
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************


###############################################################################
class Procedure(object):
    
    """
    DESCRIPTION:
        Represents the XML entity describing all the properties of a 
        satellite procedure. If this set of properties is to be extended,
        this should be done by means of adding new field names to the 
        PROC_PROPERTIES variable.
    """

    __procId = None
    __filename = None
    __source = []
    __properties = {}
    __bytecode = None

    #==========================================================================
    def __init__(self, id, properties, filename):
        self.__filename = filename
        self.__properties = properties
        self.__procId = id
        if not self.__properties.has_key(PROC_NAME):
            self.__properties[PROC_NAME] = os.path.basename(filename).split(".")[0]
        for key in PROPERTIES_1L + PROPERTIES_NL:
            if not self.__properties.has_key(key):
                self.__properties[key] = "N/A"
        self.__bytecode = None
        self.__source = []
        self.__properties['file'] = filename

    #==========================================================================
    def properties(self):
        return self.__properties

    #==========================================================================
    def __getitem__(self, name):
        return self.__properties[name]

    #==========================================================================
    def __setitem__(self, name, value):
        self.__properties[name] = value

    #==========================================================================
    def getId(self):
        return self.__procId

    #==========================================================================
    def name(self):
        return self.__properties[PROC_NAME]
    
    #==========================================================================
    def getSource(self, refresh = False):
        if (len(self.__source)==0) or refresh:
            LOG("Loading procedure body: " + self.getId())
            f = open(self.__filename, 'rt')
            try:
                self.__source = []
                for line in f:
                    line = line.strip('\r\n')
                    self.__source.append(line)
            finally:
                f.close()
            LOG("Source code loaded: " + self.__filename)
        return self.__source
    
    #==========================================================================
    def getCode(self, refresh = False):
        if self.__bytecode and not refresh:
            return self.__bytecode
        s = "\n"
        source = s.join(self.getSource(refresh)) + "\n"
        try:
            self.__bytecode = compile(source, self.getFilename(), 'exec')
        except SyntaxError,ex:
            LOG("Could not compile " + self.getFilename(), LOG_ERROR)
            LOG(repr(ex), LOG_ERROR)
            self.__bytecode = None
            from manager import ProcError
            raise ProcError("Unable to compile '" + self.getId() + "', error in line " + str(ex.lineno), "Syntax error")
        except NameError,ex:
            LOG("Could not compile " + self.getFilename(), LOG_ERROR)
            LOG(repr(ex), LOG_ERROR)
            self.__bytecode = None
            from manager import ProcError
            raise ProcError("Unable to compile '" + self.getId() + "', error in line " + str(ex.lineno), "Name error")
        except TypeError,ex:
            LOG("Could not compile " + self.getFilename(), LOG_ERROR)
            LOG(repr(ex), LOG_ERROR)
            self.__bytecode = None
            from manager import ProcError
            raise ProcError("Unable to compile '" + self.getId() + "', error in line " + str(ex.lineno), "Type error")
        except Exception,ex:
            LOG("Could not compile " + self.getFilename(), LOG_ERROR)
            LOG(repr(ex), LOG_ERROR)
            self.__bytecode = None
            from manager import ProcError
            raise ProcError("Unable to compile '" + self.getId() + "'", repr(ex))
        return self.__bytecode
        
    #==========================================================================
    def getFilename(self):
        return self.__filename

    #==========================================================================
    def getSpacecraft(self):
        return self.__properties[PROC_SC] 

    #==========================================================================
    def isLoadable(self):
        return self.__properties[PROC_LOADABLE]
