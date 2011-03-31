///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences.ui.pages
// 
// FILE      : GeneralPreferencePage.java
//
// DATE      : 2010-05-27
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
package com.astra.ses.spell.gui.preferences.ui.pages;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.preferences.Activator;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.initializer.GUIPreferencesLoader;
import com.astra.ses.spell.gui.preferences.initializer.GUIPreferencesSaver;
import com.astra.ses.spell.gui.preferences.keys.PropertyKey;

public class GeneralPreferencePage extends BasicPreferencesPage
{

	/** Show traces button */
	private Button	m_useTraces;
	/** Show debug traces */
	private Button	m_showDebug;
	/** Debug level */
	private Combo	m_debugLevel;
	/** Connect at startup */
	private Button	m_startupConnect;
	/** Response timeout */
	private Text	m_responseTimeout;
	/** Open timeout */
	private Text	m_openTimeout;
	/** Prompt blink/sound delay */
	private Text	m_promptDelay;
	/** Prompt sound file */
	private Text	m_soundFile;

	@Override
	protected Control createContents(Composite parent)
	{
		// CONTAINER
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		container.setLayout(layout);
		container.setLayoutData(layoutData);

		/* Page layout */
		GridData expand = new GridData(GridData.FILL_HORIZONTAL
		        | GridData.VERTICAL_ALIGN_BEGINNING);
		expand.grabExcessVerticalSpace = true;
		expand.grabExcessHorizontalSpace = true;

		/*
		 * Startup connection button
		 */
		Composite startupConnection = new Composite(container, SWT.NONE);
		startupConnection.setLayout(new GridLayout(2, false));
		startupConnection.setLayoutData(GridDataFactory.copyData(expand));
		// Button
		m_startupConnect = new Button(startupConnection, SWT.CHECK);
		// Label
		Label startupDesc = new Label(startupConnection, SWT.NONE);
		startupDesc.setText("Connect to server at startup");

		/*
		 * Communication group
		 */
		GridLayout communicationLayout = new GridLayout(3, false);
		Group communicationGroup = new Group(container, SWT.BORDER);
		communicationGroup.setText("Communication");
		communicationGroup.setLayout(communicationLayout);
		communicationGroup.setLayoutData(GridDataFactory.copyData(expand));
		/*
		 * Response timeout
		 */
		// Label
		Label responseTimeout = new Label(communicationGroup, SWT.NONE);
		responseTimeout.setText("Response timeout");
		// Text
		m_responseTimeout = new Text(communicationGroup, SWT.BORDER | SWT.RIGHT);
		m_responseTimeout.setLayoutData(GridDataFactory.copyData(expand));
		// Units label
		Label units = new Label(communicationGroup, SWT.NONE);
		units.setText("milliseconds");
		/*
		 * Procedure opening timeout
		 */
		// Label
		Label procedureOpenLabel = new Label(communicationGroup, SWT.NONE);
		procedureOpenLabel.setText("Procedure opening timeout");
		// Text
		m_openTimeout = new Text(communicationGroup, SWT.BORDER | SWT.RIGHT);
		m_openTimeout.setLayoutData(GridDataFactory.copyData(expand));
		// Label
		Label openUnits = new Label(communicationGroup, SWT.NONE);
		openUnits.setText("milliseconds");

		/*
		 * Traces group
		 */
		GridLayout tracingLayout = new GridLayout(1, true);
		Group tracingGroup = new Group(container, SWT.BORDER);
		tracingGroup.setText("Tracing");
		tracingGroup.setLayout(tracingLayout);
		tracingGroup.setLayoutData(GridDataFactory.copyData(expand));
		/*
		 * Show traces
		 */
		m_useTraces = new Button(tracingGroup, SWT.CHECK);
		m_useTraces.setText("Show traces in master view");
		/*
		 * Show debug traces
		 */
		m_showDebug = new Button(tracingGroup, SWT.CHECK);
		m_showDebug.setText("Show debug traces");
		/*
		 * Debug level
		 */
		Composite debugLevel = new Composite(tracingGroup, SWT.NONE);
		debugLevel.setLayout(new GridLayout(2, false));
		debugLevel.setLayoutData(GridDataFactory.copyData(expand));
		// Label
		Label debugLevelLabel = new Label(debugLevel, SWT.NONE);
		debugLevelLabel.setText("Debug level");
		// Combo
		m_debugLevel = new Combo(debugLevel, SWT.READ_ONLY);
		for (Level level : Level.values())
		{
			m_debugLevel.add(level.name());
		}

		/*
		 * User group
		 */
		GridLayout userLayout = new GridLayout(3, true);
		Group userGroup = new Group(container, SWT.BORDER);
		userGroup.setText("User settings");
		userGroup.setLayout(userLayout);
		userGroup.setLayoutData(GridDataFactory.copyData(expand));
		/*
		 * Prompt delay
		 */
		// Label
		Label promptDelayLabel = new Label(userGroup, SWT.NONE);
		promptDelayLabel.setText("Prompt blinking delay");
		m_promptDelay = new Text(userGroup, SWT.BORDER | SWT.RIGHT);
		m_promptDelay.setLayoutData(GridDataFactory.copyData(expand));
		// Label
		Label promptDelayUnitsLabel = new Label(userGroup, SWT.NONE);
		promptDelayUnitsLabel.setText("seconds");
		/*
		 * Prompt sound file
		 */
		// Label
		Label promptSoundLabel = new Label(userGroup, SWT.NONE);
		promptSoundLabel.setText("Prompt sound file");
		m_soundFile = new Text(userGroup, SWT.BORDER);
		m_soundFile.setLayoutData(GridDataFactory.copyData(expand));
		// Browse button
		final Button browse = new Button(userGroup, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent arg0)
			{
				FileDialog dialog = new FileDialog(browse.getShell());
				dialog.setFilterExtensions(new String[] { "*.wav" });
				dialog.setText("Select prompt sound file");
				dialog.setFilterNames(new String[] { "Waveform files (*.wav)" });
				String selected = dialog.open();
				if (selected != null)
				{
					m_soundFile.setText(selected);
				}
			}
		});

		/*
		 * Save group
		 */
		GridLayout saveLayout = new GridLayout(3, true);
		Group saveGroup = new Group(container, SWT.BORDER);
		saveGroup.setText("Save preferences");
		saveGroup.setLayout(saveLayout);
		saveGroup.setLayoutData(GridDataFactory.copyData(expand));

		/*
		 * Save to current XML file
		 */
		final Button saveBtn = new Button(saveGroup, SWT.PUSH);
		saveBtn.setText("Save to current file");
		saveBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				ConfigurationManager conf = getConfigurationManager();
				String cfgFile = conf.getConfigurationFile();
				try
				{
					if (!MessageDialog.openConfirm(saveBtn.getShell(),
					        "Overwrite preferences?",
					        "This action will overwrite the current preferences stored in "
					                + "the default configuration file '"
					                + cfgFile + "'. Do you want to continue?")) return;

					GUIPreferencesSaver saver = new GUIPreferencesSaver(cfgFile);
					saver.savePreferences();
					MessageDialog.openInformation(saveBtn.getShell(),
					        "Preferences saved", "Preferences saved to file '"
					                + cfgFile + "'");
				}
				catch (Exception ex)
				{
					MessageDialog.openError(saveBtn.getShell(),
					        "Cannot save preferences",
					        "Unable to save preferences to '" + cfgFile + "'\n"
					                + ex.getLocalizedMessage());
				}
			}
		});

		/*
		 * Save to alternate XML file
		 */
		final Button saveOtherBtn = new Button(saveGroup, SWT.PUSH);
		saveOtherBtn.setText("Save to another file...");
		saveOtherBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dialog = new FileDialog(saveOtherBtn.getShell(),
				        SWT.SAVE);
				dialog.setFilterExtensions(new String[] { "*.xml" });
				dialog.setText("Select file to save preferences");
				dialog.setFilterNames(new String[] { "XML files (*.xml)" });
				String selected = dialog.open();
				if (selected != null)
				{
					/*
					 * If user has not set the file extension, then add it
					 */
					if (!selected.endsWith(".xml"))
					{
						selected += ".xml";
					}
					try
					{
						File file = new File(selected);
						if (file.exists())
						{
							if (!MessageDialog.openConfirm(
							        saveOtherBtn.getShell(), "Overwrite file?",
							        "File '" + selected
							                + "' already exists. Overwrite?")) return;
						}
						GUIPreferencesSaver saver = new GUIPreferencesSaver(
						        selected);
						saver.savePreferences();
						MessageDialog.openInformation(saveOtherBtn.getShell(),
						        "Preferences saved",
						        "Preferences saved to file '" + selected + "'");
					}
					catch (Exception ex)
					{
						MessageDialog.openError(saveOtherBtn.getShell(),
						        "Cannot save preferences",
						        "Unable to save preferences to '" + selected
						                + "'\n" + ex.getLocalizedMessage());
					}
				}
			}
		});

		/*
		 * Load preferences from an external file
		 */
		final Button loadPrefsBtn = new Button(saveGroup, SWT.PUSH);
		loadPrefsBtn.setText("Load from external file...");
		loadPrefsBtn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog dialog = new FileDialog(loadPrefsBtn.getShell(),
				        SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.xml", "*.XML" });
				String selected = dialog.open();
				if (selected != null)
				{
					File file = new File(selected);
					if (file.exists())
					{
						if (!MessageDialog.openConfirm(
						        loadPrefsBtn.getShell(),
						        "Overwrite preferences?",
						        "All the current values will be replaced with the ones defined in the selected file.\n"
						                + "Do you wish to continue?")) return;

					}

					/*
					 * Overwrite preferences dialog
					 */
					IPreferenceStore store = Activator.getDefault()
					        .getPreferenceStore();
					GUIPreferencesLoader loader = new GUIPreferencesLoader(
					        selected, store);
					boolean loaded = loader.overwrite();
					if (!loaded)
					{
						MessageDialog.openError(
						        loadPrefsBtn.getShell(),
						        "Error while loading configuration file",
						        "An unexpected error ocurred while loading the configuration file.\n"
						                + "Check the application log for details");
					}
				}
			}
		});

		refreshPage();
		return container;
	}

	@Override
	public void performApply()
	{
		ConfigurationManager conf = getConfigurationManager();

		// startup connection
		boolean startup = m_startupConnect.getSelection();
		conf.setBooleanProperty(PropertyKey.STARTUP_CONNECT, startup);
		// response timeout
		String timeout = m_responseTimeout.getText();
		conf.setProperty(PropertyKey.RESPONSE_TIMEOUT, timeout);
		// open timeout
		String openTimeout = m_openTimeout.getText();
		conf.setProperty(PropertyKey.OPEN_TIMEOUT, openTimeout);
		// use traces
		boolean useTraces = m_useTraces.getSelection();
		conf.setBooleanProperty(PropertyKey.USE_TRACES, useTraces);
		// show debug
		boolean showDebug = m_showDebug.getSelection();
		conf.setBooleanProperty(PropertyKey.SHOW_DEBUG, showDebug);
		// debug level
		String debugLevel = m_debugLevel.getItem(m_debugLevel
		        .getSelectionIndex());
		conf.setProperty(PropertyKey.DEBUG_LEVEL, debugLevel);
		// prompt delay
		String delay = m_promptDelay.getText();
		conf.setProperty(PropertyKey.PROMPT_SOUND_DELAY, delay);
		// prompt file
		String file = m_soundFile.getText();
		conf.setProperty(PropertyKey.PROMPT_SOUND_FILE, file);
	}

	@Override
	public void performDefaults()
	{
		ConfigurationManager conf = getConfigurationManager();
		conf.resetProperty(PropertyKey.STARTUP_CONNECT);
		conf.resetProperty(PropertyKey.RESPONSE_TIMEOUT);
		conf.resetProperty(PropertyKey.OPEN_TIMEOUT);
		conf.resetProperty(PropertyKey.USE_TRACES);
		conf.resetProperty(PropertyKey.SHOW_DEBUG);
		conf.resetProperty(PropertyKey.DEBUG_LEVEL);
		conf.resetProperty(PropertyKey.PROMPT_SOUND_DELAY);
		conf.resetProperty(PropertyKey.PROMPT_SOUND_FILE);
		refreshPage();
	}

	@Override
	public void refreshPage()
	{
		ConfigurationManager conf = getConfigurationManager();

		boolean value = conf.getBooleanProperty(PropertyKey.STARTUP_CONNECT);
		m_startupConnect.setSelection(value);
		String timeout = conf.getProperty(PropertyKey.RESPONSE_TIMEOUT);
		m_responseTimeout.setText(timeout);
		String openTimeout = conf.getProperty(PropertyKey.OPEN_TIMEOUT);
		m_openTimeout.setText(openTimeout);
		value = conf.getBooleanProperty(PropertyKey.USE_TRACES);
		m_useTraces.setSelection(value);
		value = conf.getBooleanProperty(PropertyKey.SHOW_DEBUG);
		m_showDebug.setSelection(value);
		String currentLevel = conf.getProperty(PropertyKey.DEBUG_LEVEL);
		for (Level level : Level.values())
		{
			if (level.name().equals(currentLevel))
			{
				m_debugLevel.select(level.ordinal());
			}
		}
		String delay = conf.getProperty(PropertyKey.PROMPT_SOUND_DELAY);
		m_promptDelay.setText(delay);
		String file = conf.getProperty(PropertyKey.PROMPT_SOUND_FILE);
		m_soundFile.setText(file);
	}
}
