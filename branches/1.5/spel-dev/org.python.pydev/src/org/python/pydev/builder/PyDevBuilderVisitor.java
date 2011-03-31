/*
 * Created on Oct 25, 2004
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.python.pydev.core.ICodeCompletionASTManager;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IModulesManager;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceModule;
import org.python.pydev.plugin.nature.PythonNature;

/**
 * Visitors within pydev should be subclasses of this class.
 * 
 * They should be prepared for being reused to, as they are instantiated and reused for visiting many resources.
 * 
 * @author Fabio Zadrozny
 */
public abstract class PyDevBuilderVisitor implements Comparable<PyDevBuilderVisitor>{

    public static final int MAX_TO_VISIT_INFINITE = -1;

    /**
     * identifies the key for the module in the cache
     */
    public static final String MODULE_CACHE = "MODULE_CACHE";

    /**
     * identifies the key for the module name in the cache
     */
    private static final String MODULE_NAME_CACHE = "MODULE_NAME";

    /**
     * The default priority is 5. 
     * 
     * Higher priorities are minor numbers (and vice-versa).
     */
    public static final int PRIORITY_DEFAULT = 5;

    /**
     * Maximum priority is 0
     */
    public static final int PRIORITY_MAX = 0;
    
    /**
     * Minimum priority is 10
     */
    public static final int PRIORITY_MIN = 10;
    
    /**
     * Compares them by priority (they are ordered before visiting by priority, so, this can
     * be useful if some visitor needs to run only after some other visitor was executed).
     */
    public int compareTo(PyDevBuilderVisitor o) {
        int priority = getPriority();
        int otherPriority = o.getPriority();
        if(priority < otherPriority){
            return -1;
        }
        if(otherPriority < priority){
            return 1;
        }
        return 0; //equal
    }
    
    /**
     * @return the priority of this visitor (visitors with higher priority -- 
     * lower numbers -- are visited before)
     */
    protected int getPriority() {
        return PRIORITY_DEFAULT;
    }

    /**
     * This field acts like a memory. 
     * 
     * It is set before a given resource is visited, and is maintained 
     * for each visitor while the same resource is being visited. 
     * 
     * In this way, we can keep from having to recreate some info (such as the ast) each time over and over
     * for each visitor. 
     */
    public HashMap<String, Object> memo;

    /**
     * Constant indicating value in memory to represent a ful build.
     */
    public static final String IS_FULL_BUILD = "IS_FULL_BUILD";

    /**
     * @return whether we are doing a full build right now.
     */
    protected boolean isFullBuild(){
        Boolean b = (Boolean) memo.get(IS_FULL_BUILD);
        if(b == null){
            return false; // we surely will have it set when it is a full build. (the other way around may not be true).
        }
        return b.booleanValue();
    }
    
    /**
     * This method returns the module that is created from the given resource.
     * 
     * It also uses the cache, to see if the module is already available for that.
     * 
     * @param resource the resource we are analyzing
     * @param document the document with the resource contents
     * @return the module that is created by the given resource
     */
    protected SourceModule getSourceModule(IResource resource, IDocument document, IPythonNature nature) {
        SourceModule module = (SourceModule) memo.get(MODULE_CACHE);
        if(module == null){
            module = createSoureModule(resource, document, getModuleName(resource, nature));
            setModuleInCache(module);
        }
        return module;
    }

    /**
     * @param resource
     * @param document
     * @return
     */
    private static SourceModule createSoureModule(IResource resource, IDocument document, String moduleName) {
        SourceModule module;
        PythonNature nature = PythonNature.getPythonNature(resource.getProject());
        IFile f = (IFile) resource;
        String file = f.getRawLocation().toOSString();
        module = (SourceModule)AbstractModule.createModuleFromDoc(moduleName, new File(file), document, nature, 0);
        return module;
    }

    /**
     * @param module this is the module to set in the cache
     */
    protected void setModuleInCache(IModule module) {
        memo.put(MODULE_CACHE, module);
    }

    /**
     * @param resource the resource we are analyzing
     * @return the nature associated to the project where the resource is contained
     */
    protected PythonNature getPythonNature(IResource resource) {
        PythonNature pythonNature = PythonNature.getPythonNature(resource);
        return pythonNature;
    }

