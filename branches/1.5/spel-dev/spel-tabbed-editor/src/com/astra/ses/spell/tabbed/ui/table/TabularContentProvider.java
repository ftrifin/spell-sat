///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.tabbed.ui.table
// 
// FILE      : TabularContentProvider.java
//
// DATE      : 2009-11-23
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.tabbed.ui.table;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.astra.ses.spell.tabbed.ui.editor.TabularEditorInput;
import com.astra.ses.spell.tabbed.ui.editor.TabularEditorInput.ITableInputChangeListener;


	/**************************************************************************
	 * Editor content provider
	 *************************************************************************/
public class TabularContentProvider implements IStructuredContentProvider {
	
		/** Columns that each line might have */
		private int m_columns;	
		/** Input listener */
		private ITableInputChangeListener m_inputListener;
		/** Current input */
		private TabularEditorInput m_currentInput;
		
		/**********************************************************************
		 * Constructor
		 * @param columnCount
		 *********************************************************************/
		public TabularContentProvider(int columnCount)
		{ 
			m_columns = columnCount;
		}
		
		@Override
		public void inputChanged(final Viewer v, Object oldInput, Object newInput) {
			TabularEditorInput old = (TabularEditorInput) oldInput;
			// unregister the listener from the old input
			if (m_inputListener != null)
			{
				old.unregisterInputListener(m_inputListener);
			}
			m_inputListener = new ITableInputChangeListener()
			{
				@Override
				public void rowAdded(int position) {
					v.refresh();
				}

				@Override
				public void rowChanged(int position) {
					v.refresh();
				}

				@Override
				public void rowRemoved(int position) {
					v.refresh();
				}	
			};
			// Store new input and register the listener
			m_currentInput = ((TabularEditorInput) newInput);
			if (m_currentInput != null)
			{
				m_currentInput.registerInputListener(m_inputListener);
			}
			else
			{
				m_inputListener = null;
			}	
		}
		
		@Override
		public void dispose() {
			if ((m_currentInput != null) && (m_inputListener != null))
			{
				m_currentInput.unregisterInputListener(m_inputListener);
			}
			else
			{
				m_inputListener = null;
			}
		}
		
		@Override
		public Object[] getElements(Object parent) {
			TabularEditorInput input = (TabularEditorInput) parent;
			ArrayList<String[]> tabbedModel = (ArrayList<String[]>) input.getTabbedTextModel();
			
			int i = 0;
			for (String[] line : tabbedModel)
			{
				if (line.length < m_columns)
				{
					String[] newLine = Arrays.copyOf((String[]) line, m_columns);
					Arrays.fill(newLine, line.length, newLine.length, "");
					tabbedModel.set(i, newLine);
				}
				i++;
			}
			return tabbedModel.toArray();
		}
}