///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : CodeColorProvider.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.presentation.code.controls.drawing;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.GuiColorKey;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewerColumn;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

public class CodeColorProvider implements ITableColorProvider,
        IPropertyChangeListener
{
	/** Reference to the viewer table */
	private Table	               m_table;
	/** Reference to the data provider */
	private IProcedureDataProvider	m_dataProvider;
	/** Holds the highlighting color */
	private Color	               m_highlightColor;
	/** Holds the foreground color */
	private Color	               m_fgColor;
	/** Holds the items status colors */
	private Color[]	               m_statusColors;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public CodeColorProvider(IProcedureDataProvider dataProvider, Table table)
	{
		m_dataProvider = dataProvider;
		m_table = table;

		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		m_highlightColor = cfg.getGuiColor(GuiColorKey.HIGHLIGHT);
		m_fgColor = cfg.getGuiColor(GuiColorKey.ITEMS);

		ItemStatus[] status = ItemStatus.values();
		m_statusColors = new Color[status.length];
		for (ItemStatus st : status)
		{
			m_statusColors[st.ordinal()] = cfg.getStatusColor(st);
		}

		cfg.addPropertyChangeListener(this);
	}

	/***************************************************************************
	 * Get foreground color.
	 **************************************************************************/
	@Override
	public Color getForeground(Object element, int columnIndex)
	{
		return m_fgColor;
	}

	/***************************************************************************
	 * Get background color.
	 **************************************************************************/
	@Override
	public Color getBackground(Object element, int columnIndex)
	{
		TableItem item = (TableItem) element;
		int lineIndex = item.getParent().indexOf(item) + 1;
		int dataLineIndex = -1;
		try
		{
			dataLineIndex = m_dataProvider.getCurrentLine();
		}
		catch (UninitProcedureException e)
		{
		}

		Color result = m_table.getBackground();
		// If we are in the current line, use highlight color instead
		if (lineIndex == dataLineIndex)
		{
			result = m_highlightColor;
		}

		CodeViewerColumn col = CodeViewerColumn.values()[columnIndex];
		switch (col)
		{
		case STATUS:
			if (item.getData(CodeViewer.DATA_STATUS) != null)
			{
				ItemStatus istatus = (ItemStatus) item
				        .getData(CodeViewer.DATA_STATUS);
				result = m_statusColors[istatus.ordinal()];
			}
			break;
		}
		return result;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.equals(GuiColorKey.HIGHLIGHT.getPreferenceName()))
		{
			ConfigurationManager cfg = (ConfigurationManager) ServiceManager
			        .get(ConfigurationManager.ID);

			Color old = m_highlightColor;
			m_highlightColor = cfg.getGuiColor(GuiColorKey.HIGHLIGHT);

			m_table.redraw();

			old.dispose();
		}
		else if (property.equals(GuiColorKey.ITEMS.getPreferenceName()))
		{
			ConfigurationManager cfg = (ConfigurationManager) ServiceManager
			        .get(ConfigurationManager.ID);

			Color old = m_fgColor;
			m_fgColor = cfg.getGuiColor(GuiColorKey.ITEMS);

			m_table.redraw();

			old.dispose();
		}
		else if (property.startsWith(PreferenceCategory.STATUS_COLOR.tag))
		{
			String statusStr = property
			        .substring(PreferenceCategory.STATUS_COLOR.tag.length() + 1);
			ItemStatus st = ItemStatus.fromName(statusStr);

			ConfigurationManager cfg = (ConfigurationManager) ServiceManager
			        .get(ConfigurationManager.ID);

			Color old = m_statusColors[st.ordinal()];
			m_statusColors[st.ordinal()] = cfg.getStatusColor(st);

			m_table.redraw();

			old.dispose();
		}
	}

	/***************************************************************************
	 * Associated table has been disposed, so graphics resources must be
	 * released
	 **************************************************************************/
	public void dispose()
	{
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		cfg.removePropertyChangeListener(this);

		m_highlightColor.dispose();
		m_fgColor.dispose();
		for (Color color : m_statusColors)
		{
			color.dispose();
		}
	}
}
