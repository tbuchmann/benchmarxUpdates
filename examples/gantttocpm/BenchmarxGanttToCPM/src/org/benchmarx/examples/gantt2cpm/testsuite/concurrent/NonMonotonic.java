package org.benchmarx.examples.gantt2cpm.testsuite.concurrent;

import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.gantt2cpm.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.examples.gantt2cpm.testsuite.GanttToCPMTestCase;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cpm.CPMNetwork;
import gantt.GanttDiagram;

/**
 * Concurrent non-monotonic tests for Gantt-to-CPM: the source side performs a mix of
 * deletion and attribute-change in the same concurrent step, while the target is idle,
 * as opposed to {@link MonotonicCreating}/{@link MonotonicDeleting} which only ever
 * exercise one kind of change.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 * Note: only the source side is actively edited here (target stays idle), following the
 * pattern of every other concurrent test in this example - see memory
 * {@code bxagent_gantt2cpm_concurrent_backward_drop_bug.md} for why a real target-side
 * edit inside {@code performAndPropagateEdit} is currently unreliable for this tool.
 */
@ExtendWith(BXToolParameterResolver.class)
public class NonMonotonic extends GanttToCPMTestCase {

	public NonMonotonic() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for a non-monotonic concurrent step: source removes the helper
	 * activities (deletion) and changes the GanttModel/CPMModel activity durations
	 * (attribute change) in the same edit, while target is idle (NM-DelChangeSrcIdleTrg).<br/>
	 * <b>Expect</b>: Both the deletion and the attribute change are propagated together
	 * without loss.<br/>
	 * <b>Features</b>: concurrent, non-monotonic, del+attribute, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceDeleteHelpersAndChangeModelDurationTargetIdle(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::createGantt2CPMTestCases,
				helperGantt::addGantt2CPMHelpers,
				helperGantt::addGantt2CPMComparators,
				helperGantt::addGantt2CPMModels,
				helperGantt::addGantt2CPMModelsToComparatorDependencies,
				helperGantt::changeIncrementalID));
		tool.performIdleTargetEdit(trgEdit(helperCPM::changeIncrementalID));
		util.assertPrecondition("NonMonotonicPreGantt", "NonMonotonicPreCPM");
		// Concurrent: SRC mixes a deletion (Helpers) and an attribute change (Model
		// durations) in the same edit; TRG is idle.
		tool.performAndPropagateEdit(
				srcEdit(helperGantt::deleteGantt2CPMHelpers, helperGantt::changeGantt2CPMModelDuration),
				trgEdit(helperCPM::idleDelta));
		util.assertPostcondition("NonMonotonicPostGantt", "NonMonotonicPostCPM");
	}
}
