///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.impl.marshall
// 
// FILE      : CommandMarshaller.java
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database.impl.marshall;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.astra.ses.spell.dev.database.impl.TelecommandArgument;
import com.astra.ses.spell.dev.database.impl.TelecommandSequence;
import com.astra.ses.spell.dev.database.impl.Telecommand;
import com.astra.ses.spell.dev.database.interfaces.ArgumentDefinition;
import com.astra.ses.spell.dev.database.interfaces.ArgumentType;
import com.astra.ses.spell.dev.database.interfaces.ITelecommandArgument;
import com.astra.ses.spell.dev.database.interfaces.ITelecommand;
import com.astra.ses.spell.dev.database.interfaces.ITelecommandSequence;

/***************************************************************************
 * 
 **************************************************************************/
public class CommandMarshaller
{

	/***************************************************************************
	 * 
	 **************************************************************************/
	public static void marshall(DataOutputStream writeOut, ITelecommand comm)
	{
		// false for single commands
		boolean commandType = false;
		if (comm instanceof TelecommandSequence)
		{
			commandType = true;
		}
		try
		{
			writeOut.writeBoolean(commandType);
			// Write name
			String name = comm.getName();
			writeOut.writeInt(name.length());
			writeOut.write(name.getBytes());
			// Write description
			String desc = comm.getDescription();
			writeOut.writeInt(desc.length());
			writeOut.write(desc.getBytes());
			// isCritical
			writeOut.writeBoolean(comm.isCritical());
			// Arguments
			List<ITelecommandArgument> arguments = comm.getArguments();
			if (arguments == null)
			{
				writeOut.writeInt(0);
				return;
			}
			int totalArguments = arguments.size();
			writeOut.writeInt(totalArguments);
			for (ITelecommandArgument argument : arguments)
			{
				// NAME
				String argName = argument.getName();
				writeOut.writeInt(argName.length());
				writeOut.write(argName.getBytes());
				// DESCRIPTION
				String argDesc = argument.getDescription();
				writeOut.writeInt(argDesc.length());
				writeOut.write(argDesc.getBytes());
				// TYPE
				String argType = argument.getType();
				writeOut.writeInt(argType.length());
				writeOut.write(argType.getBytes());
				// DEFAULT VALUE
				String argDef = argument.getDefaultValue();
				writeOut.writeInt(argDef.length());
				writeOut.write(argDef.getBytes());
			}
			// Single commands do not need to store any more data
			if (commandType == false) { return; }
			ITelecommandSequence seq = (ITelecommandSequence) comm;
			List<ITelecommand> commands = (List<ITelecommand>) seq
			        .getSequenceElements();
			writeOut.writeInt(commands.size());
			for (ITelecommand command : commands)
			{
				CommandMarshaller.marshall(writeOut, command);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	public static ITelecommand unmarshall(DataInputStream readIn)
	{
		try
		{
			boolean commandType = readIn.readBoolean();
			// Name
			int nameLen = readIn.readInt();
			byte[] nameBytes = new byte[nameLen];
			readIn.read(nameBytes);
			String name = new String(nameBytes);
			// Description
			int descLen = readIn.readInt();
			byte[] descBytes = new byte[descLen];
			readIn.read(descBytes);
			String desc = new String(descBytes);
			// Critical
			boolean isCritical = readIn.readBoolean();
			// Arguments
			int totalArgs = readIn.readInt();
			List<ITelecommandArgument> arguments = new ArrayList<ITelecommandArgument>();
			for (int i = 0; i < totalArgs; i++)
			{
				// Arg name
				int argNameLen = readIn.readInt();
				byte[] argNameBytes = new byte[argNameLen];
				readIn.read(argNameBytes);
				String argName = new String(argNameBytes);
				// Arg description
				int argDescLen = readIn.readInt();
				byte[] argDescBytes = new byte[argDescLen];
				readIn.read(argDescBytes);
				String argDesc = new String(argDescBytes);
				// Arg type
				int argTypeLen = readIn.readInt();
				byte[] argTypeBytes = new byte[argTypeLen];
				readIn.read(argTypeBytes);
				String argType = new String(argTypeBytes);
				// Arg default value
				int argDefLen = readIn.readInt();
				byte[] argDefBytes = new byte[argDefLen];
				readIn.read(argDefBytes);
				String argDef = new String(argDefBytes);
				ArgumentDefinition argDefinition = new ArgumentDefinition(
				        argName, argDesc, ArgumentType.getTypeFromChar(argType
				                .charAt(0)));
				ITelecommandArgument arg = new TelecommandArgument(
				        argDefinition, argDef);
				arguments.add(arg);
			}
			if (commandType == false) { return new Telecommand(name, desc,
			        isCritical, arguments); }
			// For sequences, retrieve the commands that compose the sequence
			int commandCount = readIn.readInt();
			List<ITelecommand> commands = new ArrayList<ITelecommand>();
			for (int i = 0; i < commandCount; i++)
			{
				ITelecommand comm = CommandMarshaller.unmarshall(readIn);
				commands.add(comm);
			}
			return new TelecommandSequence(name, desc, isCritical, arguments,
			        commands);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
