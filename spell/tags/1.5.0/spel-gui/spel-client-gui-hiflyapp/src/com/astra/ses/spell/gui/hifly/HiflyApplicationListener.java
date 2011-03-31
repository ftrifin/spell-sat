///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.hifly
// 
// FILE      : HiflyApplicationListener.java
//
// DATE      : 2008-11-21 08:55
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.hifly;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/*******************************************************************************
 * 
 * HiflyApplicationListener will notify the application status for 
 * changing the color of the button in the Application launcher
 * @author jpizar
 *
 ******************************************************************************/
public class HiflyApplicationListener {

	/** Environment variable */
	private static final String HIFLY_SCOSII = "scosii_homedir";
    /**
     * System property containing operating system name
     */
    private static final String OS_NAME_PROPERTY = "os.name";

	/** Boolean to determine if this plugin is active */
	private boolean m_runningWithinHifly = false;
	/** Process pid */
	private String m_pid = null;
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public HiflyApplicationListener()
	{                
		String operatingSystem = System.getProperty(OS_NAME_PROPERTY).toLowerCase();
		boolean isLinux = operatingSystem.indexOf("linux") > -1;
		m_runningWithinHifly = isLinux && (System.getenv(HIFLY_SCOSII) != null);
		System.out.println("Running within hifly: " + m_runningWithinHifly);
		// Retrieve process pid
		String identity = ManagementFactory.getRuntimeMXBean().getName();
		String[] splitted = identity.split("@");
		if (splitted.length == 2)
		{
			m_pid = splitted[0];
		}
	}
	
	/***************************************************************************
	 * Application has finished
	 * @param status return code
	 **************************************************************************/
	public void notifyApplicationFinished() {
		if (!m_runningWithinHifly)
		{
			return;
		}
		System.out.println("Notifying application finished");
		notifyApplicationStatus(false);
	}

	/***************************************************************************
	 * Application has started
	 * @param status return code
	 **************************************************************************/
	public void notifyApplicationStarted() {
		if (!m_runningWithinHifly)
		{
			return;
		}
		System.out.println("Notifying application started");
		notifyApplicationStatus(true);
	}
	
	/***************************************************************************
	 * Notify application status
	 * @param start
	 **************************************************************************/
	private void notifyApplicationStatus(boolean start)
	{
		/*
		 * Construct a command for notifying the application status
		 */
		String path = System.getenv("BIN_DIR");
		String appliNotify = path + File.separator + "APPLInotify";
		String desire_status = start ? "start" : "stop";
		final String command = appliNotify + " " + desire_status + " " + m_pid;
		System.out.println("Notifying application status: " + command);
		Runnable execThread = new Runnable()
		{
			public void run() {
				try {
					Process subprocess = Runtime.getRuntime().exec(command);
					if (command.contains(" stop ")) {
						subprocess.waitFor();
					}
				} catch (IOException e) {
					System.err.println(e.getLocalizedMessage());
				} catch (InterruptedException e) {
					System.err.println(e.getLocalizedMessage());
				}
			}
		};
		execThread.run();
	}
	
}
