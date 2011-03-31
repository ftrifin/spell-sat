///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.services
// 
// FILE      : ConfigurationManager.java
//
// DATE      : 2008-11-21 16:37
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
package com.astra.ses.spell.gui.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.ICoreConstants;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.BaseService;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServerProxy;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.model.ColorInfo;
import com.astra.ses.spell.gui.model.FontInfo;
import com.astra.ses.spell.gui.model.IConfig;


/*******************************************************************************
 * @brief Service which provides all the information in relation with the
 *        currently selected SPELL configuration. It provides the XML GUI
 *        configuration data as well.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ConfigurationManager extends BaseService
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	/** Holds the path of the XML configuration file */
	protected static final String DEFAULT_CONFIG_PATH = "config";
	protected static final String INTERNAL_CONFIG_PATH = "data";
	protected static final String DEFAULT_CONFIG_FILE = "gui-config.xml";
	// PUBLIC ------------------------------------------------------------------
	/** Holds the service ID */
	public static final String ID = "com.astra.ses.spell.gui.ConfigurationManager";

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Profile document DOM model */
	private Document m_cfgDoc;
	/** Configuration property list */
	private TreeMap<String, String> m_properties;
	/** List of available servers */
	private TreeMap<String, ServerInfo> m_servers;
	/** List of defined fonts */
	private TreeMap<String, FontInfo> m_fonts;
	/** List of defined colors for status */
	private TreeMap<String, ColorInfo> m_statusColors;
	/** List of defined colors for procs */
	private TreeMap<String, ColorInfo> m_procColors;
	/** List of defined colors for gui */
	private TreeMap<String, ColorInfo> m_guiColors;
	/** Required presentations for the procedures */
	private Vector<String> m_presentations;
	/** Path separator */
	private String m_pathSeparator;
	/** Internal selection service */
	private TreeMap<String,Object> m_selection;
	
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ConfigurationManager()
	{
		super(ID);
		m_properties = null;
		m_cfgDoc = null;
		m_selection = new TreeMap<String,Object>();
		m_fonts = new TreeMap<String,FontInfo>();
		m_statusColors = new TreeMap<String,ColorInfo>();
		m_procColors = new TreeMap<String,ColorInfo>();
		m_guiColors = new TreeMap<String,ColorInfo>();
		m_presentations = new Vector<String>();
		Logger.debug("Created", Level.CONFIG, this);
	}

	/***************************************************************************
	 * Setup the service: load the XML configuration file
	 **************************************************************************/
	public void setup()
	{
		Logger.debug("Setting up", Level.CONFIG, this);
		Properties props = System.getProperties();
		m_pathSeparator = props.get("file.separator").toString();
		loadConfigFile();
		loadProperties();
		setInitialConfigurations();
		loadServers();
		loadAppearance();
		loadPresentations();
	}

	/***************************************************************************
	 * Cleanup the service
	 **************************************************************************/
	public void cleanup()
	{
		Logger.debug("Cleanup", Level.CONFIG, this);
	}

	/***************************************************************************
	 * Subscribe to required services
	 **************************************************************************/
	public void subscribe()
	{
	}

	/***************************************************************************
	 * Obtain the list of available servers. This list is defined in the XML
	 * configuration file.
	 * 
	 * @return The list of available servers
	 **************************************************************************/
	public Vector<String> getAvailableServers()
	{
		Vector<String> servers = new Vector<String>(m_servers.keySet());
		return servers;
	}

	/***************************************************************************
	 * Obtain the server information
	 * 
	 * @return Server data structure
	 **************************************************************************/
	public ServerInfo getServerData( String serverID )
	{
		return m_servers.get(serverID);
	}

	/***************************************************************************
	 * Obtain the system path separator
	 * 
	 * @return The system path separator
	 **************************************************************************/
	public String getPathSeparator()
	{
		return m_pathSeparator;
	}
	
	/***************************************************************************
	 * Get the required procedure presentations
	 **************************************************************************/
	public Vector<String> getPresentations()
	{
		return m_presentations;
	}
	
	/***************************************************************************
	 * Obtain the value of a XML file property
	 * 
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 **************************************************************************/
	public String getProperty(String propertyName)
	{
		if (propertyName==null)
		{
			System.err.println("ERROR: NULL PROPERTY NAME");
			return null;
		}
		if (m_properties.containsKey(propertyName))
		{
			return m_properties.get(propertyName);
		}
		System.err.println("ERROR: NO SUCH PROPERTY: " + propertyName);
		return null;
	}

	/***************************************************************************
	 * Obtain the value of a XML file property
	 * 
	 * @param propertyName
	 *            The property name
	 * @return The property value
	 **************************************************************************/
	public boolean isEnabled(String propertyName)
	{
		if (m_properties.containsKey(propertyName))
		{
			return getProperty(propertyName).equals(IConfig.PROPERTY_VALUE_YES);
		}
		return false;
	}
	
	/***************************************************************************
	 * Obtain a predefined color
	 * 
	 * @param code
	 *            Color code
	 * @return The color object
	 **************************************************************************/
	public Color getStatusColor( ItemStatus status )
	{
		String st = status.getName();
		ColorInfo ci = m_statusColors.get(st);
		if (ci == null)
		{
			Logger.error("Undefined status color: " + st, Level.GUI, this);
			return null;
		}
		return ci.getColor();
	}

	/***************************************************************************
	 * Obtain a predefined color
	 * 
	 * @param code
	 *            Color code
	 * @return The color object
	 **************************************************************************/
	public Color getGuiColor( String id )
	{
		ColorInfo ci = m_guiColors.get(id);
		if (ci == null)
		{
			Logger.error("Undefined gui color: " + id, Level.GUI, this);
			return null;
		}
		return ci.getColor();
	}

	/***************************************************************************
	 * Obtain a predefined color
	 * 
	 * @param code
	 *            Color code
	 * @return The color object
	 **************************************************************************/
	public Color getProcedureColor( ExecutorStatus status )
	{
		String st = status.toString();
		ColorInfo ci = m_procColors.get(st);
		if (ci == null)
		{
			Logger.error("Undefined procedure color: " + st, Level.GUI, this);
			return null;
		}
		return ci.getColor();
	}

	/***************************************************************************
	 * Obtain predefined font
	 * 
	 * @return The font
	 **************************************************************************/
	public Font getFont( String code )
	{
		return m_fonts.get(code).getFont();
	}

	/***************************************************************************
	 * Obtain predefined font
	 * 
	 * @return The font
	 **************************************************************************/
	public Font getFont( String code, int size )
	{
		return m_fonts.get(code).getFont(size);
	}

	/***************************************************************************
	 * Get the SPELL home directory
	 **************************************************************************/
	public String getHome()
	{
		String home = System.getenv(ICoreConstants.CLIENT_HOME_ENV);
		if (home == null) return ".";
		return home;
	}

	/***************************************************************************
	 * Set selection data
	 **************************************************************************/
	public void setSelection( String key, Object data )
	{
		m_selection.put(key,data);
	}

	/***************************************************************************
	 * Get selection data
	 **************************************************************************/
	public Object getSelection( String key )
	{
		return m_selection.get(key);
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Load the XML configuration file
	 **************************************************************************/
	protected void loadConfigFile()
	{
		String[] args = Platform.getApplicationArgs();
		String path = null;
		if (args.length>0)
		{
			int count = 0;
			for(String arg : args)
			{
				if (arg.equals("-config") && (args.length-1) > count)
				{
					path = args[count+1];
					break;
				}
				count++;
			}
		}
		
		if (path == null)
		{
			path = getHome()+ getPathSeparator() + DEFAULT_CONFIG_PATH + getPathSeparator() + DEFAULT_CONFIG_FILE;
			if (!(new File(path).canRead()))
			{
				String loc = Platform.getBundle(Activator.PLUGIN_ID).getLocation();
				loc = loc.substring(loc.lastIndexOf(":") + 1, loc.length());
				path = loc + getPathSeparator() + INTERNAL_CONFIG_PATH + getPathSeparator() + DEFAULT_CONFIG_FILE;
			}
		}
		Logger.info("Using configuration file: " + path, Level.CONFIG, this);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try
		{
			FileInputStream stream = new FileInputStream(path);
			DocumentBuilder db = dbf.newDocumentBuilder();
			m_cfgDoc = db.parse(stream);
		}
		catch(FileNotFoundException e)
		{
			Logger.error("Cannot read configuration" + " file: '" + path + "'", Level.CONFIG, this);
			System.exit(1);
		}
		catch (ParserConfigurationException e)
		{
			Logger.error("Parse (CFG) error: " + e.getLocalizedMessage(), Level.CONFIG, this);
			System.exit(1);
		}
		catch (SAXException e)
		{
			Logger.error("Parse (SAX) error: " + e.getLocalizedMessage(), Level.CONFIG, this);
			System.exit(1);
		}
		catch (IOException e)
		{
			Logger.error("Parse (UKN) error: " + e.getLocalizedMessage(), Level.CONFIG, this);
			System.exit(1);
		}
		Logger.info("Configuration loaded", Level.CONFIG, this);
	}
	
	/***************************************************************************
	 * Load GUI general properties
	 **************************************************************************/
	protected void loadProperties()
	{
		Element docElement = m_cfgDoc.getDocumentElement();
		// Read all properties
		NodeList nl = docElement.getElementsByTagName("property");
		if (nl != null && nl.getLength() > 0)
		{
			if (m_properties == null)
			{
				m_properties = new TreeMap<String, String>();
			}
			else
			{
				m_properties.clear();
			}
			for (int count = 0; count < nl.getLength(); count++)
			{
				Element element = (Element) nl.item(count);
				String name = element.getAttribute("name");
				String value = element.getTextContent();
				m_properties.put(name, value);
				Logger.info("Loaded property: " + name + "->" + value,Level.CONFIG,this);
			}
		}
		Logger.info("All properties loaded (" + m_properties.size() + ")", Level.CONFIG, this);
	}
	
	/***************************************************************************
	 * Load the servers section
	 **************************************************************************/
	protected void loadServers()
	{
		Element docElement = m_cfgDoc.getDocumentElement();
		// Read the first "servers" tag found
		NodeList nl = docElement.getElementsByTagName("servers");
		
		if (nl == null || nl.getLength() == 0 )
		{
			Logger.error("No SPELL server names found!", Level.CONFIG, this);
			return;
		}
		Element servers = (Element) nl.item(0);
		
		if (m_servers == null)
		{
			m_servers = new TreeMap<String, ServerInfo>();
		}
		else
		{
			m_servers.clear();
		}
		NodeList serverList = servers.getElementsByTagName("server");
		if (serverList == null || serverList.getLength() == 0 )
		{
			Logger.error("No SPELL server names found!", Level.CONFIG, this);
			return;
		}
		for (int count = 0; count < serverList.getLength(); count++)
		{
			Element server = (Element) serverList.item(count);

			ServerInfo info = new ServerInfo(server);
			
			if (info.validate())
			{
				m_servers.put(info.getId(),info);
			}
		}
	}

	/***************************************************************************
	 * Load the procedure presentations section
	 **************************************************************************/
	protected void loadPresentations()
	{
		Element docElement = m_cfgDoc.getDocumentElement();
		// Read the first "presentations" tag found
		NodeList nl = docElement.getElementsByTagName("presentations");
		
		if (nl == null || nl.getLength() == 0 )
		{
			Logger.error("No presentation configuration found!", Level.CONFIG, this);
			return;
		}
		Element presentations = (Element) nl.item(0);
		
		m_presentations.clear();

		NodeList pDefinitions = presentations.getElementsByTagName("presentation");

		if (pDefinitions == null || pDefinitions.getLength() == 0 )
		{
			Logger.error("No presentations found!", Level.CONFIG, this);
		}
		else
		{
			String defaultPresentation = null;
			for (int count = 0; count < pDefinitions.getLength(); count++)
			{
				Node node = pDefinitions.item(count);
				NamedNodeMap attrs = node.getAttributes();
				String pName = attrs.getNamedItem("name").getNodeValue();
				if (attrs.getLength()>1)
				{
					if (attrs.getNamedItem("default").getNodeValue().toLowerCase().equals("yes"))
					{
						Logger.debug("Added DEFAULT presentation: " + pName, Level.CONFIG, this);
						defaultPresentation = pName;
					}
					else
					{
						Logger.debug("Added presentation: " + pName, Level.CONFIG, this);
						m_presentations.add(pName);
					}
				}
				else
				{
					Logger.debug("Added presentation: " + pName, Level.CONFIG, this);
					m_presentations.add(pName);
				}
			}
			if (defaultPresentation != null) 
			{
				m_presentations.insertElementAt(defaultPresentation, 0);
			}
		}
	}
	
	/***************************************************************************
	 * Load the appearance section
	 **************************************************************************/
	protected void loadAppearance()
	{
		Element docElement = m_cfgDoc.getDocumentElement();
		// Read the first "appearance" tag found
		NodeList nl = docElement.getElementsByTagName("appearance");
		
		if (nl == null || nl.getLength() == 0 )
		{
			Logger.error("No appearance configuration found!", Level.CONFIG, this);
			return;
		}
		Element appearance = (Element) nl.item(0);
		
		m_fonts.clear();
		m_statusColors.clear();
		m_procColors.clear();
		m_guiColors.clear();

		NodeList colorDefinitions = appearance.getElementsByTagName("colors");
		Element colorDefs = (Element) colorDefinitions.item(0);

		NodeList statusColors = colorDefs.getElementsByTagName("statuscolors");
		if (statusColors == null || statusColors.getLength() == 0 )
		{
			Logger.error("No status color definitions found!", Level.CONFIG, this);
		}
		else
		{
			NodeList colorList = statusColors.item(0).getChildNodes();
			for (int count = 0; count < colorList.getLength(); count++)
			{
				Node node = colorList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element color = (Element) node;
					ColorInfo info = new ColorInfo(color);
					if (info.getColor() != null)
					{
						m_statusColors.put(info.getId(), info);
					}
				}
			}
		}

		NodeList guiColors = appearance.getElementsByTagName("guicolors");
		if (guiColors == null || guiColors.getLength() == 0 )
		{
			Logger.error("No gui color definitions found!", Level.CONFIG, this);
		}
		else
		{
			NodeList colorList = guiColors.item(0).getChildNodes();
			for (int count = 0; count < colorList.getLength(); count++)
			{
				Node node = colorList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element color = (Element) node;
					ColorInfo info = new ColorInfo(color);
					if (info.getColor() != null)
					{
						m_guiColors.put(info.getId(), info);
					}
				}
			}
		}
		
		NodeList procColors = appearance.getElementsByTagName("proccolors");
		if (procColors == null || procColors.getLength() == 0 )
		{
			Logger.error("No proc color definitions found!", Level.CONFIG, this);
		}
		else
		{
			NodeList colorList = procColors.item(0).getChildNodes();
			for (int count = 0; count < colorList.getLength(); count++)
			{
				Node node = colorList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element color = (Element) node;
					ColorInfo info = new ColorInfo(color);
					if (info.getColor() != null)
					{
						m_procColors.put(info.getId(), info);
					}
				}
			}
		}
		
		NodeList fontDefinitions = appearance.getElementsByTagName("fonts");
		Element fontDefs = (Element) fontDefinitions.item(0);

		NodeList fontList = fontDefs.getElementsByTagName("font");
		if (fontList == null || fontList.getLength() == 0 )
		{
			Logger.error("No font definitions found!", Level.CONFIG, this);
		}
		else
		{
			for (int count = 0; count < fontList.getLength(); count++)
			{
				Node node = fontList.item(count);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element fontDef = (Element) node;
					FontInfo info = new FontInfo(fontDef);
					if (info.getFont() != null)
					{
						m_fonts.put(info.getId(), info);
					}
				}
			}
		}
	}
	
	protected void setInitialConfigurations()
	{
		if (!isEnabled(IConfig.PROPERTY_USETRACES))
		{
			Logger.warning("Traces disabled by configuration", Level.CONFIG, this);
			Logger.enableTraces(false);
		}
		if (isEnabled(IConfig.PROPERTY_SHOWDEBUG))
		{
			Logger.info("Enabled traces on standard out", Level.CONFIG, this);
			Logger.showDebug(true);
		}
		String sd = m_properties.get(IConfig.PROPERTY_SHOWDEBUG);
		Logger.info("Show debug traces: " + sd, Level.CONFIG, this);
		Logger.showDebug( isEnabled(IConfig.PROPERTY_SHOWDEBUG) );
		String level = m_properties.get(IConfig.PROPERTY_DEBUG_LEVEL);
		Logger.info("Tracing level: " + level,Level.CONFIG,this);
		Logger.setShowLevel(level);
		long rtimeout = Long.parseLong(getProperty(IConfig.PROPERTY_RTIMEOUT));
		long otimeout = Long.parseLong(getProperty(IConfig.PROPERTY_OTIMEOUT));
		ContextProxy cproxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		cproxy.setResponseTimeout(rtimeout);
		cproxy.setOpenTimeout(otimeout);
		ServerProxy sproxy = (ServerProxy) ServiceManager.get(ServerProxy.ID);
		sproxy.setResponseTimeout(rtimeout);
		sproxy.setOpenTimeout(otimeout);
	}
}
