///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.event.sfunctions.model
// 
// FILE      : Rule.java
//
// DATE      : Mar 22, 2011
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
package com.astra.ses.spell.dev.scheck.event.sfunctions.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IDefinedArgument;
import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IDefinedModifier;
import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IGivenArgument;
import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IGivenModifier;
import com.astra.ses.spell.dev.scheck.event.sfunctions.interfaces.IRule;
import com.astra.ses.spell.dev.scheck.interfaces.AbstractEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.dev.scheck.interfaces.ValueType;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.exprType;
import com.astra.ses.spell.language.model.ast.keywordType;

public class Rule extends AbstractEventRuleChecker implements IRule
{
	private final static String ELEMENT_MODIFIERS = "modifiers";
	private final static String ELEMENT_MODIFIER = "modifier";
	
	private final static String ELEMENT_ARGUMENTS = "arguments";
	private final static String ELEMENT_ARGUMENT = "argument";

	private final static String ELEMENT_REQUIRED = "required";
	private final static String ELEMENT_ALLOWED = "allowed";

	private String m_name;
	private String m_definitionFile;
	private Map<String,IDefinedModifier> m_requiredModifiers;
	private Map<String,IDefinedModifier> m_allowedModifiers;
	private Map<Integer,IDefinedArgument> m_requiredArguments;
	private Map<Integer,IDefinedArgument> m_allowedArguments;

	@Override
    public String getName()
    {
	    return m_name;
    }

