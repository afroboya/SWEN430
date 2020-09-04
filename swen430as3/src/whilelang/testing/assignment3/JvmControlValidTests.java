package whilelang.testing.assignment3;

import java.util.Arrays;
import java.util.Collection;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class JvmControlValidTests extends AbstractJvmValidTests {

	/**
	 * The set of tests which are suitable for this part of the assignment
	 */
	private static String[] tests = {
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

	public JvmControlValidTests(String testName) {
		super(testName);
	}

	// Here we enumerate all available test cases.
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return AbstractJvmValidTests.data(tests);
	}
}
