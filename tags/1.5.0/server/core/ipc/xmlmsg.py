################################################################################

"""
PACKAGE 
    server.core.ipc.xmlmsg
FILE
    xmlmsg.py
    
DESCRIPTION
    Implementation of SPELL messages (obsolete)
    
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
# SPELL Imports
#*******************************************************************************
from spell.utils.log import *
from server.core.messages.base import *

#*******************************************************************************
# Local Imports
#*******************************************************************************
 
#*******************************************************************************
# System Imports
#*******************************************************************************
import xml.dom.minidom
from xml.dom.minidom import Node
from xml.sax.saxutils import escape
import sys
 
#*******************************************************************************
# Exceptions 
#*******************************************************************************
class MessageException(BaseException): pass
 
#*******************************************************************************
# Module globals
#*******************************************************************************

__all__ = [ 'MessageException', 'MessageClass' ]

KEY_SEP = "\2"
PAIR_SEP = "\1"

################################################################################
class XmlDataClass(object):
    
    # Root node, the message type
    __root = None
    # Message fields
    __properties = {}
    
    #==========================================================================
    def __init__(self, data = None):
        self.__root = None
        self.__properties = {}
        if data:
            self.setXML(data)

    #==========================================================================
    def __str__(self):
        return self.getXML()

    #==========================================================================
    def getXML(self):
        s = '<?xml version="1.0"?>\n'
        s = '%s<%s>' % (s, self.__root) 
        for name, value in self.__properties.iteritems():
            s = '%s     <property name="%s"' % (s, name)
            s = '%s>%s</property>\n' % (s,self._adapt(value))
        
        s = '%s</%s>\n' % (s,self.__root)
        return s

    #==========================================================================
    def setXML(self, data):
        try:
            document = xml.dom.minidom.parseString(data)
            for node in document.childNodes:
                self.__root = node.nodeName
                break
            for property in document.getElementsByTagName("property"):
                name = property.getAttribute("name")
                for child in property.childNodes:
                    if child.nodeType == Node.TEXT_NODE:
                        value = str(child.data)
                        self.__properties[name] = value
        except BaseException,ex:
            LOG("<UNPARSED>**********************", level = LOG_COMM)
            LOG(data, level = LOG_COMM)
            LOG("<UNPARSED>**********************", level = LOG_COMM)
            raise MessageException(ex.message)

    
    #==========================================================================
    def setRoot(self, rootName):
        self.__root = rootName

    #==========================================================================
    def getRoot(self):
        return self.__root

    #==========================================================================
    def _adapt(self, data, reverse = False):
        if reverse:
            data = data.replace("&amp;", "&")
            data = data.replace("&lt;", "<")
            data = data.replace("&gt;", ">")
            data = data.replace("&apos;", "'")
        else:
            data = data.replace("&", "&amp;")
            data = data.replace("<", "&lt;")
            data = data.replace(">", "&gt;")
            data = data.replace("'", "&apos;")
        return data
            

    #==========================================================================
    def __getitem__(self, name):
        if name == "type":
            return self.__root
        if (self.__properties.has_key(name)):
            data = self.__properties[name]
            return self._adapt(data,True)
        return None

    #==========================================================================
    def get(self, name):
        return self.__getitem__(name)
    
    #==========================================================================
    def setData(self, data):
        self.__properties.clear()
        for key in data.keys():
            value = str(data[key])
            self.__properties[key] = self._adapt(value)

    #==========================================================================
    def getData(self):
        data = {}
        for key in self.__properties.keys():
            value = self.__properties.get(key)
            data[key] = self._adapt(value,True)
        return data

    #==========================================================================
    def getFields(self):
        return self.__properties.keys()

    #==========================================================================
    def getProperties(self):
        return self.__properties
    
    #==========================================================================
    def __setitem__(self, name, value):
        if name == self.__root:
            return
        value = str(value) 
        self.__properties[name] = self._adapt(value)

    #==========================================================================
    def contains(self, name):
        return self.__properties.has_key(name)

################################################################################
class MessageClass(object):
    
    def __init__(self, data = None):
        self._data = {}
        self._data[FIELD_SENDER_ID] = GENERIC_ID
        self._data[FIELD_RECEIVER_ID] = GENERIC_ID
        if data:
            elements = data.split(PAIR_SEP)
            for e in elements:
                kv = e.split(KEY_SEP)
                self._data[kv[0]] = kv[1]
        
    #==========================================================================
    def __getitem__(self, name):
        return self._data.get(name)
    
    #==========================================================================
    def __setitem__(self, name, value):
        self._data[name] = value 

    #==========================================================================
    def data(self):
        return self.__str__() 

    #==========================================================================
    def getType(self):
        return self._data["root"]

    #==========================================================================
    def setType(self, type):
        self._data["root"] = type

    #==========================================================================
    def getId(self):
        return self._data["Id"]

    #==========================================================================
    def setId(self, id):
        self._data["Id"] = id

    #==========================================================================
    def getKey(self):
        return int(self._data.get(FIELD_IPC_KEY))
    
    #==========================================================================
    def setKey(self, key):
        self._data[FIELD_IPC_KEY] = int(key)

    #==========================================================================
    def getSender(self):
        return self._data.get(FIELD_SENDER_ID)

    #==========================================================================
    def setSender(self, senderId):
        self._data[FIELD_SENDER_ID] = str(senderId)

    #==========================================================================
    def getReceiver(self):
        return self._data.get(FIELD_RECEIVER_ID)

    #==========================================================================
    def setReceiver(self, receiverId):
        self._data[FIELD_RECEIVER_ID] = str(receiverId)

    #==========================================================================
    def getSequence(self):
        return self._data.get(FIELD_SEQUENCE)

    #==========================================================================
    def setSequence(self, seq):
        self._data[FIELD_SEQUENCE] = seq

    #==========================================================================
    def __str__(self):
        result = ""
        numKeys = len(self._data)
        count = 0
        for key in self._data:
            result += str(key) + KEY_SEP + str(self._data[key])
            if count<(numKeys-1): result += PAIR_SEP
            count += 1
        return result
    
    #==========================================================================
    def setProps(self, data):
        self._data = data

    #==========================================================================
    def getProps(self):
        return self._data
