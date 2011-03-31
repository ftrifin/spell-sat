///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.parsers.telemetry
// 
// FILE      : HiflyPCFReader.java
//
// DATE      : 2009-09-14
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
package com.astra.ses.spell.dev.database.hifly.parsers.telemetry;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.dev.database.hifly.parsers.HiflyDatabaseFileReader;
import com.astra.ses.spell.dev.database.impl.telemetry.TelemetryParameter;


public class HiflyPCFReader extends HiflyDatabaseFileReader {

	/** File name */
	private static final String FILENAME = "pcf.dat";
	/** Collection of available parameters */
	private Map<String, TelemetryParameter> m_tmParameters;
	
	/**************************************************************************
	 * Constructor
	 * @param fileToParse
	 *************************************************************************/
	public HiflyPCFReader(String fileToParse) {
		super(fileToParse + File.separator + FILENAME);
		init();
	}
	
	/**************************************************************************
	 * Initialize parameters collection
	 *************************************************************************/
	private void init()
	{
		m_tmParameters = new HashMap<String, TelemetryParameter>();
		String[] lineFields = super.getLineFields();
		while (lineFields != null) 
		{
			TelemetryParameter parameter = new TelemetryParameter(lineFields[0], lineFields[1], lineFields[3]);
			m_tmParameters.put(lineFields[0], parameter);
			lineFields = super.getLineFields();
		}
	}
	
	/**************************************************************************
	 * Get telemetry parameters collection
	 * @return
	 *************************************************************************/
	public Map<String, TelemetryParameter> getTelemetryParameters()
	{
		return m_tmParameters;
	}

}
