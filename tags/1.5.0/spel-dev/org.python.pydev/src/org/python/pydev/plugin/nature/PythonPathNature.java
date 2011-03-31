/*
 * License: Common Public License v1.0
 * Created on Jun 2, 2005
 * 
 * @author Fabio Zadrozny
 */
package org.python.pydev.plugin.nature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.python.pydev.core.ExtensionHelper;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IModulesManager;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IPythonPathNature;
import org.python.pydev.core.REF;
import org.python.pydev.core.log.Log;
import org.python.pydev.core.structure.FastStringBuffer;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.filetypes.FileTypesPreferencesPage;

/**
 * @author Fabio Zadrozny
 */
public class PythonPathNature implements IPythonPathNature {

    private IProject project;
    private PythonNature nature;


    /**
     * This is the property that has the python path - associated with the project.
     */
    private static QualifiedName projectSourcePathQualifiedName = null;
    static QualifiedName getProjectSourcePathQualifiedName() {
        if(projectSourcePathQualifiedName == null){
            projectSourcePathQualifiedName = new QualifiedName(PydevPlugin.getPluginID(), "PROJECT_SOURCE_PATH");
        }
        return projectSourcePathQualifiedName;
    }
    /**
     * This is the property that has the external python path - associated with the project.
     */
    private static QualifiedName projectExternalSourcePathQualifiedName = null;
    static QualifiedName getProjectExternalSourcePathQualifiedName() {
        if(projectExternalSourcePathQualifiedName == null){
            projectExternalSourcePathQualifiedName = new QualifiedName(PydevPlugin.getPluginID(), "PROJECT_EXTERNAL_SOURCE_PATH");
        }
        return projectExternalSourcePathQualifiedName;
    }


    public void setProject(IProject project, IPythonNature nature){
        this.project = project;
        this.nature = (PythonNature) nature;
        if(project == null){
            this.projectSourcePathSet = null;//empty
        }
    }
    

    /**
     * Returns a list of paths with the complete pythonpath for this nature.
     * 
     * This includes the pythonpath for the project, all the referenced projects and the
     * system.
     */
    public List<String> getCompleteProjectPythonPath(String interpreter, IInterpreterManager manager) {
        IModulesManager projectModulesManager = getProjectModulesManager();
        if(projectModulesManager == null){
            return null;
        }
        return projectModulesManager.getCompletePythonPath(interpreter, manager);
    }
    
    private IModulesManager getProjectModulesManager(){
        if(project == null){
            return null;
        }
        if(nature == null) {
            return null;
        }
        
        if(nature.getAstManager() == null) {
            // AST manager might not be yet available
            // Code completion job is scheduled to be run
            return null;
        }
              
        return nature.getAstManager().getModulesManager();
    }

    /**
     * @return the project pythonpath with complete paths in the filesystem.
     */
    public String getOnlyProjectPythonPathStr() throws CoreException {
        if(project == null){
            return "";
        }
        
        String source = getProjectSourcePath();
        String external = getProjectExternalSourcePath();
        String contributed = getContributedSourcePath();
        
        if(source == null){
            source = "";
        }
        //we have to work on this one to resolve to full files, as what is stored is the position
        //relative to the project location
        String[] strings = source.split("\\|");
        FastStringBuffer buf = new FastStringBuffer();
        for (int i = 0; i < strings.length; i++) {
            if(strings[i].trim().length()>0){
                IPath p = new Path(strings[i]);
                
                if(ResourcesPlugin.getPlugin() == null){
                    //in tests
                    buf.append(strings[i]);
                    buf.append("|");
                    continue;
                }
                
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                
                //try to get relative to the workspace 
                IContainer container = null;
                IResource r = null;
                try {
                    r = root.findMember(p);
                } catch (Exception e) {
                    PydevPlugin.log(e);
                }
                if(r instanceof IContainer){
                    container = (IContainer) r;
                    buf.append(REF.getFileAbsolutePath(container.getLocation().toFile()));
                    buf.append("|");
                
                }else if(r instanceof IFile){ //zip/jar/egg file
                    String extension = r.getFileExtension();
                    if(extension == null || FileTypesPreferencesPage.isValidZipFile("."+extension) == false){
                        PydevPlugin.log("Error: the path "+strings[i]+" is a file but is not a recognized zip file.");
                        
                    }else{
                        buf.append(REF.getFileAbsolutePath(r.getLocation().toFile()));
                        buf.append("|");
                    }
                
                }else{
                    if(root.isSynchronized(IResource.DEPTH_INFINITE)){
                        //if it's synchronized, it really doesn't exist (let's warn about it)
                        //not in workspace?... maybe it was removed, so, do nothing, but let the user know about it
                        Log.log(IStatus.WARNING, "Unable to find the path "+strings[i]+" in the project were it's \n" +
                                "added as a source folder for pydev (project: "+project.getName()+") member:"+r, null);
                    }
                    
                    IPath rootLocation = root.getRawLocation();
                    //still, let's add it there (this'll be cached for later use)
                    buf.append(REF.getFileAbsolutePath(rootLocation.append(strings[i].trim()).toFile()));
                    buf.append("|");
                    
                }
            }
        }
        
        
        if(external == null){
            external = "";
        }
        return buf.append("|").append(external).append("|").append(contributed).toString();
    }


