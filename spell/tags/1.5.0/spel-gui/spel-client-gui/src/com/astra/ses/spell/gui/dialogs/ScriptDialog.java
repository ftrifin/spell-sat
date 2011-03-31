///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs
// 
// FILE      : PromptDialog.java
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
package com.astra.ses.spell.gui.dialogs;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.comm.commands.CmdScript;
import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.procs.model.Procedure;
import com.astra.ses.spell.gui.procs.services.ProcedureManager;
import com.astra.ses.spell.gui.services.ConfigurationManager;


/*******************************************************************************
 * @brief Dialog for entering scripts
 * @date 18/09/07
 * @author Rafael Chinchilla (GMV)
 ******************************************************************************/
public class ScriptDialog extends TitleAreaDialog implements KeyListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final int EXECUTE_ID = 998897;
	private static final String EXECUTE_LABEL = "Execute";
	private static final int CLEAR_ID = 998899;
	private static final String CLEAR_LABEL = "Clear";
	private static final int CLOSE_ID = 998898;
	private static final String CLOSE_LABEL = "Close";
	private static ConfigurationManager s_cfg = null;
	private static ArrayList<String> s_history = new ArrayList<String>();
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the dialog image icon */
	private Image m_image;
	/** Holds the procedure id */
	private String m_procId;
	/** Holds the text input */
	private Text m_input;
	/** Holds the text output */
	private Text m_output;
	/** Holds the pointer in history */
	private int m_historyIndex;

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
	public ScriptDialog(Shell shell, String procId)
	{
		super(shell);
		// Obtain the image for the dialog icon
		ImageDescriptor descr = Activator.getImageDescriptor("icons/dlg_question.png");
		m_image = descr.createImage();
		m_procId = procId;
		
		if (s_cfg == null)
		{
			s_cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		}
		m_historyIndex = s_history.size();
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
	 * Get focus
	 **************************************************************************/
	public boolean setFocus()
	{
		m_input.setFocus();
		return true;
	}

	@Override
	public void keyPressed(KeyEvent arg0) 
	{}

	@Override
	public void keyReleased(KeyEvent event) 
	{
		if ((event.stateMask & SWT.CTRL)!=0)
		{
			if (event.keyCode == SWT.ARROW_UP)
			{
				if (m_historyIndex>=1 && s_history.size()>=1)
				{
					m_historyIndex--;
					System.err.println("Go back in history (" + m_historyIndex + "): " + s_history.get(m_historyIndex));
					m_input.setText(s_history.get(m_historyIndex));
				}
			}
			else if (event.keyCode == SWT.ARROW_DOWN)
			{
				if (s_history.size()>=1 && m_historyIndex<s_history.size()-1)
				{
					m_historyIndex++;
					System.err.println("Go forward in history (" + m_historyIndex + "): " + s_history.get(m_historyIndex));
					m_input.setText(s_history.get(m_historyIndex));
				}
				else
				{
					m_historyIndex = s_history.size();
					m_input.setText("");
				}
			}
		}
	}

	/***************************************************************************
	 * Adds output
	 **************************************************************************/
	public void addScriptOutput( String output )
	{
		m_output.append(output + m_output.getLineDelimiter());
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
		setMessage("Manual script execution for '" + m_procId + "'");
		setTitle(m_procId + " manual execution");
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
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.marginTop = 10;
		layout.marginBottom = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.numColumns = 1;
		top.setLayout(layout);

		m_output = new Text(top, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI );
		GridData gdout = new GridData();
		gdout.heightHint = 150;
		gdout.widthHint = 550;
		gdout.grabExcessHorizontalSpace = true;
		m_output.setLayoutData( gdout );
		m_output.setEditable(false);
		m_output.setBackground( new Color( Display.getCurrent(), 220, 220, 220 ));
		//TODO font zoom
		m_output.setFont( s_cfg.getFont("CODE") );
		
		
		m_input = new Text(top, SWT.BORDER  | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI );
		GridData gdin = new GridData();
		gdin.heightHint = 150;
		gdin.widthHint = 550;
		gdin.grabExcessHorizontalSpace = true;
		m_input.setLayoutData(gdin);
		m_input.setBackground(  new Color( Display.getCurrent(), 220, 220, 220 ));
		m_input.setFont( s_cfg.getFont("CODE") );
		m_input.addKeyListener(this);
		m_input.setFocus();
		
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
		createButton(parent, EXECUTE_ID, EXECUTE_LABEL, false);
		createButton(parent, CLEAR_ID, CLEAR_LABEL, false);
		createButton(parent, CLOSE_ID, CLOSE_LABEL, false);
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
		case EXECUTE_ID:
			ExecutorCommand cmd = new CmdScript();
			String code = m_input.getText();
			if (code != null && (!code.isEmpty()))
			{
				// Disallow Goto commands
				if (code.indexOf("Goto") != -1)
				{
					MessageDialog.openError(getShell(), "Invalid code", "Cannot issue goto commands from the script dialog");
					m_input.setFocus();
				}
				else
				{
					// Store the command in the history
					s_history.add(m_input.getText());
					// Refursbish it
					code = code.replaceAll("'", "\"");
					// Set command script as argument of the executor command
					Vector<String> args = new Vector<String>();
					args.add(code);
					cmd.setArgs( args );
					// Issue the command to the procedure
					ProcedureManager mgr = (ProcedureManager) ServiceManager.get(ProcedureManager.ID);
					Procedure proc = mgr.getProcedure(m_procId);
					proc.issueCommand(cmd);
					// Put the history index at top
					m_historyIndex = s_history.size();
				}
			}
			break;
		case CLOSE_ID:
			close();
			break;
		case CLEAR_ID:
			m_input.setText("");
			m_input.setFocus();
			break;
		}
	}
}
