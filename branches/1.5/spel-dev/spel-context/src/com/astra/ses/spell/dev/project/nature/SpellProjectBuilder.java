///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.project.nature
// 
// FILE      : SpellProjectBuilder.java
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.project.nature;

import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/******************************************************************************
 * This project builder will perform some actions over spell projects
 * when any of its internals resources changes or by a workspace request
 * @author jpizar
 *****************************************************************************/
public class SpellProjectBuilder extends IncrementalProjectBuilder {

	/** Builder ID */
	public static final String BUILDER_ID = "com.astra.ses.spell.dev.SpellProjectBuilder";
	
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		switch (kind)
		{
			case INCREMENTAL_BUILD: break;
			case AUTO_BUILD: break;
			case CLEAN_BUILD: break;
			case FULL_BUILD: 
				// Fall through to default
			default: getProject().accept(new SpellProjectVisitor(), IContainer.DEPTH_ONE, false);
		}
		return null;
	}

}
