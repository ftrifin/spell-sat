///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views
// 
// FILE      : MasterView.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.views;

import java.awt.print.Printable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.interfaces.IShellListener;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.ShellManager;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.extensions.ServerBridge;
import com.astra.ses.spell.gui.model.log.GuiLogModel;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.print.SpellFooterPrinter;
import com.astra.ses.spell.gui.print.SpellHeaderPrinter;
import com.astra.ses.spell.gui.print.graphic.utils.GraphicUtils;
import com.astra.ses.spell.gui.print.printables.TabbedTextPrintable;
import com.astra.ses.spell.gui.services.RuntimeSettingsService;
import com.astra.ses.spell.gui.services.ViewManager;
import com.astra.ses.spell.gui.services.RuntimeSettingsService.RuntimeProperty;
import com.astra.ses.spell.gui.views.controls.FilterUtils;
import com.astra.ses.spell.gui.views.controls.LogViewer;
import com.astra.ses.spell.gui.views.controls.input.PromptField;

/*******************************************************************************
 * @brief This view contains the master console.
 * @date 09/10/07
 ******************************************************************************/
public class MasterView extends ViewPart implements KeyListener, IShellListener
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static ShellManager	s_smgr	= null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** The view identifier */
	public static final String	ID	   = "com.astra.ses.spell.gui.views.MasterView";

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the console manager handle */
	// private ConsoleManager s_console = null;
	/** Console display */
	private Text	            m_display;
	/** The command input field */
	private PromptField	        m_prompt;
	/** Holds the log control */
	private LogViewer	        m_log;
	private GuiLogModel	        m_logModel;
	private boolean	            m_haveShell;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public MasterView()
	{
		super();
		m_haveShell = false;
		Logger.debug("Created", Level.INIT, this);
		s_smgr = (ShellManager) ServiceManager.get(ShellManager.ID);
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
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		Font codeFont = cfg.getFont(FontKey.MASTERC);
		Color bcolor = cfg.getGuiColor(GuiColorKey.CONSOLE_BG);
		Color wcolor = cfg.getGuiColor(GuiColorKey.CONSOLE_FG);

		// Create a group for holding the display and the input field
		Group g = new Group(parent, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		g.setLayout(glayout);
		g.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the console display
		m_display = new Text(g, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		m_display.setFont(codeFont);
		m_display.setBackground(bcolor);
		m_display.setForeground(wcolor);
		m_display.setLayoutData(new GridData(GridData.FILL_BOTH));
		m_display.setText("");
		m_display.setEditable(false);

		GridData data1 = new GridData();
		data1.horizontalAlignment = GridData.FILL;
		data1.grabExcessVerticalSpace = true;
		data1.verticalAlignment = GridData.FILL;
		m_display.setLayoutData(data1);

		// Create the input field
		m_prompt = new PromptField(g, "");
		m_prompt.getContents().setLayoutData(
		        new GridData(GridData.FILL_HORIZONTAL));
		m_prompt.getContents().addKeyListener(this);

		// Place the log viewer
		Composite logTop = new Composite(g, SWT.NONE);
		GridData data2 = new GridData();
		data2.horizontalAlignment = GridData.FILL;
		data2.heightHint = 180;
		logTop.setLayoutData(data2);

		m_logModel = new GuiLogModel();
		m_log = new LogViewer(logTop, m_logModel, Logger.getShowLevel());
		m_logModel.setView(m_log);

		// Create log controls
		Composite buttonBar = new Composite(g, SWT.BORDER);
		GridLayout blayout = new GridLayout();
		blayout.marginHeight = 0;
		blayout.marginWidth = 0;
		blayout.numColumns = 8;
		GridData datab = new GridData(GridData.FILL_HORIZONTAL);
		datab.grabExcessHorizontalSpace = true;
		datab.horizontalAlignment = SWT.FILL;
		buttonBar.setLayout(blayout);
		buttonBar.setLayoutData(datab);

		/* Severity Button */
		Button severityButton = new Button(buttonBar, SWT.PUSH);
		severityButton.setText("Type");
		final Menu severityMenu = new Menu(severityButton);
		severityButton.setMenu(severityMenu);
		/* We want to show the menu even by making left click on the button */
		severityButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				severityMenu.setVisible(true);
			}
		});
		/* End severity Button */
		for (final Severity sev : Severity.values())
		{
			MenuItem severityItem = new MenuItem(severityMenu, SWT.CHECK);
			severityItem.setText(sev.toString());
			severityItem.setData(sev);
			severityItem.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					// Nothing to do
				}

				@Override
				public void widgetSelected(SelectionEvent e)
				{
					MenuItem m = (MenuItem) e.widget;
					Severity sev = (Severity) m.getData();
					if (m.getSelection()) m_logModel.addRequiredSeverity(sev);
					else m_logModel.removeRequiredSeverity(sev);
				}
			});

			if (sev == Severity.DEBUG)
			{
				severityItem.setSelection(Logger.getShowDebug());
			}
			else
			{
				severityItem.setSelection(true);
			}
		}

		/*
		 * Max Level Button
		 */
		Button maxLevelButton = new Button(buttonBar, SWT.PUSH);
		maxLevelButton.setText("Max Level");
		final Menu maxLevelMenu = new Menu(maxLevelButton);
		maxLevelButton.setMenu(maxLevelMenu);
		/* We want to show the menu even by making left click on the button */
		maxLevelButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				maxLevelMenu.setVisible(true);
			}
		});
		for (Level l : Level.values())
		{
			/*
			 * We always put items at position 0 to paint them from highest to
			 * lowest
			 */
			MenuItem levelItem = new MenuItem(maxLevelMenu, SWT.RADIO, 0);
			levelItem.setText(l.name());
			levelItem.setData(l);
			levelItem.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
				}

				@Override
				public void widgetSelected(SelectionEvent e)
				{
					MenuItem levelItem = (MenuItem) e.widget;
					Level level = (Level) levelItem.getData();
					m_logModel.setMaxLevel(level);
				}
			});
		}

		/*
		 * date filter misc objects
		 */
		Date now = new Date();
		SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		date.setLenient(false);
		final String today = date.format(now);
		/*
		 * A KeyListener is created for avoiding typing characters into date
		 * widgets and for skipping forbidden positions
		 */
		KeyListener dateKeyListener = new KeyListener()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				// Only digits are allows
				if (Character.isDigit(e.character))
				{
					Text text = (Text) e.widget;
					/* Get cursor position */
					int position = text.getCaretPosition();
					/* Get widget content */
					String currentValue = text.getText();

					if (position >= today.length())
					{
						text.setSelection(position);
						e.doit = false;
						return;
					}

					// Position is not a forbidden position
					if (!Character.isDigit(currentValue.charAt(position)))
					{
						text.setSelection(++position);
					}
					char[] array = currentValue.toCharArray();
					array[text.getCaretPosition()] = e.character;
					text.setText(String.valueOf(array));
					text.setSelection(++position);

					e.doit = false;
				}
				// Arrow keys
				else if ((e.keyCode == 16777219) || (e.keyCode == 16777220))
				{
					e.doit = true;
				}
				else
				{
					e.doit = false;
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
			}
		};

		/*
		 * Start date widgets
		 */
		Text startDate = new Text(buttonBar, SWT.BORDER);
		startDate.setData(date);
		startDate.setToolTipText("Start date");
		startDate.setTextLimit(today.length());
		startDate.setText(today);
		startDate.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				Text text = (Text) e.widget;
				SimpleDateFormat dateFormat = (SimpleDateFormat) text.getData();
				try
				{
					Date date = dateFormat.parse(text.getText());
					text.setBackground(null);
					m_log.setStartDateFilter(date);
					m_log.refresh();
				}
				catch (ParseException e1)
				{
					Color red = Display.getCurrent().getSystemColor(
					        SWT.COLOR_YELLOW);
					text.setBackground(red);
					return;
				}
			}
		});
		startDate.addKeyListener(dateKeyListener);

		/*
		 * End date filter
		 */
		Text endDate = new Text(buttonBar, SWT.BORDER);
		endDate.setToolTipText("End date");
		endDate.setData(date);
		endDate.setTextLimit(today.length());
		endDate.setText(today);
		endDate.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				Text text = (Text) e.widget;
				SimpleDateFormat dateFormat = (SimpleDateFormat) text.getData();
				try
				{
					Date date = dateFormat.parse(text.getText());
					text.setBackground(null);
					m_log.setEndDateFilter(date);
					m_log.refresh();
				}
				catch (ParseException e1)
				{
					Color red = Display.getCurrent().getSystemColor(
					        SWT.COLOR_YELLOW);
					text.setBackground(red);
					return;
				}
			}
		});
		endDate.addKeyListener(dateKeyListener);

		/*
		 * Message filtering
		 */
		Text messageFilter = new Text(buttonBar, SWT.BORDER);
		messageFilter.setToolTipText("Message");
		GridData messageData = new GridData(GridData.FILL_HORIZONTAL
		        | GridData.GRAB_HORIZONTAL);
		messageFilter.setLayoutData(messageData);
		messageFilter.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				Text messageText = (Text) e.widget;
				String message = messageText.getText();
				String regex = FilterUtils.globify(message);
				Pattern filterPattern = Pattern.compile(regex);
				m_log.setMessageFilter(filterPattern);
				m_log.refresh();
			}
		});

		/*
		 * Source filtering
		 */
		Text sourceFilter = new Text(buttonBar, SWT.BORDER);
		sourceFilter.setToolTipText("Source");
		GridData sourceData = new GridData();
		sourceData.widthHint = 120;
		sourceFilter.setLayoutData(sourceData);
		sourceFilter.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				Text messageText = (Text) e.widget;
				String message = messageText.getText();
				String regex = FilterUtils.globify(message);
				Pattern filterPattern = Pattern.compile(regex);
				m_log.setSourceFilter(filterPattern);
				m_log.refresh();
			}
		});

		/*
		 * Clear Log button
		 */
		Button b = new Button(buttonBar, SWT.PUSH);
		b.setText("Clear log");
		b.setImage(Activator.getImageDescriptor("icons/img_clean.png")
		        .createImage());
		b.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				// Nothing to do
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				m_logModel.clearLog();
			}
		});
		GridData cb = new GridData(GridData.END, GridData.CENTER, true, false);
		b.setLayoutData(cb);

		// Try to load the shell plugin if available
		m_haveShell = s_smgr.haveShell();
		if (m_haveShell)
		{
			Logger.debug("Registering as SHELL listener", Level.PROC, this);
			s_smgr.addShellListener(this);
		}

		// Register for the log service
		ServerBridge.get().addLogListener(m_logModel);

		// Register in the view manager to make this view available
		ViewManager mgr = (ViewManager) ServiceManager.get(ViewManager.ID);
		mgr.registerView(ID, this);

		// Subscribe to selection service. When this part becomes visible,
		// the statuscontribution item is hidden and viceversa.
		getViewSite().setSelectionProvider(m_log);
	}

	/***************************************************************************
	 * Destroy the view.
	 **************************************************************************/
	public void dispose()
	{
		ServerBridge.get().removeLogListener(m_logModel);
		if (m_haveShell)
		{
			s_smgr.removeShellListener(this);
		}
		m_display.dispose();
		m_display = null;
		m_log.dispose();
		m_log = null;
		m_prompt = null;
		super.dispose();
		Logger.debug("Disposed", Level.PROC, this);
	}

	/***************************************************************************
	 * Receive the input focus.
	 **************************************************************************/
	public void setFocus()
	{
		RuntimeSettingsService runtime = (RuntimeSettingsService) ServiceManager
		        .get(RuntimeSettingsService.ID);
		runtime.setRuntimeProperty(RuntimeProperty.ID_PROCEDURE_SELECTION, "");
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
				addDisplayMessage(m_prompt.getPrompt()
				        + PromptField.PROMPT_SYMBOL + cmdString);
				s_smgr.shellInput(cmdString);
			}
			catch (CommandFailed ex)
			{
				// If the console manager raises an error show it on the
				// display
				addDisplayMessage("ERROR (" + m_prompt.getPrompt() + "): "
				        + ex.getLocalizedMessage());
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
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
	{
		if (adapter.equals(Printable.class))
		{
			Table logTable = m_log.getTable();
			int columnCount = logTable.getColumnCount();
			int[] columnsLayout = new int[columnCount];
			String[] tableHeader = new String[columnCount];
			for (int i = 0; i < columnCount; i++)
			{
				TableColumn column = logTable.getColumn(i);
				columnsLayout[i] = column.getWidth();
				tableHeader[i] = column.getText();
			}
			String[][] plainTable = GraphicUtils
			        .convertTableToTextPlain(logTable);
			SpellHeaderPrinter header = new SpellHeaderPrinter("Master Log");
			SpellFooterPrinter footer = new SpellFooterPrinter();
			return new TabbedTextPrintable(plainTable, tableHeader,
			        columnsLayout, header, footer);
		}
		return super.getAdapter(adapter);
	}
}
