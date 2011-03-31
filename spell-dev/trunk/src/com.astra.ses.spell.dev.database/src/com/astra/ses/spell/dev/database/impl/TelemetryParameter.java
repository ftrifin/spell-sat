///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.impl.telemetry
// 
// FILE      : TelemetryParameter.java
//
// DATE      : 2009-09-15
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.astra.ses.spell.dev.database.interfaces.IMonitoringCheck;
import com.astra.ses.spell.dev.database.interfaces.IRepresentation;
import com.astra.ses.spell.dev.database.interfaces.ITelemetryParameter;

/*******************************************************************************
 * 
 * TelemetryParameter is an ITelemetryParameter implementation for representing
 * a basic telemetry parameters model. Basic representation includes: - Name -
 * Description - Measuring units, if it has - Parameter representations, if it
 * has more than one - Monitoring checks, to determine parameter current status
 * 
 ******************************************************************************/
public class TelemetryParameter implements ITelemetryParameter
{

	/** Parameter name */
	private String	                    m_name;
	/** Parameter description */
	private String	                    m_description;
	/** Measuring unit */
	private String	                    m_measuringUnit;
	/** Collection of available representations */
	private ArrayList<IRepresentation>	m_availableRepresentations;
	/** Default representation */
	private IRepresentation	            m_defaultRepresentation;
	/** Monitoring checks */
	private ArrayList<IMonitoringCheck>	m_monitoringChecks;

	/**************************************************************************
	 * Constructor
	 * 
	 * @param name
	 *            the parameter's name
	 * @param description
	 *            the parameter's description
	 * @param measuringUnit
	 *            the parameter's measuring unit
	 *************************************************************************/
	public TelemetryParameter(String name, String description,
	        String measuringUnit)
	{
		m_name = name;
		m_description = description;
		m_measuringUnit = measuringUnit;
		m_availableRepresentations = new ArrayList<IRepresentation>();
		m_monitoringChecks = new ArrayList<IMonitoringCheck>();
	}

	@Override
	public Collection<IRepresentation> getRepresentations()
	{
		return m_availableRepresentations;
	}

	/**************************************************************************
	 * Add a new representation for the parameter
	 * 
	 * @param newRepresentation
	 *************************************************************************/
	@Override
	public void addRepresentation(IRepresentation newRepresentation,
	        boolean isDefault)
	{
		m_availableRepresentations.add(newRepresentation);
		m_defaultRepresentation = isDefault ? newRepresentation
		        : m_defaultRepresentation;
	}

	/**************************************************************************
	 * Add a monitoring check to this parameter
	 * 
	 * @param check
	 *            a monitoring check that applies to this parameter
	 *************************************************************************/
	public void addMonitoringCheck(IMonitoringCheck check)
	{
		m_monitoringChecks.add(check);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IRepresentation getDefaultRepresentation()
	{
		return m_defaultRepresentation;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getDescription()
	{
		return m_description;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getName()
	{
		return m_name;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getMeasuringUnit()
	{
		return m_measuringUnit;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public Collection<? extends IMonitoringCheck> getMonitoringChecks()
	{
		return m_monitoringChecks;
	}

	/***************************************************************************
	 * Serialize this object using the given dataoutputstream object
	 * 
	 * @param writeOut
	 *            the stream where this object shall be serialized
	 * @throws IOException
	 *             if an error is raised while writing this object's
	 *             serialization into the stream
	 **************************************************************************/
	@Override
	public void serialize(DataOutputStream writeOut)
	{
		try
		{
			writeOut.writeInt(m_name.length());
			writeOut.write(m_name.getBytes());
			writeOut.writeInt(m_description.length());
			writeOut.write(m_description.getBytes());
			writeOut.writeInt(m_measuringUnit.length());
			writeOut.write(m_measuringUnit.getBytes());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String toString()
	{
		return m_name;
	}
}
