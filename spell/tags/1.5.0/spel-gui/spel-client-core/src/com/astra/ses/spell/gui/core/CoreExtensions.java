///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core
// 
// FILE      : CoreExtensions.java
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
package com.astra.ses.spell.gui.core;

import java.util.ArrayList;
import java.util.Collection;

import com.astra.ses.spell.gui.core.interfaces.IClientOperation;
import com.astra.ses.spell.gui.core.interfaces.IContextOperation;
import com.astra.ses.spell.gui.core.interfaces.ILogListener;
import com.astra.ses.spell.gui.core.interfaces.IProcedureInput;
import com.astra.ses.spell.gui.core.interfaces.IProcedureOperation;
import com.astra.ses.spell.gui.core.interfaces.IProcedureRuntime;
import com.astra.ses.spell.gui.core.interfaces.IServerOperation;
import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.BaseExtensions;
import com.astra.ses.spell.gui.core.services.Logger;


public class CoreExtensions extends BaseExtensions
{
	private static CoreExtensions s_instance = null;

	private Collection<IClientOperation>    m_clientOperationEx;
	private Collection<IContextOperation>   m_contextOperationEx;
	private Collection<ILogListener> 	    m_logEx;
	private Collection<IProcedureInput>	    m_procInputEx;
	private Collection<IProcedureOperation> m_procOperationEx;
	private Collection<IProcedureRuntime>   m_procRuntimeEx;
	private Collection<IServerOperation>    m_serverOperationEx;
	
	private static final String EXTENSION_CLIENT_OP = "com.astra.ses.spell.gui.extensions.ClientOperations";
	private static final String EXTENSION_CTX_OP    = "com.astra.ses.spell.gui.extensions.ContextOperations";
	private static final String EXTENSION_LOG 		= "com.astra.ses.spell.gui.extensions.Logging";
	private static final String EXTENSION_PROC_IO   = "com.astra.ses.spell.gui.extensions.ProcedureIO";
	private static final String EXTENSION_PROC_OP   = "com.astra.ses.spell.gui.extensions.ProcedureOperations";
	private static final String EXTENSION_PROC_RT   = "com.astra.ses.spell.gui.extensions.ProcedureRuntime";
	private static final String EXTENSION_SERVER_OP = "com.astra.ses.spell.gui.extensions.ServerOperations";

