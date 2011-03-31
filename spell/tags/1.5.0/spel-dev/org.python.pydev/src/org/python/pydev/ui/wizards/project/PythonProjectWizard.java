package org.python.pydev.ui.wizards.project;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.project.PyDevProjectManager;
import org.python.pydev.ui.perspective.PythonPerspectiveFactory;

import com.astra.ses.spell.dev.project.SpellDevProjectManager;

/**
 * Python Project creation wizard
 * 
 * <ul>
 * <li>Asks users information about Python project
 * <li>Launches another thread to create Python project. A progress monitor is shown in UI thread
 * </ul>
 * 
 * TODO: Add a checkbox asking should a skeleton of a Python program generated
 * 
 * @author Mikko Ohtamaa
 */
public class PythonProjectWizard extends Wizard implements INewWizard {

    /**
     * The workbench.
     */
    private IWorkbench workbench;

    /**
     * The current selection.
     */
    protected IStructuredSelection selection;

    public static final String WIZARD_ID = "org.python.pydev.ui.wizards.project.PythonProjectWizard";

    IWizardNewProjectNameAndLocationPage projectPage;

    WizardNewProjectReferencePage referencePage;

    Shell shell;

    /** Target project created by this wizard */
    IProject generatedProject;

    /** Exception throw by generator thread */
    Exception creationThreadException;

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        this.workbench = workbench;
        this.selection = currentSelection;
        initializeDefaultPageImageDescriptor();
        projectPage = createProjectPage();
    }

    /**
     * Creates the project page.
     */
    protected IWizardNewProjectNameAndLocationPage createProjectPage(){
        return new CopiedWizardNewProjectNameAndLocationPage("Setting project properties");
    }

    /**
     * Add wizard pages to the instance
     * 
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        addPage(projectPage);
        /*
         * As we don't want to relate some projects to others,
         * Project reference page is unnecesary 
         */
        //addProjectReferencePage();
    }

    /**
     * Adds the project references page to the wizard.
     */
    protected void addProjectReferencePage(){
        // only add page if there are already projects in the workspace
        if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
            referencePage = new WizardNewProjectReferencePage("Reference Page");
            referencePage.setTitle("Reference page");
            referencePage.setDescription("Select referenced projects");
            this.addPage(referencePage);
        }
    }

    /**
     * Creates a project resource given the project handle and description.
     * 
     * @param description the project description to create a project resource for
     * @param projectHandle the project handle to create a project resource for
     * @param monitor the progress monitor to show visual progress with
     * @param projectType
     * @param projectInterpreter 
     * 
     * @exception CoreException if the operation fails
     * @exception OperationCanceledException if the operation is canceled
     */
    private void createProject(IProjectDescription description, IProject project, IProgressMonitor monitor, 
            String projectType, String projectInterpreter) 
    throws CoreException, OperationCanceledException {
    	
        monitor.beginTask("", 2000);
        project.create(description, new SubProgressMonitor(monitor, 1000));
        if (monitor.isCanceled()){
            throw new OperationCanceledException();
        }
    	try
    	{
            boolean createProjectStructure = projectPage.shouldCreatSourceFolder();
            SpellDevProjectManager.getInstance().createProject(project, monitor, createProjectStructure);
            PyDevProjectManager.getInstance().createProject(project, monitor, projectType, projectInterpreter); 
    	}
    	finally
    	{
    		monitor.done();
    	}
    }

    /**
     * Creates a new project resource with the entered name.
     * 
     * @return the created project resource, or <code>null</code> if the project was not created
     */
    protected IProject createNewProject() {
        // get a project handle
        final IProject newProjectHandle = projectPage.getProjectHandle();

        // get a project descriptor
        IPath defaultPath = Platform.getLocation();
        IPath newPath = projectPage.getLocationPath();
        if (defaultPath.equals(newPath)){
            newPath = null;
        }
        
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
        description.setLocation(newPath);

        // update the referenced project if provided
        if (referencePage != null) {
            IProject[] refProjects = referencePage.getReferencedProjects();
            if (refProjects.length > 0)
                description.setReferencedProjects(refProjects);
        }

        final String projectType = projectPage.getProjectType();
        final String projectInterpreter = projectPage.getProjectInterpreter();
        // define the operation to create a new project
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor) throws CoreException {
                createProject(description, newProjectHandle, monitor, projectType, projectInterpreter);
            }
        };

        // run the operation to create a new project
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof CoreException) {
                if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
                    MessageDialog.openError(getShell(), "IDEWorkbenchMessages.CreateProjectWizard_errorTitle", "IDEWorkbenchMessages.CreateProjectWizard_caseVariantExistsError");
                } else {
                    ErrorDialog.openError(getShell(), "IDEWorkbenchMessages.CreateProjectWizard_errorTitle", null, ((CoreException) t).getStatus());
                }
            } else {
                // Unexpected runtime exceptions and errors may still occur.
                PydevPlugin.log(IStatus.ERROR, t.toString(), t);
                MessageDialog.openError(getShell(), "IDEWorkbenchMessages.CreateProjectWizard_errorTitle", t.getMessage());
            }
            return null;
        }

        return newProjectHandle;
    }

    /**
     * The user clicked Finish button
     * 
     * Launches another thread to create Python project. A progress monitor is shown in the UI thread.
     */
    public boolean performFinish() {
        createNewProject();

        // Switch to default perspective
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

        try {
            workbench.showPerspective(PythonPerspectiveFactory.PERSPECTIVE_ID, window);
        } catch (WorkbenchException we) {
            we.printStackTrace();
        }

        // TODO: If initial program skeleton is generated, open default file
        /*
         * if(generatedProject != null) { IFile defaultFile = generatedProject.getFile(new Path("__init__.py")); try { window.getActivePage().openEditor(new FileEditorInput(defaultFile),
         * PyDevPlugin.EDITOR_ID); } catch(CoreException ce) { ce.printStackTrace(); } }
         */

        return true;
    }

    /**
     * Set Python logo to top bar
     */
    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = PydevPlugin.imageDescriptorFromPlugin(PydevPlugin.getPluginID(), "icons/spellWizardHeader.png");//$NON-NLS-1$
        setDefaultPageImageDescriptor(desc);
    }
}
