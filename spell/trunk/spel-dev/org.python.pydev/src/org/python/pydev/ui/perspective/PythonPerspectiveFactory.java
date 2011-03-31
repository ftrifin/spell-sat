package org.python.pydev.ui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.progress.IProgressConstants;
import org.python.pydev.ui.wizards.files.PythonModuleWizard;
import org.python.pydev.ui.wizards.files.PythonPackageWizard;
import org.python.pydev.ui.wizards.files.PythonSourceFolderWizard;
import org.python.pydev.ui.wizards.project.PythonProjectWizard;

/**
 * Python perspective constructor
 * 
 * @author Mikko Ohtamaa
 */
public class PythonPerspectiveFactory implements IPerspectiveFactory {
    
    public static final String PERSPECTIVE_ID = "org.python.pydev.ui.PythonPerspective";

    /**
     * Creates Python perspective layout
     * 
     * Copied from org.eclipse.jdt.internal.ui.JavaPerspectiveFactory
     */
    public void createInitialLayout(IPageLayout layout) {
        defineLayout(layout);
        defineActions(layout);
    }
    
    /**
     * @param layout
     * @param editorArea
     */
    public void defineLayout(IPageLayout layout) {
        IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, (float)0.25, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$    
        topLeft.addView("org.python.pydev.navigator.view");
        topLeft.addView(IPageLayout.ID_OUTLINE);

        IFolderLayout outputfolder= layout.createFolder("bottomRest", IPageLayout.BOTTOM, (float)0.75, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);        
        outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
    }

    /**
     * @param layout
     */
    public void defineActions(IPageLayout layout) {
        layout.addNewWizardShortcut(PythonProjectWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonSourceFolderWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonPackageWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut(PythonModuleWizard.WIZARD_ID); //$NON-NLS-1$        
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
    }

}
