///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.preferences
// 
// FILE      : ConnectionPreferences.java
//
// DATE      : 2009-11-05 08:55
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
package com.astra.ses.spell.gui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.astra.ses.spell.gui.Activator;

/******************************************************************************
 * 
 * ConnectionPreferences will provide the keys for connection preferences
 * as well as it will set the default values
 * @author jpizar
 *
 *****************************************************************************/
public class ConnectionPreferences extends AbstractPreferenceInitializer {

	/** Prefix for the connection preferences */
	private static final String PREFIX = "/connection/";
	public static final String PREF_SERVER_MODE_MANUAL = PREFIX + "ServerModeManual";
	public static final String PREF_PREDEFINED_SERVER = PREFIX + "PredefinedServer";
	public static final String PREF_OTHER_SERVER_NAME = PREFIX + "OtherServerName";
	public static final String PREF_OTHER_SERVER_PORT = PREFIX + "OtherServerPort";
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PREF_SERVER_MODE_MANUAL, false);
		store.setDefault(PREF_PREDEFINED_SERVER, "");
		store.setDefault(PREF_OTHER_SERVER_NAME, "");
		store.setDefault(PREF_OTHER_SERVER_PORT, "");
	}

}
