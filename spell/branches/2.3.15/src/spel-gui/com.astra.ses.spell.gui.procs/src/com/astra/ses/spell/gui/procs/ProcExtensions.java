///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs
// 
// FILE      : ProcExtensions.java
//
// DATE      : 2008-11-21 08:55
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs;

import java.util.ArrayList;
import java.util.Collection;

import com.astra.ses.spell.gui.core.interfaces.BaseExtensions;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.procs.extensionpoints.IProcedureViewExtension;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ProcExtensions extends BaseExtensions
{
	private static ProcExtensions	            s_instance	             = null;

	private Collection<IProcedureViewExtension>	m_procedureViewEx;

	private static final String	                EXTENSION_PROCEDURE_VIEW	= "com.astra.ses.spell.gui.extensions.ProcedureView";

	/***************************************************************************
	 * Singleton accessor
	 * 
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
		m_procedureViewEx = new ArrayList<IProcedureViewExtension>();
	}

	/***************************************************************************
	 * Load all the available extensions
	 **************************************************************************/
	public void loadExtensions()
	{
		loadExtensions(EXTENSION_PROCEDURE_VIEW, m_procedureViewEx,
		        IProcedureViewExtension.class);
	}

	/***************************************************************************
	 * Fire prompt event corresponding to IProcedureInput extensions
	 * 
	 * @param inputData
	 *            Event information
	 **************************************************************************/
	public void firePrompt(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedurePrompt(model);
			return;
		}
	}

	/***************************************************************************
	 * Fire prompt finish event corresponding to IProcedureInput extensions
	 * 
	 * @param inputData
	 *            Event information
	 **************************************************************************/
	public void fireFinishPrompt(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureFinishPrompt(model);
			return;
		}
	}

	/***************************************************************************
	 * Fire prompt cancel event corresponding to IProcedureInput extensions
	 * 
	 * @param inputData
	 *            Event information
	 **************************************************************************/
	public void fireCancelPrompt(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureCancelPrompt(model);
			return;
		}
	}

	/***************************************************************************
	 * Fire display event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureDisplay(IProcedure model, DisplayData data)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureDisplay(model, data);
		}
	}

	/***************************************************************************
	 * Fire error event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureError(IProcedure model, ErrorData data)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureError(model, data);
		}
	}

	/***************************************************************************
	 * Fire item event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureItem(IProcedure model, ItemNotification data)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureItem(model, data);
		}
	}

	/***************************************************************************
	 * Fire stack event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureStack(IProcedure model, StackNotification data)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureStack(model, data);
		}
	}

	/***************************************************************************
	 * Fire status event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureStatus(IProcedure model, StatusNotification data)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureStatus(model, data);
		}
	}

	/***************************************************************************
	 * Fire user action event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureUserAction(IProcedure model,
	        UserActionNotification data)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureUserAction(model, data);
		}
	}

	/***************************************************************************
	 * Fire configuration event corresponding to IProcedureRuntime extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireProcedureConfiguration(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureModelConfigured(model);
		}
	}

	/***************************************************************************
	 * Fire model loaded event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelLoaded(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureModelLoaded(model);
		}
	}

	/***************************************************************************
	 * Fire model unloaded event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelUnloaded(IProcedure model, UnloadType type )
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureModelUnloaded(model, type);
		}
	}

	/***************************************************************************
	 * Fire model configured event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelConfigured(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureModelConfigured(model);
		}
	}

	/***************************************************************************
	 * Fire model enabled event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelEnabled(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureModelEnabled(model);
		}
	}

	/***************************************************************************
	 * Fire model disabled event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelDisabled(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureModelDisabled(model);
		}
	}

	/***************************************************************************
	 * Fire model reset event corresponding to IProcedureView extensions
	 * 
	 * @param data
	 *            Event information
	 **************************************************************************/
	public void fireModelReset(IProcedure model)
	{
		for (IProcedureViewExtension view : m_procedureViewEx)
		{
			view.notifyProcedureModelReset(model);
		}
	}

}
