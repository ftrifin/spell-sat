///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.controls
// 
// FILE      : DisplayViewer.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.presentation.text.controls;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.astra.ses.spell.gui.core.model.notification.Input;
import com.astra.ses.spell.gui.core.model.types.ExecutorStatus;
import com.astra.ses.spell.gui.core.model.types.Severity;
import com.astra.ses.spell.gui.core.services.ServiceManager;
import com.astra.ses.spell.gui.presentation.text.model.ParagraphType;
import com.astra.ses.spell.gui.presentation.text.model.TextParagraph;
import com.astra.ses.spell.gui.services.ConfigurationManager;


/*******************************************************************************
 * @brief Text-based view of the procedure execution
 * @date 09/10/07
 * @author Rafael Chinchilla Camara (GMV)
 ******************************************************************************/
public class DisplayViewer
{
	// =========================================================================
	// # INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Text contents */
	private CustomStyledText m_text;
	
	/** Text contents */
	private ExecutorStatus previousStatus = null;
	
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------

	// =========================================================================
	// # ACCESSIBLE METHODS
	// =========================================================================

	/***************************************************************************
	 * Constructor.
	 * 
	 * @param view 
	 * 		Parent procedure view
	 * @param top
	 * 		Container composite
	 **************************************************************************/
	public DisplayViewer(Composite top)
	{
		m_text = new CustomStyledText(top);
		m_text.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL ));
		// Setup initial font
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		Font font = cfg.getFont("CODE");
		m_text.setFont(font);
	}

	/***************************************************************************
	 * Enable or disable the viewer
	 **************************************************************************/
	public void setEnabled( boolean enable )
	{
		m_text.setEnabled(enable);
	}

	/***************************************************************************
	 * Create a prompt
	 **************************************************************************/
	public void prompt( Input promptMsg, double msgTime )
	{
		Vector<String> msg = new Vector<String>();
		
		msg.addElement("REQUIRED USER INPUT: ");
		msg.addElement(promptMsg.getText());
		
		if (promptMsg.isList())
		{
			msg.addElement("Available options:");
			for (String option : promptMsg.getOptions())
			{
				msg.addElement("  " + option);
			}
		}
		addPrompt(msg, msgTime);
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
	public void zoom( boolean increase )
	{
		m_text.zoom(increase);
	}
	
	/***************************************************************************
	 * Change autoscroll mode
	 * @param enabled
	 **************************************************************************/
	public void setAutoscroll( boolean enabled )
	{
		m_text.setAutoScroll(enabled);
	}

	/***************************************************************************
	 * Callback for status notifications
	 **************************************************************************/
	public void notifyProcStatus( ExecutorStatus status )
	{
		// Do not consider WAITING as a relevant status to be show as message
		if ((status != ExecutorStatus.WAITING) && (status != previousStatus))
		{
			previousStatus = status;
			addInternal(status.toString());
		}
		ConfigurationManager rsc = (ConfigurationManager) ServiceManager.get(ConfigurationManager.ID);
		Color bkg = rsc.getProcedureColor(status);
		m_text.setRedraw(false);
		m_text.setBackground(bkg);
		m_text.setRedraw(true);
	}

	/***************************************************************************
	 * Add a normal message to the model
	 **************************************************************************/
	public void addMessage(String text, Severity severity, double msgTime )
	{
		TextParagraph p = null;
		if (severity == Severity.ERROR )
		{
			p = getTextParagraph(ParagraphType.ERROR,text,SWT.NORMAL, msgTime);
		}
		else if (severity == Severity.WARN )
		{
			p = getTextParagraph(ParagraphType.WARNING,text,SWT.NORMAL, msgTime);
		} 
		// When replaying prompts
		else if (severity == Severity.PROMPT )
		{
			p = getTextParagraph(ParagraphType.PROMPT,text,SWT.BOLD, msgTime);
		} 
		else 
		{
			p = getTextParagraph(ParagraphType.NORMAL,text,SWT.NORMAL, msgTime);
		}
		appendParagraph(p);
	}
	
	/***************************************************************************
	 * Add an internal message to the model
	 **************************************************************************/
	public void addInternal(String text)
	{
		double msgTime = System.currentTimeMillis() / 1000.0;
		TextParagraph p = getTextParagraph( ParagraphType.SPELL, text, SWT.BOLD, msgTime );
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
	 * Add a prompt output to the model
	 **************************************************************************/
	protected void addPrompt(Vector<String> text, double msgTime)
	{
		String textParagraph = "";
		for(String line : text) textParagraph += line + "\n";
		TextParagraph p = getTextParagraph(ParagraphType.PROMPT, textParagraph, SWT.BOLD, msgTime);
		appendParagraph(p);
	}

	/***************************************************************************
	 * Add a notification message to the model
	 **************************************************************************/
	protected void addNotification(String text, ParagraphType type, double msgTime )
	{
		TextParagraph p = getTextParagraph(type, text, SWT.ITALIC, msgTime );
		appendParagraph(p);
	}

	/***************************************************************************
	 * Add a notification message to the model
	 **************************************************************************/
	protected void addNotification(Vector<String> text, ParagraphType type, double msgTime )
	{
		String textParagraph = "";
		for(String line : text) textParagraph += line + "\n";
		TextParagraph p = getTextParagraph(type, textParagraph, SWT.ITALIC, msgTime );
		appendParagraph(p);
	}

	/***************************************************************************
	 * Add an error message to the model
	 **************************************************************************/
	protected void addError(String text, double msgTime )
	{
		TextParagraph p = getTextParagraph( ParagraphType.ERROR, text, SWT.BOLD, msgTime );
		appendParagraph(p);
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
	private TextParagraph getTextParagraph(ParagraphType t, String text, int style, double time) 
	{
		switch (t) 
		{
		case ERROR:
			return new TextParagraph( ParagraphType.ERROR, text, style, time );
		case WARNING:
			return new TextParagraph( ParagraphType.WARNING, text, style, time );
		case NOTIF_WARN:
			return new TextParagraph( ParagraphType.WARNING, text, style, time );
		case NOTIF_ERR:
			return new TextParagraph( ParagraphType.ERROR, text, style, time );
		case PROMPT:
			return new TextParagraph( ParagraphType.WARNING, text, style, time );
		case SPELL:
			return new TextParagraph( ParagraphType.NORMAL, text, style, time );
		default: /* NORMAL */
			return new TextParagraph( ParagraphType.NORMAL, text, style, time );
		}
	}
}
 