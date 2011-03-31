///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.context.services
// 
// FILE      : ConfigurationManager.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.dev.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/******************************************************************************
 * This class imitates Spel GUI configuration manager in order to provide
 * acces to some properties stored in configuration files
 *****************************************************************************/
public class ConfigurationManager 
{

	// PUBLIC ------------------------------------------------------------------
	/** Holds the service ID */
	public static final String ID = "com.astra.ses.spell.dev.config.ConfigurationManager";
	/** DOM element names */
	public static final String ELEMENT_DATABASE = "database";
	public static final String ELEMENT_DATABASE_DRIVER = "driver";
	public static final String ELEMENT_DATABASE_PATH = "path";
	public static final String ELEMENT_PROJECT = "project";
	public static final String ELEMENT_FOLDER = "folder";
	public static final String ELEMENT_PACKAGE = "package";
	public static final String ELEMENT_KSATS = "known-sats";
	public static final String ELEMENT_KFUNCTIONS = "known-functions";
	public static final String ELEMENT_DFUNCTIONS = "discouraged-functions";
	
	// PRIVATE -----------------------------------------------------------------
	/** Holds the path of the XML configuration file */
	private static final String DEFAULT_CONFIG_HOME = "config";
	private static final String DEFAULT_CONFIG_FILE = "config.xml";
	private static final String CLIENT_HOME_ENV = "SPELL_DEV_HOME";
	private static final String CLIENT_CONFIG_ENV = "SPELL_DEV_CONFIG";
	
	private static final String[] language =
	{
		"int", "float",
		"abs", "min", "max", "sin", "cos", "asin", "acos", "tan", "atan", "pow", "round",
		"len", "str", "repr", "globals", "vars", "locals", "list", "dict", "eval",
		"keys", "range", "type", "set",
		"TIME"
	};

	private static final String[] disc_language =
	{
		"open", "file", "close", "write", "read", "time", "sleep", "append", "eval"
	};

	private static final String[] satellites =
	{
		"A1N", "A1M", "A2B", "A3B", "A4B", "N12", "SES01", "SES02",
		"SES03", "SES04", "AMC15", "AMC21", "AMC16", "QS1"
	};

	/** Singleton instance */
	private static ConfigurationManager s_instance;
	
	/** Project structure as a DOM Object */
	private Element m_projectStructure;
	/** Holds the name of source folder */
	private String m_sourceFolder;
	/** Database structure as a DOM object */
	private Element m_dbStructure;
	/** Holds the default database driver name if any */
	private String m_defaultDatabaseDriver;
	/** Holds the default database path if any */
	private String m_defaultDatabasePath;
	/** Holds the list of known satellite families */
	private List<String> m_knownSatellites;
	/** Holds the list of known functions */
	private List<String> m_knownFunctions;
	/** Holds the list of discouraged functions */
	private List<String> m_discouragedFunctions;
	
	
	// PRIVATE -----------------------------------------------------------------
	/** Profile document DOM model */
	private Document m_cfgDoc;
	
