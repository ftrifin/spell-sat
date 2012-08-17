// ################################################################################
// FILE       : SPELLrequestProcessor.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Processing code for client requests
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

#ifndef __SPELL_REQUEST_PROCESSOR_H__
#define __SPELL_REQUEST_PROCESSOR_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcMessage.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_CIFS */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLserverCif;
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief IPC request processor for executor
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLrequestProcessor
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLrequestProcessor();

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLrequestProcessor();

    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    friend class SPELLserverCif;

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // STATIC ////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Process a message regarding a child procedure.
		 *
		 * \param msg IN: IPC message to process.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processMessageForChild( SPELLipcMessage* msg );

		//////////////////////////////////////////////////////////////////////
		/** Process a notification regarding a child procedure.
		 *
		 * \param msg IN: IPC message to process.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processNotificationForChild( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request to obtain the current configuration.
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processGetConfig( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request to set the current configuration.
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processSetConfig( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request to toggle a breapoint.
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processSetBreakpoint ( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request to clear all breakpoints.
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processClearBreakpoints ( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request to get current variable names.
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processGetVariables( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request to watch a variable.
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processVariableWatch( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request stop watching a variable.
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processVariableNoWatch( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request to stop watching all variables.
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processWatchNothing( SPELLipcMessage* msg, SPELLipcMessage* response );

		//////////////////////////////////////////////////////////////////////
		/** Process a request to change a variable value
		 *
		 * \param msg IN: IPC request.
		 * \param response INOUT: IPC response to complete.
		 *
		**////////////////////////////////////////////////////////////////////
    	void processChangeVariable( SPELLipcMessage* msg, SPELLipcMessage* response );
};

/*@}*/
#endif
