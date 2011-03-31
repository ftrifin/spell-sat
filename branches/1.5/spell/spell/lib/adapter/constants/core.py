###############################################################################

"""
PACKAGE 
    spell.lib.adapter.constants.core
FILE
    core.py
    
DESCRIPTION
    Core constants and identifiers
    
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

from spell.lang.constants import eq,neq,lt,le,gt,ge,bw,nbw

###############################################################################
# AVAILABLE DRIVER IDENTIFIERS
DRIVER_HIFLY = "hifly"
DRIVER_DUMMY = "STANDALONE"
DRIVER_SCORPIO = "SCORPIO"

KNOWN_DRIVERS = [ DRIVER_HIFLY, DRIVER_DUMMY, DRIVER_SCORPIO ]

DRIVER_PACKAGES = { DRIVER_HIFLY:'spell.lib.hifly', 
                    DRIVER_DUMMY:'spell.lib.dummy',
                    DRIVER_SCORPIO:'spell.lib.scorpio' }

###############################################################################
# Services
SVC_EXMGR = 'EXMGR'
SVC_TM = 'TM'
SVC_TC = 'TC'
SVC_UI = 'UI'
SVC_SMGR = 'SMGR'
SVC_PMGR = 'PMGR'

###############################################################################
KEY_SEPARATOR = ":"

###############################################################################
COMP_SYMBOLS = { eq:"=", neq:"!=", lt:"<", le:"<=", gt:">", ge:">=", bw:"bw", nbw:"nbw" }


