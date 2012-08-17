///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.commands
// 
// FILE      : StopWatchAllVariables.java
//
// DATE      : 2010-09-02
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
package com.astra.ses.spell.gui.watchvariables.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.watchvariables.commands.args.IWatchCommandArgument;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariables;

/*******************************************************************************
 * 
 * {@link StopWatchAllVariables}
 * 
 ******************************************************************************/
public class StopWatchAllVariables extends AbstractHandler
{

	public static final String	ID	= "com.astra.ses.spell.gui.commands.StopWatchAllVariables";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		/*
		 * Retrieve the arguments
		 */
		String procId = event.getParameter(IWatchCommandArgument.PROCEDURE_ID);
		/*
		 * Get the procedure
		 */
		IProcedureManager mgr = (IProcedureManager) ServiceManager
		        .get(IProcedureManager.class);
		IProcedure proc = mgr.getProcedure(procId);
		/*
		 * This command can only be executed while the procedure is paused
		 */
		ExecutorStatus st = proc.getDataProvider().getExecutorStatus();
		switch (st)
		{
		case PAUSED:
		case WAITING:
		case PROMPT:
		case ERROR:
		case ABORTED:
		case FINISHED:
		case INTERRUPTED:
		case RELOADING:
			/*
			 * Switch the view
			 */
			IWatchVariables watch = (IWatchVariables) ServiceManager.get(IWatchVariables.class);
			watch.getVariableManager(procId).clearAllWatches();
			return CommandResult.SUCCESS;
		default:
			return CommandResult.NO_EFFECT;
		}
	}

}
