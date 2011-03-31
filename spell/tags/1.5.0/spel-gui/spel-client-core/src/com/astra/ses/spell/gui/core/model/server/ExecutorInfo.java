///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ExecutorInfo.java
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

import java.util.Vector;

import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;


public class ExecutorInfo
{
	/** Holds the main procedure (root) identifier */
	private String m_procId;
	/** Holds the parent procedure identifier, if any */
	private String m_parentProcId;
	/** Holds the execution status */
	private ExecutorStatus m_status;
	/** Holds the execution condition */
	private String m_condition;
	/** Holds the controlling client if any */
	private String m_controllingClient;
	/** Holds the list of monitoring clients, if any */
	private Vector<String> m_monitoringClients;
	/** Holds the executor auxiliary port */
	private String m_port;
	/** Mode of this client for this executor */
	private ClientMode m_mode;
	/** Holds the current stage identifier if any */
	private String m_stageId;
	/** Holds the current stage name if any */
	private String m_stageTitle;
	/** True if the procedure is started in visible mode */
	private boolean m_visible;
	/** True if the procedure is started in automatic mode */
	private boolean m_automatic;
	/** True if the procedure is started in blocking mode */
	private boolean m_blocking;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ExecutorInfo( String procId )
	{
		m_port = "";
		m_controllingClient = "";
		m_monitoringClients = new Vector<String>();
		m_procId = procId;
		m_parentProcId = null;
		m_mode = ClientMode.UNKNOWN;
		m_condition = null;
		reset();
	}
	
	/***************************************************************************
	 * Reset runtime data
	 **************************************************************************/
	public void reset()
	{
		m_stageId = null;
		m_stageTitle = null;
		m_status = ExecutorStatus.UNKNOWN;
		m_visible = true;
		m_automatic = true;
		m_blocking = true;
	}
	
	/***************************************************************************
	 * Copy data from given info
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	public void copyFrom( ExecutorInfo info )
	{
		m_port = info.m_port;
		m_status = info.m_status;
		m_controllingClient = info.m_controllingClient;
		m_monitoringClients  = (Vector<String>) info.m_monitoringClients.clone();
		m_mode = info.m_mode;
		m_procId = info.m_procId;
		m_parentProcId = info.m_parentProcId;
		m_condition = info.m_condition;
		m_stageId = info.m_stageId;
		m_stageTitle = info.m_stageTitle;
		m_visible = info.m_visible;
		m_automatic = info.m_automatic;
		m_blocking = info.m_blocking;
	}
	
	/***************************************************************************
	 * Assign the executor current port
	 **************************************************************************/
	public void setPort( String port )
	{
		m_port = port;
	}
	
	/***************************************************************************
	 * Assign the client mode
	 **************************************************************************/
	public void setMode( ClientMode mode )
	{
		m_mode = mode;
	}

	/***************************************************************************
	 * Obtain the client mode
	 **************************************************************************/
	public ClientMode getMode()
	{
		return m_mode;
	}

	/***************************************************************************
	 * Assign the parent procedure
	 **************************************************************************/
	public void setParent( String parentId )
	{
		m_parentProcId = parentId;
	}
	
	/***************************************************************************
	 * Get the parent procedure if any
	 **************************************************************************/
	public String getParent()
	{
		return m_parentProcId;
	}

	/***************************************************************************
	 * Obtain the execution condition
	 **************************************************************************/
	public String getCondition()
	{
		return m_condition;
	}

	/***************************************************************************
	 * Set the execution condition
	 **************************************************************************/
	public void setCondition( String condition )
	{
		m_condition = condition;
	}

	/***************************************************************************
	 * Set automatic mode
	 **************************************************************************/
	public void setAutomatic( boolean automatic )
	{
		m_automatic = automatic;
	}

	/***************************************************************************
	 * Set visible mode
	 **************************************************************************/
	public void setVisible( boolean visible )
	{
		m_visible = visible;
	}

	/***************************************************************************
	 * Set blocking mode
	 **************************************************************************/
	public void setBlocking( boolean blocking )
	{
		m_blocking = blocking;
	}

	/***************************************************************************
	 * Get automatic mode
	 **************************************************************************/
	public boolean getAutomatic()
	{
		return m_automatic;
	}

	/***************************************************************************
	 * Get visible mode
	 **************************************************************************/
	public boolean getVisible()
	{
		return m_visible;
	}

	/***************************************************************************
	 * Get blocking mode
	 **************************************************************************/
	public boolean getBlocking()
	{
		return m_blocking;
	}

	/***************************************************************************
	 * Assing the current controlling client 
	 **************************************************************************/
	public void setControllingClient( String clientKey )
	{
		m_controllingClient = clientKey;
	}

	/***************************************************************************
	 * Assign the list of current monitoring clients
	 **************************************************************************/
	public void setMonitoringClients( String[] clientKeys )
	{
		m_monitoringClients.clear();
		for(String clt : clientKeys) m_monitoringClients.addElement(clt.trim());
	}
	
	/***************************************************************************
	 * Obtain the executor port
	 **************************************************************************/
	public String getPort()
	{
		return m_port;
	}
	
	/***************************************************************************
	 * Obtain the procedure identifier
	 **************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}

	/***************************************************************************
	 * Obtain the list of monitoring clients
	 **************************************************************************/
	public Vector<String> getMonitoringClients()
	{
		return m_monitoringClients;
	}

	/***************************************************************************
	 * Obtain the controlling client
	 **************************************************************************/
	public String getControllingClient()
	{
		return m_controllingClient.trim();
	}

	/***************************************************************************
	 * Obtain the current executor status
	 **************************************************************************/
	public ExecutorStatus getStatus()
	{
		return m_status;
	}

	/***************************************************************************
	 * Set the executor status
	 **************************************************************************/
	public void setStatus( ExecutorStatus st )
	{
		m_status = st;
	}

	/***************************************************************************
	 * Obtain the current stage identifier
	 **************************************************************************/
	public String getStageId()
	{
		return m_stageId;
	}

	/***************************************************************************
	 * Obtain the current stage title
	 **************************************************************************/
	public String getStageTitle()
	{
		return m_stageTitle;
	}

	/***************************************************************************
	 * Set the current stage
	 **************************************************************************/
	public void setStage( String id, String title )
	{
		m_stageId = id;
		m_stageTitle = title;
	}
}
