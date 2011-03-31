///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.print.dialog
// 
// FILE      : PrintDialog.java
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.print.dialog;

import java.io.File;

import javax.print.PrintService;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.astra.ses.spell.gui.print.Activator;

/******************************************************************************
 * PrintDialog class shows printing options
 * @author jpizar
 *
 *****************************************************************************/
public class PrintDialog extends TitleAreaDialog {
	
	/**************************************************************************
	 * PageSize to be chosen by the user
	 * @author jpizar
	 *************************************************************************/
	private enum PageSize{
		ISO_A3("ISO A3", MediaSizeName.ISO_A3),
		ISO_A4("ISO A4", MediaSizeName.ISO_A4),
		LETTER_SIZE("Letter size", MediaSizeName.NA_LETTER),
		ISO_A5("ISO A5", MediaSizeName.ISO_A5);
		
		/** Margin that can't be printed due to hardware reasons */
		private static final int INNER_MARGIN_MM = 7;
		
		/** MediaSizeName */
		private MediaSizeName m_mediaSizeName;
		/** Size name */
		private String m_name;
		
		/***********************************************************************
		 * Private constructor
		 **********************************************************************/
		private PageSize(String sizeName, MediaSizeName name)
		{
			m_mediaSizeName = name;
			m_name = sizeName;
		}
		
		public String getName()
		{
			return m_name;
		}
		
		/***********************************************************************
		 * Determine the page format of the printing job depending on the size 
		 * and the page layout
		 * @param landscape
		 **********************************************************************/
		public void fillAttributes(AttributeSet aset)
		{
			/* Media Size Name */
			aset.add(m_mediaSizeName);
			/* Page Dimensions */
			MediaSize size = MediaSize.getMediaSizeForName(m_mediaSizeName);
			float[] dimensions = size.getSize(MediaPrintableArea.MM);
			MediaPrintableArea area = new MediaPrintableArea(
					0 + INNER_MARGIN_MM,
					0 + INNER_MARGIN_MM,
					dimensions[0] -2*INNER_MARGIN_MM,
					dimensions[1] -2*INNER_MARGIN_MM,
					MediaPrintableArea.MM);
			aset.add(area);
		}
	}

	/** Available printing services */
	private PrintService[] m_services;
	/** Selected print service */
	private PrintService m_service;
	/** Attribute set to fill */
	private AttributeSet m_printSet;
	
	/** Widgets */
	private Combo m_printersCombo;
	private Combo m_formatCombo;
	private Button m_portraitButton;
	private Button m_colourButton;
	private Text m_filePathText;
	
	/***************************************************************************
	 * Constructor
	 * @param parentShell
	 **************************************************************************/
	public PrintDialog(Shell shell, PrintService[] services, AttributeSet set) {
		super(shell);
		m_services = services;
		m_printSet = set;
	}
	
	/***************************************************************************
	 * Return print service selected by the user
	 * @return
	 **************************************************************************/
	public PrintService getPrintService()
	{
		return m_service;
	}
	
