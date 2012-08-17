///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls
// 
// FILE      : ItemInfoTableLabelProvider.java
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
package com.astra.ses.spell.gui.presentation.code.controls;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;

/***************************************************************************
 * 
 * {@link ItemInfoTableLabelProvider} helps rendering the different item
 * notifications
 * 
 **************************************************************************/
class ItemInfoTableLabelProvider implements ITableLabelProvider,
        ITableColorProvider, IPropertyChangeListener
{
	/** Status colors */
	private Color[]	m_statusColors;

	/***********************************************************************
	 * Constructor
	 **********************************************************************/
	public ItemInfoTableLabelProvider()
	{

		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);

		ItemStatus[] status = ItemStatus.values();
		m_statusColors = new Color[status.length];
		for (ItemStatus st : status)
		{
			m_statusColors[st.ordinal()] = cfg.getStatusColor(st);
		}

		cfg.addPropertyChangeListener(this);
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		ILineData info = (ILineData) element;
		ItemInfoTableColumn column = ItemInfoTableColumn.values()[columnIndex];
		switch (column)
		{
		case EXECUTION:
			String ieg = info.getId().split("-")[0];
			return String.valueOf(Integer.parseInt(ieg) + 1);
		case NAME:
			String name = info.getName();
			if (name.indexOf("@") != -1)
			{
				name = name.split("@")[1];
			}
			return name;
		case VALUE:
			return info.getValue();
		case STATUS:
			return info.getStatus().getName();
		case TIME:
			return info.getTime();
		case COMMENTS:
			return info.getComments();
		default:
			return "";
		}
	}

	@Override
	public Color getBackground(Object element, int columnIndex)
	{
		ILineData info = (ILineData) element;
		if (columnIndex == ItemInfoTableColumn.STATUS.ordinal())
		{
			ItemStatus status = info.getStatus();
			Color bg = m_statusColors[status.ordinal()];
			return bg;
		}
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener)
	{
	}

	@Override
	public void dispose()
	{
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		cfg.removePropertyChangeListener(this);

		for (Color color : m_statusColors)
		{
			color.dispose();
		}
		m_statusColors = null;
	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		/*
		 * If property color has changed, then reload the color
		 */
		if (property.startsWith(PreferenceCategory.STATUS_COLOR.tag))
		{
			/*
			 * We add 1 to the length because the preferences are stored like
			 * tag.status
			 */
			String statusStr = property
			        .substring(PreferenceCategory.STATUS_COLOR.tag.length() + 1);
			ItemStatus st = ItemStatus.valueOf(statusStr);

			ConfigurationManager cfg = (ConfigurationManager) ServiceManager
			        .get(ConfigurationManager.ID);

			Color old = m_statusColors[st.ordinal()];
			m_statusColors[st.ordinal()] = cfg.getStatusColor(st);
			old.dispose();
		}
	}
}
