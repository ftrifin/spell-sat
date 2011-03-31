///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.db.commanding
// 
// FILE      : HiflyCommandManager.java
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
package com.astra.ses.spell.dev.database.hifly.db.commanding;

import java.util.Collection;
import java.util.Map;

import com.astra.ses.spell.dev.database.hifly.parsers.commanding.HiflyCommandReader;
import com.astra.ses.spell.dev.database.hifly.parsers.commanding.HiflySequenceReader;
import com.astra.ses.spell.dev.database.impl.commanding.Command;

/*******************************************************************************
 *
 * HiflyCommandManager will provide the command elements defined inside hifly
 * database
 * @author jpizar
 *
 ******************************************************************************/
public class HiflyCommandManager {

	/** Collection of available telecommands */
	private Map<String, Command> m_commands;
	
	/***************************************************************************
	 * Constructor
	 * @param databaseRootDir
	 **************************************************************************/
	public HiflyCommandManager(String databaseRootDir)
	{
		init(databaseRootDir);
	}

	/***************************************************************************
	 * Start parsing files
	 * @param rootDir
	 **************************************************************************/
	private void init(String rootDir)
	{
		// Simple Commands
		HiflyCommandReader commandReader = new HiflyCommandReader(rootDir);
		m_commands = commandReader.getCommands();
		// Sequences
		HiflySequenceReader sequenceReader = new HiflySequenceReader(rootDir, m_commands);
		m_commands.putAll(sequenceReader.getCommandSequences());
	}
	
	/***************************************************************************
	 * Get Commands defined inside the database
	 * @return
	 **************************************************************************/
	public Collection<String> getCommands()
	{
		return m_commands.keySet();
	}
	
	/***************************************************************************
	 * Retrieve command information
	 * @param name
	 * @return
	 **************************************************************************/
	public Command getCommand(String name)
	{
		return m_commands.get(name);
	}
}
