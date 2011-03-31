/*
 * Created on Aug 16, 2004
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.codecompletion.shell;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.python.pydev.core.REF;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.runners.SimpleRunner;

/**
 * @author Fabio Zadrozny
 */
public class PythonShell extends AbstractShell{

    
    /**
     * Initialize with the default python server file.
     * 
     * @throws IOException
     * @throws CoreException
     */
    public PythonShell() throws IOException, CoreException {
        super(PydevPlugin.getScriptWithinPySrc("pycompletionserver.py"));
    }


    @Override
    protected synchronized String createServerProcess(String interpreter, int pWrite, int pRead) throws IOException {
        File file = new File(interpreter);
        if(file.exists() == false ){
            throw new RuntimeException("The interpreter location found does not exist. "+interpreter);
        }
        if(file.isDirectory() == true){
            throw new RuntimeException("The interpreter location found is a directory. "+interpreter);
        }


        String execMsg;
        if(REF.isWindowsPlatform()){ //in windows, we have to put python "path_to_file.py"
            execMsg = interpreter+" \""+REF.getFileAbsolutePath(serverFile)+"\" "+pWrite+" "+pRead;
        }else{ //however in mac, or linux, this gives an error...
            execMsg = interpreter+" "+REF.getFileAbsolutePath(serverFile)+" "+pWrite+" "+pRead;
        }
        String[] parameters = {interpreter, REF.getFileAbsolutePath(serverFile), ""+pWrite, ""+pRead};
        process = SimpleRunner.createProcess(parameters, serverFile.getParentFile());

        return execMsg;
    }



}