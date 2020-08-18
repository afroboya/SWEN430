// This file is part of the WhileLang Compiler (wlc).
//
// The WhileLang Compiler is free software; you can redistribute
// it and/or modify it under the terms of the GNU General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// The WhileLang Compiler is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE. See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public
// License along with the WhileLang Compiler. If not, see
// <http://www.gnu.org/licenses/>
//
// Copyright 2013, David James Pearce.

package whilelang.compiler;

import static whilelang.util.SyntaxError.internalFailure;
import static whilelang.util.SyntaxError.syntaxError;

import java.util.*;

import whilelang.ast.Attribute;
import whilelang.ast.Expr;
import whilelang.ast.Stmt;
import whilelang.ast.Type;
import whilelang.ast.WhileFile;
import whilelang.util.Pair;
import whilelang.util.SyntacticElement;

/**
 * <p>
 * Responsible for ensuring that all types are used appropriately. For example,
 * that we only perform arithmetic operations on arithmetic types; that we only
 * access fields in records guaranteed to have those fields, etc.
 * </p>
 *
 * @author David J. Pearce
 *
 */
public class TypeChecker {
	private WhileFile file;
	private WhileFile.MethodDecl method;
	private HashMap<String,WhileFile.MethodDecl> methods;
	private HashMap<String,WhileFile.TypeDecl> types;

	public void check(WhileFile wf) {
		this.file = wf;
		this.methods = new HashMap<String,WhileFile.MethodDecl>();
		this.types = new HashMap<String,WhileFile.TypeDecl>();

		for(WhileFile.Decl declaration : wf.declarations) {
			if(declaration instanceof WhileFile.MethodDecl) {
				WhileFile.MethodDecl fd = (WhileFile.MethodDecl) declaration;
				this.methods.put(fd.name(), fd);
			} else if(declaration instanceof WhileFile.TypeDecl) {
				WhileFile.TypeDecl fd = (WhileFile.TypeDecl) declaration;
				this.types.put(fd.name(), fd);
			}
		}

		for(WhileFile.Decl declaration : wf.declarations) {
			if(declaration instanceof WhileFile.TypeDecl) {
				check((WhileFile.TypeDecl) declaration);
			} else if(declaration instanceof WhileFile.MethodDecl) {
				check((WhileFile.MethodDecl) declaration);
			}
		}
	}

	public void check(WhileFile.TypeDecl td) {
		checkNotVoid(td.getType(),td.getType());
	}

	public void check(WhileFile.MethodDecl fd) {
		this.method = fd;

		// First, initialise the typing environment
		HashMap<String,Type> environment = new HashMap<String,Type>();
		for (WhileFile.Parameter p : fd.getParameters()) {
			checkNotVoid(p.getType(),p);
			environment.put(p.name(), p.getType());
		}

		// Second, check all statements in the function body
		check(fd.getBody(),environment);
	}

	public void check(List<Stmt> statements, Map<String,Type> environment) {
		for(Stmt s : statements) {
			check(s,environment);
		}
	}

	public void check(Stmt stmt, Map<String,Type> environment) {
		if(stmt instanceof Stmt.Assert) {
			check((Stmt.Assert) stmt, environment);
		} else if(stmt instanceof Stmt.Print) {
			check((Stmt.Print) stmt, environment);
		} else if(stmt instanceof Stmt.Assign) {
			check((Stmt.Assign) stmt, environment);
		} else if(stmt instanceof Stmt.Return) {
			check((Stmt.Return) stmt, environment);
		} else if(stmt instanceof Stmt.Break) {
			// nothing to do
		} else if(stmt instanceof Stmt.Continue) {
			// nothing to do
		} else if(stmt instanceof Stmt.VariableDeclaration) {
			check((Stmt.VariableDeclaration) stmt, environment);
		} else if(stmt instanceof Expr.Invoke) {
			check((Expr.Invoke) stmt, false, environment);
		} else if(stmt instanceof Stmt.IfElse) {
			check((Stmt.IfElse) stmt, environment);
		} else if(stmt instanceof Stmt.For) {
			check((Stmt.For) stmt, environment);
		} else if(stmt instanceof Stmt.While) {
			check((Stmt.While) stmt, environment);
		} else if(stmt instanceof Stmt.Switch) {
			check((Stmt.Switch) stmt, environment);
		} else {
			internalFailure("unknown statement encountered (" + stmt + ")", file.filename,stmt);
		}
	}


