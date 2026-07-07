package org.benchmarx.examples.gantt2cpm.testsuite.alignment_based.roundtrip;

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
 * Round-trip tests for the Gantt-to-CPM transformation.
 * Each test performs a sequence of forward and backward propagation steps and
 * verifies consistency at each intermediate point.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 *
 * Resource naming convention:
 *   TestsHelperModelComparatorXxx  (no dash) = create + addHelpers + addComparators + addModels
 *   TestsHelperModel-ComparatorXxx (dash)    = above + addModelsToComparatorDependencies
 */
@ExtendWith(BXToolParameterResolver.class)
public class RoundtripTests extends GanttToCPMTestCase {

	public RoundtripTests() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for round-trip stability followed by forward bulk addition.<br/>
	 * Starting from the base test-cases diagram, a backward idle propagation is followed by a
	 * forward batch addition of helper, comparator, and model activities.<br/>
	 * <b>Expect</b>: Backward idle leaves both models unchanged. The batch addition propagates
	 * all three categories of activities to the CPM network.<br/>
	 * <b>Features</b>: roundtrip, stability, fwd+bwd, add
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripIdleBwdThenBulkAdd(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool;
		initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::createGantt2CPMTestCases,
				helperGantt::changeIncrementalID));
		tool.performIdleTargetEdit(trgEdit(helperCPM::changeIncrementalID));
		util.assertPrecondition("TestsGantt", "TestsCPM");
		// Backward idle: nothing must change
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::idleDelta));
		util.assertPostcondition("TestsGantt", "TestsCPM");
		// Forward: add helpers, comparators and models in one step
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::addGantt2CPMHelpers,
				helperGantt::addGantt2CPMComparators,
				helperGantt::addGantt2CPMModels));
		util.assertPostcondition("TestsHelperModelComparatorGantt", "TestsHelperModelComparatorCPM");
	}

	/**
	 * <b>Test</b> for a round-trip: forward rename Helper→Builder and change duration, then backward idle.<br/>
	 * Starting from the full Gantt/CPM state (including dependencies), the Helper activity is
	 * renamed to Builder and the model duration is changed. A backward idle confirms the new state
	 * is stable.<br/>
	 * <b>Expect</b>: Builder/modified-duration state is stable under backward idle.<br/>
	 * <b>Features</b>: roundtrip, fwd+bwd, rename, attribute
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testRoundtripRenameToBuilderThenIdleBwd(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
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
		// Forward: rename Helper→Builder and adjust model duration
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::changeGantt2CPMHelperToBuilder,
				helperGantt::changeGantt2CPMModelDuration));
		util.assertPostcondition("TestsBuilderMModel-ComparatorGantt", "TestsBuilderMModel-ComparatorCPM");
		// Backward idle: Builder/modified state must remain stable
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::idleDelta));
		util.assertPostcondition("TestsBuilderMModel-ComparatorGantt", "TestsBuilderMModel-ComparatorCPM");
	}
}
