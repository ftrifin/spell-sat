///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.spelleditor.preferences
// 
// FILE      : ColorPreferences.java
//
// DATE      : 2008-11-21 08:55
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
// SUBPROJECT: SPELL Dev
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.spelleditor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.astra.ses.spell.dev.spelleditor.Activator;

public class ColorPreferences extends AbstractPreferenceInitializer {

	/** Function color key */
	public static final String FUNCTION_COLOR = "FUNCTION_COLOR";
	/** Modifier colro key */
	public static final String MODIFIER_COLOR = "MODIFIER_COLOR";
	/** Constant color key */
	public static final String CONSTANT_COLOR = "CONSTANT_COLOR";
	/** Entity color key */
	public static final String ENTITY_COLOR = "ENTITY_COLOR";
	
	/** Preferences */
	private static final ScopedPreferenceStore PREFERENCES;
	
	/**
	 * Innitialize preferences scope
	 */
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
	public ColorPreferences() {
	}

	@Override
	public void initializeDefaultPreferences() {
		PreferenceConverter.setValue(PREFERENCES, FUNCTION_COLOR, new RGB(128, 0, 0));
		PreferenceConverter.setValue(PREFERENCES, MODIFIER_COLOR, new RGB(170, 115, 0));
		PreferenceConverter.setValue(PREFERENCES, CONSTANT_COLOR, new RGB(255, 0, 0));
		PreferenceConverter.setValue(PREFERENCES, ENTITY_COLOR, new RGB(63, 89, 46));
	}

	/***************************************************************************
	 * Returns the RGB code of one color by giving its preferences key
	 * @param key
	 * @return
	 ***************************************************************************/
	public static RGB getRGBColor(String key)
	{
		return PreferenceConverter.getColor(PREFERENCES, key);
	}
}
