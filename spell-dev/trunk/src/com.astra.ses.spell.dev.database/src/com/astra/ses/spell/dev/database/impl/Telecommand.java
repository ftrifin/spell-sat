///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.impl
//
// FILE      : Telecommand.java
//
// DATE      : Feb 21, 2011
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
package com.astra.ses.spell.dev.database.impl;

import java.util.List;

import com.astra.ses.spell.dev.database.interfaces.ITelecommandArgument;
import com.astra.ses.spell.dev.database.interfaces.ITelecommand;

/******************************************************************************
 * 
 * Representation of a telecommand model
 * 
 *****************************************************************************/
public class Telecommand implements ITelecommand
{
	/** Telecommand name */
	private String	                   m_name;

	/** Telecommand description */
	private String	                   m_description;

	/** Critical */
	private boolean	                   m_critical;

	/** Command parameters */
	private List<ITelecommandArgument>	m_commandArguments;

	/**************************************************************************
	 * Constructor
	 * 
	 * @param name
	 *            command's name
	 * @param description
	 *            command's description
	 * @param critical
	 *            flag determining if this command is critical or not
	 * @param arguments
	 *            a list of {@link TelecommandArgument} used by this Command
	 *************************************************************************/
	public Telecommand(String name, String description, boolean critical,
	        List<ITelecommandArgument> arguments)
	{
		m_name = name;
		m_description = description;
		m_critical = critical;
		m_commandArguments = arguments;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getDescription()
	{
		return m_description;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getName()
	{
		return m_name;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isCritical()
	{
		return m_critical;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public List<ITelecommandArgument> getArguments()
	{
		return m_commandArguments;
	}

}
