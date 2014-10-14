///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.jobs
// 
// FILE      : ExportLogFileJob.java
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
package com.astra.ses.spell.gui.presentation.text.export;

import java.io.FileOutputStream;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.astra.ses.spell.gui.presentation.text.TextPresentation;
import com.astra.ses.spell.gui.interfaces.IProcedureView;
import com.astra.ses.spell.gui.services.IViewManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ExportTextJob implements IRunnableWithProgress
{
	public CommandResult result;
	private IProcedure   m_proc;
	private String       m_destinationFileName;
	private String[]     m_displayLines;

	public ExportTextJob( IProcedure proc, String destinationFile)
	{
		m_proc = proc;
		m_destinationFileName = destinationFile;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
	        InterruptedException
	{
		try
		{
			monitor.setTaskName("Retrieving Text view file for procedure " + m_proc.getProcName());
			
			IViewManager viewMgr = (IViewManager) ServiceManager.get(IViewManager.class);
			IProcedureView procView = viewMgr.getProcedureView(m_proc.getProcId());
			TextPresentation textPres = (TextPresentation) procView.getPresentation("com.astra.ses.spell.gui.presentation.TextView");
			m_displayLines = textPres.getDisplayTextLines();
			
			monitor.beginTask("Exporting Text view data", m_displayLines.length);
			
			PrintWriter writer = new PrintWriter( new OutputStreamWriter( new FileOutputStream(m_destinationFileName)));
			
			for(String line : m_displayLines)
			{
				writer.println( line );
				monitor.worked(1);
				if (monitor.isCanceled()) break;
			}
			writer.close();
			monitor.done();
			result = CommandResult.SUCCESS;
		}
		catch (Exception e)
		{
			Logger.error("Could retrieve Text view:" + e.getLocalizedMessage(),Level.PROC, this);
		}
		monitor.done();
	}

}
