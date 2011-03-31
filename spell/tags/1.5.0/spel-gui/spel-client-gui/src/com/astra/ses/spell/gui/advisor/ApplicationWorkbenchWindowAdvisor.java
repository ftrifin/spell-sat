///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.advisor
// 
// FILE      : ApplicationWorkbenchWindowAdvisor.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.advisor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.eclipse.jface.dialogs.MessageDialog;
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
import com.astra.ses.spell.gui.core.model.types.ContextStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServerProxy;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.model.IConfig;
import com.astra.ses.spell.gui.model.commands.AttachContext;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.DetachContext;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.model.jobs.ConnectServerJob;
import com.astra.ses.spell.gui.model.jobs.StartContextJob;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.services.ConfigurationManager;
import com.astra.ses.spell.gui.services.ViewManager;
import com.astra.ses.spell.gui.views.MasterView;


public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the release version */
	private static String RELEASE;
	/** Holds the name of SPELL release information file */
	private static final String NFO_FILE = "/release.nfo";
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
			// Obtain the resource manager to get the file URL
			ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);

			String home = cfg.getHome();
			if (home != null)
			{
				String rev = "";
				String ver = "";
				String dat = "";
				String nfo = home + cfg.getPathSeparator() + NFO_FILE;
				
				// Read the file contents
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(nfo)));
				// Obtain the release version number
				String line = reader.readLine();
				while(line != null)
				{
					if (line.toLowerCase().indexOf("revision")!=-1)
					{
						rev = line.substring(line.indexOf(":")+1).trim();
					}
					else if (line.toLowerCase().indexOf("version")!=-1)
					{
						ver = line.substring(line.indexOf(":")+1).trim();
					}
					else if (line.toLowerCase().indexOf("generation")!=-1)
					{
						dat = line.substring(line.indexOf(":")+1).trim();
					}
					line = reader.readLine();
				}
				RELEASE = ver + " (" + rev + ") - " + dat;
			}
			else
			{
				RELEASE = "[Unknown release]";
			}
		}
		catch (Exception e)
		{
			RELEASE = "[Unknown release]";
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
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

	/***************************************************************************
	 * Create the action bar advisor.
	 * 
	 * @param configurer
	 *            Action bar configurer.
	 * @return The action bar advisor.
	 **************************************************************************/
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
	/***************************************************************************
	 * Called just before the workbench window is open. Window initialization is
	 * done here.
	 **************************************************************************/
    public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		Logger.debug("Using release number: " + RELEASE, Level.INIT, this);
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		String appName = cfg.getProperty(IConfig.PROPERTY_APPNAME);
		configurer.setTitle( appName + " Procedure Executor - " + RELEASE);
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
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow(); 
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
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(viewMgr);
	}

	/***************************************************************************
	 * Called just before the workbench window is to be closed.
	 * Returning false prevents the window to be closed
	 **************************************************************************/
	public boolean preWindowShellClose()
	{
		Logger.debug("Closing workbench", Level.INIT, this);
		IWorkbenchWindow window = getWindowConfigurer().getWindow();
		ProcedureManager mgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
		ServerProxy sproxy = (ServerProxy) ServiceManager.get(ServerProxy.ID);
		ContextProxy cproxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);

		if (cproxy.isConnected() && mgr.getOpenLocalProcedures().size() > 0)
		{
			MessageDialog.openWarning(window.getShell(), "Shutdown",
				"Cannot shutdown client.\n\n" + "There are open procedures");
			return false;
		}
	
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
		// If configured to do so, select now the initial configuration
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		ServerProxy proxy = (ServerProxy) ServiceManager.get(ServerProxy.ID);
		
		String serverID = cfg.getProperty(IConfig.PROPERTY_INITIALSERVER);
		String ctxName = cfg.getProperty(IConfig.PROPERTY_AUTOCONFIG);
		String auto = cfg.getProperty(IConfig.PROPERTY_AUTOCONNECT);
		
		boolean autoConnect = (auto != null) && auto.toUpperCase().equals(IConfig.PROPERTY_VALUE_YES);
		boolean autoConfig = (ctxName != null) && !ctxName.toUpperCase().equals(IConfig.PROPERTY_VALUE_NONE);
		CommandResult result = CommandResult.FAILED;
		try
		{
			if (autoConnect)
			{
				ServerInfo info = cfg.getServerData(serverID);
				if (info == null)
				{
					Logger.error("Autoconnect failed", Level.PROC, "GUI");
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "Autoconnect", "Autoconnect failed:\n\nUnkown server Id in configuration" );
					return;
				}
				cfg.setSelection(IConfig.ID_SERVER_SELECTION, info);
				ConnectServerJob job = new ConnectServerJob();
				CommandHelper.executeInProgress(job,true,false);
				result = job.result;
			}
			if (autoConfig && result == CommandResult.SUCCESS)
			{
				Logger.info("Setting context automatically to " + ctxName, Level.INIT, "GUI");
				// Will fail with exception if the context does not exist
				ContextInfo ctxInfo = proxy.getContextInfo(ctxName);
				cfg.setSelection(IConfig.ID_CONTEXT_SELECTION, ctxInfo);
				if (!ctxInfo.isRunning())
				{
					Logger.info("Starting context " + ctxName, Level.INIT, "GUI");
					StartContextJob job = new StartContextJob();
					CommandHelper.executeInProgress(job,true,false);
					result = job.result;
				}
				if (result == CommandResult.SUCCESS)
				{
					ctxInfo.setStatus(ContextStatus.RUNNING);
					cfg.setSelection(IConfig.ID_CONTEXT_SELECTION, ctxInfo);
					CommandHelper.execute(AttachContext.ID);
				}
			}
		}
		catch (Exception e)
		{
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Autoconnect", "Autoconnect failed:\n\n" + e.getLocalizedMessage() );
		}
	}
}
