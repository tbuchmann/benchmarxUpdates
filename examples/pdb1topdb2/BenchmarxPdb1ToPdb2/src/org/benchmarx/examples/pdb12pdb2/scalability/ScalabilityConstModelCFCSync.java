package org.benchmarx.examples.pdb12pdb2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.concurrent.MonotonicCreating;
import org.benchmarx.util.BXToolTimer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import pdb1.Database;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with a constant model size and a growing number of conflict-free (CF)
 * changes. A conflict-free change consists of creating disjoint sets of new
 * persons on the source and target sides (see
 * {@link MonotonicCreating#testNonMatchingCreate}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCFCSync extends ScalabilityTests {

	public static final int NR_OF_PERSONS = 100;

	public ScalabilityConstModelCFCSync() { super("CMCFCSync_"); }

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
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedPersons, //
						timer.timeEditAfterSetUpInS(
								srcEdit(helperPerson1::setDatabaseName, () -> helperPerson1.createNPersons(nrOfPersons, "P")),
								srcEdit(() -> helperPerson1.createNPersons(nrOfEditedPersons, "S_")),
								trgEdit(() -> helperPerson2.createNPersons(nrOfEditedPersons, "T_"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000003ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000005ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000010ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000020ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000030ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000040ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000050ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 50); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000060ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 60); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000070ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 70); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000080ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 80); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000090ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 90); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAndCreate0000100ConflictFreeChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictFreeChanges(NR_OF_PERSONS, 100); }
}
