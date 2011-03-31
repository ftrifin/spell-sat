///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.shell.commands
// 
// FILE      : LoadProcedure.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.shell.commands;

import java.util.Vector;

import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.core.shell.services.ShellManager;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;


/*******************************************************************************
 * Command to load a procedure
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class LoadProcedure extends ShellCommand
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	LoadProcedure( ShellManager mgr )
	{
		super(mgr);
	}

	/***************************************************************************
	 * Execute the command
	 **************************************************************************/
	@Override
	public void execute(Vector<String> args) throws CommandFailed
	{
		if (args==null || args.size()!=1)
		{
			throw new CommandFailed("Expected arguments: procedure identifier");
		}
		try
		{
			m_args = args;
			ProcedureManager pmgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
			if (!pmgr.canOperate())
			{
				throw new CommandFailed("Cannot open procedures, check connection with server");
			}
			String procId = args.get(0);
			
			if (!pmgr.getAvailableProcedures().containsKey(procId))
			{
				throw new CommandFailed("No such procedure: " + procId);
			}
			
			display("Loading procedure " + procId, Severity.INFO);
			pmgr.openProcedure(procId,null);
			display("Procedure loaded.");
		}
		catch(CommandFailed ex)
		{
			throw ex;
		}
		catch(Exception ex)
		{
			throw new CommandFailed(ex.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Get command string
	 **************************************************************************/
	@Override
	public String getCmdString()
	{
		return "load";
	}

	/***************************************************************************
	 * Get command help
	 **************************************************************************/
	@Override
	public String getHelp()
	{
		return "load the given procedure";
	}
	
	/***************************************************************************
	 * Get command syntax
	 **************************************************************************/
	@Override
	public String getSyntax()
	{
		return "load <procedure identifier>";
	}
}
