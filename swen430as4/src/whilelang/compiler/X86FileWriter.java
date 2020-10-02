package whilelang.compiler;

import java.util.*;

import jx86.lang.Constant;
import jx86.lang.Instruction;
import jx86.lang.Register;
import jx86.lang.Target;
import jx86.lang.X86File;
import whilelang.ast.Attribute;
import whilelang.ast.Expr;
import whilelang.ast.Expr.LVal;
import whilelang.ast.Stmt;
import whilelang.ast.Type;
import whilelang.ast.WhileFile;
import whilelang.util.Pair;

public class X86FileWriter {

	// ==========================================
	// Fields
	// ==========================================

	/**
	 * Determines the target architecture.
	 */
	private final jx86.lang.Target target;
	/**
	 * Simple constant identifying word size of target architecture in bytes.
	 */
	private final int WORD_SIZE;
	/**
	 * Used for extracting type information during invocations.
	 */
	private HashMap<String, WhileFile.MethodDecl> functions;
	/**
	 * Used for unwrapping types.
	 */
	private HashMap<String, WhileFile.TypeDecl> types;

	// ==========================================
	// Constructors
	// ==========================================

	public X86FileWriter(jx86.lang.Target target) {
		this.target = target;
		// Initialise register heads --- the largest register available in a
		// given family for the target platform.
		HAX = headOfFamily(Register.AX);
		HBX = headOfFamily(Register.BX);
		HCX = headOfFamily(Register.CX);
		HDX = headOfFamily(Register.DX);
		HDI = headOfFamily(Register.DI);
		HSI = headOfFamily(Register.SI);
		HBP = headOfFamily(Register.BP);
		HSP = headOfFamily(Register.SP);
		HIP = headOfFamily(Register.IP);
		// Initialise the default register pool
		REGISTER_POOL = new ArrayList<>();
		REGISTER_POOL.add(HAX);
		REGISTER_POOL.add(HBX);
		REGISTER_POOL.add(HCX);
		REGISTER_POOL.add(HDX);
		REGISTER_POOL.add(HDI);
		REGISTER_POOL.add(HSI);
		// Initial word size constant
		WORD_SIZE = target.widthInBytes();
	}

	// ==========================================
	// Public Build Method
	// ==========================================

	public X86File build(WhileFile wf) {
		X86File.Code code = new X86File.Code();
		X86File.Data data = new X86File.Data();

		this.functions = new HashMap<>();
		this.types = new HashMap<>();

		for (WhileFile.Decl declaration : wf.declarations) {
			if (declaration instanceof WhileFile.MethodDecl) {
				WhileFile.MethodDecl fd = (WhileFile.MethodDecl) declaration;
				this.functions.put(fd.name(), fd);
			} else if (declaration instanceof WhileFile.TypeDecl) {
				WhileFile.TypeDecl fd = (WhileFile.TypeDecl) declaration;
				this.types.put(fd.name(), fd);
			}
		}

		for (WhileFile.Decl d : wf.declarations) {
			if (d instanceof WhileFile.MethodDecl) {
				translate((WhileFile.MethodDecl) d, code, data);
			}
		}

		addMainLauncher(code);

		return new X86File(code, data);
	}

	// ==========================================
	// Build Helpers
	// ==========================================

