///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text
// 
// FILE      : TextPresentation.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.presentation.text;

import java.awt.print.Printable;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.NotificationData;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.interfaces.IProcedurePresentation;
import com.astra.ses.spell.gui.presentation.text.controls.DisplayViewer;
import com.astra.ses.spell.gui.presentation.text.model.ParagraphType;
import com.astra.ses.spell.gui.presentation.text.model.TextParagraph;
import com.astra.ses.spell.gui.print.SpellFooterPrinter;
import com.astra.ses.spell.gui.print.SpellHeaderPrinter;
import com.astra.ses.spell.gui.print.printables.PlainTextPrintable;
import com.astra.ses.spell.gui.services.ConfigurationManager;
import com.astra.ses.spell.gui.views.ProcedureView;


public class TextPresentation implements IProcedurePresentation
{
	private DisplayViewer  m_displayViewer;
	private ProcedureView m_view;
	private static final String ID = "com.astra.ses.spell.gui.presentation.TextView";
	private static final String PRESENTATION_TITLE = "Text";
	private static final String PRESENTATION_DESC  = "Procedure view in text mode";
	private static final String PRESENTATION_ICON  = "icons/16x16/text.png";
	
	public TextPresentation()
	{
		// Configure images and colors
		TextParagraph.configureIconImage(ParagraphType.NORMAL, "icons/16x16/img_disp.png");
		TextParagraph.configureIconImage(ParagraphType.WARNING,"icons/16x16/img_warning.png");
		TextParagraph.configureIconImage(ParagraphType.ERROR,  "icons/16x16/img_error.png");
		TextParagraph.configureIconImage(ParagraphType.PROMPT, "icons/16x16/img_prompt.png");
		TextParagraph.configureIconImage(ParagraphType.SPELL,  "icons/16x16/img_spell.png");

		ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		TextParagraph.configureParagraphColor(ParagraphType.WARNING, cfg.getStatusColor(ItemStatus.WARNING));
		TextParagraph.configureParagraphColor(ParagraphType.ERROR, cfg.getStatusColor(ItemStatus.ERROR));
	}
	
	/***************************************************************************
	 * Create the composite
	 **************************************************************************/
	public Composite createContents(ProcedureView view, Composite stack)
	{
		m_view = view;
		Composite displayPage = new Composite(stack,SWT.NONE);
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

	/***************************************************************************
	 * Obtain the parent view
	 **************************************************************************/
	public ProcedureView getView()
	{
		return m_view;
	}
	
	/***************************************************************************
	 * Obtain extension identifier
	 **************************************************************************/
	public String getExtensionId()
	{
		return ID;
	}

	/***************************************************************************
	 * Obtain presentation title
	 **************************************************************************/
	public String getTitle()
	{
		return PRESENTATION_TITLE;
	}

	/***************************************************************************
	 * Obtain presentation icon
	 **************************************************************************/
	public Image getIcon()
	{
		return Activator.getImageDescriptor(PRESENTATION_ICON).createImage();
	}
	
	/***************************************************************************
	 * Obtain presentation description
	 **************************************************************************/
	public String getDescription()
	{
		return PRESENTATION_DESC;
	}
	
	/***************************************************************************
	 * Zoom font
	 **************************************************************************/
	public void zoom( boolean zoomIn )
	{
		m_displayViewer.zoom(zoomIn);
	}
	
	/***************************************************************************
	 * Not used
	 **************************************************************************/
	public void notifyCancelPrompt(Input inputData) 
	{
		//Nothing to do
	}
	
	/***************************************************************************
	 * Display messages
	 **************************************************************************/
	public void notifyDisplay(DisplayData data) 
	{
		m_displayViewer.addMessage(data.getMessage(), data.getSeverity(), getDataTime(data) );
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelUnloaded() 
	{
		m_displayViewer.addInternal("Procedure has been disabled");
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyPrompt(Input inputData)
	{
		m_displayViewer.prompt(inputData, getDataTime(inputData));
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyError(ErrorData data)
	{
		m_displayViewer.addMessage(data.getMessage() + "\n   " +
				data.getReason(), Severity.ERROR, getDataTime(data) );
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelLoaded() 
	{
		m_displayViewer.clear();
		// Try to recover any message to replay if we are monitoring
		ArrayList<DisplayData> replayMessages = getView().getModel().getReplayMessages();
		// If there are messages, put them in the display after clearing
		if (replayMessages != null)
		{
			for(DisplayData data : replayMessages)
			{
				m_displayViewer.addMessage(data.getMessage(), data.getSeverity(), getDataTime(data) );
			}
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyCode(CodeNotification data)   
	{
		m_displayViewer.addInternal("Loading code: " + data.getProcId());
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyItem(ItemNotification data) 
	{
		// Nothing to do
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyLine(LineNotification data)
	{
		// Nothing to do
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelDisabled()
	{
		m_displayViewer.setEnabled(false);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelEnabled()
	{
		m_displayViewer.setEnabled(true);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelReset() 
	{
		m_displayViewer.clear();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyStatus(StatusNotification data) 
	{
		if (data.getStatus() != ExecutorStatus.UNKNOWN)
		{
			m_displayViewer.notifyProcStatus(data.getStatus());
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setAutoScroll( boolean enabled )
	{
		m_displayViewer.setAutoscroll(enabled);
	}

	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void notifyModelConfigured()
	{
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("unchecked")
	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Printable.class))
		{
			String[] lines = m_displayViewer.getTextLines();
			String title = m_view.getProcId() + " - Text view";
			SpellHeaderPrinter header = new SpellHeaderPrinter(title);
			SpellFooterPrinter footer = new SpellFooterPrinter();
			return new PlainTextPrintable(lines, header, footer);
		}
		return null;
	}

	@Override
	/***************************************************************************
	 * 
	 **************************************************************************/
	public void selected() {
		// TODO Auto-generated method stub
		
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	private double getDataTime( NotificationData data ) 
	{
		double msgTime = -1;
		try
		{
			msgTime = Double.parseDouble(data.getTime());
		}
		catch(Exception ex) 
		{
			msgTime = System.currentTimeMillis()/1000.0;
		};
		return msgTime;
	}
}
