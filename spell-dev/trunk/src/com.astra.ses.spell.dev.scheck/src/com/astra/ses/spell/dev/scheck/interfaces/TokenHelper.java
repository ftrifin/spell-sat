///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.scheck.interfaces
//
// FILE      : TokenHelper.java
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
package com.astra.ses.spell.dev.scheck.interfaces;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import com.astra.ses.spell.dev.scheck.ResourceManager;
import com.astra.ses.spell.language.SpellProgrammingLanguage;
import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.SpecialStr;
import com.astra.ses.spell.language.model.ast.Attribute;
import com.astra.ses.spell.language.model.ast.BinOp;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.Dict;
import com.astra.ses.spell.language.model.ast.FunctionDef;
import com.astra.ses.spell.language.model.ast.List;
import com.astra.ses.spell.language.model.ast.ListComp;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.NameTok;
import com.astra.ses.spell.language.model.ast.Num;
import com.astra.ses.spell.language.model.ast.Str;
import com.astra.ses.spell.language.model.ast.StrJoin;
import com.astra.ses.spell.language.model.ast.Subscript;
import com.astra.ses.spell.language.model.ast.commentType;
import com.astra.ses.spell.language.model.ast.exprType;

public class TokenHelper  
{
	public static int getStartOffset( IResource resource, SimpleNode node )
	{
		String sourceStr = ResourceManager.instance().getSource( (IFile) resource );
		// The file is forced to be unix
		String[] source = sourceStr.split( "\n" );
		int offset = 0;
		int lengthToBeginLine = 0;
		for( int index = 0; index<node.beginLine-1; index++)
		{
			lengthToBeginLine += source[index].length()+1;
		}
		offset = lengthToBeginLine + node.beginColumn-1;
		if (node instanceof Call)
		{
			Call call = (Call) node;
			if (call.func instanceof Name)
			{
				offset -= ((Name)call.func).id.length();
			}
		}
		else if (node instanceof FunctionDef)
		{
			offset += 4;
		}
		return offset;
	}

	public static int getTokenLength( Object node, boolean addSpecials )
	{
		int length = 0;
		
		if (addSpecials && (node instanceof SimpleNode))
		{
			for( Object obj : ((SimpleNode)node).getSpecialsBefore())
			{
				length += getTokenLength(obj, true);
			}
		}

		if (node instanceof Call)
		{
			Call call = (Call) node;
			if (call.func instanceof Name)
			{
				Name functionName = (Name) call.func;
				length = functionName.id.length();
			}
			else 
			{
				Attribute attr = (Attribute) call.func;
				length = getTokenLength(attr.value, false);
			}
		}
		else if (node instanceof Str)
		{
			Str str = (Str) node;
			length = str.s.length() + 2;
		}
		else if (node instanceof String)
		{
			length = ((String)node).length();
		}
		else if (node instanceof SpecialStr)
		{
			SpecialStr sstr = (SpecialStr) node;
			length = sstr.str.length();
		}
		else if (node instanceof Name)
		{
			Name name = (Name) node;
			length = name.id.length();
		}
		else if (node instanceof NameTok)
		{
			NameTok name = (NameTok) node;
			length = name.id.length();
		}
		else if (node instanceof Num)
		{
			Num num = (Num) node;
			length = num.num.length();
		}
		else if (node instanceof FunctionDef)
		{
			FunctionDef def = (FunctionDef) node;
			length = ((NameTok)def.name).id.length();
		}
		else if (node instanceof StrJoin)
		{
			StrJoin join = (StrJoin) node;
			for( exprType expr : join.strs )
			{
				Str s = (Str) expr;
				length += s.s.length() + 2;
			}
		}
		else if (node instanceof BinOp)
		{
			BinOp op = (BinOp) node;
			length = getTokenLength(op.left, true) + getTokenLength(op.right, true); 
		}
		else if (node instanceof Subscript)
		{
			Subscript sc = (Subscript) node;
			length = getTokenLength(sc.value, true);
		}
		else if (node instanceof List)
		{
			List list = (List) node;
			for( exprType expr : list.elts)
			{
				length += getTokenLength(expr, true);
			}
		}
		else if (node instanceof Dict)
		{
			Dict dict = (Dict) node;
			for( exprType expr : dict.keys)
			{
				length += getTokenLength(expr, true);
			}
			for( exprType expr : dict.values)
			{
				length += getTokenLength(expr, true);
			}
		}
		else if (node instanceof commentType)
		{
			commentType comment = (commentType) node;
			length = comment.id.length();
		}
		else if (node instanceof ListComp)
		{
			ListComp lcomp = (ListComp) node;
			length = getTokenLength(lcomp.elt, true);
		}
		else
		{
			System.err.println("Cannot find token length: " + node);
			length = 2;
		}
		
		if (addSpecials && (node instanceof SimpleNode))
		{
			for( Object obj : ((SimpleNode)node).getSpecialsAfter())
			{
				length += getTokenLength(obj,true);
			}
		}

		return length;
	}
	
