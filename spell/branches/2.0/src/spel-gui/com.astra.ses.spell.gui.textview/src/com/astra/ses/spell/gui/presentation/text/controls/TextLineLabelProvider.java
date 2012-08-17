///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.text.controls
// 
// FILE      : TextLineLabelProvider.java
//
// DATE      : 2010-10-20
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

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.astra.ses.spell.gui.core.model.services.ServiceManager;
import com.astra.ses.spell.gui.core.model.types.ItemStatus;
import com.astra.ses.spell.gui.core.model.types.Scope;
import com.astra.ses.spell.gui.preferences.ConfigurationManager;
import com.astra.ses.spell.gui.preferences.keys.PreferenceCategory;
import com.astra.ses.spell.gui.presentation.text.Activator;
import com.astra.ses.spell.gui.presentation.text.model.ParagraphType;

/*******************************************************************************
 * 
 * {@link TextLineLabelProvider} will determine how different TextLine objects
 * will be rendered
 * 
 ******************************************************************************/
public class TextLineLabelProvider implements IPropertyChangeListener
{

	private StyledText	              m_text;
	/** Holds the style icons */
	private Map<ParagraphType, Image>	m_icons;
	/** Reference to background colors */
	private Map<ParagraphType, Color>	m_backgroundColors;
	/** Reference to foreground colors */
	private Map<Scope, Color>	      m_foregroundColors;
	/** Reference to styles */
	private Map<Scope, Integer>	      m_scopeStyles;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public TextLineLabelProvider(StyledText widget)
	{
		m_text = widget;

		loadPreferences();

		/*
		 * Subscribe to property changes
		 */
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		cfg.addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (property.startsWith(PreferenceCategory.STYLES.tag))
		{
			loadPreferences();
			m_text.redraw();
		}
	}

	/***************************************************************************
	 * Get background color for the given paragraph
	 * 
	 * @param type
	 * @return
	 **************************************************************************/
	public Color getBackgroundColor(ParagraphType type)
	{
		return m_backgroundColors.get(type);
	}

	/***************************************************************************
	 * Get the image to use for the given paragraph
	 * 
	 * @param paragraph
	 * @return
	 **************************************************************************/
	public Image getImage(ParagraphType paragraph)
	{
		return m_icons.get(paragraph);
	}

	/***************************************************************************
	 * Get forground color for the given scope
	 * 
	 * @param scope
	 * @return
	 **************************************************************************/
	public Color getForegroundColor(ParagraphType type, Scope scope)
	{
		if (type.equals(ParagraphType.WARNING)
		        || type.equals(ParagraphType.ERROR)) { return Display
		        .getCurrent().getSystemColor(SWT.COLOR_BLACK); }
		return m_foregroundColors.get(scope);
	}

	/***************************************************************************
	 * Get font style to use for the given scope
	 * 
	 * @param scope
	 * @return
	 **************************************************************************/
	public int getFontStyle(Scope scope)
	{
		return m_scopeStyles.get(scope);
	}

	/***************************************************************************
	 * Dispose the graphic objects to release resources
	 **************************************************************************/
	public void dispose()
	{
		for (Image img : m_icons.values())
		{
			img.dispose();
		}
		m_icons = null;

		for (Color bg : m_backgroundColors.values())
		{
			bg.dispose();
		}
		m_backgroundColors = null;

		for (Color fg : m_foregroundColors.values())
		{
			fg.dispose();
		}
		m_foregroundColors = null;

		m_scopeStyles = null;

		m_text = null;
	}

	/***************************************************************************
	 * Load the look and feel attributes from preferences
	 **************************************************************************/
	private void loadPreferences()
	{
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);

		m_foregroundColors = new TreeMap<Scope, Color>();
		m_scopeStyles = new TreeMap<Scope, Integer>();

		m_backgroundColors = new TreeMap<ParagraphType, Color>();
		m_icons = new TreeMap<ParagraphType, Image>();

		for (Scope scope : Scope.values())
		{
			Color color = cfg.getScopeColor(scope);
			m_foregroundColors.put(scope, color);

			int style = cfg.getScopeStyle(scope);
			m_scopeStyles.put(scope, style);
		}

		m_icons.put(ParagraphType.NORMAL,
		        Activator.getImageDescriptor("icons/16x16/img_disp.png")
		                .createImage());
		m_icons.put(ParagraphType.WARNING,
		        Activator.getImageDescriptor("icons/16x16/img_warning.png")
		                .createImage());
		m_icons.put(ParagraphType.ERROR,
		        Activator.getImageDescriptor("icons/16x16/img_error.png")
		                .createImage());
		m_icons.put(ParagraphType.PROMPT,
		        Activator.getImageDescriptor("icons/16x16/img_prompt.png")
		                .createImage());
		m_icons.put(ParagraphType.SPELL,
		        Activator.getImageDescriptor("icons/16x16/img_spell.png")
		                .createImage());

		m_backgroundColors.put(ParagraphType.WARNING,
		        cfg.getStatusColor(ItemStatus.WARNING));
		m_backgroundColors.put(ParagraphType.ERROR,
		        cfg.getStatusColor(ItemStatus.ERROR));
	}

	/***************************************************************************
	 * Unsubscribe from property changes, as the parent widget is being disposed
	 **************************************************************************/
	void unsubuscribeFromPreferences()
	{
		ConfigurationManager cfg = (ConfigurationManager) ServiceManager
		        .get(ConfigurationManager.ID);
		cfg.removePropertyChangeListener(this);
	}
}
