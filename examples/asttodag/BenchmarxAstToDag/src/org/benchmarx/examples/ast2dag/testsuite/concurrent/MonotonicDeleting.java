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
 * Concurrent monotonic-deletion tests for AST-to-DAG.
 * Both models are edited independently (without synchronisation) and
 * synchronisation is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicDeleting extends Ast2DagTestCase {

	public MonotonicDeleting() {
		super();
	}

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return Ast2DagTestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent source deletion of the MoreBestDigits extension while target is idle (MD-FwdRemove).<br/>
	 * Starting from the MoreBestDigits AST/DAG, the source removes the extension sub-tree concurrently
	 * while the DAG side is idle, reducing the structure back to BestDigit shape.<br/>
	 * <b>Expect</b>: The removed sub-tree disappears from both AST and DAG after synchronisation.<br/>
	 * <b>Features</b>: concurrent, del, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceRemoveMoreBestDigitsTargetIdle(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperAst::create42,
				helperAst::createBestDigit,
				helperAst::createMoreBestDigits));
		util.assertPrecondition("ConcurrentMoreBestDigitsAst", "ConcurrentMoreBestDigitsDag");
		// Concurrent: SRC removes the MoreBestDigits extension; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperAst::removeSomeBestDigits),
				trgEdit(helperDag::idleDelta));
		util.assertPostcondition("ConcurrentBestDigitAfterDeleteAst", "ConcurrentBestDigitAfterDeleteDag");
	}
}
