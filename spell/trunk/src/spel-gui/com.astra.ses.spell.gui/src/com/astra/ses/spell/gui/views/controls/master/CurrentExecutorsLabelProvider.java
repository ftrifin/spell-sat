///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls.master
// 
// FILE      : CurrentExecutorsLabelProvider.java
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
package com.astra.ses.spell.gui.views.controls.master;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.interfaces.IProcedureClient;
import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.utils.Logger;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;

/**
 * @author Rafael Chinchilla
 *
 */
public class CurrentExecutorsLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider 
{
	private static IConfigurationManager s_cfg = null;
	private Image m_tickImg;

	private IConfigurationManager getConfig()
	{
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}
		return s_cfg;
	}
	
	public CurrentExecutorsLabelProvider()
	{
		super();
		m_tickImg = Activator.getImageDescriptor("icons/tick.png").createImage();
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose()
    {
    	super.dispose();
    	m_tickImg.dispose();
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex)
    {
    	if (columnIndex == CurrentExecutorsTableItems.CONTROLLED.ordinal()) 
    	{
    		IProcedureClient cClient = null;
        	if (element instanceof IProcedure)
        	{
    		    IProcedure proc = (IProcedure) element;
		    	cClient = proc.getRuntimeInformation().getControllingClient();
        	}
	    	if ((cClient != null)&&(!cClient.getKey().isEmpty()))
			{
	    		return m_tickImg;
			}
    	}
    	else if (columnIndex == CurrentExecutorsTableItems.MONITORED.ordinal())
    	{
    		int numC = 0;
        	if (element instanceof IProcedure)
        	{
    		    IProcedure proc = (IProcedure) element;
    			Logger.debug("Instance in provider " + proc, Level.GUI, this);
    		    IProcedureClient[] clts = proc.getRuntimeInformation().getMonitoringClients();
    			Logger.debug("Monitorings " + clts, Level.GUI, this);
    		    if (clts != null)
    		    {
    		    	numC = clts.length;
    		    }
        	}
        	Logger.debug("Number of clients " + numC, Level.GUI, this);
	    	if (numC>0)
	    	{
	    		return m_tickImg;
	    	}
    	}
	    return null;
    }
    
	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex)
    {
    	if (element instanceof IProcedure)
    	{
		    IProcedure proc = (IProcedure) element;
		    CurrentExecutorsTableItems item = CurrentExecutorsTableItems.index(columnIndex);
		    switch(item)
		    {
		    case PROCEDURE:
		    	return proc.getProcName();
		    case STATUS:
		    	return proc.getRuntimeInformation().getStatus().description;
		    }
    	}
    	else if (columnIndex ==0)
    	{
    		return element.toString();
    	}
    	return null;
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
     */
    @Override
    public Color getForeground(Object element, int columnIndex)
    {
	    return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    }

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
     */
    @Override
    public Color getBackground(Object element, int columnIndex)
    {
    	if (element instanceof IProcedure)
    	{
		    IProcedure proc = (IProcedure) element;
		    return getConfig().getProcedureColor(proc.getRuntimeInformation().getStatus());
    	}
    	return null;
    }

}
