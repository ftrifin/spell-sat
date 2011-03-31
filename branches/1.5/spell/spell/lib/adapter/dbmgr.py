###############################################################################

"""
PACKAGE 
    spell.lib.adapter.config 
FILE
    config.py
    
DESCRIPTION
    Setup environment for correct core driver instantiation
    
PROJECT: SPELL

 Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 This library is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, either
 version 3 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License and GNU General Public License (to which the GNU Lesser
 General Public License refers) along with this library.
 If not, see <http://www.gnu.org/licenses/>.
 
"""

###############################################################################

#*******************************************************************************
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import DriverException
from spell.lib.adapter.config import Configurable
from spell.lib.adapter.constants.core import *
from spell.lib.adapter.database import *
from spell.lib.adapter.dbfile import *
from spell.lib.adapter.dbasrun import *
from spell.lib.adapter.dbsvn import *

#*******************************************************************************
# Local Imports
#*******************************************************************************

#*******************************************************************************
# System Imports
#*******************************************************************************
import os

#*******************************************************************************
# Import Definition
#*******************************************************************************
__all__ = [ 'DBMGR' ]

#*******************************************************************************
# Module Globals
#*******************************************************************************

################################################################################
class DatabaseManagerClass(Configurable):
    
    __ctxConfig = None
    __databases = {}

    #===========================================================================    
    def __init__(self):
        Configurable.__init__(self)
        self.__databases = {}
        self.__ctxConfig = None
        LOG("Created")

    #===========================================================================    
    def setup(self, ctxConfig, drvConfig):
        self.__ctxConfig = ctxConfig
        LOG("Loading spacecraft database")
        self.loadDatabase('SCDB')
        LOG("Loading ground database")
        self.loadDatabase('GDB')

    #===========================================================================    
    def cleanup(self):
        self.__databases.clear()
        
    #===========================================================================    
    def __getitem__(self, key):
        if not self.__databases.has_key(key):
            raise DriverException("No such database: " + repr(key))
        return self.__databases.get(key)
    
    #===========================================================================    
    def fromURItoPath(self, dbName):
        LOG("Create database " + repr(dbName))
        idx = dbName.find('://')
        if idx != -1:
            # User database
            dbType = dbName[0:idx]
            dbName = dbName[idx+3:]
            dbPath = self.__ctxConfig.getLocationPath(dbType.upper()) + os.sep + dbName 
            LOG("User path: " + repr(dbPath))
        else:
            # Preconfigured database
            location,filename = self.__ctxConfig.getDatabaseInfo(dbName)
            if location is None:
                raise DriverException("Unknown database location")
            if filename is None:
                raise DriverException("No database file")
            dbType = location
            lpath = self.__ctxConfig.getLocationPath(location)
            dbPath = lpath + os.sep + filename
            
        # Translate path tags
        idx = dbPath.find("$SATNAME$")
        if idx != -1:
            dbPath = dbPath[0:idx] + self.__ctxConfig.getSatName() + dbPath[idx+9:]
        idx = dbPath.find("$SATID$")
        if idx != -1:
            dbPath = dbPath[0:idx] + self.__ctxConfig.getSC() + dbPath[idx+7:]
            
        LOG("Database path: " + repr(dbPath))
        return dbType, dbPath
        
    #===========================================================================    
    def getDatabaseInstance(self, dbName):
        dbType, dbPath = self.fromURItoPath(dbName)
        
        dbType = dbType.lower()
        LOG("Database type: " + repr(dbType))
        db = None
        
        if dbType == DB_TYPE_FILE:
            db = DatabaseFile(dbName, dbPath)
        elif dbType == DB_TYPE_MMD:
            ext = self.__ctxConfig.getLocationExt(dbType.upper())
            LOG("Using default extension for type " + repr(dbType) + ": " + ext)
            db = DatabaseFile(dbName, dbPath, ext)
        elif dbType == DB_TYPE_USR:
            ext = self.__ctxConfig.getLocationExt(dbType.upper())
            LOG("Using default extension for type " + repr(dbType) + ": " + ext)
            db = DatabaseFile(dbName, dbPath, ext)
        elif dbType == DB_TYPE_SCDB:
            ext = self.__ctxConfig.getLocationExt(dbType.upper())
            LOG("Using default extension for type " + repr(dbType) + ": " + ext)
            db = DatabaseFile(dbName, dbPath, ext)
        elif dbType == DB_TYPE_GDB:
            ext = self.__ctxConfig.getLocationExt(dbType.upper())
            LOG("Using default extension for type " + repr(dbType) + ": " + ext)
            db = DatabaseFile(dbName, dbPath, ext)
        elif dbType == DB_TYPE_SQLITE:
            db = DatabaseSQLite(dbName, dbPath)
        elif dbType == DB_TYPE_ASRUN:
            ext = self.__ctxConfig.getLocationExt(dbType.upper())
            LOG("Using default extension for type " + repr(dbType) + ": " + ext)
            db = DatabaseAsRun(dbName, dbPath, ext)
        elif dbType == DB_TYPE_SVN:
            ext = self.__ctxConfig.getLocationExt(dbType.upper())
            LOG("Using default extension for type " + repr(dbType) + ": " + ext)
            db = DatabaseSubversion(dbName, dbPath, ext)
        else:
            raise DriverException("Unknown database type: " + repr(dbType)) 
        
        self.__databases[dbName] = db 
        return db

    #===========================================================================    
    def createDatabase(self, dbName):
        if not self.__databases.has_key(dbName):
            db = self.getDatabaseInstance(dbName)
        
        db = self.__databases.get(dbName) 
        db.create()
        
        return db
        
    #===========================================================================    
    def loadDatabase(self, dbName):
        if not self.__databases.has_key(dbName):
            db = self.getDatabaseInstance(dbName)
        
        db = self.__databases.get(dbName) 
        db.load()
        
        return db

DBMGR = DatabaseManagerClass()
