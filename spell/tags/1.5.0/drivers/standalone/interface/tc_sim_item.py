###############################################################################
"""
DESCRIPTION: TC item for simulations in standalone driver.

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
from spell.lib.adapter.tc_item import TcItemClass

#*******************************************************************************
# Local imports
#*******************************************************************************

#*******************************************************************************
# System imports
#*******************************************************************************

#*******************************************************************************
# Import definition
#*******************************************************************************
__all__ = ['TcItemSimClass']

#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class TcItemSimClass(TcItemClass):

    tmItemName = None
    changeDef = None

    def __init__(self, model, name, tmItemName, change):
        TcItemClass.__init__(self,model.tcClass,name)
        self.tmItemName = tmItemName
        self.changeDef = change
        
    def getTmItemName(self):
        return self.tmItemName
    
    def getTmChange(self):
        return self.changeDef

################################################################################