	/**************************************************************************
	 * Return the singleton instance
	 * @return
	 ***************************************************************************/
	public static ConfigurationManager getInstance()
	{
		if (s_instance == null)
		{
			s_instance = new ConfigurationManager();
		}
		return s_instance;
	}
	
	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	private ConfigurationManager()
	{
		loadConfigFile();
		loadContents();
	}
	
	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Load the XML configuration file
	 **************************************************************************/
	protected void loadConfigFile()
	{
		String path = getConfigHome() + File.separator + DEFAULT_CONFIG_FILE;
		System.out.println("[*] Using configuration file: " + path);
		if (!(new File(path).canRead()))
		{
			path = getHome()+ File.separator + DEFAULT_CONFIG_HOME + File.separator + DEFAULT_CONFIG_FILE;
			System.out.println("[*] Using default configuration: " + path);
			if (!(new File(path).canRead()))
			{
				throw new RuntimeException("Unable to read configuration file: " + path);
			}
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			FileInputStream stream = new FileInputStream(path);
			DocumentBuilder db = dbf.newDocumentBuilder();
			m_cfgDoc = db.parse(stream);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**************************************************************************
	 * Load the contents of the XML file
	 *************************************************************************/
	private void loadContents()
	{
		Element docElement = m_cfgDoc.getDocumentElement();
		loadDatabaseProperties(docElement);
		loadProjectStructure(docElement);
		loadKnownSatellites(docElement);
		loadKnownFunctions(docElement);
		loadDiscouragedFunctions(docElement);
	}
	
	/**************************************************************************
	 * Read the database properties 
	 * @param docElement
	 *************************************************************************/
	private void loadDatabaseProperties(Element docElement)
	{
		// Load the project structure
		NodeList nodes = docElement.getElementsByTagName(ELEMENT_DATABASE);
		if (nodes.getLength() > 1)
		{
			System.err.println("[ERROR] Redundant database definition");
		}
		else if (nodes.getLength()==0)
		{
			System.err.println("[ERROR] Missing database definition");
			return;
		}
		m_dbStructure = (Element) nodes.item(0);
		m_defaultDatabaseDriver = "";
		m_defaultDatabasePath = "";
		NodeList childs = m_dbStructure.getChildNodes();
		for (int i=0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = child.getNodeName();
				if (nodeName.equals(ConfigurationManager.ELEMENT_DATABASE_DRIVER))
				{	
					m_defaultDatabaseDriver = child.getTextContent();
				}
				else if (nodeName.equals(ConfigurationManager.ELEMENT_DATABASE_PATH))
				{	
					m_defaultDatabasePath = child.getTextContent();			
				}
			}
		}
	}
	
	/**************************************************************************
	 * Load the project structure and save it in a XML file
	 * @param docElement
	 *************************************************************************/
	private void loadProjectStructure(Element docElement)
	{
		// Load the project structure
		NodeList nodes = docElement.getElementsByTagName(ELEMENT_PROJECT);
		if (nodes.getLength() > 1)
		{
			System.err.println("[ERROR] Redundant project structure definition");
		}
		else if (nodes.getLength()==0)
		{
			System.err.println("[ERROR] Missing project structure definition");
			return;
		}
		m_projectStructure = (Element) nodes.item(0);
		NodeList childs = m_projectStructure.getChildNodes();
		for (int i=0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = child.getNodeName();
				if (nodeName.equals(ConfigurationManager.ELEMENT_FOLDER))
				{	
					NamedNodeMap map = child.getAttributes();
					String folderName = map.getNamedItem("name").getNodeValue();
					String folderType = map.getNamedItem("type").getNodeValue();
					if (folderType.equals("procedure"))
					{
						m_sourceFolder = folderName;
						break;
					}
				}
			}
		}
	}
	
	/**************************************************************************
	 * Load the known satellites
	 * @param docElement
	 *************************************************************************/
	private void loadKnownSatellites(Element docElement)
	{
		// Load the project structure
		NodeList nodes = docElement.getElementsByTagName(ELEMENT_KSATS);
		if (nodes.getLength() > 1)
		{
			System.err.println("[ERROR] Redundant known satellite list");
		}
		else if (nodes.getLength()==0)
		{
			System.err.println("[ERROR] Missing known satellite list");
			return;
		}
		m_knownSatellites = new ArrayList<String>();
		Element node = (Element) nodes.item(0);
		String list = node.getTextContent();
		if ((list != null)&&(!list.isEmpty()))
		{
			String[] sats = list.split(",");
			for(String sat : sats)
			{
				m_knownSatellites.add( sat.trim() );
			}
		}
		if (m_knownSatellites.size()==0)
		{
			for(String sat : satellites )
			{
				m_knownSatellites.add(sat);
			}
		}
	}

