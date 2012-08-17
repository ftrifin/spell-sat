///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : CloseProcDialog.java
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.types.ClientMode;

/*******************************************************************************
 * @brief Dialog for closing procedures
 * @date 24/04/08
 ******************************************************************************/
public class CloseProcDialog extends TitleAreaDialog
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	public static final int	DETACH	= 97;
	public static final int	KILL	= 98;
	public static final int	CLOSE	= 99;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image	        m_image;
	/** Holds the procedure status string */
	private String	        m_status;
	/** Holds the client mode */
	private ClientMode	    m_mode;
	/** Holds the procedure name string */
	private String	        m_name;
	/** Holds the active prompt flag */
	private boolean	        m_promptActive;

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
	public CloseProcDialog(Shell shell, String name, String status,
	        ClientMode mode, boolean doingPrompt)
	{
		super(shell);
		m_name = name;
		m_mode = mode;
		m_status = status;
		m_promptActive = doingPrompt;
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator
		        .getImageDescriptor("icons/dlg_question.png");
		m_image = descr.createImage();
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
		setTitle("Close procedure '" + m_name + "'");
		setTitleImage(m_image);
		String msg = "The procedure status is " + m_status;
		setMessage(msg);
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
		String longMessage = "";
		if (m_mode != ClientMode.CONTROLLING)
		{
			longMessage = "You can only release the procedure, since you are";
			longMessage += " not in control of the procedure\n";
		}
		else if (m_promptActive)
		{
			longMessage = "You can kill the procedure only, since some input";
			longMessage += " is required by it (a prompt is active)\n";
		}
		else
		{
			longMessage = "You can kill the execution, stop it and cleanup orderly ";
			longMessage += "or release the procedure and leave it working.\n";
			longMessage += "\nWhat do you want to do?";
		}

		parent.setLayout(new GridLayout());

		Label l = new Label(parent, SWT.NONE);
		l.setText(longMessage);
		l.setLayoutData(new GridData(GridData.FILL_BOTH));

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
		if (m_mode != ClientMode.CONTROLLING)
		{
			createButton(parent, DETACH, "Stop monitor", false);
		}
		else
		{
			if (!m_promptActive)
			{
				createButton(parent, DETACH, "Release control", false);
				createButton(parent, CLOSE, "Close procedure", false);
			}
			createButton(parent, KILL, "Kill procedure", false);
		}
		Button cancel = createButton(parent, IDialogConstants.CANCEL_ID,
		        IDialogConstants.CANCEL_LABEL, true);
		cancel.setFocus();
	}

	/***************************************************************************
	 * Called when one of the buttons of the button bar is pressed.
	 * 
	 * @param buttonId
	 *            The button identifier.
	 **************************************************************************/
	protected void buttonPressed(int buttonId)
	{
		setReturnCode(buttonId);
		close();
	}
}
