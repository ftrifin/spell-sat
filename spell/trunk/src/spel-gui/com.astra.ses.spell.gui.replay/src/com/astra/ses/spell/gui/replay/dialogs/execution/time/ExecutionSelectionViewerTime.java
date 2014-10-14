///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.replay.dialogs.execution.time
// 
// FILE      : ExecutionSelectionViewerTime.java
//
// DATE      : Jul 2, 2013
//
// Copyright (C) 2008, 2014 SES ENGINEERING, Luxembourg S.A.R.L.
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.replay.dialogs.execution.time;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.astra.ses.spell.gui.replay.dialogs.execution.ExecutionSelectionContentProvider;
import com.astra.ses.spell.gui.replay.dialogs.execution.ExecutionSelectionNode;
import com.astra.ses.spell.gui.replay.dialogs.execution.NodeType;


public class ExecutionSelectionViewerTime extends TreeViewer
{
	/***************************************************************************
	 * 
	 **************************************************************************/
	public ExecutionSelectionViewerTime( Tree tree )
	{
		super(tree);
		
		setComparator(new ViewerComparator()
		{
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				ExecutionSelectionNode t1 = (ExecutionSelectionNode) e1;
				ExecutionSelectionNode t2 = (ExecutionSelectionNode) e2;
				int result = 0;
				if (t1.getType().equals(NodeType.ASRUN))
				{
					result = t1.getDate().compareTo(t2.getDate());
					if (result == 0)
					{
						result = t1.getLabel().compareTo(t2.getLabel());
					}
				}
				else
				{
					result = t1.getDate().compareTo(t2.getDate());
				}
				return result;
			};
		});

		for (ExecutionSelectionColumnTime col : ExecutionSelectionColumnTime.values())
		{
			TreeColumn c = new TreeColumn(tree, col.style);
			c.setText(col.title);
			c.setWidth(col.width);
		}

		setContentProvider(new ExecutionSelectionContentProvider());
		setLabelProvider(new ExecutionSelectionLabelProviderTime());

	}
}