	/**
	 * Translate a given function declaration into a sequence of assembly language
	 * instructions.
	 *
	 * @param md   Method Declaration to translate.
	 * @param code x86 code section where translation should be added.
	 * @param data x86 data section where constants should be stored.
	 */
	public void translate(WhileFile.MethodDecl md, X86File.Code code, X86File.Data data) {
		List<Instruction> instructions = code.instructions;
		// NOTE: prefix name with "wl_" to avoid potential name clashes with
		// other symbols (e.g. as found in standard library, etc).
		instructions.add(new Instruction.Label("wl_" + md.getName()));
		// Save the old frame pointer. This is necessary because we are about to
		// overwrite the frame pointer with a new one for this method. When this
		// method returns, we need to make sure we restore the frame pointer for
		// the enclosing method.
		instructions.add(new Instruction.Reg(Instruction.RegOp.push, HBP));
		// Create new frame pointer for this function, which is stored in the
		// the HBP register. The frame pointer is a fixed reference point from
		// which we can access local variables.
		instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, HSP, HBP));
		// Create stack frame and ensure every variable has a known location on
		// the stack. Parameters are passed on the stack by the caller. That is,
		// in this relatively simple implementation of While, we do not attempt
		// to pass any parameters via registers (e.g. as in System V ABI).
		HashMap<String, MemoryLocation> localVariables = new HashMap<>();
		assignCallerStackFrame(md, localVariables);
		assignCalleeStackFrame(md, localVariables);
		Context context = new Context(REGISTER_POOL, localVariables, code, data);
		// Create the label for return statements. This is the point where
		// return statements will branch to, so we can avoid repeating the code
		// necessary for restoring the stack.
		context = context.setExitLabel("label" + labelIndex++);
		// Create space for the stack frame, which consists of the local
		// variables. This means that new values can be safely stored in the
		// stack at the position given by HSP. That is, they won't overwrite the
		// local variables for this method.
		int widthOfLocals = determineCalleeStackFrameWidth(md);
		allocateSpaceOnStack(widthOfLocals, context);
		// translate the statements which make up the body of this method.
		translate(md.getBody(), context);
		// Add the return label. This is where any return statements in the
		// While program will jump to.
		instructions.add(new Instruction.Label(context.exitLabel));
		// Restore stack pointer.
		instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, HBP, HSP));
		// Restore old frame pointer.
		instructions.add(new Instruction.Reg(Instruction.RegOp.pop, HBP));
		// Return from procedure.
		instructions.add(new Instruction.Unit(Instruction.UnitOp.ret));
	}

	/**
	 * <p>
	 * Assign every parameter and return declared in a function to an appropriate
	 * amount of space on the stack. These variables will have been preallocated by
	 * the caller and, hence, are located above the frame pointer. The amount of
	 * space required by each variable is determined by from its type.
	 * </p>
	 *
	 * <pre>
	 *       +----------------+
	 *       |  parameter #1  |
	 *       +----------------+
	 *              ...
	 *       +----------------+
	 *       |  parameter #n  |
	 *       +----------------+
	 *       |  return value  |
	 *       +----------------+
	 *       | return address |
	 *       +----------------+
	 *  FP-> | old frame ptr  |
	 *       +----------------+
	 * </pre>
	 * <p>
	 * Here, the parameters and return value are accessed at positive offsets from
	 * the frame pointer (whilst e.g. local variables are accessed at negative
	 * offsets).
	 * </p>
	 *
	 * @param method     Method for which to create the stack frame
	 * @param allocation The mapping of variable names to stack offsets. This will
	 *                   be populated with those offsets for any parameters, returns
	 *                   or declared local variables after this method is called.
	 * @return The number of bytes that should be allocated on the stack to store
	 *         the local variables.
	 */
	public void assignCallerStackFrame(WhileFile.MethodDecl method, Map<String, MemoryLocation> allocation) {
		// First, allocate space for parameters and return value. We need to
		// include one natural word to account for the caller return address.
		int twoWordsWidth = 2 * WORD_SIZE;
		int offset = determineCallerEnvironmentWidth(method) + twoWordsWidth;
		// Second, determine the offset for each parameter in turn
		List<WhileFile.Parameter> fun_params = method.getParameters();
		for (int i = 0; i < fun_params.size(); i++) {
			// invariant: offset >= returnAddressWidth
			WhileFile.Parameter p = fun_params.get(i);
			Type type = unwrap(p.getType());
			offset -= WORD_SIZE;
			MemoryLocation loc = new MemoryLocation(HBP, offset);
			allocation.put(p.getName(), loc);
		}
		// Third, determine the offset for the special return value
		Type returnType = unwrap(method.getRet());
		if (!(returnType instanceof Type.Void)) {
			offset -= WORD_SIZE;
			MemoryLocation loc = new MemoryLocation(HBP, offset);
			allocation.put("$", loc);
		}
	}

	/**
	 * <p>
	 * Assign every local variable declared in a function to an appropriate amount
	 * of space on the stack. These variables remain to be allocated by the callee
	 * and, hence, will be located below the frame pointer. The amount of space
	 * required by each variable is determined by from its type.
	 * </p>
	 *
	 * <pre>
	 *       +----------------+
	 *       | return address |
	 *       +----------------+
	 *  FP-> | old frame ptr  |
	 *       +----------------+
	 *       |  local var #1  |
	 *       +----------------+
	 *              ...
	 *       +----------------+
	 *       |  local var #m  |
	 *       +----------------+
	 * </pre>
	 * <p>
	 * Here, the local variables are accessed at negative offsets from the frame
	 * pointer (whilst e.g. parameters are accessed at positive offsets).
	 * </p>
	 *
	 * @param method     Method for which to create the stack frame
	 * @param allocation The mapping of variable names to stack offsets. This will
	 *                   be populated with those offsets for any parameters, returns
	 *                   or declared local variables after this method is called.
	 */
	private void assignCalleeStackFrame(WhileFile.MethodDecl method, Map<String, MemoryLocation> allocation) {
		// First, we go through and determine the type of all declared
		// variables. During this process if we have two declarations for
		// variables with the same name, we retain the larger type. This
		// guarantees there is enough space for the variable in question.
		HashMap<String, Type> variables = new HashMap<>();
		extractLocalVariableTypes(method.getBody(), variables);
		// The starting offset for local variables must be below the old frame
		// pointer.
		int offset = 0;
		// Now, allocate each variable by descending from current offset.
		for (Map.Entry<String, Type> e : variables.entrySet()) {
			offset -= WORD_SIZE;
			Type type = unwrap(e.getValue());
			MemoryLocation loc = new MemoryLocation(HBP, offset);
			allocation.put(e.getKey(), loc);
		}
	}

	// =================================================================
	// Statements
	// =================================================================

	/**
	 * Translate a list of statements into their corresponding machine code
	 * instructions. Observe that we implicitly assume all registers are available
	 * for use between statements.
	 *
	 * @param statements List of statements to be translated.
	 * @param context    The enclosing context
	 */
	public void translate(List<Stmt> statements, Context context) {
		for (Stmt statement : statements) {
			translate(statement, context);
		}
	}

	/**
	 * Translate a given statement into its corresponding corresponding machine code
	 * instructions. Observe that we implicitly assume all registers are available
	 * for use between statements.
	 *
	 * @param statement Statement to be translated
	 */
	public void translate(Stmt statement, Context context) {
		if (statement instanceof Stmt.Assert) {
			translateAssert((Stmt.Assert) statement, context);
		} else if (statement instanceof Stmt.Assign) {
			translateAssign((Stmt.Assign) statement, context);
		} else if (statement instanceof Stmt.Break) {
			translateBreak((Stmt.Break) statement, context);
		} else if (statement instanceof Stmt.Continue) {
			translateContinue((Stmt.Continue) statement, context);
		} else if (statement instanceof Stmt.For) {
			translateFor((Stmt.For) statement, context);
		} else if (statement instanceof Stmt.IfElse) {
			translateIfElse((Stmt.IfElse) statement, context);
		} else if (statement instanceof Expr.Invoke) {
			// Here, we have an invocation expression being treated as a
			// statement. This means that the return value from the method is
			// being ignored. Therefore, we translate this as an expression
			// which writes the result into some register. But, we then just
			// ignore this value and it'll get overwritten later on.
			translateInvoke((Expr.Invoke) statement, null, context);
		} else if (statement instanceof Stmt.Return) {
			translateReturn((Stmt.Return) statement, context);
		} else if (statement instanceof Stmt.VariableDeclaration) {
			translateVariableDeclaration((Stmt.VariableDeclaration) statement, context);
		} else if (statement instanceof Stmt.While) {
			translateWhile((Stmt.While) statement, context);
		} else if (statement instanceof Stmt.Switch) {
			translateSwitch((Stmt.Switch) statement, context);
		} else {
			throw new IllegalArgumentException("Unknown statement encountered: " + statement);
		}
	}

	/**
	 * Translate an assert statement in the While language into a sequence of
	 * machine instructions. This is done by calling the "assertion" method defined
	 * in the runtime.c library which, in turn, uses the standard C "assert" macro
	 * to actually do the work of checking the assertion and throwing an appropriate
	 * error if it fails.
	 *
	 * @param statement
	 * @param localVariables
	 * @param code
	 * @param data
	 */
	public void translateAssert(Stmt.Assert statement, Context context) {
		// We know the following must be a register location because the
		// condition returns a boolean which alwasy fits in a register.
		RegisterLocation loc = (RegisterLocation) allocateLocation(statement.getExpr(), context);
		// Translate the asserted expression and load result into target
		// register
		translate(statement.getExpr(), loc, context);
		// Now, generate a call to the assertion() function runtime.c
		makeExternalMethodCall("assertion", context, null, loc.register);
	}

	/**
	 * Translate an assignment statement in the While language into a sequence of
	 * machine instructions. The translation depends on the form of the left-hand
	 * side. That is, whether we are assigning to a variable directly, or into a
	 * compound value (e.g. a record or array).
	 *
	 * @param statement
	 */
	public void translateAssign(Stmt.Assign statement, Context context) {
		Expr lhs = statement.getLhs();
		// Translate assignment from HDI to left-hand side
		if (lhs instanceof Expr.Variable) {
			Expr.Variable v = (Expr.Variable) lhs;
			// Determine the offset within the stack of this local variable.
			MemoryLocation loc = context.getVariableLocation(v.getName());
			// Translate right-hand side and load result into variable location.
			translate(statement.getRhs(), loc, context);
		} else {
			// Translate lval
			MemoryLocation payloadField = translate((LVal) lhs, context);
			// Lock location to prevent being overwritten
			context = context.lockLocation(payloadField);
			// Translate right-hand side
			translate(statement.getRhs(), payloadField, context);
		}
	}

	/**
	 * Translate a break statement. This performs an unconditional jump to the break
	 * destination.
	 *
	 * @param statement
	 * @param context
	 */
	public void translateBreak(Stmt.Break statement, Context context) {
		// DONEFIXME: you will need to implement this!
		List<Instruction> instructions = context.instructions();
		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, context.viewRecentBreak()));
	}

	/**
	 * Translate a continue statement. This performs an unconditional jump to the continue
	 * destination.
	 *
	 * @param statement
	 * @param context
	 */
	public void translateContinue(Stmt.Continue statement, Context context) {
		// DONEFIXME: you will need to implement this!
		List<Instruction> instructions = context.instructions();
		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, context.viewRecentContinue()));
	}

	/**
	 * Translate a for statement in the While language to a sequence of machine
	 * instructions.
	 *
	 * @param statement
	 */
	public void translateFor(Stmt.For statement, Context context) {
		List<Instruction> instructions = context.instructions();
		// Translate Variable Declaration
		translateVariableDeclaration(statement.getDeclaration(), context);
		// Construct label for top of loop

		String headerLabel = freshLabel();
		String continueLabel = context.generateContinueLabel();
		// Construct break label for exit of loop
		String breakLabel = context.generateBreakLabel();

		// Start loop, and translate condition
		instructions.add(new Instruction.Label(headerLabel));
		// Translate the condition expression and branch to the false label
		translateCondition(statement.getCondition(), breakLabel, context);
		// Translate Loop Body
		translate(statement.getBody(), context);

		instructions.add(new Instruction.Label(continueLabel));
		// Translate Increment and loop around
		translate(statement.getIncrement(), context);
		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, headerLabel));
		// Exit ...
		instructions.add(new Instruction.Label(breakLabel));

		//remove labels
		context.getRecentBreak();
		context.getRecentContinue();
	}

	/**
	 * Translate an if statement in the While language to a sequence of machine
	 * instructions.
	 *
	 * @param statement
	 * @param localVariables
	 * @param code
	 * @param data
	 */
	public void translateIfElse(Stmt.IfElse statement, Context context) {
		List<Instruction> instructions = context.instructions();
		boolean hasFalseBranch = statement.getFalseBranch().size() > 0;
		String exitLabel = freshLabel();
		String falseLabel = hasFalseBranch ? freshLabel() : exitLabel;
		// Translate the condition expression and branch to the false label
		translateCondition(statement.getCondition(), falseLabel, context);
		// Translate true branch
		translate(statement.getTrueBranch(), context);
		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, exitLabel));
		// Translate false branch (if applicable)
		if (hasFalseBranch) {
			instructions.add(new Instruction.Label(falseLabel));
			translate(statement.getFalseBranch(), context);
		}
		// done
		instructions.add(new Instruction.Label(exitLabel));
	}

	/**
	 * <p>
	 * Translate a return statement in the While language to a sequence of machine
	 * instructions. Although there is a machine instruction for returning from a
	 * method, we don't use that here. The reason is that, before returning from the
	 * method, we must restore the stack frame as it was when the method was called.
	 * The code for doing this is generated at the end of the method itself and,
	 * therefore, we simply branch to that point rather than repeating it here.
	 * </p>
	 * <p>
	 * In the case of a return value being supplied, we must write that into the
	 * appropriate place in the stack frame. This has been given a pretend variable
	 * name, "$", which we can simply read out from the local variables map.
	 * </p>
	 *
	 * @param statement
	 * @param localVariables
	 * @param code
	 * @param data
	 */
	public void translateReturn(Stmt.Return statement, Context context) {
		List<Instruction> instructions = context.instructions();
		Expr rv = statement.getExpr();
		// Handle return values (if applicable)
		if (rv != null) {
			// Determine the offset within the stack of this local variable.
			MemoryLocation loc = context.getVariableLocation("$");
			// Translate right-hand side and load into variable location
			translate(rv, loc, context);
		}
		// Finally, we branch to the end of the function where the code
		// necessary for restoring the stack is located.
		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, context.exitLabel()));
	}

	/**
	 * Translate a variable declaration in the While language to a sequence of
	 * machine instructions. This will only actually correspond to any instructions
	 * if the declaration includes an initialiser. In such case, the generated code
	 * is the same as for a simple assignment statement.
	 *
	 * @param statement
	 * @param localVariables
	 * @param code
	 * @param data
	 */
	public void translateVariableDeclaration(Stmt.VariableDeclaration statement, Context context) {
		Expr initialiser = statement.getExpr();

		if (initialiser != null) {
			// Determine the offset within the stack of this local variable.
			MemoryLocation loc = context.getVariableLocation(statement.getName());
			// Translate the right-hand side and load result into target
			// register
			translate(initialiser, loc, context);
		}
	}

	/**
	 * Translate a while statement in the While language to a sequence of machine
	 * instructions.
	 *
	 * @param statement
	 * @param localVariables
	 * @param code
	 * @param data
	 */
	public void translateWhile(Stmt.While statement, Context context) {
		List<Instruction> instructions = context.instructions();

		// Construct label for top of loop
		String headerLabel = context.generateContinueLabel();
		// Construct break label for exit of loop
		String breakLabel = context.generateBreakLabel();
		// Start loop, and translate condition
		instructions.add(new Instruction.Label(headerLabel));
		// Translate the condition expression and branch to the false   label
		translateCondition(statement.getCondition(), breakLabel, context);
		// Translate Loop Body
		translate(statement.getBody(), context);

		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, headerLabel));
		// Exit ...
		instructions.add(new Instruction.Label(breakLabel));

		//remove labels
		context.getRecentBreak();
		context.getRecentContinue();
	}

	/**
	 * Translate a switch statement in the While language to a sequence of machine
	 * instructions.
	 *
	 * @param statement
	 * @param localVariables
	 * @param code
	 * @param data
	 */
	public void translateSwitch(Stmt.Switch statement, Context context) {
		List<Instruction> instructions = context.instructions();
		// Allocate two temporary locations for equality comparisons
		RegisterLocation[] tmps = allocateRegisterLocations(context, statement.getExpr(), statement.getExpr());
		// The exit label will represent the exit point from the switch
		// statement. Any cases which end in a break will branch to it.
		String exitLabel = context.generateBreakLabel();
		// Translate the expression we are switching on, and place result
		// into the target register.
		translate(statement.getExpr(), tmps[0], context);
		// Lock the target register here. This is necessary because we will only
		// evaluate this once, and we want to retain its value across each of
		// the comparisons needed for the cases.
		context = context.lockLocation(tmps[0]);
		// Translate each of the case blocks. For simplicity, we're just
		// going to use a chain of conditional branches. This is not optimal,
		// and it would be better to use a jump table in situations where we can
		// (e.g. for integer values). However, in the general case (e.g. when
		// switching on records), we cannot use a jump table anyway.

		String nextBody = freshLabel();

		for (int i=0;i<statement.getCases().size();i++) {
			Stmt.Case c = statement.getCases().get(i);
			String nextLabel = freshLabel();
			Expr constant = c.getValue();
			if (constant != null) {
				//add missing attributes
				Attribute.Type attr = statement.getExpr().attribute(Attribute.Type.class);
				c.getValue().attributes().add(attr);
				// Not a default block
				translate(c.getValue(), tmps[1], context);
				Type expr_type = unwrap(statement.getExpr().attribute(Attribute.Type.class).type);

				// DONEFIXME: above will not work for arrays or records!
				if (isPrimitive(expr_type)) {
					// Perform a bitwise comparison of the two data chunks
					bitwiseEquality(false, tmps[0], tmps[1], nextLabel, context);
				}else{
					//swap around as left is locked
					compoundEquality(false,tmps[1],tmps[0],nextLabel,context);
				}

			}
			//DONEFIXME: need to handle break and continue statements!
			instructions.add(new Instruction.Label(nextBody));
			translate(c.getBody(), context);
			//if we are here then cond is true, skip next cond and go straight to body
			//default should always be at the end of list so default check is slightly redundant
			if (!c.isDefault() && (i+1)<statement.getCases().size()) {
				nextBody = freshLabel();
				instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, nextBody));
			}

			instructions.add(new Instruction.Label(nextLabel));


		}
		// Finally, add the exit label
		instructions.add(new Instruction.Label(exitLabel));

		context.getRecentBreak();

	}

	// =================================================================
	// Conditions
	// =================================================================

	/**
	 * Translate a condition expression and, if it is false, branch to a given false
	 * destination. Otherwise, execution continues to the following instruction.
	 *
	 * @param e          A binary relational expression
	 * @param falseLabel The branch destination for the case the equality does not
	 *                   hold.
	 */
	public void translateCondition(Expr e, String falseLabel, Context context) {
		//
		if (e instanceof Expr.Unary) {
			translateLogicalNotCondition((Expr.Unary) e, falseLabel, context);
		} else if (e instanceof Expr.Binary) {
			Expr.Binary b = (Expr.Binary) e;
			switch (b.getOp()) {
			case AND:
					translateCondition(b.getLhs(),falseLabel,context);
					translateCondition(b.getRhs(),falseLabel,context);
					break;
			case OR:
				translateShortCircuitDisjunctCondition((Expr.Binary) e, falseLabel, context);
				break;
			case EQ:
			case NEQ:
				translateEqualityCondition((Expr.Binary) e, falseLabel, context);
				break;
			case LT:
			case LTEQ:
			case GTEQ:
			case GT:
				translateRelationalCondition((Expr.Binary) e, falseLabel, context);
				break;
			default:
				throw new IllegalArgumentException("invalid binary condition");
			}
		} else {
			List<Instruction> instructions = context.instructions();
			// Need to deal with the general case here. For example, the
			// expression could be a variable or field load or the result of a
			// method invocation. Eitherway, we do know that the result will fit
			// into a register.
			RegisterLocation loc = (RegisterLocation) allocateLocation(e, context);
			//
			translate(e, loc, context);
			//
			instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.cmp, 0, loc.register));
			instructions.add(new Instruction.Addr(Instruction.AddrOp.jz, falseLabel));
		}
	}

	/**
	 * Translate a logical NOT expression (i.e. '!').
	 *
	 * @param e          A binary logical expression
	 * @param falseLabel The branch destination for the case the equality does not
	 *                   hold.
	 */
	public void translateLogicalNotCondition(Expr.Unary e, String falseLabel, Context context) {
		List<Instruction> instructions = context.instructions();
		if (e.getOp() != Expr.UOp.NOT) {
			throw new IllegalArgumentException("invalid unary condition");
		}
		// What we do here, is redirect the branching. So, the subcondition ends
		// up branching to the falseLabel if it evaluates to true, and
		// vice-versa.
		String trueLabel = freshLabel();
		translateCondition(e.getExpr(), trueLabel, context);
		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, falseLabel));
		instructions.add(new Instruction.Label(trueLabel));
	}

	/**
	 * Translate a logical OR expression (i.e. '||'). This supports short-circuiting
	 * evaluation.
	 *
	 * @param e          A binary logical expression
	 * @param falseLabel The branch destination for the case the equality does not
	 *                   hold.
	 */
	public void translateShortCircuitDisjunctCondition(Expr.Binary e, String falseLabel, Context context) {
		List<Instruction> instructions = context.instructions();
		String nextLabel = freshLabel();
		String exitLabel = freshLabel();
		translateCondition(e.getLhs(), nextLabel, context);
		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, exitLabel));
		instructions.add(new Instruction.Label(nextLabel));
		translateCondition(e.getRhs(), falseLabel, context);
		instructions.add(new Instruction.Label(exitLabel));
	}

	/**
	 * Translate one of the four relational comparators (i.e. <,<=,>= and >). These
	 * comparators are relatively straightforward because they can only be applied
	 * to integer (i.e. primitive) values.
	 *
	 * @param e          A binary relational expression
	 * @param falseLabel The branch destination for the case the equality does not
	 *                   hold.
	 */
	public void translateRelationalCondition(Expr.Binary e, String falseLabel, Context context) {
		List<Instruction> instructions = context.instructions();
		// Translate left-hand side (which must be a register location)
		RegisterLocation[] tmps = allocateRegisterLocations(context, e.getLhs(), e.getRhs());
		translate(e.getLhs(), tmps[0], context);
		context = context.lockLocation(tmps[0]);
		// Translate right-hand side (which must be a register location)
		translate(e.getRhs(), tmps[1], context);
		// NOTE: it is not a mistake that rhs is in the left operand
		// position. This is because we're in AT&T syntax!
		Register lhsReg = tmps[0].register;
		Register rhsReg = tmps[1].register;
		instructions.add(new Instruction.RegReg(Instruction.RegRegOp.cmp, rhsReg, lhsReg));
		//
		switch (e.getOp()) {
		case LT:
			instructions.add(new Instruction.Addr(Instruction.AddrOp.jge, falseLabel));
			break;
		case LTEQ:
			instructions.add(new Instruction.Addr(Instruction.AddrOp.jg, falseLabel));
			break;
		case GT:
			instructions.add(new Instruction.Addr(Instruction.AddrOp.jle, falseLabel));
			break;
		case GTEQ:
			instructions.add(new Instruction.Addr(Instruction.AddrOp.jl, falseLabel));
			break;
		default:
			throw new IllegalArgumentException("Unknown binary operator: " + e);
		}
	}

	/**
	 * Translate the equality comparator for values of a given type. In the case the
	 * equality holds, control will continue to the next instruction in sequence.
	 * Otherwise, it will branch to a given false destination.
	 *
	 * @param type       The type of values being compared.
	 * @param falseLabel The branch destination for the case the equality does not
	 *                   hold.
	 */
	public void translateEqualityCondition(Expr.Binary e, String falseLabel, Context context) {
		List<Instruction> instructions = context.instructions();
		Type lhs_t = unwrap(e.getLhs().attribute(Attribute.Type.class).type);
		Type rhs_t = unwrap(e.getRhs().attribute(Attribute.Type.class).type);
		// Translate left-hand side
		RegisterLocation[] tmps = allocateRegisterLocations(context, e.getLhs(), e.getRhs());
		translate(e.getLhs(), tmps[0], context);
		// Lock left-hand side to prevent being overwritten
		context = context.lockLocation(tmps[0]);
		// Translate right-hand side
		translate(e.getRhs(), tmps[1], context);
		// Release left-hand side
		context = context.unlockLocation(tmps[0]);
		//
		if (isPrimitive(lhs_t) && isPrimitive(rhs_t)) {
			// Perform a bitwise comparison of the two data chunks
			bitwiseEquality(e.getOp() != Expr.BOp.EQ, tmps[0], tmps[1], falseLabel, context);
		}else{
			// DONEFIXME: above will not work for arrays or records!
			compoundEquality((e.getOp() != Expr.BOp.EQ),tmps[0],tmps[1],falseLabel,context);
		}
	}

	// =================================================================
	// LVals
	// =================================================================

	/**
	 * Translate a given lval expression into the corresponding machine code
	 * instructions. This returns a reference to the location which should be
	 * written. In the case of compound structures, this requires cloning them as
	 * necessary to ensure the correct semantics.
	 *
	 * @param lval
	 * @param context
	 */
	public MemoryLocation translate(LVal lval, Context context) {
		if (lval instanceof Expr.Variable) {
			Expr.Variable v = (Expr.Variable) lval;
			// Determine the offset within the stack of this local variable.
			return context.getVariableLocation(v.getName());
		} else if (lval instanceof Expr.RecordAccess) {
			return translateRecordLVal((Expr.RecordAccess) lval, context);
		} else {
			return translateArrayLVal((Expr.IndexOf) lval, context);
		}
	}

	public MemoryLocation translateRecordLVal(Expr.RecordAccess lval, Context context) {
		// Translate lval source
		MemoryLocation src = translate((LVal) lval.getSource(), context);
		// Construct register base pointer
		RegisterLocation base = context.tryAndLockLocation(src).selectFreeRegister();
		bitwiseCopy(src, base, context);
		// Determine type of field being assigned
		Type.Record type = (Type.Record) unwrap(lval.getSource().attribute(Attribute.Type.class).type);
		// Create payload location as relative offset from base register
		return new MemoryLocation(base.register, getFieldOffset(type, lval.getName()));
	}

	public MemoryLocation translateArrayLVal(Expr.IndexOf lval, Context context) {
		//FIXME all of this is from  translateIndexOf it is probably wrong
		List<Instruction> instructions = context.instructions();
		// Translate source expression into a temporary register. In other words, store
		// the records heap address into a temporary.
		RegisterLocation base = context.selectFreeRegister();
		translate(lval.getSource(), base, context);
		// Lock base register to protected it
		context = context.lockLocation(base);
		// Translate index expression into another temporary register
		RegisterLocation index = context.selectFreeRegister();
		translate(lval.getIndex(), index, context);
		// Multiply index by WORD_SIZE
		multiplyByWordSize(index.register, context);
		// Add length field
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.add, WORD_SIZE, index.register));
		// Add index onto base register.
		instructions.add(new Instruction.RegReg(Instruction.RegRegOp.add, index.register, base.register));
		// Finally, copy bits into target location
		return new MemoryLocation(base.register, 0);
	}

	// =================================================================
	// Expressions
	// =================================================================

	/**
	 * Translate a given expression into the corresponding machine code
	 * instructions. The expression is expected to return its result in the target
	 * register or, if that is null, on the stack. The set of free registers is
	 * provided to identify the pool from which target registers can be taken.
	 *
	 * @param expression Expression to be translated.
	 * @param target     Location to store result in.
	 */
	public void translate(Expr expression, Location target, Context context) {
		if (expression instanceof Expr.ArrayGenerator) {
			translateArrayGenerator((Expr.ArrayGenerator) expression, target, context);
		} else if (expression instanceof Expr.ArrayInitialiser) {
			translateArrayInitialiser((Expr.ArrayInitialiser) expression, target, context);
		} else if (expression instanceof Expr.Binary) {
			if (target instanceof RegisterLocation) {
				translateBinary((Expr.Binary) expression, (RegisterLocation) target, context);
			} else {
				translateViaRegister(expression, (MemoryLocation) target, context);
			}
		} else if (expression instanceof Expr.IndexOf) {
			translateIndexOf((Expr.IndexOf) expression, target, context);
		} else if (expression instanceof Expr.Invoke) {
			translateInvoke((Expr.Invoke) expression, target, context);
		} else if (expression instanceof Expr.Literal) {
			translateLiteral((Expr.Literal) expression, target, context);
		} else if (expression instanceof Expr.RecordAccess) {
			translateRecordAccess((Expr.RecordAccess) expression, target, context);
		} else if (expression instanceof Expr.RecordConstructor) {
			translateRecordConstructor((Expr.RecordConstructor) expression, target, context);
		} else if (expression instanceof Expr.Unary) {
			if (target instanceof RegisterLocation) {
				translateUnary((Expr.Unary) expression, (RegisterLocation) target, context);
			} else {
				translateViaRegister(expression, (MemoryLocation) target, context);
			}
		} else if (expression instanceof Expr.Variable) {
			translateVariable((Expr.Variable) expression, target, context);
		} else {
			throw new IllegalArgumentException("Unknown expression encountered: " + expression);
		}
	}

	/**
	 * Translate a given expression via a register target. Thus, the expression is
	 * first loaded into a temporary register and then bit-blasted into the target
	 * location.
	 *
	 * @param e       Expression to be translate
	 * @param target  target location in memory
	 * @param context
	 */
	public void translateViaRegister(Expr e, MemoryLocation target, Context context) {
		MemoryLocation tloc = target;
		RegisterLocation rloc = context.selectFreeRegister();
		translate(e, rloc, context);
		bitwiseCopy(rloc, tloc, context);
	}

	/**
	 * Translate an array access operation into the corresponding machine code
	 * instructions.
	 *
	 * @param e
	 * @param target
	 * @param context
	 */
	public void translateIndexOf(Expr.IndexOf e, Location target, Context context) {
		List<Instruction> instructions = context.instructions();
		// Translate source expression into a temporary register. In other words, store
		// the records heap address into a temporary.
		RegisterLocation base = context.selectFreeRegister();
		translate(e.getSource(), base, context);
		// Lock base register to protected it
		context = context.lockLocation(base);
		// Translate index expression into another temporary register
		RegisterLocation index = context.selectFreeRegister();
		translate(e.getIndex(), index, context);
		// Multiply index by WORD_SIZE
		multiplyByWordSize(index.register, context);
		// Add length field
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.add, WORD_SIZE, index.register));
		// Add index onto base register.
		instructions.add(new Instruction.RegReg(Instruction.RegRegOp.add, index.register, base.register));
		// Finally, copy bits into target location
		MemoryLocation elementLocation = new MemoryLocation(base.register, 0);
		bitwiseCopy(elementLocation, target, context);
	}

	/**
	 * <p>
	 * Translate a record constructor. To do this, we allocate the record on the
	 * heap using the "standard compound layout". Whilst not necessarily the most
	 * efficient, it does simplify the handling of nested arrays and records. The
	 * layout for a record <code>[v1,v2,v3]</code> looks like this on the heap:
	 *
	 * <pre>
	 *      +----------------+
	 *      |     length     |
	 *      +----------------+
	 *      |       v1       |
	 *      +----------------+
	 *             ...
	 *      +----------------+
	 *      |       vn       |
	 *      +----------------+
	 * </pre>
	 *
	 * <p>
	 * Here, the length indicates the number of fields where each field requires one
	 * words (i.e. the payload).
	 * </p>
	 *
	 * @param e      Expression to be translated.
	 * @param target Location to store result in (either register or stack location)
	 */
	public void translateArrayGenerator(Expr.ArrayGenerator e, Location target, Context context) {
//		//DONEFIXME: you will need to implement this
		List<Instruction> instructions = context.instructions();

		//get size
		RegisterLocation size = context.selectFreeRegister();
		translate(e.getSize(), size, context);
		context = context.lockLocation(size);
		//find place to put array
		RegisterLocation array = context.selectFreeRegister();
		bitwiseCopy(size, array, context);
		//increment size to account for length part
		instructions.add(new Instruction.Reg(Instruction.RegOp.inc, array.register));
		//multiply by how much space each takes
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.imul, WORD_SIZE, array.register));
		//get some space
		//array contains size (in bytes) to be allocated, after call will hold pointer to allocated memory
		allocateSpaceOnHeap(array, context);
		context = context.lockLocation(array);

		// Write length field
		MemoryLocation lengthField = new MemoryLocation(array.register, 0);
		bitwiseCopy(size, lengthField, context);

		//save to target
		bitwiseCopy(array, target, context);
		//save the value we want to write to a register
		RegisterLocation val = context.selectFreeRegister();
		translate(e.getValue(), val, context);
		context = context.lockLocation(val);

		//increment to get to where we want to fill
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.add, WORD_SIZE, array.register));
		//fill
		makeExternalMethodCall("intfill", context, null, array.register, val.register);

	}

	/**
	 * <p>
	 * Translate a record constructor. To do this, we allocate the record on the
	 * heap using the "standard compound layout". Whilst not necessarily the most
	 * efficient, it does simplify the handling of nested arrays and records. The
	 * layout for a record <code>[v1,v2,v3]</code> looks like this on the heap:
	 *
	 * <pre>
	 *      +----------------+
	 *      |     length     |
	 *      +----------------+
	 *      |       v1       |
	 *      +----------------+
	 *             ...
	 *      +----------------+
	 *      |       vn       |
	 *      +----------------+
	 * </pre>
	 *
	 * <p>
	 * Here, the length indicates the number of fields where each field requires one
	 * words (i.e. the payload).
	 * </p>
	 *
	 * @param e      Expression to be translated.
	 * @param target Location to store result in (either register or stack location)
	 */
	public void translateArrayInitialiser(Expr.ArrayInitialiser e, Location target, Context context) {
		// DONEFIXME: you will need to implement this


		Type.Array type = (Type.Array) unwrap(e.attribute(Attribute.Type.class).type);
		// Construct uninitialised compound object
		RegisterLocation base = compoundInitialiser(e.getArguments().size(), type, target, context);
		// Lock base to prevent it being overwritten
		context = context.lockLocation(base);
		// Translate fields in the order and write into the heap space
		int offset = WORD_SIZE;
		//
		for (Expr arr_expr: e.getArguments()){
			// Write payload
			MemoryLocation payloadField = new MemoryLocation(base.register, offset);
			translate(arr_expr, payloadField, context);
			// Advance over payload
			offset += WORD_SIZE;
		}

	}

	/**
	 * Translate a binary expression into the corresponding machine code
	 * instructions.
	 *
	 * @param expression Expression to be translated.
	 * @param target     Location to store result in (either register or stack
	 *                   location)
	 */
	public void translateBinary(Expr.Binary e, RegisterLocation target, Context context) {
		switch (e.getOp()) {
		case ADD:
		case SUB:
		case MUL:
		case DIV:
		case REM:
			translateArithmeticOperator(e, target, context);
			break;
		case AND:
		case OR:
		case EQ:
		case NEQ:
		case LT:
		case LTEQ:
		case GT:
		case GTEQ:
			translateBinaryCondition(e, target, context);
			break;
		default:
			throw new IllegalArgumentException("Unknown binary operator: " + e);
		}
	}

	/**
	 * Translate a binary condition when used in the context of a general
	 * expression. This means that the condition must load either a zero or one into
	 * the target register.
	 *
	 * @param e      Expression to be translated.
	 * @param target Location to store result in (either register or stack location)
	 */
	public void translateBinaryCondition(Expr.Binary e, RegisterLocation target, Context context) {
		List<Instruction> instructions = context.instructions();
		//
		String falseLabel = freshLabel();
		String exitLabel = freshLabel();
		translateCondition(e, falseLabel, context);
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, 1, target.register));
		instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, exitLabel));
		instructions.add(new Instruction.Label(falseLabel));
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, 0, target.register));
		instructions.add(new Instruction.Label(exitLabel));
	}

	/**
	 * Translate one of the arithmetic operators (i.e. +,-,*, etc). These are
	 * relatively straightforward because they can only be applied to integer (i.e.
	 * primitive) data.
	 *
	 * @param e      Expression to be translated.
	 * @param target Location to store result in (either register or stack location)
	 */
	public void translateArithmeticOperator(Expr.Binary e, RegisterLocation target, Context context) {
		List<Instruction> instructions = context.instructions();
		// Translate lhs and store result in the target register.
		translate(e.getLhs(), target, context);
		// We need to lock the target register here because it's value cannot be
		// overwritten in subsequent operations.
		context = context.lockLocation(target);
		// Determine register into which to store rhs. Note that we don't need
		// to lock this register because it can still be used as a temporary
		// when translating the right-hand side.
		RegisterLocation rhs = (RegisterLocation) allocateLocation(e.getRhs(), context);
		// Translate rhs and store result in temporary register
		translate(e.getRhs(), rhs, context);
		// UNlock the target location. This is necessary for division to ensure
		// that the target is not saved to the stack
		context = context.unlockLocation(target);
		// Finally, perform the binary operation.
		// Translate one of the arithmetic operators
		switch (e.getOp()) {
		case ADD:
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.add, rhs.register, target.register));
			break;
		case SUB:
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.sub, rhs.register, target.register));
			break;
		case MUL:
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.imul, rhs.register, target.register));
			break;
		case DIV:
			// The idiv instruction is curious because you cannot control where
			// the result is stored. That is, the result is always stored into
			// the hdx:hax register pairing (where hdx = remainder, hax =
			// quotient).
			saveRegistersIfUsed(Arrays.asList(HAX, HDX), context);
			if (rhs.register == HAX || rhs.register == HDX) {
				// For HAX, can resolve this using an xchg; for HDX, unsure.
				throw new RuntimeException("*** REGISTER CONFLICT ON DIVISION");
			}
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, target.register, HAX));
			// The following is needed for signed extension. This is broken on
			// 32bit architectures :(
			instructions.add(new Instruction.Unit(Instruction.UnitOp.cqto));
			instructions.add(new Instruction.Reg(Instruction.RegOp.idiv, rhs.register));
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, HAX, target.register));
			restoreRegistersIfUsed(Arrays.asList(HAX, HDX), context);
			break;
		case REM:
			// The idiv instruction is curious because you cannot control where
			// the result is stored. That is, the result is always stored into
			// the hdx:has register pairing (where hdx = remainder, hax =
			// quotient).
			saveRegistersIfUsed(Arrays.asList(HAX, HDX), context);
			if (rhs.register == HAX || rhs.register == HDX) {
				// Can resolve this using an xchg; for HDX, unsure.
				throw new RuntimeException("*** REGISTER CONFLICT ON REMAINDER");
			}
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, target.register, HAX));
			// The following is needed for signed extension. This is broken on
			// 32bit artchitectures :(
			instructions.add(new Instruction.Unit(Instruction.UnitOp.cqto));
			instructions.add(new Instruction.Reg(Instruction.RegOp.idiv, rhs.register));
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, HDX, target.register));
			restoreRegistersIfUsed(Arrays.asList(HAX, HDX), context);
			break;
		default:
			throw new IllegalArgumentException("Unknown binary operator: " + e);
		}
	}

	/**
	 * Translate a literal (e.g. integer or boolean) into the corresponding machine
	 * instructions.
	 *
	 * @param e
	 * @param target Location to store result in (either register or stack location)
	 * @param context Enclosing context.
	 */
	public void translateLiteral(Expr.Literal e, Location target, Context context) {
		Type type = unwrap(e.attribute(Attribute.Type.class).type);
		translateObjectLiteral(type, e.getValue(),target,context);
	}

	/**
	 * Translate an arbitrary object literal (e.g. integer or boolean) into the
	 * corresponding machine instructions.
	 *
	 * @param type    The type of the literal being translated.
	 * @param value   The literal represented as a Java object.
	 * @param target  Location to store result in (either register or stack
	 *                location)
	 * @param context The enclosing context
	 */
	public void translateObjectLiteral(Type type, Object value, Location target, Context context) {
		//
		if (value instanceof Boolean || value instanceof Character || value instanceof Integer) {
			translatePrimitiveLiteral(value, target, context);
		} else if (value instanceof String) {
			translateStringLiteral((String) value, target, context);
		} else if(value instanceof HashMap) {
			translateRecordLiteral((Type.Record) type, (HashMap) value, target, context);
		} else {
			translateArrayLiteral((Type.Array) type, (ArrayList) value,target,context);
		}
	}

	/**
	 * Translate a primitive literal. This is done by simply assigning the constant
	 * value into the target location. An intermedate register is required if the
	 * target is not itself a register.
	 *
	 * @param value   The literal represented as a Java object.
	 * @param target  Location to store result in (either register or stack
	 *                location)
	 * @param context The enclosing context
	 */
	public void translatePrimitiveLiteral(Object value, Location target, Context context) {
		List<Instruction> instructions = context.instructions();
		RegisterLocation tmp;
		// We need to identify a register location into which to load this
		// constant value. If the current target is a register location,
		// then fine. Otherwise, we need to find a free register.
		if (target instanceof RegisterLocation) {
			tmp = (RegisterLocation) target;
		} else {
			tmp = context.selectFreeRegister();
		}
		//
		if (value instanceof Boolean) {
			Boolean b = (Boolean) value;
			instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, b ? 1 : 0, tmp.register));
		} else if(value instanceof Character) {
			Character i = (Character) value;
			instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, i, tmp.register));
		} else {
			Integer i = (Integer) value;
			instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, i, tmp.register));
		}
		// Copy from tmp to target. If they are the same, this will be a
		// no-operation.
		bitwiseCopy(tmp, target, context);
	}

	/**
	 * Translate a string literal by converting it into a generic compound literal!
	 *
	 * @param value   The literal represented as a Java string object.
	 * @param target  Location to store result in (either register or stack
	 *                location)
	 * @param context The enclosing context
	 */
	public void translateStringLiteral(String literal, Location target, Context context) {
		ArrayList<Pair<Type,Object>> chars = new ArrayList<>();
		//
		for (int i = 0; i != literal.length(); ++i) {
			chars.add(new Pair<>(new Type.Int(), literal.charAt(i)));
		}
		//
		translateCompoundLiteral(chars, target, context);
	}

	/**
	 * Translate a record literal by converting it into a generic compound literal!
	 *
	 * @param type    The type of the literal being translated (which is necessary
	 *                to determine the correct field order).
	 * @param value   The literal represented as a Java Map object.
	 * @param target  Location to store result in (either register or stack
	 *                location)
	 * @param context The enclosing context
	 */
	public void translateRecordLiteral(Type.Record type, Map<String, Object> literal, Location target, Context context) {
		ArrayList<Pair<Type,Object>> fields = new ArrayList<>();
		//
		for (Pair<Type,String> p : type.getFields()) {
			fields.add(new Pair<>(p.first(), literal.get(p.second())));
		}
		//
		translateCompoundLiteral(fields, target, context);
	}

	/**
	 * Translate an array literal by converting it into a generic compound literal!
	 *
	 * @param type    The array type of the literal being translated.
	 * @param value   The literal represented as a Java Map object.
	 * @param target  Location to store result in (either register or stack
	 *                location)
	 * @param context The enclosing context
	 */
	public void translateArrayLiteral(Type.Array type, List<Object> literal, Location target, Context context) {
		ArrayList<Pair<Type,Object>> elements = new ArrayList<>();
		//
		for (Object l : literal) {
			elements.add(new Pair<>(type.getElement(),l));
		}
		//
		translateCompoundLiteral(elements, target, context);
	}

	/**
	 * Translate a generic compound literal. This basically creates a generic
	 * compound structure and then recursively assigns each component into this.
	 *
	 * @param literals The list of component literals
	 * @param target   Location to store result in (either register or stack
	 *                 location)
	 * @param context
	 */
	public void translateCompoundLiteral(List<Pair<Type,Object>> literals, Location target, Context context) {
		// Construct uninitialised compound object
		RegisterLocation base = compoundInitialiser(literals.size(), new Type.Int(), target, context);
		// Lock base to prevent it being overwritten
		context = context.lockLocation(base);
		// Translate fields in the order and write into the heap space
		int offset = WORD_SIZE;
		//
		for (int i = 0; i != literals.size(); ++i) {
			Pair<Type,Object> ith = literals.get(i);
			// Write payload
			MemoryLocation payloadField = new MemoryLocation(base.register, offset);
			translateObjectLiteral(ith.first(), ith.second(), payloadField, context);
			// Advance over payload
			offset += WORD_SIZE;
		}
	}

	/**
	 * /
	 * <p>
	 * Translate an invocation expression. This requires setting up the caller
	 * environment appropriately so that parameters can be passed into the called
	 * method, and returns can be passed back. The amount of space required by each
	 * variable is determined by from its type.
	 * </p>
	 *
	 * <pre>
	 *       +----------------+
	 *       |  parameter #1  |
	 *       +----------------+
	 *              ...
	 *       +----------------+
	 *       |  parameter #n  |
	 *       +----------------+
	 *  SP-> |  return value  |
	 *       +----------------+
	 * </pre>
	 * <p>
	 * Here, the parameters and return value are accessed at positive offsets from
	 * the stack pointer (whilst e.g. local variables are accessed at negative
	 * offsets).
	 * </p>
	 *
	 * @param e      Expression to be translated.
	 * @param target Location to store result in (either register or stack location)
	 */
	public void translateInvoke(Expr.Invoke e, Location target, Context context) {
		List<Instruction> instructions = context.instructions();
		// Save all used registers to the stack as part of the caller-save
		// calling convention.
		int savedRegistersWidth = saveRegistersIfUsed(REGISTER_POOL, context);
		// First, determine the amount of space to reserve on the stack for
		// parameters and the return value (if applicable).
		WhileFile.MethodDecl md = functions.get(e.getName());
		int callerEnvWidth = determineCallerEnvironmentWidth(md);
		// Second, create space on the stack for parameters and return value
		callerEnvWidth = allocateSpaceOnStack(callerEnvWidth, context);
		// Third, translate invocation arguments and load them onto the stack.
		// We have to go "backwards" because we are moving up the stack
		// allocated space. Furthermore, we must start above the space allocated
		// for the return value.
		Type returnType = unwrap(md.getRet());
		int offset = (returnType instanceof Type.Void) ? 0 : WORD_SIZE;
		List<Expr> arguments = e.getArguments();
		for (int i = arguments.size() - 1; i >= 0; --i) {
			Expr argument = arguments.get(i);
			Location tmp = new MemoryLocation(HSP, offset);
			translate(argument, tmp, context);
			offset += WORD_SIZE;
		}
		// Fourth, actually invoke the function
		String fn_name = "wl_" + md.getName();
		instructions.add(new Instruction.Addr(Instruction.AddrOp.call, fn_name));
		// Free space previously allocated on the stack
		freeSpaceOnStack(callerEnvWidth, context);
		// Restore all used registers
		restoreRegistersIfUsed(REGISTER_POOL, context);
		// Handle return value (if applicable)
		if (target != null && !(returnType instanceof Type.Void)) {
			// Finally, copy return value into its destination. It may seem odd
			// that we do this here, *after* we've freed space on the stack,
			// especially as we must then account for this discrepancy. However,
			// we must do it here, as the target location may have been relative
			// to the stack pointer on entry to this method and, if so, would be
			// pointing to the wrong place before we adjusted the stack.
			MemoryLocation tmp = new MemoryLocation(HSP, -(callerEnvWidth + savedRegistersWidth));
			bitwiseCopy(tmp, target, context);
		}
	}

	/**
	 * Translate a record access expression. This is done by first writing the
	 * source pointer into a given register and then reading out the field value
	 * from the appropriate offset.
	 *
	 * @param e
	 * @param target
	 * @param context
	 */
	public void translateRecordAccess(Expr.RecordAccess e, Location target, Context context) {
		// Determine the field offset
		Type.Record type = (Type.Record) unwrap(e.getSource().attribute(Attribute.Type.class).type);
		int offset = getFieldOffset(type, e.getName());
		// Translate source expression into a temporary register. In other words, store
		// the records heap address into a temporary.
		RegisterLocation base = context.selectFreeRegister();
		translate(e.getSource(), base, context);
		// Finally, copy bits into target location
		MemoryLocation fieldLocation = new MemoryLocation(base.register, offset);
		bitwiseCopy(fieldLocation, target, context);
	}

	/**
	 * <p>
	 * Translate a record constructor. To do this, we allocate the record on the
	 * heap using the "standard compound layout". Whilst not necessarily the most
	 * efficient, it does simplify the handling of nested arrays and records. The
	 * layout for a record <code>{T1 f1, ..., Tn fn}</code> looks like this on the
	 * heap:
	 *
	 * <pre>
	 *      +----------------+
	 *      |     length     |
	 *      +----------------+
	 *      |       f1       |
	 *      +----------------+
	 *             ...
	 *      +----------------+
	 *      |       fn       |
	 *      +----------------+
	 * </pre>
	 *
	 * <p>
	 * Here, the length indicates the number of fields where each field requires one
	 * words (i.e. the payload).
	 * </p>
	 *
	 * @param e      Expression to be translated.
	 * @param target Location to store result in (either register or stack location)
	 */
	public void translateRecordConstructor(Expr.RecordConstructor e, Location target, Context context) {
		List<Pair<String, Expr>> fields = e.getFields();
		Type.Record type = (Type.Record) unwrap(e.attribute(Attribute.Type.class).type);
		// Construct uninitialised compound object
		RegisterLocation base = compoundInitialiser(fields.size(), type, target, context);
		// Lock base to prevent it being overwritten
		context = context.lockLocation(base);
		// Translate fields in the order and write into the heap space
		int offset = WORD_SIZE;
		//
		for (int i = 0; i != fields.size(); ++i) {
			Pair<String, Expr> p = fields.get(i);
			// Write payload
			MemoryLocation payloadField = new MemoryLocation(base.register, offset);
			translate(p.second(), payloadField, context);
			// Advance over payload
			offset += WORD_SIZE;
		}
	}

	public void translateUnary(Expr.Unary e, RegisterLocation target, Context context) {
		// DONEFIXME: you need to implement this!
		List<Instruction> instructions = context.instructions();
		translate(e.getExpr(),target,context);

		switch (e.getOp()){
			case NOT:
				instructions.add(new Instruction.Reg(Instruction.RegOp.not,target.register));
				instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.and,1,target.register));
				break;
			case NEG:
				instructions.add(new Instruction.Reg(Instruction.RegOp.neg,target.register));
				break;
			case LENGTHOF:
				instructions.add(new Instruction.ImmIndReg(Instruction.ImmIndRegOp.mov,0,target.register,target.register));
				break;
			default:
				throw new IllegalArgumentException("unknown unary operator: "+e.getOp());
		}

	}

	public void translateVariable(Expr.Variable e, Location target, Context context) {
		// Determine the offset within the stack of this local variable.
		MemoryLocation loc = context.getVariableLocation(e.getName());
		//TODO check this is correct
		Type type = unwrap(e.attribute(Attribute.Type.class).type);
		if(type instanceof Type.Array) {
			compoundCopy(loc, context);
		}
		// Copy data from variable location into target location.
		bitwiseCopy(loc, target, context);
	}

	// ==========================================
	// Other Helpers
	// ==========================================

	/**
	 * Make an external method call, passing in zero or more arguments and
	 * potentially returning a value. Note that the return register should be a free
	 * register, whilst the argument registers may or may not be (and if not they
	 * will not be preserved).
	 *
	 * @param name         The external method to call
	 * @param code         Code section to add on those instructions corresponding
	 *                     to this expression
	 * @param returnTarget The register into which the result should be placed, or
	 *                     null if no result.
	 * @param arguments    Zero or more arguments.
	 */
	public void makeExternalMethodCall(String name, Context context, Register returnTarget, Register... arguments) {
		if (returnTarget != null && context.isLocked(returnTarget)) {
			// The return target should not be locked since otherwise it gets
			// saved on the stack and then overwrites the actual return value
			throw new IllegalArgumentException("return target register should not be locked");
		}
		// The argument registers lists the order in which arguments are passed
		// to external registers on the call stack.
		final Register[] parameterTargets = { HDI, HSI, HDX, HCX };
		List<Instruction> instructions = context.instructions();

		// Save all used registers onto the stack. A used register is one which
		// is not in the list of freeRegisters and, hence, whose value must be
		// preserved across the method call. In principle, we could optimise
		// this further as some registers are known to be callee-saved.
		saveRegistersIfUsed(REGISTER_POOL, context);

		// At this point, we now configure the register parameters to pass into
		// the external method. We have to be careful if the parameter target
		// at a given pointer overlaps with one (or more) of remaining argument
		// registers. When this happens, we swap the contents of the register
		// argument with that being overlapped.
		for (int i = 0; i != arguments.length; ++i) {
			Register argument = arguments[i];
			Register target = parameterTargets[i];
			if (argument == target) {
				// In the case that the argument register matches the target
				// register, then we don't need to do anything at all!
			} else {
				// Now, we need to look for a potential conflict between the
				// current target and one (or more) of the remaining arguments;
				Register conflict = null;
				for (int j = i + 1; j < arguments.length; ++j) {
					Register other = arguments[j];
					if (other == target) {
						// Yes, we have a conflict. We need to substitute
						// through with the current argument register to ensure
						// the swap operation is safe.
						conflict = other;
						arguments[j] = argument;
					}
				}
				if (conflict != null) {
					// In this case, a conflict was detected. Therefore, we
					// simply swap the contents of the current argument with
					// that of the conflicting register. This is guaranteed to
					// be safe since we know argument != target.
					instructions.add(new Instruction.RegReg(Instruction.RegRegOp.xchg, argument, conflict));
				} else {
					// In this case, no conflict occurred. Therefore, we simply
					// move the operand register into the desired target
					// register for the given calling convention.
					instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, argument, target));
				}
			}
		}
		// Ok, we're all done.
		instructions.add(new Instruction.Addr(Instruction.AddrOp.call, externalSymbolName(name)));
		if (returnTarget != null) {
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, HAX, returnTarget));
		}
		restoreRegistersIfUsed(REGISTER_POOL, context);
	}

	/**
	 * Save all registers in a given pool which are in use on the stack. This is
	 * done (for example) as part of the caller-save protocol. The set of used
	 * registers is determined as the set of registers which are not "Free".
	 *
	 * @param pool The pool of registers which we want to save (if necessary)
	 */
	public int saveRegistersIfUsed(List<Register> pool, Context context) {
		final int oneWordWidth = WORD_SIZE;
		List<Instruction> instructions = context.instructions();
		List<Register> usedRegisters = context.getUsedRegisters(pool);
		int width = usedRegisters.size() * oneWordWidth;
		width = allocateSpaceOnStack(width, context);
		for (int i = 0; i != usedRegisters.size(); ++i) {
			int offset = i * oneWordWidth;
			Register r = usedRegisters.get(i);
			instructions.add(new Instruction.RegImmInd(Instruction.RegImmIndOp.mov, r, offset, HSP));
		}
		return width;
	}

	/**
	 * Restore all registers in a given pool which were in use on the stack. This is
	 * done (for example) as part of the caller-save protocol. The set of used
	 * registers is determined as the set of registers which are not "Free".
	 *
	 * @param pool The array of registers which we want to restore (if necessary)
	 */
	public void restoreRegistersIfUsed(List<Register> pool, Context context) {
		List<Instruction> instructions = context.instructions();
		final int oneWordWidth = WORD_SIZE;
		List<Register> usedRegisters = context.getUsedRegisters(pool);
		for (int i = 0; i != usedRegisters.size(); ++i) {
			int offset = i * oneWordWidth;
			Register r = usedRegisters.get(i);
			instructions.add(new Instruction.ImmIndReg(Instruction.ImmIndRegOp.mov, offset, HSP, r));
		}
		freeSpaceOnStack(usedRegisters.size() * oneWordWidth, context);
	}

	/**
	 * <p>
	 * Allocate a location for storing the result of an expression. For results
	 * which fit in a single register, the allocated location will be one of the
	 * available free registers. For results which don't fit in a single register
	 * (e.g. record types), the location will be allocated on the stack.
	 * </p>
	 *
	 * <p>
	 * <b>NOTE:</b>In the event that a free register is allocated, this register
	 * will not be locked. The reason for this is that the register could still be
	 * useful as an intermediate when translating the given expression.
	 * </p>
	 * <p>
	 * <b>NOTE:</b>Locations allocated using this method should be explicitly freed
	 * once they are no longer needed. This is to ensure that any stack space
	 * allocated is eventually released.
	 * </p>
	 *
	 * @param e       Expression whose result we are allocating a location for.
	 * @param context
	 */
	public Location allocateLocation(Expr e, Context context) {
		return allocateRegisterLocations(context, e)[0];
	}

	/**
	 * <p>
	 * Allocate a location for storing the results of one or more expressions. For
	 * results which fit in a single register, the allocated location will be one of
	 * the available free registers. For results which don't fit in a single
	 * register (e.g. record types), the location will be allocated on the stack.
	 * </p>
	 *
	 * <p>
	 * <b>NOTE:</b>In the event that a free register is allocated, this register
	 * will not be locked. The reason for this is that the register could still be
	 * useful as an intermediate when translating the given expression.
	 * </p>
	 * <p>
	 * <b>NOTE:</b>Locations allocated using this method should be explicitly freed
	 * once they are no longer needed. This is to ensure that any stack space
	 * allocated is eventually released.
	 * </p>
	 *
	 * @param e       Expression whose result we are allocating a location for.
	 * @param context
	 */
	public RegisterLocation[] allocateRegisterLocations(Context context, Expr... es) {
		RegisterLocation[] rs = new RegisterLocation[es.length];
		for (int i = 0; i != rs.length; ++i) {
			RegisterLocation reg = context.selectFreeRegister();
			// Temporarily lock the register so that we don't allocate it
			// again!
			context = context.lockLocation(reg);
			rs[i] = reg;
		}
		return rs;
	}

	/**
	 * Perform a bitwise copy of a constant to a given location (which could either
	 * be a register target, or a memory location). For a memory target, first write
	 * the constant into a register and then blast out.
	 *
	 * @param from
	 * @param to
	 * @param context
	 */
	public void bitwiseCopy(int constant, Location to, Context context) {
		List<Instruction> instructions = context.instructions();
		if (to instanceof RegisterLocation) {
			// Easy case, write directly to register
			RegisterLocation tmp = (RegisterLocation) to;
			instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, constant, tmp.register));
		} else {
			// Harder case, write indirectly to memory.
			RegisterLocation tmp = context.selectFreeRegister();
			// First, write constant to temporary register
			instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, constant, tmp.register));
			// Second, write temporary register to target location
			bitwiseCopy(tmp, to, context);
		}
	}

	/**
	 * Perform a bitwise copy from one location to another. If both locations are
	 * registers, then it's just a register assignment. Otherwise, it will involve
	 * one or more indirect reads / writes.
	 *
	 * NOTE: when writing from one memory location to another, a temporary register
	 * is used. Therefore, any significant registers must be locked beforehand.
	 *
	 * @param from Location to copy bits from
	 * @param to   Location to copy bits to
	 */
	public void bitwiseCopy(Location from, Location to, Context context) {
		List<Instruction> instructions = context.instructions();
		// Make a quick sanity check
		if (from.equals(to)) {
			// In this very special case, we are attempting to copy to/from the
			// same location. Hence, we can just do nothing.
			return;
		}
		if (from instanceof RegisterLocation && to instanceof RegisterLocation) {
			// direct register-to-register copy
			Register fromReg = ((RegisterLocation) from).register;
			Register toReg = ((RegisterLocation) to).register;
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.mov, fromReg, toReg));
		} else if (from instanceof RegisterLocation) {
			// register-to-memory copy
			Register fromReg = ((RegisterLocation) from).register;
			MemoryLocation toLoc = (MemoryLocation) to;
			instructions.add(new Instruction.RegImmInd(Instruction.RegImmIndOp.mov, fromReg, toLoc.offset, toLoc.base));
		} else if (to instanceof RegisterLocation) {
			// memory-to-register copy
			MemoryLocation fromLoc = (MemoryLocation) from;
			Register toReg = ((RegisterLocation) to).register;
			instructions
					.add(new Instruction.ImmIndReg(Instruction.ImmIndRegOp.mov, fromLoc.offset, fromLoc.base, toReg));
		} else {
			MemoryLocation fromLoc = (MemoryLocation) from;
			MemoryLocation toLoc = (MemoryLocation) to;
			int width = WORD_SIZE;
			int oneWordWidth = WORD_SIZE;
			RegisterLocation tmp = context.selectFreeRegister();
			for (int i = 0; i < width; i = i + oneWordWidth) {
				instructions.add(new Instruction.ImmIndReg(Instruction.ImmIndRegOp.mov, fromLoc.offset + i,
						fromLoc.base, tmp.register));
				instructions.add(new Instruction.RegImmInd(Instruction.RegImmIndOp.mov, tmp.register, toLoc.offset + i,
						toLoc.base));
			}
		}
	}

	/**
	 * Perform bitwise equality test of two locations. In the positive case, if they
	 * are equal, branch to the target label. For the negative case, If they are not
	 * equal, then branch to the true label. In both cases, control otherwise,
	 * proceeds to the next instruction in sequence.
	 *
	 * @param positive    Indicates whether target is for true outcome or false
	 *                    outcome.
	 * @param lhs
	 * @param rhs
	 * @param targetLabel
	 */
	public void bitwiseEquality(boolean positive, Location lhs, Location rhs, String targetLabel, Context context) {
		Instruction.AddrOp kind = positive ? Instruction.AddrOp.jz : Instruction.AddrOp.jnz;
		List<Instruction> instructions = context.instructions();
		// Make a quick sanity check
		if (lhs.equals(rhs)) {
			// In this very special case, we are attempting to copy to/from the
			// same location. Hence, the outcome is statically known.
			if (positive) {
				instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, targetLabel));
			}
		} else if (lhs instanceof RegisterLocation && rhs instanceof RegisterLocation) {
			// direct register-to-register copy
			Register lhsReg = ((RegisterLocation) lhs).register;
			Register rhsReg = ((RegisterLocation) rhs).register;
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.cmp, lhsReg, rhsReg));
			instructions.add(new Instruction.Addr(kind, targetLabel));
		} else if (lhs instanceof RegisterLocation) {
			// register-to-memory copy
			Register lhsReg = ((RegisterLocation) lhs).register;
			RegisterLocation tmp = context.selectFreeRegister();
			MemoryLocation rhsLoc = (MemoryLocation) rhs;
			instructions.add(
					new Instruction.ImmIndReg(Instruction.ImmIndRegOp.mov, rhsLoc.offset, rhsLoc.base, tmp.register));
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.cmp, lhsReg, tmp.register));
			instructions.add(new Instruction.Addr(kind, targetLabel));
		} else if (rhs instanceof RegisterLocation) {
			// memory-to-register copy
			RegisterLocation tmp = context.selectFreeRegister();
			MemoryLocation lhsLoc = (MemoryLocation) lhs;
			Register rhsReg = ((RegisterLocation) rhs).register;
			instructions.add(
					new Instruction.ImmIndReg(Instruction.ImmIndRegOp.mov, lhsLoc.offset, lhsLoc.base, tmp.register));
			instructions.add(new Instruction.RegReg(Instruction.RegRegOp.cmp, rhsReg, tmp.register));
			instructions.add(new Instruction.Addr(kind, targetLabel));
		} else {
			// memory-to-memory copy of arbitrary width
			MemoryLocation lhsLoc = (MemoryLocation) lhs;
			MemoryLocation rhsLoc = (MemoryLocation) rhs;
			RegisterLocation lhsReg = context.selectFreeRegister();
			context = context.lockLocation(lhsReg);
			RegisterLocation rhsReg = context.selectFreeRegister();
			int width = WORD_SIZE;
			for (int i = 0; i < width; i = i + WORD_SIZE) {
				instructions.add(new Instruction.ImmIndReg(Instruction.ImmIndRegOp.mov, lhsLoc.offset + i, lhsLoc.base,
						lhsReg.register));
				instructions.add(new Instruction.ImmIndReg(Instruction.ImmIndRegOp.mov, rhsLoc.offset + i, rhsLoc.base,
						rhsReg.register));
				instructions.add(new Instruction.RegReg(Instruction.RegRegOp.cmp, rhsReg.register, lhsReg.register));
				instructions.add(new Instruction.Addr(kind, targetLabel));
			}
		}
	}

	/**
	 * Perform compound equality test of two locations. In the positive case, if
	 * they are equal, branch to the target label. For the negative case, If they
	 * are not equal, then branch to the true label. In both cases, control
	 * otherwise, proceeds to the next instruction in sequence.
	 *
	 * @param positive    Indicates whether target is for true outcome or false
	 *                    outcome.
	 * @param lhs
	 * @param rhs
	 * @param targetLabel
	 */
	public void compoundEquality(boolean positive, Location lhs, Location rhs, String targetLabel, Context context) {
		Instruction.AddrOp kind = positive ? Instruction.AddrOp.jnz : Instruction.AddrOp.jz;
		List<Instruction> instructions = context.instructions();
		RegisterLocation left;
		RegisterLocation right;
		// Make a quick sanity check
		if (lhs.equals(rhs)) {
			// In this very special case, we are attempting to copy to/from the
			// same location. Hence, the outcome is statically known.
			if (positive) {
				instructions.add(new Instruction.Addr(Instruction.AddrOp.jmp, targetLabel));
			}
			return;
		} else if (lhs instanceof RegisterLocation && rhs instanceof RegisterLocation) {
			left = ((RegisterLocation) lhs);
			right = ((RegisterLocation) rhs);
		} else if (lhs instanceof RegisterLocation) {
			left = ((RegisterLocation) lhs);
			right = context.selectFreeRegister();
			bitwiseCopy(rhs, right, context);
		} else if (rhs instanceof RegisterLocation) {
			left = context.selectFreeRegister();
			right = ((RegisterLocation) rhs);
			bitwiseCopy(lhs, left, context);
		} else {
			left = context.selectFreeRegister();
			// NOTE: must lock left location to avoid left and right being assigned same
			// register.
			right = context.lockLocation(left).selectFreeRegister();
			bitwiseCopy(lhs, left, context);
			bitwiseCopy(rhs, right, context);
		}
		// Call intcmp from runtime
		makeExternalMethodCall("intcmp", context, left.register, left.register, right.register);
		// Compare results from call against zero
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.cmp, 0, left.register));
		// Finally, dispatch on result
		instructions.add(new Instruction.Addr(kind, targetLabel));
	}

	/**
	 * Copy (i.e. clone) a compound object. The resulting reference is stored into
	 * the given location.
	 *
	 * @param from    Hold references to compound being cloned.
	 * @param to      Will hold reference to cloned compound.
	 * @param context
	 */
	public void compoundCopy(Location from, Context context) {
		RegisterLocation base;
		if (from instanceof RegisterLocation) {
			base = ((RegisterLocation) from);
		} else {
			context = context.tryAndLockLocation(from);
			base = context.selectFreeRegister();
			bitwiseCopy(from, base, context);
		}
		// Call intcpy from runtime
		makeExternalMethodCall("intcpy", context, base.register, base.register);
		// Copy resulting pointer to target location
		bitwiseCopy(base, from, context);
	}

	/**
	 * Initialise a new compound object and assign reference to a given target
	 * location. The layout for compound objects is as follows:
	 *
	 * <pre>
	 *      +----------------+
	 *      |     length     |
	 *      +----------------+
	 *      |       v1       |
	 *      +----------------+
	 *             ...
	 *      +----------------+
	 *      |       vn       |
	 *      +----------------+
	 * </pre>
	 *
	 * <p>
	 * Here, the length indicates the number of fields where each field requires one
	 * words (i.e. the payload).
	 * </p>
	 *
	 * @param n       The number of elements in the compound to construct.
	 * @param type    The type of the compound.
	 * @param target  The target location.
	 * @param context The enclosing context.
	 * @return An (unlocked) register location which also references the compound.
	 */
	public RegisterLocation compoundInitialiser(int n, Type type, Location target, Context context) {
		List<Instruction> instructions = context.instructions();
		RegisterLocation base;
		// We need to identify a register location into which to load the array. If the
		// current target is a register location, then fine. Otherwise, we need to find
		// a free register.
		if (target instanceof RegisterLocation) {
			base = (RegisterLocation) target;
		} else {
			base = context.selectFreeRegister();
		}
		// Write compound size to target register (one word for length, two words per
		// item).
		int size = (1 + n) * WORD_SIZE;
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, size, base.register));
		// Allocate size bytes on heap
		allocateSpaceOnHeap(base, context);
		// Lock base register
		context = context.lockLocation(base);
		// Write length field
		MemoryLocation lengthField = new MemoryLocation(base.register, 0);
		bitwiseCopy(n, lengthField, context);
		// Write reference to target location
		bitwiseCopy(base, target, context);
		// Done
		return base;
	}

	/**
	 * <p>
	 * Allocate a chunk of space on the stack. The stack pointer will be moved
	 * accordingly passed the allocated space. This may allocate more space that
	 * actually required. In particular,padding may have been allocated by the
	 * caller to ensure the stack was aligned to a 16byte boundary on entry to the
	 * function.
	 * </p>
	 *
	 * <p>
	 * <b>NOTE:</b>The use of padding is not strictly necessary for correct
	 * operation. However, when calling external functions (e.g. malloc, assert,
	 * printInt) we must adhere to the System V ABI rules which dictate this
	 * alignment. Therefore, it's much easier if we just keep the stack aligned the
	 * whole way along.
	 * </p>
	 *
	 * @param minimumWidth The number of bytes to allocate on the stack. This is a
	 *                     minimum value, as more bytes might actually be allocated
	 *                     than requested (i.e. for padding).
	 * @param code         Code section to add on those instructions corresponding
	 *                     to this expression
	 */
	private int allocateSpaceOnStack(int minimumWidth, Context context) {
		if (minimumWidth > 0) {
			List<Instruction> instructions = context.instructions();
			int paddedWidth = determinePaddedWidth(minimumWidth);
			instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.sub, paddedWidth, HSP));
			return paddedWidth;
		} else {
			return 0;
		}
	}

	/**
	 * <p>
	 * Free a chunk of space allocated on the stack. The stack pointer will be moved
	 * accordingly passed the allocated space. This may allocate more space that
	 * actually required. In particular,padding may have been allocated by the
	 * caller to ensure the stack was aligned to a 16byte boundary on entry to the
	 * function.
	 * </p>
	 *
	 * <p>
	 * <b>NOTE:</b>The use of padding is not strictly necessary for correct
	 * operation. However, when calling external functions (e.g. malloc, assert,
	 * printInt) we must adhere to the System V ABI rules which dictate this
	 * alignment. Therefore, it's much easier if we just keep the stack aligned the
	 * whole way along.
	 * </p>
	 *
	 * @param minimumWidth The number of bytes to allocate on the stack. This is a
	 *                     minimum value, as more bytes might actually be allocated
	 *                     than requested (i.e. for padding).
	 * @param code         Code section to add on those instructions corresponding
	 *                     to this expression
	 */
	private void freeSpaceOnStack(int minimumWidth, Context context) {
		if (minimumWidth > 0) {
			List<Instruction> instructions = context.instructions();
			int paddedWidth = determinePaddedWidth(minimumWidth);
			instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.add, paddedWidth, HSP));
		}
	}

	/**
	 * <p>
	 * Allocate memory on the heap dynamically using malloc. This expects the amount
	 * of space to be allocated is held in the target register. On exit, the target
	 * register will hold a pointer to the memory in question. This method will
	 * ensure that all other registers are saved properly.
	 * </p>
	 * <p>
	 * <b>NOTE:</b> this does not allocate any additional padding, as this is
	 * unnecessary for heap-allocated data.
	 * </p>
	 *
	 * @param target This register must contain size (in bytes) to be allocated and,
	 *               on return, will hold a pointer to the heap allocated data.
	 */
	public void allocateSpaceOnHeap(RegisterLocation target, Context context) {
		makeExternalMethodCall("malloc", context, target.register, target.register);
	}

	/**
	 * Determine the width of the callee stack frame. That is, the amount of space
	 * which must be reserved for the local variables.
	 *
	 * @param method
	 * @return
	 */
	private int determineCalleeStackFrameWidth(WhileFile.MethodDecl method) {
		HashMap<String, Type> variables = new HashMap<>();
		extractLocalVariableTypes(method.getBody(), variables);
		// Each varaible occupies exactly one slot
		return variables.size() * WORD_SIZE;
	}

	/**
	 * Determine the amount of space required for the parameters and return value of
	 * a given method. This must be padded out so that it is aligned on certain
	 * architectures.
	 *
	 * @param md
	 * @return
	 */
	public int determineCallerEnvironmentWidth(WhileFile.MethodDecl md) {
		int width = 0;
		for (WhileFile.Parameter p : md.getParameters()) {
			width += WORD_SIZE;
		}
		Type returnType = unwrap(md.getRet());
		if(!(returnType instanceof Type.Void)) {
			width += WORD_SIZE;
		}
		return width;
	}

	/**
	 * This ensures that the width returned is a multiple of 16. This is necessary
	 * to ensure that the stack is 16byte aligned in order to meet the requirements
	 * of the System V ABI.
	 *
	 * @param minimum The minumum number of bytes required for the stack frame to
	 *                hold all the necessary local variables, etc.
	 * @return
	 */
	private int determinePaddedWidth(int minimum) {
		// round up to nearest 16 bytes
		int tmp = (minimum / 16) * 16;
		if (tmp < minimum) {
			tmp = tmp + 16;
		}
		return tmp;
	}

	/**
	 * Determine the offset of a given payload field in a record. This is done by
	 * calculating the width of each payload until we find the one we're looking
	 * for.
	 *
	 * @param type
	 * @param field
	 * @return
	 */
	public int getFieldOffset(Type.Record type, String field) {
		// Calculate offset of field we are reading
		int offset = WORD_SIZE;
		//
		for (Pair<Type, String> e : type.getFields()) {
			// Check for field
			if (e.second().equals(field)) {
				break;
			}
			// Skip payload
			offset += WORD_SIZE;
		}
		return offset;
	}

	/**
	 * Determine the type of a given field
	 *
	 * @param type
	 * @param field
	 * @return
	 */
	public Type getFieldType(Type.Record type, String field) {
		for (Pair<Type, String> e : type.getFields()) {
			if (e.second().equals(field)) {
				return e.first();
			}
		}
		throw new IllegalArgumentException("invalid field: " + field);
	}

	/**
	 * Determine the type of every declared local variable. In cases where we have
	 * two local variables with the same name but different types, choose the
	 * physically largest type (in bytes).
	 *
	 * @param statements
	 * @param allocation
	 */
	private void extractLocalVariableTypes(List<Stmt> statements, Map<String, Type> allocation) {
		for (Stmt stmt : statements) {
			if (stmt instanceof Stmt.VariableDeclaration) {
				Stmt.VariableDeclaration vd = (Stmt.VariableDeclaration) stmt;
				allocation.put(vd.getName(), vd.getType());
			} else if (stmt instanceof Stmt.IfElse) {
				Stmt.IfElse ife = (Stmt.IfElse) stmt;
				extractLocalVariableTypes(ife.getTrueBranch(), allocation);
				extractLocalVariableTypes(ife.getFalseBranch(), allocation);
			} else if (stmt instanceof Stmt.For) {
				Stmt.For fe = (Stmt.For) stmt;
				// Allocate loop variable
				Stmt.VariableDeclaration vd = fe.getDeclaration();
				allocation.put(vd.getName(), vd.getType());
				// Explore loop body
				extractLocalVariableTypes(fe.getBody(), allocation);
			} else if (stmt instanceof Stmt.While) {
				Stmt.While fe = (Stmt.While) stmt;
				extractLocalVariableTypes(fe.getBody(), allocation);
			} else if (stmt instanceof Stmt.Switch) {
				Stmt.Switch fe = (Stmt.Switch) stmt;
				for (Stmt.Case c : fe.getCases()) {
					extractLocalVariableTypes(c.getBody(), allocation);
				}
			}
		}
	}

	/**
	 * Remove any nominal information from the type in question. This is important
	 * as, otherwise, we might not get what we are expecting.
	 *
	 * @param type
	 * @return
	 */
	public Type unwrap(Type type) {
		if (type instanceof Type.Named) {
			Type.Named tn = (Type.Named) type;
			WhileFile.TypeDecl td = types.get(tn.getName());
			return td.getType();
		} else {
			return type;
		}
	}

	/**
	 * Multiply the value in a given register by the architecture's WORD SIZE. This
	 * is necessary for calculating the offsets of array elements.
	 *
	 * @param context
	 */
	public void multiplyByWordSize(Register register, Context context) {
		List<Instruction> instructions = context.instructions();
		int i = 1;
		// NOTE: whilst using a multiplication instruction is simpler, using shifts is
		// more efficient :)
		while (i < WORD_SIZE) {
			instructions.add(new Instruction.Reg(Instruction.RegOp.shl, register));
			i = i << 1;
		}
	}

	public boolean isPrimitive(Type t) {
		return t instanceof Type.Bool || t instanceof Type.Int;
	}

	private static int labelIndex = 0;

	public static String freshLabel() {
		return "label" + labelIndex++;
	}

	/**
	 * Determine the appropriate symbol name to use when calling a method which
	 * employs standard C calling conventions. Perhaps surprisingly, this is
	 * architecture dependent. In particular, MacOS usese the Mach-O object file
	 * format and supports alternative calling conventions (perhaps for
	 * efficiency?). Thus, on MacOS, symbols which adhere to standard calling
	 * conventions are prefixed with an underscore.
	 *
	 * @param name
	 */
	private String externalSymbolName(String name) {
		if (target == Target.MACOS_X86_64) {
			return "_" + name;
		} else {
			return name;
		}
	}

	/**
	 * Add a standard main method which will be called by the operating system when
	 * this process is executed. This sequence is operating system dependent, and
	 * simply calls the translated <code>main()</code> method from the original
	 * while source file.
	 *
	 * @param xf
	 */
	private void addMainLauncher(X86File.Code code) {
		List<Instruction> instructions = code.instructions;
		instructions.add(new Instruction.Label(externalSymbolName("main"), 1, true));
		instructions.add(new Instruction.Reg(Instruction.RegOp.push, HBP));
		instructions.add(new Instruction.Addr(Instruction.AddrOp.call, "wl_main"));
		instructions.add(new Instruction.Reg(Instruction.RegOp.pop, HBP));
		// Ensure an exit code of 0 if we get here. If not, then an assertion
		// occurred and we get a non-zero exit code.
		instructions.add(new Instruction.ImmReg(Instruction.ImmRegOp.mov, 0, HAX));
		instructions.add(new Instruction.Unit(Instruction.UnitOp.ret));
	}

	/**
	 * Returns the head of a given registers family. For example, on
	 * <code>x86_64</code> the head of the <code>bx</code> family is
	 * <code>rbx</code>. Conversely, the head of the <code>bx</code> family is
	 * <code>ebx</code> on <code>x86_32</code>.
	 *
	 * @param register
	 * @return
	 */
	private Register headOfFamily(Register register) {
		Register.Width width;
		switch (target.arch) {
		case X86_32:
			width = Register.Width.Long;
			break;
		case X86_64:
			width = Register.Width.Quad;
			break;
		default:
			throw new IllegalArgumentException("Invalid architecture: " + target.arch);
		}
		return register.sibling(width);
	}

	private final Register HAX;
	private final Register HBX;
	private final Register HCX;
	private final Register HDX;
	private final Register HDI;
	private final Register HSI;
	private final Register HBP;
	private final Register HSP;
	private final Register HIP;

	public final List<Register> REGISTER_POOL;

	/**
	 * An abstraction describing the destination for the result of a given
	 * expression.
	 *
	 * @author David J. Pearce
	 *
	 */
	private interface Location {

	}

	/**
	 * A register target is the simple case where the given result fits into a
	 * register.
	 *
	 * @author David J. Pearce
	 *
	 */
	private class RegisterLocation implements Location {
		private final Register register;

		public RegisterLocation(Register target) {
			this.register = target;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RegisterLocation) {
				RegisterLocation l = (RegisterLocation) o;
				return l.register == register;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return register.hashCode();
		}

		@Override
		public String toString() {
			return register.toString();
		}
	}

	/**
	 * Describes a location in memory relative to a given base register (typically
	 * either the stack or frame pointer). This is necessary for the case where we
	 * are writing a value which doesn't fit into a single register (e.g. a record).
	 *
	 * @author David J. Pearce
	 *
	 */
	private class MemoryLocation implements Location {
		private final Register base;
		private final int offset;

		public MemoryLocation(Register base, int offset) {
			this.base = base;
			this.offset = offset;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof MemoryLocation) {
				MemoryLocation l = (MemoryLocation) o;
				return base == l.base && offset == l.offset;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return base.hashCode() ^ offset;
		}

		@Override
		public String toString() {
			return "&(" + base + "+" + offset + ")";
		}
	}

	private class Context {
		private final Map<String, MemoryLocation> localVariables;
		private final X86File.Code code;
		private final X86File.Data data;
		private List<Register> freeRegisters;
		private String exitLabel;
		private final Stack<String> breakLabels;
		private final Stack<String> continueLabels;

		public Context(List<Register> freeRegisters, Map<String, MemoryLocation> localVariables, X86File.Code code,
				X86File.Data data) {
			this.localVariables = localVariables;
			this.code = code;
			this.data = data;
			this.freeRegisters = freeRegisters;
			this.exitLabel = null;
			this.breakLabels = new Stack<>();
			this.continueLabels = new Stack<>();
		}

		/**
		 * Copy constructor.
		 *
		 * @param other
		 */
		private Context(Context other) {
			this.localVariables = other.localVariables;
			this.code = other.code;
			this.data = other.data;
			this.freeRegisters = other.freeRegisters;
			this.exitLabel = other.exitLabel;
			this.breakLabels = other.breakLabels;
			this.continueLabels = other.continueLabels;
		}

		public MemoryLocation getVariableLocation(String name) {
			return localVariables.get(name);
		}

		/**
		 * Get target label for method exit. This is necessary, for example, when
		 * implementing return statements.
		 *
		 * @return
		 */
		public String exitLabel() {
			return exitLabel;
		}

		public String generateBreakLabel(){
			String label = freshLabel();
			addBreak(label);
			return label;
		}

		public String viewRecentBreak() {
			return breakLabels.peek();
		}

		public String getRecentBreak() {
			return breakLabels.pop();
		}

		public void addBreak(String breakLabel){
			breakLabels.push(breakLabel);
		}

		public String generateContinueLabel(){
			String label = freshLabel();
			addContinue(label);
			return label;
		}

		public String viewRecentContinue() {
			return continueLabels.peek();
		}

		public String getRecentContinue() {
			return continueLabels.pop();
		}

		public void addContinue(String continueLabel){
			continueLabels.push(continueLabel);
		}

		/**
		 * Set target label for method exit.
		 *
		 * @param label
		 * @return
		 */
		public Context setExitLabel(String label) {
			Context c = new Context(this);
			c.exitLabel = label;
			return c;
		}

		public List<Instruction> instructions() {
			return code.instructions;
		}

		public List<Constant> constants() {
			return data.constants;
		}

		/**
		 * Check whether a given register is currently locked or not.
		 *
		 * @param register
		 * @return
		 */
		public boolean isLocked(Register register) {
			return !freeRegisters.contains(register);
		}

		/**
		 * Select a free register from the list of free registers. Note that this
		 * register is not removed from the list of free registers (as this only happens
		 * when the register is locked).
		 *
		 * @param freeRegister
		 * @return
		 */
		private RegisterLocation selectFreeRegister() {
			// FIXME: The following line fails because it does not handle the
			// case where we run out of registers. In such case, we need to
			// implement some kind of register spilling mechanism.
			Register reg = freeRegisters.get(0);
			return new RegisterLocation(reg);
		}

		/**
		 * Try and lock a given location. Specifically, if it is already locked then do
		 * nothing. Otherwise, lock it.
		 *
		 * @param location
		 * @return
		 */
		private Context tryAndLockLocation(Location location) {
			Register reg;
			if(location instanceof RegisterLocation) {
				reg = ((RegisterLocation)location).register;
			} else {
				reg = ((MemoryLocation)location).base;
			}
			//
			if(isLocked(reg)) {
				return this;
			} else {
				return lockLocation(location);
			}
		}

		/**
		 * "lock" a given location. This only has the effect of ensuring this location
		 * remains valid until the location is unlocked (either explicitly or
		 * implicitly). We are essentially "locking" the register involved and
		 * preventing it from being used as a location for a subsequent operation.
		 *
		 * @param location. The location which is to be locked.
		 */
		private Context lockLocation(Location location) {
			Register reg;
			if (location instanceof RegisterLocation) {
				RegisterLocation sl = (RegisterLocation) location;
				reg = sl.register;
			} else {
				MemoryLocation l = (MemoryLocation) location;
				reg = l.base;
			}
			// Quick sanity check
			if (!freeRegisters.contains(reg)) {
				throw new IllegalArgumentException("attempting to lock register which is already locked");
			}
			Context c = new Context(this);
			c.freeRegisters = new ArrayList<>(freeRegisters);
			c.freeRegisters.remove(reg);
			return c;
		}

		/**
		 * "unlock" a given location. This only has an effect if the location is a
		 * register, in which case it is removed from the list of free registers. We are
		 * essentially "unlocking" that register so that it can be used as a location
		 * for a subsequent operation.
		 *
		 * @param freeRegister. The free register to unlock. This should be in the list
		 *        of free registers.
		 */
		private Context unlockLocation(Location location) {
			Register reg;
			if (location instanceof RegisterLocation) {
				RegisterLocation sl = (RegisterLocation) location;
				reg = sl.register;
			} else {
				MemoryLocation l = (MemoryLocation) location;
				reg = l.base;
			}
			// First, do a sanity check
			if (freeRegisters.contains(reg)) {
				throw new IllegalArgumentException("attempting to unlock register which is not locked");
			}
			Context c = new Context(this);
			c.freeRegisters = new ArrayList<>(freeRegisters);
			c.freeRegisters.add(reg);
			return c;
		}

		/**
		 * Determine the set of used registers from a given pool of registers. That is,
		 * the registers in the pool which are not in the list of free registers.
		 *
		 * @param freeRegisters
		 * @return
		 */
		public List<Register> getUsedRegisters(List<Register> pool) {
			ArrayList<Register> usedRegisters = new ArrayList<>();
			for (int i = 0; i != pool.size(); ++i) {
				Register r = pool.get(i);
				if (!freeRegisters.contains(r)) {
					usedRegisters.add(r);
				}
			}
			return usedRegisters;
		}

	}
}
