package org.benchmarx.examples.ecore2sql.scalability;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.Collection;

import org.benchmarx.BXTool;
import org.benchmarx.examples.ecore2sql.testsuite.BXToolParameterResolver;
import org.benchmarx.examples.ecore2sql.testsuite.Decisions;
import org.benchmarx.examples.ecore2sql.testsuite.concurrent.Conflicts;
import org.benchmarx.util.BXToolTimer;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sql.Schema;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with a constant model size and a growing number of conflicting changes. Unlike
 * pntopnw/gantttocpm/asttodag, this example's real conflict
 * ({@link Conflicts#testConcurrentRenameListLengthConflict}) has a natural
 * per-element identity to repeat (independently-named List classes), so the
 * conflicting-change count is scaled literally across n of the seeded classes
 * rather than via a stand-in axis.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCSync extends ScalabilityTests {

	public static final int NR_OF_LISTS = 100;

	public ScalabilityConstModelCSync() { super("CMCSync_"); }

	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createListsAndConflictingChanges(int nrOfLists, int nrOfEditedLists) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedLists, //
						timer.timeEditAfterSetUpInS(
								srcEdit(() -> helperEcore.createNListsWithLength(nrOfLists, "List")),
								srcEdit(() -> helperEcore.renameNListLengthAttributes(nrOfEditedLists, "List")),
								trgEdit(() -> helperSQL.renameNListLengthColumns(nrOfEditedLists, "List"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAnd0000003ConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(NR_OF_LISTS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAnd0000005ConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(NR_OF_LISTS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAnd0000010ConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(NR_OF_LISTS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAnd0000020ConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(NR_OF_LISTS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAnd0000030ConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(NR_OF_LISTS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAnd0000040ConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(NR_OF_LISTS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAnd0000050ConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(NR_OF_LISTS, 50); }
}
