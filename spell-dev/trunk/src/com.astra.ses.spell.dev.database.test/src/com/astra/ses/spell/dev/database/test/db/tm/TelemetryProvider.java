///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.driver.test.tm
// 
// FILE      : TelemetryProvider.java
//
// DATE      : 2010-05-19 15:40
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database.test.db.tm;

import java.util.Collection;
import java.util.Vector;

import com.astra.ses.spell.dev.database.interfaces.ITelemetryParameter;
import com.astra.ses.spell.dev.database.interfaces.TelemetryFactory;

/*******************************************************************************
 * 
 * Class TelemetryParameters deals with fake TmParameters
 * 
 ******************************************************************************/
public class TelemetryProvider
{

	/***************************************************************************
	 * 
	 * Fake telemetry parameters definition
	 * 
	 **************************************************************************/
	private enum TmParameter
	{
		FCOUNTER("FCOUNTER", "Frame counter", ""), PARAM1("PARAM1",
		        "Constant parameter", ""), PARAM2("PARAM2",
		        "Sequential parameter", ""), PARAM3("PARAM3",
		        "Complex expression", ""), PARAM4("PARAM4",
		        "String calibration", ""), OBTIME("OBTIME", "On board time", "");

		private String	m_name;
		private String	m_description;
		private String	m_measuringUnits;

		/***********************************************************************
		 * Private constructor
		 * 
		 * @param name
		 * @param desc
		 * @param unit
		 **********************************************************************/
		private TmParameter(String name, String desc, String unit)
		{
			m_name = name;
			m_description = desc;
			m_measuringUnits = unit;
		}

		/***********************************************************************
		 * Get the telemetry parameter associated to this TmParameter
		 * 
		 * @return
		 **********************************************************************/
		public ITelemetryParameter getTelemetryParameter()
		{
			return TelemetryFactory.createParameter(m_name, m_description,
			        m_measuringUnits);
		}
	}

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public TelemetryProvider()
	{
		// NOTHING TO DO HERE AT THE MOMENT
	}

	/***************************************************************************
	 * Get available TelemetryParameters
	 * 
	 * @return
	 **************************************************************************/
	public Collection<String> getTelemetryParameters()
	{
		Vector<String> names = new Vector<String>();
		for (TmParameter par : TmParameter.values())
		{
			names.add(par.m_name);
		}
		return names;
	}

	/***************************************************************************
	 * Get telemetry parameter representation for this name
	 * 
	 * @param name
	 * @return
	 **************************************************************************/
	public ITelemetryParameter getParameter(String name)
	{
		return TmParameter.valueOf(name).getTelemetryParameter();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public boolean isTelemetry(String mnemonic)
	{
		try
		{
			TmParameter.valueOf(mnemonic);
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}
}
