///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.jobs
// 
// FILE      : ConnectServerJob.java
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
package com.astra.ses.spell.gui.model.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.exceptions.ServerError;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.services.ServerProxy;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.dialogs.StringDialog;
import com.astra.ses.spell.gui.model.IConfig;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.services.ConfigurationManager;


public class ConnectServerJob implements IRunnableWithProgress
{
	protected IWorkbenchWindow m_window;
	public CommandResult result;
	
	public ConnectServerJob()
	{
		m_window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		result = CommandResult.FAILED;
	}
	
    public void run(IProgressMonitor monitor) 
    {
		ServerProxy proxy = (ServerProxy) ServiceManager.get(ServerProxy.ID);
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		if (proxy.isConnected())
		{
			MessageDialog.openWarning(m_window.getShell(), "Connect to server", 
					"Already connected to server");
			result = CommandResult.NO_EFFECT;
		}
		else
		{
	    	try 
	    	{
				ServerInfo info = (ServerInfo) cfg.getSelection(IConfig.ID_SERVER_SELECTION);
				if (info != null)
				{
					monitor.setTaskName("Connecting to server");
					if (info.getUser()!=null)
					{
						// Ask the user for password
						StringDialog dialog = new StringDialog(m_window.getShell(), 
								"Tunneled connection to " + info.getName(), 
								"Access to this server requires secure access",
								"Access to this server requires secure access\n" +
								"please enter password for user '" + info.getUser() + "'", true);
						int dresult = dialog.open();
						if ( dresult == StringDialog.CANCEL )
						{
							MessageDialog.openError(m_window.getShell(), "Connect to server", 
							"Cannot connect to server without password");
							result = CommandResult.FAILED;
							monitor.done();
							return;
						}
						String password = dialog.getAnswer();
						info.setPwd(password);
					}
					proxy.changeServer(info);
					proxy.connect();
		    		result = CommandResult.SUCCESS;
				}
				else
				{
					MessageDialog.openWarning(m_window.getShell(), "Connect to server", 
											  "No server selected");
					result = CommandResult.NO_EFFECT;
				}
	    	}
	    	catch(ServerError err)
	    	{
				MessageDialog.openError(m_window.getShell(), "Connect to server", 
						"Cannot connect to server:\n\n" + err.getLocalizedMessage());
				result = CommandResult.FAILED;
	    	}
	    	catch (Exception e) 
	    	{
				e.printStackTrace();
				result = CommandResult.FAILED;
			}
		}
		monitor.done();
    }
}
