///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : ses.astra.spell.core.model.server
// 
// FILE      : ClientInfo.java
//
// DATE      : 2008-11-21 08:58
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.model.server;

import java.util.ArrayList;
import java.util.Vector;

/*******************************************************************************
 * Representation of an Log file
 * 
 * @author Rafael Chinchilla (GMV)
 *
 ******************************************************************************/
public class LogFile implements TabbedFile
{
	public enum LogLineType
	{
		INFO("INFO"),
		WARNING("WARN"),
		ERROR("ERROR"),
		UNKNOWN("UNKNOWN");
		
		/** Text Representation */
		private String m_textRepresentation;
		
		/***********************************************************************
		 * Constructor
		 **********************************************************************/
		private LogLineType(String text)
		{
			m_textRepresentation = text;
		}
		
		/***********************************************************************
		 * Get text representation for this log line type
		 * @return
		 **********************************************************************/
		public String getTextRepresentation()
		{
			return m_textRepresentation;
		}
	}
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the Log file lines */
	private ArrayList<TabbedFileLine> m_logLines;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * INNER CLASS: LogLine - represents a line of the Log file
	 **************************************************************************/
	public class LogLine implements TabbedFileLine
	{
		public LogLine()
		{
			elements = new ArrayList<String>();
		}
		
		public String getCallStack() {
			String result = elements.get(0);
			return result;
		}
		
		public String getType() {
			String result = elements.get(1);
			return result;
		}

		public String getTime() {
			String result = elements.get(2);
			return result;
		}

		public String getLevel() {
			String result = elements.get(3);
			return result;
		}

		public String getMessage() {
			String result = elements.get(4);
			return result;
		}
		
		@Override
		public String getElement(int index) {
			switch (index)
			{
				case 0 : return getCallStack();
				case 1 : return getType();
				case 2 : return getTime();
				case 3 : return getLevel();
				case 4 : return getMessage();
				default : return "";
			}
		}
		
		@Override
		public String getFormalElements() {
			String result = getCallStack() + "\t" + getType() + "\t" +
				getTime() + "\t" + getLevel() + "\t" + getMessage();
			return result;
		}
		
		/***********************************************************************
		 * Get the type of this log line
		 * @return the type, which may be INFO, ERROR or WARN
		 **********************************************************************/
		public LogLineType getLogLineType()
		{
			String type = getType();
			String cleanedType = type.replaceAll("[\\[\\] ]", "");
			try
			{
				return LogLineType.valueOf(cleanedType);
			}
			catch (Exception e)
			{
				return LogLineType.UNKNOWN;
			}
		}
		
		public ArrayList<String>  elements;
	}
	
	/***************************************************************************
	 * Constructor
	 * @param source
	 **************************************************************************/
	public LogFile( Vector<String> source )
	{
		m_logLines = new ArrayList<TabbedFileLine>();
		if (source != null)
		{
			parse(source);
		}
	}
	
	/***************************************************************************
	 * Parse the given Log source data
	 * @param source
	 **************************************************************************/
	private void parse( Vector<String> source )
	{
		for(String line : source )
		{
			line = line.replaceFirst("%C%", "");
			String[] elements = line.split("\t");
			LogLine logLine = new LogLine();
			for(int i=0; i<getHeaderLabels().length; i++)
			{
				// There is an element in that position
				if (elements.length > i)
				{
					logLine.elements.add(elements[i]);
				}
				else
				{
					logLine.elements.add("");
				}
			}
			m_logLines.add(logLine);
		}
	}
	
	/***************************************************************************
	 * Obtain the list of Log lines
	 * @return
	 **************************************************************************/
	@Override
	public ArrayList<TabbedFileLine> getData()
	{
		return m_logLines;
	}
	
	@Override
	public String[] getHeaderLabels() {
		String[] headerLabels = {"Call Stack","Type","Time","Level","Message"};
		return headerLabels;
	}

	@Override
	public int[] getHeaderLabelsSize() {
		int[] headerLabelsSize = {255,75,170,75,650};
		return headerLabelsSize;
	}
}
