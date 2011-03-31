///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.startup
// 
// FILE      : SetPythonInterpreterCommand.java
//
// DATE      : 2010-05-21
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
package com.astra.ses.spell.dev.startup;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.interpreters.PythonInterpreterManager;

import com.astra.ses.spell.dev.preferences.SpellPythonInterpreterPage;

/*******************************************************************************
 *
 * SetPythonInterpretarCommand check that python interpretar has been set.
 * If it doesn't then user is asked to set it.
 *
 ******************************************************************************/
public class SetPythonInterpreterCommand extends AbstractHandler {

	public static final String CMD_ID = "com.astra.ses.spell.dev.command.setpythoninterpreter";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*
		 * Step 1. Resolve if Python interpreter has been set previously
		 */
		String nodeQualifier = PydevPlugin.DEFAULT_PYDEV_SCOPE;
		String prefName = PythonInterpreterManager.PYTHON_INTERPRETER_PATH;
		String prefPage = SpellPythonInterpreterPage.PAGE_ID;

		IEclipsePreferences confScope = new InstanceScope().getNode(nodeQualifier);
		String interpreter = confScope.get(prefName, "");
		if (interpreter.isEmpty())
		{
			IEclipsePreferences defScope = new DefaultScope().getNode(nodeQualifier);
			interpreter = defScope.get(prefName, "");
		}
		if (!interpreter.isEmpty())
		{
			//Python interpretar has been set 
			return null;
		}
		/*
		 * Step 2. Open python interpretar wizard
		 */
		String title = "Python intrepreter has not beeen set";
		String message=  "Python interpreter has not been configured yet. " +
				"It is required for procedures development.\n" +
				"Would you like to configure it now?\n" +
				"It can also be configured later through the Window > Preferences menu.";
		Shell shell = new Shell();
		boolean open = MessageDialog.openQuestion(shell, title, message);
		if (open)
		{
			PreferenceDialog dialog = PreferencesUtil.
				createPreferenceDialogOn(shell, prefPage, null, null, SWT.NONE);
			dialog.open();
		}
		return null;
	}

}
