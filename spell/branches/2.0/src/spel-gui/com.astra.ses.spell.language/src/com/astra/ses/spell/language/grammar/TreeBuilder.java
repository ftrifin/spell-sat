package com.astra.ses.spell.language.grammar;

import java.util.ArrayList;
import java.util.Collections;

import com.astra.ses.spell.language.ParseException;
import com.astra.ses.spell.language.common.AbstractTreeBuilder;
import com.astra.ses.spell.language.common.ComprehensionCollection;
import com.astra.ses.spell.language.common.Decorators;
import com.astra.ses.spell.language.common.DefaultArg;
import com.astra.ses.spell.language.common.ExtraArg;
import com.astra.ses.spell.language.common.ExtraArgValue;
import com.astra.ses.spell.language.common.FuncDefReturnAnn;
import com.astra.ses.spell.language.common.ITreeBuilder;
import com.astra.ses.spell.language.common.ITreeConstants;
import com.astra.ses.spell.language.common.JfpDef;
import com.astra.ses.spell.language.model.SimpleNode;
import com.astra.ses.spell.language.model.ast.Assert;
import com.astra.ses.spell.language.model.ast.Assign;
import com.astra.ses.spell.language.model.ast.Call;
import com.astra.ses.spell.language.model.ast.ClassDef;
import com.astra.ses.spell.language.model.ast.Comprehension;
import com.astra.ses.spell.language.model.ast.Dict;
import com.astra.ses.spell.language.model.ast.DictComp;
import com.astra.ses.spell.language.model.ast.Ellipsis;
import com.astra.ses.spell.language.model.ast.Expr;
import com.astra.ses.spell.language.model.ast.FunctionDef;
import com.astra.ses.spell.language.model.ast.Global;
import com.astra.ses.spell.language.model.ast.IfExp;
import com.astra.ses.spell.language.model.ast.ImportFrom;
import com.astra.ses.spell.language.model.ast.Index;
import com.astra.ses.spell.language.model.ast.Lambda;
import com.astra.ses.spell.language.model.ast.List;
import com.astra.ses.spell.language.model.ast.ListComp;
import com.astra.ses.spell.language.model.ast.Name;
import com.astra.ses.spell.language.model.ast.NameTok;
import com.astra.ses.spell.language.model.ast.NameTokType;
import com.astra.ses.spell.language.model.ast.NonLocal;
import com.astra.ses.spell.language.model.ast.Raise;
import com.astra.ses.spell.language.model.ast.Return;
import com.astra.ses.spell.language.model.ast.Set;
import com.astra.ses.spell.language.model.ast.SetComp;
import com.astra.ses.spell.language.model.ast.Slice;
import com.astra.ses.spell.language.model.ast.Str;
import com.astra.ses.spell.language.model.ast.StrJoin;
import com.astra.ses.spell.language.model.ast.Subscript;
import com.astra.ses.spell.language.model.ast.Suite;
import com.astra.ses.spell.language.model.ast.TryExcept;
import com.astra.ses.spell.language.model.ast.TryFinally;
import com.astra.ses.spell.language.model.ast.Tuple;
import com.astra.ses.spell.language.model.ast.While;
import com.astra.ses.spell.language.model.ast.With;
import com.astra.ses.spell.language.model.ast.Yield;
import com.astra.ses.spell.language.model.ast.aliasType;
import com.astra.ses.spell.language.model.ast.argumentsType;
import com.astra.ses.spell.language.model.ast.excepthandlerType;
import com.astra.ses.spell.language.model.ast.exprType;
import com.astra.ses.spell.language.model.ast.keywordType;
import com.astra.ses.spell.language.model.ast.sliceType;
import com.astra.ses.spell.language.model.ast.stmtType;
import com.astra.ses.spell.language.model.ast.suiteType;

