###################################################################################
## MODULE     : core.ipc.interfaceclient
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: IPC interface for clients
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
from server.core.messages.msghelper import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
from ipc import IPCworker,IPCerror,IPC_DEBUG,IPC_KEY_ENCODING
from xmlmsg import MessageClass,MessageException

from mailbox import Mailbox
from input import IPCinput
from output import IPCoutput

from incoming import IncomingMessage,IncomingRequest
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import os,sys,thread,time
import socket,os,sys,select,struct

#*******************************************************************************
# Exceptions 
#*******************************************************************************
 
#*******************************************************************************
# Module globals
#*******************************************************************************

################################################################################
class IPCinterfaceClient(object):

    # Interface name
    name = None

    # Callbacks for processing incoming requests, messages and errors
    messageCallback = None
    requestCallback = None
    errorCallback   = None

    # Connectivity data
    clientKey       = None
    serverHost      = None
    serverPort      = None
    
    # Input reader and output writer for the server
    writer          = None
    reader          = None
    
    # Connected flag
    connected       = False
    
    # Connection socket
    socket          = None
    
    # Mailbox stores for a while the responses
    mailbox         = None

    # Outgoing request sequence count
    reqSeq          = None
    reqLock         = None
    
    #===========================================================================
    def __init__(self, name):
        self.name = name
        self.clientKey  = None
        self.serverHost = None
        self.serverPort = None
        
        self.messageCallback = None
        self.requestCallback = None
        self.errorCallback   = None
        
        self.writer = None
        self.reader = None

        self.socket = None

        self.mailbox  = Mailbox(name)
        
        self.reqSeq = 0
        self.reqLock = thread.allocate_lock()
        
        self.connected = False

    #===========================================================================
    def connect(self, serverHost, serverPort, messageCallback = None, requestCallback = None, errorCallback = None):
        if self.connected: return
        self.messageCallback = messageCallback
        self.requestCallback = requestCallback
        self.errorCallback   = errorCallback
        try:
            self.serverHost = socket.gethostbyname(serverHost)
            self.serverPort = serverPort
            addr = ( self.serverHost, self.serverPort )
            self.socket = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
            linger = struct.pack('ii', 1, 0)
            self.socket.setsockopt( socket.SOL_SOCKET, socket.SO_KEEPALIVE, 0 )
            self.socket.setsockopt( socket.SOL_SOCKET, socket.SO_LINGER, linger )
            self.socket.connect( addr )
            # Once connected, the first thing is to obtain the client key,
            # after that conversation can be started
            myClientKey = self.socket.recv(2)
            myClientKey ,= struct.unpack( IPC_KEY_ENCODING, myClientKey )
            self.clientKey = int(myClientKey)
            LOG( self.name + ": Obtained client key: " + repr(self.clientKey), level = LOG_COMM)
            self.reader = IPCinput( self.name, self.socket, self.clientKey, self )
            self.writer = IPCoutput( self.name, self.socket, self.clientKey, self )
            # Start the input thread
            self.reader.start()       
            LOG( self.name + ": Client channel ready", level = LOG_COMM)
            self.connected = True
        except BaseException,ex:
            LOG( self.name + ": Setup failed, disconnecting", LOG_ERROR, level = LOG_COMM)
            self.disconnect()
            raise ex
                
    #===========================================================================
    def disconnect(self, eoc = True ):
        LOG( self.name + ": Disconnect interface from server", level = LOG_COMM )
        self.reader.disconnect()
        self.writer.disconnect( sendEOC = eoc )
        self.mailbox.shutdown()
        self.socket.close()

    #===========================================================================
    def incomingRequest(self, peerKey, msg):

        senderId   = msg.getSender()
        receiverId = msg.getReceiver()
        reqId = receiverId + "-" + senderId + ":" + msg.getSequence()
        
        IncomingRequest( reqId, msg, self.requestCallback, self.writer ).start()
        
    #===========================================================================
    def incomingMessage(self, peerKey, msg):

        senderId   = msg.getSender()
        receiverId = msg.getReceiver()
        msgId = receiverId + "-" + senderId

        IncomingMessage( msg, self.messageCallback ).start()

    #===========================================================================
    def incomingResponse(self, peerKey, msg):
        
        senderId   = msg.getSender()
        receiverId = msg.getReceiver()
        seq        = msg.getSequence()
        
        if seq is None and msg.getType() == MSG_TYPE_ERROR:
            self.incomingMessage(peerKey, msg)
        else:
            reqId = senderId + "-" + receiverId + ":" + msg.getSequence()
            self.mailbox.place(reqId, msg)

    #===========================================================================
    def connectionLost(self, peerKey):
        self.errorCallback(peerKey)

    #===========================================================================
    def sendMessage(self, msg ):
        self.writer.send(msg)
        
    #===========================================================================
    def sendRequest(self, msg, timeout = 150000 ):
        
        # The message shall contain the target clientKey, sender id and receiver id 
        senderId   = msg.getSender()
        receiverId = msg.getReceiver()
        reqId = receiverId + "-" + senderId
        
        # Set the request sequence number. The sequence number shall be
        # the same for the corresponding response.
        reqId = self.setSequence(reqId, msg)
        
        response = None
        self.mailbox.prepare(reqId)
        self.writer.send(msg)
        response = self.mailbox.retrieve(reqId,timeout)
        
        if response is None:
            response = MsgHelper.createError(MSG_ID_TIMEOUT, msg, "Cannot get response", "IPC")

        return response

    #===========================================================================
    def forwardRequest(self, msg, clientKey ):
        originalSeq = msg.getSequence()
        response = self.sendRequest(msg, clientKey)
        response.setSequence(originalSeq)
        return response

    #===========================================================================
    def setSequence(self, id, msg):
        reqId = id
        self.reqLock.acquire()
        try:
            msg.setSequence(self.reqSeq)
            reqId += ":" + str(self.reqSeq)
            self.reqSeq += 1
        finally:
            self.reqLock.release()
        return reqId
