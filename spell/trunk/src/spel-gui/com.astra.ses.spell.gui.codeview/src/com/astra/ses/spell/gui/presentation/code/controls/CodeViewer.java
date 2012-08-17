///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : CodeViewer.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.presentation.code.controls;

import java.nio.CharBuffer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.presentation.code.controls.drawing.CodeColorProvider;
import com.astra.ses.spell.gui.presentation.code.controls.drawing.TableEventDispatcher;
import com.astra.ses.spell.gui.presentation.code.controls.menu.CodeViewerMenuManager;
import com.astra.ses.spell.gui.presentation.code.dialogs.ItemInfoDialog;
import com.astra.ses.spell.gui.presentation.code.dialogs.SearchDialog;
import com.astra.ses.spell.gui.presentation.code.search.CodeSearch;
import com.astra.ses.spell.gui.presentation.code.search.CodeSearch.SearchMatch;
import com.astra.ses.spell.gui.presentation.code.syntax.ISyntaxFormatter;
import com.astra.ses.spell.gui.presentation.code.syntax.SyntaxFormatter;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineSummaryData;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/*******************************************************************************
 * @brief This viewer uses a table for showing the procedure code and the
 *        execution live status.
 * @date 09/10/07
 ******************************************************************************/
public class CodeViewer extends TableViewer implements IPropertyChangeListener
{
	/***************************************************************************
	 * This interface will allow ItemInfoDialog to receive notifications
	 **************************************************************************/
	public interface ItemNotificationListener
	{
		/***********************************************************************
		 * Notify when an ItemNotification object arrives
		 * 
		 * @param data
		 **********************************************************************/
		public void notifyItem(ItemNotification data, String csPosition);
	}

	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Data identifier */
	public static final String DATA_SOURCE = "com.astra.ses.spell.gui.presentation.code.Source";
	public static final String DATA_STATUS = "com.astra.ses.spell.gui.presentation.code.Status";
	public static final String DATA_SEARCH_RANGE = "com.astra.ses.spell.gui.presentation.code.SearchRange";
	/** Highlight color for searchs */
	private static Color s_hrColor = null;
	/** Font size range */
	private static final int FONT_RANGE = 4;

	private static IConfigurationManager s_cfg = null;

	/** Autoscroll flag */
	private boolean m_autoScroll;
	/** Currently selected font size */
	private int m_fontSize;
	/** Procedure code syntax formatter */
	private ISyntaxFormatter m_syntaxFormatter;
	/** Color provider for painters */
	private ITableColorProvider m_colorProvider;
	/** holds the viewer container */
	private Composite m_container;
	/** Min and max font size */
	private int m_minFontSize;
	private int m_maxFontSize;
	/** ItemInfo dialog */
	private ItemInfoDialog m_infoDialog;
	/** Procedure data provider */
	private IProcedure m_model;
	/** Holds the code search mechanism */
	private CodeSearch m_search;
	/**
	 * Holds the procedure status. Used to know which status transitions take
	 * place
	 */
	private ExecutorStatus m_status;

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param procId
	 *            The procedure identifier
	 * @param parent
	 *            The parent composite
	 **************************************************************************/
	public CodeViewer(Composite container, IProcedure model)
	{
		super(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);

		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}

		s_cfg.addPropertyChangeListener(this);

		m_status = ExecutorStatus.UNINIT;

		m_model = model;
		m_container = container;
		m_colorProvider = new CodeColorProvider(m_model.getDataProvider(), getTable());

		/*
		 * Determine min and max font size
		 */
		Font font = s_cfg.getFont(FontKey.CODE);
		int fontSize = font.getFontData()[0].getHeight();
		m_minFontSize = Math.max(1, fontSize - FONT_RANGE);
		m_maxFontSize = fontSize + FONT_RANGE;

		/*
		 * Create widgets
		 */
		setFont(font);
		m_syntaxFormatter = new SyntaxFormatter(font);
		m_autoScroll = true;
		getTable().setHeaderVisible(true);
		getTable().setLinesVisible(false);
		getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		getTable().setBackground(s_cfg.getProcedureColor(ExecutorStatus.LOADED));

		createColumns();

