package org.benchmarx.examples.pdb12pdb2.testsuite.concurrent;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.Pdb12Pdb2TestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class NonMonotonic extends Pdb12Pdb2TestCase {

	public NonMonotonic(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		super(tool);
	}

	public static Collection<BXTool<pdb1.Database, pdb2.Database, Decisions>> tools() {
		return Pdb12Pdb2TestCase.tools();
	}

	/**
	 * Shared helper: build the six-chancellor precondition state.
	 */
	private void buildSixChancellorPrecondition() {
		tool.performAndPropagateSourceEdit(srcEdit(
				helperPerson1::setDatabaseName,
				helperPerson1::createKonradAdenauer,
				helperPerson1::createLudwigErhard,
				helperPerson1::createKurtKiesinger,
				helperPerson1::createWillyBrandt,
				helperPerson1::createHelmutSchmidt,
				helperPerson1::createHelmutKohl));
		tool.performIdleTargetEdit(trgEdit(helperPerson2::changeIncrementalIDs));
	}

	/**
	 * <b>Test</b> for combined concurrent delete and create: the source
	 * simultaneously deletes Kiesinger and renames Adenauer's first name,
	 * while the target creates Schroeder and sets incremental IDs (idle). <br/>
	 * <b>Expect</b>: Kiesinger is removed from both databases, Adenauer's name
	 * is updated in both, and Schroeder appears in both databases. <br/>
	 * <b>Features</b>: concurrent, delete, create, rename, non-monotonic
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testConcurrentDeleteCreate(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		buildSixChancellorPrecondition();
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::deleteKurtKiesinger,
						helperPerson1::changeFirstNameOfKonradAdenauer),
				trgEdit(helperPerson2::createGerhardSchroeder,
						helperPerson2::changeIncrementalIDs));
		// ------------
		util.assertPostcondition("ConcSyncNonMonotonicDeleteCreateNewPDB1",
				"ConcSyncNonMonotonicDeleteCreateNewPDB2");
		terminate();
	}

	/**
	 * <b>Test</b> for combined concurrent delete and rename on unrelated persons:
	 * the source deletes Kiesinger while the target renames Erhard's last name. <br/>
	 * <b>Expect</b>: Kiesinger is removed from both databases and Erhard's
	 * updated last name is reflected in both. <br/>
	 * <b>Features</b>: concurrent, delete, rename, non-monotonic
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testConcurrentDeleteRename(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		buildSixChancellorPrecondition();
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::deleteKurtKiesinger),
				trgEdit(helperPerson2::changeLastNameOfLudwigErhard));
		// ------------
		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		util.assertPostcondition("ConcSyncNonMonotonicDeleteRenameNewPDB1",
				"ConcSyncNonMonotonicDeleteRenameNewPDB2");
		terminate();
	}
}