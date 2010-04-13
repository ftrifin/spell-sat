///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code
// 
// FILE      : CodePresentation.java
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
package com.astra.ses.spell.gui.presentation.code;

import java.awt.print.Printable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.notification.CodeNotification;
import com.astra.ses.spell.gui.core.model.notification.DisplayData;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.LineNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.interfaces.IProcedurePresentation;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.presentation.code.printable.CodeViewPrintable;
import com.astra.ses.spell.gui.print.SpellFooterPrinter;
import com.astra.ses.spell.gui.print.SpellHeaderPrinter;
import com.astra.ses.spell.gui.procs.model.ProcedureLine;
import com.astra.ses.spell.gui.views.ProcedureView;

public class CodePresentation implements IProcedurePresentation
{
	private CodeViewer m_codeViewer;
	private ProcedureView m_view;
	private static final String ID = "com.astra.ses.spell.gui.presentation.CodeView";
	private static final String PRESENTATION_TITLE = "Tabular";
	private static final String PRESENTATION_DESC  = "Procedure view in source code";
	private static final String PRESENTATION_ICON  = "icons/16x16/code.png";

	/***************************************************************************
	 * Create the presentation contents
	 * @return A composite containing all the presentation controls
	 **************************************************************************/
	public Composite createContents(ProcedureView view, Composite stack)
	{
		m_view = view;
		Composite codePage = new Composite(stack,SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginHeight = 0;
		groupLayout.marginWidth = 0;
		groupLayout.marginBottom = 0;
		groupLayout.marginTop = 0;
		groupLayout.verticalSpacing = 0;
		groupLayout.numColumns = 1;
		codePage.setLayout(groupLayout);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.verticalAlignment = SWT.FILL;
		codePage.setLayoutData(data);
		
		Logger.debug("Creating proc viewer", Level.INIT, this);
		// Create the viewer, main control. 
		m_codeViewer = new CodeViewer(m_view.getProcId(), codePage, this);
		return codePage;
	}

	/***************************************************************************
	 * Obtain the corresponding view
	 * @return The view reference
	 **************************************************************************/
	public ProcedureView getView()
	{
		return m_view;
	}

	/***************************************************************************
	 * Obtain the extension identifier
	 * @return The extension identifier
	 **************************************************************************/
	public String getExtensionId()
	{
		return ID;
	}

	/***************************************************************************
	 * Obtain the extension title
	 * @return The extension title
	 **************************************************************************/
	public String getTitle()
	{
		return PRESENTATION_TITLE;
	}

	/***************************************************************************
	 * Obtain the extension icon
	 * @return The extension icon
	 **************************************************************************/
	public Image getIcon()
	{
		return Activator.getImageDescriptor(PRESENTATION_ICON).createImage();
	}
	
	/***************************************************************************
	 * Obtain the extension description
	 * @return The extension description
	 **************************************************************************/
	public String getDescription()
	{
		return PRESENTATION_DESC;
	}
		
	/***************************************************************************
	 * Do zoom in/out
	 * @param zoomIn True if text size is to be increased
	 **************************************************************************/
	public void zoom( boolean zoomIn )
	{
		m_codeViewer.zoom(zoomIn);
	}

	/***************************************************************************
	 * Not used
	 **************************************************************************/
	public void notifyCancelPrompt(Input inputData) {}

	/***************************************************************************
	 * Not used
	 **************************************************************************/
	public void notifyDisplay(DisplayData data) {}

	/***************************************************************************
	 * Not used
	 **************************************************************************/
	public void notifyModelUnloaded() {}

	/***************************************************************************
	 * Not used
	 **************************************************************************/
	public void notifyPrompt(Input inputData)
	{
		m_codeViewer.showLastLine();
	}

	/***************************************************************************
	 * Not used
	 **************************************************************************/
	public void notifyError(ErrorData data){}

	/***************************************************************************
	 * Model loaded callback
	 **************************************************************************/
	public void notifyModelLoaded() 
	{
		m_codeViewer.updateCode(false);
	}

	/***************************************************************************
	 * Code change callback
	 **************************************************************************/
	public void notifyCode(CodeNotification data)   
	{
		m_codeViewer.updateCode(false);
	}

	/***************************************************************************
	 * Item notification callback
	 **************************************************************************/
	public void notifyItem(ItemNotification data) 
	{
		m_codeViewer.notifyItem(data);
	}

	/***************************************************************************
	 * Item line callback
	 **************************************************************************/
	public void notifyLine(LineNotification data)
	{
		m_codeViewer.notifyLine(data);
	}

	/***************************************************************************
	 * Model disabled callback
	 **************************************************************************/
	public void notifyModelDisabled()
	{
		m_codeViewer.setEnabled(false);
	}

	/***************************************************************************
	 * Model enabled callback
	 **************************************************************************/
	public void notifyModelEnabled()
	{
		m_codeViewer.setEnabled(true);
	}

	/***************************************************************************
	 * Model reset callback
	 **************************************************************************/
	public void notifyModelReset() 
	{
		m_codeViewer.updateCode( true );
	}

	/***************************************************************************
	 * Status callback
	 **************************************************************************/
	public void notifyStatus(StatusNotification data) 
	{
		m_codeViewer.notifyProcStatus(data.getStatus());
	}

	@Override
	public void setAutoScroll(boolean enabled)
	{
		m_codeViewer.setAutoScroll(enabled);
		if (enabled)
		{
			m_codeViewer.showLastLine();
		}
	}

	@Override
	public void selected() 
	{
		// Nothing to do at this moment
	}

	@Override
	public void notifyModelConfigured()
	{
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter.equals(Printable.class))
		{
			Table table = m_codeViewer.getTable();
			int columnCount = table.getColumnCount();
			int rowCount = table.getItemCount();
			int[] columnsLayout = new int[columnCount];
			for (int i=0; i<columnCount; i++)
			{
				columnsLayout[i] = table.getColumn(i).getWidth();
			}
			String[][] tabbedData = new String[rowCount][columnCount];
			int j = 0;
			for (TableItem item : table.getItems())
			{
				String[] line = new String[columnCount];
				// Code column
				String code = ((ProcedureLine) item.getData("PROC_LINE")).getSource();
				line[CodeViewer.EXEC_COLUMN] = item.getText(CodeViewer.EXEC_COLUMN);
				line[CodeViewer.CODE_COLUMN] = code;
				line[CodeViewer.NAME_COLUMN] = item.getText(CodeViewer.NAME_COLUMN);
				line[CodeViewer.NUM_COLUMN] = item.getText(CodeViewer.NUM_COLUMN);
				line[CodeViewer.STS_COLUMN] = item.getText(CodeViewer.STS_COLUMN);
				line[CodeViewer.VALUE_COLUMN] = item.getText(CodeViewer.VALUE_COLUMN);
				// Assign the line
				tabbedData[j] = line;
				j++;
			}
			String title = m_view.getProcId() + " - Code view";
			SpellHeaderPrinter header = new SpellHeaderPrinter(title);
			SpellFooterPrinter footer = new SpellFooterPrinter();
			return new CodeViewPrintable(tabbedData, columnsLayout, header,footer);
		}
		return null;
	}
}
