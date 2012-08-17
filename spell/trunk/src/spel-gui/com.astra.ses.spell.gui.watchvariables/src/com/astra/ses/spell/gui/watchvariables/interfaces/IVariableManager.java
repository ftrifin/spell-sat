////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.interfaces
// 
// FILE      : IVariableManager.java
//
// DATE      : 2010-08-13
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.watchvariables.interfaces;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.watchvariables.notification.VariableData;
import com.astra.ses.spell.gui.watchvariables.notification.WhichVariables;

/*******************************************************************************
 * 
 * IVariableManager determines operations performed on procedure variables.
 * 
 ******************************************************************************/
public interface IVariableManager
{
	/***************************************************************************
	 * Obtain all available variables
	 * 
	 * @return List of variable data
	 **************************************************************************/
	public VariableData[] getVariables();

	/***************************************************************************
	 * Obtain all available variables from server
	 * 
	 * @return List of variable data
	 **************************************************************************/
	public void updateModel( IProgressMonitor monitor );

	/***************************************************************************
	 * Obtain all available variables from server
	 * 
	 * @return List of variable data
	 **************************************************************************/
	public void setMode( WhichVariables mode );

	/***************************************************************************
	 * Register a variable watch
	 * 
	 * @param varName
	 *            Name of the variable
	 * @param global
	 *            True if the variable is global
	 * 
	 * @return Current data of the watched variable
	 **************************************************************************/
	public VariableData registerVariableWatch(String varName, boolean global);

	/***************************************************************************
	 * Obtain the data for a given variable
	 * 
	 * @param varName
	 *            Name of the variable
	 * 
	 * @return The variable data
	 **************************************************************************/
	public VariableData getVariable(String varName);

	/***************************************************************************
	 * Change the value of a given variable
	 * 
	 * @param varName
	 *            Name of the variable
	 * @param valueExpression
	 *            Python expression for the value
	 * @param global
	 *            True if the variable is global
	 * 
	 * @return True on success
	 **************************************************************************/
	public boolean changeVariable(String varName, String valueExpression,
	        boolean global);

	/***************************************************************************
	 * Unregister a variable watch
	 * 
	 * @param varName
	 *            Name of the variable
	 * @param global
	 *            True if the variable is global
	 **************************************************************************/
	public void unregisterVariableWatch(String varName, boolean global);

	/***************************************************************************
	 * Clear all variable watches
	 **************************************************************************/
	public void clearAllWatches();

	/***************************************************************************
	 * Add a listener for variable changes
	 * 
	 * @param listener
	 *            The listener
	 **************************************************************************/
	public void addWatchListener(IVariableWatchListener listener);

	/***************************************************************************
	 * Remove a listener for variable changes
	 * 
	 * @param listener
	 *            The listener
	 **************************************************************************/
	public void removeWatchListener(IVariableWatchListener listener);
}
