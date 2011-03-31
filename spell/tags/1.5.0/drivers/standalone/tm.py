################################################################################

"""
PACKAGE 
    spell.lib.dummy.tm
FILE
    tm.py
    
DESCRIPTION
    TM interface for standalone driver

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
from spell.lib.registry import REGISTRY

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
import os

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
        if name == "INVALID":
            param._status = False
        elif name == "TIMEOUT":
            import time
            time.sleep(1000)

        eng = (config.get(ValueFormat) == ENG)
        
        if eng:
            value = param._engValue
        else:
            value = param._rawValue
        return [value, param._status]

    #===========================================================================
    def _setLimit(self, param, limit, value, config ):
        REGISTRY['CIF'].write("Set limit for " + repr(param) + ": " + repr(limit) + "=" + repr(value))
        result = False
        Display("Loading limits from a file is not supported", WARNING)
        return result

    #===========================================================================
    def _getLimit(self, param, limit, config ):
        REGISTRY['CIF'].write("Get limit for " + repr(param) + ": " + repr(limit))
        result = False
        Display("Loading limits from a file is not supported", WARNING)
        return result

    #===========================================================================
    def _getLimits(self, param, config ):
        REGISTRY['CIF'].write("Get limits for " + repr(param))
        result = False
        Display("Loading limits from a file is not supported", WARNING)
        return result

    #===========================================================================
    def _setLimits(self, param, limits, config ):
        REGISTRY['CIF'].write("Set limits for " + repr(param) + ": " + repr(limits))
        result = False
        Display("Loading limits from a file is not supported", WARNING)
        return result
               
    #===========================================================================
    def _loadLimits( self, limitsList, useConfig ):
        result = False
        Display("Loading limits from a file is not supported", WARNING)
        return result
               
################################################################################
# Interface handle
TM = TmInterface()
