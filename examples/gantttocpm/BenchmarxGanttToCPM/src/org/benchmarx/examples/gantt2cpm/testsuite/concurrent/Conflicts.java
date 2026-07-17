package org.benchmarx.examples.gantt2cpm.testsuite.concurrent;

import static java.util.Map.entry;

import java.util.Collection;
import java.util.Map;

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
 * Concurrent conflict tests for Gantt-to-CPM: both sides edit the same element
 * incompatibly in the same concurrent step. Resolution is tool-dependent, so
 * {@code util.assertAnyPostcondition} is used to accept every outcome the tool could
 * reasonably produce.
 * Note: {@code terminate()} is called automatically by {@code @AfterEach}; do not call it manually.
 */
@ExtendWith(BXToolParameterResolver.class)
public class Conflicts extends GanttToCPMTestCase {

	public Conflicts() {
		super();
	}

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return GanttToCPMTestCase.tools();
	}

	/**
	 * <b>Test</b> for a conflict on the "GanttHelper" activity name: source renames it to
	 * "CPMBuilder" (as part of {@code changeGantt2CPMHelperToBuilder}) while target
	 * independently renames the corresponding CPM activity to "Alternativname" in the
	 * same concurrent step (CF-HelperRename).<br/>
	 * <b>Expect</b>: The tool resolves the naming conflict one way or another; both
	 * models end up consistent with whichever side won.<br/>
	 * <b>Features</b>: concurrent, conflict, rename
	 */
	@ParameterizedTest
	@MethodSource("tools")
	public void testConcurrentRenameHelperConflict(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) {
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
		util.assertPrecondition("ConflictsPreGantt", "ConflictsPreCPM");
		// Concurrent: SRC renames "GanttHelper" to "CPMBuilder" (as part of a larger
		// rename); TRG independently renames the same corresponding activity to
		// "Alternativname" - a genuine incompatible edit to the same element.
		tool.performAndPropagateEdit(
				srcEdit(helperGantt::changeGantt2CPMHelperToBuilder),
				trgEdit(helperCPM::renameHelperToAlternative));
		// Source-wins was captured directly from a real BXAgent run via tool.saveModels(...).
		// Target-wins is not independently observed, but was derived from that verified
		// structure by substituting only the contested activity's name (GanttHelper's
		// name; CPMHelper was untouched by TRG so it stays at SRC's "GanttBuilder" either
		// way) - both are accepted since target-wins is just as valid a conflict
		// resolution policy as source-wins.
		util.assertAnyPostcondition(Map.ofEntries(
				entry("ConflictsHelperSrcWinsGantt", "ConflictsHelperSrcWinsCPM"),
				entry("ConflictsHelperTrgWinsGantt", "ConflictsHelperTrgWinsCPM")));
	}
}
