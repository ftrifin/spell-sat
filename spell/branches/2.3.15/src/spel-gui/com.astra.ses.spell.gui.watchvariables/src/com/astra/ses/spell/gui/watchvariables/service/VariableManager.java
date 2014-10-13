////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.service
// 
// FILE      : VariableManager.java
//
// DATE      : 2010-07-30
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.watchvariables.service;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableManager;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableWatchListener;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableWatcher;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariablesProxy;
import com.astra.ses.spell.gui.watchvariables.notification.ScopeNotification;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.notification.VariableNotification;
import com.astra.ses.spell.gui.watchvariables.notification.WhichVariables;

/*******************************************************************************
 * 
 * Variable manager
 * 
 ******************************************************************************/
public class VariableManager implements IVariableManager, IVariableWatcher
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private IProcedure						  m_procedure;
	/** Holds the list of listeners */
	private ArrayList<IVariableWatchListener> m_listeners;
	/** Holds the list of currently used variables */
	private VariableData[]					  m_variables;
	/** Watch variables proxy */
	private IWatchVariablesProxy m_proxy = null;
	/** Current mode */
	private WhichVariables m_mode;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public VariableManager( IProcedure proc, IWatchVariablesProxy proxy )
	{
		m_proxy = proxy;
		m_procedure = proc;
		m_listeners = new ArrayList<IVariableWatchListener>();
		m_variables = null;
		m_proxy.addVariableWatcher(proc.getProcId(), this);
		m_mode = WhichVariables.AVAILABLE_ALL;
	}

	@Override
	public VariableData[] getVariables()
	{
		return m_variables;
	}

	@Override
	public void updateModel( IProgressMonitor monitor )
	{
		if (!checkValidStatus()) return;
		try
		{
			m_variables = m_proxy.retrieveVariables(m_procedure.getProcId(), m_mode, monitor);
		}
		catch(Exception ex)
		{
			monitor.setCanceled(true);
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot retrieve procedure variables", ex.getLocalizedMessage());
			ex.printStackTrace();
		}
	}

	@Override
	public VariableData registerVariableWatch(String varName, boolean global)
	{
		if (!checkValidStatus()) return null;

		VariableData result = null;
		try
		{
			result = m_proxy.registerVariableWatch(m_procedure.getProcId(), varName, global);
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
		for(VariableData var : m_variables)
		{
			if (var.name.equals(varName)) return var;
		}
		return null;
	}

	@Override
	public void unregisterVariableWatch(String varName, boolean global)
	{
		if (!checkValidStatus()) return;

		try
		{
			m_proxy.unregisterVariableWatch(m_procedure.getProcId(), varName, global);
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
			m_proxy.watchNothing(m_procedure.getProcId());
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
	public boolean changeVariable(String varName, String valueExpression,
	        boolean global)
	{
		boolean result = true;
		try
		{
			m_proxy.changeVariable(m_procedure.getProcId(), varName, valueExpression, global);
			getVariable(varName).value = valueExpression;
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

		ExecutorStatus st = m_procedure.getRuntimeInformation().getStatus();
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

	@Override
    public void setMode(WhichVariables mode)
    {
		m_mode = mode;
    }

	@Override
    public void callbackVariableScopeChange(ScopeNotification data)
    {
	    notifyVariableScopeChange(data);
    }

	@Override
    public void callbackVariableChange(VariableNotification data)
    {
		for(VariableData var : data.getChangedVariables())
		{
			getVariable(var.type).value = var.type;
			getVariable(var.value).value = var.value;
		}
	    notifyVariableChange(data);
    }

	@Override
    public void callbackConnectionLost()
    {
	    m_variables = null;
	    notifyConnectionLost();
    }

	private void notifyVariableScopeChange(ScopeNotification data)
	{
		for (IVariableWatchListener listener : m_listeners)
		{
			listener.scopeChanged(data);
		}
	}

	private void notifyVariableChange(VariableNotification data)
	{
		for (IVariableWatchListener listener : m_listeners)
		{
			listener.variableChanged(data);
		}
	}

	private void notifyConnectionLost()
	{
		for (IVariableWatchListener listener : m_listeners)
		{
			listener.connectionLost();
		}
	}
}
