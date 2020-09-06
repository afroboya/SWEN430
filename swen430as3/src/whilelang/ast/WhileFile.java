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
import java.util.List;

import whilelang.util.SyntacticElement;

/**
 * Represents the Abstract Syntax Tree for a given While language source file.
 *
 * @author David J. Pearce
 *
 */
public class WhileFile {
	/**
	 * The name of the originating source file.
	 */
	public final String filename;

	/**
	 * The list of declarations within the source file.
	 */
	public final ArrayList<Decl> declarations;

	/**
	 * Construct a new AST representation for a given source file.
	 *
	 * @param filename
	 * @param decls
	 */
	public WhileFile(String filename, List<Decl> decls) {
		this.filename = filename;
		this.declarations = new ArrayList<Decl>(decls);
	}

	/**
	 * Represents an arbitrary (named) declaration within a source file.
	 * @author David J. Pearce
	 *
	 */
	public interface Decl extends SyntacticElement {

		/**
		 * @return Return the name of this declaration.
		 */
		public String name();
	}

	/**
	 * Represents a type declaration within a given <code>WhileFile</code>.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class TypeDecl extends SyntacticElement.Impl implements Decl {
		/**
		 * The underlying type for this declaration.
		 */
		private final Type type;
		/**
		 * The name for the newly declared type.
		 */
		private final String name;

		/**
		 * Construct a new type declaration.
		 *
		 * @param type       The type for this declaration. May not be null.
		 * @param name       The name of this declaration. May not be null.
		 * @param attributes
		 */
		public TypeDecl(Type type, String name, Attribute... attributes) {
			super(attributes);
			this.type = type;
			this.name = name;
		}

		@Override
		public String name() {
			return getName();
		}

		@Override
		public String toString() {
			return "type " + getType() + " is " + getName();
		}

		/**
		 *
		 * @return The underyling type for this declaration.
		 */
		public Type getType() {
			return type;
		}

		/**
		 * @return The name of this declaration
		 */
		public String getName() {
			return name;
		}
	}

	/**
	 * Represents a method declaration within a given <code>WhileFile</code>.
	 *
	 * @author David J. Pearce
	 *
	 */
	public final static class MethodDecl extends SyntacticElement.Impl implements Decl {

		/**
		 * Determines the name of this method.
		 */
		private final String name;
		/**
		 * Determines the return type of this method.
		 */
		private final Type ret;
		/**
		 * Determines the declared parameters of this method.
		 */
		private final ArrayList<Parameter> parameters;
		/**
		 * Determines the body of this method.
		 */
		private final ArrayList<Stmt> statements;

		/**
		 * Construct an object representing a While function.
		 *
		 * @param name
		 *            - The name of the function.
		 * @param ret
		 *            - The return type of this method
		 * @param parameters
		 *            - The list of parameter names and their types for this
		 *            method
		 * @param statements
		 *            - The Statements making up the function body.
		 * @param attributes
		 */
		public MethodDecl(String name, Type ret, List<Parameter> parameters, List<Stmt> statements,
				Attribute... attributes) {
			super(attributes);
			this.name = name;
			this.ret = ret;
			this.parameters = new ArrayList<Parameter>(parameters);
			this.statements = new ArrayList<Stmt>(statements);
		}

		@Override
		public String name() {
			return getName();
		}

		/**
		 * @return The name of this method.
		 */
		public String getName() {
			return name;
		}

		/**
		 *
		 * @return The return type of this method.
		 */
		public Type getRet() {
			return ret;
		}

		/**
		 *
		 * @return The declared parameters for this method.
		 */
		public List<Parameter> getParameters() {
			return parameters;
		}

		/**
		 *
		 * @return The body of this method.
		 */
		public List<Stmt> getBody() {
			return statements;
		}
	}

	/**
	 * Represents a parameter declaration within a method declaration.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static final class Parameter extends SyntacticElement.Impl implements Decl {
		/**
		 * Determines the type of the given parameter.
		 */
		private final Type type;
		/**
		 * Determines the name of the given parameter.
		 */
		private final String name;

		/**
		 * Construct a new parameter declaration.
		 *
		 * @param type
		 * @param name
		 * @param attributes
		 */
		public Parameter(Type type, String name, Attribute... attributes) {
			super(attributes);
			this.type = type;
			this.name = name;
		}

		@Override
		public String name() {
			return getName();
		}

		/**
		 * @return The type of this parameter.
		 */
		public Type getType() {
			return type;
		}

		/**
		 *
		 * @return The name of this parameter.
		 */
		public String getName() {
			return name;
		}
	}
}
