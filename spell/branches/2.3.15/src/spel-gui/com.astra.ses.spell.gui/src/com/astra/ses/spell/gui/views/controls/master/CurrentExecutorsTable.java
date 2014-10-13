///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : CurrentExecutorsTable.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.views.controls.master;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;

import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.dialogs.ControlInfoDialog;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief
 * @date 09/10/07
 ******************************************************************************/
public class CurrentExecutorsTable extends TableViewer implements IDoubleClickListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Procedure manager handle */
	private static IProcedureManager	    s_procMgr	      = null;
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
	 * Constructor.
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param parent
	 *            The parent composite
	 **************************************************************************/
	public CurrentExecutorsTable( ExecutorComposite parent )
	{
		super(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER
		        | SWT.V_SCROLL);
		if (s_procMgr == null)
		{
			s_procMgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}
		getTable().addSelectionListener(parent);
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(true);
		createColumns();
		setContentProvider( new CurrentExecutorsContentProvider() );
		setLabelProvider( new CurrentExecutorsLabelProvider() );
		setInput(s_procMgr);
		addDoubleClickListener(this);
	}

	/***************************************************************************
	 * Refresh the selected procedure. In case of failure, refresh the whole
	 * list, since the procedure may habe disappeared
	 **************************************************************************/
	public void refresh( String procId )
	{
		try
		{
			IProcedure proc = s_procMgr.getProcedure(procId);
			Logger.debug("Refreshing procedure with id " + procId, Level.GUI, this);
			Logger.debug("Executor details " + proc.getProcId(), Level.GUI, this);
			Logger.debug("    instance " + proc, Level.GUI, this);
			Logger.debug("    controlling client " + proc.getRuntimeInformation().getControllingClient(), Level.GUI,this);
			Logger.debug("    monitoring clients " + proc.getRuntimeInformation().getMonitoringClients(), Level.GUI,this);
			refresh(proc);
		}
		catch(Exception ex)
		{
			refresh();
		}
	}

	/***************************************************************************
	 * Refresh all procedure models
	 **************************************************************************/
	public void refresh()
	{
		try
		{
			Logger.debug("Refresh executors table", Level.GUI, this);
			Object[] procs = ((IStructuredContentProvider)getContentProvider()).getElements(s_procMgr);
			for(Object procObj : procs)
			{
				if (procObj instanceof IProcedure)
				{
					IProcedure proc = (IProcedure) procObj;
					Logger.debug("Executor details" + proc.getProcId(), Level.GUI, this);
					Logger.debug("    controlling client " + proc.getRuntimeInformation().getControllingClient(), Level.GUI,this);
					Logger.debug("    monitoring clients " + proc.getRuntimeInformation().getMonitoringClients(), Level.GUI,this);
					proc.getController().updateInfo();
					refresh(proc);
				}
			}
			super.refresh();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Get the selected procedure
	 **************************************************************************/
	public String[] getSelectedProcedures()
	{
		ArrayList<String> ids = new ArrayList<String>();
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		if (!sel.isEmpty())
		{
			@SuppressWarnings("unchecked")
            Iterator<IProcedure> it = sel.iterator();
			while(it.hasNext())
			{
				Object proc = it.next();
				if (proc instanceof IProcedure)
				{
					ids.add(((IProcedure)proc).getProcId());
				}
				else if (proc instanceof IExecutorInfo)
				{
					ids.add(((IExecutorInfo)proc).getProcId());
				}
			}
		}
		return ids.toArray(new String[0]);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void createColumns()
	{
		
		for(int index = 0; index < CurrentExecutorsTableItems.values().length; index++)
		{
			CurrentExecutorsTableItems item = CurrentExecutorsTableItems.index(index);
			TableViewerColumn col = new TableViewerColumn( this, SWT.NONE);
			col.getColumn().setText(item.title);
			if (item.width>0) col.getColumn().setWidth(item.width);
			col.getColumn().setResizable(true);
			col.getColumn().setMoveable(false);
			if (item.center) col.getColumn().setAlignment(SWT.CENTER);
		}
	}

	@Override
    public void doubleClick(DoubleClickEvent event)
    {
	    ISelection sel = getSelection();
	    if ((sel != null) &&( !sel.isEmpty()))
	    {
	    	IStructuredSelection isel = (IStructuredSelection) sel;
	    	IProcedure proc = (IProcedure) isel.getFirstElement();
	    	ControlInfoDialog dialog = new ControlInfoDialog(getTable().getShell(), proc);
	    	dialog.open();
	    }
    }

}
