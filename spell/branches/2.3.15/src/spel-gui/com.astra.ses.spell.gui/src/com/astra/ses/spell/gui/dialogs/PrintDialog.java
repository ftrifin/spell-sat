///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : PrintDialog.java
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
package com.astra.ses.spell.gui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureManager;

/*******************************************************************************
 * @brief Dialog for printing procedures
 * @date 31/03/08
 ******************************************************************************/
public class PrintDialog extends TitleAreaDialog
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the procedure manager handle */
	private static IProcedureManager	s_mgr	= null;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image	                m_image;
	/** Data to be printed */
	private Combo	                m_printData;

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
	public PrintDialog(Shell shell)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator
		        .getImageDescriptor("icons/dlg_print.png");
		m_image = descr.createImage();
		// Obtain the manager handlers
		if (s_mgr == null)
		{
			s_mgr = (IProcedureManager) ServiceManager.get(IProcedureManager.class);
		}
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
		setMessage("Select data to be printed:");
		setTitle("Print procedure data");
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
		GridLayout layout = new GridLayout();
		top.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL,
		        GridData.VERTICAL_ALIGN_FILL, true, false));
		layout.marginHeight = 15;
		layout.marginWidth = 15;
		layout.marginTop = 10;
		layout.marginBottom = 10;
		layout.marginLeft = 15;
		layout.marginRight = 15;
		layout.numColumns = 2;
		top.setLayout(layout);

		Label label = new Label(top, SWT.NONE);
		label.setText("Available data:");
		m_printData = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
		// Fill the combo with the list of available proc data
		m_printData.add("As-Run file");
		m_printData.add("Tabular view");
		m_printData.add("Textual view");
		m_printData.add("Procedure log");
		m_printData.select(0);
		return parent;
	}

	/***************************************************************************
	 * Create the button bar buttons.
	 * 
	 * @param parent
	 *            The Button Bar.
	 **************************************************************************/
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
		        true);
		createButton(parent, IDialogConstants.CANCEL_ID,
		        IDialogConstants.CANCEL_LABEL, false);
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
		case IDialogConstants.OK_ID:
			int page = m_printData.getSelectionIndex();
			Logger.debug("Selected for printing: " + page, Level.GUI, this);
			// s_mgr.printActiveProc( page );
		case IDialogConstants.CANCEL_ID:
			close();
		}
	}
}
