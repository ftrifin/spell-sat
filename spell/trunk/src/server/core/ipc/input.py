###################################################################################
## MODULE     : core.ipc.input
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Data reader
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
from xmlmsg import MessageClass,MessageException
from ipc import IPCworker, IPCconnectionLost,IPC_BUFFER_SIZE,IPC_ENCODING, IPCerror
from ipc import IPC_DEBUG,IPC_MSG_DEBUG 

#*******************************************************************************
# System Imports
#*******************************************************************************
import Queue,os, sys
from datetime import datetime
import os,sys,socket,errno,time,traceback,struct,thread

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class IPCinput(IPCworker):
    """
    Data input interface. It is an active object which uses the
    thread working method 'work()' to read continuously from the input channel,
    a socket. All data collected from the input is put together in a string buffer. 
    As soon as an XML message separator is found, the data bunch is sent to the 
    process() method.
    
    The process() method splits the data bunch into individual messages which
    are sent to the dispatch() method. dispatch() redirects the messages to
    the command/request listener if required, or stores the message as a 
    response to a request previously done on the server side. What to do depends
    on the message type field. 
    
    A blocking queue of 1 element is used for storing request responses. The 
    need for storing several responses enqueued is not foreseen at this moment.
    """
    
    # name id
    name = None
    # Holds the reference to the owner IPC interface
    interface = None
    # Holds the client socket
    channel = None
    # Holds the input string buffer
    buffer = None
    # Length of the msg prefix
    PFX_LEN = struct.calcsize(IPC_ENCODING)
    # Read message length
    msgLen = None
    # Corresponding client key
    peerKey = None
    # Lock
    lock = None

    #===========================================================================
    def __init__(self, name, skt, peerKey, ifc):
        IPCworker.__init__(self,name)
        self.name = self.name + "(I)"
        self.interface = ifc
        self.buffer = ""
        self.channel = skt
        self.msgLen = 0
        self.peerKey = peerKey
        self.lock = thread.allocate_lock()
        if IPC_DEBUG: LOG(self.name + ": Created", level = LOG_COMM)
        if IPC_MSG_DEBUG: LOG("Encoding size: " + repr(self.PFX_LEN));

    #===========================================================================
    def work(self):
        """
        See IPCworker.work()
        """
        data = None
        try:
            data = self.readData()
        except IPCconnectionLost,ex:
            LOG( self.name + ": Connection lost at input: " + ex.message, LOG_ERROR, level = LOG_COMM)
            self.interface.connectionLost(self.peerKey)
            self.working(False)
            return

        if data:
            self.buffer = self.buffer + data
            if IPC_MSG_DEBUG:  LOG("Read so far: " + str(len(self.buffer)), level = LOG_COMM)
            while len(self.buffer) >= self.PFX_LEN and self.working():
                try:
                    length ,= struct.unpack( IPC_ENCODING, self.buffer[:self.PFX_LEN])
                    if IPC_MSG_DEBUG: LOG("To read length: " + str(length) + ": " + repr(self.buffer[:self.PFX_LEN]), level = LOG_COMM)
                except:
                    traceback.print_exc()
                if len(self.buffer) < length + self.PFX_LEN:
                    break
                if IPC_MSG_DEBUG: LOG("Read enough: " + str(length) + ": " + repr(self.buffer[:self.PFX_LEN]), level = LOG_COMM)
                packet = self.buffer[self.PFX_LEN:length + self.PFX_LEN]
                if IPC_MSG_DEBUG: LOG("Packet: '" + repr(packet) + "'", level = LOG_COMM)
                self.buffer = self.buffer[length + self.PFX_LEN:]
                if self.buffer is None: self.buffer = ""
                self.dispatch(packet)

    #===========================================================================
    def readData(self):
        try:
            while self.working():
                try:
                    data = self.channel.recv(IPC_BUFFER_SIZE)
                    if data:
                        return data
                    elif self.working():
                        self.working(False)
                        raise IPCconnectionLost( self.name + ": Client aborted")
                except socket.error,se:
                    if se[0] == errno.EWOULDBLOCK:
                        continue
                    elif se[0] in [errno.ECONNRESET,errno.ECONNABORTED,errno.EBADF]:
                        self.channel.close()
                        LOG( self.name + ": Channel closed", level = LOG_COMM)
                        if self.working():
                            self.working(False)
                            raise IPCconnectionLost( self.name + ": Connection lost: " + str(se))
                    else:
                        raise se
            return
        except IPCconnectionLost,ex:
            raise ex
        except Exception,ex:
            traceback.print_exc( file = sys.stderr )
            LOG( self.name + " ERROR ON INPUT: " + str(ex), LOG_ERROR)
            raise IPCconnectionLost(repr(ex))
        except socket.error,ex:
            LOG( self.name + ": SOCKET ERROR: " + repr(ex))
            raise IPCconnectionLost(repr(ex))

    #===========================================================================
    def dispatch(self, msg):
        """
        dispatch( <XML message string> ) -> string
        """
        try:
            #LOG( self.name + ": Raw message: " + str(msg), level = LOG_COMM)
            receivedMsg = MessageClass(msg)
        except MessageException:
            # This means that we tried to create a message class
            # by using a PARTIAL message. Thus, we have to
            # keep this part in the buffer, since the rest of
            # the message will come later.
            return msg
        
        self.lock.acquire()
        try:
            tp = receivedMsg.getType()
            
            if tp in [MSG_TYPE_RESPONSE]:
                self.interface.incomingResponse(self.peerKey,receivedMsg)
            elif tp == MSG_TYPE_EOC:
                LOG( self.name + ": Received EOC", level = LOG_COMM)
                self.interface.disconnect(self.peerKey)
            elif tp in [MSG_TYPE_NOTIFY_ASYNC,MSG_TYPE_COMMAND,MSG_TYPE_WRITE,MSG_TYPE_ERROR]:
                self.interface.incomingMessage(self.peerKey,receivedMsg)
            elif tp in [MSG_TYPE_REQUEST, MSG_TYPE_NOTIFY, MSG_TYPE_PROMPT]:
                self.interface.incomingRequest(self.peerKey,receivedMsg)
            else:
                err = "UNKNOWN MESSAGE TYPE! : " + str(tp) + "\n\n" + repr(msg) 
                sys.stderr.write(err + "\n")
                LOG(err)
        finally:
            self.lock.release()
    
    #===========================================================================
    def disconnect(self):
        if not self.working(): return
        LOG( self.name + ": Disconnect", level = LOG_COMM)
        self.working(False)
        if self.channel is not None:
            LOG( self.name + ": Shutdown input", level = LOG_COMM)
            try:
                self.channel.shutdown(socket.SHUT_RD)
                LOG( self.name + ": Channel shut down", level = LOG_COMM)
            except socket.error,e:
                if e[0]!=errno.ENOTCONN and self.working():
                    self.working(False)
                    raise IPCerror( self.name + ": Error in disconnection: " + str(e[0]))
