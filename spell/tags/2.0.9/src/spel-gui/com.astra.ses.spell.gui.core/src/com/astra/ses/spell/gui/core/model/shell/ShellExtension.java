///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.shell
// 
// FILE      : ShellExtension.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.model.shell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.astra.ses.spell.gui.core.interfaces.IShellListener;
import com.astra.ses.spell.gui.core.interfaces.IShellManager;
import com.astra.ses.spell.gui.core.services.BaseExtensions;

/*******************************************************************************
 * Shell extension plugins loader class
 ******************************************************************************/
public class ShellExtension extends BaseExtensions
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the singleton instance */
	private static ShellExtension	  s_instance	  = null;
	/** Holds the extension plugin id */
	private static final String	      EXTENSION_SHELL	= "com.astra.ses.spell.gui.extensions.ShellManager";
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the set of loaded extensions (only the first one is taken) */
	private Collection<IShellManager>	m_shellEx;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	/***************************************************************************
	 * Singleton accessor
	 * 
	 * @return The singleton instance
	 **************************************************************************/
	public static ShellExtension get()
	{
		if (s_instance == null)
		{
			s_instance = new ShellExtension();
		}
		return s_instance;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	protected ShellExtension()
	{
		m_shellEx = new ArrayList<IShellManager>();
	}

	/***************************************************************************
	 * Load the plugins providing the extension
	 **************************************************************************/
	@Override
	public void loadExtensions()
	{
		loadExtensions(EXTENSION_SHELL, m_shellEx, IShellManager.class);
	}

	/***************************************************************************
	 * Check if there is any plugin providing the shell extension
	 * 
	 * @return True if there is such a plugin
	 **************************************************************************/
	public boolean haveShell()
	{
		return !m_shellEx.isEmpty();
	}

	/***************************************************************************
	 * Add a shell listener to receive shell output
	 * 
	 * @param lst
	 *            The shell listener
	 **************************************************************************/
	public void addShellListener(IShellListener lst)
	{
		if (haveShell())
		{
			Iterator<IShellManager> it = m_shellEx.iterator();
			IShellManager mgr = it.next();
			mgr.addShellOutputListener(lst);
		}
	}

	/***************************************************************************
	 * Remove a given shell listener
	 * 
	 * @param lst
	 *            The listener
	 **************************************************************************/
	public void removeShellListener(IShellListener lst)
	{
		if (haveShell())
		{
			Iterator<IShellManager> it = m_shellEx.iterator();
			IShellManager mgr = it.next();
			mgr.removeShellOutputListener(lst);
		}
	}

	/***************************************************************************
	 * Provide input to the shell
	 * 
	 * @param input
	 *            Input string with the command(s)
	 **************************************************************************/
	public void shellInput(String input)
	{
		if (haveShell())
		{
			Iterator<IShellManager> it = m_shellEx.iterator();
			IShellManager mgr = it.next();
			mgr.shellInput(input);
		}
	}
}
