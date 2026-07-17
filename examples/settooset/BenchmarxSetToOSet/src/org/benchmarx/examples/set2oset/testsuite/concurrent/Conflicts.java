package org.benchmarx.examples.set2oset.testsuite.concurrent;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.Map;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.Set2OsetTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Concurrent conflict tests for Set-to-OrderedSet: both sides edit the same element
 * incompatibly in the same concurrent step. Resolution is tool-dependent, so
 * {@code util.assertAnyPostcondition} is used to accept every outcome the tool could
 * reasonably produce.
 */
@ExtendWith(BXToolParameterResolver.class)
public class Conflicts extends Set2OsetTestCase {

	public Conflicts() {
		super();
	}

	public static Collection<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> tools() {
		return Set2OsetTestCase.tools();
	}

	/**
	 * <b>Test</b> for a delete-vs-modify conflict on element A: source deletes A while
	 * target independently renames A to Z in the same concurrent step
	 * (CF-DeleteRenameA).<br/>
	 * <b>Expect</b>: The tool resolves the delete-vs-rename conflict on A one way or
	 * another (A is gone, or A survives renamed to Z).<br/>
	 * <b>Features</b>: concurrent, conflict, delete-vs-modify
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentDeleteASrcRenameATrgConflict(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::setSetName, helperSet::createA, helperSet::createB, helperSet::createC));
		util.assertPrecondition("ConflictsPreSet", "ConflictsPreOset");
		// Concurrent: SRC deletes A; TRG independently renames the same A to Z - a
		// genuine incompatible edit on A, isolated from any uncontested collateral data
		// (unlike changeABCtoZXY, which also touches B/C - see the note on
		// OsetHelper.renameAToZ for why that matters here).
		tool.performAndPropagateEdit(
				srcEdit(helperSet::deleteA),
				trgEdit(helperOset::renameAToZ));
		// Delete-wins was captured directly from a real BXAgent run via tool.saveModels(...).
		// Rename-wins is not independently observed, but derived from the verified
		// precondition structure by substituting only A's value - both are accepted
		// since rename-wins is just as valid a conflict resolution policy as delete-wins.
		util.assertAnyPostcondition(Map.ofEntries(
				entry("ConflictsDeleteWinsSet", "ConflictsDeleteWinsOset"),
				entry("ConflictsRenameWinsSet", "ConflictsRenameWinsOset")));
	}
}
