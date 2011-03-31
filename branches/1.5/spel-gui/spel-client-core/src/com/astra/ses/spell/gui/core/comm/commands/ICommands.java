///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.commands
// 
// FILE      : ICommands.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.comm.commands;

/*******************************************************************************
 * @brief Interface describing the set of commands that can be sent to the SPELL
 *        server.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public interface ICommands
{
	// =========================================================================
	// # DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** Identifier for code toggle */
	public static final int IMG_TOGGLE_CODE = 0;
	/** Identifier for log toggle */
	public static final int IMG_TOGGLE_LOG = 1;
	/** Identifier for menu toggle */
	public static final int IMG_TOGGLE_MENU = 2;
	/** Identifier for showing proc info */
	public static final int IMG_SHOW_INFO = 0;
	/** Identifier for more font size */
	public static final int IMG_MORE = 1;
	/** Identifier for less font size */
	public static final int IMG_LESS = 2;

	public static final String ToggleIcons[] =
	{
		"icons/16x16/code.png",	
		"icons/16x16/log.png",	
		"icons/16x16/menu.png",	
	};

	public static final String OtherIcons[] =
	{
		"icons/16x16/info.png",	
		"icons/16x16/more.png",	
		"icons/16x16/less.png"	
	};
	
	public static final String CMD_ABORT = "CMD_ABORT";
	public static final String CMD_CONTINUE = "CMD_CONTINUE";
	public static final String CMD_CUSTOM = "CMD_CUSTOM";
	public static final String CMD_GOTO = "CMD_GOTO";
	public static final String CMD_PAUSE = "CMD_PAUSE";
	public static final String CMD_RELOAD = "CMD_RELOAD";
	public static final String CMD_RUN = "CMD_RUN";
	public static final String CMD_SCRIPT = "CMD_SCRIPT";
	public static final String CMD_SKIP = "CMD_SKIP";
	public static final String CMD_STEP = "CMD_STEP";
	public static final String CMD_STEP_OVER = "CMD_STEP_OVER";
}
