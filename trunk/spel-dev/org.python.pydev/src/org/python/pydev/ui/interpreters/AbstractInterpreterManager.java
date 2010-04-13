/*
 * Created on May 17, 2005
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.ui.interpreters;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.python.copiedfromeclipsesrc.JDTNotAvailableException;
import org.python.pydev.core.ExtensionHelper;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.ISystemModulesManager;
import org.python.pydev.core.IToken;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.structure.FastStringBuffer;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.NotConfiguredInterpreterException;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;

/**
 * Does not write directly in INTERPRETER_PATH, just loads from it and works with it.
 * 
 * @author Fabio Zadrozny
 */
public abstract class AbstractInterpreterManager implements IInterpreterManager {

    /**
     * This is the cache, that points from an interpreter to its information.
     */
    private Map<String, InterpreterInfo> exeToInfo = new HashMap<String, InterpreterInfo>();
    private Preferences prefs;
    private String[] interpretersFromPersistedString;
    

    //caches that are filled at runtime -------------------------------------------------------------------------------
    /**
     * This is used to keep the builtin completions
     */
    protected transient IToken[] builtinCompletions;
    
    /**
     * This is used to keep the builtin module
     */
    protected transient IModule builtinMod;

    public void setBuiltinCompletions(IToken[] comps) {
        this.builtinCompletions = comps;
    }

    public IToken[] getBuiltinCompletions() {
        return builtinCompletions;
    }

    public IModule getBuiltinMod() {
        return builtinMod;
    }

    public void setBuiltinMod(IModule mod) {
        this.builtinMod = mod;
    }


    /**
     * Constructor
     */
    @SuppressWarnings("unchecked")
    public AbstractInterpreterManager(Preferences prefs) {
        this.prefs = prefs;
        List<IInterpreterObserver> participants = ExtensionHelper.getParticipants(ExtensionHelper.PYDEV_INTERPRETER_OBSERVER);
        for (IInterpreterObserver observer : participants) {
            observer.notifyInterpreterManagerRecreated(this);
        }
        prefs.addPropertyChangeListener(new Preferences.IPropertyChangeListener(){

            public void propertyChange(PropertyChangeEvent event) {
                clearCaches();
            }
        });
    }

    public void clearCaches() {
        builtinMod = null;
        builtinCompletions = null;
        interpretersFromPersistedString = null;
    }

    /**
     * @return the preference name where the options for this interpreter manager should be stored
     */
    protected abstract String getPreferenceName();
    
    /**
     * @throws NotConfiguredInterpreterException
     * @see org.python.pydev.core.IInterpreterManager#getDefaultInterpreter()
     */
    public String getDefaultInterpreter() throws NotConfiguredInterpreterException {
        String[] interpreters = getInterpreters();
        if(interpreters.length > 0){
            String interpreter = interpreters[0];
            if(interpreter == null){
                throw new NotConfiguredInterpreterException("The configured interpreter is null, some error happened getting it.\n" +getNotConfiguredInterpreterMsg());
            }
            return interpreter;
        }else{
            throw new NotConfiguredInterpreterException(this.getClass().getName()+":"+getNotConfiguredInterpreterMsg());
        }
    }

    public void clearAllBut(List<String> allButTheseInterpreters) {
        synchronized(exeToInfo){
            ArrayList<String> toRemove = new ArrayList<String>();
            for (String interpreter : exeToInfo.keySet()) {
                if(!allButTheseInterpreters.contains(interpreter)){
                    toRemove.add(interpreter);
                }
            }
            //we do not want to remove it while we are iterating...
            for (Object object : toRemove) {
                exeToInfo.remove(object);
            }
        }
    }
    
    /**
     * @return a message to show to the user when there is no configured interpreter
     */
    protected abstract String getNotConfiguredInterpreterMsg(); 

    /**
     * @see org.python.pydev.core.IInterpreterManager#getInterpreters()
     */
    public String[] getInterpreters() {
        if(interpretersFromPersistedString == null){
            interpretersFromPersistedString = getInterpretersFromPersistedString(getPersistedString());
        }
        return interpretersFromPersistedString;
    }
    
