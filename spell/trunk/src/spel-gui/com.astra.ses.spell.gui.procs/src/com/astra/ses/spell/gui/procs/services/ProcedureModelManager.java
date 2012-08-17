///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.services
// 
// FILE      : ProcedureModelManager.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.procs.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.exceptions.ContextError;
import com.astra.ses.spell.gui.core.interfaces.IContextProxy;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.interfaces.IFileManager;
import com.astra.ses.spell.gui.core.model.files.AsRunFile;
import com.astra.ses.spell.gui.core.model.files.AsRunFileLine;
import com.astra.ses.spell.gui.core.model.files.IServerFileLine;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.InputData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.core.model.types.PromptDisplayType;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.model.types.ServerFileType;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.ProcExtensions;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.exceptions.NoSuchProcedure;
import com.astra.ses.spell.gui.procs.exceptions.NotConnected;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.priv.IExecutionInformationHandler;
import com.astra.ses.spell.gui.procs.model.Procedure;
import com.astra.ses.spell.gui.procs.model.RemoteProcedure;

/**
 * @author Rafael Chinchilla
 *
 */
class ProcedureModelManager
{
	/** Holds the reference to the context proxy */
	private IContextProxy m_proxy;
	/** Holds the reference to the context proxy */
	private IFileManager m_fileMgr;
	/** Holds the list of local procedures */
	private Map<String, IProcedure> m_localModels;
	/** Holds the list of remote procedures */
	private Map<String, IProcedure> m_remoteModels;
	/** Holds the list of valid procedure identifiers */
	private Map<String, String>	    m_availableProcedures;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ProcedureModelManager( IContextProxy proxy, IFileManager fileMgr )
	{
		m_localModels = new TreeMap<String, IProcedure>();
		m_remoteModels = new TreeMap<String, IProcedure>();
		m_availableProcedures = new HashMap<String, String>();
		m_proxy = proxy;
		m_fileMgr = fileMgr;
	}
	
	/***************************************************************************
	 * Request the list of available procedures to the context. Called as soon
	 * as the context proxy Connected Event is received.
	 **************************************************************************/
	Map<String, String> obtainAvailableProcedures()
	{
		try
		{
			// Ensure we are connected
			checkConnectivity();
			Logger.debug("Loading available procedures", Level.PROC, this);
			m_availableProcedures = m_proxy.getAvailableProcedures();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			m_availableProcedures.clear();
		}
		return m_availableProcedures;
	}

