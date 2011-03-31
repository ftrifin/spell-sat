################################################################################

"""
PACKAGE 
    spell.lib.adapter.tc_item 
FILE
    tc_item.py
    
DESCRIPTION
    Telecommand item. 
    
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
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from config import Configurable
from value import *

#*******************************************************************************
# System imports
#*******************************************************************************
import sys,traceback,datetime

###############################################################################
# MODULE CONSTANTS

TC_DEFAULTS   = {Confirm:False, Time:"", Block:False, Timeout:5}

###############################################################################
# IMPORT DEFINITION

__all__ = [ 'TcItemClass' ]

###############################################################################

###############################################################################
class TcItemParamClass(object):
    
    name = None
    value = None
    
    #==========================================================================
    def __init__(self, name, value, format, radix = DEC, vtype = ENG, units = "" ):
        self.name = name
        self.value = ValueClass(value, format, radix, vtype, units )

###############################################################################
class TcItemClass(Configurable):
    
    """
    DESCRIPTION:
        Telecommand item in charge of sending a command to the system
    
    TODO: 
        Review the class. Some methods/vars are not required anymore.
    """
    
    _tcClass = None
    _parameters = []
    _cmdName = None
    _cmdDescription  = ""
    _executionStage  = ["UKN"]
    _executionStatus = ["UKN"]
    _updateTime      = [""]
    _comment         = [""]
    _elements        = ["ITEM"]
    _completed       = [False]
    _success         = [False]
    
    #==========================================================================
    def __init__(self, tcClass, cmd, description = ""):
        Configurable.__init__(self)
        self._tcClass = tcClass
        self._cmdName = cmd
        self._cmdDescription = description
        self._parameters= []
        self._completed       = [False]
        self._success         = [False]
        self._elements        = [ self._cmdName ]
        self._executionStage  = ['UKN']
        self._executionStatus = ['UKN']
        self._comment         = [" "]
        self._updateTime      = [" "]
    
    #==========================================================================
    def name(self):
        return self._cmdName

    #==========================================================================
    def desc(self):
        return self._cmdDescription

    #==========================================================================
    def clear(self):
        LOG("Item clear")
        self._reset()
        for p in self._parameters:
            self._parameters.remove(p)
        self._parameters = []
    
    #==========================================================================
    def _getParams(self):
        return self._parameters

    #==========================================================================
    def _reset(self):
        LOG("Item execution reset")
        self._completed       = [False]*len(self._completed)
        self._success         = [False]*len(self._success)
        self._executionStage  = ["UKN"] + [" "]*(len(self._executionStage)-1)
        self._executionStatus = ["UKN"] + [" "]*(len(self._executionStatus)-1)
        self._comment         = [" "]   + [" "]*len(self._comment)
        self._updateTime      = [" "]   + [" "]*len(self._updateTime)
        self.setConfig(self._tcClass.getConfig())
    
    #==========================================================================
    def __extractConfig(self, dict, key, default):
        if dict.has_key(key):
            return dict.get(key)
        else:
            return default
    
    #==========================================================================
    def __setitem__(self, name, descList):
        if type(descList)!=list:
            # Value only has been given
            param = TcItemParamClass(name, descList, ENG )                        
        else:
            value = descList[0]
            if len(descList)==2:
                if not type(descList[1]==dict):
                    raise SyntaxException("Malformed TC argument")
                argCfg = descList[1]
                format = self.__extractConfig(argCfg,ValueFormat,ENG)
                radix  = self.__extractConfig(argCfg,Radix,DEC)
                vtype   = self.__extractConfig(argCfg,ValueType,LONG)
                units  = self.__extractConfig(argCfg,Units,"")
    
                param = TcItemParamClass(name, value,format,radix,vtype,units)
            else:
                # Only value given
                param = TcItemParamClass(name, value, ENG )
        for p in self._parameters:
            if (p.name == name):
                idx = self._parameters.index(p)
                self._parameters.pop(idx)
                self._parameters.insert(idx, param)
                return
        self._parameters.append(param)
    
    #==========================================================================
    def send(self):
        return self._tcClass.send(self, self.getConfig())

    #==========================================================================
    def configure(self, *args, **kargs ):
        config = self.buildConfig( args, kargs, self._tcClass.getConfig(), TC_DEFAULTS)
        self.setConfig(config)

    #==========================================================================
    def _setElements(self, elements):
        elementIndex = 0
        self._elements = [ self._cmdName ]
        for element in elements:
            self._elements += [ str(elementIndex) + "@" + element ]
            elementIndex += 1
        self._executionStage  = ["UKN"] + [" "]*len(elements)
        self._executionStatus = ["UKN"] + [" "]*len(elements)
        self._comment         = [" "]   + [" "]*len(elements)
        self._updateTime      = [" "]   + [" "]*len(elements)
        self._completed       = [False] + [False]*len(elements)
        self._success         = [False] + [False]*len(elements)
        LOG("Elements set: " + repr(self._elements))
        
    #==========================================================================
    def _setExecutionStageStatus(self, stage, status, comment="",elementId = None):
        updTime = str(datetime.datetime.now())[:-3]
        if elementId is None:
            LOG("Set item " + repr(self._cmdName) + ' stage ' + repr(stage) + ' status ' + repr(status))
            self._executionStage[0] = stage
            self._executionStatus[0] = status
            self._comment[0] = comment
            self._updateTime[0] = updTime
        else:
            LOG("Update for element ID " + repr(elementId) + ":" + repr(self._elements))
            if self._executionStage[0] == "UKN":
                self._executionStage[0] = "Execution"
                self._executionStatus[0] = "Ongoing"
            idx = self._elements.index(elementId)
            if (self._completed[idx]==True): return
            LOG("Update element " + repr(self._cmdName) + ":" + repr(elementId) + ' stage ' + repr(stage) + ' status ' + repr(status) + " with index " + repr(idx))
            self._executionStage[idx] = stage
            self._executionStatus[idx] = status
            self._comment[idx] = comment
            self._updateTime[idx] = updTime
        self._tcClass._updateStatus(self)

    #==========================================================================
    def _setCompleted(self, success, elementId = None):
        updTime = str(datetime.datetime.now())[:-3]
        if elementId is None:
            LOG("Set item " + repr(self._cmdName) + ' complete, success=' + repr(success))
            self._completed[0] = True
            self._success[0]   = success
            self._updateTime[0] = updTime
        else:
            LOG("Set element " + repr(self._cmdName) + ":" + repr(elementId) + ' completed, success='+ repr(success))
            LOG("C: " + repr(self._completed))
            LOG("S: " + repr(self._success))
            idx = self._elements.index(elementId)
            LOG("The element index is " + repr(idx))
            self._completed[idx] = True
            self._success[idx]   = success
            self._updateTime[idx]= updTime
            LOG("C: " + repr(self._completed))
            LOG("S: " + repr(self._success))
        self._tcClass._updateStatus(self)

    #==========================================================================
    def getElements(self):
        return self._elements[:]

    #==========================================================================
    def isComplex(self):
        return len(self._elements)>1

    #==========================================================================
    def getExecutionStageStatus(self, elementId = None):
        if elementId is None:
            return [ self._executionStage[0], self._executionStatus[0] ]
        else:
            idx = self._elements.index(elementId)
            return [ self._executionStage[idx], self._executionStatus[idx] ]

    #==========================================================================
    def getIsCompleted(self, elementId = None):
        if elementId is None:
            return self._completed[0]
        else:
            idx = self._elements.index(elementId)
            return self._completed[idx]

    #==========================================================================
    def getIsSuccess(self, elementId = None):
        if elementId is None:
            return self._success[0]
        else:
            idx = self._elements.index(elementId)
            return self._success[idx]

    #==========================================================================
    def getUpdateTime(self, elementId = None):
        if elementId is None:
            return self._updateTime[0]
        else:
            idx = self._elements.index(elementId)
            return self._updateTime[idx]

    #==========================================================================
    def getComment(self, elementId = None):
        if elementId is None:
            return self._comment[0]
        else:
            idx = self._elements.index(elementId)
            return self._comment[idx]
