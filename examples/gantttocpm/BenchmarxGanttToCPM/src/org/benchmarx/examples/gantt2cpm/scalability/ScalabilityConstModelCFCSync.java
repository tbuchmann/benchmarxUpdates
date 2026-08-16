package org.benchmarx.examples.gantt2cpm.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.gantt2cpm.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.examples.gantt2cpm.testsuite.concurrent.MonotonicCreating;
import org.benchmarx.util.BXToolTimer;
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
 * with a constant model size and a growing number of conflict-free (CF)
 * changes. A conflict-free change consists of the source adding new
 * independent activities while the target stays idle (see
 * {@link MonotonicCreating#testConcurrentSourceBulkAddTargetIdle}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCFCSync extends ScalabilityTests {

	public static final int NR_OF_ACTIVITIES = 100;

	public ScalabilityConstModelCFCSync() { super("CMCFCSync_"); }

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
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedActivities, //
						timer.timeEditAfterSetUpInS(
								srcEdit(helperGantt::createGantt2CPMTestCases, () -> helperGantt.createNActivities(nrOfActivities, "P")),
								srcEdit(() -> helperGantt.createNActivities(nrOfEditedActivities, "X")),
								trgEdit(helperCPM::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000003ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000005ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000010ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000020ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000030ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000040ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000050ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 50); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000060ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 60); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000070ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 70); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000080ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 80); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000090ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 90); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAndCreate0000100ConflictFreeChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictFreeChanges(NR_OF_ACTIVITIES, 100); }
}
