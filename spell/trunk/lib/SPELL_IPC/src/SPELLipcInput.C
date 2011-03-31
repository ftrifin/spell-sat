// ################################################################################
// FILE       : SPELLipcInput.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data reader
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
// Local includes ----------------------------------------------------------
#include "SPELL_IPC/SPELLipcError.H"
#include "SPELL_IPC/SPELLipcInput.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmonitor.H"
#include "SPELL_UTIL/SPELLlog.H"
// System includes ---------------------------------------------------------

// DEFINES /////////////////////////////////////////////////////////////////

#define NAME std::string("[ IPC-IN-") + ISTR(m_key) + "-" + m_name + " ] "
#define SIZE sizeof(unsigned int)

//=============================================================================
// CONSTRUCTOR: SPELLipcInput::SPELLipcInput
//=============================================================================
SPELLipcInput::SPELLipcInput( std::string name, SPELLsocket* skt, int peerKey, SPELLipcInterface* ifc )
    : SPELLthread("IFCinput"),
      m_socket(skt),
      m_interface(ifc),
      m_lock()
{
    m_name = name;
    m_key = peerKey;
    m_connected = true;
    m_finishEvent.clear();
    DEBUG( NAME + "SPELLipcInput created");
}

//=============================================================================
// DESTRUCTOR: SPELLipcInput:SPELLipcInput
//=============================================================================
SPELLipcInput::~SPELLipcInput()
{
    if (m_packet != NULL)
    {
        delete m_packet;
        m_packet = NULL;
        m_lastByte = NULL;
    }
    DEBUG( NAME + "SPELLipcInput destroyed ##");
}


//=============================================================================
// METHOD: SPELLipcInput::readPacketLength
//=============================================================================
int SPELLipcInput::readPacketLength( int numRead )
{
    assert( (unsigned int)numRead >= SIZE);
    //DEBUG(NAME << "Reading length");

    // The buffer shall contain the length in the first SIZE bytes

    // We shall take into account endianess
    unsigned int length = 0;
    for( unsigned int c = 0; c<SIZE; c++ )
    {
        length = (length<<8) + (m_buffer[c] < 0 ? m_buffer[c] + 256 : m_buffer[c]);
    }
    m_totalPacketLength = length;

    //DEBUG( NAME << "Read length: " << m_totalPacketLength);
    assert(m_totalPacketLength < 9999000);

    // Remove the length bytes from buffer
    for( unsigned int byte=0; byte<(numRead-SIZE); byte++)
    {
        memcpy(m_buffer+byte,m_buffer+SIZE+byte,1);
    }
    // Reset the SIZE latest bytes
    memset(m_buffer+numRead-SIZE, 0, SIZE);
    // Reduce the number of read bytes
    numRead -= SIZE;
    //DEBUG( NAME << "Bytes after reading length: " << numRead);
    return numRead;
}

//=============================================================================
// METHOD: SPELLipcInput::readMoreBytes
//=============================================================================
int SPELLipcInput::readMoreBytes()
{
    int numRead = 0;
    memset ( m_buffer, 0, IPC_BUFFER_SIZE + 1);

    while(1)
    {
        bool dataIn = m_socket->waitData(500);//0.5 sec
        if (dataIn)
        {
            numRead = m_socket->receive( m_buffer, IPC_BUFFER_SIZE );
            if (numRead == 0)
            {
                // When we receive zero it means that the peer has invoked
                // shutdown(). We shall do the same on our side. The IPC
                // interface is in charge of that, we shall not touch the
                // socket file descriptor here in SPELLipcInput. Just finish the
                // loop.
                forceDisconnect();
            }
            return numRead;
        }
        if (!isConnected())
        {
            return 0;
        }
    }
}

//=============================================================================
// METHOD: SPELLipcInput::completeLength
//=============================================================================
int SPELLipcInput::completeLength( int numRead )
{
    int more = m_socket->receive( m_buffer+numRead, IPC_BUFFER_SIZE-numRead );
    assert( more >= 0 );
    if (more == 0)
    {
        DEBUG( NAME + "Read 0, peer has disconnected");
        // When we receive zero it means that the peer has invoked
        // shutdown(). We shall do the same on our side. The IPC
        // interface is in charge of that, we shall not touch the
        // socket file descriptor here in SPELLipcInput. Just finish the
        // loop.
        forceDisconnect();
    }
    return more;
}

//=============================================================================
// METHOD: SPELLipcInput::addBytesToPacket
//=============================================================================
void SPELLipcInput::addBytesToPacket( int numRead )
{
    assert( m_lastByte != NULL );
    //DEBUG(NAME << "[1] Adding " << numRead << " bytes to packet");
    memcpy(m_lastByte,m_buffer,numRead);
    m_lastByte += numRead;
    //DEBUG(NAME << "[1] Read so far: " << (m_lastByte-m_packet) << " bytes");
    //DEBUG(NAME << "[1] No bytes remaining");
}

