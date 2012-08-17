////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.interfaces.model
// 
// FILE      : ILineData.java
//
// DATE      : 2010-08-03
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
package com.astra.ses.spell.gui.procs.interfaces.model;

import com.astra.ses.spell.gui.core.model.types.ItemStatus;

/***************************************************************************
 * 
 * ILineData contains information "Ready to use" about the
 * {@link IExecutionTreeLine}
 * 
 **************************************************************************/
public interface ILineData extends Comparable<ILineData>
{
	/***********************************************************************
	 * Return the identifier of this data
	 * 
	 * @return The identifier
	 **********************************************************************/
	public String getId();

	/***********************************************************************
	 * Get the comments data if any
	 * 
	 * @return
	 **********************************************************************/
	public String getComments();

	/***********************************************************************
	 * Get the name of the associated item
	 * 
	 * @return
	 **********************************************************************/
	public String getName();

	/***********************************************************************
	 * Get the status of the associated item
	 * 
	 * @return
	 **********************************************************************/
	public ItemStatus getStatus();

	/***********************************************************************
	 * Get the time of the associated item
	 * 
	 * @return
	 **********************************************************************/
	public String getTime();

	/***********************************************************************
	 * Get the value of the associated item
	 * 
	 * @return
	 **********************************************************************/
	public String getValue();

	/***********************************************************************
	 * Get the sequence number of the data. Used for filtering past or future
	 * events.
	 * 
	 * @return
	 **********************************************************************/
	public long getSequence();

	/***********************************************************************
	 * Update this instance with the information contained in the recent
	 * {@link ILineData} object
	 * 
	 * @param recent
	 **********************************************************************/
	public void update(ILineData recent);
}
