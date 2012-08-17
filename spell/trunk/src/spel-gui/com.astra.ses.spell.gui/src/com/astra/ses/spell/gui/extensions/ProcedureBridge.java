///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : ProcedureBridge.java
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
package com.astra.ses.spell.gui.extensions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

import com.astra.ses.spell.gui.core.interfaces.IProcedureOperation;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.notification.UserActionNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.UnloadType;
import com.astra.ses.spell.gui.interfaces.IProcedureItemsListener;
import com.astra.ses.spell.gui.interfaces.IProcedureMessageListener;
import com.astra.ses.spell.gui.interfaces.IProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.IProcedureStackListener;
import com.astra.ses.spell.gui.interfaces.IProcedureStatusListener;
import com.astra.ses.spell.gui.model.properties.AsRunOpenTester;
import com.astra.ses.spell.gui.model.properties.ProcedureConfigurableTester;
import com.astra.ses.spell.gui.model.properties.ProcedureOpenTester;
import com.astra.ses.spell.gui.model.properties.ProcedurePausedTester;
import com.astra.ses.spell.gui.procs.extensionpoints.IProcedureViewExtension;
import com.astra.ses.spell.gui.procs.interfaces.IProcedureModelView;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ProcedureBridge
{
	private Set<IProcedureViewExtension> m_procedureListeners = new HashSet<IProcedureViewExtension>();
	private Set<IProcedureModelView> m_procedureModelListeners = new HashSet<IProcedureModelView>();
	private Set<IProcedureMessageListener> m_procedureMessageListeners = new HashSet<IProcedureMessageListener>();
	private Set<IProcedurePromptListener> m_procedurePromptListeners = new HashSet<IProcedurePromptListener>();
	private Set<IProcedureItemsListener> m_procedureItemListeners = new HashSet<IProcedureItemsListener>();
	private Set<IProcedureStackListener> m_procedureStackListeners = new HashSet<IProcedureStackListener>();
	private Set<IProcedureStatusListener> m_procedureStatusListeners = new HashSet<IProcedureStatusListener>();
	private Set<IProcedureOperation> m_procedureOperationListeners = new HashSet<IProcedureOperation>();

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
	public void addProcedureListener(IProcedureViewExtension listener)
	{
		m_procedureListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeProcedureListener(IProcedureViewExtension listener)
	{
		m_procedureListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addProcedureModelListener(IProcedureModelView listener)
	{
		m_procedureModelListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeProcedureModelListener(IProcedureModelView listener)
	{
		m_procedureModelListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addProcedureOperationListener(IProcedureOperation listener)
	{
		m_procedureOperationListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeProcedureOperationListener(IProcedureOperation listener)
	{
		m_procedureOperationListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addProcedureMessageListener(IProcedureMessageListener listener)
	{
		m_procedureMessageListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeProcedureMessageListener(IProcedureMessageListener listener)
	{
		m_procedureMessageListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addProcedureInputListener(IProcedurePromptListener listener)
	{
		m_procedurePromptListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeProcedureInputListener(IProcedurePromptListener listener)
	{
		m_procedurePromptListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addProcedureStatusListener(IProcedureStatusListener listener)
	{
		m_procedureStatusListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeProcedureStatusListener(IProcedureStatusListener listener)
	{
		m_procedureStatusListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addProcedureStackListener(IProcedureStackListener listener)
	{
		m_procedureStackListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeProcedureStackListener(IProcedureStackListener listener)
	{
		m_procedureStackListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void addProcedureItemListener(IProcedureItemsListener listener)
	{
		m_procedureItemListeners.add(listener);
	}

	/***************************************************************************
	 * 
	 * @param listener
	 **************************************************************************/
	public void removeProcedureItemListener(IProcedureItemsListener listener)
	{
		m_procedureItemListeners.remove(listener);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private void refreshEvaluationService()
	{
		IEvaluationService svc = (IEvaluationService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		        .getService(IEvaluationService.class);
		if (svc != null)
		{
			svc.requestEvaluation(ProcedurePausedTester.ID);
			svc.requestEvaluation(ProcedureConfigurableTester.ID);
			svc.requestEvaluation(AsRunOpenTester.ID);
			svc.requestEvaluation(ProcedureOpenTester.ID);
		}
	}

	// ==========================================================================
	// EVENT FIRE METHODS
	// ==========================================================================

	void fireModelDisabled(final IProcedure model)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureModelDisabled(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedureModelView listener : m_procedureModelListeners)
				{
					try
					{
						listener.notifyProcedureModelDisabled(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireModelEnabled(final IProcedure model)
	{
		Display.getDefault().asyncExec(new Runnable()
		{

			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureModelEnabled(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedureModelView listener : m_procedureModelListeners)
				{
					try
					{
						listener.notifyProcedureModelEnabled(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireModelLoaded(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{

			@Override
			public void run()
			{
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureModelLoaded(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				}
				for (IProcedureModelView listener : m_procedureModelListeners)
				{
					try
					{
						listener.notifyProcedureModelLoaded(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireModelReset(final IProcedure model)
	{
		Display.getDefault().asyncExec(new Runnable()
		{

			@Override
			public void run()
			{
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureModelReset(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				}
				for (IProcedureModelView listener : m_procedureModelListeners)
				{
					try
					{
						listener.notifyProcedureModelReset(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				}
			}
		});
	}

	// ==========================================================================
	void fireModelUnloaded(final IProcedure model, final UnloadType type)
	{
		Display.getDefault().asyncExec(new Runnable()
		{

			@Override
			public void run()
			{
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureModelUnloaded(model, type);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedureModelView listener : m_procedureModelListeners)
				{
					try
					{
						listener.notifyProcedureModelUnloaded(model, type);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireModelConfigured(final IProcedure model)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureModelConfigured(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedureModelView listener : m_procedureModelListeners)
				{
					try
					{
						listener.notifyProcedureModelConfigured(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureDisplay(final IProcedure model, final DisplayData data)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureDisplay(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedureMessageListener listener : m_procedureMessageListeners)
				{
					try
					{
						listener.notifyDisplay(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureError(final IProcedure model, final ErrorData data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureError(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedureStatusListener listener : m_procedureStatusListeners)
				{
					try
					{
						listener.notifyError(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureItem(final IProcedure model, final ItemNotification data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureItem(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedureItemsListener listener : m_procedureItemListeners)
				{
					try
					{
						listener.notifyItem(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureStack(final IProcedure model, final StackNotification data)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureStack(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedureStackListener listener : m_procedureStackListeners)
				{
					try
					{
						listener.notifyStack(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureStatus(final IProcedure model, final StatusNotification data)
	{
		try
		{
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					refreshEvaluationService();

					for (IProcedureViewExtension clt : m_procedureListeners)
					{
						try
						{
							clt.notifyProcedureStatus(model, data);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
					for (IProcedureStatusListener listener : m_procedureStatusListeners)
					{
						try
						{
							listener.notifyStatus(model, data);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
			});
		}
		catch (Exception ex)
		{
		}
		;
	}

	// ==========================================================================
	void fireProcedurePrompt(final IProcedure model)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedurePrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedurePromptListener listener : m_procedurePromptListeners)
				{
					try
					{
						listener.notifyPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureFinishPrompt(final IProcedure model)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureFinishPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				for (IProcedurePromptListener listener : m_procedurePromptListeners)
				{
					try
					{
						listener.notifyFinishPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureCancelPrompt(final IProcedure model)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();
				
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureCancelPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}

				}
				for (IProcedurePromptListener listener : m_procedurePromptListeners)
				{
					try
					{
						listener.notifyCancelPrompt(model);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureUserAction(final IProcedure model, final UserActionNotification data)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureViewExtension clt : m_procedureListeners)
				{
					try
					{
						clt.notifyProcedureUserAction(model, data);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureClosed(final String procId, final String guiKey)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureOperation mon : m_procedureOperationListeners)
				{
					try
					{
						mon.notifyRemoteProcedureClosed(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureControlled(final String procId, final String guiKey)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureOperation mon : m_procedureOperationListeners)
				{
					try
					{
						mon.notifyRemoteProcedureControlled(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureKilled(final String procId, final String guiKey)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureOperation mon : m_procedureOperationListeners)
				{
					try
					{
						mon.notifyRemoteProcedureKilled(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureCrashed(final String procId, final String guiKey)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureOperation mon : m_procedureOperationListeners)
				{
					try
					{
						mon.notifyRemoteProcedureCrashed(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureMonitored(final String procId, final String guiKey)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureOperation mon : m_procedureOperationListeners)
				{
					try
					{
						mon.notifyRemoteProcedureMonitored(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureOpen(final String procId, final String guiKey)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureOperation mon : m_procedureOperationListeners)
				{
					try
					{
						mon.notifyRemoteProcedureOpen(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureReleased(final String procId, final String guiKey)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				for (IProcedureOperation mon : m_procedureOperationListeners)
				{
					try
					{
						mon.notifyRemoteProcedureReleased(procId, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}

	// ==========================================================================
	void fireProcedureStatus(final String procId, final ExecutorStatus status, final String guiKey)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				refreshEvaluationService();

				for (IProcedureOperation mon : m_procedureOperationListeners)
				{
					try
					{
						mon.notifyRemoteProcedureStatus(procId, status, guiKey);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		});
	}
}
