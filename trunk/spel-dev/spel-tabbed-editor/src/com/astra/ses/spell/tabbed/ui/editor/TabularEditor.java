///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.tabbed.ui.editor
// 
// FILE      : TabularEditor.java
//
// DATE      : 2009-11-23
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
package com.astra.ses.spell.tabbed.ui.editor;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.astra.ses.spell.tabbed.ui.editor.menu.TableEditorMenuManager;
import com.astra.ses.spell.tabbed.ui.table.ColumnFilter;
import com.astra.ses.spell.tabbed.ui.table.FilterUtils;
import com.astra.ses.spell.tabbed.ui.table.TabularContentProvider;
import com.astra.ses.spell.tabbed.ui.table.TabularLabelProvider;
import com.astra.ses.spell.tabbed.ui.table.jobs.SaveTableJob;


/******************************************************************************
 * TabularEditopr will show tabbed file as a table and will allow to 
 * add new registers
 * @author jpizar
 *
 *****************************************************************************/
public abstract class TabularEditor extends EditorPart {
	
	/** Editor ID */
	public final static String ID = "spel.astra.spell.tabbed.Editor";
	/** Flag for checking if the editor has been modified */
	private boolean m_isDirty;
	/** Current editing cell label */
	private Label m_currentCellLabel;
	/** Table */
	private TableViewer m_table;
	/** Table cursor for moving inside the table with the keyboard */
	private TableCursor m_cursor;
	/** Table cells editor */
	private ControlEditor m_cellEditor;
	private Text m_cellEditorControl;
	/** Editor input */
	private TabularEditorInput m_input;
	/** Clipboard for storing copied, cut contents */
	private Clipboard m_clipboard;
	
	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public TabularEditor() {
		m_isDirty = false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		String filePath = m_input.getFilePath();
		Table table = m_table.getTable();
		SaveTableJob save = new SaveTableJob(filePath, table);
		save.run(monitor);
		setDirty(false);
	}

