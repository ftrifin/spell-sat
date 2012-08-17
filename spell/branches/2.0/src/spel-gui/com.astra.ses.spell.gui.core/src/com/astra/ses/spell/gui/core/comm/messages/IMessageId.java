///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : IMessageId.java
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
package com.astra.ses.spell.gui.core.comm.messages;

interface IMessageId
{
	// /////////////////////////////////////////////////////////////////////////
	// SPELL Context Messages
	// /////////////////////////////////////////////////////////////////////////

	/** Oneway messages */
	public final String	MSG_LOGIN	         = "MSG_GUI_LOGIN";
	public final String	MSG_LOGOUT	         = "MSG_GUI_LOGOUT";
	public final String	MSG_CLOSE	         = "MSG_CLOSE_CTX";
	public final String	MSG_CLIENT_OP	     = "MSG_CLIENT_OP";
	public final String	MSG_EXEC_OP	         = "MSG_EXEC_OP";
	public final String	MSG_CONTEXT_OP	     = "MSG_CONTEXT_OP";
	public final String	MSG_CANCEL	         = "MSG_CANCEL";
	public final String	MSG_UNKNOWN	         = "MSG_UNKNOWN";
	public final String	MSG_PING	         = "MSG_PING";
	public final String	MSG_PROMPT_START	 = "MSG_PROMPT_START";
	public final String	MSG_PROMPT_END	     = "MSG_PROMPT_END";

	public final String	MSG_SETUACTION	     = "MSG_SET_UACTION";
	public final String	MSG_DISABLEUACTION	 = "MSG_DISABLE_UACTION";
	public final String	MSG_ENABLEUACTION	 = "MSG_ENABLE_UACTION";
	public final String	MSG_DISMISSUACTION	 = "MSG_DISMISS_UACTION";

	public final String	MSG_SHOWNODEDEPTH	 = "MSG_SHOW_NODE_DEPTH";

	public final String	MSG_VARIABLE_CHANGE	 = "MSG_VARIABLE_CHANGE";
	public final String	MSG_SCOPE_CHANGE	 = "MSG_SCOPE_CHANGE";

	// Nasty workarround, there is a bug in python subprocess.Popen and sockets
	public final String	MSG_LISTENER_LOST	 = "MSG_LISTENER_LOST";
	public final String	MSG_CONTEXT_LOST	 = "MSG_CONTEXT_LOST";

	/** Identifier for proc code request */
	public final String	REQ_PROC_CODE	     = "REQ_PROC_CODE";
	/** Identifier for proc list request */
	public final String	REQ_PROC_LIST	     = "REQ_PROC_LIST";
	/** Identifier for procedure properties request */
	public final String	REQ_PROC_PROP	     = "REQ_PROC_PROP";

	/** Identifier for create executor request */
	public final String	REQ_OPEN_EXEC	     = "REQ_OPEN_EXEC";
	/** Identifier for closing executor request */
	public final String	REQ_CLOSE_EXEC	     = "REQ_CLOSE_EXEC";
	/** Identifier for killing executor request */
	public final String	REQ_KILL_EXEC	     = "REQ_KILL_EXEC";
	/** Identifier for attach to executor request */
	public final String	REQ_ATTACH_EXEC	     = "REQ_ATTACH_EXEC";
	/** Identifier for executor list request */
	public final String	REQ_EXEC_LIST	     = "REQ_EXEC_LIST";
	/** Identifier for detach from executor request */
	public final String	REQ_DETACH_EXEC	     = "REQ_DETACH_EXEC";
	/** Identifier for getting executor info */
	public final String	REQ_EXEC_INFO	     = "REQ_EXEC_INFO";
	/** Identifier for getting client info */
	public final String	REQ_CLIENT_INFO	     = "REQ_CLIENT_INFO";
	/** Identifier for getting asrun file */
	public final String	REQ_SERVER_FILE	     = "REQ_SERVER_FILE";
	/** Identifier for getting a procedure id */
	public final String	REQ_INSTANCE_ID	     = "REQ_INSTANCE_ID";
	/** Identifier for setting executor configuration */
	public final String	REQ_SET_CONFIG	     = "REQ_SET_CONFIG";
	/** Identifier for getting executor configuration */
	public final String	REQ_GET_CONFIG	     = "REQ_GET_CONFIG";
	/** Identfiier for toggling a breakpoint */
	public final String	REQ_SET_BREAKPOINT	 = "REQ_SET_BREAKPOINT";
	/** Identifier for removing all breakpoints in the code */
	public final String	REQ_CLEAR_BREAKPOINT	= "REQ_CLEAR_BREAKPOINT";
	public final String	REQ_VARIABLE_NAMES	 = "REQ_VARIABLE_NAMES";
	public final String	REQ_VARIABLE_WATCH	 = "REQ_VARIABLE_WATCH";
	public final String	REQ_VARIABLE_NOWATCH	= "REQ_VARIABLE_NOWATCH";
	public final String	REQ_WATCH_NOTHING	 = "REQ_WATCH_NOTHING";
	public final String	REQ_CHANGE_VARIABLE	 = "REQ_CHANGE_VARIABLE";

	// /////////////////////////////////////////////////////////////////////////
	// SPELL Listener Messages
	// /////////////////////////////////////////////////////////////////////////

	/** Identifier for create context request */
	public final String	REQ_OPEN_CTX	     = "REQ_OPEN_CTX";
	/** Identifier for destroy context request */
	public final String	REQ_CLOSE_CTX	     = "REQ_CLOSE_CTX";
	/** Identifier for attach to ctx request */
	public final String	REQ_ATTACH_CTX	     = "REQ_ATTACH_CTX";
	/** Identifier for ctx list request */
	public final String	REQ_CTX_LIST	     = "REQ_CTX_LIST";
	/** Identifier for detach from ctx request */
	public final String	REQ_DETACH_CTX	     = "REQ_DETACH_CTX";
	/** Identifier for destroy ctx request */
	public final String	REQ_DESTROY_CTX	     = "REQ_DESTROY_CTX";
	/** Identifier for getting ctx info */
	public final String	REQ_CTX_INFO	     = "REQ_CTX_INFO";
}
