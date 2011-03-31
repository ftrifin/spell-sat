///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.language
// 
// FILE      : SpellProgrammingLanguage.java
//
// DATE      : 2009-11-23
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/*****************************************************************************
 * Class for providing spell language special constants, functions and symbols
 * It tries to add new symbols to Python programming language
 * @author jpizar
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
	 * Get spell constants
	 *************************************************************************/
	public String[] getSpellConstants()
	{
		if (m_constants.size()==0) parseConstants();

		return m_constants.toArray( new String[0] );
	}

	/**************************************************************************
	 * Parse the SPELL language constants file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseConstants()
	{
		String path = System.getenv("SPELL_HOME");
		if (path == null)
		{
			System.err.println("[LANGUAGE] Unable to read constants file. No SPELL_HOME defined!!!");
			return;
		}
		String sep = System.getProperty("file.separator"); 
		path += sep + "spell" + sep + "spell" + sep + "lang" + sep + "constants.py"; 
		File constantsFile = new File( path );
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
					if (line.startsWith("#") || line.trim().length()==0) continue;
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
			System.err.println("[LANGUAGE] Unable to find constants file: " + e);
		} 
		catch (IOException e) 
		{
			System.err.println("[LANGUAGE] Unable to read constants file: " + e);
		}
	}

	/**************************************************************************
	 * Get spell functions
	 *************************************************************************/
	public String[] getSpellFunctions()
	{
		if (m_functions.size()==0) parseFunctions();

		return m_constants.toArray( new String[0] );
	}
	
	/**************************************************************************
	 * Parse the SPELL language functions file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseFunctions()
	{
		String path = System.getenv("SPELL_HOME");
		if (path == null)
		{
			System.err.println("[LANGUAGE] Unable to read functions file. No SPELL_HOME defined!!!");
			return;
		}
		String sep = System.getProperty("file.separator"); 
		path += sep + "spell" + sep + "spell" + sep + "lang" + sep + "functions.py"; 
		File constantsFile = new File( path );
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
	 * Get spell modifiers
	 *************************************************************************/
	public String[] getSpellModifiers()
	{
		if (m_modifiers.size()==0) parseModifiers();

		return m_modifiers.toArray( new String[0] );
	}

	/**************************************************************************
	 * Parse the SPELL language modifiers file to get the actual set of
	 * available constants
	 *************************************************************************/
	private void parseModifiers()
	{
		String path = System.getenv("SPELL_HOME");
		if (path == null)
		{
			System.err.println("[LANGUAGE] Unable to read modifiers file. No SPELL_HOME defined!!!");
			return;
		}
		String sep = System.getProperty("file.separator"); 
		path += sep + "spell" + sep + "spell" + sep + "lang" + sep + "modifiers.py"; 
		File constantsFile = new File( path );
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
					if (line.startsWith("#") || line.trim().length()==0) continue;
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

}
