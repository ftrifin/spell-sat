###############################################################################
"""
DESCRIPTION: Simulator for standalone driver.

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
from spell.lib.adapter.config import Configurable
from spell.lib.exception import *
from spell.lang.constants import *
from spell.lang.modifiers import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from loader import ModelLoader
from tm_sim_item import *
from tc_sim_item import *

#*******************************************************************************
# System imports
#*******************************************************************************
import time,os,sys
import threading,thread

#*******************************************************************************
# Import definition
#*******************************************************************************
__all__ = ['SimulatorModel']

#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class SimulatorModel(threading.Thread,Configurable):
    
    tmClass = None
    tcClass = None
    currentTime = None
    isWorking = True
    lock = None
    finishEvent = None
    tmItems = {}
    tcItems = {}

    #===========================================================================    
    def __init__(self):
        threading.Thread.__init__(self)
        Configurable.__init__(self)
        self.tmClass = None
        self.tcClass = None
        self.currentTime = time.time()
        self.isWorking = True
        self.lock = thread.allocate_lock()
        self.finishEvent = threading.Event()
        self.finishEvent.clear()
        self.tmItems = {}
        self.tcItems = {}
    
    #===========================================================================    
    def working(self, w = None):
        self.lock.acquire()
        if w is None:
            ret = self.isWorking
        else:
            self.isWorking = w
            ret = None
        self.lock.release()
        return ret
    
    #===========================================================================    
    def setup(self, defFile = None):
        # Load simulated items
        if defFile:
            self.load(defFile)
        self.start()

    #===========================================================================    
    def load(self, defFile):
        loader = ModelLoader(self)
        tmItems,tcItems = loader.loadFromFile(defFile)
        self.tmItems = tmItems
        self.tcItems = tcItems
    
    #===========================================================================    
    def cleanup(self):
        self.working(False)
        self.finishEvent.wait(2)
    
    #===========================================================================    
    def run(self):
        while (self.working()):
            time.sleep(1)
            self.lock.acquire()
            self.currentTime = time.time()
            for itemName in self.tmItems.keys():
                self.tmItems[itemName].refreshSimulatedValue()
            self.lock.release()
        self.finishEvent.set()
                
    def getCurrentTime(self):
        return self.currentTime
    
    #===========================================================================    
    def executeCommand(self, tcItemName):
        
        tcItem = self.getTCitem(tcItemName)
        
        tmItemName = tcItem.getTmItemName()
        
        tmItem = self.getTMitem(tmItemName)
        
        changeDef = tcItem.getTmChange()
        self.lock.acquire()
        tmItem.change( changeDef )
        self.lock.release()

    #===========================================================================    
    def changeItem(self, tmItemName, value):
        tmItem = self.getTMitem(tmItemName)
        tmItem.change(value)

    #===========================================================================    
    def getTMitem(self, name, description = ""):
        if not self.tmItems.has_key(name):
            tmItem = TmItemSimClass(self,name,description,'0','"SIMVALUE"', 0)
        else:
            tmItem = self.tmItems[name]
        return tmItem

    #===========================================================================    
    def getTCitem(self, name):
        tcItem = TcItemSimClass(self,name,'PARAM','0')
        return tcItem
