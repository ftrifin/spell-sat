///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.controls
// 
// FILE      : DisplayViewer.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.presentation.text.controls;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.interfaces.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.preferences.interfaces.IConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.presentation.text.model.ParagraphType;
import com.astra.ses.spell.gui.presentation.text.model.TextParagraph;

/*******************************************************************************
 * @brief Text-based view of the procedure execution
 * @date 09/10/07
 ******************************************************************************/
public class DisplayViewer implements IPropertyChangeListener
{
	private static IConfigurationManager s_cfg = null;

	/** Text contents */
	private CustomStyledText m_text;
	/** Show timestamp control */
	private Button m_chkTimestamp;
	/** Previous status */
	private ExecutorStatus m_previousStatus = null;

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param view
	 *            Parent procedure view
	 * @param top
	 *            Container composite
	 **************************************************************************/
	public DisplayViewer(Composite top)
	{
		if (s_cfg == null)
		{
			s_cfg = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		}

		Composite base = new Composite(top, SWT.NONE);
		base.setLayoutData( new GridData( GridData.FILL_BOTH ));
		base.setLayout( new GridLayout(1,true) );
		
		m_text = new CustomStyledText(base);
		m_text.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		m_text.setBackground(s_cfg.getProcedureColor(ExecutorStatus.LOADED));

		Composite tools = new Composite(base, SWT.BORDER);
		tools.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		tools.setLayout( new RowLayout( SWT.HORIZONTAL ) );
		
		m_chkTimestamp = new Button(tools, SWT.CHECK );
		m_chkTimestamp.setText("Show message timestamp");
		m_chkTimestamp.addSelectionListener( new SelectionAdapter()
		{
			public void widgetSelected( SelectionEvent ev )
			{
				toggleTimestamp();
			}
		}
		);
		
		s_cfg.addPropertyChangeListener(this);
		m_text.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				unsubscribefFromProperties();
			}
		});
	}

	/***************************************************************************
	 * Toggle timestamp display
	 **************************************************************************/
	private void toggleTimestamp()
	{
		m_text.setShowTimestamp( m_chkTimestamp.getSelection() );
	}
	
	/***************************************************************************
	 * Enable or disable the viewer
	 **************************************************************************/
	public void setEnabled(boolean enable)
	{
		m_text.setEnabled(enable);
		m_chkTimestamp.setEnabled(enable);
	}

	/***************************************************************************
	 * Refresh the text view with the appropiate contents
	 **************************************************************************/
	public void refresh()
	{
		// Nothing to do
	}

	/***************************************************************************
	 * Receive the focus
	 **************************************************************************/
	public void setFocus()
	{
		m_text.setFocus();
	}

	/***************************************************************************
	 * Increase or decrease the font size
	 **************************************************************************/
	public void zoom(boolean increase)
	{
		m_text.zoom(increase);
	}

	/***************************************************************************
	 * Change autoscroll mode
	 * 
	 * @param enabled
	 **************************************************************************/
	public void setAutoscroll(boolean enabled)
	{
		m_text.setAutoScroll(enabled);
	}

	/***************************************************************************
	 * Callback for status notifications
	 **************************************************************************/
	public void notifyProcStatus(ExecutorStatus status)
	{
		// Do not consider WAITING as a relevant status to be show as message
		if ((status != ExecutorStatus.PROMPT) && (status != ExecutorStatus.WAITING) && (status != m_previousStatus))
		{
			m_previousStatus = status;
		}
		m_text.setRedraw(false);
		m_text.setBackground(s_cfg.getProcedureColor(status));
		m_text.setRedraw(true);
	}

	/***************************************************************************
	 * Add a normal message to the model
	 **************************************************************************/
	public synchronized void addMessage(String text, Severity severity, Scope scope, long sequence)
	{
		TextParagraph p = null;
		if (severity == Severity.ERROR)
		{
			p = getTextParagraph(ParagraphType.ERROR, scope, text, sequence);
		}
		else if (severity == Severity.WARN)
		{
			p = getTextParagraph(ParagraphType.WARNING, scope, text, sequence);
		}
		// When replaying prompts
		else if (severity == Severity.PROMPT)
		{
			p = getTextParagraph(ParagraphType.PROMPT, scope, text, sequence);
		}
		else
		{
			p = getTextParagraph(ParagraphType.NORMAL, scope, text, sequence);
		}
		appendParagraph(p);
	}

	/***************************************************************************
	 * Clear the text view model
	 **************************************************************************/
	public void clear()
	{
		m_text.clear();
	}

	/***************************************************************************
	 * Obtain the context as lines of text
	 **************************************************************************/
	public String[] getTextLines()
	{
		return m_text.getTextLines();
	}

	/***************************************************************************
	 * Append a paragraph and show last line
	 **************************************************************************/
	private void appendParagraph(TextParagraph p)
	{
		m_text.append(p);
	}

	/***************************************************************************
	 * Create a text parahraph with the given type.
	 **************************************************************************/
	private TextParagraph getTextParagraph(ParagraphType t, Scope scope, String text, long sequence)
	{
		switch (t)
		{
		case ERROR:
			return new TextParagraph(ParagraphType.ERROR, scope, text, sequence);
		case WARNING:
			return new TextParagraph(ParagraphType.WARNING, scope, text, sequence);
		case NOTIF_WARN:
			return new TextParagraph(ParagraphType.WARNING, scope, text, sequence);
		case NOTIF_ERR:
			return new TextParagraph(ParagraphType.ERROR, scope, text, sequence);
		case PROMPT:
			return new TextParagraph(ParagraphType.NORMAL, scope, text, sequence);
		case SPELL:
			return new TextParagraph(ParagraphType.NORMAL, scope, text, sequence);
		default: /* NORMAL */
			return new TextParagraph(ParagraphType.NORMAL, scope, text, sequence);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.startsWith(PreferenceCategory.PROC_COLOR.tag))
		{
			String statusStr = property.substring(PreferenceCategory.PROC_COLOR.tag.length() + 1);
			ExecutorStatus st = ExecutorStatus.valueOf(statusStr);
			m_text.setBackground(s_cfg.getProcedureColor(st));
		}
	}

	/***************************************************************************
	 * Stop listening from preferences changes
	 **************************************************************************/
	private void unsubscribefFromProperties()
	{
		IConfigurationManager rsc = (IConfigurationManager) ServiceManager.get(IConfigurationManager.class);
		rsc.removePropertyChangeListener(this);
	}
}
