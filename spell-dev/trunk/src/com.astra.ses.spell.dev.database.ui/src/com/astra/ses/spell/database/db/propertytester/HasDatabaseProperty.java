///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.database.browser.db.propertytester
// 
// FILE      : HasDatabaseProperty.java
//
// DATE      : 2009-09-14
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
package com.astra.ses.spell.database.db.propertytester;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.astra.ses.spell.dev.commands.GetExplorerSelection;
import com.astra.ses.spell.dev.properties.WorkspacePropertyTester;

public class HasDatabaseProperty extends PropertyTester {

	/** Property to check if the resource (or its project) has a defined database */
	private static final String HAS_DATABASE = "hasDatabase";
	
	public HasDatabaseProperty() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		Object result = null;
		/*
		 * Checking if resource or its project has a defined database
		 */
		if (property.equals(HAS_DATABASE))
		{
			result = checkCurrentProcHasDatabase();
		}
		/*
		 * Check if return value is equal to the expectedValue
		 */
		return expectedValue.equals(result);
	}

	/***************************************************************************
	 * Check if the given resource has database
	 * @return
	 **************************************************************************/
	private boolean checkCurrentProcHasDatabase()
	{
		if (!PlatformUI.isWorkbenchRunning()) return false;

		boolean hasDB = false;
		try 
		{
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			Command getSelection = commandService.getCommand(GetExplorerSelection.CMD_ID);

			Object result = getSelection.executeWithChecks(new ExecutionEvent());
			if (result != null)
			{
				Object[] items = (Object[]) result;
				if ((items.length == 1) && (items[0] instanceof IResource))
				{
					IResource resource = (IResource) items[0];
					if (resource.getProject().isAccessible())
					{
						WorkspacePropertyTester tester = new WorkspacePropertyTester();
						hasDB = tester.hasSpellNature((IResource) items[0]);
					}
				}
			}
		} 
		catch (Exception e)
		{
			// Nothing to do
		}

		return hasDB;
	}

}
