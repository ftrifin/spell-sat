/*
 * Created on Jan 17, 2006
 */
package org.python.pydev.ui.wizards.files;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.python.pydev.project.PyDevProjectManager;


public class PythonPackageWizard  extends AbstractPythonWizard {

    public PythonPackageWizard() {
        super("Create a new Python package");
    }

    public static final String WIZARD_ID = "org.python.pydev.ui.wizards.files.PythonPackageWizard";

    @Override
    protected PythonAbstractPathPage createPathPage() {
        return new PythonAbstractPathPage(this.description, selection){

            @Override
            protected boolean shouldCreatePackageSelect() {
                return false;
            }
            
        };
    }

    /**
     * We will create the complete package path given by the user (all filled with __init__) 
     * and we should return the last __init__ module created.
     */
    @Override
    protected IFile doCreateNew(IProgressMonitor monitor) throws CoreException {
        IContainer validatedSourceFolder = filePage.getValidatedSourceFolder();
        String validatedName = filePage.getValidatedName();
        PyDevProjectManager.getInstance().createPackage(validatedSourceFolder, validatedName, monitor);
        return null;
    }


}
