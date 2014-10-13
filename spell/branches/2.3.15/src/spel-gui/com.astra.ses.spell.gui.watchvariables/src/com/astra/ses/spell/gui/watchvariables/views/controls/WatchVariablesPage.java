////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.views.controls
// 
// FILE      : WatchVariablesPage.java
//
// DATE      : Sep 22, 2010 10:27:15 AM
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
package com.astra.ses.spell.gui.watchvariables.views.controls;

import java.util.HashMap;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.model.commands.CommandResult;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.watchvariables.Activator;
import com.astra.ses.spell.gui.watchvariables.commands.ChangeVariable;
import com.astra.ses.spell.gui.watchvariables.commands.StopWatchAllVariables;
import com.astra.ses.spell.gui.watchvariables.commands.StopWatchVariable;
import com.astra.ses.spell.gui.watchvariables.commands.WatchVariable;
import com.astra.ses.spell.gui.watchvariables.commands.args.IWatchCommandArgument;
import com.astra.ses.spell.gui.watchvariables.dialogs.VariableDetailDialog;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableManager;
import com.astra.ses.spell.gui.watchvariables.interfaces.IVariableWatchListener;
import com.astra.ses.spell.gui.watchvariables.interfaces.IWatchVariables;
import com.astra.ses.spell.gui.watchvariables.jobs.FormatVariableValueJob;
import com.astra.ses.spell.gui.watchvariables.jobs.UpdateVariablesJob;
import com.astra.ses.spell.gui.watchvariables.model.WatchVariablesContentProvider;
import com.astra.ses.spell.gui.watchvariables.model.WatchVariablesLabelProvider;
import com.astra.ses.spell.gui.watchvariables.model.WatchVariablesTableColumns;
import com.astra.ses.spell.gui.watchvariables.notification.ScopeNotification;
import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.notification.VariableNotification;
import com.astra.ses.spell.gui.watchvariables.notification.WhichVariables;

/*******************************************************************************
 * 
 * Variables page shows the existing variables inside an execution scope
 * 
 ******************************************************************************/
public class WatchVariablesPage extends Page implements ISelectionProvider, SelectionListener, IVariableWatchListener
{

	/***************************************************************************
	 * 
	 * {@link IWatchVariablesPageListener} implementing objects are notified
	 * whenever a page state becomes active/inactive
	 * 
	 **************************************************************************/
	public interface IWatchVariablesPageListener
	{
		/***********************************************************************
		 * Notify that this page becomes active/inactive
		 * 
		 * @param active
		 **********************************************************************/
		public void notifyActive(IPage page, boolean active);
	}

	/** Procedure identifier */
	private IProcedure m_proc;
	/** Table viewer for showing variable values */
	private TableViewer m_viewer;
	/** Globals checkbox */
	private Button m_chkGlobals;
	/** Locals checkbox */
	private Button m_chkLocals;
	/** Combobox */
	private Button m_showList;
	/** Top composite */
	private Composite m_top;
	/** Variable manager */
	private IVariableManager m_manager;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param procId
	 *            the procedure identifier which this page is showing the
	 *            variables
	 **************************************************************************/
	public WatchVariablesPage( IProcedure proc, IWatchVariablesPageListener listener)
	{
		m_proc = proc;
	}

	public String getProcId()
	{
		return m_proc.getProcId();
	}
	
