///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.perspectives
// 
// FILE      : Perspective.java
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
package com.astra.ses.spell.gui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.views.MasterView;
import com.astra.ses.spell.gui.views.NavigationView;
import com.astra.ses.spell.gui.views.ProcedureView;
import com.astra.ses.spell.gui.views.StackView;
import com.astra.ses.spell.gui.views.TabbedView;


/*******************************************************************************
 * @brief Unique perspective of the RCP application. Sets up all the views and
 *        folders used by the GUI.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class Perspective implements IPerspectiveFactory {

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	public void createInitialLayout(IPageLayout layout) 
	{
		Logger.debug("Creating initial layout", Level.INIT, this);

		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		// Create the navigation folder. This allows minimizing the view.
		IFolderLayout navigationFolder = layout.createFolder("navigation",
				IPageLayout.LEFT, 0.15f, editorArea);
		// Add the navigation view to the folder
		navigationFolder.addView(NavigationView.ID);
		
		// Create the stack folder.
		IFolderLayout stackFolder = layout.createFolder("stack", IPageLayout.BOTTOM, 0.75f, "navigation");
		// Add the stack view
		stackFolder.addView(StackView.ID);

		// Create the procedure folder
		IFolderLayout procedureFolder = layout.createFolder("procedure",
				IPageLayout.TOP, 0.70f, editorArea);
		// Add the placeholder for procedure views
		procedureFolder.addPlaceholder(ProcedureView.ID + ":*");
		procedureFolder.addPlaceholder(TabbedView.ID + ":*");
		// Add the master view to the procedure folder
		procedureFolder.addView(MasterView.ID);
		
		// Set view properties
		layout.getViewLayout(NavigationView.ID).setCloseable(false);
		layout.getViewLayout(NavigationView.ID).setMoveable(false);
		layout.getViewLayout(StackView.ID).setMoveable(false);
		layout.getViewLayout(StackView.ID).setCloseable(false);
		layout.getViewLayout(MasterView.ID).setCloseable(false);
		layout.getViewLayout(MasterView.ID).setMoveable(false);

		Logger.debug("Layout created", Level.INIT, this);	}
}