	public void check(Stmt.VariableDeclaration stmt, Map<String,Type> environment) {
		if(environment.containsKey(stmt.getName())) {
			syntaxError("TC07", "variable already declared: " + stmt.getName(),
					file.filename, stmt);
		} else if(stmt.getExpr() != null) {
			Type type = check(stmt.getExpr(),environment);
			checkSubtype(stmt.getType(),type,stmt.getExpr());
		}
		environment.put(stmt.getName(), stmt.getType());
	}

	public void check(Stmt.Assert stmt, Map<String,Type> environment) {
		Type t = check(stmt.getExpr(),environment);
		checkInstanceOf(t,stmt.getExpr(),Type.Bool.class);
	}

	public void check(Stmt.Print stmt, Map<String,Type> environment) {
		// Check the expression is valid, but no additional checks are required
		check(stmt.getExpr(),environment);
	}

	public void check(Stmt.Assign stmt, Map<String,Type> environment) {
		Type lhs = check(stmt.getLhs(),environment);
		Type rhs = check(stmt.getRhs(),environment);
		// Make sure the type being assigned is a subtype of the destination
		checkSubtype(lhs,rhs,stmt.getRhs());
	}

	public void check(Stmt.Return stmt, Map<String, Type> environment) {
		if(stmt.getExpr() != null) {
			Type ret = check(stmt.getExpr(),environment);
			// Make sure returned value is subtype of enclosing method's return
			// type
			checkSubtype(method.getRet(),ret,stmt.getExpr());
		} else {
			// Make sure return type is instance of Void
			checkInstanceOf(new Type.Void(),stmt,method.getRet().getClass());
		}
	}

	public void check(Stmt.IfElse stmt, Map<String,Type> environment) {
		Type ct = check(stmt.getCondition(),environment);
		// Make sure condition has bool type
		checkInstanceOf(ct,stmt.getCondition(),Type.Bool.class);
		check(stmt.getTrueBranch(),environment);
		check(stmt.getFalseBranch(),environment);
	}

	public void check(Stmt.For stmt, Map<String,Type> environment) {

		Stmt.VariableDeclaration vd = stmt.getDeclaration();
		check(vd,environment);

		// Clone the environment in order that the loop variable is only scoped
		// for the life of the loop itself.
		environment = new HashMap<String,Type>(environment);
		environment.put(vd.getName(), vd.getType());

		Type ct = check(stmt.getCondition(),environment);
		// Make sure condition has bool type
		checkInstanceOf(ct,stmt.getCondition(),Type.Bool.class);
		check(stmt.getIncrement(),environment);
		check(stmt.getBody(),environment);
	}

	public void check(Stmt.While stmt, Map<String,Type> environment) {
		Type ct = check(stmt.getCondition(),environment);
		// Make sure condition has bool type
		checkInstanceOf(ct,stmt.getCondition(),Type.Bool.class);
		check(stmt.getBody(),environment);
	}

	public void check(Stmt.Switch stmt, Map<String,Type> environment) {
		Type ct = check(stmt.getExpr(),environment);
		// Now, check each case individually
		for(Stmt.Case c : stmt.getCases()) {
			if(!c.isDefault()) {
				Type et = check(c.getValue(),environment);
				checkSubtype(ct,et,c.getValue());
			}
			check(c.getBody(),environment);
		}
	}

