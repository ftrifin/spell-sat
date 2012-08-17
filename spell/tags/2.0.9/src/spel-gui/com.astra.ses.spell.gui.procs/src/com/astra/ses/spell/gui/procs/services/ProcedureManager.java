///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.services
// 
// FILE      : ProcedureManager.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.procs.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.comm.commands.ExecutorCommand;
import com.astra.ses.spell.gui.core.exceptions.CommandFailed;
import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.extensionpoints.IProcedureRuntimeExtension;
import com.astra.ses.spell.gui.core.interfaces.IContextOperation;
import com.astra.ses.spell.gui.core.interfaces.IProcedureInput;
import com.astra.ses.spell.gui.core.interfaces.IProcedureOperation;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.files.AsRunFileLine;
import com.astra.ses.spell.gui.core.model.files.IServerFile;
import com.astra.ses.spell.gui.core.model.files.IServerFileLine;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.ScopeNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.notification.VariableNotification;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.services.BaseService;
import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcExtensions;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.exceptions.NotConnected;
import com.astra.ses.spell.gui.procs.exceptions.UnloadFailed;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.model.Procedure;
import com.astra.ses.spell.gui.procs.utils.GrabProcedureTask;

/*******************************************************************************
 * @brief Manages procedure models.
 * @date 09/10/07
 ******************************************************************************/
