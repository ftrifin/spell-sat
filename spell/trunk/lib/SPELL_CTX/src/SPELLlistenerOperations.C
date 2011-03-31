// ################################################################################
// FILE       : SPELLlistenerOperations.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the listener operations
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
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLutils.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLerror.H"
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipcHelper.H"
// Local includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLcontext.H"


//=============================================================================
// CONSTRUCTOR : SPELLlistenerOperations::SPELLlistenerOperations
//=============================================================================
SPELLlistenerOperations::SPELLlistenerOperations( SPELLcontext& context )
    : SPELLipcInterfaceListener(),
      m_context(context)
{
}

//=============================================================================
// DESTRUCTOR: SPELLlistenerOperations::~SPELLlistenerOperations
//=============================================================================
SPELLlistenerOperations::~SPELLlistenerOperations()
{
}

//=============================================================================
// METHOD :    SPELLlistenerOperations::processMessage
//=============================================================================
void SPELLlistenerOperations::processMessage( SPELLipcMessage* msg )
{
    DEBUG("[LST-OP] Received listener message: " + msg->getId());
    if (msg->getType() == MSG_TYPE_ONEWAY)
    {
        if (msg->getId() == ContextMessages::MSG_CLOSE_CTX)
        {
            m_context.close();
        }
        else
        {
            LOG_ERROR("[LST-OP] Unprocessed message: " + msg->getId());
        }
    }
    else
    {
        LOG_ERROR("[LST-OP] Unprocessed request: " + msg->getId());
    }
}

//=============================================================================
// METHOD :    SPELLlistenerOperations::processRequest
//=============================================================================
SPELLipcMessage* SPELLlistenerOperations::processRequest( SPELLipcMessage* msg )
{
    SPELLipcMessage* response = NULL;
    if (msg->getId() == ContextMessages::REQ_CAN_CLOSE)
    {
        response = request_CanClose(msg);
    }
    return response;
}

//=============================================================================
// METHOD :    SPELLlistenerOperations::processError
//=============================================================================
void SPELLlistenerOperations::processError( std::string error, std::string reason )
{
	/** \todo listener lost */
}

//=============================================================================
// METHOD :    SPELLlistenerOperations::request_CanClose
//=============================================================================
SPELLipcMessage* SPELLlistenerOperations::request_CanClose( SPELLipcMessage* msg )
{
    DEBUG("[LST-OP] Requested can-close")
	/** \todo ask context if can be closed now */
    SPELLipcMessage* response = SPELLipcHelper::createResponse( ContextMessages::RSP_CAN_CLOSE, msg);
    response->set( MessageField::FIELD_BOOL, "True" );
    DEBUG("[CLT-OP] Request finished")
    return response;
}
