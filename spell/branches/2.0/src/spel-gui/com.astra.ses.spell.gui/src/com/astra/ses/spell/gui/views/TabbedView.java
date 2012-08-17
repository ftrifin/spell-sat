///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : ProcedureView.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.model.files.IServerFile;
import com.astra.ses.spell.gui.core.model.files.IServerFileLine;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.services.RuntimeSettingsService;
import com.astra.ses.spell.gui.services.RuntimeSettingsService.RuntimeProperty;

public class TabbedView extends ViewPart implements ControlListener
{
	/** Browser ID used by main plugin for preparing the perspective layout */
	public static final String	ID	= "com.astra.ses.spell.gui.views.TabbedView";

	/** Table viewer */
	private TableViewer	       m_viewer;
	/** Tabbed file to fill the table */
	private IServerFile	       m_tabbedFile;
	/** Holds the procedure id */
	private String	           m_procId;

	/**************************************************************************
	 * View content provider
	 *************************************************************************/
	class TabbedContentProvider implements IStructuredContentProvider
	{
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{
		}

		@Override
		public void dispose()
		{
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Object[] getElements(Object parent)
		{
			return ((List) parent).toArray();
		}
	}

	/**************************************************************************
	 * View label provider
	 **************************************************************************/
	class TabbedLabelProvider extends LabelProvider implements
	        ITableLabelProvider, ITableFontProvider
	{

		/** Font */
		private Font	m_font;

		/***********************************************************************
		 * Constructor
		 **********************************************************************/
		public TabbedLabelProvider()
		{
			ConfigurationManager cfg = (ConfigurationManager) ServiceManager
			        .get(ConfigurationManager.ID);
			m_font = cfg.getFont(FontKey.MASTERC);
		}

		@Override
		public String getColumnText(Object obj, int index)
		{
			return ((IServerFileLine) obj).getElement(index);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		@Override
		public Font getFont(Object element, int columnIndex)
		{
			return m_font;
		}
	}

	/**************************************************************************
	 * The constructor.
	 *************************************************************************/
	public TabbedView()
	{
	}

	@Override
	public void createPartControl(Composite parent)
	{
		RuntimeSettingsService runtime = (RuntimeSettingsService) ServiceManager
		        .get(RuntimeSettingsService.ID);
		m_procId = (String) runtime
		        .getRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION);
		ProcedureManager ProcedureMgr = (ProcedureManager) ServiceManager
		        .get(ProcedureManager.ID);

		// Get the AsRun file
		ServerFileType fileType = ServerFileType.EXECUTOR_LOG;
		if (getViewSite().getSecondaryId().matches(".* - AS-RUN"))
		{
			fileType = ServerFileType.ASRUN;
		}
		m_tabbedFile = ProcedureMgr.getServerFile(m_procId, fileType);

		m_viewer = new TableViewer(parent, SWT.VIRTUAL | SWT.SINGLE
		        | SWT.H_SCROLL | SWT.V_SCROLL);
		String[] headerLabels = m_tabbedFile.getHeaderLabels();

		Table table = getTable();
		table.addControlListener(this);

		for (int i = 0; i < headerLabels.length; i++)
		{
			TableColumn col = new TableColumn(table, SWT.LEFT);
			col.setText(headerLabels[i]);
			col.setWidth(10); // Temporary
		}

		GridData viewerData = new GridData(GridData.FILL_BOTH
		        | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		table.setLayoutData(viewerData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		setPartName(getViewSite().getSecondaryId());
		setTitleToolTip(getViewSite().getSecondaryId());

		initContents();
	}

	/**************************************************************************
	 * Get the table.
	 *************************************************************************/
	protected Table getTable()
	{
		return m_viewer.getTable();
	}

	/**************************************************************************
	 * Get the table.
	 *************************************************************************/
	protected IServerFile getTabbedFile()
	{
		return m_tabbedFile;
	}

	/**************************************************************************
	 * Initialize contents
	 *************************************************************************/
	public void initContents()
	{
		m_viewer.setContentProvider(new TabbedContentProvider());
		m_viewer.setLabelProvider(new TabbedLabelProvider());
		m_viewer.setInput(m_tabbedFile.getLines());
	}

	@Override
	public void setFocus()
	{
	}

	/***************************************************************************
	 * Get table viewer
	 * 
	 * @return
	 **************************************************************************/
	protected TableViewer getViewer()
	{
		return m_viewer;
	}

	/***************************************************************************
	 * Get ProcId
	 * 
	 * @return
	 **************************************************************************/
	public String getProcId()
	{
		return m_procId;
	}

	@Override
	public void controlMoved(ControlEvent arg0)
	{
	}

	/***************************************************************************
	 * Dimension the columns
	 **************************************************************************/
	@Override
	public void controlResized(ControlEvent arg0)
	{
		Table table = getTable();

		int tableWidth = table.getClientArea().width;
		int[] headerLabelsSize = m_tabbedFile.getHeaderLabelsSize();

		int count = 0;
		for (TableColumn col : table.getColumns())
		{
			int width = (int) (tableWidth * (((double) headerLabelsSize[count]) / 100.0));
			col.setWidth(width);
			count++;
		}
	}

	/***************************************************************************
	 * Refresh this view's contents
	 **************************************************************************/
	public void refreshView()
	{
		ProcedureManager ProcedureMgr = (ProcedureManager) ServiceManager
		        .get(ProcedureManager.ID);
		ServerFileType fileType = ServerFileType.EXECUTOR_LOG;
		if (getViewSite().getSecondaryId().matches(".* - AS-RUN"))
		{
			fileType = ServerFileType.ASRUN;
		}
		try
		{
			m_tabbedFile = ProcedureMgr.getServerFile(m_procId, fileType);

			m_viewer.setInput(m_tabbedFile.getLines());
			m_viewer.getControl().setFocus();

			RuntimeSettingsService runtime = (RuntimeSettingsService) ServiceManager
			        .get(RuntimeSettingsService.ID);
			runtime.setRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION,
			        m_procId);
		}
		catch (Exception ex)
		{
			Logger.warning("Error: cannot retrive the log file", Level.PROC,
			        this);
		}
	}
}
