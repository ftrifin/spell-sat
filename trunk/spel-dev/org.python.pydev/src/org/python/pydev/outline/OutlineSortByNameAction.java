package org.python.pydev.outline;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.python.pydev.core.bundle.ImageCache;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.UIConstants;

/**
 * Action that addds a way to sort items by the name.
 * 
 * @author Fabio
 */
public class OutlineSortByNameAction extends Action {

    private static final String PREF_ALPHA_SORT = "org.python.pydev.OUTLINE_ALPHA_SORT";

    ViewerSorter sortByNameSorter;

    private WeakReference<PyOutlinePage> page;
    
    public OutlineSortByNameAction(PyOutlinePage page, ImageCache imageCache) {
        super("Sort by name", IAction.AS_CHECK_BOX);
        this.page = new WeakReference<PyOutlinePage>(page);

        setChecked(page.getStore().getBoolean(PREF_ALPHA_SORT));
        setAlphaSort(isChecked());

        try {
            setImageDescriptor(imageCache.getDescriptor(UIConstants.ALPHA_SORT));
        } catch (MalformedURLException e) {
            PydevPlugin.log("Missing Icon", e);
        }    
        setToolTipText("Sort by name");
    }

    /**
     * @param doSort :
     *            sort or not?
     */
    public void setAlphaSort(boolean doSort) {
        PyOutlinePage p = this.page.get();
        if (p != null) {
            p.getStore().setValue(PREF_ALPHA_SORT, doSort);
            if (sortByNameSorter == null) {
                sortByNameSorter = new ViewerSorter() {
                    @SuppressWarnings("unchecked")
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        return ((Comparable) e1).compareTo(e2);
                    }
                };
            }
            p.getTreeViewer().setSorter(doSort ? sortByNameSorter : null);
        }
    }

    public void run() {
        setAlphaSort(isChecked());
    }

}