	public Type check(Expr expr, Map<String,Type> environment) {
		Type type;

		if(expr instanceof Expr.Binary) {
			type = check((Expr.Binary) expr, environment);
		} else if(expr instanceof Expr.Literal) {
			type = check((Expr.Literal) expr, environment);
		} else if(expr instanceof Expr.IndexOf) {
			type = check((Expr.IndexOf) expr, environment);
		} else if(expr instanceof Expr.Invoke) {
			type = check((Expr.Invoke) expr, true, environment);
		} else if(expr instanceof Expr.ArrayGenerator) {
			type = check((Expr.ArrayGenerator) expr, environment);
		} else if(expr instanceof Expr.ArrayInitialiser) {
			type = check((Expr.ArrayInitialiser) expr, environment);
		} else if(expr instanceof Expr.RecordAccess) {
			type = check((Expr.RecordAccess) expr, environment);
		} else if(expr instanceof Expr.RecordConstructor) {
			type = check((Expr.RecordConstructor) expr, environment);
		} else if(expr instanceof Expr.Unary) {
			type = check((Expr.Unary) expr, environment);
		} else if(expr instanceof Expr.Variable) {
			type = check((Expr.Variable) expr, environment);
		}else if(expr instanceof Expr.Cast) {
			type = check((Expr.Cast) expr, environment);
		}else if(expr instanceof Expr.Is) {
			type = check((Expr.Is) expr, environment);
		}  else {
			internalFailure("unknown expression encountered (" + expr + ")", file.filename,expr);
			return null; // dead code
		}

		// Save the type attribute so that subsequent compiler stages can use it
		// without having to recalculate it from scratch.
		expr.attributes().add(new Attribute.Type(type));

		return type;
	}

	public Type check(Expr.Binary expr, Map<String,Type> environment) {
		Type leftType = check(expr.getLhs(), environment);
		Type rightType = check(expr.getRhs(), environment);

		switch(expr.getOp()) {
		case AND:
		case OR:
			// Check arguments have bool type
			checkInstanceOf(leftType,expr.getLhs(),Type.Bool.class);
			checkInstanceOf(rightType,expr.getRhs(),Type.Bool.class);
			return leftType;
		case ADD:
		case SUB:
		case DIV:
		case MUL:
		case REM:
			// Check arguments have int type
			checkInstanceOf(leftType,expr.getLhs(),Type.Int.class);
			checkInstanceOf(rightType,expr.getRhs(),Type.Int.class);
			return leftType;
		case EQ:
		case NEQ:
			// FIXME: we could do better here by making sure one of the
			// arguments is a subtype of the other.
			return new Type.Bool();
		case LT:
		case LTEQ:
		case GT:
		case GTEQ:
			// Chewck arguments have int type
			checkInstanceOf(leftType,expr.getLhs(),Type.Int.class);
			checkInstanceOf(rightType,expr.getRhs(),Type.Int.class);
			return new Type.Bool();
		default:
			internalFailure("unknown unary expression encountered (" + expr + ")", file.filename,expr);
			return null; // dead code
		}
	}

	public Type check(Expr.Literal expr, Map<String,Type> environment) {
		return typeOf(expr.getValue(),expr);
	}

	public Type check(Expr.IndexOf expr, Map<String, Type> environment) {
		Type srcType = check(expr.getSource(), environment);
		Type indexType = check(expr.getIndex(), environment);
		// Make sure index has integer type
		checkInstanceOf(indexType, expr.getIndex(), Type.Int.class);
		// Check src has array type (of some kind)
		srcType = checkInstanceOf(srcType, expr.getSource(), Type.Array.class);
		return ((Type.Array) srcType).getElement();
	}

	public Type check(Expr.Invoke expr, boolean returnRequired, Map<String,Type> environment) {
		WhileFile.MethodDecl fn = methods.get(expr.getName());
		List<Expr> arguments = expr.getArguments();
		List<WhileFile.Parameter> parameters = fn.getParameters();
		if(arguments.size() != parameters.size()) {
			syntaxError("TC04", "incorrect number of arguments to function",
					file.filename, expr);
		}
		for(int i=0;i!=parameters.size();++i) {
			Type argument = check(arguments.get(i),environment);
			Type parameter = parameters.get(i).getType();
			// Check supplied argument is subtype of declared parameter
			checkSubtype(parameter,argument,arguments.get(i));
		}
		Type returnType = fn.getRet();
		if(returnRequired) {
			checkNotVoid(returnType,fn.getRet());
		}
		return returnType;
	}

