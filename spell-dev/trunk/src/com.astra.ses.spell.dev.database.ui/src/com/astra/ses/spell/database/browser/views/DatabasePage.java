///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.database.browser.views
//
// FILE      : DatabasePage.java
//
// DATE      : Feb 18, 2011
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.database.browser.views;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.Page;

import com.astra.ses.spell.dev.database.DatabaseManager;
import com.astra.ses.spell.dev.database.datatransfer.DatabaseTransferable;
import com.astra.ses.spell.dev.database.datatransfer.OfflineDatabaseTransfer;
import com.astra.ses.spell.dev.database.interfaces.IDatabaseElement;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.interfaces.ITelecommand;
import com.astra.ses.spell.dev.database.interfaces.ITelemetryParameter;

public abstract class DatabasePage extends Page
{
	/** Holds the resource name */
	private String m_projectName;
	/** Holds the parent composite */
	private Composite m_parent;
	/** Database details label */
	private Label m_lblProjName;
	/** Database version label */
	private Label m_lblDbName;
	/** Table viewer */
	private TableViewer m_viewer;
	/** Table viewer filter */
	private DatabaseViewerFilter m_viewerFilter;
	/** Text input widget for filtering the text */
	private Text m_filterText;
	/** Current database in use */
	private ISpellDatabase m_currentDB;
	/** True if the viewer has been initialized. We do not want
	 * to keep updating the viewer model, this is a static view!
	 */
	private boolean m_initialized;

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
		public Object[] getElements(Object parent) 
		{
			return (IDatabaseElement[]) parent;
		}
	}
	
	/**************************************************************************
	 * View label provider
	 * 
	 **************************************************************************/
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			IDatabaseElement element = (IDatabaseElement) obj;
			switch (index)
			{
				case 0: return element.getName();
				case 1: return element.getDescription();
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
	 **************************************************************************/
	class NameSorter extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			IDatabaseElement el1 = (IDatabaseElement) e1;
			IDatabaseElement el2 = (IDatabaseElement) e2;
			return el1.getName().compareTo(el2.getName());
		}
	}
	
	/**************************************************************************
	 * Viewer filter
	 *************************************************************************/
	class DatabaseViewerFilter extends ViewerFilter
	{
		/** filtering expression */
		private Pattern m_filterPattern;
		
		/**********************************************************************
		 * Constructor
		 *********************************************************************/
		public DatabaseViewerFilter(Pattern regex)
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
			IDatabaseElement dbelement = (IDatabaseElement) element;
			String name = dbelement.getName();
			return name.matches(m_filterPattern.toString());
		}
	}

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public DatabasePage( IFile file )
	{
		super();
		m_projectName = file.getProject().getName();
		m_currentDB = DatabaseManager.getInstance().getProjectDatabase(file);
		m_initialized = false;
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
		if (m_initialized) return;
		m_viewerFilter = new DatabaseViewerFilter(Pattern.compile(FilterUtils.globify("")));
		m_viewer.setContentProvider(new ViewContentProvider());
		m_viewer.setLabelProvider(new ViewLabelProvider());
		m_viewer.setSorter(new NameSorter());
		m_viewer.addFilter(m_viewerFilter);
    	if (!m_initialized)
    	{
    		if (m_currentDB == null)
    		{
    			m_currentDB = DatabaseManager.getInstance().getProjectDatabase(m_projectName);
    		}
	    	if (m_currentDB != null)
	    	{
				m_viewer.setInput(getInput());
				m_viewer.refresh();
				ViewUtils.adjustLabelContents(m_lblDbName, "Database: " + m_currentDB.getName() + " " + m_currentDB.getVersion());
				m_initialized = true;
	    	}
    	}
	}
	
	/**************************************************************************
	 * Create the page controls
	 *************************************************************************/
	@Override
    public void createControl(Composite parent)
    {
		m_parent = new Composite(parent, SWT.BORDER);
		m_parent.setLayoutData( new GridData( GridData.FILL_BOTH ));
		m_parent.setLayout( new GridLayout(1, true));
		
		m_lblProjName = new Label(m_parent, SWT.NONE);
		m_lblProjName.setText("Project: " + m_projectName);
		m_lblProjName.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		
		m_lblDbName = new Label(m_parent, SWT.NONE);
		m_lblDbName.setText("Database: (none)");
		m_lblDbName.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		
		m_viewer = new TableViewer(m_parent, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
		TableColumn nameCol = new TableColumn(m_viewer.getTable(), SWT.LEFT);
		nameCol.setText("Name");
		nameCol.setWidth(80);
		TableColumn descCol = new TableColumn(m_viewer.getTable(), SWT.LEFT);
		descCol.setText("Description");
		descCol.setWidth(140);
		
		/* Table viewer */
		GridData viewerData = new GridData(GridData.FILL_BOTH);
		m_viewer.getTable().setLayoutData(viewerData);	
		m_viewer.getTable().setHeaderVisible(true);
		m_viewer.getTable().setLinesVisible(false);

		/* Text widget */
		GridData textData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		textData.horizontalIndent = 0;
		textData.verticalIndent = 0;
		m_filterText = new Text(m_parent, SWT.BORDER);
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
		
		getSite().setSelectionProvider(m_viewer);
		initContents();
		initDragAction();
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
			public void dragFinished(DragSourceEvent event) 
			{
					event.data = null;
			}

			@Override
			public void dragSetData(DragSourceEvent event) 
			{
				if (OfflineDatabaseTransfer.getInstance().isSupportedType(event.dataType))
				{
		        	TelemetryView tmView = (TelemetryView) getSite().getWorkbenchWindow().getActivePage().findView(TelemetryView.ID);
		        	TelecommandView tcView = (TelecommandView) getSite().getWorkbenchWindow().getActivePage().findView(TelecommandView.ID);

		        	List<ITelemetryParameter> tmList = new ArrayList<ITelemetryParameter>();
		        	if (tmView != null)
		        	{
		        		tmList = tmView.getSelectedParameters();
			        	tmView.deselectAll();
		        	}

		        	List<ITelecommand> tcList = new ArrayList<ITelecommand>();
		        	if (tcView != null)
		        	{
		        		tcList = tcView.getSelectedCommands();
			        	tcView.deselectAll();
		        	}
			        event.data = new DatabaseTransferable( tmList, tcList );
				}
			}

			@Override
			public void dragStart(DragSourceEvent event) 
			{
				IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
				if (selection.size() < 1)
				{
					event.doit = false;
				}
			}	
		};
		
		m_viewer.addDragSupport(DND.DROP_COPY, types, dragListener);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public Control getControl()
    {
		return m_parent;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void setFocus()
    {
	    m_viewer.getControl().setFocus();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    public void update( ISpellDatabase db )
    {
    	if ((m_currentDB == null)||(!db.getDatabasePath().equals(m_currentDB.getDatabasePath())))
    	{
        	m_currentDB = db;
    		m_initialized = false;
    	}
	    initContents();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    protected ISpellDatabase getCurrentDB()
    {
		return m_currentDB;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    protected boolean hasCurrentDB()
    {
		return (m_currentDB!=null);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
    public ISelection getViewerSelectedElements()
    {
    	return m_viewer.getSelection();
    }

	/***************************************************************************
	 * Get the collection of IDatabaseElements from the database
	 **************************************************************************/
	protected abstract IDatabaseElement[] getInput();
}
