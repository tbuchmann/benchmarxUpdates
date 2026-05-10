package org.benchmarx.examples.pdb12pdb2.testsuite.concurrent;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.Map;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.Pdb12Pdb2TestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(BXToolParameterResolver.class)
public class Conflicts extends Pdb12Pdb2TestCase {

	public Conflicts(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
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
	 * <b>Test</b> for resolution of a delete/rename conflict on the same
	 * person (Kiesinger): the source deletes Kiesinger (ID "KK") while the
	 * target simultaneously changes Kiesinger's birthday. <br/>
	 * <b>Expect</b>: One of two valid resolutions:
	 * (1) the rename (birthday change) wins and Kiesinger is kept with the new
	 * birthday in both databases; or
	 * (2) the delete wins and Kiesinger is removed from both databases, the
	 * birthday change is discarded. <br/>
	 * <b>Features</b>: concurrent, conflict, delete, rename
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testDeleteRenameConflict(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		buildSixChancellorPrecondition();
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::deleteKurtKiesinger),
				trgEdit(helperPerson2::changeBirthdayOfKurtKiesinger));
		// ------------
		util.assertAnyPostcondition(Map.ofEntries(
				// Rename (birthday) wins: Kiesinger kept with updated birthday
				entry("ConcSyncConflictDeleteRenameKiwinsRenamePDB1",
					  "ConcSyncConflictDeleteRenameKiwinsRenamePDB2"),
				// Delete wins: Kiesinger removed from both databases
				entry("IncrFwdPDB1FirstSixChancellorsWithoutKiesinger",
					  "IncrFwdPDB2FirstSixChancellorsWithoutKiesinger")));
		terminate();
	}

	/**
	 * <b>Test</b> for resolution of a rename/rename conflict on the same
	 * person (Adenauer, ID "KA"): the source changes Adenauer's first name
	 * (via firstName field in pdb1) while the target independently changes
	 * Adenauer's full name (via the pdb2 name field). <br/>
	 * <b>Expect</b>: One of two valid resolutions:
	 * (1) the source-side rename wins: Adenauer's name becomes "Heinz Adenauer"
	 * in both databases; or
	 * (2) the target-side rename wins: Adenauer's name becomes
	 * "Heinz Jochen Adenauer" in both databases. <br/>
	 * <b>Features</b>: concurrent, conflict, rename, rename
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testRenameRenameConflict(BXTool<pdb1.Database, pdb2.Database, Decisions> tool) {
		this.tool = tool; initialise();
		buildSixChancellorPrecondition();
		util.assertPrecondition("Pre_IncrFwdPDB1FirstSixChancellors", "Pre_IncrFwdPDB2FirstSixChancellors");
		// ------------
		//tool.performIdleSourceEdit(srcEdit(helperPerson1::changeIncrementalIDs));
		tool.performAndPropagateEdit(
				srcEdit(helperPerson1::changeFirstNameOfKonradAdenauer),
				trgEdit(helperPerson2::changeFirstNameOfKonradAdenauer));
		// ------------
		util.assertAnyPostcondition(Map.ofEntries(
				// Source-side firstName rename wins: "Heinz Adenauer"
				entry("ConcSyncConflictRenameRenameSrcWinsPDB1",
					  "ConcSyncConflictRenameRenameSrcWinsPDB2"),
				// Target-side full-name rename wins: "Heinz Jochen Adenauer"
				entry("ConcSyncConflictRenameRenameTgtWinsPDB1",
					  "ConcSyncConflictRenameRenameTgtWinsPDB2")));
		terminate();
	}
}