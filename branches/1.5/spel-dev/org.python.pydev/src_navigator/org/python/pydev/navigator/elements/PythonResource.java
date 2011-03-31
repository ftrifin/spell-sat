package org.python.pydev.navigator.elements;

import org.eclipse.core.resources.IResource;


public class PythonResource extends WrappedResource<IResource>{
    
    public PythonResource(IWrappedResource parentElement, IResource object, PythonSourceFolder pythonSourceFolder) {
        super(parentElement, object, pythonSourceFolder, IWrappedResource.RANK_PYTHON_RESOURCE);
        //System.out.println("Created PythonResource:"+this+" - "+actualObject+" parent:"+parentElement);
    }

}