    /**
     * Gets the source path contributed by plugins.
     * 
     * See: http://sourceforge.net/tracker/index.php?func=detail&aid=1988084&group_id=85796&atid=577329
     * 
     * @throws CoreException
     */
    @SuppressWarnings("unchecked")
    private String getContributedSourcePath() throws CoreException {
        FastStringBuffer buff = new FastStringBuffer();
        List<IPythonPathContributor> contributors = ExtensionHelper.getParticipants("org.python.pydev.pydev_pythonpath_contrib");
        for (IPythonPathContributor contributor : contributors) {
            String additionalPythonPath = contributor.getAdditionalPythonPath(project);
            if (additionalPythonPath != null && additionalPythonPath.trim().length() > 0) {
                if (buff.length() > 0){
                    buff.append("|");
                }
                buff.append(additionalPythonPath.trim());
            }
        }
        return buff.toString();
    }

    

    public void setProjectSourcePath(String newSourcePath) throws CoreException {
        synchronized(project){
            projectSourcePathSet = null;
            nature.getStore().setPathProperty(PythonPathNature.getProjectSourcePathQualifiedName(), newSourcePath);
        }
    }

    public void setProjectExternalSourcePath(String newExternalSourcePath) throws CoreException {
        synchronized(project){
            nature.getStore().setPathProperty(PythonPathNature.getProjectExternalSourcePathQualifiedName(), newExternalSourcePath);
        }
    }

    /**
     * Cache for the project source path.
     */
    private Set<String> projectSourcePathSet;
    
    public Set<String> getProjectSourcePathSet() throws CoreException {
        if(project == null){
            return new HashSet<String>();
        }
        if(projectSourcePathSet == null){
            String projectSourcePath = getProjectSourcePath();
            String[] paths = projectSourcePath.split("\\|");
            projectSourcePathSet = new HashSet<String>(Arrays.asList(paths));
        }
        return projectSourcePathSet;
    }
    
    public String getProjectSourcePath() throws CoreException {
        if(project == null){
            return "";
        }
        synchronized(project){
            boolean restore = false;
            String projectSourcePath = nature.getStore().getPathProperty(PythonPathNature.getProjectSourcePathQualifiedName());
            if(projectSourcePath == null){
                //has not been set
                return "";
            }
            //we have to validate it, because as we store the values relative to the workspace, and not to the 
            //project, the path may become invalid (in which case we have to make it compatible again).
            StringBuffer buffer = new StringBuffer();
            String[] paths = projectSourcePath.split("\\|");
            for (String path : paths) {
                if(path.trim().length() > 0){
                    IPath p = new Path(path);
                    if(p.isEmpty()){
                        continue; //go to the next...
                    }
                    IPath projectPath = project.getFullPath();
                    if(projectPath != null && !projectPath.isPrefixOf(p)){
                        p = p.removeFirstSegments(1);
                        p = projectPath.append(p);
                        restore = true;
                    }
                    buffer.append(p.toString());
                    buffer.append("|");
                }
            }
            
            //it was wrong and has just been fixed
            if(restore){
                projectSourcePathSet = null;
                projectSourcePath = buffer.toString();
                setProjectSourcePath(projectSourcePath);
                if(nature != null){
                    //yeap, everything has to be done from scratch, as all the filesystem paths have just
                    //been turned to dust!
                    nature.rebuildPath();
                }
            }
            return projectSourcePath;
        }
    }

    public String getProjectExternalSourcePath() throws CoreException {
        if(project == null){
            return "";
        }
        synchronized(project){
            //no need to validate because those are always 'file-system' related
            String extPath = nature.getStore().getPathProperty(PythonPathNature.getProjectExternalSourcePathQualifiedName());
            if(extPath == null){
                extPath = "";
            }
            return extPath;
        }
    }

}
