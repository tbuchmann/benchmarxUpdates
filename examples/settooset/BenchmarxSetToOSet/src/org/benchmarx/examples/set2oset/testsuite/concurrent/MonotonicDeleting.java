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
 * Concurrent monotonic-deletion tests for Set-to-OrderedSet.
 * Both models are edited independently (without synchronisation) and
 * synchronisation is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicDeleting extends Set2OsetTestCase {

	public MonotonicDeleting() {
		super();
	}

	public static Collection<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> tools() {
		return Set2OsetTestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent source deletion of A while target is idle (MD-FwdDelete).<br/>
	 * Starting from a set with A and C, A is removed from the source Set concurrently
	 * while the OSet is idle.<br/>
	 * <b>Expect</b>: A is removed from both the Set and the OSet; only C remains.<br/>
	 * <b>Features</b>: concurrent, del, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceDeleteTargetIdle(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::setSetName, helperSet::createA, helperSet::createC));
		util.assertPrecondition("acSet", "AcOset");
		// Concurrent: SRC deletes A; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperSet::deleteA),
				trgEdit(helperOset::idleDelta));
		util.assertPostcondition("CSet", "COset");
	}
}
