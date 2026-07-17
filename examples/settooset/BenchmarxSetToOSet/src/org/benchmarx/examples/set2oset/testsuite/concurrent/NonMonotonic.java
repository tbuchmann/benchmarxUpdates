package org.benchmarx.examples.set2oset.testsuite.concurrent;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.set2oset.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.set2oset.testsuite.Decisions;
import org.benchmarx.examples.set2oset.testsuite.Set2OsetTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Concurrent non-monotonic tests for Set-to-OrderedSet: source performs a mix of
 * creation and deletion in the same concurrent step, while the target is idle, as
 * opposed to {@link MonotonicCreating}/{@link MonotonicDeleting} which only ever
 * exercise one kind of change.
 */
@ExtendWith(BXToolParameterResolver.class)
public class NonMonotonic extends Set2OsetTestCase {

	public NonMonotonic() {
		super();
	}

	public static Collection<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> tools() {
		return Set2OsetTestCase.tools();
	}

	/**
	 * <b>Test</b> for a non-monotonic concurrent step: source adds D (creation) and
	 * removes A (deletion) in the same edit, while target is idle (NM-AddDDelASrcIdleTrg).<br/>
	 * <b>Expect</b>: Both the addition and the deletion are propagated together without
	 * loss.<br/>
	 * <b>Features</b>: concurrent, non-monotonic, add+del, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceAddDDeleteATargetIdle(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::setSetName, helperSet::createA, helperSet::createB, helperSet::createC));
		util.assertPrecondition("NonMonotonicPreSet", "NonMonotonicPreOset");
		// Concurrent: SRC mixes a creation (D) and a deletion (A) in the same edit; TRG is idle.
		tool.performAndPropagateEdit(
				srcEdit(helperSet::createD, helperSet::deleteA),
				trgEdit(helperOset::idleDelta));
		util.assertPostcondition("NonMonotonicPostSet", "NonMonotonicPostOset");
	}
}
