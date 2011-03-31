///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : StackHelper.java
//
// DATE      : 2008-11-24 08:34
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.gui.procs.model;

import java.util.Vector;

/*******************************************************************************
 * @brief Help methods for stack management 
 * @date 05/03/09
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class StackHelper 
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================
	private static final String VIEW_MARKER="$";

	// =========================================================================
	// # STATIC METHODS
	// =========================================================================
	
	/***************************************************************************
	 * Obtain the substack corresponding to children procs/lines
	 * @param stack Original stack
	 * @return Substack
	 **************************************************************************/
	public static Vector<String> getSubStack( Vector<String> stack )
	{
		Vector<String> subStack = new Vector<String>();
		int count = 0;
		for(String element : stack)
		{
			if (count>1)
			{
				subStack.add(element);
			}
			count++;
		}
		return subStack;
	}
	
	/***************************************************************************
	 * Obtain the Nth element of the given stack, removing the view marker
	 * if applicable
	 * @param stack
	 * @return
	 **************************************************************************/
	public static String getStackElement( Vector<String> stack, int idx )
	{
		String element = stack.get(idx);
		if (element.startsWith(VIEW_MARKER))
		{
			element = element.substring(1);
		}
		return element;
	}

	/***************************************************************************
	 * Check wether the Nth element of the given stack contains the view marker
	 * @param stack
	 * @return
	 **************************************************************************/
	public static boolean isViewElement( Vector<String> stack, int idx )
	{
		String element = stack.get(idx);
		return element.startsWith(VIEW_MARKER);
	}

	/***************************************************************************
	 * Obtain the Nth element of the given stack, removing the view marker
	 * if applicable
	 * @param stack
	 * @return
	 **************************************************************************/
	public static String[] getViewElement( Vector<String> stack )
	{
		String[] viewElement = new String[2];
		for(int idx=0; idx<stack.size()-1; idx++)
		{
			if (stack.get(idx).startsWith(VIEW_MARKER))
			{
				viewElement[0] = stack.get(idx).substring(1);
				viewElement[1] = stack.get(idx+1);
				return viewElement;
			}
		}
		return null;
	}
}
