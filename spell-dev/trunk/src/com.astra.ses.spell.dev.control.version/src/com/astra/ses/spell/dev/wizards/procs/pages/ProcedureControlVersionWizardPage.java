/////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards.pages
// 
// FILE      : ProcedureControlVersionWizardPage.java
//
// DATE      : Nov 30, 2010 10:57:15 AM
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
// SUBPROJECT: SPELL DEV
//
/////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.wizards.procs.pages;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.astra.ses.spell.dev.wizards.extensions.IProcedureCreationExtension;
import com.astra.ses.spell.dev.wizards.procs.job.AddNewProcedureJob;


/*******************************************************************************
 * 
 * {@link ProcedureControlVersionWizardPage} allows to add to control version
 * the file that is being created
 *
 ******************************************************************************/
public class ProcedureControlVersionWizardPage 
	extends WizardPage implements IProcedureCreationExtension
{

	/** Page name */
	private static final String PAGE_NAME = "Version Control Options";
	/** Add to Control Version button */
	private Button m_addButton;
	/** Set svn:needs-lock property for its edition */
	private Button m_needsLockButton;
	/** Auto commti the file after it has been created */
	private Button m_commitButton;
	/** Control version job to perform */
	private IRunnableWithProgress m_controlVersionJob;
	
	/***************************************************************************
	 * Constructor
	 * @param selection
	 **************************************************************************/
	public ProcedureControlVersionWizardPage()
	{
		super(PAGE_NAME);
		//title
		setTitle("Control version information");
		//description
		setDescription("Set control version options for the on-creation procedure");
	}

	/*==========================================================================
	 * (non-Javadoc)
	 * @see IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 =========================================================================*/
	@Override
	public void createControl(Composite parent) {
		 // Main composite
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayout(layout);
		container.setLayoutData(layoutData);
		
		// Control version group
		Group cvGroup = new Group(container, SWT.BORDER);
		cvGroup.setText("Control version options");
		GridLayout groupLayout = GridLayoutFactory.copyLayout(layout);
		cvGroup.setLayout(groupLayout);
		GridData groupData = GridDataFactory.copyData(layoutData);
		cvGroup.setLayoutData(groupData);
		
		// Add to control version button
		m_addButton = new Button(cvGroup, SWT.CHECK);
		m_addButton.setText("Add this file to version control after it has been created");
		m_addButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button add = (Button) e.widget;
				boolean selected  = add.getSelection();
				m_commitButton.setEnabled(selected);
				m_needsLockButton.setEnabled(selected);
			}
		});
		
		// Commit button
		m_commitButton = new Button(cvGroup, SWT.CHECK);
		m_commitButton.setText("Commit this file after its creation");
		
		Group propertiesGroup = new Group(container, SWT.BORDER);
		propertiesGroup.setText("Control version options");
		GridLayout propLayout = GridLayoutFactory.copyLayout(groupLayout);
		propertiesGroup.setLayout(propLayout);
		GridData propData = new GridData(GridData.FILL_HORIZONTAL);
		propData.grabExcessHorizontalSpace = true;
		propertiesGroup.setLayoutData(propData);
		
		// Needs lock property button
		m_needsLockButton = new Button(propertiesGroup, SWT.CHECK);
		m_needsLockButton.setText("This file requires to be locked for its edition");
		
		Label descLabel = new Label(propertiesGroup, SWT.NONE);
		descLabel.setText("Version control properties can only be set if the procedure is added.\n" +
				          "Properties setting will only be effective after the procedure has been committed.");
		
		//Initialize the values
		m_addButton.setSelection(false);
		m_needsLockButton.setSelection(false);
		m_commitButton.setSelection(true);		
		m_commitButton.setEnabled(false);
		m_needsLockButton.setEnabled(false);
		
		
		// validate page
		validatePage();
		
		// set Control
		setControl(container);
	}
	
	/***************************************************************************
	 * Check entered information is valid so procedure creation can be performed
	 * @return true if the page contains valid values. Otherwise false is 
	 * returned
	 **************************************************************************/
	public boolean validatePage()
	{
		// Nothing needs to be checked in this page
		return true;
	}
	
	/***************************************************************************
	 * Check the add to repository button selection
	 * @return
	 **************************************************************************/
	public boolean getAddProperty()
	{
		return m_addButton.getSelection();
	}

	/***************************************************************************
	 * Check the needs lock button selection
	 * @return
	 **************************************************************************/
	public boolean getLockProperty()
	{
		return m_needsLockButton.getSelection();
	}
	
	/***************************************************************************
	 * Check the commit button selection
	 * @return
	 **************************************************************************/
	public boolean getCommitProperty()
	{
		return m_commitButton.getSelection();
	}

	/*==========================================================================
	 * (non-Javadoc)
	 * @see FileCreationPage#performActions(org.eclipse.core.resources.IFile)
	 =========================================================================*/
	@Override
	public void performActions() 
	{
		if (m_controlVersionJob == null) return;
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
		try
		{
			dialog.setCancelable(true);
			dialog.run(false, true, m_controlVersionJob);
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/*==========================================================================
	 * (non-Javadoc)
	 * @see IProcedureCreationExtension#init(org.eclipse.core.resources.IFile)
	 =========================================================================*/
	@Override
	public IRunnableWithProgress prepareJob(IFile procedure) 
	{
		boolean add = getAddProperty();
		boolean lock = getLockProperty();
		boolean commit = getCommitProperty();
		
		if (!add) return null;
		
		return new AddNewProcedureJob(procedure, commit, lock);
	}
}