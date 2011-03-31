///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.database.browser.views.telemetry
// 
// FILE      : TelemetryViewPart.java
//
// DATE      : 2009-09-14
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.database.browser.views.telemetry;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.database.browser.views.FilterUtils;
import com.astra.ses.spell.database.browser.views.ViewUtils;
import com.astra.ses.spell.database.browser.views.telecommand.TelecommandViewPart;
import com.astra.ses.spell.database.transfer.DatabaseTransferable;
import com.astra.ses.spell.database.transfer.OfflineDatabaseTransfer;
import com.astra.ses.spell.dev.database.DatabaseManager;
import com.astra.ses.spell.dev.database.DatabaseManager.IWorkingDatabaseListener;
import com.astra.ses.spell.dev.database.impl.commanding.Command;
import com.astra.ses.spell.dev.database.impl.telemetry.TelemetryParameter;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;

public class TelemetryViewPart extends ViewPart implements IWorkingDatabaseListener {

	/** Browser ID used by main plugin for preparing the perspective layout */
	public static final String ID = "ses.astra.spel.browser.views.telemetry.TelemetryViewPart";
	
	/** Database details label */
	private Label m_dbPathLabel;
	/** Database version label */
	private Label m_dbDetailsLabel;
	/** Table viewer */
	private TableViewer m_viewer;
	/** Table viewer filter */
	private TelemetryViewerFilter m_viewerFilter;
	/** Text input widget for filtering the text */
	private Text m_filterText;
	/** Current database in use */
	private ISpellDatabase m_currentDB;
	
	/**************************************************************************
	 * View content provider
	 *************************************************************************/
	class ViewContentProvider implements IStructuredContentProvider {
		
		@Override
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		
		@Override
		public void dispose() {
		}
		
		@Override
		public Object[] getElements(Object parent) {
			return (TelemetryParameter[]) parent;
		}
	}
	
