package org.benchmarx.dsl.tests;

import org.benchmarx.dsl.benchmarxDSL.BenchmarxDSLPackage;
import org.benchmarx.dsl.benchmarxDSL.BxModel;
import org.benchmarx.dsl.validation.BenchmarxDSLValidator;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(BenchmarxDSLInjectorProvider.class)
public class ValidationTest {

	@Inject ParseHelper<BxModel> parseHelper;
	@Inject ValidationTestHelper validationHelper;

	private static final String PREAMBLE = """
			problem P {
			  source metamodel "a.ecore"
			  target metamodel "b.ecore"
			}
			state s : P.source { R {} }
			state t : P.target { R {} }
			""";

	private BxModel parse(String text) throws Exception {
		return parseHelper.parse(text);
	}

	// ── checkEditMatchesCategory ──────────────────────────────────────────

	@Test
	void batchForward_with_source_edit_is_valid() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 {
				    category : batch.forward
				    edit source { op1 }
				    postcondition { source : s, target : t }
				  }
				}
				""");
		validationHelper.assertNoErrors(m);
	}

	@Test
	void batchForward_with_target_edit_is_error() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 {
				    category : batch.forward
				    edit target { op1 }
				    postcondition { source : s, target : t }
				  }
				}
				""");
		validationHelper.assertError(m,
				BenchmarxDSLPackage.Literals.EDIT_DECL,
				BenchmarxDSLValidator.INVALID_EDIT_SIDE);
	}

	@Test
	void concurrent_missing_source_edit_is_error() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 {
				    category : concurrent
				    edit target { op1 }
				    postcondition { source : s, target : t }
				  }
				}
				""");
		validationHelper.assertError(m,
				BenchmarxDSLPackage.Literals.TEST_DECL,
				BenchmarxDSLValidator.INVALID_EDIT_SIDE);
	}

	// ── checkDecisionReferenced ───────────────────────────────────────────

	@Test
	void declared_decision_is_valid() throws Exception {
		BxModel m = parse("""
				problem P {
				  source metamodel "a.ecore"
				  target metamodel "b.ecore"
				  decisions { PREFER_PARENT }
				}
				state s : P.source { R {} }
				state t : P.target { R {} }
				suite S for P {
				  test t1 {
				    category : batch.forward
				    edit source { decide(PREFER_PARENT = true) op1 }
				    postcondition { source : s, target : t }
				  }
				}
				""");
		validationHelper.assertNoErrors(m);
	}

	@Test
	void undeclared_decision_is_error() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 {
				    category : batch.forward
				    edit source { decide(UNKNOWN = true) op1 }
				    postcondition { source : s, target : t }
				  }
				}
				""");
		validationHelper.assertError(m,
				BenchmarxDSLPackage.Literals.EDIT_DECL,
				BenchmarxDSLValidator.UNKNOWN_DECISION);
	}

	// ── checkStateMetamodelSide ───────────────────────────────────────────

	@Test
	void precondition_sides_match_is_valid() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 {
				    category : incremental.forward
				    precondition { source : s, target : t }
				    edit source { op1 }
				    postcondition { source : s, target : t }
				  }
				}
				""");
		validationHelper.assertNoErrors(m);
	}

	@Test
	void precondition_source_referencing_target_state_is_error() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 {
				    category : incremental.forward
				    precondition { source : t, target : s }
				    edit source { op1 }
				    postcondition { source : s, target : t }
				  }
				}
				""");
		validationHelper.assertError(m,
				BenchmarxDSLPackage.Literals.TEST_DECL,
				BenchmarxDSLValidator.WRONG_STATE_SIDE);
	}

	// ── checkNoDuplicateTestNames ─────────────────────────────────────────

	@Test
	void unique_test_names_produce_no_warning() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 { category : batch.forward  edit source { op1 } postcondition { source : s, target : t } }
				  test t2 { category : batch.forward  edit source { op2 } postcondition { source : s, target : t } }
				}
				""");
		validationHelper.assertNoWarnings(m,
				BenchmarxDSLPackage.Literals.TEST_DECL,
				BenchmarxDSLValidator.DUPLICATE_TEST_NAME);
	}

	@Test
	void duplicate_test_names_produce_warning() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 { category : batch.forward  edit source { op1 } postcondition { source : s, target : t } }
				  test t1 { category : batch.forward  edit source { op2 } postcondition { source : s, target : t } }
				}
				""");
		validationHelper.assertWarning(m,
				BenchmarxDSLPackage.Literals.TEST_DECL,
				BenchmarxDSLValidator.DUPLICATE_TEST_NAME);
	}

	// ── checkCategoryNotMixedInSuite ──────────────────────────────────────

	@Test
	void pure_batch_suite_produces_no_warning() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 { category : batch.forward   edit source { op1 } postcondition { source : s, target : t } }
				  test t2 { category : batch.backward  edit target { op2 } postcondition { source : s, target : t } }
				}
				""");
		validationHelper.assertNoWarnings(m,
				BenchmarxDSLPackage.Literals.SUITE_DECL,
				BenchmarxDSLValidator.MIXED_CATEGORIES);
	}

	@Test
	void mixed_batch_and_incremental_suite_produces_warning() throws Exception {
		BxModel m = parse(PREAMBLE + """
				suite S for P {
				  test t1 { category : batch.forward        edit source { op1 } postcondition { source : s, target : t } }
				  test t2 { category : incremental.forward  edit source { op2 } postcondition { source : s, target : t } }
				}
				""");
		validationHelper.assertWarning(m,
				BenchmarxDSLPackage.Literals.SUITE_DECL,
				BenchmarxDSLValidator.MIXED_CATEGORIES);
	}
}
