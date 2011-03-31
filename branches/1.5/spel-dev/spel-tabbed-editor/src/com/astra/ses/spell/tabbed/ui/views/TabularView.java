///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.tabbed.ui.table.views
// 
// FILE      : TabularView.java
//
// DATE      : 2009-11-23
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.tabbed.ui.views;

import java.util.ArrayList;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.astra.ses.spell.parser.TabbedFileParser;
import com.astra.ses.spell.tabbed.ui.table.TabularContentProvider;
import com.astra.ses.spell.tabbed.ui.table.TabularLabelProvider;


public class TabularView extends ViewPart {

	/** Header names */
	private String[] m_headerNames;
	/** File path */
	private String m_filePath;
	/** Comment sequence */
	private static final String COMMENT = "#";
	
	/**************************************************************************
	 * Constructor
	 * @param tabbedFile
	 *************************************************************************/
	public TabularView(String tabbedFile) {
		m_filePath = tabbedFile;
		m_headerNames = null;
	}

	/**************************************************************************
	 * Constructor, defining column names
	 * @param tabbedFile
	 * @param headerNames
	 *************************************************************************/
	public TabularView(String tabbedFile, String[] headerNames)
	{
		m_filePath = tabbedFile;
		m_headerNames = null;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, true);
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parent.setLayout(parentLayout);
		
		TableViewer table = new TableViewer(parent, SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL);
		
		TabbedFileParser parser = new TabbedFileParser(m_filePath, COMMENT);
		ArrayList<String[]> m_tabbedModel = parser.getTabbedText();
		int columns = parser.getLongestLength();
		
		for (int i = 0; i < columns; i++)
		{
			TableColumn column = new TableColumn(table.getTable(), SWT.LEFT);
			column.setText(String.valueOf(i));
			column.setWidth(350);
			column.setResizable(true);
		}
		if (m_headerNames != null)
		{
			table.setColumnProperties(m_headerNames);
		}
		table.setContentProvider(new TabularContentProvider(columns));
		table.setLabelProvider(new TabularLabelProvider(COMMENT));
		
		GridData viewerData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		table.getTable().setLayoutData(viewerData);
		table.getTable().setLinesVisible(true);
		
		table.setInput(m_tabbedModel);
	}

	@Override
	public void setFocus() {}
}
