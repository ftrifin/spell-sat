///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ServerInfo.java
//
// DATE      : 2008-11-21 08:58
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.gui.core.model.server;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*******************************************************************************
 * @brief Data structure holding the connection info for a SPELL server
 * @date 28/04/08
 * @author Rafael Chinchilla (GMV)
 ******************************************************************************/
public class ServerInfo
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final String SERVER_ID       = "id";
	private static final String SERVER_NAME     = "name";
	private static final String SERVER_HOST     = "host";
	private static final String SERVER_PORT     = "port";
	private static final String SERVER_USER     = "user";
	private static final String SERVER_PASSWORD = "pwd";
	private static final String SERVER_ROLE     = "role";
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static enum ServerRole
	{
		COMMANDING,
		MONITORING
	}

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the server ID */
	private String m_id;
	/** Holds the server name */
	private String m_name;
	/** Holds the server hostname */
	private String m_host;
	/** Holds the username to be used when establishing SSH tunnels */
	private String m_user;
	/** Holds the password to be used when establishing SSH tunnels */
	private String m_pwd;
	/** Holds the server port */
	private int m_port;
	/** Holds the server role */
	private ServerRole m_role;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ServerInfo( String id )
	{
		m_id = null;
		m_name = null;
		m_host = null;
		m_user = null;
		m_pwd = null;
		m_role = ServerRole.COMMANDING;
		m_port = 0;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ServerInfo( Element xmlElement )
	{
		this("");
		
		if (xmlElement.getNodeType() == Node.ELEMENT_NODE)
		{
			NodeList nodes = xmlElement.getChildNodes();
			for(int idx= 0; idx<nodes.getLength(); idx++)
			{
				Node node = (Node) nodes.item(idx);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					String name = node.getNodeName();
					if (name.equals(SERVER_HOST))
					{
						m_host = node.getTextContent();
					}
					else if (name.equals(SERVER_PORT))
					{
						m_port = Integer.parseInt(node.getTextContent());
					}
					else if (name.equals(SERVER_USER))
					{
						m_user = node.getTextContent();
					}
					else if (name.equals(SERVER_PASSWORD))
					{
						m_pwd = node.getTextContent();
					}
					else if (name.equals(SERVER_NAME))
					{
						m_name = node.getTextContent();
					}
					else if (name.equals(SERVER_ID))
					{
						m_id = node.getTextContent();
					}
					else if (name.equals(SERVER_ROLE))
					{
						try
						{
							m_role = ServerRole.valueOf(node.getTextContent());
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
							m_role = ServerRole.COMMANDING;
						}
					}
				}
			}
		}
	}
	
	public boolean validate()
	{
		return (!m_id.equals("")) && (m_name != null) && (m_host != null) && (m_port != 0); 
	}
	
	public String getId()
	{
		return m_id;
	}

	public void setName( String name )
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}

	public void setHost( String host )
	{
		m_host = host;
	}

	public String getHost()
	{
		return m_host;
	}
	
	public String getUser()
	{
		return m_user;
	}

	public void setUser( String user )
	{
		m_user = user;
	}

	public String getPwd()
	{
		return m_pwd;
	}

	public void setPwd( String pwd )
	{
		m_pwd = pwd;
	}

	public void setPort( int port )
	{
		m_port = port;
	}

	public int getPort()
	{
		return m_port;
	}

	public ServerRole getRole()
	{
		return m_role;
	}
}
