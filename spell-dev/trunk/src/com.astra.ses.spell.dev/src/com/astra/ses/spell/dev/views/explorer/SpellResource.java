////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.views.explorer
// 
// FILE      : SpellResource.java
//
// DATE      : 2010-07-06
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.views.explorer;

import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.python.pydev.plugin.nature.PythonNature;

/*******************************************************************************
 * 
 * SpellResource wraps IResource objects giving extra information on its Python
 * project role. For example, it tells if it is a source folder, a package, ...
 *
 ******************************************************************************/
public enum SpellResource {
	
	PROJECT("images/spell16.gif", Category.PROJECT),
	SOURCE_FOLDER("images/resources/sourcefolder.gif", Category.SOURCE_FOLDER),
	PACKAGE("images/resources/package.gif", Category.PACKAGE),
	FOLDER(null, Category.FOLDER);
	
	/** Path to icon */
	public String icon_path;
	/** Resource category */
	public Category category;

	/***********************************************************************
	 * Constructor
	 * @param icon_path
	 **********************************************************************/
	private SpellResource(String icon_path, Category category)
	{
		this.icon_path = icon_path;
		this.category = category;
	}
	
	/***********************************************************************
	 * Determine which type of SPELL resource do we have
	 **********************************************************************/
	public static SpellResource valueOf(IResource resource)
	{
		SpellResource result = null;
		
		if (!resource.getProject().isAccessible())
		{
			return null;
		}
		
		switch (resource.getType())
		{
			case IResource.PROJECT:
				result = PROJECT;
				break;
			case IResource.FOLDER:
				IFolder folder = (IFolder) resource;
				// Get python project source folders
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
				// Check if the folder contains an __init__.py file
				boolean hasInit = folder.getFile("__init__.py").exists();
				if (isSourceFolder)
				{
					result = SOURCE_FOLDER;
				}
				else if (hasInit)
				{
					result = PACKAGE;
				}
				else
				{
					result = FOLDER;
				}
				break;
		}
		return result;
	}
}
