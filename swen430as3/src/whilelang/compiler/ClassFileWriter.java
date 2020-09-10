package whilelang.compiler;

import java.io.*;
import java.util.*;

import jasm.attributes.SourceFile;
import jasm.lang.Bytecode;
import jasm.lang.Bytecode.IfMode;
import jasm.lang.ClassFile;
import jasm.lang.JvmType;
import jasm.lang.JvmTypes;
import jasm.lang.Modifier;

import whilelang.ast.*;

import static jasm.lang.Bytecode.InvokeMode.STATIC;
import static jasm.lang.Bytecode.InvokeMode.VIRTUAL;

/**
 * Responsible for translating a While source file into a JVM Class file.
 *
 * @author David J. Pearce
 *
 */
public class ClassFileWriter {
	// Look in the Java Virtual Machine spec for information about this this
	// number
	private static int CLASS_VERSION = 49;

	/**
	 * The Jasm classfile writer to which we will write our compiled While file.
	 * This takes care of lots of the messy bits of working with the JVM.
	 */
	private jasm.io.ClassFileWriter writer;

	/**
	 * Maps each declared type to its body
	 */
	private HashMap<String,Type> declaredTypes;

	/**
	 * Maps each declared method to its JvmType
	 */
	private HashMap<String,JvmType.Function> methodTypes;

	/**
	 * Construct a ClassFileWriter which will compile a given WhileFile into a
	 * JVM class file of the given name.
	 *
	 * @param classFile
	 * @throws FileNotFoundException
	 */
	public ClassFileWriter(String classFile) throws FileNotFoundException {
		writer = new jasm.io.ClassFileWriter(new FileOutputStream(classFile));
		declaredTypes = new HashMap<String,Type>();
		methodTypes = new HashMap<String,JvmType.Function>();
	}

	public void write(WhileFile sourceFile) throws IOException {
		String moduleName = new File(sourceFile.filename).getName().replace(".while","");
		// Modifiers for class
		List<Modifier> modifiers = Arrays.asList(Modifier.ACC_PUBLIC, Modifier.ACC_FINAL);
		// List of interfaces implemented by class
		List<JvmType.Clazz> implemented = new ArrayList<JvmType.Clazz>();
		// Base class for this class
		JvmType.Clazz superClass = JvmTypes.JAVA_LANG_OBJECT;
		// The class name for this class
		JvmType.Clazz owner = new JvmType.Clazz(moduleName);
		// Create the class!
		ClassFile cf = new ClassFile(CLASS_VERSION, owner, superClass, implemented, modifiers);
		// Add an attribute to the generated class file which indicates the
		// source file from which it was generated. This is useful for getting
		// better error messages out of the JVM.
		cf.attributes().add(new SourceFile(sourceFile.filename));
		// Now, we need to write out all methods defined in the WhileFile. We
		// don't need to worry about other forms of declaration though, as they
		// have no meaning on the JVM.
		for(WhileFile.Decl d : sourceFile.declarations) {
			if(d instanceof WhileFile.MethodDecl) {
				ClassFile.Method m = translate((WhileFile.MethodDecl) d, owner);
				cf.methods().add(m);
			} else if(d instanceof WhileFile.TypeDecl) {
				// Add the type to the map of declared types
				WhileFile.TypeDecl td = (WhileFile.TypeDecl) d;
				declaredTypes.put(td.getName(), td.getType());
			}
		}
		// Finally, write the generated classfile to disk
		writer.write(cf);
	}

	/**
	 * Translate a given WhileFile method into a ClassFile method.
	 *
	 * @param decl
	 */
	private ClassFile.Method translate(WhileFile.MethodDecl method, JvmType.Clazz owner) {
		// Modifiers for method
		List<Modifier> modifiers = Arrays.asList(Modifier.ACC_PUBLIC, Modifier.ACC_STATIC);
		// Construct type for method
		JvmType.Function ft = constructMethodType(method);
		// Construct method object
		ClassFile.Method cm = new ClassFile.Method(method.name(), ft, modifiers);
		// Generate bytecodes representing method body
		Context context = new Context(owner,constructMethodEnvironment(method),constructTypeEnvironment(method));
		ArrayList<Bytecode> bytecodes = new ArrayList<Bytecode>();
		translate(method.getBody(),context,bytecodes);
		// Handle methods with missing return statements, as these need a
		// bytecode
		addReturnAsNecessary(method,bytecodes);
		//
		jasm.attributes.Code code = new jasm.attributes.Code(bytecodes, Collections.EMPTY_LIST, cm);
		// Finally, add the jvm Code attribute to this method
		cm.attributes().add(code);
		// Done
		System.out.println("final: "+bytecodes);
		return cm;
	}

	/**
	 * Translate a list of statements in the While language into a series of
	 * bytecodes which implement their behaviour. The result indicates whether
	 * or not execution will fall-through to the next statement after this.
	 *
	 * @param stmts
	 *            The list of statements being translated
	 * @param environment
	 *            The current translation context
	 * @param bytecodes
	 *            The list of bytecodes being accumulated
	 */
	private void translate(List<Stmt> stmts, Context context, List<Bytecode> bytecodes) {
		for(Stmt s : stmts) {
			translate(s,context,bytecodes);
		}
	}

