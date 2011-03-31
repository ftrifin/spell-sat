///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : LogViewer.java
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
package com.astra.ses.spell.gui.views.controls;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.model.log.GuiLogModel;
import com.astra.ses.spell.gui.model.log.GuiLogModel.LogEvent;
import com.astra.ses.spell.gui.services.ConfigurationManager;
import com.astra.ses.spell.gui.views.controls.drawing.TableSizer;


/*******************************************************************************
 * @brief Composite that uses a table for showing the log messages.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class LogViewer extends TableViewer
{
	/**************************************************************************
	 * Table viewer filter
	 * @author jpizar
	 **************************************************************************/
	private class SeverityViewerFilter extends ViewerFilter 
	{
		/** Severities that will make the filter */
		private ArrayList<Severity> m_severities;
		/** Minimum level that events shall have to pass the filter */
		private Level m_minimumLevel;
		/** Message Pattern */
		private Pattern m_messagePattern;
		/** Source pattern */
		private Pattern m_sourcePattern;
		/** Start Date */
		private Date m_startDate;
		/** End Date */
		private Date m_endDate;
		
		/**********************************************************************
		 * Constructor
		 *********************************************************************/
		public SeverityViewerFilter(ArrayList<Severity> severitiesCollection, Level currentLevel) {
			m_severities = severitiesCollection;
			m_minimumLevel = currentLevel;
			m_messagePattern = Pattern.compile(".*");
			m_sourcePattern = Pattern.compile(".*");
		}
		
		/**********************************************************************
		 * Event level changed
		 * @param newLevel
		 **********************************************************************/
		public void updateLevel (Level newLevel) {
			m_minimumLevel = newLevel;
		}
		
		/***********************************************************************
		 * Update start date
		 * @param startDate
		 **********************************************************************/
		public void setStartDate (Date startDate)
		{
			m_startDate = startDate;
		}
		
		/***********************************************************************
		 * Update start date
		 * @param startDate
		 **********************************************************************/
		public void setEndDate (Date endDate)
		{
			m_endDate = endDate;
		}
		
		/***********************************************************************
		 * Update the message pattern for filtering
		 * @param filteringString
		 **********************************************************************/
		public void setMessagePattern (Pattern messagePattern)
		{
			m_messagePattern = messagePattern;
		}
		
		/***********************************************************************
		 * Update the source pattern for filtering
		 * @param filteringString
		 **********************************************************************/
		public void setSourcePattern (Pattern sourcePattern)
		{
			m_sourcePattern = sourcePattern;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			LogEvent e = (LogEvent) element;
			// Type
			if (!m_severities.contains(e.severity))
			{
				return false;
			}
			// Max Level
			if (m_minimumLevel.compareTo(e.level) < 0)
			{
				return false;
			}
			// Message
			if (!e.message.matches(m_messagePattern.toString()))
			{
				return false;
			}
			// Source
			if (!e.source.matches(m_sourcePattern.toString()))
			{
				return false;
			}
			// Start date
			if ((m_startDate != null) && (m_startDate.compareTo(e.date) > 0))
			{
				return false;
			}
			// End date
			if ((m_endDate != null) && (m_endDate.compareTo(e.date) < 0))
			{
				return false;
			}
			return true;
		}
	}
	
	/**************************************************************************
	 * Table viewer content provider
	 * @author jpizar
	 **************************************************************************/
	private class LogContentProvider implements IStructuredContentProvider
	{	
		/** Maximum of events that will be shown */
		private static final int MAX_EVENTS = 1000;
		
		/**********************************************************************
		 * Constructor
		 *********************************************************************/
		public LogContentProvider () {
		}

		@Override
		public Object[] getElements(Object inputElement)
		{
			Object[] result = ((List) inputElement).toArray();
			if (result.length > MAX_EVENTS)
			{
				int from = result.length - 1 - MAX_EVENTS;
				int to = result.length - 1;
				return Arrays.copyOfRange(result, from, to);
			}
			return result;
		}

		@Override
		public void dispose() 
		{
			// Nothing to do
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
		{
			//Nothing to do
		}
	}
	
	/**************************************************************************
	 * Table viewer label provider
	 * @author jpizar
	 **************************************************************************/
	private class LogTableLabelProvider implements ITableLabelProvider, IColorProvider
	{
		/** Warning background color */
		private Color m_warningColor;
		/** Error background color */
		private Color m_errorColor;
		
		/**********************************************************************
		 * Constructor
		 *********************************************************************/
		public LogTableLabelProvider()
		{
			ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
			m_warningColor = cfg.getStatusColor(ItemStatus.WARNING);
			m_errorColor = cfg.getStatusColor(ItemStatus.ERROR);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			LogEvent event = (LogEvent) element;
			switch (columnIndex) 
			{
				case SEV_COLUMN: return event.severity.toString();
				case LEVEL_COLUMN: return event.level.toString();
				case DATE_COLUMN: return s_df.format(event.date);
				case MSG_COLUMN: return event.message;
				case SRC_COLUMN: return event.source;
				default: return "";
			}
		}

		@Override
		public void addListener(ILabelProviderListener listener) {}

		@Override
		public void dispose() {}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {}

		@Override
		public Color getBackground(Object element) {
			LogEvent event = (LogEvent) element;
			switch (event.severity) {
				case ERROR : return m_errorColor;
				case WARN : return m_warningColor;
				default : return null;
			}
		}

		@Override
		public Color getForeground(Object element) {
			return null;
		}
	}

	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Date string formatter */
	private static DateFormat s_df = DateFormat.getDateTimeInstance();
	/** Column names */
	private static final String[] COLUMN_NAMES = { "Type", "Level", "Date", "Message", "Source" };
	/** Initial column sizes */
	private static final int[] COLUMN_SIZES = { 60, 60, 210, 500, 150 };
	/** column identifiers */
	private static final int SEV_COLUMN   = 0;
	private static final int LEVEL_COLUMN = 1;
	private static final int DATE_COLUMN  = 2;
	private static final int MSG_COLUMN   = 3;
	private static final int SRC_COLUMN   = 4;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private Table m_table;
	
	private SeverityViewerFilter m_severityFilter;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public LogViewer(Composite parent, GuiLogModel model, Level currentLevel )
	{
		super(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.VIRTUAL);

		m_severityFilter = new SeverityViewerFilter(model.getSeverities(), currentLevel);

		m_table = getTable();
		m_table.setHeaderVisible(true);
		m_table.setLinesVisible(false);
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);

		m_table.setFont( cfg.getFont("CODE") );
		m_table.setLayoutData(new GridData(GridData.FILL_BOTH));

		createColumns();
		new TableSizer(parent, m_table, MSG_COLUMN);
		
		super.setContentProvider(new LogContentProvider());
		super.setLabelProvider(new LogTableLabelProvider());
		super.addFilter(m_severityFilter);
		super.setInput(model.getEvents());
	}

	/***************************************************************************
	 * Refresh the viewer
	 **************************************************************************/
	public void addEvent( LogEvent event )
	{
		// Disable redrawing
		getTable().setRedraw(false);
		//Add the event
		super.add(event);
		super.refresh(event);
		// Put the vertical scroll bar at the bottom
		getTable().select( getTable().getItemCount()-1 );
		getTable().showSelection();
		getTable().deselectAll();
		//Enable redawing
		getTable().setRedraw(true);
	}
	
	/**************************************************************************
	 * Level filter has changed
	 * @param newLevel
	 *************************************************************************/
	public void updateMaxLevel(Level newLevel) 
	{
		m_severityFilter.updateLevel(newLevel);
	}
	
	public void setStartDateFilter(Date startDate)
	{
		m_severityFilter.setStartDate(startDate);
	}
	
	public void setEndDateFilter(Date endDate)
	{
		m_severityFilter.setEndDate(endDate);
	}
	
	/***************************************************************************
	 * Update message filter
	 * @param messagePattern
	 **************************************************************************/
	public void setMessageFilter(Pattern messagePattern)
	{
		m_severityFilter.setMessagePattern(messagePattern);
	}
	
	/***************************************************************************
	 * Update source filter
	 * @param sourcePattern
	 **************************************************************************/
	public void setSourceFilter (Pattern sourcePattern)
	{
		m_severityFilter.setSourcePattern(sourcePattern);
	}

	/***************************************************************************
	 * Dispose the control
	 **************************************************************************/
	public void dispose()
	{
		m_table.dispose();
	}
	
	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Create and configure the table columns
	 **************************************************************************/
	private void createColumns()
	{
		for (int i = 0; i < COLUMN_NAMES.length; i++)
		{
			// Create the TableColumn with right alignment
			TableColumn column = new TableColumn(m_table, SWT.RIGHT);
			column.setAlignment(SWT.LEFT);
			column.setResizable(false);
			column.setText(COLUMN_NAMES[i]);
		}
		TableColumn columns[] = m_table.getColumns();
		columns[SEV_COLUMN].setWidth(COLUMN_SIZES[SEV_COLUMN]);
		columns[SRC_COLUMN].setWidth(COLUMN_SIZES[SRC_COLUMN]);
		columns[LEVEL_COLUMN].setWidth(COLUMN_SIZES[LEVEL_COLUMN]);
		columns[DATE_COLUMN].setWidth(COLUMN_SIZES[DATE_COLUMN]);
		columns[MSG_COLUMN].setWidth(COLUMN_SIZES[MSG_COLUMN]);
	}
}
