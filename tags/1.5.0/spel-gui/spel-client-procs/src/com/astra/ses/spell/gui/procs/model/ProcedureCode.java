///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ProcedureCode.java
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

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.procs.exceptions.LoadFailed;
import com.astra.ses.spell.gui.procs.model.LineExecutionModel.ItemInfo;


/*******************************************************************************
 * @brief Represents a procedure source code script 
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ProcedureCode
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static ContextProxy s_ctx = null;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the id of the associated procedure */
	protected String m_codeId;
	/** Holds the parent code reference */
	protected ProcedureLine m_parent;
	/** Holds the unprocessed source code */
	protected Vector<String> m_sourceLines;
	/** Composed of a group of line objects*/
	protected Vector<ProcedureLine> m_codeLines;
	/** Holds the current execution line in the current stack position*/
	protected int m_currentLine;
	/** True if this code instance is the one in the current view position */
	protected boolean m_inViewPosition;
	/** Holds a fast reference of all children procedures. This field has
	 * data only if this procedure code is the root */
	protected TreeMap<String,ProcedureCode> m_subprocedures;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================
	
	/***************************************************************************
	 * Constructor
	 * 
	 * @param codeId The code identifier (identifier of the (sub)procedure)
	 * @param codeLines The source code splitted in lines
	 **************************************************************************/
	public ProcedureCode( String codeId, Vector<String> codeLines, ProcedureLine parent )
	{
		if (s_ctx==null)
		{
			s_ctx = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		}
		m_parent = parent;
		m_codeId = codeId;
		m_subprocedures = null;
		m_currentLine = 1;
		m_inViewPosition = false;
		setSourceCode(codeLines);
		Logger.debug("[c-" + m_codeId + "] Created: " + codeId, Level.PROC, this);
	}
	
	/***************************************************************************
	 * Constructor with no code. The code is obtained dynamically
	 * 
	 * @param codeId The code identifier (identifier of the (sub)procedure)
	 **************************************************************************/
	public ProcedureCode( String codeId, ProcedureLine parent )
	{
		if (s_ctx==null)
		{
			s_ctx = (ContextProxy) ServiceManager.get(ContextProxy.ID);
		}
		m_parent = parent;
		m_codeId = codeId;
		obtainProcedureCode();
		if (m_codeLines == null)
		{
			throw new LoadFailed("Unable to obtain procedure code");
		}
		Logger.debug("[c-" + m_codeId + "] Created (2): " + codeId, Level.PROC, this);
	}
	
	/***************************************************************************
	 * Obtain the code identifier
	 * @return The code identifier
	 **************************************************************************/
	public String getCodeId()
	{
		return m_codeId;
	}
	
	/***************************************************************************
	 * Return the requested child line
	 * @param lineNo The line number
	 * @return The requested line or null
	 **************************************************************************/
	public ProcedureLine getLine( int lineNo )
	{
		if ((lineNo>=0) && m_codeLines.size()>=lineNo)
		{
			return m_codeLines.elementAt(lineNo);
		}
		return null;
	}

	/***************************************************************************
	 * Obtain the code lines 
	 * @return The code lines
	 */
	@SuppressWarnings("unchecked")
	public Vector<ProcedureLine> getLines()
	{
		Vector<ProcedureLine> lines = (Vector<ProcedureLine>) m_codeLines.clone();
		lines.removeElementAt(0);
		return lines;
	}

	/***************************************************************************
	 * Obtain the source code corresponding to the given identifier 
	 * @param codeId The code identifier
	 * @return The procedure code
	 **************************************************************************/
	public ProcedureCode getCodeById( String codeId )
	{
		if (m_codeId.equals(codeId)) return this;
		if (m_subprocedures != null)
		{
			if (m_subprocedures.containsKey(codeId))
			{
				return m_subprocedures.get(codeId);
			}
		}
		Logger.warning("[c-" + m_codeId + "] No code found!: " + codeId, Level.PROC, this );
		return null;
	}

	/***************************************************************************
	 * Obtain the set of subprocedures
	 * @return
	 **************************************************************************/
	public Map<String,ProcedureCode> getSubProcs()
	{
		return m_subprocedures;
	}

	/***************************************************************************
	 * Get stack position
	 * @return The stack position
	 * 
	 * The stack position is in the form PROC1:34:PROC2:45:PROC3:45 where PROC1 
	 * shall match the id of this code. The stack is composed using the current
	 * line information.
 	 **************************************************************************/
	public Vector<String> getStackPosition()
	{
		Vector<String> stackPosition = new Vector<String>();
		stackPosition.add(m_codeId);
		stackPosition.add(Integer.toString(m_currentLine));
		ProcedureLine line = getLine(m_currentLine);
		if (line!=null)
		{
			Vector<String> lineStack = line.getStackPosition();
			if (lineStack.size()>0)
			{
				stackPosition.addAll(lineStack);
			}
		}
		//Logger.debug("[c-"+ m_codeId + "] Stack position: " + stackPosition, Level.PROC, this);
		return stackPosition;
	}

	/***************************************************************************
	 * Get view position
	 * @return The view position
	 * 
	 * The view position corresponds to the code identifier in the call stack
	 * where whe procedure presentations should remain in during the procedure 
	 * execution. This is the step-over mechanism. The view position is determined
	 * by the executor (server side) and identified by the $ marker in the 
	 * stack position.
 	 **************************************************************************/
	public Vector<String> getViewPosition()
	{
		Vector<String> viewPosition = new Vector<String>();
		viewPosition.add(m_codeId);
		viewPosition.add(Integer.toString(m_currentLine));
		if (!m_inViewPosition)
		{
			ProcedureLine line = getLine(m_currentLine);
			if (line!=null)
			{
				Vector<String> lineViewPosition = line.getViewPosition();
				if (lineViewPosition.size()>0)
				{
					viewPosition.addAll(lineViewPosition);
				}
			}
		}
		return viewPosition;
	}

	/***************************************************************************
	 * Obtain the parent code id
	 * @return parent code id or null
	 **************************************************************************/
	public String getParentCode()
	{
		if (m_parent!=null)
		{
			return m_parent.getParentCode();
		}
		else
		{
			return null;
		}
	}
	
	/***************************************************************************
	 * Obtain the current line
	 * @return the current line
	 **************************************************************************/
	public int getCurrentLine()
	{
		return m_currentLine;
	}

	/***************************************************************************
	 * Register a subprocedure 
	 * 
	 * @param codeId The identifier of the code
	 * @param code The code object
	 **************************************************************************/
	void registerSubprocedure( String codeId, ProcedureCode code )
	{
		if (m_subprocedures == null)
		{
			m_subprocedures = new TreeMap<String,ProcedureCode>();
		}
		if (!m_subprocedures.containsKey(codeId))
		{
			Logger.debug( "[c-" + m_codeId + "] Register subprocedure: " + codeId, Level.PROC, this);
			m_subprocedures.put(codeId,code);
		}
	}

	/***************************************************************************
	 * Add procedure code
	 * 
	 * @param stack Position where add the code
	 * @param code The source code
	 **************************************************************************/
	void addProcedureCode( Vector<String> stack, Vector<String> code )
	{
		if (stack.size()<2)
		{
			Logger.error("[c-"+ m_codeId + "] Bad stack: " + stack, Level.PROC, this);
			return;
		}
		String firstElement = StackHelper.getStackElement(stack, 0); 
		if (!firstElement.equals(m_codeId))
		{
			Logger.error("[c-"+ m_codeId + "] Stack does not match code: " + firstElement + "<>" + m_codeId,Level.PROC, this);
		}
		else
		{
			try
			{
				int lineNo = Integer.parseInt(stack.get(1));
				ProcedureLine line = getLine(lineNo);
				if (line != null)
				{
					Logger.debug("[c-"+ m_codeId + "] Add code to position: " + m_codeId + ":" + lineNo, Level.PROC, this);
					Vector<String> subStack = StackHelper.getSubStack( stack );
					if (subStack.size()>0)
					{
						line.addCode( subStack, code );
					}
				}
				else
				{
					Logger.error("[c-"+ m_codeId + "] Stack does not match line: " + m_codeId + ":" + lineNo,Level.PROC, this);
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	/***************************************************************************
	 * Set stack position
	 * @param stack The stack position
	 * 
	 * The stack position is in the form PROC1:34:$PROC2:45:PROC3:45 where PROC1 
	 * shall match the id of this code. The stack is decomposed and this call
	 * is forwarded to children line (34 in the example). This line should have
	 * the children code PROC2. Once PROC2 is found, this call is forwarded to 
	 * it, and the cycle repeats. In the last procedure, the current line of the 
	 * code will be set.
	 * 
	 * The $ marker means that the procedure views shall keep showing this code
	 * (if this is applicable regarding the nature of the view). This marker
	 * does not affect the structure of the execution model itself.
 	 **************************************************************************/
	void setStackPosition( Vector<String> stack )
	{
		if (stack.size()<2)
		{
			Logger.error("[c-"+ m_codeId + "] Bad stack: " + stack,Level.PROC,this);
			return;
		}
		String firstElement = StackHelper.getStackElement(stack,0);
		if (!firstElement.equals(m_codeId))
		{
			Logger.error("[c-"+ m_codeId + "] Stack does not match code: " + stack.firstElement() + "<>" + m_codeId,Level.PROC, this);
		}
		else
		{
			try
			{
				int lineNo = Integer.parseInt(stack.get(1));
				ProcedureLine line = getLine(lineNo);
				if (line != null)
				{
					setCurrentLine(lineNo);
					//Logger.debug("["+ m_codeId + "] Set stack position: " + m_codeId + ":" + m_currentLine, Level.PROC, this);
					Vector<String> subStack = StackHelper.getSubStack( stack );
					if (subStack.size()>0)
					{
						//Logger.debug("["+ m_codeId + "] Set substack position: " + subStack, Level.PROC, this);
						line.setStackPosition( subStack );
					}
				}
				else
				{
					Logger.error("[c-"+ m_codeId + "] Stack uses unexistent line: " + m_codeId + ":" + lineNo,Level.PROC, this);
				}
				// View position flag: if the first element of the stack contains
				// the marker, this code is the one to be shown.
				m_inViewPosition = StackHelper.isViewElement(stack, 0);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}


	/***************************************************************************
	 * Obtain the source code corresponding to the given stack position 
	 * @param stack Stack position
	 * @return The procedure code
	 **************************************************************************/
	ProcedureCode getCodeAt( Vector<String> stack )
	{
		if (stack.size()<2)
		{
			Logger.error("[c-"+ m_codeId + "] Bad stack: " + stack,Level.PROC, this);
			return null;
		}
		String firstElement = StackHelper.getStackElement(stack,0);
		if (!firstElement.equals(m_codeId))
		{
			Logger.error("[c-"+ m_codeId + "] Stack does not match code: " + stack.firstElement() + "<>" + m_codeId,Level.PROC, this);
		}
		else
		{
			try
			{
				int lineNo = Integer.parseInt(stack.get(1));
				ProcedureLine line = getLine(lineNo);
				if (line != null)
				{
					//Logger.debug("["+ m_codeId + "] Get code at: " + m_codeId + ":" + m_currentLine, Level.PROC, this);
					Vector<String> subStack = StackHelper.getSubStack( stack );
					if (subStack.size()>0)
					{
						//Logger.debug("["+ m_codeId + "] Get code at substack position: " + subStack, Level.PROC, this);
						return line.getCodeAt( subStack );
					}
					else
					{
						//Logger.debug("["+ m_codeId + "] Got current code: " + m_codeId, Level.PROC, this);
						return this;
					}
				}
				else
				{
					Logger.error("[c-"+ m_codeId + "] Stack does not match line: " + m_codeId + ":" + lineNo,Level.PROC, this);
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}

	/***************************************************************************
	 * Obtain the parent line, if any
	 * @return The parent line or null
	 **************************************************************************/
	ProcedureLine getParent()
	{
		return m_parent;
	}

	/***************************************************************************
	 * Obtain the root of the code
	 * @return The root of the code
	 **************************************************************************/
	ProcedureCode getRoot()
	{
		if (m_parent == null)
		{
			return this;
		}
		else
		{
			return m_parent.getRoot();
		}
	}

	/***************************************************************************
	 * Add notification to this code. Will be redirected to the corresponding
	 * child code if needed
	 * @param data Notification data
	 **************************************************************************/
	void addNotification( Vector<String> stack, ItemNotification data )
	{
		// Search for the corresponding line
		int lineNo = Integer.parseInt(stack.get(1));
		ProcedureLine line = getLine(lineNo);
		if (line != null)
		{
			Logger.debug("[c-" + m_codeId + "] Add notification for line " + lineNo, Level.GUI, this);
			line.addNotification(stack, data);
		}
		else
		{
			Logger.error("[c-"+ m_codeId + "] Discarded notification, no such line: " + lineNo, Level.PROC, this);
		}
	}
	
	/***************************************************************************
	 * Obtain item information for code view
	 * @return Item information for all lines in this code
	 **************************************************************************/
	Vector<ItemInfo> getItemData(boolean onlyLatest)
	{
		// Put all the lines item information together
		Vector<ItemInfo> info = new Vector<ItemInfo>();
		for(ProcedureLine line : m_codeLines)
		{
			Vector<ItemInfo> lineInfo = line.getItemData(onlyLatest);
			if (lineInfo!=null)
			{
				info.addAll(lineInfo);
			}
		}
		return info;
	}
	
	/***************************************************************************
	 * Obtain execution completion information for all this code
	 * @return Completion information
	 **************************************************************************/
	String getCompletionString( boolean onlyLatest )
	{
		int totalElements = 0;
		int successElements = 0;
		// Put all the lines item information together
		for(ProcedureLine line : m_codeLines)
		{
			totalElements += line.getNumInfoElements( onlyLatest );
			successElements += line.getNumSuccessInfoElements( onlyLatest );
			
		}
		return "(" + successElements + "/" + totalElements + ")";
	}
	
	/***************************************************************************
	 * Obtain execution completion information for a given line
	 * @return Completion information
	 **************************************************************************/
	String getCompletionString( int lineNo, boolean onlyLatest )
	{
		ProcedureLine line = getLine(lineNo);
		if (line!=null)
		{
			return "(" + line.getNumSuccessInfoElements(onlyLatest) + "/" + line.getNumInfoElements(onlyLatest) + ")";
		}
		return "(?/?)";
	}

	/***************************************************************************
	 * Obtain number of success elements in all this code
	 * @return Completion information
	 **************************************************************************/
	int getNumSuccessInfoElements( boolean onlyLatest )
	{
		int successElements = 0;
		// Put all the lines item information together
		for(ProcedureLine line : m_codeLines)
		{
			successElements += line.getNumSuccessInfoElements( onlyLatest );
			
		}
		return successElements;
	}

	/***************************************************************************
	 * Obtain number of elements in all this code
	 * @return Completion information
	 **************************************************************************/
	int getNumInfoElements( boolean onlyLatest )
	{
		int totalElements = 0;
		// Put all the lines item information together
		for(ProcedureLine line : m_codeLines)
		{
			totalElements += line.getNumInfoElements( onlyLatest );
			
		}
		return totalElements;
	}
	
	/***************************************************************************
	 * Reset this code
	 **************************************************************************/
	void reset()
	{
		Logger.debug("[c-"+ m_codeId + "] Reset model", Level.PROC, this);
		m_subprocedures = null;
		m_parent = null;
		m_currentLine = 1;
		obtainProcedureCode();
	}

	/***************************************************************************
	 * Set the current execution line
	 * 
	 * @param lineno
	 * 		The line number
	 **************************************************************************/
	void setCurrentLine( int lineNo )
	{
		if (m_currentLine != lineNo)
		{
			ProcedureLine line = getLine(lineNo);
			if (line!=null)
			{
				//Logger.debug("[c-"+ m_codeId + "] Current line: " + lineNo, Level.PROC, this);
				m_currentLine = lineNo;
				line.visit();
			}
			else
			{
				Logger.error("[c-"+ m_codeId + "] No such line: " + lineNo, Level.PROC, this);
			}
		}
	}

	/***************************************************************************
	 * Set the procedure source code data
	 * @param codeLines The source code splitted in lines
	 **************************************************************************/
	void setSourceCode( Vector<String> codeLines )
	{
		m_sourceLines = codeLines;
		m_codeLines = new Vector<ProcedureLine>();
		// Add a blank line. this is easier for modelling table
		ProcedureLine pline = new ProcedureLine( this, m_codeLines.size(), " " );
		m_codeLines.addElement(pline);
		for(String line : codeLines)
		{
			pline = new ProcedureLine( this, m_codeLines.size(), line );
			m_codeLines.addElement(pline);
		}
	}

	/***************************************************************************
	 * Get the procedure source code data
	 **************************************************************************/
	Vector<String> getSourceCode()
	{
		return m_sourceLines;
	}
	
	/***************************************************************************
	 * Obtain the procedure code
	 * 
	 * @param procId
	 *            The procedure identifier
	 **************************************************************************/
	void obtainProcedureCode()
	{
		Logger.debug( "[c-"+ m_codeId + "] Obtain procedure code for " + m_codeId, Level.PROC, this);
		// Check if the code is already abailable in the model tree
		Vector<String> codeLines = null;
		if (m_parent != null)
		{
			ProcedureCode code = m_parent.getRoot().getCodeById(m_codeId);
			if (code != null)
			{
				codeLines = code.getSourceCode();
			}
		}
		if (codeLines == null)
		{
			codeLines = s_ctx.getProcedureCode(m_codeId);
		}
		if (codeLines != null)
		{
			Logger.debug("[c-"+ m_codeId + "] Creating code model for " + m_codeId, Level.PROC, this);
			setSourceCode(codeLines);
		}
		else
		{
			Logger.error("[c-"+ m_codeId + "] Unable to obtain source code", Level.PROC, this);
		}
	}
}
