package org.benchmarx.dsl.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.benchmarx.dsl.benchmarxDSL.BxModel;
import org.benchmarx.dsl.generator.BenchmarxDSLGenerator;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.util.CancelIndicator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(BenchmarxDSLInjectorProvider.class)
public class GeneratorTest {

	@Inject ParseHelper<BxModel> parseHelper;
	@Inject BenchmarxDSLGenerator generator;

	private static final String DEFAULT = IFileSystemAccess.DEFAULT_OUTPUT;

	@Test
	void generates_test_case_and_decisions_for_minimal_problem() throws Exception {
		BxModel model = parseHelper.parse("""
				problem MyBX {
				  source metamodel "src/My.ecore"
				  target metamodel "tgt/My.ecore"
				  decisions { PREFER_A_TO_B }
				}
				suite MySuite for MyBX {
				  test createBasic {
				    category: batch.forward
				    edit source { createRoot }
				    postcondition { source: Empty, target: Empty }
				  }
				}
				""");
		assertNotNull(model);
		assertTrue(model.eResource().getErrors().isEmpty());

		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		generator.doGenerate(model.eResource(), fsa, CancelIndicator.NullImpl);

		Map<String, CharSequence> files = fsa.getTextFiles();

		assertTrue(files.containsKey(DEFAULT + "org/benchmarx/examples/mybx/testsuite/MyBXTestCase.java"),
				"TestCase not generated");
		assertTrue(files.containsKey(DEFAULT + "org/benchmarx/examples/mybx/testsuite/MyBXParameterResolver.java"),
				"ParameterResolver not generated");
		assertTrue(files.containsKey(DEFAULT + "org/benchmarx/examples/mybx/testsuite/Decisions.java"),
				"Decisions not generated");
		assertTrue(files.containsKey(DEFAULT + "org/benchmarx/examples/mybx/testsuite/batch/fwd/BatchForward.java"),
				"BatchForward not generated");
	}

	@Test
	void decisions_enum_contains_declared_constants() throws Exception {
		BxModel model = parseHelper.parse("""
				problem Foo {
				  source metamodel "src/Foo.ecore"
				  target metamodel "tgt/Foo.ecore"
				  decisions { OPTION_A, OPTION_B }
				}
				suite FooSuite for Foo {
				  test dummy {
				    category: batch.forward
				    edit source { op1 }
				    postcondition { source: S1, target: T1 }
				  }
				}
				""");

		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		generator.doGenerate(model.eResource(), fsa, CancelIndicator.NullImpl);

		String decisions = fsa.getTextFiles()
				.get(DEFAULT + "org/benchmarx/examples/foo/testsuite/Decisions.java").toString();
		assertTrue(decisions.contains("OPTION_A"), "Missing OPTION_A");
		assertTrue(decisions.contains("OPTION_B"), "Missing OPTION_B");
		assertTrue(decisions.contains("DO NOT EDIT"), "Missing DO NOT EDIT marker");
	}

	@Test
	void batch_forward_test_names_are_emitted() throws Exception {
		BxModel model = parseHelper.parse("""
				problem Bar {
				  source metamodel "src/Bar.ecore"
				  target metamodel "tgt/Bar.ecore"
				}
				suite BarSuite for Bar {
				  test testAddNode {
				    category: batch.forward
				    edit source { addNode }
				    postcondition { source: S1, target: T1 }
				  }
				  test testRemoveNode {
				    category: batch.forward
				    edit source { removeNode }
				    postcondition { source: S2, target: T2 }
				  }
				}
				""");

		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		generator.doGenerate(model.eResource(), fsa, CancelIndicator.NullImpl);

		String batchFwd = fsa.getTextFiles()
				.get(DEFAULT + "org/benchmarx/examples/bar/testsuite/batch/fwd/BatchForward.java").toString();
		assertTrue(batchFwd.contains("public void testAddNode"), "testAddNode not generated");
		assertTrue(batchFwd.contains("public void testRemoveNode"), "testRemoveNode not generated");
	}

	@Test
	void helper_stubs_are_generated_for_encountered_operations() throws Exception {
		BxModel model = parseHelper.parse("""
				problem Baz {
				  source metamodel "src/Baz.ecore"
				  target metamodel "tgt/Baz.ecore"
				}
				suite BazSuite for Baz {
				  test t1 {
				    category: batch.forward
				    edit source { opAlpha, opBeta }
				    postcondition { source: S, target: T }
				  }
				}
				""");

		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		generator.doGenerate(model.eResource(), fsa, CancelIndicator.NullImpl);

		String srcHelper = fsa.getTextFiles()
				.get(DEFAULT + "org/benchmarx/examples/baz/helpers/BazSrcHelper.java").toString();
		assertTrue(srcHelper.contains("public void opAlpha"), "opAlpha not in SrcHelper stub");
		assertTrue(srcHelper.contains("public void opBeta"),  "opBeta not in SrcHelper stub");
	}

	@Test
	void no_decisions_file_generated_when_no_decisions_declared() throws Exception {
		BxModel model = parseHelper.parse("""
				problem NoDec {
				  source metamodel "src/NoDec.ecore"
				  target metamodel "tgt/NoDec.ecore"
				}
				suite NoDecSuite for NoDec {
				  test t1 {
				    category: batch.forward
				    edit source { op1 }
				    postcondition { source: S, target: T }
				  }
				}
				""");

		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		generator.doGenerate(model.eResource(), fsa, CancelIndicator.NullImpl);

		assertFalse(fsa.getTextFiles().containsKey(
				DEFAULT + "org/benchmarx/examples/nodec/testsuite/Decisions.java"),
				"Decisions.java should not be generated when no decisions declared");
	}
}
