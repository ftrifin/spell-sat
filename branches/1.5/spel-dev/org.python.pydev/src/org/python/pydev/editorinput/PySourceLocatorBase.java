package org.python.pydev.editorinput;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.python.pydev.core.REF;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.log.Log;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.codecompletion.revisited.PythonPathHelper;
import org.python.pydev.ui.filetypes.FileTypesPreferencesPage;

/**
 * Refactored from the PydevPlugin: helpers to find some IFile / IEditorInput 
 * from a Path (or java.io.File) 
 * 
 * @author fabioz
 */
public class PySourceLocatorBase {
    
    /**
     * This method will try to find the most likely file that matches the given path,
     * considering:
     * - The workspace files
     * - The open editors
     * 
     * and if all fails, it'll still ask the user which path should be used.
     * 
     * 
     * @param path
     * @return
     */
    public IEditorInput createEditorInput(IPath path) {
        return createEditorInput(path, true);
    }

    
    /**
     * @param file the file we want to get in the workspace
     * @return a workspace file that matches the given file.
     */
    public IFile getWorkspaceFile(File file) {
        IFile[] files = getWorkspaceFiles(file);
        return selectWorkspaceFile(files);
    }

    
    /**
     * @param file the file we want to get in the workspace
     * @return a workspace file that matches the given file.
     */
    public IFile[] getWorkspaceFiles(File file) {
        IWorkspace workspace= ResourcesPlugin.getWorkspace();
        IPath location= Path.fromOSString(file.getAbsolutePath());
        IFile[] files= workspace.getRoot().findFilesForLocation(location);
        files= filterNonExistentFiles(files);
        if (files == null || files.length == 0){
            return null;
        }
        
        return files;
    }
    
    
    
    //---------------------------------- PRIVATE API BELOW --------------------------------------------

    /**
     * Creates the editor input from a given path.
     * 
     * @param path the path for the editor input we're looking
     * @param askIfDoesNotExist if true, it'll try to ask the user/check existing editors and look
     * in the workspace for matches given the name
     * 
     * @return the editor input found or none if None was available for the given path
     */
    private IEditorInput createEditorInput(IPath path, boolean askIfDoesNotExist) {
        String pathTranslation = PySourceLocatorPrefs.getPathTranslation(path);
        if(pathTranslation != null){
            if(!pathTranslation.equals(PySourceLocatorPrefs.DONTASK)){
                //change it for the registered translation
                path = Path.fromOSString(pathTranslation);
            }else{
                //DONTASK!!
                askIfDoesNotExist = false;
            }
        }
        
        IEditorInput edInput = null;
        IWorkspace w = ResourcesPlugin.getWorkspace();      
        
        //let's start with the 'easy' way
        IFile fileForLocation = w.getRoot().getFileForLocation(path);
        if(fileForLocation != null && fileForLocation.exists()){
            return new FileEditorInput(fileForLocation);
        }
        
        IFile files[] = w.getRoot().findFilesForLocation(path);
        if (files == null  || files.length == 0 || !files[0].exists()){
            //it is probably an external file
            File systemFile = path.toFile();
            if(systemFile.exists()){
                edInput = createEditorInput(systemFile);
                
            }else if(askIfDoesNotExist){
                //here we can do one more thing: if the file matches some opened editor, let's use it...
                //(this is done because when debugging, we don't want to be asked over and over
                //for the same file)
                IEditorInput input = getEditorInputFromExistingEditors(systemFile.getName());
                if(input != null){
                    return input;
                }
                
                //this is the last resort... First we'll try to check for a 'good' match,
                //and if there's more than one we'll ask it to the user
                List<IFile> likelyFiles = getLikelyFiles(path, w);
                IFile iFile = selectWorkspaceFile(likelyFiles.toArray(new IFile[0]));
                if(iFile != null){
                    PySourceLocatorPrefs.addPathTranslation(path, iFile.getLocation());
                    return new FileEditorInput(iFile);
                }
                
                //ok, ask the user for any file in the computer
                PydevFileEditorInput pydevFileEditorInput = selectFilesystemFileForPath(path);
                input = pydevFileEditorInput;
                if(input != null){
                    PySourceLocatorPrefs.addPathTranslation(path, pydevFileEditorInput.getPath());
                    return input;
                }
                
                PySourceLocatorPrefs.setIgnorePathTranslation(path);
            }
        }else{ //file exists
            IFile workspaceFile = selectWorkspaceFile(files);
            if(workspaceFile != null){
                edInput = new FileEditorInput(workspaceFile);
            }
        }
        return edInput;
    }

    
    /**
     * @param matchName the name to match in the editor
     * @return an editor input from an existing editor available
     */
    private IEditorInput getEditorInputFromExistingEditors(final String matchName) {
        final Tuple<IWorkbenchWindow, IEditorInput> workbenchAndReturn = new Tuple<IWorkbenchWindow, IEditorInput>(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), null);
        
