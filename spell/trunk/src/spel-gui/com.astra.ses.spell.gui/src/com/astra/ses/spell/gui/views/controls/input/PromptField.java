///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.input
// 
// FILE      : PromptField.java
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
package com.astra.ses.spell.gui.views.controls.input;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.views.controls.actions.GuiExecutorCommand;

/*******************************************************************************
 * Custom implementation of a prompt field. This control parses the input to
 * remove the prompt text, keeps the prompt text visible during user input,
 * shows input hints if required, and mantains an input history which can be
 * used with the cursor up/down keys.
 * 
 */
public class PromptField implements KeyListener
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================
	private static IConfigurationManager s_cfg = null;
	
	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Defined symbol for the prompt text end */
	public static final String	PROMPT_SYMBOL	= ">";

	/** Static block to initialize the colors */
	static
	{
		s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
	}

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the real text input. Cannot be subclassed */
	private Text	           m_contents;
	/** Prompt string */
	private String	           m_promptStr;
	/** Part of the prompt string to be removed */
	private String             m_promptRest;
	/** Default prompt string */
	private String	           m_defaultPromptStr;
	/** Hint string, if any */
	private String	           m_hintStr;
	/** History of previous inputs */
	private Vector<String>	   m_previousValues;
	/** Current position at the history */
	private int	               m_historyIndex;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 *            Top composite
	 * @param prompt
	 *            Initial prompt text
	 **************************************************************************/
	public PromptField(Composite parent, String prompt)
	{
		// Create an empty history
		m_previousValues = new Vector<String>();
		m_historyIndex = 0;

		// Create the input text control
		m_contents = new Text(parent, SWT.BORDER | SWT.SINGLE);
		m_contents.setBackground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_BG));
		m_contents.setForeground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_FG));

		// Assign the prompt and hint strings
		m_promptStr = prompt;
		m_defaultPromptStr = prompt;
		m_hintStr = "";

		// Create field decoration
		createDecoration();

		IConfigurationManager cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		m_contents.setFont(cfg.getFont(FontKey.MASTERC));

		// We need to listen ourselves for controlling the input
		m_contents.addKeyListener(this);

		m_contents.setLayoutData(new GridData( GridData.FILL_HORIZONTAL ));

		// Reset the prompt to the initial state
		reset();
	}

	/***************************************************************************
	 * Create decoration for the input field
	 **************************************************************************/
	private void createDecoration()
	{
		// Field decoration
		ControlDecoration deco = new ControlDecoration(m_contents, SWT.LEFT);
		deco.setDescriptionText("Use CTRL+Space to see the possible commands");
		deco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION)
		        .getImage());
		deco.setShowOnlyOnFocus(false);
		deco.setShowHover(true);

		KeyStroke keyStroke;
		ArrayList<String> possibleCommandsList = new ArrayList<String>();
		for (GuiExecutorCommand cmd : GuiExecutorCommand.values())
		{
			possibleCommandsList.add(cmd.label.toLowerCase());
		}
		String[] possibleCommands = possibleCommandsList.toArray(new String[0]);
		char[] autoActivationCharacters = new char[0];
		IContentProposalProvider provider = new SimpleContentProposalProvider(possibleCommands);
		try
		{
			keyStroke = KeyStroke.getInstance("Ctrl+Space");
			new ContentProposalAdapter(m_contents, new TextContentAdapter(), provider, keyStroke,
			        autoActivationCharacters);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 *            Top composite
	 * @param prompt
	 *            Initial prompt text
	 **************************************************************************/
	public PromptField(Composite parent, String prompt, KeyListener listener)
	{
		this(parent, prompt);
		m_contents.addKeyListener(listener);
	}

	/***************************************************************************
	 * Set control font size
	 **************************************************************************/
	public void setFont( Font font )
	{
		m_contents.setFont(font);
	}

	/***************************************************************************
	 * Obtain the wrapped control
	 * 
	 * @return Text the wrapped control
	 **************************************************************************/
	public Text getContents()
	{
		return m_contents;
	}

	/***************************************************************************
	 * Enable or disable the control
	 **************************************************************************/
	public void setEnabled(boolean enable)
	{
		m_contents.setEnabled(enable);
	}

	/***************************************************************************
	 * Reset the prompt field to the initial state
	 **************************************************************************/
	public void reset()
	{
		String suffix = PROMPT_SYMBOL;
		
		if (!m_hintStr.isEmpty())
		{
			// If the hint is too long, don't use it but put it as tooltip
			if (m_hintStr.length()>15)
			{
				m_contents.setToolTipText("Possible inputs: " + m_hintStr);
				m_hintStr = "";
			}
			else
			{
				suffix = " (" + m_hintStr + ")" + suffix;
			}
		}
		else
		{
			m_contents.setToolTipText("Enter prompt answers in this control");
		}
		m_promptStr = m_defaultPromptStr;
		m_promptRest = m_promptStr + suffix;
		m_contents.setText(m_promptRest);
		// Ensure that the caret is at the end of the prompt
		m_contents.setSelection(m_contents.getText().length());
		m_contents.setFocus();
		promptEnd();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void promptStart()
	{
		m_contents.setBackground(s_cfg.getStatusColor(ItemStatus.WARNING));
		m_contents.setForeground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_BG));
		m_contents.setFocus();
	}

	/***************************************************************************
	 * Prompt has finished, so presentation must be set to default
	 **************************************************************************/
	public void promptEnd()
	{
		m_contents.setBackground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_BG));
		m_contents.setForeground(s_cfg.getGuiColor(GuiColorKey.CONSOLE_FG));
	}

	/***************************************************************************
	 * Set the field value
	 * 
	 * @param value
	 *            The field value
	 **************************************************************************/
	public void setValue(String value)
	{
		String suffix = PROMPT_SYMBOL;
		if (!m_hintStr.equals(""))
		{
			suffix = "(" + m_hintStr + ")" + suffix;
		}
		m_contents.setText( m_promptRest + value);
		m_contents.setSelection(m_contents.getText().length());
	}

	/***************************************************************************
	 * Obtain the field value (without the prompt text)
	 * 
	 * @return The field value
	 **************************************************************************/
	public String getValue()
	{
		String answer = m_contents.getText();
		answer = answer.replace( m_promptRest, "");
		return answer;
	}

	/***************************************************************************
	 * Set the hint text.
	 * 
	 * @param hint
	 *            The hint text
	 **************************************************************************/
	public void setHint(String hint)
	{
		m_hintStr = hint;
	}

	/***************************************************************************
	 * Remove the hint text
	 **************************************************************************/
	public void delHint()
	{
		if (!m_hintStr.equals(""))
		{
			m_hintStr = "";
			reset();
		}
	}

	/***************************************************************************
	 * Set the prompt text
	 * 
	 * @param prompt
	 *            The prompt text
	 **************************************************************************/
	public void setPrompt(String prompt)
	{
		String value = getValue();
		m_promptStr = prompt;
		setValue(value);
	}

	/***************************************************************************
	 * Obtain the prompt text
	 * 
	 * @return The prompt text
	 **************************************************************************/
	public String getPrompt()
	{
		return m_promptStr;
	}

	/***************************************************************************
	 * Callback for key pressed event
	 * 
	 * @param e
	 *            Key event
	 **************************************************************************/
	public void keyPressed(KeyEvent e)
	{
		// If the user presses Cursor Up, move one step back in the history
		if (e.keyCode == 16777217)
		{
			if (m_previousValues.size() > 0)
			{
				setValue(m_previousValues.elementAt(m_historyIndex));
				m_historyIndex--;
				if (m_historyIndex < 0) m_historyIndex = 0;
			}
		}
		// If the user presses Cursor Down, move one step further in the hist.
		if (e.keyCode == 16777218)
		{
			if (m_previousValues.size() > 0)
			{
				// If we are at the very beginning of the history, clear
				// the field to make easy to enter a new value
				if (m_historyIndex >= m_previousValues.size() - 1)
				{
					m_historyIndex = m_previousValues.size() - 1;
					setValue("");
				}
				else
				{
					m_historyIndex++;
					setValue(m_previousValues.elementAt(m_historyIndex));
				}
			}
		}
	}

	/***************************************************************************
	 * Callback for key release events
	 * 
	 * @param e
	 *            The key event
	 **************************************************************************/
	public void keyReleased(KeyEvent e)
	{
		// Prevent the user for deleting the prompt
		if (m_contents.getText().indexOf(m_promptStr + PROMPT_SYMBOL) == -1)
		{
			e.doit = false;
			reset();
		}
		// If the enter keys are used, store the field value in the history
		if (e.keyCode == 13 || e.keyCode == 16777296)
		{
			m_previousValues.addElement(getValue());
			m_historyIndex = m_previousValues.size() - 1;
		}
	}
}
