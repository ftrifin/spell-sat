///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.services
// 
// FILE      : RuntimeSettingsService.java
//
// DATE      : 2010-05-27
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
package com.astra.ses.spell.gui.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.astra.ses.spell.gui.core.interfaces.BaseService;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

/*******************************************************************************
 * 
 * RuntimeSettingsService will handle propeties which are only valid during
 * application lifecycle. That means that they won't be stored as preferences
 * are being made.
 ******************************************************************************/
public class RuntimeSettingsService extends BaseService implements IRuntimeSettings
{
	/** Service identifier */
	public static final String	                ID	= "com.astra.ses.spell.gui.runtimesettings";

	/** Runtime settings map */
	private Map<String, Object>	                m_runtimeSettings;
	/** Holds the listeners */
	private ArrayList<IRuntimeSettingsListener>	m_listeners;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public RuntimeSettingsService()
	{
		super(ID);
	}

	@Override
	public void cleanup()
	{
		Logger.debug("Cleanup", Level.CONFIG, this);
		m_runtimeSettings.clear();
	}

	@Override
	public void setup()
	{
		m_runtimeSettings = new HashMap<String, Object>();
		m_listeners = new ArrayList<IRuntimeSettingsListener>();
	}

	@Override
	public void subscribe()
	{
	}

	@Override
	public void addRuntimeSettingsListener(IRuntimeSettingsListener listener)
	{
		m_listeners.add(listener);
	}

	@Override
	public void removeRuntimeSettingsListener(IRuntimeSettingsListener listener)
	{
		m_listeners.remove(listener);
	}

	@Override
	public void setRuntimeProperty(RuntimeProperty key, Object element)
	{
		m_runtimeSettings.put(key.tag, element);
		for (IRuntimeSettingsListener listener : m_listeners)
		{
			listener.runtimePropertyChanged(key, element);
		}
	}

	@Override
	public Object getRuntimeProperty(RuntimeProperty key)
	{
		return m_runtimeSettings.get(key.tag);
	}
}
