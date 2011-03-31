///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.properties
// 
// FILE      : ProcedureCloseTester.java
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
package com.astra.ses.spell.gui.model.properties;

import org.eclipse.core.expressions.PropertyTester;

import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.model.IConfig;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.services.ConfigurationManager;


public class ProcedureCloseTester extends PropertyTester
{
	private static ConfigurationManager s_cfg = null;
	private static ProcedureManager 	s_proc = null;
	public static final String ID = "com.astra.ses.spell.gui.properties.ProcedureCloseable";

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue)
	{
		if (s_cfg==null)
		{
			s_cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		}
		if (s_proc==null)
		{
			s_proc = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
		}
		String procId = (String) s_cfg.getSelection(IConfig.ID_PROCEDURE_SELECTION);
		if (procId != null)
		{
			if (!s_proc.isLocallyLoaded(procId)) return false;
			ExecutorStatus st = ExecutorStatus.UNKNOWN;
			try
			{
				st = s_proc.getProcedure(procId).getStatus();
			}
			catch(NoSuchProcedure ex)
			{
				return false;
			}
			if (st != ExecutorStatus.RUNNING) return true;
		}
		return false;
	}
}
