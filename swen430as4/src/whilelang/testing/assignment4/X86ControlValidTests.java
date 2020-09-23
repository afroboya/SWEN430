package whilelang.testing.assignment4;

import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class X86ControlValidTests extends AbstractX86ValidTests {

	/**
	 * The set of tests which are suitable for this part of the assignment
	 */
	private static String[] tests = {
			// Part 2 Tests
			"For_Valid_1",
			"For_Valid_2",
			"For_Valid_3",
			"For_Valid_5",
			"Switch_Valid_10",
			"Switch_Valid_17",
			"Switch_Valid_18",
			"Switch_Valid_19",
			"Switch_Valid_1",
			"Switch_Valid_20",
			"Switch_Valid_22",
			"Switch_Valid_2",
			"Switch_Valid_3",
			"Switch_Valid_6",
			"Switch_Valid_8",
			"Switch_Valid_9",
			"While_Valid_7",
			"While_Valid_8",
	};

	public X86ControlValidTests(String testName) {
		super(testName);
	}

	// Here we enumerate all available test cases.
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return AbstractX86ValidTests.data(tests);
	}

}
