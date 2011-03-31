///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.datatransfer
//
// FILE      : OfflineDatabaseTransfer.java
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
package com.astra.ses.spell.dev.database.datatransfer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.astra.ses.spell.dev.database.impl.marshall.CommandMarshaller;
import com.astra.ses.spell.dev.database.impl.marshall.TelemetryParameterMarshaller;
import com.astra.ses.spell.dev.database.interfaces.ITelecommand;
import com.astra.ses.spell.dev.database.interfaces.ITelemetryParameter;

public class OfflineDatabaseTransfer extends ByteArrayTransfer
{

	/** static instance */
	private static OfflineDatabaseTransfer	s_instance;
	/** transfer types */
	private static final String	           TYPE	= "db-elements";
	private static final int	           TYPEID;

	/***************************************************************************
	 * static block to retrieve TYPEID
	 **************************************************************************/
	static
	{
		TYPEID = registerType(TYPE);
	}

	/***************************************************************************
	 * static instance constructor
	 * 
	 * @return
	 **************************************************************************/
	public static OfflineDatabaseTransfer getInstance()
	{
		if (s_instance == null)
		{
			s_instance = new OfflineDatabaseTransfer();
		}
		return s_instance;
	}

	/***************************************************************************
	 * Get the known data type ids
	 * 
	 * @return
	 **************************************************************************/
	@Override
	protected int[] getTypeIds()
	{
		return new int[] { TYPEID };
	}

	/***************************************************************************
	 * Get the known data type names
	 * 
	 * @return
	 **************************************************************************/
	@Override
	protected String[] getTypeNames()
	{
		return new String[] { TYPE };
	}

	/***************************************************************************
	 * Convert Java objects to transferable data
	 * 
	 * @return The transferable data
	 **************************************************************************/
	@Override
	public void javaToNative(Object object, TransferData transferData)
	{
		if ((object == null) || !(object instanceof DatabaseTransferable)) { return; }

		if (isSupportedType(transferData))
		{
			// casting
			DatabaseTransferable db = (DatabaseTransferable) object;
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				DataOutputStream writeOut = new DataOutputStream(out);
				// Store TM parameters
				ITelemetryParameter[] params = db.getTM();
				writeOut.writeInt(params.length);
				for (ITelemetryParameter param : params)
				{
					param.serialize(writeOut);
				}
				// Store TC commands
				ITelecommand[] commands = db.getTC();
				writeOut.writeInt(commands.length);
				for (ITelecommand comm : commands)
				{
					CommandMarshaller.marshall(writeOut, comm);
				}
				// Write the total amount of elements to read

				byte[] buffer = out.toByteArray();
				writeOut.close();
				super.javaToNative(buffer, transferData);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/***************************************************************************
	 * Convert transferable data to objects
	 * 
	 * @return The obtained object
	 **************************************************************************/
	@Override
	public Object nativeToJava(TransferData transferData)
	{
		if (!isSupportedType(transferData)) { return null; }

		byte[] buffer = (byte[]) super.nativeToJava(transferData);
		if (buffer == null) { return null; }

		ByteArrayInputStream in = new ByteArrayInputStream(buffer);
		DataInputStream readIn = new DataInputStream(in);

		try
		{
			// Retrieve TM parameters
			int totalTM = readIn.readInt();
			List<ITelemetryParameter> params = new ArrayList<ITelemetryParameter>();
			for (int i = 0; i < totalTM; i++)
			{
				// Construct the transfer
				ITelemetryParameter transfer = TelemetryParameterMarshaller
				        .unmarshall(readIn);
				params.add(transfer);
			}
			// Retrieve TC Commands
			int totalTC = readIn.readInt();
			List<ITelecommand> commands = new ArrayList<ITelecommand>();
			for (int i = 0; i < totalTC; i++)
			{
				// Construct the transfer
				ITelecommand transfer = CommandMarshaller.unmarshall(readIn);
				commands.add(transfer);
			}
			DatabaseTransferable transfer = new DatabaseTransferable(params,
			        commands);
			return transfer;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/***************************************************************************
	 * Return a byte array of the serialized object which is currently being 
	 * dragged
	 * 
	 * @return The byte array
	 **************************************************************************/
	public byte[] toByteArray(Object object)
	{
		if ((object == null) || !(object instanceof DatabaseTransferable)) { return null; }
		// casting
		DatabaseTransferable db = (DatabaseTransferable) object;
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream writeOut = new DataOutputStream(out);
			// Store TM parameters
			ITelemetryParameter[] params = db.getTM();
			writeOut.writeInt(params.length);
			for (ITelemetryParameter param : params)
			{
				param.serialize(writeOut);
			}
			// Store TC commands
			ITelecommand[] commands = db.getTC();
			writeOut.writeInt(commands.length);
			for (ITelecommand comm : commands)
			{
				CommandMarshaller.marshall(writeOut, comm);
			}
			// Write the total amount of elements to read
			byte[] buffer = out.toByteArray();
			return buffer;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	/***************************************************************************
	 * Reconstruct the database transferable by giving the byte array
	 * 
	 * @param data
	 * @return
	 **************************************************************************/
	public Object toJava(byte[] buffer)
	{
		ByteArrayInputStream in = new ByteArrayInputStream(buffer);
		DataInputStream readIn = new DataInputStream(in);

		try
		{
			// Retrieve TM parameters
			int totalTM = readIn.readInt();
			List<ITelemetryParameter> params = new ArrayList<ITelemetryParameter>();
			for (int i = 0; i < totalTM; i++)
			{
				// Construct the transfer
				ITelemetryParameter transfer = TelemetryParameterMarshaller
				        .unmarshall(readIn);
				params.add(transfer);
			}
			// Retrieve TC Commands
			int totalTC = readIn.readInt();
			List<ITelecommand> commands = new ArrayList<ITelecommand>();
			for (int i = 0; i < totalTC; i++)
			{
				// Construct the transfer
				ITelecommand transfer = CommandMarshaller.unmarshall(readIn);
				commands.add(transfer);
			}
			DatabaseTransferable transfer = new DatabaseTransferable(params,
			        commands);
			return transfer;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
