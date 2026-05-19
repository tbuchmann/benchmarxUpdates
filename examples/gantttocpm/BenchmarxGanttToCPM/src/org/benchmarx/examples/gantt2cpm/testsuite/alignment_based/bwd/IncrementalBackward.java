package org.benchmarx.examples.gantt2cpm.testsuite.alignment_based.bwd;

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

@ExtendWith(BXToolParameterResolver.class)
public class IncrementalBackward extends GanttToCPMTestCase {

	public IncrementalBackward() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for inserting new activities and events into an existing cpm network. <br/>
	 * <b>Expect</b> : New dependencies and activities are added to the gantt diagram, while the old dependencies and activities
	 * remain unchanged. <br/>
	 * <b>Features</b>: fwd, add, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInserts(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperCPM::createCPM2GanttTestCases,
				helperCPM::changeIncrementalID));
		tool.performIdleSourceEdit(srcEdit(helperGantt::changeIncrementalID));

		util.assertPrecondition("TestsGantt", "TestsCPM");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperCPM::addCPM2GanttHelpers,
				helperCPM::addCPM2GanttComparators,
				helperCPM::addCPM2GanttModels));
		//------------
		util.assertPostcondition("TestsHelperModelComparatorGantt", "TestsHelperModelComparatorCPM");
	}

	/**
	 * <b>Test</b> for deleting dependencies at first. After that two activites with their incoming dependencies will be deleted.
	 * <b>Expect</b>: Delete the correct events and activities in the gantt diagram.
	 * <b>Features</b>: fwd, del, corr-based, structural
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalDeletions(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperCPM::createCPM2GanttTestCases,
				helperCPM::addCPM2GanttHelpers,
				helperCPM::addCPM2GanttComparators,
				helperCPM::addCPM2GanttModels,
				helperCPM::addCPM2GanttModelsToComparatorDependencies,
				helperCPM::changeIncrementalID));
		tool.performIdleSourceEdit(srcEdit(helperGantt::changeIncrementalID));

		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		//Delete Dependency
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::deleteCPM2GanttModelsToComparatorDependencies));
		//------------
		util.assertPostcondition("TestsHelperModelComparatorGantt", "TestsHelperModelComparatorCPM");

		//Delete Activity
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::deleteCPM2GanttHelpers));
		//------------
		util.assertPostcondition("TestsModelComparatorGantt", "TestsModelComparatorCPM");
	}

	/**
	 * <b>Test</b> for changing values in different events and activities.
	 * <b>Expect</b>: Change the values of the affected variables in activities and dependencies of the gantt diagram.
	 * <b>Features</b>: fwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalValueChange(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperCPM::createCPM2GanttTestCases,
				helperCPM::addCPM2GanttHelpers,
				helperCPM::addCPM2GanttComparators,
				helperCPM::addCPM2GanttModels,
				helperCPM::addCPM2GanttModelsToComparatorDependencies,
				helperCPM::changeIncrementalID));
		tool.performIdleSourceEdit(srcEdit(helperGantt::changeIncrementalID));

		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperCPM::changeCPM2GanttHelperToBuilder,
				helperCPM::changeCPM2GanttModelDuration));
		//------------
		util.assertPostcondition("TestsBuilderMModel-ComparatorGantt", "TestsBuilderMModel-ComparatorCPM");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(
				helperCPM::changeCPM2GanttTestCasesNameDuration,
				helperCPM::changeCPM2GanttModelToComparatorDependencyTypeDurationTargetAndSource));
		//------------
		util.assertPostcondition("TestsBuilderModelComparatorModifiedGantt", "TestsBuilderModelComparatorModifiedCPM");
	}

	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle source delta does not change the source model.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testStability(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperCPM::createCPM2GanttTestCases,
				helperCPM::addCPM2GanttHelpers,
				helperCPM::addCPM2GanttComparators,
				helperCPM::addCPM2GanttModels,
				helperCPM::addCPM2GanttModelsToComparatorDependencies,
				helperCPM::changeIncrementalID));
		tool.performIdleSourceEdit(srcEdit(helperGantt::changeIncrementalID));

		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::idleDelta));
		//------------
		util.assertPostcondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
	}

	/**
	 * <b>Test</b> for hippocraticness of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after changing the incrementalID does not change the diagram.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testHipporcraticness(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateTargetEdit(trgEdit(
				helperCPM::createCPM2GanttTestCases,
				helperCPM::addCPM2GanttHelpers,
				helperCPM::addCPM2GanttComparators,
				helperCPM::addCPM2GanttModels,
				helperCPM::addCPM2GanttModelsToComparatorDependencies,
				helperCPM::changeIncrementalID));
		tool.performIdleSourceEdit(srcEdit(helperGantt::changeIncrementalID));

		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		//------------
		tool.performAndPropagateTargetEdit(trgEdit(helperCPM::changeIncrementalID));
		//------------
		util.assertPostcondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorChangedAgainCPM");
	}
}