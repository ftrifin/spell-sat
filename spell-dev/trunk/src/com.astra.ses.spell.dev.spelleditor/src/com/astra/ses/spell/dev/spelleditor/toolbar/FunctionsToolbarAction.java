///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.toolbar
// 
// FILE      : FunctionsToolbarAction.java
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

import java.util.Arrays;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Menu;

import com.astra.ses.spell.dev.spelleditor.actions.SpellCodeAction;
import com.astra.ses.spell.language.SpellProgrammingLanguage;

public class FunctionsToolbarAction extends SpellEditorToolbarAction {

	/******************************************************************************
	 * 
	 *****************************************************************************/
	private class DropDownFunctionsAction extends DropDownAction
	{
		public DropDownFunctionsAction(String text) {
			super(text);
		}

		@Override
		protected void fillMenu(Menu menu) {
			for (SpellCodeAction action : FUNCTIONS)
			{
				action.setActiveEditor(getActiveEditor());
				this.addActionToMenu(menu, action);
			}	
		}	
	}
	
	/** Function to show in the toolbar */
	private static final SpellCodeAction[] FUNCTIONS;
	/** Action label */
	private static final String LABEL = "Functions";
	
	static
	{
		SpellProgrammingLanguage lang = SpellProgrammingLanguage.getInstance();
		String[] functions = lang.getSpellFunctions();
		Arrays.sort(functions);
		FUNCTIONS = new SpellCodeAction[functions.length];
		int i = 0;
		for (String function : functions)
		{
			FUNCTIONS[i] = new SpellCodeAction(function) {	
				@Override
				public String getCodeSnippet() {
					return getText() + "()";
				}
			};
			i++;
		}
	}
	
	/****************************************************************************
	 * Constructor
	 ***************************************************************************/
	public FunctionsToolbarAction()
	{
		super(LABEL);
	}

	@Override
	public IAction createToolbarAction() {
		IAction toolbarAction = new DropDownFunctionsAction(LABEL);
		toolbarAction.setToolTipText(LABEL);
		//toolbarAction.setImageDescriptor(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, ICON));
		
		return toolbarAction;
	}

	@Override
	public void prepareMenu(IMenuManager mgr) {
		for (IAction action : FUNCTIONS)
		{
			mgr.add(action);
		}
	}
}
