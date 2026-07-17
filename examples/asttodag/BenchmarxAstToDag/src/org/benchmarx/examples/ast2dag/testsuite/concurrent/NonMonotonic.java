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
 * Concurrent non-monotonic tests for AST-to-DAG: at least one side performs a
 * deletion paired with an (unrelated) attribute change in the same concurrent step,
 * as opposed to {@link MonotonicCreating}/{@link MonotonicDeleting} which only ever
 * exercise one direction of change.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 */
@ExtendWith(BXToolParameterResolver.class)
public class NonMonotonic extends Ast2DagTestCase {

	public NonMonotonic() {
		super();
	}

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return Ast2DagTestCase.tools();
	}

	/**
	 * <b>Test</b> for a non-monotonic concurrent step: source removes the MoreBestDigits
	 * extension (deletion) while target independently changes the incremental IDs of its
	 * nodes (attribute change) (NM-DelSrcChangeTrg).<br/>
	 * <b>Expect</b>: The source-side deletion is propagated (reducing back to BestDigit
	 * shape) without losing or conflicting with the independent target-side attribute
	 * change.<br/>
	 * <b>Features</b>: concurrent, non-monotonic, del+attribute
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceDeleteTargetChangeIncrementalID(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperAst::create42,
				helperAst::createBestDigit,
				helperAst::createMoreBestDigits));
		util.assertPrecondition("NonMonotonicMoreBestDigitsAst", "NonMonotonicMoreBestDigitsDag");
		// Concurrent: SRC removes the MoreBestDigits extension (delete); TRG independently
		// changes incremental IDs (attribute change) - a mixed, non-conflicting edit.
		tool.performAndPropagateEdit(
				srcEdit(helperAst::removeSomeBestDigits),
				trgEdit(helperDag::changeIncrementalID));
		util.assertPostcondition("NonMonotonicBestDigitAfterDeleteAst", "NonMonotonicBestDigitAfterDeleteDag");
	}
}
