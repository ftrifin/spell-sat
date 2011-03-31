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
from spell.config.constants import COMMON
from spell.lib.registry import REGISTRY
from spell.lib.exception import DriverException
from spell.config.reader import Config

#*******************************************************************************
# Local Imports
#*******************************************************************************
from interface.model import SimulatorModel

#*******************************************************************************
# System Imports
#*******************************************************************************
import os

###############################################################################
# Module import definition

__all__ = ['CONFIG']

INTERFACE_DEFAULTS = {}

###############################################################################
# Superclass
import spell.lib.adapter.config
superClass = spell.lib.adapter.config.ConfigInterface
        
###############################################################################
class ConfigInterface(superClass):

    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
    
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone CFG interface")
        dataPath = Config.getRuntimeDir()
        driverConfig = self.getDriverConfig()
        simulationPath = driverConfig['SimPath']
        simulationFile = self.getContextConfig().getDriverParameter('Simulation')
        home = Config.getHome()
        if home is None:
            raise DriverException("SPELL home is not defined")
        
        LOG("Loading simulation: " + simulationFile)
        simulationFile = dataPath + os.sep +  simulationPath + \
                         os.sep + simulationFile
        SIM = SimulatorModel()
        SIM.tmClass = REGISTRY['TM']
        SIM.tcClass = REGISTRY['TC']
        SIM.setup( simulationFile )
        REGISTRY['SIM'] = SIM

    #==========================================================================
    def cleanup(self, shutdown = False):
        superClass.cleanup(self, shutdown)
        LOG("Cleanup standalone CFG interface")
        REGISTRY['SIM'].cleanup()
        REGISTRY.remove('SIM')
                
###############################################################################
# Interface instance
CONFIG = ConfigInterface()
