///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.stack
// 
// FILE      : StackModel.java
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
package com.astra.ses.spell.gui.model.stack;

import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.extensions.ProcedureBridge;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureView;
import com.astra.ses.spell.gui.procs.model.Procedure;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.views.StackView;


/*******************************************************************************
 * @brief Model which provides input data to the Tree Viewer on the StackView.
 * @date 28/04/08
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class StackModel implements IProcedureView
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static ProcedureManager s_mgr = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	
	/** Root item for the tree view */
	private StackItem m_root;
	/** Handle of the stack view */
	private StackView m_view;
	/** Empty flag */
	private boolean m_empty;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	
	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public StackModel( StackView view )
	{
		m_view = view;
		ProcedureBridge.get().addProcedureListener(this);
		if (s_mgr==null)
		{
			s_mgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
		}
		emptyModel();
	}
	
	/***************************************************************************
	 * Provide the root node for a Tree viewer.
	 * 
	 * @return The model root.
	 **************************************************************************/
	public StackItem getModelData()
	{
		return m_root;
	}

	// =========================================================================
	// NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Set an empty model
	 * ************************************************************************/
	protected void emptyModel()
	{
		if (m_root == null)
		{
			m_root = new StackItem();
		}
		Logger.debug("Empty model", Level.GUI, this);
		m_root.addChild(new StackItem("(none)", ""));
		m_empty = true;
	}
	
	/***************************************************************************
	 * Find the root stack item for a given procedure
	 * ************************************************************************/
	private StackItem findRootItem( String instanceId )
	{
		for(StackItem sitem : m_root.getChildren())
		{
			if (sitem.getId().equals(instanceId))
			{
				return sitem;
			}
		}
		return null;
	}

	@Override
	public void procedureModelDisabled(String instanceId)
	{
		// TODO disable item in view
	}

	@Override
	public void procedureModelEnabled(String instanceId)
	{
		// TODO enable item in view
	}

	@Override
	public void procedureModelLoaded(String instanceId)
	{
		Logger.debug("Received notification: procedure " + instanceId + " open", Level.GUI, this);
		Procedure proc = s_mgr.getProcedure(instanceId);
		if (m_empty)
		{
			m_root.removeChild(m_root.getChildren()[0]);
			m_empty = false;
		}
		StackItem item = new StackItem( instanceId, proc.getRootCode());
		m_root.addChild(item);
		m_view.refresh();
	}

	@Override
	public void procedureModelReset(String instanceId)
	{
		try
		{
			StackItem item = findRootItem(instanceId);
			if (item !=null)
			{
				Procedure proc = s_mgr.getProcedure(instanceId);
				item.reset(proc.getRootCode());
				m_view.refresh();
			}
		}
		catch(NoSuchProcedure ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public void procedureModelConfigured(String instanceId)
	{
		// Nothing to do
	}
	
	@Override
	public void procedureModelUnloaded(String instanceId, boolean doneLocally )
	{
		Logger.debug("Received notification: procedure " + instanceId + " closed", Level.GUI, this);
		StackItem item = findRootItem(instanceId);
		if (item!=null)
		{
			m_root.removeChild(item);
			m_view.refresh();
		}
	}

	@Override
	public void procedureCode(CodeNotification data)
	{
//		try
//		{
//			StackItem item = findRootItem(data.getProcId());
//			Procedure proc = s_mgr.getProcedure(data.getProcId());
//			item.update(proc.getRootCode());
//			m_view.refresh();
//		}
//		catch(NoSuchProcedure ex)
//		{
//			ex.printStackTrace();
//		}	
	}
	
	@Override
	public void procedureLine(LineNotification data) 
	{
//		try
//		{
//			StackItem item = findRootItem(data.getProcId());
//			if (item!=null)
//			{
//				Procedure proc = s_mgr.getProcedure(data.getProcId());
//				item.update(proc.getRootCode());
//				m_view.refresh();
//			}
//		}
//		catch(NoSuchProcedure ex)
//		{
//			ex.printStackTrace();
//		}	
	}

	@Override
	public void procedureStatus(StatusNotification data)
	{
//		try
//		{
//			StackItem item = findRootItem(data.getProcId());
//			if (item!=null)
//			{
//				Procedure proc = s_mgr.getProcedure(data.getProcId());
//				item.update(proc.getRootCode());
//				m_view.refresh();
//			}
//		}
//		catch(NoSuchProcedure ex)
//		{
//			ex.printStackTrace();
//		}	
	}

	@Override
	public String getListenerId()
	{
		return "com.astra.ses.spell.gui.models.Stack";
	}


	@Override
	public void procedureDisplay(DisplayData data) {}
	@Override
	public void procedureError(ErrorData data) {}
	@Override
	public void procedureItem(ItemNotification data) {}
	@Override
	public void procedureCancelPrompt(Input inputData){}
	@Override
	public void procedurePrompt(Input inputData) {}
}
