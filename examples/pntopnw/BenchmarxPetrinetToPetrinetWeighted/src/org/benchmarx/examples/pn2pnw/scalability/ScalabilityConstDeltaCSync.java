package org.benchmarx.examples.pn2pnw.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pn.Net;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflicting changes
 * (see {@link Conflicts#testConcurrentRenameNetNameConflict} and
 * {@link ScalabilityConstModelCSync} for the scaling rationale).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_TIMES = 3;

	public ScalabilityConstDeltaCSync() { super("CDCsync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(helperPn::renameToLettersAndDigits, () -> helperPn.createNPTPUnits(nrOfUnits, "P")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfUnits,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperPn.changeIncrementalIDNTimes(nrOfEditedTimes)),
						trgEdit(() -> helperPnw.changeIncrementalIDNTimes(nrOfEditedTimes))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(3, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(5, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(10, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(20, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(30, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(40, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(50, NR_OF_EDITED_TIMES); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(1000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(5000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(10000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(50000, NR_OF_EDITED_TIMES); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(100000, NR_OF_EDITED_TIMES); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(500000, NR_OF_EDITED_TIMES); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000PTPUnitsAndConflictingChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictingChanges(1000000, NR_OF_EDITED_TIMES); }
}
