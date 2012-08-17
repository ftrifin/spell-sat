// ################################################################################
// FILE       : SPELLipcInterface.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Common IPC interface
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

#ifndef __SPELL_IPC_INTERFACE_H__
#define __SPELL_IPC_INTERFACE_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipc.H"
#include "SPELL_IPC/SPELLipcMessageMailbox.H"
#include "SPELL_IPC/SPELLipcInterfaceListener.H"
#include "SPELL_IPC/SPELLipcTrash.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"


/** \addtogroup SPELL_IPC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
class SPELLipcMessage;
class SPELLipcOutput;
class SPELLipcInput;
class SPELLipcIncomingBase;
class SPELLipcIncomingMessage;
class SPELLipcIncomingRequest;
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief IPC base interface
 **
 ** \par Description and usage:
 **
 ** 	Base class of both client and server interfaces. This class
 ** 	encapsulates the IPC socket-based mechanism used to send messages
 ** 	between SPELL processes.
 **
 ** \see SPELLipcClientInterface and SPELLipcServerInterface
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLipcInterface : public SPELLthread
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		 *
		 * \param name IN: name of the interface.
		**////////////////////////////////////////////////////////////////////
		SPELLipcInterface( std::string name );

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLipcInterface();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Initialize the interface to use the given IPC listener. This class
		 * will process all incoming messages and requests, as well as errors.
		 *
		 * \param listener IN: the listener to be used.
		**////////////////////////////////////////////////////////////////////
		void initialize( SPELLipcInterfaceListener* listener )
		{
			m_listener = listener;
		};

		//////////////////////////////////////////////////////////////////////
		/** Connect the IPC interface.
		**////////////////////////////////////////////////////////////////////
		virtual void connectIfc() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Disconnect the IPC interface.
		 *
		 * \param send_eoc IN: if true, send an "End-Of-Conversation" message.
		 * Otherwise, disconnect abruptly.
		**////////////////////////////////////////////////////////////////////
		virtual void disconnect( bool send_eoc );

		//////////////////////////////////////////////////////////////////////
		/** Disconnect the IPC interface for the given peer only.
		 *
		 * \param peerKey IN: key of the peer to be disconnected.
		 * \param send_eoc IN: if true, send an "End-Of-Conversation" message.
		 * Otherwise, disconnect abruptly.
		**////////////////////////////////////////////////////////////////////
		virtual void disconnect( int peerKey, bool send_eoc );

		//////////////////////////////////////////////////////////////////////
		/** Disconnect the output channel only
		 *
		 * \param peerKey IN: key of the peer to be disconnected.
		**////////////////////////////////////////////////////////////////////
		void disconnectOutput( int peerKey );

		//////////////////////////////////////////////////////////////////////
		/** Interface working thread method.
		 **///////////////////////////////////////////////////////////////////
		virtual void run() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Cancel all outgoing requests
		 **///////////////////////////////////////////////////////////////////
		void cancelOutgoingRequests( int peerKey );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    friend class SPELLipcIncomingMessage;
    friend class SPELLipcIncomingRequest;
    friend class SPELLipcInput;
    friend class SPELLipcOutput;

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // STATIC ////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Set the interface as ready to work.
		 **///////////////////////////////////////////////////////////////////
		void setReady();

		//////////////////////////////////////////////////////////////////////
		/** Wait for the interface to be ready.
		 **///////////////////////////////////////////////////////////////////
		void waitReady();

		//////////////////////////////////////////////////////////////////////
		/** Connection closed callback. Called when a peer disconnects from
		 * this interface.
		 *
		 * \param peerKey IN: key of the peer that disconnected.
		**////////////////////////////////////////////////////////////////////
		virtual void connectionClosed( int peerKey )= 0;

		//////////////////////////////////////////////////////////////////////
		/** Connection lost callback. Called when a peer abruptly disconnects
		 * from this interface.
		 *
		 * \param peerKey IN: key of the peer that disconnected.
		**////////////////////////////////////////////////////////////////////
		virtual void connectionLost( int peerKey ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Called when a SPELL request comes in.
		 *
		 * \param msg IN: request to be processed.
		**////////////////////////////////////////////////////////////////////
		void incomingRequest( SPELLipcMessage* msg );

		//////////////////////////////////////////////////////////////////////
		/** Called when a SPELL message comes in.
		 *
		 * \param msg IN: message to be processed.
		**////////////////////////////////////////////////////////////////////
		void incomingMessage( SPELLipcMessage* msg );

		//////////////////////////////////////////////////////////////////////
		/** Called when a response to a SPELL request comes in.
		 *
		 * \param msg IN: response to be processed.
		**////////////////////////////////////////////////////////////////////
		void incomingResponse( SPELLipcMessage* msg );

		//////////////////////////////////////////////////////////////////////
		/** Get the writer (output interface) for the given peer.
		 *
		 * \param peerKey IN: the key of the peer
		 *
		 * \return The output interface associated with the given peer.
		 **///////////////////////////////////////////////////////////////////
		virtual SPELLipcOutput& getWriter( int peerKey ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get the reader (input interface) for the given peer.
		 *
		 * \param peerKey IN: the key of the peer
		 *
		 * \return The input interface associated with the given peer.
		 **///////////////////////////////////////////////////////////////////
		virtual SPELLipcInput& getReader( int peerKey ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Start the processing of a given request.
		 *
		 * \param msg IN: request message
		 *
		 * \return The request unique identifier.
		 **///////////////////////////////////////////////////////////////////
		std::string startRequest( SPELLipcMessage* msg );

		//////////////////////////////////////////////////////////////////////
		/** Finish the given request processor.
		 *
		 * \param reqId IN: identifier of the request.
		 **///////////////////////////////////////////////////////////////////
		void endRequest( std::string reqId );

		//////////////////////////////////////////////////////////////////////
		/** Wait the response for a request given by its unique id.
		 *
		 * \param reqId IN: the unique request identifier.
		 * \param timeoutSec IN: the amount of seconds to wait for the response
		 *
		 * \return The response message.
		 *
		 **///////////////////////////////////////////////////////////////////
		SPELLipcMessage* waitResponse( std::string reqId, unsigned long timeoutSec );

		//////////////////////////////////////////////////////////////////////
		/** Wait the response for a request given by its unique id, with
		 *  no timeout.
		 *
		 * \param reqId IN: the unique request identifier.
		 *
		 * \return The response message.
		 *
		 **///////////////////////////////////////////////////////////////////
		SPELLipcMessage* waitResponse( std::string reqId );

		//////////////////////////////////////////////////////////////////////
		/** Check if the interface is connected.
		 *
		 * \return True if the interface is connected.
		 **///////////////////////////////////////////////////////////////////
		bool isConnected() {
			SPELLmonitor m(m_lock);
			return m_connected;
		};

		//////////////////////////////////////////////////////////////////////
		/** Perform request on the given writer.
		 *
		 * \param writer INOUT: the writer to use.
		 * \param request IN: request to perform
		 * \param timeoutSec IN: timeout for the response to arrive.
		 **///////////////////////////////////////////////////////////////////
		SPELLipcMessage* performRequest( SPELLipcOutput& writer, SPELLipcMessage* request, unsigned long timeoutSec );

		//////////////////////////////////////////////////////////////////////
		/** Perform request on the given writer without timeout.
		 *
		 * \param writer INOUT: the writer to use.
		 * \param request IN: request to perform
		 **///////////////////////////////////////////////////////////////////
		SPELLipcMessage* performRequest( SPELLipcOutput& writer, SPELLipcMessage* request );

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the interface name */
		std::string                m_name;
		/** Synchronization mutex for operations */
		SPELLmutex                 m_lock;
		/** True if the interface is connected */
		bool                       m_connected;
		/** Associated IPC listener */
		SPELLipcInterfaceListener* m_listener;

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the set of incoming request processors to be cleaned up */
		SPELLipcTrash          m_trash;
		/** Sequence number for outgoing requests */
		unsigned long          m_requestSequence;
		/** Sequence number for outgoing messages */
		unsigned long          m_messageSequence;
		/** Mailbox for incoming responses */
		SPELLipcMessageMailbox m_mailbox;
		/** Synchronization event for holding the caller until the ifc is ready */
		SPELLevent             m_readyEvent;
		/** Holds the map of ongoing requests by peer */
		std::map<std::string,int> m_outgoingRequests;
};

/*@}*/
#endif
