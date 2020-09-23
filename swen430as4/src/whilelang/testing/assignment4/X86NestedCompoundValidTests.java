package whilelang.testing.assignment4;

import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class X86NestedCompoundValidTests extends AbstractX86ValidTests {

	/**
	 * The set of tests which are suitable for this part of the assignment
	 */
	private static String[] tests = {
			// Part 4 Tests
			"ArrayAccess_Valid_3",
			"ArrayAssign_Valid_10",
			"ArrayAssign_Valid_3",
			"ArrayAssign_Valid_4",
			"ArrayInitialiser_Valid_2",
			"ArrayLength_Valid_2",
			"For_Valid_4",
			"RecordAssign_Valid_6",
			"String_Valid_10",
			"String_Valid_12",
			"String_Valid_18",
			"String_Valid_1",
			"String_Valid_9",
	};

	public X86NestedCompoundValidTests(String testName) {
		super(testName);
	}

	// Here we enumerate all available test cases.
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return AbstractX86ValidTests.data(tests);
	}

}
