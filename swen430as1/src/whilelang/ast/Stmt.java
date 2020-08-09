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

package whilelang.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import whilelang.util.SyntacticElement;

/**
 * Represents a statement in the source code of a While program. Many standard
 * statement kinds are provided, including <code>if</code>, <code>while</code>,
 * <code>for</code>, etc.
 *
 * @author David J. Pearce
 *
 */
public interface Stmt extends SyntacticElement {

	/**
	 * Represents an assert statement which checks whether a given expression
	 * evaluates to true or not.
	 *
	 * <pre>
	 * void f(int x) {
	 * 	assert x >= 0;
	 * }
	 * </pre>
	 *
	 * If the assertion fails, then a runtime fault is raised and execution
	 * aborts.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Assert extends SyntacticElement.Impl implements
			Stmt {

		/**
		 * The expression which, when evaluated, determines whether or not the assertion
		 * holds.
		 */
		private final Expr expr;

		/**
		 * Construct an assert statement from a given expression.
		 *
		 * @param expr
		 *            May not be null.
		 * @param attributes
		 */
		public Assert(Expr expr, Attribute... attributes) {
			super(attributes);
			this.expr = expr;
		}

		/**
		 * Construct a print statement from a given expression.
		 *
		 * @param expr
		 *            May not be null.
		 * @param attributes
		 */
		public Assert(Expr expr, Collection<Attribute> attributes) {
			super(attributes);
			this.expr = expr;
		}

		@Override
		public String toString() {
			return "assert " + getExpr();
		}

		/**
		 * Get the expression whose value is to be printed.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public Expr getExpr() {
			return expr;
		}
	}


	/**
	 * Represents an assignment statement of the form <code>lhs = rhs</code>.
	 * Here, the <code>rhs</code> is any expression, whilst the <code>lhs</code>
	 * must be an <code>LVal</code> --- that is, an expression permitted on the
	 * left-side of an assignment. The following illustrates different possible
	 * assignment statements:
	 *
	 * <pre>
	 * x = y; // variable assignment
	 * x.f = y; // field assignment
	 * x[i] = y; // list assignment
	 * x[i].f = y; // compound assignment
	 * </pre>
	 *
	 * The last assignment here illustrates that the left-hand side of an
	 * assignment can be arbitrarily complex, involving nested assignments into
	 * lists and records.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Assign extends SyntacticElement.Impl implements
			Stmt {

		/**
		 * The left-hand side of the assignment. That is, the variable (or component
		 * thereof) being assigned.
		 */
		private final Expr.LVal lhs;
		/**
		 * The right-hand side of the assignment determines the value which is actually
		 * assigned.
		 */
		private final Expr rhs;

		/**
		 * Create an assignment from a given <code>lhs</code> and
		 * <code>rhs</code>.
		 *
		 * @param lhs
		 *            --- left-hand side, which may not be <code>null</code>.
		 * @param rhs
		 *            --- right-hand side, which may not be <code>null</code>.
		 * @param attributes
		 */
		public Assign(Expr.LVal lhs, Expr rhs, Attribute... attributes) {
			super(attributes);
			this.lhs = lhs;
			this.rhs = rhs;
		}

		/**
		 * Create an assignment from a given <code>lhs</code> and
		 * <code>rhs</code>.
		 *
		 * @param lhs
		 *            left-hand side, which may not be <code>null</code>.
		 * @param rhs
		 *            right-hand side, which may not be <code>null</code>.
		 * @param attributes
		 */
		public Assign(Expr.LVal lhs, Expr rhs, Collection<Attribute> attributes) {
			super(attributes);
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public String toString() {
			return getLhs() + " = " + getRhs();
		}

		/**
		 * Get the left-hand side of this assignment.
		 *
		 * @return Guaranteed non-null.
		 */
		public Expr.LVal getLhs() {
			return lhs;
		}

		/**
		 * Get the right-hand side of this assignment.
		 *
		 * @return Guaranteed non-null.
		 */
		public Expr getRhs() {
			return rhs;
		}
	}

	/**
	 * Represents a return statement which (optionally) returns a value. The
	 * following illustrates:
	 *
	 * <pre>
	 * int f(int x) {
	 * 	return x + 1;
	 * }
	 * </pre>
	 *
	 * Here, we see a simple <code>return</code> statement which returns an
	 * <code>int</code> value.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Return extends SyntacticElement.Impl implements
			Stmt {

		/**
		 * The (optional) expression which determines the return value. This maybe
		 * <code>null</code> if no return expression is given.
		 */
		private final Expr expr;

