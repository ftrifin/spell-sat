///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor
// 
// FILE      : SpellEditor.java
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.spelleditor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.ITextEditorDropTargetListener;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.plugin.preferences.PydevPrefs;
import org.python.pydev.ui.ColorCache;

import com.astra.ses.spell.dev.spelleditor.dnd.SpellEditorDropTargetListener;
import com.astra.ses.spell.dev.spelleditor.model.SpellEditorConfiguration;

/*******************************************************************************
 * 
 * SpellEditor extends PYEdit adding new features related with the SPELL
 * environment
 * @author jpizar
 *
 ******************************************************************************/
public class SpellEditor extends PyEdit {

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public SpellEditor()
	{
		super();
		
		IPreferenceStore pyPrefs = PydevPrefs.getChainedPrefStore();
		ColorCache colorCache = new ColorCache(PydevPrefs.getChainedPrefStore());
		setSourceViewerConfiguration(new SpellEditorConfiguration(colorCache, this, pyPrefs));
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles)
	{
		ISourceViewer viewer = super.createSourceViewer(parent, ruler, styles);
		initializeDragAndDrop(viewer);
		return viewer;
	}
	
    @Override
    public Object getAdapter(Class adapter) {
    	if (ITextEditorDropTargetListener.class.equals(adapter))
        {
        	return new SpellEditorDropTargetListener(getSourceViewer());
        }
        return super.getAdapter(adapter);
    }
}
