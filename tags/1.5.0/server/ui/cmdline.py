################################################################################

"""
PACKAGE 
    server.ui.cmdline
FILE
    cmdline.py
    
DESCRIPTION
    Command line interface
    
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

from spell.lang.constants import *
from spell.lang.modifiers import *
from spell.lib.registry import *
from spell.lib.adapter.config import Configurable

import server.core.messages.executor
from server.core.messages.base import *
from spell.lib.adapter.constants.notification import *
from spell.lib.adapter.constants.core import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from clientbase import ClientInterfaceBase
 
#*******************************************************************************
# System Imports
#*******************************************************************************
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************
ExcMessages = server.core.messages.executor

__all__ = ['ClientIF']

################################################################################
class ClientInterface(ClientInterfaceBase,Configurable):

    """
    DESCRIPTION:
        TODO description
    """

    #==========================================================================
    def __init__(self):
        ClientInterfaceBase.__init__(self)
        LOG("Created")

    #==========================================================================
    def setup(self, ctxName):
        ClientInterfaceBase.setup(self, ctxName)
        LOG("Command line test CIF setup")
        self.ready = True

    #==========================================================================
    def cleanup(self):
        ClientInterfaceBase.cleanup(self)
        LOG("Command line test CIF cleanup")
        self.ready = False
            
    #==========================================================================
    def _sendNotificationMessage(self, msg):
        self._notificationCmdline(msg)
        
    #===========================================================================
    def _sendWriteMessage(self, msg ):
        self._writeCmdLine(msg)

    #===========================================================================
    def _sendPromptMessage(self, msg, timeout = 0 ):
        return self._promptCmdLine(msg)
        
    #===========================================================================
    def _getProcId(self):
        return "SHELL"

    #===========================================================================
    def _getCSP(self):
        return "NA"
    
    #===========================================================================
    def _getTimeID(self):
        return time.strftime('%Y-%m-%d_%H%M%S')
        
################################################################################
ClientIF = ClientInterface()

