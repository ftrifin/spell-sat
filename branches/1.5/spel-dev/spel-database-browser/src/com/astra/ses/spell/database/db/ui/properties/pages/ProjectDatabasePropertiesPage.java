///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.database.db.ui.properties.pages
// 
// FILE      : ProjectDatabasePropertiesPage.java
//
// DATE      : 2009-09-14
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
package com.astra.ses.spell.database.db.ui.properties.pages;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import com.astra.ses.spell.dev.database.DatabaseDriverManager;
import com.astra.ses.spell.dev.database.DatabaseManager;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabaseDriver;
import com.astra.ses.spell.dev.database.properties.DatabasePropertiesManager;

/*******************************************************************************
 * Database properties page
 * @author jpizar
 ******************************************************************************/
public class ProjectDatabasePropertiesPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	/** Page ID */
	public static final String PAGE_ID = "com.astra.ses.spell.database.db.ui.property.pages";
	
	/** Driver selection combo */
	private Combo m_driverCombo;
	/** Text Selection combo */
	private Text m_path;
	
	public ProjectDatabasePropertiesPage() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Control createContents(Composite parent) {
		/* Get project for this page */
		IProject project = (IProject) getElement();
		DatabasePropertiesManager manager = new DatabasePropertiesManager(project);
		String currentDriver = manager.getDatabaseDriverName();
		String currentPath = manager.getDatabasePath();
		/* Layout this page */
		GridLayout layout = new GridLayout(3, false);
		parent.setLayout(layout);

		/* Database driver selection */
    	GridData driverData = new GridData(GridData.FILL_HORIZONTAL);
    	driverData.grabExcessHorizontalSpace = true;
    	driverData.horizontalSpan = 2;
    	
		Label driverLabel = new Label(parent, SWT.NONE);
		driverLabel.setText("Database Driver");
		
		m_driverCombo = new Combo(parent, SWT.READ_ONLY);
		Collection<String> drivers = 
			DatabaseDriverManager.getInstance().getDriversNames();
		int selection = 0;
		int i = 0;
		for (String driver : drivers)
		{
			m_driverCombo.add(driver);
			if (driver.equals(currentDriver))
			{
				selection = i;
			}
			i++;
		}
		m_driverCombo.select(selection);
		m_driverCombo.setLayoutData(driverData);
		m_driverCombo.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}

			@Override
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}
		});
		
		/* Database Path */
		GridData pathData = 
			new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.BEGINNING);
		
		Label pathLabel = new Label(parent, SWT.NONE);
		pathLabel.setText("Database Path");
		
		m_path = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		m_path.setText(currentPath);
		m_path.setLayoutData(pathData);
		
		Button dirButton = new Button(parent, SWT.PUSH);
		dirButton.setText("Browse");
		dirButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(new Shell(),SWT.ON_TOP);
				dialog.open();
				String path = dialog.getFilterPath();
				m_path.setText(path);
				validatePage();
			}
		});
		
		validatePage();
		return null;
	}
	
	@Override
	public boolean performOk()
	{
		String path = m_path.getText();
		boolean valid = validatePage();
		if (valid)
		{
			IProject project = (IProject) getElement();
			DatabasePropertiesManager manager = new DatabasePropertiesManager(project);
				
			manager.setDatabasePath(path);
			
			String driver = m_driverCombo.getItem(m_driverCombo.getSelectionIndex());
			manager.setDatabaseDriver(driver);
			
			DatabaseManager.getInstance().refreshProjectDatabase(project);
		}
		// Although values are not consistent, we allow to exit.
		// But changes are not made
		return true;
	}
	
	@Override
	public void performDefaults()
	{
		IProject project = (IProject) getElement();
		DatabasePropertiesManager manager = new DatabasePropertiesManager(project);
		manager.restoreDefaults();
		String currentDriver = manager.getDatabaseDriverName();
		String currentPath = manager.getDatabasePath();
		m_path.setText(currentPath);
		int selection = 0;
		int i = 0;
		for (String driver : m_driverCombo.getItems())
		{
			if (driver.equals(currentDriver))
			{
				selection = i;
				break;
			}
			i++;
		}
		m_driverCombo.select(selection);
	}
	
	/***************************************************************************
	 * Check page values are correct as one is related to the other
	 **************************************************************************/
	private boolean validatePage()
	{
		ISpellDatabaseDriver selectedDriver = null;
		int selected = m_driverCombo.getSelectionIndex();
		String driverName = m_driverCombo.getItem(selected);
		selectedDriver = DatabaseDriverManager.getInstance().getDriver(driverName);
		
		String newPath = m_path.getText();
		if (!selectedDriver.checkDatabase(newPath))
		{
			setMessage(
					"The specified path does not contain a suitable " + selectedDriver.getName() + " database",
					IMessageProvider.WARNING);
			return false;
		}
		else
		{
			setMessage(null);
			return true;
		}
	}
}
