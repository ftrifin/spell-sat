/*
 * Created on Dec 20, 2004
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.core;

import java.io.File;
import java.io.Serializable;

import org.python.pydev.core.docutils.StringUtils;

/**
 * This class defines the key to use for some module. All its operations are based on its name.
 * The file may be null.
 * 
 * @author Fabio Zadrozny
 */
public class ModulesKey implements Comparable<ModulesKey>, Serializable{

    /**
     * 1L = just name and file
     * 2L = + zipModulePath
     */
    private static final long serialVersionUID = 2L;
    
    /**
     * Then name is always needed!
     */
    public String name;
    
    /**
     * Builtins may not have the file (null)
     */
    public File file;

    
    /**
     * Builtins may not have the file
     */
    public ModulesKey(String name, File f) {
        this.name = name;
        this.file = f;
    }
    

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ModulesKey o) {
        return name.compareTo(o.name);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof ModulesKey )){
            return false;
        }
        
        ModulesKey m = (ModulesKey)o;
        if(!(name.equals(m.name))){
            return false;
        }
        
        //consider only the name
        return true;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.name.hashCode();
    }
    
    @Override
    public String toString() {
        if(file != null){
            StringBuffer ret = new StringBuffer(name);
            ret.append(" - ");
            ret.append(file);
            return ret.toString();
        }
        return name;
    }


    /**
     * @return true if any of the parts in this modules key start with the passed string (considering the internal
     * parts lower case).
     */
    public boolean hasPartStartingWith(String startingWithLowerCase) {
        for (String mod : StringUtils.dotSplit(this.name.toLowerCase())) {
            if(mod.startsWith(startingWithLowerCase)){
                return true;
            }
        }
        return false;
    }
}
