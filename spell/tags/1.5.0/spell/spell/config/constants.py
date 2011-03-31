################################################################################

"""
PACKAGE 
    spell.config.constants
FILE
    constants.py
    
DESCRIPTION
    Constants defining XML configuration file tags and properties
    
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

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

# Default configuration file name
CONFIG_FILE_NAME = "spell_config.xml"

# Sections of the configuration file
LISTENER = "listener"
CONTEXT  = "context"
EXECUTOR = "executor"
DRIVERS  = "drivers"
DRIVER   = "driver"
CONTEXTS = "contexts"
LANGUAGE = "language"
COMMON   = "common"

# Sections listed here will become groups of properties in the config reader
CONFIG_SECTIONS = [ LISTENER, CONTEXT, EXECUTOR, COMMON ]

################################################################################

