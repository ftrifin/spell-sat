///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageExecInfo.java
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

import java.util.Vector;

import com.astra.ses.spell.gui.core.model.server.FileTransferData;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;



public class SPELLmessageServerFile extends SPELLmessageRequest
{
	public SPELLmessageServerFile( String procId, ServerFileType serverFileId )
	{
		super(IMessageId.REQ_SERVER_FILE);
		set(IMessageField.FIELD_PROC_ID, procId);
		set(IMessageField.FIELD_SERVER_FILE_ID, serverFileId.toString());
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
	}

	public SPELLmessageServerFile( String procId, ServerFileType serverFileId, int chunkNo )
	{
		super(IMessageId.REQ_SERVER_FILE);
		set(IMessageField.FIELD_PROC_ID, procId);
		set(IMessageField.FIELD_SERVER_FILE_ID, serverFileId.toString());
		set(IMessageField.FIELD_CHUNK, Integer.toString(chunkNo));
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
	}

	public static FileTransferData getDataFrom( SPELLmessage msg )
	{
		FileTransferData data = null;
		try
		{
			String code = msg.get(IMessageField.FIELD_SERVER_FILE);
			String[] lines = code.split("\n");
			Vector<String> codeLines = new Vector<String>();
			for(String line : lines) codeLines.addElement(line);
			
			int chunk = 0;
			int totalChunks = 1;
			if (msg.hasKey(IMessageField.FIELD_CHUNK))
			{
				chunk = Integer.parseInt(msg.get(IMessageField.FIELD_CHUNK));
				totalChunks = Integer.parseInt(msg.get(IMessageField.FIELD_TOTAL_CHUNKS));
			}
			data = new FileTransferData( codeLines, chunk, totalChunks );
		}
		catch(MessageException ex)
		{
			ex.printStackTrace();
		}
		return data;
	}
}
