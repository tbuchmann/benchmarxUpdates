package org.benchmarx.examples.pdb12pdb2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.concurrent.Conflicts;
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
 * with an increasing model size and a constant number of conflicting changes
 * (see {@link Conflicts#testDeleteRenameConflict}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_PERSONS = 3;

	public ScalabilityConstDeltaCSync() { super("CDCsync_"); }

	public static Collection<BXTool<Database, pdb2.Database, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createPersonsAndConflictingChanges(int nrOfPersons, int nrOfEditedPersons) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		timer.prepareSourceEditAfterSetUp(srcEdit(helperPerson1::setDatabaseName, () -> helperPerson1.createNPersons(nrOfPersons, "P")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfPersons,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperPerson1.deleteNPersons(nrOfEditedPersons, "P")),
						trgEdit(() -> helperPerson2.changeBirthdayOfNPersons(nrOfEditedPersons, "P"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(3, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(5, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(10, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(20, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(30, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(40, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(50, NR_OF_EDITED_PERSONS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(1000, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(5000, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(10000, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(50000, NR_OF_EDITED_PERSONS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(100000, NR_OF_EDITED_PERSONS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(500000, NR_OF_EDITED_PERSONS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000PersonsAndConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(1000000, NR_OF_EDITED_PERSONS); }
}
