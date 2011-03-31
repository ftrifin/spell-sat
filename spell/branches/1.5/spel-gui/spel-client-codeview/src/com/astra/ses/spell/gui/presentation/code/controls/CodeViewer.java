///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : CodeViewer.java
//
// DATE      : 2008-11-24 08:34
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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.presentation.code.CodePresentation;
import com.astra.ses.spell.gui.presentation.code.controls.drawing.CodeItemDrawer;
import com.astra.ses.spell.gui.presentation.code.controls.drawing.SyntaxDrawer;
import com.astra.ses.spell.gui.presentation.code.controls.drawing.SyntaxFormatter;
import com.astra.ses.spell.gui.presentation.code.controls.drawing.TableSizer2;
import com.astra.ses.spell.gui.presentation.code.dialogs.ItemInfoDialog;
import com.astra.ses.spell.gui.procs.model.ProcedureCode;
import com.astra.ses.spell.gui.procs.model.ProcedureLine;
import com.astra.ses.spell.gui.procs.model.StackHelper;
import com.astra.ses.spell.gui.procs.model.LineExecutionModel.ItemInfo;
import com.astra.ses.spell.gui.services.ConfigurationManager;


/*******************************************************************************
 * @brief This viewer uses a table for showing the procedure code and the
 *        execution live status.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class CodeViewer extends TableViewer
{
	/***************************************************************************
	 * This interface will allow ItemInfoDialog to receive notifications
	 **************************************************************************/
	public interface ItemNotificationListener
	{
		/***********************************************************************
		 * Notify when an ItemNotification object arrives
		 * @param data
		 **********************************************************************/
		public void notifyItem(ItemNotification data, String csPosition);
	}
	
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Resource manager handle */
	private static ConfigurationManager s_rsc = null;
	/** Maximum font size */
	private static final int MAX_FONT_SIZE = 16;
	/** Minimum font size */
	private static final int MIN_FONT_SIZE = 7;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Column names */
	private static final String[] COLUMN_NAMES = {" ", "#", "Code", "Name", "Value", "Status"};
	/** Identifier for executed times column */
	public static final int EXEC_COLUMN = 0;
	/** Identifier for the line no column */
	public static final int NUM_COLUMN   = 1;
	/** Identifier for the code text column */
	public static final int CODE_COLUMN  = 2;
	/** Identifier for the item name column */
	public static final int NAME_COLUMN  = 3;
	/** Identifier for the value column */
	public static final int VALUE_COLUMN = 4;
	/** Identifier for the status column */
	public static final int STS_COLUMN   = 5;
	/** Initial column widths, in percentage */
	private static final double COLUMN_PW[] = {0.02,  0.04, 0.57, 0.1, 0.13, 0.14};
	/** Data key */
	private static final String TITEM_DATA_KEY = "PROC_LINE";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Holds the procedure identifier */
	private String m_procedureId;
	/** Call stack position */
	private String m_csPosition;
	/** Table containing the procedure code */
	private Table m_table;
	/** Current execution line */
	private int m_currentLine;
	/** True if the viewer is initialized */
	private boolean m_initialized;
	/** Autoscroll flag */
	private boolean m_autoScroll;
	/** Currently selected font size */
	private int m_fontSize;
	/** Currently selected background color */
	private Color m_currentColor;
	/** Table item special drawer */
	private CodeItemDrawer m_itemDrawer;
	/** Procedure code syntax drawer (table item drawer) */
	private SyntaxDrawer m_syntaxDrawer;
	/** Procedure code syntax formatter */
	private SyntaxFormatter m_syntaxFormatter;
	/** Table size calculator */
	private TableSizer2 m_sizer;
	/** Holds the container view */
	private CodePresentation m_presentation;
	/** holds the viewer container */
	private Composite m_container;
	/** Notification listeners */
	private ArrayList<ItemNotificationListener> m_notificationListeners;
	
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
	public CodeViewer( String procId, Composite container, CodePresentation presentation)
	{
		super(container, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.READ_ONLY | SWT.VIRTUAL );
		m_table = getTable();
		m_presentation = presentation;
		m_container = container;
		if (s_rsc == null)
		{
			s_rsc = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		}
		Font font = s_rsc.getFont("CODE");
		setFont(font);
		m_itemDrawer = new CodeItemDrawer(this);
		m_syntaxFormatter = new SyntaxFormatter( m_fontSize );
		m_syntaxDrawer = new SyntaxDrawer(m_syntaxFormatter);
		m_autoScroll = true;
		m_procedureId = procId;
		m_csPosition = null;
		m_table.setHeaderVisible(true);
		m_table.setLinesVisible(false);
		m_table.setLayoutData(new GridData(GridData.FILL_BOTH));
		m_currentLine = 0;
		m_initialized = false;

		m_currentColor = s_rsc.getProcedureColor(ExecutorStatus.UNINIT);
		
		createColumns();

		m_sizer = new TableSizer2(container, m_table, CODE_COLUMN, COLUMN_PW);
		
		/*
		 * MouseAdapter is used for catching double click events
		 * and show item infor dialogs
		 */
		m_table.addMouseListener(new MouseAdapter()
		{
			public void mouseDoubleClick(MouseEvent e)
			{
				// FIXME : At this moment only one ItemInfoDialog is allowed
				if (m_notificationListeners.size() > 0)
				{
					return;
				}
				Point p = new Point(e.x,e.y);
				TableItem item = m_table.getItem(p);
				if (item != null)
				{
					ProcedureLine line = (ProcedureLine) item.getData(TITEM_DATA_KEY);
					if (line.getNumInfoElements(false)!=0)
					{
						ItemInfoDialog dialog = 
							new ItemInfoDialog(getControl().getShell(), m_procedureId, line, m_csPosition);
						m_notificationListeners.add(dialog);
						dialog.open();
						m_notificationListeners.remove(dialog);
					}
				}
			}
		});
		
		/*
		 * This selection listener is added to the table for allowing 
		 * Selection/Deselection of table rows
		 */
		m_table.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem item = (TableItem) e.item;
				e.doit = switchSelection(item);
			}	
		});
		
		m_notificationListeners = new ArrayList<ItemNotificationListener>();
	}

	/***************************************************************************
	 * Enable or disable the viewer
	 **************************************************************************/
	public void setEnabled( boolean enable )
	{
		if (enable)
		{
			m_table.setBackground(m_currentColor);
		}
		else
		{
			m_table.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		}
	}

	/***************************************************************************
	 * Enable or disable the autoscroll
	 **************************************************************************/
	public void setAutoScroll( boolean enable )
	{
		m_autoScroll = enable;
	}

	/***************************************************************************
	 * Update the table
	 **************************************************************************/
	public void updateCode( boolean forceReload )
	{
		// The model will determine which source code is to be shown. When
		// the calls stack changes, the code obtained here will be the current
		// subprocedure. As the code ID will be different from the current
		// call stack position in this viewer, the rows will be regenerated.
		Logger.debug("Updating code", Level.GUI, this);
		ProcedureCode code = m_presentation.getView().getModel().getCurrentViewCode();

		if (code == null || 
			code.getLines() == null || 
			code.getLines().size() == 0)
		{
			Shell s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			MessageDialog.openError(s, "Procedure load error",
									"Unable to obtain procedure code");
			return;
		}
		
		// Do nothing if the are already in that code
		if (!forceReload && code.getCodeId().equals(m_csPosition))
		{
			m_table.redraw();
			return;
		}
		
		Logger.debug("Item count      : " + m_table.getItemCount(), Level.GUI, this);
		Logger.debug("Current code    : " + code.getCodeId(), Level.GUI, this);
		m_table.setRedraw(false);

		// (Re)generate the table rows when there is no code (first time)
		// or when the call stack position has changed.
		if (m_table.getItemCount()==0)
		{
			Logger.debug("Creating full table for proc " + m_procedureId, Level.GUI, this);
			generateTableRows( code );
		}
		else if (m_csPosition == null || !m_csPosition.equals(code.getCodeId()) || forceReload)
		{
			Logger.debug("Updating full table for proc " + m_procedureId, Level.GUI, this);
			generateTableRows( code );
		}
		
		fillTableRows( code );
		
		if (!m_initialized)
		{
			initializeTableHandlers();
			m_initialized = true;
		}
		
		m_table.setFont(m_syntaxFormatter.getCodeFont());
		m_table.setRedraw(true);
		showLastLine();
		m_currentLine = code.getCurrentLine();
		notifyLine(m_currentLine);
	}

	/***************************************************************************
	 * Obtain the code id currently shown by the viewer
	 **************************************************************************/
	public String getCurrentPosition()
	{
		return m_csPosition;
	}

	/***************************************************************************
	 * Obtain the currently highlighted line
	 **************************************************************************/
	public int getCurrentLine()
	{
		return m_currentLine;
	}

	/***************************************************************************
	 * Obtain the current background color. Required for information items
	 * painter
	 **************************************************************************/
	public Color getCurrentBackground()
	{
		return m_currentColor;
	}
	
	/***************************************************************************
	 * SPELL notification callback
	 * 
	 * @param data
	 * 		Notification information
	 **************************************************************************/
	public void notifyItem( ItemNotification data )
	{
		// We shall use the line number corresponding to the currently shown
		// procedure
		//Logger.debug("Code viewer currently showing " + m_csPosition, Level.GUI, this);
		Vector<String> stack = data.getStackPosition();
		String stackStr = "";
		for(String elem : stack) stackStr += ":" + elem;
		//Logger.debug("Code viewer add notification, stack " + stackStr, Level.GUI, this);
		
		// We shall refresh all the line numbers corresponding to the currently
		// showed code. These can be more than one when using local step overs.
		ArrayList<Integer> lineNos = new ArrayList<Integer>();
		for(int idx = 0; idx<stack.size()-1; idx++)
		{
			if (StackHelper.getStackElement(stack,idx).equals(m_csPosition))
			{
				lineNos.add(Integer.parseInt(stack.get(idx+1)));
			}
		}
		for(int lineNo : lineNos)
		{
			if ((lineNo)>m_table.getItemCount())
			{
				Logger.error("Line notification out of range: " + 
						(lineNo) + ">" + m_table.getItemCount(), Level.GUI, this);
				return;
			}
			updateLine(lineNo);
		}
		/*
		 * Notify listeners
		 */
		for (ItemNotificationListener listener : m_notificationListeners)
		{
			listener.notifyItem(data, m_csPosition);
		}
		showLastLine();
	}

	/***************************************************************************
	 * SPELL line notification callback
	 * 
	 * @param line
	 * 		Current execution line
	 * @param eos
	 * 		True if the end of the procedure has been reached
	 **************************************************************************/
	public void notifyLine( LineNotification line )
	{
		if (m_table.getItemCount() == 0) return;
		// Process the line notification only if the stack matches with the
		// currently shown proc
		Vector<String> stack = line.getStackPosition();
		String[] proc_line = StackHelper.getViewElement(stack);
		if  (proc_line == null) return;
		int lineNo = Integer.parseInt(proc_line[1]);
		if (!proc_line[0].equals(m_csPosition))
		{
			// If the line notification does not match the code position, we 
			// shall change to it before (it means that there was a code jump
			// like returning from an external function)
			updateCode(false);
		}
		notifyLine(lineNo);
	}

	/***************************************************************************
	 * SPELL proc status notification callback
	 * 
	 * @param status
	 * 		Procedure status code
	 **************************************************************************/
	public void notifyProcStatus( ExecutorStatus status )
	{
		//Logger.debug("Received status: " + ProcedureHelper.toString(status), Level.GUI, this);
		if (status != ExecutorStatus.BUSY)
		{
			m_currentColor = s_rsc.getProcedureColor(status);
			m_table.setRedraw(false);
			m_itemDrawer.setBackground(m_currentColor);
			m_table.setBackground(m_currentColor);
			m_table.setRedraw(true);
		}
	}
	
	/***************************************************************************
	 * Gain the focus
	 **************************************************************************/
	public void setFocus()
	{
		m_table.setFocus();
	}

	/***************************************************************************
	 * Increase or decrease the font size
	 * @param increase
	 * 		If true, increase the font size 
	 **************************************************************************/
	public void zoom( boolean increase )
	{
		boolean changed = true;
		if (increase)
		{
			m_fontSize++;
			if (m_fontSize > MAX_FONT_SIZE) 
			{
				m_fontSize = MAX_FONT_SIZE;
				changed = false;
			}
		}
		else
		{
			m_fontSize--;
			if (m_fontSize < MIN_FONT_SIZE) 
			{
				m_fontSize = MIN_FONT_SIZE;
				changed = false;
			}
		}
		if (changed)
		{
			m_syntaxFormatter.setFontSize(m_fontSize);
			updateFontWithSize();
			updateCode(false);
			showLastLine();
		}
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	protected void setFont( Font font )
	{
		m_fontSize = font.getFontData()[0].getHeight();
		getTable().setFont(font);
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	protected void updateFontWithSize()
	{
		Font font = s_rsc.getFont("CODE",m_fontSize);
		getTable().setFont(font);
	}

	/***************************************************************************
	 * Initialize the table drawers and the table initial settings
	 **************************************************************************/
	protected void initializeTableHandlers()
	{
		m_table.addListener(SWT.EraseItem, m_itemDrawer);
		m_table.addListener(SWT.PaintItem, m_syntaxDrawer);
		// Now that we've set the text into the columns,
		// we call pack() on each one to size it to the
		// contents
		TableColumn[] columns = m_table.getColumns();
		int w = m_table.getBounds().width;
		columns[CODE_COLUMN].setWidth( (int) (COLUMN_PW[CODE_COLUMN]*w) );
		columns[NAME_COLUMN].setWidth( (int) (COLUMN_PW[NAME_COLUMN]*w) );
		columns[STS_COLUMN].setWidth( (int) (COLUMN_PW[STS_COLUMN]*w) );
		columns[EXEC_COLUMN].setWidth( (int) (COLUMN_PW[EXEC_COLUMN]*w) );
		m_container.addControlListener(m_sizer);

		// Set redraw back to true so that the table
		// will paint appropriately
		m_table.setBackground(m_currentColor);
	}
	
	/***************************************************************************
	 * Generate the table rows using the procedure code model
	 * 
	 * @param code
	 * 		The procedure code model
	 **************************************************************************/
	protected void generateTableRows( ProcedureCode code )
	{
		m_csPosition = code.getCodeId();
		m_table.removeAll();
		int  count = 0;
		for (ProcedureLine line : code.getLines())
		{
			count++;
			TableItem titem = new TableItem(m_table, SWT.NONE);
			titem.setText(NUM_COLUMN, new Integer(count).toString());
			titem.setData("PROC_LINE", line);
		}
		Logger.debug("Rows generated: " + count, Level.GUI, this);
	}

	/***************************************************************************
	 * Update the table rows contents using the procedure code model
	 * 
	 * @param code
	 * 		The procedure code model
	 **************************************************************************/
	protected void fillTableRows( ProcedureCode code )
	{
		Logger.debug("Filling table rows for " + code.getCodeId(), Level.GUI, this);
		for (TableItem item : m_table.getItems())
		{
			updateInfo(item);
		}
	}

	/***************************************************************************
	 * Update the information columns for the given line
	 * @param titem The corresponding table item (row)
	 * @param line The procedure line providing information
	 **************************************************************************/
	protected void updateInfo( TableItem titem )
	{
		ProcedureLine line = (ProcedureLine) titem.getData("PROC_LINE");
		Vector<ItemInfo> rows = line.getItemData(true);
		int visits = line.getNumVisits();
		String times = (visits > 0) ? "\u2022" : "";
		if (rows!=null)
		{
			int infoCount = rows.size();
			String name = "";
			String value = "";
			String status = "";
			if (infoCount==1)
			{
				name = rows.firstElement().name;
				value = rows.firstElement().value;
				status = rows.firstElement().status;
			}
			else if (infoCount>1)
			{
				int successCount = line.getNumSuccessInfoElements(true);
				int totalCount   = line.getNumInfoElements(true);
				status = line.getOverallStatus(true) + " (" + successCount + "/" + totalCount + ")";
			}
			titem.setText(NAME_COLUMN, name);
			titem.setText(VALUE_COLUMN, value);
			titem.setText(STS_COLUMN, status);
		}
		else
		{
			titem.setText(NAME_COLUMN, "");
			titem.setText(VALUE_COLUMN, "");
			titem.setText(STS_COLUMN, "");
		}
		titem.setText(EXEC_COLUMN, times);
	}
	
	/***************************************************************************
	 * Update a given line getting the information from the code model
	 * 
	 * @param idx
	 * 		Line number
	 **************************************************************************/
	protected void updateLine( int idx )
	{
		//TODO remove this, this is a workarround for a bug in the executor
		if (idx<=0) return;
		Logger.debug("Updating line index "+ idx + " in " + m_csPosition, Level.GUI, this);
		TableItem item = m_table.getItem(idx-1);
		updateInfo(item);
		m_table.redraw();
	}
	
	/***************************************************************************
	 * Perform line update after notifications
	 * 
	 * @param lineNo
	 * 		Current execution line
	 **************************************************************************/
	protected void notifyLine( int lineNo )
	{
		if (m_table.getItemCount() == 0) return;
		int tableIndex = lineNo;
		if (tableIndex > m_table.getItemCount())
		{
			Logger.warning("Line notification out of range: " + 
					(tableIndex) + ">=" + m_table.getItemCount(), Level.GUI, this);
			return;
		}
		m_currentLine = tableIndex;
		showLastLine();
		updateLine(m_currentLine);
	}

	
	/***************************************************************************
	 * Create the table columns
	 **************************************************************************/
	protected void createColumns()
	{
		for (int i = 0, n = COLUMN_NAMES.length; i < n; i++)
		{
			// Create the TableColumn with right alignment
			TableColumn column = new TableColumn(m_table, SWT.RIGHT);

			// This text will appear in the column header
			if (i == CODE_COLUMN)
			{
				column.setAlignment(SWT.LEFT);
			}
			else
			{
				column.setAlignment(SWT.CENTER);
			}
			column.setText(COLUMN_NAMES[i]);
		}
	}

	/***************************************************************************
	 * Center the table view on the currently executed line.
	 **************************************************************************/
	public void showLastLine()
	{
		if (m_autoScroll && m_currentLine>0 && m_table.getItemCount()>m_currentLine)
		{
			Rectangle area = m_table.getClientArea();
			int itemHeight = m_table.getItemHeight();
			int visibleCount = (area.height + itemHeight -1)/itemHeight;

			int currentY = m_table.getItem(m_currentLine).getBounds().y;
			int itemToShow = 0;
			if (currentY<0)
			{
				itemToShow = m_currentLine - (visibleCount/2)-1;
			}
			else
			{
				itemToShow = m_currentLine + (visibleCount/2)-1;
			}
			if (itemToShow>=m_table.getItemCount())
			{
				itemToShow = m_table.getItemCount()-1;
			}
			else if (itemToShow<0)
			{
				itemToShow = 1;
			}
			m_table.showItem(m_table.getItem(itemToShow));
		}
	}	
	
	/***************************************************************************
	 * Switch the tabl row to selected/deselected
	 * @param indexSelection
	 **************************************************************************/
	public boolean switchSelection(TableItem item)
	{
		int itemIndex = m_table.indexOf(item);
		Object selection = item.getData("SELECT");
		boolean selected = true;
		if (selection != null)
		{
			selected = !(Boolean) selection;
		}
		item.setData("SELECT", selected);
		if (selected)
		{
			m_table.select(itemIndex);
		}
		else
		{
			m_table.deselect(itemIndex);
		}
		return selected;
	}
}
