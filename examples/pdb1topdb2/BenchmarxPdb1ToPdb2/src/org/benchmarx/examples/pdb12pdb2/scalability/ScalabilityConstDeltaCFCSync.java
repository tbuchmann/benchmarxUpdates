package org.benchmarx.examples.pdb12pdb2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.concurrent.MonotonicCreating;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pdb1.Database;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflict-free (CF)
 * changes (see {@link MonotonicCreating#testNonMatchingCreate}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCFCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_PERSONS = 3;

	public ScalabilityConstDeltaCFCSync() { super("CDCFCSync_"); }

	public static Collection<BXTool<Database, pdb2.Database, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createPersonsAndConflictFreeChanges(int nrOfPersons, int nrOfEditedPersons) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareSourceEditAfterSetUp(srcEdit(helperPerson1::setDatabaseName, () -> helperPerson1.createNPersons(nrOfPersons, "P")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfPersons,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperPerson1.createNPersons(nrOfEditedPersons, "S_")),
						trgEdit(() -> helperPerson2.createNPersons(nrOfEditedPersons, "T_"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(3, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(5, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(10, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(20, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(30, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(40, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(50, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000060PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(60, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000070PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(70, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000080PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(80, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000090PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(90, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(100, NR_OF_EDITED_PERSONS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(1000, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(5000, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(10000, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(50000, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(100000, NR_OF_EDITED_PERSONS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(500000, NR_OF_EDITED_PERSONS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000PersonsAndConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(1000000, NR_OF_EDITED_PERSONS); }
}