	/**************************************************************************
	 * 
	 * @param definitionFile
	 *************************************************************************/
	public Rule( String name, String definitionFile )
	{
		m_name = name;
		m_definitionFile = definitionFile;
		m_requiredModifiers = new TreeMap<String,IDefinedModifier>();
		m_allowedModifiers = new TreeMap<String,IDefinedModifier>();
		m_requiredArguments = new TreeMap<Integer,IDefinedArgument>();
		m_allowedArguments = new TreeMap<Integer,IDefinedArgument>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			FileInputStream stream = new FileInputStream(m_definitionFile);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document cfgDoc = db.parse(stream);
			Element docElement = cfgDoc.getDocumentElement();
			loadModifiers( docElement );
			loadArguments( docElement );
		}
		catch(FileNotFoundException e)
		{
			System.err.println("[SEMANTICS] Failed to load rule for " + name);
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			System.err.println("[SEMANTICS] Failed to load rule for " + name);
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			System.err.println("[SEMANTICS] Failed to load rule for " + name);
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.err.println("[SEMANTICS] Failed to load rule for " + name);
			e.printStackTrace();
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private void loadModifiers( Element docElement )
	{
		m_requiredModifiers.clear();
		m_allowedModifiers.clear();
		
		NodeList list = docElement.getElementsByTagName(ELEMENT_MODIFIERS);
		if (list.getLength()>=1)
		{
			Element node = (Element) list.item(0);
			NodeList childs = node.getChildNodes();
			for (int i=0; i < childs.getLength(); i++)
			{
				Node child = childs.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					String nodeName = child.getNodeName();
					if (nodeName.equals(ELEMENT_REQUIRED))
					{	
						loadModifiersTo( (Element) child, m_requiredModifiers );
					}
					else if (nodeName.equals(ELEMENT_ALLOWED))
					{
						loadModifiersTo( (Element) child, m_allowedModifiers );
					}
				}
			}
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private void loadModifiersTo( Element element, Map<String,IDefinedModifier> list )
	{
		NodeList childs = element.getChildNodes();
		for (int i=0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = child.getNodeName();
				if (nodeName.equals(ELEMENT_MODIFIER))
				{	
					Element childElement = (Element) child;
					IDefinedModifier modifier = ModifierFactory.createDefinedModifier(childElement);
					list.put( modifier.getName(), modifier );
				}
			}
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void loadArguments( Element docElement )
	{
		m_requiredArguments.clear();
		m_allowedArguments.clear();
		
		NodeList list = docElement.getElementsByTagName(ELEMENT_ARGUMENTS);
		if (list.getLength()>=1)
		{
			Element node = (Element) list.item(0);
			NodeList childs = node.getChildNodes();
			for (int i=0; i < childs.getLength(); i++)
			{
				Node child = childs.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					String nodeName = child.getNodeName();
					if (nodeName.equals(ELEMENT_REQUIRED))
					{	
						loadArgumentsTo( (Element) child, m_requiredArguments );
					}
					else if (nodeName.equals(ELEMENT_ALLOWED))
					{
						loadArgumentsTo( (Element) child, m_allowedArguments );
					}
				}
			}
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void loadArgumentsTo( Element element, Map<Integer,IDefinedArgument> list )
	{
		NodeList childs = element.getChildNodes();
		for (int i=0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = child.getNodeName();
				if (nodeName.equals(ELEMENT_ARGUMENT))
				{	
					Element childElement = (Element) child;
					IDefinedArgument argument = ArgumentFactory.createDefinedArgument(childElement);
					list.put( new Integer(argument.getPosition()), argument);
				}
			}
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void checkRule( Call call, IIssueList issues )
	{
		checkModifiers( call, issues );
		checkArguments( call, issues );
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void checkArguments( Call call, IIssueList issues )
	{
		Map<Integer,IGivenArgument> givenArguments = getGivenArguments(call, issues);
		
		// If no arguments passed
		if (givenArguments.isEmpty())
		{
			if (!m_requiredArguments.isEmpty())
			{
				issues.addIssue( 
						IssueFactory.createErrorIssue("No positional arguments in function call, expected " + m_requiredArguments.size() + " arguments at least",  
								call));
			}
		}
		// If arguments passed
		else
		{
			// If given arguments are not enough
			if (m_requiredArguments.size()>givenArguments.size())
			{
				issues.addIssue( 
						IssueFactory.createErrorIssue("Wrong number of positional arguments in function call, expected " + m_requiredArguments.size() + " arguments at least",  
								call));
			}
			// If given arguments are too much
			else if (givenArguments.size() > m_requiredArguments.size() + m_allowedArguments.size())
			{
				issues.addIssue( 
						IssueFactory.createErrorIssue("Too many positional arguments in function call, expected " + (m_requiredArguments.size() + m_allowedArguments.size()) + " arguments at most",  
								call));
			}
			else
			{
				// Check that all required arguments are there
				for( IDefinedArgument darg : m_requiredArguments.values() )
				{
					int dposition = darg.getPosition();
					IGivenArgument given = givenArguments.get(dposition);
					if (given == null)
					{
						issues.addIssue( 
								IssueFactory.createErrorIssue("Positional argument " + dposition + " not found",  
										call));
					}
					// If there is an argument in that position
					else
					{
						// Ensure the value type is correct
						if (!darg.isValidType(given.getGivenType()))
						{
							issues.addIssue( 
									IssueFactory.createErrorIssue("Wrong positional argument (" + given.getPosition() + ") value type, expected " + Arrays.toString(darg.getValidTypes().toArray()).toLowerCase()
											+ ", found " + given.getGivenType().toString().toLowerCase(),  
											given.getValueToken()));
						}
						if (!darg.isValidValue(given.getGivenValue(),given.getGivenType()))
						{
							issues.addIssue( 
									IssueFactory.createErrorIssue("Wrong value for positional argument (" + given.getPosition() + "), expected " + Arrays.toString(darg.getExpectedValues().toArray()).toLowerCase()
											+ ", found " + given.getGivenValue(),  
											given.getValueToken()));
						}
					}
				}
			}
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void checkModifiers( Call call, IIssueList issues )
	{
		Map<String,IGivenModifier> givenModifiers = getGivenModifiers(call, issues);
		// If no modifiers passed
		if (givenModifiers.isEmpty())
		{
			// Check if there are mandatory modifiers
			if (!m_requiredModifiers.isEmpty())
			{
				String missingMods = "";
				for( String modifier : m_requiredModifiers.keySet() )
				{
					if (!missingMods.isEmpty()) missingMods += ", ";
					missingMods += modifier;
				}
				issues.addIssue( 
						IssueFactory.createErrorIssue("Wrong number of modifiers in function call, expected: " + missingMods, 
								call));

			}
		}
		else // Modifiers passed, check that they are consistent
		{
			// Check consistency of given modifiers
			for( String modName : givenModifiers.keySet() )
			{
				if ((!m_allowedModifiers.containsKey(modName)&&(!m_requiredModifiers.containsKey(modName))))
				{
					issues.addIssue( 
							IssueFactory.createErrorIssue("Modifier '" + modName + "' not allowed", 
									givenModifiers.get(modName).getNameToken()));
				}
				else // Given modifier is accepted
				{
					// Check given value type, unless definition says any
					IDefinedModifier definition = m_allowedModifiers.get(modName);
					if (definition == null) definition = m_requiredModifiers.get(modName);
					IGivenModifier given = givenModifiers.get(modName);

					if (!definition.isValidType(ValueType.ANY))
					{
						if (!definition.isValidType(given.getGivenType()))
						{
							issues.addIssue( 
									IssueFactory.createErrorIssue("Modifier value type is wrong, expected type " +
											Arrays.toString(definition.getValidTypes().toArray()).toLowerCase() + ", found "
											+ given.getGivenType().toString().toLowerCase(), 
											given.getValueToken()));
						}
					}
					
					// Check required values
					if (!definition.getExpectedValues().isEmpty())
					{
						if (!definition.isValidValue(given.getGivenValue(), given.getGivenType()))
						{
							issues.addIssue( 
									IssueFactory.createErrorIssue("Modifier value is wrong, expected: " +
											Arrays.toString(definition.getExpectedValues().toArray()) + " found " + given.getGivenValue(), 
											given.getValueToken()));
						}
					}
					
					// Check if required modifiers, for given modifiers are present
					for( String reqMod : definition.getRequires())
					{
						if (!givenModifiers.containsKey(reqMod))
						{
							issues.addIssue( 
									IssueFactory.createErrorIssue("Modifier '" + definition.getName() + "' requires the modifier '" + reqMod + "'", 
											given.getNameToken()));
						}
						//TODO check required modifier values
					}
					
				}//else given modifier accepted
				
			}//for given modifier
			
			// Check that all required modifiers are present
			
			// Hold a list of the already checked, not to repeat with alternatives
			List<String> checked = new ArrayList<String>();
			for( IDefinedModifier reqMod : m_requiredModifiers.values() )
			{
				if (!checked.contains(reqMod.getName()))
				{
					// If the modifier is not present
					if (!givenModifiers.containsKey(reqMod.getName()))
					{
						boolean alternativeFound = false;
						// If there are alternatives
						for( String alternative : reqMod.getAlternatives() )
						{
							// And the alternative is present, check the value and type for the alternative
							if (givenModifiers.containsKey(alternative))
							{
								alternativeFound = true;
								checked.add(alternative);
								break;
							}
						}
						if (!alternativeFound)
						{
							issues.addIssue( 
									IssueFactory.createErrorIssue("Modifier '" + reqMod.getName() + "' is required", 
											call));
						}
					}
					checked.add(reqMod.getName());
				}
			}
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private Map<String,IGivenModifier> getGivenModifiers( Call call, IIssueList issues )
	{
		Map<String,IGivenModifier> givenModifiers = new TreeMap<String,IGivenModifier>();
		
		if (call.keywords.length != 0)
		{
			for( keywordType kw : call.keywords )
			{
				IGivenModifier mod = ModifierFactory.createGivenModifier(kw);
				givenModifiers.put(mod.getName(),mod);
			}
		}
		
		return givenModifiers;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private Map<Integer,IGivenArgument> getGivenArguments( Call call, IIssueList issues )
	{
		Map<Integer,IGivenArgument> givenArguments = new TreeMap<Integer,IGivenArgument>();

		if (call.args.length != 0)
		{
			int index = 0;
			for( exprType arg : call.args)
			{
				IGivenArgument ag = ArgumentFactory.createGivenArgument( index, arg );
				givenArguments.put( new Integer(index),ag);
				index++;
			}
		}
		
		return givenArguments;
	}
}
