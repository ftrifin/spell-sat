###################################################################################
## MODULE     : src.usr
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: User management interface of the driver connection layer
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

#*******************************************************************************
# SPELL imports
#*******************************************************************************
from spell.utils.log import *
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
# Module import definition

__all__ = ['USER']

###############################################################################
# Superclass
import spell.lib.adapter.usr
superClass = spell.lib.adapter.usr.UserInterface

###############################################################################
class UserInterface(superClass):
    
    """
    DESCRIPTION:
        User management interface. 
    """
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
            
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone USER interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone USER interface")
        
    #==========================================================================
    def _login(self, username, password, config = {} ):
        LOG("USER LOGIN: " + username + " PASSWORD " + password + " CONFIG " + repr(config))
        return True
        
    #==========================================================================
    def _logout(self, username, config = {}):
        LOG("USER LOGOUT: " + username + " CONFIG " + repr(config))
        return True

    #==========================================================================
    def _isLoggedIn(self, username, config = {}):
        LOG("USER CHECK: " + username + " CONFIG " + repr(config))
        return True
            
###############################################################################
# Interface instance
USER = UserInterface()
