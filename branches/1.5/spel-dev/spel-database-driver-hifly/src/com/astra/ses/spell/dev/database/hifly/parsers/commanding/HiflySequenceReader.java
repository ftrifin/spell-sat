///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.db.commanding
// 
// FILE      : HiflySequenceReader.java
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
package com.astra.ses.spell.dev.database.hifly.parsers.commanding;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.dev.database.hifly.parsers.HiflyDatabaseFileReader;
import com.astra.ses.spell.dev.database.impl.commanding.Command;
import com.astra.ses.spell.dev.database.impl.commanding.CommandSequence;
import com.astra.ses.spell.dev.database.impl.commanding.args.ArgumentDefinition;
import com.astra.ses.spell.dev.database.impl.commanding.args.ArgumentType;
import com.astra.ses.spell.dev.database.impl.commanding.args.CommandArgument;

/******************************************************************************
 *
 * HiflySequenceReader will manage command sequences 
 * @author jpizar
 *
 *****************************************************************************/
public class HiflySequenceReader {

	/***************************************************************************
	 * 
	 * HiflyCSFReader will parse the Command Sequence Characteristics file (CSF)
	 * @author jpizar
	 *
	 **************************************************************************/
	private class HiflyCSFReader extends HiflyDatabaseFileReader
	{

		/** Filename */
		private static final String FILENAME="csf.dat";
		
		/***********************************************************************
		 * Constructor
		 * @param rootDir
		 **********************************************************************/
		public HiflyCSFReader(String rootDir) {
			super(rootDir + File.separator + FILENAME);
		}
	}
	
	/***************************************************************************
	 * 
	 * HiflyCSFReader will parse the Command Sequence Characteristics file (CSF)
	 * @author jpizar
	 *
	 **************************************************************************/
	private class HiflyCSSReader extends HiflyDatabaseFileReader
	{

		/** Filename */
		private static final String FILENAME="css.dat";
		
		/***********************************************************************
		 * Constructor
		 * @param rootDir
		 **********************************************************************/
		public HiflyCSSReader(String rootDir) {
			super(rootDir + File.separator + FILENAME);
		}
	}
	
	/***************************************************************************
	 * 
	 * HiflyCSFReader will parse the Command Sequence Characteristics file (CSF)
	 * @author jpizar
	 *
	 **************************************************************************/
	private class HiflyCSPReader extends HiflyDatabaseFileReader
	{

		/** Filename */
		private static final String FILENAME="csp.dat";
		
		/***********************************************************************
		 * Constructor
		 * @param rootDir
		 **********************************************************************/
		public HiflyCSPReader(String rootDir) {
			super(rootDir + File.separator + FILENAME);
		}
	}
	
	/** Collection of available telecommands */
	private Map<String, CommandSequence> m_sequences;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public HiflySequenceReader(String rootDir, Map<String, Command> availableCommands)
	{
		m_sequences = new HashMap<String, CommandSequence>();
		try {
			init(rootDir, availableCommands);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***************************************************************************
	 * Start parsing the related files
	 * @throws Exception 
	 **************************************************************************/
	private void init(String rootDir, Map<String, Command> availableCommands) throws Exception
	{
		// Sequences Characteristics (CSF)
		Map<String, CommandSequence> sequencesMap = new HashMap<String, CommandSequence>();
		HiflyCSFReader csfReader = new HiflyCSFReader(rootDir);
		String[] lineFields = csfReader.getLineFields();
		while (lineFields != null)
		{
			boolean critical = Boolean.valueOf(lineFields[6]);
			//TODO When Sequence elements are parsed, array shall be bigger then 0
			Command[] seqElements = new Command[0];
			//Command[] seqElements = new Command[Integer.valueOf(lineFields[5])];
			CommandArgument[] formalParameters = new CommandArgument[Integer.valueOf(lineFields[4])]; 
			CommandSequence seq = 
				new CommandSequence(lineFields[0], lineFields[1], critical,
						formalParameters, seqElements);
			sequencesMap.put(lineFields[0], seq);
			lineFields = csfReader.getLineFields();
		}
		
		// Formal parameters
		HiflyCSPReader cspReader = new HiflyCSPReader(rootDir);
		lineFields = cspReader.getLineFields();
		while (lineFields != null)
		{
			String seqName = lineFields[0];
			int order = Integer.valueOf(lineFields[2]);
			ArgumentType type = ArgumentType.getTypeFromChar(lineFields[6].charAt(0));
			ArgumentDefinition argDef = new ArgumentDefinition(lineFields[1], lineFields[3], type);
			CommandArgument fp = new CommandArgument(argDef, lineFields[10]);
			sequencesMap.get(seqName).getArguments()[order - 1] = fp;
			lineFields = cspReader.getLineFields();
		}
		m_sequences.putAll(sequencesMap);
		
		// TODO : process formal parameters
		// Sequences Definition (CSS)
		/*HiflyCSSReader cssReader = new HiflyCSSReader(rootDir);
		lineFields = cssReader.getLineFields();
		while (lineFields != null)
		{
			String seqName = lineFields[0];
			char elementType = lineFields[3].charAt(0);
			String id = lineFields[4];
			Command element = null;
			switch (elementType)
			{
				case 'C': // COMMAND
					element = availableCommands.get(id);
					break;
				case 'S': // SEQUENCE
					element = sequencesMap.get(id);
					break;
				case 'F': // Fall through
				case 'P': // FORMAL PARAMETER
					element = sequencesMap.get(seqName).getFormalParameter(id);
					break;
				default : throw new Exception("Unknown sequence element");
			}
			int order = Integer.valueOf(lineFields[2]);
			sequencesMap.get(seqName).getSequenceCommands()[order - 1] = element;
			lineFields = cssReader.getLineFields();
		}*/
		
		//HiflySDFReader sdfReader = new HiflySDFReader(rootDir);	
	}
	
	/***************************************************************************
	 * Retrieve command sequences
	 * @return
	 **************************************************************************/
	public Map<String, ? extends Command> getCommandSequences()
	{
		return m_sequences;
	}
}
