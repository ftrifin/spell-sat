////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.unittest.printer
// 
// FILE      : TestResultPrinter.java
//
// DATE      : Nov 16, 2010 2:41:48 PM
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
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.unittest.printer;

import java.io.PrintStream;
import java.util.Enumeration;

import junit.framework.TestFailure;
import junit.textui.ResultPrinter;

/*******************************************************************************
 * 
 * {@link TestResultPrinter} prints tests results in a customized way
 * @author jpizar
 *
 ******************************************************************************/
public class TestResultPrinter extends ResultPrinter {

	/***************************************************************************
	 * Constructor
	 * @param writer
	 **************************************************************************/
	public TestResultPrinter(PrintStream writer) 
	{
		super(writer);
	}
	
	/*==========================================================================
	 * (non-Javadoc)
	 * @see ResultPrinter#printDefects(java.util.Enumeration, int, java.lang.String)
	 =========================================================================*/
	@Override
	protected void printDefects(Enumeration<TestFailure> failures, int count, String type)
	{
		if (count == 0) return;
		for (int i=1; failures.hasMoreElements(); i++)
		{
			if (type.equals("failure"))
			{	getWriter().print("[FAIL] ");
			}
			else
			{
				getWriter().print("[ERROR] ");
				
			}
			getWriter().print(i + ".- ");
			printTestTrace(failures.nextElement());
			getWriter().println();
		}
	}
	
	/***************************************************************************
	 * Print the trace for this defective test
	 * @param test
	 **************************************************************************/
	private void printTestTrace(TestFailure failure)
	{
		String trace;
		
		/*
		 * Show the class where the failure was produced,
		 * the line and the message
		 */
		StackTraceElement first = 
			findstackTraceElement(failure.thrownException().getStackTrace());
		int line = first.getLineNumber();
		String className = failure.failedTest().getClass().getSimpleName();
		String message = failure.exceptionMessage();
		trace = "class " + className + ", line " + line + " : " + message;
		
		// Print the trace
		getWriter().print(trace);
	}
	
	/***************************************************************************
	 * Fint the stack trace element which raised the exception
	 * @param elements the stack trace elements
	 * @return the first {@link StackTraceElement} which does not belong
	 * to junit universe classes
	 **************************************************************************/
	private StackTraceElement findstackTraceElement(StackTraceElement[] elements)
	{		
		for (StackTraceElement element : elements)
		{
			if (isValidTrace(element))
			{
				return element;
			}
		}
		return null;
	}
	
	/***************************************************************************
	 * Check if this trace element belong to JUnit or Java native classes
	 * @param element
	 * @return
	 **************************************************************************/
	private boolean isValidTrace(StackTraceElement element)
	{
		String[] patterns = new String[]
		                               {
				"junit.framework.TestCase",
				"junit.framework.TestResult",
				"junit.framework.TestSuite",
				"junit.framework.Assert",
				"junit.swingui.TestRunner",
				"junit.awtui.TestRunner",
				"junit.textui.TestRunner",
				"java.lang.reflect.Method.invoke("
		                               };
		
		String strTrace = element.toString();
		
		for (String pattern : patterns)
		{
			if (strTrace.contains(pattern))
			{
				return false;
			}
		}
		return true;
	}
}
