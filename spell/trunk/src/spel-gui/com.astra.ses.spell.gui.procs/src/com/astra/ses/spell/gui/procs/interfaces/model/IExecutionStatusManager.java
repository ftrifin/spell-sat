package com.astra.ses.spell.gui.procs.interfaces.model;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.gui.core.model.notification.ItemNotification;
import com.astra.ses.spell.gui.core.model.notification.StackNotification;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.procs.interfaces.listeners.IExecutionListener;

public interface IExecutionStatusManager
{
	public String getCodeId();
	public int getCurrentLineNo();
	public ICodeLine getCurrentLine();
	public ICodeLine getLine( int lineNo );
	public List<ICodeLine> getLines();
	public boolean isInReplay();

	public void initialize( IProgressMonitor monitor );
	
	public void onItemNotification( ItemNotification data );
	public void onStackNotification( StackNotification data );

	public void reset();
	public void dispose();
	
	public void clearNotifications();
	public void clearBreakpoints();
	public void setBreakpoint( int lineNo, BreakpointType type );

	public void setReplay( boolean replay );
	
	public void addListener(IExecutionListener listener);
	public void removeListener(IExecutionListener listener);

}
