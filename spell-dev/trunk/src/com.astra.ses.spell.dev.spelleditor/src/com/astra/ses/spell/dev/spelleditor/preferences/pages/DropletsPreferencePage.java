///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.preferences
// 
// FILE      : DropletsPreferencePage.java
//
// DATE      : 2009-11-23
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.spelleditor.preferences.pages;

import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.astra.ses.spell.dev.spelleditor.dnd.droplet.IOfflineDatabaseDroplet;
import com.astra.ses.spell.dev.spelleditor.preferences.DropletPreferences;

public class DropletsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	/** Available snippets */
	private IOfflineDatabaseDroplet[] m_droplets;
	/** Droplet preferences */
	private DropletPreferences m_preferences;
	/** Available snippets combo */
	private Combo m_combo;
	/** Snippet */
	private Text m_code;
	
	public DropletsPreferencePage() {
	}

	public DropletsPreferencePage(String title) {
		super(title);
	}

	public DropletsPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout layout = new GridLayout(1,true);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayout(layout);
		parent.setLayoutData(data);
		/*
		 * Available Droplets
		 */
		Group availableDroplets = new Group(parent, SWT.BORDER);
		availableDroplets.setText("Available snippets");
		GridData avData = new GridData(GridData.FILL_HORIZONTAL);
		availableDroplets.setLayoutData(avData);
		//LAYOUT
		GridLayout avLayout = new GridLayout(2,false);
		availableDroplets.setLayout(avLayout);
		//WDIGETS
		Label avLabel = new Label(availableDroplets, SWT.NONE);
		avLabel.setText("Droplet");
		GridData comboData = new GridData(GridData.FILL_HORIZONTAL);
		comboData.grabExcessHorizontalSpace = true;
		m_combo = new Combo(availableDroplets, SWT.READ_ONLY);
		m_combo.setLayoutData(comboData);
		/*
		 * Droplet edition
		 */
		Group dropletEdition = new Group(parent, SWT.BORDER);
		dropletEdition.setText("Edit");
		GridData edData = new GridData(SWT.FILL, SWT.FILL, true, true);
		edData.grabExcessHorizontalSpace = true;
		edData.grabExcessVerticalSpace = true;
		dropletEdition.setLayoutData(edData);
		//LAYOUT
		GridLayout edLayout = new GridLayout(2,false);
		dropletEdition.setLayout(edLayout);
		//WIDGETS			
		GridData snippetData = new GridData(SWT.FILL, SWT.FILL, true, true);
		snippetData.grabExcessVerticalSpace = true;
		snippetData.verticalSpan = 7;
		m_code = new Text(dropletEdition, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		m_code.setLayoutData(snippetData);
		
		/*Composite buttonsComposite = new Composite(dropletEdition, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(3, true);
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonData.horizontalSpan = 2;
		buttonsComposite.setLayout(buttonLayout);
		buttonsComposite.setLayoutData(buttonData);*/

		// COMBO SELECTION CHANGED
		m_combo.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				Combo combo = (Combo) e.widget;
				String selection = combo.getItem(combo.getSelectionIndex());
				updateEdition(selection);
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.widget;
				String selection = combo.getItem(combo.getSelectionIndex());
				updateEdition(selection);
			}
		});
		
		/*
		 * Buttons for inserting code templates
		 */
		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		
		Button loopButton = new Button(dropletEdition, SWT.PUSH);
		loopButton.setText("LOOP");
		loopButton.setLayoutData(GridDataFactory.copyData(buttonData));
		loopButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) {
				String code = 
					"<foreach type=\"\" separator=\"\">\n" +
					"</foreach>\n";
				m_code.insert(code);
			}
		});
		
		Button tmNameButton = new Button(dropletEdition, SWT.PUSH);
		tmNameButton.setText("TM name");
		tmNameButton.setLayoutData(GridDataFactory.copyData(buttonData));
		tmNameButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) {
				String code = "<tm_name>";
				m_code.insert(code);
			}
		});
		
		Button tmDescButton = new Button(dropletEdition, SWT.PUSH);
		tmDescButton.setText("TM description");
		tmDescButton.setLayoutData(GridDataFactory.copyData(buttonData));
		tmDescButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) {
				String code = "<tm_desc>";
				m_code.insert(code);
			}
		});
		
		Button tcNameButton = new Button(dropletEdition, SWT.PUSH);
		tcNameButton.setText("TC name");
		tcNameButton.setLayoutData(GridDataFactory.copyData(buttonData));
		tcNameButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) {
				String code = "<tc_name>";
				m_code.insert(code);
			}
		});
		
		Button tcInfoButton = new Button(dropletEdition, SWT.PUSH);
		tcInfoButton.setText("TC info");
		tcInfoButton.setLayoutData(GridDataFactory.copyData(buttonData));
		tcInfoButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e) {
				String code = "<tc_info>";
				m_code.insert(code);
			}
		});
		
		fillWidgets();
				
		return parent;
	}
	
	/****************************************************************************
	 * Fill the widgets with the contents of the map
	 ***************************************************************************/
	private void fillWidgets()
	{
		/*
		 * Fill the combo with the available elements
		 */
		m_combo.removeAll();
		String[] items = new String[m_droplets.length];
		int i = 0;
		// Droplets are referenced by the combo as data
		for (IOfflineDatabaseDroplet droplet : m_droplets)
		{
			items[i++] = droplet.getName();
			m_combo.setData(droplet.getName(), droplet);
		}
		//Droplets are ordered alphabetically
		Arrays.sort(items);
		m_combo.setItems(items);
		boolean enabled = m_droplets.length > 0;
		if (enabled)
		{
			m_combo.select(0);
			updateEdition(m_combo.getItem(0));
		}
		else
		{
			updateEdition("");
		}
		m_combo.setEnabled(enabled);
	}
	
	/****************************************************************************
	 * Fill edition widgets according to the selection
	 * @param selection
	 ***************************************************************************/
	private void updateEdition(String selection)
	{
		/*
		 * Update contents
		 */
		if (!selection.isEmpty())
		{
			IOfflineDatabaseDroplet droplet = (IOfflineDatabaseDroplet) m_combo.getData(selection);
			String code = m_preferences.getDropletCode(droplet);
			m_code.setText(code);
			m_code.setData(droplet);
		}
		else
		{
			m_code.setText("");
			m_code.setData(null);
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		m_preferences = new DropletPreferences();
		m_droplets = DropletPreferences.getDroplets();
	}
	
	@Override
	public boolean performOk()
	{
		/*
		 * Save previous data
		 */
		Object data = m_code.getData();
		if (data != null)
		{
			IOfflineDatabaseDroplet droplet = (IOfflineDatabaseDroplet) data;
			m_preferences.setDropletCode(droplet, m_code.getText());
		}
		return super.performOk();
	}
	
	@Override
	public void performDefaults()
	{
		/*
		 * Save previous data
		 */
		Object data = m_code.getData();
		if (data != null)
		{
			IOfflineDatabaseDroplet droplet = (IOfflineDatabaseDroplet) data;
			String defaultCode = droplet.getDefaultDropletCode();
			m_preferences.setDropletCode(droplet, defaultCode);
			m_code.setText(defaultCode);
		}
		super.performDefaults();
	}
}