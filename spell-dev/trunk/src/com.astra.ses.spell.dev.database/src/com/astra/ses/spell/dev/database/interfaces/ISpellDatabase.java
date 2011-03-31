///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.interfaces
//
// FILE      : ISpellDatabase.java
//
// DATE      : Feb 22, 2011
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
package com.astra.ses.spell.dev.database.interfaces;

import java.util.Collection;

import com.astra.ses.spell.dev.database.impl.Telecommand;
import com.astra.ses.spell.dev.database.impl.TelemetryParameter;

/******************************************************************************
 * 
 * ISpellDatabase interface defines the main interface to be implemented by
 * any plugin using the extension point defined in this plugin
 *
 *****************************************************************************/
public interface ISpellDatabase {
	
	/**************************************************************************
	 * Return this database location
	 * @return the path where the database is located
	 *************************************************************************/
	public String getDatabasePath();
	
	/**************************************************************************
	 * Return this database name
	 * @return the name of the database
	 *************************************************************************/
	public String getName();
	
	/**************************************************************************
	 * Return this database version
	 * @return the version of the database
	 *************************************************************************/
	public String getVersion();

	/**************************************************************************
	 * Get available monitoring parameters
	 * @return the name of the available parameters
	 *************************************************************************/
	public Collection<String> getTelemetryParameterNames();

	/**************************************************************************
	 * Check if a given mnemonic exists in the database
	 *************************************************************************/
	public boolean isTelemetryParameter( String mnemonic );

	/**************************************************************************
	 * Get available command elements
	 * @return the name of the available command elements
	 *************************************************************************/
	public Collection<String> getTelecommandNames();

	/**************************************************************************
	 * Check if a given mnemonic exists in the database
	 *************************************************************************/
	public boolean isTelecommand( String mnemonic );

	/**************************************************************************
	 * Check if a given mnemonic exists in the database
	 *************************************************************************/
	public boolean isSequence( String mnemonic );

	/**************************************************************************
	 * Get available telemetry displays
	 * @return the definition of the available displays as 
	 * {@link ITelemetryDisplayDefinition} implementations
	 *************************************************************************/
	public Collection<? extends ITelemetryDisplayDefinition> getDisplays();
	
	/**************************************************************************
	 * Get one parameter by giving its name
	 * @param parameterName the name of the requested parameter
	 * @return the parameter model as a {@link TelemetryParameter} instance
	 *************************************************************************/
	public ITelemetryParameter getTelemetryModel(String parameterName);
	
	/**************************************************************************
	 * Get a commanding element by giving its name
	 * @param commandName the name of the requested commanding element
	 * @return the command model as a {@link Telecommand} instance
	 *************************************************************************/
	public ITelecommand getTelecommandModel(String commandName);
}
