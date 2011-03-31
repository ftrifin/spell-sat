///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.perspective
// 
// FILE      : DevelopmentPerspective.java
//
// DATE      : 2010-05-28
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
package com.astra.ses.spell.dev.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.progress.IProgressConstants;

/*******************************************************************************
 * 
 * Procedure development perspective is defined in this class
 ******************************************************************************/
public class DevelopmentPerspective implements IPerspectiveFactory {

	/** Perspective ID */
	public static final String PERSPECTIVE_ID = "com.astra.ses.spell.dev.perspective.development";
	
	@Override
	public void createInitialLayout(IPageLayout layout) 
	{
		defineLayout(layout);
		defineActions(layout);
	}

	/***************************************************************************
	 * Add views to this perspective
	 **************************************************************************/
	public void defineLayout(IPageLayout layout)
	{
		//Add python navigator view
        IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$    
        topLeft.addView("com.astra.ses.spell.dev.views.explorer");
        
		//Add problems view
        IFolderLayout outputfolder= 
        	layout.createFolder("bottomRest", IPageLayout.BOTTOM, (float)0.75, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);        
        outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
        
        //Add outline view
        IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, (float) 0.75, IPageLayout.ID_EDITOR_AREA);
        topRight.addView(IPageLayout.ID_OUTLINE);
	}
	
	/***************************************************************************
	 * Defines actions to add to this perspective
	 * @param layout
	 **************************************************************************/
	public void defineActions(IPageLayout layout) {
		//Subversion action set
		layout.addActionSet("org.eclipse.ui.edit.text.actionSet.presentation");
		//New wizard shortcuts
		layout.addNewWizardShortcut("com.astra.ses.spell.dev.wizard.newproject");
		layout.addNewWizardShortcut("com.astra.ses.spell.dev.wizard.newsourcefolder");
		layout.addNewWizardShortcut("com.astra.ses.spell.dev.wizard.newpackage");
		layout.addNewWizardShortcut("com.astra.ses.spell.dev.wizard.newprocedure");
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
	}
}
