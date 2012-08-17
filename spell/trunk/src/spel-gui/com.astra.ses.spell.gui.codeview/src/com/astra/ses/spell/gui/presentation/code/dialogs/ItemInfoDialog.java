///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.dialogs
// 
// FILE      : ItemInfoDialog.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.presentation.code.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.presentation.code.Activator;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer.ItemNotificationListener;
import com.astra.ses.spell.gui.presentation.code.controls.ItemInfoTable;
import com.astra.ses.spell.gui.presentation.code.controls.ItemInfoTableColumn;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

/*******************************************************************************
 * @brief Dialog for selecting the SPELL server connection to be used.
 * @date 18/09/07
 ******************************************************************************/
public class ItemInfoDialog extends TitleAreaDialog implements ItemNotificationListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================
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
	/** Procedure id */
	private String m_procId;
	/** Procedure line */
	private IProcedureDataProvider m_dataProvider;
	/** Line number */
	private int m_lineNumber;

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
	public ItemInfoDialog(Shell shell, String procId, IProcedureDataProvider dataProvider, int lineNumber)
	{
		super(shell);
		setShellStyle(SWT.CLOSE | SWT.RESIZE);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_detail.png");
		m_image = descr.createImage();
		m_procId = procId;
		m_lineNumber = lineNumber;
		m_dataProvider = dataProvider;
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
	public void notifyItem(ItemNotification data, String csPos)
	{
		m_items.refresh();
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
		setTitle("Item information for " + m_procId);
		String message = "Line: " + m_lineNumber;
		try
		{
			int executions = m_dataProvider.getExecutionCount(m_lineNumber);
			message += " (Executed " + executions + " times)";
		}
		catch (UninitProcedureException e)
		{
			e.printStackTrace();
		}
		setMessage(message);
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
	 * Get the line number this dialog is interested in
	 * 
	 * @return
	 **************************************************************************/
	public int getLineNumber()
	{
		return m_lineNumber;
	}

	/***************************************************************************
	 * Update this dialog
	 **************************************************************************/
	public void update()
	{
		m_items.getTable().deselectAll();
		m_items.refresh(true);
		// Show the last item
		ScrollBar vertical = m_items.getTable().getVerticalBar();
		int maximum = vertical.getMaximum();
		int thumb = vertical.getThumb();
		int current = vertical.getSelection();
		if (current >= maximum - thumb)
		{
			m_items.reveal(m_items.getElementAt(m_items.getTable().getItemCount() - 1));
		}
	}

	/***************************************************************************
	 * Create the context information group
	 **************************************************************************/
	protected void createItemInfoArea(Composite parent)
	{
		boolean summaryValue = true;
		Button summary = new Button(parent, SWT.CHECK);
		summary.setSelection(summaryValue);
		summary.setText("Show latest information");
		summary.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button button = (Button) e.widget;
				boolean val = button.getSelection();
				m_items.setSummaryMode(val);
			}
		});
		m_items = new ItemInfoTable(m_lineNumber, parent);
		m_items.setInput(m_dataProvider);
		// Resize the columns in the beginning to each fits to the contents
		m_items.getTable().getColumns()[ItemInfoTableColumn.NAME.ordinal()].pack();
		m_items.getTable().getColumns()[ItemInfoTableColumn.TIME.ordinal()].pack();
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
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
}
