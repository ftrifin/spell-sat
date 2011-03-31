package org.python.pydev.ui.actions.container;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.plugin.PydevPlugin;

/**
 * Action used to delete the .pyc and $py.class files (generated from the python or jython interpreter).
 *  
 * @author Fabio
 */
public class PyDeletePycAndClassFiles extends PyContainerAction implements IObjectActionDelegate {
    

    /**
     * Deletes the files... recursively pass the folders and delete the files (and sum them so that we know how many
     * files were deleted).
     * 
     * @param container the folder from where we want to remove the files
     * @return the number of files deleted
     */
    protected int doActionOnContainer(IContainer container) {
        IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
        
        int deleted = 0;
        try{
            IResource[] members = container.members();
            
            for (IResource c:members) {
                if(c instanceof IContainer){
                    deleted += this.doActionOnContainer((IContainer) c);
                    
                }else if(c instanceof IFile){
                    String name = c.getName();
                    if(name != null){
                        if(name.endsWith(".pyc") || name.endsWith(".pyo") || name.endsWith("$py.class")){
                            c.delete(true, nullProgressMonitor);
                            deleted += 1;
                        }
                    }
                }
            }
        } catch (CoreException e) {
            PydevPlugin.log(e);
        }
            
        return deleted;
    }

    @Override
    protected void afterRun(int deleted) {
        MessageDialog.openInformation(null, "Files deleted", StringUtils.format("Deleted %s files.", deleted));
    }

    @Override
    protected boolean confirmRun() {
        return MessageDialog.openConfirm(null, "Confirm deletion", "Are you sure that you want to delete the *.pyc and *$py.class files from the selected folder(s)?");
    }





}
