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

import whilelang.util.Pair;
import whilelang.util.SyntacticElement;

/**
 * Represents an expression in the source code of a While program. Many standard
 * expression kinds are provided, including unary operations (e.g.
 * <code>!e</code>, <code>-e</code>, <code>|e|</code>), binary operations (e.g.
 * <code>x==y</code>, <code>x!=y</code>, <code>x+y</code>, etc), list
 * expressions (e.g. <code>ls[i]</code>, <code>[1,2,3]</code>, etc), record
 * expressions (e.g. <code>r.f</code>, <code>{x: 1, y: 2}</code>, etc).
 */
public interface Expr extends SyntacticElement {

	/**
	 * Captures the expression kinds which are permitted on the left-side of an
	 * assignment statement.
	 *
	 * @author David J. Pearce
	 *
	 */
	public interface LVal extends Expr {
	}

	public static class Cast extends SyntacticElement.Impl implements Expr{

		private final Type castType;
		private final Expr expr;

		public Cast(Type castType,Expr expr, Attribute... attributes) {
			super(attributes);
			this.castType = castType;
			this.expr = expr;
		}

		public Type getCastType() {
			return castType;
		}

		public Expr getExpr() {
			return expr;
		}

		@Override
		public String toString() {
			return "("+castType.toString()+")"+" "+expr.toString();
		}


	}

	/**
	 * Represents a single occurrence of a variable within a expression. For
	 * example, the expression <code>x+x+y</code> will contain three instances
	 * of <code>Variable</code> --- one for each variable usage.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Variable extends SyntacticElement.Impl implements Expr,
			LVal {

		/**
		 * The actual variable name
		 */
		private String name;

		/**
		 * Construct a variable expression from a given variable name.
		 *
		 * @param name
		 *            Must be non-null.
		 * @param attributes
		 */
		public Variable(String name, Attribute... attributes) {
			super(attributes);
			this.setVar(name);
		}

		@Override
		public String toString() {
			return getName();
		}

