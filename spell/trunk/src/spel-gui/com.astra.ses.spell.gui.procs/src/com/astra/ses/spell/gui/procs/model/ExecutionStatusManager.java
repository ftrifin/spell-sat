///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : ExecutionStatusManager.java
//
// DATE      : Nov 6, 2012
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.astra.ses.spell.gui.core.interfaces.ICoreStatusListener;
import com.astra.ses.spell.gui.core.interfaces.IStatusManager;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ClientStatus;
import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification.StackType;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IExecutionListener;
import com.astra.ses.spell.gui.procs.interfaces.model.ICodeLine;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionStatusManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

public class ExecutionStatusManager implements IExecutionStatusManager, ICoreStatusListener
{
	private static final DateFormat s_df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	/** Holds the base code identifier */
	private String m_instanceId;
	/** Holds the current code identifier */
	private String m_codeId;
	/** Holds the current line number */
	private int m_currentLineNo;
	/** Holds code lines per code identifier */
	private Map<String,List<ICodeLine>> m_lines;
	/** True if in replay mode */
	private boolean m_replay;
	/** Holds the listeners */
	private List<IExecutionListener> m_listeners;
	/** Reference to the model */
	private IProcedure m_model;
	
	/** Used to order the notifications */
	private long m_lastLineNotificationSequence;
	