	@Override
	public void doSaveAs() {}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		m_input = createTableEditorInput(input);
		setSite(site);
		setInput(m_input);
		setPartName(m_input.getName());
	}
	
	/**************************************************************************
	 * Mark the editor as dirty
	 *************************************************************************/
	public void setDirty(boolean newValue)
	{
		m_isDirty = newValue;
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		return m_isDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, true);
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parent.setLayout(parentLayout);
		
		/*
		 * Create clipboard object
		 */
		m_clipboard = new Clipboard(parent.getDisplay());
		
		/*
		 * Composite for showing the current cell value
		 */
		Composite currentCellComposite = new Composite(parent, SWT.NONE);
		GridData compositeData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		currentCellComposite.setLayoutData(compositeData);
		GridLayout cellLayout = new GridLayout(2, false);
		cellLayout.marginHeight = 0;
		cellLayout.marginWidth = 0;
		currentCellComposite.setLayout(cellLayout);
		// LABEL
		Label cellLabel = new Label(currentCellComposite, SWT.NONE);
		cellLabel.setText("Value: ");
		// TEXT WIDGET
		m_currentCellLabel = new Label(currentCellComposite, SWT.BORDER);
		GridData cellData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		m_currentCellLabel.setLayoutData(cellData);		
		/*
		 * Table
		 */
		/* Number of columns */
		int numColumns = m_input.getColumnCount();
		/* Create table viewer */
		m_table = new TableViewer(parent, SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		/* Configure table presentation */
		configureTable(m_table);
		/* Set table contents */
		m_table.setInput(m_input);  
		
	    /*
	     * Filter composite
	     */
		
	    if (!filteringAllowed())
	    {
	    	return;
	    }
	    
	    Composite filterComposite = new Composite(parent, SWT.NONE);
		GridLayout filterLayout = new GridLayout((numColumns * 2) + 1, false);
		filterLayout.marginHeight = 0;
		filterLayout.marginWidth = 0;
		filterComposite.setLayout(filterLayout);
		/*
		 * Filter Label
		 */
		Label filterLabel = new Label(filterComposite, SWT.NONE);
		filterLabel.setText("Filter columns: ");
		/*
		 * Text widgets for filtering columns
		 */
		for (int i = 0; i < numColumns; i++)
		{
			Label colLabel = new Label(filterComposite, SWT.NONE);
			colLabel.setText(m_table.getTable().getColumn(i).getText());
			
			Text colFilterText = new Text(filterComposite, SWT.BORDER);
			final ColumnFilter colViewerFilter = new ColumnFilter(Pattern.compile(FilterUtils.globify("")), i);
			m_table.addFilter(colViewerFilter);
			colFilterText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e) {	
					Text filterText = (Text) e.widget;
					/*
					 * Filter should be case insensitive
					 */
					final String filter = filterText.getText().toUpperCase();
					try
					{
						Pattern regex = Pattern.compile(FilterUtils.globify(filter));
						colViewerFilter.setPattern(regex);
						m_table.refresh();
					}
					catch (PatternSyntaxException p)
					{
						// do nothing
					}
				}
			});
		}
	}

	@Override
	public void setFocus() {}
	
	/**************************************************************************
	 * Required by the editoraction bar contributor
	 * @return
	 *************************************************************************/
	public TableViewer getViewer()
	{
		return m_table;
	}
	
	/**************************************************************************
	 * Convert editor input to an appropiate editor input
	 *************************************************************************/
	protected abstract TabularEditorInput createTableEditorInput(IEditorInput input);
	
	/***************************************************************************
	 * Get tabular editor input
	 * @return
	 **************************************************************************/
	protected TabularEditorInput getTabularInput()
	{
		return m_input;
	}
	
	/***************************************************************************
	 * Set if cell lines shall be visible
	 * @return
	 **************************************************************************/
	protected abstract boolean linesVisible();
	
	/***************************************************************************
	 * Set if column will allow filtering
	 * @return
	 **************************************************************************/
	protected abstract boolean filteringAllowed();
	
	/***************************************************************************
	 * Configure table layout
	 * Default table settings are:
	 * 	- Number of columns depending on the input
	 *  - Table cells are editable
	 *  - Table columns have same width
	 *  - Table columns resize to fit width
	 **************************************************************************/
	protected void configureTable(TableViewer tableViewer)
	{
		/*
		 * Columns layout
		 */
		int numColumns = m_input.getColumnCount();
		
		tableViewer.setContentProvider(new TabularContentProvider(numColumns));
		tableViewer.setLabelProvider(new TabularLabelProvider(TabularEditorInput.COMMENT));

		/*
		 * TABLE CONFIGURATION
		 */
		Table table = tableViewer.getTable();
		// TABLE LAYOUT
		GridData viewerData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		table.setLayoutData(viewerData);
		// PRESENTATION PROPERTIES
		table.setLinesVisible(linesVisible());
		// Add a control listener to adjust columns width to the table width
		table.addControlListener(new ControlListener()
		{
			@Override
			public void controlMoved(ControlEvent e) {}

			@Override
			public void controlResized(ControlEvent e) {
				Table table = (Table) e.widget;
				Rectangle area = table.getClientArea();
				int newTotalWidth = area.width;
				int oldTotalWidth  = 0;
				for (TableColumn column : table.getColumns())
				{
					oldTotalWidth += column.getWidth();
				}
				for (TableColumn column : table.getColumns())
				{
					int oldWidth = column.getWidth();
					int newWidth = (newTotalWidth * oldWidth) / oldTotalWidth;
					column.setWidth(newWidth);
				}
			}
		});
		/*
		 * Table cursor for being able to select table cells with the keyboard
		 */
		m_cursor = new TableCursor(table, SWT.BORDER_SOLID);
		// CONTROL EDITOR FOR SHOWING A TEXT WIDGET INSIDE THE CURSOR
		m_cellEditor = new ControlEditor(m_cursor);
		m_cellEditor.grabHorizontal = true;
		m_cellEditor.grabVertical = true;
		/*
		 * Selection listener
		 */
		m_cursor.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				/*TableCursor cursor = (TableCursor) e.widget;
				Table table = (Table) cursor.getParent();
				table.setSelection(new TableItem[] {cursor.getRow()});*/
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				stopEdition();
				updatePresentation();
			}
		});
		/*
		 * add mouse listener for single click, double click events
		 */
		m_cursor.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				startEdition();
			}
		});
		/*
		 * Insert a new row when CTRL+Enter is pressed
		 * Give focus to the cell editor when pressing enter
		 */
		KeyAdapter adapter = new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				// If Control key is pressed
				if ((e.stateMask & SWT.CTRL) != 0)
				{
					if (e.character == SWT.CR) // new row
					{
						insertRow();
					}
					else if (e.character == SWT.DEL) // delete row
					{
						deleteRow();
					}
					else if (e.keyCode == 'c' || e.keyCode == 'C') // copy cell
					{
						copy();
					}
					else if (e.keyCode == 'v' || e.keyCode == 'V') // paste cell
					{
						paste();
					}
					else if (e.keyCode == 'x' || e.keyCode == 'X') // cut cell
					{
						cut();
					}
				}
				//Clear cell content
				else if (e.character == SWT.DEL)
				{
					clearCell();
				}
				// If enter is pressed, the cell should be edited
				else if (e.character == SWT.CR)
				{
					startEdition();
				}
			}
		};
		m_cursor.addKeyListener(adapter);
		// There is need to add the listener to the table when the table has not 
		// any row, so the cursor is not enabled
		table.addKeyListener(adapter);

		String[] colNames = m_input.getColumnNames();
		
		for (int i = 0; i < numColumns; i++)
		{
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText(colNames[i]);
			column.setWidth(200);
			column.setResizable(true);
		}

		/*
		 * Create popup menu for the table
		 */
		TableEditorMenuManager popupMenu = new TableEditorMenuManager(m_table, m_cursor, m_clipboard);
	    final Menu menu = popupMenu.createContextMenu(table);
	    menu.addMenuListener(new MenuListener()
	    {
			@Override
			public void menuHidden(MenuEvent e) {}

			@Override
			public void menuShown(MenuEvent e) {
				for (MenuItem item : menu.getItems())
				{
					item.setEnabled(item.isEnabled());
				}
			}
	    	
	    });
	    m_cursor.setMenu(menu);
	    table.setMenu(menu);
	}
	
	/***************************************************************************
	 * Insert a row where the cursor is or in the first row
	 **************************************************************************/
	private void insertRow()
	{
		Table table = m_table.getTable();
		TableItem currentRow = m_cursor.getRow();
		int rowPosition = 0;
		if (currentRow != null)
		{
			rowPosition = Math.max(table.indexOf(currentRow),0);
		}
		m_input.addBlankLine(rowPosition);
		m_currentCellLabel.setText("");
		table.setSelection(rowPosition);
		m_cursor.setSelection(rowPosition, 0);
		updatePresentation();
		setDirty(true);
	}
	
	/***************************************************************************
	 * Delete the row where the cursor is
	 **************************************************************************/
	private void deleteRow()
	{
		Table table = m_table.getTable();
		TableItem currentRow = m_cursor.getRow();
		int currentCol = m_cursor.getColumn();
		int rowPosition = 0;
		if (currentRow != null)
		{
			rowPosition = Math.max(table.indexOf(currentRow),0);
		}
		m_input.removeLine(rowPosition);
		String previous = "";
		if (currentRow != null)
		{
			previous = currentRow.getText(currentCol);
		}
		m_currentCellLabel.setText(previous);

		if (m_input.getRowCount() == 0)
		{
			table.setFocus();
		}
		updatePresentation();
		setDirty(true);
	}
	
	/***************************************************************************
	 * Cursor position has changed, so we must update the widgets
	 **************************************************************************/
	private void updatePresentation()
	{
		TableItem row = m_cursor.getRow();
		int col = m_cursor.getColumn();
		String cellContent = row.getText(col);
		m_currentCellLabel.setText(cellContent);
		m_cursor.redraw();
	}
	
	/***************************************************************************
	 * An event trigger edition start
	 **************************************************************************/
	private void startEdition()
	{
		/*
		 * Create text widget
		 */
		m_cellEditorControl = new Text(m_cursor, SWT.NONE);
		/*
		 * Add key listener for setting changes
		 */
		m_cellEditorControl.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				Text text = (Text) e.widget;
				TableItem row = m_cursor.getRow();
				int currentCol = m_cursor.getColumn();
				int col = m_cursor.getColumn();
				if (e.character == SWT.CR) {
					if ((row != null) && (currentCol != -1))
					{
						String newValue = text.getText();
						int rowPos = m_table.getTable().indexOf(row);
						row.setText(currentCol, newValue);
						row.setData(String.valueOf(col),newValue);
						m_input.setValue(rowPos, currentCol, newValue);
						stopEdition();
						updatePresentation();
						setDirty(true);
					}
					m_cursor.setFocus();
				}
				else if (e.character == SWT.ESC)
				{
					stopEdition();
					updatePresentation();
				}
			}
		});
		/*
		 * Add a modify listener for updating the current cell label
		 */
		m_cellEditorControl.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e) {
				Text text = (Text) e.widget;
				String newText = text.getText();
				m_currentCellLabel.setText(newText);
			}
		});
		/*
		 * Set Control contents
		 */
		TableItem row = m_cursor.getRow();
		int col = m_cursor.getColumn();
		String content = row.getText(col);
		m_cellEditorControl.setText(content);
		m_cellEditorControl.setSelection(content.length());
		/*
		 * Set the control to the cell editor 
		 */
		m_cellEditor.setEditor(m_cellEditorControl);
		m_cursor.setFocus();
	}
	
	/***************************************************************************
	 * Edition must stop
	 **************************************************************************/
	private void stopEdition()
	{
		if ((m_cellEditorControl != null) && (!m_cellEditorControl.isDisposed()))
		{
			m_cellEditorControl.dispose();
			m_cellEditorControl = null;
			m_cellEditor.setEditor(null);
		}
	}
	
	/***************************************************************************
	 * Remove cell content
	 **************************************************************************/
	private void clearCell()
	{
		TableItem row = m_cursor.getRow();
		if (row != null)
		{
			int col = m_cursor.getColumn();
			int rowPosition = m_table.getTable().indexOf(row);
			String newValue = "";
			row.setText(col, newValue);
			row.setData(String.valueOf(col),newValue);
			m_input.setValue(rowPosition, col, newValue);
			updatePresentation();
			setDirty(true);
		}
	}
	
	/***************************************************************************
	 * Perform copy action
	 **************************************************************************/
	private void copy()
	{
		/*
		 * Retrieve cell content
		 */
		TableItem row = m_cursor.getRow();
		if (row != null)
		{
			int col = m_cursor.getColumn();
			String cellValue = row.getText(col);
			/*
			 * Put the content into the clipboard
			 */
			m_clipboard.setContents(new Object[]{cellValue}, 
					new Transfer[] {TextTransfer.getInstance()},
					DND.CLIPBOARD);
		}
	}
	
	/***************************************************************************
	 * Perform paste action
	 **************************************************************************/
	private void paste()
	{

		TableItem row = m_cursor.getRow();
		if (row != null)
		{
			// Retrieve clipboard text content
			Object clipContent = m_clipboard.getContents(TextTransfer.getInstance());
			if (clipContent == null)
			{
				return;
			}
			String clipboardContent = String.valueOf(clipContent);
			// Set cell content with the clipboard content
			int col = m_cursor.getColumn();
			int rowPosition = m_table.getTable().indexOf(row);
			row.setText(col, clipboardContent);
			row.setData(String.valueOf(col),clipboardContent);
			m_input.setValue(rowPosition, col, clipboardContent);
			// Clear the clipboard
			m_clipboard.clearContents(DND.CLIPBOARD);
		}
	}
	
	/***************************************************************************
	 * Perform cut action
	 **************************************************************************/
	private void cut()
	{
		/* Copu cell content */
		copy();
		/* Clear cell content */
		clearCell();
	}
}