package org.python.pydev.ui.wizards.files;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.python.pydev.project.PyDevProjectManager;


/**
 * Python module creation wizard
 * 
 * TODO: Create initial file content from a comment templates
 * 
 * @author Mikko Ohtamaa
 * 
 */
public class PythonModuleWizard extends AbstractPythonWizard {

    public PythonModuleWizard() {
        super("Create a new procedure");
    }

    public static final String WIZARD_ID = "org.python.pydev.ui.wizards.files.PythonModuleWizard";

    @Override
    protected PythonAbstractPathPage createPathPage() {
        return new PythonAbstractPathPage(this.description, selection){

            @Override
            protected boolean shouldCreatePackageSelect() {
                return true;
            }
            
        };
    }

    /**
     * We will create a new module (file) here given the source folder and the package specified (which
     * are currently validated in the page) 
     * @param monitor 
     * @throws CoreException 
     */
    @Override
    protected IFile doCreateNew(IProgressMonitor monitor) throws CoreException {
        IContainer validatedSourceFolder = filePage.getValidatedSourceFolder();
        if(validatedSourceFolder == null){
            return null;
        }
        IContainer validatedPackage = filePage.getValidatedPackage();
        if(validatedPackage == null){
            return null;
        }
        String validatedName = filePage.getValidatedName();
        
	    return PyDevProjectManager.getInstance().createProcedure(validatedSourceFolder, validatedPackage, validatedName, monitor);
    }




}
