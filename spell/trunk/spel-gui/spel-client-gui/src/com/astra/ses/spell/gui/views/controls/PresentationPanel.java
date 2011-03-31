///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : PresentationPanel.java
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
package com.astra.ses.spell.gui.views.controls;

import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.comm.commands.ICommands;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Level;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.Logger;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.services.ConfigurationManager;
import com.astra.ses.spell.gui.views.ProcedureView;


/*******************************************************************************
 * @brief Composite which contains the set of controls used for changing pages
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class PresentationPanel extends Composite implements
		SelectionListener
{
	// =========================================================================
	// # STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private static final String CMD_ID = "CommandId";
	private static final int NUM_ELEMENTS = 5;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	private ProcedureView m_view;
	private Label m_stageDisplay;
	private String m_currentStage;
	private Text m_procDisplay;
	private Color m_okColor;
	private Color m_warningColor;
	private Color m_errorColor;
	private TreeMap<String,Button> m_presentationButton;
	private TreeMap<String,Integer> m_presentationIndex;
	private Composite m_presentationsPanel;
	private Button m_btnIncrFont;
	private Button m_btnDecrFont;
	private Text m_satName;
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	
	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 **************************************************************************/
	public PresentationPanel(ProcedureView view, Composite parent, int style, int numPresentations )
	{
		super(parent, style);

		ConfigurationManager rsc = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);

		m_view = view;
		m_currentStage = null;
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		layout.numColumns = NUM_ELEMENTS;
		setLayout(layout);
		
		Composite stagePanel = new Composite(this,SWT.BORDER);
		GridLayout splayout = new GridLayout();
		splayout.marginHeight = 0;
		splayout.marginWidth = 0;
		splayout.marginLeft = 10;
		splayout.numColumns = 1;
		stagePanel.setLayout(splayout);
		GridData spData = new GridData( GridData.FILL_HORIZONTAL );
		spData.horizontalSpan = NUM_ELEMENTS;
		stagePanel.setLayoutData( spData );
		
		m_stageDisplay = new Label(stagePanel,SWT.NONE);
		GridData sdd = new GridData( GridData.FILL_HORIZONTAL );
		m_stageDisplay.setLayoutData(sdd);
		Font sFont = rsc.getFont("GUI_NORM");
		m_stageDisplay.setFont(sFont);
		
		resetStage();

		m_presentationButton = new TreeMap<String,Button>();
		m_presentationIndex = new TreeMap<String,Integer>();

		m_presentationsPanel = new Composite(this,SWT.NONE);
		GridLayout playout = new GridLayout();
		playout.marginHeight = 0;
		playout.marginWidth = 0;
		playout.numColumns = numPresentations;
		m_presentationsPanel.setLayout(playout);
		
		m_btnIncrFont = new Button(this, SWT.PUSH );
		m_btnIncrFont.setText("");
		Image image = Activator.getImageDescriptor(
				ICommands.OtherIcons[ICommands.IMG_MORE]).createImage();
		m_btnIncrFont.setImage(image);
		m_btnIncrFont.setData(CMD_ID, "+");
		m_btnIncrFont.addSelectionListener(this);
		m_btnIncrFont.setToolTipText("Increase font size");

		m_btnDecrFont = new Button(this, SWT.PUSH );
		m_btnDecrFont.setText("");
		image = Activator.getImageDescriptor(
				ICommands.OtherIcons[ICommands.IMG_LESS]).createImage();
		m_btnDecrFont.setImage(image);
		m_btnDecrFont.setData(CMD_ID, "-");
		m_btnDecrFont.addSelectionListener(this);
		m_btnDecrFont.setToolTipText("Decrease font size");

		Font bigFont = rsc.getFont("HEADER");

		m_okColor = rsc.getGuiColor("TABLE_BG");
		m_warningColor = rsc.getStatusColor(ItemStatus.WARNING);
		m_errorColor = rsc.getStatusColor(ItemStatus.ERROR);
		
		m_procDisplay = new Text(this, SWT.BORDER | SWT.SINGLE);
		m_procDisplay.setFont(bigFont);
		m_procDisplay.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_procDisplay.setText("");
		m_procDisplay.setBackground( m_okColor );
		m_procDisplay.setEditable(false);
		m_procDisplay.setToolTipText("Procedure display");
		
		m_satName = new Text(this, SWT.BORDER | SWT.SINGLE );
		m_satName.setFont( rsc.getFont("BANNER") );
		m_satName.setText( m_view.getDomain() );
		m_satName.setBackground( m_okColor );
		m_satName.setToolTipText("Satellite name");
		m_satName.setEditable(false);	
	}

	/***************************************************************************
	 * Add a presentation button
	 **************************************************************************/
	public void addPresentation( String title, String desc, Image icon, int pageIndex )
	{
		Logger.debug("Added presentation '" + title + "' with index " + pageIndex, Level.GUI, this);
		Button btn = new Button(m_presentationsPanel, SWT.TOGGLE);
		btn.setText(title);
		btn.setImage(icon);
		btn.setData(CMD_ID,title);
		btn.addSelectionListener(this);
		btn.setToolTipText(desc);
		m_presentationButton.put(title,btn);
		m_presentationIndex.put(title,pageIndex);
	}

	/***************************************************************************
	 * Display a message
	 **************************************************************************/
	public void displayMessage( String message, Severity sev )
	{
		m_procDisplay.setText(message);
		if (sev == Severity.INFO)
		{
			m_procDisplay.setBackground(m_okColor);
		}
		else if (sev == Severity.WARN)
		{
			m_procDisplay.setBackground(m_warningColor);
		}
		else if (sev == Severity.ERROR)
		{
			m_procDisplay.setBackground(m_errorColor);
		}
	}

	/***************************************************************************
	 * Reset all controls
	 **************************************************************************/
	public void reset()
	{
		m_procDisplay.setText("");
		m_procDisplay.setBackground(m_okColor);
		resetStage();
	}

	/***************************************************************************
	 * Reset all controls
	 **************************************************************************/
	public void setEnabled( boolean enabled )
	{
		super.setEnabled(enabled);
		m_procDisplay.setEnabled(enabled);
		m_satName.setEnabled(enabled);
	}

	/***************************************************************************
	 * Set current stage
	 **************************************************************************/
	public void setStage( String id, String title )
	{
		if (id != null && !id.isEmpty())
		{
			if (m_currentStage == null || !m_currentStage.equals(id))
			{
				if (!title.trim().isEmpty())
				{
					title = " - " + title;
				}
				m_stageDisplay.setText( "Current step: " + id + title);
				m_currentStage = id;
			}
		}
	}

	/***************************************************************************
	 * Reset the current stage
	 **************************************************************************/
	public void resetStage()
	{
		m_currentStage = null;
		m_stageDisplay.setText( "Current step: None" );
	}

	/***************************************************************************
	 * Select the given presentation
	 **************************************************************************/
	public void selectPresentation( String title )
	{
		Logger.debug("Selected presentation '" + title + "'", Level.GUI, this);
		m_view.showPresentation( m_presentationIndex.get(title) );
		for(String btitle : m_presentationButton.keySet())
		{
			m_presentationButton.get(btitle).setSelection(false);
		}
		m_presentationButton.get(title).setSelection(true);
	}

	/***************************************************************************
	 * Callback for control buttons
	 **************************************************************************/
	public void widgetDefaultSelected(SelectionEvent e)
	{
		widgetSelected(e);
	}

	/***************************************************************************
	 * Callback for control buttons
	 **************************************************************************/
	public void widgetSelected(SelectionEvent e)
	{
		String title = (String) e.widget.getData(CMD_ID);
		if ( title.equals("+") )
		{
			m_view.zoom(true);
		}
		else if ( title.equals("-") )
		{
			m_view.zoom(false);
		}
		else if ( m_presentationIndex.containsKey(title) )
		{
			selectPresentation(title);
		}
		else
		{
			Logger.error("Unexpected action: " + title, Level.GUI, this);
		}
	}
}
