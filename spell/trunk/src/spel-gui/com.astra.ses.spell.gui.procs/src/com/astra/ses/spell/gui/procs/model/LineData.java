////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : LineData.java
//
// DATE      : 2010-07-30
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

import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.procs.interfaces.model.ILineData;

/***************************************************************************
 * 
 * LineData is the {@link ILineData} implementation for the ProcedureModel
 * 
 **************************************************************************/
class LineData implements ILineData
{
	/** Holds the execution identifier */
	private int	       m_executionId;
	/** Holds the item name */
	private String	   m_name;
	/** Holds the item status */
	private ItemStatus	m_status;
	/** Holds the item value */
	private String	   m_value;
	/** Holds the comments */
	private String	   m_comment;
	/** Holds the time */
	private String	   m_time;
	/** Holds the notification sequence */
	private long	   m_sequence;

	/**************************************************************************
	 * Constructor.
	 * 
	 * @param execution
	 * @param name
	 * @param status
	 * @param value
	 * @param comments
	 * @param time
	 *************************************************************************/
	public LineData(long sequence, int execution, String name,
	        ItemStatus status, String value, String comments, String time)
	{
		m_sequence = sequence;
		m_executionId = execution;
		m_name = name;
		m_status = status;
		m_value = value;
		m_comment = comments;
		m_time = time;
	}

	@Override
	public String getName()
	{
		return m_name;
	}

	@Override
	public ItemStatus getStatus()
	{
		return m_status;
	}

	@Override
	public String getValue()
	{
		return m_value;
	}

	@Override
	public String getComments()
	{
		return m_comment;
	}

	@Override
	public String getTime()
	{
		return m_time;
	}

	@Override
	public String getId()
	{
		return String.valueOf(m_executionId);
	}

	@Override
	public long getSequence()
	{
		return m_sequence;
	}

	@Override
	public int compareTo(ILineData o)
	{
		Long oSequence = o.getSequence();
		Long thisSequence = this.getSequence();
		return thisSequence.compareTo(oSequence);
	}

	@Override
	public void update(ILineData recent)
	{
		m_status = recent.getStatus();
		m_value = recent.getValue();
		m_comment = recent.getComments();
		m_time = recent.getTime();
	}
}
