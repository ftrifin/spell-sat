/*
 * License: Common Public License v1.0
 * Created on 08/08/2005
 * 
 * @author Fabio Zadrozny
 */
package org.python.pydev.ui.interpreters;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.python.copiedfromeclipsesrc.JDTNotAvailableException;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.REF;
import org.python.pydev.core.Tuple;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.runners.SimpleJythonRunner;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;

public class JythonInterpreterManager extends AbstractInterpreterManager{

    public JythonInterpreterManager(Preferences prefs) {
        super(prefs);
    }

    @Override
    protected String getPreferenceName() {
        return JYTHON_INTERPRETER_PATH;
    }
    
    @Override
    protected String getNotConfiguredInterpreterMsg() {
        return "Interpreter is not properly configured!\r\n" +
               "Please go to window->preferences->PyDev->Jython Interpreters and configure it.\r\n" +
               "If this is not supposed to be a Jython project, change the project type on the\r\n" +
               "project properties to the project you want (e.g.: Python project).";
    }

    @Override
    public Tuple<InterpreterInfo,String>createInterpreterInfo(String executable, IProgressMonitor monitor) throws CoreException, JDTNotAvailableException {
        return doCreateInterpreterInfo(executable, monitor);
    }

    /**
     * This is the method that creates the interpreter info for jython. It gets the info on the jython side and on the java side
     * 
     * @param executable the jar that should be used to get the info
     * @param monitor a monitor, to keep track of what's happening
     * @return the interpreter info, with the default libraries and jars
     * 
     * @throws CoreException
     */
    public static Tuple<InterpreterInfo,String> doCreateInterpreterInfo(String executable, IProgressMonitor monitor) throws CoreException, JDTNotAvailableException {
        boolean isJythonExecutable = InterpreterInfo.isJythonExecutable(executable);
        
        if(!isJythonExecutable){
            throw new RuntimeException("In order to get the info for the jython interpreter, a jar is needed (e.g.: jython.jar)");
        }
        File script = PydevPlugin.getScriptWithinPySrc("interpreterInfo.py");
        if(! script.exists()){
            throw new RuntimeException("The file specified does not exist: "+script);
        }
        
        //gets the info for the python side
        Tuple<String, String> outTup = new SimpleJythonRunner().runAndGetOutputWithJar(REF.getFileAbsolutePath(script), executable, null, null, null, monitor);
        String output = outTup.o1;
        
        InterpreterInfo info = createInfoFromOutput(monitor, outTup, true);
        if(info == null){
            //cancelled
            return null;
        }
        //the executable is the jar itself
        info.executableOrJar = executable;
        
        //we have to find the jars before we restore the compiled libs 
//        List<File> jars = JavaVmLocationFinder.findDefaultJavaJars();
//        for (File jar : jars) {
//            info.libs.add(REF.getFileAbsolutePath(jar));
//        }
        
        //java, java.lang, etc should be found now
        info.restoreCompiledLibs(monitor);
        

        return new Tuple<InterpreterInfo,String>(info, output);
    }

    @Override
    public boolean canGetInfoOnNature(IPythonNature nature) {
        try {
            return nature.isJython();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isJython() {
        return true;
    }

    public boolean isPython() {
        return false;
    }

    public String getManagerRelatedName() {
        return "jython";
    }
}
