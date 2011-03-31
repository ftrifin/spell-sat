///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.services
// 
// FILE      : ServiceManager.java
//
// DATE      : 2008-11-21 08:58
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
package com.astra.ses.spell.gui.core.services;

import java.util.TreeMap;
import java.util.Vector;

import com.astra.ses.spell.gui.core.exceptions.NoSuchServiceException;


/*******************************************************************************
 * @brief Entry point for obtaining any GUI service.
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class ServiceManager
{
	private static ServiceManager		s_instance	= null;

	private TreeMap<String, Service>	m_services;
	private Vector<String>				m_sOrder;

	protected ServiceManager()
	{
		m_services = new TreeMap<String, Service>();
		m_sOrder = new Vector<String>();
	}

	public static void registerService(Service provider)
	{
		if (s_instance == null)
		{
			s_instance = new ServiceManager();
		}
		s_instance.addService(provider);
	}

	public static Service get(String serviceName) throws NoSuchServiceException
	{
		if (s_instance == null)
		{
			s_instance = new ServiceManager();
		}
		return s_instance.obtainService(serviceName);
	}

	protected void addService( Service provider )
	{
		if (m_services.containsKey(provider.getServiceId())) return;
		m_services.put(provider.getServiceId(), provider);
		m_sOrder.addElement(provider.getServiceId());
	}
	
	protected Service obtainService( String serviceName ) throws NoSuchServiceException
	{

		if (m_services.containsKey(serviceName))
		{
			return m_services.get(serviceName);
		}
		else
		{
			for(String id : m_services.keySet())
			{
				System.err.println("  -> " + id);
			}
			throw new NoSuchServiceException(serviceName);
		}
	}
}
