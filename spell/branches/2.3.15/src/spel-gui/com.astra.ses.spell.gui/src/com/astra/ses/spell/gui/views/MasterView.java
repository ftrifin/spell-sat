///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : MasterView.java
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
package com.astra.ses.spell.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.core.CoreExtensions;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.interfaces.IShellListener;
import com.astra.ses.spell.gui.core.interfaces.IShellManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.services.IRuntimeSettings;
import com.astra.ses.spell.gui.services.IRuntimeSettings.RuntimeProperty;
import com.astra.ses.spell.gui.services.IViewManager;
import com.astra.ses.spell.gui.views.controls.input.PromptField;
import com.astra.ses.spell.gui.views.controls.master.ExecutorComposite;
import com.astra.ses.spell.gui.views.controls.master.RecoveryComposite;

/*******************************************************************************
 * @brief This view contains the master console.
 * @date 09/10/07
 ******************************************************************************/
public class MasterView extends ViewPart implements KeyListener, IShellListener, ICoreContextOperationListener
{
	private static IShellManager	s_smgr	= null;
	/** The view identifier */
	public static final String	 ID	       = "com.astra.ses.spell.gui.views.MasterView";
	/** Console display */
	private Text	             m_display;
	/** The command input field */
	private PromptField	         m_prompt;
	private boolean	             m_haveShell;

	/** Holds the stacked container for the condition definition widgets */
	private Composite			    m_stackContainer;
	/** Holds the stack layout */
	private StackLayout	            m_stack;
	/** Holds the "not connected" panel */
	private Composite			    m_notConnectedPanel;
	/** Holds the executors composite */
	private Composite               m_executorsPanel;

	/** Holds the tab folder for executors */
	private TabFolder	   		 m_tabs;
	/** Holds the table of executors */
	private ExecutorComposite 	 m_executorsComposite;
	/** Holds the table of recovery files */
	private RecoveryComposite 	 m_recoveryComposite;
	
	/** Holds the executors area label */
	private Label                m_executorsLabel;


	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public MasterView()
	{
		super();
		m_haveShell = false;
		Logger.debug("Created", Level.INIT, this);
		s_smgr = (IShellManager) ServiceManager.get(IShellManager.class);
		CoreExtensions.get().addContextOperationListener(this);
	}

	/***************************************************************************
	 * Create the view contents.
	 * 
	 * @param parent
	 *            The view top composite
	 **************************************************************************/
	public void createPartControl(Composite parent)
	{
		// Obtain the required resources
		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		Font codeFont = cfg.getFont(FontKey.MASTERC);
		Color bcolor = cfg.getGuiColor(GuiColorKey.CONSOLE_BG);
		Color wcolor = cfg.getGuiColor(GuiColorKey.CONSOLE_FG);

		parent.setLayout( new GridLayout(1,true) );
		
		m_stack = new StackLayout();
		m_stackContainer = new Composite(parent, SWT.BORDER);
		GridData std = new GridData(GridData.FILL_BOTH);
		m_stackContainer.setLayoutData(std);
		m_stackContainer.setLayout(m_stack);

		// STACK / EXECUTORS =================================================================

		m_executorsPanel = new Composite(m_stackContainer, SWT.NONE);
		m_executorsPanel.setLayout( new GridLayout(1,true) );
		m_executorsPanel.setLayoutData( new GridData( GridData.FILL_BOTH ));
		
		// TABS ===============================================================================

		m_executorsLabel = new Label(m_executorsPanel, SWT.NONE);
		m_executorsLabel.setText("???" );
		m_executorsLabel.setFont( cfg.getFont( FontKey.GUI_BOLD ) );
		m_executorsLabel.setLayoutData( new GridData( GridData.FILL_HORIZONTAL));

		m_tabs = new TabFolder(m_executorsPanel, SWT.NONE);
		GridData cld = new GridData(GridData.FILL_BOTH);
		m_tabs.setLayoutData(cld);
		
		// TAB - Currently open procedures
		TabItem executorsItem = new TabItem(m_tabs,SWT.NONE);
		executorsItem.setText("Open procedures");
		m_executorsComposite = new ExecutorComposite(m_tabs, SWT.NONE );
		m_executorsComposite.setLayoutData( new GridData( GridData.FILL_BOTH ));
		executorsItem.setControl(m_executorsComposite);

		// TAB - Recovery
		TabItem recoveryItem = new TabItem(m_tabs,SWT.NONE);
		recoveryItem.setText("Past Executions");
		m_recoveryComposite = new RecoveryComposite( m_tabs, SWT.NONE );
		m_recoveryComposite.setLayoutData( new GridData( GridData.FILL_BOTH ));
		recoveryItem.setControl(m_recoveryComposite);

		// STACK / NOT CONNECTED ==============================================================
		m_notConnectedPanel = new Composite(m_stackContainer, SWT.NONE );
		m_notConnectedPanel.setLayout( new GridLayout(1,true) );
		Label label = new Label(m_notConnectedPanel, SWT.NONE);
		label.setAlignment(SWT.CENTER);
		label.setText("Not connected");
		label.setFont( cfg.getFont( FontKey.BANNER ) );
		label.setLayoutData( new GridData( GridData.FILL_BOTH ));

		Label label2 = new Label(m_notConnectedPanel, SWT.BORDER);
		label2.setAlignment(SWT.CENTER);
		label2.setText("Please open the Connection Dialog in the System menu \nin order to establish a connection to a SPELL server\n and attach to a Spacecraft context.");
		label2.setFont( cfg.getFont( FontKey.GUI_BOLD ) );
		label2.setLayoutData( new GridData( GridData.FILL_BOTH ));

		m_stack.topControl = m_notConnectedPanel;
		m_stackContainer.layout();

		// SEPARATOR ==========================================================================
		
		Label sep = new Label( parent, SWT.SEPARATOR | SWT.HORIZONTAL );
		sep.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		
		// CONSOLE ============================================================================
		
		// Create a group for holding the display and the input field
		Composite consoleComposite = new Composite(parent, SWT.NONE);
		consoleComposite.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		consoleComposite.setLayout(glayout);

		Label label3 = new Label(consoleComposite, SWT.BORDER);
		label3.setText(" Master Console");
		label3.setFont( cfg.getFont( FontKey.GUI_BOLD ) );
		label3.setLayoutData( new GridData( GridData.FILL_HORIZONTAL));
		
		// Create the console display
		m_display = new Text(consoleComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		m_display.setFont(codeFont);
		m_display.setBackground(bcolor);
		m_display.setForeground(wcolor);
		GridData ddata = new GridData( GridData.FILL_BOTH );
		ddata.minimumHeight = 200;
		m_display.setLayoutData( ddata );
		m_display.setText("");
		m_display.setEditable(false);

		// Create the input field
		m_prompt = new PromptField(consoleComposite, "");
		m_prompt.getContents().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_prompt.getContents().addKeyListener(this);

		// Try to load the shell plugin if available
		m_haveShell = s_smgr.haveShell();
		if (m_haveShell)
		{
			Logger.debug("Registering as SHELL listener", Level.PROC, this);
			s_smgr.addShellListener(this);
		}
		// Register in the view manager to make this view available
		IViewManager mgr = (IViewManager) ServiceManager.get(IViewManager.class);
		mgr.registerView(ID, this);
	}

	/***************************************************************************
	 * Destroy the view.
	 **************************************************************************/
	public void dispose()
	{
		CoreExtensions.get().removeContextOperationListener(this);
		if (m_haveShell)
		{
			s_smgr.removeShellListener(this);
		}
		m_display.dispose();
		m_display = null;
		m_prompt = null;
		super.dispose();
		Logger.debug("Disposed", Level.PROC, this);
	}

	/***************************************************************************
	 * Receive the input focus.
	 **************************************************************************/
	public void setFocus()
	{
		IRuntimeSettings runtime = (IRuntimeSettings) ServiceManager.get(IRuntimeSettings.class);
		runtime.setRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION, "");
		m_tabs.setFocus();
	}

