///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.messages
// 
// FILE      : SPELLmessageVariableScopeChange.java
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

import com.astra.ses.spell.gui.core.comm.messages.MessageException;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessage;
import com.astra.ses.spell.gui.core.comm.messages.SPELLmessageOneway;
import com.astra.ses.spell.gui.core.interfaces.IMessageField;
import com.astra.ses.spell.gui.watchvariables.notification.ScopeNotification;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;

/*******************************************************************************
 * 
 * Dismiss Variable scope change message
 * 
 ******************************************************************************/
public class SPELLmessageVariableScopeChange extends SPELLmessageOneway
{

	/** Procedure Id */
	private String	m_procId;
	/** Holds the variable names */
	private String	m_nameStr;
	/** Holds the global variable types */
	private String	m_typeStr;
	/** Holds the global variable values */
	private String	m_valueStr;
	/** Holds the local variable names */
	private String	m_globalStr;

	/***************************************************************************
	 * Tag based constructor
	 * 
	 * @param data
	 **************************************************************************/
	public SPELLmessageVariableScopeChange( SPELLmessage msg )
	{
		super(msg);
		setId(IWVMessageId.MSG_VARIABLE_CHANGE);
		try
		{
			m_procId = get(IMessageField.FIELD_PROC_ID);
			m_nameStr = get(IWVMessageField.FIELD_VARIABLE_NAME);
			m_typeStr = get(IWVMessageField.FIELD_VARIABLE_TYPE);
			m_valueStr = get(IWVMessageField.FIELD_VARIABLE_VALUE);
			m_globalStr = get(IWVMessageField.FIELD_VARIABLE_GLOBAL);
		}
		catch (MessageException e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * Get the user action to be performed on demand
	 * 
	 * @return
	 **************************************************************************/
	public ScopeNotification getData()
	{
		ScopeNotification data = new ScopeNotification(m_procId);

		String[] names = m_nameStr.split(IMessageField.VARIABLE_SEPARATOR);
		String[] types = m_typeStr.split(IMessageField.VARIABLE_SEPARATOR);
		String[] values = m_valueStr.split(IMessageField.VARIABLE_SEPARATOR);
		String[] globals = m_globalStr.split(IMessageField.VARIABLE_SEPARATOR);

		for (int index = 0; index < names.length; index++)
		{
			boolean global = globals[index].equals("True");
			VariableData vdata = new VariableData(names[index], types[index],
			        values[index], global, true // All variables in scope change
												// notif are registered
			);
			if (global)
			{
				data.addGlobalVariable(vdata);
			}
			else
			{
				data.addLocalVariable(vdata);
			}
		}
		return data;
	}
}
