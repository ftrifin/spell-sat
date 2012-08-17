///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ServerInfo.java
//
// DATE      : 2008-11-21 08:58
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
 ******************************************************************************/
public class ServerInfo
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final String	SERVER_NAME	    = "name";
	private static final String	SERVER_HOST	    = "host";
	private static final String	SERVER_PORT	    = "port";
	private static final String	SERVER_USER	    = "user";
	private static final String	SERVER_PASSWORD	= "pwd";
	private static final String	SERVER_ROLE	    = "role";

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	public static enum ServerRole
	{
		COMMANDING, MONITORING
	}

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final String	SEPARATOR	= "<>";
	/** Holds the server name */
	private String	            m_name;
	/** Holds the server hostname */
	private String	            m_host;
	/** Holds the username to be used when establishing SSH tunnels */
	private String	            m_tunnelUser;
	/** Holds the password to be used when establishing SSH tunnels */
	private String	            m_tunnelPassword;
	/** Holds the server port */
	private int	                m_port;
	/** Holds the server role */
	private ServerRole	        m_role;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	/**************************************************************************
	 * Create a SPELLServer instance from a String
	 * 
	 * @param stringifiedServer
	 *            the SPELL serve represented as a String following the pattern
	 *            id<>name<>host<>port<>user<>role
	 * @return
	 *************************************************************************/
	public static ServerInfo valueOf(String stringifiedServer)
	{
		String[] s = stringifiedServer.split(SEPARATOR);
		return new ServerInfo(s[0], s[1], Integer.valueOf(s[2]), s[3], s[4],
		        ServerRole.valueOf(s[5]));
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ServerInfo()
	{
		m_name = null;
		m_host = null;
		m_tunnelUser = null;
		m_tunnelPassword = null;
		m_role = ServerRole.COMMANDING;
		m_port = 0;
	}

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public ServerInfo(String name, String host, int port, String user,
	        String password, ServerRole role)
	{
		m_name = name;
		m_host = host;
		m_port = port;
		m_tunnelUser = (user.equals("null")) ? null : user;
		m_tunnelPassword = (password.equals("null")) ? null : password;
		m_role = role;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ServerInfo(Element xmlElement)
	{
		this();

		if (xmlElement.getNodeType() == Node.ELEMENT_NODE)
		{
			NodeList nodes = xmlElement.getChildNodes();
			for (int idx = 0; idx < nodes.getLength(); idx++)
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
						m_tunnelUser = node.getTextContent();
					}
					else if (name.equals(SERVER_PASSWORD))
					{
						m_tunnelPassword = node.getTextContent();
					}
					else if (name.equals(SERVER_NAME))
					{
						m_name = node.getTextContent();
					}
					else if (name.equals(SERVER_ROLE))
					{
						try
						{
							m_role = ServerRole.valueOf(node.getTextContent());
						}
						catch (Exception ex)
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
		return (m_name != null) && (m_host != null) && (m_port != 0);
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}

	public void setHost(String host)
	{
		m_host = host;
	}

	public String getHost()
	{
		return m_host;
	}

	/***************************************************************************
	 * Obtain the user name for tunneled connection
	 * 
	 * @return The user name
	 **************************************************************************/
	public String getTunnelUser()
	{
		return m_tunnelUser;
	}

	/***************************************************************************
	 * Set the user name for tunneled connections
	 * 
	 * @param user
	 *            The user name
	 **************************************************************************/
	public void setTunnelUser(String user)
	{
		m_tunnelUser = user;
	}

	/***************************************************************************
	 * Obtain the password for tunneled connections
	 * 
	 * @return The password
	 **************************************************************************/
	public String getTunnelPassword()
	{
		return m_tunnelPassword;
	}

	/***************************************************************************
	 * Set the password for tunneled connections
	 * 
	 * @param pwd
	 *            The password
	 **************************************************************************/
	public void setTunnelPassword(String pwd)
	{
		m_tunnelPassword = pwd;
	}

	public void setPort(int port)
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

	@Override
	public String toString()
	{
		return m_name + SEPARATOR + m_host + SEPARATOR + m_port + SEPARATOR
		        + m_tunnelUser + SEPARATOR + m_tunnelPassword + SEPARATOR + m_role.toString();
	}
}
