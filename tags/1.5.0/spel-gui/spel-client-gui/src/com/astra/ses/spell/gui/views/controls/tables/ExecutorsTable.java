///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.tables
// 
// FILE      : ExecutorsTable.java
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
package com.astra.ses.spell.gui.views.controls.tables;

import java.util.Set;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.server.ClientInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.dialogs.ExecutorsDialog;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.model.Procedure;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.services.ConfigurationManager;


/*******************************************************************************
 * @brief 
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ExecutorsTable extends BasicTable 
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Procedure manager handle */
	private static ContextProxy s_proxy = null;
	/** Procedure manager handle */
	private static ProcedureManager s_procMgr = null;
	/** Resource manager handle */
	private static ConfigurationManager s_cfg = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Column names */
	public static final String[] COLUMN_NAMES = { "Procedure", "Status", "Condition", "Controlled", "Monitored" };
	/** Identifier for the procedure column */
	public static final int PROCEDURE_COLUMN   = 0;
	/** Identifier for the status column */
	public static final int STATUS_COLUMN  = 1;
	/** Identifier for the condition column */
	public static final int CONDITION_COLUMN  = 2;
	/** Identifier for the controlled column */
	public static final int CONTROLLED_COLUMN  = 3;
	/** Identifier for the monitored column */
	public static final int MONITORED_COLUMN = 4;
	/** Initial column widths */
	public static final Integer COLUMN_WIDTH[] = { 120, 120, 120, 130, 130 };

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private Vector<String> m_modelIds;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	
	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param procId
	 * 		The procedure identifier
	 * @param parent
	 * 		The parent composite
	 **************************************************************************/
	public ExecutorsTable(Composite parent, ExecutorsDialog dialog )
	{
		super(parent);
		if (s_procMgr == null)
		{
			s_procMgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
		}
		if (s_proxy == null)
		{
			s_proxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		}
		if (s_cfg == null)
		{
			s_cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		}
		m_modelIds = new Vector<String>();
		m_table.addSelectionListener(dialog);
	}
	
	/***************************************************************************
	 * Update the table contents
	 **************************************************************************/
	public void updateExecutors()
	{
		m_table.setRedraw(false);
		m_table.removeAll();
		
		m_modelIds.clear();
		Set<String> remoteIds = s_procMgr.getOpenRemoteProcedures();
		Set<String> localIds  = s_procMgr.getOpenLocalProcedures();
		m_modelIds.addAll(remoteIds);
		m_modelIds.addAll(localIds);
		
		if (m_modelIds.size()==0)
		{
			TableItem titem = new TableItem(m_table, SWT.NONE);
			titem.setText(PROCEDURE_COLUMN, "(no executors)");
			titem.setText(STATUS_COLUMN, "");
			titem.setText(CONDITION_COLUMN, "");
			titem.setText(CONTROLLED_COLUMN, "");
			titem.setText(MONITORED_COLUMN, "");
		}
		else
		{
			Logger.debug("Generating table rows for " + m_modelIds.size() + " executors", Level.GUI, this);
			int  count = 0;
			for (count=0; count<m_modelIds.size(); count++)
			{
				new TableItem(m_table, SWT.NONE);
			}
			count = 0;
			for ( String exec : m_modelIds)
			{
				if (s_procMgr.isLocallyLoaded(exec))
				{
					Procedure proc = s_procMgr.getProcedure(exec);
					TableItem titem = m_table.getItem(count);
					titem.setText(PROCEDURE_COLUMN, proc.getProcName());
					
					ExecutorInfo info = proc.getInfo();
					s_proxy.updateExecutorInfo(proc.getProcId(), info);
					
					titem.setText(STATUS_COLUMN, info.getStatus().toString());
					titem.setText(CONDITION_COLUMN, info.getCondition());
					String monitoringClients = buildMonitoringClients(info);
					if (info.getMode()==ClientMode.CONTROLLING)
					{
						titem.setText(CONTROLLED_COLUMN, "LOCAL");
					}
					else
					{
						String controlClient = buildControlClient(info); 
						titem.setText(CONTROLLED_COLUMN, controlClient);
					}
					titem.setText(MONITORED_COLUMN, monitoringClients);
					titem.setBackground(s_cfg.getProcedureColor(info.getStatus()));
				}
				else
				{
					ExecutorInfo info = s_procMgr.getRemoteProcedure(exec);
					TableItem titem = m_table.getItem(count);
					titem.setText(PROCEDURE_COLUMN, info.getProcId());
					titem.setText(STATUS_COLUMN, info.getStatus().toString());
					titem.setText(CONDITION_COLUMN, info.getCondition());
					String controlClient = buildControlClient(info); 
					String monitoringClients = buildMonitoringClients(info);
					titem.setText(CONTROLLED_COLUMN, controlClient);
					titem.setText(MONITORED_COLUMN, monitoringClients);
					titem.setBackground(s_cfg.getProcedureColor(info.getStatus()));
				}
				count++;
			}
		}
		m_table.getColumn(0).setAlignment(SWT.LEFT);
		m_table.setRedraw(true);
	}

	/***************************************************************************
	 * Update the given executor
	 **************************************************************************/
	public void updateExecutor( String procId )
	{
		if (!m_modelIds.contains(procId))
		{
			addExecutor(procId);
			return;
		}
		if (s_procMgr.isLocallyLoaded(procId))
		{
			Procedure proc = s_procMgr.getProcedure(procId);
			int idx = m_modelIds.indexOf(procId);
			TableItem titem = m_table.getItem(idx);
			m_table.setRedraw(false);
			
			ExecutorInfo info = proc.getInfo();
			s_proxy.updateExecutorInfo(proc.getProcId(), info);
			
			titem.setText(PROCEDURE_COLUMN, proc.getProcName());
			titem.setText(STATUS_COLUMN, info.getStatus().toString());
			titem.setText(CONDITION_COLUMN, info.getCondition());
			String monitoringClients = buildMonitoringClients(info);
			if (info.getMode()==ClientMode.CONTROLLING)
			{
				titem.setText(CONTROLLED_COLUMN, "LOCAL");
			}
			else
			{
				String controlClient = buildControlClient(info); 
				titem.setText(CONTROLLED_COLUMN, controlClient);
			}
			titem.setText(MONITORED_COLUMN, monitoringClients);
			titem.setBackground(s_cfg.getProcedureColor(info.getStatus()));
			m_table.setRedraw(true);
		}
		else
		{
			try
			{
				ExecutorInfo info = s_procMgr.getRemoteProcedure(procId);
				int idx = m_modelIds.indexOf(procId);
				TableItem titem = m_table.getItem(idx);
				m_table.setRedraw(false);
				titem.setText(PROCEDURE_COLUMN, info.getProcId());
				titem.setText(STATUS_COLUMN, info.getStatus().toString());
				titem.setText(CONDITION_COLUMN, info.getCondition());
				String controlClient = buildControlClient(info); 
				String monitoringClients = buildMonitoringClients(info);
				titem.setText(CONTROLLED_COLUMN, controlClient);
				titem.setText(MONITORED_COLUMN, monitoringClients);
				titem.setBackground(s_cfg.getProcedureColor(info.getStatus()));
				m_table.setRedraw(true);
			}
			catch(NoSuchProcedure ex)
			{
				// Pass: it may happen if a procedure fails to load
			}
		}
	}

	/***************************************************************************
	 * Add the given executor
	 **************************************************************************/
	public void addExecutor( String procId )
	{
		if (m_modelIds.size()==0)
		{
			// Remove the "no executors" row
			m_table.removeAll();
		}
		m_table.setRedraw(false);
		TableItem titem = new TableItem(m_table, SWT.NONE);
		m_modelIds.addElement(procId);
		if (s_procMgr.isLocallyLoaded(procId))
		{
			Procedure proc = s_procMgr.getProcedure(procId);
			titem.setText(PROCEDURE_COLUMN, proc.getProcName());
			titem.setText(STATUS_COLUMN, proc.getStatus().toString());
			titem.setText(CONDITION_COLUMN, proc.getCondition());
			String monitoringClients = buildMonitoringClients(proc.getInfo());
			titem.setText(CONTROLLED_COLUMN, "LOCAL");
			titem.setText(MONITORED_COLUMN, monitoringClients);
			titem.setBackground(s_cfg.getProcedureColor(proc.getStatus()));
		}
		else
		{
			try
			{
				ExecutorInfo model = s_procMgr.getRemoteProcedure(procId);
				titem.setText(PROCEDURE_COLUMN, model.getProcId());
				titem.setText(STATUS_COLUMN, model.getStatus().toString());
				titem.setText(CONDITION_COLUMN, model.getCondition());
				String controlClient = buildControlClient(model); 
				String monitoringClients = buildMonitoringClients(model);
				titem.setText(CONTROLLED_COLUMN, controlClient);
				titem.setText(MONITORED_COLUMN, monitoringClients);
				titem.setBackground(s_cfg.getProcedureColor(model.getStatus()));
			}
			catch(NoSuchProcedure ex)
			{
				// Pass: it may happen if a procedure fails to load
			}
		}
		m_table.setRedraw(true);
	}

	/***************************************************************************
	 * Remove the given executor
	 **************************************************************************/
	public void removeExecutor( String procId )
	{
		if (m_modelIds.contains(procId))
		{
			m_table.setRedraw(false);
			int idx = m_modelIds.indexOf(procId);
			m_modelIds.removeElement(procId);
			m_table.remove(idx);
			m_table.setRedraw(true);
			if (m_modelIds.size()==0)
			{
				TableItem titem = new TableItem(m_table, SWT.NONE);
				titem.setText(PROCEDURE_COLUMN, "(no executors)");
				titem.setText(STATUS_COLUMN, "");
				titem.setText(CONDITION_COLUMN, "");
				titem.setText(CONTROLLED_COLUMN, "");
				titem.setText(MONITORED_COLUMN, "");
			}
		}
	}

	/***************************************************************************
	 * Get the selected procedure
	 **************************************************************************/
	public String getSelectedProcedure()
	{
		if ((m_table.getSelectionIndex()!=-1)&&m_modelIds.size()>0)
		{
			return m_modelIds.get(m_table.getSelectionIndex());
		}
		return null;
	}

	/***************************************************************************
	 * Get the column names
	 **************************************************************************/
	protected String[] getColumnNames()
	{
		return COLUMN_NAMES;
	}
	
	/***************************************************************************
	 * Get the column sizes
	 **************************************************************************/
	protected Integer[] getColumnSizes()
	{
		return COLUMN_WIDTH;
	}
	
	/***************************************************************************
	 * Obtain the adjustable column
	 **************************************************************************/
	protected int getAdjustableColumn()
	{
		return PROCEDURE_COLUMN;
	}
	
	@Override
	public int getTableWidthHint() 
	{
		int result = 0;
		for (int columnWidth : COLUMN_WIDTH) 
		{
			result += columnWidth;
		}
		return result;
	}
	
	@Override
	public int getTableHeightHint()
	{
		return 200;
	}
	
	/***************************************************************************
	 * Check if the given column is resizable
	 **************************************************************************/
	protected boolean isResizable( int idx )
	{
		return false;
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	private String buildControlClient( ExecutorInfo info )
	{
		String controlClient = info.getControllingClient();
		if (controlClient.trim().length()>0)
		{
			Logger.debug("Controlling client info: " + controlClient, Level.GUI, this);
		}
		else
		{
			controlClient = "";
		}
		return controlClient;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private String buildMonitoringClients( ExecutorInfo info )
	{
		Vector<String> mclients = info.getMonitoringClients();
		String monitoringClients = "";
		for(String ckey: mclients)
		{
			if (ckey.trim().length()==0) continue;
			Logger.debug("Monitoring client info: " + ckey, Level.GUI, this);
			ClientInfo cinfo = s_proxy.getClientInfo(ckey);
			if (cinfo == null)
			{
				Logger.error("Unable to get information of client: " + ckey, Level.PROC, this);
				continue;
			}
			if (monitoringClients.length()>0) monitoringClients += ",";
			monitoringClients += cinfo.toString();
		}
		return monitoringClients;
	}
}