	/**************************************************************************
	 * 
	 *************************************************************************/
	public static ValueType getEquivalentType( SimpleNode node )
	{
		ValueType type = ValueType.ANY;
		if (node instanceof Str)
		{
			type = ValueType.STRING;
		}
		else if (node instanceof Num)
		{
			type = ValueType.NUM;
		}
		else if (node instanceof BinOp)
		{
			type = getEquivalentType( ((BinOp)node).left );
		}
		else if (node instanceof StrJoin)
		{
			type = ValueType.STRING;
		}
		else if (node instanceof Name)
		{
			Name name = (Name)node;
			if (name.id.equals("TIME"))
			{
				type = ValueType.TIME;
			}
			else if (name.id.equals("True")||name.id.equals("False"))
			{
				type = ValueType.BOOL;
			}
			else
			{
				type = ValueType.NAME;
			}
		}
		else if (node instanceof List)
		{
			type = ValueType.LIST;
		}
		else if (node instanceof Dict)
		{
			type = ValueType.DICT;
		}
		else if (node instanceof Subscript)
		{
			type = ValueType.NAME;
		}
		else if (node instanceof ListComp)
		{
			type = ValueType.LIST;
		}
//		else
//		{
//			System.err.println("Cannot find type for modifier token " + node.getClass().getName() + ": " + node);
//		}
		return type;
	}

	/**************************************************************************
	 * 
	 *************************************************************************/
	public static String getEquivalentValue( Object node )
	{
		String value = "";
		
		if (node instanceof SimpleNode)
		{
			for( Object obj : ((SimpleNode)node).getSpecialsBefore())
			{
				value += getEquivalentValue(obj);
			}
		}

		if (node instanceof Str)
		{
			value = ((Str)node).s;
		}
		else if (node instanceof Num)
		{
			value = ((Num)node).num;
		}
		else if (node instanceof BinOp)
		{
			BinOp bo = (BinOp) node;
			String operator = "";
			switch(bo.op)
			{
			case BinOp.Add:
				operator = "+";
				break;
			case BinOp.Div:
				operator = "/";
				break;
			case BinOp.Mult:
				operator = "*";
				break;
			case BinOp.Sub:
				operator = "-";
				break;
			case BinOp.BitAnd:
				operator = "&";
				break;
			case BinOp.BitOr:
				operator = "|";
				break;
			case BinOp.BitXor:
				operator = "^";
				break;
			case BinOp.FloorDiv:
				operator = "\\";
				break;
			case BinOp.Mod:
				operator = "%";
				break;
			case BinOp.Pow:
				operator = "**";
				break;
			case BinOp.LShift:
				operator = "<<";
				break;
			case BinOp.RShift:
				operator = ">>";
				break;
			}
			value = getEquivalentValue(bo.left) + operator + getEquivalentValue( ((BinOp)node).right ); 
		}
		else if (node instanceof StrJoin)
		{
			for( exprType s : ((StrJoin)node).strs )
			{
				value += getEquivalentValue( s );
			}
		}
		else if (node instanceof Name)
		{
			String nam = ((Name)node).id;
			if (SpellProgrammingLanguage.getInstance().isSpellConstant(nam))
			{
				value = nam;
			}
			else
			{
				value = "@name";
			}
		}
		else if (node instanceof List)
		{
			value = "@list";
		}
		else if (node instanceof Dict)
		{
			value = "@dict";
		}
		
		if (node instanceof SimpleNode)
		{
			for( Object obj : ((SimpleNode)node).getSpecialsAfter())
			{
				value += getEquivalentValue(obj);
			}
		}

		return value;
	}

}