public final class TreeBuilder extends AbstractTreeBuilder implements
        ITreeBuilder, ITreeConstants
{

	public TreeBuilder(JJTSpellGrammarState stack)
	{
		super(stack);
	}

	public final SimpleNode onCloseNode(SimpleNode n, int arity)
	        throws Exception
	{
		exprType value;
		exprType[] exprs;
		suiteType orelseSuite;
		stmtType[] body;
		exprType iter;
		exprType target;
		Suite suite;

		int l;
		switch (n.getId())
		{
		case JJTEXPR_STMT:
			value = (exprType) stack.popNode();
			if (arity > 1)
			{
				exprs = makeExprs(arity - 1);
				ctx.setStore(exprs);
				return new Assign(exprs, value);
			}
			else
			{
				return new Expr(value);
			}
		case JJTINDEX_OP:
			sliceType slice = (sliceType) stack.popNode();
			value = (exprType) stack.popNode();
			return new Subscript(value, slice, Subscript.Load);

		case JJTBEGIN_FOR_ELSE_STMT:
			return new suiteType(null);
		case JJTBEGIN_ELSE_STMT:
			return new suiteType(null);
		case JJTBEGIN_WHILE_STMT:
			return new While(null, null, null);
		case JJTWHILE_STMT:
			orelseSuite = null;
			if (stack.nodeArity() == 5)
			{
				orelseSuite = popSuiteAndSuiteType();
			}

			body = popSuite();
			exprType test = (exprType) stack.popNode();
			While w = (While) stack.popNode();
			w.test = test;
			w.body = body;
			w.orelse = orelseSuite;
			return w;
		case JJTCALL_OP:
			exprType starargs = null;
			exprType kwargs = null;

			java.util.List<exprType> args = new ArrayList<exprType>();
			java.util.List<keywordType> keywords = new ArrayList<keywordType>();

			for (int i = arity - 2; i >= 0; i--)
			{
				SimpleNode node = stack.popNode();
				if (node instanceof keywordType)
				{
					keywords.add(0, (keywordType) node);

				}
				else if (node.getId() == JJTEXTRAARGVALUELIST)
				{
					ExtraArgValue nstarargs = (ExtraArgValue) node;
					starargs = nstarargs.value;
					this.addSpecialsAndClearOriginal(nstarargs, starargs);

				}
				else if (node.getId() == JJTEXTRAKEYWORDVALUELIST)
				{
					ExtraArgValue nkwargs = (ExtraArgValue) node;
					kwargs = nkwargs.value;
					this.addSpecialsAndClearOriginal(nkwargs, kwargs);

				}
				else if (node instanceof ComprehensionCollection)
				{
					// what can happen is something like print sum(x for x in
					// y), where we have already passed x in the args, and then
					// get 'for x in y'
					args.add(0, new ListComp((exprType) stack.popNode(),
					        ((ComprehensionCollection) node).getGenerators()));
					i--; // popped node

				}
				else
				{
					args.add(0, (exprType) node);
				}
			}

			exprType func = (exprType) stack.popNode();
			Call c = new Call(func, args.toArray(new exprType[args.size()]),
			        keywords.toArray(new keywordType[keywords.size()]),
			        starargs, kwargs);
			addSpecialsAndClearOriginal(n, c);
			return c;
		case JJTFUNCDEF_RETURN_ANNOTTATION:
			SimpleNode funcdefReturn = stack.popNode();
			return new FuncDefReturnAnn(funcdefReturn);
		case JJTFUNCDEF:
			suite = (Suite) stack.popNode();
			body = suite.body;
			arity--;

			SimpleNode funcDefReturnAnn = stack.peekNode();
			exprType actualReturnAnnotation = null;
			if (funcDefReturnAnn instanceof FuncDefReturnAnn)
			{
				stack.popNode();
				actualReturnAnnotation = (exprType) ((FuncDefReturnAnn) funcDefReturnAnn).node;
				arity--;
				addSpecialsAndClearOriginal(funcDefReturnAnn,
				        actualReturnAnnotation);
			}
			argumentsType arguments = makeArguments(arity - 1);
			NameTok nameTok = makeName(NameTok.FunctionName);
			// decorator is always null at this point... it's decorated later on
			FunctionDef funcDef = new FunctionDef(nameTok, arguments, body,
			        null, actualReturnAnnotation);
			addSpecialsAndClearOriginal(suite, funcDef);
			setParentForFuncOrClass(body, funcDef);
			return funcDef;
		case JJTTFPDEF:
			Name tfpdefName = null;
			exprType typeDef = null;
			if (arity == 1)
			{
				tfpdefName = (Name) stack.popNode();
			}
			else if (arity == 2)
			{
				typeDef = (exprType) stack.popNode();
				tfpdefName = (Name) stack.popNode();
			}
			else
			{
				throw new RuntimeException("Unexpected arity: " + arity);
			}

			return new JfpDef(tfpdefName, typeDef);
		case JJTONLYKEYWORDARG2:
		case JJTDEFAULTARG2:
			DefaultArg defaultArg;
			JfpDef jfpDef;
			if (arity == 1)
			{
				jfpDef = (JfpDef) stack.popNode();
				defaultArg = new DefaultArg(jfpDef.nameNode, null,
				        jfpDef.typeDef, n.getId());
			}
			else if (arity == 2)
			{
				exprType defaultValue = (exprType) stack.popNode();
				jfpDef = (JfpDef) stack.popNode();
				defaultArg = new DefaultArg(jfpDef.nameNode, defaultValue,
				        jfpDef.typeDef, n.getId());
			}
			else
			{
				throw new RuntimeException("Unexpected arity: " + arity);
			}
			return defaultArg;
		case JJTONLYKEYWORDARG:
		case JJTDEFAULTARG:
			// no type definition in this case
			if (arity == 1) { return new DefaultArg(
			        ((exprType) stack.popNode()), null, null, n.getId()); }
			exprType parameter = (exprType) stack.popNode();
			return new DefaultArg((exprType) stack.popNode(), parameter, null,
			        n.getId());
		case JJTEXTRAARGLIST:
			if (arity == 0)
			{
				// nothing here (just '*')
				return new ExtraArg(null, JJTEXTRAARGLIST, null);
			}
			return new ExtraArg(makeName(NameTok.VarArg), JJTEXTRAARGLIST);
		case JJTEXTRAKEYWORDLIST:
			return new ExtraArg(makeName(NameTok.KwArg), JJTEXTRAKEYWORDLIST);
		case JJTEXTRAARGLIST2: // with type declaration
			if (arity == 0)
			{
				// nothing here (just '*')
				return new ExtraArg(null, JJTEXTRAARGLIST, null);
			}
			jfpDef = (JfpDef) stack.popNode();
			NameTok jfpDefName = makeName(NameTok.VarArg, jfpDef.nameNode);
			ExtraArg extra = new ExtraArg(jfpDefName, JJTEXTRAARGLIST,
			        jfpDef.typeDef);
			return extra;
		case JJTEXTRAKEYWORDLIST2: // with type declaration
			jfpDef = (JfpDef) stack.popNode();
			return new ExtraArg(makeName(NameTok.KwArg, jfpDef.nameNode),
			        JJTEXTRAKEYWORDLIST, jfpDef.typeDef);
		case JJTDECORATED:
			if (stack.nodeArity() != 2) { throw new RuntimeException(
			        "Expected 2 nodes at this context, found: " + arity); }
			SimpleNode def = stack.popNode();
			Decorators decorators = (Decorators) stack.popNode();
			if (def instanceof ClassDef)
			{
				ClassDef classDef = (ClassDef) def;
				classDef.decs = decorators.exp;
			}
			else
			{
				FunctionDef fDef = (FunctionDef) def;
				fDef.decs = decorators.exp;
			}
			return def;
		case JJTCLASSDEF:
			suite = (Suite) stack.popNode();
			body = suite.body;
			int nodeArity = stack.nodeArity() - 1;
			ArrayList<keywordType> classDefKeywords = new ArrayList<keywordType>();
			starargs = null;
			kwargs = null;

			for (int i = 0; i < nodeArity; i++)
			{
				SimpleNode node = stack.peekNode();
				if (node instanceof keywordType)
				{
					stack.popNode();
					classDefKeywords.add((keywordType) node);
					nodeArity--;
				}
				else if (node instanceof ExtraArgValue)
				{
					if (node.getId() == JJTEXTRAARGVALUELIST)
					{
						ExtraArgValue nstarargs = (ExtraArgValue) stack
						        .popNode();
						starargs = nstarargs.value;
						this.addSpecialsAndClearOriginal(nstarargs, starargs);
						nodeArity--;
					}
					else if (node.getId() == JJTEXTRAKEYWORDVALUELIST)
					{
						ExtraArgValue nkwargs = (ExtraArgValue) stack.popNode();
						kwargs = nkwargs.value;
						this.addSpecialsAndClearOriginal(nkwargs, kwargs);
						nodeArity--;
					}
				}
			}

			exprType[] bases = makeExprs(nodeArity);
			nameTok = makeName(NameTok.ClassName);
			// decorator is always null at this point... it's decorated later on
			ClassDef classDef = new ClassDef(nameTok, bases, body, null,
			        classDefKeywords.toArray(new keywordType[classDefKeywords
			                .size()]), starargs, kwargs);

			addSpecialsAndClearOriginal(suite, classDef);
			setParentForFuncOrClass(body, classDef);
			return classDef;
		case JJTBEGIN_RETURN_STMT:
			return new Return(null);
		case JJTRETURN_STMT:
			value = arity == 2 ? ((exprType) stack.popNode()) : null;
			Return ret = (Return) stack.popNode();
			ret.value = value;
			return ret;
		case JJTYIELD_STMT:
			return stack.popNode();
		case JJTYIELD_EXPR:
			exprType yieldExpr = null;
			if (arity > 0)
			{
				// we may have an empty yield, so, we have to check it before
				yieldExpr = (exprType) stack.popNode();
			}
			return new Yield(yieldExpr);
		case JJTRAISE_STMT:
			exprType from = arity >= 2 ? ((exprType) stack.popNode()) : null;
			exprType type = arity >= 1 ? ((exprType) stack.popNode()) : null;
			return new Raise(type, null, null, from);
		case JJTGLOBAL_STMT:
			if (arity == 0)
			{
				return new Global(makeIdentifiers(NameTok.GlobalName), null);
			}
			else
			{
				exprType globalValue = (exprType) stack.popNode();
				return new Global(makeIdentifiers(NameTok.GlobalName),
				        globalValue);
			}
		case JJTNONLOCAL_STMT:
			if (arity == 0)
			{
				return new NonLocal(makeIdentifiers(NameTok.NonLocalName), null);
			}
			else
			{
				exprType nonLocalValue = (exprType) stack.popNode();
				return new NonLocal(makeIdentifiers(NameTok.NonLocalName),
				        nonLocalValue);
			}
		case JJTASSERT_STMT:
			exprType msg = arity == 2 ? ((exprType) stack.popNode()) : null;
			test = (exprType) stack.popNode();
			return new Assert(test, msg);
		case JJTBEGIN_TRY_STMT:
			// we do that just to get the specials
			return new TryExcept(null, null, null);
		case JJTTRYELSE_STMT:
			orelseSuite = popSuiteAndSuiteType();
			return orelseSuite;
		case JJTTRYFINALLY_OUTER_STMT:
			orelseSuite = popSuiteAndSuiteType();
			return new TryFinally(null, orelseSuite); // it does not have a body
													  // at this time... it will
													  // be filled with the
													  // inner try..except
		case JJTTRY_STMT:
			TryFinally outer = null;
			if (stack.peekNode() instanceof TryFinally)
			{
				outer = (TryFinally) stack.popNode();
				arity--;
			}
			orelseSuite = null;
			if (stack.peekNode() instanceof suiteType)
			{
				orelseSuite = (suiteType) stack.popNode();
				arity--;
			}

			l = arity;
			excepthandlerType[] handlers = new excepthandlerType[l];
			for (int i = l - 1; i >= 0; i--)
			{
				handlers[i] = (excepthandlerType) stack.popNode();
			}
			suite = (Suite) stack.popNode();
			TryExcept tryExc = (TryExcept) stack.popNode();
			if (outer != null)
			{
				outer.beginLine = tryExc.beginLine;
			}
			tryExc.body = suite.body;
			tryExc.handlers = handlers;
			tryExc.orelse = orelseSuite;
			addSpecials(suite, tryExc);
			if (outer == null)
			{
				return tryExc;
			}
			else
			{
				if (outer.body != null) { throw new RuntimeException(
				        "Error. Expecting null body to be filled on try..except..finally"); }
				outer.body = new stmtType[] { tryExc };
				return outer;
			}
		case JJTBEGIN_TRY_ELSE_STMT:
			// we do that just to get the specials
			return new suiteType(null);
		case JJTBEGIN_EXCEPT_CLAUSE:
			return new excepthandlerType(null, null, null);
		case JJTEXCEPT_CLAUSE:
			suite = (Suite) stack.popNode();
			body = suite.body;
			exprType excname = arity == 4 ? ((exprType) stack.popNode()) : null;
			if (excname != null)
			{
				ctx.setStore(excname);
			}
			type = arity >= 3 ? ((exprType) stack.popNode()) : null;
			excepthandlerType handler = (excepthandlerType) stack.popNode();
			handler.type = type;
			handler.name = excname;
			handler.body = body;
			addSpecials(suite, handler);
			return handler;
		case JJTBEGIN_FINALLY_STMT:
			// we do that just to get the specials
			return new suiteType(null);
		case JJTTRYFINALLY_STMT:
			suiteType finalBody = popSuiteAndSuiteType();
			body = popSuite();
			// We have a try..except in the stack, but we will change it for a
			// try..finally
			// This is because we recognize a try..except in the 'try:' token,
			// but actually end up with a try..finally
			TryExcept tryExcept = (TryExcept) stack.popNode();
			TryFinally tryFinally = new TryFinally(body, finalBody);
			tryFinally.beginLine = tryExcept.beginLine;
			tryFinally.beginColumn = tryExcept.beginColumn;
			addSpecialsAndClearOriginal(tryExcept, tryFinally);
			return tryFinally;

		case JJTWITH_STMT:
			suite = (Suite) stack.popNode();
			arity--;

			exprType asOrExpr = (exprType) stack.popNode();
			arity--;

			exprType expr = null;
			if (arity > 0)
			{
				expr = (exprType) stack.popNode();
				arity--;
			}
			else
			{
				expr = asOrExpr;
				asOrExpr = null;
			}

			suiteType s = new suiteType(suite.body);
			addSpecialsAndClearOriginal(suite, s);

			return new With(expr, asOrExpr, s);
		case JJTWITH_VAR:
			expr = (exprType) stack.popNode(); // expr
			if (expr != null)
			{
				ctx.setStore(expr);
			}
			return expr;
		case JJTEXTRAKEYWORDVALUELIST:
			return new ExtraArgValue(((exprType) stack.popNode()),
			        JJTEXTRAKEYWORDVALUELIST);
		case JJTEXTRAARGVALUELIST:
			return new ExtraArgValue(((exprType) stack.popNode()),
			        JJTEXTRAARGVALUELIST);
		case JJTARGUMENT:
			SimpleNode keyword = stack.popNode();
			if (keyword instanceof keywordType)
			{
				nameTok = makeName(NameTok.KeywordName);
				((keywordType) keyword).arg = nameTok;
			}
			return keyword;
		case JJTKEYWORD:
			value = (exprType) stack.popNode();
			return new keywordType(null, value);
		case JJTTUPLE:
			if (stack.nodeArity() > 0)
			{
				SimpleNode peeked = stack.peekNode();
				if (peeked instanceof ComprehensionCollection)
				{
					ComprehensionCollection col = (ComprehensionCollection) stack
					        .popNode();
					return new ListComp(((exprType) stack.popNode()),
					        col.getGenerators());
				}
			}
			try
			{
				exprType[] exp = makeExprs();
				Tuple t = new Tuple(exp, Tuple.Load);
				addSpecialsAndClearOriginal(n, t);
				return t;
			}
			catch (ClassCastException e)
			{
				if (e.getMessage().equals(ExtraArgValue.class.getName())) { throw new ParseException(
				        "Token: '*' is not expected inside tuples.", lastPop); }
				e.printStackTrace();
				throw new ParseException("Syntax error while detecting tuple.",
				        lastPop);
			}
		case JJTLIST:
			if (stack.nodeArity() > 0
			        && stack.peekNode() instanceof ComprehensionCollection)
			{
				ComprehensionCollection col = (ComprehensionCollection) stack
				        .popNode();
				return new ListComp(((exprType) stack.popNode()),
				        col.getGenerators());
			}
			return new List(makeExprs(), List.Load);
		case JJTSET:
			return new Set(null);
		case JJTDICTIONARY:
			if (arity == 0) { return new Dict(new exprType[0], new exprType[0]); }

			SimpleNode dictNode0 = stack.popNode();

			if (dictNode0 instanceof Set)
			{
				Set set = (Set) dictNode0;
				exprType[] elts = new exprType[arity - 1]; // -1 because the set
														   // was already taken
														   // from there
				for (int i = arity - 2; i >= 0; i--)
				{ // same thing here
					elts[i] = (exprType) stack.popNode();
				}
				set.elts = elts;
				return set;
			}

			SimpleNode dictNode1 = stack.popNode();

			if (dictNode0 instanceof ComprehensionCollection)
			{
				if (arity == 2)
				{
					ComprehensionCollection comp = (ComprehensionCollection) dictNode0;
					return new SetComp((exprType) dictNode1,
					        comp.getGenerators());

				}
				else if (arity == 3)
				{
					ComprehensionCollection comp = (ComprehensionCollection) dictNode0;
					return new DictComp((exprType) stack.popNode(),
					        (exprType) dictNode1, comp.getGenerators());
				}
			}

			l = arity / 2;
			exprType[] keys = new exprType[l];
			exprType[] vals = new exprType[l];
			for (int i = l - 1; i >= 0; i--)
			{
				if (i == l - 1)
				{
					vals[i] = (exprType) dictNode0;
					keys[i] = (exprType) dictNode1;

				}
				else
				{
					vals[i] = (exprType) stack.popNode();
					keys[i] = (exprType) stack.popNode();
				}
			}
			return new Dict(keys, vals);
			// case JJTSTR_1OP: #No more backticks in python 3.0
			// return new Repr(((exprType) stack.popNode()));
		case JJTSTRJOIN:
			Str str2 = (Str) stack.popNode();
			Object o = stack.popNode();
			if (o instanceof Str)
			{
				Str str1 = (Str) o;
				return new StrJoin(new exprType[] { str1, str2 });
			}
			else
			{
				StrJoin strJ = (StrJoin) o;
				exprType[] newStrs = new exprType[strJ.strs.length + 1];
				System.arraycopy(strJ.strs, 0, newStrs, 0, strJ.strs.length);
				newStrs[strJ.strs.length] = str2;
				strJ.strs = newStrs;
				return strJ;
			}
		case JJTTEST:
			if (arity == 2)
			{
				IfExp node = (IfExp) stack.popNode();
				node.body = (exprType) stack.popNode();
				return node;
			}
			else
			{
				return stack.popNode();
			}
		case JJTIF_EXP:
			exprType ifExprOrelse = (exprType) stack.popNode();
			exprType ifExprTest = (exprType) stack.popNode();
			return new IfExp(ifExprTest, null, ifExprOrelse);
		case JJTLAMBDEF_NOCOND:
		case JJTLAMBDEF:
			test = (exprType) stack.popNode();
			arguments = makeArguments(arity - 1);
			Lambda lambda = new Lambda(arguments, test);
			if (arguments == null || arguments.args == null
			        || arguments.args.length == 0)
			{
				lambda.getSpecialsBefore().add("lambda");
			}
			else
			{
				lambda.getSpecialsBefore().add("lambda ");
			}
			return lambda;
		case JJTELLIPSES:
			return new Ellipsis();
		case JJTSLICE:
			SimpleNode[] arr = new SimpleNode[arity];
			for (int i = arity - 1; i >= 0; i--)
			{
				arr[i] = stack.popNode();
			}

			exprType[] values = new exprType[3];
			int k = 0;
			java.util.List<Object> specialsBefore = new ArrayList<Object>();
			java.util.List<Object> specialsAfter = new ArrayList<Object>();
			for (int j = 0; j < arity; j++)
			{
				if (arr[j].getId() == JJTCOLON)
				{
					if (arr[j].specialsBefore != null)
					{
						specialsBefore.addAll(arr[j].specialsBefore);
						arr[j].specialsBefore.clear(); // this nodes may be
													   // reused among parses,
													   // so, we have to erase
													   // the specials
					}
					if (arr[j].specialsAfter != null)
					{
						specialsAfter.addAll(arr[j].specialsAfter);
						arr[j].specialsAfter.clear();
					}
					k++;
				}
				else
				{
					values[k] = (exprType) arr[j];
					if (specialsBefore.size() > 0)
					{
						values[k].getSpecialsBefore().addAll(specialsBefore);
						specialsBefore.clear();
					}
					if (specialsAfter.size() > 0)
					{
						values[k].getSpecialsBefore().addAll(specialsAfter);
						specialsAfter.clear();
					}
				}
			}
			SimpleNode sliceRet;
			if (k == 0)
			{
				sliceRet = new Index(values[0]);
			}
			else
			{
				sliceRet = new Slice(values[0], values[1], values[2]);
			}
			// this may happen if we have no values
			sliceRet.getSpecialsBefore().addAll(specialsBefore);
			sliceRet.getSpecialsAfter().addAll(specialsAfter);
			specialsBefore.clear();
			specialsAfter.clear();
			return sliceRet;
		case JJTCOMP_FOR:
			ComprehensionCollection col = null;
			if (stack.peekNode() instanceof ComprehensionCollection)
			{
				col = (ComprehensionCollection) stack.popNode();
				arity--;
			}
			else
			{
				col = new ComprehensionCollection();
			}

			ArrayList<exprType> ifs = new ArrayList<exprType>();
			for (int i = arity - 3; i >= 0; i--)
			{
				SimpleNode ifsNode = stack.popNode();
				ifs.add((exprType) ifsNode);
			}
			iter = (exprType) stack.popNode();
			target = (exprType) stack.popNode();
			ctx.setStore(target);
			col.added.add(new Comprehension(target, iter, ifs
			        .toArray(new exprType[0])));
			return col;
		case JJTIMPORTFROM:
			ArrayList<aliasType> aliastL = new ArrayList<aliasType>();
			while (arity > 0 && stack.peekNode() instanceof aliasType)
			{
				aliastL.add(0, (aliasType) stack.popNode());
				arity--;
			}
			NameTok nT;
			if (arity > 0)
			{
				nT = makeName(NameTok.ImportModule);
			}
			else
			{
				nT = new NameTok("", NameTok.ImportModule);
			}
			return new ImportFrom((NameTokType) nT,
			        aliastL.toArray(new aliasType[0]), 0);

		default:
			System.out.println("Error at TreeBuilder: default not treated:"
			        + n.getId());
			return null;
		}
	}

	/**
	 * Should only be called from makeArguments
	 */
	private argumentsType __makeArguments(DefaultArg[] def, NameTok varg,
	        NameTok kwarg) throws Exception
	{
		java.util.List<exprType> fpargs = new ArrayList<exprType>();
		java.util.List<exprType> fpargsAnn = new ArrayList<exprType>();
		java.util.List<exprType> fpargsDefaults = new ArrayList<exprType>();

		java.util.List<exprType> kwonlyargs = new ArrayList<exprType>();
		java.util.List<exprType> kwonlyargsAnn = new ArrayList<exprType>();
		java.util.List<exprType> kwonlyargsDefaults = new ArrayList<exprType>();

		for (int i = 0; i < def.length; i++)
		{
			DefaultArg node = def[i];
			exprType parameter = node.parameter;

			if (node.id == JJTONLYKEYWORDARG || node.id == JJTONLYKEYWORDARG2)
			{
				ctx.setKwOnlyParam(parameter);
				kwonlyargs.add(parameter);
				kwonlyargsAnn.add(node.typeDef);
				kwonlyargsDefaults.add(node.value);
			}
			else
			{
				// regular parameter
				ctx.setParam(parameter);
				fpargs.add(parameter);
				fpargsAnn.add(node.typeDef);
				fpargsDefaults.add(node.value);
			}

			if (node.specialsBefore != null && node.specialsBefore.size() > 0)
			{
				parameter.getSpecialsBefore().addAll(node.specialsBefore);
			}
			if (node.specialsAfter != null && node.specialsAfter.size() > 0)
			{
				parameter.getSpecialsAfter().addAll(node.specialsAfter);
			}

		}

		return new argumentsType(fpargs.toArray(new exprType[fpargs.size()]),
		        varg, kwarg, fpargsDefaults.toArray(new exprType[fpargsDefaults
		                .size()]),

		        // new on Python 3.0
		        kwonlyargs.toArray(new exprType[kwonlyargs.size()]),
		        kwonlyargsDefaults.toArray(new exprType[kwonlyargsDefaults
		                .size()]),

		        // annotations
		        fpargsAnn.toArray(new exprType[fpargsAnn.size()]), null, // this
																		 // one
																		 // will
																		 // be
																		 // set
																		 // later
																		 // on
																		 // makeArguments
																		 // (varargannotation)
		        null, // this one will be set later on makeArguments
					  // (kwargannotation)
		        kwonlyargsAnn.toArray(new exprType[kwonlyargsAnn.size()]));

	}

	private argumentsType makeArguments(int l) throws Exception
	{
		NameTok kwarg = null;
		NameTok stararg = null;
		exprType varargannotation = null;
		exprType kwargannotation = null;

		ArrayList<SimpleNode> list = new ArrayList<SimpleNode>();
		for (int i = l - 1; i >= 0; i--)
		{
			SimpleNode popped = null;
			try
			{
				popped = stack.popNode();
				if (popped.getId() == JJTEXTRAKEYWORDLIST)
				{
					ExtraArg node = (ExtraArg) popped;
					kwarg = node.tok;
					kwargannotation = node.typeDef;
					addSpecialsAndClearOriginal(node, kwarg);
				}
				else if (popped.getId() == JJTEXTRAARGLIST)
				{
					ExtraArg node = (ExtraArg) popped;
					stararg = node.tok;
					varargannotation = node.typeDef;
					if (stararg != null)
					{
						// can happen, as in 3.0 we can have a single '*'
						addSpecialsAndClearOriginal(node, stararg);
					}
				}
				else
				{
					list.add((DefaultArg) popped);
				}
			}
			catch (ClassCastException e)
			{
				throw new ParseException("Internal error (ClassCastException):"
				        + e.getMessage() + "\n" + popped, popped);
			}
		}
		Collections.reverse(list);// we get them in reverse order in the stack
		argumentsType arguments = __makeArguments(
		        (DefaultArg[]) list.toArray(new DefaultArg[0]), stararg, kwarg);
		arguments.varargannotation = varargannotation;
		arguments.kwargannotation = kwargannotation;
		return arguments;
	}
}
