///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.db.telemetry.calibration
// 
// FILE      : NumericalCalibration.java
//
// DATE      : 2009-09-15
//
// COPYRIGHT (c) 2008, 2010 SES-ENGINEERING
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//    SES-ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL Development Environment
//
// AUTHOR    : J. Andres Pizarro (jpizar) - japg@gmv.es 
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database.hifly.db.telemetry.calibration;

import java.util.ArrayList;

import com.astra.ses.spell.dev.database.interfaces.telemetry.calibration.ICalibration;

/*******************************************************************************
 * 
 * NumericalCalibration class defines numerical calibration items as specified
 * inside hifly database
 * @author jpizar
 *
 ******************************************************************************/
public class NumericalCalibration implements ICalibration {

	/** Description */
	private String m_description;
	/** Points */
	private ArrayList<String[]> m_points;
	
	/***************************************************************************
	 * Constructor
	 * @param description
	 **************************************************************************/
	public NumericalCalibration(String description, ArrayList<String[]> points)
	{
		m_description = description;
		m_points = points;
	}
	
	@Override
	public String getDescription() {
		return m_description;
	}

	@Override
	public String getType() {
		return "NUMERICAL";
	}

	@Override
	public String toString()
	{
		String description = "NUMERICAL CALIBRATION\n";
		description += m_description + "\n";
		description += "Curve points:\n";
		for (String[] point : m_points)
		{
			description += "\t" + point[0] + " - " + point[1] + "\n";
		}
		return description;
	}
}
