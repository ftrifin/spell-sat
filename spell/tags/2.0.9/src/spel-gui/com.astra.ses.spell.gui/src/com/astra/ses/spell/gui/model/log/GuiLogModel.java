///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.model.log
// 
// FILE      : GuiLogModel.java
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
package com.astra.ses.spell.gui.model.log;

import java.util.ArrayList;
import java.util.Date;

import com.astra.ses.spell.gui.core.interfaces.ILogListener;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.views.controls.LogViewer;

public class GuiLogModel implements ILogListener
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final int	MAX_EVENTS	       = 1000;
	private static final int	ELEMENTS_TO_REMOVE	= 250;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private ArrayList<LogEvent>	m_events;
	private LogViewer	        m_view;
	private ArrayList<Severity>	m_requiredSeverities;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// INNER CLASSES
	// =========================================================================
	public class LogEvent
	{
		public String	message;
		public String	source;
		public Level	level;
		public Severity	severity;
		public Date		date;
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public GuiLogModel()
	{
		m_events = new ArrayList<LogEvent>(MAX_EVENTS);
		m_view = null;
		m_requiredSeverities = new ArrayList<Severity>();
		m_requiredSeverities.add(Severity.INFO);
		m_requiredSeverities.add(Severity.WARN);
		m_requiredSeverities.add(Severity.ERROR);
		if (Logger.getShowDebug())
		{
			m_requiredSeverities.add(Severity.DEBUG);
		}
	}

	/***************************************************************************
	 * Add a log event
	 **************************************************************************/
	@Override
	public void addMessage(String message, String source, Level level,
	        Severity severity)
	{
		LogEvent event = new LogEvent();
		event.message = message;
		event.source = source;
		event.level = level;
		event.severity = severity;
		event.date = new Date();
		m_events.add(event);
		m_view.addEvent(event);
		if (m_events.size() >= MAX_EVENTS)
		{
			for (int i = 0; i < ELEMENTS_TO_REMOVE; i++)
				m_events.remove(0);
		}
	}

	/***************************************************************************
	 * Configure the visible levels
	 **************************************************************************/
	public void setMaxLevel(Level level)
	{
		m_view.updateMaxLevel(level);
		m_view.setInput(m_events);
	}

	/***************************************************************************
	 * Configure the visible severities
	 **************************************************************************/
	public void addRequiredSeverity(Severity severity)
	{
		m_requiredSeverities.add(severity);
		m_view.setInput(m_events);
	}

	/***************************************************************************
	 * Configure the visible severities
	 **************************************************************************/
	public void removeRequiredSeverity(Severity severity)
	{
		m_requiredSeverities.remove(severity);
		m_view.setInput(m_events);
	}

	/***************************************************************************
	 * Clear the log model
	 **************************************************************************/
	public void clearLog()
	{
		m_events.clear();
		m_view.setInput(m_events);
	}

	/***************************************************************************
	 * Set the model view
	 **************************************************************************/
	public void setView(LogViewer log)
	{
		m_view = log;
	}

	/***************************************************************************
	 * Obtain the required events
	 **************************************************************************/
	public ArrayList<LogEvent> getEvents()
	{
		return m_events;
	}

	/**************************************************************************
	 * Obtain the current severities
	 * 
	 * @return
	 *************************************************************************/
	public ArrayList<Severity> getSeverities()
	{
		return m_requiredSeverities;
	}

	/***************************************************************************
	 * Listener identifier
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return "com.astra.ses.spell.gui.model.LogModel";
	}
}
