package org.benchmarx.examples.pn2pnw.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pn2pnw.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pn2pnw.testsuite.Decisions;
import org.benchmarx.examples.pn2pnw.testsuite.concurrent.MonotonicCreating;
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
 * with an increasing model size and a constant number of conflict-free (CF)
 * changes (see {@link MonotonicCreating#testConcurrentSourceExtendTargetIdle}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCFCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_UNITS = 3;

	public ScalabilityConstDeltaCFCSync() { super("CDCFCSync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(helperPn::renameToLettersAndDigits, () -> helperPn.createNPTPUnits(nrOfUnits, "P")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfUnits,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperPn.createNPTPUnits(nrOfEditedUnits, "X")),
						trgEdit(helperPnw::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(3, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(5, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(10, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(20, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(30, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(40, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(50, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000060PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(60, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000070PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(70, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000080PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(80, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000090PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(90, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(100, NR_OF_EDITED_UNITS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(1000, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(5000, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(10000, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(50000, NR_OF_EDITED_UNITS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(100000, NR_OF_EDITED_UNITS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(500000, NR_OF_EDITED_UNITS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000PTPUnitsAndConflictFreeChanges(BXTool<Net, pnw.Net, Decisions> tool) { this.tool = tool; initialise(); createPTPUnitsAndConflictFreeChanges(1000000, NR_OF_EDITED_UNITS); }
}
