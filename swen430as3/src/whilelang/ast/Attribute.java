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

/**
 * Represents an attribute which can be attached to an AST node.
 *
 * @author David J. Pearce
 *
 */
public interface Attribute {

	/**
	 * Represents the location within a source-level where a given AST node
	 * orginated.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Source implements Attribute {
		/**
		 * The starting character position within the source file.
		 */
		public final int start;

		/**
		 * The last character position within the source file.
		 */
		public final int end;

		/**
		 * Construct a new source attribute which can be attached to a given AST node.
		 *
		 * @param start
		 * @param end
		 */
		public Source(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return "@" + start + ":" + end;
		}
	}

	/**
	 * Represents the inferred type for a given AST node representing an expression,
	 * as determined by the type checker.
	 *
	 * @author David J. Pearce
	 *
	 */
	public static class Type implements Attribute {

		/**
		 * The type associate with the given AST node
		 */
		public final whilelang.ast.Type type;

		/**
		 * Construct a new type attribute which can be attached to a given AST node.
		 *
		 * @param type
		 */
		public Type(whilelang.ast.Type type) {
			this.type = type;
		}
	}
}
