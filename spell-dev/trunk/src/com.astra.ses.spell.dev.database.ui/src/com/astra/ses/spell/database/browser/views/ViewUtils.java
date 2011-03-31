///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.database.browser
// 
// FILE      : ViewUtils.java
//
// DATE      : 2009-09-14
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
// SUBPROJECT: SPELL Development Environment
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.database.browser.views;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Label;


/******************************************************************************
 * This class offers some facilities for showing data inside the views
 *****************************************************************************/
public class ViewUtils {

	/**************************************************************************
	 * Adjust the text to show according to the avialable width
	 * @param label
	 * @param textToShow
	 *************************************************************************/
	public static void adjustLabelContents(Label label, String textToShow)
	{
		GC graphicsContext = new GC(label);
		int neededWidth = graphicsContext.textExtent(textToShow).x;
		int availableWidth = label.getBounds().width;
		String content = textToShow;
		if (neededWidth > availableWidth)
		{
			content = ViewUtils.getSimplifiedString(textToShow, neededWidth, availableWidth);
		}
		label.setToolTipText(textToShow);
		label.setText(content);
	}
	
	/**************************************************************************
	 * Returned a simplified representation of a path
	 * @param content
	 * @return
	 *************************************************************************/
	private static String getSimplifiedString(String content, int neededWidth, int availableWidth)
	{
		String middleString = "...";
		
		int charCount = content.length();
		double proportion = (float) availableWidth / (float) neededWidth;
		int charsToShow = (int) (charCount * proportion);
		
		int originalCharsToShow = charsToShow - middleString.length();
		if (originalCharsToShow <= 0)
		{
			return middleString;
		}
		int prefixSufix = (originalCharsToShow/2);
		String result = content.substring(0,prefixSufix) + middleString + content.substring(charCount - 1 - prefixSufix);
		return result;		
	}
	
}
