///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.db.telemetry.calibration
// 
// FILE      : CalibrationProvider.java
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

import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.dev.database.hifly.parsers.telemetry.calibration.HiflyNumericalCalibrationReader;
import com.astra.ses.spell.dev.database.hifly.parsers.telemetry.calibration.HiflyPolynomialCalibrationReader;
import com.astra.ses.spell.dev.database.hifly.parsers.telemetry.calibration.HiflyTextualCalibrationReader;
import com.astra.ses.spell.dev.database.interfaces.telemetry.calibration.ICalibration;

/*******************************************************************************
 * Monitoring Calibration Provider manages calibration for each monitoring
 * parameter 
 * @author jpizar
 *
 ******************************************************************************/
public class CalibrationProvider {

	/** Map for storing calibrations with their identification numbers */
	private Map<Integer, ICalibration> m_calibrationMap;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public CalibrationProvider(String rootDir)
	{
		m_calibrationMap = new HashMap<Integer, ICalibration>();
		init(rootDir);
	}
	
	/***************************************************************************
	 * Init calibration map
	 **************************************************************************/
	private void init(String rootDir)
	{
		// Numerical calibrations
		HiflyNumericalCalibrationReader numericalCalReader = new HiflyNumericalCalibrationReader(rootDir);
		m_calibrationMap.putAll(numericalCalReader.getCalibrations());
		// Text calibrations
		//TODO parse textual calibrations
		HiflyTextualCalibrationReader textualReader;
		// Polynomial calibrations
		//TODO parse polynomial calibrations
		HiflyPolynomialCalibrationReader polynomialReader;
	}
	
	/****************************************************************************
	 * Get a ICalibration object by giving the identity
	 * @param calibrationIdentifier
	 ***************************************************************************/
	public ICalibration getCalibration(int calibrationIdentifier)
	{
		return m_calibrationMap.get(calibrationIdentifier);
	}
	
}
