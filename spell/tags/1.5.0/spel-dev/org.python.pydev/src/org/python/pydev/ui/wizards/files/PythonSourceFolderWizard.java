/*
 * Created on Jan 22, 2006
 */
package org.python.pydev.ui.wizards.files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.python.pydev.project.PyDevProjectManager;

public class PythonSourceFolderWizard extends AbstractPythonWizard {

    public PythonSourceFolderWizard() {
        super("Create a new Source Folder");
    }

    public static final String WIZARD_ID = "org.python.pydev.ui.wizards.files.PythonSourceFolderWizard";

    @Override
    protected PythonAbstractPathPage createPathPage() {
        return new PythonAbstractPathPage(this.description, selection){

            @Override
            protected boolean shouldCreateSourceFolderSelect() {
                return false;
            }
            
            @Override
            protected boolean shouldCreatePackageSelect() {
                return false;
            }
            
        };
    }

    @Override
    protected IFile doCreateNew(IProgressMonitor monitor) throws CoreException {
        IProject project = filePage.getValidatedProject();
        String name = filePage.getValidatedName();
        PyDevProjectManager.getInstance().createSourceFolder(project, name, monitor);
        return null;
    }


}
