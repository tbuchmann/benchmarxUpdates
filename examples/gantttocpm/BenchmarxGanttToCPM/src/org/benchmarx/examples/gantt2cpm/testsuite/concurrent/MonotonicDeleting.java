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
 * Concurrent monotonic-deletion tests for Gantt-to-CPM.
 * Both models are edited independently (without synchronisation) and
 * synchronisation is triggered via {@code performAndPropagateEdit(srcEdit, trgEdit)}.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 */
@ExtendWith(BXToolParameterResolver.class)
public class MonotonicDeleting extends GanttToCPMTestCase {

	public MonotonicDeleting() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for concurrent source deletion of helper activities while target is idle (MD-FwdDeleteHelpers).<br/>
	 * Starting from the full Gantt/CPM state (with dependencies), helper activities are deleted from the
	 * Gantt concurrently while the CPM network is idle.<br/>
	 * <b>Expect</b>: Helper activities (and their associated CPM events) are removed from both models;
	 * the state reduces to comparators and models only.<br/>
	 * <b>Features</b>: concurrent, del, fwd-dominant
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentSourceDeleteHelpersTargetIdle(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
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
		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		// Concurrent: SRC removes helper activities; TRG does nothing
		tool.performAndPropagateEdit(
				srcEdit(helperGantt::deleteGantt2CPMHelpers),
				trgEdit(helperCPM::idleDelta));
		util.assertPostcondition("TestsModelComparatorGantt", "TestsModelComparatorCPM");
	}
}
