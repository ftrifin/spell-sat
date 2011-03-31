///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.properties
// 
// FILE      : DatabasePropertiesManager.java
//
// DATE      : 2009-09-14
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
package com.astra.ses.spell.dev.database.properties;

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

import com.astra.ses.spell.dev.context.services.ConfigurationManager;
import com.astra.ses.spell.dev.database.properties.ProjectDatabaseProperties.SpellDBProperties;


/*******************************************************************************
 * Properties Manager will deal with DB related project properties
 * @author jpizar
 ******************************************************************************/
public class DatabasePropertiesManager {

	/** Project */
	private IProject m_project;
	
	/***************************************************************************
	 * Private constructor
	 **************************************************************************/
	public DatabasePropertiesManager(IProject project)
	{
		m_project = project;
	}
	
	/***************************************************************************
	 * Restore project properties
	 * @param project
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	private void restoreProjectProperties(SpellDBProperties property)
	{
		HashMap<QualifiedName, Object> properties = null;
		try {
			properties = new HashMap<QualifiedName, Object>(m_project.getPersistentProperties());
		} catch (CoreException e1) {
			System.err.println("Cannot retrieve persistent propertier for project " + m_project.getName());
			return;
		}
		if (!properties.containsKey(property.getKey()))
		{
			try {
				String defaultVal = getDefaultValue(property);
				m_project.setPersistentProperty(property.getKey(), defaultVal);
			} catch (CoreException e) {
				System.err.println("Cannot set default value for property " + property.getKey());
			}
		}
	}
	
	/**************************************************************************
	 * Get the database driver attached to a project
	 * @param project
	 * @return
	 *************************************************************************/
	@SuppressWarnings("unchecked")
	public String getDatabaseDriverName()
	{
		HashMap<QualifiedName, Object> properties = null;
		try {
			properties = new HashMap<QualifiedName, Object>(m_project.getPersistentProperties());
		} catch (CoreException e1) {
			System.err.println("Cannot retrieve persistent properties for project " + m_project.getName());
			return null;
		}
		if (!properties.containsKey(SpellDBProperties.GCS_DRIVER.getKey()))
		{
			restoreProjectProperties(SpellDBProperties.GCS_DRIVER);
		}
		
		try {
			return m_project.getPersistentProperty(SpellDBProperties.GCS_DRIVER.getKey());
		} catch (CoreException e) {
			return null;
		}
	}
	
	/**************************************************************************
	 * Get the database path for this project
	 * @return
	 *************************************************************************/
	public String getDatabasePath()
	{
		HashMap<QualifiedName, Object> properties = null;
		try {
			properties = new HashMap<QualifiedName, Object>(m_project.getPersistentProperties());
		} catch (CoreException e1) {
			System.err.println("Cannot retrieve persistent properties for project " + m_project.getName());
			return null;
		}
		if (!properties.containsKey(SpellDBProperties.GCS_DATABASE.getKey()))
		{
			restoreProjectProperties(SpellDBProperties.GCS_DATABASE);
		}
		
		try {
			String path = m_project.getPersistentProperty(SpellDBProperties.GCS_DATABASE.getKey());
			// Relative path
			boolean absolute = true;
			if (!path.isEmpty())
			{
				absolute = new File(path).isAbsolute();
			}
			if (!path.isEmpty() && !absolute)
			{
				IPath projAbsolute = m_project.getLocation();
				String projectPath = projAbsolute.toFile().getAbsolutePath();
				path = projectPath + File.separator + path;
			}
			return path;
		} catch (CoreException e) {
			return null;
		}
	}
	
	/**************************************************************************
	 * Change the database driver for loading databases
	 * @param project
	 * @param newDBPath
	 *************************************************************************/
	public void setDatabaseDriver(String driver)
	{
		try {
			m_project.setPersistentProperty(SpellDBProperties.GCS_DRIVER.getKey(), driver);
		} catch (CoreException e) {
			System.err.println("Cannot set property " + SpellDBProperties.GCS_DRIVER.getKey());
		}
	}
	
	/**************************************************************************
	 * Change the database to load when a resource from this project is opened
	 * @param project
	 * @param newDBPath
	 *************************************************************************/
	public void setDatabasePath(String newDBPath)
	{
		try {
			// If path is inside the project directory, then change
			String projectPath = m_project.getLocation().toFile().getAbsolutePath();
			if (newDBPath.startsWith(projectPath))
			{
				// + 1 for removing a File.separator
				newDBPath = newDBPath.substring(projectPath.length() + 1);
			}
			m_project.setPersistentProperty(SpellDBProperties.GCS_DATABASE.getKey(), newDBPath);
		} catch (CoreException e) {
			System.err.println("Cannot set property " + SpellDBProperties.GCS_DATABASE.getKey());
		}
	}
	
	/***************************************************************************
	 * Restore the default values for this project
	 * @param project
	 **************************************************************************/
	public void restoreDefaults()
	{
		for (SpellDBProperties prop : SpellDBProperties.values())
		{
			String defaultVal= getDefaultValue(prop);
			try {
				m_project.setPersistentProperty(prop.getKey(), defaultVal);
			} catch (CoreException e) {
				System.err.println("Cannot set default value for " + prop.getKey());
			}
		}
	}
	
	/***************************************************************************
	 * Get the default value forthe given property
	 * @param property
	 * @return
	 **************************************************************************/
	private String getDefaultValue(SpellDBProperties property)
	{
		ConfigurationManager mgr = ConfigurationManager.getInstance();
		String defaultVal= "";
		switch (property)
		{
			case GCS_DATABASE: 
				defaultVal = mgr.getDatabasePath(); 
				break;
			case GCS_DRIVER: 
				defaultVal = mgr.getDatabaseDriver(); 
				break;
			default: break;
		}
		return defaultVal;
	}
}
