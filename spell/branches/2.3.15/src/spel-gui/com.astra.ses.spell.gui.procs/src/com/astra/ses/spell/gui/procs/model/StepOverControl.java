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
	private int m_currentDepth;
	private int m_tempMaxDepth;
	private int m_maxDepth;
	private boolean m_intoOne;

	public StepOverControl()
	{
		m_mode = null;
		reset();
	}

	@Override
    public StepOverMode getMode()
    {
	    return m_mode;
    }

	@Override
    public void setMode(StepOverMode mode)
    {
		Logger.debug("Set mode " + mode + ", current " + m_mode, Level.PROC, this);
		switch(mode)
		{
		case STEP_OVER_ALWAYS:
			// If this mode is set, we need to stick to the current level or above. This means that
			// any level above this maximum shall not be notified.
			m_maxDepth = m_currentDepth;
			m_tempMaxDepth = -1;
			break;
		case STEP_OVER_ONCE:
			// There is a temporary maximum level. This level will be reset when the current level
			// is back equal to this temporary max.
			m_tempMaxDepth = m_currentDepth;
			// Do not modify the absolute maximum level: a click on STEP OVER button shall not affect
			// the fact that we are still in step over always
			break;
		case STEP_INTO_ONCE:
			// Modify the absolute maximum level: a click on STEP INTO button will change the
			// step over always mode, in a way that now the maximum level is updated to the current one
			if ((m_mode != null) && (m_mode.equals(StepOverMode.STEP_OVER_ALWAYS))) m_intoOne = true;
			break;
		case STEP_INTO_ALWAYS:
			// There is no maximum level
			m_maxDepth = -1;
			m_tempMaxDepth = -1;
			break;
		default:
			break;
		}
		m_mode = mode;
    }

	@Override
    public void onExecutionCall()
    {
		m_currentDepth++;
		if (m_intoOne) 
		{
			m_maxDepth++;
			m_intoOne = false;
		}
    }

	@Override
    public void onExecutionLine()
    {
    }

	@Override
    public void onExecutionReturn()
    {
		m_currentDepth--;
		// If we started the SO in a level 3 but we return to level 2, now
		// the maximum level shall be 2 as well
		if (m_currentDepth < m_maxDepth)
		{
			m_maxDepth = m_currentDepth;
		}
		// Reset the temporary max level if we come back to the level were it was set
		if (m_currentDepth == m_tempMaxDepth)
		{
			m_tempMaxDepth = -1;
		}
    }

	@Override
    public boolean isSteppingOver()
    {
		if ( (m_tempMaxDepth != -1) && (m_tempMaxDepth < m_currentDepth))
		{
			return true;
		}
		if ( (m_maxDepth != -1) && (m_maxDepth < m_currentDepth))
		{
			return true;
		}
		return false;
    }

	@Override
	public void reset()
	{
		m_intoOne = false;
		m_mode = null;
		m_currentDepth = 0;
		m_tempMaxDepth = -1;
		m_maxDepth = -1;
	}
}
