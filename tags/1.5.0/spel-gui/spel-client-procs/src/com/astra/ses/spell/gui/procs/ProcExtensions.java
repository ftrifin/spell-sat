///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs
// 
// FILE      : ProcExtensions.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.procs;

import java.util.ArrayList;
import java.util.Collection;

import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.services.BaseExtensions;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureView;


public class ProcExtensions extends BaseExtensions
{
	private static ProcExtensions s_instance = null;
	
	private Collection<IProcedureView>      m_procedureViewEx;
	
	private static final String EXTENSION_PROCEDURE_VIEW  = "com.astra.ses.spell.gui.extensions.ProcedureView";

	/***************************************************************************
	 * Singleton accessor
	 * @return The singleton instance
	 **************************************************************************/
	public static ProcExtensions get()
	{
		if (s_instance == null)
		{
			s_instance = new ProcExtensions();
		}
		return s_instance;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	protected ProcExtensions()
	{
		m_procedureViewEx = new ArrayList<IProcedureView>();
	}
	
	/***************************************************************************
	 * Load all the available extensions
	 **************************************************************************/
	public void loadExtensions()
	{
		loadExtensions(EXTENSION_PROCEDURE_VIEW, m_procedureViewEx     , IProcedureView.class );
	}
	
	public void firePrompt( Input inputData )
	{
		for(IProcedureView view : m_procedureViewEx )
		{
			view.procedurePrompt(inputData);
			return;
		}
	}

	public void fireCancelPrompt( Input inputData )
	{
		for(IProcedureView view : m_procedureViewEx )
		{
			view.procedureCancelPrompt(inputData);
			return;
		}
	}

	public void fireProcedureDisplay( DisplayData data )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureDisplay( data );
		}
	}

	public void fireProcedureError( ErrorData data )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureError( data );
		}
	}

	public void fireProcedureItem( ItemNotification data )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureItem( data );
		}
	}

	public void fireProcedureLine( LineNotification data )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureLine( data );
		}
	}

	public void fireProcedureStatus( StatusNotification data )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureStatus( data );
		}
	}

	public void fireProcedureCode( CodeNotification data )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureCode( data );
		}
	}

	public void fireModelLoaded( String instanceId )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureModelLoaded( instanceId);
		}
	}

	public void fireModelUnloaded( String instanceId, boolean doneLocally )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureModelUnloaded( instanceId, doneLocally );
		}
	}

	public void fireModelConfigured( String instanceId )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureModelConfigured( instanceId );
		}
	}

	public void fireModelEnabled( String instanceId )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureModelEnabled( instanceId );
		}
	}

	public void fireModelDisabled( String instanceId )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureModelDisabled( instanceId );
		}
	}

	public void fireModelReset( String instanceId )
	{
		for(IProcedureView view : m_procedureViewEx)
		{
			view.procedureModelReset( instanceId );
		}
	}
}
