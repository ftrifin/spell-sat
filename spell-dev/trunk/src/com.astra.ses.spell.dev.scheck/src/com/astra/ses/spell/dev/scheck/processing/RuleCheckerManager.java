///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.processing
//
// FILE      : RuleCheckerManager.java
//
// DATE      : Feb 7, 2011
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.scheck.processing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.astra.ses.spell.dev.scheck.ResourceManager;
import com.astra.ses.spell.dev.scheck.interfaces.EventType;
import com.astra.ses.spell.dev.scheck.interfaces.IEventRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IIssue;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;
import com.astra.ses.spell.dev.scheck.interfaces.IStaticRuleChecker;
import com.astra.ses.spell.dev.scheck.interfaces.IssueFactory;
import com.astra.ses.spell.language.ParseException;
import com.astra.ses.spell.language.Parser;
import com.astra.ses.spell.language.Visitor;
import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.TokenMgrError;

/******************************************************************************
 * Loads and manages the defined checkers. Executes the checks agains the
 * given source code.
 *****************************************************************************/
public class RuleCheckerManager 
{
	private static final String STATIC_CHECKER_EXTENSION_POINT = "com.astra.ses.spell.dev.scheck.StatickRuleChecker";
	private static final String EVENT_CHECKER_EXTENSION_POINT = "com.astra.ses.spell.dev.scheck.EventRuleChecker";
	private static final String CFG_ELEMENT_CLASS = "class";
	private static final String CFG_ELEMENT_EVENTS = "events";

	private List<IStaticRuleChecker> m_staticCheckers;
	private List<IEventRuleChecker> m_eventCheckers;
	private IEventDispatcher m_dispatcher;
	
	/**************************************************************************
	* 
	**************************************************************************/
	public RuleCheckerManager()
	{
		m_staticCheckers = new ArrayList<IStaticRuleChecker>();
		m_eventCheckers = new ArrayList<IEventRuleChecker>();
		m_dispatcher = new EventDispatcher();
	}
	
	/**************************************************************************
	* 
	**************************************************************************/
	public void loadCheckers()
	{

		// Extensions registry
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// Load static checker extensions
		IExtensionPoint ep = registry.getExtensionPoint(STATIC_CHECKER_EXTENSION_POINT);
		// If there are extensions
		if (ep != null)
		{
			IExtension[] extensions = ep.getExtensions();
			for (IExtension extension : extensions)
			{
				try
				{
					IConfigurationElement cfgElement = extension.getConfigurationElements()[0];
					Object obj = cfgElement.createExecutableExtension(CFG_ELEMENT_CLASS);
					IStaticRuleChecker checker = IStaticRuleChecker.class.cast(obj);
					m_staticCheckers.add(checker);
				}
				catch(CoreException ex)
				{
					ex.printStackTrace();
				}
			}
		}

		// Load event checker extensions
		ep = registry.getExtensionPoint(EVENT_CHECKER_EXTENSION_POINT);
		// If there are extensions
		if (ep != null)
		{
			IExtension[] extensions = ep.getExtensions();
			for (IExtension extension : extensions)
			{
				try
				{
					for( IConfigurationElement cfgElement : extension.getConfigurationElements())
					{
						Object obj = cfgElement.createExecutableExtension(CFG_ELEMENT_CLASS);
						IEventRuleChecker checker = IEventRuleChecker.class.cast(obj);
						m_eventCheckers.add(checker);
						
						// Register the checker in the dispatcher
						String eventString = cfgElement.getAttribute(CFG_ELEMENT_EVENTS);
						String[] events = eventString.split(",");
						
						for(String eventStr : events)
						{
							try
							{
								EventType type = EventType.valueOf(eventStr);
								m_dispatcher.registerForEvent(type, checker);
							}
							catch(Exception ex)
							{
								ex.printStackTrace();
							}
						}
					}
				}
				catch(CoreException ex)
				{
					ex.printStackTrace();
				}
			}
			
			// Initialize checkers
			for(IEventRuleChecker checker : m_eventCheckers)
			{
				try
				{
					checker.initialize();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}
	
	/**************************************************************************
	* 
	**************************************************************************/
	public void performStaticChecks( IIssueList issues, IProgressMonitor monitor )
	{
		// Perform static checks
		if (m_staticCheckers.size() > 0)
		{
			for(IStaticRuleChecker checker : m_staticCheckers )
			{
				monitor.subTask("Running statick check " + checker.getName());
				checker.performCheck(issues);
				if (monitor.isCanceled()) return;
			}
			monitor.subTask("Statick checks finished");
		}
		else
		{
			IIssue issue = IssueFactory.createInformationIssue("No static checks to be performed");
			issues.addIssue(issue);
		}
	}
	
	/**************************************************************************
	* 
	**************************************************************************/
	public void performEventChecks( IIssueList issues, IProgressMonitor monitor )
	{
		if (m_eventCheckers.size()>0)
		{
			Parser parser = new Parser();
			try 
			{
				if (!issues.getResource().isSynchronized( IResource.DEPTH_INFINITE ))
				{
					IIssue issue = IssueFactory.createErrorIssue( "Cannot perform check on " + issues.getResource().getName());
					issues.addIssue(issue);
					return;
				}
				monitor.subTask("Running parse check on '" + issues.getResource().getName() + "'");
				
				// Get source information
				String sourceCode = ResourceManager.instance().getSource( (IFile) issues.getResource() );
				int totalLines = ResourceManager.instance().getSourceLines( (IFile) issues.getResource() );
				
				// Initialize the dispatcher
				m_dispatcher.initialize( issues.getResource(), totalLines, monitor );
				for( IEventRuleChecker checker : m_eventCheckers )
				{
					checker.notifyParsingStarted( issues );
					if (monitor.isCanceled()) return;
				}
				
				// Perform the actual parse
				monitor.subTask("Parsing '" + issues.getResource().getName() + "'");
				SimpleNode root = parser.parseCodeGetTree(sourceCode);
				
				// Perform the dispatch
				monitor.beginTask("Applying parser rules on '" + issues.getResource().getName() + "'", totalLines);
				if ((root != null)&&(!monitor.isCanceled()))
				{
					root.accept( (Visitor) m_dispatcher );
				}
				
				if (!monitor.isCanceled())
				{
					monitor.subTask("Gathering information");
					m_dispatcher.gatherIssues(issues);
					for( IEventRuleChecker checker : m_eventCheckers )
					{
						checker.notifyParsingFinished(issues);
						if (monitor.isCanceled()) return;
					}
				}
			}
			catch (ParseException error) 
			{
				IIssue issue = IssueFactory.createErrorIssue( "Parser error, cannot continue check: " + error.getLocalizedMessage(), error.currentToken.beginLine, -1, -1);
				issues.addIssue(issue);
			}
			catch(TokenMgrError error)
			{
				IIssue issue = IssueFactory.createErrorIssue( "Token error, cannot continue check: " + error.getLocalizedMessage(), error.errorLine, -1, -1);
				issues.addIssue(issue);
			}
			catch(Exception error)
			{
				error.printStackTrace();
				IIssue issue = IssueFactory.createErrorIssue( "Internal error, cannot continue check: " + error.getLocalizedMessage());
				issues.addIssue(issue);
			}
		}
		else
		{
			IIssue issue = IssueFactory.createInformationIssue("No event-based checks to be performed");
			issues.addIssue(issue);
		}
	}
}
