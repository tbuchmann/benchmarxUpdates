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
public class MonotonicDeleting extends Pdb12Pdb2TestCase {

	public MonotonicDeleting(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
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
	 * <b>Test</b> for concurrent deletion of the same person (Kiesinger, ID "KK")
	 * on both sides simultaneously. <br/>
	 * <b>Expect</b>: Kiesinger is removed exactly once from both databases; the
	 * remaining five persons are unchanged. <br/>
	 * <b>Features</b>: concurrent, delete, matching
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testMatchingDeletion(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		buildSixChancellorPrecondition();
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::deleteKurtKiesinger),
				trgEdit(helperPerson2::deleteKurtKiesinger));
		// ------------
		util.assertPostcondition("IncrFwdPDB1FirstSixChancellorsWithoutKiesinger",
				"IncrFwdPDB2FirstSixChancellorsWithoutKiesinger");
		terminate();
	}

	/**
	 * <b>Test</b> for concurrent deletion of the same person combined with
	 * an independent attribute update. The source deletes Kiesinger while the
	 * target also deletes Kiesinger and additionally changes Brandt's place of
	 * birth (an out-of-correspondence-scope attribute). <br/>
	 * <b>Expect</b>: Kiesinger is removed from both databases; Brandt's updated
	 * place of birth is preserved. <br/>
	 * <b>Features</b>: concurrent, delete, matching, combined
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testCombinedMatchingDeletion(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		buildSixChancellorPrecondition();
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::deleteKurtKiesinger),
				trgEdit(helperPerson2::deleteKurtKiesinger,
						helperPerson2::changePlaceOfBirthOfWillyBrandt));
		// ------------
		util.assertPostcondition("IncrBwdPDB1FirstSixChancellorsWithoutKiesinger",
				"IncrBwdPDB2FirstSixChancellorsWithoutKiesinger");
		terminate();
	}
}