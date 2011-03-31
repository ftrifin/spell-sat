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
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.REF;
import org.python.pydev.core.Tuple;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.runners.SimplePythonRunner;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;

public class PythonInterpreterManager extends AbstractInterpreterManager{

    public PythonInterpreterManager(Preferences prefs) {
        super(prefs);
    }

    @Override
    protected String getPreferenceName() {
        return PYTHON_INTERPRETER_PATH;
    }
    
    @Override
    protected String getNotConfiguredInterpreterMsg() {
        return "Interpreter is not properly configured!\n" +
                "Please go to window->preferences->PyDev->Python Interpreters and configure it.\n" +
                "If this is not supposed to be a Python project, change the project type on the\n" +
                "project properties to the project you want (e.g.: Jython project).";
    }

    @Override
    public Tuple<InterpreterInfo,String>createInterpreterInfo(String executable, IProgressMonitor monitor) throws CoreException {
        return doCreateInterpreterInfo(executable, monitor, true);
    }

    /**
     * @param executable the python interpreter from where we should create the info
     * @param monitor a monitor to see the progress
     * 
     * @return the created interpreter info
     * @throws CoreException
     */
    public static Tuple<InterpreterInfo,String> doCreateInterpreterInfo(String executable, IProgressMonitor monitor, boolean prompt) throws CoreException {
        if (executable == null)
        {
        	return null;
        }
        
        File script = PydevPlugin.getScriptWithinPySrc("interpreterInfo.py");

        Tuple<String, String> outTup = new SimplePythonRunner().runAndGetOutputWithInterpreter(executable, REF.getFileAbsolutePath(script), null, null, null, monitor);
        InterpreterInfo info = createInfoFromOutput(monitor, outTup, prompt);
        
        if(info == null){
            //cancelled
            return null;
        }

        info.restoreCompiledLibs(monitor);
        
        return new Tuple<InterpreterInfo,String>(info, outTup.o1);
    }


    @Override
    public boolean canGetInfoOnNature(IPythonNature nature) {
        try {
            return nature.isPython();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isJython() {
        return false;
    }

    public boolean isPython() {
        return true;
    }

    public String getManagerRelatedName() {
        return "python";
    }

}
