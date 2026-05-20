package org.benchmarx.dsl.validation;

import java.util.HashSet;
import java.util.Set;

import org.benchmarx.dsl.benchmarxDSL.BatchBwd;
import org.benchmarx.dsl.benchmarxDSL.BatchFwd;
import org.benchmarx.dsl.benchmarxDSL.BenchmarxDSLPackage;
import org.benchmarx.dsl.benchmarxDSL.Concurrent;
import org.benchmarx.dsl.benchmarxDSL.EditDecl;
import org.benchmarx.dsl.benchmarxDSL.IncrBwd;
import org.benchmarx.dsl.benchmarxDSL.IncrFwd;
import org.benchmarx.dsl.benchmarxDSL.ProblemDecl;
import org.benchmarx.dsl.benchmarxDSL.StateDecl;
import org.benchmarx.dsl.benchmarxDSL.SuiteDecl;
import org.benchmarx.dsl.benchmarxDSL.TestDecl;
import org.eclipse.xtext.validation.Check;

public class BenchmarxDSLValidator extends AbstractBenchmarxDSLValidator {

	public static final String INVALID_EDIT_SIDE   = "invalidEditSide";
	public static final String UNKNOWN_DECISION    = "unknownDecision";
	public static final String WRONG_STATE_SIDE    = "wrongStateSide";
	public static final String DUPLICATE_TEST_NAME = "duplicateTestName";
	public static final String MIXED_CATEGORIES    = "mixedCategories";

	@Check
	public void checkEditMatchesCategory(TestDecl test) {
		var cat = test.getCategory();
		var edits = test.getEdits();
		if (cat instanceof BatchFwd) {
			for (EditDecl edit : edits) {
				if (!"source".equals(edit.getSide()))
					error("batch.forward tests must only have 'edit source'",
							edit, BenchmarxDSLPackage.Literals.EDIT_DECL__SIDE, INVALID_EDIT_SIDE);
			}
		} else if (cat instanceof BatchBwd) {
			for (EditDecl edit : edits) {
				if (!"target".equals(edit.getSide()))
					error("batch.backward tests must only have 'edit target'",
							edit, BenchmarxDSLPackage.Literals.EDIT_DECL__SIDE, INVALID_EDIT_SIDE);
			}
		} else if (cat instanceof Concurrent) {
			boolean hasSource = edits.stream().anyMatch(e -> "source".equals(e.getSide()));
			boolean hasTarget = edits.stream().anyMatch(e -> "target".equals(e.getSide()));
			if (!hasSource || !hasTarget)
				error("concurrent tests must have both 'edit source' and 'edit target'",
						test, BenchmarxDSLPackage.Literals.TEST_DECL__EDITS, INVALID_EDIT_SIDE);
		}
	}

	@Check
	public void checkDecisionReferenced(EditDecl edit) {
		String decision = edit.getDecision();
		if (decision == null || decision.isEmpty()) return;

		if (!(edit.eContainer() instanceof TestDecl test)) return;
		if (!(test.eContainer() instanceof SuiteDecl suite)) return;
		ProblemDecl problem = suite.getProblem();
		if (problem == null || problem.eIsProxy()) return;

		boolean found = problem.getDecisions().stream()
				.anyMatch(d -> decision.equals(d.getName()));
		if (!found)
			error("Decision '" + decision + "' is not declared in problem '" + problem.getName() + "'",
					edit, BenchmarxDSLPackage.Literals.EDIT_DECL__DECISION, UNKNOWN_DECISION);
	}

	@Check
	public void checkStateMetamodelSide(TestDecl test) {
		StateDecl preSource = test.getPreSource();
		if (preSource != null && !preSource.eIsProxy() && !"source".equals(preSource.getSide()))
			error("Precondition source state must have side 'source'",
					test, BenchmarxDSLPackage.Literals.TEST_DECL__PRE_SOURCE, WRONG_STATE_SIDE);

		StateDecl preTarget = test.getPreTarget();
		if (preTarget != null && !preTarget.eIsProxy() && !"target".equals(preTarget.getSide()))
			error("Precondition target state must have side 'target'",
					test, BenchmarxDSLPackage.Literals.TEST_DECL__PRE_TARGET, WRONG_STATE_SIDE);
	}

	@Check
	public void checkNoDuplicateTestNames(SuiteDecl suite) {
		Set<String> seen = new HashSet<>();
		for (TestDecl test : suite.getTests()) {
			if (test.getName() != null && !seen.add(test.getName()))
				warning("Duplicate test name '" + test.getName() + "'",
						test, BenchmarxDSLPackage.Literals.TEST_DECL__NAME, DUPLICATE_TEST_NAME);
		}
	}

	@Check
	public void checkCategoryNotMixedInSuite(SuiteDecl suite) {
		boolean hasBatch = suite.getTests().stream()
				.anyMatch(t -> t.getCategory() instanceof BatchFwd
						    || t.getCategory() instanceof BatchBwd);
		boolean hasIncr = suite.getTests().stream()
				.anyMatch(t -> t.getCategory() instanceof IncrFwd
						    || t.getCategory() instanceof IncrBwd);
		if (hasBatch && hasIncr)
			warning("Suite '" + suite.getName()
					+ "' mixes batch and incremental tests; by convention keep them separate",
					suite, BenchmarxDSLPackage.Literals.SUITE_DECL__TESTS, MIXED_CATEGORIES);
	}
}
