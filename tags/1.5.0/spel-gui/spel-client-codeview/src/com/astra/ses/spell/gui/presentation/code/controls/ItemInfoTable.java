///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : ItemInfoTable.java
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
package com.astra.ses.spell.gui.presentation.code.controls;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.procs.model.ProcedureLine;
import com.astra.ses.spell.gui.procs.model.LineExecutionModel.ItemInfo;
import com.astra.ses.spell.gui.services.ConfigurationManager;


/*******************************************************************************
 * @brief 
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ItemInfoTable extends TableViewer 
{
	/***************************************************************************
	 * ItemContentProvider will provide content to the table
	 * @author jpizar
	 **************************************************************************/
	private class ItemContentProvider implements IStructuredContentProvider
	{
		
		/** input elements */
		private ArrayList<ItemInfo> m_input;
		/** Viewer */
		private TableViewer m_viewer;
		/** summary mode */
		private boolean m_summary;
		
		/***********************************************************************
		 * Constructor
		 **********************************************************************/
		public ItemContentProvider()
		{
			m_summary = true;
		}
		
		/***********************************************************************
		 * Change summary mode
		 * @param summaryMode
		 **********************************************************************/
		public void setSummaryMode(boolean summaryMode)
		{
			m_summary = summaryMode;
			m_viewer.refresh(true, true);
		}

		@Override
		public Object[] getElements(Object inputElement) {
			ItemInfo[] result = new ItemInfo[m_input.size()];
			m_input.toArray(result);
			if (m_summary && !m_input.isEmpty())
			{
				ArrayList<ItemInfo> lastList = new ArrayList<ItemInfo>();
				ItemInfo last = result[result.length - 1];
				int exec = last.execution;
				for (int i = result.length - 1; i>=0; i--)
				{
					ItemInfo element = result[i];
					if (element.execution != exec)
					{
						break;
					}
					lastList.add(0,element);
				}
				result = new ItemInfo[lastList.size()];
				lastList.toArray(result);
			}
			return result;
		}

		@Override
		public void dispose() {
			m_input = null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
		{
			m_viewer = (TableViewer) viewer;
			m_input = (ArrayList<ItemInfo>) newInput;
		}
		
		/***********************************************************************
		 * Input has been updated
		 **********************************************************************/
		public void inputUpdated(boolean showLatest)
		{
			m_viewer.refresh(true, true);
		}
	}
	
	/***************************************************************************
	 * 
	 * @author jpizar
	 *
	 **************************************************************************/
	private class ItemLabelProvider implements ITableLabelProvider, ITableColorProvider
	{
		/** Configuration manager */
		private ConfigurationManager m_rsc = null;
		
		/***********************************************************************
		 * Constructor
		 **********************************************************************/
		public ItemLabelProvider()
		{
			if (m_rsc == null)
			{
				m_rsc = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
			}
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ItemInfo info = (ItemInfo) element;
			switch (columnIndex)
			{
			case CNT_COLUMN:
				int currentExeGroup=1;
				String eg = Integer.toString(currentExeGroup);
				String ieg = info.id.split("-")[0];
				if (!ieg.equals(eg))
				{
					currentExeGroup = Integer.parseInt(ieg);
					eg = Integer.toString(currentExeGroup);
				}
				return eg;
			case NAME_COLUMN:
				return info.name;
			case VALUE_COLUMN:
				return info.value;
			case STATUS_COLUMN:
				return info.status;
			case TIME_COLUMN:
				return info.time;
			case STACK_COLUMN:
				String[] elements = info.stack.split(":");
				String st = elements[elements.length-2];
				if (st.startsWith("$"))
				{
					st = st.substring(1);
				}
				return st;
			case COMMENT_COLUMN:
				return info.reason;
			default: return "";
			}
		}
		
		@Override
		public Color getBackground(Object element, int columnIndex) {
			ItemInfo info = (ItemInfo) element;
			if (columnIndex == STATUS_COLUMN)
			{
				ItemStatus status = ItemStatus.fromName(info.status);
				Color bg = m_rsc.getStatusColor(status);
				return bg;
			}
			return null;
		}
		
		@Override
		public Color getForeground(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {}

		@Override
		public void dispose() {
			m_rsc = null;
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {}
	}
	
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Column names */
	public static final String[] COLUMN_NAMES = { "EX", "Item Name", "Value", "Status", "Time", "Stack", "Comments"};
	/** Identifier for the name column */
	public static final int CNT_COLUMN   = 0;
	/** Identifier for the name column */
	public static final int NAME_COLUMN   = 1;
	/** Identifier for the value column */
	public static final int VALUE_COLUMN  = 2;
	/** Identifier for the status column */
	public static final int STATUS_COLUMN  = 3;
	/** Identifier for the status column */
	public static final int TIME_COLUMN  = 4;
	/** Identifier for the status column */
	public static final int STACK_COLUMN  = 5;
	/** Identifier for the comment column */
	public static final int COMMENT_COLUMN = 6;
	/** Initial column widths */
	public static final Integer COLUMN_WIDTH[] = { 30, 150, 100, 100, 160, 100, 150 };

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Line whose info is being shown */
	private ProcedureLine m_line;
	/** Table input */
	private ArrayList<ItemInfo> m_input;
	/** ContentProvider */
	private ItemContentProvider m_contentProvider;
	// PROTECTED ---------------------------------------------------------------
	protected Table m_table;
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
	public ItemInfoTable( ProcedureLine line, Composite parent)
	{
		super(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
		
		m_input = new ArrayList<ItemInfo>();
		m_contentProvider = new ItemContentProvider();
		
		setContentProvider(m_contentProvider);
		setLabelProvider(new ItemLabelProvider());
		
		m_line = line;
		 
		m_table = getTable();
		m_table.setHeaderVisible(true);
		m_table.setLinesVisible(true);
		m_table.setFont(new Font(Display.getCurrent(), "Arial", 9, SWT.NORMAL));
		m_table.setLayoutData(new GridData(GridData.FILL_BOTH));
		initializeTable();

		m_table.getColumn(NAME_COLUMN).setAlignment(SWT.LEFT);
		m_table.getColumn(COMMENT_COLUMN).setAlignment(SWT.LEFT);
		
		setInput(m_input);
	}
	
	/***************************************************************************
	 * Initialize the table drawers and the table initial settings
	 **************************************************************************/
	protected void initializeTable()
	{
		// Now that we've set the text into the columns,
		// we call pack() on each one to size it to the
		// contents
		String[] names = getColumnNames();
		Integer[] sizes = getColumnSizes();
		for (int i = 0, n = names.length; i < n; i++)
		{
			// Create the TableColumn with right alignment
			TableColumn column = new TableColumn(m_table, SWT.RIGHT);
			column.setText(names[i]);
			column.setAlignment(SWT.LEFT);
		}
		TableColumn[] columns = m_table.getColumns();
		for (int i = 0, n = names.length; i < n; i++)
		{
			columns[i].pack();
			columns[i].setWidth(sizes[i]);
			columns[i].setResizable(true);
		}
		m_table.getColumn(STATUS_COLUMN).setAlignment(SWT.CENTER);
	}
	
	/***************************************************************************
	 * Update the table contents
	 * 
	 * @param ctxNames
	 * 		List of known contexts
	 **************************************************************************/
	public void updateInfo(String csPosition)
	{
		// Clear the table
		if (m_line != null)
		{
			//Determine if the scrollbar should be sticket to the bottom
			ScrollBar bar = m_table.getVerticalBar();
			int max = bar.getMaximum();
			int thumb = bar.getThumb();
			int current = bar.getSelection();
			boolean bottom = current >= (max - thumb);
			// Update the model
			Vector<ItemInfo> infoItems = m_line.getItemData(false);
			m_input.clear();
			if (infoItems!=null)
			{
				// Build the table rows with the item information
				for (ItemInfo info : infoItems)
				{
					m_input.add(info);
				}
				m_contentProvider.inputUpdated(true);
				if (bottom)
				{
					showLastItem();
				}
			}
		}
		else
		{
			Logger.error("Retrieved no line: " + m_line.getLineNum(), Level.GUI, this);
		}
	}
	
	/***************************************************************************
	 * Change the summary mode
	 * @param summary
	 **************************************************************************/
	public void setSummaryMode(boolean summary)
	{
		m_contentProvider.setSummaryMode(summary);
	}

	/***************************************************************************
	 * Return the table column names
	 **************************************************************************/
	protected String[] getColumnNames()
	{
		return COLUMN_NAMES;
	}
	
	/***************************************************************************
	 * Return the table column sizes
	 **************************************************************************/
	protected Integer[] getColumnSizes()
	{
		return COLUMN_WIDTH;
	}
	
	/***************************************************************************
	 * Return the adjustable column name
	 **************************************************************************/
	protected int getAdjustableColumn()
	{
		return NAME_COLUMN;
	}
	
	/****************************************************************************
	 * Show the last Table's item
	 ***************************************************************************/
	private void showLastItem()
	{
		int items = m_table.getItemCount();
		TableItem last = m_table.getItem(items -1);
		m_table.showItem(last);
	}
}
