################################################################################

"""
PACKAGE 
    spell.lib.dummy.task
FILE
    task.py
    
DESCRIPTION
    Task management interface for standalone driver

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
################################################################################

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

__all__ = ['TASK']

###############################################################################
# Superclass
import spell.lib.adapter.task
superClass = spell.lib.adapter.task.TaskInterface

###############################################################################
class TaskInterface(superClass):
    
    """
    DESCRIPTION:
        Task management library interface. This class is in charge of
        managing the underlying system applications.
    """
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
            
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone TASK interface")

    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone TASK interface")
    
    #==========================================================================
    def _startTask(self, name, config = {}  ):
        return True
        
    #==========================================================================
    def _stopTask(self, name, config = {} ):
        return True

    #==========================================================================
    def _checkTask(self, name, config = {} ):
        return True

    #==========================================================================
    def _openDisplay(self, displayName, config = {} ):
        return True

    #==========================================================================
    def _printDisplay(self, displayName, config = {} ):
        return True
        
    #==========================================================================
    def _closeDisplay(self, displayName, config = {} ):
        return True

################################################################################
# Interface handle
TASK = TaskInterface()
        
            