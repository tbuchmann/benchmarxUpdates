package org.benchmarx.examples.pn2pnw.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pn.Net;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with a constant model size and a growing number of conflicting changes. Since
 * this example's genuine conflict ({@link Conflicts#testConcurrentRenameNetNameConflict})
 * is a single whole-net rename with no per-element identity to repeat, the
 * conflicting-change count is instead scaled by bundling n concurrent
 * incrementalID attribute toggles into a single concurrent sync step on both
 * sides - still exercising n atomic conflicting edit steps per measurement.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCSync extends ScalabilityTests {

	public static final int NR_OF_UNITS = 100;

	public ScalabilityConstModelCSync() { super("CMCSync_"); }

	public static Collection<BXTool<Net, pnw.Net, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createPTPUnitsAndConflictingChanges(int nrOfUnits, int nrOfEditedTimes) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedTimes, //
						timer.timeEditAfterSetUpInS(
								srcEdit(helperPn::renameToLettersAndDigits, () -> helperPn.createNPTPUnits(nrOfUnits, "P")),
								srcEdit(() -> helperPn.changeIncrementalIDNTimes(nrOfEditedTimes)),
								trgEdit(() -> helperPnw.changeIncrementalIDNTimes(nrOfEditedTimes))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAnd0000003ConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(NR_OF_UNITS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAnd0000005ConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(NR_OF_UNITS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAnd0000010ConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(NR_OF_UNITS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAnd0000020ConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(NR_OF_UNITS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAnd0000030ConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(NR_OF_UNITS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAnd0000040ConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(NR_OF_UNITS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAnd0000050ConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(NR_OF_UNITS, 50); }
}
