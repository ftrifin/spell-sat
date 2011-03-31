///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.driver.test
// 
// FILE      : TestDatabaseDriver.java
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
package com.astra.ses.spell.dev.database.test;

import org.eclipse.core.runtime.IProgressMonitor;

import com.astra.ses.spell.dev.database.interfaces.ISpellDatabase;
import com.astra.ses.spell.dev.database.interfaces.ISpellDatabaseDriver;
import com.astra.ses.spell.dev.database.test.db.TestDatabase;

/*******************************************************************************
 *
 * Main database driver interface
 *
 ******************************************************************************/
public class TestDatabaseDriver implements ISpellDatabaseDriver 
{
	/** Driver name */
	private static final String DRIVER_NAME = "Test";
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public TestDatabaseDriver() 
	{
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean checkDatabase(String rootPath) 
	{
		/*
		 * rootPath is supposed to contain a database URI reference.
		 * For simulation puporses, paths are not processed, so any URI
		 * is considered as valid
		 */
		return true;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getName() 
	{
		return DRIVER_NAME;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public ISpellDatabase loadDatabase(String rootPath, IProgressMonitor monitor) 
	{
		/*
		 * Given the rootath, and the IProgressMonitor instance, it is supposed that
		 * a database located in rootPath should be loaded, and the monitor should be
		 * notified about the loading progress.
		 * For this simulated driver, there is no need to load anything but just to
		 * instantiate a TestDatabase object.
		 */
		return new TestDatabase();
	}

}
