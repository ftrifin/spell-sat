///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.callstack
// 
// FILE      : CallstackLabelProvider.java
//
// DATE      : 2008-11-21 08:55
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.model.callstack;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.astra.ses.spell.gui.Activator;

/******************************************************************************
 * Provides the labels and icons for the callstack tree.
 *****************************************************************************/
public class CallstackLabelProvider extends LabelProvider implements
        IColorProvider
{
	/** Holds the image for procedure nodes */
	private Image	m_imgProc;
	/** Holds the image for call nodes */
	private Image	m_imgLiveNode;
	/** Holds the image for a finished node */
	private Image	m_imgFinishedNode;
	/** Finished nodes foreground color */
	private Color	m_finishedColor;

	/**************************************************************************
	 * Constructor.
	 *************************************************************************/
	public CallstackLabelProvider()
	{
		ImageDescriptor descr = Activator
		        .getImageDescriptor("icons/16x16/procedure.jpg");
		m_imgProc = descr.createImage();
		descr = Activator
		        .getImageDescriptor("icons/16x16/liveExecutionNode.png");
		m_imgLiveNode = descr.createImage();
		descr = Activator
		        .getImageDescriptor("icons/16x16/finishedExecutionNode.png");
		m_imgFinishedNode = descr.createImage();

		m_finishedColor = new Color(Display.getDefault(), 128, 128, 128);
	}

	/**************************************************************************
	 * Obtain the text corresponding to the given element.
	 *************************************************************************/
	@Override
	public String getText(Object obj)
	{
		return obj.toString();
	}

	/**************************************************************************
	 * Obtain the image corresponding to the given element.
	 *************************************************************************/
	@Override
	public Image getImage(Object obj)
	{
		CallstackNode node = (CallstackNode) obj;
		Image result = null;
		switch (node.getType())
		{
		case ROOT:
			result = m_imgProc;
			break;
		case EXECUTION:
			result = m_imgLiveNode;
			if (node.isFinished())
			{
				result = m_imgFinishedNode;
			}
			break;
		default:
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			break;
		}
		return result;
	}

	/**************************************************************************
	 * {@link IColorProvider} methods
	 *************************************************************************/
	@Override
	public Color getForeground(Object element)
	{
		CallstackNode node = (CallstackNode) element;
		Color result = null;
		switch (node.getType())
		{
		case EXECUTION:
			boolean finished = ((CallstackExecutionNode) node).isFinished();
			result = finished ? m_finishedColor : null;
			break;
		default:
			break;
		}
		return result;
	}

	@Override
	public Color getBackground(Object element)
	{
		return null;
	}

	/**************************************************************************
	 * Disposal.
	 *************************************************************************/
	@Override
	public void dispose()
	{
		m_imgProc.dispose();
		m_imgLiveNode.dispose();
		m_finishedColor.dispose();
	}
}
