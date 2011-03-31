///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.model
// 
// FILE      : SpellEditorConfiguration.java
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
// SUBPROJECT: SPELL Dev
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.spelleditor.model;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.PyEditConfiguration;
import org.python.pydev.ui.ColorAndStyleCache;

public class SpellEditorConfiguration extends PyEditConfiguration {

	/** Code Scanner */
	private SpellCodeScanner m_codeScanner;
	/** Color Cache */
	private ColorAndStyleCache m_colorManager;
	
	/****************************************************************************
	 * Constructor
	 * @param colorManager
	 * @param edit
	 * @param preferenceStore
	 ***************************************************************************/
	public SpellEditorConfiguration(ColorAndStyleCache colorManager, PyEdit edit,
			IPreferenceStore preferenceStore) {
		super(colorManager, edit, preferenceStore);
		m_colorManager = colorManager;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
	{
		PresentationReconciler pyReconciler = (PresentationReconciler)
		super.getPresentationReconciler(sourceViewer);
		
		m_codeScanner = new SpellCodeScanner(m_colorManager);
		
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(m_codeScanner);
        pyReconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        pyReconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        
        return pyReconciler;
	}
	
	@Override
    public void updateSyntaxColorAndStyle() {
		m_codeScanner.updateColors();
		super.updateSyntaxColorAndStyle();
    }
}
