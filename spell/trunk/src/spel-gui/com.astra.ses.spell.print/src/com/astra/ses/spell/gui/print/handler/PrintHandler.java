///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.handler
// 
// FILE      : PrintHandler.java
//
// DATE      : 2008-11-21 13:54
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.gui.print.handler;

import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.astra.ses.spell.gui.print.dialog.PrintDialog;

/*******************************************************************************
 * PrintHandler class is responsible of getting user printing properties and
 * launching the printing job
 */
public class PrintHandler extends AbstractHandler implements IHandler
{

	private static final String	PROVIDER_ARGUMENT	= "com.astra.ses.spell.gui.print.printableProvider";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		/*
		 * First get printing options defined by the user at runtime
		 */
		final PrinterJob pj = PrinterJob.getPrinterJob();

		final PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

		PrintDialog dialog = new PrintDialog(new Shell(),
		        PrinterJob.lookupPrintServices(), aset);

		if (dialog.open() == Window.CANCEL) { return null; }

		try
		{
			pj.setPrintService(dialog.getPrintService());
		}
		catch (PrinterException e1)
		{
			return null;
		}

		/*
		 * Get printable part
		 */
		String printableProviderCommand = event.getParameter(PROVIDER_ARGUMENT);
		ICommandService commandService = (ICommandService) PlatformUI
		        .getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand(printableProviderCommand);

		Parameterization[] params = null;
		IParameter[] commandParameters = null;
		try
		{
			commandParameters = command.getParameters();
		}
		catch (NotDefinedException e2)
		{
			e2.printStackTrace();
		}
		if (commandParameters != null)
		{
			params = new Parameterization[commandParameters.length];
			int i = 0;
			for (Object key : event.getParameters().keySet())
			{
				try
				{
					IParameter param = command.getParameter(key.toString());
					if (param != null)
					{
						Parameterization parm = new Parameterization(param,
						        event.getParameter(key.toString()));
						params[i] = parm;
						i++;
					}
				}
				catch (NotDefinedException e)
				{
					// Continue
				}
			}
		}
		final IHandlerService handlerService = (IHandlerService) PlatformUI
		        .getWorkbench().getService(IHandlerService.class);
		final ParameterizedCommand paramCommand = new ParameterizedCommand(
		        command, params);
		Display disp = Display.getCurrent();
		if (disp != null)
		{
			Runnable printJob = new Runnable()
			{
				public void run()
				{
					try
					{
						Object result = handlerService.executeCommand(
						        paramCommand, null);
						Printable printable = (Printable) result;
						pj.setPrintable(printable);
						pj.print(aset);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			};
			disp.asyncExec(printJob);
		}
		else
		{
			try
			{
				Object result = handlerService.executeCommand(paramCommand,
				        null);
				Printable printable = (Printable) result;
				pj.setPrintable(printable);
				pj.print();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}

}
