###################################################################################
## MODULE     : interface.tm_sim_item
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: TM item for the simulation model
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
from spell.lib.adapter.tm_item import TmItemClass

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************
from math import *
from threading import Event
import sys,os,time

#*******************************************************************************
# Import definition
#*******************************************************************************
__all__ = ['TmItemSimClass']

#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class TmItemSimClass(TmItemClass):
    
    model = None
    rawExpression = None
    engExpression = None
    period = None
    timerCount = 0
    currentPosition = 0
    updateEvent = None
    
    #==========================================================================    
    def __init__(self, model, name, description, rawExpression, engExpression, period = 0):
        TmItemClass.__init__(self,model.tmClass,name,description)
        self._status = True
        self.model = model
        self.rawExpression = rawExpression
        self.engExpression = engExpression
        self.rawValue = None
        self.engValue = None
        self.period = period
        self.timerCount = 0
        self.currentPosition = -1
        LOG("NAME: " + repr(name) + ", DESC:" + repr(description))
        self.updateEvent = Event()
        self._recalculate()

    #==========================================================================
    def waitUpdate(self, timeout = None):
        self.updateEvent.clear()
        if timeout:
            startTime = time.time()
            self.updateEvent.wait(timeout)
            endTime = time.time()
            if (endTime-startTime)>timeout:
                raise DriverException("Parameter value timeout")
        else:
            self.updateEvent.wait()

    #==========================================================================    
    def refreshSimulatedValue(self):    
        if self.period == 0: return
        self.timerCount = self.timerCount + 1
        if self.timerCount == self.period:
            self.timerCount = 0
            self._recalculate()
            self.updateEvent.set()

    #==========================================================================    
    def change(self, changeDef):
        raw,eng = self._evaluate()
        
        # Changes for shitfting values of a list
        if type(changeDef) == str and changeDef.startswith('+'):
            # For lists, the amount must be integer
            amount = eval(changeDef[1:])
            if type(raw)==list or type(eng)==list:
                # Shift the position as requested
                max = len(raw)
                self.currentPosition = self.currentPosition + int(amount)
                if self.currentPosition>=max: self.currentPosition = 0
                # Update values
                if type(raw)==list:
                    self._setRaw(raw[self.currentPosition])
                if type(eng)==list:
                    self._setEng(eng[self.currentPosition])
            else:
                # For non-lists, just add the amount
                self._setRaw(self._getRaw() + amount)
                if eng is not None:
                    self._setEng(self._getEng() + amount)
        elif type(changeDef) == str and changeDef.startswith('-'):
            amount = eval(changeDef[1:])
            if type(raw)==list or type(eng)==list:
                max = len(raw)
                self.currentPosition = self.currentPosition - int(amount)
                if self.currentPosition<0: self.currentPosition = max-1
                if type(raw)==list:
                    self._setRaw(raw[self.currentPosition])
                if type(eng)==list:
                    self._setEng(eng[self.currentPosition])
            else:
                self._setRaw(self._getRaw() - amount)
                if eng is not None:
                    self._setEng(self._getEng() - amount)
        # Changes for setting values
        else:
            if type(raw)==list or type(eng)==list:
                if changeDef in eng:
                    idx = eng.index(changeDef)
                    self._setEng(eng[idx])
                    self._setRaw(raw[idx])
                elif changeDef in raw:
                    idx = raw.index(changeDef)
                    self._setRaw(raw[idx])
                    if eng is not None:
                        self._setEng(eng[idx])
                elif str(changeDef) in map(str,raw):
                    idx = map(str,raw).index(str(changeDef))
                    self._setRaw(raw[idx])
                    if eng is not None:
                        self._setEng(eng[idx])
                elif str(changeDef) in map(str,eng):
                    idx = map(str,eng).index(str(changeDef))
                    self._setEng(eng[idx])
                    self._setRaw(raw[idx])
            else:
                try:
                    self._setRaw(eval(str(changeDef)))
                except:
                    self._setRaw(eval("'" + str(changeDef) + "'"))
                if eng is not None:
                    try:
                        self._setEng(eval(str(changeDef)))
                    except:
                        self._setRaw(eval("'" + str(changeDef) + "'"))
        if eng is None:
            self._setEng(self._getRaw())
    
    #==========================================================================    
    def _recalculate(self):
        # First change the wildcards in the expressions
        raw,eng = self._evaluate()

        if type(raw)==list or type(eng)==list:
            max = len(raw)
            self.currentPosition = self.currentPosition +1
            if self.currentPosition==max: self.currentPosition = 0
            if raw is not None:
                self._setRaw(raw[self.currentPosition])
            if eng is not None:
                self._setEng(eng[self.currentPosition])
        else:
            self._setRaw(raw)
            if eng is not None:
                self._setEng(eng)
        if eng is None:
            self._setEng(self._getRaw())            

    #==========================================================================    
    def _evaluate(self):
        try:
            rawExpression = self._changeWildcards(self.rawExpression)
            raw = eval(rawExpression)
            engExpression = self._changeWildcards(self.engExpression)
            eng = eval(engExpression)
        except BaseException,ex:
            raise DriverException("Malformed simulation expression (" + repr(ex) + ")")
        if type(raw)==list and type(eng)==list:
            if len(raw)!=len(eng):
                raise DriverException("Malformed simulation expression")
        return [raw,eng]
                
    #==========================================================================    
    def _changeWildcards(self, expression):
        if expression.find('$TIME$')!=-1:
            ctime = str(self.model.getCurrentTime())
            expression = expression.replace('$TIME$',ctime)
        return expression

    #==========================================================================    
    def _setLimit(self, limitName, limitValue ):
        LOG("Limit " + limitName + " value set to " + str(limitValue))

################################################################################
