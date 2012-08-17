///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.actions
// 
// FILE      : ExecutorActions.java
//
// DATE      : 2010-08-26
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
package com.astra.ses.spell.gui.views.controls.actions;

import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.model.commands.CmdAbort;
import com.astra.ses.spell.gui.model.commands.CmdGoto;
import com.astra.ses.spell.gui.model.commands.CmdPause;
import com.astra.ses.spell.gui.model.commands.CmdRecover;
import com.astra.ses.spell.gui.model.commands.CmdReload;
import com.astra.ses.spell.gui.model.commands.CmdRun;
import com.astra.ses.spell.gui.model.commands.CmdSkip;
import com.astra.ses.spell.gui.model.commands.CmdStep;
import com.astra.ses.spell.gui.model.commands.CmdStepOver;

/*******************************************************************************
 * 
 * {@link GuiExecutorCommand} model how {@link ExecutorCommand} instances over
 * the procedure may be presented to the user
 * 
 ******************************************************************************/
public enum GuiExecutorCommand
{

	RUN(ExecutorCommand.RUN, CmdRun.ID, "Run", "Run the given procedure",
	        "icons/16x16/run.png"), STEP(ExecutorCommand.STEP, CmdStep.ID,
	        "Step", "Step into one statement", "icons/16x16/step.png"), STEP_OVER(
	        ExecutorCommand.STEP_OVER, CmdStepOver.ID, "Step Over",
	        "Step over one statement", "icons/16x16/step.png"), SKIP(
	        ExecutorCommand.SKIP, CmdSkip.ID, "Skip", "Skip one statement",
	        "icons/16x16/skip.png"), PAUSE(ExecutorCommand.PAUSE, CmdPause.ID,
	        "Pause", "Pause execution", "icons/16x16/pause.png"), GOTO(
	        ExecutorCommand.GOTO, CmdGoto.ID, "Goto",
	        "Goto a given label in the procedure", "icons/16x16/goto.png"), RELOAD(
	        ExecutorCommand.RELOAD, CmdReload.ID, "Reload", "Reload procedure",
	        "icons/16x16/reload.png"), ABORT(ExecutorCommand.ABORT,
	        CmdAbort.ID, "Abort", "Abort the execution",
	        "icons/16x16/abort.png"), RECOVER(ExecutorCommand.RECOVER,
	        CmdRecover.ID, "Recover", "Recover from failure",
	        "icons/16x16/reload.png");

	/** Command identifier */
	public ExecutorCommand	command;
	/** Eclipse command id */
	public String	       handler;
	/** Action label */
	public String	       label;
	/** Description */
	public String	       description;
	/** Image path */
	public String	       imagePath;

	/***************************************************************************
	 * 
	 * @param command
	 * @param label
	 * @param descritpion
	 * @param path
	 **************************************************************************/
	private GuiExecutorCommand(ExecutorCommand command, String handler,
	        String label, String description, String path)
	{
		this.command = command;
		this.handler = handler;
		this.label = label;
		this.description = description;
		this.imagePath = path;
	}
}
