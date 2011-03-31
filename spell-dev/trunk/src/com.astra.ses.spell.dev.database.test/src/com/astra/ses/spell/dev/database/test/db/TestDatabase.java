///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.driver.test.db
// 
// FILE      : TestDatabase.java
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
package com.astra.ses.spell.dev.database.test.db;

import java.util.Collection;

import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.interfaces.ITelecommand;
import com.astra.ses.spell.dev.database.interfaces.ITelemetryDisplayDefinition;
import com.astra.ses.spell.dev.database.interfaces.ITelemetryParameter;
import com.astra.ses.spell.dev.database.test.db.tc.TelecommandProvider;
import com.astra.ses.spell.dev.database.test.db.tm.TelemetryProvider;

/*******************************************************************************
 * 
 * TestDatabase is an ISpellDatabase implementation using fake TM parameter and
 * Commands
 * 
 ******************************************************************************/
public class TestDatabase implements ISpellDatabase
{
	/** Database name */
	private static final String	DB_NAME	   = "Test Database";
	/** Database version */
	private static final String	DB_VERSION	= "Test version";
	/** Database path */
	private static final String	DB_PATH	   = "";

	/** Telemetry manager */
	private TelemetryProvider	m_tmMgr;
	/** Telecommand manager */
	private TelecommandProvider	m_tcMgr;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public TestDatabase()
	{
		m_tmMgr = new TelemetryProvider();
		m_tcMgr = new TelecommandProvider();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ITelecommand getTelecommandModel(String commandName)
	{
		return m_tcMgr.getTelecommand(commandName);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Collection<String> getTelecommandNames()
	{
		return m_tcMgr.getTelecommands();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getDatabasePath()
	{
		return DB_PATH;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Collection<? extends ITelemetryDisplayDefinition> getDisplays()
	{
		// TODO this feature is not still supported
		return null;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Collection<String> getTelemetryParameterNames()
	{
		return m_tmMgr.getTelemetryParameters();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getName()
	{
		return DB_NAME;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ITelemetryParameter getTelemetryModel(String parameterName)
	{
		return m_tmMgr.getParameter(parameterName);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getVersion()
	{
		return DB_VERSION;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isTelemetryParameter(String mnemonic)
	{
		return m_tmMgr.isTelemetry(mnemonic);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isTelecommand(String mnemonic)
	{
		return m_tcMgr.isTelecommand(mnemonic);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isSequence(String mnemonic)
	{
		return false;
	}

}
