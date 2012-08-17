///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.status
// 
// FILE      : StatusControlContribution.java
//
// DATE      : 2008-11-21 08:55
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
package com.astra.ses.spell.gui.views.controls.status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

import com.astra.ses.spell.gui.core.interfaces.ICoreContextOperationListener;
import com.astra.ses.spell.gui.core.interfaces.ICoreServerOperationListener;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.notification.ErrorData;
import com.astra.ses.spell.gui.core.model.server.ContextInfo;
import com.astra.ses.spell.gui.core.model.server.ServerInfo;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.extensions.ServerBridge;
import com.astra.ses.spell.gui.model.ConnectionStatusConstants;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;

public class StatusControlContribution extends WorkbenchWindowControlContribution implements ICoreServerOperationListener, ICoreContextOperationListener
{
	IConfigurationManager s_cfg = null;

	private static Color s_okColor = null;
	private static Color s_warnColor = null;
	private static Color s_errorColor = null;
	private static Font s_boldFont = null;
	private static ServerInfo s_serverInfo = null;
	private static ContextInfo s_contextInfo = null;

	public static final String ID = "com.astra.ses.spell.gui.controls.StatusControlContribution";

	private Label m_connection;
	private Label m_context;
	private Label m_mode;
	private Label m_domain;
	private Label m_driver;
	private Label m_family;

	private Composite m_base;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public StatusControlContribution()
	{
		super(ID);
		if (s_okColor == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
			s_okColor = s_cfg.getStatusColor(ItemStatus.SUCCESS);
			s_warnColor = s_cfg.getStatusColor(ItemStatus.WARNING);
			s_errorColor = s_cfg.getStatusColor(ItemStatus.ERROR);
			s_boldFont = s_cfg.getFont(FontKey.GUI_BOLD);
		}
		Logger.debug("Created", Level.GUI, this);
	}

	/***************************************************************************
	 * Create the contents
	 **************************************************************************/
	@Override
	protected Control createControl(Composite parent)
	{
		m_base = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 12;
		m_base.setLayout(layout);

		// LABEL
		GridData serverLabelData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		Label label = new Label(m_base, SWT.NONE);
		label.setText("Server: ");
		label.setFont(s_boldFont);
		label.setLayoutData(serverLabelData);
		// WIDGET
		GridData serverWidgetData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		m_connection = new Label(m_base, SWT.BORDER);
		m_connection.setText(ConnectionStatusConstants.DISCONNECTED);
		m_connection.setBackground(s_warnColor);
		m_connection.setAlignment(SWT.CENTER);
		m_connection.setLayoutData(serverWidgetData);

		// LABEL
		GridData contextLabelData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		Label label2 = new Label(m_base, SWT.NONE);
		label2.setText("Context: ");
		label2.setFont(s_boldFont);
		label2.setLayoutData(contextLabelData);
		// WIDGET
		GridData contextWidgetData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		m_context = new Label(m_base, SWT.BORDER);
		m_context.setText(ConnectionStatusConstants.UNKNOWN);
		m_context.setBackground(s_warnColor);
		m_context.setAlignment(SWT.CENTER);
		m_context.setLayoutData(contextWidgetData);

		// LABEL
		GridData modeLabelData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		Label label3 = new Label(m_base, SWT.NONE);
		label3.setText("Mode: ");
		label3.setFont(s_boldFont);
		label3.setLayoutData(modeLabelData);
		// WIDGET
		GridData modeWidgetData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		m_mode = new Label(m_base, SWT.BORDER);
		m_mode.setText(ConnectionStatusConstants.UNKNOWN);
		m_mode.setBackground(s_warnColor);
		m_mode.setAlignment(SWT.CENTER);
		m_mode.setLayoutData(modeWidgetData);

		// LABEL
		GridData domainLabelData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		Label label4 = new Label(m_base, SWT.NONE);
		label4.setFont(s_boldFont);
		label4.setText("Domain: ");
		label4.setLayoutData(domainLabelData);
		// WIDGET
		GridData domainWidgetData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		m_domain = new Label(m_base, SWT.BORDER);
		m_domain.setText(ConnectionStatusConstants.UNKNOWN);
		m_domain.setBackground(s_warnColor);
		m_domain.setAlignment(SWT.CENTER);
		m_domain.setLayoutData(domainWidgetData);

		// LABEL
		GridData familyLabelData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		Label label5 = new Label(m_base, SWT.NONE);
		label5.setFont(s_boldFont);
		label5.setText("Family: ");
		label5.setLayoutData(familyLabelData);
		// WIDGET
		GridData familyWidgetData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		m_family = new Label(m_base, SWT.BORDER);
		m_family.setText(ConnectionStatusConstants.UNKNOWN);
		m_family.setBackground(s_warnColor);
		m_family.setAlignment(SWT.CENTER);
		m_family.setLayoutData(familyWidgetData);

		// LABEL
		GridData driverLabelData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		Label label6 = new Label(m_base, SWT.NONE);
		label6.setFont(s_boldFont);
		label6.setText("Driver: ");
		label6.setLayoutData(driverLabelData);
		// WIDGET
		GridData driverWidgetData = new GridData(SWT.CENTER, SWT.CENTER, false, true);
		m_driver = new Label(m_base, SWT.BORDER);
		m_driver.setText(ConnectionStatusConstants.UNKNOWN);
		m_driver.setBackground(s_warnColor);
		m_driver.setAlignment(SWT.CENTER);
		m_driver.setLayoutData(driverWidgetData);

		m_base.pack();

		ServerBridge.get().addContextListener(this);
		ServerBridge.get().addServerListener(this);

		updateServer(false);
		updateContext(false);

		parent.pack();
		return m_base;
	}

