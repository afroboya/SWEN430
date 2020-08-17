

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import whilelang.ast.WhileFile;
import whilelang.compiler.Lexer;
import whilelang.compiler.Parser;
import whilelang.compiler.TypeChecker;
import whilelang.util.SyntaxError;
import whilelang.util.SyntaxError.InternalFailure;

@RunWith(Parameterized.class)
public class TypeCheckingTests {
private static final String WHILE_SRC_DIR = "test_files/invalid/typing/".replace('/', File.separatorChar);

	private final String testName;

	public TypeCheckingTests(String testName) {
		this.testName = testName;
	}

	// Here we enumerate all available test cases.
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> testcases = new ArrayList<Object[]>();
		for (File f : new File(WHILE_SRC_DIR).listFiles()) {
			if (f.isFile()) {
				String name = f.getName();
				if (name.endsWith(".while")) {
					// Get rid of ".while" extension
					String testName = name.substring(0, name.length() - 6);
					testcases.add(new Object[] { testName });
				}
			}
		}
		// Sort the result by filename
		Collections.sort(testcases, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				return ((String) o1[0]).compareTo((String) o2[0]);
			}
		});
		return testcases;
	}

	@Test
	public void valid() throws IOException {
		runTest(this.testName);
	}

	/**
	 * Run the interpreter over a given source file. This should not produce any
	 * exceptions.
	 *
	 * @param filename
	 * @throws IOException
	 */
	private void runTest(String testname) throws IOException {
		File srcFile = new File(WHILE_SRC_DIR + testname + ".while");
		// First, lex and parse the input file. If any errors occur here then
		// they are genuine errors.
		Lexer lexer = new Lexer(srcFile.getPath());
		Parser parser = new Parser(srcFile.getPath(), lexer.scan());
		WhileFile ast = parser.read();

		// Second, run type checker over the AST. We expect every test to
		// throw an error here. So, tests which do should pass, tests which
		// don't should fail.
		try {
			new TypeChecker().check(ast);
			fail();
		} catch (InternalFailure e) {
			throw e;
		} catch (SyntaxError e) {
			String expected = testname.split("-")[1];
			String code = e.code();
			
			assertTrue("Expected '" + expected + "' but found '" + code + "'", expected.equals(code));
			
			// Success!
			e.outputSourceError(System.err);
		}
	}
}
