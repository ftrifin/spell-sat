################################################################################

"""
PACKAGE 
    server.procedures.manager
FILE
    manager.py
    
DESCRIPTION
    Procedure manager for SPEL processes
    
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
from spell.config.reader import *
from spell.config.constants import CONTEXT
from spell.lib.exception import CoreException
from spell.utils.memoize import memoize

#*******************************************************************************
# Local Imports
#*******************************************************************************
from procedure import Procedure
from parser import HeaderParser,ParseError,NotLoadable
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import os,string,os.path,sys
from os.path import dirname
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
class ProcError(CoreException): pass
class NotLoaded(CoreException): pass

#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = [ 'ProcedureManager', 'ProcError', 'NotLoaded' ]

__instance__ = None

# Optimization for procedure test (isProcDir method)

# 1. Check the string 
################################################################################
def __ptest__(f,x):
    return dirname(f).startswith(x)

# 2. Memoize the mapping to all procedure paths
################################################################################
@memoize
def __isproc__(f,paths):
    return any(__ptest__(f,x) for x in paths)

################################################################################
class ProcedureManagerClass(object):
    """
    DESCRIPTION:
        
    """
    # Path(s) to procedure folders
    __procpath = None
    # Path(s) to user library folders
    __libpath = None
    # Procedure objects
    __procedures = {}
    # Library objects
    __libraries = {}
    # The list of procedure id/name pairs for loadable procedures
    __loadableProcedures = []
    # Shortcut to obtain ID from filename
    __filenameToId = {}
    __pathToId = {}
    __parser = None
    __ctxInfo = None

    #==========================================================================
    @staticmethod
    def instance():
        global __instance__
        if __instance__ is None:
            __instance__ = ProcedureManagerClass()
        return __instance__

    #==========================================================================
    def __init__(self):
        self.__parser = None
        self.__pathToId = {}
        self.__procedures = {}
        self.__libraries = {}
        self.__loadableProcedures = []
        self.__procpath = None
        self.__libpath = None
        self.__ctxInfo = None
        self.__filenameToId = {}

    #==========================================================================
    def cleanup(self):
        LOG("Cleaning up")
        self.__pathToId = {}
        self.__procedures = {}
        self.__libraries = {}
        self.__loadableProcedures = []
        self.__filenameToId = {}
        self.__procpath = None
        self.__parser = None
        self.__libpath = None
    
    #==========================================================================
    def setup(self, contextName):
        LOG("Setting up")
        
        self.__ctxInfo = Config.instance().getContextConfig(contextName)
        
        # Get the defined procedure paths
        self.__obtainProcPath()
        
        # Get the defined library paths
        self.__obtainLibPath()
        
        # Get the defined procedure properties
        k1L, kNL = self.__getDefinedProperties() 
        # And build the header parser
        self.__parser = HeaderParser(k1L,kNL)

        spacecraft = self.__ctxInfo.getSC()
        LOG("Loading procedures for spacecraft " + spacecraft)
        self.refresh()        
        LOG("Ready")

    #==========================================================================
    def __obtainProcPath(self):
        # Proc path can be a semicolon-separated list of paths
        self.__procpath = None
        procpath = self.__ctxInfo.getProcPath()
        
        # Ensure that there is at least one path defined
        if procpath is None:
            raise ProcError("Cannot load","Procedure path not defined")
        
        paths = procpath.split(";")
        self.__procpath = [] 
        for path in paths:
            self.__procpath += [Config.instance().resolvePath(path)]

        if len(self.__procpath)==0:
            raise ProcError("Cannot load","Procedure path not defined")
        for path in self.__procpath:
            if not os.path.exists(path):
                raise ProcError("Cannot load","Procedure path not found: '" + path + "'")
            LOG("Using procpath: " + repr(path))

    #==========================================================================
    def __obtainLibPath(self):
        # Library path can be a semicolon-separated list
        self.__libpath = None 
        libpath = self.__ctxInfo.getLibPath()
        
        if libpath and len(libpath)>0 and libpath != 'None':
            libpath = libpath.split(";")
            self.__libpath = []
            for path in libpath:
                lpath = Config.instance().resolvePath(path)
                if not os.path.exists(lpath):
                    LOG("User library path not found: '" + lpath + "'", LOG_WARN)
                else:
                    self.__libpath += [lpath]
                    LOG("Using libpath: " + repr(lpath))
                
        # Directly append userlibs to sys path
        if (self.__libpath != None):
            for path in self.__libpath:
                sys.path.append( path )

    #==========================================================================
    def refresh(self):
        # Obtain the procedure list
        self.__procedures = {}
        self.__libraries = {}
        self.__loadableProcedures = []
        self.__filenameToId = {}
        if self.__procpath:
            for path in self.__procpath:
                self.__processFiles( path, self.__ctxInfo.getSC(), path, self.__procedures, True )
        if self.__libpath: 
            for path in self.__libpath:
                self.__processFiles( path, self.__ctxInfo.getSC(), path, self.__libraries, False )
        LOG("Registered procedures: " + str(len(self.__procedures)))
        LOG("Registered libraries: " + str(len(self.__libraries)))

    #==========================================================================
    def getLibPath(self):
        return self.__libpath
        
    #==========================================================================
    def getProcPath(self):
        return self.__procpath 
        
    #==========================================================================
    def isProcDir(self, fileName):
        return __isproc__(fileName,self.__procpath)
    
    #==========================================================================
    def getProcList(self):
        self.refresh()
        list = self.__loadableProcedures[:]
        list.sort()
        return list
    
    #==========================================================================
    def isProc(self, procId):
        procId = self.__removeInstanceNumber(procId)
        return (procId in self.__procedures)
    
    #==========================================================================
    def getProcId(self, filename):
        if filename in self.__filenameToId:
            return self.__filenameToId[filename]
        else:
            return filename

    #==========================================================================
    def getCode(self, procId, refresh = True ):
        if self.__procpath is None:
            raise NotLoaded("Cannot get code", "Manager not loaded")
        procId = self.__removeInstanceNumber(procId)
        proc = self.getProcedure(procId)
        return proc.getCode( refresh )
    
    #==========================================================================
    def getSource(self, procId, refresh = True ):
        if self.__procpath is None:
            raise NotLoaded("Cannot get source", "Manager not loaded")
        procId = self.__removeInstanceNumber(procId)
        LOG("Getting script: " + procId)
        try:
            proc = self.getProcedure(procId)
        except ProcError,ex:
            proc = self.getLibrary(procId)
        return proc.getSource( refresh )

    #==========================================================================
    def getProcedure(self, procId):
        procId = self.__removeInstanceNumber(procId)
        
        if self.__procedures.has_key(procId):
            return self.__procedures[procId]
        elif self.__pathToId.has_key(procId):
            procId = self.__pathToId.get(procId)
            return self.__procedures[procId]
        else:
            sys.stderr.write("GET PROCEDURE: " + repr(procId) + "\n")
            sys.stderr.write("PROCS: " + repr(self.__procedures.keys()) + "\n")
            sys.stderr.write("PATHS: " + repr(self.__pathToId.keys()) + "\n")
            raise ProcError("Unable to obtain procedure " + repr(procId),"Procedure not found")

    #==========================================================================
    def getLibrary(self, procId):
        if self.__libraries.has_key(procId):
            return self.__libraries[procId]
        elif self.__pathToId.has_key(procId):
            procId = self.__pathToId.get(procId)
            return self.__libraries[procId]
        else:
            sys.stderr.write("GET LIBRARY: " + repr(procId) + "\n")
            sys.stderr.write("PROCS: " + repr(self.__libraries.keys()) + "\n")
            sys.stderr.write("PATHS: " + repr(self.__pathToId.keys()) + "\n")
            raise ProcError("Unable to obtain library " + repr(procId),"Library not found")
        
    #==========================================================================
    def __removeInstanceNumber(self, procId):
        if string.find(procId,"#")>0:
            return procId[0:string.find(procId,"#")] 
        else:
            return procId 

    #==========================================================================
    def __processFiles(self, path, spacecraft, procpath, collection, checkLoadable = True ):
    
        if not os.path.isdir(path):
            LOG("Discarding file: " + path)
            return

        if not path in sys.path:
            LOG("Append python path: " + path)
            sys.path.append( path )
        
        filelist = os.listdir(path)
        for procfile in filelist:
            thefile = os.path.splitext(procfile)
            if thefile[1] == '.py' and thefile[0] != "__init__":
                try:
                    self.__parser.parseFile( path + os.sep + procfile)
                except ParseError,e:
                    LOG(e.message + ": " + procfile)
                    continue
                except NotLoadable,e:
                    LOG(e.message + ": " + procfile)
                    continue
                
                fullpath = path + os.sep + procfile
                procId = fullpath.split( procpath )[1][1:][:-3]
                theProcedure = Procedure(procId, self.__parser.properties(), fullpath)

                if self.__isDomainApplicable(theProcedure, spacecraft):
                    collection[procId] = theProcedure
                    self.__filenameToId[fullpath] = procId
                    procName = theProcedure.name()
                    id = procId + "|" + procName
                    if checkLoadable and theProcedure.isLoadable()\
                       and not id in self.__loadableProcedures:
                        self.__loadableProcedures.append(id)
                else:
                    LOG("Discarding procedure: " + procId)
            elif thefile[1]=='.pyc':
                os.remove(path + os.sep + procfile)
            elif thefile[1]=='':
                self.__processFiles( path + os.sep + thefile[0], spacecraft, procpath, collection, checkLoadable)
    
    #==========================================================================
    def __isDomainApplicable(self, procedureObject, spacecraft ):
        
        if spacecraft == "STD": return True
        try:
            scList = procedureObject.getSpacecraft()
        except:
            scList = None
        if (scList is None) or len(scList)==0:
            return False
        
        procSC = scList.split(",")
        for sc in procSC:
            sc = sc.strip()
            if sc == spacecraft: return True
        return False            
    
    #==========================================================================
    def __getDefinedProperties(self):
        data = Config.instance().getProperty(CONTEXT,"ProcPropertiesSL")
        if data:
            k1L = data.split(",")
        else:
            k1L = []
        data = Config.instance().getProperty(CONTEXT,"ProcPropertiesML")
        if data:
            kNL = data.split(",")
        else:
            kNL = []
        return [k1L,kNL]

################################################################################
ProcedureManager = ProcedureManagerClass