    /**
     * @see org.python.pydev.core.IInterpreterManager#hasInfoOnInterpreter(java.lang.String)
     */
    public boolean hasInfoOnInterpreter(String interpreter) {
        if(interpreter == null){
            InterpreterInfo info = (InterpreterInfo) exeToInfo.get(getDefaultInterpreter());
            return info != null;
        }
        interpreter = interpreter.toLowerCase();
        String[] interpreters = getInterpreters();
        for (String str : interpreters) {
            if(str.toLowerCase().equals(interpreter)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * @see org.python.pydev.core.IInterpreterManager#getDefaultInterpreterInfo(org.eclipse.core.runtime.IProgressMonitor)
     */
    public InterpreterInfo getDefaultInterpreterInfo(IProgressMonitor monitor) {
        String interpreter = getDefaultInterpreter();
        return getInterpreterInfo(interpreter, monitor);
    }
    
    /**
     * Given an executable, should create the interpreter info that corresponds to it
     * 
     * @param executable the executable that should be used to create the info
     * @param monitor a monitor to keep track of the info
     * 
     * @return the interpreter info for the executable
     * @throws CoreException 
     * @throws JDTNotAvailableException 
     */
    public abstract Tuple<InterpreterInfo,String> createInterpreterInfo(String executable, IProgressMonitor monitor) throws CoreException, JDTNotAvailableException;

    /**
     * Creates the interpreter info from the output. Checks for errors.
     */
    protected static InterpreterInfo createInfoFromOutput(IProgressMonitor monitor, Tuple<String, String> outTup, boolean prompt) {
        if(outTup.o1 == null || outTup.o1.trim().length() == 0){
            throw new RuntimeException(
                    "No output was in the standard output when trying to create the interpreter info.\n" +
                    "The error output contains:>>"+outTup.o2+"<<");
        }
        InterpreterInfo info = InterpreterInfo.fromString(outTup.o1, prompt);
        return info;
    }

    /**
     * @see org.python.pydev.core.IInterpreterManager#getInterpreterInfo(java.lang.String)
     */
    public InterpreterInfo getInterpreterInfo(String executable, IProgressMonitor monitor) {
        synchronized(lock){
            InterpreterInfo info = (InterpreterInfo) exeToInfo.get(executable);
            if(info == null){
                monitor.worked(5);
                //ok, we have to get the info from the executable (and let's cache results for future use)...
                Tuple<InterpreterInfo,String> tup = null;
                try {
    
                    tup = createInterpreterInfo(executable, monitor);
                    if(tup == null){
                        //cancelled (in the dialog that asks the user to choose the valid paths)
                        return null;
                    }
                    info = tup.o1;
                    
                } catch (RuntimeException e) {
                    PydevPlugin.log(e);
                    throw e;
                } catch (Exception e) {
                    PydevPlugin.log(e);
                    throw new RuntimeException(e);
                }
                if(info.executableOrJar != null && info.executableOrJar.trim().length() > 0){
                    exeToInfo.put(info.executableOrJar, info);
                    
                }else{ //it is null or empty
                    final String title = "Invalid interpreter:"+executable;
                    final String msg = "Unable to get information on interpreter!";
                    String reasonCreation = "The interpreter (or jar): '"+executable+"' is not valid - info.executable found: "+info.executableOrJar+"\n";
                    if(tup != null){
                        reasonCreation += "The standard output gotten from the executed shell was: >>"+tup.o2+"<<";
                    }
                    final String reason = reasonCreation;
                    
                    try {
                        final Display disp = Display.getDefault();
                        disp.asyncExec(new Runnable(){
                            public void run() {
                                ErrorDialog.openError(null, title, msg, new Status(Status.ERROR, PydevPlugin.getPluginID(), 0, reason, null));
                            }
                        });
                    } catch (Throwable e) {
                        // ignore error comunication error
                    }
                    throw new RuntimeException(reason);
                }
            }
            return info;
        }
    }

    /**
     * Called when an interpreter should be added.
     * 
     * @see org.python.pydev.core.IInterpreterManager#addInterpreter(java.lang.String)
     */
    public String addInterpreter(String executable, IProgressMonitor monitor) {
        exeToInfo.remove(executable); //always clear it
        InterpreterInfo info = getInterpreterInfo(executable, monitor);
        if(info == null){
            //cancelled on the screen that the user has to choose paths...
            return null;
        }
        return info.executableOrJar;
    }

    private Object lock = new Object();
    //little cache...
    private String persistedCache;
    private String [] persistedCacheRet;
    
    /**
     * @see org.python.pydev.core.IInterpreterManager#getInterpretersFromPersistedString(java.lang.String)
     */
    public String[] getInterpretersFromPersistedString(String persisted) {
        synchronized(lock){
            if(persisted == null || persisted.trim().length() == 0){
                return new String[0];
            }
            
            if(persistedCache == null || persistedCache.equals(persisted) == false){
                List<String> ret = new ArrayList<String>();
    
                try {
                    List<InterpreterInfo> list = new ArrayList<InterpreterInfo>();                
                    String[] strings = persisted.split("&&&&&");
                    
                    //first, get it...
                    for (String string : strings) {
                        try {
                            list.add(InterpreterInfo.fromString(string));
                        } catch (Exception e) {
                            //ok, its format might have changed
                            String errMsg = "Interpreter storage changed.\r\n" +
                            "Please restore it (window > preferences > Pydev > Interpreter)";
                            PydevPlugin.log(errMsg, e);
                            
                            return new String[0];
                        }
                    }
                    
                    //then, put it in cache
                    for (InterpreterInfo info: list) {
                        if(info != null && info.executableOrJar != null){
                            this.exeToInfo.put(info.executableOrJar, info);
                            ret.add(info.executableOrJar);
                        }
                    }
                    
                    //and at last, restore the system info
                    for (final InterpreterInfo info: list) {
                        try {
                            ISystemModulesManager systemModulesManager = (ISystemModulesManager) PydevPlugin.readFromWorkspaceMetadata(info.getExeAsFileSystemValidPath());
                            info.setModulesManager(systemModulesManager);
                        } catch (Exception e) {
                            //PydevPlugin.logInfo(e); -- don't log it, that should be 'standard' (something changed in the way we store it).
                            
                            //if it does not work it (probably) means that the internal storage format changed among versions,
                            //so, we have to recreate that info.
                            final Display def = Display.getDefault();
                            def.syncExec(new Runnable(){
    
                                public void run() {
                                    Shell shell = def.getActiveShell();
                                    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
                                    dialog.setBlockOnOpen(false);
                                    try {
                                        dialog.run(false, false, new IRunnableWithProgress(){

                                            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                                monitor.beginTask("Updating the interpreter info (internal storage format changed).", 100);
                                                //ok, maybe its file-format changed... let's re-create it then.
                                                info.restorePythonpath(monitor);
                                                //after restoring it, let's save it.
                                                PydevPlugin.writeToWorkspaceMetadata(info.getModulesManager(), info.getExeAsFileSystemValidPath());
                                                monitor.done();
                                            }}
                                        );
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                
                            });
                            System.out.println("Finished restoring information for: "+info.executableOrJar);
                        }
                    }
                    
                } catch (Exception e) {
                    PydevPlugin.log(e);
                    
                    //ok, some error happened (maybe it's not configured)
                    return new String[0];
                }
                
                persistedCache = persisted;
                persistedCacheRet = ret.toArray(new String[0]);
            }
        }
        return persistedCacheRet;
    }

    /**
     * @see org.python.pydev.core.IInterpreterManager#getStringToPersist(java.lang.String[])
     */
    public String getStringToPersist(String[] executables) {
        FastStringBuffer buf = new FastStringBuffer();
        for (String exe : executables) {
            InterpreterInfo info = this.exeToInfo.get(exe);
            if(info!=null){
                buf.append(info.toString());
                buf.append("&&&&&");
            }
        }
        
        return buf.toString();
    }

    String persistedString;
    public String getPersistedString() {
        if(persistedString == null){
            persistedString = prefs.getString(getPreferenceName());
        }
        return persistedString;
    }
    
    public void setPersistedString(String s) {
        persistedString = s;
        prefs.setValue(getPreferenceName(), s);
    }
    
    /**
     * This method persists all the modules managers that are within this interpreter manager
     * (so, all the SystemModulesManagers will be saved -- and can be later restored).
     */
    public void saveInterpretersInfoModulesManager() {
        for(InterpreterInfo info : this.exeToInfo.values()){
            PydevPlugin.writeToWorkspaceMetadata(info.getModulesManager(), info.getExeAsFileSystemValidPath());
        }
    }


    /**
     * @return whether this interpreter manager can be used to get info on the specified nature
     */
    public abstract boolean canGetInfoOnNature(IPythonNature nature);
    
    /**
     * @see org.python.pydev.core.IInterpreterManager#hasInfoOnDefaultInterpreter(IPythonNature)
     */
    public boolean hasInfoOnDefaultInterpreter(IPythonNature nature) {
        if(!canGetInfoOnNature(nature)){
            throw new RuntimeException("Cannot get info on the requested nature");
        }
        
        try {
            InterpreterInfo info = (InterpreterInfo) exeToInfo.get(getDefaultInterpreter());
            return info != null;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.python.pydev.core.IInterpreterManager#restorePythopathFor(org.eclipse.core.runtime.IProgressMonitor)
     */
    @SuppressWarnings("unchecked")
    public void restorePythopathFor(IProgressMonitor monitor) {
        synchronized(lock){
            for(String interpreter:exeToInfo.keySet()){
                final InterpreterInfo info = getInterpreterInfo(interpreter, monitor);
                info.restorePythonpath(monitor); //that's it, info.modulesManager contains the SystemModulesManager
                
                List<IInterpreterObserver> participants = ExtensionHelper.getParticipants(ExtensionHelper.PYDEV_INTERPRETER_OBSERVER);
                for (IInterpreterObserver observer : participants) {
                    try {
                        observer.notifyDefaultPythonpathRestored(this, interpreter, monitor);
                    } catch (Exception e) {
                        PydevPlugin.log(e);
                    }
                }
            }
            
            //Commented out: this is not really needed, as the information on the interpreter is mainly kept separated
            //from the projects info (this was initially done on an attempt to track dependencies through different
            //modules, but it proved too time consuming in a dynamic language such as python). 
            
//            final Boolean[] restoreNatures = new Boolean[]{false}; 
//            
//            final Display def = Display.getDefault();
//            def.syncExec(new Runnable(){
//
//                public void run() {
//                    Shell shell = def.getActiveShell();
//                    restoreNatures[0] = MessageDialog.openQuestion(shell, "Interpreter info changed", 
//                            "The interpreter info has been changed, do you want to make a full build?\n" +
//                            "(the same thing can be later achieved through the menu: Project > Clean)");
//                }
//            });
//            
//            //update the natures...
//            if(restoreNatures[0]){
//                List<IPythonNature> pythonNatures = PythonNature.getAllPythonNatures();
//                for (IPythonNature nature : pythonNatures) {
//                    try {
//                        //if they have the same type of the interpreter manager.
//                        if (this.isPython() == nature.isPython() || this.isJython() == nature.isJython()) {
//                            nature.rebuildPath(defaultSelectedInterpreter, monitor);
//                        }
//                    } catch (Throwable e) {
//                        PydevPlugin.log(e);
//                    }
//                }
//            }
        }        
    }

    public boolean isConfigured() {
        try {
            String defaultInterpreter = getDefaultInterpreter();
            if(defaultInterpreter == null){
                return false;
            }
            if(defaultInterpreter.length() == 0){
                return false;
            }
        } catch (NotConfiguredInterpreterException e) {
            return false;
        }
        return true;
    }
    
    public int getRelatedId() {
        if(isPython()){
            return IPythonNature.PYTHON_RELATED;
        }else if(isJython()){
            return IPythonNature.JYTHON_RELATED;
        }else{
            throw new RuntimeException("Expected Python or Jython");
        }
    }
}

