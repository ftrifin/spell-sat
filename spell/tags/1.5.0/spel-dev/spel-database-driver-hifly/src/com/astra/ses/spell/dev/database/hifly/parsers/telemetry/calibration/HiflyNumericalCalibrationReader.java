///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.parsers.telemetry.calibrations
// 
// FILE      : HiflyNumericalCalibrationReader.java
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
package com.astra.ses.spell.dev.database.hifly.parsers.telemetry.calibration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.dev.database.hifly.db.telemetry.calibration.NumericalCalibration;
import com.astra.ses.spell.dev.database.hifly.parsers.HiflyDatabaseFileReader;

/*******************************************************************************
 * 
 * HiflyNumericalCalibrationReader reads the files related to numerical 
 * calibration and constructs the NumericalCalibrationObjects
 * @author jpizar
 *
 ******************************************************************************/
public class HiflyNumericalCalibrationReader {

	/***************************************************************************
	 * HiflyCAFReader will parse CAF file and return the calibration curves
	 * @author jpizar
	 **************************************************************************/
	private class HiflyCAFReader extends HiflyDatabaseFileReader
	{
		/** Curves file name */
		private static final String CURVES_FILENAME = "caf.dat";
		
		/***********************************************************************
		 * Constructor
		 * @param rootDir
		 **********************************************************************/
		public HiflyCAFReader(String rootDir) {
			super(rootDir + File.separator + CURVES_FILENAME);
		}
	}
	
	/***************************************************************************
	 * HiflyCAPReader will parse CAF file and return the calibration curves
	 * @author jpizar
	 **************************************************************************/
	private class HiflyCAPReader extends HiflyDatabaseFileReader
	{
		/** Curves file name */
		private static final String CURVES_FILENAME = "cap.dat";
		
		/***********************************************************************
		 * Constructor
		 * @param rootDir
		 **********************************************************************/
		public HiflyCAPReader(String rootDir) {
			super(rootDir + File.separator + CURVES_FILENAME);
		}
	}

	/** Database root directory */
	private String m_rootDir;
	/** Collection of available parameters */
	private Map<Integer, NumericalCalibration> m_calMap;
	
	/***************************************************************************
	 * Constructor
	 * @param fileToParse
	 **************************************************************************/
	public HiflyNumericalCalibrationReader(String rootDir) {
		m_rootDir = rootDir;
		m_calMap = new HashMap<Integer, NumericalCalibration>();
		init();
	}
	
	/***************************************************************************
	 * Parse the files
	 **************************************************************************/
	private void init()
	{
		HiflyCAFReader cafReader = new HiflyCAFReader(m_rootDir);
		HiflyCAPReader capReader = new HiflyCAPReader(m_rootDir);
		/*
		 * Read points definition for each curve
		 */
		Map<Integer, ArrayList<String[]>> pointsMap = new HashMap<Integer, ArrayList<String[]>>();
		String[] lineFields = capReader.getLineFields();
		while (lineFields != null)
		{
			String[] point = {lineFields[1], lineFields[2]};
			Integer curveId = Integer.valueOf(lineFields[0]);
			if (!pointsMap.containsKey(curveId))
			{
				pointsMap.put(curveId, new ArrayList<String[]>());
			}
			pointsMap.get(curveId).add(point);
			lineFields = capReader.getLineFields();
		}
		/*
		 * Read curve definition
		 */
		lineFields = cafReader.getLineFields();
		while (lineFields != null)
		{
			Integer curveId = Integer.valueOf(lineFields[0]);
			NumericalCalibration cal = new NumericalCalibration(lineFields[1], pointsMap.get(curveId));
			m_calMap.put(curveId, cal);
			lineFields = cafReader.getLineFields();
		}
	}
	
	/***************************************************************************
	 * Get numerical calibrations defined in files
	 * @return
	 **************************************************************************/
	public Map<Integer, NumericalCalibration> getCalibrations()
	{
		return m_calMap;
	}
}
