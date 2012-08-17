///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.commands
// 
// FILE      : ToggleByStep.java
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ToggleByStep extends AbstractHandler
{
	public static final String	ID	     = "com.astra.ses.spell.gui.commands.ToggleByStep";
	public static final String	STATE_ID	= "org.eclipse.ui.commands.toggleState";

	/***************************************************************************
	 * The constructor.
	 **************************************************************************/
	public ToggleByStep()
	{
	}

	/***************************************************************************
	 * The command has been executed, so extract extract the needed information
	 * from the application context.
	 **************************************************************************/
	public CommandResult execute(ExecutionEvent event)
	        throws ExecutionException
	{
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager
		        .get(IRuntimeSettings.class);
		String procId = (String) runtime
		        .getRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION);
		IProcedureManager mgr = (IProcedureManager) ServiceManager
		        .get(IProcedureManager.class);
		/*
		 * Although IProcedure objects are not supposed to be updated through
		 * set methods, there is a way to change their status throught the visit
		 * methods, providing the configuration they should acquire
		 */
		IProcedure proc = mgr.getProcedure(procId);
		ExecutorConfig currentCfg = (ExecutorConfig) proc
		        .getAdapter(ExecutorConfig.class);
		boolean oldValue = currentCfg.getStepByStep();
		currentCfg.setStepByStep(!oldValue);
		proc.getController().setStepByStep(!oldValue);
		return CommandResult.SUCCESS;
	}
}
