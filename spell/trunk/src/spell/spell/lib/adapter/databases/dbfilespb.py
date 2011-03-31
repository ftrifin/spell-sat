###################################################################################
## MODULE     : spell.lib.adapter.databases.dbfilespb
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Database based on local SPB files
## -------------------------------------------------------------------------------- 
##
##  Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
##
##  This file is part of SPELL.
##
## This component is free software: you can redistribute it and/or
## modify it under the terms of the GNU Lesser General Public
## License as published by the Free Software Foundation, either
## version 3 of the License, or (at your option) any later version.
##
## This software is distributed in the hope that it will be useful,
## but WITHOUT ANY WARRANTY; without even the implied warranty of
## MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
## Lesser General Public License for more details.
##
## You should have received a copy of the GNU Lesser General Public
## License and GNU General Public License (to which the GNU Lesser
## General Public License refers) along with this library.
## If not, see <http://www.gnu.org/licenses/>.
##
###################################################################################

from dbfile import *

################################################################################
class DatabaseFileSPB(DatabaseFile):
    
    #===========================================================================
    def __init__(self, name, path, defaultExt = None):
        super(DatabaseFileSPB, self).__init__(name,path,defaultExt)
    
    #===========================================================================
    def _readData(self):
        # Load the file contents
        lines = file(self._filename).readlines()
        self._vkeys = []
        self._types = {}
        self._properties = {}
        # Will hold the line to be imported
        for line in lines:

            # Ignore blank lines
            if (line is None) or (len(line)==0): continue
            # Process lines with "$ :=" only
            if (not line.startswith("$")) or (not ":=" in line): continue

            key,orig_value = line.split(":=")
            key = key.strip()
            orig_value = orig_value.strip()
            value,vtype = ImportValue(orig_value)
            
            # Warn about duplicated data
            if self._properties.has_key(key):
                LOG("WARNING: duplicated database key: " + repr(key))
            else:
                self[key] = value
                if vtype:
                    self._types[key] = vtype
        # End for

    #===========================================================================
    def _writeData(self, theFile):
        pass
