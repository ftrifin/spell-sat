///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.driver.test.tc
// 
// FILE      : TelecommandProvider.java
//
// DATE      : 2010-05-19 15:40
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database.test.db.tc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import com.astra.ses.spell.dev.database.interfaces.ArgumentDefinition;
import com.astra.ses.spell.dev.database.interfaces.CommandFactory;
import com.astra.ses.spell.dev.database.interfaces.ITelecommand;
import com.astra.ses.spell.dev.database.interfaces.ITelecommandArgument;

/*******************************************************************************
 * 
 * Telecommand provider deals with fake Telecommands
 * 
 ******************************************************************************/
public class TelecommandProvider
{

	/***************************************************************************
	 * 
	 * Fake telecommand definition
	 * 
	 **************************************************************************/
	private enum FakeCommand
	{
		TC1("TC1", "Test command 1", false, 0), TC2("TC2", "Test command 2",
		        false, 1), TC3("TC3", "Test command 3", false, 2), TC4("TC4",
		        "Test command 4", false, 3);

		private String	m_name;
		private String	m_description;
		private boolean	m_critical;
		private int		m_arguments;

		/***********************************************************************
		 * 
		 * @param name
		 * @param desc
		 * @param critical
		 * @param args
		 **********************************************************************/
		private FakeCommand(String name, String desc, boolean critical, int args)
		{
			m_name = name;
			m_description = desc;
			m_critical = critical;
			m_arguments = args;
		}

		/***********************************************************************
		 * Return a command representation for this enum
		 * 
		 * @return
		 **********************************************************************/
		private ITelecommand getCommand()
		{
			// Prepare command arguments
			List<ITelecommandArgument> args = new ArrayList<ITelecommandArgument>();
			for (int i = 0; i < m_arguments; i++)
			{
				ArgumentDefinition def = new ArgumentDefinition(m_name
				        + "_ARG_" + i);
				ITelecommandArgument arg = CommandFactory.createArgument(def,
				        "");
				args.add(arg);
			}
			return CommandFactory.createCommand(m_name, m_description,
			        m_critical, args);
		}
	}

	/***************************************************************************
	 * Default constructor
	 **************************************************************************/
	public TelecommandProvider()
	{
	}

	/***************************************************************************
	 * Return available commands in thi database
	 * 
	 * @return
	 **************************************************************************/
	public Collection<String> getTelecommands()
	{
		Vector<String> names = new Vector<String>();
		for (FakeCommand tc : FakeCommand.values())
		{
			names.add(tc.m_name);
		}
		return names;
	}

	/***************************************************************************
	 * Returm command definition for this command's name
	 * 
	 * @param cmdName
	 * @return
	 **************************************************************************/
	public ITelecommand getTelecommand(String cmdName)
	{
		return FakeCommand.valueOf(cmdName).getCommand();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean isTelecommand(String mnemonic)
	{
		try
		{
			FakeCommand.valueOf(mnemonic);
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

}
