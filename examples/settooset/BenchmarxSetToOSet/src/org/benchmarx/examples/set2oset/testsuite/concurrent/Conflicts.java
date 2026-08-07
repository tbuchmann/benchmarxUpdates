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

	/**
	 * <b>Test</b> for the same delete-vs-modify conflict on A, but with a genuine
	 * non-conflicting target-side edit alongside it: target renames A to Z (contested)
	 * <i>and</i> B/C to X/Y (uncontested) in the same concurrent step
	 * (CF-DeleteRenameAFullTrgEdit).<br/>
	 * <b>Expect</b>: the A conflict resolves (delete wins), and the uncontested B/C
	 * renames backward-propagate to the source, leaving both models at {@code {X, Y}}.<br/>
	 * <b>Features</b>: concurrent, conflict, delete-vs-modify, bwd-required
	 *
	 * <p><b>Fixed 2026-08-07</b>: this reproduced a confirmed BXAgent bug (the
	 * uncontested B/C renames never backward-propagated during concurrent sync, while
	 * the delete-vs-rename conflict on A resolved correctly) until
	 * {@code performAndPropagateEdit} was switched from {@code transform()} to
	 * {@code sync()} and the underlying deletion-cascade gap in generated {@code sync()}
	 * code was fixed upstream in the {@code bxagent} generator repo. This was the same
	 * underlying bug {@link #testConcurrentDeleteASrcRenameATrgConflict} used to work
	 * around via the narrower {@code OsetHelper.renameAToZ()} instead of
	 * {@code changeABCtoZXY()} (that workaround is no longer necessary but left as-is).
	 * See {@code BXAgent-KnownIssues.md} #2b and {@code BXAgent-KnownIssues-Fixes.md}.</p>
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentDeleteASrcFullRenameTrgConflict(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::setSetName, helperSet::createA, helperSet::createB, helperSet::createC));
		util.assertPrecondition("ConflictsPreSet", "ConflictsPreOset");
		// Concurrent: SRC deletes A; TRG independently renames A to Z (contested, same
		// conflict as above) AND renames B/C to X/Y (uncontested, disjoint from the
		// conflict) in the same edit.
		tool.performAndPropagateEdit(
				srcEdit(helperSet::deleteA),
				trgEdit(helperOset::changeABCtoZXY));
		util.assertPostcondition("ConflictsFullRenameSet", "ConflictsFullRenameOset");
	}
}
