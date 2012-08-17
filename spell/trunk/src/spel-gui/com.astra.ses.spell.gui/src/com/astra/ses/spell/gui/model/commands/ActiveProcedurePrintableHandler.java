///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : ActiveProcedurePrintableHandler.java
//
// DATE      : 2008-11-21 08:55
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.model.commands;

import java.awt.print.Printable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.interfaces.IProcedurePresentation;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.ViewManager;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;
import com.astra.ses.spell.gui.views.ProcedureView;

/*******************************************************************************
 * ActiveProcedurePrintable handler will provide an IPrintable object from the
 * active procedure. The kind of printable returned depends on the mode provided
 ******************************************************************************/
public class ActiveProcedurePrintableHandler extends AbstractHandler implements
        IHandler
{

	/** Part argument */
	private static final String	MODE_ARG	= "com.astra.ses.spell.gui.commands.procPrintable.mode";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		String mode = event.getParameter(MODE_ARG);
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager
		        .get(IRuntimeSettings.class);
		String selectedProcedure = runtime.getRuntimeProperty(
		        RuntimeProperty.ID_PROCEDURE_SELECTION).toString();
		ViewManager vm = (ViewManager) ServiceManager.get(ViewManager.class);
		ProcedureView procView = vm.getProcView(selectedProcedure);
		IProcedurePresentation presentation = procView.getPresentation(mode);
		Printable printable = (Printable) presentation
		        .getAdapter(Printable.class);
		return printable;
	}
}
