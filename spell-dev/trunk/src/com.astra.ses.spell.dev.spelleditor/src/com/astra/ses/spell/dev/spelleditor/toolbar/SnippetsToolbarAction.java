///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.toolbar
// 
// FILE      : DropDownItems.java
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
package com.astra.ses.spell.dev.spelleditor.toolbar;

import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;

import com.astra.ses.spell.dev.spelleditor.Activator;
import com.astra.ses.spell.dev.spelleditor.actions.ConditionalStatementAction;
import com.astra.ses.spell.dev.spelleditor.actions.CustomSnippetAction;
import com.astra.ses.spell.dev.spelleditor.actions.ForLoopStatementAction;
import com.astra.ses.spell.dev.spelleditor.actions.SpellCodeAction;
import com.astra.ses.spell.dev.spelleditor.actions.TimeVariablesAction;
import com.astra.ses.spell.dev.spelleditor.actions.WhileLoopStatementAction;
import com.astra.ses.spell.dev.spelleditor.preferences.SnippetPreferences;

public class SnippetsToolbarAction extends SpellEditorToolbarAction {
	
	/****************************************************************************
	 * 
	 ***************************************************************************/
	private class DropDownSnippetsAction extends DropDownAction {

		/**
		 * Constructor
		 */
		public DropDownSnippetsAction(String text) {
			super(text);
		}

		/************************************************************************
		 * Menu manager
		 * @param mgr
		 ***********************************************************************/
		protected void fillMenu(Menu menu)
		{
			IEditorPart editor = getActiveEditor();
			// SNIPPETS
			SNIPPET_ACTIONS[0].setActiveEditor(editor);
			this.addActionToMenu(menu, SNIPPET_ACTIONS[0]);
			SNIPPET_ACTIONS[1].setActiveEditor(editor);
			this.addActionToMenu(menu, SNIPPET_ACTIONS[1]);
			SNIPPET_ACTIONS[2].setActiveEditor(editor);
			this.addActionToMenu(menu, SNIPPET_ACTIONS[2]);
			// TIME
			MenuItem time = new MenuItem(menu, SWT.CASCADE);
			time.setMenu(new Menu(menu));
			time.setText("TIME");
			Menu timeMenu = time.getMenu();
			for (int i = 3; i < SNIPPET_ACTIONS.length; i++)
			{
				SNIPPET_ACTIONS[i].setActiveEditor(editor);
				this.addActionToMenu(timeMenu, SNIPPET_ACTIONS[i]);
			}
			// CUSTOM
			loadCustomSnippets();
			if (CUSTOM_SNIPPET_ACTIONS.length > 0)
			{
				MenuItem custom = new MenuItem(menu, SWT.CASCADE);
				custom.setMenu(new Menu(menu));
				custom.setText("CUSTOM");
				Menu customMenu = custom.getMenu();
				for (SpellCodeAction action : CUSTOM_SNIPPET_ACTIONS)
				{
					action.setActiveEditor(editor);
					this.addActionToMenu(customMenu, action);
				}
			}
		}
	}
	
	/** Actions over the editor */
	private static final SpellCodeAction[] SNIPPET_ACTIONS;
	/** Custom snippet actions */
	private static SpellCodeAction[] CUSTOM_SNIPPET_ACTIONS;
	/** Icon path */
	private static final String ICON = "icon/help.png";
	/** Action label */
	private static final String LABEL = "Snippets";
	
	static
	{
		/*
		 * Default Snippet actions
		 */
		SNIPPET_ACTIONS = new SpellCodeAction[]
		                                       {
				new ConditionalStatementAction(),
				new WhileLoopStatementAction(),
				new ForLoopStatementAction(),
				new TimeVariablesAction("Now", "NOW"),
				new TimeVariablesAction("Today","TODAY"),
				new TimeVariablesAction("Tomorrow","TOMORROW"),
				new TimeVariablesAction("Yesterday","YESTERDAY"),
				new TimeVariablesAction("Hour","HOUR"),
				new TimeVariablesAction("Minute","MINUTE"),
				new TimeVariablesAction("Second","SECOND")
		                                       };
		/*
		 * Custom snippets
		 */
		SnippetPreferences prefs = new SnippetPreferences();
		Map<String, String> available = prefs.getAvailableSnippets();
		CUSTOM_SNIPPET_ACTIONS = new SpellCodeAction[available.size()];
		int i = 0;
		for (String key : available.keySet())
		{
			String code = available.get(key);
			CUSTOM_SNIPPET_ACTIONS[i] = new CustomSnippetAction(key, code);
			i++;
		}
	}
	
	/****************************************************************************
	 * Constructor
	 ***************************************************************************/
	public SnippetsToolbarAction() {
		super(LABEL);		
	}
	
	/***************************************************************************
	 * Load snippets as defined in preferences
	 **************************************************************************/
	private static void loadCustomSnippets()
	{
		/*
		 * Custom snippets
		 */
		SnippetPreferences prefs = new SnippetPreferences();
		Map<String, String> available = prefs.getAvailableSnippets();
		CUSTOM_SNIPPET_ACTIONS = new SpellCodeAction[available.size()];
		int i = 0;
		for (String key : available.keySet())
		{
			String code = available.get(key);
			CUSTOM_SNIPPET_ACTIONS[i] = new CustomSnippetAction(key, code);
			i++;
		}
	}

	@Override
	public IAction createToolbarAction() {
		IAction toolbarAction = new DropDownSnippetsAction(LABEL);
		toolbarAction.setToolTipText(LABEL);
		toolbarAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, ICON));
		return toolbarAction;
	}

	@Override
	public void prepareMenu(IMenuManager mgr) {
		for (IAction action : SNIPPET_ACTIONS) {
			mgr.add(action);
		}
		for (IAction action : CUSTOM_SNIPPET_ACTIONS)
		{
			mgr.add(action);
		}
	}
}
