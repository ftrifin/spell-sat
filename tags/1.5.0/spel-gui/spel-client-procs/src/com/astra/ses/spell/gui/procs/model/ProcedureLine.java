///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ProcedureLine.java
//
// DATE      : 2008-11-24 08:34
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
package com.astra.ses.spell.gui.procs.model;

import java.util.Vector;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.procs.model.LineExecutionModel.ItemInfo;



/*******************************************************************************
 * @brief Represents a procedure source code line 
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ProcedureLine 
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the line number in the source code */
	private int m_lineNo;
	/** Holds the number of executions for this line */
	private int m_executed;
	/** Holds the source code of this line */
	private String m_source;
	/** Holds the parent code of this line */
	private ProcedureCode m_parent;
	/** Holds the child code of this line, if any */
	private ProcedureCode m_child;
	/** Holds the set of lines to retrieve data from (used in local step overs )*/
	private Vector<Integer> m_localInfoSources;
	/** Holds the execution information model of this line */
	private LineExecutionModel m_executionModel;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Create a procedure line
	 * @param parent The parent code object
	 * @param lineNo The corresponding line number
	 * @param lineSource The source code
	 **************************************************************************/
	public ProcedureLine( ProcedureCode parent, int lineNo, String lineSource )
	{
		m_lineNo = lineNo;
		m_source = lineSource;
		m_source = m_source.replace("\t", "    ");
		m_parent = parent;
		m_child = null;
		m_executed = 0;
		m_localInfoSources = null;
		m_executionModel = null;
	}
	
	/***************************************************************************
	 * Obtain the root of the code
	 * @return The code root
	 **************************************************************************/
	ProcedureCode getRoot()
	{
		return m_parent.getRoot();
	}

	/***************************************************************************
	 * Add new source code to the procedure code model. Take into account
	 * that the line may have a child already, and the code goes below that 
	 * child.
	 * @param stack The stack position to place the code in
	 * @param code The source code
	 **************************************************************************/
	void addCode( Vector<String> stack, Vector<String> code )
	{
		String firstElement = StackHelper.getStackElement(stack,0);
		if (m_child != null)
		{
			// If there is a child already, it is assumed that the new code goes
			// below it. But, the first procedure of the given stack shall match
			// the child code id. Otherwise we are trying to add code in the wrong
			// place!
			if (!firstElement.equals(m_child.getCodeId()))
			{
				Logger.error(this + " Cannot add code: child id does not match: " + firstElement + "<>" + m_child.getCodeId(), Level.PROC, this);
				return;
			}
			Logger.debug(this + " Adding new child to child: " + firstElement, Level.PROC, this);
			m_child.addProcedureCode(stack, code);
		}
		else
		{
			// It is assumed that the code will be added as this child. 
			// The first proc of the passed stack is the child code id
			Logger.debug(this + " Creating new child: " + firstElement, Level.PROC, this);
			m_child = new ProcedureCode(firstElement,code,this);
			// Register a new subprocedure in the procedure execution model root
			getRoot().registerSubprocedure( firstElement, m_child );
			if (getRoot() != m_parent)
			{
				m_parent.registerSubprocedure( firstElement, m_child );
			}
		}
	}
	
	/***************************************************************************
	 * Set the stack position. The stack has the format PROC1:34:PROC2:45. If a
	 * procedure name is given in the stack, there are two posibilities:
	 * 
	 * A) There is a child code already in this line, therefore the procedure
	 *    identifier shall match the one given in the stack. If so, the method
	 *    setStackPosition of the child code is called with the same stack (the
	 *    procedure line acts as a bridge or connector between procedure code
	 *    objects here)
	 *    
	 * B) There is no child code in this line.
	 * 
	 *  	B.1) The call is local, that is, the code id is the same as the 
	 *  		 parent code. The setStackPosition is directy called.
	 *  
	 *      B.2) A new child code is created and stored as a child. Then the 
	 *      setStackPosition method of the child code is called.
	 *     
	 * @param stack The current stack
	 **************************************************************************/
	void setStackPosition( Vector<String> stack )
	{
		if (stack.size()>=2)
		{
			String subproc = StackHelper.getStackElement(stack,0);
			if (subproc.equals(m_parent.getCodeId()))
			{
				// CASE B.1
				//Logger.debug(this + " Local jump: " + stack, Level.PROC, this);
				m_parent.setStackPosition(stack);
			}
			else
			{
				// CASE A
				if (m_child != null)
				{
					if (!subproc.equals(m_child.getCodeId()))
					{
						Logger.error(this + " Stack does not match: " + stack + "<>" + m_child.getCodeId(), Level.PROC, this);
					}
					else
					{
						m_child.setStackPosition(stack);
					}
				}
				// CASE B.2
				else
				{
					Logger.debug(this + " Creating new child (2): " + subproc, Level.PROC, this);
					m_child = new ProcedureCode(subproc,this);
					m_child.setStackPosition(stack);
					// Register the new subprocedure in the procedure execution model root
					getRoot().registerSubprocedure( subproc, m_child );
					if (getRoot() != m_parent)
					{
						m_parent.registerSubprocedure( subproc, m_child );
					}
				}
			}
		}
		else
		{
			Logger.error(this + " Stack position with no information: " + stack, Level.PROC, this);
		}
	}

	/***************************************************************************
	 * Recover a given code object positioned at the given stack.
	 * 
	 * - If the given stack has not at least one proc ID and one line number,
	 *   return the parent code. 
	 *   
	 * - If there is at least one proc ID and line number, but there is no child
	 *   code, return the parent code.
	 *   
	 * - If there is at least one proc ID and line number, and there is a child
	 *   code, propagate the call to the child code.
	 *   
	 * @param stack The stack corresponding to the code to be retrieved.
	 * @return The code object
	 **************************************************************************/
	ProcedureCode getCodeAt( Vector<String> stack )
	{
		if (stack.size()>=2)
		{
			String subproc = StackHelper.getStackElement(stack,0);
			if (m_child != null)
			{
				if (!subproc.equals(m_child.getCodeId()))
				{
					Logger.error(this + " Stack does not match: " + stack + "<>" + m_child.getCodeId(), Level.PROC, this);
				}
				else
				{
					return m_child.getCodeAt(stack);
				}
			}
			else
			{
				return m_parent;
			}
		}
		return m_parent;
	}
	
	/***************************************************************************
	 * Obtain the current stack position. The stack position is composed using
	 * the code object ids and line numbers. If there is child code the call
	 * is propagated to it. Otherwise, this line does not contribute to the
	 * stack. The line number is added by the parent code.
	 * @return The stack position of child code, or an empty string
	 **************************************************************************/
	Vector<String> getStackPosition()
	{
		Vector<String> stackPosition = new Vector<String>();
		if (m_child!=null)
		{
			stackPosition.addAll(m_child.getStackPosition());
		}
		return stackPosition;
	}

	/***************************************************************************
	 * Obtain the current view position. 
	 **************************************************************************/
	Vector<String> getViewPosition()
	{
		Vector<String> viewPosition = new Vector<String>();
		if (m_child!=null)
		{
			viewPosition.addAll(m_child.getViewPosition());
		}
		return viewPosition;
	}

	/***************************************************************************
	 * Add a notification to this line. If the code id does not math the id of
	 * the parent code, forward the notification to child code, if any.
	 * @param data Notification data
	 **************************************************************************/
	void addNotification( Vector<String> stack, ItemNotification data )
	{
		// Check if this notification is for this very same line or is for
		// the child code
		if (stack.size()<2)
		{
			Logger.error(this + " Bad stack: " + stack, Level.PROC, this);
			return;
		}
		
		String stackStr = "";
		for(String elem : stack) stackStr += ":" + elem;
		Logger.debug(this + " Add notification, stack " + stackStr, Level.GUI, this);
		
		// The stack has the form <proc1>,<line1>,<proc2>,<line2>,...

		//----------------------------------------------------------------------
		// 1. The first pair SHALL match this line (match parent and line no)
		//----------------------------------------------------------------------
		String firstElement = StackHelper.getStackElement(stack,0);
		if (!firstElement.equals(m_parent.getCodeId()))
		{
			Logger.error( this + " Unmatched proc notification for this line: " + stack.firstElement() , Level.GUI, this);
			return;
		}
		if (!stack.get(1).equals(Integer.toString(m_lineNo)))
		{
			Logger.error( this + " Unmatched lineno notification for this line: " + stack.get(1) , Level.GUI, this);
			return;
		}

		// Create the execution model if needed
		if (m_executionModel==null)
		{
			m_executionModel = new LineExecutionModel(this);
		}

		//----------------------------------------------------------------------
		// 2. If the stack has only two elements, it means that the notification
		//    is for this very same line
		//----------------------------------------------------------------------
		if (stack.size()==2)
		{
			Logger.debug(this + " notification added to myself", Level.GUI, this);
			m_executionModel.addNotification(data);
		}
		//----------------------------------------------------------------------
		// 3. If the stack has more than two elements, it means that it is for
		//    a child code or another line in this proc (local function stepover)
		//----------------------------------------------------------------------
		else
		{
			// First remove the first two elements
			Vector<String> subStack = new Vector<String>();
			for(int idx=2; idx<stack.size(); idx++)
			{
				subStack.add(stack.get(idx));
			}
			if (StackHelper.getStackElement(subStack,0).equals(m_parent.getCodeId()))
			{
				//------------------------------------------------------------------
				// 3.1 If the code id is the same as the parent, we are doing
				//     local stepover
				//------------------------------------------------------------------
				int stackLineNo = Integer.parseInt(subStack.get(1));
				Logger.debug(this + " Add local source: line" + stackLineNo, Level.GUI, this);
				// Add the notification to the sibling line and store its
				// reference so that we will get the summary
				if (m_localInfoSources == null)
				{
					m_localInfoSources = new Vector<Integer>();
				}
				if (!m_localInfoSources.contains(stackLineNo))
				{
					m_localInfoSources.addElement(stackLineNo);
				}
				m_parent.addNotification(subStack, data);
			}
			else
			{
				//------------------------------------------------------------------
				// 3.2 If the code id is not the same as the parent, we are doing
				//     external stepover. This means that the notification
				//     goes to child code.
				//------------------------------------------------------------------
				if (m_child==null)
				{
					// Children to be created dynamically
					String childProc = subStack.firstElement();
					int childProcLine = Integer.parseInt(subStack.get(1));
					Logger.debug(this + " Child code not available, creating " + childProc, Level.PROC, this);
					m_child = new ProcedureCode(childProc,this);
					m_child.setCurrentLine(childProcLine);
				}
				// Place the notification now
				Logger.debug(this + " notification added to child", Level.GUI, this);
				m_child.addNotification(subStack,data);
			}
		}
	}
	
	/***************************************************************************
	 * Reset the execution model and child code, if any
	 **************************************************************************/
	void reset()
	{
		if (m_child!=null)
		{
			Logger.debug(this + " Resetting child", Level.GUI, this);
			m_child.reset();
		}
		if (m_localInfoSources != null)
		{
			Logger.debug(this + " Clearing local sources", Level.GUI, this);
			m_localInfoSources.clear();
			m_localInfoSources = null;
		}
		if (m_executionModel != null)
		{
			Logger.debug(this + " Clear execution model", Level.GUI, this);
			m_executionModel.clear();
			m_executionModel = null;
		}
		m_executed = 0;
	}

	/***************************************************************************
	 * Execute the line
	 **************************************************************************/
	void visit()
	{
		m_executed++;
	}
	
	/***************************************************************************
	 * Get the parent code id
	 * @return The parent code id
	 **************************************************************************/
	public String getParentCode()
	{
		if (m_parent!=null)
		{
			return m_parent.getCodeId();
		}
		return null;
	}

	/***************************************************************************
	 * Get the number of times the line was executed
	 * @return Number of executions
	 **************************************************************************/
	public int getNumVisits()
	{
		return m_executed;
	}
	
	/***************************************************************************
	 * Get the corresponding line number
	 * @return The line number
	 **************************************************************************/
	public int getLineNum()
	{
		return m_lineNo;
	}

	/***************************************************************************
	 * Get the execution information of this line in a suitable form for views
	 * @return The execution information
	 **************************************************************************/
	public Vector<ItemInfo> getItemData(boolean onlyLatest)
	{
		if (m_child != null)
		{
			// This line leads to children proc, so get the information from
			// that subproc
			Vector<ItemInfo> info = m_child.getItemData(onlyLatest); 
			Logger.debug(this + " Retrieving data from child: " + info.size(), Level.GUI, this);
			return info;
		}
		else if (m_localInfoSources != null)
		{
			// This line leads to a locally defined function and we have
			// step over it, so we gather information from those lines
			Vector<ItemInfo> srcInfo = new Vector<ItemInfo>();
			Vector<ItemInfo> lineInfo = null;
			for(Integer lineNo : m_localInfoSources)
			{
				lineInfo = m_parent.getLine(lineNo).getItemData(onlyLatest);
				if (lineInfo!=null)
				{
					Logger.debug(this + " Append data from local source " + lineNo + ": " + lineInfo.size(), Level.GUI, this);
					srcInfo.addAll(lineInfo);
				}
			}
			if (srcInfo.size()>0) 
			{
				return srcInfo;
			}
			else
			{
				return null;
			}
		}
		else
		{
			if (m_executionModel==null)
			{
				// The model is empty (nothing executed)
				return null;
			}
			else
			{
				// This line is simple, get data from its execution model
				Vector<ItemInfo> info = m_executionModel.getViewItemData(onlyLatest);
				if (info!=null && info.size()>0) 
					Logger.debug(this + " Provide data from myself: " + info.size(), Level.GUI, this);
				return info;
			}
		}
	}
	
	/***************************************************************************
	 * Get the number of information elements for view rendering.
	 * @return The number of information elements.
	 **************************************************************************/
	public int getNumInfoElements( boolean onlyLatest )
	{
		if (m_child != null)
		{
			// This line leads to children proc, so get the information from
			// that subproc
			return m_child.getNumInfoElements( onlyLatest );
		}
		else if (m_localInfoSources != null)
		{
			// This line leads to a locally defined function and we have
			// step over it, so we gather information from those lines
			int totalCount   = 0;
			for(Integer lineNo : m_localInfoSources)
			{
				ProcedureLine localLine = m_parent.getLine(lineNo);
				totalCount   += localLine.getNumInfoElements( onlyLatest );
			}
			return totalCount;
		}
		else
		{
			if (m_executionModel == null)
			{
				return 0;
			}
			else
			{
				return m_executionModel.getElementCount( onlyLatest );
			}
		}
	}

	/***************************************************************************
	 * Get the number of information elements which are success.
	 * @return The number of success information elements.
	 **************************************************************************/
	public int getNumSuccessInfoElements( boolean onlyLatest )
	{
		if (m_child != null)
		{
			// This line leads to children proc, so get the information from
			// that subproc
			return m_child.getNumSuccessInfoElements( onlyLatest );
		}
		else if (m_localInfoSources != null)
		{
			// This line leads to a locally defined function and we have
			// step over it, so we gather information from those lines
			int successCount = 0;
			for(Integer lineNo : m_localInfoSources)
			{
				ProcedureLine localLine = m_parent.getLine(lineNo);
				successCount += localLine.getNumSuccessInfoElements( onlyLatest );
			}
			return successCount;
		}
		else
		{
			if (m_executionModel == null)
			{
				return 0;
			}
			else
			{
				return m_executionModel.getSuccessCount( onlyLatest );
			}
		}
	}
	
	/***************************************************************************
	 * Obtain the overall status of the line execution
	 * 
	 * @return The status corresponding to all operations referenced by this line.
	 **************************************************************************/
	public String getOverallStatus( boolean onlyLatest )
	{
		Vector<ItemInfo> allTogether = getItemData(onlyLatest);
		if (allTogether != null)
		{
			for( ItemInfo info : allTogether )
			{
				if(info.status.equals(ItemStatus.PROGRESS.getName())) 
					return ItemStatus.PROGRESS.getName();
				else if(info.status.equals(ItemStatus.SKIPPED.getName()))
					return ItemStatus.SKIPPED.getName();
				else if(info.status.equals(ItemStatus.TIMEOUT.getName()))
					return ItemStatus.FAILED.getName();
				else if(info.status.equals(ItemStatus.ERROR.getName()))
					return ItemStatus.FAILED.getName();
				else if(info.status.equals(ItemStatus.FAILED.getName()))
					return ItemStatus.FAILED.getName();
			}
			return ItemStatus.SUCCESS.getName();
		}
		return ItemStatus.UNKNOWN.getName();
	}

	/***************************************************************************
	 * Obtain the source code
	 * @return The source code
	 **************************************************************************/
	public String getSource()
	{
		return m_source;
	}

	/***************************************************************************
	 * Serialization
	 * @return Serialized line info
	 **************************************************************************/
	public String toString()
	{
		return "["+ m_parent.getCodeId() + ":" + m_lineNo + "]";
	}

}
