package org.python.pydev.editorinput;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.editor.preferences.PydevEditorPrefs;
import org.python.pydev.plugin.PydevPlugin;

/**
 * Class used to deal with the source locator prefs (even though they're edited in the SourceLocatorPrefsPage that's in
 * org.python.pydev.debug
 * 
 * @author Fabio
 */
public class PySourceLocatorPrefs {

    /**
     * Constant used to define that a path should not be asked for (so, the translation will return nothing and
     * the user won't be bothered by that)
     */
    public static final String DONTASK = "DONTASK";
    

    /**
     * Checks if a translation path passed is valid.
     * 
     * @param translation the translation path entered by the user
     * @return null if it's valid or the error message to be shown to the user
     */
    public static String isValid(String[] translation) {
        if(translation.length != 2){
            return "Input must have 2 elements.";
        }
        
        if(translation[1].equals(DONTASK)){
            return null;
        }
        if(!new File(translation[1]).exists()){
            return StringUtils.format("The file: %s does not exist and doesn't match 'DONTASK'.", translation[1]);
        }
        
        return null;
    }


    /**
     * @see #addPathTranslation(String) -- with toOSString for each path and a comma separator
     */
    public static void addPathTranslation(IPath path, IPath location) {
        addPathTranslation(new String[]{path.toOSString(), location.toOSString()});
    }
    
    
    /**
     * Any request to the passed path translation will be ignored.
     * @param path the path that should have the translation ignored (silently)
     */
    public static void setIgnorePathTranslation(IPath path) {
        addPathTranslation(new String[]{path.toOSString(), DONTASK});
    }

    
    /**
     * Adds a path to the translation table.
     * 
     * @param translation the translation path to be added. 
     * E.g.: 
     * path asked, new path -- means that a request for the "path asked" should return the "new path"
     * path asked, DONTASK -- means that if some request for that file was asked it should silently ignore it
     * 
     * E.g.: 
     * c:\foo\c.py,c:\temp\c.py
     * c:\foo\c.py,DONTASK
     */
    private static void addPathTranslation(String[] translation){
        String valid = isValid(translation);
        if(valid != null){
            throw new RuntimeException(valid);
        }
        IPreferenceStore store = PydevPlugin.getDefault().getPreferenceStore();
        String available = store.getString(PydevEditorPrefs.SOURCE_LOCATION_PATHS);
        
        if(available == null || available.trim().length() == 0){
            available = StringUtils.join(",", translation);
        }else{
            String pathAsked = translation[0].trim();
            
            String existent = getPathTranslation(pathAsked);
            if(existent != null){
                String[] splitted = StringUtils.split(available, '\n');
                for(int i=0;i<splitted.length;i++){
                    String s = splitted[i];
                    String initialPart = StringUtils.split(s, ',')[0].trim();
                    if(initialPart.equals(pathAsked)){
                        splitted[i] = StringUtils.join(",", translation);
                        break;
                    }
                }
                available = StringUtils.join("\n", splitted);
            }else{
                available += "\n";
                available += StringUtils.join(",", translation);
            }
        }
        store.putValue(PydevEditorPrefs.SOURCE_LOCATION_PATHS, available);
    }
    
    
    /**
     * @see #getPathTranslation(String) -- with toOSString from path.
     */
    public static String getPathTranslation(IPath pathToTranslate){
        return getPathTranslation(pathToTranslate.toOSString());
    }
    
    
    /**
     * Translates a path given the current translation settings
     * 
     * @param pathToTranslate the path to be translated
     * @return the translated path or DONTASK or null if no translation path was found for it
     */
    public static String getPathTranslation(String pathToTranslate){
        pathToTranslate = pathToTranslate.trim();
        IPreferenceStore store = PydevPlugin.getDefault().getPreferenceStore();
        String available = store.getString(PydevEditorPrefs.SOURCE_LOCATION_PATHS);
        if(available == null || available.trim().length() == 0){
            return null; //nothing available
        }else{
            String[] splitted = StringUtils.split(available, '\n');
            for (String string : splitted) {
                String[] translation = StringUtils.split(string, ',');
                if(translation.length == 2){
                    if(translation[0].trim().equals(pathToTranslate)){
                        return translation[1].trim();
                    }
                }
            }
        }
        return null;
    }


    /**
     * @param words words to be gotten as string
     * @return a string with all the passed words separated by '\n'
     */
    public static String wordsAsString(List<String[]> words){
        StringBuffer buf = new StringBuffer();
        for (String[] string : words) {
            buf.append(string[0].trim());
            buf.append(',');
            buf.append(string[1].trim());
            buf.append('\n');
        }
        return buf.toString();
    }
    
    
    /**
     * @param string the string that has to be returned as a list of strings
     * @return an array of strings from the passed string (reverse logic from wordsAsString)
     */
    public static List<String[]> stringAsWords(String string){
        ArrayList<String[]> strs = new ArrayList<String[]>();
        for(String str: StringUtils.split(string, '\n')){
            strs.add(StringUtils.split(str, ','));
        }
        return strs;
    }
    
}
