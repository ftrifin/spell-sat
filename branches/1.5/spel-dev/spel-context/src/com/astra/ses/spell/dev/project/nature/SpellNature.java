///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.project.nature
// 
// FILE      : SpellNature.java
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
package com.astra.ses.spell.dev.project.nature;

import java.util.HashMap;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/******************************************************************************
 * Class for adding Spell nature to IProject resources
 * @author jpizar
 *****************************************************************************/
public class SpellNature implements IProjectNature {

	/***************************************************************************
	 * Properties that can be defined in the SPELL Nature
	 * @author jpizar
	 **************************************************************************/
	public enum SpellNatureBuildProperties
	{
		PROJECT_STRUCTURE("STRUCTURE", Boolean.TRUE);
		
		/** KEY */
		private String m_key;
		/** VALUE */
		private Object m_value;
		
		/***********************************************************************
		 * Constructor
		 * @param key
		 * @param defaultValue
		 **********************************************************************/
		private SpellNatureBuildProperties(String key, Object defaultValue)
		{
			m_key = key;
			m_value = defaultValue;
		}
		
		/***********************************************************************
		 * Get the key
		 * @return
		 **********************************************************************/
		public String getKey()
		{
			return m_key;
		}
		
		/***********************************************************************
		 * Get the defualt value for this property
		 * @return
		 **********************************************************************/
		public Object getDefaultValue()
		{
			return m_value;
		}
	}
	
	/** Nature ID */
	public static final String SPELL_NATURE_ID = "com.astra.ses.spell.dev.SpellNature";
	/** Current project */
	private IProject m_projectHandler;
	
	/**************************************************************************
	 * Add the Spell Nature to this project
	 * @param project
	 * @param gcsDriver
	 *************************************************************************/
	public static void addNature(IProject project, IProgressMonitor monitor,
			boolean createProjectStructure)
	{
		// TODO This must be taken out from this class
		/*
		 * A good solution for this TODO would be to create a Nature Manager
		 * which manages both Spell Nature and Python Nature (pydev)
		 * but previous to this there is need to understand the PyDev nature
		 * crappy code
		 */
        IProjectDescription desc;
		try {
			desc = project.getDescription();
			ICommand[] commands = desc.getBuildSpec();
	        //only add the nature if it still hasn't been added.
	        if (!project.hasNature(SPELL_NATURE_ID)) 
	        {
	            String[] natures = desc.getNatureIds();
	            String[] newNatures = new String[natures.length + 1];
	            System.arraycopy(natures, 0, newNatures, 0, natures.length);
	            newNatures[natures.length] = SPELL_NATURE_ID;
	            desc.setNatureIds(newNatures);
	            
	            //add builder to project
	            ICommand command = desc.newCommand();
	            command.setBuilderName(SpellProjectBuilder.BUILDER_ID);
	            HashMap<String, Object> args = new HashMap<String, Object>();
	            args.put(SpellNatureBuildProperties.PROJECT_STRUCTURE.m_key, createProjectStructure);
	            command.setArguments(args);
	            ICommand[] newCommands = new ICommand[commands.length + 1];
	            
	            // Add it before other builders.
	            System.arraycopy(commands, 0, newCommands, 1, commands.length);
	            newCommands[0] = command;
	            desc.setBuildSpec(newCommands);
	            
	            //Set description to the project
	            project.setDescription(desc, monitor);          
	        }
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/***************************************************************************
	 * Try to repair incomplete SPELL projects description
	 * @param project
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	public static void restoreProjectNature(IProject project)
	{
		try {
			IProjectDescription desc = project.getDescription();
			ICommand[] commands = desc.getBuildSpec();
			/*
			 * Search for the SPELL Builder command and restore it
			 */
			for (ICommand command : commands)
			{
				if (command.getBuilderName().equals(SpellProjectBuilder.BUILDER_ID))
				{
					HashMap<String, Object> args = new HashMap<String, Object>(command.getArguments());
					for (SpellNatureBuildProperties prop : SpellNatureBuildProperties.values())
					{
						/*
						 * For any missing property we restore it with its default value
						 */
						if (!args.containsKey(prop.getKey()))
						{
							System.err.println("Restoring property " + prop.getKey());
							args.put(prop.getKey(), prop.getDefaultValue());
						}
					}
					command.setArguments(args);
					desc.setBuildSpec(commands);
					project.setDescription(desc, new NullProgressMonitor());
					return;
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		/*
		 * If project does not have SPELL Neture, create it with default values
		 */
		addNature(project, null, true);
	}
	
	@Override
	public void configure() throws CoreException {}

	@Override
	public void deconfigure() throws CoreException {}

	@Override
	public IProject getProject() {
		return m_projectHandler;
	}

	@Override
	public void setProject(IProject project) {
		m_projectHandler = project;
	}
}
