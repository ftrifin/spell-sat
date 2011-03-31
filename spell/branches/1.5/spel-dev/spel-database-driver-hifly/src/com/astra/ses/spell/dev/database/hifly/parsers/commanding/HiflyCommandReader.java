///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.parsers.commanding
// 
// FILE      : HiflyCommandReader.java
//
// DATE      : 2009-09-15
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
package com.astra.ses.spell.dev.database.hifly.parsers.commanding;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.dev.database.hifly.parsers.HiflyDatabaseFileReader;
import com.astra.ses.spell.dev.database.impl.commanding.Command;
import com.astra.ses.spell.dev.database.impl.commanding.args.ArgumentDefinition;
import com.astra.ses.spell.dev.database.impl.commanding.args.ArgumentType;
import com.astra.ses.spell.dev.database.impl.commanding.args.CommandArgument;

/******************************************************************************
 *
 * HiflyCommandReader class will read single command and single command 
 * parameters files
 * @author jpizar
 *
 *****************************************************************************/
public class HiflyCommandReader {

	/**************************************************************************
	 * 
	 * HiflyCCF Reader will parse the Command Characteristics File (CCF)
	 * @author jpizar
	 *
	 *************************************************************************/
	private class HiflyCCFReader extends HiflyDatabaseFileReader
	{

		/** File name */
		private static final String FILENAME = "ccf.dat";

		/**************************************************************************
		 * Constructor
		 * @param fileToParse
		 *************************************************************************/
		public HiflyCCFReader(String rootPath) {
			super(rootPath + File.separator + FILENAME);
		}
	}
	
	/***************************************************************************
	 *
	 * HiflyCPCReader parses the Command parameters file CPC
	 * @author jpizar
	 *
	 **************************************************************************/
	private class HiflyCPCReader extends HiflyDatabaseFileReader
	{
		/** File name */
		private static final String FILENAME = "cpc.dat";

		public HiflyCPCReader(String rootDir) {
			super(rootDir + File.separator + FILENAME);
		}
	}
	
	/***************************************************************************
	 * 
	 * @author jpizar
	 *
	 **************************************************************************/
	private class HiflyCDFReader extends HiflyDatabaseFileReader
	{
		/** File name */
		private static final String FILENAME = "cdf.dat";

		public HiflyCDFReader(String rootDir) {
			super(rootDir + File.separator + FILENAME);
		}
	}
	
	/** Commands collection */
	private Map<String, Command> m_commands;
	
	/**************************************************************************
	 * Constructor
	 * @param databaseRootDir
	 *************************************************************************/
	public HiflyCommandReader(String databaseRootDir)
	{
		m_commands = new HashMap<String, Command>();
		init(databaseRootDir);
	}
	
	/**************************************************************************
	 * Parse the related files and construct the command elements
	 * @param rootDir
	 *************************************************************************/
	private void init(String rootDir)
	{
		// Command arguments
		HiflyCPCReader cpcReader = new HiflyCPCReader(rootDir);
		Map<String, ArgumentDefinition> argumentsMap = new HashMap<String, ArgumentDefinition>();
		String[] lineFields = cpcReader.getLineFields();
		while(lineFields != null)
		{
			ArgumentType type = ArgumentType.getTypeFromChar(lineFields[4].charAt(0));
			ArgumentDefinition arg = new ArgumentDefinition(lineFields[0], lineFields[1], type);
			argumentsMap.put(lineFields[0], arg);
			lineFields = cpcReader.getLineFields();
		}
		
		// Command - Parameter relationship
		HiflyCDFReader cdfReader = new HiflyCDFReader(rootDir);
		Map<String, ArrayList<CommandArgument>> commandParamMap = new HashMap<String, ArrayList<CommandArgument>>();
		lineFields = cdfReader.getLineFields();
		while(lineFields != null)
		{
			String command = lineFields[0];
			if (!commandParamMap.containsKey(command))
			{
				commandParamMap.put(command, new ArrayList<CommandArgument>());
			}
			ArgumentDefinition def = argumentsMap.get(lineFields[6]);
			if (argumentsMap.get(lineFields[6]) == null)
			{
				def = new ArgumentDefinition(lineFields[6]);
			}
			CommandArgument arg = new CommandArgument(def, lineFields[9]);
			commandParamMap.get(command).add(arg);
			lineFields = cdfReader.getLineFields();
		}
		
		// Commands
		HiflyCCFReader ccfReader = new HiflyCCFReader(rootDir);
		lineFields = ccfReader.getLineFields();
		while (lineFields != null) 
		{
			String cmdName = lineFields[0];
			ArrayList<CommandArgument> args = commandParamMap.get(cmdName);
			Boolean critical = Boolean.valueOf(lineFields[4]);
			CommandArgument[] arguments = null;
			if (args != null)
			{
				arguments = new CommandArgument[args.size()];
				args.toArray(arguments);
			}
			Command parameter = new Command(lineFields[0], lineFields[1], critical, arguments);
			m_commands.put(parameter.getName(), parameter);
			lineFields = ccfReader.getLineFields();
		}
	}
	
	
	/**************************************************************************
	 * Get telecommands collection
	 * @return
	 *************************************************************************/
	public Map<String, Command> getCommands()
	{
		return m_commands;
	}
}