	/***************************************************************************
	 * Singleton accessor
	 * @return The singleton instance
	 **************************************************************************/
	public static CoreExtensions get()
	{
		if (s_instance == null)
		{
			s_instance = new CoreExtensions();
		}
		return s_instance;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	protected CoreExtensions()
	{
		m_clientOperationEx = new ArrayList<IClientOperation>();
		m_contextOperationEx = new ArrayList<IContextOperation>();
		m_logEx = new ArrayList<ILogListener>();
		m_procInputEx = new ArrayList<IProcedureInput>();
		m_procOperationEx = new ArrayList<IProcedureOperation>();
		m_procRuntimeEx = new ArrayList<IProcedureRuntime>();
		m_serverOperationEx = new ArrayList<IServerOperation>();
	}
	
	/***************************************************************************
	 * Load all the available extensions
	 **************************************************************************/
	@Override
	public void loadExtensions()
	{
		loadExtensions(EXTENSION_CLIENT_OP, m_clientOperationEx   , IClientOperation.class );
		loadExtensions(EXTENSION_CTX_OP   , m_contextOperationEx  , IContextOperation.class );
		loadExtensions(EXTENSION_LOG      , m_logEx               , ILogListener.class );
		loadExtensions(EXTENSION_PROC_IO  , m_procInputEx         , IProcedureInput.class );
		loadExtensions(EXTENSION_PROC_OP  , m_procOperationEx     , IProcedureOperation.class );
		loadExtensions(EXTENSION_PROC_RT  , m_procRuntimeEx 	  , IProcedureRuntime.class );
		loadExtensions(EXTENSION_SERVER_OP, m_serverOperationEx   , IServerOperation.class );
	}
	
	public void fireClientConnected( String clientKey, String host )
	{
		for(IClientOperation clt : m_clientOperationEx)
		{
			Logger.debug("Notify [client connected] to " + clt.getListenerId(), Level.COMM, this);
			clt.clientConnected(clientKey, host);
		}
	}

	public void fireClientDisconnected( String clientKey, String host )
	{
		for(IClientOperation clt : m_clientOperationEx)
		{
			Logger.debug("Notify [client disconnected] to " + clt.getListenerId(), Level.COMM, this);
			clt.clientDisconnected(clientKey,host);
		}
	}

	public void fireContextAttached( ContextInfo info )
	{
		for(IContextOperation clt : m_contextOperationEx)
		{
			Logger.debug("Notify [context attached] to " + clt.getListenerId(), Level.COMM, this);
			clt.contextAttached(info);
		}
	}

	public void fireContextDetached()
	{
		for(IContextOperation clt : m_contextOperationEx)
		{
			Logger.debug("Notify [context detached] to " + clt.getListenerId(), Level.COMM, this);
			clt.contextDetached();
		}
	}
	
	public void fireLog( String message, String source, Level level, Severity severity )
	{
		for(ILogListener clt : m_logEx)
		{
			clt.addMessage(message, source, level, severity);
		}
	}
	
	public void firePrompt( Input inputData )
	{
		for(IProcedureInput clt : m_procInputEx )
		{
			clt.procedurePrompt(inputData);
			return;
		}
	}

	public void fireCancelPrompt( Input inputData )
	{
		for(IProcedureInput clt : m_procInputEx )
		{
			clt.procedureCancelPrompt(inputData);
			return;
		}
	}

	public void fireProcedureOpen( String procId, String guiKey )
	{
		for(IProcedureOperation clt : m_procOperationEx)
		{
			clt.procedureOpen( procId, guiKey );
		}
	}

	public void fireProcedureClosed( String procId, String guiKey )
	{
		for(IProcedureOperation clt : m_procOperationEx)
		{
			clt.procedureClosed( procId, guiKey );
		}
	}
	
	public void fireProcedureKilled( String procId, String guiKey )
	{
		for(IProcedureOperation clt : m_procOperationEx)
		{
			clt.procedureKilled( procId, guiKey );
		}
	}
	
	public void fireProcedureControlled( String procId, String guiKey )
	{
		for(IProcedureOperation clt : m_procOperationEx)
		{
			clt.procedureControlled( procId, guiKey );
		}
	}

	public void fireProcedureMonitored( String procId, String guiKey )
	{
		for(IProcedureOperation clt : m_procOperationEx)
		{
			clt.procedureMonitored( procId, guiKey );
		}
	}
	
	public void fireProcedureReleased( String procId, String guiKey )
	{
		for(IProcedureOperation clt : m_procOperationEx)
		{
			clt.procedureReleased( procId, guiKey );
		}
	}
	
	public void fireProcedureStatus( String procId, ExecutorStatus status, String guiKey )
	{
		for(IProcedureOperation clt : m_procOperationEx)
		{
			clt.procedureStatus( procId, status, guiKey );
		}
	}

	public void fireProcedureDisplay( DisplayData data )
	{
		for(IProcedureRuntime clt : m_procRuntimeEx)
		{
			clt.procedureDisplay( data );
		}
	}

	public void fireProcedureError( ErrorData data )
	{
		for(IProcedureRuntime clt : m_procRuntimeEx)
		{
			clt.procedureError( data );
		}
	}

	public void fireProcedureItem( ItemNotification data )
	{
		for(IProcedureRuntime clt : m_procRuntimeEx)
		{
			clt.procedureItem( data );
		}
	}

	public void fireProcedureLine( LineNotification data )
	{
		for(IProcedureRuntime clt : m_procRuntimeEx)
		{
			clt.procedureLine( data );
		}
	}

	public void fireProcedureStatus( StatusNotification data )
	{
		for(IProcedureRuntime clt : m_procRuntimeEx)
		{
			clt.procedureStatus( data );
		}
	}

	public void fireProcedureCode( CodeNotification data )
	{
		for(IProcedureRuntime clt : m_procRuntimeEx)
		{
			clt.procedureCode( data );
		}
	}

	public void fireListenerConnected( ServerInfo info )
	{
		for(IServerOperation clt : m_serverOperationEx)
		{
			Logger.debug("Notify [listener connected] to " + clt.getListenerId(), Level.COMM, this);
			clt.listenerConnected( info );
		}
	}

	public void fireListenerDisconnected()
	{
		for(IServerOperation clt : m_serverOperationEx)
		{
			Logger.debug("Notify [listener disconnected] to " + clt.getListenerId(), Level.COMM, this);
			clt.listenerDisconnected();
		}
	}

	public void fireListenerError( ErrorData data )
	{
		for(IServerOperation clt : m_serverOperationEx)
		{
			Logger.debug("Notify [listener error] to " + clt.getListenerId(), Level.COMM, this);
			clt.listenerError(data);
		}
	}

	public void fireContextStarted( ContextInfo info )
	{
		for(IServerOperation clt : m_serverOperationEx)
		{
			Logger.debug("Notify [context started] to " + clt.getListenerId(), Level.COMM, this);
			clt.contextStarted( info );
		}
	}

	public void fireContextStopped( ContextInfo info )
	{
		for(IServerOperation clt : m_serverOperationEx)
		{
			Logger.debug("Notify [context stopped] to " + clt.getListenerId(), Level.COMM, this);
			clt.contextStopped( info );
		}
	}

	public void fireContextError( ErrorData data )
	{
		for(IContextOperation clt : m_contextOperationEx)
		{
			Logger.debug("Notify [context error] to " + clt.getListenerId(), Level.COMM, this);
			clt.contextError(data);
		}
	}
}