	/**
	 * Translate a given statement in the While language into a series of one of
	 * more bytecodes which implement its behaviour.
	 *
	 * @param stmt
	 *            The statement being translated
	 * @param environment
	 *            The current translation context
	 * @param bytecodes
	 *            The list of bytecodes being accumulated
	 */
	private void translate(Stmt stmt, Context context, List<Bytecode> bytecodes) {
		if(stmt instanceof Stmt.Assert) {
			translate((Stmt.Assert) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.Assign) {
			translate((Stmt.Assign) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.Break) {
			translate((Stmt.Break) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.Continue) {
			translate((Stmt.Continue) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.For) {
			translate((Stmt.For) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.IfElse) {
			translate((Stmt.IfElse) stmt, context, bytecodes);
		} else if(stmt instanceof Expr.Invoke) {
			translate((Expr.Invoke) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.While) {
			translate((Stmt.While) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.Return) {
			translate((Stmt.Return) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.Switch) {
			translate((Stmt.Switch) stmt, context, bytecodes);
		} else if(stmt instanceof Stmt.VariableDeclaration) {
			translate((Stmt.VariableDeclaration) stmt, context, bytecodes);
		} else {
			throw new IllegalArgumentException("Unknown statement encountered: " + stmt);
		}
	}

	private void translate(Stmt.Assert stmt, Context context, List<Bytecode> bytecodes) {
		String label = freshLabel();
		translate(stmt.getExpr(), context, bytecodes);
		bytecodes.add(new Bytecode.If(IfMode.NE, label));
		// If the assertion fails, through runtime exception
		constructObject(JvmTypes.JAVA_LANG_RUNTIMEEXCEPTION, bytecodes);
		bytecodes.add(new Bytecode.Throw());
		bytecodes.add(new Bytecode.Label(label));
	}

	private void translate(Stmt.Assign stmt, Context context, List<Bytecode> bytecodes) {
		// translate assignment
		translateAssignmentHelper(stmt.getLhs(),stmt.getRhs(),context,bytecodes);
	}

	private void translate(Stmt.Break stmt, Context context, List<Bytecode> bytecodes) {
		String label = context.viewMostRecentLoop().get(EXIT_TEXT);
		bytecodes.add(new Bytecode.Goto(label));
	}

	private void translate(Stmt.Continue stmt, Context context, List<Bytecode> bytecodes) {
		String label = context.viewMostRecentLoop().get(INCREMENTAL_TEXT);
		bytecodes.add(new Bytecode.Goto(label));
	}

	private final String CONDITIONAL_TEXT = "conditional";
	private final String INCREMENTAL_TEXT = "incremental";
	private final String EXIT_TEXT = "exit";
	private void translate(Stmt.For stmt, Context context, List<Bytecode> bytecodes) {
		//set up for
		translate(stmt.getDeclaration(),context,bytecodes);

		String trueLabel, exitLabel,conditionLabel,incrementLabel;
		conditionLabel = freshLabel()+"_conditional";
		incrementLabel = freshLabel()+"_increment";
		trueLabel = freshLabel()+"_true";
		exitLabel = freshLabel()+"_exit";

		HashMap<String,String> loop_details = new HashMap<>();
		loop_details.put(CONDITIONAL_TEXT,conditionLabel);
		loop_details.put(INCREMENTAL_TEXT,incrementLabel);
		loop_details.put(EXIT_TEXT,exitLabel);
		context.addLoop(loop_details);

		//check condition
		bytecodes.add(new Bytecode.Label(conditionLabel));
		translate(stmt.getCondition(),context,bytecodes);
		bytecodes.add(new Bytecode.If(IfMode.NE, trueLabel));
		bytecodes.add(new Bytecode.Goto(exitLabel));
		//cond is true, run body and increment
		bytecodes.add(new Bytecode.Label(trueLabel));

		for(Stmt s:stmt.getBody()){
			translate(s,context,bytecodes);
		}
		//increment
		bytecodes.add(new Bytecode.Label(incrementLabel));
		translate(stmt.getIncrement(),context,bytecodes);
		//check cond
		bytecodes.add(new Bytecode.Goto(conditionLabel));

		bytecodes.add(new Bytecode.Label(exitLabel));

		context.removeLoop();
	}

	private Integer convertComparisonOperator(Expr.BOp operator){
		switch (operator){
			case AND:
				return Bytecode.BinOp.AND;
			case OR:
				return Bytecode.BinOp.OR;
			case ADD:
				return Bytecode.BinOp.ADD;
			case SUB:
				return Bytecode.BinOp.SUB;
			case MUL:
				return Bytecode.BinOp.MUL;
			case DIV:
				return Bytecode.BinOp.DIV;
			case REM:
				return Bytecode.BinOp.REM;
			case EQ:
				return Bytecode.IfCmp.EQ;
			case NEQ:
				return Bytecode.IfCmp.NE;
			case LT:
				return Bytecode.IfCmp.LT;
			case LTEQ:
				return Bytecode.IfCmp.LE;
			case GT:
				return Bytecode.IfCmp.GT;
			case GTEQ:
				return Bytecode.IfCmp.GE;
		}
		throw new RuntimeException("Error: got unexpected operator "+operator);
	}

	private void translate(Stmt.IfElse stmt, Context context, List<Bytecode> bytecodes) {
		String trueBranch = freshLabel();
		String exitLabel = freshLabel();
		translate(stmt.getCondition(),context,bytecodes);
		bytecodes.add(new Bytecode.If(IfMode.NE, trueBranch));
		// translate the false branch
		translate(stmt.getFalseBranch(),context,bytecodes);
		if(!allPathsReturn(stmt.getFalseBranch())) {
			bytecodes.add(new Bytecode.Goto(exitLabel));
		}
		// translate true branch
		bytecodes.add(new Bytecode.Label(trueBranch));
		translate(stmt.getTrueBranch(),context,bytecodes);
		bytecodes.add(new Bytecode.Label(exitLabel));
	}

	private void translate(Stmt.While stmt, Context context, List<Bytecode> bytecodes) {

		String trueLabel, exitLabel,conditionLabel;
		conditionLabel = freshLabel()+"_conditional";
		trueLabel = freshLabel()+"_true";
		exitLabel = freshLabel()+"_exit";

		HashMap<String,String> loop_details = new HashMap<>();
		loop_details.put(CONDITIONAL_TEXT,conditionLabel);
		loop_details.put(EXIT_TEXT,exitLabel);
		context.addLoop(loop_details);

		//check condition
		bytecodes.add(new Bytecode.Label(conditionLabel));
		translate(stmt.getCondition(),context,bytecodes);
		bytecodes.add(new Bytecode.If(IfMode.NE, trueLabel));
		bytecodes.add(new Bytecode.Goto(exitLabel));
		//cond is true, run body and increment
		bytecodes.add(new Bytecode.Label(trueLabel));
		for(Stmt s:stmt.getBody()){
			translate(s ,context,bytecodes);
		}
		//check cond
		bytecodes.add(new Bytecode.Goto(conditionLabel));

		bytecodes.add(new Bytecode.Label(exitLabel));

		context.removeLoop();
	}

	private void translate(Stmt.Return stmt, Context context, List<Bytecode> bytecodes) {
		Expr expr = stmt.getExpr();
		if(expr != null) {
			// Determine type of returned expression
			Attribute.Type attr = expr.attribute(Attribute.Type.class);
			// Translate returned expression
			translate(expr,context,bytecodes);
			// Add return bytecode
			bytecodes.add(new Bytecode.Return(toJvmType(attr.type)));
		} else {
			bytecodes.add(new Bytecode.Return(null));
		}
	}

	private void translate(Stmt.Switch stmt, Context context, List<Bytecode> bytecodes) {
		String exitLabel,conditionLabel;
		conditionLabel = freshLabel()+"_conditional";
		exitLabel = freshLabel()+"_exit";

		HashMap<String,String> loop_details = new HashMap<>();
		loop_details.put(CONDITIONAL_TEXT,conditionLabel);
		loop_details.put(EXIT_TEXT,exitLabel);
		context.addLoop(loop_details);

		//assuming expressions is of type Variable or an array or record access. Regardless, it should reduce down to a
		Object value = context.getRegister(stmt.getExpr().toString());

		Attribute.Type attr = stmt.getExpr().attribute(Attribute.Type.class);
		JvmType type = toJvmType(attr.type);


		//Generate labels for each case
		HashMap<Stmt.Case,String> case_to_label = new HashMap<>();
		for(Stmt.Case switch_case:stmt.getCases()){
			String label = freshLabel()+"_"+switch_case.toString();
			case_to_label.put(switch_case,label);
		}

		for(Stmt.Case switch_case:stmt.getCases()){
			String label = case_to_label.get(switch_case);
			//put value on stack
			if(switch_case.isDefault()){
				System.out.println(switch_case.toString());
				bytecodes.add(new Bytecode.Goto(label));
			}else {
				translate(stmt.getExpr(), context, bytecodes);
				translate(switch_case.getValue(), context, bytecodes);
				bytecodes.add(new Bytecode.IfCmp(Bytecode.IfCmp.EQ, type, label));
			}
		}
		//after checking all cases with no break or return, should go to exit
		bytecodes.add(new Bytecode.Goto(context.viewMostRecentLoop().get(EXIT_TEXT)));

		//label: body
		for(Stmt.Case switch_case:stmt.getCases()){
			bytecodes.add(new Bytecode.Label(case_to_label.get(switch_case)));
			translate(switch_case.getBody(),context,bytecodes);
		}

		//any break will go to here
		bytecodes.add(new Bytecode.Label(context.viewMostRecentLoop().get(EXIT_TEXT)));

		context.removeLoop();

	}

	private void translate(Stmt.VariableDeclaration stmt, Context context, List<Bytecode> bytecodes) {
		Expr rhs = stmt.getExpr();
		Attribute.Type attr = rhs.attribute(Attribute.Type.class);
		Type t = attr.type;

		// Declare the variable in the context
		context.declareRegister(stmt.getName(),t);
		//
		if(rhs != null) {
			Expr.LVal lhs = new Expr.Variable(stmt.getName());
			translateAssignmentHelper(lhs,rhs,context,bytecodes);
		}
	}

	/**
	 * Implement an assignment from a given expression to a given lval. This
	 * code is split out because it is used both in translating assignment
	 * statements and variable declarations. In particular, this code is pretty
	 * tricky to get right because it needs to handle cloning of compound data,
	 * and boxing of primitive data (in some cases).
	 *
	 * @param lhs
	 *            Expression being assigned to
	 * @param rhs
	 *            Expression being assigned
	 * @param context
	 *            The current translation context
	 * @param bytecodes
	 *            The list of bytecodes being accumulated
	 */
	private void translateAssignmentHelper(Expr.LVal lhs, Expr rhs, Context context,
			List<Bytecode> bytecodes) {
		// Determine type of assigned expression
		Attribute.Type attr = rhs.attribute(Attribute.Type.class);
		JvmType rhsType = toJvmType(attr.type);
		//
		if (lhs instanceof Expr.Variable) {
			Expr.Variable var = (Expr.Variable) lhs;
			translate(rhs, context, bytecodes);
			int register = context.getRegister(var.getName());
			bytecodes.add(new Bytecode.Store(register, rhsType));
		}else if(lhs instanceof Expr.IndexOf){
			Expr.IndexOf expr = (Expr.IndexOf)lhs;
			//put array on stack
			translate(expr.getSource(),context,bytecodes);

			//index
			translate(expr.getIndex(),context,bytecodes);
			//value
			translate(rhs,context,bytecodes);
			//convert to object
			boxAsNecessary(attr.type,bytecodes);

			JvmType.Function boxMethodType =
					new JvmType.Function(JvmTypes.JAVA_LANG_OBJECT,JvmTypes.INT,JvmTypes.JAVA_LANG_OBJECT);

			//returns element
			bytecodes.add(new Bytecode.Invoke(JAVA_UTIL_ARRAYLIST, "set", boxMethodType,VIRTUAL));

			//pop element returned by set, we do not need it.
			bytecodes.add(new Bytecode.Pop(JvmTypes.JAVA_LANG_BOOLEAN));
			System.out.println("current: "+bytecodes);
		} else {
			throw new IllegalArgumentException("unknown lval encountered: "+lhs.toString());
		}
	}

	/**
	 * Translate a given expression in the While language into a series of one
	 * of more bytecodes which implement its behaviour. The result of the
	 * expression should be left on the top of the stack.
	 *
	 * @param stmts
	 * @param bytecodes
	 */
	private void translate(Expr expr, Context context, List<Bytecode> bytecodes) {
		if(expr instanceof Expr.ArrayGenerator) {
			translate((Expr.ArrayGenerator) expr, context, bytecodes);
		} else if(expr instanceof Expr.ArrayInitialiser) {
			translate((Expr.ArrayInitialiser) expr, context, bytecodes);
		} else if(expr instanceof Expr.Binary) {
			translate((Expr.Binary) expr, context, bytecodes);
		} else if(expr instanceof Expr.Literal) {
			translate((Expr.Literal) expr, context, bytecodes);
		} else if(expr instanceof Expr.IndexOf) {
			translate((Expr.IndexOf) expr, context, bytecodes);
		} else if(expr instanceof Expr.Invoke) {
			translate((Expr.Invoke) expr, context, bytecodes);
		} else if(expr instanceof Expr.RecordAccess) {
			translate((Expr.RecordAccess) expr, context, bytecodes);
		} else if(expr instanceof Expr.RecordConstructor) {
			translate((Expr.RecordConstructor) expr, context, bytecodes);
		} else if(expr instanceof Expr.Unary) {
			translate((Expr.Unary) expr, context, bytecodes);
		} else if(expr instanceof Expr.Variable) {
			translate((Expr.Variable) expr, context, bytecodes);
		} else {
			throw new IllegalArgumentException("Unknown expression encountered: " + expr);
		}
	}

	private void translate(Expr.ArrayGenerator expr, Context context, List<Bytecode> bytecodes) {
		System.out.println("generator");//FIXME whole thing is untested

		// construct array
		constructObject(JAVA_UTIL_ARRAYLIST,bytecodes);
		//store it
		String name = freshLabel()+"_array";
		int array_reg_index = context.declareRegister(name);
		bytecodes.add(new Bytecode.Store(array_reg_index,JAVA_UTIL_ARRAYLIST));


		//declare my own variable for keeping count
		//wont overlap with existing as fresh label and contains char that variables cannot have
		String increment_name = freshLabel()+"!";
		context.declareRegister(increment_name);
		int increment_register = context.getRegister(increment_name);
		//store 0 in increment
		bytecodes.add(new Bytecode.LoadConst(0));
		bytecodes.add(new Bytecode.Store(increment_register, new JvmType.Int()));


		String trueLabel, exitLabel,conditionLabel;
		conditionLabel = freshLabel()+"_conditional";
		trueLabel = freshLabel()+"_true";
		exitLabel = freshLabel()+"_exit";

		//condition start
		bytecodes.add(new Bytecode.Label(conditionLabel));
		translate(expr.getSize(),context,bytecodes);
		bytecodes.add(new Bytecode.Load(increment_register,new JvmType.Int()));
		//check condition
		bytecodes.add(new Bytecode.IfCmp(Bytecode.IfCmp.LT, new JvmType.Int(), trueLabel));
		bytecodes.add(new Bytecode.Goto(exitLabel));
		bytecodes.add(new Bytecode.Label(trueLabel));
		//add item here
		putInArray(expr.getValue(),array_reg_index,context,bytecodes);
		//increment
		new Bytecode.Iinc(increment_register,1);
		//final stuff
		bytecodes.add(new Bytecode.Goto(conditionLabel));
		bytecodes.add(new Bytecode.Label(exitLabel));

		//leave on top of array
		bytecodes.add(new Bytecode.Load(array_reg_index, JAVA_UTIL_ARRAYLIST));
	}

	private void translate(Expr.ArrayInitialiser expr, Context context, List<Bytecode> bytecodes) {
		System.out.println("arr initialiser");
		constructObject(JAVA_UTIL_ARRAYLIST,bytecodes);

		String name = freshLabel()+"_array";
		int reg_index = context.declareRegister(name);
		bytecodes.add(new Bytecode.Store(reg_index,JAVA_UTIL_ARRAYLIST));

		for(Expr e:expr.getArguments()){
			putInArray(e,reg_index,context,bytecodes);
		}
		//leave on top of array
		bytecodes.add(new Bytecode.Load(reg_index, JAVA_UTIL_ARRAYLIST));
	}

	private void putInArray(Expr e,int array_reg_index,Context context,List<Bytecode> bytecodes){
		Attribute.Type attr = e.attribute(Attribute.Type.class);
		Type t = attr.type;
		//get array
		bytecodes.add(new Bytecode.Load(array_reg_index, JAVA_UTIL_ARRAYLIST));
		//put on stack
		translate(e,context,bytecodes);
		//convert to object
		boxAsNecessary(t,bytecodes);
		JvmType.Function boxMethodType =
				new JvmType.Function(new JvmType.Primitive.Bool(), JvmTypes.JAVA_LANG_OBJECT);

		//add object to array
		bytecodes.add(new Bytecode.Invoke(JAVA_UTIL_ARRAYLIST, "add", boxMethodType,VIRTUAL));
		//pop
		bytecodes.add(new Bytecode.Pop(JvmTypes.JAVA_LANG_BOOLEAN));
	}

	private void translateArrCompare(Expr lhs,Expr rhs,Context context,List<Bytecode> bytecodes) {
		//FIXME all untested
		translate(lhs,context,bytecodes);
		//get the load call from end of lhs translate
		Bytecode lhs_arr_load = bytecodes.remove(bytecodes.size()-1);
		translate(rhs,context,bytecodes);
		//put lhs load just before rhs
		bytecodes.add(bytecodes.size()-1,lhs_arr_load);

		//now we have both load calls on top of stack

		//check equality
		JvmType.Function boxMethodType =
				new JvmType.Function(new JvmType.Primitive.Bool(), JvmTypes.JAVA_LANG_OBJECT);

		//returns 0(False) or 1(True)
		bytecodes.add(new Bytecode.Invoke(JAVA_UTIL_ARRAYLIST, "equals", boxMethodType,VIRTUAL));

	}
	private void translate(Expr.Binary expr, Context context, List<Bytecode> bytecodes) {
		Attribute.Type attr = expr.getLhs().attribute(Attribute.Type.class);
		JvmType type = toJvmType(attr.type);

		Expr lhs = expr.getLhs();
		Expr rhs = expr.getRhs();
		if(lhs instanceof Expr.ArrayGenerator || lhs instanceof Expr.ArrayInitialiser || rhs instanceof Expr.ArrayGenerator || rhs instanceof Expr.ArrayInitialiser){
			translateArrCompare(lhs,rhs,context,bytecodes);
			return;
		}else {
			translate(lhs, context, bytecodes);
			translate(rhs, context, bytecodes);
		}

		int binOp = convertComparisonOperator(expr.getOp());
		switch (expr.getOp()) {
			case AND:
			case OR:
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case REM:
				bytecodes.add(new Bytecode.BinOp(binOp, type));
				break;
			case EQ:
			case NEQ:
			case LT:
			case LTEQ:
			case GT:
			case GTEQ:
				String trueLabel = freshLabel();
				String exitLabel = freshLabel();
				bytecodes.add(new Bytecode.IfCmp(binOp, type, trueLabel));
				bytecodes.add(new Bytecode.LoadConst(false));
				bytecodes.add(new Bytecode.Goto(exitLabel));
				bytecodes.add(new Bytecode.Label(trueLabel));
				bytecodes.add(new Bytecode.LoadConst(true));
				bytecodes.add(new Bytecode.Label(exitLabel));
				break;
			default:
				throw new IllegalArgumentException("unknown binary operator encountered");
		}
	}

	private void translate(Expr.Literal expr, Context context, List<Bytecode> bytecodes) {
		Object value = expr.getValue();
		// FIXME: it's possible that the value here is an instanceof List or
		// Map. This indicates a record or array constant, which cannot be
		// passed through to the LoadConst bytecode.
		bytecodes.add(new Bytecode.LoadConst(value));
	}

	private void translate(Expr.IndexOf expr, Context context, List<Bytecode> bytecodes) {
		//getting value out of array

		//put array on stack
		translate(expr.getSource(),context,bytecodes);

		//put index on stack
		translate(expr.getIndex(),context,bytecodes);

		//get element at index
		JvmType.Function boxMethodType =
				new JvmType.Function(JvmTypes.JAVA_LANG_OBJECT,JvmTypes.INT);

		//returns 0(False) or 1(True)
		bytecodes.add(new Bytecode.Invoke(JAVA_UTIL_ARRAYLIST, "get", boxMethodType,VIRTUAL));

		//this should be an array type
		Type.Array arr_type = (Type.Array)context.getType(expr.getSource().toString());
		Type arr_element_type = arr_type.getElement();
		System.out.println("arr_element_type: "+arr_element_type);
		addReadConversion(arr_element_type,bytecodes);

	}

	private void translate(Expr.Invoke expr, Context context, List<Bytecode> bytecodes) {
		JvmType.Function type = methodTypes.get(expr.getName());
		List<Expr> arguments = expr.getArguments();
		for(int i=0;i!=arguments.size();++i) {
			translate(arguments.get(i),context,bytecodes);
		}
		bytecodes.add(new Bytecode.Invoke(context.getEnclosingClass(), expr.getName(), type, STATIC));
	}

	private void translate(Expr.RecordAccess expr, Context context, List<Bytecode> bytecodes) {

	}

	private void translate(Expr.RecordConstructor expr, Context context, List<Bytecode> bytecodes) {

	}

	private void translate(Expr.Unary expr, Context context, List<Bytecode> bytecodes) {
		System.out.println("calling translate expr");
		translate(expr.getExpr(),context,bytecodes);
		switch(expr.getOp()) {
		case NOT: {
			translateNotHelper(bytecodes);
			break;
		}
		case NEG:
			bytecodes.add(new Bytecode.Neg(JvmTypes.INT));
			break;
		case LENGTHOF:
			JvmType.Function boxMethodType =
					new JvmType.Function(JvmTypes.INT);

			//returns 0(False) or 1(True)
			bytecodes.add(new Bytecode.Invoke(JAVA_UTIL_ARRAYLIST, "size", boxMethodType,VIRTUAL));


			//array already on stack, translate call above
			System.out.println("length: "+bytecodes.toString());

			break;
		default:
			throw new IllegalArgumentException("unknown unary operator encountered: "+expr.toString());
		}
	}

	private void translateNotHelper(List<Bytecode> bytecodes) {
		String trueBranch = freshLabel();
		String exitLabel = freshLabel();
		bytecodes.add(new Bytecode.If(IfMode.EQ, trueBranch));
		bytecodes.add(new Bytecode.LoadConst(false));
		bytecodes.add(new Bytecode.Goto(exitLabel));
		bytecodes.add(new Bytecode.Label(trueBranch));
		bytecodes.add(new Bytecode.LoadConst(true));
		bytecodes.add(new Bytecode.Label(exitLabel));
	}

	private void translate(Expr.Variable expr, Context context, List<Bytecode> bytecodes) {
		// Determine type of variable
		Attribute.Type attr = expr.attribute(Attribute.Type.class);
		JvmType type = toJvmType(attr.type);
		int register = context.getRegister(expr.getName());
		bytecodes.add(new Bytecode.Load(register, type));
	}

	/**
	 * This method is responsible for ensuring that the last bytecode in a
	 * method is a return bytecode. This is only necessary (and valid) in the
	 * case of a method which returns void.
	 *
	 * @param body
	 * @param bytecodes
	 */
	private void addReturnAsNecessary(WhileFile.MethodDecl md, List<Bytecode> bytecodes) {
		if(!allPathsReturn(md.getBody())) {
			bytecodes.add(new Bytecode.Return(null));
		}
	}

	/**
	 * Check whether every path through a given statement block ends in a return
	 * or not.  This is helpful in a few places.
	 *
	 * @param stmts
	 * @return
	 */
	private boolean allPathsReturn(List<Stmt> stmts) {
		for(Stmt stmt : stmts) {
			if(allPathsReturn(stmt)) {
				return true;
			}
		}
		return false;
	}

	private boolean allPathsReturn(Stmt stmt) {
		if(stmt instanceof Stmt.IfElse) {
			Stmt.IfElse ife = (Stmt.IfElse) stmt;
			return allPathsReturn(ife.getTrueBranch()) && allPathsReturn(ife.getFalseBranch());
		} else if(stmt instanceof Stmt.Return) {
			return true;
		}
		return false;
	}

	/**
	 * Clone the element on top of the stack, if it is of an appropriate type
	 * (i.e. is not a primitive).
	 *
	 * @param type
	 *            The type of the element on the top of the stack.
	 * @param context
	 *            The current translation context
	 * @param bytecodes
	 *            The list of bytecodes being accumulated
	 */
	private void cloneAsNecessary(JvmType type, List<Bytecode> bytecodes) {
		if(type instanceof JvmType.Primitive || type == JvmTypes.JAVA_LANG_STRING) {
			// no need to do anything in the case of a primitive type
		} else {
			// Invoke the clone function on the datatype in question
			JvmType.Function ft = new JvmType.Function(JvmTypes.JAVA_LANG_OBJECT);
			bytecodes.add(new Bytecode.Invoke((JvmType.Reference) type, "clone", ft, Bytecode.InvokeMode.VIRTUAL));
			bytecodes.add(new Bytecode.CheckCast(type));
		}
	}

	/**
	 * Box the element on top of the stack, if it is of an appropriate type
	 * (i.e. is not a primitive).
	 *
	 * @param from
	 *            The type of the element we are converting from (i.e. on the
	 *            top of the stack).
	 * @param context
	 *            The current translation context
	 * @param bytecodes
	 *            The list of bytecodes being accumulated
	 */
	private void boxAsNecessary(Type from, List<Bytecode> bytecodes) {
		JvmType.Clazz owner;
		JvmType jvmType = toJvmType(from);

		if(jvmType instanceof JvmType.Reference) {
			// Only need to box primitive types
			return;
		} else if(jvmType instanceof JvmType.Bool) {
			owner = JvmTypes.JAVA_LANG_BOOLEAN;
		} else if(jvmType instanceof JvmType.Char) {
			owner = JvmTypes.JAVA_LANG_CHARACTER;
		} else if(jvmType instanceof JvmType.Int) {
			owner = JvmTypes.JAVA_LANG_INTEGER;
		} else {
			throw new IllegalArgumentException("unknown primitive type encountered: " + jvmType);
		}

		String boxMethodName = "valueOf";
		JvmType.Function boxMethodType = new JvmType.Function(owner,jvmType);
		bytecodes.add(new Bytecode.Invoke(owner, boxMethodName, boxMethodType,
				STATIC));
	}

	/**
	 * The element on the top of the stack has been read out of a compound data
	 * structure, such as an ArrayList or HashMap representing an array or
	 * record. This value has type Object, and we want to convert it into its
	 * correct form. At a minimum, this requires casting it into the expected
	 * type. This may also require unboxing the element if it is representing a
	 * primitive type.
	 *
	 * @param to
	 *            The type of the element we are converting to (i.e. that we
	 *            want to be on the top of the stack).
	 * @param bytecodes
	 *            The list of bytecodes being accumulated
	 */
	private void addReadConversion(Type to, List<Bytecode> bytecodes) {
		// First cast to the boxed jvm type
		JvmType.Reference boxedJvmType = toBoxedJvmType(to);
		bytecodes.add(new Bytecode.CheckCast(boxedJvmType));
		// Second, unbox as necessary
		unboxAsNecessary(boxedJvmType,bytecodes);
	}

	/**
	 * Unbox a reference type when appropriate. That is, when it represented a
	 * boxed primitive type.
	 *
	 * @param jvmType
	 * @param bytecodes
	 */
	private void unboxAsNecessary(JvmType.Reference jvmType, List<Bytecode> bytecodes) {
		String unboxMethodName;
		JvmType.Primitive unboxedJvmType;

		if (jvmType.equals(JvmTypes.JAVA_LANG_BOOLEAN)) {
			unboxMethodName = "booleanValue";
			unboxedJvmType = JvmTypes.BOOL;
		} else if (jvmType.equals(JvmTypes.JAVA_LANG_CHARACTER)) {
			unboxMethodName = "charValue";
			unboxedJvmType = JvmTypes.CHAR;
		} else if (jvmType.equals(JvmTypes.JAVA_LANG_INTEGER)) {
			unboxMethodName = "intValue";
			unboxedJvmType = JvmTypes.INT;
		} else {
			return; // not necessary to unbox
		}
		JvmType.Function unboxMethodType = new JvmType.Function(unboxedJvmType);
		bytecodes.add(new Bytecode.Invoke(jvmType, unboxMethodName, unboxMethodType, Bytecode.InvokeMode.VIRTUAL));
	}

	/**
	 * The construct method provides a generic way to construct a Java object
	 * using a default constructor which accepts no arguments.
	 *
	 * @param owner
	 *            The class type to construct
	 * @param bytecodes
	 *            The list of bytecodes being accumulated
	 */
	private void constructObject(JvmType.Clazz owner, List<Bytecode> bytecodes) {
		bytecodes.add(new Bytecode.New(owner));
		bytecodes.add(new Bytecode.Dup(owner));
		JvmType.Function ftype = new JvmType.Function(JvmTypes.VOID, Collections.EMPTY_LIST);
		bytecodes.add(new Bytecode.Invoke(owner, "<init>", ftype, Bytecode.InvokeMode.SPECIAL));
	}

	/**
	 * Construct the JVM function type for this method declaration.
	 *
	 * @param method
	 * @return
	 */
	private JvmType.Function constructMethodType(WhileFile.MethodDecl method) {
		List<JvmType> parameterTypes = new ArrayList<JvmType>();
		// Convert each parameter type
		for(WhileFile.Parameter p : method.getParameters()) {
			JvmType jpt = toJvmType(p.getType());
			parameterTypes.add(jpt);
		}
		// convert the return type
		JvmType returnType = toJvmType(method.getRet());
		//
		System.out.println("return type is: "+returnType);
		JvmType.Function ft = new JvmType.Function(returnType, parameterTypes);
		methodTypes.put(method.getName(), ft);
		return ft;
	}

	/**
	 * Construct an initial context for the given method. In essence, this
	 * just maps every parameter to the corresponding JVM register, as these are
	 * automatically assigned by the JVM when the method is in invoked.
	 *
	 * @param method
	 * @return
	 */
	private Map<String,Integer> constructMethodEnvironment(WhileFile.MethodDecl method) {
		HashMap<String,Integer> environment = new HashMap<String,Integer>();
		int index = 0;
		for(WhileFile.Parameter p : method.getParameters()) {
			environment.put(p.getName(), index++);
		}
		return environment;
	}

	/**
	 * Construct an initial context for the given method. In essence, this
	 * just maps every parameter to the corresponding JVM register, as these are
	 * automatically assigned by the JVM when the method is in invoked.
	 *
	 * @param method
	 * @return
	 */
	private Map<String,Type> constructTypeEnvironment(WhileFile.MethodDecl method) {
		HashMap<String,Type> environment = new HashMap<>();
		int index = 0;
		for(WhileFile.Parameter p : method.getParameters()) {
			environment.put(p.getName(),p.getType());
		}
		return environment;
	}


	/**
	 * Check whether a While type is a primitive or not
	 *
	 * @param type
	 * @return
	 */
	private boolean isPrimitive(Type type) {
		if(type instanceof Type.Record || type instanceof Type.Array) {
			return false;
		} else if(type instanceof Type.Named) {
			Type.Named d = (Type.Named) type;
			return isPrimitive(declaredTypes.get(d.getName()));
		} else {
			return true;
		}
	}

	/**
	 * Get a new label name which has not been used before.
	 *
	 * @return
	 */
	private String freshLabel() {
		return "label" + fresh++;
	}


	private static int fresh = 0;

	/**
	 * Convert a While type into its JVM type.
	 *
	 * @param t
	 * @return
	 */
	private JvmType toJvmType(Type t) {
		if(t instanceof Type.Void) {
			return JvmTypes.VOID;
		} else if(t instanceof Type.Bool) {
			return JvmTypes.BOOL;
		} else if(t instanceof Type.Int) {
			return JvmTypes.INT;
		} else if(t instanceof Type.Named) {
			Type.Named d = (Type.Named) t;
			return toJvmType(declaredTypes.get(d.getName()));
		} else if(t instanceof Type.Array) {
			return JAVA_UTIL_ARRAYLIST;
		} else if(t instanceof Type.Record) {
			return JAVA_UTIL_HASHMAP;
		} else {
			throw new IllegalArgumentException("Unknown type encountered: " + t);
		}
	}

	/**
	 * Convert a While type into its boxed JVM type.
	 *
	 * @param t
	 * @return
	 */
	private JvmType.Reference toBoxedJvmType(Type t) {
		if(t instanceof Type.Bool) {
			return JvmTypes.JAVA_LANG_BOOLEAN;
		} else if(t instanceof Type.Int) {
			return JvmTypes.JAVA_LANG_INTEGER;
		} else if(t instanceof Type.Named) {
			Type.Named d = (Type.Named) t;
			return toBoxedJvmType(declaredTypes.get(d.getName()));
		} else if(t instanceof Type.Array) {
			return JAVA_UTIL_ARRAYLIST;
		} else if(t instanceof Type.Record) {
			return JAVA_UTIL_HASHMAP;
		} else {
			throw new IllegalArgumentException("Unknown type encountered: " + t);
		}
	}

	// A few helpful constants not defined in JvmTypes
	private static final JvmType.Clazz JAVA_UTIL_LIST = new JvmType.Clazz("java.util","List");
	private static final JvmType.Clazz JAVA_UTIL_ARRAYLIST = new JvmType.Clazz("java.util","ArrayList");
	private static final JvmType.Clazz JAVA_UTIL_HASHMAP = new JvmType.Clazz("java.util","HashMap");
	private static final JvmType.Clazz JAVA_UTIL_COLLECTION = new JvmType.Clazz("java.util","Collection");
	private static final JvmType.Clazz JAVA_UTIL_COLLECTIONS = new JvmType.Clazz("java.util","Collections");

	/**
	 * Provides useful contextual information which passed down through the
	 * translation process.
	 *
	 * @author David J. Pearce
	 *
	 */
	private static class Context {
		/**
		 * The type of the enclosing class. This is needed to invoke methods
		 * within the same class.
		 */
		private final JvmType.Clazz enclosingClass;

		/**
		 * Maps each declared variable to a jvm register index
		 */
		private final Map<String,Integer> environment;
		private final Map<String,Type> arr_to_type;

		private final Deque<Map<String,String>> loopManagement;
		JvmType mostRecentType = null;


		public Context(JvmType.Clazz enclosingClass, Map<String,Integer> environment,Map<String,Type> arr_to_type) {
			this.enclosingClass = enclosingClass;
			this.environment = environment;
			this.loopManagement = new ArrayDeque<>();
			this.arr_to_type = arr_to_type;
		}

		public Context(Context context) {
			this.enclosingClass = context.enclosingClass;
			this.environment = new HashMap<String,Integer>(context.environment);
			this.loopManagement = new ArrayDeque<>(context.loopManagement);
			this.arr_to_type = new HashMap<>(context.arr_to_type);
		}

		/**
		 * Get the enclosing class for this translation context.
		 *
		 * @return
		 */
		public JvmType.Clazz getEnclosingClass() {
			return enclosingClass;
		}

		/**
		 * Declare a new variable in the given context. This basically allocated
		 * the given variable to the next available register slot.
		 *
		 * @param var
		 * @param environment
		 * @return
		 */
		public int declareRegister(String var) {
			int register = environment.size();
			environment.put(var, register);
			return register;
		}

		/**
		 * Declare variable with a type
		 * **/
		public int declareRegister(String var,Type type) {
			arr_to_type.put(var,type);
			return declareRegister(var);
		}

		/**
		 * Return the register index associated with a given variable which has
		 * been previously declared.
		 *
		 * @param var
		 * @return
		 */
		public int getRegister(String var) {
			return environment.get(var);
		}

		public Type getType(String var){return arr_to_type.get(var);}

		public void addLoop(HashMap<String,String> loop){
			loopManagement.add(loop);
		}

		public Map<String,String> viewMostRecentLoop(){
			return loopManagement.peek();
		}

		public void removeLoop(){
			loopManagement.poll();
		}

	}



}
