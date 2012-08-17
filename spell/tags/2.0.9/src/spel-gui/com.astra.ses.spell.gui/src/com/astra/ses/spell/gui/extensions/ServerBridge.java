///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : ServerBridge.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.extensions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.interfaces.IClientOperation;
import com.astra.ses.spell.gui.core.interfaces.IContextOperation;
import com.astra.ses.spell.gui.core.interfaces.ILogListener;
import com.astra.ses.spell.gui.core.interfaces.IServerOperation;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;

public class ServerBridge
{
	private Set<IClientOperation>	m_clientOperationListeners	= new HashSet<IClientOperation>();
	private Set<IContextOperation>	m_contextOperationListeners	= new HashSet<IContextOperation>();
	private Set<IServerOperation>	m_serverOperationListeners	= new HashSet<IServerOperation>();
	private Set<ILogListener>	   m_logListeners	            = new HashSet<ILogListener>();

	private static ServerBridge	   s_instance	                = null;

	public static ServerBridge get()
	{
		if (s_instance == null)
		{
			s_instance = new ServerBridge();
		}
		return s_instance;
	}

	public void addClientListener(IClientOperation listener)
	{
		m_clientOperationListeners.add(listener);
	}

	public void addContextListener(IContextOperation listener)
	{
		m_contextOperationListeners.add(listener);
	}

	public void addServerListener(IServerOperation listener)
	{
		m_serverOperationListeners.add(listener);
	}

	public void addLogListener(ILogListener listener)
	{
		m_logListeners.add(listener);
	}

	public void removeClientListener(IClientOperation listener)
	{
		m_clientOperationListeners.remove(listener);
	}

	public void removeContextListener(IContextOperation listener)
	{
		m_contextOperationListeners.remove(listener);
	}

	public void removeServerListener(IServerOperation listener)
	{
		m_serverOperationListeners.remove(listener);
	}

	public void removeLogListener(ILogListener listener)
	{
		m_logListeners.remove(listener);
	}

	// ==========================================================================
	void fireClientConnected(final String clientKey, final String host)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IClientOperation clt : m_clientOperationListeners)
				{
					clt.notifyClientConnected(clientKey, host);
				}
			}
		});
	}

	// ==========================================================================
	void fireClientDisconnected(final String clientKey, final String host)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IClientOperation clt : m_clientOperationListeners)
				{
					clt.notifyClientDisconnected(clientKey, host);
				}
			}
		});
	}

	// ==========================================================================
	void fireContextAttached(final ContextInfo info)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IContextOperation clt : m_contextOperationListeners)
				{
					clt.notifyContextAttached(info);
				}
			}
		});
	}

	// ==========================================================================
	void fireContextDetached()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IContextOperation clt : m_contextOperationListeners)
				{
					// Logger.debug("Notify [context detached] to " +
					// clt.getListenerId(), Level.COMM, this);
					clt.notifyContextDetached();
				}
			}
		});
	}

	// ==========================================================================
	void fireListenerConnected(final ServerInfo info)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IServerOperation clt : m_serverOperationListeners)
				{
					clt.notifyListenerConnected(info);
				}
			}
		});
	}

	// ==========================================================================
	void fireListenerDisconnected()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IServerOperation clt : m_serverOperationListeners)
				{
					clt.notifyListenerDisconnected();
				}
			}
		});
	}

	// ==========================================================================
	void fireListenerError(final ErrorData data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IServerOperation clt : m_serverOperationListeners)
				{
					clt.notifyListenerError(data);
				}
			}
		});
	}

	// ==========================================================================
	void fireContextStarted(final ContextInfo info)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IServerOperation clt : m_serverOperationListeners)
				{
					clt.notifyContextStarted(info);
				}
			}
		});
	}

	// ==========================================================================
	void fireContextStopped(final ContextInfo info)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IServerOperation clt : m_serverOperationListeners)
				{
					// Logger.debug("Notify [context stopped] to " +
					// clt.getListenerId(), Level.COMM, this);
					clt.notifyContextStopped(info);
				}
			}
		});
	}

	// ==========================================================================
	void fireContextError(final ErrorData data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IContextOperation clt : m_contextOperationListeners)
				{
					// Logger.debug("Notify [context error] to " +
					// clt.getListenerId(), Level.COMM, this);
					clt.notifyContextError(data);
				}
			}
		});
	}

	// ==========================================================================
	void fireLog(final String message, final String source, final Level level,
	        final Severity severity)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (ILogListener clt : m_logListeners)
				{
					clt.addMessage(message, source, level, severity);
				}
			}
		});
	}

}
