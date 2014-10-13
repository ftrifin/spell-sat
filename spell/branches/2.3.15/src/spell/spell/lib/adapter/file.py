###################################################################################
## MODULE     : spell.lib.adapter.file
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: File wrapper
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
import os,sys
from spell.lang.constants import READ,WRITE,READ_WRITE,APPEND
from spell.lib.exception import DriverException

###################################################################################
class File():
    
    __filename = None
    __file = None
    __fd = None
    __mode = None
    
    #==============================================================================
    def __init__(self, filename):
        self.__filename = os.path.abspath(filename)
        self.__file = None
        self.__mode = None
        self.__fd = None 
        
    #==============================================================================
    def filename(self):
        return self.__filename
    
    #==============================================================================
    def basename(self):
        return os.path.basename(self.__filename)
    
    #==============================================================================
    def dirname(self):
        return os.path.dirname(self.__filename)
    
    #==============================================================================
    def exists(self):
        return os.path.exists(self.__filename)
    
    #==============================================================================
    def isdir(self):
        return os.path.isdir(self.__filename)
    
    #==============================================================================
    def isfile(self):
        return os.path.isfile(self.__filename)
    
    #==============================================================================
    def canWrite(self):
        return os.access(self.__filename, os.W_OK)
    
    #==============================================================================
    def canRead(self):
        return os.access(self.__filename, os.R_OK)
    
    #==============================================================================
    def isOpen(self):
        return (self.__file is not None) or (self.__fd is not None)
    
    #==============================================================================
    def __list(self):
        children = []
        try:
            children = os.listdir(self.__filename)
        except Exception,ex:
            raise DriverException("Cannot list children of " + repr(self.__filename), str(ex))
        
        return children
        
    #==============================================================================
    def open(self, mode = READ ):
        
        if self.isOpen():
            return
        
        if mode == READ:
            
            if not self.exists():
                raise DriverException("Cannot open " + repr(self.__filename) + " for read", "Not found")
    
            if not self.canRead():
                raise DriverException("Cannot open " + repr(self.__filename) + " for read", "Permission denied")
            
        elif mode == READ_WRITE or mode == WRITE or mode == APPEND:
            
            if self.isdir():
                raise DriverException("Cannot open " + repr(self.__filename) + " in this mode", "It is a directory")
        
            if self.exists():
                
                if not self.canWrite():
                    raise DriverException("Cannot open " + repr(self.__filename) + " for R/W", "Permission denied")
            else:
                if not os.access(self.dirname(), os.W_OK):
                    raise DriverException("Cannot open " + repr(self.__filename) + " for R/W", "Permission denied on parent directory " + repr(self.dirname()))

        try:
            
            if self.isdir():
                self.__fd = os.open(self.__filename, os.O_RDONLY)
            else:
                modeStr = 'r'
                if mode == READ:
                    pass
                elif mode == WRITE:
                    modeStr = 'w'
                    if not self.exists(): modeStr = 'a' + modeStr
                elif mode == READ_WRITE:
                    modeStr = 'rw'
                    if not self.exists(): modeStr = 'a' + modeStr
                elif mode == APPEND:
                    modeStr  = 'a'
                else:
                    raise DriverException("Cannot open " + repr(self.__filename), "Unsupported mode: " + repr(mode))
                self.__file = file(self.__filename, modeStr)

            self.__mode = mode
        
        except DriverException,ex:
            self.__file = None
            self.__fd = None
            raise ex
        except Exception,ex:
            self.__file = None
            self.__fd = None
            raise DriverException("Cannot open " + repr(self.__filename), str(ex))
        
    #==============================================================================
    def close(self):
        try:
            if self.isOpen():
                if self.isfile():
                    self.__file.close()
                else:
                    os.close(self.__fd)
        except Exception,ex:
            raise DriverException("Cannot close " + repr(self.__filename), str(ex))

    #==============================================================================
    def flush(self):
        try:
            if not self.isOpen():
                raise DriverException("Cannot flush file", "File is not open")
            if self.isdir():
                raise DriverException("Cannot flush file", "It is a directory")
            if not self.canWrite():
                raise DriverException("Cannot flush file", "Permission denied")
            if self.__mode != WRITE and self.__mode != READ_WRITE and self.__mode != APPEND:
                raise DriverException("Cannot flush file", "Incorrect file mode")

            self.__file.flush()

        except DriverException,ex:
            raise ex
            
        except Exception,ex:
            raise DriverException("Cannot flush file " + repr(self.__filename), str(ex))

    #==============================================================================
    def write(self, data):
        try:
            if not self.isOpen():
                raise DriverException("Cannot write to file", "File is not open")
            if self.isdir():
                raise DriverException("Cannot write to file", "It is a directory")
            if not self.canWrite():
                raise DriverException("Cannot write to file", "Permission denied")
            if self.__mode != WRITE and self.__mode != READ_WRITE and self.__mode != APPEND:
                raise DriverException("Cannot write to file", "Incorrect file mode")
            if type(data)!=str:
                raise DriverException("Cannot write to file", "Expected a string as input, received " + repr(type(data)))

            self.__file.write(data)

        except DriverException,ex:
            raise ex
            
        except Exception,ex:
            raise DriverException("Cannot write to file " + repr(self.__filename), str(ex))
        
    #==============================================================================
    def writeln(self, data):
        if type(data)!=str:
            raise DriverException("Cannot write to file", "Expected a string as input, received " + repr(type(data)))
        self.write(data + "\n")


    #==============================================================================
    def read(self):
        if not self.isOpen():
            raise DriverException("Cannot read file lines", "File is not open")
        if not self.canRead():
            raise DriverException("Cannot read file lines", "Permission denied")
        
        if self.isdir():
            return self.__list()
        else:
            if self.__mode != READ and self.__mode != READ_WRITE:
                raise DriverException("Cannot read file lines", "Incorrect file mode")
            lines = []
            try:
                lines = self.__file.readlines()
            except Exception,ex:
                raise DriverException("Cannot read file lines", str(ex))
            return lines
    
    #==============================================================================
    def truncate(self):
        if not self.isOpen():
            raise DriverException("Cannot truncate file", "File is not open")
        if self.isdir():
            raise DriverException("Cannot truncate file", "It is a directory")
        if not self.canWrite():
            raise DriverException("Cannot truncate file", "Permission denied")
        if self.__mode != WRITE and self.__mode != READ_WRITE and self.__mode != APPEND:
            raise DriverException("Cannot truncate file", "Incorrect file mode")
        try:
            self.__file.truncate()
        except Exception,ex:
            raise DriverException("Cannot truncate file", str(ex))
