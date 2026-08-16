package org.benchmarx.examples.gantt2cpm.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.gantt2cpm.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.examples.gantt2cpm.testsuite.concurrent.MonotonicCreating;
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
 * with an increasing model size and a constant number of conflict-free (CF)
 * changes (see {@link MonotonicCreating#testConcurrentSourceBulkAddTargetIdle}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCFCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_ACTIVITIES = 3;

	public ScalabilityConstDeltaCFCSync() { super("CDCFCSync_"); }

	public static Collection<BXTool<GanttDiagram, CPMNetwork, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createActivitiesAndConflictFreeChanges(int nrOfActivities, int nrOfEditedActivities) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareSourceEditAfterSetUp(srcEdit(helperGantt::createGantt2CPMTestCases, () -> helperGantt.createNActivities(nrOfActivities, "P")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfActivities,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperGantt.createNActivities(nrOfEditedActivities, "X")),
						trgEdit(helperCPM::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(3, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(5, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(10, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(20, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(30, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(40, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(50, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000060ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(60, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000070ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(70, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000080ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(80, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000090ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(90, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(100, NR_OF_EDITED_ACTIVITIES); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(1000, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(5000, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(10000, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(50000, NR_OF_EDITED_ACTIVITIES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(100000, NR_OF_EDITED_ACTIVITIES); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(500000, NR_OF_EDITED_ACTIVITIES); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000ActivitiesAndConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(1000000, NR_OF_EDITED_ACTIVITIES); }
}
