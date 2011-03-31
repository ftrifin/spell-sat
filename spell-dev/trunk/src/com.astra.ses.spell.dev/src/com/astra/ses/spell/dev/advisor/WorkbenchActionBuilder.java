///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.advisor
// 
// FILE      : WorkbenchActionBuilder.java
//
// DATE      : 2010-05-21
//
// Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.advisor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardDropDownAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.internal.ide.actions.QuickMenuAction;
import org.eclipse.ui.internal.ide.actions.RetargetActionWithDefault;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;


/**
 * Adds actions to a workbench window.
 */
@SuppressWarnings({ "restriction", "deprecation" })
public final class WorkbenchActionBuilder extends ActionBarAdvisor {
    private final IWorkbenchWindow window;

    // generic actions
    private IWorkbenchAction closeAction;

    private IWorkbenchAction closeAllAction;
    
    private IWorkbenchAction closeOthersAction;

    private IWorkbenchAction closeAllSavedAction;

    private IWorkbenchAction saveAction;

    private IWorkbenchAction saveAllAction;

    private IWorkbenchAction newWindowAction;
    
    private IWorkbenchAction newEditorAction;

    private IWorkbenchAction helpContentsAction;

    private IWorkbenchAction helpSearchAction;
	
    private IWorkbenchAction dynamicHelpAction;
    
    private IWorkbenchAction aboutAction;

    private IWorkbenchAction openPreferencesAction;

    private IWorkbenchAction saveAsAction;

    private IWorkbenchAction hideShowEditorAction;

    private IWorkbenchAction savePerspectiveAction;

    private IWorkbenchAction resetPerspectiveAction;

    private IWorkbenchAction editActionSetAction;

    private IWorkbenchAction closePerspAction;

    private IWorkbenchAction lockToolBarAction;

    private IWorkbenchAction closeAllPerspsAction;

    private IWorkbenchAction showViewMenuAction;

    private IWorkbenchAction showPartPaneMenuAction;

    private IWorkbenchAction nextPartAction;

    private IWorkbenchAction prevPartAction;

    private IWorkbenchAction nextEditorAction;

    private IWorkbenchAction prevEditorAction;

    private IWorkbenchAction nextPerspectiveAction;

    private IWorkbenchAction prevPerspectiveAction;

    private IWorkbenchAction activateEditorAction;

    private IWorkbenchAction maximizePartAction;
    
    private IWorkbenchAction minimizePartAction;

    private IWorkbenchAction switchToEditorAction;

	private IWorkbenchAction workbookEditorsAction;

    private IWorkbenchAction quickAccessAction;

    private IWorkbenchAction backwardHistoryAction;

    private IWorkbenchAction forwardHistoryAction;

    // generic retarget actions
    private IWorkbenchAction undoAction;

    private IWorkbenchAction redoAction;

    private IWorkbenchAction quitAction;

    private IWorkbenchAction goIntoAction;

    private IWorkbenchAction backAction;

    private IWorkbenchAction forwardAction;

    private IWorkbenchAction upAction;

    private IWorkbenchAction nextAction;

    private IWorkbenchAction previousAction;

    // IDE-specific actions
    private IWorkbenchAction openWorkspaceAction;

    private IWorkbenchAction projectPropertyDialogAction;

    private IWorkbenchAction newWizardAction;

    private IWorkbenchAction newWizardDropDownAction;

    private IWorkbenchAction importResourcesAction;
    
    private IWorkbenchAction exportResourcesAction;

    IWorkbenchAction buildAllAction; // Incremental workspace build

    private IWorkbenchAction cleanAction;

    private IWorkbenchAction toggleAutoBuildAction;

    MenuManager buildWorkingSetMenu;

    private QuickMenuAction showInQuickMenu;

    private QuickMenuAction newQuickMenu;

    private IWorkbenchAction introAction;

    // IDE-specific retarget actions
    IWorkbenchAction buildProjectAction;

    // contribution items
    // @issue should obtain from ContributionItemFactory
    private NewWizard newWizardMenu;

    private IContributionItem pinEditorContributionItem;

    // @issue class is workbench internal
    private StatusLineContributionItem statusLineItem;

	private Preferences.IPropertyChangeListener prefListener;

    // listener for the "close editors automatically"
    // preference change
    private IPropertyChangeListener propPrefListener;

    private IPageListener pageListener;

    private IResourceChangeListener resourceListener;
    
    /**
     * Indicates if the action builder has been disposed
     */
    private boolean isDisposed = false;

