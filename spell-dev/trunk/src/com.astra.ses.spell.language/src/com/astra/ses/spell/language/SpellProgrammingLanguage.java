///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language
// 
// FILE      : SpellProgrammingLanguage.java
//
// DATE      : 2009-11-23
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

/*****************************************************************************
 * Class for providing spell language special constants, functions and symbols
 * It tries to add new symbols to Python programming language
 *****************************************************************************/
public class SpellProgrammingLanguage 
{
	private static SpellProgrammingLanguage s_instance;
	/** Holds the list of available SPELL constants */
	private ArrayList<String> m_constants;
	/** Holds the list of available SPELL functions */
	private ArrayList<String> m_functions;
	/** Holds the list of available SPELL modifiers */
	private ArrayList<String> m_modifiers;
	
	/** Holds the default list of SPELL functions, applied when the plugin cannot obtain them */
	private static String[] s_defaultFunctions =
	{
		"GetTM", "Display", "Verify", "SendAndVerify",
		"SendAndVerifyAdjLim",
		"Send","SetGroundParameter","Prompt","Abort",
		"Event","GetResource", "SetResource",
		"UserLogin", "UserLogout", "CheckUser",
		"StartTask", "StopTask", "CheckTask",
		"SetExecDelay", "StartProc","LoadDictionary",
		"WaitFor","AdjustLimits","EnableAlarms",
		"DisableAlarms","SetTMparam","GetTMparam",
		"Script","OpenDisplay","CloseDisplay",
		"PrintDisplay", "Step", "Goto"
	};
	/** Holds the default list of SPELL modifiers, applied when the plugin cannot obtain them */
	private static String[] s_defaultModifiers =
	{
		"ValueFormat", "OnFailure", "Wait",
		"Timeout", "Delay", "TryAll", "Time",
		"Retries", "Host", "Tolerance",
		"Delay","Type","Severity","Scope",
		"OnTrue","OnFalse","PromptUser","PromptFailure",
		"Retry","GiveChoice","HandleError",
		"ValueType","Radix","Units","Strict",
		"Interval","Until","HiYel","HiRed",
		"LoYel","LoRed","HiBoth","LoBoth",
		"Midpoint","Limits","IgnoreCase",
		"Block","Sequence","Default","Mode",
		"Confirm","OnSkip","SendDelay",
		"Printer", "Format", "Extended"
	};
	/** Holds the default list of SPELL constants, applied when the plugin cannot obtain them */
	private static String[] s_defaultConstants =
	{
		"YES_NO", "ALPHA", "NUM", "LIST", "YES", "OK_CANCEL",
		"OK", "CANCEL", "NO", "YES_NO", "COMBO",
		"ENG", "RAW", "DEC", "BIN", "OCT", "HEX",
		"INFO", "WARNING", "ERROR", 
		"DISPLAY", "LOGVIEW","DIALOG","LONG","DATETIME","STRING",
		"FLOAT","BOOLEAN",
		"ABORT", "SKIP", "REPEAT", "RECHECK", "RESEND", "NOACTION",
		"PROMPT","NOPROMPT", 
		"ACTION_ABORT", "ACTION_SKIP", "ACTION_REPEAT", "ACTION_RESEND",
		"ACTION_CANCEL",
		"MINUTE", "HOUR", "TODAY", "YESTERDAY", "DAY", "SECOND"
	};
	/** Holds the default list of SPELL entities, applied when the plugin cannot obtain them */
	private static String[] s_defaultEntities =
	{
		"DAY",
		"GDB",
		"SCDB",
		"TIME",
		"HOUR",
		"MINUTE",
		"NOW",
		"SECOND",
		"TODAY",
		"TOMORROW",
		"YESTERDAY"
	};
	/** Holds the list of python and other keywords */
	private static String[] s_defaultKeywords =
	{
		"for", "if", "elif", "else", "try", "except",
		"while","in","print",
		"del", "def", 
		"command", "sequence", "group", "args",
		"verify", "config",
		"True", "False", "import", "type", "level",
		"and", "or", "not", "global", "str",
		"abs", "float", "int", "pass", "assert",
		"eq", "gt", "lt", "neq", "ge", "le", "bw", "nbw",
	};
	
	/**************************************************************************
	 * Spell languange singleton get() method
	 * @return
	 *************************************************************************/
	public static SpellProgrammingLanguage getInstance() 
	{
		if (s_instance == null) 
		{
			s_instance = new SpellProgrammingLanguage();
		}
		return s_instance;
	}
	
	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	private SpellProgrammingLanguage() 
	{
		m_constants = new ArrayList<String>();
		m_functions = new ArrayList<String>();
		m_modifiers = new ArrayList<String>();
	}
	
