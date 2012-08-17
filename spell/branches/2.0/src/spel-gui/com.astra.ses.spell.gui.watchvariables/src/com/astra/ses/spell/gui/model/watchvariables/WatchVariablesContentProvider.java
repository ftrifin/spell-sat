////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.model
// 
// FILE      : WatchVariablesContentProvider.java
//
// DATE      : Sep 22, 2010 11:44:03 AM
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
package com.astra.ses.spell.gui.model.watchvariables;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.notification.ScopeNotification;
import com.astra.ses.spell.gui.core.model.notification.VariableData;
import com.astra.ses.spell.gui.core.model.notification.VariableNotification;
import com.astra.ses.spell.gui.core.model.notification.WhichVariables;
import com.astra.ses.spell.gui.procs.interfaces.model.IVariableManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IVariableWatchListener;

/*******************************************************************************
 * 
 * {@link WatchVariablesContentProvider} provides the variables to show in the
 * variables view
 * 
 ******************************************************************************/
public class WatchVariablesContentProvider implements
        IStructuredContentProvider, IVariableWatchListener
{

	/** Procedure data provider reference */
	private IVariableManager	m_input;
	/** Holds the reference to the table viewer */
	private TableViewer	     m_viewer;
	/** Holds the show mode */
	private WhichVariables	 m_mode;
	/** Data */
	private VariableData[]	 m_elements;

	/**************************************************************************
	 * Constructor.
	 *************************************************************************/
	public WatchVariablesContentProvider()
	{
		m_mode = WhichVariables.AVAILABLE_ALL;
	}

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput)
	{
		m_viewer = (TableViewer) v;
		// (Un)Subscribe to events
		if (oldInput != null)
		{
			m_input.removeWatchListener(this);
		}
		if (newInput != null)
		{
			m_input = (IVariableManager) newInput;
			m_input.addWatchListener(this);
		}
	}

	@Override
	public void dispose()
	{
		m_input.removeWatchListener(this);
	}

	@Override
	public Object[] getElements(Object parent)
	{
		VariableData[] elements = null;
		switch (m_mode)
		{
		case AVAILABLE_GLOBALS:
			elements = m_input.getGlobalVariables();
			break;
		case AVAILABLE_LOCALS:
			elements = m_input.getLocalVariables();
			break;
		case AVAILABLE_ALL:
			elements = m_input.getAllVariables();
			break;
		case REGISTERED_GLOBALS:
			elements = m_input.getRegisteredGlobalVariables();
			break;
		case REGISTERED_LOCALS:
			elements = m_input.getRegisteredLocalVariables();
			break;
		case REGISTERED_ALL:
			elements = m_input.getRegisteredVariables();
			break;
		case NONE:
			elements = new VariableData[0];
			break;
		}
		if (elements == null) elements = new VariableData[0];
		m_elements = elements;
		return m_elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.model.watchvariables.IWatchVariablesContentProvider
	 * #variableChanged(com.astra.ses.spell.gui.core.model.notification.
	 * VariableNotification)
	 */
	@Override
	public void variableChanged(final VariableNotification data)
	{

		VariableData[] vars = data.getChangedVariables();
		/*
		 * Process changed variables so notified variables must set its changed
		 * flag to true
		 */
		for (VariableData var : m_elements)
		{
			for (VariableData notifiedVar : vars)
			{
				if (var.compareTo(notifiedVar) == 0)
				{
					var.isChanged = true;
					var.type = notifiedVar.type;
					var.value = notifiedVar.value;
					break;
				}
				else
				{
					var.isChanged = false;
				}
			}
		}

		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				m_viewer.update(m_elements, null);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.model.watchvariables.IWatchVariablesContentProvider
	 * #scopeChanged(com.astra.ses.spell.gui.core.model.notification.
	 * ScopeNotification)
	 */
	@Override
	public void scopeChanged(ScopeNotification data)
	{
		/*
		 * Reset changed status
		 */
		for (VariableData var : m_elements)
		{
			var.isChanged = false;
		}

		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				m_viewer.refresh();
			}
		});
	}

	/**************************************************************************
	 * Change the view mode.
	 *************************************************************************/
	public void setMode(WhichVariables mode)
	{
		if (!mode.equals(m_mode))
		{
			m_mode = mode;
			m_viewer.refresh();
		}
	}
}