	/***************************************************************************
	 * Request the list of available procedures to the context. Called as soon
	 * as the context proxy Connected Event is received.
	 **************************************************************************/
	void obtainRemoteProcedures()
	{
		try
		{
			m_remoteModels.clear();
			Logger.debug("Loading active executor models", Level.PROC, this);
			List<String> executorIds = m_proxy.getAvailableExecutors();
			for (String instanceId : executorIds)
			{
				// Do not load those which are local
				if (m_localModels.containsKey(instanceId)) continue;
				createRemoteProcedureModel(instanceId);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			clearRemoteProcedures();
		}
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
	IProcedure createLocalProcedureModel(String procedureId, IProgressMonitor monitor) throws LoadFailed
	{
		Logger.debug("Creating local model " + procedureId, Level.PROC, this);
		// Check proxy connection
		checkConnectivity();

		// Procedure ID shall exist
		if (!m_availableProcedures.containsKey(procedureId))
		{
			throw new LoadFailed("No such procedure: '" + procedureId + "'"); 
		}

		// Obtain a valid instance identifier from the context
		String instanceId = getAvailableId(procedureId);
		Logger.debug("Getting procedure properties for " + procedureId, Level.PROC, this);
		TreeMap<ProcProperties, String> props = m_proxy.getProcedureProperties(procedureId);

		Logger.debug("Instantiate model " + instanceId, Level.PROC, this);
		IProcedure proc = new Procedure(instanceId, props, ClientMode.CONTROLLING);

		// Provoke the first source code acquisition
		Logger.debug("Acquire source code for the first time", Level.PROC, this);
		proc.getDataProvider().getRootSource(monitor);

		Logger.debug("Store local model " + instanceId, Level.PROC, this);
		m_localModels.put(instanceId, proc);

		return proc;
	}
	
	/***************************************************************************
	 * Create a remote procedure model and add it to the list
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	synchronized void createRemoteProcedureModel(String instanceId)
	{
		try
		{
			if (!m_remoteModels.containsKey(instanceId))
			{
				Logger.debug("Registered remote model: " + instanceId,Level.PROC, this);
				IProcedure proc = new RemoteProcedure(instanceId);
				Logger.debug("Updating remote model for " + instanceId , Level.PROC, this);
				proc.getController().refresh();
				Logger.debug("Store remote model " + instanceId, Level.PROC, this);
				m_remoteModels.put(instanceId, proc);
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
	synchronized void deleteRemoteProcedureModel(String instanceId)
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			Logger.debug("Deleting remote model: " + instanceId , Level.PROC, this);
			m_remoteModels.remove(instanceId);
		}
	}

	/***************************************************************************
	 * Remove a local procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	synchronized void deleteLocalProcedureModel(String instanceId)
	{
		if (m_localModels.containsKey(instanceId))
		{
			Logger.debug("Deleting local model: " + instanceId , Level.PROC, this);
			m_localModels.remove(instanceId);
		}
	}

	/***************************************************************************
	 * Convert a local model to remote model
	 * 
	 * @param instanceId
	 *            Procedure model to convert
	 **************************************************************************/
	synchronized void convertToRemote(String instanceId, IProgressMonitor monitor )
	{
		Logger.debug("Converting local model to remote" + instanceId, Level.PROC, this);
		IProcedure proc = getProcedure(instanceId);
		// The procedure becomes remote
		IProcedure remote = new RemoteProcedure(proc);
		Logger.debug("Store remote model" + instanceId, Level.PROC, this);
		m_remoteModels.put(instanceId, remote);
		// IMPORTANT do not delete the local model yet. It will be done by the
		// procedure manager. In case of load failure, we will not want to
		// remove the local, but the remote (rollback)
	}

	/***************************************************************************
	 * Convert a remote model to local
	 * 
	 * @param procedureId
	 *            The procedure identifier, no instance number info
	 **************************************************************************/
	IProcedure convertToLocal(String instanceId, ClientMode mode) throws LoadFailed
	{
		// Check proxy connection
		checkConnectivity();

		Logger.debug("Converting remote procedure model to local: " + instanceId, Level.PROC, this);

		if (!m_remoteModels.containsKey(instanceId)) 
		{ 
			throw new LoadFailed( "Could not find remote procedure: '" + instanceId + "'"); 
		}
		
		String[] elements = instanceId.split("#");

		Logger.debug("Retrieving procedure properties" , Level.PROC, this);
		TreeMap<ProcProperties, String> props = m_proxy.getProcedureProperties(elements[0]);

		Logger.debug("Instantiate model", Level.PROC, this);
		IProcedure proc = new Procedure(instanceId, props, mode);

		Logger.debug("Store local model: " + instanceId , Level.PROC, this);
		m_localModels.put(instanceId, proc);

		return proc;
	}


	/***************************************************************************
	 * Update a remote procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	void updateRemoteProcedureModel(String instanceId) throws Exception
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			Logger.debug("Updating remote procedure model: " + instanceId , Level.PROC, this);
			IProcedure proc = m_remoteModels.get(instanceId);
			IExecutorInfo info = new ExecutorInfo(instanceId);
			m_proxy.updateExecutorInfo(instanceId, info);
			((IExecutionInformationHandler) proc.getRuntimeInformation()).copyFrom(info);
		}
	}

	/***************************************************************************
	 * Obtain a local procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure model
	 **************************************************************************/
	IProcedure getProcedure(String instanceId) throws NoSuchProcedure
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
	 * Obtain a remote procedure model.
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return The procedure reduced model
	 **************************************************************************/
	synchronized IProcedure getRemoteProcedure(String instanceId) throws NoSuchProcedure
	{
		if (m_remoteModels.containsKey(instanceId))
		{
			return m_remoteModels.get(instanceId);
		}
		else
		{
			throw new NoSuchProcedure(instanceId);
		}
	}

	/**************************************************************************
	 * Get procedure name given its Id
	 * 
	 * @param procId
	 * @return
	 *************************************************************************/
	String getProcedureName(String procId)
	{
		if (!m_availableProcedures.containsKey(procId)) { return null; }
		return m_availableProcedures.get(procId);
	}

	/***************************************************************************
	 * Request the procedure properties
	 **************************************************************************/
	public TreeMap<ProcProperties, String> getProcedureProperties(String procedureId)
	{
		TreeMap<ProcProperties, String> map = null;
		try
		{
			// Ensure we are connected
			checkConnectivity();
			map = m_proxy.getProcedureProperties(procedureId);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return map;
	}


	/***************************************************************************
	 * Check if a model is local
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return True if locally loaded
	 **************************************************************************/
	boolean isLocal(String instanceId)
	{
		return (m_localModels.containsKey(instanceId));
	}

	/***************************************************************************
	 * Check if a model is remote
	 * 
	 * @param instanceId
	 *            The instance identifier
	 * @return True if remotelly loaded
	 **************************************************************************/
	boolean isRemote(String instanceId)
	{
		return (m_remoteModels.containsKey(instanceId));
	}

	/***************************************************************************
	 * Update a remote procedure model
	 * 
	 * @param instanceId
	 *            Procedure instance identifier
	 **************************************************************************/
	void updateLocalProcedureModel(String instanceId, String asRunFilePath, ClientMode mode, boolean replay, IProgressMonitor monitor) throws Exception
	{
		if (m_localModels.containsKey(instanceId))
		{
			Logger.debug("Updating local procedure model: " + instanceId , Level.PROC, this);
			IProcedure proc = m_localModels.get(instanceId);
			// Reset the executor info for replaying execution
			AsRunFile asRun = null;
			boolean replaySuccess = true;

			if (replay)
			{
				monitor.subTask("Retrieving AS-RUN file");
				if (asRunFilePath != null)
				{
					Logger.debug("Retrieving As-Run file: " + asRunFilePath, Level.PROC, this);
					asRun = (AsRunFile) m_fileMgr.getServerFile(asRunFilePath, ServerFileType.ASRUN, monitor);
				}
				else
				{
					Logger.debug("Retrieving As-Run file for " + instanceId, Level.PROC, this);
					String path = m_fileMgr.getServerFilePath(instanceId, ServerFileType.ASRUN, monitor);
					asRun = (AsRunFile) m_fileMgr.getServerFile(path, ServerFileType.ASRUN, monitor);
				}
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
			Logger.debug("Refresh controller status (update local model)" , Level.PROC, this);
			proc.getController().refresh();

			monitor.worked(1);
		}
	}

	/***************************************************************************
	 * Obtain the list of available procedures in context
	 * 
	 * @return The procedure list
	 **************************************************************************/
	Map<String, String> getAvailableProcedures( boolean refresh )
	{
		if (refresh)
		{
			m_availableProcedures = m_proxy.getAvailableProcedures();
		}
		return m_availableProcedures;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	Set<String> getOpenLocalProcedures()
	{
		return m_localModels.keySet();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	Set<String> getOpenRemoteProcedures()
	{
		return m_remoteModels.keySet();
	}

	/***************************************************************************
	 * Clear the list of available procedures. Called as soon as the context
	 * proxy Disconnected Event is received.
	 **************************************************************************/
	void clearAvailableProcedures()
	{
		m_availableProcedures.clear();
	}

	/***************************************************************************
	 * Clear the list of available procedures. Called as soon as the context
	 * proxy Disconnected Event is received.
	 **************************************************************************/
	void clearRemoteProcedures()
	{
		m_remoteModels.clear();
	}

	/***************************************************************************
	 * When the context connection is lost, all models shall be disabled. This
	 * should result on disabling procedure model views as well, but this is up
	 * to the view provider plugin.
	 **************************************************************************/
	void disableProcedures( String reason )
	{
		// Notify the consumers of the local procedures that they cannot be
		// used any more
		for (IProcedure proc : m_localModels.values())
		{
			disableProcedure( reason, proc );
		}
	}

	/***************************************************************************
	 * When the context connection is lost, all models shall be disabled. This
	 * should result on disabling procedure model views as well, but this is up
	 * to the view provider plugin.
	 **************************************************************************/
	void disableProcedure( String reason, IProcedure procedure )
	{
		String procId = procedure.getProcId();
		ErrorData data = new ErrorData(procId,reason, "", true);
		IExecutionInformationHandler handler = (IExecutionInformationHandler) procedure.getRuntimeInformation();
		handler.setExecutorLost();
		procedure.getRuntimeProcessor().notifyProcedureError(data);
		ProcExtensions.get().fireProcedureError(procedure, data);
		ProcExtensions.get().fireModelDisabled(procedure);
	}
	
	/***************************************************************************
	 * When the context connection is recovered, all models can be reenabled.
	 * This should result on enabling procedure model views as well, but this is
	 * up to the view provider plugin.
	 **************************************************************************/
	void enableProcedures()
	{
		// Notify the consumers of the local procedures that they cannot be
		// used any more
		for (IProcedure proc : m_localModels.values())
		{
			// We shall refresh the model first
			try
			{
				proc.getController().refresh();
				// Then notify consumers
				ProcExtensions.get().fireModelEnabled(proc);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
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

		if (!model.isInReplayMode())
		{
			Logger.error("Cannot replay, model is not in the correct mode", Level.PROC, this );
			return false; 
		}
		
		ErrorData retrievedError = null;
		StatusNotification retrievedStatus = null;

		// Data to build a prompt
		String promptMessage = null;
		Vector<String> promptOptions = null;
		Vector<String> promptExpected = null;
		boolean numericPrompt = false;
		Scope promptScope = Scope.OTHER;

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
					StackNotification cdata = (StackNotification) arLine.getNotificationData();
					model.getRuntimeProcessor().notifyProcedureStack(cdata);
					break;
				case ITEM:
					ItemNotification item = (ItemNotification) arLine.getNotificationData();
					model.getRuntimeProcessor().notifyProcedureItem(item);
					break;
				case DISPLAY: // Fall through
				case ANSWER:
					DisplayData ddata = (DisplayData) arLine.getNotificationData();
					model.getRuntimeProcessor().notifyProcedureDisplay(ddata);
					break;
				case STATUS:
					// Update only if the procedure is not in error
					retrievedStatus = (StatusNotification) arLine.getNotificationData();
					break;
				case ERROR:
					retrievedError = (ErrorData) arLine.getNotificationData();
					break;
				case INIT:
					break;
				case PROMPT: 
					promptMessage = arLine.getDataA();
					// Clear other prompt data, this may be not the first prompt in the asrun
					numericPrompt = false;
					promptOptions = null;
					promptExpected = null;
					break;
				case PROMPT_TYPE:
					if (arLine.getSubType().equals("16"))
					{
						numericPrompt = true;
					}
					break;
				case PROMPT_OPTIONS:
					String opts = arLine.getSubType();
					String[] options = opts.split(",,");
					promptOptions = new Vector<String>();
					promptOptions.addAll( Arrays.asList(options) );
					break;
				case PROMPT_EXPECTED:
					String expt = arLine.getSubType();
					String[] expected = expt.split(",,");
					promptExpected = new Vector<String>();
					promptExpected.addAll( Arrays.asList(expected) );
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
				        + tabbedLine.toString().replace("\t", "<T>"), Level.PROC, this);
				Logger.error("   " + ex.getLocalizedMessage(), Level.PROC, this);
				result = false;
			}
			count++;
		}
		
		Logger.debug("Finished replay with status " + retrievedStatus.getStatus(), Level.PROC, this);
		
		if (retrievedStatus != null)
		{
			if (retrievedStatus.getStatus().equals(ExecutorStatus.PROMPT))
			{
				Logger.debug("Recreating prompt", Level.PROC, this);
				InputData inputData = null;
				if (promptOptions != null)
				{
					inputData = new InputData(null, model.getProcId(), promptMessage, promptScope, promptOptions, promptExpected, false, PromptDisplayType.RADIO );
				}
				else
				{
					inputData = new InputData(null, model.getProcId(), promptMessage, promptScope, numericPrompt, false );
				}
				model.getController().notifyProcedurePrompt(inputData);
			}
			// Send the status notification to the model
			model.getRuntimeProcessor().notifyProcedureStatus(retrievedStatus);
		}
		if (retrievedError != null)
		{
			model.getController().setError(retrievedError);
		}
		
		if (retrievedLine == false)
		{
			Logger.error("Unable to retrieve current line information", Level.PROC, this);
			result = false;
		}
		else
		{
			Logger.info("Finished execution replay on " + model.getProcId() + ", processed " + count + " lines", Level.PROC, this);
		}

		return result;
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
			Logger.debug("Obtaining available instance for " + procId, Level.PROC, this);
			instanceId = m_proxy.getProcedureInstanceId(procId);
			Logger.debug("Available instance is " + instanceId, Level.PROC, this);
		}
		catch (ContextError ex)
		{
			ex.printStackTrace();
		}
		return instanceId;
	}

	/***************************************************************************
	 * Check if we have connection to context
	 * 
	 * @throws NotConnected
	 *             if context proxy is not connected
	 **************************************************************************/
	private void checkConnectivity() throws NotConnected
	{
		if (!m_proxy.isConnected()) 
		{ 
			throw new NotConnected("Cannot operate: not conected to context");
		}
	}
}
