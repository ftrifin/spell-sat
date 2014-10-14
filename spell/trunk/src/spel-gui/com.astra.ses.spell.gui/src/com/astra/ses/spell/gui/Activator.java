///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui
// 
// FILE      : Activator.java
//
// DATE      : 2008-11-21 08:55
//
// Copyright (C) 2008, 2014 SES ENGINEERING, Luxembourg S.A.R.L.
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
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVclassED
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
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCclassENTAL,
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
package com.astra.ses.spell.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.extensions.GuiNotifications;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IViewManager;
import com.astra.ses.spell.gui.services.RuntimeSettingsService;
import com.astra.ses.spell.gui.services.ViewManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{

	// The plug-in class
	public static final String	PLUGIN_ID = "com.astra.ses.spell.gui";

	// The shared instance
	private static Activator	plugin;

	/***************************************************************************
	 * The constructor
	 **************************************************************************/
	public Activator()
	{
	}

	/* *************************************************************************
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 * ************************************************************************
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
		System.out.println("[*] Activated: " + PLUGIN_ID);
		createServices();
		connectServices();
		setupServices();
	}

	/* *************************************************************************
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 * ************************************************************************
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		clearServices();
	}

	/***************************************************************************
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 **************************************************************************/
	public static Activator getDefault()
	{
		return plugin;
	}

	/***************************************************************************
	 * Create the services provided by this plugin
	 **************************************************************************/
	private void createServices()
	{
		try
		{
			ServiceManager.registerService(IViewManager.class, new ViewManager());
			ServiceManager.registerService(IRuntimeSettings.class, new RuntimeSettingsService());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * Setup the services
	 **************************************************************************/
	private void setupServices()
	{
		ServiceManager.get(IViewManager.class).setup();
		ServiceManager.get(IRuntimeSettings.class).setup();
	}

	/***************************************************************************
	 * Connect the services
	 **************************************************************************/
	private void connectServices()
	{
		ServiceManager.get(IViewManager.class).subscribe();
		ServiceManager.get(IRuntimeSettings.class).subscribe();
		GuiNotifications.get().subscribe();
	}

	/***************************************************************************
	 * Clear the services
	 **************************************************************************/
	private void clearServices()
	{
		ServiceManager.get(IViewManager.class).cleanup();
		ServiceManager.get(IRuntimeSettings.class).cleanup();
		GuiNotifications.get().unsubscribe();
	}

	/***************************************************************************
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 **************************************************************************/
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
