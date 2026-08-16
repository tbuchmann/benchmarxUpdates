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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import sql.Schema;

/**
 * This class implements scalability tests for concurrent synchronisation cases
 * with an increasing model size and a constant number of conflicting changes
 * (see {@link Conflicts#testConcurrentRenameListLengthConflict} and
 * {@link ScalabilityConstModelCSync} for the scaling rationale).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_LISTS = 3;

	public ScalabilityConstDeltaCSync() { super("CDCSync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(() -> helperEcore.createNListsWithLength(nrOfLists, "List")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfLists,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperEcore.renameNListLengthAttributes(nrOfEditedLists, "List")),
						trgEdit(() -> helperSQL.renameNListLengthColumns(nrOfEditedLists, "List"))));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(3, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(5, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(10, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(20, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(30, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(40, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(50, NR_OF_EDITED_LISTS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(1000, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(5000, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(10000, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(50000, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(100000, NR_OF_EDITED_LISTS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(500000, NR_OF_EDITED_LISTS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000ListsAndConflictingChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictingChanges(1000000, NR_OF_EDITED_LISTS); }
}