//=============================================================================
// METHOD: SPELLipcInput::addBytesToPacketExtra
//=============================================================================
int SPELLipcInput::addBytesToPacketExtra( int numRead, int readLength )
{
    int bytesToRead = (m_totalPacketLength - readLength);
    //DEBUG(NAME << "[2] Second case, there are " << numRead << " bytes and packet is " << m_totalPacketLength << " bytes long");
    //DEBUG(NAME << "[2] So far I read " << readLength << " so I need to read " << bytesToRead << " bytes");
    memcpy(m_lastByte,m_buffer,bytesToRead);

    // Dispatch the completed packet
    //DEBUG( NAME << "[2] Dispatch")
    dispatch();

    // Remove the length bytes from buffer
    for( int byte=0; byte<(numRead-bytesToRead); byte++)
    {
        memcpy(m_buffer+byte,m_buffer+bytesToRead+byte,1);
    }
    // Reset the bytesToRead latest bytes
    memset(m_buffer+numRead-bytesToRead, 0, bytesToRead);
    // Reduce the number of read bytes
    numRead -= bytesToRead;
    //DEBUG(NAME << "[2] Remaining: " << numRead << " bytes: '" << std::string(m_buffer) << "'");

    return numRead;
}

//=============================================================================
// METHOD: SPELLipcInput::addBytesToPacketFit
//=============================================================================
void SPELLipcInput::addBytesToPacketFit( int numRead )
{
    //DEBUG( NAME << "[3] Add packets")
    memcpy(m_lastByte,m_buffer,numRead);
    // Dispatch the completed packet
    //DEBUG( NAME << "[3] Dispatch")
    dispatch();
    //DEBUG(NAME << "[3] No bytes remaining");
}

//=============================================================================
// METHOD: SPELLipcInput::createPacket
//=============================================================================
void SPELLipcInput::createPacket()
{
    assert( m_packet == NULL );
    //DEBUG(NAME << "Creating packet of length " << m_totalPacketLength);
    assert(m_totalPacketLength>0);
    // Create the packet
    // We need one byte more for the final \0 character
    m_packet = new char[m_totalPacketLength+1];
    memset(m_packet, 0, m_totalPacketLength+1);
    m_lastByte = m_packet;
}

//=============================================================================
// METHOD: SPELLipcInput::processReadBytes
//=============================================================================
int SPELLipcInput::processReadBytes( int numRead )
{
    // Copy the bytes to the packet latest position
    // This is the packet length so fat
    int readLength = m_lastByte - m_packet;

    //DEBUG(NAME << "+---------------------------------------------------------+")
    //DEBUG(NAME << "Incoming bytes: " << numRead);
    //DEBUG(NAME << "Read so far: " << readLength);

    // First case: the number of read bytes does not complete a packet yet
    if ( (unsigned int)numRead < (m_totalPacketLength - readLength))
    {
        addBytesToPacket( numRead );
        numRead = 0;
    }
    // Second case: the number of read bytes is bigger than the number of bytes
    // required to complete the packet
    else if ( (unsigned int)numRead > (m_totalPacketLength - readLength))
    {
        numRead = addBytesToPacketExtra( numRead, readLength );
    }
    // Third case: the bytes comming are exactly the amount needed
    else if ((unsigned int)numRead == (m_totalPacketLength - readLength))
    {
        addBytesToPacketFit( numRead );
        numRead = 0;
    }
    //DEBUG(NAME << "+---------------------------------------------------------+")
    return numRead;
}