	/***************************************************************************
	 * Dispose the control
	 **************************************************************************/
	@Override
	public void dispose()
	{
		ServerBridge.get().removeContextListener(this);
		ServerBridge.get().removeServerListener(this);
		super.dispose();
	}

	/***************************************************************************
	 * Obtain the listener id
	 **************************************************************************/
	@Override
	public String getListenerId()
	{
		return ID;
	}

	/***************************************************************************
	 * Listener connected callback
	 **************************************************************************/
	@Override
	public void notifyListenerConnected(ServerInfo info)
	{
		s_serverInfo = info;
		if (isVisible())
		{
			updateServer(false);
		}
	}

	/***************************************************************************
	 * Listener disconnected callback
	 **************************************************************************/
	@Override
	public void notifyListenerDisconnected()
	{
		s_serverInfo = null;
		if (isVisible())
		{
			updateServer(false);
		}
	}

	/***************************************************************************
	 * Listener error callback
	 **************************************************************************/
	@Override
	public void notifyListenerError(ErrorData error)
	{
		s_serverInfo = null;
		if (isVisible())
		{
			updateServer(true);
		}
	}

	/***************************************************************************
	 * Context attached callback
	 **************************************************************************/
	@Override
	public void notifyContextAttached(ContextInfo ctx)
	{
		s_contextInfo = ctx;
		if (isVisible())
		{
			updateContext(false);
		}
	}

	/***************************************************************************
	 * Context detached callback
	 **************************************************************************/
	@Override
	public void notifyContextDetached()
	{
		s_contextInfo = null;
		if (isVisible())
		{
			updateContext(false);
		}
	}

	/***************************************************************************
	 * Context error callback
	 **************************************************************************/
	@Override
	public void notifyContextError(ErrorData error)
	{
		s_contextInfo = null;
		if (isVisible())
		{
			updateContext(true);
		}
	}

	@Override
	public void notifyContextStarted(ContextInfo info)
	{
	}

	@Override
	public void notifyContextStopped(ContextInfo info)
	{
	}

	/***************************************************************************
	 * Update server information
	 **************************************************************************/
	private void updateServer(boolean connectionLost)
	{
		if (s_serverInfo != null)
		{
			String name = s_serverInfo.getName();
			String role = s_serverInfo.getRole().name();
			m_connection.setText("  " + name + "  ");
			m_mode.setText(role);
			m_mode.setBackground(s_okColor);
			m_connection.setBackground(s_okColor);
		}
		else
		{
			m_connection.setText(ConnectionStatusConstants.DISCONNECTED);
			m_mode.setText(ConnectionStatusConstants.UNKNOWN);
			if (connectionLost)
			{
				m_connection.setBackground(s_errorColor);
				m_mode.setBackground(s_errorColor);
			}
			else
			{
				m_connection.setBackground(s_warnColor);
				m_mode.setBackground(s_warnColor);
			}
		}
		m_base.pack();
	}

	/***************************************************************************
	 * Update context information
	 **************************************************************************/
	private void updateContext(boolean connectionLost)
	{
		if (s_contextInfo != null)
		{
			String ctxName = s_contextInfo.getName();
			String domain = s_contextInfo.getSC();
			m_context.setText("  " + ctxName + "  ");
			m_context.setBackground(s_okColor);

			m_domain.setText("  " + domain + "  ");
			m_domain.setBackground(s_okColor);

			m_driver.setText("  " + s_contextInfo.getDriver() + "  ");
			m_driver.setBackground(s_okColor);

			m_family.setText("  " + s_contextInfo.getFamily() + "  ");
			m_family.setBackground(s_okColor);

			String role = s_serverInfo.getRole().name();
			m_mode.setText(role);
			m_mode.setBackground(s_okColor);
		}
		else
		{
			m_mode.setText(ConnectionStatusConstants.UNKNOWN);
			m_domain.setText(ConnectionStatusConstants.UNKNOWN);
			m_driver.setText(ConnectionStatusConstants.UNKNOWN);
			m_family.setText(ConnectionStatusConstants.UNKNOWN);
			if (connectionLost)
			{
				m_context.setText(ConnectionStatusConstants.FAILURE);
				m_context.setBackground(s_errorColor);
			}
			else
			{
				m_context.setText(ConnectionStatusConstants.UNKNOWN);
				m_context.setBackground(s_warnColor);
			}
			m_mode.setBackground(s_warnColor);
			m_domain.setBackground(s_warnColor);
			m_driver.setBackground(s_warnColor);
			m_family.setBackground(s_warnColor);
		}
		m_base.pack();
	}
}
