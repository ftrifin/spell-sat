package org.python.pydev.navigator.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BaseNewWizardMenu;

public class NewWizard extends BaseNewWizardMenu 
{
    private boolean enabled = true;

    /**
     * Creates a new wizard shortcut menu for the IDE.
     * 
     * @param window
     *            the window containing the menu
     */
    public NewWizard(IWorkbenchWindow window) 
    {
        this(window, null);
        
    }
    
    /**
     * Creates a new wizard shortcut menu for the IDE.
     * 
     * @param window
     *            the window containing the menu
     * @param id
     *            the identifier for this contribution item 
     */
    public NewWizard(IWorkbenchWindow window, String id) 
    {
        super(window, id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.BaseNewWizardMenu#addItems(org.eclipse.jface.action.IContributionManager)
     */
    @SuppressWarnings("unchecked")
	protected void addItems(List list) {
    	ArrayList shortCuts= new ArrayList();
    	addShortcuts(shortCuts);
        if (!shortCuts.isEmpty()) 
        {
        	list.addAll(shortCuts);
        }
    }

	/* (non-Javadoc)
	 * Method declared on IContributionItem.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled state of the receiver.
	 * 
	 * @param enabledValue if <code>true</code> the menu is enabled; else
	 * 		it is disabled
	 */
	public void setEnabled(boolean enabledValue) {
		this.enabled = enabledValue;
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.BaseNewWizardMenu#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {
		if (isEnabled()) {
			return super.getContributionItems();
		}
		return new IContributionItem[0];
	}
}
