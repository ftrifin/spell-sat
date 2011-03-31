################################################################################

"""
PACKAGE 
    spell.utils.timing
FILE
    timing.py
    
DESCRIPTION
    Performance and timing utilities
    
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
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import time,sys
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class TMR(object):
    
    @staticmethod
    def tick(name = "."):
        
        import __main__
        
        if not __main__.__dict__.get('TIMING'):
            __main__.__dict__['TIMING'] = time.clock()
        else:
            current = time.clock()
            theTime = current - __main__.__dict__.get("TIMING")
            LOG("*************************** [" + name + "] time: %.2g" % theTime)
            __main__.__dict__.pop("TIMING")

    @staticmethod
    def time():
        LOG("*********************************** " + repr(time.time()))
        