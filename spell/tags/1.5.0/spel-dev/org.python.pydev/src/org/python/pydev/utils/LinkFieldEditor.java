/*
 * @author Fabio Zadrozny
 */
package org.python.pydev.utils;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

/**
 * Helper class to provide a field that can be used as a link.
 * 
 * @note: to actually create a text that can be linked, it must be written as html with <a>text</a>.
 * 
 * @author Fabio
 */
public class LinkFieldEditor extends FieldEditor {

    /**
     * Link class
     */
    private Link link;
    
    /**
     * The selection listener that will do some action when the link is selected
     */
    private SelectionListener selectionListener;

    /**
     * @param name the name of the property 
     * @param linkText the text that'll appear to the user
     * @param parent the parent composite
     * @param selectionListener a listener that'll be executed when the linked text is clicked
     */
    public LinkFieldEditor(String name, String linkText, Composite parent, SelectionListener selectionListener) {
        init(name, linkText);
        this.selectionListener = selectionListener;
        createControl(parent);
    }

    protected void adjustForNumColumns(int numColumns) {
    }

    protected void doFillIntoGrid(Composite parent, int numColumns) {
        getLinkControl(parent);
    }

    /**
     * Returns this field editor's link component.
     * <p>
     * The link is created if it does not already exist
     * </p>
     *
     * @param parent the parent
     * @return the label control
     */
    public Link getLinkControl(Composite parent) {
        if (link == null) {
            link = new Link(parent, SWT.NONE);
            link.setFont(parent.getFont());
            String text = getLabelText();
            if (text != null) {
                link.setText(text);
            }

            link.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent event) {
                    link = null;
                }
            });

            link.addSelectionListener(getSelectionListener());

        } else {
            checkParent(link, parent);
        }
        return link;
    }

    private SelectionListener getSelectionListener() {
        return selectionListener;
    }

    protected void doLoad() {
    }

    protected void doLoadDefault() {
    }

    protected void doStore() {
    }

    public int getNumberOfControls() {
        return 1;
    }
}