		/**
		 * Get the name of the variable in question.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the variable name of this variable access expression.
		 *
		 * @param var
		 */
		public void setVar(String var) {
			this.name = var;
		}
	}

	/**
	 * Represents the occurrence of a literal value within an expression. For
	 * example, in the expression <code>1+x</code>, an instance of
	 * <code>Literal</code> is used to represent the value <code>1</code>.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Literal extends SyntacticElement.Impl implements Expr {

		/**
		 * The actual value this literal represents.
		 */
		private Object value;

		/**
		 * Construct a constant expression from a given (primitive) value. The
		 * value must be either a boolean, character, integer, real, or string
		 * constant; alternative, it can be null (to signal the null constant).
		 *
		 * @param value
		 *            Must be an instance of <code>java.lang.Boolean</code>,
		 *            <code>java.lang.Character</code>,
		 *            <code>java.lang.Integer</code>,
		 *            <code>java.lang.String</code>.
		 * @param attributes
		 */
		public Literal(Object value, Attribute... attributes) {
			super(attributes);
			this.value = value;
		}

		/**
		 * Construct a constant expression from a given (primitive) value. The
		 * value must be either a boolean, character, integer, real, or string
		 * constant; alternative, it can be null (to signal the null constant).
		 *
		 * @param value
		 *            Must be an instance of <code>java.lang.Boolean</code>,
		 *            <code>java.lang.Character</code>,
		 *            <code>java.lang.Integer</code>,
		 *            <code>java.lang.String</code>.
		 * @param attributes
		 */
		public Literal(Object value, Collection<Attribute> attributes) {
			super(attributes);
			this.value = value;
		}

		@Override
		public String toString() {
			if(value == null) {
				return "null";
			} else {
				return value.toString();
			}
		}

		/**
		 * Get the value represented by this constant, which must be an instance of
		 * <code>java.lang.Boolean</code>, <code>java.lang.Character</code>,
		 * <code>java.lang.Integer</code>, <code>java.lang.Double</code>,
		 * <code>java.lang.String</code> or <code>null</code>.
		 *
		 * @return The actual value this literal represents.
		 */
		public Object getValue() {
			return value;
		}
	}

	/**
	 * The kind of a binary operator.
	 *
	 * @author David J. Pearce
	 *
	 */
	public enum BOp {
		/**
		 * Represents logical AND
		 */
		AND {

			@Override
			public String toString() {
				return "&&";
			}
		},
		/**
		 * Represents logical OR
		 */
		OR {

			@Override
			public String toString() {
				return "||";
			}
		},
		/**
		 * Represents integer addition
		 */
		ADD {

			@Override
			public String toString() {
				return "+";
			}
		},
		/**
		 *
		 * Represents integer subtraction
		 */
		SUB {

			@Override
			public String toString() {
				return "-";
			}
		},
		/**
		 * Represents integer multiplication
		 */
		MUL {

			@Override
			public String toString() {
				return "*";
			}
		},
		/**
		 * Represents integer division
		 */
		DIV {

			@Override
			public String toString() {
				return "/";
			}
		},
		/**
		 * Represents integer remainder
		 */
		REM {

			@Override
			public String toString() {
				return "%";
			}
		},
		/**
		 * Represents equality
		 */
		EQ {

			@Override
			public String toString() {
				return "==";
			}
		},
		/**
		 * Represents inequality.
		 */
		NEQ {

			@Override
			public String toString() {
				return "!=";
			}
		},
		/**
		 * Represents integer less-than.
		 */
		LT {

			@Override
			public String toString() {
				return "<";
			}
		},
		/**
		 * Represents integer less-than-or-equal.
		 */
		LTEQ {

			@Override
			public String toString() {
				return "<=";
			}
		},
		/**
		 * Represents integer greater-than.
		 */
		GT {

			@Override
			public String toString() {
				return ">";
			}
		},
		/**
		 * Represents integer greater-than.
		 */
		GTEQ {
			@Override
			public String toString() {
				return ">=";
			}
		}
	};

	/**
	 * Represents a binary expression, composed recursively from a left-hand
	 * side and right-hand side. For example, in the expression <code>1+x</code>
	 * we have an instance of <code>Binary</code> to represent the addition
	 * whose left-hand side is an instance of <code>Constant</code> and
	 * right-hand side is an instance of <code>Variable</code>.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Binary extends SyntacticElement.Impl implements Expr {
		/**
		 * The kind of binary operator this object represents.
		 */
		private final BOp op;
		/**
		 * The left-hand side of this binary expression.
		 */
		private final Expr lhs;
		/**
		 * The right-hand size of this binary expression.
		 */
		private final Expr rhs;

		/**
		 * Construct a binary expression from a given left-hand expression and
		 * right-hand expression.
		 *
		 * @param op
		 *            The operation this expression descibes; may not be null.
		 * @param lhs
		 *            The left-hand side; may not be null.
		 * @param rhs
		 *            The right-hand side; may not be null.
		 * @param attributes
		 */
		public Binary(BOp op, Expr lhs, Expr rhs, Attribute... attributes) {
			super(attributes);
			this.op = op;
			this.lhs = lhs;
			this.rhs = rhs;
		}

		/**
		 * Construct a binary expression from a given left-hand expression and
		 * right-hand expression.
		 *
		 * @param op
		 *            The operation this expression descibes; may not be null.
		 * @param lhs
		 *            The left-hand side; may not be null.
		 * @param rhs
		 *            The right-hand side; may not be null.
		 * @param attributes
		 */
		public Binary(BOp op, Expr lhs, Expr rhs,
				Collection<Attribute> attributes) {
			super(attributes);
			this.op = op;
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public String toString() {
			return "(" + getOp() + " " + getLhs() + " " + getRhs() + ")";
		}

		/**
		 * Get the left-hand side of this binary expression.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public Expr getLhs() {
			return lhs;
		}

		/**
		 * Get the right-hand side of this binary expression.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public Expr getRhs() {
			return rhs;
		}

		/**
		 * Get the operation that this binary expression represents.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public BOp getOp() {
			return op;
		}
	}

	/**
	 * <p>
	 * Represents a list or string access expression, which may also form the
	 * left-hand side of an assignment. For example,
	 * <code>return 1 + xs[i]</code> and <code>ls[i] = 1</code> are valid uses
	 * of a list access expression.
	 * <p>
	 * <p>
	 * Array or string access expressions can give rise to index-out-of-bounds
	 * exceptions, in the case that the index is negative or larger or equal to
	 * the size of the source list.
	 * </p>
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class IndexOf extends SyntacticElement.Impl implements
			Expr, LVal {
		/**
		 * The source expression returns an element of array type.
		 */
		private final Expr source;
		/**
		 * The index expression returns an element of integer type.
		 */
		private final Expr index;

		/**
		 * Create a list access expression from a given source expression (which
		 * must evaluate to a list value) and index expression (which must
		 * evaluate to an integer value).
		 *
		 * @param source
		 *            The source expression which generates a list value; may
		 *            not be null.
		 * @param index
		 *            The index expression which determines the list element to
		 *            return; may not be null.
		 * @param attributes
		 */
		public IndexOf(Expr source, Expr index, Attribute... attributes) {
			super(attributes);
			this.source = source;
			this.index = index;
		}

		/**
		 * Create a list access expression from a given source expression (which
		 * must evaluate to a list value) and index expression (which must
		 * evaluate to an integer value).
		 *
		 * @param src
		 *            The source expression which generates a list value; may
		 *            not be null.
		 * @param index
		 *            The index expression which determines the list element to
		 *            return; may not be null.
		 * @param attributes
		 */
		public IndexOf(Expr src, Expr index, Collection<Attribute> attributes) {
			super(attributes);
			this.source = src;
			this.index = index;
		}

		@Override
		public String toString() {
			return source + "[" + index + "]";
		}

		/**
		 * Get the source expression for this list access.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public Expr getSource() {
			return source;
		}

		/**
		 * Get the index expression for this list access.
		 *
		 * @return Guaranteed to be non-null.
		 */
		public Expr getIndex() {
			return index;
		}
	}

	/**
	 * The kind of a unary operator.
	 *
	 * @author David J. Pearce
	 *
	 */
	public enum UOp {
		/**
		 * Represents logical not.
		 */
		NOT,
		/**
		 * Represents integer negation.
		 */
		NEG,
		/**
		 * Represents array length.
		 */
		LENGTHOF,
	}

	/**
	 * Represents a unary expression, composed recursively from a single
	 * expression. For example, in the expression <code>!x</code> which have a
	 * <code>Unary</code> instance representing the logical inversion whose
	 * sub-expression is a <code>Variable</code> instance.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Unary extends SyntacticElement.Impl implements Expr {

		/**
		 * The kind of unary operator this expression represents.
		 */
		private final UOp op;
		/**
		 * The operand being manipulated by this expression.
		 */
		private final Expr expr;

		/**
		 * Construct a unary expression from a given unary operation, and
		 * sub-expression.
		 *
		 * @param op
		 *            The unary operation this expression represents; may not be
		 *            null.
		 * @param expr
		 *            The sub-expression whose value will be manipulated by the
		 *            unary operation; may not be null.
		 * @param attributes
		 */
		public Unary(UOp op, Expr expr, Attribute... attributes) {
			super(attributes);
			this.op = op;
			this.expr = expr;
		}

		@Override
		public String toString() {
			return op + expr.toString();
		}

		/**
		 * Get the operation this unary expression represents; guaranteed to be
		 * non-null.
		 *
		 * @return The kind of unary operator this object represents.
		 */
		public UOp getOp() {
			return op;
		}

		/**
		 * Get the sub-expression this unary expression operates over; guaranteed to be
		 * non-null.
		 *
		 * @return The operand expression being manipulated by this expression.
		 */
		public Expr getExpr() {
			return expr;
		}
	}

	/**
	 * Represents an array generate which constructs an array value from a
	 * default value and a size. For example, <code>[1;3]</code> generates the
	 * array <code>[1,1,1]</code> whilst <code>[1;0]</code> generates the array
	 * <code>[]</code>.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class ArrayGenerator extends SyntacticElement.Impl implements
			Expr {

		/**
		 * Determines the value used to populate all elements of the new array with.
		 */
		private final Expr value;
		/**
		 * Determines the size of the array to create.
		 */
		private final Expr size;

		/**
		 * Construct an array generator expression from a default value and size
		 *
		 * @param value      Determines the value to populate the array with.
		 * @param size       Determines the size of the resulting array.
		 *
		 * @param attributes
		 */
		public ArrayGenerator(Expr value, Expr size, Attribute... attributes) {
			super(attributes);
			this.value = value;
			this.size = size;
		}

		/**
		 * Get the default value used by this generator
		 *
		 * @return The value expression itself.
		 */
		public Expr getValue() {
			return value;
		}

		/**
		 * Get the size expression which determines the length of the generated array
		 *
		 * @return The size expression itself.
		 */
		public Expr getSize() {
			return size;
		}
	}

	/**
	 * Represents an array initialiser which constructs an array value from zero
	 * or more element expressions. For example, <code>[1,2,3]</code>,
	 * <code>[1,x]</code> and <code>[]</code> are valid list constructor
	 * expressions.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class ArrayInitialiser extends SyntacticElement.Impl implements
			Expr {

		/**
		 * The sequence of zero or more expressions which determine the elements used to
		 * initialise the array with.
		 */
		private final ArrayList<Expr> arguments;

		/**
		 * Construct a list constructor expression from a list of zero or more
		 * element expressions.
		 *
		 * @param arguments
		 *            A list of zero or more element expressions; may not be
		 *            null.
		 * @param attributes
		 */
		public ArrayInitialiser(Collection<Expr> arguments,
				Attribute... attributes) {
			super(attributes);
			this.arguments = new ArrayList<Expr>(arguments);
		}

		/**
		 * Construct a list constructor expression from a list of zero or more element
		 * expressions.
		 *
		 * @param attribute This is attached to the resulting expression node.
		 *
		 * @param arguments A list of zero or more element expressions; may not be null.
		 */
		public ArrayInitialiser(Attribute attribute, Expr... arguments) {
			super(attribute);
			this.arguments = new ArrayList<Expr>();
			for (Expr a : arguments) {
				this.getArguments().add(a);
			}
		}

		/**
		 * Get the list of element expressions used in this list constructor
		 * expression.
		 *
		 * @return A list of zero or more expression; guaranteed to be non-null.
		 */
		public List<Expr> getArguments() {
			return arguments;
		}
	}

	/**
	 * Represents a record access expression, which is composed of a <i>source
	 * expression</i> and <i>field name</i>. For example, in the expression
	 * <code>x.f</code> the variable <code>x</code> is the source expression,
	 * whilst the field name is <code>f</code>.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class RecordAccess extends SyntacticElement.Impl implements
			LVal {

		/**
		 * The source expression returns a value of record type.
		 */
		private final Expr source;
		/**
		 * Determines the field name to look up in the given record.
		 */
		private final String name;

		/**
		 * Construct a record access expression from a given source expression
		 * and field name.
		 *
		 * @param source
		 *            An expression which must evaluate to a record containing a
		 *            field with the given name; may not be null.
		 * @param name
		 *            The name of a field contained within the record that the
		 *            source expression evaluates to; may not be null.
		 * @param attributes
		 */
		public RecordAccess(Expr source, String name, Attribute... attributes) {
			super(attributes);
			this.source = source;
			this.name = name;
		}

		@Override
		public String toString() {
			return getSource() + "." + getName();
		}

		/**
		 * Get the source expression for this record access.
		 *
		 * @return The source expression.
		 */
		public Expr getSource() {
			return source;
		}

		/**
		 * Get the field name for this record access.
		 *
		 * @return The field name.
		 */
		public String getName() {
			return name;
		}
	}

	/**
	 * Represents a record construct expression, which constructs a list value
	 * from a number of <i>field expressions</i>. For example, the expression
	 * <code>{x: 1, y: 2}</code> evaluates to produce a record containing the
	 * fields <code>x</code> and <code>y</code> which (respectively) hold the
	 * values <code>1</code> and <code>2</code>.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class RecordConstructor extends SyntacticElement.Impl
			implements Expr {

		/**
		 * The sequence of expressions used to initialse the record with, along with the
		 * associated field names.
		 */
		private final ArrayList<Pair<String, Expr>> fields;

		/**
		 * Construct a record constructor expression from a given mapping of
		 * field names to their generating expressions.
		 *
		 * @param fields
		 *            A map of zero or more field names to generating
		 *            expressions; may not be null.
		 * @param attributes
		 */
		public RecordConstructor(List<Pair<String, Expr>> fields,
				Attribute... attributes) {
			super(attributes);
			this.fields = new ArrayList<Pair<String, Expr>>(fields);
		}

		/**
		 * Get the mapping from field names to generating expressions;
		 * guaranteed to be non-null.
		 *
		 * @return The sequence of field initialier expressions.
		 */
		public List<Pair<String, Expr>> getFields() {
			return fields;
		}
	}

	/**
	 * Represents a function invocation, which is composed of a <i>function
	 * name</i> and zero or more <i>argument expressions</i>. For example,
	 * <code>f(1)</code> is an invocation expression for the function named
	 * <code>f</code> which accepts a single argument.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Invoke extends SyntacticElement.Impl implements Expr,
			Stmt {

		/**
		 * The name of the method to invoke.
		 */
		private final String name;

		/**
		 * The sequence of expression which, when evaluated, determine the arguments to
		 * pass to the invoked method.
		 */
		private final ArrayList<Expr> arguments;

		/**
		 * Construct a function invocation expression from a given function name
		 * and list of zero or more argument expressions.
		 *
		 * @param name
		 *            The function name that this invocation will call; may not
		 *            be null.
		 * @param arguments
		 *            The list of zero or more argument expressions; may not be
		 *            null.
		 * @param attributes
		 */
		public Invoke(String name, List<Expr> arguments,
				Attribute... attributes) {
			super(attributes);
			this.name = name;
			this.arguments = new ArrayList<Expr>(arguments);
		}

		/**
		 * Get the function name being invoked in this expression; guaranteed to
		 * be non-null.
		 *
		 * @return The name of the method being invoked.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Get the list of zero or more argument expression being pass to the
		 * function being invoked in this expression; guaranteed to be non-null.
		 *
		 * @return The list of argument expressions.
		 */
		public List<Expr> getArguments() {
			return arguments;
		}
	}
}
