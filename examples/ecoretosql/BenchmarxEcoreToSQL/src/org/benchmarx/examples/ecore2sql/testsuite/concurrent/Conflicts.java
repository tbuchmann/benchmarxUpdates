package org.benchmarx.examples.ecore2sql.testsuite.concurrent;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ecore2sql.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.examples.ecore2sql.testsuite.EcoreToSQLTestCase;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sql.Schema;

/**
 * Concurrent conflict tests for Ecore-to-SQL: both sides rename the same attribute/column
 * incompatibly in the same concurrent step. {@code BXAgentEcore2SQL} hardcodes
 * {@code SyncConflictPolicy.TARGET_WINS}, so resolution is deterministic here (unlike
 * examples whose adapter exposes a {@code Configurator}): the target's name always wins
 * and is propagated back to the source.
 */
@ExtendWith(BXToolParameterResolver.class)
public class Conflicts extends EcoreToSQLTestCase {

	public Conflicts() {
		super();
	}

	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return EcoreToSQLTestCase.tools();
	}

	/**
	 * <b>Test</b> for a rename-vs-rename conflict on List.length: source renames the
	 * attribute to "count" while target independently renames the corresponding column
	 * to "size" in the same concurrent step (CF-RenameListLength).<br/>
	 * <b>Expect</b>: TARGET_WINS resolves the conflict; both sides end up named "size".<br/>
	 * <b>Features</b>: concurrent, conflict, rename-vs-rename
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentRenameListLengthConflict(BXTool<EPackage, Schema, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperEcore::createSimpleCompositeList, helperEcore::changePackageName));
		util.assertPrecondition("MonotonicCreatingPreEcore", "MonotonicCreatingPreSQL");
		// Concurrent: SRC renames List.length to "count"; TRG independently renames the
		// same column to "size" - a genuine incompatible edit on the same attribute/column.
		tool.performAndPropagateEdit(
				srcEdit(helperEcore::renameListLengthAttribute),
				trgEdit(helperSQL::renameListLengthColumnToSize));
		util.assertPostcondition("ConflictsListLengthEcore", "ConflictsListLengthSQL");
		terminate();
	}
}
