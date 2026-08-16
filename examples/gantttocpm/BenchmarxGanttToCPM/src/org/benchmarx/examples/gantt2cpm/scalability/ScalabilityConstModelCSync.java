package org.benchmarx.examples.gantt2cpm.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.gantt2cpm.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.gantt2cpm.testsuite.Decisions;
import org.benchmarx.examples.gantt2cpm.testsuite.concurrent.Conflicts;
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
 * with a constant model size and a growing number of conflicting changes. Since
 * this example's genuine conflict ({@link Conflicts#testConcurrentRenameHelperConflict})
 * is a single whole-diagram rename with no per-element identity to repeat, the
 * conflicting-change count is instead scaled by bundling n concurrent
 * incrementalID attribute toggles into a single concurrent sync step on both
 * sides - still exercising n atomic conflicting edit steps per measurement.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCSync extends ScalabilityTests {

	public static final int NR_OF_ACTIVITIES = 100;

	public ScalabilityConstModelCSync() { super("CMCSync_"); }

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
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedTimes, //
						timer.timeEditAfterSetUpInS(
								srcEdit(helperGantt::createGantt2CPMTestCases, () -> helperGantt.createNActivities(nrOfActivities, "P")),
								srcEdit(() -> helperGantt.changeIncrementalIDNTimes(nrOfEditedTimes)),
								trgEdit(() -> helperCPM.changeIncrementalIDNTimes(nrOfEditedTimes))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAnd0000003ConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(NR_OF_ACTIVITIES, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAnd0000005ConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(NR_OF_ACTIVITIES, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAnd0000010ConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(NR_OF_ACTIVITIES, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAnd0000020ConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(NR_OF_ACTIVITIES, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAnd0000030ConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(NR_OF_ACTIVITIES, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAnd0000040ConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(NR_OF_ACTIVITIES, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateActivitiesAnd0000050ConflictingChanges(BXTool<GanttDiagram, CPMNetwork, Decisions> tool) { this.tool = tool; initialise(); createActivitiesAndConflictingChanges(NR_OF_ACTIVITIES, 50); }
}