	/**************************************************************************
	 * Load the known functions for semantics
	 * @param docElement
	 *************************************************************************/
	private void loadKnownFunctions(Element docElement)
	{
		// Load the project structure
		NodeList nodes = docElement.getElementsByTagName(ELEMENT_KFUNCTIONS);
		if (nodes.getLength() > 1)
		{
			System.err.println("[ERROR] Redundant known function list");
		}
		else if (nodes.getLength()==0)
		{
			System.err.println("[ERROR] Missing known function list");
			return;
		}
		m_knownFunctions = new ArrayList<String>();
		Element node = (Element) nodes.item(0);
		String list = node.getTextContent();
		if ((list != null)&&(!list.isEmpty()))
		{
			String[] funs = list.split(",");
			for(String func : funs)
			{
				m_knownFunctions.add( func.trim() );
			}
		}
		if (m_knownFunctions.size()==0)
		{
			for(String function : language )
			{
				m_knownFunctions.add(function);
			}
		}
	}

	/**************************************************************************
	 * Load the known functions for semantics
	 * @param docElement
	 *************************************************************************/
	private void loadDiscouragedFunctions(Element docElement)
	{
		// Load the project structure
		NodeList nodes = docElement.getElementsByTagName(ELEMENT_DFUNCTIONS);
		if (nodes.getLength() > 1)
		{
			System.err.println("[ERROR] Redundant discouraged function list");
		}
		else if (nodes.getLength()==0)
		{
			System.err.println("[ERROR] Missing discouraged function list");
			return;
		}
		m_discouragedFunctions = new ArrayList<String>();
		Element node = (Element) nodes.item(0);
		String list = node.getTextContent();
		if ((list != null)&&(!list.isEmpty()))
		{
			String[] funs = list.split(",");
			for(String func : funs)
			{
				m_discouragedFunctions.add( func.trim() );
			}
		}
		if (m_discouragedFunctions.size()==0)
		{
			for(String function : disc_language )
			{
				m_discouragedFunctions.add(function);
			}
		}
	}
	
	/***************************************************************************
	 * Get the SPELL home directory
	 **************************************************************************/
	public String getHome()
	{
		String home = System.getenv(CLIENT_HOME_ENV);
		return home;
	}

	/***************************************************************************
	 * Get the SPELL configuration home directory
	 **************************************************************************/
	public String getConfigHome()
	{
		String home = System.getenv(CLIENT_CONFIG_ENV);
		if (home == null)
		{
			home = getHome() + File.separator + "config";
		}
		return home;
	}

	/**************************************************************************
	 * Return the project structure object
	 * @return
	 *************************************************************************/
	public Element getProjectStructure()
	{
		return m_projectStructure;
	}

	/**************************************************************************
	 * Return the list of known satellites
	 * @return
	 *************************************************************************/
	public List<String> getKnownSatellites()
	{
		return m_knownSatellites;
	}

	/**************************************************************************
	 * Return the list of known functions
	 * @return
	 *************************************************************************/
	public List<String> getKnownFunctions()
	{
		return m_knownFunctions;
	}

	/**************************************************************************
	 * Return the list of discouraged functions
	 * @return
	 *************************************************************************/
	public List<String> getDiscouragedFunctions()
	{
		return m_discouragedFunctions;
	}

	/**************************************************************************
	 * Get the project source folder if it has one
	 * @return the absolute path for the source folder, or null if it does not
	 * exist
	 *************************************************************************/
	public String getProjectSourceFolder()
	{
		return m_sourceFolder;
	}
	
	/***************************************************************************
	 * Get default database driver
	 * @return
	 **************************************************************************/
	public String getDefaultDatabaseDriver()
	{
		return m_defaultDatabaseDriver;
	}
	
	/***************************************************************************
	 * Get default database path
	 * @return
	 **************************************************************************/
	public String getDefaultDatabasePath()
	{
		return m_defaultDatabasePath;
	}
}
