package org.benchmarx.examples.ast2dag.testsuite.concurrent;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.Ast2DagTestCase;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Concurrent monotonic-creation tests for AST-to-DAG.
 * Both models are edited independently (without synchronisation) and
 * synchronisation is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicCreating extends Ast2DagTestCase {

	public MonotonicCreating() {
		super();
	}

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return Ast2DagTestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent source creation of BestDigit while target is idle (MC-FwdBestDigit).<br/>
	 * Starting from the basic 42 AST/DAG, the source adds the BestDigit sub-tree concurrently
	 * while the DAG side is idle.<br/>
	 * <b>Expect</b>: BestDigit is added to both AST and DAG after synchronisation.<br/>
	 * <b>Features</b>: concurrent, add, fwd-dominant, structural-dedup
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceAddBestDigitTargetIdle(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::create42));
		util.assertPrecondition("42Ast", "42Dag");
		// Concurrent: SRC adds BestDigit; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperAst::createBestDigit),
				trgEdit(helperDag::idleDelta));
		util.assertPostcondition("BestDigitAst", "BestDigitDag");
	}

	/**
	 * <b>Test</b> for concurrent source creation of MoreBestDigits while target is idle (MC-FwdMore).<br/>
	 * Starting from the BestDigit AST/DAG, the source adds more best-digit nodes concurrently
	 * while the DAG side is idle.<br/>
	 * <b>Expect</b>: The additional best-digit nodes appear in both AST and DAG after synchronisation.<br/>
	 * <b>Features</b>: concurrent, add, fwd-dominant, structural-dedup
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceAddMoreBestDigitsTargetIdle(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::create42, helperAst::createBestDigit));
		util.assertPrecondition("BestDigitAst", "BestDigitDag");
		// Concurrent: SRC adds more best-digit nodes; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperAst::createMoreBestDigits),
				trgEdit(helperDag::idleDelta));
		util.assertPostcondition("MoreBestDigitsAst", "MoreBestDigitsDag");
	}

	/**
	 * <b>Test</b> for concurrent source creation of BestDigitRef while target is idle (MC-FwdRef).<br/>
	 * Starting from the BestDigit AST/DAG, the source creates a reference to the shared best-digit
	 * node concurrently while the DAG side is idle.<br/>
	 * <b>Expect</b>: The BestDigitRef structure is propagated to the DAG (structural sharing).<br/>
	 * <b>Features</b>: concurrent, add, fwd-dominant, structural-dedup, sharing
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceAddBestDigitRefTargetIdle(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperAst::create42, helperAst::createBestDigit));
		util.assertPrecondition("BestDigitAst", "BestDigitDag");
		// Concurrent: SRC adds reference to shared BestDigit node; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperAst::createBestDigitRef),
				trgEdit(helperDag::idleDelta));
		util.assertPostcondition("BestDigitRefAst", "BestDigitRefDag");
	}
}
