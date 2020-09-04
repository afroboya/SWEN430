package whilelang.testing.assignment3;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.junit.*;
import org.junit.runners.Parameterized.Parameters;

import whilelang.ast.WhileFile;
import whilelang.compiler.ClassFileWriter;
import whilelang.compiler.WhileCompiler;
import whilelang.util.Interpreter;

public abstract class AbstractJvmValidTests {
	private static final String WHILE_SRC_DIR = "tests/valid/".replace('/', File.separatorChar);

	private final String testName;

	public AbstractJvmValidTests(String testName) {
		this.testName = testName;
	}

	// Here we enumerate all available test cases.
	public static Collection<Object[]> data(String... tests) {
		HashSet<String> allowedTests = new HashSet<String>();
		for(String t : tests) {
			allowedTests.add(t);
		}

		ArrayList<Object[]> testcases = new ArrayList<Object[]>();
		for (File f : new File(WHILE_SRC_DIR).listFiles()) {
			if (f.isFile()) {
				String name = f.getName();
				if (name.endsWith(".while")) {
					// Get rid of ".while" extension
					String testName = name.substring(0, name.length() - 6);
					if(allowedTests.contains(testName)) {
						testcases.add(new Object[] { testName });
					}
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
	public void valid() throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		compileTest(this.testName);
		executeTest(this.testName);
	}

	/**
	 * Compiler the source file into a JVM classfile
	 *
	 * @param filename
	 * @throws IOException
	 */
	private void compileTest(String testname) throws IOException {
		// First, compile the source file into a class file
		String sourceFilename = WHILE_SRC_DIR + testname + ".while";
		String classFilename = WHILE_SRC_DIR + testname + ".class";
		WhileCompiler compiler = new WhileCompiler(sourceFilename);
		WhileFile ast = compiler.compile();
		new ClassFileWriter(classFilename).write(ast);
	}

	/**
	 * Execute the generate class file on the JVM using reflection.
	 *
	 * @param filename
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void executeTest(String testname) throws IOException, ClassNotFoundException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// Now, we attempt to run the test using reflection
		Class testClass = Class.forName(testname);
		System.out.println("testname: "+testname);
		System.out.println("testClass: "+ Arrays.toString(testClass.getFields()));
		Method m = testClass.getMethod("main");
		m.invoke(null);
	}
}
