///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.extensions
// 
// FILE      : GuiBridge.java
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
package com.astra.ses.spell.gui.extensions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/*******************************************************************************
 * Invoke an arbitrary method with or without arguments, on a given object 
 * instance. Perform the call within the SWT display event loop if the caller
 * is not in the SWT thread.
 *  
 * @author Rafael Chinchilla Camara (GMV)
 *
 ******************************************************************************/
public class GuiBridge
{
	private static Map<String,Method>  s_methodBuffer = new TreeMap<String,Method>();
	
	/***************************************************************************
	 * To perform the method invocation within the display event loop, a runnable
	 * object shall be used
	 * 
	 * @author Rafael Chinchilla Camara
	 **************************************************************************/
	private static class GuiTask implements Runnable
	{
		Object theTarget;
		Object[] theArgs;
		String theMethod;
		Object theResult;
		
		/***********************************************************************
		 * Constructor
		 * @param target	Object instance to use
		 * @param method	Method to be invoked
		 * @param args		Arguments for the invocation
		 **********************************************************************/
		public GuiTask( Object target, String method, Object...args )
		{
			theTarget = target;
			theMethod = method;
			theArgs = args;
			theResult = null;
		}
		
		/***********************************************************************
		 * Execute the call
		 **********************************************************************/
		public void run()
		{
			theResult = doExecute(theTarget,theMethod,theArgs);
		}
	}
	
	/***************************************************************************
	 * Entry point of the bridge. Execute a call of the given method on the given
	 * object instance with the given arguments. If the current thread is not
	 * the one for display events, execute the call using syncExec.
	 * 
	 * @param target	Object to use for invocation
	 * @param method	Method name to be invoked
	 * @param args		Arguments for the call
	 * @return			Result of the invocation
	 **************************************************************************/
	public static Object execute( Object target, String method, Object... args )
	{
		if (Display.getCurrent()==null && PlatformUI.isWorkbenchRunning())
		{
			GuiTask task = new GuiTask(target,method,args);
			PlatformUI.getWorkbench().getDisplay().syncExec(task);
			return task.theResult;
		}
		else
		{
			return doExecute(target,method,args);
		}
	}

	/***************************************************************************
	 * Perform the actual method call
	 * @param target
	 * @param method
	 * @param args
	 * @return
	 **************************************************************************/
	private static Object doExecute( Object target, String method, Object... args )
	{
		//Logger.debug("Execute method '" + method + " on " + target, Level.COMM, "GuiBridge");
		//for(Object arg : args)
		//{
		//	Logger.debug("     - Argument: " + arg, Level.COMM, "GuiBridge");
		//}
		Object result = null;
		try
		{
			Method theMethod = obtainMethod(target,method,args);
			if (theMethod != null)
			{
				result = theMethod.invoke(target, args);
			}
		}
		catch(IllegalArgumentException ex)
		{
			System.err.println("Do execute " + method);
			System.err.println("Arguments exception: " + ex.getLocalizedMessage());
			System.err.println("Arguments passed: ");
			for(Object arg : args)
			{
				System.err.println("   - " + arg);
			}
			ex.printStackTrace();
		}
		catch(Exception ex)
		{
			System.err.println("Do execute " + method);
			System.err.println("Arguments passed: ");
			for(Object arg : args)
			{
				System.err.println("   - " + arg);
			}
			ex.printStackTrace();
		}
		return result;
	}
	
	/***************************************************************************
	 * Obtain the method instance using reflection.
	 * 
	 * @param target 	Object to be used
	 * @param method	Method name
	 * @param args		Method arguments
	 * @return			The method instance, or null
	 **************************************************************************/
	private static Method obtainMethod( Object target, String method, Object...args )
	{
		if (s_methodBuffer.containsKey(method))
		{
			return s_methodBuffer.get(method);
		}
		ArrayList<Class<?>> listArgs = new ArrayList<Class<?>>();
		if (args!=null)
		{
			for(Object obj : args)
			{
				if (obj != null) listArgs.add(obj.getClass());
			}
		}
		Class<?>[] nullList = new Class[0];
		Class<?>[] argClass = listArgs.toArray( nullList );
		Method theMethod = null;
		try
		{
			theMethod = target.getClass().getMethod(method, argClass);
		}
		catch(NoSuchMethodException nsm)
		{
			System.err.println("[!] Method not found: " + method + " on object " + target);
			Method[] mlist = target.getClass().getMethods();
			for(Method m : mlist)
			{
				System.err.println("[!] - " + m.getName()); 
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		s_methodBuffer.put(method, theMethod);
		return theMethod;
	}
}
