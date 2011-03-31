///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly.parsers
// 
// FILE      : HiflyDatabaseFileReader.java
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/******************************************************************************
 * Hifly database files parser base class
 * @author jpizar
 *
 *****************************************************************************/
public class HiflyDatabaseFileReader {
	
	/** Splitter char */
	private static final String SPLITTER = "\t";

	/** File to be parsed */
	private File m_fileToParse;
	/** Buffered reader to read the file line by line */
	private BufferedReader m_bufferedReader;
	
	/*
	 * What this class basically does is to open a hifly database file
	 * and read it line by line as requested, returning the line splitted
	 * with the SPLITTER String
	 */
	
	/**************************************************************************
	 * Constructor
	 * @param arg0
	 *************************************************************************/
	public HiflyDatabaseFileReader(String fileToParse) {
		m_fileToParse = new File(fileToParse);
		initFileResources();
	}
	
	/**************************************************************************
	 * Construct the buffered reader
	 *************************************************************************/
	public void initFileResources()
	{
		try{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fileInputStream = new FileInputStream(m_fileToParse);
			// Get the object of DataInputStream
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			m_bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	/**************************************************************************
	 * Read a line and split the line according to the separator list
	 * @return
	 *************************************************************************/
	public String[] getLineFields() {
		String strLine;
		try {
			strLine = m_bufferedReader.readLine();
			if (strLine == null)
			{
				m_bufferedReader.close();
				return null;
			}
			return strLine.split(SPLITTER);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
