///////////////////////////////////////////////////////////////////////////////
//
// (C) SES Engineering 2008
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.commands
// 
// FILE      : ExecutorCommand.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.core.comm.commands;

import java.util.Vector;

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.ServiceManager;


public abstract class ExecutorCommand implements Command
{
	private static ContextProxy s_proxy = null;
	
	protected String m_msgId;
	protected String m_cmdString;
	protected Vector<String> m_args;
	protected String m_icon;
	protected String m_help;
	protected String m_procId;
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutorCommand( String msgId, String cmdString, String iconPath, String help )
	{
		if (s_proxy == null)
		{
			s_proxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		}
		m_msgId = msgId;
		m_cmdString = cmdString;
		m_args = null;
		m_icon = iconPath;
		m_help = help;
		m_procId = null;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getMsgId()
	{
		return m_msgId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void resetInfo()
	{
		m_args = null;
		m_procId = null;
	}

	/* (non-Javadoc)
	 * @see com.astra.spellrun.model.commands.Command#getCmdString()
	 */
	@Override
	public String getCmdString()
	{
		return m_cmdString;
	}
	
	/* (non-Javadoc)
	 * @see com.astra.spellrun.model.commands.Command#getArgs()
	 */
	@Override
	public Vector<String> getArgs()
	{
		return m_args;
	}

	/***************************************************************************
	 * Provide the command icon path
	 * @return
	 **************************************************************************/
	public String getIconPath()
	{
		return m_icon;
	}
	
	/* (non-Javadoc)
	 * @see com.astra.spellrun.model.commands.Command#getHelp()
	 */
	@Override
	public String getHelp()
	{
		return m_help;
	}
	
	/* (non-Javadoc)
	 * @see com.astra.spellrun.model.commands.Command#setArgs(java.lang.String[])
	 */
	@Override
	public void setArgs( Vector<String> args )
	{
		m_args = args;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setProcId( String procId )
	{
		m_procId = procId;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}
	
	/***************************************************************************
	 * Determine if a executor command is valid for the current ExecutorStatus
	 **************************************************************************/
	public abstract boolean validate( ExecutorStatus st );
	
	/* (non-Javadoc)
	 * @see com.astra.spellrun.model.commands.Command#execute()
	 */
	@Override
	public void execute( Vector<String> args ) throws CommandFailed
	{
		if (m_procId == null)
		{
			throw new CommandFailed("Cannot execute command, no procedure ID defined");
		}
		try
		{
			s_proxy.command(this);
		}
		catch(Exception ex)
		{
			throw new CommandFailed("Cannot execute command: " + ex.getLocalizedMessage());
		}
	}
}
