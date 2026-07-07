package org.benchmarx.examples.ast2dag.testsuite.alignment_based.roundtrip;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.Ast2DagTestCase;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Round-trip tests for the AST-to-DAG transformation.
 * Each test performs a sequence of forward and backward propagation steps and
 * verifies consistency at each intermediate point.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 */
@ExtendWith(BXToolParameterResolver.class)
public class RoundtripTests extends Ast2DagTestCase {

	public RoundtripTests() {
		super();
	}

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return Ast2DagTestCase.tools();
	}

	/**
	 * <b>Test</b> for a round-trip: forward create, then backward modify reference, then backward idle.<br/>
	 * Starting with the BestDigitRef AST/DAG state, the backward modification of the shared reference
	 * is propagated, then a backward idle confirms the new state is stable.<br/>
	 * <b>Expect</b>: After modifyBestDigitRef both models reflect the modified reference.
	 * Backward idle leaves the state unchanged.<br/>
	 * <b>Features</b>: roundtrip, fwd+bwd, structural-dedup, modify
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripCreateRefThenModify(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperAst::create42,
				helperAst::createBestDigit,
				helperAst::createBestDigitRef));
		util.assertPrecondition("BestDigitRefAst", "BestDigitRefDag");
		// Backward: modify the shared reference in the DAG
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::modifyBestDigitRef));
		util.assertPostcondition("BestDigitRefModifiedAst", "BestDigitRefModifiedDag");
		// Backward idle: state must remain stable
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::idleDelta));
		util.assertPostcondition("BestDigitRefModifiedAst", "BestDigitRefModifiedDag");
	}

	/**
	 * <b>Test</b> for round-trip stability: backward idle does not change either model,
	 * then forward extension adds more best digits.<br/>
	 * <b>Expect</b>: After idle backward the BestDigit state is unchanged.
	 * After forward createMoreBestDigits both models contain the extended set of digits.<br/>
	 * <b>Features</b>: roundtrip, stability, fwd+bwd, add
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripIdleBwdThenAddMore(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::create42, helperAst::createBestDigit));
		util.assertPrecondition("BestDigitAst", "BestDigitDag");
		// Backward idle: nothing must change
		tool.performAndPropagateTargetEdit(trgEdit(helperDag::idleDelta));
		util.assertPostcondition("BestDigitAst", "BestDigitDag");
		// Forward: add more best digits
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::createMoreBestDigits));
		util.assertPostcondition("MoreBestDigitsAst", "MoreBestDigitsDag");
	}
}