	public Type check(Expr.ArrayGenerator expr, Map<String, Type> environment) {
		Type element = check(expr.getValue(), environment);
		Type size = check(expr.getSize(), environment);
		// Check size expression has int type
		checkInstanceOf(size,expr.getSize(),Type.Int.class);
		return new Type.Array(element);
	}

	public Type check(Expr.ArrayInitialiser expr, Map<String, Type> environment) {
		ArrayList<Type> types = new ArrayList<Type>();
		List<Expr> arguments = expr.getArguments();
		for (Expr argument : arguments) {
			types.add(check(argument, environment));
		}
		// Compute Least Upper Bound of element Types
		Type lub = leastUpperBound(types,expr);
		return new Type.Array(lub);
	}

	public Type check(Expr.RecordAccess expr, Map<String, Type> environment) {
		Type srcType = check(expr.getSource(), environment);
		// Check src has record type
		Type.Record recordType = (Type.Record) checkInstanceOf(srcType, expr.getSource(), Type.Record.class);
		for (Pair<Type, String> field : recordType.getFields()) {
			if (field.second().equals(expr.getName())) {
				return field.first();
			}
		}
		// Couldn't find the field!
		syntaxError("TC05", "expected type to contain field: " + expr.getName(), file.filename, expr);
		return null; // deadcode
	}

	public Type check(Expr.RecordConstructor expr, Map<String, Type> environment) {
		List<Pair<String, Expr>> arguments = expr.getFields();
		List<Pair<Type, String>> types = new ArrayList<Pair<Type, String>>();

		for (Pair<String, Expr> p : arguments) {
			Type t = check(p.second(), environment);
			types.add(new Pair<Type, String>(t, p.first()));
		}

		return new Type.Record(types);
	}

	public Type check(Expr.Unary expr, Map<String,Type> environment) {
		Type type = check(expr.getExpr(), environment);
		switch(expr.getOp()) {
		case NEG:
			checkInstanceOf(type,expr.getExpr(),Type.Int.class);
			return type;
		case NOT:
			checkInstanceOf(type,expr.getExpr(),Type.Bool.class);
			return type;
		case LENGTHOF:
			checkInstanceOf(type,expr.getExpr(),Type.Array.class);
			return new Type.Int();
		default:
			internalFailure("unknown unary expression encountered (" + expr + ")", file.filename,expr);
			return null; // dead code
		}
	}

	public Type check(Expr.Variable expr, Map<String, Type> environment) {
		Type type = environment.get(expr.getName());
		if (type == null) {
			syntaxError("TC06", "unknown variable encountered: " + expr.getName(),
					file.filename, expr);
		}
		return type;
	}

	public Type check(Expr.Cast cast_expr, Map<String, Type> environment) {
		Type expr_type = check(cast_expr.getExpr(),environment);
		Type cast_type = cast_expr.getCastType();
		checkSubtype(expr_type,cast_type,cast_expr);
		return cast_type;
	}

	public Type check(Expr.Is expr, Map<String, Type> environment) {
		Type expr_type = check(expr.getExpr(),environment);
		checkSubtype(expr_type,expr.getIsType(),expr);
		return new Type.Bool();
	}