	/**************************************************************************
	 * Get spell functions
	 *************************************************************************/
	public String[] getSpellFunctions()
	{
		generateFunctions();
		return m_functions.toArray( new String[0] );
	}

	/**************************************************************************
	 * Check spell functions
	 *************************************************************************/
	public boolean isSpellFunction( String name )
	{
		generateFunctions();
		return m_functions.contains(name);
	}

	/**************************************************************************
	 * Generate set of spell functions
	 *************************************************************************/
	private void generateFunctions()
	{
		if (m_functions.isEmpty())
		{
			parseFunctions();
			// Provide the hardcoded defaults if there are no items read
			if (m_functions.size()==0)
			{
				m_functions.addAll( Arrays.asList(s_defaultFunctions) );
			}
			else
			{
				// FIXME this is hardcoded right now since they are not in functions.py
				m_functions.add("Goto");
				m_functions.add("Step");
			}
		}
	}
	
	/**************************************************************************
	 * Get spell modifiers
	 *************************************************************************/
	public String[] getSpellModifiers()
	{
		generateModifiers();
		return m_modifiers.toArray( new String[0] );
	}

	/**************************************************************************
	 * Check spell modifiers
	 *************************************************************************/
	public boolean isSpellModifier( String name )
	{
		generateModifiers();
		return m_modifiers.contains(name);
	}

	/**************************************************************************
	 * Generate set of spell modifiers
	 *************************************************************************/
	private void generateModifiers()
	{
		if (m_modifiers.isEmpty())
		{
			parseModifiers();
			// Provide the hardcoded defaults if there are no items read
			if (m_modifiers.size()==0)
			{
				m_modifiers.addAll( Arrays.asList(s_defaultModifiers) );
			}
		}
	}

	/**************************************************************************
	 * Get spell constants
	 *************************************************************************/
	public String[] getSpellConstants()
	{
		generateConstants();
		return m_constants.toArray( new String[0] );
	}

	/**************************************************************************
	 * Check spell constants
	 *************************************************************************/
	public boolean isSpellConstant( String name )
	{
		generateConstants();
		return m_constants.contains(name);
	}

	/**************************************************************************
	 * Generate set of spell constants
	 *************************************************************************/
	private void generateConstants()
	{
		if (m_constants.isEmpty())
		{
			parseConstants();
			// Provide the hardcoded defaults if there are no items read
			if (m_constants.size()==0)
			{
				m_constants.addAll( Arrays.asList(s_defaultConstants) );
			}
		}
	}

	/***************************************************************************
	 * Get spell entities
	 * @return a set of tokens which are considered "entities" in SPELL language
	 **************************************************************************/
	public String[] getSpellEntities()
	{
		/*
		 * FIXME at this moment these entities are hardcoded
		 */
		return s_defaultEntities;
	}

	/***************************************************************************
	 * Get spell entities
	 * @return a set of tokens which are considered "entities" in SPELL language
	 **************************************************************************/
	public String[] getSpellKeywords()
	{
		/*
		 * FIXME at this moment these entities are hardcoded
		 */
		return s_defaultKeywords;
	}

	/**************************************************************************
	 * Check spell functions
	 *************************************************************************/
	public boolean isSpellKeyword( String name )
	{
		return Arrays.asList(s_defaultKeywords).contains(name);
	}

