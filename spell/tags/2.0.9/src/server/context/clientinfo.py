###################################################################################
## MODULE     : context.clientinfo
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Client model
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

CLIENT_MODE_CONTROL = "CONTROL"
CLIENT_MODE_MONITOR = "MONITOR"
NO_CLIENT = 'no client'

#*******************************************************************************
# Client information
#*******************************************************************************
class ClientInfo(object):
    
    key = None
    mode = None
    host = None
    executors = []
    
    #===========================================================================
    def __init__(self, key, host, mode):
        self.key = key
        self.mode = mode
        self.host = host
        self.executors = []
        
    #===========================================================================
    def getKey(self):
        return self.key
        
    #===========================================================================
    def getHost(self):
        return self.host
        
    #===========================================================================
    def getMode(self):
        return self.mode
    
    #===========================================================================
    def setMode(self, mode):
        self.mode = mode

    #===========================================================================
    def getExecutors(self):
        return self.executors[:]
    
    #===========================================================================
    def addExecutor(self, procId):
        self.executors.append(procId)
        
    #===========================================================================
    def delExecutor(self, procId):
        if procId in self.executors:
            self.executors.remove(procId)

