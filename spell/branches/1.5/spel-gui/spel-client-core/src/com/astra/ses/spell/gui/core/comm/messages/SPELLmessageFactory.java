///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageFactory.java
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
package com.astra.ses.spell.gui.core.comm.messages;

import java.util.TreeMap;

public class SPELLmessageFactory
{
	public static SPELLmessage createMessage( String data )
	{
		SPELLmessage msg = null;
		try
		{
			TreeMap<String,String> tags = SPELLmessage.getTags(data);
			String msgTypeStr = tags.get("root");
			if (msgTypeStr.equals(IMessageType.MSG_TYPE_WRITE))
			{
				msg = new SPELLmessageWrite(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_COMMAND))
			{
				if (tags.get(IMessageField.FIELD_ID).equals(IMessageId.MSG_EXEC_OP))
				{
					msg = new SPELLmessageExecOperation(tags);
				}
				else if (tags.get(IMessageField.FIELD_ID).equals(IMessageId.MSG_CLIENT_OP))
				{
					msg = new SPELLmessageClientOperation(tags);
				}
				else if (tags.get(IMessageField.FIELD_ID).equals(IMessageId.MSG_CONTEXT_OP))
				{
					msg = new SPELLmessageCtxOperation(tags);
				}
				// This is the prompt sent in form of oneway message, for monitoring GUIs
				else if (tags.get(IMessageField.FIELD_ID).equals(IMessageId.MSG_PROMPT_START))
				{
					msg = new SPELLmessagePromptStart(tags);
				}
				// This is the prompt sent in form of oneway message, for monitoring GUIs
				else if (tags.get(IMessageField.FIELD_ID).equals(IMessageId.MSG_PROMPT_END))
				{
					msg = new SPELLmessagePromptEnd(tags);
				}
				else
				{
					msg = new SPELLmessageCommand(tags);
				}
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_EOC))
			{
				msg = new SPELLmessageEOC();
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_ERROR))
			{
				msg = new SPELLmessageError(tags);
				if (msg.getId().equals(IMessageId.MSG_LISTENER_LOST))
				{
					msg = new SPELLlistenerLost(tags);
				}
				else if (msg.getId().equals(IMessageId.MSG_CONTEXT_LOST))
				{
					msg = new SPELLcontextLost(tags);
				}
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_NOTIFY))
			{
				msg = new SPELLmessageNotify(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_PROMPT))
			{
				msg = new SPELLmessagePrompt(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_REQUEST))
			{
				msg = new SPELLmessageRequest(tags);
			}
			else if (msgTypeStr.equals(IMessageType.MSG_TYPE_RESPONSE))
			{
				msg = new SPELLmessageResponse(tags);
			}
			else
			{
				System.err.println("Warning: unprocessed message type: " + msgTypeStr);
				msg = new SPELLmessage(tags);
			}
		}
		catch (MessageException e)
		{
			e.printStackTrace();
		}
		return msg;
	}
}
