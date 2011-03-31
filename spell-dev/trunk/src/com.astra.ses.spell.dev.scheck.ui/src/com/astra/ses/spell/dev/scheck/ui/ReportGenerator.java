///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.processing
//
// FILE      : ReportGenerator.java
//
// DATE      : Feb 11, 2011
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ReportGenerator
{
	private int m_numFiles;
	
	public ReportGenerator()
	{
		m_numFiles = 0;
	}
	
	public int getNumFiles()
	{
		return m_numFiles;
	}
	
	public void generateReportFiles( String outputDirectory, String timestamp,
									 List<IMarker> markers,
									 IProgressMonitor monitor
									 )
	{
		Map<String,List<IMarker>> orderedMarkers = new TreeMap<String, List<IMarker>>();
		
		for( IMarker marker : markers )
		{
			String id = marker.getResource().getFullPath().toPortableString();
			if (!orderedMarkers.containsKey(id))
			{
				orderedMarkers.put(id, new ArrayList<IMarker>() );
			}
			orderedMarkers.get(id).add(marker);
		}
		
		for( String id : orderedMarkers.keySet() )
		{
			List<IMarker> allMarkers = orderedMarkers.get(id);
			if (allMarkers.size()>0)
			{
				IResource head = allMarkers.get(0).getResource();
				String resourceName = head.getName();
				String filename = "";
				if ( head instanceof IFile )
				{
					filename = head.getName();
					String extension = head.getFileExtension();
					int idx = filename.lastIndexOf(extension)-1;
					filename = outputDirectory + File.separator + timestamp + filename.substring(0,idx) + ".chk";
					resourceName = filename.substring(0,idx);
				}
				else
				{
					filename = outputDirectory + File.separator + timestamp + resourceName + ".chk";
				}
				monitor.subTask("Generating report for " + resourceName);
				
				try 
				{
					PrintWriter writer = new PrintWriter( new OutputStreamWriter( new FileOutputStream(filename)));
					List<IMarker> infos = new ArrayList<IMarker>();
					List<IMarker> warnings = new ArrayList<IMarker>();
					List<IMarker> errors = new ArrayList<IMarker>();

					monitor.subTask("Sorting issues");
					for(IMarker marker : allMarkers )
					{
						String severity = (String) marker.getAttribute( IMarker.SEVERITY );
						if (severity.equals(IMarker.SEVERITY_INFO))
						{
							infos.add(marker);
						}
						else if (severity.equals(IMarker.SEVERITY_WARNING))
						{
							warnings.add(marker);
						}
						else 
						{
							errors.add(marker);
						}
					}
					
					monitor.subTask("Dumping issues");

					String line = "ERRORS =======================================================================";
					writer.println(line);
					for(IMarker marker : errors)
					{
						String lineStr = (String) marker.getAttribute( IMarker.LINE_NUMBER );
						String message = (String) marker.getAttribute( IMarker.MESSAGE );
						if (lineStr != null)
						{
							line = lineStr + "\t" + message;
						}
						else
						{
							line = "n/a\t" + message;
						}
						writer.println(line);
					}
					
					line = "WARNINGS =====================================================================";
					writer.println(line);
					for(IMarker marker : warnings)
					{
						String lineStr = (String) marker.getAttribute( IMarker.LINE_NUMBER );
						String message = (String) marker.getAttribute( IMarker.MESSAGE );
						if (lineStr != null)
						{
							line = lineStr + "\t" + message;
						}
						else
						{
							line = "n/a\t" + message;
						}
						writer.println(line);
					}
					
					line = "INFORMATION AND RECOMMENDATIONS ==============================================";
					writer.println(line);
					for(IMarker marker : infos)
					{
						String lineStr = (String) marker.getAttribute( IMarker.LINE_NUMBER );
						String message = (String) marker.getAttribute( IMarker.MESSAGE );
						if (lineStr != null)
						{
							line = lineStr + "\t" + message;
						}
						else
						{
							line = "n/a\t" + message;
						}
						writer.println(line);
					}
					writer.close();
					m_numFiles++;
					monitor.subTask("File report finished");
				} 
				catch (CoreException e) 
				{
					e.printStackTrace();
				}
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}

			}
		}
	}
}
