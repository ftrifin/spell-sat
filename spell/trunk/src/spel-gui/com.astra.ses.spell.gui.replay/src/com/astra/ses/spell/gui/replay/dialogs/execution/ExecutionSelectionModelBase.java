///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.dialogs.execution
// 
// FILE      : ExecutionSelectionModelBase.java
//
// DATE      : Jul 2, 2013
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.replay.dialogs.execution;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;

public abstract class ExecutionSelectionModelBase implements IExecutionSelectionModel
{
	protected static final DateFormat s_df = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
	protected static final DateFormat s_df2 = new SimpleDateFormat("MMMMM");
	protected static final DateFormat s_df3 = new SimpleDateFormat("EEEEE dd");
	/** Reference to the procedure manager */
	protected static IProcedureManager s_pmgr = null;
	/** Holds the parent directory of ASRUN files */
	protected String m_asrunPath;
	/** Root of the tree model */
	protected ExecutionSelectionNode m_root;

	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutionSelectionModelBase( String asrunPath )
	{
		m_root = null;
		m_asrunPath = asrunPath;
		if (s_pmgr == null)
		{
			s_pmgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	protected Calendar createYear( int year )
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH,1);
		c.set(Calendar.HOUR,0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
		return c;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected Calendar createMonth( int year, int month )
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH,1);
		c.set(Calendar.HOUR,0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
		return c;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	protected Calendar createDay( int year, int month, int day )
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH,day);
		c.set(Calendar.HOUR,0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND,0);
		return c;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ExecutionSelectionNode getRoot()
	{
		return m_root;
	}
}
