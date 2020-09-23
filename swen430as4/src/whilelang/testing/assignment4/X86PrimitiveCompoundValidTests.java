package whilelang.testing.assignment4;

import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

public class X86PrimitiveCompoundValidTests extends AbstractX86ValidTests {

	/**
	 * The set of tests which are suitable for this part of the assignment
	 */
	private static String[] tests = {
			// Part 3 Tests
			"ArrayAccess_Valid_1",
			"ArrayAssign_Valid_1",
			"ArrayAssign_Valid_2",
			"ArrayAssign_Valid_5",
			"ArrayAssign_Valid_6",
			"ArrayAssign_Valid_7",
			"ArrayAssign_Valid_8",
			"ArrayAssign_Valid_9",
			"ArrayEmpty_Valid_1",
			"ArrayEquals_Valid_1",
			"ArrayGenerator_Valid_1",
			"ArrayGenerator_Valid_2",
			"ArrayInitialiser_Valid_1",
			"ArrayInitialiser_Valid_3",
			"ArrayLength_Valid_1",
			"ArrayLength_Valid_3",
			"BoolArray_Valid_1",
			"BoolArray_Valid_2",
			"BoolIfElse_Valid_1",
			"BoolIfElse_Valid_2",
			"BoolRecord_Valid_1",
			"BoolRecord_Valid_2",
			"Char_Valid_2",
			"Char_Valid_3",
			"IfElse_Valid_1",
			"IntEquals_Valid_1",
			"Method_Valid_1",
			"RecordAccess_Valid_2",
			"RecordAssign_Valid_1",
			"RecordAssign_Valid_2",
			"RecordAssign_Valid_3",
			"RecordAssign_Valid_4",
			"RecordAssign_Valid_5",
			"RecordDefine_Valid_1",
			"String_Valid_11",
			"String_Valid_13",
			"String_Valid_15",
			"String_Valid_6",
			"String_Valid_7",
			"Switch_Valid_11",
			"Switch_Valid_12",
			"Switch_Valid_13",
			"Switch_Valid_14",
			"Switch_Valid_15",
			"Switch_Valid_16",
			"Switch_Valid_23",
			"Switch_Valid_24",
			"Switch_Valid_4",
			"Switch_Valid_7",
			"TypeDecl_Valid_1",
			"TypeDecl_Valid_2",
			"While_Valid_1",
			"While_Valid_2",
			"While_Valid_3",
			"While_Valid_4",
			"While_Valid_5"
	};

	public X86PrimitiveCompoundValidTests(String testName) {
		super(testName);
	}

	// Here we enumerate all available test cases.
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return AbstractX86ValidTests.data(tests);
	}

}
