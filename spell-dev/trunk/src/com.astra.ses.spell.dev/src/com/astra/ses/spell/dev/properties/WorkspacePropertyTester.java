///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.properties
// 
// FILE      : WorkspacePropertyTester.java
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
package com.astra.ses.spell.dev.properties;

import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.python.pydev.plugin.nature.PythonNature;

import com.astra.ses.spell.dev.resources.nature.SpellNature;

/*******************************************************************************
 * 
 * WorkspacePropertyTester works on the workspace resources to check
 * some of their properties
 *
 ******************************************************************************/
public class WorkspacePropertyTester extends PropertyTester {

	private static final String NAMESPACE = "com.astra.ses.spell.dev.properties.workspace";
	
	/** is_package property */
	public static final String PROPERTY_PACKAGE = NAMESPACE + ".is_package";
	/** is_source_folder_property */
	public static final String PROPERTY_SOURCE_FOLDER = NAMESPACE + ".is_source_folder";
	public static final String PROPERTY_PACKAGE_DEFINITION = NAMESPACE + ".is_package_definition_file";
	/** has spell nature property */
	public static final String PROPERTY_SPELL_NATURE = NAMESPACE + "has_spell_nature";
	
	/** A package always contains a file called __init__.py */
	private static final String PACKAGE_DEFINITION_FILE = "__init__.py";
	
	public WorkspacePropertyTester() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		/*
		 * It is assumed that the receiver should be an IResource
		 * implementing object
		 * And the expected value should be a boolean
		 */
		IResource resource = (IResource) receiver;
		boolean result = false;
		if (property.equals("is_package"))
		{
			if (resource.getType() == IResource.FOLDER)
			{
				result = isPackageFolder((IFolder) resource);
			}
		}
		else if (property.equals("is_source_folder"))
		{
			if (resource.getType() == IResource.FOLDER)
			{
				result = isSourceFolder((IFolder) resource);
			}
		}
		else if (property.equals("is_package_definition_file"))
		{
			IFile file = (IFile) resource;
			result = file.getName().equals(PACKAGE_DEFINITION_FILE);
		}
		else if (property.equals("has_spell_nature"))
		{

			result = hasSpellNature(resource);
		}
		boolean expectedResult = (Boolean) expectedValue;
		return result == expectedResult;
	}

	/***************************************************************************
	 * Check if the given folder is a package folder
	 * @param folder
	 * @return
	 **************************************************************************/
	public boolean isPackageFolder(IFolder folder)
	{
		return folder.getFile(PACKAGE_DEFINITION_FILE).exists();
	}
	
	/***************************************************************************
	 * Check if the given folder is a soruce folder inside a SPELL project
	 * @param folder
	 * @return
	 **************************************************************************/
	public boolean isSourceFolder(IFolder folder)
	{
		boolean isSourceFolder = false;
		try {
			PythonNature nature = (PythonNature) folder.getProject().getNature(PythonNature.PYTHON_NATURE_ID);
			Set<String> sourcePaths = nature.getPythonPathNature().getProjectSourcePathSet(false);
			String folderPath = folder.getFullPath().toString();
			for (String sourcePath : sourcePaths)
			{
				if (sourcePath.equals(folderPath))
				{
					isSourceFolder = true;
					break;
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return isSourceFolder;
	}
	
	/***************************************************************************
	 * Determine if the given resource is contained inside a valid project
	 * @param resource
	 * @return
	 **************************************************************************/
	public boolean hasSpellNature(IResource resource)
	{
		boolean result = false;
		
		try {
			IProject project =  resource.getProject();
			result = !project.isOpen();
			if (!result)
			{
				result = project.hasNature(SpellNature.SPELL_NATURE_ID); 
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
