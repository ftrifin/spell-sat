/*
 * Created on Oct 29, 2006
 * @author Fabio
 */
package org.python.pydev.navigator.ui;

import java.util.HashSet;
import java.util.LinkedList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.navigator.CommonNavigatorActionGroup;
import org.eclipse.ui.internal.navigator.ContributorTrackingSet;
import org.eclipse.ui.internal.navigator.NavigatorContentService;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorPipelineService;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.python.pydev.navigator.elements.IWrappedResource;
import org.python.pydev.navigator.elements.PythonFile;
import org.python.pydev.plugin.PydevPlugin;

/**
 * This class is the package explorer for pydev. It uses the CNF (Common Navigator Framework) to show
 * the resources as python elements.
 */
public class PydevPackageExplorer extends CommonNavigator implements IShowInTarget {

	private PydevCommonViewer viewer; 
    /**
     * This viewer is the one used instead of the common viewer -- should only be used to fix failures in the base class.
     */
    public static class PydevCommonViewer extends CommonViewer {
        
        /**
         * This is used so that we only restore the memento in the 'right' place
         */
        public boolean availableToRestoreMemento = false;
        
        public PydevCommonViewer(String id, Composite parent, int style) {
            super(id, parent, style);
        }
        
        /**
         * Returns the tree path for the given item.
         * 
         * It's overriden because when using mylar, the paths may be expanded but not shown, so segment is null
         * -- that's why we return null if a given segment is null (instead of the assert that it contains in the superclass) 
         * @since 3.2
         */
        @Override
        protected TreePath getTreePathFromItem(Item item) {
            LinkedList<Object> segments = new LinkedList<Object>();
            while(item!=null) {
                Object segment = item.getData();
                if(segment == null){
                    return null;
                }
                segments.addFirst(segment);
                item = getParentItem(item);
            }
            return new TreePath(segments.toArray());
        }
    }

    /**
     * This is the memento to be used.
     */
    private IMemento memento;

    /**
     * Overridden to keep the memento to be used later (it's private in the superclass).
     */
    public void init(IViewSite aSite, IMemento aMemento) throws PartInitException {
        super.init(aSite, aMemento);
        memento = aMemento;
    }

    /**
     * Overridden to create our viewer and not the superclass CommonViewer.
     * 
     * (Unfortunately, the superclass does a little more than creating it, so, we have to do those operations here 
     * too -- that's why we have to keep the memento object in the init method).
     */
    @Override
    protected CommonViewer createCommonViewer(Composite aParent) {
        //super.createCommonViewer(aParent); -- don't even call the super class
        CommonViewer aViewer = new PydevCommonViewer(getViewSite().getId(), aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        initListeners(aViewer);
        
        //commented: we do that only after the part is completely created (because otherwise the state is reverted later)
        //aViewer.getNavigatorContentService().restoreState(memento);
        
        return aViewer;
    }

    /**
     * Overridden because if the state is not restored as the last thing, it is reverted back to the previous state.
     */
    @Override
    public void createPartControl(Composite aParent) {
        super.createPartControl(aParent);
        viewer = (PydevCommonViewer) getCommonViewer();
        viewer.availableToRestoreMemento = true;
        for(int i=0;i<3;i++){
            try {
                //I don't know why the 1st time we restore it it doesn't work... so, we have to do it twice 
                //(and the other 1 is because we may have an exception in the 1st step).
                viewer.getNavigatorContentService().restoreState(memento);
            } catch (Exception e1) {
                if(i>1){
                    PydevPlugin.log("Unable to restore the state of the Pydev Package Explorer.", e1);
                }
            }
        }
    }
    
    /**
     * Returns the element contained in the EditorInput
     */
    Object getElementOfInput(IEditorInput input) {
        if (input instanceof IFileEditorInput) {
            return ((IFileEditorInput) input).getFile();
        }
        return null;
    }

    /**
     * Implements the 'show in...' action
     */
    public boolean show(ShowInContext context) {
        Object elementOfInput = null;
        ISelection selection = context.getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
            if (structuredSelection.size() == 1) {
                elementOfInput = structuredSelection.getFirstElement();
            }
        }

        Object input = context.getInput();
        if (input instanceof IEditorInput) {
            elementOfInput = getElementOfInput((IEditorInput) context.getInput());
        }

        return elementOfInput != null && tryToReveal(elementOfInput);
    }
    
    /**
     * This is the method that actually tries to reveal some item in the tree.
     * 
     * It will go through the pipeline to see if the actual object to reveal has been replaced in the replace pipeline.
     */
    public boolean tryToReveal(Object element) {
        element = getPythonModelElement(element);
        
        
        //null is checked in the revealAndVerify function
        if (revealAndVerify(element)) {
            return true;
        }

        //if it is a wrapped resource that we couldn't show, try to reveal as a resource...
        if (element instanceof IAdaptable && !(element instanceof IResource)) {
            IAdaptable adaptable = (IAdaptable) element;
            IResource resource = (IResource) adaptable.getAdapter(IResource.class);
            if (resource != null) {
                if (revealAndVerify(resource)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param element the element that should be gotten as an element from the pydev model
     * @return a pydev element or the same element passed as a parameter.
     */
    private Object getPythonModelElement(Object element) {
        if(element instanceof IWrappedResource){
            return element;
        }
        INavigatorPipelineService pipelineService = this.getNavigatorContentService().getPipelineService();
        if(element instanceof IAdaptable){
            IAdaptable adaptable = (IAdaptable) element;
            IFile file = (IFile) adaptable.getAdapter(IFile.class);
            if(file != null){
                HashSet<Object> files = new ContributorTrackingSet((NavigatorContentService) this.getNavigatorContentService());
                files.add(file);
                pipelineService.interceptAdd(new PipelinedShapeModification(file.getParent(), files));
                if(files.size() > 0){
                    element = files.iterator().next();
                }
            }
        }
        return element;
    }
    
    /**
     * Tries to reveal some selection
     * @return if it revealed the selection correctly (and false otherwise)
     */
    private boolean revealAndVerify(Object element) {
        if (element == null){
            return false;
        }
        
        selectReveal(new StructuredSelection(element));
        return !getSite().getSelectionProvider().getSelection().isEmpty();
    }

    
    /**
     * Overriden to show a selection without expanding PythonFiles (only its parent)
     */
    @Override
    public void selectReveal(ISelection selection) {
        CommonViewer commonViewer = getCommonViewer();
        if (commonViewer != null) {
            if(selection instanceof IStructuredSelection) {
                //we don't want to expand PythonFiles
                Object[] newSelection = ((IStructuredSelection)selection).toArray();
                for (int i = 0; i < newSelection.length; i++) {
                    Object object = getPythonModelElement(newSelection[i]);
                    if(object instanceof PythonFile){
                        PythonFile file = (PythonFile) object;
                        newSelection[i] = file.getParentElement();
                    }
                }
                
                //basically the same as in the superclass, but with those changed elements.
                Object[] expandedElements = commonViewer.getExpandedElements();
                Object[] newExpandedElements = new Object[newSelection.length + expandedElements.length];
                System.arraycopy(expandedElements, 0, newExpandedElements, 0, expandedElements.length);
                System.arraycopy(newSelection, 0, newExpandedElements, expandedElements.length, newSelection.length);
                commonViewer.setExpandedElements(newExpandedElements);
            }
            commonViewer.setSelection(selection, true);
        }
    }



}
