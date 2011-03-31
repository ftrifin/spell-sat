///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.services
// 
// FILE      : ProcedureManager.java
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
package com.astra.ses.spell.gui.procs.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.interfaces.IContextOperation;
import com.astra.ses.spell.gui.core.interfaces.IProcedureInput;
import com.astra.ses.spell.gui.core.interfaces.IProcedureOperation;
import com.astra.ses.spell.gui.core.interfaces.IProcedureRuntime;
import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.server.AsRunFile;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.server.LogFile;
import com.astra.ses.spell.gui.core.model.server.TabbedFile;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.services.BaseService;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.procs.ProcExtensions;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.exceptions.NotConnected;
import com.astra.ses.spell.gui.procs.exceptions.UnloadFailed;
import com.astra.ses.spell.gui.procs.model.Procedure;
import com.astra.ses.spell.gui.procs.utils.GrabProcedureTask;


/*******************************************************************************
 * @brief Manages procedure models.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ProcedureManager extends BaseService 
implements IContextOperation, IProcedureInput, IProcedureRuntime, IProcedureOperation
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the server proxy handle */
	private static ContextProxy s_ctx = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** The service identifier */
	public static final String ID = "com.astra.ses.spell.gui.procs.ProcedureManager";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	// PRIVATE -----------------------------------------------------------------
	/** Holds the list of local procedures */
	private Map<String,Procedure> m_localModels;
	/** Holds the list of remote procedures */
	private Map<String,ExecutorInfo> m_remoteModels;
	/** Holds the list of valid procedure identifiers */
	private Map<String, String> m_availableProcedures;
	
	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	public ProcedureManager()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
		m_localModels = new TreeMap<String,Procedure>();
		m_remoteModels = new TreeMap<String,ExecutorInfo>();
		m_availableProcedures = new HashMap<String, String>();
		ServiceManager.registerService(this);
	}
	
	/* #########################################################################
	 * # BASIC SERVICE METHODS
	 * #######################################################################*/
		
	/***************************************************************************
	 * Setup the service
	 **************************************************************************/
	public void setup()
	{
	}
	
	/***************************************************************************
	 * Cleanup the service
	 **************************************************************************/
	public void cleanup()
	{
	}
	
	/***************************************************************************
	 * Subscribe to required resources
	 **************************************************************************/
	public void subscribe()
	{
		s_ctx = (ContextProxy) ServiceManager.get(ContextProxy.ID);
	}

	/* #########################################################################
	 * # PROCEDURE MANAGEMENT METHODS
	 * #######################################################################*/
	
	/***************************************************************************
	 * Open a new procedure instance for the given procedure. 
	 * 
	 * @param procedureId The procedure identifier, no instance number info
	 * @throws LoadFailed if the procedure could not be loaded
	 **************************************************************************/
	public void openProcedure( String procedureId, Map<String,String> arguments ) throws LoadFailed
	{
		Logger.debug("Opening procedure " + procedureId, Level.PROC, this);
		loadLocalProcedure(procedureId,true,ClientMode.CONTROLLING,arguments);
	}

	/***************************************************************************
	 * Attach an existing procedure instance in control mode. 
	 * 
	 * @param procedureId The procedure identifier, no instance number info
	 * @throws LoadFailed if the procedure could not be attached
	 **************************************************************************/
	public void controlProcedure( String procedureId ) throws LoadFailed
	{
		Logger.debug("Attaching procedure " + procedureId + " in control mode", Level.PROC, this);
		loadLocalProcedure(procedureId,false,ClientMode.CONTROLLING,null);
	}

	/***************************************************************************
	 * Attach an existing procedure instance in monitoring mode. 
	 * 
	 * @param procedureId The procedure identifier, no instance number info
	 * @throws LoadFailed if the procedure could not be attached
	 **************************************************************************/
	public void monitorProcedure( String procedureId ) throws LoadFailed
	{
		Logger.debug("Attaching procedure " + procedureId + " in monitor mode", Level.PROC, this);
		loadLocalProcedure(procedureId,false,ClientMode.MONITORING,null);
	}

	/***************************************************************************
	 * Schedule a new procedure instance for the given procedure. 
	 * 
	 * @param procedureId The procedure identifier, no instance number info
	 * @throws LoadFailed if the procedure could not be loaded
	 **************************************************************************/
	public void scheduleProcedure( String procedureId, String condition ) throws LoadFailed
	{
		int procCount = m_localModels.size() + m_remoteModels.size();
		ContextInfo info = s_ctx.getInfo();
		if (procCount == info.getMaxProc())
		{
			throw new LoadFailed("Cannot schedule procedure\n\n" +
			"Maximum number of procedures for this driver (" + procCount + ") reached.");
		}
		Logger.debug("Scheduling procedure " + procedureId, Level.PROC, this);
		doScheduleProcedure(procedureId,condition);
	}

	/***************************************************************************
	 * Close a given procedure instance. 
	 * 
	 * @param instanceId The procedure identifier, WITH instance number info
	 * @throws UnloadFailed if the procedure could not be unloaded
	 **************************************************************************/
	public void closeProcedure( String instanceId ) throws UnloadFailed
	{
		Logger.debug("Closing procedure " + instanceId, Level.PROC, this);
		if (isLocallyLoaded(instanceId))
		{
			unloadLocalProcedure(instanceId,false,false);
		}
		else
		{
			unloadRemoteProcedure(instanceId, false);
		}
	}

	/***************************************************************************
	 * Release a given procedure instance. 
	 * 
	 * @param instanceId The procedure identifier, WITH instance number info
	 * @throws UnloadFailed if the procedure could not be unloaded
	 **************************************************************************/
	public void releaseProcedure( String instanceId ) throws UnloadFailed
	{
		Logger.debug("Detaching procedure " + instanceId, Level.PROC, this);
		Procedure proc = getProcedure(instanceId);
		// The procedure becomes remote
		m_remoteModels.put(instanceId, proc.getInfo());
		// Unload the local procedure
		unloadLocalProcedure(instanceId, false, true);
		
	}

	/***************************************************************************
	 * Kill a given procedure instance. 
	 * 
	 * @param instanceId The procedure identifier, WITH instance number info
	 * @throws UnloadFailed if the procedure could not be unloaded
	 **************************************************************************/
	public void killProcedure( String instanceId ) throws UnloadFailed
	{
		Logger.debug("Killing procedure " + instanceId, Level.PROC, this);
		if (isLocallyLoaded(instanceId))
		{
			unloadLocalProcedure(instanceId,true, false);
		}
		else
		{
			unloadRemoteProcedure(instanceId, true);
		}
	}

	/***************************************************************************
	 * Obtain a local procedure model. 
	 * 
	 * @param instanceId The instance identifier
	 * @return The procedure model
	 **************************************************************************/
	public Procedure getProcedure(String instanceId) throws NoSuchProcedure
	{
		if (m_localModels.containsKey(instanceId))
		{
			return m_localModels.get(instanceId);
		}
		else
		{
			throw new NoSuchProcedure(instanceId);
		}
	}

	/***************************************************************************
	 * Check if a procedure is loaded. 
	 * 
	 * @param instanceId The instance identifier
	 * @return True if the model is loaded
	 **************************************************************************/
	public boolean isLocallyLoaded(String instanceId) 
	{
		return m_localModels.containsKey(instanceId);
	}

	/***************************************************************************
	 * Obtain a remote procedure model. 
	 * 
	 * @param instanceId The instance identifier
	 * @return The procedure reduced model
	 **************************************************************************/
	public synchronized ExecutorInfo getRemoteProcedure(String instanceId) throws NoSuchProcedure
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			ExecutorInfo info = m_remoteModels.get(instanceId);
			s_ctx.updateExecutorInfo(instanceId, info);
			return info;
		}
		else
		{
			throw new NoSuchProcedure(instanceId);
		}
	}

	/***************************************************************************
	 * Obtain the asrun file of a procedure 
	 * 
	 * @param instanceId The instance identifier
	 * @return The asrun file data
	 **************************************************************************/
	public TabbedFile getServerFile(String instanceId, ServerFileType ServerFileId) throws NoSuchProcedure
	{
		if (!m_remoteModels.containsKey(instanceId) && !m_localModels.containsKey(instanceId))
		{
			throw new NoSuchProcedure(instanceId);
		}
		Vector<String> source = s_ctx.getServerFile(instanceId, ServerFileId);
		TabbedFile file = null;
		if (ServerFileId == ServerFileType.EXECUTOR_LOG)
		{
			file = new LogFile(source); 
		}
		else if (ServerFileId == ServerFileType.ASRUN)
		{
			file = new AsRunFile(source);
		}
		return file;
	}

	/***************************************************************************
	 * Obtain the list of available procedures in context 
	 * 
	 * @return The procedure list
	 **************************************************************************/
	public Map<String, String> getAvailableProcedures()
	{
		return m_availableProcedures;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public Set<String> getOpenLocalProcedures()
	{
		return m_localModels.keySet();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public Set<String> getOpenRemoteProcedures()
	{
		return m_remoteModels.keySet();
	}

	/***************************************************************************
	 * Trigger a SPELL command
	 * 
	 * @param cmd
	 *            ExecutorCommand instance
	 * @param procId
	 *            The procedure identifier
	 **************************************************************************/
	public void issueCommand( ExecutorCommand cmd ) throws CommandFailed,NoSuchProcedure
	{
		// Check if the proxy is connected before trying to send a command
		checkConnectivity();
		// For certain commands we need preprocessing
		commandPreprocessing( cmd );
		cmd.execute(null);
	}

	/***************************************************************************
	 * Request the list of available procedures to the context. Called as soon
	 * as the context proxy Connected Event is received.
	 **************************************************************************/
	public Map<String, String> loadAvailableProcedures()
	{
		try
		{
			// Ensure we are connected
			checkConnectivity();
			Logger.debug("Loading available procedures", Level.PROC, this);
			m_availableProcedures = s_ctx.getAvailableProcedures();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			m_availableProcedures.clear();
		}
		return m_availableProcedures;
	}

	/***************************************************************************
	 * Request the procedure properties
	 **************************************************************************/
	public TreeMap<String,String> getProcedureProperties( String procedureId )
	{
		TreeMap<String,String> map = null;
		try
		{
			// Ensure we are connected
			checkConnectivity();
			map = s_ctx.getProcedureProperties(procedureId);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return map;
	}

	/***************************************************************************
	 * Check if the procedure manager can operate (connected to proxy)
	 **************************************************************************/
	public boolean canOperate()
	{
		return s_ctx.isConnected();
	}
	
	/**************************************************************************
	 * Get procedure name given its Id
	 * @param procId
	 * @return
	 *************************************************************************/
	public String getProcedureName(String procId)
	{
		if (!m_availableProcedures.containsKey(procId))
		{
			return null;
		}
		return m_availableProcedures.get(procId);
	}
	
	/* #########################################################################
	 * # EXTENSION IMPLEMENTATION: IProcedureRuntime
	 * #
	 * # This set of methods is used to receive procedure data from the SPELL
	 * # client core. All data is redirected to the corresponding procedure
	 * # model.
	 * #######################################################################*/

	@Override
	public void procedureCode(CodeNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			// Send the code notification to the model
			m_localModels.get(instanceId).procedureCode(data);
			// Then redirect the data to the consumers
			ProcExtensions.get().fireProcedureCode(data);
		}
	}

	@Override
	public void procedureDisplay(DisplayData data)
	{
		// Check that the procedure is loaded
		if (m_localModels.containsKey(data.getProcId()))
		{
			// Just redirect the data to the consumers
			ProcExtensions.get().fireProcedureDisplay(data);
		}
	}

	@Override
	public void procedureError(ErrorData data)
	{
		String instanceId = data.getOrigin();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			// Set the model to error status
			m_localModels.get(instanceId).setStatus(ExecutorStatus.ERROR);
			// Redirect the data to the consumers
			ProcExtensions.get().fireProcedureError(data);
		}
	}

	@Override
	public void procedureItem(ItemNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			// Send the item notification to the model
			m_localModels.get(instanceId).procedureItem(data);
			// Redirect the data to the consumers
			ProcExtensions.get().fireProcedureItem(data);
		}
	}

	@Override
	public void procedureLine(LineNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			// Send the item notification to the model
			m_localModels.get(instanceId).procedureLine(data);
			// Redirect the data to the consumers
			ProcExtensions.get().fireProcedureLine(data);
		}
	}

	@Override
	public void procedureStatus(StatusNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			// Send the item notification to the model
			m_localModels.get(instanceId).procedureStatus(data);
			if (data.getStatus() == ExecutorStatus.LOADED)
			{
				//Redirect the data to the consumers
				ProcExtensions.get().fireModelReset(data.getProcId());
			}
			else
			{
				//Redirect the data to the consumers
				ProcExtensions.get().fireProcedureStatus(data);
			}
		}
	}

	@Override
	public String getListenerId()
	{
		return ID;
	}

	/* #########################################################################
	 * # EXTENSION IMPLEMENTATION: IProcedureOperation
	 * #
	 * # This set of methods is used to keep track of the procedures active in
	 * # the execution environment (SPELL server side), which are controlled
	 * # by other clients. This is required for:
	 * #
	 * # A) Informing the user about the status of remote procedures
	 * # B) Procedure instance number management
	 * # C) User hand-over and other remote procedure interaction
	 * #
	 * #######################################################################*/

	@Override
	public void procedureClosed(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			removeRemoteProcedureModel(procId);
		}
		else
		{
			// The procedure is locally loaded. We shall be in monitoring mode, 
			// since if we were controlling this notification would not come.
			removeLocalProcedureModel(procId);
			ProcExtensions.get().fireModelUnloaded(procId, false);
		}
	}

	@Override
	public void procedureControlled(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			updateRemoteProcedureModel(procId);
		}
	}

	@Override
	public void procedureKilled(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			removeRemoteProcedureModel(procId);
		}
		else
		{
			// The procedure is locally loaded. We shall be in monitoring mode, 
			// since if we were controlling this notification would not come.
			removeLocalProcedureModel(procId);
			ProcExtensions.get().fireModelUnloaded(procId, false);
		}
	}

	@Override
	public void procedureMonitored(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			updateRemoteProcedureModel(procId);
		}
	}

	@Override
	public void procedureOpen(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			loadRemoteProcedureModel(procId);
			// If the gui key corresponds to this GUI there are two possibilities:
			//    1) The procedure is loaded locally, just ignore the notification
			//    2) The procedure is not loaded. Then it is a child procedure
			//       being started by a parent proc controlled by this gui.
			if (s_ctx.getClientKey().equals(guiKey))
			{
				ExecutorInfo info = s_ctx.getExecutorInfo(procId);
				if (info.getVisible())
				{
					new GrabProcedureTask(this,procId).start();
				}
			}
		}
	}

	@Override
	public void procedureReleased(String procId, String guiKey)
	{
		updateRemoteProcedureModel(procId);
	}

	@Override
	public void procedureStatus(String procId, ExecutorStatus status, String guiKey)
	{
		updateRemoteProcedureModel(procId);
	}

	/* #########################################################################
	 * # EXTENSION IMPLEMENTATION: IProcedureInput
	 * #
	 * # This set of methods is used to receive procedure input requests from 
	 * # the SPELL client core. All requests are redirected to the corresponding 
	 * # procedure model.
	 * #######################################################################*/
	
	@Override
	public void procedureCancelPrompt(Input inputData)
	{
		String instanceId = inputData.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			m_localModels.get(instanceId).setWaitingInput(false);
			// Just redirect the data to the consumers
			ProcExtensions.get().fireCancelPrompt(inputData);
		}
	}

	@Override
	public void procedurePrompt(Input inputData)
	{
		String instanceId = inputData.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			m_localModels.get(instanceId).setWaitingInput(true);
			// Just redirect the data to the consumers
			ProcExtensions.get().firePrompt(inputData);
		}
	}

	/* #########################################################################
	 * # EXTENSION IMPLEMENTATION: IContextOperation
	 * #
	 * # This set of methods is used to receive events regarding the status
	 * # of the Context proxy connection.
	 * #######################################################################*/

	@Override
	public void contextAttached(ContextInfo ctx)
	{
		// Once the context is connected, load the list of available procedures
		loadAvailableProcedures();
		// Build the list of remote procedure models
		loadRemoteProcedures();
		// We shall enable any open model since we can control the SEE
		// counterpart again
		enableProcedures();
	}
	
	@Override
	public void contextDetached()
	{
		noContextConnection();
	}
	

	@Override
	public void contextError(ErrorData error)
	{
		noContextConnection();
	}

	/* #########################################################################
	 * # INTERNAL METHODS
	 * #######################################################################*/
	
	/***************************************************************************
	 * Remove all models
	 **************************************************************************/
	private void noContextConnection()
	{
		// If the context is detached we cannot open any procedure, so clear
		// the list of available procedures
		clearAvailableProcedures();
		// Clear the list of remote procedure models
		clearRemoteProcedures();
		// We shall disable any open model since we cannot control the SEE
		// counterpart
		disableProcedures();
	}
	
	/***************************************************************************
	 * Clear the list of available procedures. Called as soon as the context
	 * proxy Disconnected Event is received.
	 **************************************************************************/
	private void clearAvailableProcedures()
	{
		m_availableProcedures.clear();
	}

	/***************************************************************************
	 * Request the list of available procedures to the context. Called as soon
	 * as the context proxy Connected Event is received.
	 **************************************************************************/
	private void loadRemoteProcedures()
	{
		try
		{
			m_remoteModels.clear();
			Logger.debug("Loading active executor models", Level.PROC, this);
			Vector<String> executorIds = s_ctx.getAvailableExecutors();
			for(String instanceId : executorIds)
			{
				loadRemoteProcedureModel(instanceId);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			clearRemoteProcedures();
		}
	}
	
	/***************************************************************************
	 * Clear the list of available procedures. Called as soon as the context
	 * proxy Disconnected Event is received.
	 **************************************************************************/
	private void clearRemoteProcedures()
	{
		m_remoteModels.clear();
	}

	/***************************************************************************
	 * When the context connection is lost, all models shall be disabled. This
	 * should result on disabling procedure model views as well, but this is
	 * up to the view provider plugin.
	 **************************************************************************/
	private void disableProcedures()
	{
		// Notify the consumers of the local procedures that they cannot be
		// used any more
		for(Procedure proc: m_localModels.values())
		{
			String procId = proc.getProcId();
			StatusNotification sdata = new StatusNotification(procId, ExecutorStatus.ERROR);
			ProcExtensions.get().fireProcedureStatus(sdata);
			ErrorData data = new ErrorData(procId, "Lost connection with context", "");
			ProcExtensions.get().fireProcedureError(data);
			ProcExtensions.get().fireModelDisabled(procId);
		}
	}

	/***************************************************************************
	 * When the context connection is recovered, all models can be reenabled. This
	 * should result on enabling procedure model views as well, but this is
	 * up to the view provider plugin.
	 **************************************************************************/
	private void enableProcedures()
	{
		// Notify the consumers of the local procedures that they cannot be
		// used any more
		for(Procedure proc: m_localModels.values())
		{
			// We shall refresh the model first
			proc.refresh();
			// Then notify consumers
			ProcExtensions.get().fireModelEnabled(proc.getProcId());
		}
	}

	/***************************************************************************
	 * Check if we have connection to context
	 * @throws NotConnected if context proxy is not connected
	 **************************************************************************/
	private void checkConnectivity() throws NotConnected
	{
		if (!s_ctx.isConnected())
		{
			throw new NotConnected("Cannot operate: not conected to context");
		}
	}
	
	/***************************************************************************
	 * Build a proper procedure identifier with free instance number
	 * @param procId
	 * @return The identifier with instance number
	 **************************************************************************/
	private String getAvailableId( String procId )
	{
		String instanceId = null;
		try
		{
			Logger.debug("Obtaining available instance for " + procId, Level.PROC, this);
			instanceId = s_ctx.getProcedureInstanceId( procId );
			Logger.debug("Available instance is " + instanceId, Level.PROC, this);
		}
		catch(ContextError ex)
		{
			ex.printStackTrace();
		}
		return instanceId;
	}
	
	/***************************************************************************
	 * Create a procedure model and add it to the local list
	 * @param instanceId Procedure instance identifier
	 * @return true if procedure was successfully loaded
	 **************************************************************************/
	private boolean loadLocalProcedureModel( String instanceId )
	{
		Procedure proc = new Procedure(instanceId);
		String[] elements = instanceId.split("#");
		TreeMap<String,String> props = s_ctx.getProcedureProperties(elements[0]);
		if (props != null)
		{
			proc.setProperties(props);
		}
		m_localModels.put(instanceId, proc);
		boolean loaded = proc.getRootCode() != null;
		return loaded;
	}

	/***************************************************************************
	 * Remove a local procedure model 
	 * @param instanceId Procedure instance identifier
	 **************************************************************************/
	private void removeLocalProcedureModel( String instanceId )
	{
		m_localModels.remove(instanceId);
	}

	/***************************************************************************
	 * Convert a local model to remote model 
	 * @param instanceId Procedure instance identifier
	 **************************************************************************/
	private void convertToRemoteModel( String instanceId )
	{
		Procedure proc = m_localModels.remove(instanceId);
		m_remoteModels.put(instanceId, proc.getInfo());
	}

	/***************************************************************************
	 * Create a remote procedure model and add it to the list
	 * @param instanceId Procedure instance identifier
	 **************************************************************************/
	private synchronized void loadRemoteProcedureModel( String instanceId )
	{
		try
		{
			if (!m_remoteModels.containsKey(instanceId))
			{
				Logger.debug("Registered remote model: " + instanceId, Level.PROC, this);
				ExecutorInfo info = new ExecutorInfo(instanceId);
				m_remoteModels.put(instanceId, info);
				s_ctx.updateExecutorInfo(instanceId, info);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Remove a remote procedure model 
	 * @param instanceId Procedure instance identifier
	 **************************************************************************/
	private synchronized void removeRemoteProcedureModel( String instanceId )
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			m_remoteModels.remove(instanceId);
		}
	}

	/***************************************************************************
	 * Update a remote procedure model 
	 * @param instanceId Procedure instance identifier
	 **************************************************************************/
	private synchronized void updateRemoteProcedureModel( String instanceId )
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			s_ctx.updateExecutorInfo(instanceId, m_remoteModels.get(instanceId));
		}
	}

	/***************************************************************************
	 * Update a remote procedure model 
	 * @param instanceId Procedure instance identifier
	 **************************************************************************/
	private void updateLocalProcedureModel( String instanceId, ExecutorInfo info, boolean replay )
	{
		if (m_localModels.containsKey(instanceId))
		{
			Procedure proc = m_localModels.get(instanceId);
			// Reset the executor info for replaying execution
			AsRunFile asRun = null;
			if (replay)
			{
				asRun = (AsRunFile) getServerFile( instanceId, ServerFileType.ASRUN );
			}
			
			proc.setInfo(info, asRun, replay);
			// Refresh the proc status
			proc.refresh();
		}
	}

	/***************************************************************************
	 * Load a new procedure instance for the given procedure. The sequence of
	 * operations is:
	 * 
	 * 1) Obtain a proper procedure instance id
	 * 2) Create a model for the procedure and store it
	 * 3) Request the context to load the procedure
	 * 
	 * @param procedureId The procedure identifier, no instance number info
	 * @param create If true, the procedure is created. If false, it is attached.
	 * @throws LoadFailed if the procedure could not be loaded
	 **************************************************************************/
	private void loadLocalProcedure( String procedureId, boolean create, ClientMode mode, Map<String,String> arguments ) throws LoadFailed
	{
		// Check proxy connection
		checkConnectivity();
		
		// Check the identifier
		if (create && !m_availableProcedures.containsKey(procedureId))
		{
			throw new LoadFailed("No such procedure: '" + procedureId + "'");
		}
		if (!create && !m_remoteModels.containsKey(procedureId))
		{
			throw new LoadFailed("Could not find remote procedure: '" + procedureId + "'");
		}

		String instanceId = procedureId;
		if (create)
		{
			// Obtain a valid instance identifier
			instanceId = getAvailableId(procedureId);
		}

		// Create the local model for the procedure 
		boolean loaded = loadLocalProcedureModel( instanceId );
		if (!loaded)
		{
			removeLocalProcedureModel(instanceId);
			throw new LoadFailed("Could not load the procedure '" + procedureId);
		}

		ExecutorInfo info = null;
		try
		{
			if (create)
			{
				// Request context to load the procedure
				info = s_ctx.openExecutor(instanceId, null, arguments);
			}
			else
			{
				// Request context to attach the procedure
				info = s_ctx.attachToExecutor(instanceId, mode );
			}
		}
		catch(ContextError err)
		{
			// Remove the model
			removeLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" +
                                  err.getLocalizedMessage());
		}
		
		try
		{
			// Update the mode of this client
			if (create)
			{
				Logger.debug("Set client mode for " + instanceId + ": " + ClientMode.CONTROLLING, Level.PROC, this);
				info.setMode(ClientMode.CONTROLLING);
			}
			else
			{
				Logger.debug("Set client mode for " + instanceId + ": " + mode, Level.PROC, this);
				info.setMode(mode);
			}
			updateLocalProcedureModel(instanceId, info, !create);

			if (!create)
			{
				// Now remove the remote model
				removeRemoteProcedureModel(instanceId);
			}
			
			// Reached to this point we have the model of the procedure created.
			// Now notify any plugin extensions implementing the ProcedureView
			// support.
			ProcExtensions.get().fireModelLoaded(instanceId);
			// Notify about procedure configuration (the first time it is 
			// initialized with the executor data
			ProcExtensions.get().fireModelConfigured(instanceId);

			if (!create)
			{
				// If we are not creating the procedure, there wont be initial status
				// notifications so provoke it
				StatusNotification st = new StatusNotification( instanceId, info.getStatus() );
				ProcExtensions.get().fireProcedureStatus(st);
			}
		}
		catch(LoadFailed err)
		{
			// Remove the model
			removeLocalProcedureModel(instanceId);
			s_ctx.detachFromExecutor(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId + "'.\n\n" + 
					              err.getLocalizedMessage());
		}
	}
	
	/***************************************************************************
	 * Load a new procedure instance for the given procedure. The sequence of
	 * operations is:
	 * 
	 * 1) Obtain a proper procedure instance id
	 * 2) Create a model for the procedure and store it
	 * 3) Request the context to load the procedure
	 * 
	 * @param procedureId The procedure identifier, no instance number info
	 * @param create If true, the procedure is created. If false, it is attached.
	 * @throws LoadFailed if the procedure could not be loaded
	 **************************************************************************/
	private void doScheduleProcedure( String procedureId, String condition ) throws LoadFailed
	{
		// Check proxy connection
		checkConnectivity();
		
		// Check the identifier
		if (!m_availableProcedures.containsKey(procedureId))
		{
			throw new LoadFailed("No such procedure: '" + procedureId + "'");
		}

		String instanceId = getAvailableId(procedureId);

		// Create the local model for the procedure 
		boolean loaded = loadLocalProcedureModel( instanceId );
		if (!loaded)
		{
			removeLocalProcedureModel(instanceId);
			throw new LoadFailed("Could not load the procedure '" + procedureId);
		}

		try
		{
			ExecutorInfo info = null;
			// Request context to load the procedure
			Logger.debug("Scheduling executor " + procedureId + " with condition " + condition, Level.COMM, this);
			info = s_ctx.openExecutor(instanceId, condition, null);
			// Update the mode of this client
			Logger.debug("Set client mode for " + instanceId + ": " + ClientMode.CONTROLLING, Level.PROC, this);
			info.setMode(ClientMode.CONTROLLING);
			updateLocalProcedureModel(instanceId, info, false);
			// Reached to this point we have the model of the procedure created.
			// Now notify any plugin extensions implementing the ProcedureView
			// support.
			ProcExtensions.get().fireModelLoaded(instanceId);
		}
		catch(ContextError err)
		{
			// Remove the model
			removeLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + procedureId + "'.\n\n" + err.getLocalizedMessage());
		}
		
	}

	/***************************************************************************
	 * Close or kill a given procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id
	 * 2) Request the context to unload the procedure
	 * 3) Unload the model
	 * 4) Notify extensions
	 * 
	 * @param instanceId The procedure identifier, WITH instance number info
	 * @param kill If true, the procedure is killed instead of closed
	 * @throws UnloadFailed if the procedure could not be unloaded
	 **************************************************************************/
	private void unloadLocalProcedure( String instanceId, boolean kill, boolean release ) throws UnloadFailed
	{
		// Check the identifier
		if (!m_localModels.containsKey(instanceId))
		{
			throw new UnloadFailed("No such procedure: '" + instanceId + "'");
		}
		if(!s_ctx.isConnected())
		{
			// In this case we remove the model directly
			removeLocalProcedureModel( instanceId );
		}
		else
		{
			try
			{
				// Check first the role of this client unless we are releasing. 
				// If we are monitoring, we cannot close or kill the procedure.
				if (!release)
				{
					ClientMode mode = m_localModels.get(instanceId).getInfo().getMode();
					if (mode != ClientMode.CONTROLLING)
					{
						throw new UnloadFailed("Cannot kill or close this procedure while in monitoring mode");
					}
				}
				// Request context to load the procedure
				boolean closed = false;
				if (kill)
				{
					closed = s_ctx.killExecutor(instanceId);
				}
				else
				{
					if (release)
					{
						closed = s_ctx.detachFromExecutor(instanceId);
					}
					else
					{
						closed = s_ctx.closeExecutor(instanceId);
					}
				}
				if (!closed)
				{
					// The context command was sent but it raised an error
					removeLocalProcedureModel( instanceId );
					ProcExtensions.get().fireModelUnloaded(instanceId, true);
					throw new UnloadFailed("Could not unload the procedure "+ instanceId);
				}
			}
			catch(ContextError err)
			{
				// The procedure could not be unloaded due to an error in the context
				// processing
				throw new UnloadFailed("Could not unload the procedure " + instanceId + ": " + err.getLocalizedMessage());
			}
	
			// If we are releasing, the procedure becomes remote
			if (release)
			{
				convertToRemoteModel( instanceId );
			}
			else
			{
				// Remove the model for the procedure
				removeLocalProcedureModel( instanceId );
			}
		}
		
		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcExtensions.get().fireModelUnloaded(instanceId, true);
	}

	/***************************************************************************
	 * Close or kill a given procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id
	 * 2) Request the context to unload the procedure
	 * 3) Unload the model
	 * 4) Notify extensions
	 * 
	 * @param instanceId The procedure identifier, WITH instance number info
	 * @param kill If true, the procedure is killed instead of closed
	 * @throws UnloadFailed if the procedure could not be unloaded
	 **************************************************************************/
	private void unloadRemoteProcedure( String instanceId, boolean kill ) throws UnloadFailed
	{
		// Check proxy connection
		checkConnectivity();
		
		// Check the identifier
		if (!m_remoteModels.containsKey(instanceId))
		{
			throw new UnloadFailed("No such procedure: '" + instanceId + "'");
		}

		try
		{
			// Request context to load the procedure
			boolean closed = false;
			if (kill)
			{
				closed = s_ctx.killExecutor(instanceId);
			}
			else
			{
				if (kill)
				{
					closed = s_ctx.killExecutor(instanceId);
				}
				else
				{
					closed = s_ctx.closeExecutor(instanceId);
				}
			}
			if (!closed)
			{
				// The context command was sent but it raised an error
				throw new UnloadFailed("Could not unload the procedure "+ instanceId);
			}
		}
		catch(ContextError err)
		{
			// The procedure could not be unloaded due to an error in the context
			// processing
			throw new UnloadFailed("Could not unload the procedure " + instanceId + ": " + err.getLocalizedMessage());
		}

		// Reached to this point we have the model of the procedure unloaded.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcExtensions.get().fireModelUnloaded(instanceId, true);
	}

	/***************************************************************************
	 *  Preprocess commands
	 **************************************************************************/
	private void commandPreprocessing( ExecutorCommand cmd ) throws NoSuchProcedure
	{
		String instanceId = cmd.getProcId();
		if (!m_localModels.containsKey(instanceId))
		{
			throw new NoSuchProcedure(instanceId);
		}
	}

}
