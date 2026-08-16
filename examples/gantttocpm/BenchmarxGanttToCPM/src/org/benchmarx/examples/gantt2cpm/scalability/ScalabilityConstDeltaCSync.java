package org.benchmarx.examples.gantt2cpm.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.gantt2cpm.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.examples.gantt2cpm.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cpm.CPMNetwork;
import gantt.GanttDiagram;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflicting changes
 * (see {@link Conflicts#testConcurrentRenameHelperConflict} and
 * {@link ScalabilityConstModelCSync} for the scaling rationale).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_TIMES = 3;

	public ScalabilityConstDeltaCSync() { super("CDCSync_"); }

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createActivitiesAndConflictingChanges(int nrOfActivities, int nrOfEditedTimes) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareSourceEditAfterSetUp(srcEdit(helperGantt::createGantt2CPMTestCases, () -> helperGantt.createNActivities(nrOfActivities, "P")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfActivities,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperGantt.changeIncrementalIDNTimes(nrOfEditedTimes)),
						trgEdit(() -> helperCPM.changeIncrementalIDNTimes(nrOfEditedTimes))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(3, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(5, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(10, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(20, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(30, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(40, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(50, NR_OF_EDITED_TIMES); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(1000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(5000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(10000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(50000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(100000, NR_OF_EDITED_TIMES); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(500000, NR_OF_EDITED_TIMES); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000ActivitiesAndConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(1000000, NR_OF_EDITED_TIMES); }
}