        Runnable r = new Runnable(){

            public void run() {
                IWorkbenchWindow workbenchWindow = workbenchAndReturn.o1;
                if(workbenchWindow == null){
                    workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                }

                if(workbenchWindow == null){
                    return;
                }

                
                IWorkbenchPage activePage = workbenchWindow.getActivePage();
                if(activePage == null){
                    return;
                }
                
                IEditorReference[] editorReferences = activePage.getEditorReferences();
                for (IEditorReference editorReference : editorReferences) {
                    IEditorPart editor = editorReference.getEditor(false);
                    if(editor != null){
                        if(editor instanceof PyEdit){
                            PyEdit pyEdit = (PyEdit) editor;
                            IEditorInput editorInput = pyEdit.getEditorInput();
                            if(editorInput instanceof IPathEditorInput){
                                IPathEditorInput pathEditorInput = (IPathEditorInput) editorInput;
                                IPath localPath = pathEditorInput.getPath();
                                if(localPath != null){
                                    String considerName = localPath.segment(localPath.segmentCount()-1);
                                    if(matchName.equals(considerName)){
                                        workbenchAndReturn.o2 = editorInput;
                                        return;
                                    }
                                }
                            }else{
                                File editorFile = pyEdit.getEditorFile();
                                if(editorFile != null){
                                    if(editorFile.getName().equals(matchName)){
                                        workbenchAndReturn.o2 = editorInput;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }                        
            }
        };
        
        
        if(workbenchAndReturn.o1 == null){ //not ui-thread
            Display.getDefault().syncExec(r);
        }else{
            r.run();
        }

        

        return workbenchAndReturn.o2;
    }


    /**
     * This is the last resort... pointing to some filesystem file to get the editor for some path.
     */
    protected PydevFileEditorInput selectFilesystemFileForPath(final IPath path) {
        final List<String> l = new ArrayList<String>();
        Runnable r = new Runnable(){

            public void run() {
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                FileDialog dialog = new FileDialog(shell);
                dialog.setText(path+" - select correspondent filesystem file.");
                dialog.setFilterExtensions(FileTypesPreferencesPage.getWildcardValidSourceFiles());
                String string = dialog.open();
                if(string != null){
                    l.add(string);
                }
            }
        };
        if(Display.getCurrent() == null){ //not ui-thread
            Display.getDefault().syncExec(r);
        }else{
            r.run();
        }
        if(l.size() > 0){
            String fileAbsolutePath = REF.getFileAbsolutePath(l.get(0));
            return new PydevFileEditorInput(new File(fileAbsolutePath));
        }
        return null;
    }
    
    /**
     * This method will pass all the files in the workspace and check if there's a file that might
     * be a match to some path (use only as an almost 'last-resort').
     */
    private List<IFile> getLikelyFiles(IPath path, IWorkspace w) {
        List<IFile> ret = new ArrayList<IFile>();
        try {
            IResource[] resources = w.getRoot().members();
            getLikelyFiles(path, ret, resources);
        } catch (CoreException e) {
            Log.log(e);
        }
        return ret;
    }
    
    /**
     * Used to recursively get the likely files given the first set of containers
     */
    private void getLikelyFiles(IPath path, List<IFile> ret, IResource[] resources) throws CoreException {
        String strPath = path.removeFileExtension().lastSegment().toLowerCase(); //this will return something as 'foo'
        
        for (IResource resource : resources) {
            if(resource instanceof IFile){
                IFile f = (IFile) resource;
                
                if(PythonPathHelper.isValidSourceFile(f)){
                    if(resource.getFullPath().removeFileExtension().lastSegment().toLowerCase().equals(strPath)){ 
                        ret.add((IFile) resource);
                    }
                }
            }else if(resource instanceof IContainer){
                getLikelyFiles(path, ret, ((IContainer)resource).members());
            }
        }
    }
    
    /**
     * Creates some editor input for the passed file
     * @param file the file for which an editor input should be created
     * @return the editor input that'll open the passed file.
     */
    private IEditorInput createEditorInput(File file) {
        IFile[] workspaceFile= getWorkspaceFiles(file);
        if (workspaceFile != null && workspaceFile.length > 0){
            IFile file2 = selectWorkspaceFile(workspaceFile);
            if(file2 != null){
                return new FileEditorInput(file2);
            }else{
                return new FileEditorInput(workspaceFile[0]);
            }
        }
        return new PydevFileEditorInput(file);
    }

    
    /**
     * @param files the files that should be filtered
     * @return a new array of IFile with only the files that actually exist.
     */
    private IFile[] filterNonExistentFiles(IFile[] files){
        if (files == null)
            return null;

        int length= files.length;
        ArrayList<IFile> existentFiles= new ArrayList<IFile>(length);
        for (int i= 0; i < length; i++) {
            if (files[i].exists())
                existentFiles.add(files[i]);
        }
        return (IFile[])existentFiles.toArray(new IFile[existentFiles.size()]);
    }
    
    
    /**
     * Ask the user to select one file of the given list of files (if some is available)
     * 
     * @param files the files available for selection.
     * @return the selected file (from the files passed) or null if there was no file available for
     * selection or if the user canceled it.
     */
    private IFile selectWorkspaceFile(final IFile[] files) {
        if(files == null || files.length == 0){
            return null;
        }
        if(files.length == 1){
            return files[0];
        }
        final List<IFile> selected = new ArrayList<IFile>();
        
        Runnable r = new Runnable(){
            public void run() {
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new PyFileLabelProvider());
                dialog.setElements(files);
                dialog.setTitle("Select Workspace File");
                dialog.setMessage("File may be matched to multiple files in the workspace.");
                if (dialog.open() == Window.OK){
                    selected.add((IFile) dialog.getFirstResult());
                }
            }
            
        };
        if(Display.getCurrent() == null){ //not ui-thread
            Display.getDefault().syncExec(r);
        }else{
            r.run();
        }
        if(selected.size() > 0){
            return selected.get(0);
        }
        return null;
    }
}
