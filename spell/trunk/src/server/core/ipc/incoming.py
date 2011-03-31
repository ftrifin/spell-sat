###################################################################################
## MODULE     : core.ipc.incoming
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Incomming messages and requests
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
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from core.messages.msghelper import MsgHelper

#*******************************************************************************
# Local Imports
#*******************************************************************************

import sys,threading

#*******************************************************************************
# System Imports
#*******************************************************************************

#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class IncomingMessage(threading.Thread):
    
    msg      = None
    callback = None
    
    #===========================================================================
    def __init__(self, msg, callback ):
        threading.Thread.__init__(self)
        self.msg      = msg
        self.callback = callback
        
    #===========================================================================
    def run(self):
        self.callback(self.msg)

################################################################################
class IncomingRequest(threading.Thread):
    
    reqId    = None
    msg      = None
    callback = None
    writer   = None
    
    #===========================================================================
    def __init__(self, reqId, msg, callback, writer ):
        threading.Thread.__init__(self)
        self.reqId    = reqId
        self.msg      = msg
        self.callback = callback
        self.writer   = writer
        
    #===========================================================================
    def run(self):
        senderId = self.msg.getSender() 
        receiverId = self.msg.getReceiver()
        response = self.callback(self.msg)
        response.setSender(receiverId)
        response.setReceiver(senderId)
        # Do not set the sequence number if it is already set 
        # that is used when forwarding requests
        if response.getSequence() == None:
            response.setSequence( self.msg.getSequence() )
        self.writer.send(response)

