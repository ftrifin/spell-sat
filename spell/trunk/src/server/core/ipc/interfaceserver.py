###################################################################################
## MODULE     : core.ipc.interfaceserver
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: IPC interface for servers
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
class IPCinterfaceServer(IPCworker):

    # Callbacks for processing incoming requests, messages and errors
    messageCallback = None
    requestCallback = None
    errorCallback   = None
    
    # Client keys
    lastClientKey   = 0
    clientKeys      = []
    
    # Connectivity data
    serverPort      = None
    serverKey       = None 
    
    # Input readers and output writers for each client
    writers         = {}
    readers         = {}
    
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
        IPCworker.__init__(self,name)
        self.lastClientKey = 0
        self.clientKeys = []
        self.serverKey = None
        self.serverPort = None
        self.messageCallback = None
        self.requestCallback = None
        self.errorCallback   = None
        
        self.writers = {}
        self.readers = {}
        
        self.socket = None

        self.mailbox  = Mailbox(name)
        
        self.reqSeq = 0
        self.reqLock = thread.allocate_lock()
        
        self.connected = False

    #===========================================================================
    def connect(self, serverKey, serverPort, messageCallback = None, requestCallback = None, errorCallback = None):
        if self.connected: return
        self.serverKey = serverKey
        self.serverPort = serverPort
        self.messageCallback = messageCallback
        self.requestCallback = requestCallback
        self.errorCallback   = errorCallback
        try:
            self.socket = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
            linger = struct.pack('ii', 1, 0)
            self.socket.setsockopt( socket.SOL_SOCKET, socket.SO_KEEPALIVE, 0 )
            self.socket.setsockopt( socket.SOL_SOCKET, socket.SO_LINGER, linger )
            self.socket.bind( ( '', serverPort) )
            self.socket.listen(5)
            self.port = self.socket.getsockname()[1]
            LOG( self.name + ": Socket server ready: " + str(self.socket.getsockname()), level = LOG_COMM)
            self.connected = True
        except BaseException,ex:
            LOG( self.name + ": Setup failed, disconnecting: " + str(ex), level = LOG_COMM)
            self.disconnect()
            raise ex
                
    #===========================================================================
    def work(self):
        # Accept connections
        readyRead,readyWrite,e = select.select([self.socket], [], [], 1)
        if readyRead:
            try:
                (clientsocket, address) = self.socket.accept()
            except:
                if not self.working(): return
            if clientsocket:
                LOG( self.name + ": Client connected from " + str(address), level = LOG_COMM)
                # Get a key for the incoming client
                clientKey = self.lastClientKey
                self.lastClientKey = clientKey + 1
                self.clientKeys.append(clientKey)
                LOG( self.name + ": Assigned key: " + repr(clientKey), level = LOG_COMM)
                # The first thing to do is to send the key to the client. After
                # that, conversation can be started
                keystr = struct.pack(IPC_KEY_ENCODING,clientKey)
                clientsocket.sendall(keystr)
                if IPC_DEBUG: LOG( self.name + ": Key sent", level = LOG_COMM)
                # Create IO classes for this client
                self.readers[clientKey] = IPCinput( self.name, clientsocket, clientKey, self )
                self.writers[clientKey] = IPCoutput( self.name, clientsocket, self.serverKey, self )
                # Start the input thread
                self.readers[clientKey].start()        
                LOG( self.name + ": Channel ready", level = LOG_COMM)
                self.connected = True
            else:
                LOG( self.name + ": Invalid client handle", LOG_ERROR, level = LOG_COMM)
                
    #===========================================================================
    def disconnect(self, clientKey = None, eoc = True ):
        if not self.connected: return
        if clientKey: clientKey = int(clientKey)
        # Shutdown input and output for the given client
        if clientKey in self.clientKeys:
            if IPC_DEBUG: LOG( self.name + ": Disconnecting '" + repr(clientKey) + "'", level = LOG_COMM)
            if (clientKey in self.readers):
                reader = self.readers.pop(clientKey)
                reader.disconnect()
                # We dont care if there is a failure here, we are removing it
            if (clientKey in self.writers):
                writer = self.writers.pop(clientKey)
                writer.disconnect( sendEOC = False )
            if (clientKey in self.clientKeys):
                self.clientKeys.remove(clientKey)
            LOG( self.name + ": Disconnected '" + repr(clientKey) + "'", level = LOG_COMM)
        # Shutdown input and output for all clients
        elif clientKey == None:
            self.connected = False
            LOG( self.name + ": Disconnecting all", level = LOG_COMM)
            for clientKey in self.clientKeys:
                self.writers[clientKey].disconnect( sendEOC = eoc )
                self.readers[clientKey].disconnect()
                LOG( self.name + ": Disconnected '" + repr(clientKey) + "'", level = LOG_COMM)
            self.writers.clear()
            self.readers.clear()
            self.clientKeys = []
            self.mailbox.shutdown()
            self.socket.close()
            self.working(False)
            LOG( self.name + ": All disconnected", level = LOG_COMM)
        else:
            LOG("Unknown client key: " + repr(clientKey), LOG_ERROR, level=LOG_COMM)

    #===========================================================================
    def incomingRequest(self, peerKey, msg):

        writer = self.writers.get(peerKey)
        senderId   = msg.getSender()
        receiverId = msg.getReceiver()
        reqId = str(peerKey) + "-" + receiverId + "-" + senderId + ":" + msg.getSequence()
        
        IncomingRequest( reqId, msg, self.requestCallback, writer ).start()
        
    #===========================================================================
    def incomingMessage(self, peerKey, msg):

        senderId   = msg.getSender()
        receiverId = msg.getReceiver()
        msgId = str(peerKey) + "-" + receiverId + "-" + senderId

        IncomingMessage( msg, self.messageCallback ).start()

    #===========================================================================
    def incomingResponse(self, peerKey, msg):
        
        senderId   = msg.getSender()
        receiverId = msg.getReceiver()
        seq        = msg.getSequence()
        
        if seq is None and msg.getType() == MSG_TYPE_ERROR:
            self.incomingMessage(peerKey, msg)
        else:
            reqId = str(peerKey) + "-" + senderId + "-" + receiverId + ":" + msg.getSequence()
            self.mailbox.place(reqId, msg)

    #===========================================================================
    def connectionLost(self, peerKey):
        self.errorCallback(peerKey)

    #===========================================================================
    def sendMessage(self, msg, clientKey ):

        # If there is no client, due to a connection lost issue, show an error
        # and continue
        clientKey = int(clientKey)
        if not self.writers.has_key(clientKey):
            senderId   = msg.getSender()
            receiverId = msg.getReceiver()
            LOG( self.name + ": Cannot send message, no such client key: " + repr(clientKey), LOG_WARN, level = LOG_COMM)
            LOG( self.name + ": Keys: " + repr(self.clientKeys) + "/" + repr(self.writers.keys()), LOG_WARN, level = LOG_COMM)
            LOG( self.name + ": Msg ID      : " + msg.getId(), LOG_ERROR, level = LOG_COMM)
            LOG( self.name + ": Msg sender  : " + senderId, LOG_ERROR, level = LOG_COMM)
            LOG( self.name + ": Msg receiver: " + receiverId, LOG_ERROR, level = LOG_COMM)
            return
        
        # Actually send the message through the corresponding output channel
        self.writers[clientKey].send(msg)
        
    #===========================================================================
    def sendRequest(self, msg, clientKey, timeout = 150000 ):
        
        # The message shall contain the target clientKey, sender id and receiver id 
        clientKey = int(clientKey)
        senderId   = msg.getSender()
        receiverId = msg.getReceiver()
        reqId = str(clientKey) + "-" + receiverId + "-" + senderId
        
        writer = self.getWriter(clientKey)
        
        # If there is no client key, fail
        if clientKey is None:
            LOG( self.name + ": Cannot send request, no client key given", LOG_ERROR, level = LOG_COMM)
            LOG( self.name + ": Req ID      : " + msg.getId(), LOG_ERROR, level = LOG_COMM)
            LOG( self.name + ": Req sender  : " + senderId, LOG_ERROR, level = LOG_COMM)
            LOG( self.name + ": Req receiver: " + receiverId, LOG_ERROR, level = LOG_COMM)
            errorMsg = MsgHelper.createError(msg.getId(), msg, "Cannot send request", "No client key set")
            return errorMsg
        
        # If there is no corresponding client, show a warning and return an error
        if writer is None:
            LOG( self.name + ": Cannot send request, no such client key: " + repr(clientKey), LOG_WARN, level = LOG_COMM)
            LOG( self.name + ": Keys: " + repr(self.clientKeys) + "/" + repr(self.writers.keys()), LOG_WARN, level = LOG_COMM)
            LOG( self.name + ": Req ID      : " + msg.getId(), LOG_ERROR, level = LOG_COMM)
            LOG( self.name + ": Req sender  : " + senderId, LOG_ERROR, level = LOG_COMM)
            LOG( self.name + ": Req receiver: " + receiverId, LOG_ERROR, level = LOG_COMM)
            errorMsg = MsgHelper.createError(msg.getId(), msg, "Cannot send request", "No such client")
            return errorMsg

        if IPC_DEBUG: 
            LOG( self.name + ": Sending request to client key " + repr(clientKey), level = LOG_COMM)
            LOG( self.name + ": Req ID      : " + msg.getId(), level = LOG_COMM)
            LOG( self.name + ": Req sender  : " + senderId, level = LOG_COMM)
            LOG( self.name + ": Req receiver: " + receiverId, level = LOG_COMM)

        # Set the request sequence number. The sequence number shall be
        # the same for the corresponding response.
        reqId = self.setSequence(reqId, msg)

        response = None
        self.mailbox.prepare(reqId)
        writer.send(msg)
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
    def getPort(self):
        return self.serverPort

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

    #===========================================================================
    def getWriter(self, clientKey):
        writer = None
        self.reqLock.acquire()
        try:
            writer = self.writers.get(clientKey)
        finally:
            self.reqLock.release()
        return writer
