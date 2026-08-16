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
 * with an increasing model size and a constant number of conflict-free (CF)
 * changes (see {@link Conflicts#testMonotonicCreating} for the independent-add
 * rationale used here, since this example has no {@code MonotonicCreating.java}).
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
@ExtendWith(BXToolParameterResolver.class)
public class ScalabilityConstDeltaCFCSync extends ScalabilityTests {

	public static final int NR_OF_EDITED_LISTS = 3;

	public ScalabilityConstDeltaCFCSync() { super("CDCFCSync_"); }

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
		timer.prepareSourceEditAfterSetUp(srcEdit(() -> helperEcore.createNListsWithLength(nrOfLists, "List")));
		try {
			assertTimeoutPreemptively(Duration.ofSeconds(TIMEOUT), () -> {
				recordResult(tool, nrOfLists,
						timer.measureEditAfterSetUpInS(
						srcEdit(() -> helperEcore.addNDataElementFeatures(nrOfEditedLists, "List")),
						trgEdit(helperSQL::idleDelta)));
			});
		} catch (Throwable t) {
			timedOut.add(tool.getName());
			throw t;
		}
	}

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000003ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(3, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000005ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(5, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000010ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(10, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000020ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(20, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000030ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(30, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000040ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(40, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000050ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(50, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000060ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(60, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000070ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(70, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000080ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(80, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000090ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(90, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0000100ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(100, NR_OF_EDITED_LISTS); }
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0001000ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(1000, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0005000ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(5000, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0010000ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(10000, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0050000ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(50000, NR_OF_EDITED_LISTS); }

	@ParameterizedTest @MethodSource("tools")
	public void testCreate0100000ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(100000, NR_OF_EDITED_LISTS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate0500000ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(500000, NR_OF_EDITED_LISTS); }

	@Disabled
	@ParameterizedTest @MethodSource("tools")
	public void testCreate1000000ListsAndConflictFreeChanges(BXTool<EPackage, Schema, Decisions> tool) { this.tool = tool; initialise(); createListsAndConflictFreeChanges(1000000, NR_OF_EDITED_LISTS); }
}
