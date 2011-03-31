/*
 * Created on May 29, 2006
 */
package org.python.pydev.editor.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.python.pydev.core.docutils.WordUtils;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.preferences.AbstractPydevPrefs;


/**
 * This class is the class that resulted of the separation of the PydevPrefs because
 * it was too big.
 * 
 * @author Fabio
 */
public class PydevTypingPrefs  extends AbstractPydevPrefs {

    public PydevTypingPrefs(){
        setDescription("Editor"); 
        setPreferenceStore(PydevPlugin.getDefault().getPreferenceStore());
        this.
        fOverlayStore= createOverlayStore();
    }
    
    protected Control createAppearancePage(Composite parent) {
        Composite appearanceComposite= new Composite(parent, SWT.NONE );
        GridLayout layout= new GridLayout(); 
        layout.numColumns= 2;
        appearanceComposite.setLayout(layout);

        // simply a holder for the current reference for a Button, so you can input a tooltip
        Button b;
        

        //auto par
        b = addCheckBox(appearanceComposite, "Automatic parentheses insertion", AUTO_PAR, 0);
        b.setToolTipText(WordUtils.wrap("Enabling this option will enable automatic insertion of parentheses.  " +
                "Specifically, whenever you hit a brace such as '(', '{', or '[', its related peer will be inserted " +
                "and your cursor will be placed between the two braces.", TOOLTIP_WIDTH));
        
        //auto par
        b = addCheckBox(appearanceComposite, "After '(' indent to its level (false will indent a single tab)", AUTO_INDENT_TO_PAR_LEVEL, 0);
        
        //auto dedent 'else:'
        b = addCheckBox(appearanceComposite, "Automatic dedent of 'else:' and 'elif:'", AUTO_DEDENT_ELSE, 0);
        
        //auto braces
        b = addCheckBox(appearanceComposite, "Automatically skip matching braces when typing", AUTO_BRACES, 0);
        b.setToolTipText(WordUtils.wrap("Enabling this option will enable automatically skipping matching braces " +
                "if you try to insert them.  For example, if you have the following code:\n\n" +
                "def function(self):\n\n" +
                "...with your cursor before the end parenthesis (after the 'f' in \"self\"), typing a ')' will " +
                "simply move the cursor to the position after the ')' without inserting a new one.", TOOLTIP_WIDTH));
        
        //smart indent
        b = addCheckBox(appearanceComposite, "Use smart-indent?", SMART_INDENT_PAR, 0);
        
        //auto colon
        b = addCheckBox(appearanceComposite, "Automatic colon detection", AUTO_COLON, 0);
        b.setToolTipText(WordUtils.wrap("Enabling this feature will enable the editor to detect if you are trying " +
                "to enter a colon which is already there.  Instead of inserting another colon, the editor will " +
                "simply move your cursor to the next position after the colon.", TOOLTIP_WIDTH));

        //auto import str
        b = addCheckBox(appearanceComposite, "Automatic insertion of the 'import' string on 'from xxx' ", AUTO_WRITE_IMPORT_STR, 0);
        b.setToolTipText(WordUtils.wrap("Enabling this will allow the editor to automatically write the" +
                "'import' string when you write a space after you've written 'from xxx '.", TOOLTIP_WIDTH));
        
        addCheckBox(appearanceComposite, "Add 'self' automatically when declaring methods?", AUTO_ADD_SELF, 0);

        return appearanceComposite;
    }

}
