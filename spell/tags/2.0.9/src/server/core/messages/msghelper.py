###################################################################################
## MODULE     : core.messages.msghelper
## DATE       : Mar 18, 2011
## PROJECT    : SPELL
## DESCRIPTION: Message utilities
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
# SPELL imports
#*******************************************************************************
from spell.utils.log import *

#*******************************************************************************
# Local imports
#*******************************************************************************
from base import *
from server.core.ipc.xmlmsg import *

#*******************************************************************************
# System imports
#*******************************************************************************

###############################################################################
class MsgHelper(object):
    
    #===========================================================================
    @staticmethod
    def createMessage(msgId):
        msg = MessageClass()
        msg.setId(msgId)
        msg.setType(MSG_TYPE_COMMAND)
        return msg

    #===========================================================================
    @staticmethod
    def createRequest(requestId, senderId, receiverId):
        resp = MessageClass()
        resp.setId(requestId)
        resp.setType(MSG_TYPE_REQUEST)
        resp.setSender(senderId)
        resp.setReceiver(receiverId)
        return resp

    #===========================================================================
    @staticmethod
    def createResponse(responseId, msg):
        resp = MessageClass()
        resp.setId(responseId)
        resp.setType(MSG_TYPE_RESPONSE)
        resp.setSender(msg.getReceiver())
        resp.setReceiver(msg.getSender())
        return resp

    #===========================================================================
    @staticmethod
    def createCancel( senderId, receiverId ):
        resp = MessageClass()
        resp.setId(MSG_ID_CANCEL)
        resp.setType(MSG_TYPE_RESPONSE)
        resp.setSender(senderId)
        resp.setReceiver(receiverId)
        return resp

    #===========================================================================
    @staticmethod
    def createError(msgId, msg, errorText, reason = None):
        #if reason:
        #    LOG(errorText + " : " + reason, severity = LOG_ERROR)
        #else:
        #    LOG(errorText, severity = LOG_ERROR)
        resp = MessageClass()
        resp.setId(msgId)
        resp.setType(MSG_TYPE_ERROR)
        resp.setSender(msg.getReceiver())
        resp.setReceiver(msg.getSender())
        resp[FIELD_ERROR] = errorText
        if reason is not None:
            resp[FIELD_REASON] = reason
        return resp
   
    #===========================================================================
    @staticmethod
    def createError2(msgId, senderId, receiverId, errorText, reason = None):
        if reason:
            LOG(errorText + ": " + reason, severity = LOG_ERROR)
        else:
            LOG(errorText, severity = LOG_ERROR)
        resp = MessageClass()
        resp.setId(msgId)
        resp.setType(MSG_TYPE_ERROR)
        resp.setSender(senderId)
        resp.setReceiver(receiverId)
        resp[FIELD_ERROR] = errorText
        if reason is not None:
            resp[FIELD_REASON] = reason
        return resp        
    