	@Override
	public void createControl(Composite parent)
	{
		m_top = new Composite(parent, SWT.BORDER);
		m_top.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout gLayout = new GridLayout();
		gLayout.numColumns = 1;
		gLayout.marginTop = 0;
		m_top.setLayout(gLayout);

		m_viewer = new TableViewer(m_top, SWT.MULTI | SWT.BORDER);
		m_viewer.setContentProvider(new WatchVariablesContentProvider());
		m_viewer.setLabelProvider(new WatchVariablesLabelProvider());
		m_viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		m_viewer.getTable().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				IStructuredSelection sel = (IStructuredSelection) m_viewer.getSelection();
				VariableData var = (VariableData) sel.getFirstElement();
				FormatVariableValueJob job = new FormatVariableValueJob( var );
				CommandHelper.executeInProgress(job, true, true);
				if (job.result.equals(CommandResult.SUCCESS))
				{
					VariableDetailDialog dialog = new VariableDetailDialog( getSite().getShell(), var.name, job.details );
					dialog.open();
				}
			}
		});
		new WatchVariablesMenuManager(m_viewer, this);

		createColumns();
		createOptions(m_top);

		IWatchVariables watch = (IWatchVariables) ServiceManager.get(IWatchVariables.class);
		m_manager = watch.getVariableManager(m_proc.getProcId());
		
		Logger.debug("Assign variable manager", Level.PROC, this);
		
		m_viewer.setInput(m_manager);

		m_manager.addWatchListener(this);
		
		// Make cells editable
		setCellEditors();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		m_manager.removeWatchListener(this);
	}


	/**************************************************************************
	 * Prepare the viewer for editing the elements
	 *************************************************************************/
	private void setCellEditors()
	{
		/*
		 * Attach cell modifier
		 */
		m_viewer.setCellModifier(new ICellModifier()
		{
			@Override
			public void modify(Object element, String property, Object value)
			{
				VariableData var = null;
				if (element instanceof Item)
				{
					var = (VariableData) ((TableItem) element).getData();
				}
				else
				{
					var = (VariableData) element;
				}
				String valueExpression = value.toString();

				/*
				 * If value has not change then exit
				 */
				if (var.value.equals(valueExpression))
				{
					return;
				}

				/*
				 * Prepare command arguments
				 */
				HashMap<String, String> args = new HashMap<String, String>();
				args.put(IWatchCommandArgument.PROCEDURE_ID, m_proc.getProcId());
				args.put(IWatchCommandArgument.VARIABLE_NAME, var.name);
				args.put(IWatchCommandArgument.VARIABLE_VALUE_EXPR, valueExpression);
				args.put(IWatchCommandArgument.VARIABLE_GLOBAL, Boolean.toString(var.isGlobal));
				/*
				 * Execute the command
				 */
				CommandHelper.execute(ChangeVariable.ID, args);
			}

			@Override
			public Object getValue(Object element, String property)
			{
				VariableData data = (VariableData) element;
				if (property.equals(WatchVariablesTableColumns.NAME_COLUMN.name()))
				{
					return data.name;
				}
				else if (property.equals(WatchVariablesTableColumns.VALUE_COLUMN.name()))
				{
					return data.value;
				}
				return null;
			}

			@Override
			public boolean canModify(Object element, String property)
			{
				return property.equals(WatchVariablesTableColumns.VALUE_COLUMN.name());
			}
		});
		/*
		 * Set column properties
		 */
		String[] properties = new String[WatchVariablesTableColumns.values().length];
		for (WatchVariablesTableColumns column : WatchVariablesTableColumns.values())
		{
			properties[column.ordinal()] = column.name();
		}
		m_viewer.setColumnProperties(properties);
		/*
		 * Set cell editors
		 */
		CellEditor[] cellEditors = new CellEditor[WatchVariablesTableColumns.values().length];
		cellEditors[WatchVariablesTableColumns.NAME_COLUMN.ordinal()] = null;
		cellEditors[WatchVariablesTableColumns.VALUE_COLUMN.ordinal()] = new TextCellEditor(m_viewer.getTable());
		m_viewer.setCellEditors(cellEditors);
	}

	/**************************************************************************
	 * Create the viewer columns
	 *************************************************************************/
	private void createColumns()
	{
		Table table = m_viewer.getTable();
		for (WatchVariablesTableColumns column : WatchVariablesTableColumns.values())
		{
			TableColumn col = new TableColumn(table, SWT.NONE);
			col.setText(column.text);
			col.setAlignment(column.alignment);
			col.setWidth(column.width);
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}

	/**************************************************************************
	 * Create the extra controls
	 *************************************************************************/
	private void createOptions(Composite parent)
	{
		Composite group = new Composite(parent, SWT.NONE);
		group.setLayout(new RowLayout(SWT.HORIZONTAL));
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String id = Activator.PLUGIN_ID;
		String baseLocation = "platform:/plugin/" + id;

		m_showList = new Button(group, SWT.TOGGLE);
		m_showList.setImage(Activator.getImageDescriptor(baseLocation + "/icons/16x16/glasses.png").createImage());
		m_showList.setToolTipText("Show watched variables");
		m_showList.setSelection(false);
		m_showList.addSelectionListener(this);

		m_chkGlobals = new Button(group, SWT.CHECK);
		m_chkGlobals.setText("Globals");
		m_chkGlobals.setSelection(true);
		m_chkGlobals.setToolTipText("Show global variables");
		m_chkGlobals.addSelectionListener(this);

		m_chkLocals = new Button(group, SWT.CHECK);
		m_chkLocals.setText("Locals");
		m_chkLocals.setSelection(true);
		m_chkLocals.setToolTipText("Show global variables");
		m_chkLocals.addSelectionListener(this);
	}

	@Override
	public Control getControl()
	{
		return m_top;
	}

	@Override
	public void setFocus()
	{
		m_top.setFocus();
	}

	/**************************************************************************
	 * Refresh the page contents.
	 *************************************************************************/
	public void updateModel()
	{
		switch(m_proc.getRuntimeInformation().getStatus())
		{
		case PAUSED:
		case WAITING:
		case PROMPT:
		case FINISHED:
		case ABORTED:
			break;
		default:
			m_viewer.getControl().setEnabled(false);
			return;
		}
		Logger.debug("Updating model", Level.PROC, this);
		UpdateVariablesJob job = new UpdateVariablesJob(m_manager);
		try
		{
			m_viewer.getControl().setEnabled(false);
			CommandHelper.executeInProgress(job, true, false);
			if (job.result.equals(CommandResult.SUCCESS))
			{
				m_viewer.refresh();
			}
		}
		finally
		{
			m_viewer.getControl().setEnabled(true);
		}
		Logger.debug("Updating model done", Level.PROC, this);
	}

	/**************************************************************************
	 * Set show mode.
	 * 
	 * @param mode
	 *************************************************************************/
	public void setShowMode(WhichVariables mode)
	{
		Logger.debug("Set view mode " + mode, Level.PROC, this);
		m_manager.setMode(mode);
		updateModel();
	}

	/**************************************************************************
	 * Subscribe to selected variables.
	 *************************************************************************/
	public void subscribeSelected()
	{
		Logger.debug("Subscribe to selected", Level.PROC, this);
		IStructuredSelection sel = (IStructuredSelection) m_viewer.getSelection();
		Object[] list = sel.toArray();
		for (Object obj : list)
		{
			VariableData var = (VariableData) obj;
			/*
			 * Prepare command arguments
			 */
			HashMap<String, String> args = new HashMap<String, String>();
			args.put(IWatchCommandArgument.PROCEDURE_ID, m_proc.getProcId());
			args.put(IWatchCommandArgument.VARIABLE_NAME, var.name);
			args.put(IWatchCommandArgument.VARIABLE_GLOBAL, new Boolean(var.isGlobal).toString());

			/*
			 * Execute the command
			 */
			CommandResult result = (CommandResult) CommandHelper.execute(WatchVariable.ID, args);
			if (result == CommandResult.SUCCESS)
			{
				var.isRegistered = true;
			}
		}
		showRegisteredNow();
	}

	/**************************************************************************
	 * Unsubscribe to selected variables.
	 *************************************************************************/
	public void unsubscribeSelected()
	{
		Logger.debug("Unsubscribe selected", Level.PROC, this);
		IStructuredSelection sel = (IStructuredSelection) m_viewer.getSelection();
		Object[] list = sel.toArray();
		for (Object obj : list)
		{
			VariableData var = (VariableData) obj;
			/*
			 * Prepare command arguments
			 */
			HashMap<String, String> args = new HashMap<String, String>();
			args.put(IWatchCommandArgument.PROCEDURE_ID, m_proc.getProcId());
			args.put(IWatchCommandArgument.VARIABLE_NAME, var.name);
			args.put(IWatchCommandArgument.VARIABLE_GLOBAL, new Boolean(var.isGlobal).toString());

			/*
			 * Execute the command
			 */
			CommandResult result = (CommandResult) CommandHelper.execute(StopWatchVariable.ID, args);
			if (result == CommandResult.SUCCESS)
			{
				var.isRegistered = false;
			}
		}
		m_viewer.refresh();
	}

	/**************************************************************************
	 * Unsubscribe all variables.
	 *************************************************************************/
	public void unsubscribeAll()
	{
		Logger.debug("Unsubscribe all", Level.PROC, this);
		/*
		 * Prepare command arguments
		 */
		HashMap<String, String> args = new HashMap<String, String>();
		args.put(IWatchCommandArgument.PROCEDURE_ID, m_proc.getProcId());
		/*
		 * Execute the command
		 */
		CommandResult result = (CommandResult) CommandHelper.execute(StopWatchAllVariables.ID, args);
		if (result == CommandResult.SUCCESS)
		{
			setShowMode(WhichVariables.AVAILABLE_ALL);
			m_showList.setSelection(false);
		}
	}

	/**************************************************************************
	 * Reset the view mode and show only watched variables.
	 *************************************************************************/
	private void showRegisteredNow()
	{
		Logger.debug("Updating model", Level.PROC, this);
		
		m_showList.setSelection(true);
		if (m_chkGlobals.getSelection() && m_chkLocals.getSelection())
		{
			setShowMode(WhichVariables.REGISTERED_ALL);
		}
		else if (m_chkGlobals.getSelection())
		{
			setShowMode(WhichVariables.REGISTERED_GLOBALS);
		}
		else if (m_chkLocals.getSelection())
		{
			setShowMode(WhichVariables.REGISTERED_LOCALS);
		}
		else
		{
			setShowMode(WhichVariables.NONE);
		}
	}

	/***************************************************************************
	 * Check if the page is currently showing the existing or the watched
	 * variables
	 * 
	 * @return
	 **************************************************************************/
	public boolean isShowingRegistered()
	{
		return m_showList.getSelection();
	}

	/***************************************************************************
	 * Check if this page is active A page is considered active when its
	 * associated {@link IProcedure} object status is different from FINISHED,
	 * ABORTED or ERROR
	 * 
	 * @return
	 **************************************************************************/
	public boolean isActive()
	{
		return m_chkGlobals.isEnabled();
	}

	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see SelectionListener declaration
	 * =========================================================================
	 */

	@Override
	public void widgetSelected(SelectionEvent e)
	{
		boolean registered = m_showList.getSelection();
		boolean globals = m_chkGlobals.getSelection();
		boolean locals = m_chkLocals.getSelection();
		setShowMode(WhichVariables.fromValues(registered, globals, locals));
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e)
	{
	}

	public void cleanup()
	{
		IWatchVariables watch = (IWatchVariables) ServiceManager.get(IWatchVariables.class);
		watch.removeVariableManager(m_proc.getProcId());
	}
	
	/*
	 * ==========================================================================
	 * (non-Javadoc)
	 * 
	 * @see ISelectionProvider declaration
	 * =========================================================================
	 */

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		m_viewer.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection()
	{
		return m_viewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		m_viewer.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection)
	{
		m_viewer.setSelection(selection);
	}

	@Override
    public void variableChanged(VariableNotification data)
    {
		m_viewer.refresh();
    }

	@Override
    public void scopeChanged(ScopeNotification data)
    {
		updateModel();
		m_viewer.refresh();
    }

	@Override
    public void connectionLost()
    {
		m_viewer.refresh();
    }
}