		/*
		 * MouseAdapter is used for catching double click events and show item
		 * infor dialogs
		 */
		getTable().addMouseListener(new MouseAdapter()
		{
			public void mouseDoubleClick(MouseEvent e)
			{
				Point p = new Point(e.x, e.y);
				TableItem item = getTable().getItem(p);
				if (item != null)
				{
					int lineNumber = getTable().indexOf(item) + 1;

					ILineSummaryData summary = null;
					try
					{
						summary = m_model.getDataProvider().getSummary(lineNumber);
					}
					catch (UninitProcedureException e1)
					{
					}

					if (summary != null)
					{
						m_infoDialog = new ItemInfoDialog(getControl().getShell(), m_model.getProcId(), m_model.getDataProvider(),
						        lineNumber);
						m_infoDialog.open();
						m_infoDialog = null;
					}
				}
			}
		});

		final CodeViewer theViewer = this;
		getTable().addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if ((e.stateMask & SWT.CONTROL) > 0)
				{
					if (e.keyCode == 99) // C
					{
						copySelected();
						getTable().deselectAll();
					}
					else if (e.keyCode == 102) // F
					{
						getTable().deselectAll();
						SearchDialog dialog = new SearchDialog(getTable().getShell(), theViewer);
						dialog.open();
					}
				}
			}
		});

		/*
		 * Popup menu manager
		 */
		new CodeViewerMenuManager(this, m_model.getProcId(), m_model.getDataProvider(), m_model.getRuntimeInformation());

		m_search = new CodeSearch(getTable());

		/*
		 * Initialize paint handlers
		 */
		initializeTableHandlers();
	}

	/***************************************************************************
	 * Dispose resources
	 **************************************************************************/
	public void dispose()
	{
		m_syntaxFormatter.dispose();
		((CodeColorProvider) m_colorProvider).dispose();
		s_cfg.removePropertyChangeListener(this);
	}

	/***************************************************************************
	 * Enable or disable the autoscroll
	 **************************************************************************/
	public void setAutoScroll(boolean enable)
	{
		m_autoScroll = enable;
	}

	/***************************************************************************
	 * Gain the focus
	 **************************************************************************/
	public void setFocus()
	{
		getTable().setFocus();
	}

	/***************************************************************************
	 * Increase or decrease the font size
	 * 
	 * @param increase
	 *            If true, increase the font size
	 **************************************************************************/
	public void zoom(boolean increase)
	{
		boolean changed = true;
		if (increase)
		{
			m_fontSize++;
			if (m_fontSize > m_maxFontSize)
			{
				m_fontSize = m_maxFontSize;
				changed = false;
			}
		}
		else
		{
			m_fontSize--;
			if (m_fontSize < m_minFontSize)
			{
				m_fontSize = m_minFontSize;
				changed = false;
			}
		}
		if (changed)
		{
			updateFontWithSize();
			getTable().redraw();
			showLastLine();
		}
	}

	/***************************************************************************
	 * Search for a string in the code
	 **************************************************************************/
	public int searchString(String toSearch)
	{
		clearMatches();
		int count = m_search.searchString(toSearch);
		if (count > 0)
		{
			showMatches(m_search.getMatches());
		}
		return count;
	}

	/***************************************************************************
	 * Show next occurrence
	 **************************************************************************/
	public boolean hasMatches()
	{
		return (m_search.getMatches() != null) && (m_search.getMatches().length > 0);
	}

	/***************************************************************************
	 * Show next occurrence
	 **************************************************************************/
	public boolean searchNext()
	{
		SearchMatch match = m_search.getNext();
		if (match == null)
			return false;
		getTable().deselectAll();
		getTable().select(match.lineNo - 1);
		showLine(match.lineNo, false);
		return true;
	}

	/***************************************************************************
	 * Show previous occurrence
	 **************************************************************************/
	public boolean searchPrevious()
	{
		SearchMatch match = m_search.getPrevious();
		if (match == null)
			return false;
		getTable().deselectAll();
		getTable().select(match.lineNo - 1);
		showLine(match.lineNo>1 ? match.lineNo -1 : match.lineNo, false);
		return true;
	}

	/***************************************************************************
	 * Highlight the given ranges
	 **************************************************************************/
	private void showMatches(SearchMatch[] matches)
	{
		if (s_hrColor == null)
			s_hrColor = Display.getCurrent().getSystemColor(SWT.COLOR_CYAN);
		getTable().setRedraw(false);
		for (SearchMatch match : matches)
		{
			TableItem item = getTable().getItem(match.lineNo - 1);
			// We will set colors later
			StyleRange range = new StyleRange(match.startOffset, match.length, null, s_hrColor);
			item.setData(DATA_SEARCH_RANGE, range);
		}
		getTable().deselectAll();
		getTable().setRedraw(true);
		getTable().select(matches[0].lineNo - 1);
		showLine(matches[0].lineNo, false);
	}

	/***************************************************************************
	 * Clear the highlight of the given ranges
	 **************************************************************************/
	public void clearMatches()
	{
		m_search.clear();
		getTable().setRedraw(false);
		getTable().deselectAll();
		for (TableItem item : getTable().getItems())
		{
			item.setData(DATA_SEARCH_RANGE, null);
		}
		getTable().setRedraw(true);
	}

	/***************************************************************************
	 * Copy selected source rows
	 **************************************************************************/
	public void copySelected()
	{
		TableItem[] selection = getTable().getSelection();
		Clipboard clipboard = new Clipboard(Display.getCurrent());
		String data = "";
		for (TableItem item : selection)
		{
			if (!data.isEmpty())
				data += "\n";
			String line = item.getData(CodeViewer.DATA_SOURCE).toString();
			if (line != null)
			{
				line = line.trim();
			}
			else
			{
				line = "";
			}
			data += line;
		}
		clipboard.setContents(new Object[] { data }, new Transfer[] { TextTransfer.getInstance() });
		clipboard.dispose();
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	protected void setFont(Font font)
	{
		m_fontSize = font.getFontData()[0].getHeight();
		getTable().setFont(font);
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	protected void updateFontWithSize()
	{
		Font oldFont = getTable().getFont();
		FontData[] data = oldFont.getFontData();
		for (FontData fdata : data)
		{
			fdata.setHeight(m_fontSize);
		}
		Font newFont = new Font(Display.getDefault(), data);
		getTable().setFont(newFont);
		m_syntaxFormatter.setFont(newFont);

		oldFont.dispose();
		getTable().getColumn(CodeViewerColumn.LINE_NO.ordinal()).pack();
	}

	/***************************************************************************
	 * Initialize the table drawers and the table initial settings
	 **************************************************************************/
	protected void initializeTableHandlers()
	{
		TableEventDispatcher dispatcher = new TableEventDispatcher(m_syntaxFormatter, m_model.getDataProvider(), m_colorProvider);

		getTable().addListener(SWT.EraseItem, dispatcher);
		// Now that we've set the text into the columns,
		// we call pack() on each one to size it to the
		// contents
		m_container.addControlListener(new ControlAdapter()
		{
			public void controlResized(ControlEvent e)
			{
				Rectangle area = m_container.getClientArea();
				if (area.width == 0)
					return;

				int totalWidth = area.width - 2 * getTable().getBorderWidth();
				if (getTable().getVerticalBar().isVisible())
				{
					totalWidth -= getTable().getVerticalBar().getSize().x;
				}

				int fixedWidth = 0;
				for (CodeViewerColumn column : CodeViewerColumn.values())
				{
					if (column == CodeViewerColumn.CODE)
						continue;
					TableColumn tColumn = getTable().getColumn(column.ordinal());
					fixedWidth += tColumn.getWidth();
				}

				if (getTable().getClientArea().width == 0)
				{
					for (CodeViewerColumn column : CodeViewerColumn.values())
					{
						TableColumn tColumn = getTable().getColumn(column.ordinal());
						tColumn.setWidth(column.getInitialWidth());
					}
				}
				else if (fixedWidth == 0) // Initial case only
				{
					for (CodeViewerColumn column : CodeViewerColumn.values())
					{
						TableColumn tColumn = getTable().getColumn(column.ordinal());
						int width = Math.min((int) (column.getWidthRatio() * totalWidth), 10);
						tColumn.setWidth(width);
					}
				}
				else
				{
					int width = totalWidth - fixedWidth - 4;
					getTable().getColumn(CodeViewerColumn.CODE.ordinal()).setWidth(width);
				}
			}
		});
	}

	/***************************************************************************
	 * Generate the table rows using the procedure code model
	 * 
	 * @param code
	 *            The procedure code model
	 **************************************************************************/
	protected void generateTableRows()
	{
		// Cleanup the table
		getTable().removeAll();
		// Get the code and generate the lines
		String[] sourceLines;
		try
		{
			sourceLines = m_model.getDataProvider().getCurrentSource(new NullProgressMonitor());
		}
		catch (Exception e)
		{
			if (m_model.getRuntimeInformation().getStatus().equals(ExecutorStatus.ERROR))
			{
				sourceLines = m_model.getDataProvider().getRootSource(new NullProgressMonitor());
			}
			else
			{
				return;
			}
		}

		for (int i = 0; i < sourceLines.length; i++)
		{
			TableItem titem = new TableItem(getTable(), SWT.NONE);
			titem.setText(CodeViewerColumn.LINE_NO.ordinal(), new Integer(i + 1).toString());
			int len = sourceLines[i].length();
			String expand = CharBuffer.allocate(len).toString().replace('\0', ' ');
			titem.setText(CodeViewerColumn.CODE.ordinal(), expand);
			// The executed line item is set in case we return from another node
			// execution
			titem.setData(CodeViewer.DATA_SOURCE, sourceLines[i]);
		}
		// Resize the line number column so all the digits can be shown
		getTable().getColumn(CodeViewerColumn.LINE_NO.ordinal()).pack();
	}

	/***************************************************************************
	 * Create the table columns
	 **************************************************************************/
	protected void createColumns()
	{
		for (CodeViewerColumn colModel : CodeViewerColumn.values())
		{
			// Create the TableColumn with right alignment
			TableViewerColumn viewerColumn = new TableViewerColumn(this, colModel.getAlignment());

			TableColumn column = viewerColumn.getColumn();
			column.setText(colModel.getName());
			column.setResizable(colModel.isResizable());
			column.setAlignment(colModel.getAlignment());
		}
	}

	/***************************************************************************
	 * Center the table view on the given line.
	 **************************************************************************/
	public void showLine(int lineNo, boolean select)
	{
		if (lineNo > 0 && getTable().getItemCount() > lineNo)
		{
			Rectangle area = getTable().getClientArea();
			int itemHeight = getTable().getItemHeight();
			int visibleCount = (area.height + itemHeight - 1) / itemHeight;

			int currentY = getTable().getItem(lineNo).getBounds().y;
			int itemToShow = 0;
			if (currentY < 0)
			{
				itemToShow = lineNo - (visibleCount / 2) - 1;
			}
			else
			{
				itemToShow = lineNo + (visibleCount / 2) - 1;
			}
			if (itemToShow >= getTable().getItemCount())
			{
				itemToShow = getTable().getItemCount() - 1;
			}
			else if (itemToShow < 0)
			{
				itemToShow = 1;
			}
			getTable().showItem(getTable().getItem(itemToShow));
			if (select)
			{
				getTable().deselectAll();
				getTable().select(lineNo - 1);
			}
		}
	}

	/***************************************************************************
	 * Center the table view on the currently executed line.
	 **************************************************************************/
	public void showLastLine()
	{
		if (!m_autoScroll)
			return;

		int currentLine;
		try
		{
			currentLine = m_model.getDataProvider().getCurrentLine();
		}
		catch (UninitProcedureException e)
		{
			return;
		}
		showLine(currentLine, false);
	}

	/***************************************************************************
	 * Updates the background color according to the executor status
	 * 
	 * @param st
	 **************************************************************************/
	public void setExecutorStatus(ExecutorStatus st)
	{
		getTable().setBackground(s_cfg.getProcedureColor(st));

		// Force source code retrieval when reloading, no matter what
		if (m_status.equals(ExecutorStatus.RELOADING))
		{
			codeChanged(true);
		}

		// Show last line in these cases
		switch (st)
		{
		case PAUSED:
		case PROMPT:
		case WAITING:
		case INTERRUPTED:
		case ERROR:
		case FINISHED:
		case ABORTED:
			showLastLine();
		}

		// Store the status
		m_status = st;
	}

	/***************************************************************************
	 * Update the information columns for the given line
	 * 
	 * @param titem
	 *            The corresponding table item (row)
	 * @param line
	 *            The procedure line providing information
	 **************************************************************************/
	private void updateItemInformation(TableItem titem)
	{
		Integer lineNumber = getTable().indexOf(titem) + 1;

		ILineSummaryData summary = null;
		try
		{
			summary = m_model.getDataProvider().getSummary(lineNumber);
		}
		catch (UninitProcedureException e1)
		{
			// No summary
		}
		String name = "";
		String value = "";
		String status = "";
		ItemStatus iStatus = null;

		if (summary != null)
		{
			/*
			 * The status without progress is retrieved in getComments()
			 */
			iStatus = summary.getSummaryStatus();
			status = iStatus.getName() + " (" + summary.getSuccessCount() + "/" + summary.getElementCount() + ")";
			name = summary.getName();
			name = name.split(":")[0];
			if (name.indexOf("@") != -1)
			{
				name = name.split("@")[1];
			}
			value = summary.getValue();
		}

		titem.setText(CodeViewerColumn.NAME.ordinal(), name);
		titem.setText(CodeViewerColumn.VALUE.ordinal(), value);
		titem.setText(CodeViewerColumn.STATUS.ordinal(), status);
		titem.setData(CodeViewer.DATA_STATUS, iStatus);
	}

	/***************************************************************************
	 * Update the table rows contents using the procedure code model
	 **************************************************************************/
	private void fillTableRows()
	{
		for (TableItem item : getTable().getItems())
		{
			updateItemInformation(item);
		}
	}

	/***************************************************************************
	 * Notify that a line has been executed
	 * 
	 * @param lineNumber
	 **************************************************************************/
	public void lineChanged(int lineNumber)
	{
		if (getTable().isDisposed())
			return;
		// redraw the table
		getTable().redraw();
		// Show last line
		showLastLine();
	}

	/***************************************************************************
	 * A notification has been received for a given item
	 * 
	 * @param lineNumber
	 **************************************************************************/
	public void newItemArrived(int lineNumber)
	{
		if (getTable().isDisposed())
			return;
		// Get affected lines
		Integer[] affectedLines;
		try
		{
			affectedLines = m_model.getDataProvider().getAffectedLines(lineNumber);
		}
		catch (UninitProcedureException e)
		{
			affectedLines = new Integer[0];
		}
		if (affectedLines.length > 0)
		{
			int infoLine = -1;
			if (m_infoDialog != null)
			{
				infoLine = m_infoDialog.getLineNumber();
			}
			for (Integer index : affectedLines)
			{
				TableItem item = getTable().getItem(index - 1);
				updateItemInformation(item);
				if (infoLine == index)
				{
					m_infoDialog.update();
				}
			}
			getTable().redraw();
			showLastLine();
		}
	}

	/***************************************************************************
	 * Code has changed, or it least the current execution model
	 **************************************************************************/
	public void codeChanged(boolean sourceCodeChanged)
	{
		// Protection
		if (getTable().isDisposed())
			return;
		if ((m_infoDialog != null) && sourceCodeChanged)
		{
			m_infoDialog.close();
			m_infoDialog = null;
		}
		// Re-create the table contents
		getTable().setRedraw(false);
		// Do not re-generate source lines if source didnt change!
		if (sourceCodeChanged)
		{
			try
			{
				String[] source = m_model.getDataProvider().getCurrentSource(new NullProgressMonitor());
				String codeId = m_model.getDataProvider().getCurrentCodeId();
				m_syntaxFormatter.newSource(codeId, source);
			}
			catch (Exception ex)
			{
			}
			;
			generateTableRows();
		}
		// Update the notifications
		fillTableRows();
		showLastLine();
		getTable().setRedraw(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		/*
		 * If code font has changed, then update it
		 */
		if (property.equals(FontKey.CODE.getPreferenceName()))
		{
			IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);

			Font font = cfg.getFont(FontKey.CODE);
			FontData[] data = font.getFontData();
			for (FontData fdata : data)
			{
				fdata.setHeight(m_fontSize);
			}
			setFont(font);
			m_syntaxFormatter.setFont(font);
			getTable().getColumn(CodeViewerColumn.LINE_NO.ordinal()).pack();
		}
		/*
		 * If any of the pocedure colors has changed, then update
		 */
		else if (property.startsWith(PreferenceCategory.PROC_COLOR.tag))
		{
			String statusStr = property.substring(PreferenceCategory.PROC_COLOR.tag.length() + 1);
			ExecutorStatus st = ExecutorStatus.valueOf(statusStr);

			/*
			 * If current status is the one whose color has changed, then update
			 * the table background
			 */
			ExecutorStatus current = m_model.getDataProvider().getExecutorStatus();
			if (current.equals(st))
			{
				getTable().setBackground( s_cfg.getProcedureColor(current) );
			}
		}
	}
}