    /**
     * Constructs a new action builder which contributes actions
     * to the given window.
     * 
     * @param configurer the action bar configurer for the window
     */
    public WorkbenchActionBuilder(IActionBarConfigurer configurer) {
        super(configurer);
        window = configurer.getWindowConfigurer().getWindow();
    }

    /**
     * Returns the window to which this action builder is contributing.
     */
    private IWorkbenchWindow getWindow() {
        return window;
    }

    /**
     * Hooks listeners on the preference store and the window's page, perspective and selection services.
     */
	private void hookListeners() {

        pageListener = new IPageListener() {
            public void pageActivated(IWorkbenchPage page) {
                // do nothing
            }

            public void pageClosed(IWorkbenchPage page) {
                // do nothing
            }

            public void pageOpened(IWorkbenchPage page) {
                // set default build handler -- can't be done until the shell is available
                IAction buildHandler = new BuildAction(page.getWorkbenchWindow(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
            	((RetargetActionWithDefault)buildProjectAction).setDefaultHandler(buildHandler);
            }
        };
        getWindow().addPageListener(pageListener);

        prefListener = new Preferences.IPropertyChangeListener() {
            public void propertyChange(Preferences.PropertyChangeEvent event) {
                if (event.getProperty().equals(
                        ResourcesPlugin.PREF_AUTO_BUILDING)) {
                   	updateBuildActions(false);
                }
            }
        };
        ResourcesPlugin.getPlugin().getPluginPreferences()
                .addPropertyChangeListener(prefListener);

        // listener for the "close editors automatically"
        // preference change
        propPrefListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(
						IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
                    if (window.getShell() != null
                            && !window.getShell().isDisposed()) {
                        // this property change notification could be from a non-ui thread
                        window.getShell().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                updatePinActionToolbar();
                            }
                        });
                    }
                }
            }
        };
        /*
         * In order to ensure that the pin action toolbar sets its size 
         * correctly, the pin action should set its visiblity before we call updatePinActionToolbar().
         * 
         * In other words we always want the PinActionContributionItem to be notified before the 
         * WorkbenchActionBuilder.
         */
        WorkbenchPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(propPrefListener);
        //listen for project description changes, which can affect enablement of build actions
        resourceListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDelta delta = event.getDelta();
				if (delta == null) {
					return;
				}
				IResourceDelta[] projectDeltas = delta.getAffectedChildren();
				for (int i = 0; i < projectDeltas.length; i++) {
					int kind = projectDeltas[i].getKind();
					//affected by projects being opened/closed or description changes
					boolean changed = (projectDeltas[i].getFlags() & (IResourceDelta.DESCRIPTION | IResourceDelta.OPEN)) != 0;
					if (kind != IResourceDelta.CHANGED || changed) {
						updateBuildActions(false);
						return;
					}
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
    }

    public void fillActionBars(int flags) {
        super.fillActionBars(flags);
        if ((flags & FILL_PROXY) == 0) {
            updateBuildActions(true);
            hookListeners();
        }
    }

    /**
     * Fills the coolbar with the workbench actions.
     */
    protected void fillCoolBar(ICoolBarManager coolBar) {

    	IActionBarConfigurer2 actionBarConfigurer = (IActionBarConfigurer2) getActionBarConfigurer();
//        { // Set up the context Menu
//            coolbarPopupMenuManager = new MenuManager();
//            coolBar.setContextMenuManager(coolbarPopupMenuManager);
//            IMenuService menuService = (IMenuService) window.getService(IMenuService.class);
//            menuService.populateContributionManager(coolbarPopupMenuManager, "popup:windowCoolbarContextMenu"); //$NON-NLS-1$
//        }
        coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_FILE));
        { // File Group
            IToolBarManager fileToolBar = actionBarConfigurer.createToolBarManager();
            fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
            fileToolBar.add(newWizardDropDownAction);
            fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
            fileToolBar.add(new GroupMarker(
                    IWorkbenchActionConstants.SAVE_GROUP));
            fileToolBar.add(saveAction);
            fileToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
            fileToolBar.add(getPrintItem());
            fileToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));

            fileToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
            fileToolBar.add(new Separator(
                    IWorkbenchActionConstants.MB_ADDITIONS));

            // Add to the cool bar manager
            coolBar.add(actionBarConfigurer.createToolBarContributionItem(fileToolBar,
                    IWorkbenchActionConstants.TOOLBAR_FILE));
        }

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_NAV));
        { // Navigate group
            IToolBarManager navToolBar = actionBarConfigurer.createToolBarManager();
            navToolBar.add(new Separator(
                    IWorkbenchActionConstants.HISTORY_GROUP));
            navToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
            navToolBar.add(backwardHistoryAction);
            navToolBar.add(forwardHistoryAction);
            navToolBar.add(new Separator(IWorkbenchActionConstants.PIN_GROUP));
            navToolBar.add(pinEditorContributionItem);

            // Add to the cool bar manager
            coolBar.add(actionBarConfigurer.createToolBarContributionItem(navToolBar,
                    IWorkbenchActionConstants.TOOLBAR_NAVIGATE));
        }
        coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
        coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_HELP));
    }

    /**
     * Fills the menu bar with the workbench actions.
     */
    protected void fillMenuBar(IMenuManager menuBar) 
    {
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(createWindowMenu());
        menuBar.add(createHelpMenu());
    }

    /**
     * Creates and returns the File menu.
     */
    private MenuManager createFileMenu() {
        MenuManager menu = new MenuManager("File", IWorkbenchActionConstants.M_FILE);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
        {
            // create the New submenu, using the same id for it as the New action
            String newText = "New";
            String newId = ActionFactory.NEW.getId();
            MenuManager newMenu = new MenuManager(newText, newId);
            newMenu.setActionDefinitionId("org.eclipse.ui.file.newQuickMenu"); //$NON-NLS-1$
            newMenu.add(new Separator(newId));
            this.newWizardMenu = new NewWizard(getWindow());
            newMenu.add(this.newWizardMenu);
            newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            menu.add(newMenu);
        }
		
        menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        menu.add(new Separator());

        menu.add(closeAction);
        menu.add(closeAllAction);
        //		menu.add(closeAllSavedAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
        menu.add(new Separator());
        menu.add(saveAction);
        menu.add(saveAsAction);
        menu.add(saveAllAction);
        menu.add(getRevertItem());
        menu.add(new Separator());
        menu.add(getMoveItem());
        menu.add(getRenameItem());
        menu.add(getRefreshItem());

        menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
        menu.add(new Separator());
        menu.add(getPrintItem());
        menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        menu.add(new Separator());
        menu.add(openWorkspaceAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        menu.add(new Separator());
        menu.add(importResourcesAction);
        menu.add(exportResourcesAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(new Separator());
        menu.add(getPropertiesItem());

        menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
        menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
        menu.add(new Separator());
        
        // If we're on OS X we shouldn't show this command in the File menu. It
		// should be invisible to the user. However, we should not remove it -
		// the carbon UI code will do a search through our menu structure
		// looking for it when Cmd-Q is invoked (or Quit is chosen from the
		// application menu.
		ActionContributionItem quitItem = new ActionContributionItem(quitAction);
		quitItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
		menu.add(quitItem);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
    }

    /**
	 * Creates and returns the Edit menu.
	 */
    private MenuManager createEditMenu() {
        MenuManager menu = new MenuManager("Edit", IWorkbenchActionConstants.M_EDIT);
        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

        menu.add(undoAction);
        menu.add(redoAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
        menu.add(new Separator());

        menu.add(getCutItem());
        menu.add(getCopyItem());
        menu.add(getPasteItem());
        menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        menu.add(new Separator());

        menu.add(getDeleteItem());
        menu.add(getSelectAllItem());
        menu.add(new Separator());

        menu.add(getFindItem());
        menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
        menu.add(new Separator());

        menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        return menu;
    }

    /**
     * Creates and returns the Window menu.
     */
    private MenuManager createWindowMenu() {
        MenuManager menu = new MenuManager("Window", IWorkbenchActionConstants.M_WINDOW);

        menu.add(newWindowAction);
        menu.add(newEditorAction);
		
        Separator sep = new Separator(IWorkbenchActionConstants.MB_ADDITIONS);
		sep.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
        
		MenuManager showViewMenuMgr = new MenuManager("Show view", "showView"); //$NON-NLS-1$
		IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST
              .create(getWindow());
		showViewMenuMgr.add(showViewMenu);
		menu.add(showViewMenuMgr);

		menu.add(sep);

        // See the comment for quit in createFileMenu
        ActionContributionItem openPreferencesItem = new ActionContributionItem(openPreferencesAction);
        openPreferencesItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
        menu.add(openPreferencesItem);
        return menu;
    }

    /**
	 * Creates and returns the Help menu.
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager("Help", "spellHelp");
		ActionContributionItem aboutItem = new ActionContributionItem(aboutAction);
		aboutItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
        menu.add(aboutItem);
		menu.add(new GroupMarker("group.about.ext")); //$NON-NLS-1$
        return menu;
    }
    
    /**
     * Disposes any resources and unhooks any listeners that are no longer needed.
     * Called when the window is closed.
     */
	public void dispose() {
        if (isDisposed) {
			return;
		}
    	isDisposed = true;
        
        getActionBarConfigurer().getStatusLineManager().remove(statusLineItem);
        if (pageListener != null) {
            window.removePageListener(pageListener);
            pageListener = null;
        }
        if (prefListener != null) 
        {
        	// Incorrect in Eclipse 3.5, but kept here for Eclipse 3.4
        	// compatibility
        	try
        	{
        		ResourcesPlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(prefListener);
        	}
        	catch(Exception ex) {}
        	prefListener = null;
        }
        if (propPrefListener != null) {
            WorkbenchPlugin.getDefault().getPreferenceStore()
                    .removePropertyChangeListener(propPrefListener);
            propPrefListener = null;
        }
        if (resourceListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
            resourceListener = null;
        }

        if (pinEditorContributionItem != null) 	pinEditorContributionItem.dispose();
        if (showInQuickMenu != null) 			showInQuickMenu.dispose();
        if (newQuickMenu != null)				newQuickMenu.dispose();

        // null out actions to make leak debugging easier
        closeAction = null;
        closeAllAction = null;
        closeAllSavedAction = null;
        closeOthersAction = null;
        saveAction = null;
        saveAllAction = null;
        newWindowAction = null;
		newEditorAction = null;
        helpContentsAction = null;
        helpSearchAction = null;
		dynamicHelpAction = null;
        aboutAction = null;
        openPreferencesAction = null;
        saveAsAction = null;
        hideShowEditorAction = null;
        savePerspectiveAction = null;
        resetPerspectiveAction = null;
        editActionSetAction = null;
        closePerspAction = null;
        lockToolBarAction = null;
        closeAllPerspsAction = null;
        showViewMenuAction = null;
        showPartPaneMenuAction = null;
        nextPartAction = null;
        prevPartAction = null;
        nextEditorAction = null;
        prevEditorAction = null;
        nextPerspectiveAction = null;
        prevPerspectiveAction = null;
        activateEditorAction = null;
        maximizePartAction = null;
        minimizePartAction = null;
        switchToEditorAction = null;
        quickAccessAction.dispose();
        quickAccessAction = null;
        backwardHistoryAction = null;
        forwardHistoryAction = null;
        undoAction = null;
        redoAction = null;
        quitAction = null;
        goIntoAction = null;
        backAction = null;
        forwardAction = null;
        upAction = null;
        nextAction = null;
        previousAction = null;
        openWorkspaceAction = null;
        projectPropertyDialogAction = null;
        newWizardAction = null;
        newWizardDropDownAction = null;
        importResourcesAction = null;
        exportResourcesAction = null;
        buildAllAction = null;
        cleanAction = null;
        toggleAutoBuildAction = null;
        buildWorkingSetMenu = null;
        showInQuickMenu = null;
        newQuickMenu = null;
        buildProjectAction = null;
        newWizardMenu = null;
        pinEditorContributionItem = null;
        statusLineItem = null;
        prefListener = null;
        propPrefListener = null;
        introAction = null;
        
        super.dispose();
    }

    void updateModeLine(final String text) {
        statusLineItem.setText(text);
    }

    /**
     * Returns true if the menu with the given ID should
     * be considered as an OLE container menu. Container menus
     * are preserved in OLE menu merging.
     */
    public boolean isApplicationMenu(String menuId) {
        if (menuId.equals(IWorkbenchActionConstants.M_FILE)) {
			return true;
		}
        if (menuId.equals(IWorkbenchActionConstants.M_WINDOW)) {
			return true;
		}
        return false;
    }

    /**
     * Return whether or not given id matches the id of the coolitems that
     * the workbench creates.
     */
    public boolean isWorkbenchCoolItemId(String id) {
        if (IWorkbenchActionConstants.TOOLBAR_FILE.equalsIgnoreCase(id)) {
			return true;
		}
        if (IWorkbenchActionConstants.TOOLBAR_NAVIGATE.equalsIgnoreCase(id)) {
			return true;
		}
        return false;
    }

    /**
     * Fills the status line with the workbench contribution items.
     */
    protected void fillStatusLine(IStatusLineManager statusLine) {
        statusLine.add(statusLineItem);
    }

    /**
     * Creates actions (and contribution items) for the menu bar, toolbar and status line.
     */
    protected void makeActions(final IWorkbenchWindow window) {
        // @issue should obtain from ConfigurationItemFactory
        statusLineItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$

        newWizardAction = ActionFactory.NEW.create(window);
        register(newWizardAction);

        newWizardDropDownAction = new ActionFactory("newWizardDropDown") 
        { //$NON-NLS-1$
            /* (non-javadoc) method declared on ActionFactory */
            public IWorkbenchAction create(IWorkbenchWindow window) {
                if (window == null) {
                    throw new IllegalArgumentException();
                }
                // @issue we are creating a NEW action just to pass to NewWizardDropDownAction
                IWorkbenchAction innerAction = ActionFactory.NEW.create(window);
                NewWizard newWizardMenu = new NewWizard(window);
                IWorkbenchAction action = new NewWizardDropDownAction(window,
                        innerAction, newWizardMenu);
                action.setId(getId());
                return action;
            }
        }.create(window);
        	
        	
        	IDEActionFactory.NEW_WIZARD_DROP_DOWN
                .create(window);
        register(newWizardDropDownAction);

        importResourcesAction = ActionFactory.IMPORT.create(window);
        register(importResourcesAction);

        exportResourcesAction = ActionFactory.EXPORT.create(window);
        register(exportResourcesAction);
        
        buildAllAction = IDEActionFactory.BUILD.create(window);
        register(buildAllAction);

        cleanAction = IDEActionFactory.BUILD_CLEAN.create(window);
        register(cleanAction);

        toggleAutoBuildAction = IDEActionFactory.BUILD_AUTOMATICALLY
                .create(window);
        register(toggleAutoBuildAction);

        saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);

        saveAsAction = ActionFactory.SAVE_AS.create(window);
        register(saveAsAction);

        saveAllAction = ActionFactory.SAVE_ALL.create(window);
        register(saveAllAction);
		
        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(getWindow());
        newWindowAction.setText("New window");
        register(newWindowAction);

		newEditorAction = ActionFactory.NEW_EDITOR.create(window);
		register(newEditorAction);

        undoAction = ActionFactory.UNDO.create(window);
        register(undoAction);

        redoAction = ActionFactory.REDO.create(window);
        register(redoAction);






        closeAction = ActionFactory.CLOSE.create(window);
        register(closeAction);

        closeAllAction = ActionFactory.CLOSE_ALL.create(window);
        register(closeAllAction);

        closeOthersAction = ActionFactory.CLOSE_OTHERS.create(window);
        register(closeOthersAction);

        closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(window);
        register(closeAllSavedAction);

        helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
        register(helpContentsAction);

        helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
        register(helpSearchAction);
		
        dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
        register(dynamicHelpAction);
        
        aboutAction = ActionFactory.ABOUT.create(window);
//        aboutAction
//                .setImageDescriptor(IDEInternalWorkbenchImages
//                        .getImageDescriptor(IDEInternalWorkbenchImages.IMG_OBJS_DEFAULT_PROD));
        register(aboutAction);

        openPreferencesAction = ActionFactory.PREFERENCES.create(window);
        register(openPreferencesAction);

        // Actions for invisible accelerators
        showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(window);
        register(showViewMenuAction);

        showPartPaneMenuAction = ActionFactory.SHOW_PART_PANE_MENU
                .create(window);
        register(showPartPaneMenuAction);

        nextEditorAction = ActionFactory.NEXT_EDITOR.create(window);
        register(nextEditorAction);
        prevEditorAction = ActionFactory.PREVIOUS_EDITOR.create(window);
        register(prevEditorAction);
        ActionFactory.linkCycleActionPair(nextEditorAction, prevEditorAction);

        nextPartAction = ActionFactory.NEXT_PART.create(window);
        register(nextPartAction);
        prevPartAction = ActionFactory.PREVIOUS_PART.create(window);
        register(prevPartAction);
        ActionFactory.linkCycleActionPair(nextPartAction, prevPartAction);

        nextPerspectiveAction = ActionFactory.NEXT_PERSPECTIVE
                .create(window);
        register(nextPerspectiveAction);
        prevPerspectiveAction = ActionFactory.PREVIOUS_PERSPECTIVE
                .create(window);
        register(prevPerspectiveAction);
        ActionFactory.linkCycleActionPair(nextPerspectiveAction,
                prevPerspectiveAction);

        activateEditorAction = ActionFactory.ACTIVATE_EDITOR
                .create(window);
        register(activateEditorAction);

        maximizePartAction = ActionFactory.MAXIMIZE.create(window);
        register(maximizePartAction);

		minimizePartAction = ActionFactory.MINIMIZE.create(window);
		register(minimizePartAction);
        
        switchToEditorAction = ActionFactory.SHOW_OPEN_EDITORS
                .create(window);
        register(switchToEditorAction);

        workbookEditorsAction = ActionFactory.SHOW_WORKBOOK_EDITORS
        		.create(window);
        register(workbookEditorsAction);
        
        quickAccessAction = ActionFactory.SHOW_QUICK_ACCESS
        	.create(window);

        hideShowEditorAction = ActionFactory.SHOW_EDITOR.create(window);
        register(hideShowEditorAction);
        savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE
                .create(window);
        register(savePerspectiveAction);
        editActionSetAction = ActionFactory.EDIT_ACTION_SETS
                .create(window);
        register(editActionSetAction);
        lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(window);
        register(lockToolBarAction);
        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE
                .create(window);
        register(resetPerspectiveAction);
        closePerspAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
        register(closePerspAction);
        closeAllPerspsAction = ActionFactory.CLOSE_ALL_PERSPECTIVES
                .create(window);
        register(closeAllPerspsAction);

        forwardHistoryAction = ActionFactory.FORWARD_HISTORY
                .create(window);
        register(forwardHistoryAction);

        backwardHistoryAction = ActionFactory.BACKWARD_HISTORY
                .create(window);
        register(backwardHistoryAction);




        quitAction = ActionFactory.QUIT.create(window);
        register(quitAction);



        goIntoAction = ActionFactory.GO_INTO.create(window);
        register(goIntoAction);

        backAction = ActionFactory.BACK.create(window);
        register(backAction);

        forwardAction = ActionFactory.FORWARD.create(window);
        register(forwardAction);

        upAction = ActionFactory.UP.create(window);
        register(upAction);

        nextAction = ActionFactory.NEXT.create(window);
//        nextAction
//                .setImageDescriptor(IDEInternalWorkbenchImages
//                        .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV));
        register(nextAction);

        previousAction = ActionFactory.PREVIOUS.create(window);
//        previousAction
//                .setImageDescriptor(IDEInternalWorkbenchImages
//                        .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV));
        register(previousAction);

        buildProjectAction = IDEActionFactory.BUILD_PROJECT.create(window);
        register(buildProjectAction);

        openWorkspaceAction = IDEActionFactory.OPEN_WORKSPACE
                .create(window);
        register(openWorkspaceAction);

        projectPropertyDialogAction = IDEActionFactory.OPEN_PROJECT_PROPERTIES
                .create(window);
        register(projectPropertyDialogAction);

        if (window.getWorkbench().getIntroManager().hasIntro()) {
            introAction = ActionFactory.INTRO.create(window);
            register(introAction);
        }

        String showInQuickMenuId = "org.eclipse.ui.navigate.showInQuickMenu"; //$NON-NLS-1$
        showInQuickMenu = new QuickMenuAction(showInQuickMenuId) {
            protected void fillMenu(IMenuManager menu) {
                menu.add(ContributionItemFactory.VIEWS_SHOW_IN
                        .create(window));
            }
        };
        register(showInQuickMenu);

//        final String newQuickMenuId = "org.eclipse.ui.file.newQuickMenu"; //$NON-NLS-1$
//        newQuickMenu = new QuickMenuAction(newQuickMenuId) {
//            protected void fillMenu(IMenuManager menu) {
//                menu.add(new NewWizardMenu(window));
//            }
//        };
//        register(newQuickMenu);

        pinEditorContributionItem = ContributionItemFactory.PIN_EDITOR
                .create(window);
        
    }

	/**
	 * Update the build actions on the toolbar and menu bar based on the current
	 * state of autobuild. This method can be called from any thread.
	 * 
	 * @param immediately
	 *            <code>true</code> to update the actions immediately,
	 *            <code>false</code> to queue the update to be run in the
	 *            event loop
	 */
    void updateBuildActions(boolean immediately) {
        // this can be triggered by property or resource change notifications
        Runnable update = new Runnable() {
            public void run() {
                if (isDisposed) {
					return;
				}
		    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject[] projects = workspace.getRoot().getProjects();
		    	boolean enabled = BuildUtilities.isEnabled(projects, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		        //update menu bar actions in project menu
		        buildAllAction.setEnabled(enabled);
		        buildProjectAction.setEnabled(enabled);
		        toggleAutoBuildAction.setChecked(workspace.isAutoBuilding());
		        cleanAction.setEnabled(BuildUtilities.isEnabled(projects, IncrementalProjectBuilder.CLEAN_BUILD));
		
		        //update the cool bar build button
		        ICoolBarManager coolBarManager = getActionBarConfigurer()
		                .getCoolBarManager();
		        IContributionItem cbItem = coolBarManager
		                .find(IWorkbenchActionConstants.TOOLBAR_FILE);
		        if (!(cbItem instanceof IToolBarContributionItem)) {
		            // This should not happen
		            System.err.println("File toolbar contribution item is missing"); //$NON-NLS-1$
		            return;
		        }
		        IToolBarContributionItem toolBarItem = (IToolBarContributionItem) cbItem;
		        IToolBarManager toolBarManager = toolBarItem.getToolBarManager();
		        if (toolBarManager == null) {
		            // error if this happens, file toolbar assumed to always exist
		            System.err.println("File toolbar is missing"); //$NON-NLS-1$
		            return;
		        }
            }
        };
        if (immediately) {
        	update.run();
        }
        else {
	        // Dispatch the update to be run later in the UI thread.
	        // This helps to reduce flicker if autobuild is being temporarily disabled programmatically.
	        Shell shell = window.getShell();
	        if (shell != null && !shell.isDisposed()) {
        		shell.getDisplay().asyncExec(update);
	        }
        }
    }

	/**
     * Update the pin action's tool bar
     */
    void updatePinActionToolbar() {

        ICoolBarManager coolBarManager = getActionBarConfigurer()
                .getCoolBarManager();
        IContributionItem cbItem = coolBarManager
                .find(IWorkbenchActionConstants.TOOLBAR_NAVIGATE);
        if (!(cbItem instanceof IToolBarContributionItem)) {
            // This should not happen
            System.err.println("Navigation toolbar contribution item is missing"); //$NON-NLS-1$
            return;
        }
        IToolBarContributionItem toolBarItem = (IToolBarContributionItem) cbItem;
        IToolBarManager toolBarManager = toolBarItem.getToolBarManager();
        if (toolBarManager == null) {
            // error if this happens, navigation toolbar assumed to always exist
        	System.err.println("Navigate toolbar is missing"); //$NON-NLS-1$
            return;
        }

        toolBarManager.update(false);
        toolBarItem.update(ICoolBarManager.SIZE);
    }
    
    private IContributionItem getCutItem() {
		return getItem(
				ActionFactory.CUT.getId(),
				"org.eclipse.ui.edit.cut", //$NON-NLS-1$
				ISharedImages.IMG_TOOL_CUT,
				ISharedImages.IMG_TOOL_CUT_DISABLED,
				WorkbenchMessages.Workbench_cut,
				WorkbenchMessages.Workbench_cutToolTip, null);
	}
    
    private IContributionItem getCopyItem() {
		return getItem(
				ActionFactory.COPY.getId(),
				"org.eclipse.ui.edit.copy", //$NON-NLS-1$
				ISharedImages.IMG_TOOL_COPY,
				ISharedImages.IMG_TOOL_COPY_DISABLED,
				WorkbenchMessages.Workbench_copy,
				WorkbenchMessages.Workbench_copyToolTip, null);
	}
    
    private IContributionItem getPasteItem() {
		return getItem(
				ActionFactory.PASTE.getId(),
				"org.eclipse.ui.edit.paste", ISharedImages.IMG_TOOL_PASTE, //$NON-NLS-1$
				ISharedImages.IMG_TOOL_PASTE_DISABLED,
				WorkbenchMessages.Workbench_paste,
				WorkbenchMessages.Workbench_pasteToolTip, null);
	}
    
    private IContributionItem getPrintItem() {
		return getItem(
				ActionFactory.PRINT.getId(),
				"org.eclipse.ui.file.print", ISharedImages.IMG_ETOOL_PRINT_EDIT, //$NON-NLS-1$
				ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED,
				WorkbenchMessages.Workbench_print,
				WorkbenchMessages.Workbench_printToolTip, null);
	}
    
    private IContributionItem getSelectAllItem() {
		return getItem(
				ActionFactory.SELECT_ALL.getId(),
				"org.eclipse.ui.edit.selectAll", //$NON-NLS-1$
				null, null, WorkbenchMessages.Workbench_selectAll,
				WorkbenchMessages.Workbench_selectAllToolTip, null);
	}
    
    private IContributionItem getFindItem() {
		return getItem(
				ActionFactory.FIND.getId(),
				"org.eclipse.ui.edit.findReplace", //$NON-NLS-1$
				null, null, WorkbenchMessages.Workbench_findReplace,
				WorkbenchMessages.Workbench_findReplaceToolTip, null);
	}
    
//    private IContributionItem getBookmarkItem() {
//		return getItem(
//				IDEActionFactory.BOOKMARK.getId(),
//				"org.eclipse.ui.edit.addBookmark", //$NON-NLS-1$
//				null, null, IDEWorkbenchMessages.Workbench_addBookmark,
//				IDEWorkbenchMessages.Workbench_addBookmarkToolTip, null);
//	}
//    
//    private IContributionItem getTaskItem() {
//		return getItem(
//				IDEActionFactory.ADD_TASK.getId(),
//				"org.eclipse.ui.edit.addTask", //$NON-NLS-1$
//				null, null, IDEWorkbenchMessages.Workbench_addTask,
//				IDEWorkbenchMessages.Workbench_addTaskToolTip, null);
//	}
    
    private IContributionItem getDeleteItem() {
        return getItem(ActionFactory.DELETE.getId(),
        		"org.eclipse.ui.edit.delete", //$NON-NLS-1$
        		ISharedImages.IMG_TOOL_DELETE,
        		ISharedImages.IMG_TOOL_DELETE_DISABLED,
        		WorkbenchMessages.Workbench_delete,
        		WorkbenchMessages.Workbench_deleteToolTip, 
        		IWorkbenchHelpContextIds.DELETE_RETARGET_ACTION);
    }
    
    private IContributionItem getRevertItem() {
		return getItem(
				ActionFactory.REVERT.getId(),
				"org.eclipse.ui.file.revert", //$NON-NLS-1$
				null, null, WorkbenchMessages.Workbench_revert,
				WorkbenchMessages.Workbench_revertToolTip, null);
	}
    
    private IContributionItem getRefreshItem() {
		return getItem(ActionFactory.REFRESH.getId(),
				"org.eclipse.ui.file.refresh", null, null, //$NON-NLS-1$
				WorkbenchMessages.Workbench_refresh,
				WorkbenchMessages.Workbench_refreshToolTip, null);
	}
    
    private IContributionItem getPropertiesItem() {
		return getItem(ActionFactory.PROPERTIES.getId(),
				"org.eclipse.ui.file.properties", null, null, //$NON-NLS-1$
				WorkbenchMessages.Workbench_properties,
				WorkbenchMessages.Workbench_propertiesToolTip, null);
	}
    
    private IContributionItem getMoveItem() {
		return getItem(ActionFactory.MOVE.getId(), "org.eclipse.ui.edit.move", //$NON-NLS-1$
				null, null, WorkbenchMessages.Workbench_move,
				WorkbenchMessages.Workbench_moveToolTip, null);
	}
    
    private IContributionItem getRenameItem() {
		return getItem(ActionFactory.RENAME.getId(),
				"org.eclipse.ui.edit.rename", null, null, //$NON-NLS-1$
				WorkbenchMessages.Workbench_rename,
				WorkbenchMessages.Workbench_renameToolTip, null);
	}
    
//    private IContributionItem getOpenProjectItem() {
//		return getItem(IDEActionFactory.OPEN_PROJECT.getId(),
//				"org.eclipse.ui.project.openProject", null, null, //$NON-NLS-1$
//				IDEWorkbenchMessages.OpenResourceAction_text,
//				IDEWorkbenchMessages.OpenResourceAction_toolTip, null);
//	}
//    
//    private IContributionItem getCloseProjectItem() {
//		return getItem(
//				IDEActionFactory.CLOSE_PROJECT.getId(),
//				"org.eclipse.ui.project.closeProject", //$NON-NLS-1$
//				null, null, IDEWorkbenchMessages.CloseResourceAction_text,
//				IDEWorkbenchMessages.CloseResourceAction_text, null);
//	}
//    
    private IContributionItem getItem(String actionId, String commandId,
    		String image, String disabledImage, String label, String tooltip, String helpContextId) {
		ISharedImages sharedImages = getWindow().getWorkbench()
				.getSharedImages();

		IActionCommandMappingService acms = (IActionCommandMappingService) getWindow()
				.getService(IActionCommandMappingService.class);
		acms.map(actionId, commandId);

		CommandContributionItemParameter commandParm = new CommandContributionItemParameter(
				getWindow(), actionId, commandId, null, sharedImages
						.getImageDescriptor(image), sharedImages
						.getImageDescriptor(disabledImage), null, label, null,
				tooltip, CommandContributionItem.STYLE_PUSH, null, false);
		return new CommandContributionItem(commandParm);
	}
}