	/**
	 * Determine the type of a constant value
	 *
	 * @param constant
	 * @param elem
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Type typeOf(Object constant, SyntacticElement elem) {
		if(constant == null){
			return new Type.Null();
		}else if (constant instanceof Boolean) {
			return new Type.Bool();
		} else if (constant instanceof Integer) {
			return new Type.Int();
		} else if (constant instanceof Character) {
			return new Type.Int();
		} else if (constant instanceof String) {
			return new Type.Array(new Type.Int());
		} else if (constant instanceof ArrayList) {
			ArrayList<Object> list = (ArrayList<Object>) constant;
			ArrayList<Type> types = new ArrayList<Type>();
			for(Object o : list) {
				types.add(typeOf(o,elem));
			}
			Type lub = leastUpperBound(types,elem);
			return new Type.Array(lub);
		} else if (constant instanceof HashMap) {
			HashMap<String, Object> record = (HashMap<String, Object>) constant;
			ArrayList<Pair<Type, String>> fields = new ArrayList<Pair<Type, String>>();
			// FIXME: there is a known bug here related to the ordering of
			// fields. Specifically, we've lost information about the ordering
			// of fields in the original source file and we are just recreating
			// a random order here.
			for (Map.Entry<String, Object> e : record.entrySet()) {
				Type t = typeOf(e.getValue(), elem);
				fields.add(new Pair<Type, String>(t, e.getKey()));
			}
			return new Type.Record(fields);
		} else {
			internalFailure("unknown constant encountered (" + elem + ")", file.filename, elem);
			return null; // dead code
		}
	}

	private Type leastUpperBound(List<Type> types, SyntacticElement elem) {
		Type lub = new Type.Void();
		for (Type t : types) {
			if (isSubtype(t, lub, elem)) {
				lub = t;
			} else if(!isSubtype(lub, t, elem)) {
				List<String> matches = new ArrayList<>();
				//must be union, not all types in array are same
				for(Map.Entry<String, WhileFile.TypeDecl> e:this.types.entrySet()){
					Type declared_type = e.getValue().getType();
					boolean match = true;
					for (Type arr_elem_type : types) {
						if (!isSubtype(declared_type, arr_elem_type, elem)) {
							match = false;
							break;
						}
					}
					if(match){
						matches.add(e.getKey());
					}
				}
				if(matches.isEmpty()){
					syntaxError("TC01", "expected type " + t + ", found " + lub, file.filename,
							elem);
				}
				if(matches.size() ==1){
					return this.types.get(matches.get(0)).getType();
				}
				//need to figure out which is the most specific match
				Type super_type = this.types.get(matches.get(0)).getType();
				for(int i=1;i<matches.size();i++){
					Type t1 = this.types.get(matches.get(i)).getType();
					if(isSubtype(super_type,t1,elem)){
						super_type = t1;
					}
				}
				return super_type;
			}
		}

		return lub;
	}

	/**
	 * Check that a given type t2 is an instance of of another type t1. This
	 * method is useful for checking that a type is, for example, a List type.
	 *
	 * @param t1
	 * @param type
	 * @param element
	 *            Used for determining where to report syntax errors.
	 * @return
	 */
	public Type checkInstanceOf(Type type,
			SyntacticElement element, Class<?>... instances) {

		if(type instanceof Type.Named) {
			Type.Named tn = (Type.Named) type;
			if (types.containsKey(tn.getName())) {
				Type body = types.get(tn.getName()).getType();
				return checkInstanceOf(body, element, instances);
			} else {
				syntaxError("TC02", "unknown type encountered: " + type, file.filename,
						element);
			}
		}
		for (Class<?> instance : instances) {
			if (instance.isInstance(type)) {
				// This cast is clearly unsafe. It relies on the caller of this
				// method to do the right thing.
				return type;
			}
		}

		// Ok, we're going to fail with an error message. First, let's build up
		// a useful human-readable message.

		String msg = "";
		boolean firstTime = true;
		for (Class<?> instance : instances) {
			if(!firstTime) {
				msg = msg + " or ";
			}
			firstTime=false;

			if (instance.getName().endsWith("Bool")) {
				msg += "bool";
			} else if (instance.getName().endsWith("Char")) {
				msg += "char";
			} else if (instance.getName().endsWith("Int")) {
				msg += "int";
			} else if (instance.getName().endsWith("Strung")) {
				msg += "string";
			} else if (instance.getName().endsWith("Array")) {
				msg += "array";
			} else if (instance.getName().endsWith("Record")) {
				msg += "record";
			}  else if (instance.getName().endsWith("Void")) {
				msg += "void";
			} else {
				internalFailure("unknown type instanceof encountered ("
						+ instance.getName() + ")", file.filename, element);
				return null;
			}
		}

		syntaxError("TC01", "expected instance of " + msg + ", found " + type,
				file.filename, element);
		return null;
	}

