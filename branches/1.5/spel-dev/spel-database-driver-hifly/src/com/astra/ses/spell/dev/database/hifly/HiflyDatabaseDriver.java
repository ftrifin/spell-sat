///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.hifly
// 
// FILE      : HiflyDatabaseDriver.java
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
package com.astra.ses.spell.dev.database.hifly;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.dev.database.hifly.db.HiflyDatabase;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabaseDriver;

/******************************************************************************
 * hifly database driver implementation
 * @author jpizar
 *
 *****************************************************************************/
public class HiflyDatabaseDriver implements ISpellDatabaseDriver {

	/** Diver name */
	private static final String DRIVER_NAME = "hifly";
	
	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public HiflyDatabaseDriver() {
	}
	
	@Override
	public boolean checkDatabase(String rootPath) {
		// TODO improve the strategy for determining if the path contains a 
		// correct hifly database
		File rootFile = new File(rootPath);
		boolean rootAccess = (rootFile.canRead() && rootFile.exists());
		File vdfFile = new File(rootPath + File.separator + "vdf.dat");
		boolean vdf = (vdfFile.exists() && vdfFile.canRead());
		File pcfFile = new File(rootPath + File.separator + "pcf.dat");
		boolean pcf = (pcfFile.exists() && pcfFile.canRead());
		File ccfFile = new File(rootPath + File.separator + "ccf.dat");
		boolean ccf = (ccfFile.exists() && ccfFile.canRead());
		return (rootAccess && vdf && ccf && pcf);
	}
	
	@Override
	public String getName() {
		return DRIVER_NAME;
	}
	
	@Override
	public ISpellDatabase loadDatabase(String rootPath, IProgressMonitor monitor)
	{
		monitor.beginTask("Loading database...", 1);
		ISpellDatabase database = new HiflyDatabase(rootPath);
		monitor.worked(1);
		return database;
	}
}
