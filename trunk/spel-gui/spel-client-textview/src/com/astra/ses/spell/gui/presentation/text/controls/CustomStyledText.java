///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.controls
// 
// FILE      : CustomStyledText.java
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

import com.astra.ses.spell.gui.presentation.text.model.ParagraphType;
import com.astra.ses.spell.gui.presentation.text.model.TextParagraph;
import com.astra.ses.spell.gui.presentation.text.model.TextViewContent;
import com.astra.ses.spell.gui.presentation.text.model.TextViewContentListener;
import com.astra.ses.spell.gui.presentation.text.model.TextViewLine;

public class CustomStyledText extends Composite implements ControlListener, 
														   SelectionListener, 
														   MouseWheelListener, 
														   TextViewContentListener,
														   LineStyleListener,
														   PaintObjectListener
{
	
	private static GlyphMetrics s_metrics = new GlyphMetrics(0, 0, 16);
	private static Font         s_boldFont;
	private static Font         s_normalFont;
	  /** Maximum font size */
    private static final int MAX_FONT_SIZE = 16;
    /** Minimum font size */
    private static final int MIN_FONT_SIZE = 7;
	
	/** Holds the styled text contents model */
	private TextViewContent	m_model;
	/** Holds the styled text widget */
	private StyledText      m_view;
	/** Holds the position of the data window */
	private int             m_viewWindowStart;
	/** Holds the size of the data window */
	private int             m_viewWindowLength;
	/** Holds the total amount of data (used for the scrollbar */
	private int             m_totalDataLength;
	/** Holds the composite vertical scrollbar */
	private ScrollBar		m_scrollBar;
	/** True if the widget shall follow the latest data */
	private boolean			m_autoScroll;
	/** Holds the current font size */
	private int             m_fontSize;

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public CustomStyledText(Composite parent) 
	{
		super(parent, SWT.BORDER | SWT.V_SCROLL );
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
 	    setLayout(layout);

 	    // Construct the model and the view
		m_model = new TextViewContent();
		m_view = new StyledText( this, SWT.NONE | SWT.DOUBLE_BUFFERED );
		m_view.setContent(m_model);
		m_view.setEditable(false);
		m_view.setLineSpacing(0);
		m_view.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		
		// Get the vertical scroll var
		m_scrollBar = getVerticalBar();
		
		// Set the initial auto scroll value
		m_autoScroll = true;

		// Setup listeners
		// For the control-resize event
		m_view.addControlListener(this);
		// For the styles
		m_view.addLineStyleListener(this);
		// Image drawing
		m_view.addPaintObjectListener(this);
		// For the scroll drag and click events
		m_scrollBar.addSelectionListener(this);
		// For the mouse wheel scroll
		addMouseWheelListener(this);
		// For the text changed event
		m_model.addTextViewContentListener(this);

		// Scrollbar data
		m_viewWindowStart = 0;
		m_viewWindowLength = 50;
		m_totalDataLength = 0;
		
		// Scrollbar initialization
		m_scrollBar.setVisible(true);
		m_scrollBar.setMinimum(0);
		m_scrollBar.setIncrement(1);
		m_scrollBar.setPageIncrement(10);
		m_scrollBar.setSelection(m_viewWindowStart);
		m_scrollBar.setMaximum(m_totalDataLength);
		
		// Initial position of the data view
		m_model.setViewWindow(m_viewWindowStart, m_viewWindowLength,m_autoScroll);
	
		layout();
	}

	/**************************************************************************
	 * Append a line to the widget
	 *************************************************************************/
	public void append( TextParagraph p )
	{
		String[] lines = p.getText();
		boolean firstLine = true;
		for( String line : lines )
		{
			if (firstLine)
			{
				firstLine = false;
				m_model.append( new TextViewLine( line, p.getStyle(), 
						        p.getBackgroundColor(), p.getType(), p.getTime() ));
			}
			else
			{
				m_model.append( new TextViewLine( line, p.getStyle(), 
						        p.getBackgroundColor(), ParagraphType.NORMAL, p.getTime() ));
			}
		}
		m_totalDataLength = m_model.getTotalDataSize();
		// Position the scrollbar
		if (m_autoScroll)
		{
			int height = m_view.getClientArea().height;
			int lineHeight = m_view.getLineSpacing() + m_view.getLineHeight();
			// Establish the size of the buffer
			m_viewWindowLength = (height / lineHeight);
			int start = m_totalDataLength - m_viewWindowLength;
			if (start<0) start = 0;
			m_model.setViewWindow(start, m_viewWindowLength, true);
			m_scrollBar.setSelection( m_scrollBar.getMaximum() );
		}
	}

	/**************************************************************************
	 * Set the autoscroll value
	 *************************************************************************/
	public void setAutoScroll( boolean auto )
	{
		m_autoScroll = auto;
		m_model.setViewWindow(m_viewWindowStart, m_viewWindowLength, m_autoScroll);
	}

	/**************************************************************************
	 * Clear the widget
	 *************************************************************************/
	public void clear()
	{
		m_model.clear();
		m_scrollBar.setSelection( 0 );
		m_model.setViewWindow(0, m_viewWindowLength, m_autoScroll);
	}

	/**************************************************************************
	 * Set the widget text
	 *************************************************************************/
	public void setText( String text )
	{
		m_model.setText(text);
	}

	/**************************************************************************
	 * Get all lines of text
	 *************************************************************************/
	public String[] getTextLines()
	{
		return m_model.getAllLines();
	}

	/**************************************************************************
	 * Increase or decrease the font size 
	 *************************************************************************/
	public void zoom( boolean increase )
	{
		if ( increase && (m_fontSize==MAX_FONT_SIZE)) return;
		if ( !increase && (m_fontSize==MIN_FONT_SIZE)) return;
        if (increase)
        {
                m_fontSize = Math.min(m_fontSize + 1, MAX_FONT_SIZE);
        }
        else
        {
                m_fontSize = Math.max(m_fontSize - 1, MIN_FONT_SIZE);
        }
        FontData[] fdata = m_view.getFont().getFontData();
        for(int index=0; index<fdata.length; index++)
        {
        	fdata[index].setHeight(m_fontSize);
        }
        Font newFont = new Font(Display.getCurrent(),fdata);
        setFont( newFont );
	}
	
	/**************************************************************************
	 * Override the listener mechanism
	 *************************************************************************/
	public void addMouseMoveListener( MouseMoveListener lst )
	{
		m_view.addMouseMoveListener(lst);
	}

	/**************************************************************************
	 * Override the listener mechanism
	 *************************************************************************/
	public void addMouseListener( MouseListener lst )
	{
		m_view.addMouseListener(lst);
	}

	/**************************************************************************
	 * Disposal
	 *************************************************************************/
	public void dispose()
	{
		super.dispose();
		m_model.clear();
	}

	/**************************************************************************
	 * Set the font
	 *************************************************************************/
	public void setFont( Font font )
	{
		// Get the font size
        m_fontSize = font.getFontData()[0].getHeight();
		// Assign the font to the view
		m_view.setFont(font);
		// Recreate the normal and bold fonts used to paint the text
		s_normalFont = font;
		FontData[] data = s_normalFont.getFontData();
		for (int i = 0; i < data.length; i++) 
		{
			data[i].setStyle(SWT.BOLD);
		}
		s_boldFont = new Font(Display.getCurrent(),data);
		// Calculate changes
		recalculateAfterSizeChange();
	}

	/**************************************************************************
	 * Set the focus
	 *************************************************************************/
	public boolean setFocus()
	{
		return m_view.setFocus();
	}
	
	/**************************************************************************
	 * Set the background color
	 *************************************************************************/
	public void setBackground( Color c )
	{
		m_view.setBackground( c );
	}
	
	@Override
	/**************************************************************************
	 * Callback from ControlListener interface
	 *************************************************************************/
	public void controlMoved(ControlEvent e) {} 

	@Override
	/**************************************************************************
	 * Callback from ControlListener interface
	 *************************************************************************/
	public void controlResized(ControlEvent e) 
	{
		recalculateAfterSizeChange();
	}

	@Override
	/**************************************************************************
	 * Callback from SelectionListener interface of the scrollbar
	 *************************************************************************/
	public void widgetDefaultSelected(SelectionEvent e) 
	{
		widgetSelected(e);
	}

	@Override
	/**************************************************************************
	 * Callback from SelectionListener interface of the scrollbar
	 *************************************************************************/
	public void widgetSelected(SelectionEvent e) 
	{
		// Now we are moving the view position with the scroll value
		int scrollValue = m_scrollBar.getSelection();
		m_model.setViewWindow(scrollValue, m_viewWindowLength,m_autoScroll);
	}

	@Override
	/**************************************************************************
	 * Callback from MouseWheelListener interface for the composite
	 *************************************************************************/
	public void mouseScrolled(MouseEvent e) 
	{
		// Now we are moving the view position with the scroll value
		int scrollValue = m_scrollBar.getSelection();
		m_model.setViewWindow(scrollValue, m_viewWindowLength,m_autoScroll);
	}

	@Override
	/**************************************************************************
	 * Callback from TextChangeListener interface for the model. Called when
	 * data window is scrolled
	 *************************************************************************/
	public void dataWindowMoved( TextViewContent source )
	{
		// Update the total amount of data
		m_totalDataLength = m_model.getTotalDataSize();
		if (m_viewWindowLength > m_totalDataLength)
		{
			m_scrollBar.setMaximum( 1 );
		}
		else
		{
			int scrollMax = m_totalDataLength - m_viewWindowLength+1;
			m_scrollBar.setMaximum( scrollMax );
		}
	}

	@Override
	/**************************************************************************
	 * Callback from TextChangeListener interface for the model. Called when
	 * data is added to the model.
	 *************************************************************************/
	public void dataChanged( TextViewContent source ) 
	{
		// The data in the contents has changed. To take into account the scrolling,
		// update the data window position
		m_viewWindowStart = m_model.getViewWindowPosition();
		// Update the total amount of data
		m_totalDataLength = m_model.getTotalDataSize();
		// Set the maximum value for the scrollbar: take into account the size of the window
		if (m_viewWindowLength > m_totalDataLength)
		{
			m_scrollBar.setMaximum( 1 );
		}
		else
		{
			int scrollMax = m_totalDataLength - m_viewWindowLength;
			m_scrollBar.setMaximum( scrollMax );
		}
	}

	@Override
	/**************************************************************************
	 * Callback from LineStyleListener interface for the styled text widget.
	 * Used to set the text styles (bold, italic, etc)
	 *************************************************************************/
	public void lineGetStyle(LineStyleEvent event) 
	{
		// This method basically establishes the glyph metrics required for
		// creating the leading blank space at the beginning of each line,
		// which will allow us to paint the icons later. Besides, the
		// corresponding line model is stored in the style range in order
		// to be utilized later on in paintObject().
		
		// We need to create a style rang for each line
		StyleRange theStyle = new StyleRange(event.lineOffset, 
				                             event.lineText.length(),
				                             m_view.getForeground(), null);
		// Reuse the same glyphmetrics, it never changes and we save memory and creation time
	    theStyle.metrics = s_metrics;
	    // Store the line model that will be used later for painting

	    // NOT COMPATIBLE WITH ECLIPSE 3.3 --BEGIN
		// This is the corresponding line index
		// int lineIndex = m_model.getLineAtOffset(event.lineOffset);
		// theStyle.data = (TextViewLine) m_model.getLineObject(lineIndex);
	    // NOT COMPATIBLE WITH ECLIPSE 3.3 --END
	    
	    event.styles = new StyleRange[1];
		event.styles[0] = theStyle;
	}

	@Override
	/**************************************************************************
	 * Callback from PaintObjectListener interface for the styled text widget.
	 * Used to set images.
	 *************************************************************************/
	public void paintObject(PaintObjectEvent event) 
	{
		if (m_view.isVisible())
		{
			// Get the line model
		    // NOT COMPATIBLE WITH ECLIPSE 3.3 --BEGIN
			//TextViewLine line = (TextViewLine) event.style.data;
		    // NOT COMPATIBLE WITH ECLIPSE 3.3 --END

			// Solution for backwards compatibility with ECLIPSE 3.3
			int lineIndex = m_view.getLineAtOffset(event.style.start);
			TextViewLine line = (TextViewLine) m_model.getLineObject(lineIndex); 
	
			// Set background color
			Color bkg = m_view.getBackground();
			if (line.getColor()!=null)
			{
				bkg = line.getColor();
			}
			int width = m_view.getClientArea().width;
			int height = m_view.getLineHeight() + m_view.getLineSpacing();
			event.gc.setBackground( bkg );
			event.gc.fillRectangle(event.x, event.y, width, height);
			
			// If there is an icon defined, draw it.
		    Image icon = TextParagraph.getIcon( line.getIconId() );
			if (icon != null)
			{
				event.gc.drawImage(icon, 0, 0, TextParagraph.ICON_SIZE, TextParagraph.ICON_SIZE, event.x, event.y, height, height);
			}
			// Now we draw the text.
			TextLayout layout = new TextLayout(event.display);
			layout.setAscent(event.ascent);
			layout.setDescent(event.descent);
			// Determine the style, depending on it we use bold or normal font.
			if (( line.getStyle() & SWT.BOLD )>0)
			{
				layout.setFont( s_boldFont );
			}
			else
			{
				layout.setFont( s_normalFont );
			}
			// Assign the text to draw
			layout.setText( line.getText().replace("\t", "    ") );
			// And draw it.
			layout.draw(event.gc, event.x + TextParagraph.ICON_SIZE + 3, event.y);
			layout.dispose();
		}
	} 
	
	/**************************************************************************
	 * Recalculate parameters after widget or font size changes
	 *************************************************************************/
	private void recalculateAfterSizeChange()
	{
		// When the control is resized we need to calculate the amount
		// of visible lines in the widget. That determines the size
		// of the data window (used for the internal buffer)
		int height = m_view.getClientArea().height;
		int lineHeight = m_view.getLineSpacing() + m_view.getLineHeight();
		// Establish the size of the buffer
		m_viewWindowLength = (height / lineHeight);
		// Reassign the values of the data window in the model
		m_model.setViewWindow(m_viewWindowStart,m_viewWindowLength,m_autoScroll);
		// Update the scrollbar position
		// Position the scrollbar
		m_scrollBar.setMaximum(m_totalDataLength - m_viewWindowLength);
	}

}
