///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : ProcedureBridge.java
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
package com.astra.ses.spell.gui.extensions;

import java.util.HashSet;
import java.util.Set;

import com.astra.ses.spell.gui.core.interfaces.IProcedureOperation;
import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureView;


public class ProcedureBridge 
{
	private Set<IProcedureView> m_procedureListeners = new HashSet<IProcedureView>();
	private Set<IProcedureOperation> m_procedureMonitors = new HashSet<IProcedureOperation>();
	
	private static ProcedureBridge s_instance = null;
	
	public static ProcedureBridge get()
	{
		if (s_instance == null)
		{
			s_instance = new ProcedureBridge();
		}
		return s_instance;
	}
	
	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addProcedureListener( IProcedureView listener )
	{
		m_procedureListeners.add(listener);
	}

	public void removeProcedureListener( IProcedureView listener )
	{
		m_procedureListeners.remove(listener);
	}

	public void addProcedureMonitor( IProcedureOperation monitor )
	{
		m_procedureMonitors.add(monitor);
	}

	public void removeProcedureMonitor( IProcedureOperation monitor )
	{
		m_procedureMonitors.remove(monitor);
	}

	//==========================================================================
	//==========================================================================
	void fireModelDisabled( String instanceId )
	{
		GuiBridge.execute(this, "_fireModelDisabledOp", instanceId);
	}

