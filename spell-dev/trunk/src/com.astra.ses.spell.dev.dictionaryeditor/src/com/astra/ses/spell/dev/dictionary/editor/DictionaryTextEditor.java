///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.dictionary.editor
// 
// FILE      : DictionaryTextEditor.java
//
// DATE      : 2010-07-06
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
package com.astra.ses.spell.dev.dictionary.editor;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewerExtension7;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.astra.ses.spell.dev.dictionary.editor.configuration.DictionaryEditorConfiguration;


/*******************************************************************************
 * DatabaseTextEditor extends default text editor adding syntax highlighting 
 *
 ******************************************************************************/
public class DictionaryTextEditor extends TextEditor {

	/* Tab max length */
	private static final int TAB_LEN = 32;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public DictionaryTextEditor() {
		super();
		setSourceViewerConfiguration(new DictionaryEditorConfiguration());	
	}

	@Override
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);
		/* Add auto indent strategy */
		ISourceViewer viewer = getSourceViewer();
		/* Set tabs to spaces converter */
		ITextViewerExtension7 ext7 = (ITextViewerExtension7) viewer;
		ext7.setTabsToSpacesConverter(new DictionaryFileIndentStrategy(TAB_LEN));
		/* Set tab positions */
		StyledText widget = viewer.getTextWidget();
		widget.setTabs(TAB_LEN);
	}
	
	@Override
	protected void doSetInput(IEditorInput input)
	{
		try {
			super.doSetInput(input);
			//formatDocument();
			doSave(new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void doSave(IProgressMonitor progressMonitor)
	{
		//formatDocument();
		super.doSave(progressMonitor);
	}
	
	/***************************************************************************
	 * Format document
	 **************************************************************************/
	public void formatDocument()
	{
		IDocumentProvider provider = getDocumentProvider();
		IDocument doc = provider.getDocument(getEditorInput());
		int lineCount = doc.getNumberOfLines();
		for (int i = 0; i < lineCount; i++)
		{
			try {
				int offset = doc.getLineOffset(i);
				int length = doc.getLineLength(i);
				String lineText = doc.get(offset, length);
				String converted = convertLine(lineText);
				if (converted != null)
				{
					doc.replace(offset, length, converted);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/***************************************************************************
	 * Format line
	 * @return
	 **************************************************************************/
	private String convertLine(String line)
	{
		if (line.isEmpty() || line.equals("\n"))
		{
			return null;
		}
		if (line.charAt(0) == '#') // Comment line
		{
			return null;
		}
		else // Key value line
		{
			String[] splitted = line.split("[ ]+", 2);
			String newLine = splitted[0];
			if (splitted.length > 1)
			{
				int keyLength = newLine.length() % TAB_LEN;
				int toFill = TAB_LEN - keyLength;
				char[] blanks = new char[toFill];
				Arrays.fill(blanks, ' ');
				newLine += new String(blanks);
				newLine += splitted[1];
			}
			return newLine;
		}
	}
}