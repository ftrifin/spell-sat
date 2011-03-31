////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.preferences
// 
// FILE      : CodeFoldingPage.java
//
// DATE      : Nov 18, 2010 9:54:43 AM
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.python.pydev.editor.codefolding.PyDevCodeFoldingPrefPage;

/*******************************************************************************
 * 
 * {@link CodeFoldingPage} allows user to configure the elements that can be
 * folded inside a SPELL editor
 *
 ******************************************************************************/
public class CodeFoldingPage extends PyDevCodeFoldingPrefPage {

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public CodeFoldingPage() {
		super();
		setDescription("SPELL editor Code folding options");
	}
	
    protected Control createPreferencePage(Composite parent) {
        Composite top = new Composite(parent, SWT.LEFT);

        // Sets the layout data for the top composite's 
        // place in its parent's layout.
        top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // Sets the layout for the top composite's 
        // children to populate.
        top.setLayout(new GridLayout());

        Button master = addCheckBox(top, "Use Code Folding?  -  Will apply to new editors", USE_CODE_FOLDING, 0);

        Group elementsGroup = new Group(top, SWT.BORDER);
        elementsGroup.setText("Elements to fold");
        elementsGroup.setLayout(new GridLayout());
        elementsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label listLabel = new Label(elementsGroup, SWT.NONE|SWT.WRAP);
        listLabel.setText("Select the elements you would like SPELL editor to fold on.\nChanges will be applied when the document is saved");

        Button slaveImport = addCheckBox(elementsGroup, "Fold Imports?", FOLD_IMPORTS, 0);
        
        Button slaveClass = addCheckBox(elementsGroup, "Fold Class Definitions?", FOLD_CLASSDEF, 0);

        Button slaveFunc = addCheckBox(elementsGroup, "Fold Function Definitions?", FOLD_FUNCTIONDEF, 0);
        
        Button slaveString = addCheckBox(elementsGroup, "Fold Multi-line Strings?", FOLD_STRINGS, 0);
        
        Button slaveComment = addCheckBox(elementsGroup, "Fold Comments?", FOLD_COMMENTS, 0);

        Button slaveFor = addCheckBox(elementsGroup, "Fold FOR statments?", FOLD_FOR, 0);

        Button slaveIf = addCheckBox(elementsGroup, "Fold IF statments?", FOLD_IF, 0);

        Button slaveTry = addCheckBox(elementsGroup, "Fold TRY statments?", FOLD_TRY, 0);

        Button slaveWhile = addCheckBox(elementsGroup, "Fold WHILE statments?", FOLD_WHILE, 0);

        Button slaveWith = addCheckBox(elementsGroup, "Fold WITH statments?", FOLD_WITH, 0);

        createDependency(master, USE_CODE_FOLDING, slaveClass);
        createDependency(master, USE_CODE_FOLDING, slaveFunc);
        createDependency(master, USE_CODE_FOLDING, slaveImport);
        createDependency(master, USE_CODE_FOLDING, slaveFor);
        createDependency(master, USE_CODE_FOLDING, slaveIf);
        createDependency(master, USE_CODE_FOLDING, slaveTry);
        createDependency(master, USE_CODE_FOLDING, slaveWhile);
        createDependency(master, USE_CODE_FOLDING, slaveWith);
        createDependency(master, USE_CODE_FOLDING, slaveString);
        createDependency(master, USE_CODE_FOLDING, slaveComment);

        return top;

    }

}
