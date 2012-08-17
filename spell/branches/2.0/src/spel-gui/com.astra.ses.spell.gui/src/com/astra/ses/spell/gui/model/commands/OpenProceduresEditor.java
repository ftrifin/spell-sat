///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : OpenProceduresEditor.java
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
package com.astra.ses.spell.gui.model.commands;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;

/*******************************************************************************
 * @brief Command for opening the procedures editor
 * @date 09/10/07
 ******************************************************************************/
public class OpenProceduresEditor extends AbstractHandler
{
	public static final String	ID	         = "com.astra.ses.spell.gui.commands.OpenProceduresEditor";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private Process	           editorProcess	= null;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESIBLE METHODS
	// =========================================================================

	public CommandResult execute(ExecutionEvent event)
	        throws ExecutionException
	{
		boolean openEditor = true;
		CommandResult result = CommandResult.SUCCESS;
		if (editorProcess != null)
		{
			try
			{
				// Check whether a previous instance is dead
				editorProcess.exitValue();
			}
			catch (IllegalThreadStateException e)
			{
				openEditor = false;
				result = CommandResult.NO_EFFECT;
			}
		}
		// Open editor when it has not been opened previously or it has been
		// killed
		if (openEditor)
		{
			try
			{
				// Launch the procedures editor
				ConfigurationManager cfg = (ConfigurationManager) ServiceManager
				        .get(ConfigurationManager.ID);
				String editorBinFile = cfg
				        .getProperty(PropertyKey.PROCS_EDITOR);
				if (editorBinFile != null)
				{
					editorProcess = Runtime.getRuntime().exec(editorBinFile);
				}
				else
				{
					MessageDialog.openError(Display.getCurrent()
					        .getActiveShell(), "Procedure editor",
					        "No procedure editor defined in configuration");
					result = CommandResult.NO_EFFECT;
				}
			}
			catch (IOException exception)
			{
				Logger.warning("Error: cannot open the procedures editor",
				        Level.PROC, this);
			}
		}
		return result;
	}
}
