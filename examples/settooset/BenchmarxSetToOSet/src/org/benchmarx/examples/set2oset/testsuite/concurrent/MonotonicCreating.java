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
 * Concurrent monotonic-creation tests for Set-to-OrderedSet.
 * Both models are edited independently (without synchronisation) and
 * synchronisation is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicCreating extends Set2OsetTestCase {

	public MonotonicCreating() {
		super();
	}

	public static Collection<BXTool<sets.MySet, osets.MyOrderedSet, Decisions>> tools() {
		return Set2OsetTestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent source addition of D while target is idle (MC-FwdAdd).<br/>
	 * Starting from a set with A, B, C, D is added to the source Set while the OSet is idle.<br/>
	 * <b>Expect</b>: D is appended to both the Set and the OSet (A→B→C→D order).<br/>
	 * <b>Features</b>: concurrent, add, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceAddTargetIdle(BXTool<sets.MySet, osets.MyOrderedSet, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(helperSet::setSetName, helperSet::createA, helperSet::createB, helperSet::createC));
		util.assertPrecondition("FirstThreeLettersSet", "FirstThreeLettersOset");
		// Concurrent: SRC adds D; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperSet::createD),
				trgEdit(helperOset::idleDelta));
		util.assertPostcondition("abcdSet", "AbcdOset");
	}

}
