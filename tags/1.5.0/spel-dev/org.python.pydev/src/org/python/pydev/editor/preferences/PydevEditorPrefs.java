/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Scott Schlesier - Adapted for use in pydev
 *     Fabio Zadrozny 
 *******************************************************************************/

package org.python.pydev.editor.preferences;


import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.preferences.AbstractPydevPrefs;
import org.python.pydev.plugin.preferences.ColorEditor;


/**
 * The preference page for setting the editor options.
 * <p>
 * This class is internal and not intended to be used by clients.</p>
 */
public class PydevEditorPrefs extends AbstractPydevPrefs {


    public PydevEditorPrefs() {
        setDescription("Procedure editor appearance settings"); 
        setPreferenceStore(PydevPlugin.getDefault().getPreferenceStore());
        
        fOverlayStore= createOverlayStore();
    }

    protected Control createAppearancePage(Composite parent) {
         
        
        /*addTextField(appearanceComposite, "Tab length:", TAB_WIDTH, 3, 0, true);
        
        addCheckBox(appearanceComposite, "Replace tabs with spaces when typing?", SUBSTITUTE_TABS, 0);
        
        addCheckBox(appearanceComposite, "Assume tab spacing when files contain tabs?", GUESS_TAB_SUBSTITUTION, 0);*/
        
    	GridLayout layout= new GridLayout(2,true);
    	
        GridData groupData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH);
        groupData.grabExcessHorizontalSpace = true;
        groupData.grabExcessVerticalSpace = true;
        
        GridData elementsGridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        elementsGridData.verticalSpan = 3;
        elementsGridData.grabExcessHorizontalSpace = true;
        
        GridData aspectWidgetsGridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        aspectWidgetsGridData.grabExcessHorizontalSpace = false;
        
        Group symbolsAspect = new Group(parent, SWT.SHADOW_IN);
        symbolsAspect.setText("Color and font types for language elements"); 
        symbolsAspect.setLayout(layout);
        symbolsAspect.setLayoutData(groupData);

        //symbols list
        fAppearanceColorList= new List(symbolsAspect, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        fAppearanceColorList.setLayoutData(elementsGridData);

        //Color editor button
        fAppearanceColorEditor= new ColorEditor(symbolsAspect);
        Button foregroundColorButton= fAppearanceColorEditor.getButton();
        foregroundColorButton.setLayoutData(aspectWidgetsGridData);
        
        //font style bold
        fFontBoldCheckBox = addStyleCheckBox(symbolsAspect, "Bold");
        fFontBoldCheckBox.setLayoutData(aspectWidgetsGridData);
        
        //font style italic
        fFontItalicCheckBox = addStyleCheckBox(symbolsAspect, "Italic");
        fFontItalicCheckBox.setLayoutData(aspectWidgetsGridData);
        
        fAppearanceColorList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                handleAppearanceColorListSelection();
            }
        });
        
        foregroundColorButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                int i= fAppearanceColorList.getSelectionIndex();
                String key= fAppearanceColorListModel[i][1];
                
                PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColorEditor.getColorValue());
            }
        });
        
        return symbolsAspect;
    }

}