	/***************************************************************************
	 * Callback for key press event
	 * 
	 * @param e
	 *            Key press event
	 **************************************************************************/
	public void keyPressed(KeyEvent e)
	{
		// Nothing to do
	}

	/***************************************************************************
	 * Callback for key release event. Used for command processing.
	 * 
	 * @param e
	 *            Key release event
	 **************************************************************************/
	public void keyReleased(KeyEvent e)
	{
		// Check if the key code corresponds to one of the two enter keys.
		if (e.keyCode == 13 || e.keyCode == 16777296)
		{
			// Obtain the contents of the input field
			String cmdString = m_prompt.getValue();
			try
			{
				// Add the text to the display, reset the prompt, and
				// send the command string to the shell manager
				if (m_haveShell == false)
				{
					addDisplayMessage("No shell plugin available.");
					return;
				}
				addDisplayMessage(m_prompt.getPrompt() + PromptField.PROMPT_SYMBOL + cmdString);
				s_smgr.shellInput(cmdString);
			}
			catch (CommandFailed ex)
			{
				// If the console manager raises an error show it on the
				// display
				addDisplayMessage("ERROR (" + m_prompt.getPrompt() + "): " + ex.getLocalizedMessage());
			}
			m_prompt.reset();
		}
	}

	// =========================================================================
	// NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Add a text message to the display
	 **************************************************************************/
	protected void addDisplayMessage(String message)
	{
		String text = m_display.getText();
		// Take into account wether the display is empty or not
		if (text.length() > 0)
		{
			text += m_display.getLineDelimiter() + message;
		}
		else
		{
			text = message;
		}
		m_display.setText(text);
		m_display.setSelection(text.length());
	}

	/***************************************************************************
	 * Receive output from the command shell
	 **************************************************************************/
	@Override
	public void shellOutput(String output, Severity severity)
	{
		addDisplayMessage(output);
	}

	@Override
    public String getListenerId()
    {
	    return ID;
    }

	@Override
    public void notifyContextAttached( final ContextInfo ctx)
    {
		Display.getDefault().asyncExec( new Runnable()
		{
			public void run()
			{
				m_executorsLabel.setText("Procedure operations in context " + ctx.getName() );
				m_stack.topControl = m_executorsPanel;
				m_stackContainer.layout();
				m_executorsComposite.refresh();
			}
		});
    }

	@Override
    public void notifyContextDetached()
    {
		Display.getDefault().asyncExec( new Runnable()
		{
			public void run()
			{
				if (!m_stackContainer.isDisposed())
				{
					m_stack.topControl = m_notConnectedPanel;
					m_stackContainer.layout();
				}
			}
		});
    }

	@Override
    public void notifyContextError(ErrorData error)
    {
		Display.getDefault().asyncExec( new Runnable()
		{
			public void run()
			{
				if (!m_stackContainer.isDisposed())
				{
					m_stack.topControl = m_notConnectedPanel;
					m_stackContainer.layout();
				}
			}
		});
    }
}
