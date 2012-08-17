package com.astra.ses.spell.gui.unittest;

import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.textui.TestRunner;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.astra.ses.spell.gui.unittest.printer.TestResultPrinter;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	
	/** Test suite extension point ID */
	private static final String EXTENSION_ID = "com.astra.ses.spell.gui.test.suite";
	private static final String ELEMENT_CLASS = "class";
	private static final String ELEMENT_NAME = "name";
	private static final String ELEMENT_DESC = "description";
	
	/***
	 * 
	 * @author jpizar
	 *
	 */
	private class ContributedTest
	{
		/** Test to be performed */
		public Test test;
		/** Test name */
		public String testName;
		/** Test description */
		public String testDescription;
		
		/**
		 * Constructor
		 * @param name
		 * @param description
		 * @param test
		 */
		public ContributedTest(String name, String description, Test test)
		{
			this.test = test;
			testName = name;
			testDescription = description;
		}
	}
	
	/*==========================================================================
	 *  (non-Javadoc)
	 * @see IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 ==========================================================================*/
	@Override
	public Object start(IApplicationContext context) throws Exception {	
		
		System.out.println("==================================================");
		System.out.println("Starting test application");
		System.out.println("==================================================");
		
		// Prepare the test runner
		TestRunner runner = new TestRunner();
		runner.setPrinter(new TestResultPrinter(System.out));
		
		ContributedTest[] suites = loadSuites();
		for (ContributedTest test : suites)
		{
			System.out.println("Starting test: " + test.testName);
			System.out.println("Test target: " + test.testDescription);
			
			// Perform the tests
			TestResult result = runner.doRun(test.test);
			
			System.out.println("Finished test: " + test.testName + ". Passed: " + result.wasSuccessful());
			// If last test execution was not successful, return error code
			if (result.wasSuccessful())
			{
				return new Integer(-1);
			}
		}

		System.out.println("==================================================");
		System.out.println("End test application");
		System.out.println("==================================================");
		
		return IApplication.EXIT_OK;
	}

	/*==========================================================================
	 * @see IApplication#stop()
	 ==========================================================================*/
	public void stop() {
		// nothing to do
	}
	
	/***************************************************************************
	 * Load test suites provided through teh extension points
	 **************************************************************************/
	private ContributedTest[] loadSuites()
	{
		Vector<ContributedTest> tests = new Vector<ContributedTest>();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(EXTENSION_ID);
		IExtension[] extensions = ep.getExtensions();
		if (extensions.length == 0)
		{
			System.err.println("No Tests to execute");
			return new ContributedTest[0];
		}
		
		/*
		 * Load the extension and retrieve the Tests
		 */
		for (IExtension extension : extensions)
		{
			/*
			 * Activate the contributing plugin if it has not been activated
			 */
			/*String pluginId = extension.getNamespaceIdentifier();
			Bundle contributingBundle = Platform.getBundle(pluginId);
			if (contributingBundle.getState() != Bundle.ACTIVE)
			{
				try {
					contributingBundle.start();
				} catch (BundleException e) {
					e.printStackTrace();
				}
			}*/
			
			// Obtain the configuration element for this extension point
			for (IConfigurationElement conf : extension.getConfigurationElements())
			{
				try
				{
					Object execExtension =  conf.createExecutableExtension(ELEMENT_CLASS);
					String testName = conf.getAttribute(ELEMENT_NAME);
					String testDesString = conf.getAttribute(ELEMENT_DESC);
					
					
					Test extensionInterface = (Test) execExtension;
					
					ContributedTest test = new ContributedTest(testName, testDesString, extensionInterface);
					tests.add(test);
				}
				catch(CoreException ex)
				{
					ex.printStackTrace();
				}
			}
		}
		
		return tests.toArray(new ContributedTest[0]);
	}
}