public class ProcedureManager extends BaseService implements IContextOperation,
        IProcedureInput, IProcedureRuntimeExtension, IProcedureOperation
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the server proxy handle */
	private static ContextProxy	      s_ctx	          = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	/** The service identifier */
	public static final String	      ID	          = "com.astra.ses.spell.gui.procs.ProcedureManager";

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	// PRIVATE -----------------------------------------------------------------
	/** Holds the list of local procedures */
	private Map<String, IProcedure>	  m_localModels;
	/** Holds the list of remote procedures */
	private Map<String, ExecutorInfo>	m_remoteModels;
	/** Holds the list of valid procedure identifiers */
	private Map<String, String>	      m_availableProcedures;
	/** Holds the execution until procedure is loaded */
	private boolean	                  m_loadLocked	  = false;
	private Lock	                  m_loadLock	  = new ReentrantLock();
	private Condition	              m_loadCondition	= m_loadLock
	                                                          .newCondition();

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	public ProcedureManager()
	{
		super(ID);
		Logger.debug("Created", Level.INIT, this);
		m_localModels = new TreeMap<String, IProcedure>();
		m_remoteModels = new TreeMap<String, ExecutorInfo>();
		m_availableProcedures = new HashMap<String, String>();
		ServiceManager.registerService(this);
	}

	/*
	 * #########################################################################
	 * # BASIC SERVICE METHODS
	 * #######################################################################
	 */

	/***************************************************************************
	 * Setup the service
	 **************************************************************************/
	@Override
	public void setup()
	{
	}

	/***************************************************************************
	 * Cleanup the service
	 **************************************************************************/
	@Override
	public void cleanup()
	{
	}

	/***************************************************************************
	 * Subscribe to required resources
	 **************************************************************************/
	@Override
	public void subscribe()
	{
		s_ctx = (ContextProxy) ServiceManager.get(ContextProxy.ID);
	}

	/***************************************************************************
	 * Listener identifier (IBaseInterface)
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return ID;
	}

	/*
	 * #########################################################################
	 * # PROCEDURE MANAGEMENT METHODS
	 * #######################################################################
	 */

	/***************************************************************************
	 * Open a new procedure instance for the given procedure.
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	public void openProcedure(String procedureId,
	        Map<String, String> arguments, IProgressMonitor monitor)
	        throws LoadFailed
	{
		Logger.debug("Opening procedure " + procedureId, Level.PROC, this);
		// Will hold the model
		IProcedure model = null;
		// Will hold the instance identifier
		String instanceId = null;

		// Start the task in the monitor
		monitor.beginTask("Opening procedure", 6);

		// Check cancellation
		if (monitor.isCanceled()) return;

		try
		{
			// Create the model, Notify about the progress
			monitor.subTask("Creating model");
			model = createLocalProcedureModel(procedureId);
			instanceId = model.getProcId();
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			monitor.subTask("ERROR: cannot open the procedure: " + ex);
			throw ex;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			removeLocalProcedureModel(instanceId);
			return;
		}

		// Now ask the context to start the procedure process
		// Ask the context to launch or attach to the proc. It will return the
		// executor information,
		// which is the procedure data known at core level.
		try
		{
			// Report progress
			monitor.subTask("Launching process");

			// Request context to load the procedure
			s_ctx.openExecutor(instanceId, null, arguments);

			// Report progress
			monitor.worked(1);

			// Report progress
			monitor.subTask("Waiting for procedure to be ready");

			// Wait until the procedure is actually loaded on server side and
			// ready, then
			// update the configuration. This is given by the LOADED
			// notification coming
			// from the procedure.
			waitProcedureLoaded(instanceId);

			// Report progress
			monitor.worked(1);
		}
		catch (LoadFailed err)
		{
			// Remove the model
			removeLocalProcedureModel(instanceId);
			// Kill the process
			s_ctx.killExecutor(instanceId);
			// Rethrow
			throw err;
		}
		catch (ContextError err)
		{
			// Remove the model
			removeLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId
			        + "'.\n\n" + err.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			s_ctx.killExecutor(instanceId);
			removeLocalProcedureModel(instanceId);
			return;
		}

		try
		{
			// Once the model is loaded and the process is up and running,
			// update the model with the information
			monitor.subTask("Updating procedure status");
			updateLocalProcedureModel(instanceId, ClientMode.CONTROLLING,
			        false, monitor);
			// Report progress
			monitor.worked(1);
		}
		catch (Exception err)
		{
			removeLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId
			        + "'.\n\n" + err.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			s_ctx.killExecutor(instanceId);
			removeLocalProcedureModel(instanceId);
			return;
		}

		// Report progress
		monitor.subTask("Ending load process");
		notifyExtensionsProcedureReady(model);
		// Report progress
		monitor.done();
	}

	/***************************************************************************
	 * Attach an existing procedure instance in control mode.
	 * 
	 * @param instanceId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be attached
	 **************************************************************************/
	public void controlProcedure(String instanceId, IProgressMonitor monitor)
	        throws LoadFailed
	{
		Logger.debug("Attaching procedure " + instanceId + " in control mode",
		        Level.PROC, this);
		attachToRemoteProcedure(instanceId, ClientMode.CONTROLLING, monitor);
	}

	/***************************************************************************
	 * Attach an existing procedure instance in monitoring mode.
	 * 
	 * @param instanceId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be attached
	 **************************************************************************/
	public void monitorProcedure(String instanceId, IProgressMonitor monitor)
	        throws LoadFailed
	{
		Logger.debug("Attaching procedure " + instanceId + " in monitor mode",
		        Level.PROC, this);
		attachToRemoteProcedure(instanceId, ClientMode.MONITORING, monitor);
	}

	/***************************************************************************
	 * Schedule a new procedure instance for the given procedure.
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	public void scheduleProcedure(String procedureId, String condition,
	        IProgressMonitor monitor) throws LoadFailed
	{
		Logger.debug("Scheduling procedure " + procedureId, Level.PROC, this);
		// Will hold the model
		IProcedure model = null;
		// Will hold the instance identifier
		String instanceId = null;

		// Start the task in the monitor
		monitor.beginTask("Opening procedure", 6);

		// Check cancellation
		if (monitor.isCanceled()) return;

		try
		{
			// Create the model, Notify about the progress
			monitor.subTask("Creating model");
			model = createLocalProcedureModel(procedureId);
			instanceId = model.getProcId();
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			monitor.subTask("ERROR: cannot open the procedure: " + ex);
			throw ex;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			removeLocalProcedureModel(instanceId);
			return;
		}

		// Now ask the context to start the procedure process
		// Ask the context to launch or attach to the proc. It will return the
		// executor information,
		// which is the procedure data known at core level.
		try
		{
			// Report progress
			monitor.subTask("Launching process");

			// Request context to load the procedure
			s_ctx.openExecutor(instanceId, condition, null);

			// Report progress
			monitor.worked(1);

			// Report progress
			monitor.subTask("Waiting for procedure to be ready");

			// Wait until the procedure is actually loaded on server side and
			// ready, then
			// update the configuration. This is given by the LOADED
			// notification coming
			// from the procedure.
			waitProcedureLoaded(instanceId);

			// Report progress
			monitor.worked(1);
		}
		catch (LoadFailed err)
		{
			// Remove the model
			removeLocalProcedureModel(instanceId);
			// Kill the process
			s_ctx.killExecutor(instanceId);
			// Rethrow
			throw err;
		}
		catch (ContextError err)
		{
			// Remove the model
			removeLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId
			        + "'.\n\n" + err.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			s_ctx.killExecutor(instanceId);
			removeLocalProcedureModel(instanceId);
			return;
		}

		try
		{
			// Once the model is loaded and the process is up and running,
			// update the model with the information
			monitor.subTask("Updating procedure status");
			updateLocalProcedureModel(instanceId, ClientMode.CONTROLLING,
			        false, monitor);
			// Report progress
			monitor.worked(1);
		}
		catch (Exception err)
		{
			removeLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId
			        + "'.\n\n" + err.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			s_ctx.killExecutor(instanceId);
			removeLocalProcedureModel(instanceId);
			return;
		}

		// Report progress
		monitor.subTask("Ending load process");
		notifyExtensionsProcedureReady(model);
		// Report progress
		monitor.done();
	}

	/***************************************************************************
	 * Close a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	public void closeProcedure(String instanceId, IProgressMonitor monitor)
	        throws UnloadFailed
	{
		Logger.debug("Closing procedure " + instanceId, Level.PROC, this);
		if (isLocallyLoaded(instanceId))
		{
			closeLocalProcedure(instanceId, monitor);
		}
		else
		{
			unloadRemoteProcedure(instanceId, false, monitor);
		}
	}

	/***************************************************************************
	 * Release a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	public void releaseProcedure(String instanceId, IProgressMonitor monitor)
	        throws UnloadFailed
	{
		Logger.debug("Detaching procedure " + instanceId, Level.PROC, this);
		IProcedure proc = getProcedure(instanceId);
		// The procedure becomes remote
		ExecutorInfo info = (ExecutorInfo) proc.getAdapter(ExecutorInfo.class);
		m_remoteModels.put(instanceId, info);
		// Unload the local procedure
		releaseLocalProcedure(instanceId, monitor);

	}

	/***************************************************************************
	 * Kill a given procedure instance.
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	public void killProcedure(String instanceId, IProgressMonitor monitor)
	        throws UnloadFailed
	{
		Logger.debug("Killing procedure " + instanceId, Level.PROC, this);
		if (isLocallyLoaded(instanceId))
		{
			killLocalProcedure(instanceId, monitor);
		}
		else
		{
			unloadRemoteProcedure(instanceId, true, monitor);
		}
	}

	/***************************************************************************
	 * Obtain a local procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure model
	 **************************************************************************/
	public IProcedure getProcedure(String instanceId) throws NoSuchProcedure
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
	 * @param instanceId
	 *            The instance identifier
	 * @return True if the model is loaded
	 **************************************************************************/
	public boolean isLocallyLoaded(String instanceId)
	{
		return m_localModels.containsKey(instanceId);
	}

	/***************************************************************************
	 * Obtain a remote procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure reduced model
	 **************************************************************************/
	public synchronized ExecutorInfo getRemoteProcedure(String instanceId)
	        throws NoSuchProcedure
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
	 * @param instanceId
	 *            The instance identifier
	 * @return The asrun file data
	 **************************************************************************/
	public IServerFile getServerFile(String instanceId,
	        ServerFileType ServerFileId) throws NoSuchProcedure
	{
		if (!m_remoteModels.containsKey(instanceId)
		        && !m_localModels.containsKey(instanceId)) { throw new NoSuchProcedure(
		        instanceId); }
		IServerFile file = s_ctx.getServerFile(instanceId, ServerFileId);
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
	public void issueCommand(ExecutorCommand cmd, String procId)
	        throws CommandFailed, NoSuchProcedure
	{
		// Check if the proxy is connected before trying to send a command
		checkConnectivity();
		if (m_localModels.containsKey(procId))
		{
			m_localModels.get(procId).getController()
			        .issueCommand(cmd, new String[0]);
		}
		else
		{
			throw new NoSuchProcedure(procId);
		}
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
		catch (Exception ex)
		{
			ex.printStackTrace();
			m_availableProcedures.clear();
		}
		return m_availableProcedures;
	}

	/***************************************************************************
	 * Request the procedure properties
	 **************************************************************************/
	public TreeMap<ProcProperties, String> getProcedureProperties(
	        String procedureId)
	{
		TreeMap<ProcProperties, String> map = null;
		try
		{
			// Ensure we are connected
			checkConnectivity();
			map = s_ctx.getProcedureProperties(procedureId);
		}
		catch (Exception ex)
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
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	public String getProcedureName(String procId)
	{
		if (!m_availableProcedures.containsKey(procId)) { return null; }
		return m_availableProcedures.get(procId);
	}

	/*
	 * #########################################################################
	 * # EXTENSION IMPLEMENTATION: IProcedureRuntime # # This set of methods is
	 * used to receive procedure data from the SPELL # client core. All data is
	 * redirected to the corresponding procedure # model.
	 * #######################################################################
	 */

	@Override
	public void notifyProcedureDisplay(DisplayData data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			// Update the model
			IProcedure procedure = m_localModels.get(instanceId);
			procedure.getRuntimeProcessor().notifyProcedureDisplay(data);
			// Just redirect the data to the consumers
			ProcExtensions.get().fireProcedureDisplay(procedure, data);
		}
	}

	@Override
	public void notifyProcedureError(ErrorData data)
	{
		String instanceId = data.getOrigin();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			Logger.error(
			        "Procedure '" + instanceId + "' error: "
			                + data.getMessage() + ", " + data.getReason(),
			        Level.PROC, this);
			// Set the model to error status
			IProcedure procedure = m_localModels.get(instanceId);
			procedure.getController().setError(data);
			// If the manager is waiting for this procedure to be loaded, signal
			// it
			if (m_loadLocked)
			{
				signalProcedureLoaded();
			}
			// Redirect the data to the consumers
			ProcExtensions.get().fireProcedureError(procedure, data);
		}
	}

	@Override
	public void notifyProcedureItem(ItemNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			// Send the item notification to the model
			IProcedure procedure = m_localModels.get(instanceId);
			procedure.getRuntimeProcessor().notifyProcedureItem(data);
			// Redirect the data to the consumers
			ProcExtensions.get().fireProcedureItem(procedure, data);
		}
	}

	@Override
	public void notifyProcedureStack(StackNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			// Send the item notification to the model
			IProcedure procedure = m_localModels.get(instanceId);
			procedure.getRuntimeProcessor().notifyProcedureStack(data);
			// Redirect the data to the consumers
			ProcExtensions.get().fireProcedureStack(procedure, data);
		}
	}

	@Override
	public void notifyProcedureStatus(StatusNotification data)
	{
		String instanceId = data.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			IProcedure model = m_localModels.get(instanceId);
			// Send the item notification to the model
			model.getRuntimeProcessor().notifyProcedureStatus(data);
			if (data.getStatus() == ExecutorStatus.RELOADING)
			{
				model.reset();
				ProcExtensions.get().fireModelReset(model);
			}
			if (data.getStatus() == ExecutorStatus.LOADED)
			{
				signalProcedureLoaded();
			}
			else
			{
				// Redirect the data to the consumers
				ProcExtensions.get().fireProcedureStatus(model, data);
			}
		}
	}

	@Override
	public void notifyVariableScopeChange(ScopeNotification data)
	{
		String procId = data.getProcId();
		IProcedure model = m_localModels.get(procId);
		model.getVariableManager().notifyVariableScopeChange(data);
	}

	@Override
	public void notifyVariableChange(VariableNotification data)
	{
		String procId = data.getProcId();
		IProcedure model = m_localModels.get(procId);
		model.getVariableManager().notifyVariableChange(data);
	}

	@Override
	public void notifyProcedureUserAction(UserActionNotification data)
	{
		// Check that the procedure is loaded
		if (m_localModels.containsKey(data.getProcId()))
		{
			// Just redirect the data to the consumers
			ProcExtensions.get().fireProcedureUserAction(
			        m_localModels.get(data.getProcId()), data);
		}
	}

	/*
	 * #########################################################################
	 * # EXTENSION IMPLEMENTATION: IProcedureOperation # # This set of methods
	 * is used to keep track of the procedures active in # the execution
	 * environment (SPELL server side), which are controlled # by other clients.
	 * This is required for: # # A) Informing the user about the status of
	 * remote procedures # B) Procedure instance number management # C) User
	 * hand-over and other remote procedure interaction #
	 * #######################################################################
	 */

	@Override
	public void notifyRemoteProcedureClosed(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			removeRemoteProcedureModel(procId);
		}
		else
		{
			// The procedure is locally loaded. We shall be in monitoring mode,
			// since if we were controlling this notification would not come.
			IProcedure model = m_localModels.get(procId);
			ProcExtensions.get().fireModelUnloaded(model, false);
			removeLocalProcedureModel(procId);
		}
	}

	@Override
	public void notifyRemoteProcedureControlled(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			updateRemoteProcedureModel(procId);
		}
	}

	@Override
	public void notifyRemoteProcedureKilled(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			removeRemoteProcedureModel(procId);
		}
		else
		{
			// The procedure is locally loaded. We shall be in monitoring mode,
			// since if we were controlling this notification would not come.
			IProcedure model = m_localModels.get(procId);
			ProcExtensions.get().fireModelUnloaded(model, false);
			removeLocalProcedureModel(procId);
		}
	}

	@Override
	public void notifyRemoteProcedureCrashed(String procId, String guiKey)
	{
		removeRemoteProcedureModel(procId);
	}

	@Override
	public void notifyRemoteProcedureMonitored(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			updateRemoteProcedureModel(procId);
		}
	}

	@Override
	public void notifyRemoteProcedureOpen(String procId, String guiKey)
	{
		if (!isLocallyLoaded(procId))
		{
			loadRemoteProcedureModel(procId);
			// If the gui key corresponds to this GUI there are two
			// possibilities:
			// 1) The procedure is loaded locally, just ignore the notification
			// 2) The procedure is not loaded. Then it is a child procedure
			// being started by a parent proc controlled by this gui.
			if (s_ctx.getClientKey().equals(guiKey))
			{
				ExecutorInfo info = s_ctx.getExecutorInfo(procId);
				if (info.getVisible())
				{
					new GrabProcedureTask(this, procId).start();
				}
			}
		}
	}

	@Override
	public void notifyRemoteProcedureReleased(String procId, String guiKey)
	{
		updateRemoteProcedureModel(procId);
	}

	@Override
	public void notifyRemoteProcedureStatus(String procId,
	        ExecutorStatus status, String guiKey)
	{
		updateRemoteProcedureModel(procId);
	}

	/*
	 * #########################################################################
	 * # EXTENSION IMPLEMENTATION: IProcedureInput # # This set of methods is
	 * used to receive procedure input requests from # the SPELL client core.
	 * All requests are redirected to the corresponding # procedure model.
	 * #######################################################################
	 */

	@Override
	public void notifyProcedurePrompt(Input inputData)
	{
		String instanceId = inputData.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			IProcedure model = m_localModels.get(instanceId);
			model.getController().notifyProcedurePrompt(inputData);
			// Just redirect the data to the consumers
			ProcExtensions.get().firePrompt(model, inputData);
		}
	}

	@Override
	public void notifyProcedureFinishPrompt(Input inputData)
	{
		String instanceId = inputData.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			IProcedure model = m_localModels.get(instanceId);
			model.getController().notifyProcedureFinishPrompt(inputData);
			// Just redirect the data to the consumers
			ProcExtensions.get().fireFinishPrompt(model, inputData);
		}
	}

	@Override
	public void notifyProcedureCancelPrompt(Input inputData)
	{
		String instanceId = inputData.getProcId();
		// Check that the procedure is loaded
		if (m_localModels.containsKey(instanceId))
		{
			IProcedure model = m_localModels.get(instanceId);
			model.getController().notifyProcedureCancelPrompt(inputData);
			// Just redirect the data to the consumers
			ProcExtensions.get().fireCancelPrompt(model, inputData);
		}
	}

	/*
	 * #########################################################################
	 * # EXTENSION IMPLEMENTATION: IContextOperation # # This set of methods is
	 * used to receive events regarding the status # of the Context proxy
	 * connection.
	 * #######################################################################
	 */

	@Override
	public void notifyContextAttached(ContextInfo ctx)
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
	public void notifyContextDetached()
	{
		noContextConnection();
	}

	@Override
	public void notifyContextError(ErrorData error)
	{
		noContextConnection();
	}

	/*
	 * #########################################################################
	 * # INTERNAL METHODS
	 * #######################################################################
	 */

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
			for (String instanceId : executorIds)
			{
				loadRemoteProcedureModel(instanceId);
			}
		}
		catch (Exception ex)
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
	 * should result on disabling procedure model views as well, but this is up
	 * to the view provider plugin.
	 **************************************************************************/
	private void disableProcedures()
	{
		// Notify the consumers of the local procedures that they cannot be
		// used any more
		for (IProcedure proc : m_localModels.values())
		{
			String procId = proc.getProcId();
			ErrorData data = new ErrorData(procId,
			        "Lost connection with context", "", true);
			proc.getRuntimeProcessor().notifyProcedureError(data);
			ProcExtensions.get().fireProcedureError(proc, data);
			ProcExtensions.get().fireModelDisabled(proc);
		}
	}

	/***************************************************************************
	 * When the context connection is recovered, all models can be reenabled.
	 * This should result on enabling procedure model views as well, but this is
	 * up to the view provider plugin.
	 **************************************************************************/
	private void enableProcedures()
	{
		// Notify the consumers of the local procedures that they cannot be
		// used any more
		for (IProcedure proc : m_localModels.values())
		{
			// We shall refresh the model first
			proc.getController().refresh();
			// Then notify consumers
			ProcExtensions.get().fireModelEnabled(proc);
		}
	}

	/***************************************************************************
	 * Check if we have connection to context
	 * 
	 * @throws NotConnected
	 *             if context proxy is not connected
	 **************************************************************************/
	private void checkConnectivity() throws NotConnected
	{
		if (!s_ctx.isConnected()) { throw new NotConnected(
		        "Cannot operate: not conected to context"); }
	}

	/***************************************************************************
	 * Build a proper procedure identifier with free instance number
	 * 
	 * @param procId
	 * @return The identifier with instance number
	 **************************************************************************/
	private String getAvailableId(String procId)
	{
		String instanceId = null;
		try
		{
			Logger.debug("Obtaining available instance for " + procId,
			        Level.PROC, this);
			instanceId = s_ctx.getProcedureInstanceId(procId);
			Logger.debug("Available instance is " + instanceId, Level.PROC,
			        this);
		}
		catch (ContextError ex)
		{
			ex.printStackTrace();
		}
		return instanceId;
	}

	/***************************************************************************
	 * Create a remote procedure model and add it to the list
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private synchronized void loadRemoteProcedureModel(String instanceId)
	{
		try
		{
			if (!m_remoteModels.containsKey(instanceId))
			{
				Logger.debug("Registered remote model: " + instanceId,
				        Level.PROC, this);
				ExecutorInfo info = new ExecutorInfo(instanceId);
				m_remoteModels.put(instanceId, info);
				s_ctx.updateExecutorInfo(instanceId, info);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/***************************************************************************
	 * Remove a remote procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private synchronized void removeRemoteProcedureModel(String instanceId)
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			m_remoteModels.remove(instanceId);
		}
	}

	/***************************************************************************
	 * Update a remote procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private synchronized void updateRemoteProcedureModel(String instanceId)
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			s_ctx.updateExecutorInfo(instanceId, m_remoteModels.get(instanceId));
		}
	}

	/***************************************************************************
	 * Update a remote procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private void updateLocalProcedureModel(String instanceId, ClientMode mode,
	        boolean replay, IProgressMonitor monitor)
	{
		if (m_localModels.containsKey(instanceId))
		{
			IProcedure proc = m_localModels.get(instanceId);
			// Reset the executor info for replaying execution
			AsRunFile asRun = null;
			boolean replaySuccess = true;

			if (replay)
			{
				monitor.subTask("Retrieving AS-RUN file...");
				asRun = (AsRunFile) getServerFile(instanceId,
				        ServerFileType.ASRUN);
				monitor.worked(1);

				// Check cancellation
				if (monitor.isCanceled()) return;

				monitor.subTask("Restoring AsRun information");

				replaySuccess = replayExecution(proc, asRun);
			}

			if (!replaySuccess)
			{
				monitor.subTask("Failed to replay AsRun data");
				monitor.setCanceled(true);
				return;
			}

			// Check cancellation
			if (monitor.isCanceled()) return;

			monitor.subTask("Retrieving executor information");
			monitor.worked(1);

			// Check cancellation
			if (monitor.isCanceled()) return;

			// Refresh the proc status
			proc.getController().refresh();

			monitor.worked(1);
		}
	}

	/***************************************************************************
	 * Replay the AsRun data on a procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private boolean replayExecution(IProcedure model, AsRunFile data)
	{
		Logger.info("Start execution replay on " + model.getProcId(),
		        Level.PROC, this);
		int count = 1;

		model.setReplayMode(true);

		boolean retrievedStatus = false;
		boolean retrievedLine = false;
		boolean result = true;

		ArrayList<IServerFileLine> lines = data.getLines();
		Logger.debug("AsRun lines: " + lines.size(), Level.PROC, this);

		for (IServerFileLine tabbedLine : lines)
		{
			AsRunFileLine arLine = (AsRunFileLine) tabbedLine;
			try
			{
				switch (arLine.getAsRunType())
				{
				case LINE: // Fall through
				case STAGE:
					retrievedLine = true;
				case CALL: // Fall through
				case RETURN:
					StackNotification cdata = (StackNotification) arLine
					        .getNotificationData();
					notifyProcedureStack(cdata);
					break;
				case ITEM:
					ItemNotification item = (ItemNotification) arLine
					        .getNotificationData();
					notifyProcedureItem(item);
					break;
				case DISPLAY: // Fall through
				case PROMPT: // Fall through
				case ANSWER:
					DisplayData ddata = (DisplayData) arLine
					        .getNotificationData();
					notifyProcedureDisplay(ddata);
					break;
				case STATUS:
					StatusNotification status = (StatusNotification) arLine
					        .getNotificationData();
					notifyProcedureStatus(status);
					retrievedStatus = true;
					break;
				case ERROR:
				case INIT:
					break;
				default:
					Logger.error("Unknown AsRun data in line " + count,
					        Level.PROC, this);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				Logger.error("Failed to process ASRUN line " + count + ":"
				        + tabbedLine.asString(), Level.PROC, this);
				Logger.error("   " + ex.getLocalizedMessage(), Level.PROC, this);
				result = false;
			}
			count++;
		}
		if (retrievedLine == false || retrievedStatus == false)
		{
			Logger.error("Unable to process status or current line",
			        Level.PROC, this);
			result = false;
		}

		model.setReplayMode(false);

		Logger.info("Finished execution replay on " + model.getProcId()
		        + ", processed " + count + " lines", Level.PROC, this);
		return result;
	}

	/***************************************************************************
	 * Create a new procedure model for the given procedure identifier. The
	 * sequence of operations is:
	 * 
	 * 1) Obtain a proper procedure instance id 2) Create a model for the
	 * procedure and store it
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @returns The procedure model
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	private IProcedure createLocalProcedureModel(String procedureId)
	        throws LoadFailed
	{
		// Check proxy connection
		checkConnectivity();

		// Procedure ID shall exist
		if (!m_availableProcedures.containsKey(procedureId)) { throw new LoadFailed(
		        "No such procedure: '" + procedureId + "'"); }

		// Check procedure number limit
		int procCount = m_localModels.size() + m_remoteModels.size();
		ContextInfo info = s_ctx.getInfo();
		if (procCount == info.getMaxProc()) { throw new LoadFailed(
		        "Cannot create procedure\n\n"
		                + "Maximum number of procedures for this driver ("
		                + procCount + ") reached."); }

		// Obtain a valid instance identifier from the context
		String instanceId = getAvailableId(procedureId);
		String[] elements = instanceId.split("#");
		TreeMap<ProcProperties, String> props = s_ctx
		        .getProcedureProperties(elements[0]);

		IProcedure proc = new Procedure(instanceId, props,
		        ClientMode.CONTROLLING);

		m_localModels.put(instanceId, proc);

		return proc;
	}

	/***************************************************************************
	 * Create a new procedure model for the given procedure identifier. The
	 * sequence of operations is:
	 * 
	 * 1) Obtain a proper procedure instance id 2) Create a model for the
	 * procedure and store it
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 * @returns The procedure model
	 * @throws LoadFailed
	 *             if the procedure could not be loaded
	 **************************************************************************/
	private Procedure createProcedureModelFromRemote(String instanceId,
	        ClientMode mode) throws LoadFailed
	{
		// Check proxy connection
		checkConnectivity();

		if (!m_remoteModels.containsKey(instanceId)) { throw new LoadFailed(
		        "Could not find remote procedure: '" + instanceId + "'"); }

		String[] elements = instanceId.split("#");

		TreeMap<ProcProperties, String> props = s_ctx
		        .getProcedureProperties(elements[0]);

		Procedure proc = new Procedure(instanceId, props, mode);

		m_localModels.put(instanceId, proc);

		return proc;
	}

	/***************************************************************************
	 * Remove a local procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private void removeLocalProcedureModel(String instanceId)
	{
		if (m_localModels.containsKey(instanceId))
		{
			m_localModels.remove(instanceId);
		}
	}

	/***************************************************************************
	 * Attach to a remote procedure, so that it becomes local
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private void attachToRemoteProcedure(String instanceId, ClientMode mode,
	        IProgressMonitor monitor) throws LoadFailed
	{
		// Will hold the model
		IProcedure model = null;

		// Start the task in the monitor
		monitor.beginTask("Attaching to procedure", 8);

		// Check cancellation
		if (monitor.isCanceled()) return;

		try
		{
			// Create the model, Notify about the progress
			monitor.subTask("Creating model");
			model = createProcedureModelFromRemote(instanceId, mode);
			instanceId = model.getProcId();
			monitor.worked(1);
		}
		catch (LoadFailed ex)
		{
			monitor.subTask("ERROR: cannot open the procedure: " + ex);
			throw ex;
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			removeLocalProcedureModel(instanceId);
			return;
		}

		// Now ask the context to attach the procedure process
		// Ask the context to launch or attach to the proc. It will return the
		// executor information,
		// which is the procedure data known at core level.
		try
		{
			// Report progress
			monitor.subTask("Attaching to process");

			// Request context to load the procedure
			s_ctx.attachToExecutor(instanceId, mode);

			// Report progress
			monitor.worked(1);

			// TODO check failure here!!
		}
		catch (ContextError err)
		{
			// Remove the model
			removeLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId
			        + "'.\n\n" + err.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			s_ctx.detachFromExecutor(instanceId);
			removeLocalProcedureModel(instanceId);
			return;
		}

		try
		{
			// Once the model is loaded and the process is up and running,
			// update the model with the information
			monitor.subTask("Updating procedure status");
			updateLocalProcedureModel(instanceId, mode, true, monitor);
			// Report progress
			monitor.worked(1);
		}
		catch (Exception err)
		{
			s_ctx.detachFromExecutor(instanceId);
			removeLocalProcedureModel(instanceId);
			// The procedure could not be loaded due to an error in the context
			// processing
			throw new LoadFailed("Could not load the procedure '" + instanceId
			        + "'.\n\n" + err.getLocalizedMessage());
		}

		// Check cancellation
		if (monitor.isCanceled())
		{
			s_ctx.detachFromExecutor(instanceId);
			removeLocalProcedureModel(instanceId);
			return;
		}

		// Remove the remote model
		removeRemoteProcedureModel(instanceId);

		// Report progress
		monitor.subTask("Ending load process");
		notifyExtensionsProcedureReady(model);
		// Report progress
		monitor.done();
	}

	/***************************************************************************
	 * Remove a local procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	private void notifyExtensionsProcedureReady(IProcedure model)
	{
		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcExtensions.get().fireModelLoaded(model);

		// Unless the model is initially in error state, notify about the
		// model configuration and reset it
		ExecutorStatus st = model.getRuntimeInformation().getStatus();
		if (!st.equals(ExecutorStatus.ERROR))
		{
			// Notify about procedure configuration (the first time it is
			// initialized with the executor data
			ProcExtensions.get().fireModelConfigured(model);
		}
	}

	/***************************************************************************
	 * Close a given procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id 2) Request the
	 * context to unload the procedure 3) Unload the model 4) Notify extensions
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	private void closeLocalProcedure(String instanceId, IProgressMonitor monitor)
	        throws UnloadFailed
	{
		monitor.beginTask("Close procedure", 3);
		IProcedure model = getProcedure(instanceId);

		monitor.subTask("Removing local model");
		monitor.worked(1);
		// Remove the model directly
		removeLocalProcedureModel(instanceId);

		if (!s_ctx.isConnected())
		{
			monitor.worked(3);
			monitor.done();
		}
		else
		{
			try
			{
				// Check condition: the client mode shall be controlling
				monitor.subTask("Checking client mode");
				ClientMode mode = model.getRuntimeInformation().getClientMode();
				if (mode != ClientMode.CONTROLLING)
				{
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed(
					        "Cannot close this procedure while in monitoring mode");
				}
				// Report progress
				monitor.worked(1);

				monitor.subTask("Closing process");
				// Request context to close the procedure
				if (!s_ctx.closeExecutor(instanceId))
				{
					// The context command was sent but it raised an error
					ProcExtensions.get().fireModelUnloaded(model, true);
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed("Failed to close the procedure "
					        + instanceId + " on the server");
				}

				// Report progress
				monitor.worked(1);
			}
			catch (ContextError err)
			{
				// The procedure could not be unloaded due to an error in the
				// context
				// processing
				monitor.setCanceled(true);
				monitor.done();
				throw new UnloadFailed("Failed to close the procedure "
				        + instanceId + ": " + err.getLocalizedMessage());
			}
		}
		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcExtensions.get().fireModelUnloaded(model, true);
	}

	/***************************************************************************
	 * Release a given procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id 2) Request the
	 * context to unload the procedure 3) Unload the model 4) Notify extensions
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	private void releaseLocalProcedure(String instanceId,
	        IProgressMonitor monitor) throws UnloadFailed
	{
		monitor.beginTask("Release procedure", 2);

		IProcedure model = getProcedure(instanceId);

		monitor.subTask("Removing local model");
		monitor.worked(1);
		// Remove the model directly
		removeLocalProcedureModel(instanceId);

		if (!s_ctx.isConnected())
		{
			monitor.worked(2);
			monitor.done();
		}
		else
		{
			try
			{
				// Report progress
				monitor.subTask("Detaching");
				// Request context to close the procedure
				if (!s_ctx.detachFromExecutor(instanceId))
				{
					// The context command was sent but it raised an error
					ProcExtensions.get().fireModelUnloaded(model, true);
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed("Failed to release the procedure "
					        + instanceId + " on the server");
				}
				// Report progress
				monitor.worked(1);
			}
			catch (ContextError err)
			{
				// The procedure could not be unloaded due to an error in the
				// context
				// processing
				monitor.setCanceled(true);
				monitor.done();
				throw new UnloadFailed("Failed to release the procedure "
				        + instanceId + ": " + err.getLocalizedMessage());
			}
		}

		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcExtensions.get().fireModelUnloaded(model, true);
	}

	/***************************************************************************
	 * Kill a given procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id 2) Request the
	 * context to unload the procedure 3) Unload the model 4) Notify extensions
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	private void killLocalProcedure(String instanceId, IProgressMonitor monitor)
	        throws UnloadFailed
	{
		monitor.beginTask("Kill procedure", 3);
		IProcedure model = getProcedure(instanceId);

		monitor.subTask("Removing local model");
		monitor.worked(1);

		// Remove the model directly
		removeLocalProcedureModel(instanceId);

		if (!s_ctx.isConnected())
		{
			monitor.worked(3);
			monitor.done();
		}
		else
		{
			try
			{
				// Check condition: the client mode shall be controlling
				monitor.subTask("Checking client mode");
				ClientMode mode = model.getRuntimeInformation().getClientMode();
				if (mode != ClientMode.CONTROLLING)
				{
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed(
					        "Cannot kill this procedure while in monitoring mode");
				}
				// Report progress
				monitor.worked(1);

				monitor.subTask("Killing process");
				// Request context to close the procedure
				if (!s_ctx.killExecutor(instanceId))
				{
					// The context command was sent but it raised an error
					ProcExtensions.get().fireModelUnloaded(model, true);
					monitor.setCanceled(true);
					monitor.done();
					throw new UnloadFailed("Failed to close the procedure "
					        + instanceId + " on the server");
				}

				// Report progress
				monitor.worked(1);
			}
			catch (ContextError err)
			{
				// The procedure could not be unloaded due to an error in the
				// context
				// processing
				monitor.setCanceled(true);
				monitor.done();
				throw new UnloadFailed("Failed to close the procedure "
				        + instanceId + ": " + err.getLocalizedMessage());
			}
		}
		// Reached to this point we have the model of the procedure created.
		// Now notify any plugin extensions implementing the ProcedureView
		// support.
		ProcExtensions.get().fireModelUnloaded(model, true);
	}

	/***************************************************************************
	 * Unload a remote procedure instance. The sequence of operations is:
	 * 
	 * 1) Check that we have connectivity and the instance id 2) Request the
	 * context to unload the procedure 3) Unload the model 4) Notify extensions
	 * 
	 * @param instanceId
	 *            The procedure identifier, WITH instance number info
	 * @param kill
	 *            If true, the procedure is killed instead of closed
	 * @throws UnloadFailed
	 *             if the procedure could not be unloaded
	 **************************************************************************/
	private void unloadRemoteProcedure(String instanceId, boolean kill,
	        IProgressMonitor monitor) throws UnloadFailed
	{
		monitor.beginTask("Unload remote procedure", 2);

		// Check proxy connection
		checkConnectivity();

		// Check the identifier
		if (!m_remoteModels.containsKey(instanceId))
		{
			monitor.setCanceled(true);
			monitor.done();
			throw new UnloadFailed("No such procedure: '" + instanceId + "'");
		}

		monitor.worked(1);

		try
		{
			// Request context to load the procedure
			boolean closed = false;
			if (kill)
			{
				monitor.subTask("Killing process");
				closed = s_ctx.killExecutor(instanceId);
			}
			else
			{
				monitor.subTask("Closing process");
				closed = s_ctx.closeExecutor(instanceId);
			}
			if (!closed)
			{
				// The context command was sent but it raised an error
				monitor.setCanceled(true);
				monitor.done();
				throw new UnloadFailed("Could not unload the procedure "
				        + instanceId);
			}
			monitor.worked(1);
		}
		catch (ContextError err)
		{
			// The procedure could not be unloaded due to an error in the
			// context
			// processing
			throw new UnloadFailed("Could not unload the procedure "
			        + instanceId + ": " + err.getLocalizedMessage());
		}
	}

	/***************************************************************************
	 * Wait a signal until a procedure finishes the loading process
	 **************************************************************************/
	private void waitProcedureLoaded(String instanceId)
	{
		m_loadLock.lock();
		m_loadLocked = true;
		try
		{
			// Wait 15 seconds
			long remaining = m_loadCondition.awaitNanos((long) 15e9);
			if (remaining <= 0) { throw new LoadFailed(
			        "Could not open the procedure '" + instanceId
			                + "'\nIt took too long to load. Please try again."); }
		}
		catch (InterruptedException err)
		{
			throw new LoadFailed("Could not load the procedure '" + instanceId
			        + "'");
		}
		finally
		{
			m_loadLock.unlock();
		}
	}

	/***************************************************************************
	 * Trigger the signal when a procedure finishes the loading process
	 **************************************************************************/
	private void signalProcedureLoaded()
	{
		m_loadLock.lock();
		try
		{
			m_loadLocked = false;
			m_loadCondition.signalAll();
		}
		finally
		{
			m_loadLock.unlock();
		}
	}
}