//=============================================================================
// METHOD: SPELLipcInput::run
//=============================================================================
void SPELLipcInput::run()
{
    DEBUG( NAME + "SPELLipcInput start");

    int numRead = 0;
    m_packet = NULL;
    m_lastByte = m_packet;
    m_totalPacketLength = 0;

    DEBUG( NAME + "Setting interface ready");
    m_interface->setReady();

    DEBUG( NAME + "Entering loop");

    while(isConnected())
    {
        try
        {
            // Read bytes on buffer only if the buffer is empty
            if (numRead==0)
            {
                numRead = readMoreBytes();
                // If we did not read anything, terminate
                if (numRead == 0)
                {
                    break;
                }
            }
            else if ( (unsigned int)numRead < SIZE)
            {
                numRead += completeLength( numRead );
            }

            if (isConnected() && (numRead==-1 || errno != 0))
            {
                LOG_ERROR( "[IPC] Error on input, errno " + ISTR(errno));
                connectionLost();
                break;
            }

            if (m_totalPacketLength == 0)
            {
                // Once we have bytes, if there is no packet length defined yet
                if ( (unsigned int) numRead>=SIZE )
                {
                    numRead = readPacketLength( numRead );
                }
            }
            else
            {
                // If we have bytes to read
                if ( numRead>0 && isConnected() )
                {
                    //DEBUG(NAME << "Reading bytes for packet");
                    // If we still dont have a packet
                    if (m_packet == NULL)
                    {
                        createPacket();
                    }
                    numRead = processReadBytes( numRead );
                }
            }
        }
        catch( SPELLipcError& ex )
        {
            switch(ex.getCode())
            {
            case IPC_ERROR_UNKNOWN_MSG_TYPE:
                LOG_ERROR( NAME + "Unable to process packet. " + ex.getError())
                // Remove all packet data and continue processing
                if (m_packet != NULL)
                {
                    delete m_packet;
                    m_packet = NULL;
                    m_lastByte = NULL;
                    m_totalPacketLength = 0;
                }
                break;
            default:
                LOG_ERROR( NAME + "Fatal error while processing data. " + ex.getError());
                if (m_connected) m_interface->disconnect(false);
            }
        }

    }
    DEBUG( NAME + "SPELLipcInput interface end");
    m_finishEvent.set();
}

//=============================================================================
// METHOD: SPELLipcInput::getConnected
//=============================================================================
bool SPELLipcInput::isConnected()
{
    return m_connected;
}

//=============================================================================
// METHOD: SPELLipcInput::disconnect
//=============================================================================
void SPELLipcInput::disconnect()
{
    // Just change the connected flag so that the input thread finishes.
    // We shall not disconnect the socket since it is the IPC interface
    // who is in charge of creating and closing the sockets.

    if (!m_connected) return;

    DEBUG( NAME + "Disconnect input interface");
    m_connected = false;

    DEBUG( NAME + "Disconnected");
}

//=============================================================================
// METHOD: SPELLipcInput::wait
//=============================================================================
void SPELLipcInput::wait()
{
    DEBUG( NAME + "SPELLipcInput wait to finish");
    m_finishEvent.wait();
    DEBUG( NAME + "SPELLipcInput wait to finish done");
}

//=============================================================================
// METHOD: SPELLipcInput::disconnect
//=============================================================================
void SPELLipcInput::forceDisconnect()
{
    if (m_connected == true)
    {
        DEBUG( NAME + "SPELLipcInput FORCE disconnection");
        m_connected = false;
        m_finishEvent.set();
        m_interface->disconnect( m_key, false );
    }
}

//=============================================================================
// METHOD: SPELLipcInput::readData
//=============================================================================
void SPELLipcInput::dispatch()
{
    if (!m_connected) return;
    std::string spacket = std::string(m_packet);
    DEBUG( NAME + "Dispatching: '" + spacket + "'");
    assert( spacket.size() == m_totalPacketLength );
    // IMPORTANT: these messages are deleted after processing, in the IncomingXX classes.
    SPELLipcMessage* msg = new SPELLipcMessage();
    DEBUG( NAME + "Created message");
    msg->fromData( spacket );
    msg->setKey(m_key);
    switch(msg->getType())
    {
    case MSG_TYPE_RESPONSE:
    case MSG_TYPE_ERROR:
        m_interface->incomingResponse(msg);
        break;
    case MSG_TYPE_NOTIFY:
    case MSG_TYPE_PROMPT:
    case MSG_TYPE_REQUEST:
        m_interface->incomingRequest(msg);
        break;
    case MSG_TYPE_ONEWAY:
    case MSG_TYPE_NOTIFY_ASYNC:
    case MSG_TYPE_WRITE:
        m_interface->incomingMessage(msg);
        break;
    case MSG_TYPE_EOC:
        DEBUG(NAME + "Received EOC");
        connectionClosed();
        break;
    default:
        std::string msgType = ISTR(msg->getType());
        delete msg;
        throw SPELLipcError("Unknown message type: " + msgType, IPC_ERROR_UNKNOWN_MSG_TYPE );
    }
    delete m_packet;
    m_packet = NULL;
    m_lastByte = NULL;
    m_totalPacketLength = 0;
}

//=============================================================================
// METHOD: SPELLipcInput::connectionLost
//=============================================================================
void SPELLipcInput::connectionLost()
{
    disconnect();
    m_finishEvent.set();
    m_interface->connectionLost(m_key);
}

//=============================================================================
// METHOD: SPELLipcInput::connectionClosed
//=============================================================================
void SPELLipcInput::connectionClosed()
{
    disconnect();
    m_finishEvent.set();
    m_interface->connectionClosed(m_key);
}
