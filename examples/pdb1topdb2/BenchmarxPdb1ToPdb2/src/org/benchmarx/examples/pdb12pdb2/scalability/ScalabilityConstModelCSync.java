package org.benchmarx.examples.pdb12pdb2.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.pdb12pdb2.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.pdb12pdb2.testsuite.Decisions;
import org.benchmarx.examples.pdb12pdb2.testsuite.concurrent.Conflicts;
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
 * with a constant model size and a growing number of conflicting changes. A
 * conflicting change consists of deleting one person on the source side while
 * concurrently changing that same person's birthday on the target side (see
 * {@link Conflicts#testDeleteRenameConflict}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCSync extends ScalabilityTests {

	public static final int NR_OF_PERSONS = 100;

	public ScalabilityConstModelCSync() { super("CMCSync_"); }

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
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedPersons, //
						timer.timeEditAfterSetUpInS(
								srcEdit(helperPerson1::setDatabaseName, () -> helperPerson1.createNPersons(nrOfPersons, "P")),
								srcEdit(() -> helperPerson1.deleteNPersons(nrOfEditedPersons, "P")),
								trgEdit(() -> helperPerson2.changeBirthdayOfNPersons(nrOfEditedPersons, "P"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAnd0000003ConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(NR_OF_PERSONS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAnd0000005ConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(NR_OF_PERSONS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAnd0000010ConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(NR_OF_PERSONS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAnd0000020ConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(NR_OF_PERSONS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAnd0000030ConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(NR_OF_PERSONS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAnd0000040ConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(NR_OF_PERSONS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreatePersonsAnd0000050ConflictingChanges(BXTool<Database, pdb2.Database, Decisions> tool) { this.tool = tool; initialise(); createPersonsAndConflictingChanges(NR_OF_PERSONS, 50); }
}
