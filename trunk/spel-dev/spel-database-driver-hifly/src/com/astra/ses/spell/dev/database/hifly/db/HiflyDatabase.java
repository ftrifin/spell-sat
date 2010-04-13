///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.db
// 
// FILE      : HiflyDatabase.java
//
// DATE      : 2009-09-14
//
// COPYRIGHT (c) 2008, 2010 SES-ENGINEERING
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//    SES-ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL Development Environment
//
// AUTHOR    : J. Andres Pizarro (jpizar) - japg@gmv.es 
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database.hifly.db;

import java.util.Collection;

import com.astra.ses.spell.dev.database.hifly.db.commanding.HiflyCommandManager;
import com.astra.ses.spell.dev.database.hifly.db.telemetry.HiflyParameterManager;
import com.astra.ses.spell.dev.database.hifly.parsers.HiflyVDFReader;
import com.astra.ses.spell.dev.database.impl.commanding.Command;
import com.astra.ses.spell.dev.database.impl.telemetry.TelemetryParameter;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.interfaces.display.ITelemetryDisplayDefinition;

/******************************************************************************
 * hifly database model used by spell
 * @author jpizar
 *
 *****************************************************************************/
public class HiflyDatabase implements ISpellDatabase {

	/** Root directory */
	private String m_databaseRootDir;
	/** Database name */
	private String m_databaseName;
	/** Database version */
	private String m_databaseVersion;
	/** Parameter definition reader */
	private HiflyParameterManager m_parameterManager;
	/** Telecommand definition reader */
	private HiflyCommandManager m_commandManager;
	
	/**************************************************************************
	 * Root directory
	 * @param rootDir
	 *************************************************************************/
	public HiflyDatabase (String rootDir)
	{
		m_databaseRootDir = rootDir;
		initDatabase();
	}
	
	/**************************************************************************
	 * Init database elements
	 *************************************************************************/
	private void initDatabase()
	{
		HiflyVDFReader vdfReader = new HiflyVDFReader(m_databaseRootDir);
		m_databaseName = vdfReader.getVDF_NAME();
		m_databaseVersion = vdfReader.getVDF_COMMENT();
		m_parameterManager = new HiflyParameterManager(m_databaseRootDir);
		m_commandManager = new HiflyCommandManager(m_databaseRootDir);
	}
	
	/**************************************************************************
	 * Database will no longer be used so we can free our resources
	 *************************************************************************/
	public void dispose()
	{
		m_parameterManager = null;
		m_commandManager = null;
		//TODO future resources might be disposed too
	}
	
	@Override
	public Collection<ITelemetryDisplayDefinition> getDisplays() {
		//TODO This feature is not still implemented by hifly driver
		return null;
	}
	
	@Override
	public String getDatabasePath() {
		return m_databaseRootDir;
	}

	@Override
	public String getName() {
		return m_databaseName;
	}

	@Override
	public Collection<String> getCommandingElements() {
		return m_commandManager.getCommands();
	}

	@Override
	public Collection<String> getMonitoringParameters() {
		return m_parameterManager.getTelemetryParameters();
	}

	@Override
	public String getVersion() {
		return m_databaseVersion;
	}

	@Override
	public Command getCommand(String commandName) {
		return m_commandManager.getCommand(commandName);
	}

	@Override
	public TelemetryParameter getParameter(String parameterName) {
		return m_parameterManager.getParameter(parameterName);
	}
}
