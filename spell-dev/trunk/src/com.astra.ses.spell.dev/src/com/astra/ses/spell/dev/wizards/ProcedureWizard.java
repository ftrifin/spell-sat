/////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards
// 
// FILE      : ProcedureWizard.java
//
// DATE      : Nov 30, 2010 12:14:22 PM
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
package com.astra.ses.spell.dev.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

import com.astra.ses.spell.dev.wizards.extensions.IProcedureCreationExtension;
import com.astra.ses.spell.dev.wizards.pages.ProcedureInfoWizardPage;

/******************************************************************************
 * 
 * ProcedureWizard handles new procedures creation
 *
 *****************************************************************************/
public class ProcedureWizard extends Wizard implements INewWizard {

	/** WIZARD ID */
	public static final String WIZARD_ID = "com.astra.ses.spell.dev.wizard.newprocedure";
	
	/** Procedure info page */
	private ProcedureInfoWizardPage m_infoPage;
	/** Workbench */
	private IWorkbench m_workbench;
	/** Procedure creation extensions */
	private IProcedureCreationExtension[] m_procedureExtensions;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		m_workbench = workbench;
		loadExtensions();
		createWizardPages(selection);
	}
	
	@Override
	public boolean performFinish() {
		Path path = m_infoPage.getAbsoluteFilePath();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		boolean result = false;
		try {
			final IFile file = root.getFile(path);
			InputStream header = getSourceCodeHeader();
			file.create(header, true, null);
            
            /*
             * Perform extended actions
             */
			final ArrayList<IRunnableWithProgress> runnables = 
				new ArrayList<IRunnableWithProgress>();
		    for (IProcedureCreationExtension extension : m_procedureExtensions)
		    {
		    	IRunnableWithProgress job = extension.prepareJob(file);
		    	if (job != null)
		    	{
		    		runnables.add(job);
		    	}
		    }     
		    Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
					try
					{
						dialog.setCancelable(true);
					    for (IRunnableWithProgress job : runnables)
					    {
					    	dialog.run(false, true, job);
					    } 
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
			});

    		
			/*
			 * Open the editor if possible
			 */
			IWorkbenchWindow dw = m_workbench.getActiveWorkbenchWindow();
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
    		
			result = true;
		} 
		catch (CoreException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/***************************************************************************
	 * Load extensions
	 **************************************************************************/
	private void loadExtensions()
	{
		String id = IProcedureCreationExtension.EXTENSION_ID;
		Vector<IProcedureCreationExtension> procExtensions = 
							new Vector<IProcedureCreationExtension>();
		System.out.println("[*] Loading extensions for point '" + id + "'");
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(id);
		IExtension[] extensions = ep.getExtensions();
		for(IExtension extension : extensions)
		{
			// Obtain the configuration element for this extension point
			for (IConfigurationElement cfgElem : extension.getConfigurationElements())
			{
				String elementName  = cfgElem.getAttribute("id");
				try
				{
					IProcedureCreationExtension extensionInterface = 
						(IProcedureCreationExtension) IProcedureCreationExtension.class.cast(cfgElem.createExecutableExtension("page"));
					procExtensions.add(extensionInterface);
					System.out.println("[*] Extension loaded: " + elementName);
				}
				catch(CoreException ex)
				{
					ex.printStackTrace();
				}
			}
		}
		
		m_procedureExtensions = new IProcedureCreationExtension[procExtensions.size()];
		procExtensions.toArray(m_procedureExtensions);
	}
	
	/***************************************************************************
	 * Fill this wizard with its wizard pages
	 **************************************************************************/
	private void createWizardPages(IStructuredSelection selection)
	{
		m_infoPage = new ProcedureInfoWizardPage(selection);
		addPage(m_infoPage);
		for (IProcedureCreationExtension extension : m_procedureExtensions)
		{
			if (extension instanceof IWizardPage)
			{
				addPage((IWizardPage) extension);
			}
		}
	}

	/***************************************************************************
	 * Get the procedure source code header
	 * @return an {@link InputStream} instance containing the source code header
	 **************************************************************************/
	private InputStream getSourceCodeHeader()
	{
		String contents = m_infoPage.getCodeHeader();
		return new ByteArrayInputStream(contents.getBytes());
	}
}