////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.wizards.pages
// 
// FILE      : SPELLSourceCodeHeader.java
//
// DATE      : Nov 29, 2010 2:25:27 PM
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.wizards.pages;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*******************************************************************************
 * 
 * {@link SPELLSourceCodeHeader} generates the code header to be inserted in
 * any created procedure
 *
 ******************************************************************************/
public class SPELLSourceCodeHeader {

	/** Header template */
	private static final String HEADER_TEMPLATE = 
		"################################################################################\n" +
		"#\n" +
		"# NAME          : $name$\n" +
		"# DESCRIPTION   : $description$\n" +
		"#\n" +
		"# FILE      : $filename$\n" +
		"#\n" +
		"# SPACECRAFT: $spacecrafts$\n" +
		"#\n" +
		"# SPECIFICATION : $specification$\n" +
		"#\n" +
		"# CATEGORY  : $category$\n" +
		"#\n" +
		"# DEVELOPED : $author$\n" +
		"# VERIFIED  : $reviewer$\n" +
		"# VALIDATED : $validator$\n" +
		"#\n" +
		"# REVISION HISTORY:\n" +
		"#\n" +
		"# DATE          REV   AUTHOR      DESCRIPTION\n" +
		"# ===========   ===   =========   ==============================================\n" +
		"# $date$   0.1   $author$   Initial release\n" +
		"#\n" +
		"################################################################################\n" +
		"#\n" +
		"# This procedure has been developed under FS1300 programs and is based on\n" +
		"# SS/LORAL specifications.\n" +
		"#\n" +
		"# You can modify and use this procedure provided that any improvement is shared\n" +
		"# with SS/LORAL for the benefit of the FS1300 community.\n" +
		"#\n" +
		"# This procedure is licensed as is. Licensor makes no warranty as to the adequacy\n" +
		"# or suitability of this procedure for purposes required by the Licensee and\n" +
		"# shall not be held liable for the consequences of its use.\n" +
		"#\n" +
		"# LICENSOR DISCLAIMS ALL WARRANTIES EXPRESSED OR IMPLIED INCLUDING WITHOUT\n" +
		"# LIMITATION ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR\n" +
		"# PURPOSE OR INFRINGEMENT OR VALIDITY.\n" +
		"#\n" +
		"################################################################################\n\n";
	
	/***************************************************************************
	 * Generate the source code header
	 * @return teh header to be inserted in the first line of the procedure
	 **************************************************************************/
	public static String generateHeader(
			String name,
			String description,
			String author,
			String fileName,
			String[] spacecrafts,
			String specification,
			String database,
			String category,
			String validator,
			String reviewer)
	{
		String result = HEADER_TEMPLATE;
		
		// transform the spacecraft into a comma separated list
		String scs = "";
		for (int i = 0; i < spacecrafts.length - 1; i++)
		{
			scs += spacecrafts[i] + ", ";
		}
		scs += spacecrafts[spacecrafts.length - 1];
		
		//create date and time
		Date date = Calendar.getInstance().getTime();
		
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
		String strDate = dateFormatter.format(date);
		
		SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
		String strTime = timeFormatter.format(date);
		
		//PROCEDURE filed must he the file name without extension
		String procName = new String(fileName);
		int extension = procName.lastIndexOf(".py");
		procName = procName.substring(0, extension);
		
		// replace the name
		result = result.replace("$procname$", procName);
		result = result.replace("$name$", name);
		result = result.replace("$description$", description);
		result = result.replace("$author$", author);
		result = result.replace("$filename$", fileName);
		result = result.replace("$spacecrafts$", scs);
		result = result.replace("$database$", database);
		result = result.replace("$category$", category);
		result = result.replace("$validator$", validator);
		result = result.replace("$reviewer$", reviewer);
		result = result.replace("$date$", strDate);
		result = result.replace("$time$", strTime);
		result = result.replace("$specification$", specification);
		
		return result;
	}	
}