	/**************************************************************************
	 * View label provider
	 * @author jpizar
	 **************************************************************************/
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			TelemetryParameter tm = (TelemetryParameter) obj;
			switch (index)
			{
				case 0: return tm.getName();
				case 1: return tm.getDescription();
				default: return "";
			}
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	
	/***************************************************************************
	 * Class for sorting elements
	 * @author jpizar
	 *
	 **************************************************************************/
	class NameSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			TelemetryParameter tm1 = (TelemetryParameter) e1;
			TelemetryParameter tm2 = (TelemetryParameter) e2;
			return tm1.getName().compareTo(tm2.getName());
		}
	}
	
	/**************************************************************************
	 * Viewer filter
	 * @author jpizar
	 *************************************************************************/
	class TelemetryViewerFilter extends ViewerFilter
	{
		/** filtering expression */
		private Pattern m_filterPattern;
		
		/**********************************************************************
		 * Constructor
		 *********************************************************************/
		public TelemetryViewerFilter(Pattern regex)
		{
			m_filterPattern = regex;
		}
		
		/**********************************************************************
		 * Change the filtering pattern
		 * @param newPattern
		 *********************************************************************/
		public void setPattern(Pattern newPattern)
		{
			m_filterPattern = newPattern;
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			TelemetryParameter tm = (TelemetryParameter) element;
			String tmName = tm.getName();
			return tmName.matches(m_filterPattern.toString());
		}
	}

	/**************************************************************************
	 * The constructor.
	 *************************************************************************/
	public TelemetryViewPart() {
		registerToDatabaseChanges();
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, true);
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parent.setLayout(parentLayout);
		
		/*
		 * DB Details composite
		 */
		Composite dbDetails = new Composite(parent, SWT.NONE);
		GridLayout detailsLayout = new GridLayout(2, false);
		GridData detailsData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		detailsLayout.marginHeight = 0;
		detailsLayout.marginWidth = 1;
		dbDetails.setLayout(detailsLayout);
		dbDetails.setLayoutData(detailsData);
		
		Label dbPathLabel = new Label(dbDetails, SWT.NONE);
		dbPathLabel.setText("Path");
		
		GridData labelData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		m_dbPathLabel = new Label(dbDetails, SWT.NONE);
		m_dbPathLabel.setLayoutData(labelData);
		m_dbPathLabel.addControlListener(new ControlListener()
		{
			@Override
			public void controlMoved(ControlEvent e) {/*DO NOTHING*/}

			@Override
			public void controlResized(ControlEvent e) {
				if (m_currentDB != null)
				{
					String dbPath = m_currentDB.getDatabasePath();
					ViewUtils.adjustLabelContents(m_dbPathLabel, dbPath);
				}
			}
		});
		
		Label dbVersionLabel = new Label(dbDetails, SWT.NONE);
		dbVersionLabel.setText("Version");
		
		m_dbDetailsLabel = new Label(dbDetails, SWT.NONE);
		m_dbDetailsLabel.setLayoutData(labelData);
		m_dbDetailsLabel.addControlListener(new ControlListener()
		{
			@Override
			public void controlMoved(ControlEvent e) {/*DO NOTHING*/}

			@Override
			public void controlResized(ControlEvent e) {
				if (m_currentDB != null)
				{
					String content = m_currentDB.getName() + " " + m_currentDB.getVersion();
					ViewUtils.adjustLabelContents(m_dbDetailsLabel, content);
				}
			}
		});
		/*
		 * end of DB details widget
		 */
		
		
		m_viewer = new TableViewer(parent, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		
		TableColumn nameCol = new TableColumn(m_viewer.getTable(), SWT.LEFT);
		nameCol.setText("Name");
		nameCol.setWidth(80);
		TableColumn descCol = new TableColumn(m_viewer.getTable(), SWT.LEFT);
		descCol.setText("Description");
		descCol.setWidth(140);
		
		/* Table viewer */
		GridData viewerData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		m_viewer.getTable().setLayoutData(viewerData);	
		m_viewer.getTable().setHeaderVisible(true);
		m_viewer.getTable().setLinesVisible(false);

		/* Text widget */
		GridData textData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		textData.horizontalIndent = 0;
		textData.verticalIndent = 0;
		m_filterText = new Text(parent, SWT.BORDER);
		m_filterText.setLayoutData(textData);
		m_filterText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e) {	
				Text filterText = (Text) e.widget;
				String filter = filterText.getText().toUpperCase();
				try
				{
					Pattern regex = Pattern.compile(FilterUtils.globify(filter));
					m_viewerFilter.setPattern(regex);
					m_viewer.refresh();
				}
				catch (PatternSyntaxException p)
				{
					// do nothing
				}
			}
		});
		
		initContents();
		initDragAction();
	}
	
	/***************************************************************************
	 * Get selected elements
	 * @return
	 **************************************************************************/
	public TelemetryParameter[] getSelectedParameters()
	{
		// Retrieve selected elements from this viewer
		IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
		Object[] elements = selection.toArray();
		TelemetryParameter[] params = new TelemetryParameter[elements.length];
		for (int i = 0; i < elements.length; i++)
		{
			params[i] = (TelemetryParameter) elements[i];
		}
		return params;
	}
	
	/**************************************************************************
	 * Deselect all the items after making a drop
	 *************************************************************************/
	public void deselectAll()
	{
		m_viewer.getTable().deselectAll();
	}
	
	/**************************************************************************
	 * Initialize contents
	 *************************************************************************/
	private void initContents()
	{
		m_viewerFilter = new TelemetryViewerFilter(Pattern.compile(FilterUtils.globify("")));
		m_viewer.setContentProvider(new ViewContentProvider());
		m_viewer.setLabelProvider(new ViewLabelProvider());
		m_viewer.setSorter(new NameSorter());
		m_viewer.addFilter(m_viewerFilter);
		if (m_currentDB != null)
		{
			m_viewer.setInput(getInput());
			m_dbPathLabel.setText(m_currentDB.getDatabasePath());
			m_dbDetailsLabel.setText(m_currentDB.getName() + " " + m_currentDB.getVersion());
		}
	}
	
	/**************************************************************************
	 * Init the drag action for the viewer
	 *************************************************************************/
	private void initDragAction()
	{
		//Add transfer types for this drag source
		Transfer[] types = new Transfer[] {OfflineDatabaseTransfer.getInstance()};
		//Drag listener
		DragSourceListener dragListener = new DragSourceListener()
		{
			@Override
			public void dragFinished(DragSourceEvent event) {
					event.data = null;
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (OfflineDatabaseTransfer.getInstance().isSupportedType(event.dataType))
				{
					// Retrieve selected elements from this viewer
					TelemetryParameter[] params = getSelectedParameters();
					// Retrieve selected elements from the Telecommand viewer
					IViewPart tcView = getSite().getWorkbenchWindow().getActivePage().findView(TelecommandViewPart.ID);
					Command[] commands = new Command[0];
					if (tcView != null)
					{
						TelecommandViewPart tcViewPart = (TelecommandViewPart) tcView;
						commands = tcViewPart.getSelectedCommands();
						// Deselect elements
				        tcViewPart.deselectAll();
					}
			        event.data = new DatabaseTransferable(params, commands);
			        deselectAll();
				}
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
				if (selection.size() < 1)
				{
					event.doit = false;
				}
			}	
		};
		
		m_viewer.addDragSupport(DND.DROP_COPY, types, dragListener);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		m_viewer.getControl().setFocus();
	}
	
	@Override
	public void dispose()
	{
		unregisterToDatabaseChanges();
		super.dispose();
	}

	@Override
	public void workingDatabaseChanged(ISpellDatabase db) {
		m_currentDB = db;
		if (m_viewer == null)
		{
			return;
		}
		m_dbPathLabel.setText(getDatabasePath());
		m_dbDetailsLabel.setText(getDatabaseDescription());
		m_viewer.setInput(getInput());
	}
	
	/***************************************************************************
	 * Get the collection of ICommandingElements from the database
	 **************************************************************************/
	private TelemetryParameter[] getInput()
	{
		if (m_currentDB == null)
		{
			return new TelemetryParameter[0];
		}
		
		Collection<String> parameters = m_currentDB.getMonitoringParameters();
		TelemetryParameter[] result = new TelemetryParameter[parameters.size()];
		int i = 0;
		for (String param :parameters)
		{
			result[i] = m_currentDB.getParameter(param);
			i++;
		}
		return result;
	}
	
	/****************************************************************************
	 * Get database path
	 * @return
	 ***************************************************************************/
	private String getDatabasePath()
	{
		String path = "";
		if (m_currentDB != null)
		{
			path = m_currentDB.getDatabasePath();
		}
		return path;
	}
	
	/****************************************************************************
	 * Get database description
	 * @return
	 ***************************************************************************/
	private String getDatabaseDescription()
	{
		String desc = "";
		if (m_currentDB != null)
		{
			desc = m_currentDB.getName() + " " + m_currentDB.getVersion();
		}
		return desc;
	}
	
	/***************************************************************************
	 * Register to extension points
	 **************************************************************************/
	private void registerToDatabaseChanges()
	{
		DatabaseManager.getInstance().registerListener(this);
	}
	
	/***************************************************************************
	 * Unregister to database changes
	 **************************************************************************/
	private void unregisterToDatabaseChanges()
	{
		DatabaseManager.getInstance().unregisterListener(this);
	}
}