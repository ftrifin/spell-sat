///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.ui
//
// FILE      : MarkerGenerator.java
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
package com.astra.ses.spell.dev.scheck.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

import com.astra.ses.spell.dev.scheck.interfaces.IIssue;
import com.astra.ses.spell.dev.scheck.interfaces.IIssueList;

/******************************************************************************
 * Creates and destroys the markers associated to workbench resources
 *****************************************************************************/
public class MarkerManager 
{
	/** Identifier for information markers */
	private static final String INFORMATION_MARKER = "com.astra.ses.spell.dev.scheck.ui.Information";
	/** Identifier for warning markers */
	private static final String WARNING_MARKER = "com.astra.ses.spell.dev.scheck.ui.Warning";
	/** Identifier for error markers */
	private static final String ERROR_MARKER = "com.astra.ses.spell.dev.scheck.ui.Error";
	/** Identifier for session property */
	private static final QualifiedName SCISSUES_PROPERTY = new QualifiedName(null,"semantics-check-issues");
	
	/**************************************************************************
	 * Create an information marker associated to the given resource, using
	 * the data provided by the given issue 
	 *************************************************************************/
	private static void createInformationMarker( IResource resource, IIssue issue )
	{
		try
		{
			// Creates a marker using the extension point of
			// org.eclipse.ui.resources.markers
			IMarker marker = resource.createMarker( INFORMATION_MARKER );
			if (marker.exists())
			{
				// Sets the marker attributes
				issue.adaptMarker(marker);
			}
		}
		catch(CoreException ex)
		{
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * Create a warning marker associated to the given resource, using
	 * the data provided by the given issue 
	 *************************************************************************/
	private static void createWarningMarker( IResource resource, IIssue issue )
	{
		try
		{
			// Creates a marker using the extension point of
			// org.eclipse.ui.resources.markers
			IMarker marker = resource.createMarker( WARNING_MARKER );
			if (marker.exists())
			{
				// Sets the marker attributes
				issue.adaptMarker(marker);
			}
		}
		catch(CoreException ex)
		{
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * Create an error marker associated to the given resource, using
	 * the data provided by the given issue 
	 *************************************************************************/
	private static void createErrorMarker( IResource resource, IIssue issue )
	{
		try
		{
			// Creates a marker using the extension point of
			// org.eclipse.ui.resources.markers
			IMarker marker = resource.createMarker( ERROR_MARKER );
			if (marker.exists())
			{
				// Sets the marker attributes
				issue.adaptMarker(marker);
			}
		}
		catch(CoreException ex)
		{
			ex.printStackTrace();
		}
	}

	/**************************************************************************
	 * Generate markers for all the issues and resources given 
	 *************************************************************************/
	public static void generateMarkers( Collection<IIssueList> issues, IProgressMonitor monitor )
	{
		int total = 0;
		for( IIssueList list : issues)
		{
			total += list.getIssues().size();
		}
		monitor.beginTask("Generating markers", total);
		for( IIssueList list : issues )
		{
			monitor.subTask("Generating markers for " + list.getResource().getName());
			// Clean any existing marker first for that resource
			cleanMarkers( list.getResource() );
			// For each issue, generate a marker
			for( IIssue issue : list.getIssues() )
			{
				switch(issue.getSeverity())
				{
				case INFORMATION:
					createInformationMarker( list.getResource(), issue );
					break;
				case WARNING:
					createWarningMarker( list.getResource(), issue );
					break;
				case ERROR:
					createErrorMarker( list.getResource(), issue );
					break;
				}
				monitor.worked(1);
				if (monitor.isCanceled()) return;
			}
			// When there are issues associated to the resource, set the session property
			try
			{
				if (list.getResource() instanceof IFile)
				{
					// Remove the session property
					list.getResource().setSessionProperty( SCISSUES_PROPERTY , "YES");
				}
			} 
			catch (CoreException e) 
			{
				e.printStackTrace();
			}
		}
	}

	/**************************************************************************
	 * Clean semantic markers defined for the given resource
	 *************************************************************************/
	public static boolean cleanMarkers( IResource resource )
	{
		boolean someClean = false;
		try 
		{
			// Find and delete information semantics markers
			IMarker[] found = resource.findMarkers( INFORMATION_MARKER, true, IResource.DEPTH_INFINITE);
			someClean = someClean || (found.length>0);
			for(IMarker marker : found)
			{
				marker.delete();
			}
			// Find and delete warning semantics markers
			found = resource.findMarkers( WARNING_MARKER, true, IResource.DEPTH_INFINITE);
			someClean = someClean || (found.length>0);
			for(IMarker marker : found)
			{
				marker.delete();
			}
			// Find and delete error semantics markers
			found = resource.findMarkers( ERROR_MARKER, true, IResource.DEPTH_INFINITE);
			someClean = someClean || (found.length>0);
			for(IMarker marker : found)
			{
				marker.delete();
			}
			cleanSessionProperty(resource);
			// Suggest a garbage collection
			System.gc();
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}
		return someClean;
	}

	/**************************************************************************
	 * Clean the session property 
	 *************************************************************************/
	private static void cleanSessionProperty( IResource resource )
	{
		try
		{
			if (resource instanceof IFile)
			{
				// Remove the session property
				resource.setSessionProperty( SCISSUES_PROPERTY , null);
			}
			else if (resource instanceof IFolder)
			{
				IFolder folder = (IFolder) resource;
				for(IResource subresource : folder.members())
				{
					cleanSessionProperty(subresource);
				}
			}
			else if (resource instanceof IProject)
			{
				IProject project = (IProject) resource;
				for(IResource subresource : project.members())
				{
					cleanSessionProperty(subresource);
				}
			}
		}
		catch(CoreException ex)
		{
			
		}
	}

	/**************************************************************************
	 * Clean all known semantic markers 
	 *************************************************************************/
	public static boolean cleanMarkers()
	{
		IResource root = ResourcesPlugin.getWorkspace().getRoot();
		return cleanMarkers( root );
	}

	/**************************************************************************
	 * Check if the given file has associated issues
	 *************************************************************************/
	public static boolean hasIssues( IFile file )
	{
        try
        {
    		// Find semantics markers
    		int infoCount = file.findMarkers( INFORMATION_MARKER, true, IResource.DEPTH_INFINITE).length;
    		int warnCount = file.findMarkers( WARNING_MARKER, true, IResource.DEPTH_INFINITE).length;
    		int errCount =  file.findMarkers( ERROR_MARKER, true, IResource.DEPTH_INFINITE).length;
    		return ((infoCount + warnCount + errCount)>0);
        }
        catch (CoreException e)
        {
	        e.printStackTrace();
        }
        return false;
	}

	/**************************************************************************
	 * Get all existing markers
	 *************************************************************************/
	public static List<IMarker> getAllMarkers()
	{
		IResource root = ResourcesPlugin.getWorkspace().getRoot();
		return getAllMarkers(root);
	}

	/**************************************************************************
	 * Get all existing markers for a given resource
	 *************************************************************************/
	public static List<IMarker> getAllMarkers( IResource resource )
	{
		List<IMarker> markers = new ArrayList<IMarker>();
        try
        {
    		// Find semantics markers
    		IMarker[] infoMarkers = resource.findMarkers( INFORMATION_MARKER, true, IResource.DEPTH_INFINITE);
    		IMarker[] warnMarkers = resource.findMarkers( WARNING_MARKER, true, IResource.DEPTH_INFINITE);
    		IMarker[] errMarkers = resource.findMarkers( ERROR_MARKER, true, IResource.DEPTH_INFINITE);
    		markers.addAll( Arrays.asList(infoMarkers) );
    		markers.addAll( Arrays.asList(warnMarkers) );
    		markers.addAll( Arrays.asList(errMarkers) );
        }
        catch (CoreException e)
        {
	        e.printStackTrace();
        }
        return markers;
	}
}