	/**
	 * Check that a given type t2 is a subtype of another type t1.
	 *
	 * @param tsuper
	 *            Supertype to check
	 * @param tsub
	 *            Subtype to check
	 * @param element
	 *            Used for determining where to report syntax errors.
	 */
	public void checkSubtype(Type tsuper, Type tsub, SyntacticElement element) {
		if(!isSubtype(tsuper,tsub,element)) {
			syntaxError("TC01", "expected type " + tsuper + ", found " + tsub, file.filename,
					element);
		}
	}

	/**
	 * Check that a given type t2 is a subtype of another type t1.
	 *
	 * @param tsuper
	 *            Supertype to check
	 * @param tsub
	 *            Subtype to check
	 * @param element
	 *            Used for determining where to report syntax errors.
	 */
	public boolean isSubtype(Type tsuper, Type tsub, SyntacticElement element) {
		if (tsub instanceof Type.Void) {
			// OK
			return true;
		}else if (tsuper instanceof Type.Null && tsub instanceof Type.Null) {
			// OK
			return true;
		}else if (tsuper instanceof Type.Bool && tsub instanceof Type.Bool) {
			// OK
			return true;
		} else if (tsuper instanceof Type.Int && tsub instanceof Type.Int) {
			// OK
			return true;
		} else if (tsuper instanceof Type.Array && tsub instanceof Type.Array) {
			Type.Array l1 = (Type.Array) tsuper;
			Type.Array l2 = (Type.Array) tsub;
			// The following is safe because While has value semantics. In a
			// conventional language, like Java, this is not safe because of
			// references.
			return isSubtype(l1.getElement(),l2.getElement(),element);
		} else if (tsuper instanceof Type.Record && tsub instanceof Type.Record) {
			Type.Record r1 = (Type.Record) tsuper;
			Type.Record r2 = (Type.Record) tsub;
			List<Pair<Type,String>> r1Fields = r1.getFields();
			List<Pair<Type,String>> r2Fields = r2.getFields();
			// Implement "width" subtyping
			if(r1Fields.size() > r2Fields.size()) {
				return false;
			} else {
				for(int i=0;i!=r1Fields.size();++i) {
					Pair<Type,String> p1Field = r1Fields.get(i);
					Pair<Type,String> p2Field = r2Fields.get(i);
					if(!isSubtype(p1Field.first(),p2Field.first(),element)) {
						return false;
					} else if (!p1Field.second().equals(p2Field.second())) {
						return false;
					}
				}
				return true;
			}
		} else if (tsuper instanceof Type.Named) {
			Type.Named tn = (Type.Named) tsuper;
			if (types.containsKey(tn.getName())) {
				Type body = types.get(tn.getName()).getType();
				return isSubtype(body, tsub, element);
			} else {
				syntaxError("TC02", "unknown type encountered: " + tsuper, file.filename,
						element);
			}
		} else if (tsub instanceof Type.Named) {
			Type.Named tn = (Type.Named) tsub;
			if (types.containsKey(tn.getName())) {
				Type body = types.get(tn.getName()).getType();
				return isSubtype(tsuper, body, element);
			} else {
				syntaxError("TC02", "unknown type encountered: " + tsub, file.filename,
						element);
			}
		}else if (tsuper instanceof Type.Union||tsub instanceof Type.Union) {
			//important this comes after everything else
			if(tsub instanceof Type.Union && tsuper instanceof Type.Union){
				//then it must contain all the same fields as parent
				Type.Union tsub_union = (Type.Union)tsub;
				Type.Union tsuper_union = (Type.Union)tsuper;
				for(Type t:tsub_union.getType_list()){
					boolean isSub = false;
					for(Type t_super:tsuper_union.getType_list()){
						isSub = isSubtype(t_super,t,element);
						if(isSub){
							break;
						}
					}
					if(!isSub){
						return false;
					}
				}
				return true;
			}else if(tsuper instanceof Type.Union && tsub instanceof Type.Record){
				Type.Record tsub_record = (Type.Record)tsub;
				List<Pair<Type,String>> record_expanded = new ArrayList<>();
				List<Type.Union> record_expanded_union = new ArrayList<>();
				for(Pair<Type,String> p:tsub_record.getFields()){
					Type t = p.first();
					String name = p.second();
					if(t instanceof Type.Union){
						Set<Type> tempUnion = new HashSet<>();
						for(Type local_type:((Type.Union) t).getType_list()){
							Type.Record r = new Type.Record(Collections.singletonList(new Pair<Type, String>(local_type, name)));
							tempUnion.add(r);
						}
						Type.Union u =  new Type.Union(tempUnion);
						record_expanded_union.add(u);
					}else{
						record_expanded.add(p);
					}
				}


				Type.Union expandedUnion =expandUnion((Type.Union) tsuper);

				//check if any of these are direct matches
				for(Type.Union u:record_expanded_union){
					if(isSubtype(expandedUnion,u,element)){
						return true;
					}
				}
				if(record_expanded.isEmpty()){
					return false;
				}
				Type.Record expandedRecord = new Type.Record(record_expanded);
				//no direct matches keep trying
				for(Type t:expandedUnion.getType_list()){
					if(t instanceof Type.Record){
						if(Recordcontains((Type.Record) t,expandedRecord,element)){
							return true;
						}
					}
				}

				return false;


			}else if(tsuper instanceof Type.Union ){
				Type.Union u = (Type.Union)tsuper;
				for(Type t:u.getType_list()){
					if(isSubtype(t,tsub,element)){
						return true;
					}
				}
				return false;
			}else{
				//tsub is of type union
				//tsuper is not, so all its elements must be a subtype of whatever tsub is i.e int|int is subtype of int
				Type.Union tsub_union = (Type.Union)tsub;
				for(Type t:tsub_union.getType_list()){
					if(!isSubtype(tsuper,t,element)){
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}



	private Type.Union expandUnion(Type.Union u){
		HashSet<Type> union_expanded = new HashSet<>();
		for(Type t:u.getType_list()){
			if(t instanceof Type.Named){
				Type.Named n =(Type.Named)t;
				WhileFile.TypeDecl decl = this.types.get(n.getName());
				Type t1 = decl.getType();
				if(t1 instanceof Type.Union){
					Type.Union u1 = expandUnion((Type.Union) t1);
					union_expanded.addAll(u1.getType_list());
				}else {
					union_expanded.add(t1);
				}
			}else{
				union_expanded.add(t);
			}
		}
		return new Type.Union(union_expanded);
	}

	/**
	 * Check if a union contains a pair
	 * @param u
	 * @param p
	 * @param element
	 * @return
	 */
	private boolean Recordcontains(Type.Record superR,Type.Record subR,SyntacticElement element){
		List<Pair<Type,String>> superRFields = superR.getFields();
		List<Pair<Type,String>> subRFields = subR.getFields();
		for(Pair<Type,String> subp:subRFields){
			//if a pair in sub it should exist in super
			boolean match = false;
			for(Pair<Type,String> superp:superRFields){
				if((isSubtype(superp.first(),subp.first(),element) && superp.second().equals(subp.second()))){
					match = true;
				}
			}
			if(!match){
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine whether two given types are euivalent. Identical types are always
	 * equivalent. Furthermore, e.g. "int|null" is equivalent to "null|int".
	 *
	 * @param t1
	 *            first type to compare
	 * @param t2
	 *            second type to compare
	 */
	public boolean equivalent(Type t1, Type t2, SyntacticElement element) {
		return isSubtype(t1,t2,element) && isSubtype(t2,t1,element);
	}

	/**
	 * Check that a given type is not equivalent to void. This is because void
	 * cannot be used in certain situations.
	 *
	 * @param t
	 * @param elemt
	 */
	public void checkNotVoid(Type t, SyntacticElement elem) {
		if(t instanceof Type.Void) {
			syntaxError("TC03", "void type not permitted here",file.filename,elem);
		} else if(t instanceof Type.Record) {
			Type.Record r = (Type.Record) t;
			for(Pair<Type,String> field : r.getFields()) {
				checkNotVoid(field.first(),field.first());
			}
		} else if(t instanceof Type.Array) {
			Type.Array at = (Type.Array) t;
			checkNotVoid(at.getElement(),at.getElement());
		}
	}
}
