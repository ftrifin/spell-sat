###################################################################################
## MODULE     : interface.loader
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Simulation data model loaded
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

#*******************************************************************************
# SPELL imports
#*******************************************************************************
from spell.utils.log import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from tm_sim_item import *
from tc_sim_item import *

#*******************************************************************************
# System imports
#*******************************************************************************

#*******************************************************************************
# Import definition
#*******************************************************************************
__all__ = ['ModelLoader']

#*******************************************************************************
# Module globals
#*******************************************************************************

TM_ITEMS = 0
TC_ITEMS = 1

################################################################################
class ModelLoader(object):
    
    model = None
    section = None
    tmItems = {}
    tcItems = {}

    #===========================================================================    
    def __init__(self, model):
        self.model = model
        self.section = None
        self.tmItems = {}
        self.tcItems = {}
        
    #===========================================================================    
    def loadFromFile(self, defFile):
        dfn = file(defFile)
        for line in dfn.readlines():
            line = line.strip()
            if line.startswith('#') or len(line)==0: continue
            if line == '[TM ITEMS]':
                self.section = TM_ITEMS
            elif line == '[TC ITEMS]':
                self.section = TC_ITEMS
            else:
                if self.section is None: continue
                elif self.section == TM_ITEMS:
                    components = line.split(";")
                    name = components[0].strip()
                    if name in self.tmItems.keys():
                        LOG("WARNING: discarding duplicated entry for " + repr(name))
                        continue
                    descr = components[1].strip()
                    raw = components[2].strip()
                    eng = components[3].strip()
                    if len(components)>=5:
                        period = int(components[4].strip())
                    else:
                        period = 0
                    self.tmItems[name] = TmItemSimClass(self.model, name, descr, raw, eng, period)
                    LOG("Loaded simulated TM item: " + repr(name))
                elif self.section == TC_ITEMS:
                    components = line.split(";")
                    name = components[0].strip()
                    if name in self.tcItems.keys():
                        LOG("WARNING: discarding duplicated entry for " + repr(name))
                        continue
                    tmitem = components[1].strip()
                    change = components[2].strip()
                    self.tcItems[name] = TcItemSimClass(self.model,name,tmitem,change)
                    LOG("Loaded simulated TC item: " + repr(name))
        return [self.tmItems,self.tcItems]

################################################################################
