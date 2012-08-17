///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.controls
// 
// FILE      : CustomStyledText.java
//
// DATE      : 2008-11-21 13:54
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
package com.astra.ses.spell.gui.presentation.text.controls;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.FontKey;
import com.astra.ses.spell.gui.presentation.text.model.TextParagraph;
import com.astra.ses.spell.gui.presentation.text.model.TextViewContent;
import com.astra.ses.spell.gui.presentation.text.model.TextViewLine;

public class CustomStyledText extends Composite implements ControlListener,
        SelectionListener, LineStyleListener, LineBackgroundListener,
        PaintObjectListener, IPropertyChangeListener
{

	private static GlyphMetrics	  s_metrics	 = new GlyphMetrics(0, 0, 16);

	/** Font range */
	private static final int	  FONT_RANGE	= 4;

	/** Holds the styled text contents model */
	private TextViewContent	      m_model;
	/** Holds the styled text widget */
	private StyledText	          m_view;
	/** Holds the composite vertical scrollbar */
	private ScrollBar	          m_scrollBar;
	/** True if the widget shall follow the latest data */
	private boolean	              m_autoScroll;
	/** Maximum and minimum font sizes */
	private int	                  m_maxfontSize;
	private int	                  m_minFontSize;
	/** Label Provider */
	private TextLineLabelProvider	m_labelProvider;

	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	public CustomStyledText(Composite parent)
	{
		super(parent, SWT.BORDER | SWT.V_SCROLL);
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
		m_view = new StyledText(this, SWT.NONE | SWT.FULL_SELECTION);
		m_view.setContent(m_model);
		m_view.setEditable(false);
		m_view.setLineSpacing(0);
		m_view.setLayoutData(new GridData(GridData.FILL_BOTH
		        | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

		// Label provider
		m_labelProvider = new TextLineLabelProvider(m_view);

		// Get the vertical scroll var
		m_scrollBar = getVerticalBar();

		// Set the initial auto scroll value
		m_autoScroll = true;

		// Setup listeners
		// For the control-resize event
		m_view.addControlListener(this);
		// For the styles
		m_view.addLineStyleListener(this);
		// Line background listener
		m_view.addLineBackgroundListener(this);
		// Image drawing
		m_view.addPaintObjectListener(this);
		// For the scroll drag and click events
		m_scrollBar.addSelectionListener(this);
		// Scrollbar initialization
		m_scrollBar.setVisible(true);
		m_scrollBar.setIncrement(1);
		m_scrollBar.setPageIncrement(10);

		// Initial position of the data view
		m_model.setViewWindow(0, 50, m_autoScroll);

		// Setup initial font
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		Font font = cfg.getFont(FontKey.CODE);
		int fontSize = font.getFontData()[0].getHeight();
		m_minFontSize = Math.max(1, fontSize - FONT_RANGE);
		m_maxfontSize = fontSize + FONT_RANGE;
		setFont(font);

		cfg.addPropertyChangeListener(this);
		parent.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				unsubuscribeFromPreferences();
			}
		});

		layout();
	}

	/**************************************************************************
	 * Append a line to the widget
	 *************************************************************************/
	public void append(TextParagraph p)
	{
		String[] lines = p.getText();
		boolean firstLine = true;
		for (String line : lines)
		{
			if (firstLine)
			{
				firstLine = false;
				m_model.append(new TextViewLine(new String(line), p.getScope(),
				        p.getType(), p.getSequence()));
			}
			else
			{
				m_model.append(new TextViewLine(new String(line), p.getScope(),
				        p.getType(), p.getSequence()));
			}
		}
		int totalDataLength = m_model.getTotalDataSize();
		int thumb = m_scrollBar.getThumb();
		int max = Math.max(0, totalDataLength - thumb);
		m_scrollBar.setMaximum(max + thumb);
		if (m_autoScroll)
		{
			int selection = (totalDataLength - thumb);
			m_scrollBar.setSelection(selection);
		}
	}

	/**************************************************************************
	 * Set the autoscroll value
	 *************************************************************************/
	public void setAutoScroll(boolean auto)
	{
		m_autoScroll = auto;
		int scrollThumb = m_scrollBar.getThumb();
		int totalDataLength = m_model.getTotalDataSize();
		int start = Math.max(0, totalDataLength - scrollThumb);
		m_model.setViewWindow(start, scrollThumb, m_autoScroll);
	}

	/**************************************************************************
	 * Clear the widget
	 *************************************************************************/
	public void clear()
	{
		m_model.clear();
		m_scrollBar.setSelection(0);
		m_model.setViewWindow(0, m_scrollBar.getThumb(), m_autoScroll);
	}

	/**************************************************************************
	 * Set the widget text
	 *************************************************************************/
	public void setText(String text)
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
	public void zoom(boolean increase)
	{
		// Get the font size
		int fontSize = m_view.getFont().getFontData()[0].getHeight();

		if (increase && (fontSize == m_maxfontSize)) return;
		if (!increase && (fontSize == m_minFontSize)) return;
		if (increase)
		{
			fontSize = Math.min(fontSize + 1, m_maxfontSize);
		}
		else
		{
			fontSize = Math.max(fontSize - 1, m_minFontSize);
		}
		FontData[] fdata = m_view.getFont().getFontData();
		for (int index = 0; index < fdata.length; index++)
		{
			fdata[index].setHeight(fontSize);
		}
		Font newFont = new Font(Display.getCurrent(), fdata);
		setFont(newFont);
	}

	/**************************************************************************
	 * Override the listener mechanism
	 *************************************************************************/
	public void addMouseMoveListener(MouseMoveListener lst)
	{
		m_view.addMouseMoveListener(lst);
	}

	/**************************************************************************
	 * Override the listener mechanism
	 *************************************************************************/
	public void addMouseListener(MouseListener lst)
	{
		m_view.addMouseListener(lst);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		m_model.clear();
		m_labelProvider.dispose();
	}

	/**************************************************************************
	 * Set the font
	 *************************************************************************/
	public void setFont(Font font)
	{
		// Assign the font to the view
		m_view.setFont(font);
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
	public void setBackground(Color c)
	{
		m_view.setBackground(c);
	}

	@Override
	/**************************************************************************
	 * Callback from ControlListener interface
	 *************************************************************************/
	public void controlMoved(ControlEvent e)
	{
	}

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
	}

	@Override
	/**************************************************************************
	 * Callback from SelectionListener interface of the scrollbar
	 *************************************************************************/
	public void widgetSelected(SelectionEvent e)
	{
		// Now we are moving the view position with the scroll value
		int scrollValue = m_scrollBar.getSelection();
		int scrollThumb = m_scrollBar.getThumb();
		m_model.setViewWindow(scrollValue, scrollThumb, m_autoScroll);
	}

	@Override
	/**************************************************************************
	 * Callback from LineStyleListener interface for the styled text widget.
	 * Used to set the text styles (bold, italic, etc)
	 *************************************************************************/
	public void lineGetStyle(LineStyleEvent event)
	{
		/*
		 * Icon style range
		 */
		// This method basically establishes the glyph metrics required for
		// creating the leading blank space at the beginning of each line,
		// which will allow us to paint the icons later. Besides, the
		// corresponding line model is stored in the style range in order
		// to be utilized later on in paintObject().
		int lineIndex = m_view.getLineAtOffset(event.lineOffset);
		TextViewLine line = (TextViewLine) m_model.getLineObject(lineIndex);
		// We need to create a style rang for each line
		StyleRange bulletStyle = new StyleRange();
		// Reuse the same glyphmetrics, it never changes and we save memory and
		// creation time
		bulletStyle.metrics = s_metrics;
		bulletStyle.start = event.lineOffset;
		// Store the line model that will be used later for painting

		// NOT COMPATIBLE WITH ECLIPSE 3.3 --BEGIN
		// This is the corresponding line index
		// int lineIndex = m_model.getLineAtOffset(event.lineOffset);
		// theStyle.data = (TextViewLine) m_model.getLineObject(lineIndex);
		// NOT COMPATIBLE WITH ECLIPSE 3.3 --END
		event.bullet = new Bullet(ST.BULLET_CUSTOM, bulletStyle);

		TextStyle lineStyle = new TextStyle(null, null, null);
		lineStyle.foreground = m_labelProvider.getForegroundColor(
		        line.getType(), line.getScope());
		StyleRange lineStyleRange = new StyleRange(lineStyle);
		lineStyleRange.start = event.lineOffset;
		lineStyleRange.length = event.lineText.length();
		lineStyleRange.fontStyle = m_labelProvider
		        .getFontStyle(line.getScope());
		event.styles = new StyleRange[] { lineStyleRange };
	}

	@Override
	public void lineGetBackground(LineBackgroundEvent event)
	{
		int lineIndex = m_view.getLineAtOffset(event.lineOffset);
		TextViewLine line = (TextViewLine) m_model.getLineObject(lineIndex);
		// Set background color
		Color lineBg = m_labelProvider.getBackgroundColor(line.getType());
		event.lineBackground = lineBg;
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
			// TextViewLine line = (TextViewLine) event.style.data;
			// NOT COMPATIBLE WITH ECLIPSE 3.3 --END

			// Solution for backwards compatibility with ECLIPSE 3.3
			int lineIndex = m_view.getLineAtOffset(event.style.start);
			TextViewLine line = (TextViewLine) m_model.getLineObject(lineIndex);
			int height = m_view.getLineHeight() + m_view.getLineSpacing();

			// If there is an icon defined, draw it.

			Image icon = m_labelProvider.getImage(line.getType());
			if (icon != null)
			{
				event.gc.drawImage(icon, 0, 0, TextParagraph.ICON_SIZE,
				        TextParagraph.ICON_SIZE, event.x, event.y, height,
				        height);
			}
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
		int viewWindowLength = (height / lineHeight);
		// Scrollbar size attends to the number of liens that can be shown
		int max = Math.max(0, m_model.getTotalDataSize() - viewWindowLength);
		m_scrollBar.setMaximum(max + viewWindowLength);
		m_scrollBar.setThumb(viewWindowLength);
		if (m_autoScroll)
		{
			m_scrollBar.setSelection(max);
		}

		// Reassign the values of the data window in the model
		m_model.setViewWindow(m_scrollBar.getSelection(), viewWindowLength,
		        m_autoScroll);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.equals(FontKey.CODE.getPreferenceName()))
		{
			ConfigurationManager cfg = (ConfigurationManager) ServiceManager
			        .get(ConfigurationManager.ID);

			Font newFont = cfg.getFont(FontKey.CODE);

			Font oldFont = m_view.getFont();
			int height = oldFont.getFontData()[0].getHeight();
			String name = newFont.getFontData()[0].getName();
			int style = newFont.getFontData()[0].getStyle();

			FontData newFontData = new FontData(name, height, style);
			newFont = new Font(Display.getDefault(), newFontData);

			setFont(newFont);
		}
	}

	/***************************************************************************
	 * Stop listening from preferences changes
	 **************************************************************************/
	private void unsubuscribeFromPreferences()
	{
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		cfg.removePropertyChangeListener(this);
		m_labelProvider.unsubuscribeFromPreferences();
	}
}
