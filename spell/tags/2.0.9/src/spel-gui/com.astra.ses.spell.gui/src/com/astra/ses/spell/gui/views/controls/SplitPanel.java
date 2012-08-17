///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.views.controls
// 
// FILE      : SplitPanel.java
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
package com.astra.ses.spell.gui.views.controls;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SplitPanel extends Composite implements Listener, ControlListener
{
	public static enum Section
	{
		FIRST, SECOND, BOTH, NONE
	};

	/** Scrolled panel for the first section */
	private ScrolledComposite	m_scroll_section1;
	/** Scrolled panel for the second section */
	private ScrolledComposite	m_scroll_section2;
	/** First section composite */
	private Composite	      m_section1;
	/** Second section composite */
	private Composite	      m_section2;
	/** Divider */
	private Sash	          m_sash;
	/** Layout data for the divider */
	private FormData	      m_sashData;
	/** True if the division is horizontal */
	private boolean	          m_horizontalSplit;
	/** Minimum size for sections */
	private int	              m_sizeLimit;
	/** Initial division ratio */
	private int	              m_initialDivision;
	/** Holds the fixed section */
	private Section	          m_fixedSection;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 *            Container composite
	 * @param horizontalSplit
	 *            True if the split bar is to be horizontal
	 * @param limit
	 *            Size limit for sections
	 * @param initial
	 *            Initial percentage of the split bar position
	 **************************************************************************/
	public SplitPanel(Composite parent, boolean horizontalSplit, int limit,
	        int initial, Section fixedSection)
	{
		super(parent, SWT.NONE);

		m_initialDivision = initial;
		m_sizeLimit = limit;
		m_horizontalSplit = horizontalSplit;
		m_fixedSection = fixedSection;

		createSections();
		defineLayout();

		if (m_fixedSection != Section.NONE)
		{
			addControlListener(this);
		}
	}

	/***************************************************************************
	 * Create the main parts of the control
	 **************************************************************************/
	private void createSections()
	{
		m_scroll_section1 = new ScrolledComposite(this, SWT.NONE | SWT.H_SCROLL
		        | SWT.V_SCROLL);
		GridLayout scroll1_layout = new GridLayout();
		scroll1_layout.numColumns = 1;
		m_scroll_section1.setLayout(scroll1_layout);

		m_section1 = new Composite(m_scroll_section1, SWT.NONE);
		m_section1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		m_scroll_section1.setContent(m_section1);
		m_scroll_section1.setExpandHorizontal(true);
		m_scroll_section1.setExpandVertical(true);

		m_sash = new Sash(this, m_horizontalSplit ? SWT.HORIZONTAL
		        : SWT.VERTICAL);

		m_scroll_section2 = new ScrolledComposite(this, SWT.NONE | SWT.H_SCROLL
		        | SWT.V_SCROLL);
		GridLayout scroll2_layout = new GridLayout();
		scroll2_layout.numColumns = 1;
		m_scroll_section2.setLayout(scroll2_layout);

		m_section2 = new Composite(m_scroll_section2, SWT.NONE);
		m_section2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		m_scroll_section2.setContent(m_section2);
		m_scroll_section2.setExpandHorizontal(true);
		m_scroll_section2.setExpandVertical(true);
	}

	/***************************************************************************
	 * Define the split layout
	 **************************************************************************/
	private void defineLayout()
	{
		setLayout(new FormLayout());

		if (m_horizontalSplit)
		{
			FormData section1_data = new FormData();
			section1_data.left = new FormAttachment(0, 0);
			section1_data.right = new FormAttachment(100, 0);
			section1_data.top = new FormAttachment(0, 0);
			section1_data.bottom = new FormAttachment(m_sash, 0);
			m_scroll_section1.setLayoutData(section1_data);

			FormData section2_data = new FormData();
			section2_data.left = new FormAttachment(0, 0);
			section2_data.right = new FormAttachment(100, 0);
			section2_data.top = new FormAttachment(m_sash, 0);
			section2_data.bottom = new FormAttachment(100, 0);
			m_scroll_section2.setLayoutData(section2_data);

			m_sashData = new FormData();
			m_sashData.top = new FormAttachment(m_initialDivision, 0);
			m_sashData.left = new FormAttachment(0, 0);
			m_sashData.right = new FormAttachment(100, 0);
			m_sash.setLayoutData(m_sashData);
		}
		else
		{
			FormData section1_data = new FormData();
			section1_data.left = new FormAttachment(0, 0);
			section1_data.right = new FormAttachment(m_sash, 0);
			section1_data.top = new FormAttachment(0, 0);
			section1_data.bottom = new FormAttachment(100, 0);
			m_scroll_section1.setLayoutData(section1_data);

			FormData section2_data = new FormData();
			section2_data.left = new FormAttachment(m_sash, 0);
			section2_data.right = new FormAttachment(100, 0);
			section2_data.top = new FormAttachment(0, 0);
			section2_data.bottom = new FormAttachment(100, 0);
			m_scroll_section2.setLayoutData(section2_data);

			m_sashData = new FormData();
			m_sashData.left = new FormAttachment(m_initialDivision, 0);
			m_sashData.top = new FormAttachment(0, 0);
			m_sashData.bottom = new FormAttachment(100, 0);
			m_sash.setLayoutData(m_sashData);
		}

		m_sash.addListener(SWT.Selection, this);
	}

	/***************************************************************************
	 * Handle sash events
	 **************************************************************************/
	public void handleEvent(Event e)
	{
		Rectangle sashRect = m_sash.getBounds();
		Rectangle shellRect = getClientArea();
		if (m_horizontalSplit)
		{
			int height = shellRect.height - sashRect.height - m_sizeLimit;
			e.y = Math.max(Math.min(e.y, height), m_sizeLimit);
			if (e.y != sashRect.y)
			{
				m_sashData.top = new FormAttachment(0, e.y);
				layout();
			}
		}
		else
		{
			int width = shellRect.width - sashRect.width - m_sizeLimit;
			e.x = Math.max(Math.min(e.x, width), m_sizeLimit);
			if (e.x != sashRect.x)
			{
				m_sashData.left = new FormAttachment(0, e.x);
				layout();
			}
		}
	}

	/***************************************************************************
	 * Handle resize events
	 **************************************************************************/
	@Override
	public void controlResized(ControlEvent e)
	{
		if (m_fixedSection == Section.FIRST)
		{
			if (m_horizontalSplit)
			{
				m_sashData.top = new FormAttachment(0, m_section1.getSize().y);
			}
			else
			{
				m_sashData.left = new FormAttachment(0, m_section1.getSize().x);
			}
			layout();
		}
		else if (m_fixedSection == Section.SECOND)
		{
			Rectangle panelRect = getClientArea();
			if (m_horizontalSplit)
			{
				int offset = panelRect.height - m_section2.getSize().y;
				m_sashData.top = new FormAttachment(0, offset);
			}
			else
			{
				int offset = panelRect.width - m_section2.getSize().x;
				m_sashData.left = new FormAttachment(0, offset);
			}
			layout();
		}
	}

	/***************************************************************************
	 * Handle move events
	 **************************************************************************/
	@Override
	public void controlMoved(ControlEvent e)
	{
	}

	/***************************************************************************
	 * Obtain the first section handle
	 * 
	 * @return The first section base composite
	 **************************************************************************/
	public Composite getSection(Section s)
	{
		if (s == Section.FIRST)
		{
			return m_section1;
		}
		else if (s == Section.SECOND)
		{
			return m_section2;
		}
		else
		{
			return null;
		}
	}

	/***************************************************************************
	 * Compute size and scroll for first section
	 **************************************************************************/
	public void computeSection(Section s)
	{
		if (s == Section.FIRST || s == Section.BOTH)
		{
			m_section1.pack();
			m_scroll_section1.setMinSize(m_section1.computeSize(SWT.DEFAULT,
			        SWT.DEFAULT));
		}
		if (s == Section.SECOND || s == Section.BOTH)
		{
			m_section2.pack();
			m_scroll_section2.setMinSize(m_section2.computeSize(SWT.DEFAULT,
			        SWT.DEFAULT));
		}
	}

	/***************************************************************************
	 * Manually set the division position, in percentage
	 **************************************************************************/
	public void setDivision(int offset)
	{
		if (m_horizontalSplit)
		{
			m_sashData.top = new FormAttachment(0, offset);
		}
		else
		{
			m_sashData.left = new FormAttachment(0, offset);
		}
		layout();
	}

	/***************************************************************************
	 * Dispose the control
	 **************************************************************************/
	public void dispose()
	{
		for (Control c : m_section1.getChildren())
			c.dispose();
		for (Control c : m_section2.getChildren())
			c.dispose();
		m_section1.dispose();
		m_section2.dispose();
		m_scroll_section1.dispose();
		m_scroll_section2.dispose();
		m_sash.dispose();
		super.dispose();
	}

	/***************************************************************************
	 * Test method
	 * 
	 * @param args
	 **************************************************************************/
	public static void main(String[] args)
	{
		Display display = new Display();
		final Shell shell = new Shell(display);

		final SplitPanel panel = new SplitPanel(shell, true, 20, 50,
		        Section.SECOND);

		// 1. THE CONTAINER
		GridLayout l = new GridLayout();
		l.numColumns = 1;
		shell.setLayout(l);
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));

		panel.getSection(Section.FIRST).setLayout(new GridLayout());
		panel.getSection(Section.SECOND).setLayout(new GridLayout());

		TableViewer viewer = new TableViewer(panel.getSection(Section.FIRST),
		        SWT.NONE);
		Table table = viewer.getTable();

		TableColumn c1 = new TableColumn(table, SWT.NONE);
		c1.setText("Idx");
		c1.setWidth(15);
		TableColumn c2 = new TableColumn(table, SWT.NONE);
		c2.setText("Description");
		c2.setWidth(120);

		for (int idx = 0; idx < 10; idx++)
		{
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, Integer.toString(idx));
			item.setText(1, "This is the item " + idx);
		}

		Button button1 = new Button(panel.getSection(Section.FIRST), SWT.PUSH);
		button1.setText("Button 1");
		button1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button1.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			};

			public void widgetSelected(SelectionEvent e)
			{
				if (panel.getSection(Section.SECOND).getChildren().length > 5)
				{
					Control[] cld = panel.getSection(Section.SECOND)
					        .getChildren();
					cld[cld.length - 1].dispose();
				}
				else
				{
					Button newButton = new Button(panel
					        .getSection(Section.SECOND), SWT.PUSH);
					newButton.setText("New button");
				}
				panel.computeSection(Section.SECOND);
			}
		});

		panel.computeSection(Section.FIRST);

		Button button2 = new Button(panel.getSection(Section.SECOND), SWT.PUSH);
		button2.setText("Button 2");
		button2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		panel.computeSection(Section.SECOND);

		shell.pack();
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}
