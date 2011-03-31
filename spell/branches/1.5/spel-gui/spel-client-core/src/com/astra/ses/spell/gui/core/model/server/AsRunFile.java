///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
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

import com.astra.ses.spell.gui.core.model.types.AsRunType;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;


/*******************************************************************************
 * Representation of an AsRun file
 * 
 * @author Rafael Chinchilla (GMV)
 *
 ******************************************************************************/
public class AsRunFile implements TabbedFile
{
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
	/** Holds the AsRun file lines */
	private ArrayList<TabbedFileLine> m_arLines;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * INNER CLASS: AsRunLine - represents a line of the AsRun file
	 **************************************************************************/
	public class AsRunLine implements TabbedFileLine
	{
		public AsRunLine(String timestamp)
		{
			type = AsRunType.UNKNOWN;
			elements = new ArrayList<String>();
			m_timestamp = timestamp;
		}
		
		public String getType() {
			return type.toString();
		}
		
		public String getCallStack() {
			String result = "";
			if (elements.get(0).startsWith("$"))
			{
				result = elements.get(0);
			}
			return result;
		}
		
		public String getTypeValue() {
			String result = "";
			if (!elements.get(0).startsWith("$"))
			{
				result = elements.get(0);
			}
			return result;
		}
		
		public String getName() {
			String result = "";
			if (type == AsRunType.ITEM)
			{
				result = elements.get(1);
			}
			return result;
		}
		
		public String getValue() {
			String result = "";
			if (type == AsRunType.ITEM)
			{
				result = elements.get(2);
			}
			return result;
		}
		
		public String getStatus() {
			String result = "";
			if (type == AsRunType.ITEM)
			{
				result = elements.get(3);
			}
			return result;
		}
		
		public String getComments() {
			String result = "";
			if (type == AsRunType.ITEM)
			{
				result = elements.get(4);
			}
			else if (type == AsRunType.DISPLAY)
			{
				result = elements.get(1) + elements.get(2);
			}
			else if (type == AsRunType.PROMPT)
			{
				result = elements.get(1);
			}
			return result;
		}
		
		public String getExecutionTime() {
			return m_timestamp;
		}
		
		@Override
		public String getElement(int index) {
			switch (index)
			{
				case 0 : return getExecutionTime();
				case 1 : return getType();
				case 2 : return getCallStack();
				case 3 : return getTypeValue();
				case 4 : return getName();
				case 5 : return getValue();
				case 6 : return getStatus();
				case 7 : return getComments();
				default : return "";
			}
		}
		
		public String getFormalElements()
		{
			String result = getExecutionTime() + "\t" + getType();
			result += "\t[" + getCallStack() + "]";
			result += "\t[" + getTypeValue() + "]";
			result += "\t(" + getName() + "," + getValue() + "," + getStatus() + ")";
			return result;
		}
		
		public AsRunType 		  type;
		public ArrayList<String>  elements;
		public String m_timestamp;
	}
	
	/***************************************************************************
	 * Constructor
	 * @param source
	 **************************************************************************/
	public AsRunFile( Vector<String> source )
	{
		m_arLines = new ArrayList<TabbedFileLine>();
		if (source != null)
		{
			parse(source);
		}
	}
	
	/***************************************************************************
	 * Parse the given AsRun source data
	 * @param source
	 **************************************************************************/
	private void parse( Vector<String> source )
	{
		int count = 1;
		for(String line : source )
		{
			try
			{
				int idx = line.indexOf("\t");
				String timestamp = line.substring(0, idx);
				line = line.substring(idx+1);
				String[] elements = line.split("\t");
				AsRunLine arLine = new AsRunLine(timestamp);
				arLine.type = artypeFromString(elements[0]);
				for(idx=1; idx<elements.length; idx++)
				{
					arLine.elements.add(elements[idx]);
				}
				m_arLines.add(arLine);
			}
			catch(Exception ex)
			{
				System.err.println("Unable to process asrun line: '" + line + "' (" + count + "): " + ex);
				Logger.error("Unable to process asrun line: '" + line + "' (" + count + "): " + ex, Level.PROC, this);
			}
			count++;
		}
	}
	
	/***************************************************************************
	 * Obtain the list of AsRun lines
	 * @return
	 **************************************************************************/
	@Override
	public ArrayList<TabbedFileLine> getData()
	{
		return m_arLines;
	}
	
	/***************************************************************************
	 * Translate a string to an AR type
	 * @param ar
	 * @return
	 **************************************************************************/
	public static AsRunType artypeFromString( String ar )
	{
		for(AsRunType type : AsRunType.values())
		{
			if (type.toString().equals(ar))
			{
				return type;
			}
		}
		return AsRunType.UNKNOWN;
	}

	@Override
	public String[] getHeaderLabels() {
		String[] headerLabels = {"Time","Type","Call stack","Type value","Name","Value","Status","Comments"};
		return headerLabels;
	}

	@Override
	public int[] getHeaderLabelsSize() {
		int[] headerLabelsSize = {100,90,90,90,90,150,100,150};
		return headerLabelsSize;
	}
}
