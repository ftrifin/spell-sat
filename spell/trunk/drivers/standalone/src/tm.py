###################################################################################
## MODULE     : tm
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Telemetry interface of the driver connection layer
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
from spell.lib.adapter.utctime import *
from spell.lib.registry import REGISTRY

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import sys,os

###############################################################################
# Module import definition

__all__ = ['TM']

###############################################################################
# Superclass
import spell.lib.adapter.tm
superClass = spell.lib.adapter.tm.TmInterface

###############################################################################
class TmInterface( superClass ):
    
    #==========================================================================
    def __init__(self):
        superClass.__init__(self)
        LOG("Created")
            
    #==========================================================================
    def setup(self, ctxConfig, drvConfig):
        superClass.setup(self, ctxConfig, drvConfig)
        LOG("Setup standalone TM interface")
        
    #==========================================================================
    def cleanup(self):
        superClass.cleanup(self)
        LOG("Cleanup standalone TM interface")
    
    #===========================================================================
    def _createTmItem(self, mnemonic, description = ""):
        LOG("Creating simulated TM item: " + mnemonic)
        if mnemonic == "NOTFOUND":
            raise DriverException("Parameter not found")
        return REGISTRY['SIM'].getTMitem(mnemonic, description)
    
    #==========================================================================
    def _injectItem(self, param, value, config ):
        REGISTRY['SIM'].changeItem(param,value)
        return True
    
    #==========================================================================
    def _refreshItem(self, param, config ):
        name = param.name()
        param._setStatus(True)
        if name == "INVALID":
            param._setStatus(False)
        elif name == "TIMEOUT":
            import time
            time.sleep(1000)

        eng = (config.get(ValueFormat) == ENG)

        if (config.get(Wait)==True):
            timeout = config.get(Timeout)
            if timeout > 0:
                param.waitUpdate(timeout)
            else:
                param.waitUpdate()
        
        if eng:
            value = param._getEng()
        else:
            value = param._getRaw()
            
        param._setTime(TIME(NOW))
            
        return [value, param._getStatus()]

    #===========================================================================
    def _setLimit(self, param, limit, value, config ):
        REGISTRY['CIF'].write("Set limit for " + repr(param) + ": " + repr(limit) + "=" + repr(value))
        result = False
        return result

    #===========================================================================
    def _getLimit(self, param, limit, config ):
        REGISTRY['CIF'].write("Get limit for " + repr(param) + ": " + repr(limit))
        result = False
        return result

    #===========================================================================
    def _getLimits(self, param, config ):
        REGISTRY['CIF'].write("Get limits for " + repr(param))
        result = False
        return result

    #===========================================================================
    def _setLimits(self, param, limits, config ):
        REGISTRY['CIF'].write("Set limits for " + repr(param) + ": " + repr(limits))
        result = False
        return result
               
    #===========================================================================
    def _loadLimits( self, limitsList, useConfig ):
        result = False
        REGISTRY['CIF'].write("Loading limits from a file is not supported", WARNING)
        return result
               
################################################################################
# Interface handle
TM = TmInterface()
