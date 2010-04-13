################################################################################

"""
PACKAGE 
    server.executor.status
FILE
    status.py
    
DESCRIPTION
    Execution status definitions
    
PROJECT: SPELL

 Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.

 This file is part of SPELL.

 SPELL is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SPELL is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with SPELL. If not, see <http://www.gnu.org/licenses/>.

"""

################################################################################

#*******************************************************************************
# Module globals
#*******************************************************************************

UNINIT   = "UNINIT"
LOADED   = "LOADED"
PAUSED   = "PAUSED"
STEPPING = "STEPPING"
RUNNING  = "RUNNING"
FINISHED = "FINISHED"
ABORTED  = "ABORTED"
ERROR    = "ERROR"
WAITING  = "WAITING"