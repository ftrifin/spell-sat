///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.advisor
// 
// FILE      : ApplicationWorkbenchWindowAdvisor.java
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.advisor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ContextStatus;
import com.astra.ses.spell.gui.core.model.types.ICoreConstants;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.ServerProxy;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.AttachContext;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.DetachContext;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.ConnectServerJob;
import com.astra.ses.spell.gui.model.jobs.StartContextJob;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.services.RuntimeSettingsService;
import com.astra.ses.spell.gui.services.RuntimeSettingsService.RuntimeProperty;
import com.astra.ses.spell.gui.services.ViewManager;
import com.astra.ses.spell.gui.views.MasterView;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
        implements IPropertyChangeListener
{

	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the release version */
	private static String	    RELEASE;
	/** Holds the name of SPELL release information file */
	private static final String	NFO_FILE	= "/release.nfo";
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # STATIC INITIALIZATION CODE
	// =========================================================================

	/***************************************************************************
	 * Initialize the release version data
	 **************************************************************************/
	static
	{
		try
		{
			// Home
			String home = System.getenv(ICoreConstants.CLIENT_HOME_ENV);
			if (home == null)
			{
				home = ".";
			}
			String rev = "";
			String ver = "";
			String dat = "";
			String nfo = home + File.separator + NFO_FILE;

			// Read the file contents
			BufferedReader reader = new BufferedReader(new InputStreamReader(
			        new FileInputStream(nfo)));
			// Obtain the release version number
			String line = reader.readLine();
			while (line != null)
			{
				if (line.toLowerCase().indexOf("revision") != -1)
				{
					rev = line.substring(line.indexOf(":") + 1).trim();
				}
				else if (line.toLowerCase().indexOf("version") != -1)
				{
					ver = line.substring(line.indexOf(":") + 1).trim();
				}
				else if (line.toLowerCase().indexOf("generation") != -1)
				{
					dat = line.substring(line.indexOf(":") + 1).trim();
				}
				line = reader.readLine();
			}
			RELEASE = ver + " (" + rev + ") - " + dat;
		}
		catch (Exception e)
		{
			RELEASE = "2.0";
		}
	}

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param configurer
	 *            Workbench window configurer.
	 **************************************************************************/
	public ApplicationWorkbenchWindowAdvisor(
	        IWorkbenchWindowConfigurer configurer)
	{
		super(configurer);
	}

	/***************************************************************************
	 * Create the action bar advisor.
	 * 
	 * @param configurer
	 *            Action bar configurer.
	 * @return The action bar advisor.
	 **************************************************************************/
	public ActionBarAdvisor createActionBarAdvisor(
	        IActionBarConfigurer configurer)
	{
		return new ApplicationActionBarAdvisor(configurer);
	}

	/***************************************************************************
	 * Called just before the workbench window is open. Window initialization is
	 * done here.
	 **************************************************************************/
	public void preWindowOpen()
	{
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		Logger.debug("Using release number: " + RELEASE, Level.INIT, this);
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		String appName = cfg.getProperty(PropertyKey.APPLICATION_NAME);
		configurer.setTitle(appName + " Procedure Executor - " + RELEASE);
		configurer.setInitialSize(new Point(1200, 800));
		configurer.setShowCoolBar(true);
		configurer.setShowMenuBar(true);
		configurer.setShowStatusLine(true);
	}

	/***************************************************************************
	 * Called just after the workbench window is open.
	 **************************************************************************/
	public void postWindowOpen()
	{
		// Show the master console as default. Showing this view must be
		// done this way in order to activate properly the log view.
		IWorkbenchWindow window = PlatformUI.getWorkbench()
		        .getActiveWorkbenchWindow();
		try
		{
			window.getActivePage().showView(MasterView.ID);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}

		// Register the view manager for monitoring view operations
		ViewManager viewMgr = (ViewManager) ServiceManager.get(ViewManager.ID);
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
		        .addPartListener(viewMgr);
	}

	/***************************************************************************
	 * Called just before the workbench window is to be closed. Returning false
	 * prevents the window to be closed
	 **************************************************************************/
	public boolean preWindowShellClose()
	{
		Logger.debug("Closing workbench", Level.INIT, this);
		ServerProxy sproxy = (ServerProxy) ServiceManager.get(ServerProxy.ID);
		ContextProxy cproxy = (ContextProxy) ServiceManager
		        .get(ContextProxy.ID);

		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		cfg.removePropertyChangeListener(this);

		// Obtain a handle to the server proxy
		CommandResult cresult = CommandResult.SUCCESS;
		if (sproxy.isConnected() && cproxy.isConnected())
		{
			cresult = CommandHelper.execute(DetachContext.ID);
		}
		return (cresult == CommandResult.SUCCESS);
	}

	public void postWindowCreate()
	{
		// Runtime settings service is required to store connection results for
		// further reconnections
		RuntimeSettingsService runtime = (RuntimeSettingsService) ServiceManager
		        .get(RuntimeSettingsService.ID);
		// If configured to do so, select now the initial configuration
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);

		// Initialize timeouts in services
		ServerProxy proxy = (ServerProxy) ServiceManager.get(ServerProxy.ID);
		ContextProxy cproxy = (ContextProxy) ServiceManager
		        .get(ContextProxy.ID);
		long openTimeout = Long.parseLong(cfg
		        .getProperty(PropertyKey.OPEN_TIMEOUT));
		cproxy.setOpenTimeout(openTimeout);
		long responseTimeout = Long.parseLong(cfg
		        .getProperty(PropertyKey.RESPONSE_TIMEOUT));
		cproxy.setResponseTimeout(responseTimeout);

		// Subscribr to further changes
		cfg.addPropertyChangeListener(this);

		String serverID = cfg.getProperty(PropertyKey.INITIAL_SERVER);
		String ctxName = cfg.getProperty(PropertyKey.INITIAL_CONTEXT);

		boolean autoConnect = cfg
		        .getBooleanProperty(PropertyKey.STARTUP_CONNECT);
		boolean autoConfig = (ctxName != null);
		CommandResult result = CommandResult.FAILED;
		try
		{
			if (autoConnect)
			{
				ServerInfo info = cfg.getServerData(serverID);
				if (info == null)
				{
					Logger.error("Autoconnect failed", Level.PROC, "GUI");
					MessageDialog
					        .openError(Display.getCurrent().getActiveShell(),
					                "Autoconnect",
					                "Autoconnect failed:\n\nUnkown server Id in configuration");
					return;
				}
				runtime.setRuntimeProperty(RuntimeProperty.ID_SERVER_SELECTION,
				        info);
				ConnectServerJob job = new ConnectServerJob();
				CommandHelper.executeInProgress(job, true, false);
				result = job.result;
			}
			if (autoConfig && result == CommandResult.SUCCESS)
			{
				Logger.info("Setting context automatically to " + ctxName,
				        Level.INIT, "GUI");
				// Will fail with exception if the context does not exist
				ContextInfo ctxInfo = proxy.getContextInfo(ctxName);
				runtime.setRuntimeProperty(
				        RuntimeProperty.ID_CONTEXT_SELECTION, ctxInfo);
				if (!ctxInfo.isRunning())
				{
					Logger.info("Starting context " + ctxName, Level.INIT,
					        "GUI");
					StartContextJob job = new StartContextJob();
					CommandHelper.executeInProgress(job, true, false);
					result = job.result;
				}
				if (result == CommandResult.SUCCESS)
				{
					ctxInfo.setStatus(ContextStatus.RUNNING);
					runtime.setRuntimeProperty(
					        RuntimeProperty.ID_CONTEXT_SELECTION, ctxInfo);
					CommandHelper.execute(AttachContext.ID);
				}
			}
		}
		catch (Exception e)
		{
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
			        "Autoconnect",
			        "Autoconnect failed:\n\n" + e.getLocalizedMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
	 * .jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.equals(PropertyKey.OPEN_TIMEOUT.getPreferenceName()))
		{
			ContextProxy cproxy = (ContextProxy) ServiceManager
			        .get(ContextProxy.ID);
			long openTimeout = Long.parseLong((String) event.getNewValue());
			cproxy.setOpenTimeout(openTimeout);
		}
		else if (property.equals(PropertyKey.RESPONSE_TIMEOUT
		        .getPreferenceName()))
		{
			ContextProxy cproxy = (ContextProxy) ServiceManager
			        .get(ContextProxy.ID);
			long responseTimeout = Long.parseLong((String) event.getNewValue());
			cproxy.setResponseTimeout(responseTimeout);
		}
	}
}
