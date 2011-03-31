///////////////////////////////////////////////////////////////////////////////
//
// (C) SES Engineering 2008
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.preferences
// 
// FILE      : SnippetPreferences.java
//
// DATE      : 2009-11-23
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
package com.astra.ses.spell.dev.spelleditor.preferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.astra.ses.spell.dev.database.Activator;

public class SnippetPreferences extends AbstractPreferenceInitializer {

	/** Preference ID */
	public static final String SNIPPET_PREF_ID = "com.astra.ses.spell.dev.spelleditor.preferences.Snippet";
	/** Key-Value separator */
	private static final String KEY_VAL_SEPARATOR = "===";
	/** Separator for each entry in the map */
	private static final String MAP_ENTRY_SEPARATOR = "<<<>>>";
	/** Preferences */
	private static final ScopedPreferenceStore PREFERENCES;
	
	static
	{
		IScopeContext confScope = new ConfigurationScope();
		PREFERENCES = new ScopedPreferenceStore(confScope, Activator.PLUGIN_ID);
		PREFERENCES.setSearchContexts(new IScopeContext[]{
			confScope,
			new InstanceScope()
		});
	}
	
	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public SnippetPreferences() {
		
	}

	@Override
	public void initializeDefaultPreferences() {
		PREFERENCES.setDefault(SNIPPET_PREF_ID, "");
	}
	
	/***************************************************************************
	 * Get available snippets as stored in preferences
	 * @return
	 **************************************************************************/
	public Map<String, String> getAvailableSnippets()
	{
		Map<String, String> result = new HashMap<String, String>();
		//Retrieve the preferences value
		String customMap = PREFERENCES.getString(SNIPPET_PREF_ID);
		//Fill the map
		if (!customMap.isEmpty())
		{
			String[] entries = customMap.split(MAP_ENTRY_SEPARATOR);
			for (String mapEntry : entries)
			{
				String[] keyVal = mapEntry.split(KEY_VAL_SEPARATOR);
				if (keyVal.length == 2)
				{
					result.put(keyVal[0], keyVal[1]);
				}
			}
		}
		return result;
	}
	
	/****************************************************************************
	 * Store the given snippets map in preferences
	 * @param snippets
	 ***************************************************************************/
	public void storePreferences(Map<String, String> snippets)
	{
		//Stringify the map
		String mapString = "";
		Set<String> keySet = snippets.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext())
		{
			String key = keyIterator.next();
			String val = snippets.get(key);
			mapString += key + KEY_VAL_SEPARATOR + val;
			if (keyIterator.hasNext())
			{
				mapString += MAP_ENTRY_SEPARATOR;
			}
		}
		// Store preferences
		PREFERENCES.setValue(SNIPPET_PREF_ID, mapString);
		try {
			PREFERENCES.save();
		} catch (IOException e) {
		}
	}
}