	public void _fireModelDisabledOp(String instanceId)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [model disabled] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureModelDisabled(instanceId);
		}
	}

	//==========================================================================
	void fireModelEnabled( String instanceId )
	{
		GuiBridge.execute(this, "_fireModelEnabledOp", instanceId);
	}

	public void _fireModelEnabledOp(String instanceId)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [model enabled] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureModelEnabled(instanceId);
		}
	}

	//==========================================================================
	void fireModelLoaded( String instanceId )
	{
		GuiBridge.execute(this, "_fireModelLoadedOp", instanceId);
	}

	public void _fireModelLoadedOp(String instanceId)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [model loaded] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureModelLoaded(instanceId);
		}
	}

	//==========================================================================
	void fireModelReset( String instanceId )
	{
		GuiBridge.execute(this, "_fireModelResetOp", instanceId);
	}

	public void _fireModelResetOp(String instanceId)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [model reset] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureModelReset(instanceId);
		}
	}

	//==========================================================================
	void fireModelUnloaded( String instanceId, boolean doneLocally )
	{
		GuiBridge.execute(this, "_fireModelUnloadedOp", instanceId, new Boolean(doneLocally));
	}

	public void _fireModelUnloadedOp(String instanceId, Boolean doneLocally )
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [model unloaded] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureModelUnloaded(instanceId, doneLocally );
		}
	}

	//==========================================================================
	void fireModelConfigured( String instanceId )
	{
		GuiBridge.execute(this, "_fireModelConfiguredOp", instanceId);
	}

	public void _fireModelConfiguredOp(String instanceId)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [model configured] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureModelConfigured(instanceId);
		}
	}

	//==========================================================================
	void fireProcedureCode( CodeNotification data )
	{
		GuiBridge.execute(this, "_fireProcedureCodeOp", data);
	}

	public void _fireProcedureCodeOp(CodeNotification data)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [procedure code] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureCode(data);
		}
	}

	//==========================================================================
	void fireProcedureDisplay( DisplayData data )
	{
		GuiBridge.execute(this, "_fireProcedureDisplayOp", data);
	}

	public void _fireProcedureDisplayOp(DisplayData data)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [procedure display] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureDisplay(data);
		}
	}

	//==========================================================================
	void fireProcedureError( ErrorData data )
	{
		GuiBridge.execute(this, "_fireProcedureErrorOp", data);
	}

	public void _fireProcedureErrorOp(ErrorData data)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [procedure error] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureError(data);
		}
	}

	//==========================================================================
	void fireProcedureItem( ItemNotification data )
	{
		GuiBridge.execute(this, "_fireProcedureItemOp", data);
	}

	public void _fireProcedureItemOp(ItemNotification data)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [procedure item] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureItem(data);
		}
	}

	//==========================================================================
	void fireProcedureLine( LineNotification data )
	{
		GuiBridge.execute(this, "_fireProcedureLineOp", data);
	}

	public void _fireProcedureLineOp(LineNotification data)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [procedure line] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureLine(data);
		}
	}

	//==========================================================================
	void fireProcedureStatus( StatusNotification data )
	{
		GuiBridge.execute(this, "_fireProcedureStatusOp1", data);
	}

	public void _fireProcedureStatusOp1(StatusNotification data)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [procedure status] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureStatus(data);
		}
	}

	//==========================================================================
	void fireProcedureCancelPrompt( Input data )
	{
		GuiBridge.execute(this, "_fireProcedureCancelPromptOp", data);
	}

	public void _fireProcedureCancelPromptOp(Input inputData)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [procedure prompt cancel] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedureCancelPrompt(inputData);
		}
	}

	//==========================================================================
	void fireProcedurePrompt( Input data )
	{
		GuiBridge.execute(this, "_fireProcedurePromptOp", data);
	}

	public void _fireProcedurePromptOp(Input inputData)
	{
		for(IProcedureView clt : m_procedureListeners)
		{
			//Logger.debug("Notify [procedure prompt] to " + clt.getListenerId(), Level.COMM, this);
			clt.procedurePrompt(inputData);
		}
	}

	//==========================================================================
	void fireProcedureClosed( String procId, String guiKey )
	{
		GuiBridge.execute(this, "_fireProcedureClosedOp", procId, guiKey);
	}

	public void _fireProcedureClosedOp(String procId, String guiKey)
	{
		for(IProcedureOperation mon : m_procedureMonitors)
		{
			//Logger.debug("Notify [procedure closed] to " + mon.getListenerId(), Level.COMM, this);
			mon.procedureClosed(procId,guiKey);
		}
	}

	//==========================================================================
	void fireProcedureControlled( String procId, String guiKey )
	{
		GuiBridge.execute(this, "_fireProcedureControlledOp", procId, guiKey);
	}

	public void _fireProcedureControlledOp(String procId, String guiKey)
	{
		for(IProcedureOperation mon : m_procedureMonitors)
		{
			//Logger.debug("Notify [procedure controlled] to " + mon.getListenerId(), Level.COMM, this);
			mon.procedureControlled(procId,guiKey);
		}
	}

	//==========================================================================
	void fireProcedureKilled( String procId, String guiKey )
	{
		GuiBridge.execute(this, "_fireProcedureKilledOp", procId, guiKey);
	}

	public void _fireProcedureKilledOp(String procId, String guiKey)
	{
		for(IProcedureOperation mon : m_procedureMonitors)
		{
			//Logger.debug("Notify [procedure killed] to " + mon.getListenerId(), Level.COMM, this);
			mon.procedureKilled(procId,guiKey);
		}
	}

	//==========================================================================
	void fireProcedureMonitored( String procId, String guiKey )
	{
		GuiBridge.execute(this, "_fireProcedureMonitoredOp", procId, guiKey);
	}

	public void _fireProcedureMonitoredOp(String procId, String guiKey)
	{
		for(IProcedureOperation mon : m_procedureMonitors)
		{
			//Logger.debug("Notify [procedure monitored] to " + mon.getListenerId(), Level.COMM, this);
			mon.procedureMonitored(procId,guiKey);
		}
	}

	//==========================================================================
	void fireProcedureOpen( String procId, String guiKey )
	{
		GuiBridge.execute(this, "_fireProcedureOpenOp", procId, guiKey);
	}

	public void _fireProcedureOpenOp(String procId, String guiKey)
	{
		for(IProcedureOperation mon : m_procedureMonitors)
		{
			//Logger.debug("Notify [procedure open] to " + mon.getListenerId(), Level.COMM, this);
			mon.procedureOpen(procId,guiKey);
		}
	}

	//==========================================================================
	void fireProcedureReleased( String procId, String guiKey )
	{
		GuiBridge.execute(this, "_fireProcedureReleasedOp", procId, guiKey);
	}

	public void _fireProcedureReleasedOp(String procId, String guiKey)
	{
		for(IProcedureOperation mon : m_procedureMonitors)
		{
			//Logger.debug("Notify [procedure released] to " + mon.getListenerId(), Level.COMM, this);
			mon.procedureReleased(procId,guiKey);
		}
	}

	//==========================================================================
	void fireProcedureStatus( String procId, ExecutorStatus status, String guiKey )
	{
		GuiBridge.execute(this, "_fireProcedureStatusOp2", procId, status, guiKey);
	}

	public void _fireProcedureStatusOp2(String procId, ExecutorStatus status, String guiKey)
	{
		for(IProcedureOperation mon : m_procedureMonitors)
		{
			//Logger.debug("Notify [procedure status] to " + mon.getListenerId(), Level.COMM, this);
			mon.procedureStatus(procId,status,guiKey);
		}
	}
}