	@Override
	protected Control createContents(Composite parent)
	{
		Control res = super.createContents(parent);
		setTitle("Print");
		setMessage("Set printing options");
		return res;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		GridLayout layout = new GridLayout(1,true);
		parent.setLayout(layout);
		/*
		 * Printer group
		 */
		Group printerGroup = new Group(parent,SWT.BORDER);
		printerGroup.setText("Printer selection");
		GridLayout pLayout = new GridLayout(2, false);
		GridData pData = new GridData(GridData.FILL_HORIZONTAL);
		printerGroup.setLayout(pLayout);
		printerGroup.setLayoutData(pData);
		
		/* PRINTER SELECTION */
		Label printerSelection = new Label(printerGroup, SWT.NONE);
		printerSelection.setText("Printer");
		
		m_printersCombo = new Combo(printerGroup, SWT.READ_ONLY);
		GridData printersData = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		m_printersCombo.setLayoutData(printersData);
		for (PrintService service : m_services)
		{
			String name = service.getName();
			m_printersCombo.add(name);
			m_printersCombo.setData(name, service);
		}
		if (m_services.length > 0)
		{
			m_printersCombo.select(0);
		}
		
		boolean fileEnabled = false;
		Button printToFile = new Button(printerGroup, SWT.CHECK);
		printToFile.setText("Print to file");
		printToFile.setSelection(fileEnabled);
		
		m_filePathText = new Text(printerGroup, SWT.BORDER);
		GridData pathData = new GridData(GridData.FILL_HORIZONTAL);
		m_filePathText.setLayoutData(pathData);
		m_filePathText.setEnabled(fileEnabled);
		
		printToFile.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button fileButton = (Button) e.widget;
				boolean textEnabled = fileButton.getSelection();
				m_filePathText.setEnabled(textEnabled);
				m_printersCombo.setEnabled(!textEnabled);
			}
		});
		
		/*
		 * PageFormat selection
		 */
		Group formatGroup = new Group(parent,SWT.BORDER);
		formatGroup.setText("Page format");
		GridLayout fLayout = new GridLayout(2, false);
		GridData fData = new GridData(GridData.FILL_HORIZONTAL);
		formatGroup.setLayout(fLayout);
		formatGroup.setLayoutData(fData);
		
		/* PAGE SIZE */
		Label sizeSelection = new Label(formatGroup, SWT.NONE);
		sizeSelection.setText("Size");
		
		m_formatCombo = new Combo(formatGroup, SWT.READ_ONLY);
		GridData sizeData = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		m_formatCombo.setLayoutData(sizeData);
		for (PageSize pageSize : PageSize.values())
		{
			String name = pageSize.getName();
			m_formatCombo.add(name);
			m_formatCombo.setData(name, pageSize);
		}
		// A4 size selected by default
		m_formatCombo.select(1);
		
		/* PAGE LAYOUT */
		Label layoutLabel = new Label(formatGroup, SWT.NONE);
		layoutLabel.setText("Layout");
		
		// Buttons are inside a composite
		Composite buttonComposite = new Composite(formatGroup, SWT.NONE);
		GridLayout buttonsLayout = new GridLayout(2, true);
		buttonComposite.setLayout(buttonsLayout);
		
		m_portraitButton = new Button(buttonComposite, SWT.RADIO);
		m_portraitButton.setText("Portrait");
		m_portraitButton.setSelection(true);
		m_portraitButton.setImage(Activator.getImageDescriptor("images/portrait.png").createImage());
		Button landscapeButton = new Button(buttonComposite, SWT.RADIO);
		landscapeButton.setText("Landscape");
		landscapeButton.setImage(Activator.getImageDescriptor("images/landscape.png").createImage());
		
		/* PRINTING OPTIONS */
		Group printingOptions = new Group(parent, SWT.BORDER);
		printingOptions.setText("Printing options");
		GridLayout optionsLayout = new GridLayout(3, false);
		GridData optionsData = new GridData(GridData.FILL_HORIZONTAL);
		printingOptions.setLayout(optionsLayout);
		printingOptions.setLayoutData(optionsData);
		
		//COLOR/GRAYSCALE
		Label colourMode = new Label(printingOptions, SWT.NONE);
		colourMode.setText("Colour mode");
		m_colourButton = new Button(printingOptions, SWT.RADIO);
		m_colourButton.setText("Colour");
		m_colourButton.setSelection(true);
		m_colourButton.setImage(Activator.getImageDescriptor("images/colour.png").createImage());
		Button grayscaleButton = new Button(printingOptions, SWT.RADIO);
		grayscaleButton.setText("Grayscale");
		grayscaleButton.setImage(Activator.getImageDescriptor("images/bw.png").createImage());
		
		
		return parent;
	}
	
	@Override
	protected void okPressed()
	{
		/* Retrieve the print service to use */
		int selection = m_printersCombo.getSelectionIndex();
		String printer = m_printersCombo.getItem(selection);
		m_service = (PrintService) m_printersCombo.getData(printer);
		/* Fill attribute set */
		selection = m_formatCombo.getSelectionIndex();
		String sizeName = m_formatCombo.getItem(selection);
		PageSize size = (PageSize) m_formatCombo.getData(sizeName);
		size.fillAttributes(m_printSet);
		
		/* Page layout */
		if (m_portraitButton.getSelection())
		{
			m_printSet.add(OrientationRequested.PORTRAIT);
		}
		else
		{
			m_printSet.add(OrientationRequested.LANDSCAPE);
		}
		
		/* Colour / Grayscale */
		if (m_colourButton.getSelection())
		{
			m_printSet.add(Chromaticity.COLOR);
		}
		else
		{
			m_printSet.add(Chromaticity.MONOCHROME);
		}
		
		/* Print to file */
		if (m_filePathText.isEnabled())
		{
			String path = m_filePathText.getText();
			File target = new File(path);
			// Directory
			File parent = target.getParentFile();
			if ((parent != null) && (parent.exists()) && (parent.canWrite()))
			{
				m_printSet.add(new Destination(target.toURI()));
			}
		}
		
		/* Finish */
		super.okPressed();
	}

}