    /**
     * @param resource must be the resource we are analyzing because it will go to the cache without the resource (only as MODULE_NAME_CACHE)
     * @return the name of the module we are analyzing (given tho resource)
     */
    public String getModuleName(IResource resource, IPythonNature nature) {
        String moduleName = (String) memo.get(MODULE_NAME_CACHE);
        if(moduleName == null){
            moduleName = nature.resolveModule(resource);
            if(moduleName != null){
                setModuleNameInCache(moduleName);
            }else{
                throw new RuntimeException("Unable to resolve module for:"+resource);
            }
        }
        return moduleName;
    }

    /**
     * @param moduleName the module name to set in the cache
     */
    protected void setModuleNameInCache(String moduleName) {
        memo.put(MODULE_NAME_CACHE, moduleName);
    }

    /**
     * Method to return whether a resource is an __init__
     * 
     * this is needed because when we create an __init__, all sub-folders 
     * and files on the same folder become valid modules.
     * 
     * @return whether the resource is an init resource
     */
    protected boolean isInitFile(IResource resource){
        return resource.getName().startsWith("__init__.");
    }
    
    /**
     * @param resource the resource we want to know about
     * @return true if it is in the pythonpath
     */
    public static boolean isInPythonPath(IResource resource){
        if(resource == null){
            return false;
        }
        IProject project = resource.getProject();
        PythonNature nature = PythonNature.getPythonNature(project);
        if(project != null && nature != null){
            ICodeCompletionASTManager astManager = nature.getAstManager();
            if(astManager != null){
                IModulesManager modulesManager = astManager.getModulesManager();
                return modulesManager.isInPythonPath(resource, project);
            }
        }

        return false;
    }
    
    /**
     * @param initResource
     * @return all the IFiles that are below the folder where initResource is located.
     */
    protected IResource[] getInitDependents(IResource initResource){
        
        List<IResource> toRet = new ArrayList<IResource>();
        IContainer parent = initResource.getParent();
        
        try {
            fillWithMembers(toRet, parent);
            return toRet.toArray(new IResource[0]);
        } catch (CoreException e) {
            //that's ok, it might not exist anymore
            return new IResource[0];
        }
    }
    
    /**
     * @param toRet
     * @param parent
     * @throws CoreException
     */
    private void fillWithMembers(List<IResource> toRet, IContainer parent) throws CoreException {
        IResource[] resources = parent.members();
        
        for (int i = 0; i < resources.length; i++) {
            if(resources[i].getType() == IResource.FILE){
                toRet.add(resources[i]);
            }else if(resources[i].getType() == IResource.FOLDER){
                fillWithMembers(toRet, (IFolder)resources[i]);
            }
        }
    }



    /**
     * 
     * @return the maximun number of resources that it is allowed to visit (if this
     * number is higher than the number of resources changed, this visitor is not called).
     */
    public int maxResourcesToVisit() {
        return MAX_TO_VISIT_INFINITE;
    }
    
    /**
     * if all the files below a folder that has an __init__.py just added or removed should 
     * be visited, this method should return true, otherwise it should return false 
     * 
     * @return false by default, but may be reimplemented in subclasses. 
     */
    public boolean shouldVisitInitDependency(){
        return false;
    }

    /**
     * Called when a resource is changed
     * 
     * @param resource to be visited.
     */
    public abstract void visitChangedResource(IResource resource, IDocument document, IProgressMonitor monitor);

    
    /**
     * Called when a resource is added. Default implementation calls the same method
     * used for change.
     * 
     * @param resource to be visited.
     */
    public void visitAddedResource(IResource resource, IDocument document, IProgressMonitor monitor){
        visitChangedResource(resource, document, monitor);
    }

    /**
     * Called when a resource is removed
     * 
     * @param resource to be visited.
     */
    public abstract void visitRemovedResource(IResource resource, IDocument document, IProgressMonitor monitor);
    
    /**
     * This function is called right before a visiting session starts for a delta (end will
     * only be called when the whole delta is processed).
     * @param monitor this is the monitor that will be used in the visit
     * @param nature 
     */
    public void visitingWillStart(IProgressMonitor monitor, boolean isFullBuild, IPythonNature nature){
        
    }
    
    /**
     * This function is called when we finish visiting some delta (which may be the whole project or 
     * just some files).
     * 
     * A use-case is: It may be overriden if we need to store info in a persisting location
     * @param monitor this is the monitor used in the visit
     */
    public void visitingEnded(IProgressMonitor monitor){
        
    }
}
