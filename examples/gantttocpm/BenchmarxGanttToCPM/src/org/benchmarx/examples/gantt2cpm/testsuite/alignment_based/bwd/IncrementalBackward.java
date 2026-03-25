package org.benchmarx.examples.gantt2cpm.testsuite.alignment_based.bwd;

import org.benchmarx.BXTool;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.examples.gantt2cpm.testsuite.GanttToCPMTestCase;
import org.junit.Test;

import cpm.CPMNetwork;
import gantt.GanttDiagram;

public class IncrementalBackward extends GanttToCPMTestCase {
	public IncrementalBackward(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		super(tool);
	}
	
	/**
	 * <b>Test</b> for inserting new activities and events into an existing cpm network. <br/>
	 * <b>Expect</b> : New dependencies and activities are added to the gantt diagram, while the old dependencies and activities
	 * remain unchanged. <br/>
	 * <b>Features</b>: fwd, add, fixed
	 */
	@Test
	public void testIncrementalInserts() {
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
	@Test
	public void testIncrementalDeletions() {
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
	@Test
	public void testIncrementalValueChange() {
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
	@Test
	public void testStability() {		
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
	@Test
	public void testHipporcraticness() {
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
