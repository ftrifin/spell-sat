///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.interfaces.telemetry
// 
// FILE      : ITelemetryParameter.java
//
// DATE      : 2009-09-14
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
package com.astra.ses.spell.dev.database.interfaces;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;


/******************************************************************************
 * TM parameter definition 
 *****************************************************************************/
public interface ITelemetryParameter extends IDatabaseElement
{
	/**************************************************************************
	 * Get measuring unit for this parameter
	 * @return
	 *************************************************************************/
	public String getMeasuringUnit();
	
	/**************************************************************************
	 * Return the available representation for this parameter
	 *************************************************************************/
	public Collection<? extends IRepresentation> getRepresentations();
	
	/**************************************************************************
	 * Get the default representation for this TM parameter
	 * @return
	 *************************************************************************/
	public IRepresentation getDefaultRepresentation();

	/**************************************************************************
	 * Get the default representation for this TM parameter
	 * @return
	 *************************************************************************/
	public void addRepresentation( IRepresentation representation, boolean defaultRepresentation );

	/**************************************************************************
	 * Get monitoring checks for this TM parameters
	 * @return
	 *************************************************************************/
	public Collection<? extends IMonitoringCheck> getMonitoringChecks();
	
	/***************************************************************************
	 * Serialize this object using the given dataoutputstream object
	 * @param writeOut the stream where this object shall be serialized
	 * @throws IOException if an error is raised while writing this object's
	 * serialization into the stream
	 **************************************************************************/
	public void serialize(DataOutputStream writeOut);
}
