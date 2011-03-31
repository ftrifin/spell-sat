///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database.interfaces
//
// FILE      : ArgumentType.java
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

/******************************************************************************
 * ArgumentType defines the input type an argument can have
 *****************************************************************************/
public enum ArgumentType {

	ASCII("ASCII",'A'),
	SIGNED_INT("Signed integer",'I'),
	UNSIGNED_INT("Unsigned integer",'U'),
	REAL("Real number",'R'),
	ABSOLUTE_TIME("Absolute time",'T'),
	DELTA_RELATIVE_TIME("Delta/Relative time",'D'),
	UNKNOWN("Unknown",' ');
	
	/** Readable representation */
	private String m_representation;
	/** Character representation */
	private char m_charRepresentation;
	
	/***************************************************************************
	 * Private constructor
	 * This enum is defined as a class for being able to retrieve the enum object
	 * from its character, as it is defined in the database file in this way
	 * @param representation
	 **************************************************************************/
	private ArgumentType(String representation, char charRepresentation)
	{
		m_representation = representation;
		m_charRepresentation = charRepresentation;
	}
	
	/***************************************************************************
	 * 
	 * @param character
	 * @return
	 **************************************************************************/
	public static ArgumentType getTypeFromChar(char character)
	{
		for (ArgumentType type : ArgumentType.values())
		{
			if (type.m_charRepresentation == character)
			{
				return type;
			}
		}
		return UNKNOWN;
	}
	
	@Override
	public String toString()
	{
		return m_representation;
	}
}
