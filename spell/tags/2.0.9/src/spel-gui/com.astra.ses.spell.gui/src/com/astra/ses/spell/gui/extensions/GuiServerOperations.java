///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : GuiServerOperations.java
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
// SUBPROJECT: SPELL GUI Client
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.extensions;

import com.astra.ses.spell.gui.core.interfaces.IServerOperation;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;

public class GuiServerOperations implements IServerOperation
{
	public GuiServerOperations()
	{
		Logger.debug("Created", Level.INIT, this);
	}

	@Override
	public void notifyContextStarted(ContextInfo info)
	{
		Logger.debug("Fired [context started]", Level.COMM, this);
		ServerBridge.get().fireContextStarted(info);
	}

	@Override
	public void notifyContextStopped(ContextInfo info)
	{
		Logger.debug("Fired [context stopped]", Level.COMM, this);
		ServerBridge.get().fireContextStopped(info);
	}

	@Override
	public void notifyListenerConnected(ServerInfo info)
	{
		Logger.debug("Fired [listener connected]", Level.COMM, this);
		ServerBridge.get().fireListenerConnected(info);
	}

	@Override
	public void notifyListenerDisconnected()
	{
		Logger.debug("Fired [listener disconnected]", Level.COMM, this);
		ServerBridge.get().fireListenerDisconnected();
	}

	@Override
	public void notifyListenerError(ErrorData error)
	{
		Logger.debug("Fired [listener error]", Level.COMM, this);
		ServerBridge.get().fireListenerError(error);
	}

	@Override
	public String getListenerId()
	{
		return "com.astra.ses.spell.gui.extensions.ServerOperations";
	}

}
