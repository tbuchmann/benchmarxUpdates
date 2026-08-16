package org.benchmarx.examples.pn2pnw.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.concurrent.MonotonicCreating;
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
 * with a constant model size and a growing number of conflict-free (CF)
 * changes. A conflict-free change consists of the source adding new
 * independent PTP units while the target stays idle (see
 * {@link MonotonicCreating#testConcurrentSourceExtendTargetIdle}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCFCSync extends ScalabilityTests {

	public static final int NR_OF_UNITS = 100;

	public ScalabilityConstModelCFCSync() { super("CMCFCSync_"); }

	public static Collection<BXTool<Net, pnw.Net, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createPTPUnitsAndConflictFreeChanges(int nrOfUnits, int nrOfEditedUnits) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedUnits, //
						timer.timeEditAfterSetUpInS(
								srcEdit(helperPn::renameToLettersAndDigits, () -> helperPn.createNPTPUnits(nrOfUnits, "P")),
								srcEdit(() -> helperPn.createNPTPUnits(nrOfEditedUnits, "X")),
								trgEdit(helperPnw::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000003ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000005ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000010ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000020ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000030ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000040ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000050ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 50); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000060ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 60); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000070ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 70); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000080ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 80); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000090ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 90); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePTPUnitsAndCreate0000100ConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(NR_OF_UNITS, 100); }
}
