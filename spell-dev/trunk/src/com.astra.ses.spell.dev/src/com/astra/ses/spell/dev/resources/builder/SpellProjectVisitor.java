///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.project.nature
// 
// FILE      : SpellProjectVisitor.java
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
package com.astra.ses.spell.dev.resources.builder;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.astra.ses.spell.dev.config.ConfigurationManager;
import com.astra.ses.spell.dev.resources.nature.SpellNature;


public class SpellProjectVisitor implements IResourceVisitor {

	/** File name which identifies a package */
	private static final String PACKAGE_FILE = "__init__.py";
	
	@Override
	public boolean visit(IResource resource) throws CoreException {
		switch (resource.getType())
		{
			case IResource.ROOT: System.out.println("Visit root " + resource.getName());break;
			case IResource.PROJECT: 
				IProject project = (IProject) resource;
				if (project.hasNature(SpellNature.SPELL_NATURE_ID))
				{
					SpellNature nature = (SpellNature) project.getNature(SpellNature.SPELL_NATURE_ID);
					if (nature.getCreateFolderStructure())
					{
						Element projectStructure = ConfigurationManager.getInstance().getProjectStructure();
						constructProjectStructure(project, projectStructure);
					}
				}
				break;
			case IResource.FOLDER: System.out.println("Visit folder " + resource.getName());break;
			case IResource.FILE: System.out.println("Visit file " + resource.getName());break;
		default: break;
		}
		return false;
	}

	/**************************************************************************
	 * Create project structure for the given project
	 *************************************************************************/
	private void constructProjectStructure(IProject project, Element projectStructure)
	{
		NodeList childs = projectStructure.getChildNodes();
		for (int i=0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = child.getNodeName();
				if (nodeName.equals(ConfigurationManager.ELEMENT_FOLDER))
				{
					constructFolder(project, child);
				}
			}
		}
	}
	
	/**************************************************************************
	 * Construct the folder node and process its children
	 * @param parent
	 * @param folderNode
	 *************************************************************************/
	private void constructFolder(IContainer parent, Node folderNode)
	{
		NamedNodeMap map = folderNode.getAttributes();
		String folderName = map.getNamedItem("name").getNodeValue();

		String separator = "/";
		String childPath = separator + folderName;
		
		IFolder child = parent.getFolder(new Path(childPath));
		if (!child.exists())
		{
			try {
				child.create(true, true, null);
				NodeList childs = folderNode.getChildNodes();
				for (int i=0; i < childs.getLength(); i++)
				{
					Node node = childs.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE)
					{
						String nodeName = node.getNodeName();
						if (nodeName.equals(ConfigurationManager.ELEMENT_FOLDER))
						{
							constructFolder(child, node);
						}
						else if (nodeName.equals(ConfigurationManager.ELEMENT_PACKAGE))
						{
							constructPackage(child, node);
						}
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**************************************************************************
	 * Construct the package inside the given container
	 * @param sourceFolder
	 * @throws CoreException 
	 *************************************************************************/
	private void constructPackage(IContainer parent, Node packageNode) throws CoreException
	{
		String packageName = packageNode.getTextContent();
		
        String[] packageParts = packageName.split("\\.");
        for (String packagePart : packageParts) {
            IFolder folder = parent.getFolder(new Path(packagePart));
            if(!folder.exists()){
                folder.create(true, true, null);
                //Create a __init__.py file so that the newly created folder
                //becomes a package
                IFile packageFile = folder.getFile(PACKAGE_FILE);
                InputStream initial = new InputStream() {	
					@Override
					public int read() throws IOException {
						return -1;
					}
				};	
                packageFile.create(initial, false, null);
            }
        }
	}
}
