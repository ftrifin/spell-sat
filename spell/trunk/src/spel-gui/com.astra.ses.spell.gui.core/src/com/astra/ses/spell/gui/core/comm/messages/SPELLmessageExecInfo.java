///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageExecInfo.java
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
package com.astra.ses.spell.gui.core.comm.messages;

import java.util.ArrayList;
import java.util.List;

import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageId;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.model.server.ProcedureClient;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;

public class SPELLmessageExecInfo extends SPELLmessageRequest
{
	public SPELLmessageExecInfo(String procId)
	{
		super(IMessageId.REQ_EXEC_INFO);
		set(IMessageField.FIELD_PROC_ID, procId);
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
	}

	public static void fillExecInfo(IExecutorInfo model, SPELLmessage response)
	{
		String status = "UKNKNOWN";
		IProcedureClient cClient = null;;
		String condition = "";
		String mode = "";
		List<IProcedureClient> mClients = new ArrayList<IProcedureClient>();
		String parent = null;
		String actionLabel = "";
		String name = "";
		boolean actionEnabled = false;
		boolean automatic = true;
		boolean visible = true;
		boolean blocking = true;

		try
		{
			name = response.get(IMessageField.FIELD_PROC_NAME);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			status = response.get(IMessageField.FIELD_EXEC_STATUS);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			condition = response.get(IMessageField.FIELD_CONDITION);
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			String client = response.get(IMessageField.FIELD_GUI_CONTROL);
			if ((client != null)&&(!client.trim().isEmpty()))
			{
				String cClientHost = response.get(IMessageField.FIELD_GUI_CONTROL_HOST);
				if (cClientHost.trim().isEmpty())
				{
					cClientHost = "(unknown host)";
				}
				cClient = new ProcedureClient(client, cClientHost);
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			String list = response.get(IMessageField.FIELD_GUI_LIST);
			if ((list != null)&&(!list.trim().isEmpty()))
			{
				String[] mClientList = list.split(",");
				for(String mClient : mClientList)
				{
					String[] pair = mClient.split(":");
					if (pair.length==2)
					{
						mClients.add( new ProcedureClient(pair[1], pair[0]));
					}
					else
					{
						mClients.add( new ProcedureClient(pair[0], "(unknown)"));
					}
				}
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			if (response.hasKey(IMessageField.FIELD_PARENT_PROC))
			{
				parent = response.get(IMessageField.FIELD_PARENT_PROC);
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}

		try
		{
			mode = response.get(IMessageField.FIELD_OPEN_MODE);
			String elements[] = mode.split(",");
			for (String elem : elements)
			{
				String attr[] = elem.split(":");
				if (attr.length != 2)
				{
					continue;
				}
				String value = attr[1].trim();
				if (elem.indexOf(IMessageValue.OPEN_MODE_AUTOMATIC) != -1)
				{
					automatic = value.equals("True");
				}
				else if (elem.indexOf(IMessageValue.OPEN_MODE_VISIBLE) != -1)
				{
					visible = value.equals("True");
				}
				else if (elem.indexOf(IMessageValue.OPEN_MODE_BLOCKING) != -1)
				{
					blocking = value.equals("True");
				}
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}

		if (response.hasKey(IMessageField.FIELD_ACTION_LABEL))
		{
			try
			{
				actionLabel = response.get(IMessageField.FIELD_ACTION_LABEL);
			}
			catch (MessageException ex)
			{
				ex.printStackTrace();
			}
		}

		if (response.hasKey(IMessageField.FIELD_ACTION_ENABLED))
		{
			try
			{
				String actionEnabledStr = response
				        .get(IMessageField.FIELD_ACTION_ENABLED);
				actionEnabled = actionEnabledStr.equals("True");
			}
			catch (MessageException ex)
			{
				ex.printStackTrace();
			}
		}

		model.setCondition(condition);
		model.setMonitoringClients(mClients.toArray( new IProcedureClient[0] ));
		model.setControllingClient(cClient);
		model.setAutomatic(automatic);
		model.setVisible(visible);
		model.setBlocking(blocking);
		model.setStatus(ExecutorStatus.valueOf(status));
		model.setParent(parent);
		model.setUserAction(actionLabel);
		model.setUserActionEnabled(actionEnabled);
		model.setName( name );
		/*
		 * Client mode is inferred If the cClient is the same as the procId of
		 * the message, then mode is CONTROLLING If the procIc is in the
		 * monitoring clients list, then mode is MONITORING Else the mode is
		 * UNKNOWN
		 */
		/*
		 * String id = response.getKey(); ClientMode clientMode =
		 * ClientMode.UNKNOWN; if (cClient.equals(id)) { clientMode =
		 * ClientMode.CONTROLLING; } else { clientMode = ClientMode.MONITORING;
		 * } model.setMode(clientMode);
		 */
	}
}
