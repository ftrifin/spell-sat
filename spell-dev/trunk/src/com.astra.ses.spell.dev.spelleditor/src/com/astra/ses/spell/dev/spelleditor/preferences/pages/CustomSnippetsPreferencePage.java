///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.preferences
// 
// FILE      : CustomSnippetsPreferencePage.java
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

import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

import com.astra.ses.spell.dev.spelleditor.preferences.SnippetPreferences;

public class CustomSnippetsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	/** Available snippets */
	private Map<String, String> m_snippets;
	/** Available snippets combo */
	private Combo m_combo;
	/** Name */
	private Text m_name;
	/** Snippet */
	private Text m_code;
	
	/****************************************************************************
	 * Constructor
	 ***************************************************************************/
	public CustomSnippetsPreferencePage() 
	{
		
	}

	/****************************************************************************
	 * Constructor
	 ***************************************************************************/
	public CustomSnippetsPreferencePage(String title) 
	{
		super(title);
	}

	/****************************************************************************
	 * Constructor
	 ***************************************************************************/
	public CustomSnippetsPreferencePage(String title, ImageDescriptor image)
	{
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout layout = new GridLayout(1,true);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		parent.setLayout(layout);
		parent.setLayoutData(data);
		/*
		 * Available Snippets
		 */
		Group availableSnippets = new Group(parent, SWT.BORDER);
		availableSnippets.setText("Available snippets");
		GridData avData = new GridData(GridData.FILL_HORIZONTAL);
		availableSnippets.setLayoutData(avData);
		//LAYOUT
		GridLayout avLayout = new GridLayout(2,false);
		availableSnippets.setLayout(avLayout);
		//WDIGETS
		Label avLabel = new Label(availableSnippets, SWT.NONE);
		avLabel.setText("Snippet");
		GridData comboData = new GridData(GridData.FILL_HORIZONTAL);
		comboData.grabExcessHorizontalSpace = true;
		m_combo = new Combo(availableSnippets, SWT.READ_ONLY);
		m_combo.setLayoutData(comboData);
		/*
		 * Snippets edition
		 */
		Group snippetEdition = new Group(parent, SWT.BORDER);
		snippetEdition.setText("Edit");
		GridData edData = new GridData(SWT.FILL, SWT.FILL, true, true);
		edData.grabExcessHorizontalSpace = true;
		edData.grabExcessVerticalSpace = true;
		snippetEdition.setLayoutData(edData);
		//LAYOUT
		GridLayout edLayout = new GridLayout(2,false);
		snippetEdition.setLayout(edLayout);
		//WIDGETS
		Label nameLabel = new Label(snippetEdition, SWT.NONE);
		nameLabel.setText("Name");
		
		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
		nameData.grabExcessHorizontalSpace = true;
		m_name = new Text(snippetEdition, SWT.BORDER);
		m_name.setLayoutData(nameData);
		
		Label snippetLabel = new Label(snippetEdition, SWT.NONE);
		snippetLabel.setText("Code");
		
		GridData snippetData = new GridData(SWT.FILL, SWT.FILL, true, true);
		snippetData.grabExcessVerticalSpace = true;
		m_code = new Text(snippetEdition, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		m_code.setLayoutData(snippetData);
		
		Composite buttonsComposite = new Composite(snippetEdition, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(3, true);
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonData.horizontalSpan = 2;
		buttonsComposite.setLayout(buttonLayout);
		buttonsComposite.setLayoutData(buttonData);
		
		final Button saveSnippet = new Button(buttonsComposite, SWT.PUSH);
		saveSnippet.setText("Save");
		saveSnippet.setEnabled(false);
		
		final Button addSnippet = new Button(buttonsComposite, SWT.PUSH);
		addSnippet.setText("Add");
		addSnippet.setEnabled(false); 
		
		final Button deleteSnippet = new Button(buttonsComposite, SWT.PUSH);
		deleteSnippet.setText("Remove");
		deleteSnippet.setEnabled(false);
		
		/*
		 * Event handling
		 */
		//Name or code have been modified
		ModifyListener modify = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e) {
				String snippetName = m_name.getText();
				String snippetCode = m_code.getText();
				boolean newSnippet = true;
				for (String item : m_combo.getItems())
				{
					if (item.equals(snippetName))
					{
						newSnippet = false;
						break;
					}
				}
				boolean empty = snippetName.isEmpty() || snippetCode.isEmpty();
				saveSnippet.setEnabled(!newSnippet && !empty);
				addSnippet.setEnabled(newSnippet && !empty);
				deleteSnippet.setEnabled(!empty);
			}
		};
		m_name.addModifyListener(modify);
		m_code.addModifyListener(modify);
		// SAVE SNIPPET BUTTON WAS PRESSED
		SelectionAdapter update = new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				String snippetName = m_name.getText();
				String snippetCode = m_code.getText();
				m_snippets.put(snippetName, snippetCode);
				fillWidgets();
			}
		};
		saveSnippet.addSelectionListener(update);
		addSnippet.addSelectionListener(update);
		// DELETE SNIPPET BUTTON SELECTED
		SelectionAdapter remove = new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e) {
				String snippetName = m_name.getText();
				m_snippets.remove(snippetName);
				fillWidgets();
			}
		};
		deleteSnippet.addSelectionListener(remove);
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
		
		fillWidgets();
				
		return parent;
	}

	@Override
	public void init(IWorkbench workbench) {
		m_snippets = new SnippetPreferences().getAvailableSnippets();
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
		String[] snippets = new String[m_snippets.size()];
		m_snippets.keySet().toArray(snippets);
		m_combo.setItems(snippets);
		boolean enabled = m_snippets.size() > 0;
		if (enabled)
		{
			m_combo.select(0);
			updateEdition(snippets[0]);
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
		if (!selection.isEmpty())
		{
			m_name.setText(selection);
			m_code.setText(m_snippets.get(selection));
		}
		else
		{
			m_name.setText("");
			m_code.setText("");
		}
	}
	
	@Override
	public boolean performOk()
	{
		SnippetPreferences prefs = new SnippetPreferences();
		prefs.storePreferences(m_snippets);
		return super.performOk();
	}
}