		/**
		 * Create a given return statement with an optional return value.
		 *
		 * @param expr
		 *            the return value, which may be <code>null</code>.
		 * @param attributes
		 */
		public Return(Expr expr, Attribute... attributes) {
			super(attributes);
			this.expr = expr;
		}

		/**
		 * Create a given return statement with an optional return value.
		 *
		 * @param expr
		 *            the return value, which may be <code>null</code>.
		 * @param attributes
		 */
		public Return(Expr expr, Collection<Attribute> attributes) {
			super(attributes);
			this.expr = expr;
		}

		@Override
		public String toString() {
			if (getExpr() != null) {
				return "return " + getExpr();
			} else {
				return "return";
			}
		}

		/**
		 * Get the optional return value.
		 *
		 * @return --- May be <code>null</code>.
		 */
		public Expr getExpr() {
			return expr;
		}
	}

	public static final class Throw extends SyntacticElement.Impl implements
					Stmt {

		/**
		 * The (optional) expression which determines the return value. This maybe
		 * <code>null</code> if no return expression is given.
		 */
		private final Expr expr;
		private  Type type;//type of the expression above, evaluated at compile time

		/**
		 * Create a given return statement with an optional return value.
		 *
		 * @param expr
		 *            the return value, which may be <code>null</code>.
		 * @param attributes
		 */
		public Throw(Expr expr, Attribute... attributes) {
			super(attributes);
			this.expr = expr;
		}

		/**
		 * Create a given return statement with an optional return value.
		 *
		 * @param expr
		 *            the return value, which may be <code>null</code>.
		 * @param attributes
		 */
		public Throw(Expr expr, Collection<Attribute> attributes) {
			super(attributes);
			this.expr = expr;
		}

		@Override
		public String toString() {
			return "throw " + getExpr();
		}

		/**
		 * Get the optional return value.
		 *
		 * @return --- May be <code>null</code>.
		 */
		public Expr getExpr() {
			return expr;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public Type getType() {
			return type;
		}
	}

	public static final class For extends SyntacticElement.Impl implements Stmt {

		/**
		 * Represents the loop index variable.
		 */
		private final VariableDeclaration declaration;
		/**
		 * The condition, when evaluated, determines whether or not to continue looping.
		 */
		private final Expr condition;
		/**
		 * The increment expression is used to update the loop index variable.
		 */
		private final Stmt increment;
		/**
		 * A sequence of zero or more statements making up the loop body.
		 */
		private final ArrayList<Stmt> body;

		/**
		 * Construct a for loop from a given declaration, condition and
		 * increment. Note that the declaration, conditional and increment are
		 * all optional.
		 *
		 * @param declaration
		 *            An variable declation, which may be null.
		 * @param condition
		 *            A loop condition which may not be null.
		 * @param increment
		 *            An increment statement, which may be null.
		 * @param body
		 *            A list of zero or more statements, which may not be null.
		 * @param attributes
		 */
		public For(VariableDeclaration declaration, Expr condition, Stmt increment,
							 Collection<Stmt> body, Attribute... attributes) {
			super(attributes);
			this.declaration = declaration;
			this.condition = condition;
			this.increment = increment;
			this.body = new ArrayList<Stmt>(body);
		}

		/**
		 * Construct a for loop from a given declaration, condition and
		 * increment. Note that the declaration, conditional and increment are
		 * all optional.
		 *
		 * @param declaration
		 *            An variable declation, which may be null.
		 * @param condition
		 *            A loop condition which may be null.
		 * @param increment
		 *            An increment statement, which may be null.
		 * @param body
		 *            A list of zero or more statements, which may not be null.
		 * @param attributes
		 */
		public For(VariableDeclaration declaration, Expr condition, Stmt increment,
							 Collection<Stmt> body, Collection<Attribute> attributes) {
			super(attributes);
			this.declaration = declaration;
			this.condition = condition;
			this.increment = increment;
			this.body = new ArrayList<Stmt>(body);
		}

		/**
		 * Get the variable declaration for this loop.
		 *
		 * @return May be null.
		 */
		public VariableDeclaration getDeclaration() {
			return declaration;
		}

		/**
		 * Get the loop condition.
		 *
		 * @return May be null.
		 */
		public Expr getCondition() {
			return condition;
		}

		/**
		 * Get the increment statement.
		 *
		 * @return May be null.
		 */
		public Stmt getIncrement() {
			return increment;
		}

		/**
		 * Get the loop body.
		 *
		 * @return May not be null.
		 */
		public ArrayList<Stmt> getBody() {
			return body;
		}
	}


	/**
	 * Represents a while statement whose body is made up from a block of
	 * statements separated by curly braces. Note that, unlike C or Java, the
	 * body must be contained within curly braces. As an example:
	 *
	 * <pre>
	 * int sum([int] xs) {
	 *   int r = 0;
	 *   int i = 0;
	 *   while(i < |xs|) {
	 *     r = r + xs[i];
	 *     i = i + 1;
	 *   }
	 *   return r;
	 * }
	 * </pre>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class While extends SyntacticElement.Impl implements Stmt {

		/**
		 * The condition, when evaluated, determines whether or not the loop continues.
		 */
		private final Expr condition;
		/**
		 * A sequence of zero of more statements which make up the body of the loop.
		 */
		private final ArrayList<Stmt> body;

		/**
		 * Construct a While statement from a given condition and body of
		 * statements.
		 *
		 * @param condition
		 *            non-null expression.
		 * @param body
		 *            non-null collection which contains zero or more
		 *            statements.
		 * @param attributes
		 */
		public While(Expr condition, Collection<Stmt> body,
				Attribute... attributes) {
			super(attributes);
			this.condition = condition;
			this.body = new ArrayList<Stmt>(body);
		}

		/**
		 * Construct a While statement from a given condition and body of
		 * statements.
		 *
		 * @param condition
		 *            non-null expression.
		 * @param body
		 *            non-null collection which contains zero or more
		 *            statements.
		 * @param attributes
		 */
		public While(Expr condition, Collection<Stmt> body,
				Collection<Attribute> attributes) {
			super(attributes);
			this.condition = condition;
			this.body = new ArrayList<Stmt>(body);
		}

		/**
		 * Get the condition which controls the while loop.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public Expr getCondition() {
			return condition;
		}

		/**
		 * Get the statements making up the loop body.
		 *
		 * @return Guarantted to be non-null.
		 */
		public List<Stmt> getBody() {
			return body;
		}
	}

	public static final class DoWhile extends SyntacticElement.Impl implements Stmt {

		/**
		 * The condition, when evaluated, determines whether or not the loop continues.
		 */
		private final Expr condition;
		/**
		 * A sequence of zero of more statements which make up the body of the loop.
		 */
		private final ArrayList<Stmt> body;

		/**
		 * Construct a While statement from a given condition and body of
		 * statements.
		 *
		 * @param condition
		 *            non-null expression.
		 * @param body
		 *            non-null collection which contains zero or more
		 *            statements.
		 * @param attributes
		 */
		public DoWhile(Expr condition, Collection<Stmt> body,
								 Attribute... attributes) {
			super(attributes);
			this.condition = condition;
			this.body = new ArrayList<Stmt>(body);
		}

		/**
		 * Construct a While statement from a given condition and body of
		 * statements.
		 *
		 * @param condition
		 *            non-null expression.
		 * @param body
		 *            non-null collection which contains zero or more
		 *            statements.
		 * @param attributes
		 */
		public DoWhile(Expr condition, Collection<Stmt> body,
								 Collection<Attribute> attributes) {
			super(attributes);
			this.condition = condition;
			this.body = new ArrayList<Stmt>(body);
		}

		/**
		 * Get the condition which controls the while loop.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public Expr getCondition() {
			return condition;
		}

		/**
		 * Get the statements making up the loop body.
		 *
		 * @return Guarantted to be non-null.
		 */
		public List<Stmt> getBody() {
			return body;
		}
	}



	/**
	 * Represents a classical for statement made up from a <i>variable
	 * declaration</i>, a <i>loop condition</i> and an <i>increment
	 * statement</i>. The following illustrates:
	 *
	 * <pre>
	 * int sum([int] xs) {
	 *   int r = 0;
	 *   for(int i=0;i<|xs|;i=i+1) {
	 *     r = r + xs[i];
	 *   }
	 *   return r;
	 * }
	 * </pre>
	 *
	 * Observe that the variable declaration does not need to supply an
	 * initialiser expression. Furthermore, like C and Java, the variable
	 * declaration, condition and increment statements are all optional. Thus,
	 * we can safely rewrite the above as follows:
	 *
	 * <pre>
	 * int sum([int] xs) {
	 *   int r = 0;
	 *   for(int i=0;i<|xs|;) {
	 *     r = r + xs[i];
	 *   }
	 *   return r;
	 * }
	 * </pre>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class TryCatch extends SyntacticElement.Impl implements Stmt {

		/**
		 * A sequence of zero or more statements making up the loop body.
		 */
		private final List<Stmt> try_body;
		private final List<Catch> catchs;


		public TryCatch(List<Stmt> try_body, List<Catch> catch_body, Attribute... attributes) {
			super(attributes);
			this.try_body = try_body;
			this.catchs = catch_body;

		}

		public TryCatch(List<Stmt> try_body, List<Catch> catchs,Collection<Attribute> attributes) {
			super(attributes);
			this.try_body = try_body;
			this.catchs = catchs;
		}


		public List<Stmt> getTry_body() {
			return try_body;
		}

		public List<Catch> getCatchs() {
			return catchs;
		}
	}

	public static final class Catch extends SyntacticElement.Impl implements Stmt {

		/**
		 * What we are catching
		 */
		private final Stmt.VariableDeclaration caught_var;

		private final List<Stmt> catch_body;


		public Catch(Stmt.VariableDeclaration caught_var, List<Stmt> catch_body, Attribute... attributes) {
			super(attributes);
			this.caught_var = caught_var;
			this.catch_body = catch_body;

		}

		public Catch(Stmt.VariableDeclaration caught_var, List<Stmt> catch_body,Collection<Attribute> attributes) {
			super(attributes);
			this.caught_var = caught_var;
			this.catch_body = catch_body;
		}

		public VariableDeclaration getCaught_var() {
			return caught_var;
		}

		public List<Stmt> getCatch_body() {
			return catch_body;
		}
	}

	/**
	 * Represents a classical for statement made up from a <i>variable
	 * declaration</i>, a <i>loop condition</i> and an <i>increment
	 * statement</i>. The following illustrates:
	 *
	 * <pre>
	 * int sum([int] xs) {
	 *   int r = 0;
	 *   for(int i=0;i<|xs|;i=i+1) {
	 *     r = r + xs[i];
	 *   }
	 *   return r;
	 * }
	 * </pre>
	 *
	 * Observe that the variable declaration does not need to supply an
	 * initialiser expression. Furthermore, like C and Java, the variable
	 * declaration, condition and increment statements are all optional. Thus,
	 * we can safely rewrite the above as follows:
	 *
	 * <pre>
	 * int sum([int] xs) {
	 *   int r = 0;
	 *   for(int i=0;i<|xs|;) {
	 *     r = r + xs[i];
	 *   }
	 *   return r;
	 * }
	 * </pre>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class ForEach extends SyntacticElement.Impl implements Stmt {


		private final VariableDeclaration declaration;
		//array is likely not correct as we could have variable which contains values :(
		private final Expr collection_values;

		/**
		 * A sequence of zero or more statements making up the loop body.
		 */
		private final ArrayList<Stmt> body;

		/**
		 * Construct a for loop from a given declaration, condition and
		 * increment. Note that the declaration, conditional and increment are
		 * all optional.
		 *
		 * @param declaration
		 *            An variable declation, which may be null.
		 * @param collection_values
		 *            A list of values
		 * @param body
		 *            A list of zero or more statements, which may not be null.
		 * @param attributes
		 */
		public ForEach(VariableDeclaration declaration, Expr collection_values,
				   Collection<Stmt> body, Attribute... attributes) {
			super(attributes);
			this.declaration = declaration;
			this.collection_values = collection_values;
			this.body = new ArrayList<Stmt>(body);
		}


		public ForEach(VariableDeclaration declaration, Expr collection_values,
				   Collection<Stmt> body, Collection<Attribute> attributes) {
			super(attributes);
			this.declaration = declaration;
			this.collection_values = collection_values;
			this.body = new ArrayList<Stmt>(body);
		}

		/**
		 * Get the variable declaration for this loop.
		 *
		 * @return May be null.
		 */
		public VariableDeclaration getDeclaration() {
			return declaration;
		}


		/**
		 * Get the loop body.
		 *
		 * @return May not be null.
		 */
		public ArrayList<Stmt> getBody() {
			return body;
		}

		public Expr getCollection_values() {
			return collection_values;
		}
	}


	/**
	 * Represents a classical if-else statement, made up from a
	 * <i>condition</i>, <i>true branch</i> and <i>false branch</i>.
	 * The following illustrates:
	 * <pre>
	 * int max(int x, int y) {
	 *   if(x > y) {
	 *     return x;
	 *   } else {
	 *     return y;
	 *   }
	 * }
	 * </pre>
	 * @author David J. Pearce
	 *
	 */
	public static final class IfElse extends SyntacticElement.Impl implements
			Stmt {

		/**
		 * The condition, when evaluated, determines whether or not the true or false
		 * branch is taken.
		 */
		private final Expr condition;
		/**
		 * A sequence of zero or more statements which represent the true branch.
		 */
		private final ArrayList<Stmt> trueBranch;

		/**
		 * A sequence of zero or more statements which represent the false branch.
		 */
		private final ArrayList<Stmt> falseBranch;

		/**
		 * Construct an if-else statement from a condition, true branch and
		 * optional false branch.
		 *
		 * @param condition
		 *            May not be null.
		 * @param trueBranch
		 *            A list of zero or more statements to be executed when the
		 *            condition holds; may not be null.
		 * @param falseBranch
		 *            A list of zero of more statements to be executed when the
		 *            condition does not hold; may not be null.
		 * @param attributes
		 */
		public IfElse(Expr condition, List<Stmt> trueBranch,
				List<Stmt> falseBranch, Attribute... attributes) {
			super(attributes);
			this.condition = condition;
			this.trueBranch = new ArrayList<Stmt>(trueBranch);
			this.falseBranch = new ArrayList<Stmt>(falseBranch);
		}

		/**
		 * Construct an if-else statement from a condition, true branch and
		 * optional false branch.
		 *
		 * @param condition
		 *            May not be null.
		 * @param trueBranch
		 *            A list of zero or more statements to be executed when the
		 *            condition holds; may not be null.
		 * @param falseBranch
		 *            A list of zero of more statements to be executed when the
		 *            condition does not hold; may not be null.
		 * @param attributes
		 */
		public IfElse(Expr condition, List<Stmt> trueBranch,
				List<Stmt> falseBranch, Collection<Attribute> attributes) {
			super(attributes);
			this.condition = condition;
			this.trueBranch = new ArrayList<Stmt>(trueBranch);
			this.falseBranch = new ArrayList<Stmt>(falseBranch);
		}

		/**
		 * Get the if-condition.
		 *
		 * @return May not be null.
		 */
		public Expr getCondition() {
			return condition;
		}

		/**
		 * Get the true branch, which consists of zero or more statements.
		 *
		 * @return May not be null.
		 */
		public List<Stmt> getTrueBranch() {
			return trueBranch;
		}

		/**
		 * Get the false branch, which consists of zero or more statements.
		 *
		 * @return May not be null.
		 */
		public List<Stmt> getFalseBranch() {
			return falseBranch;
		}
	}

	/**
	 * Represents a break statement which immediately terminates the enclosing
	 * loop or switch statement
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Break extends SyntacticElement.Impl implements Stmt {
		/**
		 * Construct a break statement.
		 *
		 * @param attributes A sequence of zero or more attributes to be attached to the
		 *                   resulting AST node.
		 */
		public Break(Attribute... attributes) {
			super(attributes);
		}
	}

	/**
	 * Represents a continue statement which immediately moves a loop onto the
	 * next iteration.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Continue extends SyntacticElement.Impl implements Stmt {
		/**
		 * Construct a continue statement.
		 *
		 * @param attributes A sequence of zero or more attributes to be attached to the
		 *                   resulting AST node.
		 */
		public Continue(Attribute... attributes) {
			super(attributes);
		}
	}

	/**
	 * Represents a single case in a switch statement which matches against a
	 * given value. In the case of a null value, then this is a "default" case.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Case  extends SyntacticElement.Impl {
		/**
		 * The literal value to be matched by this case statement.
		 */
		private final Expr.Literal value;
		/**
		 * A sequence of zero or more statements making up the body of this case
		 * statement.
		 */
		private final ArrayList<Stmt> body;

		/**
		 * Construct a new case for a switch statement.
		 *
		 * @param value      The literal value to switch upon. This is <code>null</code>
		 *                   when this represents a <code>default</code> case.
		 * @param body       The statements making up the body of this case.
		 * @param attributes The attributes to be assocaited with the resulting AST
		 *                   node.
		 */
		public Case(Expr.Literal value, List<Stmt> body, Attribute... attributes) {
			super(attributes);
			this.value = value;
			this.body = new ArrayList<Stmt>(body);
		}

		/**
		 * @return The literal value being switched upon.
		 */
		public Expr.Literal getValue() {
			return value;
		}

		/**
		 * @return The body of this case.
		 */
		public List<Stmt> getBody() {
			return body;
		}

		/**
		 *
		 * @return <code>true</code> if this is a default case.
		 */
		public boolean isDefault() {
			return value == null;
		}
	}

	/**
	 * Represents a switch statement which selects between a number of different
	 * cases, based on a given value. The following illustrates:
	 *
	 * <pre>
	 * int f(int x) {
	 *  switch(x) {
	 *   case 1:
	 *     return 1;
	 *   case 0:
	 *     return 0;
	 *   default:
	 *     return -1;
	 *  }
	 * }
	 * </pre>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Switch extends SyntacticElement.Impl implements Stmt {
		/**
		 * When evaluated, this determines the value being switched upon.
		 */
		private final Expr expr;
		/**
		 * A sequence of one or more cases making up the body of this statement.
		 */
		private final ArrayList<Case> cases;

		/**
		 * Construct a switch statement from a given expression and a list of
		 * cases, of which one may be a "default".
		 *
		 * @param expr
		 *            The expression whose generated value is used to match
		 *            cases against
		 * @param cases
		 *            A list of zero or more cases, of which one may be a
		 *            "default" case.
		 * @param attributes
		 */
		public Switch(Expr expr, List<Case> cases, Attribute...attributes) {
			super(attributes);
			this.expr = expr;
			this.cases = new ArrayList<Case>(cases);
		}

		/**
		 * Construct a switch statement from a given expression and a list of
		 * cases, of which one may be a "default".
		 *
		 * @param expr
		 *            The expression whose generated value is used to match
		 *            cases against
		 * @param cases
		 *            A list of zero or more cases, of which one may be a
		 *            "default" case.
		 * @param attributes
		 */
		public Switch(Expr expr, List<Case> cases, Collection<Attribute> attributes) {
			super(attributes);
			this.expr = expr;
			this.cases = new ArrayList<Case>(cases);
		}

		/**
		 * Get the expression which this statement is matching against.
		 *
		 * @return The expression being switched up.
		 */
		public Expr getExpr() {
			return expr;
		}

		/**
		 * Get the list of zero or more cases which are used to match against
		 * the given expression.
		 *
		 * @return The list of cases.
		 */
		public List<Case> getCases() {
			return cases;
		}
	}

	/**
	 * Represents a variable declaration which is made up from a type, a
	 * variable name and an (optional) initialiser expression. If an initialiser
	 * is given, then this will be evaluated and assigned to the variable when
	 * the declaration is executed. Some example declarations:
	 *
	 * <pre>
	 * int x;
	 * int y = 1;
	 * int z = x + y;
	 * </pre>
	 *
	 * Observe that, unlike C and Java, declarations that declare multiple
	 * variables (separated by commas) are not permitted.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class VariableDeclaration extends SyntacticElement.Impl implements
			Stmt {
		/**
		 * Represents the declared type of the variable in question.
		 */
		private final Type type;
		/**
		 * Determines the name of the variable being declared.
		 */
		private final String name;
		/**
		 * Detemines the (optional) value to use to initialise the declared variable.
		 * May be <code>null</code> if no initialiser is given.
		 */
		private final Expr expr;

		/**
		 * Construct a variable declaration from a given type, variable name and
		 * optional initialiser expression.
		 *
		 * @param type
		 *            Type of variable being declared.
		 * @param name
		 *            Name of varaible being declared.
		 * @param expr
		 *            Optional initialiser expression, which may be null.
		 * @param attributes
		 */
		public VariableDeclaration(Type type, String name, Expr expr,
				Attribute... attributes) {
			super(attributes);
			this.type = type;
			this.name = name;
			this.expr = expr;
		}

		/**
		 * Construct a variable declaration from a given type, variable name and
		 * optional initialiser expression.
		 *
		 * @param type
		 *            Type of variable being declared.
		 * @param name
		 *            Name of varaible being declared.
		 * @param expr
		 *            Optional initialiser expression, which may be null.
		 * @param attributes
		 */
		public VariableDeclaration(Type type, String name, Expr expr,
				Collection<Attribute> attributes) {
			super(attributes);
			this.type = type;
			this.name = name;
			this.expr = expr;
		}

		@Override
		public String toString() {
			String r = getType() + " " + getName();
			if (getExpr() != null) {
				r = r + " = " + getExpr();
			}
			return r;
		}

		/**
		 * Get the type of the variable being declared.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public Type getType() {
			return type;
		}

		/**
		 * Get the name of the variable being declared.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Get the initialiser expression of the variable being declared (if
		 * present).
		 *
		 * @return May be null.
		 */
		public Expr getExpr() {
			return expr;
		}
	}
}
