///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessage.java
//
// DATE      : 2008-11-21 08:58
//
// Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.comm.messages;

import java.util.TreeMap;

/*******************************************************************************
 * @brief Helper class for processing XML messages exchanged with SPELL server
 * @date 18/09/07
 ******************************************************************************/
public class SPELLmessage
{
	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	private static final String	    KEY_SEP	 = "\2";
	private static final String	    PAIR_SEP	= "\1";

	// PRIVATE -----------------------------------------------------------------
	/** Holds the XML data parsing class */
	private TreeMap<String, String>	m_data;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Message constructor
	 **************************************************************************/
	public SPELLmessage(TreeMap<String, String> data)
	{
		m_data = data;
	}

	/***************************************************************************
	 * Empty message constructor
	 **************************************************************************/
	public SPELLmessage()
	{
		m_data = new TreeMap<String, String>();
	}

	/***************************************************************************
	 * Obtain the XML message data string
	 * 
	 * @return The XML data string
	 * @throws MessageException
	 *             if there is an error in the data.
	 **************************************************************************/
	public String data() throws MessageException
	{
		String result = "";
		int numKeys = m_data.size();
		int count = 0;
		for (String key : m_data.keySet())
		{
			result += key + KEY_SEP + m_data.get(key);
			if (count < numKeys - 1) result += PAIR_SEP;
			count++;
		}
		return result;
	}

	/***************************************************************************
	 * Obtain the type of the message. The type field specifies if the message
	 * is a SPELL command, a data request, a failure information, or other
	 * types. The type field is the root node of the DOM document as well.
	 * 
	 * @return The data type.
	 **************************************************************************/
	public String getType()
	{
		return m_data.get("root");
	}

	/***************************************************************************
	 * Set the message type.
	 * 
	 * @param type
	 *            The message type string.
	 **************************************************************************/
	public void setType(String type)
	{
		m_data.put("root", type);
	}

	/***************************************************************************
	 * Obtain the message identifier. The message identifier can be seen as the
	 * message subtype. For example, allows distinguishing between different
	 * subtypes of commands or requests.
	 * 
	 * @return The message identifier.
	 **************************************************************************/
	public String getId()
	{
		return m_data.get(IMessageField.FIELD_ID);
	}

	/***************************************************************************
	 * Assign the message identifier of this message.
	 * 
	 * @param id
	 *            The message identifier.
	 **************************************************************************/
	public void setId(String id)
	{
		m_data.put(IMessageField.FIELD_ID, id);
	}

	/***************************************************************************
	 * Assign the sequence number of this message.
	 **************************************************************************/
	public void setSequence(long seq)
	{
		m_data.put(IMessageField.FIELD_SEQUENCE, Long.toString(seq));
	}

	/***************************************************************************
	 * Get the sequence number of this message.
	 **************************************************************************/
	public long getSequence()
	{
		String seq = m_data.get(IMessageField.FIELD_SEQUENCE);
		if (seq == null) return -1;
		return Long.parseLong(seq);
	}

	/***************************************************************************
	 * Obtain the key of the source peer of this message. Each peer should have
	 * an unique key which identifies it.
	 * 
	 * @return The source peer key.
	 **************************************************************************/
	public String getKey()
	{
		return m_data.get(IMessageField.FIELD_IPC_KEY);
	}

	/***************************************************************************
	 * Obtain the identifier of the sender of this message.
	 * 
	 * @return The sender id
	 **************************************************************************/
	public String getSender()
	{
		return m_data.get(IMessageField.FIELD_SENDER_ID);
	}

	/***************************************************************************
	 * Obtain the identifier of the receiver of this message.
	 * 
	 * @return The receiver id
	 **************************************************************************/
	public String getReceiver()
	{
		return m_data.get(IMessageField.FIELD_RECEIVER_ID);
	}

	/***************************************************************************
	 * Assign the source peer key of this message.
	 * 
	 * @param src
	 *            The source peer key.
	 **************************************************************************/
	public void setKey(String src)
	{
		m_data.put(IMessageField.FIELD_IPC_KEY, src);
	}

	/***************************************************************************
	 * Assign the receiver id
	 * 
	 * @param id
	 *            The receiver id
	 **************************************************************************/
	public void setReceiver(String id)
	{
		m_data.put(IMessageField.FIELD_RECEIVER_ID, id);
	}

	/***************************************************************************
	 * Assign the sender id
	 * 
	 * @param id
	 *            The sender id
	 **************************************************************************/
	public void setSender(String id)
	{
		m_data.put(IMessageField.FIELD_SENDER_ID, id);
	}

	/***************************************************************************
	 * Set a message property name and value.
	 * 
	 * @param name
	 *            Property name.
	 * @param value
	 *            Property value.
	 **************************************************************************/
	public void set(String name, String value)
	{
		m_data.put(name, value);
	}

	/***************************************************************************
	 * Obtain the value of a given property.
	 * 
	 * @param name
	 *            Property name.
	 * @return Property value.
	 * @throws MessageException
	 *             if there is no such property.
	 **************************************************************************/
	public String get(String name) throws MessageException
	{
		if (!m_data.containsKey(name))
		{
			System.err.println("MISSING PROPERTY: " + name);
			System.err.println("--------------------------");
			System.err.println(data());
			System.err.println("--------------------------");
			throw new MessageException("Message has not such property: " + name);
		}
		return m_data.get(name);
	}

	/***************************************************************************
	 * Check if the message contains the given key
	 **************************************************************************/
	public boolean hasKey(String key)
	{
		return m_data.containsKey(key);
	}

	/***************************************************************************
	 * Extract tags
	 **************************************************************************/
	public static TreeMap<String, String> getTags(String data)
	{
		TreeMap<String, String> tags = new TreeMap<String, String>();
		String elements[] = data.split(PAIR_SEP);
		for (String element : elements)
		{
			String[] kv = element.split(KEY_SEP);
			if (kv.length == 1)
			{
				tags.put(kv[0], "");
			}
			else
			{
				tags.put(kv[0], kv[1]);
			}
		}
		return tags;
	}
}
