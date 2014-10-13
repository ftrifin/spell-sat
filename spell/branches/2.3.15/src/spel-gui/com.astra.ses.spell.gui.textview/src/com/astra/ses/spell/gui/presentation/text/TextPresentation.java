///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text
// 
// FILE      : TextPresentation.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.presentation.text;

import java.awt.print.Printable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IPresentationNotifier;
import com.astra.ses.spell.gui.interfaces.IProcedureMessageListener;
import com.astra.ses.spell.gui.interfaces.IProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.IProcedureRuntimeListener;
import com.astra.ses.spell.gui.interfaces.IProcedureStatusListener;
import com.astra.ses.spell.gui.interfaces.ProcedurePresentationAdapter;
import com.astra.ses.spell.gui.presentation.text.controls.DisplayViewer;
import com.astra.ses.spell.gui.print.SpellFooterPrinter;
import com.astra.ses.spell.gui.print.SpellHeaderPrinter;
import com.astra.ses.spell.gui.print.printables.PlainTextPrintable;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class TextPresentation extends ProcedurePresentationAdapter implements IProcedureMessageListener, IProcedurePromptListener,
        IProcedureStatusListener, IProcedureRuntimeListener
{
	/** Holds the viewer control */
	private DisplayViewer m_displayViewer;
	/** Holds reference to the procedure model */
	private IProcedure m_model;
	/** Presentation identifier for the extension */
	private static final String ID = "com.astra.ses.spell.gui.presentation.TextView";
	/** Presentation title */
	private static final String PRESENTATION_TITLE = "Text";
	/** Presentation description */
	private static final String PRESENTATION_DESC = "Procedure view in text mode";
	/** Presentation icon path */
	private static final String PRESENTATION_ICON = "icons/16x16/text.png";

	@Override
	public Composite createContents(IProcedure model, Composite stack)
	{
		m_model = model;
		Composite displayPage = new Composite(stack, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginHeight = 0;
		groupLayout.marginWidth = 0;
		groupLayout.marginBottom = 0;
		groupLayout.marginTop = 0;
		groupLayout.verticalSpacing = 0;
		groupLayout.numColumns = 1;
		displayPage.setLayout(groupLayout);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		displayPage.setLayoutData(data);

		Logger.debug("Creating proc viewer", Level.INIT, this);
		// Create the viewer, main control.
		m_displayViewer = new DisplayViewer(displayPage);
		return displayPage;
	}

	@Override
	public void subscribeNotifications(IPresentationNotifier notifier)
	{
		notifier.addMessageListener(this);
		notifier.addPromptListener(this);
		notifier.addStatusListener(this);
		notifier.addRuntimeListener(this);
	}

	@Override
	public String getExtensionId()
	{
		return ID;
	}

	@Override
	public String getTitle()
	{
		return PRESENTATION_TITLE;
	}

	@Override
	public Image getIcon()
	{
		return Activator.getImageDescriptor(PRESENTATION_ICON).createImage();
	}

	@Override
	public String getDescription()
	{
		return PRESENTATION_DESC;
	}

	@Override
	public void zoom(boolean zoomIn)
	{
		m_displayViewer.zoom(zoomIn);
	}

	@Override
	public void showLine(int lineNo)
	{
	};

	@Override
	public void setAutoScroll(boolean enabled)
	{
		m_displayViewer.setAutoscroll(enabled);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
	{
		if (adapter.equals(Printable.class))
		{
			String[] lines = m_displayViewer.getTextLines();
			String title = m_model.getProcId() + " - Text view";
			SpellHeaderPrinter header = new SpellHeaderPrinter(title);
			SpellFooterPrinter footer = new SpellFooterPrinter();
			return new PlainTextPrintable(lines, header, footer);
		}
		return null;
	}

	@Override
	public void notifyDisplay(IProcedure model, DisplayData data)
	{
		if (!data.getExecutionMode().equals(ExecutionMode.PROCEDURE))
			return;
		m_displayViewer.addMessage(data.getMessage(), data.getSeverity(), data.getScope(), data.getSequence());
	}

	@Override
	public void notifyPrompt(IProcedure model) {}

	@Override
	public void notifyFinishPrompt(IProcedure model) {}

	@Override
	public void notifyCancelPrompt(IProcedure model) {}

	@Override
	public void notifyError(IProcedure model, ErrorData data)
	{
		m_displayViewer.addMessage(data.getMessage() + "\n   " + data.getReason(), Severity.ERROR, Scope.SYS, data.getSequence());
		m_displayViewer.notifyProcStatus(ExecutorStatus.ERROR);
	}

	@Override
	public void notifyModelLoaded(IProcedure model)
	{
		m_displayViewer.clear();
		// Try to recover any message to replay if we are monitoring
		DisplayData[] replayMessages = model.getRuntimeInformation().getDisplayMessages();
		// If there are messages, put them in the display after clearing
		if (replayMessages != null)
		{
			for (DisplayData data : replayMessages)
			{
				m_displayViewer.addMessage(data.getMessage(), data.getSeverity(), data.getScope(), data.getSequence());
			}
		}
	}

	@Override
	public void notifyModelDisabled(IProcedure model)
	{
		m_displayViewer.setEnabled(false);
	}

	@Override
	public void notifyModelEnabled(IProcedure model)
	{
		m_displayViewer.setEnabled(true);
	}

	@Override
	public void notifyModelReset(IProcedure model)
	{
		m_displayViewer.clear();
	}

	@Override
	public void notifyModelUnloaded(IProcedure model)
	{
	}

	@Override
	public void notifyModelConfigured(IProcedure model)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.astra.ses.spell.gui.interfaces.IProcedureStatusListener#notifyStatus
	 * (com.astra.ses.spell.gui.procs.interfaces.model.IProcedure,
	 * com.astra.ses.spell.gui.core.model.notification.StatusNotification)
	 */
	@Override
	public void notifyStatus(IProcedure model, StatusNotification data)
	{
		if (data.getStatus() != ExecutorStatus.UNKNOWN)
		{
			m_displayViewer.notifyProcStatus(data.getStatus());
		}
	}
}
