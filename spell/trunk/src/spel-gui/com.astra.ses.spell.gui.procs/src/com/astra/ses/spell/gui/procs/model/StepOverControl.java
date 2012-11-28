///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : StepOverControl.java
//
// DATE      : Nov 8, 2012
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

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation.StepOverMode;
import com.astra.ses.spell.gui.procs.interfaces.model.IStepOverControl;

public class StepOverControl implements IStepOverControl
{
	private StepOverMode m_mode;
	private StepOverMode m_temporaryMode;
	private int m_currentDepth;
	private int m_stepOverStartDepth;
	
	public StepOverControl()
	{
		m_mode = null;
		m_temporaryMode = null;
		m_currentDepth = 0;
		m_stepOverStartDepth = -1;
	}

	@Override
    public StepOverMode getMode()
    {
		if (m_temporaryMode != null)
		{
			return m_temporaryMode;
		}
	    return m_mode;
    }

	@Override
    public void setMode(StepOverMode mode)
    {
		Logger.debug("Set mode " + mode + ", current " + m_mode + ", temporary " + m_temporaryMode, Level.PROC, this);

		switch(mode)
		{
		case STEP_INTO_ALWAYS:
		case STEP_OVER_ALWAYS:
			m_mode = mode;
			m_temporaryMode = null;
			break;
		case STEP_INTO_ONCE:
		case STEP_OVER_ONCE:
			m_temporaryMode = mode;
			break;
		}
		
		Logger.debug("Assigned mode " + m_mode + ", temporary " + m_temporaryMode, Level.PROC, this);

	    switch(mode)
	    {
		case STEP_OVER_ALWAYS:
			// We are going to remain at the present depth:
			m_stepOverStartDepth = m_currentDepth;
			break;
		case STEP_INTO_ALWAYS:
			// We are going notify all calls
			m_stepOverStartDepth = -1;
			break;
		case STEP_INTO_ONCE:
			// We are going notify only the next call, afterwards we go back to
			// the previous mode
			m_stepOverStartDepth = -1;
			break;
		case STEP_OVER_ONCE:
			// We are going to remain at the present depth, but only
			// for the next function call/return pair (functions inside that call
			// need to be stepped over as well)
			m_stepOverStartDepth = m_currentDepth;
			break;
	    }
		Logger.debug("Current depth: " + m_currentDepth + ", so depth: " + m_stepOverStartDepth, Level.PROC, this);
    }

	@Override
    public void onExecutionCall()
    {
		m_currentDepth++;
		Logger.debug("On execution call: " + m_currentDepth + ", so: " + m_stepOverStartDepth, Level.PROC, this);
		Logger.debug("Reset temporary mode", Level.PROC, this);
		m_temporaryMode = null;
    }

	@Override
    public void onExecutionLine()
    {
		Logger.debug("Reset temporary mode", Level.PROC, this);
		m_temporaryMode = null;
    }

	@Override
    public void onExecutionReturn()
    {
		m_currentDepth--;
		Logger.debug("On execution return: " + m_currentDepth + ", so: " + m_stepOverStartDepth, Level.PROC, this);
		// If we have come back to the depth where SO started,
		// reset the mode
		if (m_stepOverStartDepth == m_currentDepth)
		{
			Logger.debug("Reset temporary mode", Level.PROC, this);
			m_temporaryMode = null;
		}
    }

	@Override
    public boolean isSteppingOver()
    {
	    if (m_stepOverStartDepth==-1) return false;
	    return m_currentDepth > m_stepOverStartDepth;
    }

}
