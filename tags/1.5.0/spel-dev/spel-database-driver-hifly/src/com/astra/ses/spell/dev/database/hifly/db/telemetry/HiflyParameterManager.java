///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.db.telemetry
// 
// FILE      : HiflyParameterManager.java
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
package com.astra.ses.spell.dev.database.hifly.db.telemetry;

import java.util.Collection;
import java.util.Map;

import com.astra.ses.spell.dev.database.hifly.parsers.telemetry.HiflyPCFReader;
import com.astra.ses.spell.dev.database.impl.telemetry.TelemetryParameter;

/******************************************************************************
 * 
 * HiflyParameterManager will deal with the parameters defined inside the 
 * hifly database
 * @author jpizar
 *
 *****************************************************************************/
public class HiflyParameterManager {

	/** Collection of available parameters */
	private Map<String, TelemetryParameter> m_tmParameters;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public HiflyParameterManager(String databaseRoot)
	{
		init(databaseRoot);
	}
	
	/***************************************************************************
	 * Retrieve monitoring parameters from the database
	 **************************************************************************/
	private void init(String databaseRoot)
	{
		HiflyPCFReader pcfReader = new HiflyPCFReader(databaseRoot);
		m_tmParameters = pcfReader.getTelemetryParameters();
	}
	
	/**************************************************************************
	 * Get telemetry parameters collection
	 * @return
	 *************************************************************************/
	public Collection<String> getTelemetryParameters()
	{
		return m_tmParameters.keySet();
	}

	/**************************************************************************
	 * Get 
	 * @param parameterName
	 * @return
	 *************************************************************************/
	public TelemetryParameter getParameter(String parameterName)
	{
		return m_tmParameters.get(parameterName);
	}
}
