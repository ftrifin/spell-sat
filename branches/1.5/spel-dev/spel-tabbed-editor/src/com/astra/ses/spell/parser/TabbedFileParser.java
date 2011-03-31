///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.parser
// 
// FILE      : TabbedFileParser.java
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
package com.astra.ses.spell.parser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/******************************************************************************
 * TabbedFileParser class will read a tabbed file and return a matrix with
 * contained elements separated by \t
 * @author jpizar
 *****************************************************************************/
public class TabbedFileParser {
	
	/** File to parse */
	private String m_tabbedFilePath;
	/** Comment character */
	private String m_commentCharacter;
	/** The result fo parsing the file */
	private ArrayList<String[]> m_tabbedText;
	/** Longest line length */
	private int m_longest;
	
	/***************************************************************************
	 * Constructor
	 * @param filePath The absolute path where the file is located
	 * @param commentSeq The character sequence comment lines start with
	 **************************************************************************/
	public TabbedFileParser(String filePath, String commentSeq)
	{
		m_longest = 0;
		m_tabbedFilePath = filePath;
		m_commentCharacter = commentSeq;
		/*
		 * Create a thread which parses the file
		 */
		Runnable parsingThread = new Runnable()
		{
			@Override
			public void run()
			{
				parseFile();
			}
		};
		parsingThread.run();
	}
	
	/***************************************************************************
	 * Parse tabbed file
	 **************************************************************************/
	private void parseFile() {
		ArrayList<String[]> lines = new ArrayList<String[]>();
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(m_tabbedFilePath);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));			
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				String[] splitted = null;		
				
				// If line is a comment
				if (strLine.startsWith(m_commentCharacter))
				{
					strLine = strLine.replaceAll("\t", "    ");
					splitted = new String[]{strLine};
				}
				// Line is not a comment
				else
				{
					StringTokenizer tokenizer = new StringTokenizer(strLine);
					int totalTokens = tokenizer.countTokens();
					String key = "";
					String value = "";
					switch (totalTokens)
					{
					case 0 : // Blank line
						break;
					case 1 : // Key with no value
						key = tokenizer.nextToken();
						splitted = new String[]{key};
						break;
					default : // More than one. First token is key, the rest is value
						key = tokenizer.nextToken();
						while (tokenizer.hasMoreTokens())
						{
							value += " " + tokenizer.nextToken();
						}
						break;
					}
					splitted = new String[]{key,value};
				}
				lines.add(splitted);
				m_longest = (splitted.length > m_longest) ? splitted.length : m_longest;
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		m_tabbedText = lines;
	}
	
	/***************************************************************************
	 * Get the parsed tabbed text
	 * @return
	 **************************************************************************/
	public ArrayList<String[]> getTabbedText()
	{
		return m_tabbedText;
	}
	
	/***************************************************************************
	 * Get the length of the longest line
	 * @return
	 **************************************************************************/
	public int getLongestLength()
	{
		return m_longest;
	}
}
