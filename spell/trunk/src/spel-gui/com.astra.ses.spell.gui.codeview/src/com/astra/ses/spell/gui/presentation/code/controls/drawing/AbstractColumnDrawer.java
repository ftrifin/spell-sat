///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.drawing
// 
// FILE      : AbstractColumnDrawer.java
//
// DATE      : 2008-11-21 08:55
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.gui.presentation.code.controls.drawing;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.swt.graphics.Color;

import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

public abstract class AbstractColumnDrawer implements ICodeColumnDrawer
{
	/** Reference to the color provider */
	private ITableColorProvider m_colorProvider;
	/** Reference to the data provider */
	private IProcedureDataProvider m_dataProvider;

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public AbstractColumnDrawer(IProcedureDataProvider dataProvider, ITableColorProvider colorProvider)
	{
		m_colorProvider = colorProvider;
		m_dataProvider = dataProvider;
	}

	/***************************************************************************
	 * Obtain the data provider
	 **************************************************************************/
	protected IProcedureDataProvider getDataProvider()
	{
		return m_dataProvider;
	};

	/***************************************************************************
	 * Obtain the color provider
	 **************************************************************************/
	protected ITableColorProvider getColorProvider()
	{
		return m_colorProvider;
	};

	protected Color getSelectionColor(Color background)
	{
		int red = Math.max(background.getRed() - 80, 0);
		int green = Math.max(background.getGreen() - 80, 0);
		int blue = Math.max(background.getBlue() - 80, 0);
		Color newBackground = new Color(background.getDevice(), red, green, blue);
		return newBackground;
	}
}
