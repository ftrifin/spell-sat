///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.advisor
// 
// FILE      : NewWizard.java
//
// DATE      : 2010-05-21
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.advisor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BaseNewWizardMenu;

public class NewWizard extends BaseNewWizardMenu 
{
    private boolean enabled = true;

    /**
     * Creates a new wizard shortcut menu for the IDE.
     * 
     * @param window
     *            the window containing the menu
     */
    public NewWizard(IWorkbenchWindow window) 
    {
        this(window, null);
        
    }
    
    /**
     * Creates a new wizard shortcut menu for the IDE.
     * 
     * @param window
     *            the window containing the menu
     * @param id
     *            the identifier for this contribution item 
     */
    public NewWizard(IWorkbenchWindow window, String id) 
    {
        super(window, id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.BaseNewWizardMenu#addItems(org.eclipse.jface.action.IContributionManager)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addItems(List list) {
    	ArrayList shortCuts= new ArrayList();
    	addShortcuts(shortCuts);
        if (!shortCuts.isEmpty()) 
        {
        	list.addAll(shortCuts);
        }
    }

	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled state of the receiver.
	 * 
	 * @param enabledValue if <code>true</code> the menu is enabled; else
	 * 		it is disabled
	 */
	public void setEnabled(boolean enabledValue) {
		this.enabled = enabledValue;
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseNewWizardMenu#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {
		if (isEnabled()) {
			return super.getContributionItems();
		}
		return new IContributionItem[0];
	}
}
