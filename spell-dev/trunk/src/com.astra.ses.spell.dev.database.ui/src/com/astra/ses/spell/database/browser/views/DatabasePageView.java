///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.database.browser.views
//
// FILE      : DatabasePageView.java
//
// DATE      : Feb 18, 2011
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.database.browser.views;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import com.astra.ses.spell.dev.database.DatabaseManager;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.listener.IWorkingDatabaseListener;
import com.astra.ses.spell.dev.spelleditor.utils.SpellEditorInfo;

/**************************************************************************
 * Base class for procedure page-based views
 *************************************************************************/
public abstract class DatabasePageView extends PageBookView implements IWorkingDatabaseListener
{
	/** Holds the default empty string */
	private String m_defaultMsg;
	/** Holds the default title */
	private String m_defaultTitle;
	/** Default page */
	private MessagePage m_defaultPage;
	/** Holds the list of created pages (one per project) */
	private Map<String,Page> m_pages;
	
	/***************************************************************************
	 * Constructor.
	 * @param defaultMessage
	 **************************************************************************/
	public DatabasePageView( String defaultMessage, String defaultTitle )
	{
		super();
		m_defaultMsg = defaultMessage;
		m_defaultTitle = defaultTitle;
		m_pages = new TreeMap<String,Page>();
		DatabaseManager.getInstance().addDatabaseListener(this);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public void dispose()
	{
		super.dispose();
		DatabaseManager.getInstance().removeDatabaseListener(this);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	protected IPage createDefaultPage(PageBook book) 
	{
		m_defaultPage = new MessagePage();
		initPage(m_defaultPage);
		m_defaultPage.setMessage(m_defaultMsg);
		m_defaultPage.createControl(book);
		setPartName(m_defaultTitle);
		return m_defaultPage;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	protected abstract Page createMyPage( IWorkbenchPart part );
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) 
	{
		String project = getProjectForPart(part);
		Page page = null;
		if ((project != null)&&(m_pages.containsKey(project)))
		{
			page = m_pages.get(project);
			initPage(page);
		}
		else
		{
			// Create the page
			page = createMyPage( part );
			m_pages.put(project, page);
			initPage(page);
			page.createControl(getPageBook());
		}
		return new PageRec(part, page);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void setFocus()
	{
		super.setFocus();
		getCurrentPage().setFocus();
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) 
	{
		String project = getProjectForPart(part);
		if ((project != null)&&(m_pages.containsKey(project)))
		{
			m_pages.remove(project);
		}
		pageRecord.page.dispose();
		setPartName(m_defaultTitle);
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	protected IWorkbenchPart getBootstrapPart() 
	{
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart result = null;
		if (page != null)
		{
			IWorkbenchPart part = page.getActivePart();
			if (isImportant(part))
			{
				result = part;
			}
			else
			{
				IEditorPart editor = page.getActiveEditor();
				if (editor != null)
				{
					SpellEditorInfo info = (SpellEditorInfo) editor.getAdapter(SpellEditorInfo.class);
					if (info != null)
					{
						result = editor;
					}
				}
			}
		}
		return result;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	protected boolean isImportant(IWorkbenchPart part) 
	{
		if (part == null) return false;
		SpellEditorInfo info = (SpellEditorInfo) part.getAdapter(SpellEditorInfo.class);
		if (info != null)
		{
			return true;
		}
		return false;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    protected void showPageRec(PageRec pageRec) 
	{
        super.showPageRec(pageRec);
        String partName = m_defaultTitle;
        setPartName(partName);
    }

	/**************************************************************************
	 * 
	 * @param part
	 * @return
	 *************************************************************************/
	private String getProjectForPart( IWorkbenchPart part )
	{
		if (part == null) return null;
		SpellEditorInfo info = (SpellEditorInfo) part.getAdapter(SpellEditorInfo.class);
		if (info != null)
		{
			return info.getFile().getProject().getName();
		}
		return null;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public void workingDatabaseChanged(ISpellDatabase db)
	{
		if (db == null) return;
		IPage page = getCurrentPage();
		if ((page != null)&&(page instanceof DatabasePage))
		{
			((DatabasePage)page).update( db );
		}
	}
}