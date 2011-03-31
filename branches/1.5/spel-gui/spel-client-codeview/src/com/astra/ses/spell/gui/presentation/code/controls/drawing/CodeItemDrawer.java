///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : CodeItemDrawer.java
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
package com.astra.ses.spell.gui.presentation.code.controls.drawing;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.procs.model.ProcedureLine;
import com.astra.ses.spell.gui.procs.model.LineExecutionModel.ItemInfo;
import com.astra.ses.spell.gui.services.ConfigurationManager;


/*******************************************************************************
 * @brief In charge of drawing and formatting the table (proc code) items. 
 * A single instance should be used for all table items.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class CodeItemDrawer implements Listener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	ConfigurationManager s_rsc = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the execution line highlight color */
	private Color m_highlightColor = null;
	/** Holds the item foreground color */
	private Color m_fgColor = null;
	/** Holds the item background color */
	private Color m_bgColor = null;
	/** Handle to the proc viewer */
	private CodeViewer m_viewer = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public CodeItemDrawer(CodeViewer viewer)
	{
		m_viewer = viewer;
		if (s_rsc == null)
		{
			s_rsc = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		}
		if (m_highlightColor == null)
		{
			m_fgColor = s_rsc.getGuiColor("ITEMS");		
			m_bgColor = s_rsc.getProcedureColor(ExecutorStatus.UNINIT);		
			m_highlightColor = s_rsc.getGuiColor("HIGHLIGHT");
		}
	}
	
	/***************************************************************************
	 * Set the item background color
	 * 
	 * @param c
	 * 		Item background color
	 **************************************************************************/
	public void setBackground( Color c )
	{
		m_bgColor = c;
	}

	/***************************************************************************
	 * Handle the item paint event
	 * 
	 * @param event
	 * 		SWT event
	 **************************************************************************/
	public void handleEvent(Event event)
	{
		TableItem item = (TableItem) event.item;
		GC gc = event.gc;
		int itemIndex = item.getParent().indexOf(item);
		int lineNo = itemIndex + 1;
		if ( lineNo != m_viewer.getCurrentLine() )
		{
			if (event.index==CodeViewer.STS_COLUMN)
			{
				paintStatusItem(item,event);
			}
			else
			{
				paintNormalItem(item,event);
			}
		}
		else
		{
			if (event.index==CodeViewer.STS_COLUMN)
			{
				String itext = item.getText(event.index);
				if (itext != null && itext.length()>0 )
				{
					paintStatusItem(item,event);
				}
				else
				{
					paintHighlightedItem(item,event);
				}
			}
			else
			{
				paintHighlightedItem(item,event);
			}
		}
		gc.drawLine(event.x + event.width - 1, event.y, event.x
				+ event.width - 1, event.y + event.height);
	}

	protected void paintStatusItem( TableItem item, Event event )
	{
		ProcedureLine line = (ProcedureLine) item.getData("PROC_LINE");
		int infoElements = line.getNumInfoElements(true);
		int infoSuccess = line.getNumSuccessInfoElements(true);
		ItemStatus status = ItemStatus.UNKNOWN;
		Color bg = null;
		if (infoElements == 0)
		{
			bg = m_viewer.getCurrentBackground();
			event.gc.setBackground(bg);
			event.gc.fillRectangle(event.x, event.y, event.width, event.height);
			return;
		}
		else if (infoElements == infoSuccess) // All info elements are success
		{
			bg = s_rsc.getStatusColor(ItemStatus.SUCCESS);
		}
		else
		{
			/*
			 *  We iterate over all the items to determine which is the most
			 *  prioritaire status to be shown
			 */
			for (ItemInfo info : line.getItemData(true))
			{
				ItemStatus infoStatus = ItemStatus.fromName(info.status);
				if (infoStatus.ordinal() > status.ordinal())
				{
					status = infoStatus;
				}
			}
			bg = s_rsc.getStatusColor(status);
		}
		if (bg == null)
		{
			bg = m_viewer.getCurrentBackground();
		}
		event.gc.setBackground(bg);
		event.gc.fillRectangle(event.x, event.y, event.width, event.height);
		event.gc.setForeground(m_fgColor);
		event.gc.drawRectangle(event.x-1, event.y, event.width, event.height-1);
	}
	
	protected void paintNormalItem( TableItem item, Event event )
	{
		event.gc.setBackground(m_bgColor);
		event.gc.fillRectangle(event.x, event.y, event.width, event.height);
		String itext = item.getText(event.index);
		if (event.index >= CodeViewer.NAME_COLUMN && itext != null && itext.length()>0 )
		{
			event.gc.drawRectangle(event.x-1, event.y, event.width, event.height-1);
		}
	}
	
	protected void paintHighlightedItem( TableItem item, Event event )
	{
		int clientWidth = item.getParent().getClientArea().width;
		event.gc.setBackground(m_highlightColor);
		event.gc.fillRectangle(0, event.y, clientWidth, event.height);
	}
}
