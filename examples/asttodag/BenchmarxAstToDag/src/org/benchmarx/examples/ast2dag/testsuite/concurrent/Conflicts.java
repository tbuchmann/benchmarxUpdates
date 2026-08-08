package org.benchmarx.examples.ast2dag.testsuite.concurrent;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.Map;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ast2dag.testsuite.Ast2DagTestCase;
import org.benchmarx.examples.ast2dag.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ast2dag.testsuite.Decisions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Concurrent conflict tests for AST-to-DAG: both sides edit the same shared element
 * incompatibly in the same concurrent step. Resolution is tool-dependent, so
 * {@code util.assertAnyPostcondition} is used to accept every outcome the tool could
 * reasonably produce.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 */
@ExtendWith(BXToolParameterResolver.class)
public class Conflicts extends Ast2DagTestCase {

	public Conflicts() {
		super();
	}

	public static Collection<BXTool<ast.Model, dag.Model, Decisions>> tools() {
		return Ast2DagTestCase.tools();
	}

	/**
	 * <b>Test</b> for a conflict on the shared "sieben" variable: source renames it to
	 * "zwei" (as part of {@code modifyBestDigitRef}) while target independently renames
	 * the corresponding shared DAG variable to "unbekannt" in the same concurrent step
	 * (CF-SharedVarRename).<br/>
	 * <b>Expect</b>: The tool resolves the naming conflict one way or another; both
	 * models end up consistent with whichever side won.<br/>
	 * <b>Features</b>: concurrent, conflict, rename, sharing
	 *
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentRenameSharedVariableConflict(BXTool<ast.Model, dag.Model, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperAst::create42,
				helperAst::createBestDigit,
				helperAst::createBestDigitRef));
		util.assertPrecondition("ConflictsBestDigitRefAst", "ConflictsBestDigitRefDag");
		// Concurrent: SRC renames the shared "sieben" variable to "zwei" (as part of a
		// larger modification); TRG independently renames the same shared variable to
		// "unbekannt" - a genuine incompatible edit to the same element.
		tool.performAndPropagateEdit(
				srcEdit(helperAst::modifyBestDigitRef),
				trgEdit(helperDag::renameSharedVariableSieben));
		util.assertAnyPostcondition(Map.ofEntries(
				entry("ConflictsSharedVarSrcWinsAst", "ConflictsSharedVarSrcWinsDag"),
				entry("ConflictsSharedVarTrgWinsAst", "ConflictsSharedVarTrgWinsDag")));
	}
}
