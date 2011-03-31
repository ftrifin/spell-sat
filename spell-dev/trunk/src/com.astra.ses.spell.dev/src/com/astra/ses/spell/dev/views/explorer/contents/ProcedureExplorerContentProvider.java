///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.views.explorer.contents
// 
// FILE      : ProcedureExplorerContentProvider.java
//
// DATE      : 2010-07-05
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.views.explorer.contents;

import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.python.pydev.plugin.nature.PythonNature;

import com.astra.ses.spell.dev.views.explorer.SpellResource;

/*******************************************************************************
 * 
 * ProcedureExplorerContentProvider adds content to the navigator.
 *
 ******************************************************************************/
public class ProcedureExplorerContentProvider implements IPipelinedTreeContentProvider {

	/** CommonViewer */
	private CommonViewer m_viewer;
	
	@Override
	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
		// TODO Don't do anything, as children are being provided by default
	}

	@Override
	public void getPipelinedElements(Object anInput, Set theCurrentElements) {
		// TODO Don't do anything, as elements are being provided by default
	}

	@Override
	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
		return aSuggestedParent;
	}

	@Override
	public PipelinedShapeModification interceptAdd(
			PipelinedShapeModification anAddModification) {
		
		Set children = anAddModification.getChildren();
		for (Object child : children)
		{
			if (child instanceof IFile)
			{
				IContainer parent = ((IFile) child).getParent();
				/* 
				 * TODO As the interceptAdd documentation explains,
				 * this method should not be invoked. There should be
				 * another way to solve this problem
				 */
				m_viewer.refresh(parent);
			}
		}
		
		return anAddModification;
	}

	@Override
	public boolean interceptRefresh(
			PipelinedViewerUpdate aRefreshSynchronization) {
		return false;
	}

	@Override
	public PipelinedShapeModification interceptRemove(
			PipelinedShapeModification aRemoveModification) {
		/*
		 * We have to intercept if a source folder is about to be removed in
		 * order that the python nature shall be updated
		 */
		for (Object children : aRemoveModification.getChildren())
		{
			if (children instanceof IResource)
			{
				IResource resource = (IResource) children;
				SpellResource spellResource = SpellResource.valueOf(resource);
				if (spellResource != null)
				{
					switch (spellResource)
					{
						case SOURCE_FOLDER:
							/*
							 * Remove source folder from python nature
							 */
							try {
								PythonNature nature = (PythonNature) resource.getProject().getNature(PythonNature.PYTHON_NATURE_ID);
								Set<String> sourcePaths = nature.getPythonPathNature().getProjectSourcePathSet(false);
								sourcePaths.remove(resource.getFullPath().toString());
								String newSourcePath = "";
								for (String sourcePath : sourcePaths)
								{
									newSourcePath += sourcePath + "|";
								}
								if (!newSourcePath.isEmpty())
								{
									newSourcePath = newSourcePath.substring(0,newSourcePath.length() - 1);
								}
								nature.getPythonPathNature().setProjectSourcePath(newSourcePath);
							} catch (CoreException e) {
								e.printStackTrace();
							}
						default:
							break;
					}
				}
			}
		}
		return aRemoveModification;
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
		return false;
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] result = null;
		if (parentElement instanceof IContainer)
		{
			IContainer container = (IContainer) parentElement;
			try {
				result = container.members();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public Object getParent(Object element) {
		IResource resource = (IResource) element;
		return resource.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		boolean hasChildren = false;
		if (element instanceof IContainer)
		{
			switch (((IContainer) element).getType())
			{
			case IResource.PROJECT:
				IProject project = (IProject) element;
				if (!project.isAccessible())
				{
					return false;
				}
				// Fall through to process the project as a normal container
			default:
				IContainer container = (IContainer) element;
				try {
					hasChildren = container.members().length > 0;
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return hasChildren;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		IWorkspaceRoot root = (IWorkspaceRoot) inputElement;
		return root.getProjects();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		m_viewer = (CommonViewer) viewer;
	}

	@Override
	public void restoreState(IMemento aMemento) {
	}

	@Override
	public void saveState(IMemento aMemento) {
	}
}
