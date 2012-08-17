///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.watchvariables.notification
// 
// FILE      : WhichVariables.java
//
// DATE      : Nov 28, 2011
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.watchvariables.notification;

/*******************************************************************************
 * @brief Defines which variables are interesting.
 ******************************************************************************/
public enum WhichVariables
{
	/** Show all global variables existing in the current scope */
	AVAILABLE_GLOBALS,
	/** Show all local variables existing in the current scope */
	AVAILABLE_LOCALS,
	/** Show all variables existing in the current scope */
	AVAILABLE_ALL,
	/** Show only the global registered variables */
	REGISTERED_GLOBALS,
	/** Show only the local registered variables */
	REGISTERED_LOCALS,
	/** Show all registered variables */
	REGISTERED_ALL,
	/** Show no variables */
	NONE;

	public static WhichVariables fromValues(boolean registered,
	        boolean globals, boolean locals)
	{
		WhichVariables result = null;
		if (globals && locals)
		{
			if (registered)
			{
				result = REGISTERED_ALL;
			}
			else
			{
				result = AVAILABLE_ALL;
			}
		}
		else if (globals)
		{
			if (registered)
			{
				result = REGISTERED_GLOBALS;
			}
			else
			{
				result = AVAILABLE_GLOBALS;
			}
		}
		else if (locals)
		{
			if (registered)
			{
				result = REGISTERED_LOCALS;
			}
			else
			{
				result = AVAILABLE_LOCALS;
			}
		}
		else
		{
			result = NONE;
		}
		return result;
	}
}