	/**************************************************************************
	 * Parse the SPELL language functions file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseFunctions()
	{
		m_functions.clear();
		String path = System.getenv("SPELL_DEV_HOME");
		if (path == null)
		{
			System.err.println("[LANGUAGE] Unable to read functions file. No SPELL_DEV_HOME defined!!!");
			return;
		}
		String sep = System.getProperty("file.separator"); 
		path += sep + "spell" + sep + "spell" + sep + "lang" + sep + "functions.py"; 
		File functionsFile = new File( path );
		if (!functionsFile.exists())
		{
			System.err.println("[LANGUAGE] " + functionsFile.getAbsolutePath() + " does not exist. Using default");
			Bundle bundle = Activator.getDefault().getBundle();
			Path filePath = new Path("default_functions");
			URL url = FileLocator.find(bundle, filePath, Collections.EMPTY_MAP);
			URL fileURL = null;
			try
			{
				fileURL = FileLocator.toFileURL(url);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			functionsFile = new File(fileURL.getPath());
		}
		try 
		{
			FileReader in = new FileReader(functionsFile);
			BufferedReader reader = new BufferedReader(in);
			String line = null;
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					line = line.trim();
					if (line.isEmpty()) continue;
					if (line.startsWith("def"))
					{
						String[] items = line.split(" ");
						String funcName = items[1];
						if (funcName.endsWith("(")) funcName = funcName.substring(0,funcName.length()-1);
						m_functions.add(funcName);
					}
				}
			}
			while( line != null );
			reader.close();
			in.close();
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("[LANGUAGE] Unable to find functions file: " + e);
		}
		catch (IOException e) 
		{
			System.err.println("[LANGUAGE] Unable to read functions file: " + e);
		}
	}

	/**************************************************************************
	 * Parse the SPELL language modifiers file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseModifiers()
	{
		String path = System.getenv("SPELL_DEV_HOME");
		if (path == null)
		{
			System.err.println("[LANGUAGE] Unable to read modifiers file. No SPELL_DEV_HOME defined!!!");
			return;
		}
		String sep = System.getProperty("file.separator"); 
		path += sep + "spell" + sep + "spell" + sep + "lang" + sep + "modifiers.py"; 
		File modifiersFile = new File( path );
		if (!modifiersFile.exists())
		{
			System.err.println("[LANGUAGE] " + modifiersFile.getAbsolutePath() + " does not exist. Using default");
			Bundle bundle = Activator.getDefault().getBundle();
			Path filePath = new Path("default_modifiers");
			URL url = FileLocator.find(bundle, filePath, Collections.EMPTY_MAP);
			URL fileURL = null;
			try
			{
				fileURL = FileLocator.toFileURL(url);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			modifiersFile = new File(fileURL.getPath());
		}
		try 
		{
			FileReader in = new FileReader(modifiersFile);
			BufferedReader reader = new BufferedReader(in);
			String line = null;
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					line = line.trim();
					if (line.isEmpty()) continue;
					if (line.startsWith("#")) continue;
					if (!line.contains("=")) continue;
					if (line.startsWith("=")) continue;
					String[] items = line.split(" ");
					m_modifiers.add(items[0]);
				}
			}
			while( line != null );
			reader.close();
			in.close();
		} 
		catch (FileNotFoundException e) 
		{
			System.err.println("[LANGUAGE] Unable to find modifiers file: " + e);
		}
		catch (IOException e) 
		{
			System.err.println("[LANGUAGE] Unable to read modifiers file: " + e);
		}
	}

	/**************************************************************************
	 * Parse the SPELL language constants file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseConstants()
	{
		m_constants.clear();
		String path = System.getenv("SPELL_DEV_HOME");
		if (path == null)
		{
			System.err.println("[LANGUAGE] Unable to read constants file. No SPELL_DEV_HOME defined!!!");
			return;
		}
		String sep = System.getProperty("file.separator"); 
		path += sep + "spell" + sep + "spell" + sep + "lang" + sep + "constants.py"; 
		File constantsFile = new File( path );
		if (!constantsFile.exists())
		{
			System.err.println("[LANGUAGE] " + constantsFile.getAbsolutePath() + " does not exist. Using default");
			Bundle bundle = Activator.getDefault().getBundle();
			Path filePath = new Path("default_constants");
			URL url = FileLocator.find(bundle, filePath, Collections.EMPTY_MAP);
			URL fileURL = null;
			try
			{
				fileURL = FileLocator.toFileURL(url);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			constantsFile = new File(fileURL.getPath());
		}
		try 
		{
			FileReader in = new FileReader(constantsFile);
			BufferedReader reader = new BufferedReader(in);
			String line = null;
			do
			{
				line = reader.readLine();
				if (line != null)
				{
					line = line.trim();
					if (line.isEmpty()) continue;
					if (line.startsWith("#")) continue;
					if (!line.contains("=")) continue;
					if (line.startsWith("=")) continue;
					String[] items = line.split(" ");
					m_constants.add(items[0]);
				}
			}
			while( line != null );
			reader.close();
			in.close();
		} 
		catch (FileNotFoundException e) 
		{
			String err = "Unable to find constants file: " + e;
			System.err.println("[LANGUAGE] " + err);
		} 
		catch (IOException e) 
		{
			System.err.println("[LANGUAGE] Unable to read constants file: " + e);
		}
	}
}
