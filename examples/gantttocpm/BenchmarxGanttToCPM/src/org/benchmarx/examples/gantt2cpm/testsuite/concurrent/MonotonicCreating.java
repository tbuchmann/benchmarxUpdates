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
 * Concurrent monotonic-creation tests for Gantt-to-CPM.
 * Both models are edited independently (without synchronisation) and
 * synchronisation is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicCreating extends GanttToCPMTestCase {

	public MonotonicCreating() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent source bulk-addition of activities while target is idle (MC-FwdBulkAdd).<br/>
	 * Starting from the base test-cases diagram, helper, comparator, and model activities are added
	 * to the Gantt concurrently while the CPM network is idle.<br/>
	 * <b>Expect</b>: All three categories of activities appear in both Gantt and CPM.<br/>
	 * <b>Features</b>: concurrent, add, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceBulkAddTargetIdle(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::createGantt2CPMTestCases,
				helperGantt::changeIncrementalID));
		tool.performIdleTargetEdit(trgEdit(helperCPM::changeIncrementalID));
		util.assertPrecondition("TestsGantt", "TestsCPM");
		// Concurrent: SRC adds helpers + comparators + models; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperGantt::addGantt2CPMHelpers,
						helperGantt::addGantt2CPMComparators,
						helperGantt::addGantt2CPMModels),
				trgEdit(helperCPM::idleDelta));
		util.assertPostcondition("TestsHelperModelComparatorGantt", "TestsHelperModelComparatorCPM");
	}
}
