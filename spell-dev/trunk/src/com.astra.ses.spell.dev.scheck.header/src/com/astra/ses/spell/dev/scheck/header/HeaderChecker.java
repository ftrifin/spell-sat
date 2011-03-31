///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.header
//
// FILE      : HeaderChecker.java
//
// DATE      : Feb 9, 2011
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
package com.astra.ses.spell.dev.scheck.header;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;

import com.astra.ses.spell.dev.config.ConfigurationManager;
import com.astra.ses.spell.dev.scheck.ResourceManager;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IStaticRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;

public class HeaderChecker implements IStaticRuleChecker 
{
	@Override
	public String getName() 
	{
		return "Procedure header checker";
	}

	@Override
	public void performCheck( IIssueList issues ) 
	{
		
		boolean inHeader = false;
		String sourceCode = ResourceManager.instance().getSource( (IFile) issues.getResource() );
		String[] source = sourceCode.split( "\n" );
		
		int offset = 0;
		int lineno = 1;
		boolean nameFound = false;
		boolean spacecraftFound = false;
		boolean twoDelimiters = false;
		
		for(String line : source)
		{
			if (isHeaderDelimiter(line))
			{
				if (inHeader)
				{
					twoDelimiters = true;
					break;
				}
				else
				{
					inHeader = true;
				}
			}
			else if (inHeader)
			{
				if (line.trim().startsWith("#"))
				{
					StringTokenizer tokenizer = new StringTokenizer(line);
					ArrayList<String> elements = new ArrayList<String>();
					while(tokenizer.hasMoreTokens())
					{
						String token = tokenizer.nextToken();
						elements.add(token);
					}
					if (elements.size()>=2)
					{
						String field = elements.get(1);
						// Check name
						if ((field.equals("NAME")||(field.equals("NAME:"))) && (!nameFound))
						{
							nameFound = true;
							checkNameField( line, offset, lineno, elements, issues );
						}

						// Check spacecraft
						if ((field.equals("SPACECRAFT")||(field.equals("SPACECRAFT:"))) && (!spacecraftFound))
						{
							spacecraftFound = true;
							checkScField( line, offset, lineno, elements, issues );
						}
						
						if (field.equals("PROCEDURE")||field.equals("PROCEDURE:"))
						{
							int offsetp = line.indexOf("PROCEDURE");
							issues.addIssue( IssueFactory.createWarningIssue("Header field 'PROCEDURE' is obsolete, please remove", lineno, offset+offsetp, offset+offsetp+9) );
						}
					}
				}
				else
				{
					// Check continuity
					if (line.trim().isEmpty())
					{
						issues.addIssue( IssueFactory.createErrorIssue("Header discontinued", lineno, offset, offset+2) );
					}
					else
					{
						// We found some code inside the header
						issues.addIssue( IssueFactory.createErrorIssue("Found code within header, or header is not properly delimited", lineno, offset, offset+2) );
						return;
					}
				}
			}
			else // Not header delimiter and not in header
			{
				// We found some code before the header started
				issues.addIssue( IssueFactory.createErrorIssue("Found code before header, or header is missing", lineno, offset, offset+2) );
				return;
			}
			offset += line.length() + 1;
			lineno++;
		}
		
		if (!nameFound)
		{
			issues.addIssue( IssueFactory.createErrorIssue("NAME field not found in header"));			
		}
		if (!spacecraftFound)
		{
			issues.addIssue( IssueFactory.createErrorIssue("NAME field not found in header"));			
		}
		if (!twoDelimiters)
		{
			issues.addIssue( IssueFactory.createErrorIssue("Please check header delimiters"));			
		}
	}

	private void checkNameField( String line, int offset, int lineno, List<String> elements, IIssueList issues )
	{
		if ((!elements.get(2).equals(":"))&&(!elements.get(1).endsWith(":")))
		{
			int offsetp = line.indexOf("NAME");
			issues.addIssue( IssueFactory.createErrorIssue("Malformed NAME field in header: missing colon", lineno, offset+offsetp, offset+offsetp+4) );
		}
		
		if (  ((elements.get(2).equals(":"))&&(elements.size()<4)) ||
				  ((elements.get(1).endsWith(":"))&&(elements.size()<3)) )
		{
			int offsetp = line.indexOf("NAME");
			issues.addIssue( IssueFactory.createErrorIssue("Malformed NAME field in header: missing procedure name", lineno, offset+offsetp, offset+offsetp+4) );
		}
	}

	private void checkScField( String line, int offset, int lineno, List<String> elements, IIssueList issues )
	{
		if ((!elements.get(2).equals(":"))&&(!elements.get(1).endsWith(":")))
		{
			int offsetp = line.indexOf("SPACECRAFT");
			issues.addIssue( IssueFactory.createErrorIssue("Malformed SPACECRAFT field in header: missing colon", lineno, offset+offsetp, offset+offsetp+10) );
		}
		
		if (  ((elements.get(2).equals(":"))&&(elements.size()<4)) ||
			  ((elements.get(1).endsWith(":"))&&(elements.size()<3)) )
		{
			int offsetp = line.indexOf("SPACECRAFT");
			issues.addIssue( IssueFactory.createErrorIssue("Malformed SPACECRAFT field in header: missing spacecraft list", lineno, offset+offsetp, offset+offsetp+10) );
			return;
		}
		
		List<String> sats = new ArrayList<String>();
		for( int index=3; index<elements.size(); index++)
		{
			String s = elements.get(index);
			if (s.contains(","))
			{
				String[] s2 = s.split(",");
				for(String sub : s2)
				{
					sats.add(sub.trim());
				}
			}
			else
			{
				sats.add(s.trim());
			}
		}		
		
		List<String> knownSats = ConfigurationManager.getInstance().getKnownSatellites();
		for( String sat : sats )
		{
			if (!knownSats.contains(sat))
			{
				int offsetp = line.indexOf("SPACECRAFT");
				issues.addIssue( IssueFactory.createWarningIssue("Spacecraft family '" + sat + "' is unknown", lineno, offset+offsetp, offset+offsetp+10) );
			}
		}
	}

	private boolean isHeaderDelimiter( String line )
	{
		if (!line.startsWith("#")) return false;
		for( int index = 0; index<line.length(); index++ )
		{
			char c = line.charAt(index); 
			if ((c!='#')&&(c!='\r')&&(c!='\n')) return false;
		}
		if (line.length()<5) return false;
		return true;
	}
}
