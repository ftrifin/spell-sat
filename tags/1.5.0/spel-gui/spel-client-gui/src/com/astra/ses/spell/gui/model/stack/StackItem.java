///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.stack
// 
// FILE      : StackItem.java
//
// DATE      : 2008-11-21 08:55
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.gui.model.stack;

import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.procs.model.ProcedureCode;


/*******************************************************************************
 * @brief An item in the tree view of the NavigationView. Represents a single
 *        procedure file.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class StackItem
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the procedure identifier */
	private String m_codeId;
	/** Holds the procedure name */
	private String m_line;
	/** Holds the parent item */
	private StackItem m_parent;
	/** Holds the list of children items */
	private Map<String,StackItem> m_children;
	/** Holds the procedure status */
	private ExecutorStatus m_status;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public StackItem()
	{
		m_codeId = "(no stack)";
		m_line = "";
		m_parent = null;
		m_children = new HashMap<String,StackItem>();
		m_status = ExecutorStatus.LOADED;
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param id
	 *            The procedure identifier
	 * @param name
	 *            The procedure name
	 **************************************************************************/
	public StackItem(ProcedureCode model )
	{
		m_codeId = model.getCodeId();
		m_line = model.getStackPosition().lastElement();
		m_parent = null;
		m_children = new HashMap<String,StackItem>();
		buildModel(model);
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param id
	 *            The procedure identifier
	 * @param name
	 *            The procedure name
	 **************************************************************************/
	public StackItem( String instanceId, ProcedureCode model )
	{
		m_codeId = instanceId;
		m_line = model.getStackPosition().lastElement();
		m_parent = null;
		m_children = new HashMap<String,StackItem>();
		buildModel(model);
	}

	/***************************************************************************
	 * Constructor
	 * 
	 * @param id
	 *            The procedure identifier
	 * @param name
	 *            The procedure name
	 **************************************************************************/
	public StackItem(String codeId, String line )
	{
		m_codeId = codeId;
		m_line = line;
		m_parent = null;
		m_children = new HashMap<String,StackItem>();
	}

	/***************************************************************************
	 * Obtain the procedure item identifier
	 * 
	 * @return The procedure item identifier
	 **************************************************************************/
	public String getId()
	{
		return m_codeId;
	}

	/***************************************************************************
	 * Obtain the procedure item line
	 * 
	 * @return The procedure item line
	 **************************************************************************/
	public String getLine()
	{
		return m_line;
	}

	/***************************************************************************
	 * Obtain the procedure id
	 * 
	 * @return The procedure id
	 **************************************************************************/
	public String getProc()
	{
		return m_codeId;
	}

	/***************************************************************************
	 * Set the procedure name
	 * 
	 * @param id
	 *          The procedure name
	 **************************************************************************/
	public void setProc(String name)
	{
		//TODO
	}

	/***************************************************************************
	 * Set the procedure id
	 * 
	 * @param id
	 *          The procedure id
	 **************************************************************************/
	public void setId(String id)
	{
		m_codeId = id;
	}

	/***************************************************************************
	 * Set the procedure item line
	 * @param line
	 *          The procedure item line
	 **************************************************************************/
	public void setLine(String line)
	{
		m_line = line;
	}

	/***************************************************************************
	 * Get the procedure status
	 * 
	 * @return 
	 *          The procedure status
	 **************************************************************************/
	public ExecutorStatus getStatus()
	{
		return m_status;
	}

	/***************************************************************************
	 * Set the procedure status
	 * 
	 * @param status
	 *          The procedure status
	 **************************************************************************/
	public void setStatus( ExecutorStatus status )
	{
		m_status = status;
	}

	/***************************************************************************
	 * Set the item parent node
	 * 
	 * @param parent
	 *            The parent node
	 **************************************************************************/
	public void setParent(StackItem parent)
	{
		m_parent = parent;
	}

	/***************************************************************************
	 * Obtain the parent node
	 * 
	 * @return The parent group
	 **************************************************************************/
	public StackItem getParent()
	{
		return m_parent;
	}
	
	/***************************************************************************
	 * Check if this group has children
	 * 
	 * @return True if it has children
	 **************************************************************************/
	public boolean hasChildren()
	{
		return m_children.size() > 0;
	}

	/***************************************************************************
	 * Check if this item is leaf
	 * 
	 * @return True if it has no children
	 **************************************************************************/
	public boolean isLeaf()
	{
		return !hasChildren();
	}
	
	/***************************************************************************
	 * Check if the item is a root
	 * 
	 * @return Always true
	 **************************************************************************/
	public boolean isRoot()
	{
		return (m_parent == null);
	}

	/***************************************************************************
	 * Obtain the string translation for this item
	 * 
	 * @return The string translation
	 **************************************************************************/
	public String toString()
	{
		String str = getProc();
		if (!getParent().isRoot())
		{
			str += " (" + m_line + ")";
		}
		return str;
	}

	/***************************************************************************
	 * Add a children item
	 * 
	 * @param child
	 *            Child to be added
	 **************************************************************************/
	void addChild(StackItem child)
	{
		m_children.put(child.getId(),child);
		child.setParent(this);
	}

	/***************************************************************************
	 * Remove a children item
	 * 
	 * @param child
	 *            Child to be removed
	 **************************************************************************/
	void removeChild(StackItem child)
	{
		if (child != null && m_children.containsKey(child.getId()))
		{
			m_children.remove(child.getId());
			child.setParent(null);
		}
	}

	/***************************************************************************
	 * Remove all children items
	 **************************************************************************/
	void reset( ProcedureCode model )
	{
		buildModel(model);
		m_children.clear();
	}

	/***************************************************************************
	 * Remove all children items
	 **************************************************************************/
	void update( ProcedureCode model )
	{
		buildModel(model);
	}

	/***************************************************************************
	 * Obtain the list of children items
	 * 
	 * @return The list of children
	 **************************************************************************/
	StackItem[] getChildren()
	{
		if (hasChildren())
		{
			StackItem[] list = new StackItem[m_children.size()];
			int count= 0;
			for (String id : m_children.keySet())
			{
				list[count] = m_children.get(id);
				count++;
			}
			return list;
		}
		else
		{
			return new StackItem[0];
		}
	}

	/***************************************************************************
	 * Build or update the procedure structure model
	 * @param model Procedure code model
	 **************************************************************************/
	private void buildModel( ProcedureCode model )
	{
		Map<String,ProcedureCode> subprocs = null;
		if (model != null)
		{
			// If this model is for me:
			if (model.getCodeId().equals(m_codeId.split("#")[0]))
			{
				// Update my info first
				int idx = model.getStackPosition().indexOf(m_codeId.split("#")[0]);
				m_line = model.getStackPosition().get(idx+1);
				
				// Now update subprocs, if any
				subprocs = model.getSubProcs();
				if (subprocs != null)
				{
					// For each subproc, find my corresponding child
					for(String sproc : subprocs.keySet())
					{
						// Get the corresponding child code
						ProcedureCode childCode = model.getCodeById(sproc);
						if (childCode == null)
						{
							Logger.error("Unable to find child code of " + model.getCodeId()+ ": " + sproc, Level.PROC, this);
							return;
						}
						// If the child does exist, update the child with the child code
						if (m_children.containsKey(sproc))
						{
							m_children.get(sproc).buildModel(childCode);
						}
						else
						{
							// If the child is not there AND i am the parent, create
							// the corresponding item
							String parentCode = childCode.getParentCode();
							if (parentCode == null)
							{
								Logger.error("Unable to find parent code of " + childCode.getCodeId(), Level.PROC, this);
							}
							else if (parentCode.equals(m_codeId.split("#")[0]))
							{
								StackItem item = new StackItem(childCode);
								addChild(item);
							}
						}
						
					}
				}
			}
		}
	}

}
