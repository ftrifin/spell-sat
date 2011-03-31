///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.project.nature
// 
// FILE      : SpellNatureStore.java
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
package com.astra.ses.spell.dev.resources.nature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

/******************************************************************************
 * This class is intended to be used for loading and storing project
 * properties
 *****************************************************************************/
public class SpellNatureStore {

	/** File where the properties are saved */
	private static final String PROPERTIES_FILE = ".spell";
	/** Spell project description node in properties file */
	private final static String SPELL_PROJECT_ROOT = "spellProject";
	/** Database Driver key */
	public static final QualifiedName GCS_DRIVER = new QualifiedName(null, "DATABASE_DRIVER");
	/**  Path to database key*/
	public static final QualifiedName DB_PATH = new QualifiedName(null, "DATABASE_PATH");
	
	/** IProject handler */
	private IProject m_project;
	/** File where properties are stored and loaded */
	private File m_propertiesFile;
	/** Map for getting properties in memory */
	private Map<QualifiedName, String> m_propertiesMap;
	
	/**************************************************************************
	 * Constructor
	 * It is supposed that this project has spell nature as it has been checked
	 * by SpellNature class
	 * @param projectHandler
	 *************************************************************************/
	public SpellNatureStore(IProject projectHandler)
	{
		m_project = projectHandler;
		m_propertiesMap = new HashMap<QualifiedName, String>();
		init();
	}
	
	/**************************************************************************
	 * Initialize the properties file handler
	 *************************************************************************/
	private void init()
	{
		IFile propertiesFile = m_project.getFile(PROPERTIES_FILE);
		if (!propertiesFile.exists()) 
		{
			IPath path = propertiesFile.getRawLocation();
			m_propertiesFile = path.toFile();
			try {
				m_propertiesFile.createNewFile();
				FileInputStream fileInputStream = new FileInputStream(m_propertiesFile);
				propertiesFile.create(fileInputStream, true, new NullProgressMonitor());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		} 
		else 
		{
			IPath path = propertiesFile.getRawLocation();
			m_propertiesFile = path.toFile();
			loadFromFile();
		}
	}
	
	/**************************************************************************
	 * Load properties from a file
	 * @param file
	 *************************************************************************/
	private void loadFromFile()
	{
		try {
			FileInputStream input = new FileInputStream(m_propertiesFile);
			//Using factory get an instance of document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(input);
			
			//get the root element
			Element docEle = dom.getDocumentElement();
			NodeList children = docEle.getChildNodes();
			for (int i = 0; i < children.getLength(); i++ )
			{
				Element child = (Element) children.item(i);
				String name = child.getNodeName();
				String value = child.getTextContent();
				m_propertiesMap.put(new QualifiedName(null, name), value);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**************************************************************************
	 * Store the properties
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 *************************************************************************/
	public void doStore()
	{
		//get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			//get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//create an instance of DOM
			Document dom = db.newDocument();
			Element root = dom.createElement(SPELL_PROJECT_ROOT);
			for (QualifiedName key : m_propertiesMap.keySet())
			{
				String value = m_propertiesMap.get(key);
				Element e = dom.createElement(key.toString());
				e.setTextContent(value);
				root.appendChild(e);
			}
			FileOutputStream outputFile = new FileOutputStream(m_propertiesFile);	
			DOMImplementation implementation= DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0");
			DOMImplementationLS feature = (DOMImplementationLS) implementation.getFeature("LS",	"3.0");
			LSSerializer serializer = feature.createLSSerializer();
			LSOutput output = feature.createLSOutput();
			output.setByteStream(outputFile);
			serializer.write(dom, output);
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}
	
	/**************************************************************************
	 * Get a property by giving its qualified name
	 * @return
	 *************************************************************************/
	public Object getProperty(QualifiedName name)
	{
		if (m_propertiesMap.containsKey(name))
		{
			return m_propertiesMap.get(name);
		}
		return null;
	}
	
	/**************************************************************************
	 * Set the database driver name property for this project
	 * @param dbDriverName
	 *************************************************************************/
	public void setDatabaseDriver(String dbDriverName)
	{
		m_propertiesMap.put(GCS_DRIVER, dbDriverName);
	}
	
	/**************************************************************************
	 * Get the database driver set for this project
	 * @return
	 *************************************************************************/
	public String getDatabaseDriver()
	{
		if (m_propertiesMap.containsKey(GCS_DRIVER))
		{
			return m_propertiesMap.get(GCS_DRIVER);
		}
		return null;
	}
}
