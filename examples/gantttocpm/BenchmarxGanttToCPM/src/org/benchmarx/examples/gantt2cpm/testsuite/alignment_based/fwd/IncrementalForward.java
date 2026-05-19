package org.benchmarx.examples.gantt2cpm.testsuite.alignment_based.fwd;

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
public class IncrementalForward extends GanttToCPMTestCase {

	public IncrementalForward() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for inserting new activities and dependencies into an existing gantt diagram. <br/>
	 * <b>Expect</b> : New events and activities are added to the cpm network, while the old events and activities
	 * remain unchanged. <br/>
	 * <b>Features</b>: fwd, add, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalInserts(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::createGantt2CPMTestCases,
				helperGantt::changeIncrementalID));
		tool.performIdleTargetEdit(trgEdit(helperCPM::changeIncrementalID));

		util.assertPrecondition("TestsGantt", "TestsCPM");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::addGantt2CPMHelpers,
				helperGantt::addGantt2CPMComparators,
				helperGantt::addGantt2CPMModels));
		//------------
		util.assertPostcondition("TestsHelperModelComparatorGantt", "TestsHelperModelComparatorCPM");
	}

	/**
	 * <b>Test</b> for deleting dependencies at first. After that two activites with their incoming dependencies will be deleted.
	 * <b>Expect</b>: Delete the correct events and activities in the cpm network
	 * <b>Features</b>: fwd, del, corr-based, structural
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalDeletions(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::createGantt2CPMTestCases,
				helperGantt::addGantt2CPMHelpers,
				helperGantt::addGantt2CPMComparators,
				helperGantt::addGantt2CPMModels,
				helperGantt::addGantt2CPMModelsToComparatorDependencies,
				helperGantt::changeIncrementalID));
		tool.performIdleTargetEdit(trgEdit(helperCPM::changeIncrementalID));

		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		//Delete Dependency
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperGantt::deleteGantt2CPMModelsToComparatorDependencies));
		//------------
		util.assertPostcondition("TestsHelperModelComparatorGantt", "TestsHelperModelComparatorCPM");

		//Delete Activity
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperGantt::deleteGantt2CPMHelpers));
		//------------
		util.assertPostcondition("TestsModelComparatorGantt", "TestsModelComparatorCPM");
	}

	/**
	 * <b>Test</b> for changing all variable-values in different persons. After creating the pdb2 database.
	 * Then change values of each variable in another person and all variables of one person.
	 * <b>Expect</b>: Change the values of the affected variables in Persons of the pdb2 database.
	 * <b>Features</b>: fwd, attribute, fixed, structural, corr-based
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testIncrementalValueChange(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::createGantt2CPMTestCases,
				helperGantt::addGantt2CPMHelpers,
				helperGantt::addGantt2CPMComparators,
				helperGantt::addGantt2CPMModels,
				helperGantt::addGantt2CPMModelsToComparatorDependencies,
				helperGantt::changeIncrementalID));
		tool.performIdleTargetEdit(trgEdit(helperCPM::changeIncrementalID));

		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::changeGantt2CPMHelperToBuilder,
				helperGantt::changeGantt2CPMModelDuration));
		//------------
		util.assertPostcondition("TestsBuilderMModel-ComparatorGantt", "TestsBuilderMModel-ComparatorCPM");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::changeGantt2CPMTestCasesNameDuration,
				helperGantt::changeGantt2CPMModelToComparatorDependencyTypeDurationTargetAndSource));
		//------------
		util.assertPostcondition("TestsBuilderModelComparatorModifiedGantt", "TestsBuilderModelComparatorModifiedCPM");
	}

	/**
	 * <b>Test</b> for stability of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after an idle source delta does not change the target model.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testStability(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::createGantt2CPMTestCases,
				helperGantt::addGantt2CPMHelpers,
				helperGantt::addGantt2CPMComparators,
				helperGantt::addGantt2CPMModels,
				helperGantt::addGantt2CPMModelsToComparatorDependencies,
				helperGantt::changeIncrementalID));
		tool.performIdleTargetEdit(trgEdit(helperCPM::changeIncrementalID));

		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperGantt::idleDelta));
		//------------
		util.assertPostcondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
	}

	/**
	 * <b>Test</b> for hippocraticness of the transformation.<br/>
	 * <b>Expect</b> re-running the transformation after changing the incrementalID does not change the network.<br/>
	 * <b>Features:</b>: fwd, fixed
	 */
	@ParameterizedTest @MethodSource("tools")
	public void testHippocraticness(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
		this.tool = tool; initialise();
		tool.performAndPropagateSourceEdit(srcEdit(
				helperGantt::createGantt2CPMTestCases,
				helperGantt::addGantt2CPMHelpers,
				helperGantt::addGantt2CPMComparators,
				helperGantt::addGantt2CPMModels,
				helperGantt::addGantt2CPMModelsToComparatorDependencies,
				helperGantt::changeIncrementalID));
		tool.performIdleTargetEdit(trgEdit(helperCPM::changeIncrementalID));

		util.assertPrecondition("TestsHelperModel-ComparatorGantt", "TestsHelperModel-ComparatorCPM");
		//------------
		tool.performAndPropagateSourceEdit(srcEdit(helperGantt::changeIncrementalID));
		//------------
		util.assertPostcondition("TestsHelperModel-ComparatorChangedAgainGantt", "TestsHelperModel-ComparatorCPM");
	}
}