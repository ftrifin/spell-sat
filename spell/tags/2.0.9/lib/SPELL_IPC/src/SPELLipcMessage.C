// ################################################################################
// FILE       : SPELLipcMessage.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the IPC message
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcMessage.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"

// DEFINES /////////////////////////////////////////////////////////////////
const char KEY_SEP='\2';
const char PAIR_SEP='\1';

//=============================================================================
// FUNCTION: StringToMessageType
//=============================================================================
inline SPELLipcMessageType StringToMessageType( std::string str )
{
    for(int idx=0; idx<10; idx++)
    {
        if (MessageType::TypeStr[idx] == str) return (SPELLipcMessageType) idx;
    }
    return MSG_TYPE_UNKNOWN;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage::SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage()
{
    m_id = "noid";
    m_sequence = -1;
    m_type = MSG_TYPE_NOTYPE;
    m_senderId = MessageId::GENERIC_ID;
    m_receiverId = MessageId::GENERIC_ID;
    m_key = -1;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage::SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage( std::string id )
{
    m_id = id;
    m_sequence = -1;
    m_type = MSG_TYPE_NOTYPE;
    m_senderId = MessageId::GENERIC_ID;
    m_receiverId = MessageId::GENERIC_ID;
    m_key = -1;
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage::SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage( const SPELLipcMessage& msg )
{
    m_id = msg.m_id;
    m_sequence = msg.m_sequence;
    m_type = msg.m_type;
    m_senderId = msg.m_senderId;
    m_receiverId = msg.m_receiverId;
    m_key = msg.m_key;
    Properties::const_iterator it;
    Properties::const_iterator end = msg.m_properties.end();
    for( it = msg.m_properties.begin(); it != end; it++ )
    {
        m_properties.insert( std::make_pair( it->first, it->second ) );
    }
}

//=============================================================================
// CONSTRUCTOR: SPELLipcMessage:SPELLipcMessage
//=============================================================================
SPELLipcMessage::SPELLipcMessage( std::string id, Properties properties )
{
    m_id = id;
    m_sequence = -1;
    m_type = MSG_TYPE_NOTYPE;
    m_senderId = MessageId::GENERIC_ID;
    m_receiverId = MessageId::GENERIC_ID;
    m_key = -1;
    Properties::iterator it;
    for( it = properties.begin(); it != properties.end(); it++ )
    {
        m_properties.insert( std::make_pair( it->first, it->second ) );
    }
}

//=============================================================================
// DESTRUCTOR: SPELLipcMessage:SPELLipcMessage
//=============================================================================
SPELLipcMessage::~SPELLipcMessage()
{
    m_properties.clear();
}

//=============================================================================
// METHOD: SPELLipcMessage:set
//=============================================================================
void SPELLipcMessage::set( std::string key, std::string value )
{
    m_properties[key] = value;
}

//=============================================================================
// METHOD: SPELLipcMessage:get
//=============================================================================
std::string SPELLipcMessage::get( std::string key ) const
{
    Properties::const_iterator it = m_properties.find(key);
    if (it == m_properties.end())
    {
        return "";
    }
    return it->second;
}

//=============================================================================
// METHOD: SPELLipcMessage:hasField
//=============================================================================
bool SPELLipcMessage::hasField( std::string field ) const
{
    Properties::const_iterator it = m_properties.find(field);
    return (it != m_properties.end());
}

//=============================================================================
// METHOD: SPELLipcMessage:data
//=============================================================================
std::string SPELLipcMessage::data() const
{
    std::string data = "";
    Properties::const_iterator it;
    data += MessageField::FIELD_SENDER_ID + KEY_SEP + getSender() + PAIR_SEP;
    data += MessageField::FIELD_RECEIVER_ID + KEY_SEP + getReceiver() + PAIR_SEP;
    data += MessageField::FIELD_SEQUENCE + KEY_SEP + ISTR(m_sequence) + PAIR_SEP;
    data += MessageField::FIELD_ID + KEY_SEP + getId() + PAIR_SEP;
    data += MessageField::FIELD_TYPE + KEY_SEP + MessageType::TypeStr[getType()] + PAIR_SEP;
    data += MessageField::FIELD_IPC_KEY + KEY_SEP + ISTR(getKey());

    for( it = m_properties.begin(); it != m_properties.end(); it++ )
    {
        if (data.size()>1) data += PAIR_SEP;
        data += it->first + KEY_SEP + it->second;
    }
    return data;
}

//=============================================================================
// METHOD: SPELLipcMessage:fromData
//=============================================================================
void SPELLipcMessage::fromData( std::string data )
{
    //DEBUG("     Building message from " << data);
    m_properties.clear();
    std::string delim = "";
    delim += PAIR_SEP;
    std::vector<std::string> pairs = tokenize(data, delim );
    std::vector<std::string>::iterator it;
    for( it = pairs.begin(); it != pairs.end(); it++)
    {
        delim = "";
        delim += KEY_SEP;
        std::vector<std::string> pair = tokenize( (*it), delim );
        if (pair[0] == MessageField::FIELD_ID)
        {
            m_id = pair[1];
            //DEBUG("     SPELLipcMessage ID: " << m_id);
        }
        else if (pair[0] == MessageField::FIELD_TYPE)
        {
            m_type = StringToMessageType(pair[1]);
            //DEBUG("     SPELLipcMessage Type: " << m_type);
        }
        else if (pair[0] == MessageField::FIELD_SENDER_ID)
        {
            m_senderId = pair[1];
            //DEBUG("     SPELLipcMessage Sender: " << m_senderId);
        }
        else if (pair[0] == MessageField::FIELD_RECEIVER_ID)
        {
            m_receiverId = pair[1];
            //DEBUG("     SPELLipcMessage Receiver: " << m_receiverId);
        }
        else if (pair[0] == MessageField::FIELD_SEQUENCE)
        {
            m_sequence = atoi(pair[1].c_str());
            //DEBUG("     SPELLipcMessage Sequence: " << m_sequence);
        }
        else if (pair[0] == MessageField::FIELD_IPC_KEY)
        {
            m_key = atoi(pair[1].c_str());
            //DEBUG("     SPELLipcMessage key: " << m_key);
        }
        else
        {
            if (pair.size()==1) pair.push_back("");
            set( pair[0], pair[1] );
            //DEBUG("     Key " << pair[0] << "=" << pair[1]);
        }
    }
}
