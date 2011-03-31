///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : SPELLmessageProcInfo.java
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

import com.astra.ses.spell.gui.core.model.types.IProcProperties;



public class SPELLmessageProcInfo extends SPELLmessageRequest
{
	private static String[] PROPERTY_NAMES = 
	{ 
		IProcProperties.PROC_NAME,
		IProcProperties.PROC_DESC,
		IProcProperties.PROC_SC,
		IProcProperties.PROC_AUTH,
		IProcProperties.PROC_DATE,
		IProcProperties.PROC_DTBS,
		IProcProperties.PROC_CATG,
		IProcProperties.PROC_VALID,
		IProcProperties.PROC_APPR,
		IProcProperties.PROC_LOAD
	};

	public SPELLmessageProcInfo( String procId )
	{
		super(IMessageId.REQ_PROC_PROP);
		set(IMessageField.FIELD_PROC_ID, procId);
		setSender(IMessageValue.CLIENT_SENDER);
		setReceiver(IMessageValue.CONTEXT_RECEIVER);
	}
	
	public TreeMap<String,String> getProcProperties( SPELLmessage resp )
	{
		TreeMap<String,String> properties = new TreeMap<String, String>();
		try
		{
			// Build the property map
			for (String name : PROPERTY_NAMES)
			{
				properties.put(name, resp.get(name));
			}
		}
		catch(MessageException ex)
		{
			ex.printStackTrace();
		}
		return properties;
	}
}
