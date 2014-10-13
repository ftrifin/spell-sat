///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.utils
// 
// FILE      : Logger.java
//
// DATE      : 2008-11-21 08:58
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.core.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Properties;

import com.astra.ses.spell.gui.core.model.types.ICoreConstants;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;

/*******************************************************************************
 * @brief This class provides tracing/logging support. If needed, transmits
 *        logged data to the MasterView.
 * @date 09/10/07
 ******************************************************************************/
public class Logger
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Singleton instance */
	private static Logger s_instance = null;
	/** Date format converter */
	private static DateFormat s_df = DateFormat.getDateTimeInstance();
	/** Log file path */
	private static String s_logFileDir = ".";
	/** Log file name */
	private static String s_logFileName = "_spel-gui.log";
	/** Log file handle */
	private static PrintWriter s_logFile = null;
	/** Length for class names */
	private static final int CLASS_NAME_LEN = 22;

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================
	// PRIVATE -----------------------------------------------------------------

	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Obtain the singleton instance
	 * 
	 * @return The singleton instance
	 **************************************************************************/
	protected static Logger instance()
	{
		if (s_instance == null)
		{
			s_instance = new Logger();
		}
		return s_instance;
	}

	/***************************************************************************
	 * Log a debugging message specifying the level
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	public static void debug(String message, Level level, Object origin)
	{
		instance().log(message, level, Severity.DEBUG, origin);
	}

	/***************************************************************************
	 * Log an information message specifying the level
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	public static void info(String message, Level level, Object origin)
	{
		instance().log(message, level, Severity.INFO, origin);
	}

	/***************************************************************************
	 * Log a warning message specifying the level
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	public static void warning(String message, Level level, Object origin)
	{
		instance().log(message, level, Severity.WARN, origin);
	}

	/***************************************************************************
	 * Log an error message specifying the level
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	public static void error(String message, Level level, Object origin)
	{
		if (message == null)
			message = "Null pointer exception";
		instance().log(message, level, Severity.ERROR, origin);
	}

	/***************************************************************************
	 * Set the log file path.
	 * 
	 * @param filePath
	 *            Absolute path to the log file
	 **************************************************************************/
	public static void setLogFile(String filePath)
	{
		s_logFileName = filePath;
		createLogFile();
	}

	// =========================================================================
	// # NON-ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	protected Logger()
	{
		createLogFile();
	}

	/***************************************************************************
	 * Create the log file
	 **************************************************************************/
	protected static void createLogFile()
	{
		String name = "";
		try
		{
			if (s_logFile != null)
				s_logFile.close();
			Calendar c = Calendar.getInstance();
			Properties props = System.getProperties();
			String sep = props.get("file.separator").toString();

			String home = System.getenv(ICoreConstants.CLIENT_LOG_ENV);
			if (home != null)
			{
				name = home + sep + c.get(Calendar.YEAR) + "_" + c.get(Calendar.MONTH) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_"
				        + c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND) + s_logFileName;
			}
			else
			{
				name = s_logFileDir + sep + c.get(Calendar.YEAR) + "_" + c.get(Calendar.MONTH) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_"
				        + c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND) + s_logFileName;
			}
			System.out.println("Using log file " + name);
			s_logFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream(name)));
		}
		catch (IOException ex)
		{
			s_logFile = null;
			error("Tracing to file disabled: could not open log file: " + name, Level.INIT, "LOG");
		}
	}

	/***************************************************************************
	 * Destructor
	 **************************************************************************/
	protected void finalize()
	{
		if (s_logFile != null)
		{
			s_logFile.flush();
			s_logFile.close();
		}
	}

	/***************************************************************************
	 * Main logging method
	 * 
	 * @param message
	 *            Log message
	 * @param level
	 *            Level of the message
	 * @param severity
	 *            Message severity
	 * @param origin
	 *            Originator class
	 **************************************************************************/
	protected void log(String message, Level level, Severity severity, Object origin)
	{
		// Obtain the message heading information
		String head = "";
		String originatorName = null;
		// If we have an originator class, get the class name
		if (origin != null)
		{
			if (origin instanceof String)
			{
				originatorName = (String) origin;
			}
			else
			{
				originatorName = origin.getClass().getSimpleName();
			}
			// There is a maximum length for the class name
			// If needed, shorten it. Otherwise complete with spaces.
			int after = CLASS_NAME_LEN - originatorName.length();
			if (after < 0)
			{
				head = "[ " + originatorName.substring(0, CLASS_NAME_LEN - 1) + "..]";
			}
			else
			{
				head = "[ " + originatorName;
				for (int c = 0; c < after; c++)
					head += " ";
				head += " ]";
			}
			// Add the severity string
			head += severity.log;
		}
		else
		{
			// If there is not originator class, copy only the severity
			head = "[?] " + severity.log;
		}
		// Add the level string
		String lev = level.log;
		// Add the message date
		String msec = new Integer(Calendar.getInstance().get(Calendar.MILLISECOND)).toString();
		int l = msec.length();
		if (l < 3)
		{
			for (int i = 0; i < 3 - l; i++)
				msec += "0";
		}
		String date = "[" + s_df.format(Calendar.getInstance().getTime()) + ":" + msec + "]";
		// Build the entire message
		String msg = head + date + lev + ": ";
		msg += message;
		
		if (severity == Severity.ERROR || severity == Severity.WARN)
		{
			System.err.println(msg);
		}
		else
		{
			System.out.println(msg);
		}

		s_logFile.println(msg);
		s_logFile.flush();
	}
}
