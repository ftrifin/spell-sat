///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code
// 
// FILE      : CodePresentation.java
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
package com.astra.ses.spell.gui.presentation.code;

import java.awt.print.Printable;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StatusNotification;
import com.astra.ses.spell.gui.core.model.types.ExecutionMode;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.interfaces.IPresentationNotifier;
import com.astra.ses.spell.gui.interfaces.IProcedureItemsListener;
import com.astra.ses.spell.gui.interfaces.IProcedurePromptListener;
import com.astra.ses.spell.gui.interfaces.IProcedureStatusListener;
import com.astra.ses.spell.gui.interfaces.ProcedurePresentationAdapter;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerColumn;
import com.astra.ses.spell.gui.presentation.code.printable.CodeViewPrintable;
import com.astra.ses.spell.gui.print.SpellFooterPrinter;
import com.astra.ses.spell.gui.print.SpellHeaderPrinter;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IStackChangesListener;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

public class CodePresentation extends ProcedurePresentationAdapter implements
        IStackChangesListener, IProcedurePromptListener,
        IProcedureStatusListener, IProcedureItemsListener
{
	private static final String	   ID	              = "com.astra.ses.spell.gui.presentation.CodeView";
	private static final String	   PRESENTATION_TITLE	= "Tabular";
	private static final String	   PRESENTATION_DESC	= "Procedure view in source code";
	private static final String	   PRESENTATION_ICON	= "icons/16x16/code.png";

	/** Code Viewer */
	private CodeViewer	           m_codeViewer;
	/** Data provier */
	private IProcedureDataProvider	m_dataProvider;
	/** Parent view */
	private IProcedure	           m_model;

	@Override
	public Composite createContents(IProcedure model, Composite stack)
	{
		m_model = model;
		m_dataProvider = m_model.getDataProvider();

		Composite codePage = new Composite(stack, SWT.NONE);
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

		// Register to execution changes
		m_dataProvider.addStackChangesListener(this);

		// Create the viewer, main control.
		m_codeViewer = new CodeViewer(codePage, m_model);

		return codePage;
	}

	@Override
	public void subscribeNotifications(IPresentationNotifier notifier)
	{
		notifier.addPromptListener(this);
		notifier.addStatusListener(this);
		notifier.addItemListener(this);
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
		m_codeViewer.zoom(zoomIn);
	}

	@Override
	public void showLine(int lineNo)
	{
		m_codeViewer.showLine(lineNo, true);
	}

	@Override
	public void notifyPrompt(IProcedure model)
	{
		m_codeViewer.showLastLine();
	}

	@Override
	public void notifyFinishPrompt(IProcedure model) {}

	@Override
	public void notifyCancelPrompt(IProcedure model) {}

	@Override
	public void notifyStatus(IProcedure model, StatusNotification data)
	{
		m_codeViewer.setExecutorStatus(data.getStatus());
	}

	@Override
	public void notifyError(IProcedure model, ErrorData data)
	{
		m_codeViewer.setExecutorStatus(ExecutorStatus.ERROR);
	}

	@Override
	public void notifyItem(IProcedure model, ItemNotification data)
	{
		if (!data.getExecutionMode().equals(ExecutionMode.PROCEDURE)) return;
		List<String> stack = data.getStackPosition();
		int lineNumber = Integer.parseInt(stack.get(stack.size()-1));
		m_codeViewer.newItemArrived(lineNumber);
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
	public void setEnabled(boolean enabled)
	{
		m_codeViewer.getTable().setEnabled(enabled);
	}

	@Override
	public void viewChanged(boolean sourceCodeChanged)
	{
		final boolean changed = sourceCodeChanged;
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				m_codeViewer.codeChanged(changed);
			}
		});
	}

	@Override
	public void lineChanged(final int line)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				m_codeViewer.lineChanged(line);
			}
		});
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
	{
		if (adapter.equals(Printable.class))
		{
			Table table = m_codeViewer.getTable();
			int columnCount = table.getColumnCount();
			int rowCount = table.getItemCount();
			int[] columnsLayout = new int[columnCount];
			for (int i = 0; i < columnCount; i++)
			{
				columnsLayout[i] = table.getColumn(i).getWidth();
			}
			String[][] tabbedData = new String[rowCount][columnCount];
			int j = 0;
			for (TableItem item : table.getItems())
			{
				String[] line = new String[columnCount];
				// Code column
				line[CodeViewerColumn.BREAKPOINT.ordinal()] = item
				        .getText(CodeViewerColumn.BREAKPOINT.ordinal());
				line[CodeViewerColumn.CODE.ordinal()] = item.getData(
				        CodeViewer.DATA_SOURCE).toString();
				line[CodeViewerColumn.NAME.ordinal()] = item
				        .getText(CodeViewerColumn.NAME.ordinal());
				line[CodeViewerColumn.LINE_NO.ordinal()] = item
				        .getText(CodeViewerColumn.LINE_NO.ordinal());
				line[CodeViewerColumn.STATUS.ordinal()] = item
				        .getText(CodeViewerColumn.STATUS.ordinal());
				line[CodeViewerColumn.VALUE.ordinal()] = item
				        .getText(CodeViewerColumn.VALUE.ordinal());
				// Assign the line
				tabbedData[j] = line;
				j++;
			}
			String title = m_model.getProcId() + " - Code view";
			SpellHeaderPrinter header = new SpellHeaderPrinter(title);
			SpellFooterPrinter footer = new SpellFooterPrinter();
			return new CodeViewPrintable(tabbedData, columnsLayout, header,
			        footer);
		}
		return null;
	}
}
