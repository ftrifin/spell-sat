////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : SourceCodeProvider.java
//
// DATE      : 2010-08-06
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.core.services.ContextProxy;
import com.astra.ses.spell.gui.procs.interfaces.model.ISourceCodeProvider;

/*******************************************************************************
 * 
 * ProcedureSourceProvider implements {@link ISourceCodeProvider} to handle
 * SPELL procedures source code
 * 
 ******************************************************************************/
public class SourceCodeProvider implements ISourceCodeProvider
{

	/** Context proxy */
	private static final ContextProxy	  s_proxy;

	/** A map containing source codes for the different code ids */
	private Map<String, String[]>	      m_sources;
	/** Breakpoints for the different code ids */
	private Map<String, BreakpointType[]>	m_breakpoints;

	/*
	 * static block where ContextProxy service is retrieved
	 */
	static
	{
		s_proxy = (ContextProxy) ServiceManager.get(ContextProxy.ID);
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public SourceCodeProvider()
	{
		m_sources = new HashMap<String, String[]>();
		m_breakpoints = new HashMap<String, BreakpointType[]>();
	}

	@Override
	public String[] getSource(String codeId)
	{
		/*
		 * If the code does not exist, then retrieve it
		 */
		if (!m_sources.containsKey(codeId))
		{
			String[] source = retrieveCode(codeId);
			m_sources.put(codeId, source);
			BreakpointType[] bp = new BreakpointType[source.length];
			Arrays.fill(bp, BreakpointType.UNKNOWN);
			m_breakpoints.put(codeId, bp);
		}
		return m_sources.get(codeId);
	}

	@Override
	public String getSource(String codeId, int lineNo)
	{
		String[] sourceLines = getSource(codeId);
		if (lineNo < sourceLines.length) { return sourceLines[lineNo]; }
		return null;
	}

	@Override
	public void reset()
	{
		m_sources.clear();
		m_breakpoints.clear();
	}

	@Override
	public void clearBreakpoints()
	{
		for (String codeId : m_breakpoints.keySet())
		{
			int len = m_sources.get(codeId).length;
			BreakpointType[] bp = new BreakpointType[len];
			Arrays.fill(bp, BreakpointType.UNKNOWN);
			m_breakpoints.put(codeId, bp);
		}
	}

	@Override
	public void removeTemporaryBreakpoint(String codeId, int lineNumber)
	{
		// If the breakpoint is temporary, then remove it
		BreakpointType bp = getBreakpoint(codeId, lineNumber);
		if (bp.equals(BreakpointType.TEMPORARY))
		{
			setBreakpoint(codeId, lineNumber, BreakpointType.UNKNOWN);
		}
	}

	@Override
	public void setBreakpoint(String codeId, int line, BreakpointType type)
	{
		m_breakpoints.get(codeId)[line - 1] = type;
	}

	@Override
	public BreakpointType getBreakpoint(String codeId, int line)
	{
		BreakpointType bp = BreakpointType.UNKNOWN;
		if (!m_breakpoints.containsKey(codeId)) { return BreakpointType.UNKNOWN; }
		int index = line - 1;
		BreakpointType[] breakpoints = m_breakpoints.get(codeId);
		if (index < breakpoints.length)
		{
			bp = breakpoints[index];
		}
		return bp;
	}

	/***************************************************************************
	 * Retrieve the source code by sending a request to the {@link ContextProxy}
	 * 
	 * @param codeId
	 *            the codeId of the procedure which we request the code
	 **************************************************************************/
	private String[] retrieveCode(String codeId)
	{
		return s_proxy.getProcedureCode(codeId).toArray(new String[0]);
	}
}
