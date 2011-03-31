///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.ses.astra.spell.database.texteditor.dnd
// 
// FILE      : DropActionsMenu.java
//
// DATE      : 2009-10-06
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
package com.astra.ses.spell.dev.spelleditor.dnd;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.astra.ses.spell.dev.database.interfaces.ITelecommand;
import com.astra.ses.spell.dev.database.interfaces.ITelemetryParameter;
import com.astra.ses.spell.dev.spelleditor.beautifier.EditorCodeBeautifier;
import com.astra.ses.spell.dev.spelleditor.beautifier.ISpellCodeBeautifier;
import com.astra.ses.spell.dev.spelleditor.dnd.droplet.DropletCodeTransformer;
import com.astra.ses.spell.dev.spelleditor.dnd.droplet.IOfflineDatabaseDroplet;
import com.astra.ses.spell.dev.spelleditor.preferences.DropletPreferences;


/*******************************************************************************
 * 
 * DropActionsMenu will present a context menu when droping on a container
 * showing the possible action which can be performed with the selected
 * tm parameters and telecommands
 *
 ******************************************************************************/
public class DropletsMenuFactory {
	
	/** Parent for creating the popup menu */
	private ISourceViewer m_parent;
	/** Droplet preferences */
	private DropletPreferences m_preferences;
	/** Code Generator */
	private IOfflineDatabaseDroplet[] m_droplets;
	/** Code beautifiers */
	private ISpellCodeBeautifier[] m_beautifiers;
	/** Droplet code transformer */
	private DropletCodeTransformer m_transformer;
	
	/***************************************************************************
	 * Constructor
	 * @param parent
	 **************************************************************************/
	public DropletsMenuFactory(ISourceViewer viewer)
	{
		m_parent = viewer;
		m_preferences = new DropletPreferences();
		m_droplets = DropletPreferences.getDroplets();
		m_beautifiers = loadBeautifiers();
		m_transformer = new DropletCodeTransformer();
	}

	/***************************************************************************
	 * Define avaliable actions with the provided selection
	 * @param tmParamCount
	 * @param commandCount
	 **************************************************************************/
	public Menu createMenu(final ITelemetryParameter[] tm, final ITelecommand[] tc)
	{		
		final StyledText text = m_parent.getTextWidget();
		Menu result = new Menu(text.getShell(), SWT.POP_UP);
		for (IOfflineDatabaseDroplet item : m_droplets)
		{
			if (item.isApplicable(tm, tc))
			{
				MenuItem menuItem = new MenuItem(result, SWT.PUSH);
				menuItem.setText(item.getName());
				menuItem.setData(item);
				menuItem.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						IOfflineDatabaseDroplet item = (IOfflineDatabaseDroplet) e.widget.getData();
						String dropletCode = m_preferences.getDropletCode(item);
						String sourceCode = m_transformer.transform(dropletCode, tm, tc);
						// Beautify the code
						for (ISpellCodeBeautifier beauty : m_beautifiers)
						{
							sourceCode = beauty.beautifyCode(sourceCode, 80);
						}
						text.insert(sourceCode);
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {}
				});
			}
		}
		return result;
	}
	
	/***************************************************************************
	 * Load code beautifiers
	 * @return
	 **************************************************************************/
	private ISpellCodeBeautifier[] loadBeautifiers()
	{
		return new ISpellCodeBeautifier[]
		                                {
				new EditorCodeBeautifier()
		                                };
	}
}
