///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.model.files
// 
// FILE      : AsRunFile.java
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
package com.astra.ses.spell.gui.core.model.files;

import java.util.ArrayList;

import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * Representation of an AsRun file
 * 
 ******************************************************************************/
public class AsRunFile extends AbstractServerFile
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 * 
	 * @param source
	 **************************************************************************/
	public AsRunFile(String procId, ArrayList<String> lines)
	{
		super(procId, lines);
	}

	/***************************************************************************
	 * Parse the given AsRun source data
	 * 
	 * @param source
	 **************************************************************************/
	@Override
	public void parse(ArrayList<String> lines)
	{
		int count = 1;
		for (String line : lines)
		{
			try
			{
				line = line.replaceFirst("%C%", "");
				AsRunFileLine arLine = new AsRunFileLine(getProcId(), line);
				addLine(arLine);
			}
			catch (Exception ex)
			{
				System.err.println("Unable to process asrun line: '" + line
				        + "' (" + count + ")");
				ex.printStackTrace();
				Logger.error("Unable to process asrun line: '" + line + "' ("
				        + count + "): " + ex, Level.PROC, this);
			}
			count++;
		}
	}

	/**************************************************************************
	 * Get the asrun header labels
	 *************************************************************************/
	@Override
	public String[] getHeaderLabels()
	{
		String[] labels = new String[AsRunColumns.values().length];
		for (AsRunColumns ar : AsRunColumns.values())
		{
			labels[ar.ordinal()] = ar.name;
		}
		return labels;
	}

	/**************************************************************************
	 * Get the asrun header label relative widths (percentage)
	 *************************************************************************/
	@Override
	public int[] getHeaderLabelsSize()
	{
		int[] sizes = new int[AsRunColumns.values().length];
		for (AsRunColumns ar : AsRunColumns.values())
		{
			sizes[ar.ordinal()] = ar.width;
		}
		return sizes;
	}
}
