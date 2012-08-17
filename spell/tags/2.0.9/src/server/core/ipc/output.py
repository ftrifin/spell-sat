###################################################################################
## MODULE     : core.ipc.output
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: IPC data writer
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
from server.core.messages.base import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from xmlmsg import *
from ipc import IPCconnectionLost,IPCerror,IPC_DEBUG,IPC_MSG_DEBUG,IPC_ENCODING
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import os,sys,thread
import socket,os,sys,errno,struct,traceback

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class IPCoutput(object):
    
    # Id name
    name = None
    # Holds the socket
    channel = None
    # client key
    myKey = None
    # Connected flag
    connected = False
    interface = None
    lock = None
    
    #===========================================================================    
    def __init__(self, name, skt, myKey, interface ):
        self.name = name + "(O)"
        self.myKey = myKey
        self.interface = interface
        self.lock = thread.allocate_lock()
        self.channel = skt
        self.connected = True
        if IPC_DEBUG: LOG( self.name + ": Created, assigned client key " + repr(myKey), level = LOG_COMM)

    #===========================================================================
    def send(self, msg):
        self.lock.acquire()
        msg.setKey(self.myKey)
        try:
            self.writeData(msg.data())
        except IPCconnectionLost, ex:
            # Ignore the exception if we are sending an EOC. If the
            # connection is broken on the other side, we cannot send it anyway
            if msg.getType() != MSG_TYPE_EOC:
                LOG( self.name + ": Connection lost at base output: " + ex.message, LOG_ERROR)
                self.interface.connectionLost(self.myKey)
        finally:
            self.lock.release()
        
    #===========================================================================
    def writeData(self, data):
        try:
            if self.channel:
                try:
                    length = struct.pack(IPC_ENCODING, len(data))
                    if IPC_MSG_DEBUG: LOG("Write length: " + str(len(data)) + ": " + repr(length), level = LOG_COMM)
                    self.channel.sendall(length)
                    self.channel.sendall(data)
                except socket.error,se:
                    if se[0] == errno.ECONNRESET:
                        self.channel.close()
                        LOG( self.name + ": Channel closed", level = LOG_COMM)
                        if self.connected:
                            self.connected = False
                            raise IPCconnectionLost( self.name + ": Connection lost: " + str(se))
                    elif se[0] in [errno.ECONNABORTED,errno.ESHUTDOWN,errno.ENOTCONN,errno.EBADF]:
                        self.channel.close()
                        LOG( self.name + ": Channel closed", level = LOG_COMM)
                        if self.connected:
                            self.connected = False
                            raise IPCconnectionLost( self.name + ": Connection aborted: " + str(se))
                    else:
                        raise se
            else:
                LOG( self.name + ": Failed to send data. no channel: " + repr(data), LOG_ERROR, level = LOG_COMM)
                
        except IPCconnectionLost,ex:
            raise ex
        except Exception,ex:
            traceback.print_exc( file = sys.stderr )
            LOG( self.name + " ERROR ON OUTPUT: " + str(ex), LOG_ERROR)
            raise IPCconnectionLost(repr(ex))
        except socket.error,ex:
            LOG( self.name + ": SOCKET ERROR: " + repr(ex))
            raise IPCconnectionLost(repr(ex))
        
    #===========================================================================
    def disconnect(self, sendEOC = True ):
        if not self.connected: return
        LOG( self.name + ": Disconnect", level = LOG_COMM)
        self.connected = False
        try:
            if sendEOC:
                eoc = MessageClass()
                eoc.setType(MSG_TYPE_EOC)
                eoc.setKey(self.myKey)
                self.send(eoc)
                
            self.channel.shutdown(socket.SHUT_WR)
            LOG( self.name + ": Channel shutdown", level = LOG_COMM)
        except socket.error,e:
            if e[0] in [errno.ENOTCONN,errno.EBADF,errno.ECONNABORTED]:
                self.channel.close()
                LOG( self.name + ": Channel closed", level = LOG_COMM)
                if self.connected:
                    self.connected = False
                    raise IPCerror( self.name + ": Error in disconnection: " + str(e[0]))
