///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.input
// 
// FILE      : InputArea.java
//
// DATE      : 2008-11-21 13:54
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.views.controls.input;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.PromptDisplayType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;
import com.astra.ses.spell.gui.views.ProcedureView;
import com.astra.ses.spell.gui.views.controls.ControlArea;

/*******************************************************************************
 * @brief Special control for showing user prompts in the tabular view
 * @date 20/10/07
 ******************************************************************************/
public class InputArea extends Composite implements SelectionListener,
        KeyListener, VerifyListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final String	        KEY_SEPARATOR	= ":";
	private static ConfigurationManager	s_cfg	      = null;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Text for commit button */
	public static final String	        BTN_COMMIT	  = "Confirm";
	/** Text for reset button */
	public static final String	        BTN_RESET	  = "Reset";
	/** Identifier for commit button */
	public static final String	        BTN_COMMIT_ID	= "BTN_COMMIT";
	/** Identifier for reset button */
	public static final String	        BTN_RESET_ID	= "BTN_RESET";
	/** Maximum font size */
	private static final int	        MAX_FONT_SIZE	= 16;
	/** Minimum font size */
	private static final int	        MIN_FONT_SIZE	= 7;
	/** Currently selected font size */
	private int	                        m_fontSize;
	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Text field for text prompts */
	private Text	                    m_textPrompt;
	/** Prompt field for text prompts */
	private PromptField	                m_textInput;
	/** Future storing the prompt answers */
	private Input	                    m_promptData;
	/** Holds the list of expected values */
	private Vector<String>	            m_expected;
	/** Flag for numeric text prompts */
	private boolean	                    m_numericInput;
	/** Parent view of the input area */
	private ControlArea	                m_parent;
	/** Holds the procedure view reference */
	private ProcedureView	            m_view;
	/** Holds the radio buttons for options */
	private ArrayList<Button>	        m_optionsRadio;
	/** Holds the combo widget for options */
	private Combo	                    m_optionsCombo;
	/** Holds the type of widget for options */
	private PromptDisplayType	        m_promptDisplayType;
	/** Holds the set of radio buttons */
	private Group	                    m_optionGroup;
	/** Holds the selected option data if any */
	private String	                    m_selectedOption;
	/** Commit button */
	private Button	                    m_commitButton;
	/** Reset button */
	private Button	                    m_resetButton;
	/** Holds the client mode */
	private ClientMode	                m_clientMode;
	/* Prompt blinking mechanism */
	private PromptBlinker	            m_blinker;
	/** Launcher of the blinker task */
	private PromptBlinkerLauncher	    m_blinkerLauncher;
	/** Blinker switch flag */
	private boolean	                    m_blinkSwitch;
	/** Prompt sound file name */
	private PromptSounder	            m_sounder;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param view
	 *            The parent procedure view
	 * @param top
	 *            The container composite
	 **************************************************************************/
	public InputArea(ProcedureView view, ControlArea area)
	{
		super(area, SWT.BORDER);
		m_view = view;
		m_parent = area;
		m_promptData = null;
		m_expected = null;
		m_clientMode = null;

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		setLayout(layout);

		Group textGroup = new Group(this, SWT.NONE);
		GridLayout tg_layout = new GridLayout();
		tg_layout.marginHeight = 0;
		tg_layout.marginTop = 0;
		tg_layout.marginBottom = 3;
		tg_layout.marginWidth = 0;
		tg_layout.marginLeft = 3;
		tg_layout.marginRight = 3;
		tg_layout.numColumns = 1;
		textGroup.setLayout(tg_layout);
		GridData ldatat = new GridData(GridData.FILL_BOTH);
		ldatat.grabExcessHorizontalSpace = true;
		textGroup.setLayoutData(ldatat);

		m_textPrompt = new Text(textGroup, SWT.MULTI | SWT.BORDER);
		m_textPrompt.setText("Enter command");
		m_textPrompt.setEditable(false);
		m_textPrompt
		        .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		if (s_cfg == null)
		{
			s_cfg = (ConfigurationManager) ServiceManager
			        .get(ConfigurationManager.ID);
		}

		Font font = s_cfg.getFont(FontKey.CODE);
		setFont(font);
		m_textPrompt.setFont(font);

		Composite inputGroup = new Composite(textGroup, SWT.NONE);
		GridLayout ig_layout = new GridLayout();
		ig_layout.marginHeight = 0;
		ig_layout.marginTop = 0;
		ig_layout.marginBottom = 3;
		ig_layout.marginWidth = 0;
		ig_layout.marginLeft = 3;
		ig_layout.marginRight = 3;
		ig_layout.numColumns = 3;
		inputGroup.setLayout(ig_layout);
		inputGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		m_textInput = new PromptField(inputGroup, "", this);
		m_textInput.getContents().setLayoutData(
		        new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		m_textInput.getContents().addVerifyListener(this);

		m_commitButton = new Button(inputGroup, SWT.PUSH);
		m_commitButton.addSelectionListener(this);
		m_commitButton.setText(BTN_COMMIT);
		m_commitButton.setData("ID", BTN_COMMIT_ID);
		m_resetButton = new Button(inputGroup, SWT.PUSH);
		m_resetButton.addSelectionListener(this);
		m_resetButton.setText(BTN_RESET);
		m_resetButton.setData("ID", BTN_RESET_ID);

		m_commitButton.setEnabled(false);
		m_resetButton.setEnabled(false);

		m_optionsRadio = new ArrayList<Button>();
		m_optionsCombo = null;
		m_selectedOption = null;
		m_optionGroup = null;

		m_blinker = null;
		m_blinkerLauncher = null;
		m_blinkSwitch = true;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setEnabled(boolean enabled)
	{
		if (enabled && m_clientMode != ClientMode.CONTROLLING) return;
		m_textPrompt.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void zoom(boolean increase)
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
			Font fontP = s_cfg.getFont(FontKey.GUI_NOM, m_fontSize);
			setFont(fontP);
		}
	}

	/***************************************************************************
	 * Set the table font
	 **************************************************************************/
	public void setFont(Font font)
	{
		m_fontSize = font.getFontData()[0].getHeight();
		m_textPrompt.setFont(font);
		m_textPrompt.redraw();
		m_parent.layout();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean setFocus()
	{
		m_textInput.getContents().setFocus();
		return true;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void setClientMode(ClientMode mode)
	{
		m_clientMode = mode;
		if (mode != ClientMode.CONTROLLING)
		{
			setEnabled(false);
		}
	}

	/***************************************************************************
	 * Issue a textual prompt
	 * 
	 * @param prompt
	 *            Prompt text
	 * @param value
	 *            Future for storing the answer
	 * @param isNumeric
	 *            Numeric prompt flag
	 **************************************************************************/
	public void prompt(Input promptData)
	{
		reset();
		prepareBlinking();
		m_promptData = promptData;
		if (promptData.isNotification())
		{
			promptAsMonitoring();
		}
		else
		{
			promptAsControlling();
		}
	}

	/***************************************************************************
	 * Shows a prompt prepared to receive an answer (controlling GUI)
	 **************************************************************************/
	private void promptAsControlling()
	{
		m_textInput.getContents().removeVerifyListener(this);
		if (m_promptData.isList())
		{
			m_expected = m_promptData.getExpected();
			m_numericInput = false;
			// Set the prompt text
			m_textPrompt.setText(m_promptData.getText());
			// Set the text hint for the console input
			String hint = "Type ";
			for (String opt : m_expected)
			{
				if (hint.length() != 5)
				{
					hint += ",";
				}
				hint += opt;
			}
			m_textInput.setHint(hint);
			m_promptDisplayType = m_promptData.getPromptDisplayType();
			// Build the option list and show the option composite
			updateOptions(m_promptData.getOptions(), m_promptData.getExpected());
		}
		else
		{
			m_expected = null;
			m_textPrompt.setText(m_promptData.getText());
			m_numericInput = m_promptData.isNumeric();

		}
		// Reset the console input
		m_textInput.reset();
		// Set the focus, highlighting
		m_textInput.promptStart();
		m_textInput.getContents().addVerifyListener(this);
	}

	/***************************************************************************
	 * Shows a prompt prepared not expecting an answer (monitoring GUI)
	 **************************************************************************/
	private void promptAsMonitoring()
	{
		m_textPrompt.setText(m_promptData.getText());
		if (m_promptData.isList())
		{
			// Build the option list and show the option composite
			updateOptions(m_promptData.getOptions(), m_promptData.getExpected());
		}
		// Reset the console input
		m_textInput.reset();
		m_textInput.setEnabled(false);
		m_commitButton.setEnabled(false);
		m_resetButton.setEnabled(false);
		if (m_promptDisplayType == PromptDisplayType.RADIO)
		{
			for (Button opt : m_optionsRadio)
			{
				opt.setEnabled(false);
			}
		}
		else
		{
			m_optionsCombo.setEnabled(false);
		}
	}

	/***************************************************************************
	 * Callback for radio buttons or combo (selection list)
	 * 
	 * @param e
	 *            Selection event
	 **************************************************************************/
	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	/***************************************************************************
	 * Callback for radio buttons or combo (selection list)
	 * 
	 * @param e
	 *            Selection event
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	public void widgetSelected(SelectionEvent e)
	{
		if (e.widget instanceof Button || e.widget instanceof Combo)
		{
			String id = "";
			if (e.widget instanceof Combo)
			{
				Vector<String> expectedValues = (Vector<String>) m_optionsCombo
				        .getData("IDs");
				id = expectedValues.elementAt(m_optionsCombo
				        .getSelectionIndex());
			}
			else
			{
				Button b = (Button) e.widget;
				id = ((String) b.getData("ID"));
			}
			if (id.equals(BTN_COMMIT_ID))
			{
				if (m_promptData != null) // Prompt mode
				{
					handlePromptAnswer();
				}
				else
				{
					handleCommand();
				}
			}
			else if (id.equals(BTN_RESET_ID))
			{
				m_textInput.reset();
				m_textInput.promptStart();
				if (m_promptData != null)
				{
					resetOptions();
				}
				updateButtons();
			}
			else
			{
				m_selectedOption = id;
				updateButtons();
			}
		}
	}

	/***************************************************************************
	 * Reset the input area and show the no input page
	 * 
	 * @param resetAll
	 *            If true, reset automatically all the rest of input handlers
	 **************************************************************************/
	public void reset()
	{
		Logger.debug("Resetting prompt", Level.PROC, this);
		stopBlinking();
		m_textPrompt.setText("Enter command:");
		m_textInput.delHint();
		m_textInput.reset();
		m_textInput.setEnabled(true);
		clearOptions();
		m_promptData = null;
		m_expected = null;
	}

	/***************************************************************************
	 * Prepare the blinking
	 ***************************************************************************/
	void prepareBlinking()
	{
		m_blinkSwitch = true;
		long msec = 10000;
		try
		{
			msec = Long.parseLong(s_cfg
			        .getProperty(PropertyKey.PROMPT_SOUND_DELAY)) * 1000;
		}
		catch (NumberFormatException ex)
		{
			ex.printStackTrace();
		}
		;
		m_blinkerLauncher = new PromptBlinkerLauncher(this, msec);
	}

	/***************************************************************************
	 * Start the blinking
	 ***************************************************************************/
	void startBLinking()
	{
		m_blinker = new PromptBlinker(this);
		String soundFile = s_cfg.getProperty(PropertyKey.PROMPT_SOUND_FILE);
		if ((soundFile != null) && (!soundFile.isEmpty()))
		{
			m_sounder = new PromptSounder(soundFile);
			m_sounder.start();
		}
	}

	/***************************************************************************
	 * Start the blinking
	 ***************************************************************************/
	void stopBlinking()
	{
		if (m_blinker != null)
		{
			m_blinker.stopBlinking();
			try
			{
				m_blinker.join();
			}
			catch (InterruptedException e)
			{
			}
			;
			m_blinker = null;
		}
		if (m_sounder != null)
		{
			m_sounder.cancel();
			m_sounder = null;
		}
		if (m_blinkerLauncher != null)
		{
			m_blinkerLauncher.cancel();
			m_blinkerLauncher = null;
		}
		// Ensure the text field has null background color
		m_textPrompt.setBackground(null);
		m_textPrompt.redraw();
	}

	/***************************************************************************
	 * Do a half-blink
	 ***************************************************************************/
	void blink()
	{
		if (!m_textPrompt.isDisposed())
		{
			if (m_blinkSwitch)
			{
				m_blinkSwitch = false;
				m_textPrompt.setBackground(Display.getCurrent().getSystemColor(
				        SWT.COLOR_YELLOW));
			}
			else
			{
				m_blinkSwitch = true;
				m_textPrompt.setBackground(null);
			}
			m_textPrompt.redraw();
		}
	}

	/***************************************************************************
	 * Callback for keyboard input on the console input (key press)
	 * 
	 * @param e
	 *            Key event
	 **************************************************************************/
	public void keyPressed(KeyEvent e)
	{
	}

	/***************************************************************************
	 * Callback for input changed
	 **************************************************************************/
	@Override
	public void verifyText(VerifyEvent e)
	{
		boolean enabled = !e.text.isEmpty();
		m_commitButton.setEnabled(enabled);
		m_resetButton.setEnabled(enabled);
	}

	/***************************************************************************
	 * Callback for keyboard input (key release)
	 * 
	 * @param e
	 *            Key event
	 **************************************************************************/
	public void keyReleased(KeyEvent e)
	{
		if ((e.keyCode == 13 || e.keyCode == 16777296) && m_promptData != null)
		{
			handlePromptAnswer();
		}
		else if ((e.keyCode == 13 || e.keyCode == 16777296)
		        && m_promptData == null)
		{
			handleCommand();
		}
	}

	/***************************************************************************
	 * Handle a text command
	 **************************************************************************/
	private void handlePromptAnswer()
	{
		String answer = m_textInput.getValue();
		if (answer.length() == 0 && m_selectedOption == null)
		{
			MessageDialog.openError(getShell(), "Prompt error",
			        "Cannot commit, no value given");
			m_textInput.reset();
			m_textInput.promptStart();
			return;
		}
		if (m_numericInput)
		{
			try
			{
				Integer.parseInt(answer);
			}
			catch (NumberFormatException ex)
			{
				try
				{
					Double.parseDouble(answer);
				}
				catch (NumberFormatException ex2)
				{
					MessageDialog.openError(getShell(), "Prompt error",
					        "Cannot commit, expected a numeric value");
					m_textInput.reset();
					m_textInput.promptStart();
					return;
				}
			}
		}
		else if (m_expected != null)
		{
			boolean accept = false;
			for (String opt : m_expected)
			{
				if (opt.equals(answer))
				{
					accept = true;
					break;
				}
				if (m_selectedOption != null && opt.equals(m_selectedOption))
				{
					accept = true;
					break;
				}
			}
			if (m_selectedOption != null && accept && answer != null
			        && answer.length() > 0)
			{
				if (!m_selectedOption.equals(answer))
				{
					MessageDialog
					        .openError(getShell(), "Prompt error",
					                "Conflicting values found between text area and buttons");
					m_textInput.reset();
					resetOptions();
					m_textInput.promptStart();
					return;
				}
			}
			if (!accept)
			{
				String values = "";
				for (String exp : m_expected)
					values += exp + "\n";
				MessageDialog.openError(getShell(), "Prompt error",
				        "Must enter one of the expected values:\n" + values);
				m_textInput.reset();
				m_textInput.promptStart();
				return;
			}
		}
		if (m_selectedOption != null)
		{
			m_promptData.setReturnValue(m_selectedOption);
		}
		else
		{
			m_promptData.setReturnValue(answer);
		}
		m_parent.resetPrompt();
		m_textInput.promptEnd();
	}

	/***************************************************************************
	 * Handle a text command
	 **************************************************************************/
	private void handleCommand()
	{
		String promptValue = m_textInput.getValue();
		if (promptValue.isEmpty()) { return; }
		m_parent.issueCommand(promptValue);
		m_textInput.reset();
	}

	/***************************************************************************
	 * Clear the option buttons
	 **************************************************************************/
	private void clearOptions()
	{
		if (m_optionsRadio.size() > 0)
		{
			for (Button opt : m_optionsRadio)
			{
				opt.dispose();
			}
			m_optionsRadio.clear();
		}
		if (m_optionGroup != null)
		{
			m_optionGroup.dispose();
			m_optionGroup = null;
		}
		if (m_optionsCombo != null)
		{
			m_optionsCombo.dispose();
			m_optionsCombo = null;
		}
		m_selectedOption = null;
		updateButtons();
		m_view.computeSplit();
	}

	/***************************************************************************
	 * Reset the selection in option buttons
	 **************************************************************************/
	private void resetOptions()
	{
		for (Button opt : m_optionsRadio)
		{
			opt.setSelection(false);
		}
		if (m_optionsCombo != null)
		{
			m_optionsCombo.deselectAll();
		}
		m_selectedOption = null;
		m_view.computeSplit();
	}

	/***************************************************************************
	 * Update the available options
	 * 
	 * @param options
	 * @param expectedValues
	 **************************************************************************/
	private void updateOptions(Vector<String> options,
	        Vector<String> expectedValues)
	{
		if (options == null || options.size() == 0) return;

		m_optionGroup = new Group(this, SWT.NONE);
		GridLayout tg_layout = new GridLayout();
		tg_layout.marginHeight = 0;
		tg_layout.marginTop = 0;
		tg_layout.marginBottom = 3;
		tg_layout.marginWidth = 0;
		tg_layout.marginLeft = 8;
		tg_layout.marginRight = 3;
		tg_layout.numColumns = 1;
		m_optionGroup.setLayout(tg_layout);
		m_optionGroup
		        .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		int count = 0;
		if (m_promptDisplayType == PromptDisplayType.RADIO)
		{
			for (String option : options)
			{
				String expected = expectedValues.elementAt(count);
				count++;

				Button b = new Button(m_optionGroup, SWT.RADIO);
				String value = option.substring(
				        option.indexOf(KEY_SEPARATOR) + 1, option.length());
				// Take into account the LIST|ALPHA case. When keys are the same
				// as values,
				// we do not want to display it twice.
				if (!expected.equals(value))
				{
					b.setText(expected + " : " + value);
				}
				else
				{
					b.setText(value);
				}
				b.setSelection(false);
				b.setData("ID", expected);
				b.addKeyListener(this);
				b.addSelectionListener(this);
				m_optionsRadio.add(b);
			}
		}
		else
		{
			m_optionsCombo = new Combo(m_optionGroup, SWT.DROP_DOWN
			        | SWT.READ_ONLY);
			m_optionsCombo.setData("IDs", expectedValues);
			for (String option : options)
			{
				m_optionsCombo.add(option.substring(
				        option.indexOf(KEY_SEPARATOR) + 1, option.length()));
				m_optionsCombo.addKeyListener(this);
				m_optionsCombo.addSelectionListener(this);
			}

		}
		m_view.computeSplit();
	}

	/***************************************************************************
	 * Update the input control buttons
	 **************************************************************************/
	private void updateButtons()
	{
		boolean hasInput = (m_selectedOption != null);

		m_commitButton.setEnabled(hasInput);
		m_resetButton.setEnabled(hasInput);
	}
}
