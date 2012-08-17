///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.callstack
// 
// FILE      : CallstackContentProvider.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.model.callstack;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.extensions.ProcedureBridge;
import com.astra.ses.spell.gui.interfaces.IProcedureStackListener;
import com.astra.ses.spell.gui.interfaces.IProcedureStatusListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/******************************************************************************
 * Provides the contents of the callstack tree viewer.
 *****************************************************************************/
public class CallstackContentProvider implements ITreeContentProvider,
        IProcedureStackListener, IProcedureStatusListener
{
	/** Holds the tree viewer reference */
	private TreeViewer	            m_viewer	= null;
	/** Procedure call stack */
	private CallstackProcedureModel	m_input;
	/** Procedure id */
	private String	                m_procId;

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput)
	{
		/*
		 * Set the viewer
		 */
		m_viewer = (TreeViewer) v;
		/*
		 * Update the input
		 */
		m_input = null;
		if (newInput != null)
		{
			IProcedure proc = (IProcedure) newInput;
			/*
			 * Set procId
			 */
			m_procId = proc.getProcId();
			/*
			 * Set the input
			 */
			m_input = new CallstackProcedureModel(m_procId, proc);
			/*
			 * Subscribe to events
			 */
			if (oldInput == null)
			{
				ProcedureBridge.get().addProcedureStackListener(this);
				ProcedureBridge.get().addProcedureStatusListener(this);
			}
		}
	}

	@Override
	public void dispose()
	{
		ProcedureBridge.get().removeProcedureStackListener(this);
		ProcedureBridge.get().removeProcedureStatusListener(this);
	}

	@Override
	public Object[] getElements(Object parent)
	{
		return new Object[] { m_input.getRootNode() };
	}

	@Override
	public Object getParent(Object child)
	{
		if (child instanceof CallstackNode) { return ((CallstackNode) child)
		        .getParent(); }
		return null;
	}

	@Override
	public Object[] getChildren(Object parent)
	{
		if (parent instanceof CallstackNode) { return ((CallstackNode) parent)
		        .getChildren(); }
		return new Object[0];
	}

	@Override
	public boolean hasChildren(Object parent)
	{
		if (parent instanceof CallstackNode) { return ((CallstackNode) parent)
		        .hasChildren(); }
		return false;
	}

	@Override
	public void notifyStack(IProcedure model, StackNotification data)
	{
		// We want to keep all models updated
		String instanceId = model.getProcId();
		// There may be stack notifications coming before having a model
		// actually created
		if (!m_procId.equals(instanceId)) { return; }

		CallstackNode currentNode = m_input.getCurrentNode();
		CallstackNode newNode = m_input.notifyStack(model, data);

		switch (data.getStackType())
		{
		case CALL:
			m_viewer.add(currentNode, newNode);
			m_viewer.expandToLevel(newNode, TreeViewer.ALL_LEVELS);
			break;
		case RETURN:
			m_viewer.collapseToLevel(currentNode, TreeViewer.ALL_LEVELS);
			m_viewer.update(new Object[] { currentNode, newNode }, null);
			break;
		case LINE:
			m_viewer.refresh(newNode, true);
			break;
		}
	}

	@Override
	public void notifyStatus(IProcedure model, StatusNotification data)
	{
		// We want to keep all models updated
		String instanceId = model.getProcId();
		// There may be stack notifications coming before having a model
		// actually created
		if (!m_procId.equals(instanceId)) { return; }

		switch (data.getStatus())
		{
		case RELOADING:
			m_input.clear();
			m_viewer.refresh(true);
			break;
		default:
			break;
		}
	}

	@Override
	public void notifyError(IProcedure model, ErrorData data)
	{
	}
}
