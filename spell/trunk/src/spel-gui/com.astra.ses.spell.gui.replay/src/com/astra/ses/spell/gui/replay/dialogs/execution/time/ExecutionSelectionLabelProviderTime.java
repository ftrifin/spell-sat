///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.dialogs.execution.time
// 
// FILE      : ExecutionSelectionLabelProviderTime.java
//
// DATE      : Jul 2, 2013
//
// Copyright (C) 2008, 2014 SES ENGINEERING, Luxembourg S.A.R.L.
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.replay.dialogs.execution.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.astra.ses.spell.gui.replay.Activator;
import com.astra.ses.spell.gui.replay.dialogs.execution.ExecutionSelectionLeafNode;
import com.astra.ses.spell.gui.replay.dialogs.execution.ExecutionSelectionNode;

public class ExecutionSelectionLabelProviderTime extends LabelProvider implements ITableLabelProvider
{
	private static final DateFormat s_dfMonth = new SimpleDateFormat("MMMMM");
	private static final DateFormat s_dfDay = new SimpleDateFormat("dd EEEEE");
	private static final DateFormat s_dfHour = new SimpleDateFormat("HH:mm:ss");
	
	private Image m_imgCog;
	private Image m_imgFolder;
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutionSelectionLabelProviderTime()
	{
		m_imgCog = Activator.getImageDescriptor("icons/cog.png").createImage();
		m_imgFolder = Activator.getImageDescriptor("icons/folder.png").createImage();
	}
	
	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void dispose()
	{
		m_imgCog.dispose();
		m_imgFolder.dispose();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public Image getColumnImage(Object element, int columnIndex)
    {
		ExecutionSelectionNode node = (ExecutionSelectionNode) element;
		ExecutionSelectionColumnTime column = ExecutionSelectionColumnTime.values()[columnIndex];
		switch(column)
		{
		case YEAR:
			switch(node.getType())
			{
			case ASRUN:
			case DAY_GROUP:
			case MONTH_GROUP:
			case ROOT:
				return null;
			case YEAR_GROUP:
				return m_imgFolder;
			}
		case MONTH:
			switch(node.getType())
			{
			case ASRUN:
			case DAY_GROUP:
			case ROOT:
			case YEAR_GROUP:
				return null;
			case MONTH_GROUP:
				return m_imgFolder;
			}
		case DAY:
			switch(node.getType())
			{
			case MONTH_GROUP:
			case ROOT:
			case YEAR_GROUP:
			case ASRUN:
				return null;
			case DAY_GROUP:
				return m_imgFolder;
			}
		case HOUR:
			return null;
		case PROCEDURE:
			switch(node.getType())
			{
			case ASRUN:
				return m_imgCog;
			case DAY_GROUP:
			case MONTH_GROUP:
			case ROOT:
			case YEAR_GROUP:
				return null;
			}
		case INSTANCE:
			return null;
		}
    	return null;
    }

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public String getColumnText(Object element, int columnIndex)
    {
		ExecutionSelectionNode node = (ExecutionSelectionNode) element;
		ExecutionSelectionColumnTime column = ExecutionSelectionColumnTime.values()[columnIndex];
		switch(column)
		{
		case YEAR:
			switch(node.getType())
			{
			case YEAR_GROUP:
				return Integer.toString(node.getYear());
			default:
				return "";
			}
		case MONTH:
			switch(node.getType())
			{
			case MONTH_GROUP:
				return s_dfMonth.format(node.getDate());
			default:
				return "";
			}
		case DAY:
			switch(node.getType())
			{
			case DAY_GROUP:
				return s_dfDay.format(node.getDate());
			default:
				return "";
			}
		case HOUR:
			switch(node.getType())
			{
			case ASRUN:
				return s_dfHour.format(node.getDate());
			default:
				return "";
			}
		case PROCEDURE:
			switch(node.getType())
			{
			case ASRUN:
				ExecutionSelectionLeafNode lnode = (ExecutionSelectionLeafNode) element;
				return lnode.getProcName();
			default:
				return "";
			}
		case INSTANCE:
			switch(node.getType())
			{
			case ASRUN:
				ExecutionSelectionLeafNode lnode = (ExecutionSelectionLeafNode) element;
				return lnode.getInstanceNum();
			default:
				return "";
			}
		}
    	return element.toString();
    }

}
