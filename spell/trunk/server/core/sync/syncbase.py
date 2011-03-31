################################################################################

"""
PACKAGE 
    server.core.sync.syncbase
FILE
    syncbase.py
    
DESCRIPTION
    
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class SyncBaseClass(object):

    #===========================================================================
    def wait(self, prefix = None):
        raise NotImplementedError

    #===========================================================================
    def acquire(self, prefix = None):
        raise NotImplementedError

    #===========================================================================
    def release(self, prefix = None):
        raise NotImplementedError

    #===========================================================================
    def locked(self, prefix = None):
        raise NotImplementedError
