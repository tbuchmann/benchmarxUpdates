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
 * with a constant model size and a growing number of conflict-free (CF)
 * changes. A conflict-free change consists of the source adding new
 * independent attributes to already-seeded classes while the target stays
 * idle. This example has no {@code MonotonicCreating.java} to mirror, so this
 * is instead based on the independent-add pattern used inside
 * {@link Conflicts#testMonotonicCreating} / {@code IncrementalForward}.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstModelCFCSync extends ScalabilityTests {

	public static final int NR_OF_LISTS = 100;

	public ScalabilityConstModelCFCSync() { super("CMCFCSync_"); }

	public static Collection<BXTool<EPackage, Schema, Decisions>> tools() {
		return ScalabilityTests.tools();
	}

	@BeforeAll
	static void setup() {
		initResults();
	}

	private void createListsAndConflictFreeChanges(int nrOfLists, int nrOfEditedLists) {
		org.junit.jupiter.api.Assumptions.assumeFalse(timedOut.contains(tool.getName()),
				() -> tool.getName() + " already timed out at a smaller size in this class");
		var timer = new BXToolTimer<>(tool, REPEAT);
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfEditedLists, //
						timer.timeEditAfterSetUpInS(
								srcEdit(() -> helperEcore.createNListsWithLength(nrOfLists, "List")),
								srcEdit(() -> helperEcore.addNDataElementFeatures(nrOfEditedLists, "List")),
								trgEdit(helperSQL::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000003ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 3); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000005ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 5); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000010ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 10); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000020ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 20); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000030ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 30); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000040ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 40); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000050ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 50); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000060ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 60); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000070ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 70); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000080ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 80); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000090ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 90); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreateListsAndCreate0000100ConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(NR_OF_LISTS, 100); }
}
