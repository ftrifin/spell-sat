"""
FILE: database.py
PACKAGE: spell.lib.adapter.database

DESCRIPTION: database objects

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

import os,sys
from spell.lib.exception import DriverException
from spell.utils.log import *
from spell.config.reader import *
#from spell.lang.constants import *
#from spell.lang.modifiers import *
from spell.config.constants import COMMON
from spell.utils.vimport import ImportValue,ExportValue

DB_TYPE_FILE   = 'file'
DB_TYPE_MMD    = 'mmd'
DB_TYPE_USR    = 'usr'
DB_TYPE_SCDB   = 'scdb'
DB_TYPE_GDB    = 'gdb'
DB_TYPE_SQLITE = 'sqlite'
DB_TYPE_MYSQL  = 'sql'
DB_TYPE_ASRUN  = 'ar'
DB_TYPE_SVN    = 'svn'

################################################################################
class Database(object):
    
    #===========================================================================
    def __init__(self):
        self._vkeys = []
        self._types = {}
        self._properties = {}

        if type(self) is  Database:
            raise NotImplemented()
    
    #===========================================================================
    def __getitem__(self, key):
        raise NotImplemented()

    #===========================================================================
    def __setitem__(self, key, value):
        raise NotImplemented()

    #===========================================================================
    def create(self):
        raise NotImplemented()
    
    #===========================================================================
    def load(self):
        raise NotImplemented()
    
    #===========================================================================
    def reload(self):
        raise NotImplemented()

    #===========================================================================
    def id(self):
        raise NotImplemented()

    #===========================================================================
    def commit(self):
        raise NotImplemented()

    #===========================================================================
    def __getitem__(self, key):
        if not self._properties.has_key(key):
            raise DriverException("No such key: " + repr(key))
        return self._properties.get(key)

    #===========================================================================
    def __setitem__(self, key, value):
        if not key in self._properties.keys():
            self._vkeys.append(key)
        self._properties[key] = value

    #===========================================================================
    def set(self, key, value, format = None):
        if not key in self._properties.keys():
            self._vkeys.append(key)
        self._properties[key] = value
        if format:
            self._types[key] = format

################################################################################
def int2bin(n, count=24):
    return "".join([str((n >> y) & 1) for y in range(count-1, -1, -1)])
