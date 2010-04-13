package org.python.pydev.project;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IPythonPathNature;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.plugin.nature.PythonNature;
import org.python.pydev.ui.filetypes.FileTypesPreferencesPage;

import com.astra.ses.spell.dev.context.services.ConfigurationManager;

public class PyDevProjectManager {
	
	/**Singleton instance*/
	private static PyDevProjectManager s_instance;

	/***************************************************************************
	 * Obtain the singleton instance of this class
	 * @return
	 **************************************************************************/
	public static PyDevProjectManager getInstance()
	{
		if (s_instance == null)
		{
			s_instance = new PyDevProjectManager();
		}
		return s_instance;
	}
	
	private PyDevProjectManager() 
	{
		//TODO
	}
	
	/**************************************************************************
	 * Create python suite project
	 * @throws CoreException 
	 *************************************************************************/
	public void createProject(IProject projectHandle, 
			IProgressMonitor monitor, String projectType, String projectInterpreter) 
	throws CoreException 
	{          
			projectHandle.open(IProject.BACKGROUND_REFRESH, monitor);
            ConfigurationManager manager = ConfigurationManager.getInstance();
            String sourceFolder = manager.getProjectSourceFolder();
            String projectPythonPath = null;
            if (sourceFolder != null)
            {
            	projectPythonPath = projectHandle.getFullPath().toString() + IPath.SEPARATOR + sourceFolder;
            }
            
            //we should rebuild the path even if there's no source-folder (this way we will re-create the astmanager)
            PythonNature.addNature(projectHandle, monitor, projectType, projectPythonPath, projectInterpreter);     
	}
	
	/**************************************************************************
	 * Create project source folder
	 * @throws CoreException 
	 *************************************************************************/
	public IFolder createSourceFolder(IProject project, String folderName, IProgressMonitor monitor) 
	throws CoreException 
	{
        if(project == null || !project.exists()){
            throw new RuntimeException("The project selected does not exist in the workspace.");
        }
        IPythonPathNature pathNature = PythonNature.getPythonPathNature(project);
        if(pathNature == null){
            IPythonNature nature = PythonNature.addNature(project, monitor, null, null, null);
            pathNature = nature.getPythonPathNature();
            if(pathNature == null){
                throw new RuntimeException("Unable to add the nature to the seleted project.");
            }
        }
        IFolder folder = project.getFolder(folderName);
        if(!folder.exists()){
            folder.create(true, true, monitor);
        }
        String newPath = folder.getFullPath().toString();
        
        String curr = pathNature.getProjectSourcePath();
        if(curr == null){
            curr = "";
        }
        if(curr.endsWith("|")){
            curr = curr.substring(0, curr.length()-1);
        }
        if(curr.length() > 0){
            //there is already some path
            curr+="|"+newPath;
        }else{
            //there is still no other path
            curr=newPath;
        }
        pathNature.setProjectSourcePath(curr);
        PythonNature.getPythonNature(project).rebuildPath();
        return folder;
	}
	
	/**************************************************************************
	 * Create a package by giving the source folder it will be contained
	 * @throws CoreException 
	 *************************************************************************/
	public IContainer createPackage (IContainer sourceFolder, String module, IProgressMonitor monitor) 
	throws CoreException 
	{
        String[] packageParts = StringUtils.dotSplit(module);
        IContainer parent = sourceFolder;
        for (String packagePart : packageParts) {
            IFolder folder = parent.getFolder(new Path(packagePart));
            if(!folder.exists()){
                folder.create(true, true, monitor);
            }
            parent = folder;
        }
        return parent;
	}
	
	/**************************************************************************
	 * Create a folder
	 * @param container
	 * @param name
	 * @param monitor
	 * @return
	 * @throws CoreException 
	 **************************************************************************/
	public IFolder createFolder (IContainer container, String name, IProgressMonitor monitor) 
	throws CoreException
	{
		String[] packageParts = StringUtils.dotSplit(name);
        IContainer parent = container;
        IFolder lastFolder = null;
        for (String packagePart : packageParts) {
            IFolder folder = parent.getFolder(new Path(packagePart));
            if(!folder.exists()){
                folder.create(true, true, monitor);
            }
            parent = folder;
            lastFolder = folder;
        }
        return lastFolder;
	}
	
	/**************************************************************************
	 * Create a module by giving the source folder it will be contained
	 * @throws CoreException 
	 * @throws CoreException 
	 *************************************************************************/
	public IFile createProcedure (IContainer sourceFolder, IContainer sourcePackage, 
			String name, IProgressMonitor monitor) 
	throws CoreException 
	{
        String validatedName = name+FileTypesPreferencesPage.getDefaultDottedPythonExtension();
        IFile file = sourcePackage.getFile(new Path(validatedName));
        if(!file.exists()){
            file.create(new ByteArrayInputStream(new byte[0]), true, monitor);
        } 
        return file;
	}
}
