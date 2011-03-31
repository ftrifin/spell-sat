///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.tabbed.ui.editor
// 
// FILE      : TabularEditorInput.java
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
package com.astra.ses.spell.tabbed.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.astra.ses.spell.parser.TabbedFileParser;


public abstract class TabularEditorInput implements IEditorInput {

	/**************************************************************************
	 * Objects interested in being notified for input changes must implement
	 * this interface
	 * @author jpizar
	 *************************************************************************/
	public interface ITableInputChangeListener
	{
		/**********************************************************************
		 * A row has been added at the specified position
		 * @param position
		 *********************************************************************/
		public void rowAdded(int position);
		/**********************************************************************
		 * Row at specified position has been removed
		 * @param position
		 *********************************************************************/
		public void rowRemoved(int position);
		/**********************************************************************
		 * The given row has changed
		 * @param position
		 *********************************************************************/
		public void rowChanged(int position);
	}
	
	/* Comment lines starts with this character */
	public final static String COMMENT = "#";
	/** Tabbed model */
	private ArrayList<String[]> m_tabbedModel;
	/** Input file */
	private File m_inputFile;
	/** Part name */
	private String m_name;
	/** Input objects listeners */
	private ArrayList<ITableInputChangeListener> m_listeners;
	
	/**************************************************************************
	 * Default constructor
	 *************************************************************************/
	public TabularEditorInput(File input)
	{
		m_inputFile = input;
		TabbedFileParser parser = new TabbedFileParser(input.getAbsolutePath(), COMMENT);
		m_tabbedModel = parser.getTabbedText();
		m_name = input.getName();
		m_listeners = new ArrayList<ITableInputChangeListener>();
	}
	
	/**************************************************************************
	 * Get number of columns to show in the editor
	 * @return
	 *************************************************************************/
	public abstract int getColumnCount();
	
	/**************************************************************************
	 * Return number of rows inside the table
	 * @return
	 *************************************************************************/
	public int getRowCount()
	{
		return m_tabbedModel.size();
	}
	
	/**************************************************************************
	 * Get column names
	 * @return
	 *************************************************************************/
	public abstract String[] getColumnNames();

	/**************************************************************************
	 * Get tabbed text
	 * @return
	 *************************************************************************/
	public ArrayList<String[]> getTabbedTextModel()
	{
		return m_tabbedModel;
	}
	
	/**************************************************************************
	 * Get the file where content is saved
	 *************************************************************************/
	public String getFilePath()
	{
		return m_inputFile.getAbsolutePath();
	}
	
	/**************************************************************************
	 * Get this editor name
	 *************************************************************************/
	public String getName()
	{
		return m_name;
	}
	
	/**************************************************************************
	 * register a listener for this input object
	 * @param listener
	 *************************************************************************/
	public void registerInputListener(ITableInputChangeListener listener)
	{
		m_listeners.add(listener);
	}
	
	/**************************************************************************
	 * Register a listener for this input's object
	 * @param listener
	 *************************************************************************/
	public void unregisterInputListener(ITableInputChangeListener listener)
	{
		m_listeners.remove(listener);
	}
	
	/**************************************************************************
	 * Change the value
	 *************************************************************************/
	public void setValue(int row, int column, String value)
	{
		String[] line = m_tabbedModel.get(row);
		line[column] = value;
		for (ITableInputChangeListener listener : m_listeners)
		{
			listener.rowChanged(row);
		}
	}
	
	/**************************************************************************
	 * Append a blank line to the model
	 * @param row
	 *************************************************************************/
	public void addBlankLine(int row)
	{
		String[] content = new String[getColumnCount()];
		Arrays.fill(content, "");
		m_tabbedModel.add(row, content);
		for (ITableInputChangeListener listener : m_listeners)
		{
			listener.rowAdded(row);
		}
	}
	
	/**************************************************************************
	 * Remove the given row
	 * @param row
	 **************************************************************************/
	public void removeLine(int row)
	{
		m_tabbedModel.remove(row);
		for (ITableInputChangeListener listener : m_listeners)
		{
			listener.rowRemoved(row);
		}
	}
	

	@Override
	public boolean exists() {
		return m_inputFile.exists();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Tabular Editor Input";
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
}