///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.ui.handlers
//
// FILE      : PerformCheckCommand.java
//
// DATE      : Feb 7, 2011
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
package com.astra.ses.spell.dev.scheck.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.astra.ses.spell.dev.scheck.ui.jobs.PerformCheckJob;
import com.astra.ses.spell.dev.spelleditor.utils.SpellEditorInfo;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class PerformCheckCommand extends AbstractHandler 
{
	/**
	 * The constructor.
	 */
	public PerformCheckCommand() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelectionService sservice = window.getSelectionService();
		ISelection selection = sservice.getSelection();

		List<IResource> items = null;
		// The selection may be a structured selection or text selected in an editor part
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection sselection = (IStructuredSelection) selection;
			items = (List<IResource>) sselection.toList();
		}
		else if ((selection instanceof ITextSelection))
		{
			IEditorPart editor = window.getActivePage().getActiveEditor();
			// If an editor is selected
			if (editor != null)
			{
				SpellEditorInfo info = (SpellEditorInfo) editor.getAdapter(SpellEditorInfo.class);
				// If the editor could adapt
				if (info != null)
				{
					items = new ArrayList<IResource>();
					items.add( info.getFile() );
				}
			}
		}
		
		// If we have items to process
		if (items.size()>0)
		{
			PerformCheckJob job = new PerformCheckJob( items );
			JobHelper.executeJob(job, true, true);
			String message = "";
			if (job.numProcessedItems>0)
			{
				message = "Checked " + job.numProcessedItems + " files";
				if (job.notProcessed.size()>0)
				{
					message += "\n\nThe following files have not been processed:\n\n";
					for (String reason : job.notProcessed)
					{
						message += "     " + reason + "\n";
					}
				}
			}
			else
			{
				message = "No check was carried out";
				if (job.notProcessed.size()>0)
				{
					message += "\n\nThe following files have not been processed:\n\n";
					for (String reason : job.notProcessed)
					{
						message += "     " + reason + "\n";
					}
				}
			}
			MessageDialog.openInformation( window.getShell(), "Semantic check", message);
		}
		return null;
	}
}
