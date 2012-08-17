////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : VariableManager.java
//
// DATE      : 2010-07-30
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.astra.ses.spell.gui.core.model.notification.ScopeNotification;
import com.astra.ses.spell.gui.core.model.notification.VariableData;
import com.astra.ses.spell.gui.core.model.notification.VariableNotification;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;
import com.astra.ses.spell.gui.procs.interfaces.model.IVariableManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IVariableWatchListener;

/*******************************************************************************
 * 
 * Variable manager
 * 
 ******************************************************************************/
public class VariableManager implements IVariableManager
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Context proxy */
	private static ContextProxy	              s_proxy	= null;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private String	                          m_procId;
	/** Holds the procedure identifier */
	private IProcedureDataProvider	          m_proc;
	/** Holds the list of listeners */
	private ArrayList<IVariableWatchListener>	m_listeners;
	/** Holds the list of registered variables */
	private Map<String, VariableData>	      m_registeredVars;

	/*
	 * Static block to retrieve the context proxy
	 */
	static
	{
		s_proxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public VariableManager(String procId, IProcedureDataProvider dataProvider)
	{
		m_proc = dataProvider;
		m_procId = procId;
		m_listeners = new ArrayList<IVariableWatchListener>();
		m_registeredVars = new TreeMap<String, VariableData>();
	}

	@Override
	public VariableData[] getGlobalVariables()
	{
		if (!checkValidStatus()) return null;

		VariableData[] list = null;
		try
		{
			list = s_proxy.getGlobalVariables(m_procId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return list;
	}

	@Override
	public VariableData[] getLocalVariables()
	{
		if (!checkValidStatus()) return null;

		VariableData[] list = null;
		try
		{
			list = s_proxy.getLocalVariables(m_procId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return list;
	}

	@Override
	public VariableData[] getAllVariables()
	{
		if (!checkValidStatus()) return null;

		VariableData[] list = null;
		try
		{
			list = s_proxy.getAllVariables(m_procId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return list;
	}

	@Override
	public VariableData[] getRegisteredGlobalVariables()
	{
		if (!checkValidStatus()) return null;

		VariableData[] list = null;
		try
		{
			list = s_proxy.getRegisteredGlobalVariables(m_procId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return list;
	}

	@Override
	public VariableData[] getRegisteredLocalVariables()
	{
		if (!checkValidStatus()) return null;

		VariableData[] list = null;
		try
		{
			list = s_proxy.getRegisteredLocalVariables(m_procId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return list;
	}

	@Override
	public VariableData[] getRegisteredVariables()
	{
		if (!checkValidStatus()) return null;

		VariableData[] list = null;
		try
		{
			list = s_proxy.getRegisteredVariables(m_procId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return list;
	}

	@Override
	public VariableData registerVariableWatch(String varName, boolean global)
	{
		if (!checkValidStatus()) return null;

		VariableData result = null;
		try
		{
			result = s_proxy.registerVariableWatch(m_procId, varName, global);
			m_registeredVars.put(varName, result);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public VariableData getVariable(String varName)
	{
		return m_registeredVars.get(varName);
	}

	@Override
	public void unregisterVariableWatch(String varName, boolean global)
	{
		if (!checkValidStatus()) return;

		try
		{
			s_proxy.unregisterVariableWatch(m_procId, varName, global);
			m_registeredVars.remove(varName);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void clearAllWatches()
	{
		if (!checkValidStatus()) return;

		try
		{
			s_proxy.watchNothing(m_procId);
			m_registeredVars.clear();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void addWatchListener(IVariableWatchListener listener)
	{
		m_listeners.add(listener);
	}

	@Override
	public void removeWatchListener(IVariableWatchListener listener)
	{
		m_listeners.remove(listener);
	}

	@Override
	public void notifyVariableScopeChange(ScopeNotification data)
	{
		m_registeredVars.clear();
		for (VariableData var : data.getGlobalVariables())
		{
			m_registeredVars.put(var.name, var);
		}
		for (VariableData var : data.getLocalVariables())
		{
			m_registeredVars.put(var.name, var);
		}
		for (IVariableWatchListener listener : m_listeners)
		{
			listener.scopeChanged(data);
		}
	}

	@Override
	public void notifyVariableChange(VariableNotification data)
	{
		for (VariableData var : data.getChangedVariables())
		{
			m_registeredVars.put(var.name, var);
		}
		for (IVariableWatchListener listener : m_listeners)
		{
			listener.variableChanged(data);
		}
	}

	@Override
	public boolean changeVariable(String varName, String valueExpression,
	        boolean global)
	{
		boolean result = true;
		try
		{
			s_proxy.changeVariable(m_procId, varName, valueExpression, global);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			result = false;
		}
		return result;
	}

	/***************************************************************************
	 * Check if the current {@link ExecutorStatus} is valid for requesting
	 * variables through the proxy
	 * 
	 * @return
	 **************************************************************************/
	private boolean checkValidStatus()
	{
		boolean valid = true;

		ExecutorStatus st = m_proc.getExecutorStatus();
		switch (st)
		{
		case FINISHED:
		case ERROR:
		case ABORTED:
			valid = false;
			break;
		default:
			valid = true;
		}

		return valid;
	}
}
