///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.server
// 
// FILE      : ExecutorInfo.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.model.server;

import java.util.Map;
import java.util.TreeMap;

import com.astra.ses.spell.gui.core.model.types.ExecutorConfigKeys;

public class ExecutorConfig
{
	/** Holds the RunInto state */
	private boolean	m_runInto;
	/** Holds the execution delay */
	private int	    m_execDelay;
	/** Holds the step-by-step state */
	private boolean	m_byStep;
	/** Holds the show lib state */
	private boolean	m_showLib;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public ExecutorConfig()
	{
		m_runInto = false;
		m_byStep = false;
		m_execDelay = 0;
		m_showLib = false;
	}

	/***************************************************************************
	 * Copy data from given info
	 **************************************************************************/
	public void copyFrom(ExecutorConfig config)
	{
		m_runInto = config.m_runInto;
		m_byStep = config.m_byStep;
		m_execDelay = config.m_execDelay;
		m_showLib = config.m_showLib;
	}

	/***************************************************************************
	 * Assign the runinto status
	 **************************************************************************/
	public void setRunInto(boolean enabled)
	{
		m_runInto = enabled;
	}

	/***************************************************************************
	 * Obtain the runinto status
	 **************************************************************************/
	public boolean getRunInto()
	{
		return m_runInto;
	}

	/***************************************************************************
	 * Assign the bystep status
	 **************************************************************************/
	public void setStepByStep(boolean enabled)
	{
		m_byStep = enabled;
	}

	/***************************************************************************
	 * Obtain the bystep status
	 **************************************************************************/
	public boolean getStepByStep()
	{
		return m_byStep;
	}

	/***************************************************************************
	 * Assign the browsable lib status
	 **************************************************************************/
	public void setBrowsableLib(boolean enabled)
	{
		m_showLib = enabled;
	}

	/***************************************************************************
	 * Obtain the browsable lib status
	 **************************************************************************/
	public boolean getBrowsableLib()
	{
		return m_showLib;
	}

	/***************************************************************************
	 * Assign the execution delay
	 **************************************************************************/
	public void setExecDelay(int delay)
	{
		m_execDelay = delay;
	}

	/***************************************************************************
	 * Obtain the execution delay
	 **************************************************************************/
	public int getExecDelay()
	{
		return m_execDelay;
	}

	/***************************************************************************
	 * Obtain the client mode
	 **************************************************************************/
	public Map<String, String> getConfigMap()
	{
		Map<String, String> config = new TreeMap<String, String>();
		if (m_runInto)
		{
			config.put(ExecutorConfigKeys.RUN_INTO, "True");
		}
		else
		{
			config.put(ExecutorConfigKeys.RUN_INTO, "False");
		}
		if (m_byStep)
		{
			config.put(ExecutorConfigKeys.BY_STEP, "True");
		}
		else
		{
			config.put(ExecutorConfigKeys.BY_STEP, "False");
		}
		if (m_showLib)
		{
			config.put(ExecutorConfigKeys.BROWSABLE_LIB, "True");
		}
		else
		{
			config.put(ExecutorConfigKeys.BROWSABLE_LIB, "False");
		}
		config.put(ExecutorConfigKeys.EXEC_DELAY, Double.toString(m_execDelay));
		return config;
	}

}
