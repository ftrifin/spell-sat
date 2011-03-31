/*
 * Created on May 5, 2005
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.autoedit;

import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;

/**
 * @author Fabio Zadrozny
 */
public interface IIndentPrefs {

    /**
     * @return True if we should substitute tabs for spaces.
     */
    public boolean getUseSpaces();
    
    /**
     * Sets the forceTabs preference for auto-indentation.
     * 
     * <p>
     * This is the preference that overrides "use spaces" preference when file
     * contains tabs (like mine do).
     * <p>
     * If the first indented line starts with a tab, then tabs override spaces.
     * 
     * @return True If tabs should be used even if it says we should use spaces.
     */
    public void setForceTabs(boolean forceTabs);
    public boolean getForceTabs();
    
    /**
     * @return the width a tab should have.
     */
    public int getTabWidth();
    
    /**
     * @return the indentation string based on the current settings.
     */
    public String getIndentationString();

    /**
     * Given the current settings, convert the current string to tabs or spaces.
     */
    public void convertToStd(IDocument document, DocumentCommand command);
    
    /**
     * @return whether we should auto-close parentesis
     */
    public boolean getAutoParentesis();
    
    /**
     * Get whether or not to do colon detection.
     * @return true iff colon detection is turned on
     */
    public boolean getAutoColon();
    
    /**
     * Get whether or not to auto-skip braces insertion  
     * @return iff auto-skip braces is ENABLED
     */
    public boolean getAutoBraces();
    
    /**
     * Get whether we should auto-write 'import' if we are in a from xxx import fff
     */
    public boolean getAutoWriteImport();
    
    /**
     * Get whether we should smart-indent after a '('
     */
    public boolean getSmartIndentPar();
    
    /**
     * Get whether we should add 'self' automatically when declaring method
     */
    public boolean getAutoAddSelf();

    /**
     * Get whether we should auto-dedent 'else:'
     */
    public boolean getAutoDedentElse();
    
    /**
     * @return whether we should indent to a parenthesis level on auto-indent or only add 1 tab to the indent).
     */
    public boolean getIndentToParLevel();

    /**
     * Should be called to regenerate the indent string that's in the cache.
     */
    public void regenerateIndentString();
}
