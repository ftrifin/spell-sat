///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageGetVariables.java
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

import java.util.ArrayList;

import com.astra.ses.spell.gui.core.model.notification.VariableData;
import com.astra.ses.spell.gui.core.model.notification.WhichVariables;

public class SPELLmessageGetVariables extends SPELLmessageRequest
{
	public SPELLmessageGetVariables(String procId, WhichVariables whichOnes)
	{
		super(IMessageId.REQ_VARIABLE_NAMES);
		set(IMessageField.FIELD_PROC_ID, procId);
		set(IMessageField.FIELD_VARIABLE_GET, whichOnes.name());
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(procId);
	}

	public static VariableData[] getVariables(WhichVariables whichOnes,
	        SPELLmessage response)
	{
		ArrayList<VariableData> result = new ArrayList<VariableData>();
		try
		{
			String[] names = response.get(IMessageField.FIELD_VARIABLE_NAME)
			        .split(",,");
			String[] values = response.get(IMessageField.FIELD_VARIABLE_VALUE)
			        .split(",,");
			String[] types = response.get(IMessageField.FIELD_VARIABLE_TYPE)
			        .split(",,");
			String[] globals = response
			        .get(IMessageField.FIELD_VARIABLE_GLOBAL).split(",,");
			String[] registereds = response.get(
			        IMessageField.FIELD_VARIABLE_REGISTERED).split(",,");

			VariableData var = null;
			for (int index = 0; index < names.length; index++)
			{
				boolean global = globals[index].equals("True");
				boolean registered = registereds[index].equals("True");
				switch (whichOnes)
				{
				case AVAILABLE_ALL:
					var = new VariableData(names[index], types[index],
					        values[index], global, registered);
					result.add(var);
					break;
				case AVAILABLE_GLOBALS:
					if (!global) continue;
					var = new VariableData(names[index], types[index],
					        values[index], global, registered);
					result.add(var);
					break;
				case AVAILABLE_LOCALS:
					if (global) continue;
					var = new VariableData(names[index], types[index],
					        values[index], global, registered);
					result.add(var);
					break;
				case REGISTERED_ALL:
					if (!registered) continue;
					var = new VariableData(names[index], types[index],
					        values[index], global, registered);
					result.add(var);
					break;
				case REGISTERED_GLOBALS:
					if ((!registered) || (!global)) continue;
					var = new VariableData(names[index], types[index],
					        values[index], global, registered);
					result.add(var);
					break;
				case REGISTERED_LOCALS:
					if ((!registered) || (global)) continue;
					var = new VariableData(names[index], types[index],
					        values[index], global, registered);
					result.add(var);
					break;
				}
			}
		}
		catch (MessageException ex)
		{
			ex.printStackTrace();
		}
		if (result.size() == 0) return null;
		return result.toArray(new VariableData[0]);
	}
}
