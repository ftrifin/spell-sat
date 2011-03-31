///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.dialogs
// 
// FILE      : ItemInfoDialog.java
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
package com.astra.ses.spell.gui.presentation.code.dialogs;

import java.util.Vector;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.presentation.code.Activator;
import com.astra.ses.spell.gui.presentation.code.controls.ItemInfoTable;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer.ItemNotificationListener;
import com.astra.ses.spell.gui.procs.model.ProcedureLine;
import com.astra.ses.spell.gui.procs.model.StackHelper;


/*******************************************************************************
 * @brief Dialog for selecting the SPELL server connection to be used.
 * @date 18/09/07
 * @author Rafael Chinchilla (GMV)
 ******************************************************************************/
public class ItemInfoDialog extends TitleAreaDialog implements ControlListener,
ItemNotificationListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static int s_lastWidth = 0;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the table of items */
	private ItemInfoTable m_items;
	private String m_csPosition;
	/** Procedure id */
	private String m_procId;
	/** Procedure line */
	private ProcedureLine m_line;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param shell
	 *            The parent shell
	 **************************************************************************/
	public ItemInfoDialog(Shell shell, String procId, ProcedureLine line, String csPosition)
	{
		super(shell);
		setShellStyle( SWT.CLOSE | SWT.RESIZE );
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_detail.png");
		m_image = descr.createImage();
		m_csPosition = csPosition;
		m_procId = procId;
		m_line = line;
	}
	
	/***************************************************************************
	 * Called when the dialog is about to close.
	 * 
	 * @return The superclass return value.
	 **************************************************************************/
	public boolean close()
	{
		m_image.dispose();
		return super.close();
	}

	/***************************************************************************
	 * Dynamic item notification
	 **************************************************************************/
	public void notifyItem( ItemNotification data, String csPos )
	{
		int lineIndex = m_line.getLineNum();
		Vector<String> dcsp = data.getStackPosition();
		for( int idx=0; idx<dcsp.size(); idx++)
		{
			if (StackHelper.getStackElement(dcsp, idx).equals(csPos))
			{
				int dataIndex = Integer.parseInt(dcsp.get(idx+1));
				if ( lineIndex == dataIndex )
				{
					m_items.updateInfo(m_csPosition);
					int lineNo = m_line.getLineNum();
					int executions = m_line.getNumVisits();
					setMessage("Line: " + lineNo + "  ( executed " + executions + " times)");
					return;
				}
			}
		}
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Creates the dialog contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createContents(Composite parent)
	{
		Control contents = super.createContents(parent);
		int lineNo = m_line.getLineNum();
		int executions = m_line.getNumVisits();
		setTitle("Item information for " + m_procId);
		setMessage("Line: " + lineNo + "  ( executed " + executions + " times)");
		setTitleImage(m_image);
		return contents;
	}

	/***************************************************************************
	 * Create the dialog area contents.
	 * 
	 * @param parent
	 *            The base composite of the dialog
	 * @return The resulting contents
	 **************************************************************************/
	protected Control createDialogArea(Composite parent)
	{
		parent.addControlListener(this);
		// Main composite of the dialog area -----------------------------------
		Composite top = new Composite(parent, SWT.NONE);
		GridData areaData = new GridData(GridData.FILL_BOTH);
		areaData.widthHint = 1050;
		top.setLayoutData(areaData);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.numColumns = 1;
		top.setLayout(layout);

		createItemInfoArea(top);
		
		return parent;
	}
	
	/***************************************************************************
	 * Create the context information group
	 **************************************************************************/
	protected void createItemInfoArea( Composite parent )
	{
		boolean summaryValue = true;
		Button summary = new Button(parent, SWT.CHECK);
		summary.setSelection(summaryValue);
		summary.setText("Show latest information");
		summary.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				boolean val = button.getSelection();
				m_items.setSummaryMode(val);
			}
		});
		m_items = new ItemInfoTable(m_line,parent);
		m_items.updateInfo(m_csPosition);
	}
	
	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL,
				true);
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	protected void buttonPressed(int buttonId)
	{
		switch (buttonId)
		{
		case IDialogConstants.CLOSE_ID:
			close();
		}
	}

	/***************************************************************************
	 * Called when the dialog is moved
	 **************************************************************************/
	@Override
	public void controlMoved(ControlEvent e) {}

	/***************************************************************************
	 * Called when the dialog is resized
	 **************************************************************************/
	@Override
	public void controlResized(ControlEvent e)
	{
		Table table = m_items.getTable();
		Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Rectangle area = m_items.getControl().getBounds();
		int width = area.width - 2*table.getBorderWidth() - 1 ;
		if (width<0)
		{
			width = getDialogArea().getBounds().width 
				- 2*table.getBorderWidth()
				- 2*m_items.getControl().getBorderWidth();
		}
		if (s_lastWidth == width) return;
		table.setRedraw(false);
		s_lastWidth = width;
		if (preferredSize.y > area.height + table.getHeaderHeight())
		{
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			Point vBarSize = table.getVerticalBar().getSize();
			width -= vBarSize.x;
		}
		int fixedWidth = 0;
		TableColumn[] columns = table.getColumns();
		for( int cidx = 0; cidx<columns.length; cidx++)
		{
			if (cidx != ItemInfoTable.COMMENT_COLUMN)
			{
				fixedWidth += columns[cidx].getWidth();
			}
		}
		int cw = width - fixedWidth;
		if (table.getVerticalBar().isVisible())
		{
			cw -= table.getVerticalBar().getSize().x;
		}
		columns[ItemInfoTable.COMMENT_COLUMN].setWidth(cw);
		table.setSize(width, area.height);
		table.setRedraw(true);
	}
}
