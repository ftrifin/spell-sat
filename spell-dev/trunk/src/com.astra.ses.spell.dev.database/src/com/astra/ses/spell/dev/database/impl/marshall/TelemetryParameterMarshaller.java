///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.impl.telemetry
//
// FILE      : TelemetryParameterMarshaller.java
//
// DATE      : Feb 18, 2011
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
package com.astra.ses.spell.dev.database.impl.marshall;

import java.io.DataInputStream;
import java.io.IOException;

import com.astra.ses.spell.dev.database.impl.TelemetryParameter;
import com.astra.ses.spell.dev.database.interfaces.ITelemetryParameter;

/*******************************************************************************
 * 
 ******************************************************************************/
public class TelemetryParameterMarshaller 
{
	/**************************************************************************
	 * Construct a TelemetryParameter object by retrieving data from the
	 * given DataInputStream
	 * @param readIn the InputStream from the serialized Parameter shall be 
	 * taken
	 * @return the TelemetryParameter instance previously serialized
	 *************************************************************************/
	public static ITelemetryParameter unmarshall(DataInputStream readIn)
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
}
