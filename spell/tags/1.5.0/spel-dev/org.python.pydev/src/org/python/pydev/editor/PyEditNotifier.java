package org.python.pydev.editor;

import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.python.pydev.editor.PyEdit.MyResources;
import org.python.pydev.plugin.PydevPlugin;

/**
 * Helper to give notifications for the listeners of the editor.
 * 
 * @author Fabio
 */
public class PyEditNotifier {
    
    private WeakReference<PyEdit> pyEdit;

    public static interface INotifierRunnable{
        public void run(IProgressMonitor monitor);
    }
    
    public PyEditNotifier(PyEdit edit){
        this.pyEdit = new WeakReference<PyEdit>(edit);
    }
    
    /**
     * Notifies listeners that the actions have just been created in the editor.
     */
    public void notifyOnCreateActions(final MyResources resources) {
        final PyEdit edit = pyEdit.get();
        if(edit == null){
            return;
        }
        INotifierRunnable runnable = new INotifierRunnable(){
            public void run(final IProgressMonitor monitor){
                for(IPyEditListener listener : edit.getAllListeners()){
                    try {
                        if(!monitor.isCanceled()){
                            listener.onCreateActions(resources, edit, monitor);
                        }
                    } catch (Exception e) {
                        //must not fail
                        PydevPlugin.log(e);
                    }
                }
            }
        };
        runIt(runnable);
    }

    /**
     * Notifies listeners that the editor has just been saved
     */
    public void notifyOnSave() {
        final PyEdit edit = pyEdit.get();
        if(edit == null){
            return;
        }
        INotifierRunnable runnable = new INotifierRunnable(){
            public void run(IProgressMonitor monitor){
                for(IPyEditListener listener : edit.getAllListeners()){
                    try {
                        if(!monitor.isCanceled()){
                            listener.onSave(edit, monitor);
                        }
                    } catch (Throwable e) {
                        //must not fail
                        PydevPlugin.log(e);
                    }
                }
            }
        };
        runIt(runnable);

    }

    /**
     * Helper function to run the notifications of the editor in a job.
     * 
     * @param runnable the runnable to be run.
     */
    private void runIt(final INotifierRunnable runnable) {
        Job job = new Job("PyEditNotifier"){

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                runnable.run(monitor);
                return Status.OK_STATUS;
            }
            
        };
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
    }

    /**
     * Notifies listeners that the editor has just been disposed
     */
    public void notifyOnDispose() {
        final PyEdit edit = pyEdit.get();
        if(edit == null){
            return;
        }
        
        INotifierRunnable runnable = new INotifierRunnable(){
            public void run(IProgressMonitor monitor){
                for(IPyEditListener listener : edit.getAllListeners()){
                    try {
                        if(!monitor.isCanceled()){
                            listener.onDispose(edit, monitor);
                        }
                    } catch (Throwable e) {
                        //no need to worry... as we're disposing, in shutdown, we may not have access to some classes anymore
                    }
                }
            }
        };
        runIt(runnable);
    }

    /**
     * @param document the document just set
     */
    public void notifyOnSetDocument(final IDocument document) {
        final PyEdit edit = pyEdit.get();
        if(edit == null){
            return;
        }
        INotifierRunnable runnable = new INotifierRunnable(){
            public void run(IProgressMonitor monitor){
                for(IPyEditListener listener : edit.getAllListeners()){
                    try {
                        if(!monitor.isCanceled()){
                            listener.onSetDocument(document, edit, monitor);
                        }
                    } catch (Exception e) {
                        //must not fail
                        PydevPlugin.log(e);
                    }
                }
            }
        };
        runIt(runnable);
    }

    /**
     * Notifies the available listeners that the input has changed for the editor.
     * 
     * @param oldInput the old input of the editor
     * @param input the new input of the editor
     */
    public void notifyInputChanged(final IEditorInput oldInput, final IEditorInput input) {
        final PyEdit edit = pyEdit.get();
        if(edit == null){
            return;
        }
        INotifierRunnable runnable = new INotifierRunnable(){
            public void run(IProgressMonitor monitor){
                for(IPyEditListener listener : edit.getAllListeners()){
                    if(listener instanceof IPyEditListener3){
                        IPyEditListener3 pyEditListener3 = (IPyEditListener3) listener;
                        try {
                            if(!monitor.isCanceled()){
                                pyEditListener3.onInputChanged(edit, oldInput, input, monitor);
                            }
                        } catch (Exception e) {
                            //must not fail
                            PydevPlugin.log(e);
                        }
                    }
                }
            }
        };
        runIt(runnable);
    }

}
