///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.impl.telemetry
// 
// FILE      : TelemetryParameter.java
//
// DATE      : 2009-09-15
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database.impl.telemetry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.astra.ses.spell.dev.database.interfaces.telemetry.IMonitoringParameter;
import com.astra.ses.spell.dev.database.interfaces.telemetry.IRepresentationDefinition;
import com.astra.ses.spell.dev.database.interfaces.telemetry.check.IMonitoringCheck;

public class TelemetryParameter implements IMonitoringParameter {

	/** Parameter name */
	private String m_name;
	/** Parameter description */
	private String m_description;
	/** Measuring unit */
	private String m_measuringUnit;
	/** Collection of available representations */
	private ArrayList<IRepresentationDefinition> m_availableRepresentations;
	/** Default representation */
	private IRepresentationDefinition m_defaultRepresentation;
	/** Monitoring checks */
	private ArrayList<IMonitoringCheck> m_monitoringChecks;
	
	/**************************************************************************
	 * Construct a TelemetryParameter object by retrieveingdata from the
	 * given DataInputStream
	 * @param readIn
	 * @return
	 *************************************************************************/
	public static TelemetryParameter deserialize(DataInputStream readIn)
	{
		try {
			// NAME
			int nameLen = readIn.readInt();
			byte[] nameBytes = new byte[nameLen];
			readIn.read(nameBytes);
			String name = new String(nameBytes);
			// DESCRIPTION
			int descLen = readIn.readInt();
			byte[] descBytes = new byte[descLen];
			readIn.read(descBytes);
			String desc = new String(descBytes);
			// MEASURING UNIT
			int unitLen = readIn.readInt();
			byte[] unitBytes = new byte[unitLen];
			readIn.read(unitBytes);
			String unit = new String(unitBytes);
			// CONSTRUCT THE OBJECT
			return new TelemetryParameter(name, desc, unit);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**************************************************************************
	 * Constructor
	 * @param name
	 * @param description
	 *************************************************************************/
	public TelemetryParameter(String name, String description, String measuringUnit)
	{
		m_name = name;
		m_description = description;
		m_measuringUnit = measuringUnit;
		m_availableRepresentations = new ArrayList<IRepresentationDefinition>();
		m_monitoringChecks = new ArrayList<IMonitoringCheck>();
	}
	
	@Override
	public Collection<IRepresentationDefinition> getRepresentations() 
	{
		return m_availableRepresentations;
	}
	
	/**************************************************************************
	 * Add a new representation for the parameter
	 * @param newRepresentation
	 *************************************************************************/
	public void addRepresentation(IRepresentationDefinition newRepresentation, boolean isDefault)
	{
		m_availableRepresentations.add(newRepresentation);
		m_defaultRepresentation = isDefault ? newRepresentation : m_defaultRepresentation;
	}
	
	/**************************************************************************
	 * Add a monitoring check to this parameter
	 *************************************************************************/
	public void addMonitoringCheck(IMonitoringCheck check)
	{
		m_monitoringChecks.add(check);
	}

	@Override
	public IRepresentationDefinition getDefaultRepresentation() 
	{
		return m_defaultRepresentation;
	}

	@Override
	public String getDescription() 
	{
		return m_description;
	}

	@Override
	public String getName() 
	{
		return m_name;
	}

	@Override
	public String getMeasuringUnit() {
		return m_measuringUnit;
	}

	@Override
	public Collection<? extends IMonitoringCheck> getMonitoringChecks() {
		return m_monitoringChecks;
	}
	
	/***************************************************************************
	 * Serialize this object using the given dataoutputstream object
	 * @param writeOut
	 * @throws IOException 
	 **************************************************************************/
	public void serialize(DataOutputStream writeOut)
	{
		try {
			writeOut.writeInt(m_name.length());
			writeOut.write(m_name.getBytes());
			writeOut.writeInt(m_description.length());
			writeOut.write(m_description.getBytes());
			writeOut.writeInt(m_measuringUnit.length());
			writeOut.write(m_measuringUnit.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
