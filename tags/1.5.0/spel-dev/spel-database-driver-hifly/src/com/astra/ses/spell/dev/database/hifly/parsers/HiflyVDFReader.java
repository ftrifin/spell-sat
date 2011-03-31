///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.parsers
// 
// FILE      : HiflyVDFReader.java
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
package com.astra.ses.spell.dev.database.hifly.parsers;

import java.io.File;

public class HiflyVDFReader extends HiflyDatabaseFileReader {

	/** Line fields */
	private String[] m_versionLine;
	
	/** File name */
	private static final String FILENAME = "vdf.dat";
	
	/**************************************************************************
	 * Constructor
	 * @param fileToParse
	 *************************************************************************/
	public HiflyVDFReader(String rootPath) {
		super(rootPath + File.separator + FILENAME);
		//As the file contains only one line, it can be read in the constructor
		//and stored as an attribute
		m_versionLine = getLineFields();	
	}

	/**************************************************************************
	 * Get VDF Field from the file
	 * @return
	 *************************************************************************/
	public String getVDF_NAME()
	{
		return m_versionLine[0];
	}
	
	/**************************************************************************
	 * Get VDF_COMMENT field from the file
	 * @return
	 *************************************************************************/
	public String getVDF_COMMENT()
	{
		return m_versionLine[1];
	}
}