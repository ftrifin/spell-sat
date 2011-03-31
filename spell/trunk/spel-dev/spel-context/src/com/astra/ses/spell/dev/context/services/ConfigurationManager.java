///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.context.services
// 
// FILE      : ConfigurationManager.java
//
// DATE      : 2008-11-21 13:54
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.context.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
 * @author jpizar
 *****************************************************************************/
public class ConfigurationManager /*extends BaseService*/ {

	// PUBLIC ------------------------------------------------------------------
	/** Holds the service ID */
	public static final String ID = "com.astra.ses.spell.dev.ConfigurationManager";
	/** DOM element names */
	public static final String ELEMENT_DATABASE = "database";
	public static final String ELEMENT_DATABASE_DRIVER = "driver";
	public static final String ELEMENT_DATABASE_PATH = "path";
	public static final String ELEMENT_PROJECT = "project";
	public static final String ELEMENT_FOLDER = "folder";
	public static final String ELEMENT_PACKAGE = "package";
	
	// PRIVATE -----------------------------------------------------------------
	/** Holds the path of the XML configuration file */
	private static final String DEFAULT_CONFIG_PATH = "dev";
	private static final String DEFAULT_CONFIG_HOME = "config" + File.separator + DEFAULT_CONFIG_PATH;
	private static final String DEFAULT_CONFIG_FILE = "config.xml";
	private static final String CLIENT_HOME_ENV = "SPELL_HOME";
	private static final String CLIENT_CONFIG_ENV = "SPELL_CONFIG";
	
	/** Singleton instance */
	private static ConfigurationManager s_instance;
	
	/** Project structure as a DOM Object */
	private Element m_projectStructure;
	/** Database structure as a DOM object */
	private Element m_dbStructure;
	
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
	}
	
	/***************************************************************************
	 * Setup the service: load the XML configuration file
	 **************************************************************************/
	public void setup()
	{
		loadConfigFile();
		loadContents();
	}

	/***************************************************************************
	 * Cleanup the service
	 **************************************************************************/
	public void cleanup()
	{
	}

	/***************************************************************************
	 * Subscribe to required services
	 **************************************************************************/
	public void subscribe()
	{
	}
	
	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Load the XML configuration file
	 **************************************************************************/
	protected void loadConfigFile()
	{
		String path = getConfigHome() + File.separator + DEFAULT_CONFIG_PATH + File.separator + DEFAULT_CONFIG_FILE;
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
	}
	
	/**************************************************************************
	 * Read the database properties 
	 * @param docElement
	 *************************************************************************/
	private void loadDatabaseProperties(Element docElement)
	{
		// Load the project structure
		NodeList nodes = docElement.getElementsByTagName(ELEMENT_DATABASE);
		if (nodes.getLength() != 1)
		{
			System.err.println("[ERROR] Duplicated project database");
		}
		for (int i=0; i < nodes.getLength(); i++)
		{
			m_dbStructure = (Element) nodes.item(0);
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
		if (nodes.getLength() != 1)
		{
			System.err.println("[ERROR] Duplicated project structure definition");
		}
		for (int i=0; i < nodes.getLength(); i++)
		{
			m_projectStructure = (Element) nodes.item(0);
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
	 * Get the project source folder if it has one
	 * @return the absolute path for the source folder, or null if it does not
	 * exist
	 *************************************************************************/
	public String getProjectSourceFolder()
	{
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
						return folderName;
					}
				}
			}
		}
		return null;
	}
	
	/***************************************************************************
	 * Get default database driver
	 * @return
	 **************************************************************************/
	public String getDatabaseDriver()
	{
		// This conditional statement is set for the case the configuration file 
		// does not define a default value
		if (m_dbStructure == null)
		{
			return "";
		}
		NodeList childs = m_dbStructure.getChildNodes();
		for (int i=0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = child.getNodeName();
				if (nodeName.equals(ConfigurationManager.ELEMENT_DATABASE_DRIVER))
				{	
					return child.getTextContent();			
				}
			}
		}
		return "";
	}
	
	/***************************************************************************
	 * Get default database path
	 * @return
	 **************************************************************************/
	public String getDatabasePath()
	{
		// This conditional statement is set for the case the configuration file 
		// does not define a default value
		if (m_dbStructure == null)
		{
			return "";
		}
		NodeList childs = m_dbStructure.getChildNodes();
		for (int i=0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = child.getNodeName();
				if (nodeName.equals(ConfigurationManager.ELEMENT_DATABASE_PATH))
				{	
					return child.getTextContent();			
				}
			}
		}
		return "";
	}
}
