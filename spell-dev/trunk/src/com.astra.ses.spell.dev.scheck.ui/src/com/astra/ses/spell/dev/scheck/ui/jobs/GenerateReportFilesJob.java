///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.ui.jobs
//
// FILE      : GenerateReportFilesJob.java
//
// DATE      : Feb 9, 2011
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
package com.astra.ses.spell.dev.scheck.ui.jobs;

import java.util.Calendar;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.astra.ses.spell.dev.scheck.ui.ReportGenerator;

public class GenerateReportFilesJob implements IRunnableWithProgress
{
	private List<IMarker> m_markers;

	public String m_outputDirectory;
	public IStatus status;
	public int numGenerated;
	
	public GenerateReportFilesJob( String output, List<IMarker> markers )
	{
		m_markers = markers;
		m_outputDirectory = output;
	}

	@Override
	public void run(IProgressMonitor monitor) 
	{
		ReportGenerator generator = new ReportGenerator();
		
		// Get timestamp for file names
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.MONTH)+1;
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
		int second = Calendar.getInstance().get(Calendar.SECOND);
		
		String secStr = "" + second;
		String minStr = "" + minute;
		String hourStr = "" + hour;
		String dayStr = "" + day;
		String monthStr = "" + month;
		if (second<10) secStr = "0" + secStr;
		if (minute<10) minStr = "0" + minStr;
		if (hour<10) hourStr = "0" + hourStr;
		if (day<10) dayStr = "0" + dayStr;
		if (month<10) monthStr = "0" + monthStr;
		
		String timestamp = year + "-" + monthStr + "-" + dayStr + "_" + hourStr + minStr + secStr + "_"; 
		
		generator.generateReportFiles(m_outputDirectory, timestamp, m_markers, monitor);
		
		if (monitor.isCanceled())
		{
			numGenerated = 0;
			status = Status.CANCEL_STATUS;
		}
		else
		{
			monitor.done();
			numGenerated = generator.getNumFiles();
			status = Status.OK_STATUS;
		}
	}

}
