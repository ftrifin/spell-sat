///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.messages
// 
// FILE      : SPELLmessageGetVariables.java
//
// DATE      : Nov 28, 2011
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.watchvariables.messages;

import java.util.ArrayList;

import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageRequest;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.core.interfaces.IMessageValue;
import com.astra.ses.spell.gui.core.model.server.TransferData;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.notification.WhichVariables;

public class SPELLmessageGetVariables extends SPELLmessageRequest
{
	public SPELLmessageGetVariables(String procId, WhichVariables whichOnes)
	{
		this(procId, whichOnes, -1);
	}

	public SPELLmessageGetVariables(String procId, WhichVariables whichOnes, int chunkNo)
	{
		super(IWVMessageId.REQ_VARIABLE_NAMES);
		set(IMessageField.FIELD_PROC_ID, procId);
		set(IWVMessageField.FIELD_VARIABLE_GET, whichOnes.name());
		setSender(IMessageValue.CLIENT_SENDER);
		if (chunkNo >= 0)
		{
			set(IMessageField.FIELD_CHUNK, Integer.toString(chunkNo));
		}
		setReceiver(procId);
	}

	public static TransferData getValueChunk(SPELLmessage response)
	{
		TransferData data = null;
		try
		{
			String value = response.get(IWVMessageField.FIELD_VARIABLE_VALUE);
			if (response.hasKey(IMessageField.FIELD_CHUNK))
			{
				int chunkNo = Integer.parseInt(response.get(IMessageField.FIELD_CHUNK));
				int totalChunks = Integer.parseInt(response.get(IMessageField.FIELD_TOTAL_CHUNKS));
				data = new TransferData(value, chunkNo, totalChunks);
			}
			else
			{
				data = new TransferData(value, 0, 0);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return data;
	}

	public static String[] getValues( String valueList)
	{
		return valueList.split(IMessageField.VARIABLE_SEPARATOR);
	}

	public static VariableData[] getVariables(WhichVariables whichOnes, SPELLmessage response)
	{
		ArrayList<VariableData> result = new ArrayList<VariableData>();
		try
		{
			String nameList = response.get(IWVMessageField.FIELD_VARIABLE_NAME);
			String typeList = response.get(IWVMessageField.FIELD_VARIABLE_TYPE);
			String globalList = response.get(IWVMessageField.FIELD_VARIABLE_GLOBAL);
			String regList = response.get(IWVMessageField.FIELD_VARIABLE_REGISTERED);

			String[] names = nameList.split(IMessageField.VARIABLE_SEPARATOR);
			String[] types = typeList.split(IMessageField.VARIABLE_SEPARATOR);
			String[] globals = globalList.split(IMessageField.VARIABLE_SEPARATOR);
			String[] registereds = regList.split(IMessageField.VARIABLE_SEPARATOR);

			VariableData var = null;
			for (int index = 0; index < names.length; index++)
			{
				boolean global = globals[index].equals("True");
				boolean registered = registereds[index].equals("True");
				switch (whichOnes)
				{
				case AVAILABLE_ALL:
					var = new VariableData(names[index], types[index], null, global, registered);
					result.add(var);
					break;
				case AVAILABLE_GLOBALS:
					if (!global)
						continue;
					var = new VariableData(names[index], types[index], null, global, registered);
					result.add(var);
					break;
				case AVAILABLE_LOCALS:
					if (global)
						continue;
					var = new VariableData(names[index], types[index], null, global, registered);
					result.add(var);
					break;
				case REGISTERED_ALL:
					if (!registered)
						continue;
					var = new VariableData(names[index], types[index], null, global, registered);
					result.add(var);
					break;
				case REGISTERED_GLOBALS:
					if ((!registered) || (!global))
						continue;
					var = new VariableData(names[index], types[index], null, global, registered);
					result.add(var);
					break;
				case REGISTERED_LOCALS:
					if ((!registered) || (global))
						continue;
					var = new VariableData(names[index], types[index], null, global, registered);
					result.add(var);
					break;
				}
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		if (result.size() == 0)
			return null;
		return result.toArray(new VariableData[0]);
	}
}