	/** USed to detect processing delays */
	private long m_processingDelaySec;
	
	
	/**************************************************************************
	 * Constructor
	 * @param instanceId
	 * @param model
	 *************************************************************************/
	public ExecutionStatusManager( String instanceId, IProcedure model )
	{
		Logger.debug("Created", Level.PROC, this);
		int idx = instanceId.indexOf("#");
		if (idx != -1)
		{
			instanceId = instanceId.substring(0, idx);
		}
		m_instanceId = instanceId;
		m_codeId = instanceId;
		m_model = model;
		m_lines = new TreeMap<String,List<ICodeLine>>();
		m_lines.put(instanceId,new LinkedList<ICodeLine>());
		m_replay = false;
		m_listeners = new LinkedList<IExecutionListener>();
		m_lastLineNotificationSequence = 0;
		m_processingDelaySec = 0;
		
		IStatusManager st = (IStatusManager) ServiceManager.get(IStatusManager.class);
		st.addListener(this);
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public String getCodeId()
    {
	    return m_codeId;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public int getCurrentLineNo()
    {
	    return m_currentLineNo;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public ICodeLine getCurrentLine()
    {
	    return m_lines.get(m_codeId).get(m_currentLineNo);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public ICodeLine getLine(int lineNo)
    {
	    return m_lines.get(m_codeId).get(lineNo);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public List<ICodeLine> getLines()
    {
	    return m_lines.get(m_codeId);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void initialize(IProgressMonitor monitor)
    {
		Logger.debug("Initializing", Level.PROC, this);
	    String[] lines = m_model.getSourceCodeProvider().getSource(m_codeId, monitor);
	    int index = 1;
	    for(String source : lines)
	    {
	    	ICodeLine codeLine = new CodeLine(index, source);
	    	getLines().add(codeLine);
	    	index++;
	    }
		Logger.debug("Initialized, lines " + getLines().size(), Level.PROC, this);
	    notifyCodeChanged();
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public boolean isInReplay()
    {
	    return m_replay;
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
	public void clearNotifications()
	{
		for(ICodeLine line : getLines())
		{
			line.clearNotifications();
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onItemNotification(ItemNotification data)
    {
	    List<String> stack = data.getStackPosition();
		Logger.debug("Notified item: " + Arrays.toString(stack.toArray()), Level.PROC, this);
		
		List<ICodeLine> updatedLines = new LinkedList<ICodeLine>();
	    placeNotification( stack, data, updatedLines );
	    notifyItemsChanged(updatedLines);
    }
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	private void placeNotification( List<String> stack, ItemNotification data, List<ICodeLine> updatedLines )
	{
		Logger.debug("Place notification: " + Arrays.toString(stack.toArray()), Level.PROC, this);
		if (stack.size()>2)
		{
			int lineNo = Integer.parseInt(stack.get(1))-1;
			ICodeLine line = getLine(lineNo);
			if (line == null)
			{
				Logger.error("Cannot place item notification, no such line", Level.PROC, this);
				return;
			}
			line.onItemNotification(data);
			updatedLines.add(line);
			//notifyItemChanged(line);
			placeNotification( stack.subList(2, stack.size()), data, updatedLines);
		}
		else
		{
			int lineNo = Integer.parseInt(stack.get(1))-1;
			ICodeLine line = getLine(lineNo);
			if (line == null)
			{
				Logger.error("Cannot place item notification, no such line", Level.PROC, this);
				return;
			}
			line.onItemNotification(data);
			//notifyItemChanged(line);
			updatedLines.add(line);
		}
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void onStackNotification(StackNotification data)
    {
	    List<String> stack = data.getStackPosition();
	    if (stack.isEmpty() || stack.size()==1) return;
	    
		if (data.getStackType().equals(StackNotification.StackType.STAGE)) return;
		
		// If the code changes, update the current model
		String currentCodeId = stack.get(stack.size()-2);
		
		if (!m_codeId.equals(currentCodeId))
		{
			m_codeId = currentCodeId;
			if (!m_lines.containsKey(m_codeId))
			{
			    m_lines.put(m_codeId, new LinkedList<ICodeLine>() );
				initialize( new NullProgressMonitor() );
			}
			else
			{
				notifyCodeChanged();
			}
		}
		
		// Only on line events: if the sequence of the notification is lower than the last
		// one processed, do not NOTIFY IT to GUI components. 
		// We need to do this is messages come in bad order from the server, to avoid 
		// weird line jumping.
		
		boolean notifyGUI = true;
		if (data.getStackType().equals(StackType.LINE))
		{
			long seq = data.getSequence();
			if (seq < m_lastLineNotificationSequence)
			{
				notifyGUI = false;
			}
			else
			{
				// If we accept the notification store it so that we avoid further
				// notifications that are actually in the past
				m_lastLineNotificationSequence = seq;
			}
		}
		
	    placeExecution( stack, data.getStackType(), notifyGUI );

	    // Update processing delay
	    if (!m_replay && notifyGUI)
	    {
		    try
	        {
		    	System.out.println("NOTIFICATION TIME: " + data.getTime() );
		    	System.out.println("CURRENT TIME     : " + s_df.format( Calendar.getInstance().getTime() ));
		    	long currentTime = Calendar.getInstance().getTime().getTime();
		        long notificationTime = s_df.parse( data.getTime() ).getTime();
		        long diffUsec = Math.abs(notificationTime - currentTime);
		        long diffSec = diffUsec/1000000;
		    	System.out.println("DIFF IN SECONDS  : " + diffSec);
		        if (m_processingDelaySec != diffSec)
		        {
		        	m_processingDelaySec = diffSec;
		        	notifyDelayChanged(diffSec);
		        }
	        }
	        catch (ParseException e)
	        {
		        e.printStackTrace();
	        }
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void placeExecution( List<String> stack, StackNotification.StackType type, boolean notifyGUI )
	{
		Logger.debug("Place execution: " + Arrays.toString(stack.toArray()), Level.PROC, this);

		if (stack.size()>2)
		{
			// NOTE: the stack notifications start at index 1
			int lineNo = Integer.parseInt(stack.get(1))-1;
			ICodeLine line = getLine(lineNo);
			if (line == null)
			{
				Logger.error("Cannot place stack notification, no such line", Level.PROC, this);
				return;
			}
			line.onExecuted();
			placeExecution( stack.subList(2, stack.size()), type, notifyGUI);
		}
		else
		{
			int lineNo = Integer.parseInt(stack.get(1))-1;

			ICodeLine line = getLine(lineNo);
			if (line == null)
			{
				Logger.error("Cannot place item notification, no such line", Level.PROC, this);
				return;
			}
			line.onExecuted();
			// If the SO is inactive, notify clients
			if (!m_model.getController().getStepOverControl().isSteppingOver() && notifyGUI)
			{
				// This is the head, update the current line
				Logger.debug("Current line:" + lineNo, Level.PROC, this);
				m_currentLineNo = lineNo;
				notifyLineChanged(line);
			}
		}
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void reset()
    {
		Logger.debug("Reset model", Level.PROC, this);
	    for(ICodeLine line : getLines())
	    {
	    	line.reset();
	    }
	    for(String id : m_lines.keySet())
	    {
	    	if (!id.equals(m_instanceId))
	    	{
	    		m_lines.remove(id);
	    	}
	    }
		m_lastLineNotificationSequence = 0;
		m_replay = false;
		m_processingDelaySec = 0;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void dispose()
    {
		IStatusManager st = (IStatusManager) ServiceManager.get(IStatusManager.class);
		st.removeListener(this);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void clearBreakpoints()
    {
		Logger.debug("Clear breakpoints", Level.PROC, this);
	    for(ICodeLine line : getLines())
	    {
	    	line.removeBreakpoint();
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void setBreakpoint(int lineNo, BreakpointType type)
    {
		Logger.debug("Set breakpoint on line " + lineNo, Level.PROC, this);
	    getLine(lineNo).setBreakpoint(type);
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void setReplay(boolean replay)
    {
		Logger.debug("Set replay mode: " + replay, Level.PROC, this);
	    m_replay = replay;
	    m_lastLineNotificationSequence = 0;
	    m_processingDelaySec = 0;
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyLineChanged( ICodeLine line )
	{
		try
		{
			for(IExecutionListener listener: m_listeners)
			{
				listener.onLineChanged(line);
			}
		}
		catch(ConcurrentModificationException ignore){};
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyDelayChanged( final long delaySec )
	{
		System.err.println("PROCESSING DELAY IS " + delaySec + " seconds");
		try
		{
			for(IExecutionListener listener: m_listeners)
			{
				listener.onProcessingDelayChanged(delaySec);
			}
		}
		catch(ConcurrentModificationException ignore){};
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyCodeChanged()
	{
		try
		{
			for(IExecutionListener listener: m_listeners)
			{
				listener.onCodeChanged();
			}
		}
		catch(ConcurrentModificationException ignore){};
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	private void notifyItemsChanged( List<ICodeLine> lines )
	{
		try
		{
			for(IExecutionListener listener: m_listeners)
			{
				listener.onItemsChanged(lines);
			}
		}
		catch(ConcurrentModificationException ignore){};
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void addListener(IExecutionListener listener)
    {
	    if (!m_listeners.contains(listener))
	    {
	    	m_listeners.add(listener);
	    }
    }

	/**************************************************************************
	 * 
	 *************************************************************************/
	@Override
    public void removeListener(IExecutionListener listener)
    {
	    if (m_listeners.contains(listener))
	    {
	    	m_listeners.remove(listener);
	    }
    }

	@Override
    public void onClientStatus(ClientStatus status)
    {
	    if (status.freeMemoryPC<10.0)
	    {
	    	System.err.println("############ CLEAR FULL HISTORY");
	    	for(List<ICodeLine> lines : m_lines.values())
	    	{
	    		for(ICodeLine line : lines) line.clearFullHistory();
	    	}
	    }
    }
}
