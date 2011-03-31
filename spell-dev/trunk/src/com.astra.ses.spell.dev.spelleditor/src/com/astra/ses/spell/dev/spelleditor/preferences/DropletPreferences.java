///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.preferences
// 
// FILE      : DropletPreferences.java
//
// DATE      : 2008-11-21 08:55
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.spelleditor.preferences;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.astra.ses.spell.dev.spelleditor.Activator;
import com.astra.ses.spell.dev.spelleditor.dnd.droplet.IOfflineDatabaseDroplet;

public class DropletPreferences extends AbstractPreferenceInitializer {

	/** Droplets map */
	private static final IOfflineDatabaseDroplet[] DROPLETS;
	/** Preferences */
	private static final ScopedPreferenceStore PREFERENCES;
	
	static
	{
		IScopeContext confScope = new ConfigurationScope();
		DROPLETS = loadDroplets();
		PREFERENCES = new ScopedPreferenceStore(confScope, Activator.PLUGIN_ID);
		PREFERENCES.setSearchContexts(new IScopeContext[]{
			confScope,
			new InstanceScope()
		});
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public DropletPreferences() {
	}

	@Override
	public void initializeDefaultPreferences() {
		for (IOfflineDatabaseDroplet droplet : DROPLETS)
		{
			PREFERENCES.setDefault(droplet.getPreferenceId(), droplet.getDefaultDropletCode());
		}
	}
	
	/****************************************************************************
	 * Get the droplet code stored in preferences
	 * @param droplet
	 * @return
	 ***************************************************************************/
	public String getDropletCode(IOfflineDatabaseDroplet droplet)
	{
		return PREFERENCES.getString(droplet.getPreferenceId());
	}
	
	/****************************************************************************
	 * Store the given code for the given droplet in preferences
	 * @param droplet
	 * @param code
	 * @return
	 ***************************************************************************/
	public void setDropletCode(IOfflineDatabaseDroplet droplet, String code)
	{
		PREFERENCES.putValue(droplet.getPreferenceId(), code);
		try {
			PREFERENCES.save();
		} catch (IOException e) {
		}
	}
	
	/****************************************************************************
	 * Return available droplets
	 * @return
	 ***************************************************************************/
	public static IOfflineDatabaseDroplet[] getDroplets()
	{
		return DROPLETS;
	}

	/***************************************************************************
	 * Load the droplets for creating the menu
	 * @return
	 **************************************************************************/
	private static IOfflineDatabaseDroplet[] loadDroplets()
	{
		System.out.println("[*] Loading extensions for point '" + IOfflineDatabaseDroplet.DROPLET_EXTENSION_ID + "'");
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(IOfflineDatabaseDroplet.DROPLET_EXTENSION_ID);
		IExtension[] extensions = ep.getExtensions();
		ArrayList<IOfflineDatabaseDroplet> result = new ArrayList<IOfflineDatabaseDroplet>();
		for(IExtension extension : extensions)
		{
			System.out.println("[*] Extension ID: "+ extension.getUniqueIdentifier());
			// Obtain the configuration element for this extension point
			IConfigurationElement[] confElems = extension.getConfigurationElements();
			for (IConfigurationElement cfgElem : confElems)
			{
				try
				{
					IOfflineDatabaseDroplet extensionInterface = 
						(IOfflineDatabaseDroplet) IOfflineDatabaseDroplet.class.cast(cfgElem.createExecutableExtension(IOfflineDatabaseDroplet.ELEMENT_CLASS));
					result.add(extensionInterface);
				}
				catch(CoreException ex)
				{
					ex.printStackTrace();
				}
			}
		}
		IOfflineDatabaseDroplet[] droplets = new IOfflineDatabaseDroplet[result.size()];
		result.toArray(droplets);
		return droplets;
	}
}
